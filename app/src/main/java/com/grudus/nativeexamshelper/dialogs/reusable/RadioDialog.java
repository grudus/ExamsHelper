package com.grudus.nativeexamshelper.dialogs.reusable;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.helpers.normal.ColorHelper;


public class RadioDialog extends DialogFragment {

    private OnSaveListener listener;

    private int textColor = 0, backgroundColor = 0, accentColor = 0;
    private String[] values;
    private String title;

    private int selectedItemIndex = -1;

    private View root;
    private RadioGroup radio;
    private TextView titleView;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initViews(builder);
        setUpTitle();

        initTreeObserver();
        initColors();


        builder.setView(root);
        return builder.create();
    }

    private void initColors() {
        if (textColor == 0)
            textColor = ColorHelper.getThemeColor(getActivity(), android.R.attr.textColor);
        if (backgroundColor == 0)
            backgroundColor = ColorHelper.getThemeColor(getActivity(), R.attr.dialogBackground);
        if (accentColor == 0)
            accentColor = ColorHelper.getThemeColor(getActivity(), R.attr.colorAccent);
    }

    private void initViews(AlertDialog.Builder builder) {
        root = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_grade, null);
        titleView = (TextView) root.findViewById(R.id.dialog_input_text_title);
        builder.setNegativeButton(getString(R.string.dialog_negbut), ((dialog, which) -> dismiss()));
    }

    private void setUpTitle() {
        if (title == null || title.replaceAll("\\s+", "").isEmpty()) {
            titleView.setVisibility(View.GONE);
        }
        else titleView.setText(title);
    }

    private void initTreeObserver() {
        root.getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                               @Override
                               public void onGlobalLayout() {
                                   root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                   setUpRadio(root);
                                   setUpDialogColors();
                                   setUpDialogSize();
                                   setUpButtonListener();
                               }
                        }
                );
    }

    private void setUpDialogColors() {
        AlertDialog dialog = (AlertDialog) getDialog();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(accentColor);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(accentColor);
    }

    private void setUpDialogSize() {
        AlertDialog dialog = (AlertDialog) getDialog();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        int radioHeight = radio.getPaddingBottom() + radio.getPaddingTop() +
                (radio.getChildAt(0).getHeight() + radio.getChildAt(0).getPaddingTop() + radio.getChildAt(0).getPaddingBottom()) * radio.getChildCount()
                + titleView.getHeight() + titleView.getPaddingTop() + titleView.getPaddingBottom()
                + dialog.getButton(DialogInterface.BUTTON_POSITIVE).getHeight() +  dialog.getButton(DialogInterface.BUTTON_POSITIVE).getPaddingBottom() +  dialog.getButton(DialogInterface.BUTTON_POSITIVE).getPaddingTop();

        int height = (int)Math.min(screenHeight * 0.85, radioHeight);

        dialog.getWindow().setLayout(((int)(screenWidth * 0.85)), height);
    }

    private void setUpRadio(View root) {
        radio = (RadioGroup) root.findViewById(R.id.radio_group);
        for (int i = 0; i < values.length; i++) {
            RadioButton button = new RadioButton(getActivity());
            button.setId(i);

            button.setLayoutParams(
                    new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));

            button.setText(values[i]);
            button.setPadding(32, 20, 20, 32);
            button.setTextColor(textColor);

            if (i == selectedItemIndex)
                button.setChecked(true);
            radio.addView(button);
        }


    }

    private void setUpButtonListener() {
        radio.setOnCheckedChangeListener((group, checkedId) -> {

            View radioButton = radio.findViewById(checkedId);
            int index = radio.indexOfChild(radioButton);

            listener.onAccept(index, values[index]);
            this.dismiss();
        });
    }



    public RadioDialog addListener(OnSaveListener listener) {
        this.listener = listener;
        return this;
    }

    public RadioDialog addDisplayedValues(String[] values) {
        this.values = values;
        return this;
    }

    public RadioDialog addTitle(String title) {
        this.title = title;
        return this;
    }

    public RadioDialog addSelectedItemIndex(int index) {
        selectedItemIndex = index;
        return this;
    }


    public RadioDialog addColors(@ColorInt int textColor, @ColorInt int backgroundColor, @ColorInt int accentColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.accentColor = accentColor;
        return this;
    }

    public interface OnSaveListener {
        void onAccept(int selectedIndex, String selectedValue);
    }


}
