package com.zonesciences.pyrros.ItemTouchHelper;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Peter on 05/12/2016.
 */
public interface OnDragListener {

    void onStartDrag(RecyclerView.ViewHolder viewHolder);
    void onStopDrag();
}
