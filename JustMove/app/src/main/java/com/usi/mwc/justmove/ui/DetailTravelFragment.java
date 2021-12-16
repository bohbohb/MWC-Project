package com.usi.mwc.justmove.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.database.DatabaseHandler;
import com.usi.mwc.justmove.model.TravelModel;
import com.usi.mwc.justmove.utils.Utils;

public class DetailTravelFragment extends Fragment {

    DetailTravelFragmentArgs args;
    private TravelModel t;
    private DatabaseHandler db;
    private Context ctx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail_travel, container, false);
        args = DetailTravelFragmentArgs.fromBundle(getArguments());
        // TODO : Convert to Java
        t = args.getTravel();

        ctx = root.getContext();
        db = new DatabaseHandler(root.getContext());
//        Double[] cent = Utils.centroid(t.getPoints());

        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteTravel:
                showDialog();
                return true;
            case R.id.loadTravel:
                DetailTravelFragmentDirections.ActionDetailTravelFragmentToTravelFragment a = DetailTravelFragmentDirections.actionDetailTravelFragmentToTravelFragment(t);
                NavHostFragment.findNavController(requireParentFragment()).navigate(a);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialog() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure to delete this travel ?");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        db.deleteTravel(args.getTravel());
                        NavHostFragment.findNavController(requireParentFragment()).navigateUp();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialogInterface.dismiss();
                        break;
                }
            }
        };

        builder.setPositiveButton("YES",dialogClickListener);
        builder.setNegativeButton("NO",dialogClickListener);
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_travel_menu, menu);
    }
}