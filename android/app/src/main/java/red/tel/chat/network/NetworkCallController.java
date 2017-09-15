package red.tel.chat.network;


import red.tel.chat.Types;
import red.tel.chat.io.IO;

public class NetworkCallController extends NetworkSingleCallSessionController<NetworkCall, NetworkCallInfo> {
    private Types.SessionProtocol sessionProtocol;

    public NetworkCallController(Types.SessionProtocol sessionProtocol) {
        this.sessionProtocol = sessionProtocol;
    }

    @Override
    protected String id(NetworkCallInfo info) {
        return info.id();
    }

    @Override
    protected NetworkCall create(NetworkCallInfo info) {
        call.setNetworkCallInfo(info);
        return call;
    }

    private NetworkCallController() {
    }

    public static synchronized NetworkCallController getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private static class SingletonHelper {
        private static final NetworkCallController INSTANCE = new NetworkCallController();
    }

    public void startOutput(NetworkCallInfo networkCallInfo, IO.IODataProtocol audio, IO.IODataProtocol video) {
        if (!callInfo.id().equals(networkCallInfo.id())) {
            return;
        }

        if (networkCallInfo.audioSession != null) {
            audio = call.startOutput(networkCallInfo.audioSession);
        }

        if (networkCallInfo.videoSession != null) {
            video = call.startOutput(networkCallInfo.videoSession);
        }
    }
}
