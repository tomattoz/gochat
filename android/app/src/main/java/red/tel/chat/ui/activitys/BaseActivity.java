package red.tel.chat.ui.activitys;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import red.tel.chat.RxBus;

public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static BaseActivity current;
    private CompositeDisposable disposable;

    public static String[] PERMISSIONS_ALL = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.CAMERA
    };
    public static final int REQUEST_ALL = 123;

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

    protected void onSubscribeEvent(Object object) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //callback permission
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted: " + requestCode);
        for (String perm : perms) {
            Log.d(TAG, "onPermissionsGranted: perm " + perm);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        for (String perm : perms) {
            Log.d(TAG, "onPermissionsDenied: " + perm);
        }
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
    //end callback permission

    @AfterPermissionGranted(REQUEST_ALL)
    public void requestPermissions() {
        if (EasyPermissions.hasPermissions(this, PERMISSIONS_ALL)) {
            // Have permission, do the thing!
            Toast.makeText(this, "TODO: CameraAndroid things", Toast.LENGTH_LONG).show();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, "check",
                    REQUEST_ALL, PERMISSIONS_ALL);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void onStartCallIncoming(Bundle bundle) {
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onStartCall(Bundle bundle) {
        Intent intent = new Intent(this, OutgoingCallActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
