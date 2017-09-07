package red.tel.chat.camera;


import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import red.tel.chat.utils.Size;

public abstract class CameraController implements Preview.SurfaceCallback {
    protected final CameraView.CameraCallbacks mCameraCallbacks;
    protected final Preview mPreview;

    protected Facing mFacing;
    protected Flash mFlash;
    protected VideoQuality mVideoQuality;

    protected ExtraProperties mExtraProperties;
    protected CameraOptions mOptions;

    protected int mDisplayOffset;
    protected int mDeviceOrientation;
    protected Size mCaptureSize;
    protected Size mPreviewSize;

    protected WorkerHandler mHandler;

    CameraController(CameraView.CameraCallbacks callback, Preview preview) {
        mCameraCallbacks = callback;
        mPreview = preview;
        mPreview.setSurfaceCallback(this);
        mHandler = new WorkerHandler("CameraViewController");
    }

    // Starts the preview asynchronously.
    final void start() {
        mHandler.post(() -> onStart());
    }

    // Stops the preview asynchronously.
    final void stop() {
        mHandler.post(() -> onStop());
    }

    // Starts the preview.
    @WorkerThread
    abstract void onStart();

    // Stops the preview.
    @WorkerThread
    abstract void onStop();

    //region Rotation callbacks

    void onDisplayOffset(int displayOrientation) {
        // I doubt this will ever change.
        mDisplayOffset = displayOrientation;
    }

    void onDeviceOrientation(int deviceOrientation) {
        mDeviceOrientation = deviceOrientation;
    }

    //endregion

    //region Abstract setParameters

    abstract boolean setZoom(float zoom);

    abstract boolean setExposureCorrection(float EVvalue);

    abstract void setFacing(Facing facing);

    abstract void setFlash(Flash flash);

    abstract void setVideoQuality(VideoQuality videoQuality);

    abstract boolean startVideo(@NonNull byte[] data);

    abstract boolean endVideo();

    abstract boolean shouldFlipSizes(); // Wheter the Sizes should be flipped to match the view orientation.

    abstract boolean isCameraOpened();

    abstract boolean startAutoFocus(@Nullable Gesture gesture, PointF point);

    @Nullable
    final ExtraProperties getExtraProperties() {
        return mExtraProperties;
    }

    @Nullable
    final CameraOptions getCameraOptions() {
        return mOptions;
    }

    final Facing getFacing() {
        return mFacing;
    }

    final Flash getFlash() {
        return mFlash;
    }

    final VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    final Size getCaptureSize() {
        return mCaptureSize;
    }

    final Size getPreviewSize() {
        return mPreviewSize;
    }
}
