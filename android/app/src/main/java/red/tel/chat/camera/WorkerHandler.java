package red.tel.chat.camera;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by vmodev on 9/6/17.
 */

public class WorkerHandler {
    private HandlerThread mThread;
    private Handler mHandler;

    public WorkerHandler(String name) {
        mThread = new HandlerThread(name);
        mThread.setDaemon(true);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
    }

    public Handler get() {
        return mHandler;
    }

    public void post(Runnable runnable) {
        mHandler.post(runnable);
    }
}
