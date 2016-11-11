package com.grudus.nativeexamshelper.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.database.subjects.SubjectsQuery;
import com.grudus.nativeexamshelper.dialogs.reusable.ConfirmDialog;
import com.grudus.nativeexamshelper.dialogs.reusable.RadioDialog;
import com.grudus.nativeexamshelper.helpers.ToastHelper;
import com.grudus.nativeexamshelper.helpers.normal.CalendarHelper;
import com.grudus.nativeexamshelper.helpers.normal.ColorHelper;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.pojos.UserPreferences;
import com.grudus.nativeexamshelper.pojos.grades.Grades;
import com.grudus.nativeexamshelper.pojos.grades.UniversityGrade;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.onActivityCreateSetTheme(this);

        setContentView(R.layout.settings_layout);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainSettingsFragment())
                .commit();
        setUpToolbar();

    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null)
            return;
        toolbar.setTitle(getString(R.string.title_activity_settings));
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if (bar == null)
            return;
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public static class MainSettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener  {

        private String NIGHT_MODE_KEY ;
        private String GRADE_TYPE_KEY ;
        private String GRADE_DECIMAL_KEY ;
        private String FABRIC_EXAMS_KEY ;
        private String FABRIC_SUBJECTS_KEY ;
        private String SYNC_CALENDAR_KEY;

        private Subscription subscription;

        private boolean deletePermanatelly;

        private ToastHelper toast;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_preferences);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            setMargins(view);
            getListView().setBackgroundColor(ColorHelper.getThemeColor(getActivity(), R.attr.background));
            
            deletePermanatelly = !PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getBoolean(getString(R.string.key_user_is_logged), false);

            toast = new ToastHelper(getActivity());
            
            initKeys();

        }

        private void initKeys() {
            NIGHT_MODE_KEY = getActivity().getString(R.string.key_night_mode);
            GRADE_TYPE_KEY = getActivity().getString(R.string.key_grades_type);
            GRADE_DECIMAL_KEY = getActivity().getString(R.string.key_grades_decimal);
            FABRIC_EXAMS_KEY = getActivity().getString(R.string.key_fabric_exams);
            FABRIC_SUBJECTS_KEY = getActivity().getString(R.string.key_fabric_subjects);
            SYNC_CALENDAR_KEY = getString(R.string.key_grades_calendar);
        }

        private void setMargins(View view) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            TypedValue tv = new TypedValue();
            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            {
                params.topMargin = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }

            view.setLayoutParams(params);
        }

        private ListView getListView() {
            View view = getView();
            if (view == null) {
                throw new IllegalStateException("Content view not yet created");
            }

            View listView = view.findViewById(android.R.id.list);
            if (!(listView instanceof ListView)) {
                throw new RuntimeException("Content has view with id attribute 'android.R.id.list' that is not a ListView class");
            }
            return (ListView) listView;
        }


        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);

            if (subscription != null && !subscription.isUnsubscribed())
                subscription.unsubscribe();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference.getKey().equals(GRADE_TYPE_KEY)) {

                new ConfirmDialog()
                        .addTitle(getString(R.string.dialog_confirm_pref_title))
                        .addText(getString(R.string.dialog_confirm_pref_content))
                        .addListener((dialog, which) -> {
                            if (which == DialogInterface.BUTTON_POSITIVE)
                                new RadioDialog()
                                    .addTitle(getString(R.string.pref_grades))
                                    .addDisplayedValues(getResources().getStringArray(R.array.pref_grades_entries))
                                    .addSelectedItemIndex(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(GRADE_TYPE_KEY, 0))
                                    .addListener(((selectedIndex, selectedValue) -> {
                                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                        editor.putInt(GRADE_TYPE_KEY, selectedIndex);
                                        editor.apply();
                                        Grades.setGradeMode(selectedIndex, getActivity());

                                        cleanDb(ExamsDbHelper.getInstance(getActivity()));
                                    }))
                                    .show(getFragmentManager(), getString(R.string.tag_dialog_radio));
                        }).show(getFragmentManager(), getString(R.string.tag_dialog_confirm));

            }

            else if (preference.getKey().equals(FABRIC_EXAMS_KEY)) {
                ExamsDbHelper helper = ExamsDbHelper.getInstance(getActivity());
                helper.openDB();
                subscription = Observable.merge((deletePermanatelly ? helper.removeAllExams() : helper.updateAllExamsSetDeleteChange()),
                        helper.updateAllSubjectsSetHasGrade(false))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(i -> {
                                            Toast.makeText(getActivity(), "All exams deleted", Toast.LENGTH_SHORT).show();
                                        },
                                        error -> {
                                            toast.showErrorMessage("An error occurs", error);});
            }

            else if (preference.getKey().equals(FABRIC_SUBJECTS_KEY)) {
                ExamsDbHelper helper = ExamsDbHelper.getInstance(getActivity());
                helper.openDB();
               cleanDb(helper);
            }


            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void cleanDb(ExamsDbHelper helper) {
            SubjectsQuery.setDefaultSubjects(getActivity().getResources().getStringArray((Grades.getCurrentGrade() instanceof UniversityGrade) ? R.array.default_subjects_university : R.array.default_subjects));
            SubjectsQuery.setDefaultColors(getActivity().getResources().getStringArray(R.array.defaultSubjectsColors));
            subscription =
                    (deletePermanatelly ? helper.removeAllSubjects() : helper.updateAllSubjectsSetChangeDelete())
                            .flatMap(i -> deletePermanatelly ? helper.removeAllExams() : helper.updateAllExamsSetDeleteChange())
                            .flatMap(i -> helper.firstSubjectsInsert())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(i -> {
                                        Toast.makeText(getActivity(), "Subjects are fresh", Toast.LENGTH_SHORT).show();
                                        new UserPreferences(getActivity()).changeLastModifiedToNow();
                                    },
                                    error -> {
                                        toast.showErrorMessage("An error occurs", error);
                                    });

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(NIGHT_MODE_KEY)) {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
                if (pref.isChecked())
                    ThemeHelper.changeToTheme(getActivity(), ThemeHelper.THEME_DARK);
                else
                    ThemeHelper.changeToTheme(getActivity(), ThemeHelper.THEME_DEFAULT);
            }

            else if (key.equals(GRADE_DECIMAL_KEY)) {
                SwitchPreference pref = (SwitchPreference) findPreference(key);
                Grades.enableDecimalsInGrades(!pref.isChecked());
            }
            
            else if (key.equals(SYNC_CALENDAR_KEY)) {
                SwitchPreference pref = (SwitchPreference) findPreference(key);
                if (pref.isChecked()) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_CALENDAR)) {
                            Toast.makeText(getActivity(), R.string.toast_calendar_no_permission, Toast.LENGTH_LONG).show();
                            pref.setChecked(false);
                        }

                        else {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_CALENDAR}, CalendarHelper.MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
                            pref.setChecked(false);
                        }
                    }
                }
                
            }

        }
    }
}
