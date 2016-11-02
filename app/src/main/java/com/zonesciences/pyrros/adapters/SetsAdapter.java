package com.zonesciences.pyrros.adapters;

import android.content.Context;
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
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Peter on 01/11/2016.
 */
public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetsViewHolder> {

    private final static String TAG = "SetsAdapter";

    Context mContext;
    DatabaseReference mExerciseReference;

    List<Double> mWeightList = new ArrayList<>();
    List<Long> mRepsList = new ArrayList<>();


    public static class SetsViewHolder extends RecyclerView.ViewHolder {

        TextView mSetNumber;
        TextView mSetWeight;
        TextView mSetReps;

        public SetsViewHolder(View itemView) {
            super(itemView);
            Log.i(TAG, "SetsViewHolder called");
            mSetNumber = (TextView) itemView.findViewById(R.id.textview_set_number);
            mSetWeight = (TextView) itemView.findViewById(R.id.textview_set_weight);
            mSetReps = (TextView) itemView.findViewById(R.id.textview_set_reps);
        }
    }

    public SetsAdapter(final Context context, DatabaseReference exerciseReference){
        Log.i(TAG, "SetsAdapter Constructor called");
        this.mContext = context;
        this.mExerciseReference = exerciseReference;

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildAdded called key: " + dataSnapshot.getKey() + " Value: " + dataSnapshot.getValue());
                if (dataSnapshot.getKey() == "weight"){
                    mWeightList = (List)dataSnapshot.getValue();
                    Log.i(TAG, "datasnapshot key = weight. containing values " + dataSnapshot.getValue() + " mWeightList: " + mWeightList);
                } else if (dataSnapshot.getKey() == "reps"){
                    mRepsList = (List)dataSnapshot.getValue();
                    Log.i(TAG, "datasnapshot key = reps. containing values " + dataSnapshot.getValue() + " mRepsList: " + mRepsList);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildChanged called key: " + dataSnapshot.getKey() + " Value: " + dataSnapshot.getValue());
                if (dataSnapshot.getKey() == "weight"){
                    mWeightList = (List) dataSnapshot.getValue();
                    Log.i(TAG, "datasnapshot key = weight. containing values " + dataSnapshot.getValue() + " mWeightList: " + mWeightList);
                } else if (dataSnapshot.getKey() == "reps"){
                    mRepsList = (List) dataSnapshot.getValue();
                    Log.i(TAG, "datasnapshot key = reps. containing values " + dataSnapshot.getValue() + " mRepsList: " + mRepsList);
                }
                notifyDataSetChanged();
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
        Log.i(TAG, "onCreateViewHolder called");
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_sets, parent, false);
        return new SetsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SetsViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder called. mSetWeight = " + Double.toString(mWeightList.get(position)) + " mSetReps = " + Long.toString(mRepsList.get(position)));
        holder.mSetNumber.setText(Integer.toString(position + 1));
        holder.mSetWeight.setText(Double.toString(mWeightList.get(position)) + " kg");
        holder.mSetReps.setText(Long.toString(mRepsList.get(position)) + " reps");
    }

    @Override
    public int getItemCount() {
        return mWeightList.size();
    }


}
