package com.usi.mwc.justmove.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.database.DatabaseHandler;

public class MainFragment extends Fragment {

    private DatabaseHandler db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        this.db = new DatabaseHandler(root.getContext());

        return root;
    }
}