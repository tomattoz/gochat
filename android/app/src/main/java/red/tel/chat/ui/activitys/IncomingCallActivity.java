package red.tel.chat.ui.activitys;


import android.media.AudioRecord;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import red.tel.chat.R;
import red.tel.chat.camera.CameraView;
import red.tel.chat.generated_protobuf.Voip;
import red.tel.chat.io.AudioRecorder;
import red.tel.chat.network.NetworkCall;
import red.tel.chat.ui.fragments.ItemDetailFragment;

import static red.tel.chat.generated_protobuf.Voip.Which.CALL_CANCEL;

public class IncomingCallActivity extends BaseActivity implements View.OnClickListener, NetworkCall.NetworkCallProposalReceiverProtocol {
    private static final String TAG = IncomingCallActivity.class.getSimpleName();
    public static final String TYPE_CALL = "type_call";
    private TextView from;
    private Button btnAccept;
    private Button btnDecline;
    private CameraView cameraView;
    private AudioRecorder audioRecorder;
    private long startTime = 0;
    private boolean mIsRecording = false;
    private boolean isVideo = false;
    private NetworkCall.NetworkCallProposalInfo callInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        from = findViewById(R.id.from);
        cameraView = findViewById(R.id.camera);
        btnAccept = findViewById(R.id.accept);
        btnDecline = findViewById(R.id.decline);
        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isVideo = bundle.getBoolean(TYPE_CALL);
            String whom = bundle.getString(ItemDetailFragment.ARG_ITEM_ID);
            from.setText(whom != null ? whom : "");
        }
        cameraView.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        //NetworkCall.getInstance().callAudioAsync(from.getText().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isVideo) {
            cameraView.start();
        }

        onStartAudioRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isVideo) {
            cameraView.stop();
        }
        onDestroyAudioRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isVideo) {
            cameraView.destroy();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.accept:
                if (callInfo == null) {
                    return;
                }
                NetworkCall.NetworkCallProposalController.getInstance().accept(callInfo);
                break;
            case R.id.decline:
                if (callInfo == null) {
                    return;
                }
                NetworkCall.NetworkCallProposalController.getInstance().decline(callInfo);
                break;
            default:
                break;
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

            }
        }

        @Override
        public void onFail() {

        }
    };

    @Override
    public void callInfo(NetworkCall.NetworkCallProposalInfo info) {
        this.callInfo = info;
    }

    @Override
    protected void onSubscribeEvent(Object object) {
        super.onSubscribeEvent(object);
        if (object instanceof Voip) {
            if (((Voip) object).which == CALL_CANCEL)
                finish();
        }
    }
}
