package com.zonesciences.pyrros.ActionMode;

import android.util.Log;
import android.util.SparseBooleanArray;

/**
 * Created by Peter on 09/12/2016.
 */
public interface ActionModeAdapterInterface {

    void toggleSelection(int position);

    void removeSelection();

    void selectItem(int position, boolean isSelected);

    int getSelectedCount();

    SparseBooleanArray getSelectedItemIds();

    void clearSelectedItems();

    void deleteSelectedItems();

}
