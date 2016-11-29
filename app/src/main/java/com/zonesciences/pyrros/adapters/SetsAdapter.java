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
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperAdapter;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Record;
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

                if (dataSnapshot.getKey().equals("weight")){

                    //convert list stored on firebase to list of doubles
                    //firebase automatically converts doubles to longs when it can, e.g. 10.0 stored as 10
                    List list = (List) dataSnapshot.getValue();
                    List<Double> weightList= new ArrayList<>();
                    for (Object weight : list){

                        if(weight instanceof Long){
                            long l = (long) weight;
                            weight = (double) l;
                        }
                        weightList.add((double) weight);
                    }
                    mWeightList = weightList;

                } else if (dataSnapshot.getKey().equals("reps")){
                    mRepsList = (List)dataSnapshot.getValue();

                }
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                //Check if the change being made is removal/addition of set. If set is simply being reorder, then do nothing.
                    if (dataSnapshot.getKey() == "weight") {

                        if (mWeightList.size() != dataSnapshot.getChildrenCount()) {
                            mWeightList = (List) dataSnapshot.getValue();

                            notifyDataSetChanged();
                        } else {
                            //do nothing
                        }
                    } else if (dataSnapshot.getKey() == "reps") {
                        if (mRepsList.size() != dataSnapshot.getChildrenCount()) {
                            mRepsList = (List) dataSnapshot.getValue();

                            notifyDataSetChanged();
                        } else {
                            // do nothing
                        }
                    }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

        removeFromRecords(mWeightList.get(position), mRepsList.get(position)); // check if the set thats being dismissed is a record

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

    //if the set is a record, remove it from records
    private void removeFromRecords(final Double weight, final Long reps) {
        Log.i(TAG, "removeFromRecords called");
        mExerciseReference.getRoot().child("user-records").child(mUser).child(mExerciseReference.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Record record = dataSnapshot.getValue(Record.class);
                String key = Long.toString(reps) + " rep-max";

                //is an identical set found in the records
                if (record.getRecords().containsKey(key) && record.getRecords().get(key).contains(weight) ){

                    int index = record.getRecords().get(key).indexOf(weight);

                    // get the workout in which this record was set, and check that it is the current workout.
                    // if it isn't then the record was set in another workout and it cannot be removed from the record list!
                    if (record.getWorkoutKey().get(key).get(index).equals(mWorkoutKey)){

                        // the record may be set in this workout, and it may have been done multiple times
                        // do not want to remove the record if user removes one of the sets that equals the records.
                        // Only remove from the records if it is the ONLY set in the workout matching the record.

                        if (getMatchingSets(weight, reps) == 0) {

                            record.getRecords().get(key).remove(weight);
                            record.getDate().get(key).remove(index);
                            record.getWorkoutKey().get(key).remove(index);

                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-records/" + mUser + "/" + mExerciseReference.getKey(), record);
                            childUpdates.put("/records/" + mExerciseReference.getKey() + "/" + mUser, record);
                            mExerciseReference.getRoot().updateChildren(childUpdates);
                            mSetsListener.onRecordChanged();
                        }

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int getMatchingSets(Double weight, Long reps) {
        int matches = 0;
        Log.i(TAG, "Checking if any matches for set with weight = " + weight + " and reps = " + reps + " mWeightList size = " + mWeightList.size());
        for (int i = 0; i < mWeightList.size(); i++){
            Log.i(TAG, "weight = " + weight);
            Log.i(TAG, "mWeightList.get(" + i + ")" + mWeightList.get(i));
            if (mWeightList.get(i).equals(weight)){
                Log.i(TAG, "Found matching weight in set");
                if (mRepsList.get(i).equals(reps)){
                    Log.i(TAG, "This set contains the same number of weight and reps, therefore equals the record");
                    matches++;
                }
            }
        }
        return matches;
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
        public void onRecordChanged();
    }

    public void setSetsListener (SetsListener listener){
        this.mSetsListener = listener;
    }

}
