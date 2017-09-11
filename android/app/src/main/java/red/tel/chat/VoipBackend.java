package red.tel.chat;


import android.util.Log;

import red.tel.chat.generated_protobuf.AVSession;
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
        RxBus.getInstance().sendEvent(voip);
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
        RxBus.getInstance().sendEvent(EventBus.Event.STOP_CALL);
    }

    public void sendCallDecline(String to, NetworkCall.NetworkCallProposalInfo info) {
        Call call = new Call.Builder()
                .key(info.getId())
                .from(info.getFrom())
                .to(info.getTo())
                .audio(info.isAudio())
                .video(info.isVideo()).build();

        byte[] data = new Voip.Builder().which(Voip.Which.CALL_DECLINE).call(call).build().encode();
        WireBackend.shared().send(data, to);
    }

    /**
     * @method sendCallAccept
     * @param to String
     * @param info {@link NetworkCall.NetworkCallProposalInfo}
     */
    public void sendCallAccept(String to, NetworkCall.NetworkCallProposalInfo info) {
        Call call = new Call.Builder()
                .key(info.getId())
                .from(info.getFrom())
                .to(info.getTo())
                .audio(info.isAudio())
                .video(info.isVideo()).build();

        byte[] data = new Voip.Builder().which(Voip.Which.CALL_ACCEPT).call(call).build().encode();
        WireBackend.shared().send(data, to);
    }

    /**
     * @method sendIncomingCallStart
     * @param to String
     * @param info {@link NetworkCall.NetworkCallInfo}
     */
    public void sendIncomingCallStart(String to, NetworkCall.NetworkCallInfo info) {
        DataCall dataCall = new DataCall(info).invoke();
        AVSession.Builder audioSession = dataCall.getAudioSession();
        AVSession.Builder videoSession = dataCall.getVideoSession();
        Call call = dataCall.getCall();

        byte[] data = new Voip.Builder()
                .which(Voip.Which.CALL_START_INCOMING)
                .audioSession(audioSession.build())
                .videoSession(videoSession.build())
                .call(call)
                .build()
                .encode();
        WireBackend.shared().send(data, to);
    }

    /**
     * @method sendCallStop
     * @param to String
     * @param info {@link NetworkCall.NetworkCallInfo}
     */
    public void sendCallStop(String to, NetworkCall.NetworkCallInfo info) {
        DataCall dataCall = new DataCall(info).invoke();
        AVSession.Builder audioSession = dataCall.getAudioSession();
        AVSession.Builder videoSession = dataCall.getVideoSession();
        Call call = dataCall.getCall();

        byte[] data = new Voip.Builder()
                .which(Voip.Which.CALL_STOP)
                .audioSession(audioSession.build())
                .videoSession(videoSession.build())
                .call(call)
                .build()
                .encode();
        WireBackend.shared().send(data, to);
    }

    /**
     * @method sendOutgoingCallStart
     * @param to String
     * @param info {@link NetworkCall.NetworkCallInfo}
     */
    public void sendOutgoingCallStart(String to, NetworkCall.NetworkCallInfo info) {
        DataCall dataCall = new DataCall(info).invoke();
        AVSession.Builder audioSession = dataCall.getAudioSession();
        AVSession.Builder videoSession = dataCall.getVideoSession();
        Call call = dataCall.getCall();

        byte[] data = new Voip.Builder()
                .which(Voip.Which.CALL_START_OUTGOING)
                .audioSession(audioSession.build())
                .videoSession(videoSession.build())
                .call(call)
                .build()
                .encode();
        WireBackend.shared().send(data, to);
    }

    /**
     * class inner call data in/out
     * return data info call, audio session/video session
     */
    private class DataCall {
        private NetworkCall.NetworkCallInfo info;
        private Call call;
        private AVSession.Builder audioSession;
        private AVSession.Builder videoSession;

        public DataCall(NetworkCall.NetworkCallInfo info) {
            this.info = info;
        }

        public Call getCall() {
            return call;
        }

        public AVSession.Builder getAudioSession() {
            return audioSession;
        }

        public AVSession.Builder getVideoSession() {
            return videoSession;
        }

        public DataCall invoke() {
            call = new Call.Builder()
                    .key(info.proposal.getId())
                    .from(info.proposal.getFrom())
                    .to(info.proposal.getTo())
                    .audio(info.proposal.isAudio())
                    .video(info.proposal.isVideo()).build();
            audioSession = new AVSession.Builder();
            videoSession = new AVSession.Builder();
            if (info.audioSession != null) {
                audioSession
                        .active(true)
                        .sid(info.audioSession.id.getSid())
                        .gid(info.audioSession.id.getGid());
                if (info.audioSession.formatData != null) {
                    audioSession.data(info.audioSession.formatData);
                }
            }

            if (info.videoSession != null) {
                videoSession
                        .active(true)
                        .sid(info.videoSession.id.getSid())
                        .gid(info.videoSession.id.getGid());
                if (info.videoSession.formatData != null) {
                    videoSession.data(info.videoSession.formatData);
                }
            }
            return this;
        }
    }
}
