package red.tel.chat.ui.activitys;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.JsonObject;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import java.net.URI;
import java.util.UUID;

import red.tel.chat.Backend;
import red.tel.chat.EventBus;
import red.tel.chat.Model;
import red.tel.chat.Network;
import red.tel.chat.R;
import red.tel.chat.notification.RegistrationIntentService;
import red.tel.chat.office365.AuthenticationManager;
import red.tel.chat.office365.Constants;
import red.tel.chat.office365.TokenNotFoundException;

import static red.tel.chat.Model.parseJsonUser;
import static red.tel.chat.office365.Constants.TYPE_LOGIN_MS;
import static red.tel.chat.office365.Constants.TYPE_LOGIN_NORMAL;

public class LoginActivity extends BaseActivity implements AuthorizationService.TokenResponseCallback {

    private static final String TAG = "LoginActivity";
    private EditText usernameView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AuthenticationManager.getInstance().setContextActivity(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            AuthenticationManager.getInstance().processAuthorizationCode(getIntent(), this);
        }
        usernameView = (EditText) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener((TextView textView, int id, KeyEvent keyEvent) -> {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                validateInput();
                return true;
            }
            return false;
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
        EventBus.listenFor(this, EventBus.Event.AUTHENTICATED, this::finish);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AuthenticationManager.getInstance().onDestroyService();
        EventBus.unRegisterEvent(this);
    }

    @Override
    protected void onSubscribeEvent(Object object) {
        super.onSubscribeEvent(object);
        if (object == EventBus.Event.DISCONNECTED) {
            showProgress(false);
        }

        if (object == EventBus.Event.LOGIN_RESPONSE) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.login_fail_title);
            alert.setMessage(R.string.login_fail_message);
            alert.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                dialogInterface.cancel();
                showProgress(false);
            });
            alert.create().show();
        }
    }



    private void connect() {
        AuthenticationManager.getInstance().startAuthorizationFlow();
    }

    private static boolean hasAzureConfiguration() {
        try {
            UUID.fromString(Constants.CLIENT_ID);
            URI.create(Constants.REDIRECT_URI);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Boolean validateInput() {
        usernameView.setError(null);
        passwordView.setError(null);

        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        if (!isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            passwordView.requestFocus();
            return false;
        }

        if (!isUsernameValid(username)) {
            usernameView.setError(getString(R.string.error_invalid_username));
            usernameView.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isUsernameValid(String username) {
        return username.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    private void showProgress(boolean isProgress) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.animate().setDuration(shortAnimTime).alpha(isProgress ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loginFormView.setVisibility(isProgress ? View.GONE : View.VISIBLE);
                    }
                });

        progressView.setVisibility(isProgress ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(isProgress ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressView.setVisibility(isProgress ? View.VISIBLE : View.GONE);
                    }
                });
    }

    public void onClickSignIn(View v) {
        if (!validateInput()) {
            return;
        }
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();
        Model.shared().setAccessToken("normal");
        login(TYPE_LOGIN_NORMAL, username, password, "normal");

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void login(int typeLogin, String username, String password, String accessToken) {
        Model.shared().setUsername(username);
        Model.shared().setPassword(password);
        Model.shared().setTypeLogin(typeLogin);
        showProgress(true);
        String tokenNotificaion = Model.shared().getTokenPushNotification();
        if (tokenNotificaion != null) {
            Backend.shared().login(typeLogin, username, accessToken, tokenNotificaion);
        } else {
            showProgress(false);
        }
    }

    public void onClickRegister(View v) {
    }

    //login with microsoft office 360
    public void onClickSignInOffice360(View view) {
        //check that client id and redirect have been configured
        if (!hasAzureConfiguration()) {
            Toast.makeText(
                    LoginActivity.this,
                    getString(R.string.warning_client_id_redirect_uri_incorrect),
                    Toast.LENGTH_LONG).show();
            return;
        }
        connect();
    }

    @Override
    public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException authorizationException) {
        if (tokenResponse != null) {
            // get the UserInfo from the auth response
            JsonObject claims = AuthenticationManager.getInstance().getClaims(tokenResponse.idToken);
            String name = claims.get("name").getAsString();
            String tid = claims.get("tid").getAsString();
            try {
                String accessToken = AuthenticationManager.getInstance().getAccessToken();
                Model.shared().setAccessToken(accessToken);
                login(TYPE_LOGIN_MS, name, tid, accessToken);
                Model.shared().setTypeLogin(TYPE_LOGIN_MS);
                Log.d(TAG, "onTokenRequestCompleted: " + accessToken);
            } catch (TokenNotFoundException e) {
                e.printStackTrace();
            }
        } else if (authorizationException != null) {
            snackbar(getString(R.string.connect_toast_text_error));
            Log.e(TAG, "Token Request Fail: ", authorizationException);
        }
    }
}
