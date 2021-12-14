package com.usi.mwc.justmove.ui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.adapters.TravelAdapter;
import com.usi.mwc.justmove.database.DatabaseHandler;
import com.usi.mwc.justmove.model.TravelModel;

import java.util.ArrayList;
import java.util.List;

public class MyTravelsFragment extends Fragment implements TravelAdapter.OnItemClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_my_travels, container, false);

        // TODO : Retrive from the DB
        ArrayList<TravelModel> myTravels = this.getTravelsFromDb(root.getContext());
//        ArrayList<TravelModel> myTravels = new ArrayList<>();
//        myTravels.add(new TravelModel(1, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(2, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(3, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(4, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(5, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(6, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(7, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(8, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(9, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(10, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(11, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));
//        myTravels.add(new TravelModel(12, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));


        RecyclerView rv = root.findViewById(R.id.recycler_view);
        rv.setAdapter(new TravelAdapter(myTravels, this));
        rv.setLayoutManager(new LinearLayoutManager(root.getContext()));
        rv.setHasFixedSize(true);
        return root;
    }


    private ArrayList<TravelModel> getTravelsFromDb(Context ctx) {
        DatabaseHandler dbHandler = new DatabaseHandler(ctx);
        return dbHandler.getTravels();
    }

    @Override
    public void onClickItem(View v, int position, TravelModel t) {
        MyTravelsFragmentDirections.ActionMyTravelsToDetailTravelFragment action = MyTravelsFragmentDirections.actionMyTravelsToDetailTravelFragment(t);
        Navigation.findNavController(v).navigate(action);
    }
}