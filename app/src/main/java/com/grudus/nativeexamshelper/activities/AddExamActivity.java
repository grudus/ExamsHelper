package com.grudus.nativeexamshelper.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.grudus.nativeexamshelper.helpers.normal.CalendarHelper;
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

    @BindView(R.id.add_exam_date_input)
    EditText dateInput;
    @BindView(R.id.add_exam_subject_input)
    EditText subjectInput;
    @BindView(R.id.add_exam_extras_input)
    EditText extrasInput;
    @BindView(R.id.add_exam_time_input)
    EditText timeInput;
    @BindView(R.id.add_exam_button)
    Button addExamButton;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private CalendarDialogHelper calendarDialog;
    private TimeDialogHelper timeDialog;

    private ExamsDbHelper db;

    private boolean test;
    private CalendarHelper calendarHelper;

    private String examSubjectTitle;
    private String examInfo;
    private Date examDate;

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

        calendarHelper = new CalendarHelper(this);
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
    void addExam() {
        examSubjectTitle = subjectInput.getText().toString();
        String date = dateInput.getText().toString();

        if (!inputsAreCorrect(examSubjectTitle, date)) return;

        examDate = getDateWithTime();

        examInfo = extrasInput.getText().toString();
        if (examInfo.replaceAll("\\s+", "").isEmpty())
            examInfo = getString(R.string.sse_default_exam_info);


        addToDatabase();
        new UserPreferences(this).changeLastModifiedToNow();

        if (savingInCalendarEnabled())
            saveInCalendar();

        else
            startPreviousActivity();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == CalendarHelper.MY_PERMISSIONS_REQUEST_WRITE_CALENDAR) {

            if (permissionWasAdded(grantResults))
                calendarHelper.addToCalendar(examSubjectTitle, examInfo, examDate);

        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startPreviousActivity();
    }

    private boolean permissionWasAdded(int[] grantResults) {
        return grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }


    private boolean savingInCalendarEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.key_grades_calendar), false);
    }

    private void saveInCalendar() {
        if (applicationHasPermission()) {
            calendarHelper.addToCalendar(examSubjectTitle, examInfo, examDate);
            startPreviousActivity();
        }

        else {
            requestPermission();
        }
    }

    private boolean applicationHasPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR))
            Toast.makeText(this, R.string.toast_calendar_no_permission, Toast.LENGTH_LONG).show();

        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, CalendarHelper.MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);

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

    private void addToDatabase() {
        if (db == null)
            db = ExamsDbHelper.getInstance(this);
        db.openDB();

        db.findSubjectByTitle(examSubjectTitle)
                .flatMap(subject -> db.insertExam(Exam.getExamWithoutId(subject.getId(), examInfo, examDate)))
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
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
