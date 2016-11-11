package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperAdapter;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Created by Peter on 01/11/2016.
 */
public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetsViewHolder> implements ItemTouchHelperAdapter {

    //TODO: set number update after reorder
    private final static String TAG = "SetsAdapter";

    Context mContext;
    DatabaseReference mExerciseReference;
    String mWorkoutKey;
    String mUser;

    List<Double> mWeightList = new ArrayList<>();
    List<Long> mRepsList = new ArrayList<>();

    String mUnit;
    double mConversionMultiple;


    SetsListener mSetsListener;
    boolean mMoved;

    public static class SetsViewHolder extends RecyclerView.ViewHolder {

        TextView mSetNumber;
        TextView mSetWeight;
        TextView mSetReps;

        public SetsViewHolder(View itemView) {
            super(itemView);
            mSetNumber = (TextView) itemView.findViewById(R.id.textview_set_number);
            mSetWeight = (TextView) itemView.findViewById(R.id.textview_set_weight);
            mSetReps = (TextView) itemView.findViewById(R.id.textview_set_reps);
        }
    }

    public SetsAdapter(final Context context, DatabaseReference exerciseReference, String workoutKey, String user){
        this.mContext = context;
        this.mExerciseReference = exerciseReference;
        this.mWorkoutKey = workoutKey;
        this.mUser = user;
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getString("pref_unit", null).equals("metric")){
            mUnit = " kgs";
            mConversionMultiple = 1.0;
        } else {
            mUnit = " lbs";
            mConversionMultiple = 2.20462;
        }


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildAdded called key: " + dataSnapshot.getKey() + " Value: " + dataSnapshot.getValue());
                if (dataSnapshot.getKey().equals("weight")){

                    //convert list stored on firebase to list of doubles
                    //firebase automatically converts doubles to longs when it can, e.g. 10.0 stored as 10
                    List list = (List) dataSnapshot.getValue();
                    List<Double> weightList= new ArrayList<>();
                    for (Object weight : list){
                        Log.i(TAG, "This weight is of class: " + weight.getClass());
                        if(weight instanceof Long){
                            long l = (long) weight;
                            weight = (double) l;
                        }
                        weightList.add((double) weight);
                    }
                    mWeightList = weightList;
                    Log.i(TAG, "datasnapshot key = weight. containing values " + dataSnapshot.getValue() + " mWeightList: " + mWeightList);
                } else if (dataSnapshot.getKey().equals("reps")){
                    mRepsList = (List)dataSnapshot.getValue();
                    Log.i(TAG, "datasnapshot key = reps. containing values " + dataSnapshot.getValue() + " mRepsList: " + mRepsList);
                }
                notifyDataSetChanged();
            }

            //TODO: Clean up this method
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildChanged called key: " + dataSnapshot.getKey() + " Value: " + dataSnapshot.getValue());

                //Check if the change being made is removal/addition of set. If set is simply being reorder, then do nothing.
                    if (dataSnapshot.getKey() == "weight") {
                        Log.i(TAG, "mWeighlist = " + mWeightList.size() + " children count for weight = " + dataSnapshot.getChildrenCount());
                        if (mWeightList.size() != dataSnapshot.getChildrenCount()) {
                            mWeightList = (List) dataSnapshot.getValue();
                            Log.i(TAG, "datasnapshot key = weight. containing values " + dataSnapshot.getValue() + " mWeightList: " + mWeightList);
                            notifyDataSetChanged();
                        } else {
                            //do nothing
                        }
                    } else if (dataSnapshot.getKey() == "reps") {
                        if (mRepsList.size() != dataSnapshot.getChildrenCount()) {
                            mRepsList = (List) dataSnapshot.getValue();
                            Log.i(TAG, "datasnapshot key = reps. containing values " + dataSnapshot.getValue() + " mRepsList: " + mRepsList);
                            notifyDataSetChanged();
                        } else {
                            // do nothing
                        }
                    }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onChildRemoved called at reference: " + mExerciseReference.toString());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildMoved called at reference: " + mExerciseReference.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "onChildCancelled called at reference: " + mExerciseReference.toString());
            }
        };

        exerciseReference.addChildEventListener(childEventListener);
    }


    @Override
    public SetsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_sets, parent, false);
        return new SetsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SetsViewHolder holder, int position) {

        holder.mSetNumber.setText(Integer.toString(position + 1));

        double weight = mWeightList.get(position) * mConversionMultiple;
        /*String s = String.format("%1.2f", weight);*/
        String s = Utils.formatWeight(weight);
        holder.mSetWeight.setText(s + mUnit);
        holder.mSetReps.setText(Long.toString(mRepsList.get(position)) + " reps");
    }

    @Override
    public int getItemCount() {
        return mWeightList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        // the position of the data is changed every time the view is shifted to a new index,
        // NOT at the end of the drop event.

        mMoved = true;
        if (fromPosition < toPosition){
            for (int i = fromPosition; i < toPosition; i++){
                Collections.swap(mWeightList, i, i + 1);
                Collections.swap(mRepsList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--){
                Collections.swap(mWeightList, i, i - 1);
                Collections.swap(mRepsList, i, i - 1);
            }
        }

        //Push updates to workout-exercises and to user-workout-exercises
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/weight/", mWeightList);
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/reps/", mRepsList);
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/weight/", mWeightList);
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/reps/", mRepsList);
        mExerciseReference.getRoot().updateChildren(childUpdates);

        notifyItemMoved(fromPosition, toPosition);
        mSetsListener.onSetsChanged();
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

        mMoved = false;
        mWeightList.remove(position);
        mRepsList.remove(position);

        //Push updates to workout-exercises and to user-workout-exercises
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/weight/", mWeightList);
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/reps/", mRepsList);
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/sets/", mWeightList.size());
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/weight/", mWeightList);
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/reps/", mRepsList);
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/sets/", mWeightList.size());
        mExerciseReference.getRoot().updateChildren(childUpdates);

        notifyItemRemoved(position);
        mSetsListener.onSetsChanged();
    }

    @Override
    public void onMoveCompleted() {
        if (mMoved) {
            notifyDataSetChanged();
        } else { // do nothing }
        }
    }

    //listener to update fragment which contains exercise object with working sets
    public interface SetsListener{
        public void onSetsChanged();
    }

    public void setSetsListener (SetsListener listener){
        this.mSetsListener = listener;
    }

}
