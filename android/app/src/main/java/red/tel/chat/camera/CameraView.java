package red.tel.chat.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import red.tel.chat.R;
import red.tel.chat.utils.AspectRatio;
import red.tel.chat.utils.Size;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by vmodev on 9/6/17.
 */

public class CameraView extends FrameLayout {
    private final static int DEFAULT_JPEG_QUALITY = 100;
    private final static String TAG = CameraView.class.getSimpleName();

    // Threading
    private Handler mUiHandler;
    private WorkerHandler mWorkerHandler;
    // Self managed parameters
    private int mJpegQuality;
    private boolean mCropOutput;
    private float mZoomValue;
    private float mExposureCorrectionValue;
    private HashMap<Gesture, GestureAction> mGestureMap = new HashMap<>(4);

    // Components
    private CameraCallbacks mCameraCallbacks;
    private OrientationHelper mOrientationHelper;
    private CameraController mCameraController;
    private Preview mPreviewImpl;


    // Views
    private boolean mIsStarted;
    private boolean mKeepScreenOn;

    public CameraView(@NonNull Context context) {
        super(context, null);
        init(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CameraView, 0, 0);
        int jpegQuality = typedArray.getInteger(R.styleable.CameraView_cameraJpegQuality, DEFAULT_JPEG_QUALITY);

        Facing facing = Facing.fromValue(typedArray.getInteger(R.styleable.CameraView_cameraFacing, Facing.DEFAULT.value()));
        Flash flash = Flash.fromValue(typedArray.getInteger(R.styleable.CameraView_cameraFlash, Flash.DEFAULT.value()));
        VideoQuality videoQuality = VideoQuality.fromValue(typedArray.getInteger(R.styleable.CameraView_cameraVideoQuality, VideoQuality.DEFAULT.value()));

        // Gestures
        GestureAction tapGesture = GestureAction.fromValue(typedArray.getInteger(R.styleable.CameraView_cameraGestureTap, GestureAction.DEFAULT_TAP.value()));
        GestureAction longTapGesture = GestureAction.fromValue(typedArray.getInteger(R.styleable.CameraView_cameraGestureLongTap, GestureAction.DEFAULT_LONG_TAP.value()));
        GestureAction pinchGesture = GestureAction.fromValue(typedArray.getInteger(R.styleable.CameraView_cameraGesturePinch, GestureAction.DEFAULT_PINCH.value()));
        typedArray.recycle();

        // Components
        mCameraCallbacks = new CameraCallbacks();
        mPreviewImpl = new TextureViewPreview(context, this);
        mCameraController = new CameraAndroid(mCameraCallbacks, mPreviewImpl);
        mUiHandler = new Handler(Looper.getMainLooper());
        mWorkerHandler = new WorkerHandler("CameraViewWorker");
        mIsStarted = false;
        setJpegQuality(jpegQuality);

        // Apply camera controller params
        setFacing(facing);
        setFlash(flash);
        setVideoQuality(videoQuality);

        // Apply gestures
        //mapGesture(Gesture.TAP, tapGesture);
        //mapGesture(Gesture.LONG_TAP, longTapGesture);
        //mapGesture(Gesture.PINCH, pinchGesture);

        if (!isInEditMode()) {
            mOrientationHelper = new OrientationHelper(context, mCameraCallbacks);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            mOrientationHelper.enable(manager.getDefaultDisplay());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            mOrientationHelper.disable();
        }
        super.onDetachedFromWindow();
    }

    private String ms(int mode) {
        switch (mode) {
            case AT_MOST: return "AT_MOST";
            case EXACTLY: return "EXACTLY";
            case UNSPECIFIED: return "UNSPECIFIED";
        }
        return null;
    }

    /**
     * Measuring is basically controlled by layout params width and height.
     * The basic semantics are:
     *
     * - MATCH_PARENT: CameraView should completely fill this dimension, even if this might mean
     *                 not respecting the preview aspect ratio.
     * - WRAP_CONTENT: CameraView should try to adapt this dimension to respect the preview
     *                 aspect ratio.
     *
     * When both dimensions are MATCH_PARENT, CameraView will fill its
     * parent no matter the preview. Thanks to what happens in {@link Preview}, this acts like
     * a CENTER CROP scale type.
     *
     * When both dimensions are WRAP_CONTENT, CameraView will take the biggest dimensions that
     * fit the preview aspect ratio. This acts like a CENTER INSIDE scale type.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Size previewSize = getPreviewSize();
        if (previewSize == null) {
            Log.e(TAG, "onMeasure, surface is not ready. Calling default behavior.");
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // Let's which dimensions need to be adapted.
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthValue = MeasureSpec.getSize(widthMeasureSpec);
        final int heightValue = MeasureSpec.getSize(heightMeasureSpec);
        final boolean flip = mCameraController.shouldFlipSizes();
        final float previewWidth = flip ? previewSize.getHeight() : previewSize.getWidth();
        final float previewHeight = flip ? previewSize.getWidth() : previewSize.getHeight();

        // If MATCH_PARENT is interpreted as AT_MOST, transform to EXACTLY
        // to be consistent with our semantics (and our docs).
        final ViewGroup.LayoutParams lp = getLayoutParams();
        if (widthMode == AT_MOST && lp.width == MATCH_PARENT) widthMode = EXACTLY;
        if (heightMode == AT_MOST && lp.height == MATCH_PARENT) heightMode = EXACTLY;
        Log.e(TAG, "onMeasure, requested dimensions are (" +
                widthValue + "[" + ms(widthMode) + "]x" +
                heightValue + "[" + ms(heightMode) + "])");
        Log.e(TAG, "onMeasure, previewSize is (" + previewWidth + "x" + previewHeight + ")");


        // If we have fixed dimensions (either 300dp or MATCH_PARENT), there's nothing we should do,
        // other than respect it. The preview will eventually be cropped at the sides (by PreviewImpl scaling)
        // except the case in which these fixed dimensions manage to fit exactly the preview aspect ratio.
        if (widthMode == EXACTLY && heightMode == EXACTLY) {
            Log.e(TAG, "onMeasure, both are MATCH_PARENT or fixed value. We adapt. This means CROP_INSIDE. " +
                    "(" + widthValue + "x" + heightValue + ")");
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // If both dimensions are free, with no limits, then our size will be exactly the
        // preview size. This can happen rarely, for example in scrollable containers.
        if (widthMode == UNSPECIFIED && heightMode == UNSPECIFIED) {
            Log.e(TAG, "onMeasure, both are completely free. " +
                    "We respect that and extend to the whole preview size. " +
                    "(" + previewWidth + "x" + previewHeight + ")");
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec((int) previewWidth, EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) previewHeight, EXACTLY));
            return;
        }

        // It's sure now that at least one dimension can be determined (either because EXACTLY or AT_MOST).
        // This starts to seem a pleasant situation.

        // If one of the dimension is completely free (e.g. in a scrollable container),
        // take the other and fit the ratio.
        // One of the two might be AT_MOST, but we use the value anyway.
        float ratio = previewHeight / previewWidth;
        if (widthMode == UNSPECIFIED || heightMode == UNSPECIFIED) {
            boolean freeWidth = widthMode == UNSPECIFIED;
            int height, width;
            if (freeWidth) {
                height = heightValue;
                width = (int) (height / ratio);
            } else {
                width = widthValue;
                height = (int) (width * ratio);
            }
            Log.e(TAG, "onMeasure, one dimension was free, we adapted it to fit the aspect ratio. " +
                    "(" + width + "x" + height + ")");
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, EXACTLY));
            return;
        }

        // At this point both dimensions are either AT_MOST-AT_MOST, EXACTLY-AT_MOST or AT_MOST-EXACTLY.
        // Let's manage this sanely. If only one is EXACTLY, we can TRY to fit the aspect ratio,
        // but it is not guaranteed to succeed. It depends on the AT_MOST value of the other dimensions.
        if (widthMode == EXACTLY || heightMode == EXACTLY) {
            boolean freeWidth = widthMode == AT_MOST;
            int height, width;
            if (freeWidth) {
                height = heightValue;
                width = Math.min((int) (height / ratio), widthValue);
            } else {
                width = widthValue;
                height = Math.min((int) (width * ratio), heightValue);
            }
            Log.e(TAG, "onMeasure, one dimension was EXACTLY, another AT_MOST. We have TRIED to fit " +
                    "the aspect ratio, but it's not guaranteed. (" + width + "x" + height + ")");
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, EXACTLY));
            return;
        }

        // Last case, AT_MOST and AT_MOST. Here we can SURELY fit the aspect ratio by filling one
        // dimension and adapting the other.
        int height, width;
        float atMostRatio = (float) heightValue / (float) widthValue;
        if (atMostRatio >= ratio) {
            // We must reduce height.
            width = widthValue;
            height = (int) (width * ratio);
        } else {
            height = heightValue;
            width = (int) (height / ratio);
        }
        Log.e(TAG, "onMeasure, both dimension were AT_MOST. We fit the preview aspect ratio. " +
                "(" + width + "x" + height + ")");
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, EXACTLY),
                MeasureSpec.makeMeasureSpec(height, EXACTLY));
    }

    /**
     * Returns whether the camera has started showing its preview.
     * @return whether the camera has started
     */
    public boolean isStarted() {
        return mIsStarted;
    }


    /**
     * Starts the camera preview, if not started already.
     * This should be called onResume(), or when you are ready with permissions.
     */
    public void start() {
        if (mIsStarted || !isEnabled()) {
            // Already started, do nothing.
            return;
        }

        mIsStarted = true;
        mCameraController.start();
    }

    /**
     * Stops the current preview, if any was started.
     * This should be called onPause().
     */
    public void stop() {
        if (!mIsStarted) {
            // Already stopped, do nothing.
            return;
        }
        mIsStarted = false;
        mCameraController.stop();
    }

    /**
     * Adds a {@link CameraListener} instance to be notified of all
     * interesting events that happen during the camera lifecycle.
     *
     * @param cameraListener a listener for events.
     */
    public void addCameraListener(CameraListener cameraListener) {
        if (cameraListener != null) {
            mCameraCallbacks.addListener(cameraListener);
        }
    }

    public void destroy() {
        // TODO: this is not strictly needed
        mCameraCallbacks.clearListeners(); // Release inner listener.
    }

    //endregion

    //region Public APIs for controls

    /**
     * Returns a {@link CameraOptions} instance holding supported options for this camera
     * session. This might change over time. It's better to hold a reference from
     * {@link CameraListener#onCameraOpened(CameraOptions)}.
     *
     * @return an options map, or null if camera was not opened
     */
    @Nullable
    public CameraOptions getCameraOptions() {
        return mCameraController.getCameraOptions();
    }


    /**
     * If present, returns a collection of extra properties from the current camera
     * session.
     * @return an ExtraProperties object.
     */
    @Nullable
    public ExtraProperties getExtraProperties() {
        return mCameraController.getExtraProperties();
    }


    /**
     * Sets exposure adjustment, in EV stops. A positive value will mean brighter picture.
     *
     * If camera is not opened, this will have no effect.
     * If {@link CameraOptions#isExposureCorrectionSupported()} is false, this will have no effect.
     * The provided value should be between the bounds returned by {@link CameraOptions}, or it will
     * be capped.
     *
     * @see CameraOptions#getExposureCorrectionMinValue()
     * @see CameraOptions#getExposureCorrectionMaxValue()
     *
     * @param EVvalue exposure correction value.
     */
    public void setExposureCorrection(float EVvalue) {
        CameraOptions options = getCameraOptions();
        if (options != null) {
            float min = options.getExposureCorrectionMinValue();
            float max = options.getExposureCorrectionMaxValue();
            if (EVvalue < min) EVvalue = min;
            if (EVvalue > max) EVvalue = max;
            if (mCameraController.setExposureCorrection(EVvalue)) {
                mExposureCorrectionValue = EVvalue;
            }
        }
    }


    /**
     * Returns the current exposure correction value, typically 0
     * at start-up.
     * @return the current exposure correction value
     */
    public float getExposureCorrection() {
        return mExposureCorrectionValue;
    }

    /**
     * Returns the size used for the preview,
     * or null if it hasn't been computed (for example if the surface is not ready).
     * @return a Size
     */
    @Nullable
    public Size getPreviewSize() {
        return mCameraController != null ? mCameraController.getPreviewSize() : null;
    }


    /**
     * Returns the size used for the capture,
     * or null if it hasn't been computed yet (for example if the surface is not ready).
     * @return a Size
     */
    @Nullable
    public Size getCaptureSize() {
        return mCameraController != null ? mCameraController.getCaptureSize() : null;
    }

    /**
     * Sets video recording quality. This is not guaranteed to be supported by current device.
     * If it's not, a lower quality will be chosen, until a supported one is found.
     * If sessionType is video, this might trigger a camera restart and a change in preview size.
     *
     * @see VideoQuality#LOWEST
     * @see VideoQuality#HIGHEST
     * @see VideoQuality#MAX_QVGA
     * @see VideoQuality#MAX_480P
     * @see VideoQuality#MAX_720P
     * @see VideoQuality#MAX_1080P
     * @see VideoQuality#MAX_2160P
     *
     * @param videoQuality requested video quality
     */
    public void setVideoQuality(VideoQuality videoQuality) {
        mCameraController.setVideoQuality(videoQuality);
    }


    /**
     * Gets the current video quality.
     * @return the current video quality
     */
    public VideoQuality getVideoQuality() {
        return mCameraController.getVideoQuality();
    }

    /**
     * Sets the JPEG compression quality for image outputs.
     * @param jpegQuality a 0-100 integer.
     */
    public void setJpegQuality(int jpegQuality) {
        if (jpegQuality <= 0 || jpegQuality > 100) {
            throw new IllegalArgumentException("JPEG quality should be > 0 and <= 0");
        }
        mJpegQuality = jpegQuality;
    }

    /**
     * Sets which camera sensor should be used.
     *
     * @see Facing#FRONT
     * @see Facing#BACK
     *
     * @param facing a facing value.
     */
    public void setFacing(final Facing facing) {
        mCameraController.setFacing(facing);
    }


    /**
     * Gets the facing camera currently being used.
     * @return a facing value.
     */
    public Facing getFacing() {
        return mCameraController.getFacing();
    }


    /**
     * Toggles the facing value between {@link Facing#BACK}
     * and {@link Facing#FRONT}.
     *
     * @return the new facing value
     */
    public Facing toggleFacing() {
        Facing facing = mCameraController.getFacing();
        switch (facing) {
            case BACK:
                setFacing(Facing.FRONT);
                break;

            case FRONT:
                setFacing(Facing.BACK);
                break;
        }

        return mCameraController.getFacing();
    }


    /**
     * Sets the flash mode.
     *
     * @see Flash#OFF
     * @see Flash#ON
     * @see Flash#AUTO
     * @see Flash#TORCH

     * @param flash desired flash mode.
     */
    public void setFlash(Flash flash) {
        mCameraController.setFlash(flash);
    }


    /**
     * Gets the current flash mode.
     * @return a flash mode
     */
    public Flash getFlash() {
        return mCameraController.getFlash();
    }


    /**
     * Toggles the flash mode between {@link Flash#OFF},
     * {@link Flash#ON} and {@link Flash#AUTO}, in this order.
     *
     * @return the new flash value
     */
    public Flash toggleFlash() {
        Flash flash = mCameraController.getFlash();
        switch (flash) {
            case OFF:
                setFlash(Flash.ON);
                break;

            case ON:
                setFlash(Flash.AUTO);
                break;

            case AUTO:
            case TORCH:
                setFlash(Flash.OFF);
                break;
        }

        return mCameraController.getFlash();
    }

    class CameraCallbacks implements OrientationHelper.Callbacks {

        // Outer listeners
        private ArrayList<CameraListener> mListeners = new ArrayList<>(2);

        // Orientation TODO: move this logic into OrientationHelper
        private Integer mDisplayOffset;
        private Integer mDeviceOrientation;
        CameraCallbacks() {
        }

        public void dispatchOnCameraOpened(final CameraOptions options) {
            mUiHandler.post(() -> {
                for (CameraListener listener : mListeners) {
                    listener.onCameraOpened(options);
                }
            });
        }


        public void dispatchOnCameraClosed() {
            mUiHandler.post(() -> {
                for (CameraListener listener : mListeners) {
                    listener.onCameraClosed();
                }
            });
        }


        public void onCameraPreviewSizeChanged() {
            // CameraAndroid preview size, as returned by getPreviewSize(), has changed.
            // Request a layout pass for onMeasure() to do its stuff.
            // Potentially this will change CameraView size, which changes Surface size,
            // which triggers a new Preview size. But hopefully it will converge.
            mUiHandler.post(CameraView.this::requestLayout);
        }


        /**
         * What would be great here is to ensure the EXIF tag in the jpeg is consistent with what we expect,
         * and maybe add flipping when we have been using the front camera.
         * Unfortunately this is not easy, because
         * - You can't write EXIF data to a byte[] array, not with support library at least
         * - You don't know what byte[] is, see {@link android.hardware.Camera.Parameters#setRotation(int)}.
         *   Sometimes our rotation is encoded in the byte array, sometimes a rotated byte[] is returned.
         *   Depends on the hardware.
         *
         * So for now we ignore flipping.
         *
         * @param consistentWithView is the final image (decoded respecting EXIF data) consistent with
         *                           the view width and height? Or should we flip dimensions to have a
         *                           consistent measure?
         * @param flipHorizontally whether this picture should be flipped horizontally after decoding,
         *                         because it was taken with the front camera.
         */
        public void processImage(final byte[] jpeg, final boolean consistentWithView, final boolean flipHorizontally) {
            mWorkerHandler.post(() -> {
                byte[] jpeg2 = jpeg;
                if (mCropOutput && mPreviewImpl.isCropping()) {
                    // If consistent, dimensions of the jpeg Bitmap and dimensions of getWidth(), getHeight()
                    // Live in the same reference system.
                    int w = consistentWithView ? getWidth() : getHeight();
                    int h = consistentWithView ? getHeight() : getWidth();
                    AspectRatio targetRatio = AspectRatio.of(w, h);
                    // Log.e(TAG, "is Consistent? " + consistentWithView);
                    // Log.e(TAG, "viewWidth? " + getWidth() + ", viewHeight? " + getHeight());
                    jpeg2 = CropHelper.cropToJpeg(jpeg, targetRatio, mJpegQuality);
                }
                dispatchOnPictureTaken(jpeg2);
            });
        }


        public void processSnapshot(final YuvImage yuv, final boolean consistentWithView, boolean flipHorizontally) {
            mWorkerHandler.post(() -> {
                byte[] jpeg;
                if (mCropOutput && mPreviewImpl.isCropping()) {
                    int w = consistentWithView ? getWidth() : getHeight();
                    int h = consistentWithView ? getHeight() : getWidth();
                    AspectRatio targetRatio = AspectRatio.of(w, h);
                    jpeg = CropHelper.cropToJpeg(yuv, targetRatio, mJpegQuality);
                } else {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuv.compressToJpeg(new Rect(0, 0, yuv.getWidth(), yuv.getHeight()), mJpegQuality, out);
                    jpeg = out.toByteArray();
                }
                dispatchOnPictureTaken(jpeg);
            });
        }


        private void dispatchOnPictureTaken(byte[] jpeg) {
            final byte[] data = jpeg;
            mUiHandler.post(() -> {
                for (CameraListener listener : mListeners) {
                    listener.onPictureTaken(data);
                }
            });
        }


        public void dispatchOnVideoTaken(final File video) {
            mUiHandler.post(() -> {
                for (CameraListener listener : mListeners) {
                    listener.onVideoTaken(video);
                }
            });
        }


        public void dispatchOnFocusStart(@Nullable final Gesture gesture, final PointF point) {
            mUiHandler.post(() -> {
                if (gesture != null && mGestureMap.get(gesture) == GestureAction.FOCUS_WITH_MARKER) {
                    //mTapGestureLayout.onFocusStart(point);
                }

                for (CameraListener listener : mListeners) {
                    listener.onFocusStart(point);
                }
            });
        }


        public void dispatchOnFocusEnd(@Nullable final Gesture gesture, final boolean success,
                                       final PointF point) {
            mUiHandler.post(() -> {

                if (gesture != null && mGestureMap.get(gesture) == GestureAction.FOCUS_WITH_MARKER) {
                    //mTapGestureLayout.onFocusEnd(success);
                }

                for (CameraListener listener : mListeners) {
                    listener.onFocusEnd(success, point);
                }
            });
        }

        @Override
        public void onDisplayOffsetChanged(int displayOffset) {
            mCameraController.onDisplayOffset(displayOffset);
            mDisplayOffset = displayOffset;
            if (mDeviceOrientation != null) {
                int value = (mDeviceOrientation + mDisplayOffset) % 360;
                dispatchOnOrientationChanged(value);
            }
        }

        @Override
        public void onDeviceOrientationChanged(int deviceOrientation) {
            mCameraController.onDeviceOrientation(deviceOrientation);
            mDeviceOrientation = deviceOrientation;
            if (mDisplayOffset != null) {
                int value = (mDeviceOrientation + mDisplayOffset) % 360;
                dispatchOnOrientationChanged(value);
            }
        }


        private void dispatchOnOrientationChanged(final int value) {
            mUiHandler.post(() -> {
                for (CameraListener listener : mListeners) {
                    listener.onOrientationChanged(value);
                }
            });
        }


        public void dispatchOnZoomChanged(final float newValue, final PointF[] fingers) {
            mUiHandler.post(() -> {
                for (CameraListener listener : mListeners) {
                    listener.onZoomChanged(newValue, new float[]{0, 1}, fingers);
                }
            });
        }


        public void dispatchOnExposureCorrectionChanged(final float newValue,
                                                        final float[] bounds,
                                                        final PointF[] fingers) {
            mUiHandler.post(() -> {
                for (CameraListener listener : mListeners) {
                    listener.onExposureCorrectionChanged(newValue, bounds, fingers);
                }
            });
        }


        private void addListener(@NonNull CameraListener cameraListener) {
            mListeners.add(cameraListener);
        }


        private void removeListener(@NonNull CameraListener cameraListener) {
            mListeners.remove(cameraListener);
        }


        private void clearListeners() {
            mListeners.clear();
        }

        public void onPreviewFrame(byte[] bytes, Camera camera) {
            mUiHandler.post(() -> {
                for (CameraListener listener : mListeners) {
                    listener.onPreviewFrame(bytes, camera);
                }
            });
        }
    }
}
