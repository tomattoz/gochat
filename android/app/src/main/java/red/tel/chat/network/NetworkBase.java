package red.tel.chat.network;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
        private IO.IOID id;
        private byte[] formatData;

        public NetworkIOSessionInfo(IO.IOID id) {
            this.id = id;
        }

        public NetworkIOSessionInfo(IO.IOID id, byte[] formatData) {
            this.id = id;
            this.formatData = formatData;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NetworkInput
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class NetworkInput {
        private Map<String, byte[]> output = new HashMap<>();

        public void add(String sid, byte[] output) {
            this.output.put(sid, output);
        }

        public void remove(String sid) {
            for (Iterator<Map.Entry<String, byte[]>> it = output.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, byte[]> entry = it.next();
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
