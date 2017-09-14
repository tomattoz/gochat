package red.tel.chat.network;


import red.tel.chat.Types;

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
        return new NetworkCall(info);
    }

    private NetworkCallController() {
    }

    public static synchronized NetworkCallController getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private static class SingletonHelper {
        private static final NetworkCallController INSTANCE = new NetworkCallController();
    }


}
