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
        return NetworkCall.getInstance().getNetworkCallInfo().to();
    }

    @Override
    public NetworkCallInfo startCapture(String from, String to) {
        if (!NetworkCall.getInstance().getNetworkCallInfo().from().equals(NetworkCall.getInstance().getNetworkCallInfo().to())) {
            VoipBackend.getInstance().sendIncomingCallStart(NetworkCall.getInstance().getNetworkCallInfo().from(),
                    NetworkCall.getInstance().getNetworkCallInfo());
        }
        return NetworkCall.getInstance().getNetworkCallInfo();
    }

    @Override
    public void stop() {
        super.stop();
        VoipBackend.getInstance().sendCallStop(NetworkCall.getInstance().getNetworkCallInfo().from(),
                NetworkCall.getInstance().getNetworkCallInfo());
    }

    private static class SingletonHelper {
        private static final NetworkIncomingCall INSTANCE = new NetworkIncomingCall();
    }
}
