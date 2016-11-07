package com.grudus.nativeexamshelper.layouts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.grudus.nativeexamshelper.R;

public class ColorPickerSeekBars extends LinearLayout {

    private static final int SEEK_BARS_COUNT = 3;

    private final int[] seekBarThumbColors =
            {R.color.seekBarRed, R.color.seekBarGreen, R.color.seekBarBlue};
    private final int[] seekBarProgressDrawables =
            {R.drawable.red_seekbar, R.drawable.green_seekbar, R.drawable.blue_seekbar};


    private final Context context;
    private final SeekBar[] seekBars = new SeekBar[SEEK_BARS_COUNT];

    private final TextView[] seekBarTextViews = new TextView[SEEK_BARS_COUNT];

    private final int[] colorProgress = new int[SEEK_BARS_COUNT];

    private ProgressListener listener;

    public ColorPickerSeekBars(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }


    public ColorPickerSeekBars(Context context) {
        this (context, null);
    }

    public ColorPickerSeekBars(Context context, AttributeSet attributeSet, int flags) {
        super(context, attributeSet, flags);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);

        initViews();
        setUpProgressListeners();
        initOnGlobalLayoutListener();
    }


    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(context);

        for (int i = 0; i < SEEK_BARS_COUNT; i ++) {
            ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.seek_bar_with_progress_text, null);
            seekBars[i] = (SeekBar) viewGroup.getChildAt(1);
            seekBarTextViews[i] = (TextView) viewGroup.getChildAt(0);

            seekBars[i].setProgressDrawable(
                    ContextCompat.getDrawable(context, seekBarProgressDrawables[i]));
            seekBars[i].getThumb().setColorFilter(
                    ContextCompat.getColor(context, seekBarThumbColors[i]), PorterDuff.Mode.SRC_IN);

            this.addView(viewGroup);
        }
    }

    private void setUpProgressListeners() {
        for (int i = 0; i < SEEK_BARS_COUNT; i++) {
            final int temp = i;
            seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    colorProgress[temp] = progress;
                    seekBarTextViews[temp].setText(progress < 10 ?  "  " + progress
                            : (progress < 100 ? " " + progress : "" + progress));

                    changeTextViewPosition(temp);

                    listener.onProgressChange(progress, getColor());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}

            });
        }
    }

    private void initOnGlobalLayoutListener() {
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ColorPickerSeekBars.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                changeAllSeekBarProgresses();
                changeAllTextViewPositions();
            }
        });
    }


    private void changeTextViewPosition(int index) {
        Rect progressThumbRect = seekBars[index].getThumb().getBounds();
        seekBarTextViews[index].setX(progressThumbRect.left);
    }

    public int getColor() {
        return Color.rgb(colorProgress[0], colorProgress[1], colorProgress[2]);
    }

    public void setColor(String hexColor) {

        final int color = Color.parseColor(hexColor);
        colorProgress[0] = Color.red(color);
        colorProgress[1] = Color.green(color);
        colorProgress[2] = Color.blue(color);

        changeAllSeekBarProgresses();
        changeAllTextViewPositions();
    }

    private void changeAllSeekBarProgresses() {
        for (int i = 0; i < SEEK_BARS_COUNT; i++) {
            seekBars[i].setProgress(colorProgress[i]);
        }
    }

    private void changeAllTextViewPositions() {
        for (int i = 0; i < SEEK_BARS_COUNT; i++) {
            changeTextViewPosition(i);
            seekBarTextViews[i].setText(String.valueOf(colorProgress[i]));
        }
    }


    public ColorPickerSeekBars addListener(ProgressListener progressListener) {
        this.listener = progressListener;
        return this;
    }

    public interface ProgressListener {
        void onProgressChange(int progress, int color);
    }

}
