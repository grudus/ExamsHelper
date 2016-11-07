package com.grudus.nativeexamshelper.pojos.grades;


public class UniversityGrade extends Grade {

    private static final double[] GRADES = {2, 3, 3.5, 4, 4.5, 5, 5.5};

    @Override
    public double getFirstPassedGrade() {
        return GRADES[1];
    }

    @Override
    public double[] getGrades() {
        return GRADES;
    }

    @Override
    public String gradeToString(double grade) {
        return Double.toString(grade);
    }

    @Override
    public double findGradeFromString(String grade) {
        return Double.parseDouble(grade);
    }
}
