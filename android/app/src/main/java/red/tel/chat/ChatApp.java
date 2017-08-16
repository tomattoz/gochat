package red.tel.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import red.tel.chat.ui.BaseActivity;

public class ChatApp extends MultiDexApplication {

    private static final String TAG = "ChatApp";
    private static Context context;
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        listenForConnection();
        startService(new Intent(this, Backend.class));
    }

    private void listenForConnection() {
        EventBus.listenFor(this, EventBus.Event.DISCONNECTED, () -> {

            Context context = BaseActivity.getCurrentContext();
            if (context == null) {
                Log.e(TAG, "No current context");
                return;
            }
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle(R.string.disconnected_title);
            alert.setMessage(R.string.disconnected_message);
            alert.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                dialogInterface.cancel();
                RxBus.getInstance().sendEvent(EventBus.Event.DISCONNECTED);
            });
            alert.create().show();
        });
    }
}
