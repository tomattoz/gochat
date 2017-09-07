package red.tel.chat.camera;

import android.hardware.Camera;
import android.os.Build;

import java.util.HashMap;

/**
 * Created by vmodev on 9/6/17.
 */

public abstract class Mapper {
    abstract <T> T map(Flash flash);

    abstract <T> T map(Facing facing);

    abstract <T> Flash unmapFlash(T cameraConstant);

    abstract <T> Facing unmapFacing(T cameraConstant);

    public static class Mapper1 extends Mapper {

        private static final HashMap<Flash, String> FLASH = new HashMap<>();
        private static final HashMap<Facing, Integer> FACING = new HashMap<>();

        static {
            FLASH.put(Flash.OFF, Camera.Parameters.FLASH_MODE_OFF);
            FLASH.put(Flash.ON, Camera.Parameters.FLASH_MODE_ON);
            FLASH.put(Flash.AUTO, Camera.Parameters.FLASH_MODE_AUTO);
            FLASH.put(Flash.TORCH, Camera.Parameters.FLASH_MODE_TORCH);
            FACING.put(Facing.BACK, Camera.CameraInfo.CAMERA_FACING_BACK);
            FACING.put(Facing.FRONT, Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        @Override
        <T> T map(Flash flash) {
            return (T) FLASH.get(flash);
        }

        @Override
        public <T> T map(Facing facing) {
            return (T) FACING.get(facing);
        }

        private <T> T reverseLookup(HashMap<T, ?> map, Object object) {
            for (T value : map.keySet()) {
                if (map.get(value).equals(object)) {
                    return value;
                }
            }
            return null;
        }

        @Override
        <T> Flash unmapFlash(T cameraConstant) {
            return reverseLookup(FLASH, cameraConstant);
        }

        @Override
        <T> Facing unmapFacing(T cameraConstant) {
            return reverseLookup(FACING, cameraConstant);
        }
    }

    static class Mapper2 extends Mapper {


        @Override
        <T> T map(Flash flash) {
            return null;
        }

        @Override
        <T> Flash unmapFlash(T cameraConstant) {
            return null;
        }


        @Override
        <T> T map(Facing facing) {
            return null;
        }

        @Override
        <T> Facing unmapFacing(T cameraConstant) {
            return null;
        }
    }
}
