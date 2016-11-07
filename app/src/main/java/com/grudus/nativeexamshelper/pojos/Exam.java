package com.grudus.nativeexamshelper.pojos;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.grudus.nativeexamshelper.helpers.normal.DateHelper;
import com.grudus.nativeexamshelper.pojos.grades.GradeException;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import java.util.Date;

public class Exam implements Parcelable {

    private Long id;
    private Long subjectId;
    private String info;
    private Date date;
    private double grade;


    /**
     *
     *  Only for creating new exam in {@link com.grudus.nativeexamshelper.activities.AddExamActivity}
     *
     */
    private Exam(@NonNull Long subjectId, String info, @NonNull Date date) {
        this.subjectId = subjectId;
        this.info = info;
        this.date = date;
    }

    public Exam(@NonNull Long id, @NonNull Long subjectId, String info, @NonNull Date date) {
        this(id, subjectId, info, date, Grades.EMPTY);
    }

    public Exam(@NonNull Long id, @NonNull Long subjectId, String info, @NonNull Date date, double grade) {
        if (!Grades.isInRange(grade))
            throw new GradeException(Double.toString(grade) + " isn't in range");
        this.id = id;
        this.grade = grade;
        this.subjectId = subjectId;
        this.info = info;
        this.date = date;
    }

    public static Exam getExamWithoutId(Long subjectId, String info, Date date) {
        return new Exam(subjectId, info, date);
    }

    @NonNull
    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(@NonNull Long subjectId) {
        this.subjectId = subjectId;
    }

    @Nullable
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        if (!Grades.isInRange(grade)) throw new GradeException();
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return  "SubjectId: (" + subjectId + ") exam from " + info + " on "
                + DateHelper.getStringFromDate(date) + " - " + grade;
    }


//  Parcelable stuff

    public Exam(Parcel parcel) {
        String[] data = new String[5];
        parcel.readStringArray(data);
        this.id = Long.parseLong(data[0]);
        this.subjectId = Long.parseLong(data[1]);
        this.info = data[2];
        this.date = DateHelper.tryToGetDateFromString(data[3]);
        this.grade = Double.parseDouble(data[4]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                id.toString(),
                subjectId.toString(),
                info,
                DateHelper.getStringFromDate(date),
                Double.toString(grade)
        });
    }

    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new Exam(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new Exam[size];
        }
    };
}
