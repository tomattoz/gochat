package red.tel.chat.ui.activitys;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.support.design.widget.Snackbar;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import red.tel.chat.RxBus;

public abstract class BaseActivity extends AppCompatActivity {

    private static BaseActivity current;
    private CompositeDisposable disposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposable = new CompositeDisposable();
        onSubscribeEventRx();
    }

    @Override
    protected void onResume() {
        super.onResume();
        current = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        current = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable.clear();
        }
    }

    protected android.view.View getView() {
        return getWindow().getDecorView().getRootView();
    }

    public static void snackbar(String message) {
        Snackbar snackbar = Snackbar.make(current.getView(), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static Context getCurrentContext() {
        return current;
    }

    private synchronized void onSubscribeEventRx() {
        disposable.add(RxBus.getInstance()
                .receive()
                .subscribeOn(Schedulers.io())
                .delay(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object object) throws Exception {
                        onSubscribeEvent(object);
                    }
                }));
    }

    protected void onSubscribeEvent(Object object) {
    }
}
