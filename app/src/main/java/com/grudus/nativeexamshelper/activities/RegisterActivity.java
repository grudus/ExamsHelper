package com.grudus.nativeexamshelper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.dialogs.reusable.CustomAlertDialog;
import com.grudus.nativeexamshelper.helpers.ToastHelper;
import com.grudus.nativeexamshelper.helpers.exceptions.ExceptionsHelper;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.layouts.ShowHidePasswordView;
import com.grudus.nativeexamshelper.net.RetrofitMain;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "@@@" + RegisterActivity.class.getSimpleName();
    @BindView(R.id.registry_view_button)
    Button registerButton;

    @BindView(R.id.register_view_login)
    EditText loginEditText;

    @BindView(R.id.register_view_email)
    EditText emailEditText;

    @BindView(R.id.register_view_password_fragment)
    ShowHidePasswordView showHidePasswordFragment;

    @BindView(R.id.progress_bar_register_parent)
    LinearLayout progressBarParent;

    @BindView(R.id.register_view_login_progress)
    ProgressBar loginProgressBar;

    @BindView(R.id.register_view_email_progress)
    ProgressBar emailProgressBar;

    private TextInputLayout loginParent;
    private TextInputLayout emailParent;

    private RetrofitMain retrofit;

    private Subscription subscription;

    private boolean emailIsCorrect;
    private boolean passwordIsCorrect;
    private boolean loginIsCorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);



        retrofit = new RetrofitMain(this);
        loginParent = (TextInputLayout) loginEditText.getParent();
        emailParent = (TextInputLayout) emailEditText.getParent();


        showHidePasswordFragment.setOnErrorListener(() -> {
            if (!validatePassword()) {
                showHidePasswordFragment.setError(getString(R.string.error_password_too_short));
            }
            else
                showHidePasswordFragment.setError(null);
        });

    }

    @OnClick(R.id.registry_view_button)
    public void register() {
        String login = loginEditText.getText().toString().trim();
        String password = showHidePasswordFragment.getPasswordText().trim();
        String email = emailEditText.getText().toString().trim();


        if (!validateInputs()) {
            Toast.makeText(this, getString(R.string.toast_bad_creditionals), Toast.LENGTH_SHORT).show();
            loginProgressBar.setVisibility(View.GONE);
            return;
        }

        addUser(login, password, email);
    }

    private boolean validateInputs() {
        return validateLogin() &&
        validatePassword() &&
        validateEmail();
    }

    private boolean validateLogin() {
        String login = loginEditText.getText().toString().replaceAll("\\s+", "");
        loginEditText.setText(login);

        if (login.isEmpty()) {
            Log.d(TAG, "validateLogin: login is empty");
            loginIsCorrect = false;
            return false;
        }

        if (login.indexOf(';') > -1) {
            loginParent.setErrorEnabled(true);
            loginParent.setError(getString(R.string.error_login_incorrect));
            loginIsCorrect = false;
            return false;
        }
        loginIsCorrect = true;


        loginProgressBar.setVisibility(View.VISIBLE);
        subscription = retrofit.checkLoginOrEmailAvailability(login, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    ExceptionsHelper.checkResponse(response);

                    String message = response.body();

                    if (!getString(R.string.net_register_login_message).equals(message)) {
                        loginParent.setErrorEnabled(true);
                        loginParent.setError(getString(R.string.error_login_unavailable));
                    }
                    else {
                        loginParent.setErrorEnabled(false);
                        loginParent.setError(null);
                    }

                }, error -> {
                    Log.e(TAG, "checkLogin: err", error);
                    new ToastHelper(this).showErrorMessage(error.getMessage(), error);
                    loginProgressBar.setVisibility(View.GONE);
                }, () -> loginProgressBar.setVisibility(View.GONE));
        return passwordIsCorrect;
    }


    private boolean validatePassword() {
        String password = showHidePasswordFragment.getPasswordText();
        passwordIsCorrect = password.length() > 8;
        if (!passwordIsCorrect)
            showHidePasswordFragment.setError(getString(R.string.error_register_password));
        return passwordIsCorrect;
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString();

        emailIsCorrect = !TextUtils.isEmpty(email)
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        setErrorIfNecessary(emailIsCorrect);

        if (emailIsCorrect) {
            emailProgressBar.setVisibility(View.VISIBLE);
            subscription = retrofit.checkLoginOrEmailAvailability(null, email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        ExceptionsHelper.checkResponse(response);

                        String message = response.body();

                        if (!getString(R.string.net_register_login_message).equals(message)) {
                            emailParent.setErrorEnabled(true);
                            emailParent.setError(getString(R.string.error_email_unavailable));
                        } else {
                            emailParent.setErrorEnabled(false);
                            emailParent.setError(null);
                        }
                    }, error -> {
                        Log.e(TAG, "checkEmail: err", error);
                        emailProgressBar.setVisibility(View.GONE);
                    }, () -> emailProgressBar.setVisibility(View.GONE));
        }

        return emailIsCorrect;
    }

    private void addUser(String login, String password, String email) {

        if (!emailIsCorrect || !passwordIsCorrect || !loginIsCorrect)
            return;

        progressBarParent.setVisibility(View.VISIBLE);

        subscription = retrofit.checkLoginOrEmailAvailability(login, email)
                .flatMap(response -> {
                    ExceptionsHelper.checkResponse(response);
                    if (response.body().equals(getString(R.string.net_register_login_message)))
                        return retrofit.addNewUser(login, password, email);
                    return Observable.error(new RuntimeException(getString(R.string.error_email_or_login_unavailable)));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ExceptionsHelper::checkResponse,
                        error -> {
                    handleError(error);
                    progressBarParent.setVisibility(View.GONE);
                    loginProgressBar.setVisibility(View.GONE);
                }, () -> {
                    new CustomAlertDialog()
                            .addTitle(getString(R.string.dialog_register_title))
                            .addText(getString(R.string.dialog_register_message))
                            .addListener(((dialog, which) -> {
                                RegisterActivity.this.finish();
                                RegisterActivity.this.startActivity(new Intent(this, ExamsMainActivity.class));
                            }))
                            .show(getFragmentManager(), getString(R.string.tag_dialog_register));
                    progressBarParent.setVisibility(View.GONE);
                    loginProgressBar.setVisibility(View.GONE);

                });
    }

    private void handleError(Throwable error) {
        if (error instanceof IOException)
            Toast.makeText(this, getString(R.string.error_connection), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, "handleError: ", error);
    }

    @OnFocusChange(R.id.register_view_login)
    public void checkLogin(boolean focus) {
        if (focus) {
            loginProgressBar.setVisibility(View.GONE);
            return;
        }
        validateLogin();

    }

    @OnFocusChange(R.id.register_view_email)
    public void emailChanged(boolean focus) {
        if (focus)
            return;
        setErrorIfNecessary(validateEmail());

    }

    private void setErrorIfNecessary(boolean isCorrect) {
        if (!isCorrect) {
            emailParent.setErrorEnabled(true);
            emailParent.setError(getString(R.string.error_email_incorrect));
        }
        else {
            emailParent.setError(null);
            emailParent.setErrorEnabled(false);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

}
