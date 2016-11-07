package com.grudus.nativeexamshelper.dialogs.reusable;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.helpers.normal.ColorHelper;

public class ConfirmDialog extends DialogFragment {

    private View root;
    private TextView textView;
    private TextView titleView;
    private DialogInterface.OnClickListener listener;

    private String title;
    private String preText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());

        initViews(builder);
        initOnGlobalLayoutListener();


        builder.setView(root);
        return builder.create();
    }

    private void initViews(android.support.v7.app.AlertDialog.Builder builder) {
        root = getActivity().getLayoutInflater().inflate(R.layout.dialog_confirm, null);
        titleView = (TextView) root.findViewById(R.id.dialog_input_text_title);

        if (title == null || title.replaceAll("\\s+", "").isEmpty())
            titleView.setVisibility(View.GONE);
        else titleView.setText(title);

        textView = (TextView) root.findViewById(R.id.dialog_text_view);

        if (preText == null || preText.replaceAll("\\s+", "").isEmpty())
            textView.setVisibility(View.GONE);
        else textView.setText(preText);

        builder.setPositiveButton(getString(R.string.button_confirm_yes), listener);
        builder.setNegativeButton(getString(R.string.button_confirm_no), ((dialog, which) -> this.dismiss()));
    }

    private void initOnGlobalLayoutListener() {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setUpButtonColors();
                setUpDialogColors();
            }
        });
    }

    private void setUpDialogColors() {
        android.support.v7.app.AlertDialog dialog = (android.support.v7.app.AlertDialog) getDialog();
        final int color = ColorHelper.getThemeColor(getActivity(), R.attr.dialogBackground);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(color));

    }


    private void setUpButtonColors() {
        final int color = ColorHelper.getThemeColor(getActivity(), R.attr.colorAccent);

        final android.support.v7.app.AlertDialog dialog = (android.support.v7.app.AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(color);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(color);
    }

    public ConfirmDialog addListener(DialogInterface.OnClickListener listener) {
        this.listener = listener;
        return this;
    }

    public ConfirmDialog addTitle(String title) {
        this.title = title;
        return this;
    }

    public ConfirmDialog addText(String text) {
        this.preText = text;
        return this;
    }


}
