package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;

import java.util.List;


/**
 * Created by Peter on 01/11/2016.
 */
public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetsViewHolder> {

    private final static String TAG = "SetsAdapter";

    Context mContext;

    List<Double> mWeightList;
    List<Integer> mRepsList;


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

    public SetsAdapter(Context context, List<Double> weightsList, List<Integer> repsList){
        Log.i(TAG, "SetsAdapter Constructor called");
        this.mContext = context;
        this.mWeightList = weightsList;
        this.mRepsList = repsList;
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
        Log.i(TAG, "onBindViewHolder called. mSetWeight = " + Double.toString(mWeightList.get(position)) + " mSetReps = " + Integer.toString(mRepsList.get(position)));
        holder.mSetNumber.setText(Integer.toString(position + 1));
        holder.mSetWeight.setText(Double.toString(mWeightList.get(position)) + " kg");
        holder.mSetReps.setText(Integer.toString(mRepsList.get(position)) + " reps");
    }

    @Override
    public int getItemCount() {
        Log.i(TAG, "getItemCount called, mSets = " + mWeightList.size());
        return mWeightList.size();
    }


}
