package com.grudus.nativeexamshelper.layouts;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.grudus.nativeexamshelper.R;


public class ShowHidePasswordView extends FrameLayout {

    public static final String TAG = "@@@" + ShowHidePasswordView.class.getSimpleName();

    private FrameLayout root;
    private TextInputLayout textInputLayout;
    private EditText editText;
    private ImageView imageView;

    private ErrorPasswordListener onErrorListener;

    private boolean isPasswordVisible;

    public ShowHidePasswordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ShowHidePasswordView(Context context) {
        super(context);
        init();
    }

    public ShowHidePasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }




    private void init() {
        root = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.show_hide_password, null);

        editText = (EditText) root.findViewById(R.id.show_hide_password_password);
        textInputLayout = (TextInputLayout) root.findViewById(R.id.show_hide_password_parent);
        imageView = (ImageView) root.findViewById(R.id.show_hide_password_password_image);


        this.addView(root);

        setUpListeners();
    }


    private void setUpListeners() {
        imageView.setOnClickListener(v -> {
            int cursorPosition = editText.getSelectionStart();
            if (!isPasswordVisible) {
                editText.setTransformationMethod(null);
                editText.setSelection(cursorPosition);
                imageView.setImageResource(R.drawable.ic_visibility_off_black_24dp);
                isPasswordVisible = true;
            }
            else {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                editText.setSelection(cursorPosition);
                imageView.setImageResource(R.drawable.ic_visibility_black_24dp);
                isPasswordVisible = false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                imageView.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                onErrorListener.onError();
        });

    }

    public void setOnErrorListener(ErrorPasswordListener listener) {
        onErrorListener = listener;
    }

    public String getPasswordText() {
        return editText.getText().toString();
    }

    public void setError(@Nullable String message) {

        textInputLayout.setErrorEnabled(message != null);
        textInputLayout.setError(message);
    }

    public interface ErrorPasswordListener {
        void onError();
    }

}
