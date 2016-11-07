package com.grudus.nativeexamshelper.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.adapters.ItemClickListener;
import com.grudus.nativeexamshelper.adapters.SubjectsAdapter;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.pojos.Subject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SelectSubjectDialog extends DialogFragment implements ItemClickListener {

    private RecyclerView recyclerView;
    private SubjectsAdapter adapter;

    private SelectSubjectListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View root = inflater.inflate(R.layout.dialog_select_subject, null);
        builder.setView(root);

        initList(root);

        return builder.create();
    }

    private void initList(View root) {
        recyclerView = (RecyclerView) root.findViewById(R.id.subjects_recycler_view);
        ExamsDbHelper.getInstance(getActivity()).openDB();
        ExamsDbHelper.getInstance(getActivity())
                .getAllSubjectsWithoutDeleteChangeSortByTitle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cursor -> {
                    adapter = new SubjectsAdapter(cursor, getActivity(), this);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                });
    }

    public SelectSubjectDialog addListener(SelectSubjectListener listener) {
        this.listener = listener;
        return this;
    }


    @Override
    public void itemClicked(View v, int position) {
        listener.afterSelect(adapter.getItem(position));
        this.dismiss();
    }



    public interface SelectSubjectListener {
        void afterSelect(Subject subject);
    }
}
