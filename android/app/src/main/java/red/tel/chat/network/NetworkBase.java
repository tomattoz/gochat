package red.tel.chat.network;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okio.ByteString;
import red.tel.chat.io.IO;

import static red.tel.chat.utils.Log.logError;

public class NetworkBase {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Logs
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void logNetwork(String message) {
        //logMessage("Network", message);
    }

    public static void logNetworkPrior(String message) {
        //logPrior("Network", message);
    }

    public static void logNetworkError(String message) {
        logError("Network", message);
    }

    public static void logNetworkError(String message, Exception error) {
        logError("Network", error);
    }

    protected static class NetworkIOSessionInfo {
        public IO.IOID id;
        public ByteString formatData;

        public NetworkIOSessionInfo(IO.IOID id) {
            this.id = id;
        }

        public NetworkIOSessionInfo(IO.IOID id, ByteString formatData) {
            this.id = id;
            this.formatData = formatData;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NetworkInput
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class NetworkInput {
        private Map<String, IO.IODataProtocol> output = new HashMap<>();

        public void add(String sid, IO.IODataProtocol output) {
            this.output.put(sid, output);
        }

        public void remove(String sid) {
            for (Iterator<Map.Entry<String, IO.IODataProtocol>> it = output.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, IO.IODataProtocol> entry = it.next();
                if (entry.getKey().equals(sid)) {
                    it.remove();
                }
            }
        }

        public void removeAll() {
            output.clear();
        }

        public void process(String sid, byte[] data) {

        }
    }
}
