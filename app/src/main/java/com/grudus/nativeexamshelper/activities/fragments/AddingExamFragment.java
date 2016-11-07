package com.grudus.nativeexamshelper.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.activities.AddExamActivity;
import com.grudus.nativeexamshelper.adapters.ExamsAdapter;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AddingExamFragment extends Fragment {

    private final String TAG = "@@@" + this.getClass().getSimpleName();

    @BindView(R.id.floating_button_add_exam) FloatingActionButton floatingActionButton;
    @BindView(R.id.recycler_view_adding_exam_content) RecyclerView recyclerView;

    private ExamsDbHelper examsDbHelper;
    private ExamsAdapter adapter;
    private Subscription subscription;


    public AddingExamFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_adding_exam, container, false);

        ButterKnife.bind(this, view);

        return view;
    }


    @OnClick(R.id.floating_button_add_exam)
    public void addNewExam() {
        Intent openAddExamActivity = new Intent(getActivity(), AddExamActivity.class);
        startActivity(openAddExamActivity);
    }


    @Override
    public void onStart() {
        super.onStart();
        initDatabase();
        populateRecyclerView();
    }



    private void initDatabase() {
        if (examsDbHelper == null && getActivity() != null)
            examsDbHelper = ExamsDbHelper.getInstance(getContext());
    }


    private void populateRecyclerView() {
        subscription =
            examsDbHelper.getAllIncomingExamsWithoutDeleteChangeSortByDate()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(cursor -> {
                    adapter = new ExamsAdapter(getContext(), cursor);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                }, error -> Log.e(TAG, "populateRecyclerView: ERRRRRRR", error));

    }

    @Override
    public void onPause() {
        super.onPause();
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

    public void closeDatabase() {
        if (adapter != null)
            adapter.closeDatabase();
    }


    public void removeAll() {
        subscription =
                examsDbHelper.removeAllExams()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(howManyRows -> {
                   if (howManyRows > 0) {
                       adapter.changeCursor(null);
                       adapter.notifyItemRangeRemoved(0, howManyRows);
                   }
                }, onError -> examsDbHelper.closeDB());
    }
}
