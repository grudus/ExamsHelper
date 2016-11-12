package com.grudus.nativeexamshelper.adapters;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.database.exams.ExamEntry;
import com.grudus.nativeexamshelper.pojos.Exam;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UngradedExamsAdapter extends RecyclerView.Adapter<UngradedExamsAdapter.UngradedExamViewHolder> {

    private Cursor cursor;
    private ItemClickListener listener;

    private final ExamsDbHelper dbHelper;

    private int cursorSize = 0;

    public UngradedExamsAdapter(Context context, Cursor cursor, ItemClickListener itemClickListener) {
        this.cursor = cursor;
        this.listener = itemClickListener;

        cursorSize = cursor.getCount();
        dbHelper = ExamsDbHelper.getInstance(context);
    }

    @Override
    public UngradedExamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_exam, parent, false);
        return new UngradedExamViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UngradedExamViewHolder holder, int position) {
        cursor.moveToPosition(position);

        Exam exam = getExamByPosition(position);

        bindViews(holder, exam);

    }

    private void bindViews(UngradedExamViewHolder holder, Exam exam) {
        dbHelper.findSubjectById(exam.getSubjectId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subject -> {
                    bindTextView(holder, subject.getTitle());
                    bindInfoView(holder);
                    bindIcon(holder, subject.getTitle(), subject.getColor());
                });
    }

    private void bindTextView(UngradedExamViewHolder holder, String subject) {
        holder.textView.setText(subject);
    }

    private void bindInfoView(UngradedExamViewHolder holder) {
        String info = cursor.getString(ExamEntry.INFO_COLUMN_INDEX);
        holder.infoView.setText(info);
    }

    private void bindIcon(UngradedExamViewHolder holder, String subject, String color) {
        holder.iconView.setText(subject.substring(0,1).toUpperCase());
        GradientDrawable bg = (GradientDrawable) holder.iconView.getBackground();
        bg.setColor(Color.parseColor(color));
        holder.iconView.setBackground(bg);
    }

    @Override
    public int getItemCount() {
        return cursorSize;
    }


    public void examHasGrade(int position, Cursor newCursor) {
        notifyItemRemoved(position);
        cursor.close();
        cursor = newCursor;
        cursorSize = cursor.getCount();
    }

    public Exam getExamByPosition(int position) {
        cursor.moveToPosition(position);
        Long id = cursor.getLong(ExamEntry.INDEX_COLUMN_INDEX);
        Long subjectId = cursor.getLong(ExamEntry.SUBJECT_ID_COLUMN_INDEX);
        String info = cursor.getString(ExamEntry.INFO_COLUMN_INDEX);
        Date date = new Date(cursor.getLong(ExamEntry.DATE_COLUMN_INDEX));

        return new Exam(id, subjectId, info, date);
    }

    public void closeCursor() {
        cursor.close();
        cursorSize = 0;
    }

    public class UngradedExamViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_icon_text) TextView iconView;
        @BindView(R.id.list_item_adding_exam_subject) TextView textView;
        @BindView(R.id.list_item_adding_exam_date) TextView infoView;

        public UngradedExamViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.itemClicked(v, getAdapterPosition());
        }
    }
}
