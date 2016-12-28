package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Routine;

import java.util.List;

/**
 * Created by peter on 28/12/2016.
 */

public class RoutinesAdapter extends RecyclerView.Adapter<RoutinesAdapter.ViewHolder> {

    Context mContext;
    List<Routine> mRoutines;

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView routineName;

        public ViewHolder(View itemView) {
            super(itemView);

            routineName = (TextView) itemView.findViewById(R.id.view_routines_title_textview);
        }
    }

    public RoutinesAdapter(Context context, List<Routine> routines){
        this.mContext = context;
        this.mRoutines = routines;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_routines, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String routineName;
        if (mRoutines.get(position).getName() == null) {
            routineName = "New Routine";
        } else {
            routineName = mRoutines.get(position).getName();
        };
        holder.routineName.setText(routineName);
    }

    @Override
    public int getItemCount() {
        return mRoutines.size();
    }
}
