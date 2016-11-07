package com.grudus.nativeexamshelper.pojos;

import java.util.Date;


public class JsonUser {
    private String username;
    private Long id;
    private String email;
    private boolean enabled;
    private Date date;
    private boolean empty;
    private long lastModified;

    public JsonUser() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }


    @Override
    public String toString() {
        return "JsonUser{" +
                "username='" + username + '\'' +
                ", id=" + id +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                ", date=" + date +
                ", empty=" + empty +
                ", lastModified " + lastModified +
                '}';
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
