package red.tel.chat.camera;

import java.util.Arrays;
import java.util.List;

/**
 * Created by vmodev on 9/6/17.
 */

public enum Gesture {
    /**
     * Pinch gesture, typically assigned to the zoom control.
     * This gesture can be mapped to:
     *
     * - {@link GestureAction#ZOOM}
     * - {@link GestureAction#EXPOSURE_CORRECTION}
     * - {@link GestureAction#NONE}
     */
    PINCH(GestureAction.ZOOM, GestureAction.EXPOSURE_CORRECTION),

    /**
     * Single tap gesture, typically assigned to the focus control.
     * This gesture can be mapped to:
     *
     * - {@link GestureAction#FOCUS}
     * - {@link GestureAction#FOCUS_WITH_MARKER}
     * - {@link GestureAction#CAPTURE}
     * - {@link GestureAction#NONE}
     */
    TAP(GestureAction.FOCUS, GestureAction.FOCUS_WITH_MARKER, GestureAction.CAPTURE),
    // DOUBLE_TAP(GestureAction.FOCUS, GestureAction.FOCUS_WITH_MARKER, GestureAction.CAPTURE),

    /**
     * Long tap gesture.
     * This gesture can be mapped to:
     *
     * - {@link GestureAction#FOCUS}
     * - {@link GestureAction#FOCUS_WITH_MARKER}
     * - {@link GestureAction#CAPTURE}
     * - {@link GestureAction#NONE}
     */
    LONG_TAP(GestureAction.FOCUS, GestureAction.FOCUS_WITH_MARKER, GestureAction.CAPTURE),

    /**
     * Horizontal scroll gesture.
     * This gesture can be mapped to:
     *
     * - {@link GestureAction#ZOOM}
     * - {@link GestureAction#EXPOSURE_CORRECTION}
     * - {@link GestureAction#NONE}
     */
    SCROLL_HORIZONTAL(GestureAction.ZOOM, GestureAction.EXPOSURE_CORRECTION),

    /**
     * Vertical scroll gesture.
     * This gesture can be mapped to:
     *
     * - {@link GestureAction#ZOOM}
     * - {@link GestureAction#EXPOSURE_CORRECTION}
     * - {@link GestureAction#NONE}
     */
    SCROLL_VERTICAL(GestureAction.ZOOM, GestureAction.EXPOSURE_CORRECTION);

    Gesture(GestureAction... controls) {
        mControls = Arrays.asList(controls);
    }

    private List<GestureAction> mControls;

    boolean isAssignableTo(GestureAction control) {
        return control == GestureAction.NONE || mControls.contains(control);
    }
}
