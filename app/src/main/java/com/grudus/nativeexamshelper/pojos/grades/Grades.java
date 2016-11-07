package com.grudus.nativeexamshelper.pojos.grades;


import android.content.Context;
import android.preference.PreferenceManager;

import com.grudus.nativeexamshelper.R;

public class Grades {

    public static final double EMPTY = -1D;

    private static Grade currentGrade;

    private static boolean decimalsInGradesEnabled;


    public static void init(Context context) {
        String key = context.getString(R.string.key_grades_type);

        int gradeType;
        try {
            gradeType = PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt(key, 0);

        } catch (ClassCastException e) {
            gradeType = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(key, "0"));
        }

        currentGrade = GradeFactory.getGrade(gradeType);

        handleDecimals(context);
    }

    private static void handleDecimals(Context context) {
        String key = context.getString(R.string.key_grades_decimal);
        if (currentGrade instanceof UniversityGrade)
            decimalsInGradesEnabled = true;

        else
            decimalsInGradesEnabled = !PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(key, true);
    }

    private Grades() {}

    public static Grade getCurrentGrade() {
        return currentGrade;
    }

    public static void setGradeMode(int type, Context context) {
        currentGrade = GradeFactory.getGrade(type);
        handleDecimals(context);
    }

    public static boolean areDecimalsInGradesEnabled() {
        return decimalsInGradesEnabled;
    }

    public static void enableDecimalsInGrades(boolean enable) {
        decimalsInGradesEnabled = enable;
    }

    public static double getFirstPassedGrade() {
        return currentGrade.getFirstPassedGrade();
    }

    public static double[] getAllPossibleGrades() {
        return currentGrade.getGrades();
    }

    public static String gradeToString(double grade) {
        return currentGrade.gradeToString(grade);
    }

    public static double findGradeFromString(String grade) {
        return currentGrade.findGradeFromString(grade);
    }

    public static String[] getAllPossibleGradesAsStrings() {
        return currentGrade.getGradesAsString();
    }

    public static boolean isInRange(double grade) {
        return currentGrade.isInRange(grade) || grade == EMPTY;
    }

}
