package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.fragment.CreateWorkout.ExercisesListener;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 30/11/2016.
 */
public class ExercisesFilterAdapter extends RecyclerView.Adapter<ExercisesFilterAdapter.ExercisesFilterViewHolder> {

    private static final String TAG = "ExercisesFilterAdapter" ;
    Context mContext;

    ArrayList<Exercise> mExercises;
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();

    boolean mIsInWorkout;

    // Listener
    ExercisesListener mExercisesListener;

    // disable removing exercises by deselecting
    boolean disableRemoveExercise;

    public class ExercisesFilterViewHolder extends RecyclerView.ViewHolder {

        ImageView mExerciseAdded;
        TextView mExerciseName;
        CheckBox mCheckBox;

        public ExercisesFilterViewHolder(View itemView) {
            super(itemView);

            mExerciseAdded = (ImageView) itemView.findViewById(R.id.exercise_added);
            mExerciseName = (TextView) itemView.findViewById(R.id.exercise_filter_name);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.exercise_filter_check_box);

        }
    }

    public ExercisesFilterAdapter(final Context context, ArrayList<Exercise> exercises, boolean inEditWorkout){
        this.mContext = context;
        this.mExercises = exercises;
        this.disableRemoveExercise = inEditWorkout;
    }

    @Override
    public ExercisesFilterAdapter.ExercisesFilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_exercise_filter, parent, false);
        return new ExercisesFilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ExercisesFilterAdapter.ExercisesFilterViewHolder holder, final int position) {

        holder.mExerciseName.setText(mExercises.get(position).getName());

        final Exercise exercise = mExercises.get(position);

        holder.mCheckBox.setOnCheckedChangeListener(null);
        holder.mCheckBox.setVisibility(View.VISIBLE);
        holder.mExerciseAdded.setVisibility(View.INVISIBLE);
        holder.mCheckBox.setChecked(exercise.isSelected);

        if (disableRemoveExercise && exercise.isSelected) {
            holder.mCheckBox.setVisibility(View.INVISIBLE);
            holder.mExerciseAdded.setVisibility(View.VISIBLE);
        } else {
            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                    exercise.setSelected(isChecked);

                    if (exercise.isSelected) {
                        if (!mWorkoutExercises.contains(exercise)) {
                            mWorkoutExercises.add(exercise);
                            mExercisesListener.onExerciseAdded(exercise);
                            mExercisesListener.onExercisesChanged(mWorkoutExercises);

                            Snackbar snackbar = Snackbar.make(holder.itemView, exercise.getName() + " added to workout", Snackbar.LENGTH_SHORT);
                            View sbView = snackbar.getView();
                            sbView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.snackbarPositive));
                            snackbar.show();

                            Log.i(TAG, "Exercise added to mWorkoutExercise" + exercise.getName() + " mWorkoutExercises: " + mWorkoutExercises.size());
                        }
                    } else {

                        mWorkoutExercises.remove(exercise);
                        mExercisesListener.onExerciseRemoved(exercise);
                        mExercisesListener.onExercisesChanged(mWorkoutExercises);
                        if (mWorkoutExercises.size() == 0) {
                            mExercisesListener.onExercisesEmpty();
                        }

                        Snackbar snackbar = Snackbar.make(holder.itemView, exercise.getName() + " removed from workout", Snackbar.LENGTH_SHORT);
                        View sbView = snackbar.getView();
                        sbView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.snackbarNegative));
                        snackbar.show();

                        Log.i(TAG, "Exercise removed from mWorkoutExercise" + exercise.getName() + " mWorkoutExercises: " + mWorkoutExercises.size());
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mExercises.size();
    }

    public void setExercisesListener(ExercisesListener listener){
        this.mExercisesListener = listener;
    }

    public List<Exercise> getWorkoutExercises() {
        return mWorkoutExercises;
    }

    public void setWorkoutExercises(ArrayList<Exercise> workoutExercises) {
        mWorkoutExercises = workoutExercises;
    }

    public void setDisableRemoveExercise(boolean disableRemoveExercise) {
        this.disableRemoveExercise = disableRemoveExercise;
    }
}
