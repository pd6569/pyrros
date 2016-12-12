package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.ActionMode.ActionModeAdapterInterface;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperAdapter;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperViewHolder;
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
public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetsViewHolder> implements ActionModeAdapterInterface {

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

    // Action Mode
    SparseBooleanArray mSelectedSetsIds = new SparseBooleanArray();

    // Edit Mode
    int setBeingEdited = -1;

    public class SetsViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mSetContainer;
        TextView mSetNumber;
        TextView mSetWeight;
        TextView mSetReps;

        public SetsViewHolder(View itemView) {
            super(itemView);
            mSetNumber = (TextView) itemView.findViewById(R.id.textview_set_number);
            mSetWeight = (TextView) itemView.findViewById(R.id.textview_set_weight);
            mSetReps = (TextView) itemView.findViewById(R.id.textview_set_reps);
            mSetContainer = (LinearLayout) itemView.findViewById(R.id.linear_layout_sets);
            mSetContainer.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view){
                    mSetsListener.onSetSelected(getAdapterPosition());
                }
            });
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

                Log.i(TAG, "Sets data changed");
                //Check if the change being made is removal/addition of set. If set is simply being reorder, then do nothing.
                    if (dataSnapshot.getKey() == "weight") {

                        mWeightList = (List) dataSnapshot.getValue();
                        notifyDataSetChanged();

                    } else if (dataSnapshot.getKey() == "reps") {

                        mRepsList = (List) dataSnapshot.getValue();
                        notifyDataSetChanged();
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

        /** Change background color of the selected items in recycler view  **/
        holder.itemView.setBackgroundColor(mSelectedSetsIds.get(position) ? ResourcesCompat.getColor(mContext.getResources(), R.color.colorAccent, null) : Color.WHITE);

        if (position == setBeingEdited){
            holder.itemView.setBackgroundColor(ResourcesCompat.getColor(mContext.getResources(), R.color.colorAccent, null));
        }
    }

    @Override
    public int getItemCount() {
        return mWeightList.size();
    }


    /*//if the set is a record, remove it from records
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
    }*/

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


    /***
     * Methods for selecting exercises
     */

    // Toggle selection methods
    public void toggleSelection(int position) {
        selectItem(position, !mSelectedSetsIds.get(position));
    }

    // Remove selected selections
    public void removeSelection(){
        mSelectedSetsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    //Put or delete selected position into SparseBooleanArray
    public void selectItem(int position, boolean isSelected){
        if (isSelected){
            mSelectedSetsIds.put(position, isSelected);
        } else {
            mSelectedSetsIds.delete(position);
        }
        notifyDataSetChanged();
    }

    // Get total selected count
    public int getSelectedCount(){
        return mSelectedSetsIds.size();
    }

    // Return all selected sets
    public SparseBooleanArray getSelectedItemIds(){
        return mSelectedSetsIds;
    }

    public void clearSelectedItems(){
        mSelectedSetsIds.clear();
        notifyDataSetChanged();
    }

    public void deleteSelectedItems(){

        for (int i = (mSelectedSetsIds.size()-1); i >= 0; i--){
            if (mSelectedSetsIds.valueAt(i)){

                /*removeFromRecords(mWeightList.get(mSelectedSetsIds.keyAt(i)), mRepsList.get(mSelectedSetsIds.keyAt(i))); // check if the set thats being dismissed is a record*/

                mWeightList.remove(mSelectedSetsIds.keyAt(i));
                mRepsList.remove(mSelectedSetsIds.keyAt(i));

            }
        }

        notifyDataSetChanged();
        mSelectedSetsIds.clear();

        // Push updates to workout-exercises and to user-workout-exercises
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/weight/", mWeightList);
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/reps/", mRepsList);
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/sets/", mWeightList.size());
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/weight/", mWeightList);
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/reps/", mRepsList);
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseReference.getKey() + "/sets/", mWeightList.size());
        mExerciseReference.getRoot().updateChildren(childUpdates);

        // Notify fragment
        mSetsListener.onSetsChanged();

    }

    //listener to update fragment which contains exercise object with working sets
    public interface SetsListener{
        void onSetSelected(int setIndex);
        void onSetsChanged();
        void onRecordChanged();
    }

    public void setSetsListener (SetsListener listener){
        this.mSetsListener = listener;
    }

    public void setSetBeingEdited(int setBeingEdited) {
        this.setBeingEdited = setBeingEdited;
    }
}
