package com.grudus.nativeexamshelper.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.activities.touchhelpers.SwipeToDeleteable;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.database.exams.ExamEntry;
import com.grudus.nativeexamshelper.helpers.normal.DateHelper;
import com.grudus.nativeexamshelper.pojos.Subject;
import com.grudus.nativeexamshelper.pojos.UserPreferences;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SingleSubjectExamsAdapter extends RecyclerView.Adapter<SingleSubjectExamsAdapter.SingleSubjectExamsViewHolder> {

    private final Context context;
    private Cursor cursor;
    private String defaultInfo;
    private final ExamsDbHelper examsDbHelper;
    private Subject subject;

    private int cursorSize = 0;
    private boolean deletePermanatelly;

    public SingleSubjectExamsAdapter(Context context, Cursor cursor, Subject subject) {
        this.context = context;
        this.cursor = cursor;
        defaultInfo = context.getResources().getString(R.string.sse_default_exam_info);
        examsDbHelper = ExamsDbHelper.getInstance(context);
        this.subject = subject;
        cursorSize = cursor.getCount();

        deletePermanatelly = !PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.key_user_is_logged), false);
    }

    @Override
    public SingleSubjectExamsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_single_subject_exams, parent, false);
        return new SingleSubjectExamsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SingleSubjectExamsViewHolder holder, int position) {

        cursor.moveToPosition(position);
        double grade = cursor.getDouble(ExamEntry.GRADE_COLUMN_INDEX);
        
        bindDate(holder);
        bindSubjectInfo(holder);
        bindColor(holder, grade);
        bindGrade(holder, grade);
    }

    private void bindDate(SingleSubjectExamsViewHolder holder) {
        final long date = cursor.getLong(ExamEntry.DATE_COLUMN_INDEX);
        holder.dateView.setText(
                DateHelper.getReadableDataFromLong(date));

    }

    private void bindSubjectInfo(SingleSubjectExamsViewHolder holder) {
        String subjectInfo = cursor.getString(ExamEntry.INFO_COLUMN_INDEX);
        boolean isEmpty = subjectInfo == null || subjectInfo.replaceAll("\\s+", "").isEmpty();
        holder.infoView.setText(isEmpty ? defaultInfo : subjectInfo);
    }

    private void bindGrade(SingleSubjectExamsViewHolder holder, double grade) {
        holder.iconView.setText(Grades.gradeToString(grade));
    }

    private void bindColor(SingleSubjectExamsViewHolder holder, double grade) {
        GradientDrawable bg = (GradientDrawable) holder.iconView.getBackground();
        bg.setColor(Color.parseColor(context.getResources().getStringArray(R.array.gradesColors)[(int)(grade-0.5)]));
        holder.iconView.setBackground(bg);
    }

    @Override
    public int getItemCount() {
        return cursorSize;
    }
    
    public void closeCursor() {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        cursorSize = 0;
    }

    private void deleteExamAtPosition(int adapterPosition) {
        cursor.moveToPosition(adapterPosition);
        final long examId = cursor.getLong(ExamEntry.INDEX_COLUMN_INDEX);
        final long subjectId = cursor.getLong(ExamEntry.SUBJECT_ID_COLUMN_INDEX);

        (deletePermanatelly ? examsDbHelper.removeExam(examId) : examsDbHelper.updateExamSetDeleteChange(examId))
                .flatMap(o -> examsDbHelper.getSubjectGradesWithoutDeleteChange(subjectId))
                .flatMap(cursor -> {
                    changeCursor(cursor);
                    return examsDbHelper.setSubjectHasGrade(subject, cursor.moveToFirst());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(howMany -> {},
                        onError -> {},
                        () -> {
                            notifyItemRemoved(adapterPosition);
                            new UserPreferences(context).changeLastModifiedToNow();
                        });
    }

    public void changeCursor(Cursor _new) {
        closeCursor();
        cursor = _new;
        cursorSize = cursor.getCount();
    }

    public class SingleSubjectExamsViewHolder extends RecyclerView.ViewHolder implements SwipeToDeleteable {

        @BindView(R.id.sse_list_item_icon) TextView iconView;
        @BindView(R.id.sse_list_item_info) TextView infoView;
        @BindView(R.id.sse_list_item_date) TextView dateView;
        @BindView(R.id.list_item_sse_layout) ViewGroup swipedLayout;
        
        public SingleSubjectExamsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        @Override
        public ViewGroup getSwipedLayout() {
            return swipedLayout;
        }

        @Override
        public void clearView() {
            swipedLayout.setX(0);
            swipedLayout.setTranslationX(0);
        }

        @Override
        public void delete(int position) {
            deleteExamAtPosition(position);
        }
    }
}
