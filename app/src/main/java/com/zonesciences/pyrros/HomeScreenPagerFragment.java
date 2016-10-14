package com.zonesciences.pyrros;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Peter on 14/10/2016.
 */
public class HomeScreenPagerFragment extends Fragment {

    public static final String ARG_TAB = "HomeScreenPageFragment: ARG_TAB";

    TextView mTextView;
    LinearLayout ll;

    int mTab;

    public static HomeScreenPagerFragment newInstance(int tab){
        Bundle args = new Bundle();
        args.putInt(ARG_TAB, tab);
        HomeScreenPagerFragment fragment = new HomeScreenPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mTab = getArguments().getInt(ARG_TAB);
        Log.i("HomeScreenPagerFragment", "Current tab: " + mTab);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        //Inflate the layout for this fragment
        //Each fragment contains a recycler view with a set of flashcards
        View view = inflater.inflate(R.layout.fragment_homescreen_pager, container, false);

        mTextView = (TextView) view.findViewById(R.id.menu_page);
        ll = (LinearLayout) view.findViewById(R.id.page_linear_layout);

        if (mTab % 2 == 0){
            ll.setBackgroundColor(Color.RED);
        }

        mTextView.setText("PAGE NUMBER: " + Integer.toString(mTab + 1));

        return view;
    }
}
