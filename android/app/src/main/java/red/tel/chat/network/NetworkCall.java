package red.tel.chat.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import red.tel.chat.Model;
import red.tel.chat.Types;
import red.tel.chat.VoipBackend;
import red.tel.chat.io.IO;
import red.tel.chat.office365.Constants;

public class NetworkCall implements Types.SessionProtocol {
    private static final String TAG = NetworkCall.class.getSimpleName();
    private NetworkCallProposalReceiverProtocol ui;
    private static volatile NetworkCall ourInstance = null;

    public static NetworkCall getInstance() {
        if (ourInstance == null) {
            synchronized (NetworkCall.class) {
                if (ourInstance == null) {
                    ourInstance = new NetworkCall();
                }
            }
        }
        return ourInstance;
    }

    public NetworkCallInfo info;

    @Override
    public void start() {
        Log.d(TAG, "start: ");
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop: ");
    }

    public String counterpart() {
        return info.from();
    }

    // TODO: 9/11/17
    public NetworkCallInfo startCapture(String from, String to) {
        return new NetworkCallInfo(info.proposal, null, null);
    }

    private NetworkCallProposalInfo callAsync(String to, boolean audio, boolean video) {
        String id = UUID.fromString(Constants.CLIENT_ID).toString();
        NetworkCallProposalInfo info = new NetworkCallProposalInfo(id,
                Model.shared().getUsername(),
                to,
                audio,
                video);
        NetworkCallProposalController.getInstance().start(info);
        return info;
    }

    public NetworkCallProposalInfo callAudioAsync(String to) {
        return callAsync(to, true, false);
    }

    public NetworkCallProposalInfo callVideoAsync(String to) {
        return callAsync(to, true, true);
    }

    // TODO: 9/5/17
    public IO.IODataProtocol startOutput(NetworkVideo.NetworkVideoSessionInfo info) {
        return null;
    }

    // TODO: 9/5/17
    public IO.IODataProtocol startOutput(NetworkAudio.NetworkAudioSessionInfo info) {
        return null;
    }

    // TODO: 9/5/17  
    public static class NetworkCallController extends NetworkSingleCallSessionController<NetworkCall, NetworkCallInfo> {

        private static volatile NetworkCallController ourInstance = null;

        public static NetworkCallController getInstance() {
            if (ourInstance == null) {
                synchronized (NetworkCallController.class) {
                    if (ourInstance == null) {
                        ourInstance = new NetworkCallController();
                    }
                }
            }
            return ourInstance;
        }

        Types.SessionProtocol factory;

        public NetworkCallController() {
        }

        public NetworkCallController(Types.SessionProtocol factory) {
            this.factory = factory;
        }

        @Override
        protected String id(NetworkCallInfo info) {
            return info.id();
        }

        @Override
        protected NetworkCall create(NetworkCallInfo info) {
            return super.create(info);
        }

        public void startOutput(NetworkCallInfo call, IO.IODataProtocol audio, IO.IODataProtocol video) {
            if (!Objects.equals(this.callInfo.id(), call.id())) {
                return;
            }

            if (call.audioSession != null) {
                audio = this.call.startOutput(call.audioSession);
            }

            if (call.videoSession != null) {
                video = this.call.startOutput(call.videoSession);
            }
        }

        public void changeQuality(NetworkCallInfo info, int diff) {
            //call(info)
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Proposal
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class NetworkCallProposalInfo implements Parcelable {
        private String id;
        private String from;
        private String to;
        private boolean audio;
        private boolean video;

        public NetworkCallProposalInfo(String id, String from, String to, boolean audio, boolean video) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.audio = audio;
            this.video = video;
        }

        public String getId() {
            return id;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public boolean isAudio() {
            return audio;
        }

        public boolean isVideo() {
            return video;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.from);
            dest.writeString(this.to);
            dest.writeByte(this.audio ? (byte) 1 : (byte) 0);
            dest.writeByte(this.video ? (byte) 1 : (byte) 0);
        }

        protected NetworkCallProposalInfo(Parcel in) {
            this.id = in.readString();
            this.from = in.readString();
            this.to = in.readString();
            this.audio = in.readByte() != 0;
            this.video = in.readByte() != 0;
        }

        public static final Creator<NetworkCallProposalInfo> CREATOR = new Creator<NetworkCallProposalInfo>() {
            @Override
            public NetworkCallProposalInfo createFromParcel(Parcel source) {
                return new NetworkCallProposalInfo(source);
            }

            @Override
            public NetworkCallProposalInfo[] newArray(int size) {
                return new NetworkCallProposalInfo[size];
            }
        };
    }

    private static class NetworkCallSessionController<T, I> {
        protected T create(I info) {
            return null;
        }

        protected String id(I info) {
            return "";
        }

        protected T call(I info) {
            return null;
        }

        public void start(I info) {
        }

        public void stop(I info) {
        }
    }

    private static class NetworkSingleCallSessionController<T extends Types.SessionProtocol, I>
            extends NetworkCallSessionController<T, I> {
        T call;
        I callInfo;

        @Override
        public void start(I info) {
            super.start(info);
            stop();
            call = create(info);
            callInfo = info;
            try {
                if (call != null) {
                    call.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void stop(I info) {
            super.stop(info);
            if (this.callInfo == null) {
                return;
            }
            I callInfo = this.callInfo;
            if (!Objects.equals(id(callInfo), id(info))) {
                return;
            }

            if (call != null) {
                call.stop();
            }
            this.call = null;
            this.callInfo = null;
        }

        private void stop() {
            if (callInfo == null) {
                return;
            }
            stop(callInfo);
        }

        @Override
        protected T call(I info) {
            I callInfo = this.callInfo;
            if (!Objects.equals(id(callInfo), id(info))) {
                return null;
            }
            return call;
        }
    }

    interface NetworkCallProposalProtocol extends Types.SessionProtocol {
        void accept(NetworkCallProposalInfo info);

        void decline();
    }

    public interface NetworkCallProposalReceiverProtocol {
        void callInfo(NetworkCallProposalInfo info);
    }

    public static class NetworkCallProposal implements NetworkCallProposalProtocol {
        public NetworkCallProposalInfo info;
        //NetworkCallProposalReceiverProtocol ui;
        public NetworkCallProposal(NetworkCallProposalInfo info) {
            this.info = info;
        }

        public NetworkCallProposal() {}

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void accept(NetworkCallProposalInfo info) {
            this.info = info;
        }

        @Override
        public void decline() {

        }
    }

    public static class NetworkCallProposalController extends
            NetworkSingleCallSessionController<NetworkCallProposal, NetworkCallProposalInfo> {
        //static nested class
        private NetworkCallProposalController(){}
        private static class SingletonHelper {
            private static final NetworkCallProposalController INSTANCE = new NetworkCallProposalController();
        }

        public static synchronized NetworkCallProposalController getInstance() {
            return SingletonHelper.INSTANCE;
        }

        private Handler handler;
        private Runnable finalizer;

        @Override
        protected NetworkCallProposal create(NetworkCallProposalInfo info) {
            return new NetworkOutgoingCallProposal(info);
        }

        @Override
        protected String id(NetworkCallProposalInfo info) {
            return info.id;
        }

        @Override
        public void start(NetworkCallProposalInfo info) {
            super.start(info);
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
                finalizer = new Runnable() {
                    @Override
                    public void run() {
                        timeout(info);
                    }
                };
                handler.postDelayed(finalizer, 10000);
            }
        }

        //accept call in
        public void accept(NetworkCallProposalInfo info) {
            if (call != null) {
                call.accept(info);
                handler.removeCallbacks(finalizer);
                handler = null;
                finalizer = null;
            }
            //call = null;
        }

        public void decline(NetworkCallProposalInfo info) {
            if (call != null) {
                call.decline();
            }
            call = null;
        }

        //auto cancels a 10 second call, if? No reply
        private void timeout(NetworkCallProposalInfo info) {
            stop(info);
            Log.d(TAG, "timeout: STOP");
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Call
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class NetworkCallInfo {
        public NetworkCallProposalInfo proposal;
        public NetworkAudio.NetworkAudioSessionInfo audioSession;
        public NetworkVideo.NetworkVideoSessionInfo videoSession;

        public NetworkCallInfo(NetworkCallProposalInfo proposal,
                               NetworkAudio.NetworkAudioSessionInfo audioSession,
                               NetworkVideo.NetworkVideoSessionInfo videoSession) {
            this.proposal = proposal;
            this.audioSession = audioSession;
            this.videoSession = videoSession;
        }

        public NetworkCallInfo(NetworkCallProposalInfo proposal, NetworkAudio.NetworkAudioSessionInfo audioSession) {
            new NetworkCallInfo(proposal, audioSession, null);
        }

        public NetworkCallInfo(NetworkCallProposalInfo proposal, NetworkVideo.NetworkVideoSessionInfo videoSession) {
            new NetworkCallInfo(proposal, null, videoSession);
        }

        public NetworkCallInfo(NetworkCallProposalInfo proposal) {
            new NetworkCallInfo(proposal, null, null);
        }

        public String id() {
            return proposal.getId();
        }

        public String from() {
            return proposal.getFrom();
        }

        public String to() {
            return proposal.getTo();
        }
    }

    public static class NetworkOutgoingCallProposal extends NetworkCallProposal {


        public NetworkOutgoingCallProposal(NetworkCallProposalInfo info) {
            super(info);
        }

        @Override
        public void start() {
            super.start();
            VoipBackend.getInstance().sendCallProposal(info.to, info);
        }

        @Override
        public void stop() {
            VoipBackend.getInstance().sendCallCancel(info.to, info);
            super.stop();
        }

        @Override
        public void accept(NetworkCallProposalInfo info) {
            VoipBackend.getInstance().sendCallAccept(info.from, info);
            super.accept(info);
        }

        @Override
        public void decline() {
            VoipBackend.getInstance().sendCallDecline(info.from, info);
            super.decline();
        }
    }

    public static class NetworkIncomingCallProposal extends NetworkCallProposal {

        //static nested class
        private NetworkIncomingCallProposal(){
            super();
        }
        private static class SingletonHelper {
            private static final NetworkIncomingCallProposal INSTANCE = new NetworkIncomingCallProposal();
        }

        public static synchronized NetworkIncomingCallProposal getInstance() {
            return SingletonHelper.INSTANCE;
        }


        public NetworkIncomingCallProposal(NetworkCallProposalInfo info) {
            super(info);
        }

        @Override
        public void stop() {
            super.stop();
        }

        @Override
        public void accept(NetworkCallProposalInfo info) {
            super.accept(info);
        }

        @Override
        public void decline() {
            super.decline();
        }
    }
}
