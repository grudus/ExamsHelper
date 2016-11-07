package com.grudus.nativeexamshelper.activities;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.Chart;
import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.adapters.StatisticsAdapter;
import com.grudus.nativeexamshelper.charts.DefaultChartGenerator;
import com.grudus.nativeexamshelper.charts.SubjectChartGenerator;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.layouts.Hamburger;
import com.grudus.nativeexamshelper.pojos.Subject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StatisticMainActivity extends AppCompatActivity {

    private static final String TAG = "@@@" + StatisticMainActivity.class.getSimpleName();
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recycler_view_stats)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar_spinner)
    Spinner spinner;

    @BindView(R.id.progress_bar_stats_parent)
    LinearLayout progressBarParent;

    @BindView(R.id.progress_bar_stats)
    ProgressBar progressBar;

    private Subscription findSubjectSubscription;

    private ExamsDbHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_main);
        ButterKnife.bind(this);

        setUpDatabase();

        setUpHamburger();
        setUpToolbar();

        showSubjectTitlesInToolbar();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (findSubjectSubscription != null && !findSubjectSubscription.isUnsubscribed())
                    findSubjectSubscription.unsubscribe();

                String subjectTitle = (String) parent.getItemAtPosition(position);

                if (subjectTitle.equals(getString(R.string.stats_toolbar_main))) {
                    initDefaultCharts();
                    return;
                }

                progressBarParent.setVisibility(View.VISIBLE);
                findSubjectSubscription = ExamsDbHelper.getInstance(StatisticMainActivity.this)
                        .findSubjectByTitle(subjectTitle)
                        .flatMap(subject -> showCharts(subject))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(charts -> {
                                    StatisticsAdapter adapter = new StatisticsAdapter(charts);
                                    recyclerView.setAdapter(adapter);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(StatisticMainActivity.this));
                                },
                                error -> {
                                    Log.e(TAG, "onItemSelected: ", error);
                                    progressBarParent.setVisibility(View.GONE);
                                }, () -> progressBarParent.setVisibility(View.GONE));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpDatabase() {
        dbHelper = ExamsDbHelper.getInstance(this);
        dbHelper.openDBReadOnly();
    }

    private Observable<ArrayList<Chart>> showCharts(Subject subject) {

        ArrayList<Chart> charts = new ArrayList<>();
        SubjectChartGenerator generator = new SubjectChartGenerator(this, subject);

        return generator.calculateCountOfExamsPerMonth()
                .flatMap(chart -> {
                    charts.add(chart);
                    return generator.calculateAverage();
                })
                .flatMap(chart -> {
                    charts.add(chart);
                    return Observable.create(sub -> {
                        sub.onNext(charts);
                        sub.onCompleted();
                    });
                });


    }

    private void setUpHamburger() {
        Hamburger hamburger = new Hamburger(this, R.id.nvView, drawerLayout);

        hamburger.setSelectedItem(0);
        hamburger.setUpNavigationView();
        hamburger.setUpToolbar(toolbar);
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void showSubjectTitlesInToolbar() {
        dbHelper.getAllSubjectWithGradeTitles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cursor -> {
                    ArrayList<CharSequence> subjects = new ArrayList<>(cursor.getCount() + 1);
                    subjects.add(getString(R.string.stats_toolbar_main));
                    if (cursor.moveToFirst()) {
                        do {
                            subjects.add(cursor.getString(0));
                        } while (cursor.moveToNext());
                    }
                    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, R.layout.default_text_view, subjects);
                    spinner.setAdapter(adapter);
                    if (subjects.size() == 1)
                        spinner.setEnabled(false);

                }, error -> {
                    Log.e(TAG, "getAllSubjectWithGradeTitles: error", error);
                });
    }

    private void initDefaultCharts() {

        if (!spinner.isEnabled()) {
            recyclerView.setVisibility(View.GONE);
            this.findViewById(R.id.no_exams_stats).setVisibility(View.VISIBLE);
            return;
        }

        ArrayList<Chart> charts = new ArrayList<>();
        DefaultChartGenerator generator = new DefaultChartGenerator(this);


        progressBarParent.setVisibility(View.VISIBLE);
        generator.calculateCountOfExamsPerMonth()
                .flatMap(chart -> {
                    charts.add(chart);
                    return generator.calculateAverage();
                })
                .flatMap(chart -> {
                    charts.add(chart);
                    return generator.calculateExamsPerCent();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chart -> {
                    charts.add(chart);
                    StatisticsAdapter adapter = new StatisticsAdapter(charts);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                }, error -> {
                    Log.e(TAG, "initDefaultCharts: ", error);
                    progressBarParent.setVisibility(View.GONE);
                }, () -> progressBarParent.setVisibility(View.GONE));

    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.closeDB();
    }
}
