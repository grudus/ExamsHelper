package com.grudus.nativeexamshelper.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.helpers.normal.ColorHelper;
import com.grudus.nativeexamshelper.layouts.ColorPickerSeekBars;
import com.grudus.nativeexamshelper.pojos.Subject;

public class EditSubjectDialog extends DialogFragment {

    private final String TAG = "@@@" + this.getClass().getSimpleName();

    private Subject editedSubject = Subject.empty();
    protected OnSaveListener onSaveListener;

    private EditText subjectInput;
    private View root;
    private View colorView;
    private ColorPickerSeekBars colorPickerSeekBars;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initViews(builder);
        setUpListeners();
        useSubjectInfo();
        initOnGlobalLayoutListener();

        builder.setView(root);
        return builder.create();
    }

    private void initViews(AlertDialog.Builder builder) {
        root = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_subject, null);
        colorView = root.findViewById(R.id.dialog_edit_subject_colorview);
        colorPickerSeekBars = (ColorPickerSeekBars) root.findViewById(R.id.color_picker_seek_bars);
        subjectInput = (EditText) root.findViewById(R.id.edit_subject_title);

        builder.setPositiveButton(getString(R.string.button_text_save), null);
        builder.setNegativeButton(getString(R.string.button_text_back), null);
    }

    private void setUpListeners() {
        colorPickerSeekBars.addListener(((progress, color) -> {
            colorView.setBackgroundColor(color);
        }));
    }

    private void useSubjectInfo() {
        if (editedSubject.isEmpty())
            return;
        colorPickerSeekBars.setColor(editedSubject.getColor());
        subjectInput.setText(editedSubject.getTitle());
    }

    private void initOnGlobalLayoutListener() {
        root.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        setWidthTo90percentOfScreenWidth();
                        setUpButtons();
                        setButtonColors();
                        setBackgroundColor();
                    }
                });
    }


    protected void setUpButtons() {
        final AlertDialog dialog = (AlertDialog) getDialog();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
                {
                    if (inputIsEmpty()) {
                        Toast.makeText(getActivity(), getString(R.string.warning_add_subject_empty), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!inputIsCorrect()) {
                        Toast.makeText(getActivity(), getString(R.string.warning_add_subject_badchar), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateSubject();
                    if (onSaveListener != null)
                        onSaveListener.onCorrectInputs(editedSubject);
                    this.dismiss();
                });

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> this.dismiss());
    }

    protected boolean inputIsEmpty() {
        return subjectInput.getText().toString().replaceAll("\\s+", "").isEmpty();
    }

    protected boolean inputIsCorrect() {
        return Character.isLetter(subjectInput.getText().toString().charAt(0));
    }

    private void setWidthTo90percentOfScreenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (metrics.widthPixels * 0.9);
        getDialog().getWindow().setLayout(params.width, params.height);
    }

    private void setButtonColors() {
        final int color = ColorHelper.getThemeColor(getActivity(), R.attr.colorAccent);

        final AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(color);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(color);
    }

    private void setBackgroundColor() {
        getDialog().getWindow()
                .setBackgroundDrawable(new ColorDrawable(
                        ColorHelper.getThemeColor(getActivity(), R.attr.dialogBackground)
                ));
    }


    private void updateSubject() {
        final int color = colorPickerSeekBars.getColor();
        final String hex = ColorHelper.getHexColor(color);

        editedSubject.setColor(hex);
        editedSubject.setTitle(subjectInput.getText().toString());
    }

    public EditSubjectDialog addSubject(Subject subject) {
        this.editedSubject = subject.copy();
        return this;
    }

    public EditSubjectDialog addListener(OnSaveListener listener) {
        this.onSaveListener = listener;
        return this;
    }

    public interface OnSaveListener {
        void onCorrectInputs(Subject editedSubject);
    }

}
