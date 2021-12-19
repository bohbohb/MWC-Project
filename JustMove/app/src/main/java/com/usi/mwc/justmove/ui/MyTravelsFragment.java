package com.usi.mwc.justmove.ui;

import static com.usi.mwc.justmove.ui.MyTravelsFragmentDirections.*;

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

        ArrayList<TravelModel> myTravels = this.getTravelsFromDb(root.getContext());
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
        ActionMyTravelsToDetailTravelFragment action = actionMyTravelsToDetailTravelFragment(t);
        Navigation.findNavController(v).navigate(action);
    }
}