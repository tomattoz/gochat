package red.tel.chat.camera;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vmodev on 9/6/17.
 */

public class CameraOptions {
    private Set<Facing> supportedFacing = new HashSet<>(2);
    private Set<Flash> supportedFlash = new HashSet<>(4);

    private boolean zoomSupported;
    private boolean videoSnapshotSupported;
    private boolean exposureCorrectionSupported;
    private float exposureCorrectionMinValue;
    private float exposureCorrectionMaxValue;
    private boolean autoFocusSupported;


    // Camera1 constructor.
    @SuppressWarnings("deprecation")
    CameraOptions(Camera.Parameters params) {
        List<String> strings;
        Mapper mapper = new Mapper.Mapper1();

        // Facing
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            Facing value = mapper.unmapFacing(cameraInfo.facing);
            if (value != null) supportedFacing.add(value);
        }

        // Flash
        strings = params.getSupportedFlashModes();
        if (strings != null) {
            for (String string : strings) {
                Flash value = mapper.unmapFlash(string);
                if (value != null) supportedFlash.add(value);
            }
        }


        zoomSupported = params.isZoomSupported();
        videoSnapshotSupported = params.isVideoSnapshotSupported();
        autoFocusSupported = params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO);

        // Exposure correction
        float step = params.getExposureCompensationStep();
        exposureCorrectionMinValue = (float) params.getMinExposureCompensation() * step;
        exposureCorrectionMaxValue = (float) params.getMaxExposureCompensation() * step;
        exposureCorrectionSupported = params.getMinExposureCompensation() != 0
                || params.getMaxExposureCompensation() != 0;
    }


    // Camera2 constructor.
    @TargetApi(21)
    CameraOptions(CameraCharacteristics params) {
    }


    /**
     * Shorthand for getSupportedFacing().contains(value).
     *
     * @param facing value
     * @return whether it's supported
     */
    public boolean supports(Facing facing) {
        return getSupportedFacing().contains(facing);
    }


    /**
     * Shorthand for getSupportedFlash().contains(value).
     *
     * @param flash value
     * @return whether it's supported
     */
    public boolean supports(Flash flash) {
        return getSupportedFlash().contains(flash);
    }


    /**
     * Shorthand for other methods in this class,
     * e.g. supports(GestureAction.ZOOM) == isZoomSupported().
     *
     * @param action value to be checked
     * @return whether it's supported
     */
    public boolean supports(GestureAction action) {
        switch (action) {
            case FOCUS:
            case FOCUS_WITH_MARKER:
                return isAutoFocusSupported();
            case CAPTURE:
            case NONE:
                return true;
            case ZOOM:
                return isZoomSupported();
            case EXPOSURE_CORRECTION:
                return isExposureCorrectionSupported();
        }
        return false;
    }


    /**
     * Set of supported facing values.
     *
     * @return a set of supported values.
     * @see Facing#BACK
     * @see Facing#FRONT
     */
    @NonNull
    public Set<Facing> getSupportedFacing() {
        return Collections.unmodifiableSet(supportedFacing);
    }


    /**
     * Set of supported flash values.
     *
     * @return a set of supported values.
     * @see Flash#AUTO
     * @see Flash#OFF
     * @see Flash#ON
     * @see Flash#TORCH
     */
    @NonNull
    public Set<Flash> getSupportedFlash() {
        return Collections.unmodifiableSet(supportedFlash);
    }


    public boolean isZoomSupported() {
        return zoomSupported;
    }


    /**
     * Whether video snapshots are supported. If this is false, taking pictures
     * while recording a video will have no effect.
     *
     * @return whether video snapshot is supported.
     */
    public boolean isVideoSnapshotSupported() {
        return videoSnapshotSupported;
    }


    /**
     * Whether auto focus is supported. This means you can map gestures to
     * {@link GestureAction#FOCUS} or {@link GestureAction#FOCUS_WITH_MARKER}
     * and focus will be changed on tap.
     *
     * @return whether auto focus is supported.
     */
    public boolean isAutoFocusSupported() {
        return autoFocusSupported;
    }


    /**
     * Whether exposure correction is supported. If this is false, calling
     * {@link CameraView#(float)} has no effect.
     *
     * @return whether exposure correction is supported.
     * @see #getExposureCorrectionMinValue()
     * @see #getExposureCorrectionMaxValue()
     */
    public boolean isExposureCorrectionSupported() {
        return exposureCorrectionSupported;
    }


    /**
     * The minimum value of negative exposure correction, in EV stops.
     * This is presumably negative or 0 if not supported.
     *
     * @return min EV value
     */
    public float getExposureCorrectionMinValue() {
        return exposureCorrectionMinValue;
    }


    /**
     * The maximum value of positive exposure correction, in EV stops.
     * This is presumably positive or 0 if not supported.
     *
     * @return max EV value
     */
    public float getExposureCorrectionMaxValue() {
        return exposureCorrectionMaxValue;
    }
}
