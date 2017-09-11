package red.tel.chat.ui.activitys;

import android.media.AudioRecord;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import red.tel.chat.camera.CameraView;
import red.tel.chat.io.AudioRecorder;

/**
 * Created by vmodev on 9/11/17.
 */

public abstract class BaseCall extends BaseActivity {
    protected CameraView cameraView;
    private AudioRecorder audioRecorder;
    private long startTime = 0;
    private boolean mIsRecording = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (isVideo() && cameraView != null) {
            cameraView.start();
        }

        onStartAudioRecord();
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
