package red.tel.chat.network;

import okio.ByteString;
import red.tel.chat.io.AudioRecorder;
import red.tel.chat.io.IO;

/**
 * NetworkAudio class
 */
public class NetworkAudio {

    /**
     *
     */
    public static class NetworkAudioSessionInfo extends NetworkBase.NetworkIOSessionInfo {

        /**
         * prvs
         */
        private IO.AudioFormat audioFormat = new IO.AudioFormat();

        /**
         * ctor
         *
         * @param id An identifier
         */
        public NetworkAudioSessionInfo(IO.IOID id) {
            super(id);
        }

        /**
         * ctor
         *
         * @param id         An identifier
         * @param formatData format of data
         */
        public NetworkAudioSessionInfo(IO.IOID id, ByteString formatData) {

            // TODO
            super(id, formatData);
        }

        /**
         * ctor
         *
         * @param id          An identifier
         * @param audioFormat The audio format
         */
        public NetworkAudioSessionInfo(IO.IOID id, IO.AudioFormat audioFormat) {
            super(id);
            this.audioFormat = audioFormat;
        }
    }
}
