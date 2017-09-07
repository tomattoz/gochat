package red.tel.chat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.extensions.GraphServiceClient;
import com.microsoft.graph.extensions.IGraphServiceClient;
import com.microsoft.graph.http.IHttpRequest;

import red.tel.chat.office365.Constants;
import red.tel.chat.ui.activitys.BaseActivity;
import red.tel.chat.ui.activitys.SplashActivity;

public class ChatApp extends MultiDexApplication implements IAuthenticationProvider, Application.ActivityLifecycleCallbacks {

    private static final String TAG = "ChatApp";
    private static Context context;

    public static Context getContext() {
        return context;
    }

    private AlertDialog alertDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        listenForConnection();
        this.registerActivityLifecycleCallbacks(this);
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
            alertDialog = alert.create();
            alertDialog.show();
        });
    }

    public void onDismisDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    public IGraphServiceClient getGraphServiceClient() {
        IClientConfig clientConfig = DefaultClientConfig.createWithAuthenticationProvider(
                this
        );
        return new GraphServiceClient.Builder().fromConfig(clientConfig).buildClient();
    }

    @Override
    public void authenticateRequest(IHttpRequest request) {
        request.addHeader("Authorization", "Bearer " + Model.shared().getAccessToken());
        request.addHeader("Content-Type", "application/json");
        Log.d(TAG, "authenticateRequest: " + request);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof SplashActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent startIntent = new Intent(this, WireBackend.class);
                startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                startForegroundService(startIntent);
            } else {
                if (!isServiceRunning(WireBackend.class)) {
                    startService(new Intent(this, WireBackend.class));
                }
            }
        }
        Log.d(TAG, "onActivityCreated: ");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(TAG, "onActivityStarted: ");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, "onActivityResumed: ");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG, "onActivityPaused: ");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(TAG, "onActivityStopped: ");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        Log.d(TAG, "onActivitySaveInstanceState: ");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, "onActivityDestroyed: ");
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
