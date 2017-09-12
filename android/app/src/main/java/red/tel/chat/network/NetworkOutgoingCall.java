package red.tel.chat.network;

import red.tel.chat.VoipBackend;

/**
 * Created by vmodev on 9/11/17.
 */

public class NetworkOutgoingCall extends NetworkCall {
    private NetworkOutgoingCall() {
    }

    public static synchronized NetworkOutgoingCall getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public void stop() {
        super.stop();
        VoipBackend.getInstance().sendCallStop(info.to(), info);
    }

    @Override
    public NetworkCallInfo startCapture(String from, String to) {
        NetworkCallInfo info = super.startCapture(from, to);
        VoipBackend.getInstance().sendOutgoingCallStart(info.to(), info);
        return info;
    }

    private static class SingletonHelper {
        private static final NetworkOutgoingCall INSTANCE = new NetworkOutgoingCall();
    }
}
