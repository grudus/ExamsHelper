package com.grudus.nativeexamshelper.pojos;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.grudus.nativeexamshelper.helpers.exceptions.ExceptionsHelper;

public class Subject implements Parcelable {

    private static final String TAG = "@@@@" + Subject.class.getSimpleName();

    private String title;
    private String color;
    private Long id;

    private static final String EMPTY = "grudus_sub_empty";

    public Subject(@NonNull Long id, @NonNull String title, @NonNull String color) {
        if (ExceptionsHelper.stringsAreEmpty(title, color)) {
            setDefaultValues();
            return;
        }
        this.id = id;
        this.title = title;
        this.color = color;
   }

    private Subject(String title, String color) {
        this.id = -1L;
        this.title = title;
        this.color = color;
    }

    private Subject() {
        setDefaultValues();
    }

    public static Subject subjectWithoutId(String title, String color) {
        return new Subject(title, color);
    }

    private void setDefaultValues() {
        this.id = -1L;
        this.color = "#4286F5";
        this.title = EMPTY;
    }

    public String getTitle() {
        return title;
    }

    public static Subject empty() {
        return new Subject();
    }

    public boolean isEmpty() {
        return this.title.equals(EMPTY);
    }

    public void setTitle(@NonNull String title) {
        if (ExceptionsHelper.stringsAreEmpty(title)) return;
        this.title = title;
    }

    public Subject copy() {
        return new Subject(id, title, color);
    }


    public String getColor() {
        return color;
    }

    public void setColor(@NonNull String color) {
        if (ExceptionsHelper.stringsAreEmpty(color)) return;
        this.color = color;
    }

    @Override
    public String toString() {
        return "Subject {" +
                "title='" + title + '\'' +
                ", color='" + color + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;
        Subject that = (Subject) object;
        return that.getId().equals(id);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    //    Parcelable stuff


    public Subject(Parcel parcel) {
        String[] data = new String[3];
        parcel.readStringArray(data);
        this.id = Long.parseLong(data[0]);
        this.title = data[1];
        this.color = data[2];

        Log.d(TAG, "Subject: parcel " + this.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {id.toString(), title, color});
    }


    public static final Creator<Subject> CREATOR = new Creator<Subject>() {
        @Override
        public Subject createFromParcel(Parcel in) {
            return new Subject(in);
        }

        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

    public Long getId() {
        return id;
    }
}
