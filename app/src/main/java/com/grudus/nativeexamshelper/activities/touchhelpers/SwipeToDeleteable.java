package com.grudus.nativeexamshelper.activities.touchhelpers;


import android.view.ViewGroup;

public interface SwipeToDeleteable {

    ViewGroup getSwipedLayout();
    void clearView();
    void delete(int position);
}
