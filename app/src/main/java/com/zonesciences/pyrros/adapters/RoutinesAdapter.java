package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.fragment.Routine.RoutineSelectedListener;
import com.zonesciences.pyrros.models.Routine;
import com.zonesciences.pyrros.models.Workout;

import java.util.List;

/**
 * Created by peter on 28/12/2016.
 */

public class RoutinesAdapter extends RecyclerView.Adapter<RoutinesAdapter.ViewHolder> {

    private static final String TAG = "RoutinesAdapter";

    Context mContext;
    List<Routine> mRoutines;

    // Listener
    RoutineSelectedListener mRoutineSelectedListener;

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout routineContainer;
        TextView routineName;
        LinearLayout workoutsContainer;

        public ViewHolder(View itemView) {
            super(itemView);

            routineContainer = (LinearLayout) itemView.findViewById(R.id.item_view_routines);
            routineName = (TextView) itemView.findViewById(R.id.view_routines_title_textview);
            workoutsContainer = (LinearLayout) itemView.findViewById(R.id.linear_layout_routine_view_workouts_container);

            routineContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Routine clicked: " + mRoutines.get(getAdapterPosition()).getName());
                    mRoutineSelectedListener.onRoutineSelected(mRoutines.get(getAdapterPosition()));
                }
            });
        }
    }

    public RoutinesAdapter(Context context, List<Routine> routines, RoutineSelectedListener listener){
        this.mContext = context;
        this.mRoutines = routines;
        this.mRoutineSelectedListener = listener;
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
        holder.workoutsContainer.removeAllViews();
        for (Workout w : mRoutines.get(position).getWorkoutsList()){
            TextView workoutName = new TextView(mContext);
            workoutName.setText(w.getName());
            holder.workoutsContainer.addView(workoutName);
        }
    }

    @Override
    public int getItemCount() {
        return mRoutines.size();
    }

}
