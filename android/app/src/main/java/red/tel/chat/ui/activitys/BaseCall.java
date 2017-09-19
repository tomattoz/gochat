package red.tel.chat.ui.activitys;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import red.tel.chat.Constant;
import red.tel.chat.camera.RecordContact;
import red.tel.chat.camera.RecordPresenterImpl;
import red.tel.chat.io.AudioRecorder;

import static red.tel.chat.io.AudioRecorder.BUFFER_SIZE;
import static red.tel.chat.io.AudioRecorder.ENCODING;
import static red.tel.chat.io.AudioRecorder.SAMPLE_RATE;

/**
 * Created by vmodev on 9/11/17.
 */

public abstract class BaseCall extends BaseActivity implements RecordContact.View {
    private static final String TAG = BaseCall.class.getSimpleName();
    private AudioRecorder audioRecorder;
    private long startTime = 0;
    private boolean mIsRecording = false;
    protected AudioTrack audioTrack;
    TextureView mCameraTextureView;
    RecordContact.Presenter mRecordPresenter;
    private int cameraId = Constant.CAMERA_FACING_BACK;

    private AudioRecorder.AudioRecorderListener mAudioRecorderListener = new AudioRecorder.AudioRecorderListener() {
        @Override
        public void onAudioDataUpdate(ByteBuffer buffer, ShortBuffer[] samples, byte[] data) {
            if (mIsRecording) {
                onCallBackRecord(buffer, samples, data);
            }
        }

        @Override
        public void onFail() {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int CHANNEL_MASK = AudioFormat.CHANNEL_OUT_MONO;
        // Using 16bit PCM for output. Keep this value in sync with
        // kBytesPerAudioOutputSample in media_codec_bridge.cc.
        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE, CHANNEL_MASK,
                ENCODING, BUFFER_SIZE, AudioTrack.MODE_STREAM);

        mRecordPresenter = new RecordPresenterImpl(this);
        mRecordPresenter.setView(this);
        mRecordPresenter.setCameraFacing(cameraId);
    }

    @AfterPermissionGranted(REQUEST_ALL)
    public void requestPermissions() {
        if (EasyPermissions.hasPermissions(this, PERMISSIONS_ALL)) {
            // Have permission, do the thing!
            if (isVideo() && mCameraTextureView != null) {
                mRecordPresenter.startPreview();
                /*cameraView.start();
                cameraView.addCameraListener(new CameraListener() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        Log.d(TAG, "onPreviewFrame: " + bytes);
                        onCallVideoData(bytes);
                    }
                });*/
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
        //requestPermissions();

    }

    @Override
    protected void onPause() {
        super.onPause();
        onDestroyAudioRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isVideo() && mCameraTextureView != null) {
            //cameraView.destroy();
            if (mRecordPresenter != null) {
                mRecordPresenter.stopUpload();
            }
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

    protected abstract void onCallBackRecord(ByteBuffer buffer, ShortBuffer[] samples, byte[] data);

    protected abstract void onCallVideoData(byte[] data);

    protected abstract boolean isVideo();
}
