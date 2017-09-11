package red.tel.chat.network;

import okio.ByteString;
import red.tel.chat.io.AudioRecorder;
import red.tel.chat.io.IO;

public class NetworkAudio {
    public static class NetworkAudioSessionInfo extends NetworkBase.NetworkIOSessionInfo {
        private AudioRecorder.AudioFormat audioFormat = new AudioRecorder.AudioFormat();
        public NetworkAudioSessionInfo(IO.IOID id) {
            super(id);
        }

        public NetworkAudioSessionInfo(IO.IOID id, ByteString formatData) {
            /*if (formatData != null) {
                this.audioFormat = audioFormat
            }*/
            super(id, formatData);
        }

        public NetworkAudioSessionInfo(IO.IOID id, AudioRecorder.AudioFormat audioFormat) {
            super(id);
            this.audioFormat = audioFormat;
        }
    }
}
