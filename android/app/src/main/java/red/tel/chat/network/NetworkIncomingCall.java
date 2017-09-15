package red.tel.chat.network;

import red.tel.chat.VoipBackend;

public class NetworkIncomingCall extends NetworkCall {

    private NetworkIncomingCall() {
    }

    public static synchronized NetworkIncomingCall getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public String counterpart() {
        return getNetworkCallInfo().from();
    }

    @Override
    public NetworkCallInfo startCapture(String from, String to) {
        if (!getNetworkCallInfo().from().equals(getNetworkCallInfo().to())) {
            VoipBackend.getInstance().sendIncomingCallStart(getNetworkCallInfo().from(),
                    getNetworkCallInfo());
        }
        return getNetworkCallInfo();
    }

    @Override
    public void stop() {
        super.stop();
        VoipBackend.getInstance().sendCallStop(getNetworkCallInfo().from(),
                getNetworkCallInfo());
    }

    private static class SingletonHelper {
        private static final NetworkIncomingCall INSTANCE = new NetworkIncomingCall();
    }
}
