package com.grudus.nativeexamshelper.pojos.grades;


public class GradeException extends RuntimeException {
    public GradeException() {
        super("Grade isn't in range " + Grades.getCurrentGrade().toString());
    }

    public GradeException(String detailMessage) {
        super(detailMessage);
    }

    public GradeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public GradeException(Throwable throwable) {
        super(throwable);
    }
}
