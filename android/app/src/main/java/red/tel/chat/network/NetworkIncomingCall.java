package red.tel.chat.network;

import red.tel.chat.VoipBackend;

public class NetworkIncomingCall extends NetworkCall {
    @Override
    public String counterpart() {
        return info.to();
    }

    @Override
    public NetworkCallInfo startCapture(String from, String to) {
        if (!info.from().equals(info.to())){
            VoipBackend.getInstance().sendIncomingCallStart(info.from(), info);
        }
        return info;
    }

    @Override
    public void stop() {
        super.stop();
        VoipBackend.getInstance().sendCallStop(info.from(), info);
    }
}
