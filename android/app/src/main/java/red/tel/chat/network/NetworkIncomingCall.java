package red.tel.chat.network;

import red.tel.chat.VoipBackend;

public class NetworkIncomingCall extends NetworkCall {
    private NetworkIncomingCall(){
    }

    private static class SingletonHelper {
        private static final NetworkIncomingCall INSTANCE = new NetworkIncomingCall();
    }

    public static synchronized NetworkIncomingCall getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public String counterpart() {
        return NetworkCall.getInstance().getInfo().to();
    }

    @Override
    public NetworkCallInfo startCapture(String from, String to) {
        if (!NetworkCall.getInstance().getInfo().from().equals(NetworkCall.getInstance().getInfo().to())){
            VoipBackend.getInstance().sendIncomingCallStart(NetworkCall.getInstance().getInfo().from(), NetworkCall.getInstance().getInfo());
        }
        return NetworkCall.getInstance().getInfo();
    }

    @Override
    public void stop() {
        super.stop();
        VoipBackend.getInstance().sendCallStop(NetworkCall.getInstance().getInfo().from(), NetworkCall.getInstance().getInfo());
    }
}
