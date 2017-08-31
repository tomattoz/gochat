package red.tel.chat.network;


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
}
