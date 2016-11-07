package com.grudus.nativeexamshelper.charts;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.pojos.Subject;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import rx.Observable;

public class SubjectChartGenerator extends ChartGenerator {

    private final Subject subject;

    public SubjectChartGenerator(Context context, Subject subject) {
        super(context);
        this.subject = subject;
    }

    public Observable<LineChart> calculateAverage() {
        ExamsDbHelper helper = ExamsDbHelper.getInstance(getContext());
        helper.openDBReadOnly();

        Observable<Cursor> monthlyExamsGrades;

        if (Grades.areDecimalsInGradesEnabled())
            monthlyExamsGrades = helper.getMonthlyExamsGrades(subject.getId());
        else
            monthlyExamsGrades = helper.getMonthlyRoundedExamsGrades(subject.getId());

        return super.calculateAverage(monthlyExamsGrades, getContext().getString(R.string.chart_avg_desc) ,
                (chart, dataSet) -> {
                    dataSet.setColor(Color.parseColor(subject.getColor()));
                    dataSet.setCircleColor(Color.parseColor(subject.getColor()));

                    dataSet.disableDashedLine();
                });
    }


    public Observable<BarChart> calculateCountOfExamsPerMonth() {
        ExamsDbHelper helper = ExamsDbHelper.getInstance(getContext());
        helper.openDBReadOnly();

        return super.calculateExamFrequency(helper.getCountOfOldExamsPerMonth(subject.getId()), getContext().getString(R.string.chart_freq_desc),
                (chart, dataSet) -> {
                    dataSet.setColor(Color.parseColor(subject.getColor()));
                });
    }
}
