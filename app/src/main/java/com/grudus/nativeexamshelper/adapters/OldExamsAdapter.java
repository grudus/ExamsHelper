package com.grudus.nativeexamshelper.adapters;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.database.subjects.SubjectEntry;
import com.grudus.nativeexamshelper.helpers.normal.AnimationHelper;
import com.grudus.nativeexamshelper.helpers.normal.ColorHelper;
import com.grudus.nativeexamshelper.pojos.Subject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OldExamsAdapter extends RecyclerView.Adapter<OldExamsAdapter.OldExamsViewHolder> {

    public static final int HEADER_POSITION = 0;
    public static final int ANIMATION_DURATION = AnimationHelper.DEFAULT_ANIMATION_DURATION / 2;
    private static final String TAG = "@@@" + OldExamsAdapter.class.getSimpleName();

    private int selectedItemBackgroundColor;
    private int normalItemBackgroundColor;

    private Cursor cursor;
    private final ExamsDbHelper dbHelper;
    private ItemClickListener listener;

    private int headers;
    private int cursorSize = 0;
    private int accentColor;

    public OldExamsAdapter(Context context, Cursor cursor) {
        this.dbHelper = ExamsDbHelper.getInstance(context);
        this.cursor = cursor;
        headers = 1;
        cursorSize = cursor.getCount() + headers;
        accentColor = ColorHelper.getThemeColor(context, R.attr.colorAccent);
        setUpBackgroundColors(context);
    }

    private void setUpBackgroundColors(Context context) {
        selectedItemBackgroundColor = ColorHelper.getThemeColor(context, R.attr.selectedListItemBackgroundColor);
        normalItemBackgroundColor = ColorHelper.getThemeColor(context, R.attr.background);
    }

    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public OldExamsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        dbHelper.openDBReadOnly();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_old_exam, parent, false);
        return new OldExamsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OldExamsViewHolder holder, int position) {
        if (position == HEADER_POSITION) {
            setBackgroundColor(holder, ColorHelper.getHexColor(accentColor));
            return; //header - already defined in xml
        }
        position = position - getHeaderCount();
        cursor.moveToPosition(position);

        final Long subjectId = cursor.getLong(SubjectEntry.INDEX_COLUMN_INDEX);

        findSubjectAndBindView(holder, subjectId);

    }

    private void findSubjectAndBindView(OldExamsViewHolder holder, Long subjectId) {
        dbHelper.findSubjectById(subjectId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subjectObject -> {
                    if (subjectObject != null) {
                        setBackgroundColor(holder, subjectObject.getColor());

                        bindIconView(holder, subjectObject.getTitle());
                        bindSubjectView(holder, subjectObject.getTitle());
                    }
                }, error -> Log.e(TAG, "findSubjectAndBindView: EE", error));
    }

    private void setBackgroundColor(OldExamsViewHolder holder, String color) {
        GradientDrawable bg = (GradientDrawable) holder.iconView.getBackground();
        bg.setColor(Color.parseColor(color));
        holder.iconView.setBackground(bg);
    }

    private void bindIconView(OldExamsViewHolder holder, String subjectTitle) {
        holder.iconView.setText(subjectTitle.substring(0, 1).toUpperCase());
    }

    private void bindSubjectView(OldExamsViewHolder holder, String subjectTitle) {
        holder.subjectView.setText(subjectTitle);
    }


    public Subject getSubjectAtPosition(int adapterPosition) {
        cursor.moveToPosition(adapterPosition - getHeaderCount());
        String subjectTitle = cursor.getString(SubjectEntry.TITLE_COLUMN_INDEX);
        String color = cursor.getString(SubjectEntry.COLOR_COLUMN_INDEX);
        Long id = cursor.getLong(SubjectEntry.INDEX_COLUMN_INDEX);

        return new Subject(id, subjectTitle, color);
    }

    public void changeCursor(Cursor _new) {
        cursor.close();
        cursor = _new;
        cursorSize = cursor.getCount() + headers;
    }

    public void closeDatabase() {
        cursor.close();
        dbHelper.closeDB();
        cursorSize = headers;
    }

    public int getHeaderCount() {
        return headers;
    }

    @Override
    public int getItemCount() {
        return cursorSize;
    }


    private void deleteAllSubjectExams(int adapterPosition) {
        final Subject subject = getSubjectAtPosition(adapterPosition);

        dbHelper.removeAllOldSubjectExams(subject)
                .flatMap(howMany -> dbHelper.setSubjectHasGrade(subject, false))
                .flatMap(howMany -> dbHelper.getSubjectsWithGrade())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::changeCursor,
                        onError -> {
                        },
                        () -> notifyItemRemoved(adapterPosition));

    }

    public class OldExamsViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        @BindView(R.id.list_item_old_exam_subject)
        TextView subjectView;
        @BindView(R.id.list_item_old_exam_icon_text)
        TextView iconView;
        @BindView(R.id.list_item_image_under_icon)
        ImageView binIcon;

        private boolean deleteMode = false;

        public OldExamsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @OnClick(R.id.list_item_image_under_icon)
        public void deleteExams() {
            deleteAllSubjectExams(getAdapterPosition());
        }

        @Override
        public void onClick(View v) {
            if (!deleteMode)
                listener.itemClicked(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (getAdapterPosition() == HEADER_POSITION)
                return false;
            if (deleteMode) {
                deleteMode = false;
                itemView.setBackgroundColor(normalItemBackgroundColor);
                AnimationHelper.rotateToDisposeBinIcon(iconView, binIcon, ANIMATION_DURATION).start();
            } else {
                deleteMode = true;
                itemView.setBackgroundColor(selectedItemBackgroundColor);
                AnimationHelper.rotateToReceiveBinIcon(iconView, binIcon, ANIMATION_DURATION).start();
            }
            return true;
        }
    }
}
