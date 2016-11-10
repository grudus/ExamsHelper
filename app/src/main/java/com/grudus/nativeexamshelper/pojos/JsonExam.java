package com.grudus.nativeexamshelper.pojos;


import com.google.gson.Gson;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import java.util.Calendar;
import java.util.Date;

public class JsonExam {
    private Long id;
    private Long subjectAndroidId;
    private Long userId;
    private String examInfo;
    private Date date;
    private Double grade;

    private Long lastModified;
    private boolean deleted;

    public JsonExam(Long id, Long subjectAndroidId, Long userId, String examInfo, Date date, Double grade, Long lastModified, boolean deleted) {
        this.id = id;
        this.subjectAndroidId = subjectAndroidId;
        this.userId = userId;
        this.examInfo = examInfo;
        this.date = date;
        this.grade = grade;
        this.lastModified = lastModified;
        this.deleted = deleted;
    }

    public JsonExam() {
        this(-1L, -1L, -1L, "", new Date(), Grades.EMPTY, Calendar.getInstance().getTime().getTime(), false);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubjectAndroidId() {
        return subjectAndroidId;
    }

    public void setSubjectAndroidId(Long subjectAndroidId) {
        this.subjectAndroidId = subjectAndroidId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getExamInfo() {
        return examInfo;
    }

    public void setExamInfo(String examInfo) {
        this.examInfo = examInfo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getGrade() {
        return grade;
    }


    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
