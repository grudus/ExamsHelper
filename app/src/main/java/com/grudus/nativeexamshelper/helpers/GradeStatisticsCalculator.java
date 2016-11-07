package com.grudus.nativeexamshelper.helpers;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.pojos.grades.Grade;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class GradeStatisticsCalculator {
    private final double MINIMAL_GRADE_TO_PASS;

    private Map<Double, Integer> gradesOccurrences;
    private int maxGradeOccurrences = 0;
    private ArrayList<Double> dominants;
    private double median, sum = 0;
    private int passed = 0;
    private int numberOfAllGrades = -1, actualNumberOfGrades = 0;
    private int numberOfOccurrencesOfSingleGrade = 0;
    private double previousGrade = -1;
    private double tempGrade;


    private final Context context;
    private final Long subjectId;


    public GradeStatisticsCalculator(Context context, Long subjectId, double minimalGradeToPass) {
        this.subjectId = subjectId;
        this.context = context;
        gradesOccurrences = new HashMap<>();
        dominants = new ArrayList<>();
        MINIMAL_GRADE_TO_PASS = minimalGradeToPass;
    }

    public Observable<Double> startCalculating() {

        ExamsDbHelper db = ExamsDbHelper.getInstance(context);
        return db.getGradesFromOrderedSubjectGrades(subjectId);
    }

    public void calculate(double oneGrade) {

        if (numberOfAllGrades == -1) {
            numberOfAllGrades = (int)oneGrade;
            return;
        }

        double tempGrade = Grades.areDecimalsInGradesEnabled() ? oneGrade : (int)(oneGrade + 0.5);

        calculateMedianIfReachable(tempGrade);

        actualNumberOfGrades++;
        this.tempGrade = Grades.areDecimalsInGradesEnabled() ? tempGrade : (int)(tempGrade + 0.5);


        if (previousGrade == -1) previousGrade = tempGrade;

        if (tempGrade > previousGrade) {
            gradesOccurrences.put(previousGrade, numberOfOccurrencesOfSingleGrade);
            previousGrade = tempGrade;
            checkIfMaxOccurrencesWasReached(numberOfOccurrencesOfSingleGrade);
            numberOfOccurrencesOfSingleGrade = 1;
        } else numberOfOccurrencesOfSingleGrade++;

        sum += tempGrade;


        if (tempGrade > MINIMAL_GRADE_TO_PASS) {
            passed++;
        }
    }

    private void calculateMedianIfReachable(double tempGrade) {
        if (actualNumberOfGrades == numberOfAllGrades / 2) {
            if (numberOfAllGrades % 2 == 0) {
                calculateMedian(tempGrade, this.tempGrade);
            }
            else {
                calculateMedian(tempGrade, Grade.EMPTY_GRADE);
            }
        }
    }

    public void onCompleteCalculations() {
        gradesOccurrences.put(tempGrade, numberOfOccurrencesOfSingleGrade);
        checkIfMaxOccurrencesWasReached(numberOfOccurrencesOfSingleGrade);
        calculateDominants();
    }

    public void onError(Throwable throwable) {throwable.printStackTrace();}


    private void checkIfMaxOccurrencesWasReached(int numberOfOccurrencesOfSingleGrade) {
        if (numberOfOccurrencesOfSingleGrade > maxGradeOccurrences)
            maxGradeOccurrences = numberOfOccurrencesOfSingleGrade;
    }


    private void calculateMedian(double middleGrade, double middleMinusOneGrade) {
        median = (middleMinusOneGrade == Grade.EMPTY_GRADE)
                ? middleGrade
                : (middleGrade + middleMinusOneGrade) / 2;
    }


    public double getAverage() {
        return  sum / numberOfAllGrades;
    }


    public double getMedian() {
        return median;
    }

    @Nullable
    public ArrayList<Double> getDominants() {
        return dominants.size() == gradesOccurrences.size() ? null : dominants;
    }

    private void calculateDominants() {
        Log.d("@@@", "calculateDominants: " + gradesOccurrences.toString());
        for (Double d : gradesOccurrences.keySet()) {
            if (gradesOccurrences.get(d) == maxGradeOccurrences)
                dominants.add(d);
        }
    }


    public int getNumberOfPassedExams() {
        return passed;
    }

    public int getNumberOfFailedExams() {
        return numberOfAllGrades - passed;
    }

    public double getPercentOfPassedExams() {
        return ((double) passed) / numberOfAllGrades;
    }
    
    

}
