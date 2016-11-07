package com.grudus.nativeexamshelper.charts;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BaseDataSet;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.helpers.normal.ColorHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public abstract class ChartGenerator {

    protected static final String TAG = "@@@!@@@" + ChartGenerator.class.getSimpleName();

    private final Context context;

    protected final int textColor;
    protected final int accentColor;

    public ChartGenerator(Context context) {
        this.context = context;
        textColor = ColorHelper.getThemeColor(context, android.R.attr.textColor);
        accentColor = ColorHelper.getThemeColor(context, R.attr.colorAccent);
    }

    protected Context getContext() {
        return context;
    }


    /**
     * changes natural month's order (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
     * to school's order (8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7)
     *
     */
    protected int findSchoolMonth(int calendarMonth) {
        if (calendarMonth < 1 || calendarMonth > 12)
            throw new IllegalArgumentException("Month has to be in range [1,12]: get " + calendarMonth );

        int schoolMonth;

        if (calendarMonth < 8)
            schoolMonth = calendarMonth + 5;
        else
            schoolMonth = calendarMonth - 7;

        return schoolMonth;
    }



    protected void printCursor(Cursor cursor) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            builder.append(cursor.getString(i))
                    .append(", ");
        }
        Log.d(TAG, "printCursor: " + builder.toString());
    }

    protected Observable<LineChart> calculateAverage(Observable<Cursor> cursorData, @Nullable String description, LookAndFeel<LineChart, LineDataSet> lookAndFeel) {

        return cursorData
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(cursor -> {
                    List<Entry> entries = new ArrayList<>(cursor.getCount());
                    LineChart chart = new LineChart(getContext());
                    if (cursor.moveToFirst()) {
                        do {
                            printCursor(cursor);
                            int calendarMonth = Integer.parseInt(cursor.getString(1));
                            int schoolMonth = findSchoolMonth(calendarMonth);
                            float avg = cursor.getFloat(2);
                            entries.add(new Entry(schoolMonth, avg));
                        } while (cursor.moveToNext());

                        cursor.close();
                        setUpLineChart(chart, entries, lookAndFeel);
                        setUpDescription(chart, description);
                    }

                    return Observable.create(subscriber -> {
                        subscriber.onNext(chart);
                        subscriber.onCompleted();
                    });
                });
    }

    public Observable<BarChart> calculateExamFrequency(Observable<Cursor> cursorData, @Nullable String description, LookAndFeel<BarChart, BarDataSet> lookAndFeel) {

        return cursorData
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(cursor -> {

                    List<BarEntry> entries = new ArrayList<>();
                    BarChart barChart = new BarChart(getContext());
                    if (cursor.moveToFirst()) {
                        do {
                            int calendarMonth = Integer.parseInt(cursor.getString(1));
                            int schoolMonth = findSchoolMonth(calendarMonth);
                            int numberOfExams = Integer.parseInt(cursor.getString(2));
                            entries.add(new BarEntry(schoolMonth, numberOfExams));
                        } while (cursor.moveToNext());
                        setUpBarChart(barChart, entries, lookAndFeel);
                        setUpDescription(barChart, description);
                    }


                    cursor.close();

                    return Observable.create(subscriber -> {
                        subscriber.onNext(barChart);
                        subscriber.onCompleted();
                    });
                });
    }

    private void setUpBarChart(BarChart barChart, List<BarEntry> entries, LookAndFeel<BarChart, BarDataSet> lookAndFeel) {
        BarDataSet dataSet = new BarDataSet(entries, null);
        dataSet.setColor(accentColor);

        dataSet.setStackLabels(getContext().getResources().getStringArray(R.array.school_months));

        BarData barData = new BarData(dataSet);

        barChart.setData(barData);
        barChart.setDrawValueAboveBar(false);

        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> Integer.toString((int)value));

        barChart.getViewPortHandler().setMaximumScaleX(1.5f);

        setXAxis(barChart, entries, dataSet.getStackLabels());
        setYAxis(barChart, dataSet);

        if (lookAndFeel != null)
            lookAndFeel.customize(barChart, dataSet);
    }


    private void setUpDescription(BarLineChartBase barChart, String text) {
        Description description = new Description();
        description.setText(text);
        description.setEnabled(false);
        barChart.setDescription(description);
        barChart.getLegend().setEnabled(false);
    }

    private void setYAxis(BarLineChartBase chart, DataSet<? extends Entry> dataSet) {
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawZeroLine(true);
        yAxis.setTextColor(textColor);

        List<? extends Entry> list = dataSet.getValues();
        Collections.sort(list, (lhs, rhs) -> Float.compare(lhs.getY(), rhs.getY()));
        yAxis.setLabelCount(((int)list.get(list.size() - 1).getY()) % 9);


        chart.getAxisRight().setEnabled(false);
        dataSet.setValueTextColor(textColor);
    }

    private void setXAxis(BarLineChartBase chart, List<? extends Entry> entries, String[] labels) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return labels[(int)value-1];
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

        float min, max;
        min = max = entries.get(0).getX();
        for (Entry one : entries) {
            float val = one.getX();
            if (val < min)
                min = val;
            else if (val > max)
                max = val;
        }

        xAxis.setAxisMinimum(Math.max(min-1, 1));
        xAxis.setAxisMaximum(Math.min(max+1, 12));
        xAxis.setLabelCount(Math.min((int)(max - min) + 2, 11));
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(textColor);
    }

    public void setUpLineChart(LineChart chart, List<Entry> entries, LookAndFeel<LineChart, LineDataSet> lookAndFeel) {
        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setColor(accentColor);
        dataSet.setCircleColor(accentColor);

        LineData barData = new LineData(dataSet);

        chart.setData(barData);

        setXAxis(chart, entries, getContext().getResources().getStringArray(R.array.school_months));
        setYAxis(chart, dataSet);

        if (lookAndFeel != null)
            lookAndFeel.customize(chart, dataSet);
    }


    public interface LookAndFeel<T extends Chart, U extends BaseDataSet> {
        void customize(T chart, U dataSet);
    }





}
