package red.tel.chat.network;

import red.tel.chat.io.Audio;
import red.tel.chat.io.IO;

public class NetworkAudio {
    public static class NetworkAudioSessionInfo extends NetworkBase.NetworkIOSessionInfo {
        private Audio.AudioFormat audioFormat = new Audio.AudioFormat();
        public NetworkAudioSessionInfo(IO.IOID id) {
            super(id);
        }

        public NetworkAudioSessionInfo(IO.IOID id, byte[] formatData) {
            /*if (formatData != null) {
                this.audioFormat = audioFormat
            }*/
            super(id, formatData);
        }

        public NetworkAudioSessionInfo(IO.IOID id, Audio.AudioFormat audioFormat) {
            super(id);
            this.audioFormat = audioFormat;
        }
    }
}
