package red.tel.chat;


import android.util.Log;

import red.tel.chat.generated_protobuf.Call;
import red.tel.chat.generated_protobuf.Voip;
import red.tel.chat.io.IO;
import red.tel.chat.network.NetworkAudio;
import red.tel.chat.network.NetworkBase;
import red.tel.chat.network.NetworkCall;

public class VoipBackend {
    private static final String TAG = VoipBackend.class.getSimpleName();
    private static NetworkBase.NetworkInput audio;
    private static NetworkBase.NetworkInput video;

    private static volatile VoipBackend ourInstance = null;

    public static VoipBackend getInstance() {
        if (ourInstance == null) {
            synchronized (VoipBackend.class) {
                if (ourInstance == null) {
                    ourInstance = new VoipBackend();
                }
            }
        }
        return ourInstance;
    }

    void onReceiveFromPeer(byte[] binary, String peerId) {
        try {
            Voip voip = Voip.ADAPTER.decode(binary);
            Log.d(TAG, "incoming " + voip.which + " from " + peerId);

            switch (voip.which) {
                case TEXT:
                    Model.shared().incomingFromPeer(voip, peerId);
                    break;
                case AV:
                    break;
                case CALL_PROPOSAL:
                    getsCallProposal(voip);
                    break;
                case CALL_CANCEL:
                    getsCallCancel(voip);
                    break;
                case CALL_ACCEPT:
                    getsCallAccept(voip);
                    break;
                case CALL_DECLINE:
                    getsCallDecline(voip);
                    break;
                case CALL_START_OUTGOING:
                    break;
                case CALL_START_INCOMING:
                    break;
                case CALL_QUALITY:
                    break;
                default:
                    Log.e(TAG, "no handler for " + voip.which);
                    break;
            }
        } catch (Exception exception) {
            Log.e(TAG, exception.getLocalizedMessage());
        }
    }

    private void getsCallProposal(Voip voip) {
        NetworkCall.NetworkCallProposalController.getInstance().start(callProposalInfo(voip));
        RxBus.getInstance().sendEvent(callProposalInfo(voip));
    }

    private void getsCallCancel(Voip voip) {
        NetworkCall.NetworkCallProposalController.getInstance().stop(callProposalInfo(voip));
        RxBus.getInstance().sendEvent(voip);
    }

    //accept in call
    private void getsCallAccept(Voip voip) {
        NetworkCall.NetworkCallProposalController.getInstance().accept(callProposalInfo(voip));
    }

    private void getsCallDecline(Voip voip) {
        NetworkCall.NetworkCallProposalController.getInstance().decline(callProposalInfo(voip));
    }

    private void getsOutgoingCallStart(Voip voip) {
        NetworkCall.NetworkCallController.getInstance().start(callInfo());
    }

    private NetworkCall.NetworkCallProposalInfo callProposalInfo(Voip voip) {
        return new NetworkCall.NetworkCallProposalInfo(voip.call.key,
                voip.call.from,
                voip.call.to,
                voip.call.audio,
                voip.call.video);
    }

    // TODO: 9/5/17
    private NetworkCall.NetworkCallInfo callInfo() {
        return new NetworkCall.NetworkCallInfo(callProposalInfo());
    }

    private NetworkAudio.NetworkAudioSessionInfo audioSessionInfo() {
        Voip voip = new Voip.Builder().build();
        if (voip.audioSession.active) {
            return new NetworkAudio.NetworkAudioSessionInfo(audioSessionID());
        } else {
            return null;
        }
    }

    private IO.IOID audioSessionID() {
        Voip voip = new Voip.Builder().build();
        return new IO.IOID(voip.call.from, voip.call.to, voip.audioSession.sid, voip.audioSession.gid);
    }

    private IO.IOID videoSessionID() {
        Voip voip = new Voip.Builder().build();
        return new IO.IOID(voip.call.from, voip.call.to, voip.videoSession.sid, voip.videoSession.gid);
    }

    // TODO: 9/5/17  
    private NetworkCall.NetworkCallProposalInfo callProposalInfo() {
        Voip voip = new Voip.Builder().build();
        return new NetworkCall.NetworkCallProposalInfo(voip.call.key,
                voip.call.from,
                voip.call.to,
                voip.call.audio,
                voip.call.video);
    }

    // TODO: 9/5/17
    private void startCallOutput(Voip voip,
                                 NetworkCall.NetworkCallController call,
                                 NetworkBase.NetworkInput audio,
                                 NetworkBase.NetworkInput video) {
        IO.IODataProtocol audio_ = data -> {

        };
        IO.IODataProtocol video_ = data -> {

        };
        try {
            call.startOutput(callInfo(), audio_, video_);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (audio_ != null) {
            audio.add(voip.audioSession.sid, audio_);
        }

        if (video_ != null) {
            video.add(voip.videoSession.sid, video_);
        }
    }

    //set up call out
    public void sendCallProposal(String to, NetworkCall.NetworkCallProposalInfo info) {
        Call call = new Call.Builder()
                .key(info.getId())
                .from(info.getFrom())
                .to(info.getTo())
                .audio(info.isAudio())
                .video(info.isVideo()).build();

        byte[] data = new Voip.Builder().which(Voip.Which.CALL_PROPOSAL).call(call).build().encode();
        WireBackend.shared().send(data, to);
    }

    public void sendCallCancel(String to, NetworkCall.NetworkCallProposalInfo info) {
        Call call = new Call.Builder()
                .key(info.getId())
                .from(info.getFrom())
                .to(info.getTo())
                .audio(info.isAudio())
                .video(info.isVideo()).build();

        byte[] data = new Voip.Builder().which(Voip.Which.CALL_CANCEL).call(call).build().encode();
        WireBackend.shared().send(data, to);
    }
}
