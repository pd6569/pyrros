package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 01/01/2017.
 */

public class AddSetExerciseOptionsAdapter extends RecyclerView.Adapter<AddSetExerciseOptionsAdapter.ViewHolder> {

    Context mContext;
    List<Integer> mSets;

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView setNumberText;
        TextView numRepsText;

        public ViewHolder(View itemView) {
            super(itemView);
            setNumberText = (TextView) itemView.findViewById(R.id.set_number_textview);
            numRepsText = (TextView) itemView.findViewById(R.id.number_of_reps_textview);
        }
    }

    public AddSetExerciseOptionsAdapter(Context context, List<Integer> sets){
        this.mContext = context;
        this.mSets = sets;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_add_sets, parent, false);
        return new AddSetExerciseOptionsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        System.out.print("onBindViewHolder");
        holder.setNumberText.setText("Set " + (position + 1));
        holder.numRepsText.setText(Long.toString(mSets.get(position)) + " reps");
    }

    @Override
    public int getItemCount() {
        return mSets.size();
    }

    public List<Integer> getSets() {
        return mSets;
    }
}
