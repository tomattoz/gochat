package red.tel.chat.network;


import okio.ByteString;
import red.tel.chat.io.IO;

public class NetworkVideo {
    public static class NetworkVideoSessionInfo extends NetworkBase.NetworkIOSessionInfo {

        public NetworkVideoSessionInfo(IO.IOID id) {
            super(id);
        }

        public NetworkVideoSessionInfo(IO.IOID id, ByteString formatData) {
            super(id, formatData);
        }
    }
}
