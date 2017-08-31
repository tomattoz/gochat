package red.tel.chat.utils;


public class Log {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Pipe: Errors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void logError(String scope, String message) {
        android.util.Log.d(scope, "Error: " + message);
    }

    public static void logError(String scope, Exception error) {
        logError(scope + " error" + ": " + error.getMessage());
    }

    public static void logError(String message) {
        android.util.Log.d("global", message);
    }
}
