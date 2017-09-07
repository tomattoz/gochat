package red.tel.chat.ui.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import red.tel.chat.ChatApp;
import red.tel.chat.EventBus;
import red.tel.chat.EventBus.Event;
import red.tel.chat.R;
import red.tel.chat.notification.RegistrationIntentService;

public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_DURATION = 3500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        new Timer().schedule(timerTask, SPLASH_DURATION);
        EventBus.listenFor(this, Event.AUTHENTICATED, () -> {
            Log.d(TAG, "onCreate: ");
            timerTask.cancel();
            start(ItemListActivity.class);
        });
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            start(LoginActivity.class);
            finish();
        }
    };

    private Intent intent;
    private void start(Class next) {
        if (intent == null) {
            intent = new Intent(this, next);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
            this.finish();
        } else {
            this.finish();
        }
    }

    @Override
    protected void onStop() {
        ((ChatApp)getApplication()).onDismisDialog();
        super.onStop();
    }

    @Override
    protected void onPause() {
        EventBus.unRegisterEvent(this);
        super.onPause();
    }
}
