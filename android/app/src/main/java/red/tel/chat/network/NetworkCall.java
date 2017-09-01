package red.tel.chat.network;

import android.os.Handler;
import android.os.Looper;

import java.util.Objects;
import java.util.UUID;

import red.tel.chat.Model;
import red.tel.chat.Types;
import red.tel.chat.office365.Constants;

public class NetworkCall {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Proposal
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class NetworkCallProposalInfo {
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

        public void start(I info) {}

        public void stop(I info){}
    }

    private static class NetworkSingleCallSessionController<T extends Types.SessionProtocol, I> extends NetworkCallSessionController<T, I> {
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

    interface NetworkCallProposalReceiverProtocol {
        void  callInfo(NetworkCallProposalInfo info);
    }

    public static class NetworkCallProposal implements NetworkCallProposalProtocol {
        private NetworkCallProposalInfo info;
        private NetworkCallProposalReceiverProtocol ui;

        public NetworkCallProposal(NetworkCallProposalInfo info) {
            this.info = info;
        }

        public NetworkCallProposal(NetworkCallProposalInfo info, NetworkCallProposalReceiverProtocol ui) {
            this.info = info;
            this.ui = ui;
        }

        @Override
        public void start() {
            if (ui != null) {
                ui.callInfo(info);
            }
        }

        @Override
        public void stop() {
            // TODO: 8/31/17
            ui.callInfo(null);
        }

        @Override
        public void accept(NetworkCallProposalInfo info) {
            this.info = info;
            ui.callInfo(null);
        }

        @Override
        public void decline() {
            ui.callInfo(null);
        }
    }

    public static class NetworkCallProposalController extends NetworkSingleCallSessionController<NetworkCallProposal, NetworkCallProposalInfo> {

        private static volatile NetworkCallProposalController ourInstance = null;

        public static NetworkCallProposalController getInstance() {
            if (ourInstance == null) {
                synchronized (NetworkCallProposalController.class) {
                    if (ourInstance == null) {
                        ourInstance = new NetworkCallProposalController();
                    }
                }
            }
            return ourInstance;
        }

        @Override
        protected NetworkCallProposal create(NetworkCallProposalInfo info) {
            return super.create(info);
        }

        @Override
        protected String id(NetworkCallProposalInfo info) {
            return info.id;
        }

        @Override
        public void start(NetworkCallProposalInfo info) {
            super.start(info);
            new Handler(Looper.getMainLooper()).postDelayed(() -> timeout(info),10000);
        }

        public void accept(NetworkCallProposalInfo info) {
            if (call != null) {
                call.accept(info);
            }
            call = null;
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
        }
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

    private NetworkCallProposalInfo callAudioAsync(String to) {
        return callAsync(to, true, false);
    }

    private NetworkCallProposalInfo callVideoAsync(String to) {
        return callAsync(to, true, true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Call
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class NetworkCallInfo {
        private NetworkCallProposalInfo proposal;
        private NetworkAudio.NetworkAudioSessionInfo audioSession;
    }
}
