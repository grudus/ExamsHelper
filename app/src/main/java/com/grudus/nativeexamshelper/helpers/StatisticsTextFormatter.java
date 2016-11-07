package com.grudus.nativeexamshelper.helpers;


import java.text.DecimalFormat;

public class StatisticsTextFormatter {

    private static String decimalFormat = "0.00";
    private static GradeStatisticsCalculator calculator;
    private static DecimalFormat formatter = new DecimalFormat(decimalFormat);
    private static String noDominantText;

    public static void setFormatter(String format) {
        StatisticsTextFormatter.decimalFormat = format;
        formatter = new DecimalFormat(format);
    }

    public static void setCalculator(GradeStatisticsCalculator gradeStatisticsCalculator) {
        calculator = gradeStatisticsCalculator;
    }

    public static void setNoDominantText(String text) {
        noDominantText = text;
    }

    public static String getAverage() {
        return " " + formatter.format(calculator.getAverage());
    }

    public static String getMedian() {
        return " " + formatter.format(calculator.getMedian());
    }

    public static String getDominant() {
        if (calculator.getDominants() == null) return " " + noDominantText;
        String array = calculator.getDominants().toString();
        // without '[' and ']'
        return " " + array.substring(1, array.length() - 1);
    }

    public static String getPassedExams() {
        return " " + calculator.getNumberOfPassedExams();
    }

    public static String getFailedExams() {
        return " " + calculator.getNumberOfFailedExams();
    }
    
    public static String getPercentOfPassedExams() {
        return " " + formatter.format(100 * calculator.getPercentOfPassedExams()) + "%";
    }

}

