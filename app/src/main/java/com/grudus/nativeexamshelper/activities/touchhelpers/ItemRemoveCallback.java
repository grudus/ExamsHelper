package com.grudus.nativeexamshelper.activities.touchhelpers;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.grudus.nativeexamshelper.adapters.SubjectsAdapter;

public class ItemRemoveCallback extends ItemTouchHelper.SimpleCallback {

    private ViewGroup swipedLayout;

    public ItemRemoveCallback(int dragDirs, int swipeDirs, Class<? extends SwipeToDeleteable> type) {
        super(dragDirs, swipeDirs);
    }


    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            swipedLayout = ((SwipeToDeleteable) viewHolder).getSwipedLayout();
            swipedLayout.setTranslationX(dX);
        }

        else super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        ((SwipeToDeleteable) viewHolder).clearView();
        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();

        ((SwipeToDeleteable)viewHolder).delete(pos);
    }


}
