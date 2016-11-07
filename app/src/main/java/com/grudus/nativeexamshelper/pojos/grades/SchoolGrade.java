package com.grudus.nativeexamshelper.pojos.grades;

public class SchoolGrade extends Grade {

    private static final double[] GRADES = {1, 1.25, 1.75, 2, 2.25, 2.75, 3, 3.25, 3.75, 4, 4.25, 4.75, 5, 5.25, 5.75, 6};


    @Override
    public double getFirstPassedGrade() {
        return GRADES[2];
    }

    @Override
    public double[] getGrades() {
        return GRADES;
    }

    @Override
    public String gradeToString(double grade) {
        int digits = (int) ((grade * 100) % 100);
        int number = (int) (grade + 0.5);

        switch (digits) {
            case 0:
                return Integer.toString(number);
            case 25:
                return number + "+";
            case 75:
                return number + "-";
            default:
                throw new IllegalArgumentException("Grade " + grade + " is incorrect");
        }

    }

    @Override
    public double findGradeFromString(String grade) {
        if (!isValid(grade))
            throw new IllegalArgumentException("Grade " + grade + " isn't valid");
        if (isNatural(grade))
            return Double.parseDouble(grade);

        final char sign = grade.charAt(grade.length() - 1);

        if (sign == '+')
            return Double.parseDouble(String.valueOf(grade.charAt(0))) + 0.25;
        else return Double.parseDouble(String.valueOf(grade.charAt(0))) - 0.25;
    }

    private boolean isValid(String grade) {
        return true; //todo find regex
    }

    private boolean isNatural(String grade) {
        return grade.matches("\\d+");
    }

    @Override
    public String[] getGradesAsString() {
        String[] grades = new String[getGrades().length];
        for (int i = 0; i < getGrades().length; i++) {
            grades[i] = (int) (getGrades()[i] + 0.5)
                    + (getGrades()[i] % 1 == 0.25 ? "+" : (getGrades()[i] % 1 == 0.75 ? "-" : ""));
        }
        return grades;
    }
}
