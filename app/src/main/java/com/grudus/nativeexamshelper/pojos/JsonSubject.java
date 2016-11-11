package com.grudus.nativeexamshelper.pojos;


import com.google.gson.Gson;

public class JsonSubject {
    private Long id;
    private Long userId;
    private String title;
    private String color;
    private boolean deleted;
    private long lastModified;
    private boolean hasGrade;

    public JsonSubject() {
    }

    public JsonSubject(Long id, Long userId, String title, String color, boolean deleted, long modified, boolean hasGrade) {

        this.id = id;
        this.userId = userId;
        this.title = title;
        this.color = color;
        this.deleted = deleted;
        this.lastModified = modified;
        this.hasGrade = hasGrade;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isDeleted() {
        return deleted;
    }


    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getModified() {
        return lastModified;
    }

    public void setModified(long modified) {
        this.lastModified = modified;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public boolean isHasGrade() {
        return hasGrade;
    }

    public void setHasGrade(boolean hasGrade) {
        this.hasGrade = hasGrade;
    }
}
