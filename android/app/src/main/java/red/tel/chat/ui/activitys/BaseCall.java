package red.tel.chat.ui.activitys;

import android.hardware.Camera;
import android.media.AudioRecord;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import red.tel.chat.camera.CameraListener;
import red.tel.chat.camera.CameraView;
import red.tel.chat.io.AudioRecorder;

/**
 * Created by vmodev on 9/11/17.
 */

public abstract class BaseCall extends BaseActivity {
    private static final String TAG = BaseCall.class.getSimpleName();
    protected CameraView cameraView;
    private AudioRecorder audioRecorder;
    private long startTime = 0;
    private boolean mIsRecording = false;
    @AfterPermissionGranted(REQUEST_ALL)
        public void requestPermissions() {
            if (EasyPermissions.hasPermissions(this, PERMISSIONS_ALL)) {
                // Have permission, do the thing!
                if (isVideo() && cameraView != null) {
                    cameraView.start();
                    cameraView.addCameraListener(new CameraListener() {
                        @Override
                        public void onPreviewFrame(byte[] bytes, Camera camera) {
                            Log.d(TAG, "onPreviewFrame: " + bytes);
                        }
                    });
                }

                onStartAudioRecord();
            } else {
                // Ask for one permission
                EasyPermissions.requestPermissions(this, "check",
                        REQUEST_ALL, PERMISSIONS_ALL);
            }
        }
    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isVideo() && cameraView != null) {
            cameraView.stop();
        }
        onDestroyAudioRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isVideo() && cameraView != null) {
            cameraView.destroy();
        }
    }

    private void onStartAudioRecord() {
        mIsRecording = true;
        startTime = System.currentTimeMillis();
        if (audioRecorder == null) {
            audioRecorder = new AudioRecorder();
            audioRecorder.startRecorder(mAudioRecorderListener);
        }

        if (audioRecorder == null || audioRecorder.getAudioRecord().getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            startTime = System.currentTimeMillis();
        }
    }

    private void onDestroyAudioRecord() {
        if (audioRecorder != null) {
            audioRecorder.release();
            audioRecorder = null;
            mIsRecording = false;
        }
    }

    private AudioRecorder.AudioRecorderListener mAudioRecorderListener = new AudioRecorder.AudioRecorderListener() {
        @Override
        public void onAudioDataUpdate(ByteBuffer buffer, ShortBuffer[] samples) {
            if (mIsRecording) {
                onCallBackRecord(buffer, samples);
            }
        }

        @Override
        public void onFail() {

        }
    };

    protected abstract void onCallBackRecord(ByteBuffer buffer, ShortBuffer[] samples);

    protected abstract boolean isVideo();
}
