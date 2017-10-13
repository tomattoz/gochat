package red.tel.chat.io;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import red.tel.chat.Types;
import red.tel.chat.generated_protobuf.Image;
import red.tel.chat.office365.Constants;

/**
 * IO group of classes, structs
 */
public class IO {

    /**
     * IO indentifier class
     */
    public static class IOID {

        /**
         * Members
         */
        private String from;
        private String to;
        private String sid; // session unique ID
        private String gid; // io group (audio + video) ID

        /**
         * ctor
         *
         * @param from peer
         * @param to   peer
         * @param sid  session id
         * @param gid  guid
         */
        public IOID(String from, String to, String sid, String gid) {
            this.from = from;
            this.to = to;
            this.sid = sid;
            this.gid = gid;
        }

        /**
         * ctor
         *
         * @param from peer
         * @param to   peer
         */
        public IOID(String from, String to) {
            this.from = from;
            this.to = to;
        }

        /**
         * @return new ioid
         */
        public IOID groupNew() {
            return new IOID(from, to, IOID.newID(from, to, "sid"), gid);
        }

        /**
         * @param from peer
         * @param to   peer
         * @param kind of IO
         * @return a unique string
         */
        private static String newID(String from, String to, String kind) {
            return kind + from + "-" + to + UUID.fromString(Constants.CLIENT_ID);
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getSid() {
            return sid;
        }

        public String getGid() {
            return gid;
        }
    }

    /**
     * IO kind enums
     */
    enum IOKind {
        Audio,
        Video
    }

    /**
     * IO Data interface
     */
    public interface IODataProtocol {
        void startOut();

        void processAudio(byte[] data);

        void processVideo(Image data);
    }


    /**
     * IOFormat struct
     */
    public static class IOFormat {

        /**
         * Members
         */
        private static String kID = "kID";
        private Map<String, Object> data = new HashMap<>();

        /**
         * @return a map
         */
        public Map<String, Object> getData() {
            return data;
        }

        /**
         * @return id
         */
        public String id() {
            return (String) data.get(kID);
        }

        /**
         * ctor
         */
        public IOFormat() {
            data.put(kID, UUID.fromString(Constants.CLIENT_ID).toString());
        }

    }

    /**
     * IOSessionProtocol interface
     */
    public interface IOSessionProtocol extends Types.SessionProtocol {
    }

    /**
     * IOOutputContext struct
     */
    public static class IOOutputContext {

        /**
         * prvs
         */
        private IOTimebase timebase;
        private IOSessionProtocol session;

        /**
         * pubs
         */
        public IOID id;
        public IODataProtocol data;

        /**
         * ctor
         *
         * @param id -> identifier
         * @param session -> session
         * @param data -> data
         * @param timebase -> tb
         */
        public IOOutputContext(IOID id, IOSessionProtocol session, IODataProtocol data,
                               IOTimebase timebase) {
            this.id = id;
            this.session = session;
            this.data = data;
            this.timebase = timebase;
        }

        /**
         * ctor
         */
        public IOOutputContext() {
        }
    }

    /**
     * IOTimebase struct
     */
    public static class IOTimebase {
        double zero;
    }

    /**
     * IOFormat interface
     */
    public interface IOMediaFormatProtocol {

        public IOFormat getFormat();
        public byte[] toNetwork();
        public void fromNetwork(byte[] remoteData);
    }

    /**
     * An implementation of IOFormat with Audio
     */
    public static class AudioFormat implements IOMediaFormatProtocol {

        /**
         * consts
         */
        private static String kFormatID = "kFormatID";
        private static String kFlags = "kFlags";
        private static String kSampleRate = "kSampleRate";
        private static String kChannelCount = "kChannelCount";
        private static String kFramesPerPacket = "kFramesPerPacket";

        /**
         * prvs
         */
        private IOFormat format = new IOFormat();

        @Override
        public IOFormat getFormat() {
            return format;
        }

        /**
         *
         * @return
         */
        public Integer getFormatID() {
            return (Integer) format.getData().get(kFormatID);
        }

        /**
         *
         * @param formatID
         */
        public void setFormatID(Integer formatID) {
            format.getData().put(kFormatID, formatID);
        }

        /**
         *
         * @return
         */
        public Integer getFlags() {
            return (Integer) format.getData().get(kFormatID);
        }

        /**
         *
         * @param flags
         */
        public void setFlags(Integer flags) {
            format.getData().put(kFlags, flags);
        }

        /**
         *
         * @return
         */
        public Double getSampleRate() {
            return (Double) format.getData().get(kFormatID);
        }

        /**
         *
         * @param sampleRate
         */
        public void setSampleRate(Double sampleRate) {
            format.getData().put(kSampleRate, sampleRate);
        }

        /**
         *
         * @return
         */
        public Integer getChannelCount() {
            return (Integer) format.getData().get(kChannelCount);
        }

        /**
         *
         * @param channelCount
         */
        public void setChannelCount(Integer channelCount) {
            format.getData().put(kChannelCount, channelCount);
        }

        /**
         *
         * @return
         */
        public Integer getFramesPerPacket() {
            return (Integer) format.getData().get(kFramesPerPacket);
        }

        /**
         *
         * @param framesPerPacket
         */
        public void setFramesPerPacket(Integer framesPerPacket) {
            format.getData().put(kFramesPerPacket, framesPerPacket);
        }

        /**
         *
         * @return bytes
         */
        @Override
        public byte[] toNetwork() {
            JSONObject json = new JSONObject(format.getData());
            return json.toString().getBytes();
        }

        /**
         *
         * @param remoteData The data bytes for media format
         */
        @Override
        public void fromNetwork(byte[] remoteData) {
            String str = new String(remoteData);

            try {
                JSONObject jObject = new JSONObject(str);
                this.setFormatID((Integer) jObject.get(kFormatID));
                this.setFlags((Integer) jObject.get(kFlags));
                this.setSampleRate((Double) jObject.get(kSampleRate));
                this.setChannelCount((Integer) jObject.get(kChannelCount));
                this.setFramesPerPacket((Integer) jObject.get(kFramesPerPacket));
            } catch (Exception e) {
                Log.d("AudioFormat", "fromNetwork: ");
            }

        }
    }

    /**
     * An implementation of IOFormat with Video
     */
    public static class VideoFormat implements IOMediaFormatProtocol {

        private IOFormat format;

        @Override
        public IOFormat getFormat() {
            return format;
        }

        @Override
        public byte[] toNetwork() {
            return new byte[0];
        }

        @Override
        public void fromNetwork(byte[] remoteData) {

        }

    }

    /**
     * A Factory IOFormat struct
     */
    public static class IOFormatFactory {

        private static IOFormatFactory shared;

        /**
         * Singleton
         *
         * @return singleton of IOFormatFactory
         */
        public static IOFormatFactory shared() {
            if (shared == null) {
                synchronized (IOFormatFactory.class) {
                    if (shared == null) {
                        shared = new IOFormatFactory();
                    }
                }
            }
            return shared;
        }

        /**
         * @return audio format
         */
        public IOMediaFormatProtocol createAudioFormat() {

            // create a default audio format
            AudioFormat audioFmt = new AudioFormat();
            audioFmt.setSampleRate((double) 44100);
            audioFmt.setChannelCount(1);
            audioFmt.setFramesPerPacket(1);
            audioFmt.setFlags(1);

            return audioFmt;
        }

        /**
         * @return video format
         */
        public IOMediaFormatProtocol createVideoFormat() {
            return new VideoFormat();
        }

    }
}
