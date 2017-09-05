package red.tel.chat.network;


import red.tel.chat.io.IO;

public class NetworkVideo {
    public static class NetworkVideoSessionInfo extends NetworkBase.NetworkIOSessionInfo {

        public NetworkVideoSessionInfo(IO.IOID id) {
            super(id);
        }

        public NetworkVideoSessionInfo(IO.IOID id, byte[] formatData) {
            super(id, formatData);
        }
    }
}
