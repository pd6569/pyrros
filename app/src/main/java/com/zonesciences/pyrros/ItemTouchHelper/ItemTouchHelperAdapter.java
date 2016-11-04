package com.zonesciences.pyrros.ItemTouchHelper;

/**
 * Created by Peter on 03/11/2016.
 */
public interface ItemTouchHelperAdapter {

    boolean onItemMove (int fromPosition, int toPosition);

    void onItemDismiss(int position);

    void onMoveCompleted();
}
