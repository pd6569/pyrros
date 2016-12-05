package com.zonesciences.pyrros.ActionMode;

import android.view.View;

/**
 * Created by Peter on 05/12/2016.
 */
public interface RecyclerClickListener {

    void onClick(View view, int position);
    void onLongClick(View view, int position);

}
