package com.grudus.nativeexamshelper.activities.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.activities.SingleSubjectExamsActivity;
import com.grudus.nativeexamshelper.activities.UngradedExamsActivity;
import com.grudus.nativeexamshelper.adapters.OldExamsAdapter;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.pojos.Subject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OldExamsFragment extends Fragment {

    public final String TAG = "@@@" + this.getClass().getSimpleName();

    @BindView(R.id.recycler_view_old_exams) RecyclerView recyclerView;
    private OldExamsAdapter adapter;
    private ExamsDbHelper examsDbHelper;

    private Subscription subscription;

    public OldExamsFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_old_exams, container, false);
        ButterKnife.bind(this, view);

        return view;
    }



    @Override
    public void onStart() {
        super.onStart();
        initDatabase();
        populateList();
    }

    private void initDatabase() {
        if (examsDbHelper == null && getActivity() != null)
            examsDbHelper = ExamsDbHelper.getInstance(getContext());
    }


    private void populateList() {
        subscription =
            examsDbHelper.getSubjectsWithGrade()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(cursor -> {
                        adapter = new OldExamsAdapter(getActivity(), cursor);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        setOnItemClickListener();
                    });
    }

    private void setOnItemClickListener() {
        adapter.setListener((v, position) -> {
            if (position == OldExamsAdapter.HEADER_POSITION)
                startActivity(new Intent(getContext(), UngradedExamsActivity.class));

            else {
                Subject subject = adapter.getSubjectAtPosition(position);
                Intent intent = new Intent(getContext(), SingleSubjectExamsActivity.class);
                intent.putExtra(SingleSubjectExamsActivity.INTENT_SUBJECT_TAG, subject);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

    public void closeDatabase() {
        adapter.closeDatabase();
    }
}
