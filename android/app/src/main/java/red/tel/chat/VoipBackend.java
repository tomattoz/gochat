package red.tel.chat;


import android.hardware.Camera;
import android.util.Log;

import java.nio.ShortBuffer;

import okio.ByteString;
import red.tel.chat.generated_protobuf.AVSession;
import red.tel.chat.generated_protobuf.AudioSample;
import red.tel.chat.generated_protobuf.Av;
import red.tel.chat.generated_protobuf.Call;
import red.tel.chat.generated_protobuf.Image;
import red.tel.chat.generated_protobuf.VideoSample;
import red.tel.chat.generated_protobuf.Voip;
import red.tel.chat.io.IO;
import red.tel.chat.network.IncomingCallProposalController;
import red.tel.chat.network.NetworkAudio;
import red.tel.chat.network.NetworkBase;
import red.tel.chat.network.NetworkCallController;
import red.tel.chat.network.NetworkCallInfo;
import red.tel.chat.network.NetworkCallProposalInfo;
import red.tel.chat.network.NetworkVideo;
import red.tel.chat.network.OutgoingCallProposalController;

public class VoipBackend{
    private static final String TAG = VoipBackend.class.getSimpleName();
    private IO.IODataProtocol ioDataProtocol;

    public void setIoDataProtocol(IO.IODataProtocol ioDataProtocol) {
        this.ioDataProtocol = ioDataProtocol;
    }

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
                    Log.d(TAG, "onReceiveFromPeer: AV");
                    getAV(voip);
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
        RxBus.getInstance().sendEvent("ACCEPT");
        //NetworkOutgoingCall.getInstance().start();
        //ioid = new IO.IOID(callProposalInfo(voip).from, callProposalInfo(voip).to, callProposalInfo(voip).getId(), callProposalInfo(voip).getId());
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
        //NetworkCallController.getInstance().start(callInfo(voip));
        startCallOutput(voip, NetworkCallController.getInstance());
    }

    private void getsIncomingCallStart(Voip voip) {
        //NetworkCallController.getInstance().start(callInfo(voip));
        startCallOutput(voip, NetworkCallController.getInstance());
    }

    private synchronized void getAV(Voip voip) {
        if (voip.call.audio) {
            ioDataProtocol.processAudio(voip.av.audio.image.data.toByteArray());
        }
        if (voip.call.video) {
            ioDataProtocol.processVideo(voip.av.video.image);
            Log.d(TAG, "getAV: " + voip.av.video.image.data);
        }

    }


    private NetworkCallProposalInfo callProposalInfo(Voip voip) {
        return new NetworkCallProposalInfo(voip.call.key,
                voip.call.from,
                voip.call.to,
                voip.call.audio,
                voip.call.video);
    }

    public NetworkCallInfo callInfo(Voip voip) {
        return new NetworkCallInfo(callProposalInfo(voip), audioSessionInfo(voip), videoSessionInfo(voip));
    }

    private NetworkAudio.NetworkAudioSessionInfo audioSessionInfo(Voip voip) {
        /*if (voip.audioSession.active) {
            return new NetworkAudio.NetworkAudioSessionInfo(audioSessionID(voip));
        } else {
            return null;
        }*/
        return new NetworkAudio.NetworkAudioSessionInfo(audioSessionID(voip));
    }

    private NetworkVideo.NetworkVideoSessionInfo videoSessionInfo(Voip voip) {
        if (voip.audioSession.active) {
            return new NetworkVideo.NetworkVideoSessionInfo(videoSessionID(voip));
        } else {
            return null;
        }
    }

    private IO.IOID audioSessionID(Voip voip) {
        return new IO.IOID(voip.call.from, voip.call.to, voip.audioSession.sid, voip.audioSession.gid);
    }

    private IO.IOID videoSessionID(Voip voip) {
        return new IO.IOID(voip.call.from, voip.call.to, voip.videoSession.sid, voip.videoSession.gid);
    }

    // TODO: 9/5/17
    private void startCallOutput(Voip voip, NetworkCallController call) {
        ioDataProtocol.startOut();
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

    public void sendVideoSession(NetworkVideo.NetworkVideoSessionInfo sessionInfo, boolean isActive) {
        AVSession.Builder avSession = new AVSession.Builder()
                .sid(sessionInfo.id.getSid())
                .gid(sessionInfo.id.getGid())
                .active(isActive);
        if (sessionInfo.formatData != null) {
            avSession.data(sessionInfo.formatData);
        }

        byte[] data = new Voip.Builder()
                .which(Voip.Which.VideoSession)
                .videoSession(avSession.build()).build().encode();
        WireBackend.shared().send(data, sessionInfo.id.getTo());
    }

    public void sendAudioSession(NetworkAudio.NetworkAudioSessionInfo sessionInfo, boolean isActive) {
        AVSession.Builder avSession = new AVSession.Builder()
                .sid(sessionInfo.id.getSid())
                .gid(sessionInfo.id.getGid())
                .active(isActive);
        if (sessionInfo.formatData != null) {
            avSession.data(sessionInfo.formatData);
        }

        byte[] data = new Voip.Builder()
                .which(Voip.Which.AudioSession)
                .audioSession(avSession.build()).build().encode();
        WireBackend.shared().send(data, sessionInfo.id.getTo());
    }

    private synchronized void sendAudio(IO.IOID ioid, ByteString data) {
        Log.d(TAG, "sendAudio: " + data.asByteBuffer());
        try {
            Call call = new Call.Builder()
                    .video(false)
                    .audio(true)
                    .build();
            Image image = new Image.Builder()
                    .data(data)
                    .build();
            AudioSample media = new AudioSample.Builder()
                    .image(image)
                    .build();
            Av av = new Av.Builder()
                    .audio(media)
                    .build();
            byte[] dataByte = new Voip.Builder()
                    .which(Voip.Which.AV)
                    .av(av)
                    .call(call)
                    .audioSession(new AVSession.Builder().sid(ioid.getSid()).build())
                    .build()
                    .encode();
            WireBackend.shared().send(dataByte, ioid.getTo());
            Log.d(TAG, "sendAudio: " + ioid.getSid() + " " + ioid.getTo());
        }catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "sendAudio: Exception ");
        }
    }

    private synchronized void sendVideo(IO.IOID ioid, Camera.Size size, ByteString data) {
        try {
            Call call = new Call.Builder()
                    .video(true)
                    .audio(true)
                    .build();
            Image image = new Image.Builder()
                    .data(data)
                    .height((long) size.height)
                    .width((long) size.width)
                    .build();
            VideoSample media = new VideoSample.Builder()
                    .image(image)
                    .build();
            Av av = new Av.Builder()
                    .video(media)
                    .build();
            byte[] dataByte = new Voip.Builder()
                    .which(Voip.Which.AV)
                    .av(av)
                    .call(call)
                    .videoSession(new AVSession.Builder().sid(ioid.getSid()).build())
                    .build()
                    .encode();
            WireBackend.shared().send(dataByte, ioid.getTo());
            Log.d(TAG, "sendVideo: ");
        }catch (Exception e) {
            Log.e(TAG, "sendVideo: ", e );
        }
    }

    public synchronized void sendDataAudioToServerWhenAccept(byte[] buffer, IO.IOID ioid) {
        okio.ByteString av = okio.ByteString.of(buffer);
        VoipBackend.getInstance().sendAudio(ioid, av);
    }

    public synchronized void sendDataVideoToServerWhenAccept(byte[] buffer, Camera.Size size, IO.IOID ioid) {
        try {
            okio.ByteString av = okio.ByteString.of(buffer);
            VoipBackend.getInstance().sendVideo(ioid, size, av);
        }catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }
}
