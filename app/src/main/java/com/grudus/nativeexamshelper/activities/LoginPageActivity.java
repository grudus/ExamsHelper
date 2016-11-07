package com.grudus.nativeexamshelper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.helpers.ToastHelper;
import com.grudus.nativeexamshelper.helpers.exceptions.ExceptionsHelper;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.layouts.Hamburger;
import com.grudus.nativeexamshelper.layouts.ShowHidePasswordView;
import com.grudus.nativeexamshelper.net.RetrofitMain;
import com.grudus.nativeexamshelper.pojos.UserPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginPageActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "@@@@@@@@@@@@@" + LoginPageActivity.class.getSimpleName();
    private String AUTH_HEADER;

    private Subscription subscription;
    private RetrofitMain retrofit;

    private static final int RC_SIGN_IN = 9001;

    @BindView(R.id.login_view_login)
    AutoCompleteTextView loginTextView;

    @BindView(R.id.login_view_password)
    ShowHidePasswordView passwordView;

    @BindView(R.id.login_view_login_button)
    Button loginButton;

    @BindView(R.id.login_view_registry_button)
    Button registerButton;

    @BindView(R.id.progress_bar_login)
    ProgressBar progressBar;

    @BindView(R.id.progress_bar_login_parent)
    LinearLayout progressBarParent;

    @BindView(R.id.google_sign_in_button)
    SignInButton signInButton;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private UserPreferences userPreferences;
    private ToastHelper toastHelper;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        ButterKnife.bind(this);

        AUTH_HEADER = getString(R.string.net_auth_header);
        retrofit = new RetrofitMain(this);

        userPreferences = new UserPreferences(this);
        toastHelper = new ToastHelper(this);

        GoogleSignInOptions google = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, google)
                .build();

        signInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);

        setUpToolbar();
        setUpHamburgerMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpToolbar() {
        toolbar.setTitle(getString(R.string.toolbar_login));
        setSupportActionBar(toolbar);
    }

    private void setUpHamburgerMenu() {
        Hamburger hamburger = new Hamburger(this, R.id.nvView, drawerLayout);
        hamburger.setSelectedItem(2);
        hamburger.setUpNavigationView();
        hamburger.setUpToolbar(toolbar);
    }

    @OnClick(R.id.login_view_login_button)
    public void tryToLogIn() {
        String username = loginTextView.getText().toString().trim();
        String password = passwordView.getPasswordText();


        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginPageActivity.this, getString(R.string.toast_empty_creditionals), Toast.LENGTH_SHORT).show();
            return;
        }

        logInToServerNormally(username, password);
    }

    @OnClick(R.id.google_sign_in_button)
    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @OnClick(R.id.login_view_registry_button)
    public void register() {
        this.startActivity(new Intent(this, RegisterActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct == null) throw new RuntimeException("account is null!");
            logInToSeverByGoogleAccount(acct.getEmail(), acct.getId());
            Auth.GoogleSignInApi.signOut(googleApiClient);
        } else {
            Log.e(TAG, "handleSignInResult: errir " + result.getStatus());
        }
    }

    private void logInToSeverByGoogleAccount(String email, String id) {
        logInToServer(email, retrofit.tryToLoginUsingGoogleAccount(email, id));
    }

    private void logInToServerNormally(String username, String password) {
        logInToServer(username, retrofit.tryToLogin(username, password));
    }

    private void logInToServer(String username, Observable<Response<Void>> observable) {
        progressBarParent.setVisibility(View.VISIBLE);
        subscription = observable
                .flatMap(response -> {
                    ExceptionsHelper.checkLoginResponse(response, this);

                    checkUsername(username);
                    commitLogin(response);

                    return retrofit.getUserInfo();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    ExceptionsHelper.checkResponse(response);

                    userPreferences.changeId(response.body().getId());
                    userPreferences.changeUsername(response.body().getUsername());
                    toastHelper.showMessage(getString(R.string.toast_successful_login));

                }, error -> {
                    progressBarParent.setVisibility(View.GONE);
                    toastHelper.showErrorMessage(error.getMessage(), error);
                }, () -> {
                    progressBarParent.setVisibility(View.GONE);
                    LoginPageActivity.this.finish();
                    this.startActivity(new Intent(getApplicationContext(), ExamsMainActivity.class));
                });
    }

    private void checkUsername(String username) {
        username = username.replaceAll("(\\.|@)", "_");
        userPreferences.changeUsername(username);
    }

    private void commitLogin(Response<Void> response) {
        String token = response.headers().get(AUTH_HEADER);

        final UserPreferences.User user = userPreferences.getLoggedUser();

        if (!user.getToken().equals(token))
            userPreferences.changeToken(token);

        userPreferences.changeLoginStatus(true);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (subscription != null && subscription.isUnsubscribed())
            subscription.unsubscribe();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }
}
