package com.grudus.nativeexamshelper.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.dialogs.SelectSubjectDialog;
import com.grudus.nativeexamshelper.dialogs.reusable.EnterTextDialog;
import com.grudus.nativeexamshelper.helpers.dialogs.CalendarDialogHelper;
import com.grudus.nativeexamshelper.helpers.dialogs.TimeDialogHelper;
import com.grudus.nativeexamshelper.helpers.normal.DateHelper;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.helpers.normal.TimeHelper;
import com.grudus.nativeexamshelper.pojos.Exam;
import com.grudus.nativeexamshelper.pojos.UserPreferences;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.schedulers.Schedulers;

public class AddExamActivity extends AppCompatActivity {

    private final String TAG = "@@@" + this.getClass().getSimpleName();

    @BindView(R.id.add_exam_date_input) EditText dateInput;
    @BindView(R.id.add_exam_subject_input) EditText subjectInput;
    @BindView(R.id.add_exam_extras_input) EditText extrasInput;
    @BindView(R.id.add_exam_time_input) EditText timeInput;
    @BindView(R.id.add_exam_button) Button addExamButton;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private CalendarDialogHelper calendarDialog;
    private TimeDialogHelper timeDialog;

    private ExamsDbHelper db;

    private boolean test = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exam);
        ButterKnife.bind(this);

        toolbar.setTitle(getResources().getString(R.string.add_new_exam_toolbar_text));
        setListenerToDeleteTextViewFocus();

        calendarDialog = new CalendarDialogHelper(this, this::updateDateView);
        timeDialog = new TimeDialogHelper(this, this::updateTimeView);
    }

    private void setListenerToDeleteTextViewFocus() {
        extrasInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                deleteFocus();
            }
            return true;
        });

        extrasInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                deleteFocus();
            }
        });
    }

    private void updateDateView() {
        dateInput.setText(DateHelper.getStringFromDate(calendarDialog.getDate()));
        deleteFocus();
    }

    private void updateTimeView() {
        String textToDisplay = TimeHelper.getFormattedTime(
                timeDialog.getHour(),
                timeDialog.getMinute()
        );

        timeInput.setText(textToDisplay);
        deleteFocus();
    }

    @OnClick(R.id.add_exam_date_input)
    void showDatePicker() {
        if (!test)
            calendarDialog.showDialog();
    }

    @OnClick(R.id.add_exam_time_input)
    void showTimePicker() {
        timeDialog.showDialog();
    }

    @OnClick(R.id.add_exam_subject_input)
    void openSubjectsListActivity() {

        new SelectSubjectDialog()
                .addListener(subject -> this.subjectInput.setText(subject.getTitle()))
                .show(getFragmentManager(), getString(R.string.tag_dialog_select_subject));
    }

    @OnClick(R.id.add_exam_extras_input)
    void openEnterTextDialog() {
        new EnterTextDialog()
                .addListener(text -> this.extrasInput.setText(text))
                .show(getFragmentManager(), "qqq");
    }

    @OnClick(R.id.add_exam_button)
    void addExam()  {
        String subject = subjectInput.getText().toString();
        String date = dateInput.getText().toString();

        if (!inputsAreCorrect(subject, date)) return;

        Date correctDate = getDateWithTime();

        String info = extrasInput.getText().toString();
        if (info.replaceAll("\\s+", "").isEmpty())
            info = getString(R.string.sse_default_exam_info);


        addToDatabase(subject, info, correctDate);
        new UserPreferences(this).changeLastModifiedToNow();
        startPreviousActivity();
    }

    private Date getDateWithTime() {
        Calendar temp = calendarDialog.getCalendar();
        temp.add(Calendar.HOUR_OF_DAY, timeDialog.getHour());
        temp.add(Calendar.MINUTE, timeDialog.getMinute());
        return temp.getTime();
    }


    private boolean inputsAreCorrect(String subject, String date) {
        if (subject.replaceAll("\\s+", "").isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.empty_subject_add_exam), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (date.replaceAll("\\s+", "").isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.empty_date_add_exam), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addToDatabase(String subjectTitle, String info, Date correctDate) {
        if (db == null)
            db = ExamsDbHelper.getInstance(this);
        db.openDB();

        db.findSubjectByTitle(subjectTitle)
                .flatMap(subject -> db.insertExam(Exam.getExamWithoutId(subject.getId(), info, correctDate)))
                .subscribeOn(Schedulers.io())
                .subscribe(action -> db.closeDB(), error -> db.closeDB());

    }

    private void startPreviousActivity() {
        Intent goBack = new Intent(getApplicationContext(), ExamsMainActivity.class);
        // new subject has been added, so there is no reason to keep previous activities in stack
        goBack.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goBack);
    }

    private void deleteFocus() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        //noinspection ConstantConditions
        findViewById(R.id.add_exam_layout).requestFocus();
    }

    public void setCalendarDialog(CalendarDialogHelper calendarDialog) {
        this.calendarDialog = calendarDialog;
    }

    public void setTimeDialog(TimeDialogHelper timeDialog) {
        this.timeDialog = timeDialog;
    }

    public void startTesting() {
        test = true;
    }
}
