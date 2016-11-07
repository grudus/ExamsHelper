package com.grudus.nativeexamshelper.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.helpers.exceptions.AuthenticationException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Response;

public class ToastHelper {

    private final Context context;

    public ToastHelper(Context context) {
        this.context = context;
    }

    public void tryToShowErrorMessage(Response<?> response) {
        try {
            showError(response);
        } catch (IOException | JSONException e) {
            showErrorMessage(context.getString(R.string.toast_unexpected_error), e);
        }
    }

    public void showError(Response<?> response) throws IOException, JSONException {
        final String jsonMessage = response.errorBody().string();
        final String errorMessage = new JSONObject(jsonMessage).getString("message");
        showMessage(errorMessage);
        Log.e("@@@Error", "showError: " + errorMessage);
    }

    public void showMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showErrorMessage(String message, Throwable error) {
        if (error instanceof IOException)
            showMessage(context.getString(R.string.toast_server_error));
        else if (error instanceof AuthenticationException)
            showMessage(message);

        else showMessage(message);

        Log.e("@@@ERROR", "showErrorMessage: ", error);
    }

    public void showErrorMessage(Throwable error) {
        showErrorMessage(error.getMessage(), error);
    }


}
