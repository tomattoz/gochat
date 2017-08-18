package red.tel.chat;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.extensions.GraphServiceClient;
import com.microsoft.graph.extensions.IGraphServiceClient;
import com.microsoft.graph.http.IHttpRequest;

import red.tel.chat.ui.activitys.BaseActivity;

public class ChatApp extends MultiDexApplication implements IAuthenticationProvider {

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
}
