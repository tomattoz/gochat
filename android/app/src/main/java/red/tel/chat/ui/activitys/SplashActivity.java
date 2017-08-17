package red.tel.chat.ui.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import red.tel.chat.EventBus;
import red.tel.chat.EventBus.Event;
import red.tel.chat.R;

public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        EventBus.listenFor(this, Event.AUTHENTICATED, () -> {
            Log.d(TAG, "onCreate: ");
            timerTask.cancel();
            start(ItemListActivity.class);
        });

        new Timer().schedule(timerTask, SPLASH_DURATION);
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            start(LoginActivity.class);
            finish();
        }
    };

    private void start(Class next) {
        Intent intent = new Intent(this, next);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.unRegisterEvent(this);
    }
}
