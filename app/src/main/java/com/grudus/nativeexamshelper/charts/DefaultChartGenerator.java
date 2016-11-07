package com.grudus.nativeexamshelper.charts;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class DefaultChartGenerator extends ChartGenerator {
    
    public DefaultChartGenerator(Context context) {
        super(context);
    }

    public Observable<PieChart> calculateExamsPerCent() {
        ExamsDbHelper dbHelper = ExamsDbHelper.getInstance(getContext());
        dbHelper.openDBIfClosed();

        return dbHelper.getSubjectsExamsQuantity()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(cursor -> {

                    PieDataSet dataSet = new PieDataSet(null, null);
                    dataSet.resetColors();
                    PieChart pieChart = new PieChart(getContext());

                    if (cursor.moveToFirst()) {
                        do {

                            float val = cursor.getFloat(2);
                            String color = cursor.getString(1);
                            String title = cursor.getString(0);
                            dataSet.addEntry(new PieEntry(val == 1 ? val+0.001f : val, title));
                            dataSet.addColor(Color.parseColor(color));

                        } while (cursor.moveToNext());

                        pieChart.setData(new PieData(dataSet));

                        setUpPieChart(pieChart, getContext().getString(R.string.chart_percent_desc));
                    }

                    cursor.close();

                    return Observable.create(subscriber -> {
                        subscriber.onNext(pieChart);
                        subscriber.onCompleted();
                    });
                });

    }



    private void setUpPieChart(PieChart pieChart, String description) {
        PieData pieData = pieChart.getData();
        PieDataSet dataSet = (PieDataSet) pieData.getDataSet();

        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);

        pieChart.setHoleRadius(0.25f);
        pieChart.setTransparentCircleRadius(0.5f);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawEntryLabels(false);

        Legend legend = pieChart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setTextColor(textColor);

        dataSet.setSliceSpace(0.5f);


        Description desc = new Description();
        desc.setText(description);
        desc.setEnabled(false);
        pieChart.setDescription(desc);

    }


    public Observable<LineChart> calculateAverage() {
        ExamsDbHelper helper = ExamsDbHelper.getInstance(getContext());
        helper.openDBIfClosed();

        Observable<Cursor> monthlyExamsGrades;
        if (Grades.areDecimalsInGradesEnabled())
            monthlyExamsGrades = helper.getMonthlyExamsGrades();
        else
            monthlyExamsGrades = helper.getMonthlyRoundedExamsGrades(-1L);

       return super.calculateAverage(monthlyExamsGrades, getContext().getString(R.string.chart_avg_desc) ,null);
    }

    public Observable<BarChart> calculateCountOfExamsPerMonth() {
        ExamsDbHelper helper = ExamsDbHelper.getInstance(getContext());
        helper.openDBIfClosed();

        return super.calculateExamFrequency(helper.getCountOfOldExamsPerMonth(), getContext().getString(R.string.chart_freq_desc), null);
    }
}
