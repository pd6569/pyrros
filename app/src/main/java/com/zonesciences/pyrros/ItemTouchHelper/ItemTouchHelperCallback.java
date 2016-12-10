package com.zonesciences.pyrros.ItemTouchHelper;

import android.content.ClipData;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

/**
 * Created by Peter on 03/11/2016.
 */
public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private static final String TAG = "ItemTouchHelperCallback";

    private final ItemTouchHelperAdapter mAdapter;

    private boolean moveEnabled;
    private boolean swipeEnabled;

    public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter){
        mAdapter = adapter;
        moveEnabled = true;
        swipeEnabled = true;
    }

    public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter, boolean moveEnabled, boolean swipeEnabled){
        mAdapter = adapter;
        this.moveEnabled = moveEnabled;
        this.swipeEnabled = swipeEnabled;
    }

    @Override
    public boolean isLongPressDragEnabled(){
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled(){
        return swipeEnabled;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);

    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (moveEnabled) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            Log.i(TAG, "adapter position: " + viewHolder.getAdapterPosition());
        }
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (swipeEnabled) {
            Log.i(TAG, "onSwiped");
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }

    @Override
    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y){
        Log.i(TAG, "onMoved");
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        Log.i(TAG, "clearView");
        if (moveEnabled) {
            mAdapter.onMoveCompleted();
        }
        if (viewHolder instanceof ItemTouchHelperViewHolder){
            ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemClear();
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState){
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if(viewHolder instanceof ItemTouchHelperViewHolder){
                ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                itemViewHolder.onSelected();
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }
}
