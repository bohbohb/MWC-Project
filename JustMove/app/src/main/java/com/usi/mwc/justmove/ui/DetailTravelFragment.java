package com.usi.mwc.justmove.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.database.DatabaseHandler;
import com.usi.mwc.justmove.model.TravelModel;
import com.usi.mwc.justmove.utils.Utils;

public class DetailTravelFragment extends Fragment {

    // TODO : Convert to Java
//    val args: DetailBaladeFragmentArgs by navArgs()
    private TravelModel t;
    private DatabaseHandler db;
    private Context ctx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail_travel, container, false);
        // TODO : Convert to Java
//        b = args.balade

        ctx = root.getContext();
        db = new DatabaseHandler(root.getContext());
//        Double[] cent = Utils.centroid(t.getPoints());
        return root;
    }
}