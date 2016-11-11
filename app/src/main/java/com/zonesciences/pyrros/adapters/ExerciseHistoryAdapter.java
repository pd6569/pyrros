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
import com.zonesciences.pyrros.models.Exercise;

import java.util.List;

/**
 * Created by Peter on 10/11/2016.
 */
public class ExerciseHistoryAdapter extends RecyclerView.Adapter<ExerciseHistoryAdapter.ViewHolder> {

    private static final String TAG = "ExerciseHistoryAdapter" ;

    Context mContext;

    //Exercise data
    List<String> mWorkoutDates;
    List<Exercise> mExercises;

    public ExerciseHistoryAdapter(Context context, List<String> workoutDate, List<Exercise> exercises){
        this.mContext = context;
        this.mWorkoutDates = workoutDate;
        this.mExercises = exercises;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_exercise_history, parent, false);
        return new ExerciseHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder()");
        holder.mExerciseDate.setText(mWorkoutDates.get(position));



        LinearLayout setsContainer = (LinearLayout) holder.itemView.findViewById(R.id.exercise_history_sets_container);
        setsContainer.removeAllViews();

        Exercise currentExercise = mExercises.get(position);
        Log.i(TAG, "New exercise object created for view: " + currentExercise.getName());
        int numSets = mExercises.get(position).getSets();

        if (numSets == 0) {
            holder.mNoSetsCreated.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < numSets; i++) {
            View setsView = LayoutInflater.from(mContext).inflate(R.layout.item_sets, null);

            TextView setNumber = (TextView) setsView.findViewById(R.id.textview_set_number);
            setNumber.setVisibility(View.GONE);
            TextView setWeight = (TextView) setsView.findViewById(R.id.textview_set_weight);
            TextView setReps = (TextView) setsView.findViewById(R.id.textview_set_reps);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
            setWeight.setText("" + currentExercise.getWeight().get(i) + " kgs");
            setWeight.setLayoutParams(params);
            setReps.setText("" + currentExercise.getReps().get(i) + " reps");
            setReps.setLayoutParams(params);
            setsContainer.addView(setsView);
        }

    }

    @Override
    public int getItemCount() {
        return mExercises.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mExerciseDate;
        TextView mNoSetsCreated;

        public ViewHolder(View itemView) {
            super(itemView);
            mExerciseDate = (TextView) itemView.findViewById(R.id.exercise_history_title_date);
            mNoSetsCreated = (TextView) itemView.findViewById(R.id.exercise_history_no_sets);
        }
    }
}
