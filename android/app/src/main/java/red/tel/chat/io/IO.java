package red.tel.chat.io;

import java.util.UUID;

import red.tel.chat.Types;
import red.tel.chat.generated_protobuf.Image;
import red.tel.chat.office365.Constants;

/**
 * Created by hoanghiep on 8/31/17.
 */

public class IO {
    public static class IOID {
        private String from;
        private String to;
        private String sid; // session unique ID
        private String gid; // io group (audio + video) ID

        public IOID(String from, String to, String sid, String gid) {
            this.from = from;
            this.to = to;
            this.sid = sid;
            this.gid = gid;
        }

        public IOID(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public IOID groupNew() {
            return new IOID(from, to, IOID.newID(from, to, "sid"), gid);
        }

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Simple types
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    enum IOKind {
        Audio,
        Video
    }

    public interface IODataProtocol {
        void startOut();
        void processAudio(byte[] data);
        void processVideo(Image data);
    }

    public static class IOFormat {
        private static String kID = "kID";
        private String[] data;

        public IOFormat() {
            data = new String[]{UUID.fromString(Constants.CLIENT_ID).toString()};
        }

        public IOFormat(String[] data) {
            this.data = data;
        }

        public String id() {
            return data[0];
        }
    }

    public interface IOSessionProtocol extends Types.SessionProtocol {}

    public static class IOOutputContext {
        public IOID id;
        public IOSessionProtocol session;
        public IODataProtocol data;
        public IOTimebase timebase;

        public IOOutputContext(IOID id, IOSessionProtocol session, IODataProtocol data, IOTimebase timebase) {
            this.id = id;
            this.session = session;
            this.data = data;
            this.timebase = timebase;
        }

        public IOOutputContext() {
        }
    }

    public static class IOTimebase {
        double zero;
    }
}
