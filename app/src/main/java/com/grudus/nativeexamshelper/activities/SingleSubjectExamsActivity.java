package com.grudus.nativeexamshelper.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.activities.touchhelpers.ItemRemoveCallback;
import com.grudus.nativeexamshelper.adapters.SingleSubjectExamsAdapter;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.helpers.GradeStatisticsCalculator;
import com.grudus.nativeexamshelper.helpers.StatisticsTextFormatter;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.pojos.Subject;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SingleSubjectExamsActivity extends AppCompatActivity {

    public static final String INTENT_SUBJECT_TAG = "subject";
    private final String TAG = "@@@" + this.getClass().getSimpleName();

    @BindView(R.id.recycler_view_single_subject_exams)
    RecyclerView recyclerView;

    @BindView(R.id.sse_info_layout)
    LinearLayout infoLayout;

    private SingleSubjectExamsAdapter adapter;
    private String subjectTitle;
    private ExamsDbHelper dbHelper;
    private Subject subject;

    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_subject_exams);
        ButterKnife.bind(this);

        getIntentInformation();

        setUpToolbar();

        openDatabase();
        initRecyclerView();

        calculateAllStatistics();
        updateStatisticsLabels();

        initSwipeListener();

    }

    private void initSwipeListener() {
        ItemRemoveCallback itemRemoveCallback = new ItemRemoveCallback(0, ItemTouchHelper.RIGHT, null);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemRemoveCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void getIntentInformation() {
        subject = getIntent().getParcelableExtra(INTENT_SUBJECT_TAG);

        if (subject.getId() == null)
            throw new IllegalArgumentException("Id cannot be null! " + subject.toString());

        subjectTitle = subject.getTitle();
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setTitle(subjectTitle);
        setSupportActionBar(toolbar);
    }

    private void openDatabase() {
        dbHelper = ExamsDbHelper.getInstance(this);
        dbHelper.openDBReadOnly();
    }

    private void initRecyclerView() {
        subscription =
        dbHelper.getSubjectGradesWithoutDeleteChange(subject.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cursor -> {
                    adapter = new SingleSubjectExamsAdapter(this, cursor, subject);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                });
    }


    @SuppressWarnings({"ConstantConditions"})
    private void updateStatisticsLabels() {
        ((TextView) findViewById(R.id.sse_info_average))
                .setText(getString(R.string.sse_info_average, StatisticsTextFormatter.getAverage()));
        ((TextView) findViewById(R.id.sse_info_median))
                .setText(getString(R.string.sse_info_median, StatisticsTextFormatter.getMedian()));
        ((TextView) findViewById(R.id.sse_info_dominant))
                .setText(getString(R.string.sse_info_dominant, StatisticsTextFormatter.getDominant()));
        ((TextView) findViewById(R.id.sse_info_passed))
                .setText(getString(R.string.sse_info_passed, StatisticsTextFormatter.getPassedExams()));
        ((TextView) findViewById(R.id.sse_info_failed))
                .setText(getString(R.string.sse_info_failed, StatisticsTextFormatter.getFailedExams()));
        ((TextView) findViewById(R.id.sse_info_percent))
                .setText(getString(R.string.sse_info_percent, StatisticsTextFormatter.getPercentOfPassedExams()));
    }


    private void calculateAllStatistics() {
        GradeStatisticsCalculator calculator = new GradeStatisticsCalculator(this, subject.getId(), Grades.getFirstPassedGrade());
        setUpStatisticsTextFormatter(calculator);

        calculator.startCalculating()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(calculator::calculate,
                        calculator::onError,
                        () -> {
                            calculator.onCompleteCalculations();
                            updateStatisticsLabels();
                        });

    }

    private void setUpStatisticsTextFormatter(GradeStatisticsCalculator calculator) {
        StatisticsTextFormatter.setCalculator(calculator);
        StatisticsTextFormatter.setNoDominantText(getString(R.string.sse_info_lack_dominant));
    }
    

    @Override
    protected void onPause() {
        super.onPause();
        if (!subscription.isUnsubscribed())
            subscription.unsubscribe();
        dbHelper.closeDB();
        adapter.closeCursor();
    }



}

