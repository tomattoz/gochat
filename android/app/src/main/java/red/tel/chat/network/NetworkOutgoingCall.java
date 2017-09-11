package red.tel.chat.network;

import red.tel.chat.VoipBackend;

/**
 * Created by vmodev on 9/11/17.
 */

public class NetworkOutgoingCall extends NetworkCall {
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
}
