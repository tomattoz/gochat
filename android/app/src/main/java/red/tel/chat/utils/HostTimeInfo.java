package red.tel.chat.utils;

/**
 * HostTimeInfo class
 */
public class HostTimeInfo {

    /**
     * load native lib
     */
    static {
        System.loadLibrary("native-lib");
        HostTimeInfo.init();
    }

    /**
     * Zero time (in nanoseconds)
     */
    private static long zero;

    /**
     * Timespec private class
     */
    private static class Timespec {
        int sec;
        long nsec;
    }

    /**
     * init
     */
    private static void init() {
        Timespec ts = new Timespec();
        clockGettimeTS(ts);
        HostTimeInfo.zero = (long)(ts.sec * 1E9) + ts.nsec;
    }

    /**
     *
     * @param ts time spec POSIX
     */
    private static native void clockGettimeTS(Timespec ts);

    /**
     *
     * @param sec seconds
     * @return nanoseconds
     */
    private static long nano(double sec) {
        return (long)(sec * 1E9);
    }

    /**
     *
     * @param sec seconds
     * @return microseconds
     */
    private static long micro(double sec) {
        return (long)(sec * 1E6);
    }

    /**
     *
     * @param sec seconds
     * @return miliseconds
     */
    private static long milli(double sec) {
        return (long)(sec * 1E3);
    }

    /**
     *
     * @param nsec nanoseconds
     * @return seconds
     */
    private static double nano2second(long nsec) {
        return nsec / 1E9;
    }

    /**
     *
     * @param sec seconds
     * @return nanoseconds
     */
    private static long seconds2nano(double sec) {
        return nano(sec);
    }

    /**
     *
     * @return posix time in second
     */
    public static double absoluteSeconds() {
        Timespec ts = new Timespec();
        clockGettimeTS(ts);
        return ts.sec + nano2second(ts.nsec);
    }

    /**
     *
     * @return app time from start
     */
    public static double appAbsoluteSeconds() {
        return absoluteSeconds() - nano2second(zero);
    }
}
