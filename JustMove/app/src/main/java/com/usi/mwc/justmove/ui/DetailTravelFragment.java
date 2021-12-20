package com.usi.mwc.justmove.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.database.DatabaseHandler;
import com.usi.mwc.justmove.model.TravelModel;
import com.usi.mwc.justmove.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DetailTravelFragment extends Fragment {

    DetailTravelFragmentArgs args;
    private TravelModel t;
    private DatabaseHandler db;
    private Context ctx;

    TextView tvSteps;
    TextView tvDistance;
    TextView tvTime;
    TextView tvPB;
    TextView tvDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail_travel, container, false);
        args = DetailTravelFragmentArgs.fromBundle(getArguments());

        t = args.getTravel();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(t.getName());


        tvSteps = root.findViewById(R.id.steps_detail_string);
        tvDistance = root.findViewById(R.id.distance_detail_string);
        tvTime = root.findViewById(R.id.time_detail_string);
        tvPB = root.findViewById(R.id.bike_boolean_detail);
        tvDate = root.findViewById(R.id.label_date_details);

        tvSteps.setText(String.valueOf(t.getNbSteps()));
        tvDistance.setText(String.format("%.2f", t.getDistance()) + " km");
        tvTime.setText(t.getTime());
        tvPB.setText(t.getPublibike() == 0 ? "No" : "Yes");
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy");

        try{
            Date travelDate = format.parse(t.getDateTravel());
            tvDate.setText(dateFormat.format(travelDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ctx = root.getContext();
        db = new DatabaseHandler(root.getContext());

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