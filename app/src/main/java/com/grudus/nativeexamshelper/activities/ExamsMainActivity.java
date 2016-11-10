package com.grudus.nativeexamshelper.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.activities.fragments.AddingExamFragment;
import com.grudus.nativeexamshelper.activities.fragments.OldExamsFragment;
import com.grudus.nativeexamshelper.activities.sliding.SlidingTabLayout;
import com.grudus.nativeexamshelper.adapters.ViewPagerAdapter;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.layouts.Hamburger;
import com.grudus.nativeexamshelper.net.ServerTransporter;
import com.grudus.nativeexamshelper.pojos.UserPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ExamsMainActivity extends AppCompatActivity {

    public static final String TAG = "@@@" + ExamsMainActivity.class.getSimpleName();


    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.tabs)
    SlidingTabLayout slidingTabLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Hamburger hamburger;

    private ExamsDbHelper examsDbHelper;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_exams);
        ButterKnife.bind(this);

        initDatabase();
        ServerTransporter.tryToShareDataWithServer(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(i -> {
                        },
                        error -> {
                            initViewPager();
                        },
                        () -> {
                            initViewPager();
                            new UserPreferences(this).changeLastModifiedToNow();
                        });


        hamburger = new Hamburger(this, R.id.nvView, drawerLayout);
        hamburger.setSelectedItem(1);
        hamburger.setUpNavigationView();
        setUpToolbar();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initDatabase() {
        examsDbHelper = ExamsDbHelper.getInstance(this);
        examsDbHelper.openDB();
    }

    private void initViewPager() {
        String[] tabs = getResources().getStringArray(R.array.tab_titles);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabs);

        viewPager.setAdapter(viewPagerAdapter);

        slidingTabLayout.setDistributeEvenly(true);

        slidingTabLayout.setCustomTabColorizer(position ->
                ContextCompat.getColor(getApplicationContext(), R.color.tabsScrollColor));

        slidingTabLayout.setViewPager(viewPager);
    }


    private void setUpToolbar() {
        toolbar.setTitle(getString(R.string.toolbar_main_title));
        setSupportActionBar(toolbar);
        hamburger.setUpToolbar(toolbar);
    }


    @Override
    protected void onPause() {
        super.onPause();
        examsDbHelper.closeDB();
        ((AddingExamFragment) viewPagerAdapter.getFragment(0)).closeDatabase();
        ((OldExamsFragment) viewPagerAdapter.getFragment(1)).closeDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        examsDbHelper.openDB();
    }

}
