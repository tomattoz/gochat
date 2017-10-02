package red.tel.chat.ui.activitys;


import android.hardware.Camera;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import red.tel.chat.R;
import red.tel.chat.VoipBackend;
import red.tel.chat.generated_protobuf.Image;
import red.tel.chat.generated_protobuf.Voip;
import red.tel.chat.io.IO;
import red.tel.chat.network.NetworkIncomingCall;
import red.tel.chat.network.IncomingCallProposalController;
import red.tel.chat.network.NetworkCallProposalInfo;
import red.tel.chat.ui.fragments.ItemDetailFragment;

import static red.tel.chat.generated_protobuf.Voip.Which.CALL_CANCEL;
import static red.tel.chat.generated_protobuf.Voip.Which.CALL_STOP;
import static red.tel.chat.ui.fragments.ItemDetailFragment.CALL_INFO;

public class IncomingCallActivity extends BaseCall implements View.OnClickListener, IO.IODataProtocol {
    public static final String TYPE_CALL = "type_call";
    private static final String TAG = IncomingCallActivity.class.getSimpleName();
    private TextView from;
    private Button btnAccept;
    private Button btnDecline;
    private View viewRoot;
    private boolean isVideo = false;
    private NetworkCallProposalInfo callProposalInfo;
    private boolean isAccept = false;
    private IO.IOID ioid;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        from = findViewById(R.id.from);
        mCameraTextureView = findViewById(R.id.camera);
        btnAccept = findViewById(R.id.accept);
        btnDecline = findViewById(R.id.decline);
        viewRoot = findViewById(R.id.root);
        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isVideo = bundle.getBoolean(TYPE_CALL);
            String whom = bundle.getString(ItemDetailFragment.ARG_ITEM_ID);
            from.setText(whom != null ? whom : "");
            callProposalInfo = bundle.getParcelable(CALL_INFO);
        }
        mCameraTextureView.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        VoipBackend.getInstance().setIoDataProtocol(this);
        ioid = new IO.IOID(callProposalInfo.to, callProposalInfo.from, callProposalInfo.getId(), callProposalInfo.getId());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.accept:
                if (callProposalInfo == null) {
                    return;
                }
                IncomingCallProposalController.getInstance().accept(callProposalInfo);

                btnAccept.setVisibility(View.GONE);
                btnDecline.setText("Cancel");
                isAccept = true;
                break;
            case R.id.decline:
                if (callProposalInfo == null) {
                    return;
                }
                if (isAccept) {
                    NetworkIncomingCall.getInstance().stop();
                    //NetworkCallController.getInstance().stop(NetworkCallController.getInstance().callInfo);
                    isAccept = false;
                } else {
                    IncomingCallProposalController.getInstance().decline(callProposalInfo);
                }
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCallBackRecord(ByteBuffer buffer, ShortBuffer[] samples, byte[] data) {
        VoipBackend.getInstance().sendDataAudioToServerWhenAccept(data, ioid);
    }

    @Override
    protected void onCallVideoData(byte[] data, Camera.Size size) {
       VoipBackend.getInstance().sendDataVideoToServerWhenAccept(data, size, ioid);
    }

    @Override
    protected boolean isVideo() {
        return isVideo;
    }

    @Override
    protected void onSubscribeEvent(Object object) {
        //super.onSubscribeEvent(object);
        if (object instanceof Voip) {
            if (((Voip) object).which == CALL_CANCEL || ((Voip) object).which == CALL_STOP)
                finish();
        }
    }

    /**
     * @method startOut {@link VoipBackend#startCallOutput}
     */
    @Override
    public void startOut() {
        requestPermissions();
    }

    /**
     * @method processAudio {@link VoipBackend#getAV(Voip)}
     * @param data
     */
    @Override
    public void processAudio(byte[] data) {
        Log.d(TAG, "processAudio: ");
        if (audioTrack != null) {
            if (AudioTrack.PLAYSTATE_PLAYING != audioTrack.getPlayState()) {
                audioTrack.play();
                Log.d(TAG, "Play audio: ");
            }
            int size = audioTrack.write(data, 0, data.length);
            if (data.length != size) {
                Log.i(TAG, "Failed to send all data to audio output, expected size: " +
                        data.length + ", actual size: " + size);
            }
        }
    }

    @Override
    public void processVideo(Image data) {
        Log.d(TAG, "processVideo: "+ Arrays.toString(data.data.toByteArray()) + " " + data.height + " " + data.width);
    }

    @Override
    public void setSurfaceTextureListener(TextureView.SurfaceTextureListener listener) {
        if (mCameraTextureView != null) {
            mCameraTextureView.setSurfaceTextureListener(listener);
        }
    }

    @Override
    public TextureView getTextureView() {
        return mCameraTextureView;
    }

    @Override
    public int getUploadPanelParentWidth() {
        return viewRoot != null ? viewRoot.getWidth() : 0;
    }

    @Override
    public int getUploadPanelParentHeight() {
        return viewRoot != null ? viewRoot.getHeight() : 0;
    }

    @Override
    public void getDataVideo(byte[] bytes, Camera.Size size) {
        onCallVideoData(bytes, size);
    }

    @Override
    protected void onCallBackRecord(byte[] data) {
        VoipBackend.getInstance().sendDataAudioToServerWhenAccept(data, ioid);
    }
}
