package red.tel.chat.camera;

import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import red.tel.chat.utils.AspectRatio;
import red.tel.chat.utils.Size;

/**
 * Created by vmodev on 9/6/17.
 */

public class CameraAndroid extends CameraController implements Camera.PreviewCallback {
    private static final String TAG = CameraAndroid.class.getSimpleName();

    private int mCameraId;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private File mVideoFile;

    private int mSensorOffset;

    private final int mPostFocusResetDelay = 3000;
    private Runnable mPostFocusResetRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCameraOpened()) return;
            mCamera.cancelAutoFocus();
            synchronized (mLock) {
                Camera.Parameters params = mCamera.getParameters();
                params.setFocusAreas(null);
                params.setMeteringAreas(null);
                applyDefaultFocus(params); // Revert to internal focus.
                mCamera.setParameters(params);
            }
        }
    };

    private Mapper mMapper = new Mapper.Mapper1();
    private boolean mIsSetup = false;
    private boolean mIsCapturingImage = false;
    private boolean mIsCapturingVideo = false;
    private final Object mLock = new Object();

    CameraAndroid(CameraView.CameraCallbacks callback, Preview preview) {
        super(callback, preview);
    }

    @Override
    public void onSurfaceAvailable() {
        if (shouldSetup()) {
            setup();
        }
    }

    @Override
    public void onSurfaceChanged() {
        if (mIsSetup) {
            Size newSize = computePreviewSize();
            if (!newSize.equals(mPreviewSize)) {
                mPreviewSize = newSize;
                mCameraCallbacks.onCameraPreviewSizeChanged();
                synchronized (mLock) {
                    mCamera.stopPreview();
                    Camera.Parameters params = mCamera.getParameters();
                    params.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    mCamera.setParameters(params);
                }
                boolean invertPreviewSizes = shouldFlipSizes();
                mPreview.setDesiredSize(
                        invertPreviewSizes ? mPreviewSize.getHeight() : mPreviewSize.getWidth(),
                        invertPreviewSizes ? mPreviewSize.getWidth() : mPreviewSize.getHeight()
                );
                mCamera.startPreview();
            }
        }
    }

    @WorkerThread
    @Override
    void onStart() {
        if (isCameraOpened()) onStop();
        if (collectCameraId()) {
            mCamera = Camera.open(mCameraId);

            // Set parameters that might have been set before the camera was opened.
            synchronized (mLock) {
                Camera.Parameters params = mCamera.getParameters();
                mExtraProperties = new ExtraProperties(params);
                mOptions = new CameraOptions(params);
                applyDefaultFocus(params);
                mergeFlash(params, Flash.DEFAULT);
                params.setRecordingHint(true);
                mCamera.setParameters(params);
            }

            // Try starting preview.
            mCamera.setDisplayOrientation(computeSensorToDisplayOffset()); // <- not allowed during preview
            if (shouldSetup()) setup();
            mCameraCallbacks.dispatchOnCameraOpened(mOptions);
        }
    }

    @WorkerThread
    @Override
    void onStop() {
        mHandler.get().removeCallbacks(mPostFocusResetRunnable);
        if (isCameraOpened()) {
            if (mIsCapturingVideo) endVideo();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCameraCallbacks.dispatchOnCameraClosed();
        }
        mExtraProperties = null;
        mOptions = null;
        mCamera = null;
        mPreviewSize = null;
        mCaptureSize = null;
        mIsSetup = false;
    }

    @Override
    boolean setZoom(float zoom) {
        return false;
    }

    @Override
    boolean setExposureCorrection(float EVvalue) {
        return false;
    }

    @Override
    void setFacing(Facing facing) {
        if (facing != mFacing) {
            mFacing = facing;
            if (collectCameraId() && isCameraOpened()) {
                start();
            }
        }
    }

    @Override
    void setFlash(Flash flash) {
        Flash old = mFlash;
        mFlash = flash;
        if (isCameraOpened()) {
            synchronized (mLock) {
                Camera.Parameters params = mCamera.getParameters();
                if (mergeFlash(params, old)) mCamera.setParameters(params);
            }
        }
    }

    @Override
    void setVideoQuality(VideoQuality videoQuality) {
        if (mIsCapturingVideo) {
            throw new IllegalStateException("Can't change video quality while recording a video.");
        }

        mVideoQuality = videoQuality;
        if (isCameraOpened()) {
            // Change capture size to a size that fits the video aspect ratio.
            Size oldSize = mCaptureSize;
            mCaptureSize = computeCaptureSize();
            if (!mCaptureSize.equals(oldSize)) {
                // New video quality triggers a new aspect ratio.
                // Go on and see if preview size should change also.
                synchronized (mLock) {
                    Camera.Parameters params = mCamera.getParameters();
                    params.setPictureSize(mCaptureSize.getWidth(), mCaptureSize.getHeight());
                    mCamera.setParameters(params);
                }
                onSurfaceChanged();
            }
            Log.e(TAG, "captureSize: "+mCaptureSize);
            Log.e(TAG, "previewSize: "+mPreviewSize);
        }
    }

    @Override
    boolean startVideo(@NonNull byte[] data) {
        return false;
    }

    @Override
    boolean endVideo() {
        if (mIsCapturingVideo) {
            mIsCapturingVideo = false;
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            if (mVideoFile != null) {
                mCameraCallbacks.dispatchOnVideoTaken(mVideoFile);
                mVideoFile = null;
            }
            return true;
        }
        return false;
    }

    @Override
    boolean shouldFlipSizes() {
        return mSensorOffset % 180 != 0;
    }

    @Override
    boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    boolean startAutoFocus(@Nullable Gesture gesture, PointF point) {
        if (!isCameraOpened()) return false;
        if (!mOptions.isAutoFocusSupported()) return false;
        final PointF p = new PointF(point.x, point.y); // copy.
        List<Camera.Area> meteringAreas2 = computeMeteringAreas(p.x, p.y);
        List<Camera.Area> meteringAreas1 = meteringAreas2.subList(0, 1);
        synchronized (mLock) {
            // At this point we are sure that camera supports auto focus... right? Look at CameraView.onTouchEvent().
            Camera.Parameters params = mCamera.getParameters();
            int maxAF = params.getMaxNumFocusAreas();
            int maxAE = params.getMaxNumMeteringAreas();
            if (maxAF > 0) params.setFocusAreas(maxAF > 1 ? meteringAreas2 : meteringAreas1);
            if (maxAE > 0) params.setMeteringAreas(maxAE > 1 ? meteringAreas2 : meteringAreas1);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCameraCallbacks.dispatchOnFocusStart(gesture, p);
            // TODO this is not guaranteed to be called... Fix.
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // TODO lock auto exposure and white balance for a while
                    mCameraCallbacks.dispatchOnFocusEnd(gesture, success, p);
                    mHandler.get().removeCallbacks(mPostFocusResetRunnable);
                    mHandler.get().postDelayed(mPostFocusResetRunnable, mPostFocusResetDelay);
                }
            });
        }
        return true;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Log.d(TAG, "onPreviewFrame: " + Arrays.toString(bytes));
    }

    /////=========================================================================================

    private List<Camera.Area> computeMeteringAreas(double viewClickX, double viewClickY) {
        // Event came in view coordinates. We must rotate to sensor coordinates.
        // First, rescale to the -1000 ... 1000 range.
        int displayToSensor = -computeSensorToDisplayOffset();
        double viewWidth = mPreview.getView().getWidth();
        double viewHeight = mPreview.getView().getHeight();
        viewClickX = -1000d + (viewClickX / viewWidth) * 2000d;
        viewClickY = -1000d + (viewClickY / viewHeight) * 2000d;

        // Apply rotation to this point.
        // https://academo.org/demos/rotation-about-point/
        double theta = ((double) displayToSensor) * Math.PI / 180;
        double sensorClickX = viewClickX * Math.cos(theta) - viewClickY * Math.sin(theta);
        double sensorClickY = viewClickX * Math.sin(theta) + viewClickY * Math.cos(theta);
        // Log.e(TAG, "viewClickX:"+viewClickX+", viewClickY:"+viewClickY);
        // Log.e(TAG, "sensorClickX:"+sensorClickX+", sensorClickY:"+sensorClickY);

        // Compute the rect bounds.
        Rect rect1 = computeMeteringArea(sensorClickX, sensorClickY, 150d);
        int weight1 = 1000; // 150 * 150 * 1000 = more than 10.000.000
        Rect rect2 = computeMeteringArea(sensorClickX, sensorClickY, 300d);
        int weight2 = 100; // 300 * 300 * 100 = 9.000.000

        List<Camera.Area> list = new ArrayList<>(2);
        list.add(new Camera.Area(rect1, weight1));
        list.add(new Camera.Area(rect2, weight2));
        return list;
    }

    private Rect computeMeteringArea(double centerX, double centerY, double size) {
        double delta = size / 2d;
        int top = (int) Math.max(centerY - delta, -1000);
        int bottom = (int) Math.min(centerY + delta, 1000);
        int left = (int) Math.max(centerX - delta, -1000);
        int right = (int) Math.min(centerX + delta, 1000);
        // Log.e(TAG, "top:"+top+", left:"+left+", bottom:"+bottom+", right:"+right);
        return new Rect(left, top, right, bottom);
    }
    /**
     * Returns how much should the sensor image be rotated before being shown.
     * It is meant to be fed to Camera.setDisplayOrientation().
     */
    private int computeSensorToDisplayOffset() {
        if (mFacing == Facing.FRONT) {
            // or: (360 - ((mSensorOffset + mDisplayOffset) % 360)) % 360;
            return ((mSensorOffset - mDisplayOffset) + 360 + 180) % 360;
        } else {
            return (mSensorOffset - mDisplayOffset + 360) % 360;
        }
    }

    private boolean collectCameraId() {
        int internalFacing = mMapper.map(mFacing);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == internalFacing) {
                mSensorOffset = cameraInfo.orientation;
                mCameraId = i;
                return true;
            }
        }
        return false;
    }

    private boolean mergeFlash(Camera.Parameters params, Flash oldFlash) {
        if (mOptions.supports(mFlash)) {
            params.setFlashMode(mMapper.map(mFlash));
            return true;
        }
        mFlash = oldFlash;
        return false;
    }

    // Choose the best default focus, based on session type.
    private void applyDefaultFocus(Camera.Parameters params) {
        List<String> modes = params.getSupportedFocusModes();

        if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            return;
        }

        if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            return;
        }

        if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            return;
        }

        if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        }
    }

    private boolean shouldSetup() {
        return isCameraOpened() && mPreview.isReady() && !mIsSetup;
    }

    // The act of binding an "open" camera to a "ready" preview.
    // These can happen at different times but we want to end up here.
    private void setup() {
        try {
            if (mPreview.getOutputClass() == SurfaceHolder.class) {
                mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
            } else {
                mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean invertPreviewSizes = shouldFlipSizes(); // mDisplayOffset % 180 != 0;
        mCaptureSize = computeCaptureSize();
        mPreviewSize = computePreviewSize();
        mCameraCallbacks.onCameraPreviewSizeChanged();
        mPreview.setDesiredSize(
                invertPreviewSizes ? mPreviewSize.getHeight() : mPreviewSize.getWidth(),
                invertPreviewSizes ? mPreviewSize.getWidth() : mPreviewSize.getHeight()
        );
        synchronized (mLock) {
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight()); // <- not allowed during preview
            params.setPictureSize(mCaptureSize.getWidth(), mCaptureSize.getHeight()); // <- allowed
            mCamera.setParameters(params);
        }
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        mIsSetup = true;
    }

    /**
     * This is called either on cameraView.start(), or when the underlying surface changes.
     * It is possible that in the first call the preview surface has not already computed its
     * dimensions.
     * But when it does, the {@link Preview.SurfaceCallback} should be called,
     * and this should be refreshed.
     */
    private Size computeCaptureSize() {
        Camera.Parameters params = mCamera.getParameters();
        // Choose according to developer choice in setVideoQuality.
        // The Camcorder internally checks for cameraParameters.getSupportedVideoSizes() etc.
        // We want the picture size to be the max picture consistent with the video aspect ratio.
        List<Size> captureSizes = sizesFromList(params.getSupportedPictureSizes());
        CamcorderProfile profile = getCamcorderProfile(mVideoQuality);
        AspectRatio targetRatio = AspectRatio.of(profile.videoFrameWidth, profile.videoFrameHeight);
        return matchSize(captureSizes, targetRatio, new Size(0, 0), true);
    }

    private Size computePreviewSize() {
        Camera.Parameters params = mCamera.getParameters();
        List<Size> previewSizes = sizesFromList(params.getSupportedPreviewSizes());
        AspectRatio targetRatio = AspectRatio.of(mCaptureSize.getWidth(), mCaptureSize.getHeight());
        return matchSize(previewSizes, targetRatio, mPreview.getSurfaceSize(), false);
    }

    /**
     * Policy here is to return a size that is big enough to fit the surface size,
     * and possibly consistent with the target aspect ratio.
     * @param sizes list of possible sizes
     * @param targetRatio aspect ratio
     * @param biggerThan size representing the current surface size
     * @return chosen size
     */
    private static Size matchSize(List<Size> sizes, AspectRatio targetRatio, Size biggerThan, boolean biggestPossible) {
        if (sizes == null) return null;

        List<Size> consistent = new ArrayList<>(5);
        List<Size> bigEnoughAndConsistent = new ArrayList<>(5);

        for (Size size : sizes) {
            AspectRatio ratio = AspectRatio.of(size.getWidth(), size.getHeight());
            if (ratio.equals(targetRatio)) {
                consistent.add(size);
                if (size.getHeight() >= biggerThan.getHeight() && size.getWidth() >= biggerThan.getWidth()) {
                    bigEnoughAndConsistent.add(size);
                }
            }
        }

        if (biggestPossible) {
            if (bigEnoughAndConsistent.size() > 0) return Collections.max(bigEnoughAndConsistent);
            if (consistent.size() > 0) return Collections.max(consistent);
            return Collections.max(sizes);
        } else {
            if (bigEnoughAndConsistent.size() > 0) return Collections.min(bigEnoughAndConsistent);
            if (consistent.size() > 0) return Collections.max(consistent);
            return Collections.max(sizes);
        }
    }

    @NonNull
    private CamcorderProfile getCamcorderProfile(VideoQuality videoQuality) {
        switch (videoQuality) {
            case HIGHEST:
                return CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_HIGH);

            case MAX_2160P:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                        CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_2160P)) {
                    return CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_2160P);
                }
                // Don't break.

            case MAX_1080P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_1080P)) {
                    return CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_1080P);
                }
                // Don't break.

            case MAX_720P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_720P)) {
                    return CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_720P);
                }
                // Don't break.

            case MAX_480P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_480P)) {
                    return CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_480P);
                }
                // Don't break.

            case MAX_QVGA:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_QVGA)) {
                    return CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_QVGA);
                }
                // Don't break.

            case LOWEST:
            default:
                // Fallback to lowest.
                return CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_LOW);
        }
    }

    /**
     * Returns a list of {@link Size} out of Camera.Sizes.
     */
    @Nullable
    private static List<Size> sizesFromList(List<Camera.Size> sizes) {
        if (sizes == null) return null;
        List<Size> result = new ArrayList<>(sizes.size());
        for (Camera.Size size : sizes) {
            result.add(new Size(size.width, size.height));
        }
        return result;
    }
}
