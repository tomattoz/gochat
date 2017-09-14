package red.tel.chat;


import android.util.Log;

import red.tel.chat.generated_protobuf.AVSession;
import red.tel.chat.generated_protobuf.Call;
import red.tel.chat.generated_protobuf.Voip;
import red.tel.chat.io.IO;
import red.tel.chat.network.IncomingCallProposalController;
import red.tel.chat.network.NetworkAudio;
import red.tel.chat.network.NetworkBase;
import red.tel.chat.network.NetworkCallController;
import red.tel.chat.network.NetworkCallInfo;
import red.tel.chat.network.NetworkCallProposalInfo;
import red.tel.chat.network.OutgoingCallProposalController;

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
                    getAV(voip);
                    break;
                case AudioSession:
                    break;
                case VideoSession:
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
                    getsOutgoingCallStart(voip);
                    break;
                case CALL_START_INCOMING:
                    getsIncomingCallStart(voip);
                    break;
                case CALL_QUALITY:
                    break;
                case CALL_STOP:
                    getsCallStop(voip);
                    break;
                default:
                    Log.e(TAG, "no handler for " + voip.which);
                    break;
            }
        } catch (Exception exception) {
            Log.e(TAG, exception.getLocalizedMessage());
        }
    }

    /**
     * @param voip
     * @method nhan yeu cau mot cuoc goi den
     */
    private void getsCallProposal(Voip voip) {
        IncomingCallProposalController.getInstance().start(callProposalInfo(voip));
        RxBus.getInstance().sendEvent(callProposalInfo(voip));
    }

    /**
     * @param voip
     * @method nhan tin hieu huy cuoc goi den
     */
    private void getsCallCancel(Voip voip) {
        IncomingCallProposalController.getInstance().stop(callProposalInfo(voip));
        OutgoingCallProposalController.getInstance().stop(callProposalInfo(voip));
        RxBus.getInstance().sendEvent(voip);
    }

    /**
     * @param voip
     * @method nhan tin hieu dong y cuoc goi di
     */
    private void getsCallAccept(Voip voip) {
        OutgoingCallProposalController.getInstance().accept(callProposalInfo(voip));
        //NetworkOutgoingCall.getInstance().start();
    }

    /**
     * @param voip
     * @method nhan tin hieu tu choi cuoc goi di
     */
    private void getsCallDecline(Voip voip) {
        OutgoingCallProposalController.getInstance().decline(callProposalInfo(voip));
        RxBus.getInstance().sendEvent(voip);
    }

    private void getsCallStop(Voip voip) {
        NetworkCallController.getInstance().stop(new NetworkCallInfo(callProposalInfo(voip)));
        RxBus.getInstance().sendEvent(voip);
    }

    private void getsOutgoingCallStart(Voip voip) {
        NetworkCallController.getInstance().start(callInfo(voip));
        startCallOutput(voip, NetworkCallController.getInstance(), audio, video);
    }

    private void getsIncomingCallStart(Voip voip) {
        startCallOutput(voip, NetworkCallController.getInstance(), audio, video);
    }

    private void getAV(Voip voip) {
        if (voip.call.audio) {
            audio.process(voip.audioSession.sid, voip.av.audio.image.data);
        } else if (voip.call.video) {
            video.process(voip.videoSession.sid, voip.av.video.image.data);
        }
    }

    private void getsAudioSession(Voip voip) {
        try {
            if (voip.audioSession.active) {
                audio.removeAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getsVideoSession(Voip voip) {

    }

    private NetworkCallProposalInfo callProposalInfo(Voip voip) {
        return new NetworkCallProposalInfo(voip.call.key,
                voip.call.from,
                voip.call.to,
                voip.call.audio,
                voip.call.video);
    }

    // TODO: 9/5/17
    public NetworkCallInfo callInfo(Voip voip) {
        return new NetworkCallInfo(callProposalInfo(voip));
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
    private void startCallOutput(Voip voip,
                                 NetworkCallController call,
                                 NetworkBase.NetworkInput audio,
                                 NetworkBase.NetworkInput video) {
        IO.IODataProtocol audio_ = data -> {

        };
        IO.IODataProtocol video_ = data -> {

        };
        try {
            //call.startOutput(callInfo(), audio_, video_);
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
    public void sendCallProposal(String to, NetworkCallProposalInfo info) {
        Call call = new Call.Builder()
                .key(info.getId())
                .from(info.getFrom())
                .to(info.getTo())
                .audio(info.isAudio())
                .video(info.isVideo()).build();

        byte[] data = new Voip.Builder().which(Voip.Which.CALL_PROPOSAL).call(call).build().encode();
        WireBackend.shared().send(data, to);
    }

    public void sendCallCancel(String to, NetworkCallProposalInfo info) {
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

    /**
     * @param to
     * @param info
     * @method sendCallDecline
     */
    public void sendCallDecline(String to, NetworkCallProposalInfo info) {
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
     * @param to   String
     * @param info {@link NetworkCallProposalInfo}
     * @method sendCallAccept
     */
    public void sendCallAccept(String to, NetworkCallProposalInfo info) {
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
     * @param to   String
     * @param info {@link NetworkCallInfo}
     * @method sendCallStop
     */
    public void sendCallStop(String to, NetworkCallInfo info) {
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
     * @param to   String
     * @param info {@link NetworkCallInfo}
     * @method sendIncomingCallStart
     */
    public void sendIncomingCallStart(String to, NetworkCallInfo info) {
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
     * @param to   String
     * @param info {@link NetworkCallInfo}
     * @method sendOutgoingCallStart
     */
    public void sendOutgoingCallStart(String to, NetworkCallInfo info) {
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
        private NetworkCallInfo info;
        private Call call;
        private AVSession.Builder audioSession;
        private AVSession.Builder videoSession;

        public DataCall(NetworkCallInfo info) {
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
