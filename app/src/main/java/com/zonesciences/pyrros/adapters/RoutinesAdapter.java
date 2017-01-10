package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.fragment.Routine.RoutineSelectedListener;
import com.zonesciences.pyrros.models.Routine;

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

        TextView routineName;
        TextView creatorTextView;
        TextView routineDescriptionTextview;

        // Workout Overview
        TextView numWorkoutsTextview;
        TextView focusTextview;
        TextView levelTextview;
        TextView viewRoutineTextview;

        public ViewHolder(View itemView) {
            super(itemView);

            routineName = (TextView) itemView.findViewById(R.id.view_routines_title_textview);
            creatorTextView = (TextView) itemView.findViewById(R.id.view_routines_subtitle_creator_textview);
            routineDescriptionTextview = (TextView) itemView.findViewById(R.id.view_routines_description);
            numWorkoutsTextview = (TextView) itemView.findViewById(R.id.item_routines_num_workouts_textview);
            focusTextview = (TextView) itemView.findViewById(R.id.item_routines_focus_textview);
            levelTextview = (TextView) itemView.findViewById(R.id.item_routines_workout_level_textview);
            viewRoutineTextview = (TextView) itemView.findViewById(R.id.item_routines_view_routine_textview);
            viewRoutineTextview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
        String routineName = mRoutines.get(position).getName();
        if (routineName == null) {
            routineName = "New Routine";
        }
        holder.routineName.setText(routineName);

        holder.creatorTextView.setText("created by " + mRoutines.get(position).getCreator());

        String routineDescription = mRoutines.get(position).getDescription();
        if (routineDescription == null){
            routineDescription = "No description for this routine";
        }
        holder.routineDescriptionTextview.setText(routineDescription);

        // workout overview
        int numWorkouts = mRoutines.get(position).getWorkoutsList().size();
        String focus = mRoutines.get(position).getGoal();
        if (focus == null){
            focus = "General";
        }
        String level = mRoutines.get(position).getLevel();
        if (level == null){
            level = "Any";
        }

        holder.numWorkoutsTextview.setText(Integer.toString(numWorkouts));
        holder.levelTextview.setText(level);
        holder.focusTextview.setText(focus);

    }

    @Override
    public int getItemCount() {
        return mRoutines.size();
    }


}
