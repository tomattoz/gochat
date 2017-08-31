package red.tel.chat.network;

import red.tel.chat.io.IO;

public class NetworkAudio {
    public static class NetworkAudioSessionInfo extends NetworkBase.NetworkIOSessionInfo {

        public NetworkAudioSessionInfo(IO.IOID id) {
            super(id);
        }

        public NetworkAudioSessionInfo(IO.IOID id, byte[] formatData) {
            super(id, formatData);
        }
    }
}
