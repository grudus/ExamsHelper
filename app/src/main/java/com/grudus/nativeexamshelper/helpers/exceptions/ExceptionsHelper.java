package com.grudus.nativeexamshelper.helpers.exceptions;


import android.content.Context;
import android.util.Log;

import com.grudus.nativeexamshelper.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit2.Response;

public class ExceptionsHelper {

    private static final String TAG = "@@@ExceptionsHelper";


    public static boolean stringsAreEmpty(String... strings) {
        for (String string : strings) {
            if (string == null) {
                Log.e(TAG, "stringsAreEmpty: ", new EmptyStringException("null"));
                return true;
            }
            if (string.replaceAll("\\s+", "").isEmpty()) {
                Log.e(TAG, "stringsAreEmpty: ", new EmptyStringException("empty"));
                return true;
            }
        }
        return false;
    }

    public static void checkLoginResponse(Response<Void> response, Context context) {
        String message;
        if (response.code() != HttpURLConnection.HTTP_OK) {
            try {
                message = new JSONObject(response.errorBody().string()).getString("message");
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

            if (message.contains("user"))
                message = context.getString(R.string.toast_cannot_find_user_error);
            else if (message.toLowerCase().contains("password"))
                message = context.getString(R.string.toast_password_error);

            throw new AuthenticationException(message);
        }
    }

    public static void checkResponse(Response<?> response) {
        String message;
        if (response.code() != HttpURLConnection.HTTP_OK) {
            try {
                message = new JSONObject(response.errorBody().string()).getString("message");
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

            throw new RuntimeException("Error " + response.code() + ": " + message);
        }
    }



    public static class EmptyStringException extends IllegalArgumentException {

        public static final String DEFAULT_MESSAGE = "String argument cannot be empty";

        public EmptyStringException(String detailMessage) {
            super(detailMessage);
        }

        public EmptyStringException() {
            this(DEFAULT_MESSAGE);
        }
    }

}
