package red.tel.chat.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import java.net.URI;
import java.util.UUID;

import red.tel.chat.office365.Constants;
import red.tel.chat.EventBus;
import red.tel.chat.Model;
import red.tel.chat.Backend;
import red.tel.chat.R;
import red.tel.chat.office365.AuthenticationManager;

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

//        Button signInButton = (Button) findViewById(R.id.sign_in_button);
//        signInButton.setOnClickListener((View view) -> { didClickSignIn(); });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
        AuthenticationManager.getInstance().setContextActivity(this);
        EventBus.listenFor(this, EventBus.Event.AUTHENTICATED, () -> {
            startActivity(new Intent(this, ItemListActivity.class));
            this.finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AuthenticationManager.getInstance().onDestroyService();
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

    private void showProgress() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.animate().setDuration(shortAnimTime).alpha(0)
                .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(View.GONE);
            }
        });

        progressView.setVisibility(View.VISIBLE);
        progressView.animate().setDuration(shortAnimTime).alpha(1)
                .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onClickSignIn(View v) {
        if (!validateInput()) {
            return;
        }
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();
        login(username, password);
    }

    private void login(String username, String password) {
        Model.shared().setUsername(username);
        Model.shared().setPassword(password);
        showProgress();
        Backend.shared().login(username);
    }

    public void onClickRegister(View v) {}

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
        if(tokenResponse != null) {
            // get the UserInfo from the auth response
            JsonObject claims = AuthenticationManager.getInstance().getClaims(tokenResponse.idToken);
            String name = claims.get("name").getAsString();// claims.getString("name");
            String tid = claims.get("tid").getAsString();
            login(name, tid);


        } else if (authorizationException != null) {

        }
    }
}
