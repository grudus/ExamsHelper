package com.grudus.nativeexamshelper.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.activities.touchhelpers.SwipeToDeleteable;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.database.subjects.SubjectEntry;
import com.grudus.nativeexamshelper.pojos.Subject;
import com.grudus.nativeexamshelper.pojos.UserPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectsViewHolder> {

    private Cursor cursor;
    private ItemClickListener itemClickListener;
    private final Context context;

    private int cursorSize = 0;

    public SubjectsAdapter(Cursor cursor, Context context, ItemClickListener listener) {
        this.cursor = cursor;
        this.itemClickListener = listener;
        this.context = context;
        cursorSize = cursor.getCount();
    }


    @Override
    public SubjectsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_subject, parent, false);
        return new SubjectsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SubjectsViewHolder holder, int position) {
        cursor.moveToPosition(position);

        String titleText = cursor.getString(SubjectEntry.TITLE_COLUMN_INDEX);

        bindTitleText(holder, titleText);
        bindColor(holder);
        bindIconView(holder, titleText);

    }

    private void bindTitleText(SubjectsViewHolder holder, String title) {
        holder.titleView.setText(title);
    }

    private void bindColor(SubjectsViewHolder holder) {
        int color = Color.parseColor(cursor.getString(SubjectEntry.COLOR_COLUMN_INDEX));

        GradientDrawable bgShape = (GradientDrawable) holder.iconView.getBackground();
        bgShape.setColor(color);
        holder.iconView.setBackground(bgShape);
    }

    private void bindIconView(SubjectsViewHolder holder, String titleText) {
        holder.iconView.setText(titleText.substring(0, 1).toUpperCase());
    }

    @Override
    public int getItemCount() {
        return cursorSize;
    }

    public Subject getItem(int position) {
        cursor.moveToPosition(position);
        String color = cursor.getString(SubjectEntry.COLOR_COLUMN_INDEX);
        String titleText = cursor.getString(SubjectEntry.TITLE_COLUMN_INDEX);
        Long id = cursor.getLong(SubjectEntry.INDEX_COLUMN_INDEX);

        return new Subject(id, titleText, color);
    }

    public Long getSubjectId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(SubjectEntry.INDEX_COLUMN_INDEX);
    }

    public void changeCursor(Cursor _new) {
        closeCursor();
        cursor = _new;
        cursorSize = cursor.getCount();
    }


    public void closeCursor() {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        cursorSize = 0;
    }


    public void removeFromDbAndChangeCursor(int adapterPosition) {
        ExamsDbHelper db = ExamsDbHelper.getInstance(context);


        db.updateSubjectSetDelete(getItem(adapterPosition))
                .flatMap(howManyDeleted -> {
                    if (howManyDeleted == 0) return Observable.empty();
                    return db.getAllSubjectsWithoutDeleteChangeSortByTitle();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::changeCursor,
                        onError -> {},
                        () -> {
                            notifyItemRemoved(adapterPosition);
                            new UserPreferences(context).changeLastModifiedToNow();
                        });

        db.removeAllExamsRelatedWithSubject(cursor.getLong(SubjectEntry.INDEX_COLUMN_INDEX))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    public class SubjectsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, SwipeToDeleteable {

        @BindView(R.id.list_item_subject_text) TextView titleView;
        @BindView(R.id.list_item_subject_icon_text) TextView iconView;
        @BindView(R.id.list_item_adding_exam_linear) LinearLayout linearLayout;


        public SubjectsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            linearLayout.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.itemClicked(v, getAdapterPosition());
            }
        }


        @Override
        public ViewGroup getSwipedLayout() {
            return linearLayout;
        }

        @Override
        public void clearView() {
            linearLayout.setX(0);
            linearLayout.setTranslationX(0);
        }

        @Override
        public void delete(int position) {
            removeFromDbAndChangeCursor(position);
        }

    }

}
