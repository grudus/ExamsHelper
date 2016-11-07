package com.grudus.nativeexamshelper.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.adapters.ItemClickListener;
import com.grudus.nativeexamshelper.adapters.UngradedExamsAdapter;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.dialogs.reusable.RadioDialog;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.pojos.Exam;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UngradedExamsActivity extends AppCompatActivity implements ItemClickListener {
    
    @BindView(R.id.recycler_view_ungraded_exams)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private UngradedExamsAdapter adapter;
    private ExamsDbHelper dbHelper;

    private Subscription subscription;
    private final RadioDialog dialog = new RadioDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ungraded_exams);
        ButterKnife.bind(this);

        toolbar.setTitle(getString(R.string.ungraded_toolbar_text));
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initDatabase();
        initRecyclerView();
    }

    private void initDatabase() {
        dbHelper = ExamsDbHelper.getInstance(this);
        dbHelper.openDB();
    }

    private void initRecyclerView() {
        subscription =
            dbHelper.getExamsWithoutGradeWithoutDeleteChange()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cursor -> {
                        if (cursor.getCount() == 0) {
                            findViewById(R.id.no_exams_view).setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        adapter = new UngradedExamsAdapter(getApplicationContext(), cursor, this);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setHasFixedSize(true);
                    });
    }

    @Override
    protected void onPause() {
        super.onPause();
        dbHelper.closeDB();
        if (adapter != null)
            adapter.closeCursor();
        if (!subscription.isUnsubscribed())
            subscription.unsubscribe();
    }


    @Override
    public void itemClicked(View v, final int position) {

        dialog.addListener(((selectedIndex, selectedValue) -> {
            Exam exam = adapter.getExamByPosition(position);

            subscription =
                    dbHelper.updateExamSetGrade(exam, Grades.findGradeFromString(selectedValue))
                            .flatMap((id) -> dbHelper.getExamsWithoutGradeWithoutDeleteChange())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((cursor) -> {
                                adapter.examHasGrade(position, cursor);
                            });
        }))
                .addTitle(getString(R.string.dialog_select_grade_title))
                .addDisplayedValues(Grades.getAllPossibleGradesAsStrings());

        dialog.show(getFragmentManager(), getString(R.string.tag_dialog_select_grade));
    }

}
