package com.usi.mwc.justmove.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;
import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.database.DatabaseHandler;
import com.usi.mwc.justmove.model.TravelModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics Fragment implementation, will display the statistics of the travels.
 */
public class StatisticsFragment extends Fragment {

    TextView tvSteps;
    TextView tvDistance;
    TextView tvTime;
    private TabLayout tabLayout;
    private BarChart barChart;

    private DatabaseHandler db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics,container,false);
        BarChart chart = (BarChart) view.findViewById(R.id.chart_statistics);

        // It is connected to the db to retrieve the array of all the stored travels.
        this.db = new DatabaseHandler(getContext());
        ArrayList<TravelModel> allTravels = db.getTravels();

        setTotal(allTravels, view);

        final ArrayList<String> xAxisLabel = new ArrayList<>();
        allTravels.forEach(t -> {
            xAxisLabel.add(t.getName());
        });

        ArrayList<BarEntry> barEntry=new ArrayList<>();
        for (int i = 0; i < allTravels.size(); i++) {
            // for each travel the number of steps are inserted in the ArrayList of BarEntry
            barEntry.add(new BarEntry(i + 1, allTravels.get(i).getNbSteps()));
        }


        BarDataSet barDataSet = new BarDataSet(barEntry, "Steps");
        barDataSet.setColor(R.color.purple_primary_600);

        XAxis xAxis=chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1.0f);
        xAxis.setXOffset(1f);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(allTravels.size());

        YAxis y_axis_right=chart.getAxisRight();
        y_axis_right.setEnabled(false);

        BarData data = new BarData(barDataSet);

        chart.setData(data);
        chart.getDescription().setEnabled(false);


        tabLayout = view.findViewById(R.id.tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ArrayList<BarEntry> barEntry;
                BarDataSet barDataSet;
                BarData data;

                switch (tab.getText().toString()) {
                    case "STEPS":
                        barEntry = new ArrayList<>();
                        for (int i = 0; i < allTravels.size(); i++) {
                            // for each travel the number of steps are inserted in the ArrayList of BarEntry
                            barEntry.add(new BarEntry(i + 1, allTravels.get(i).getNbSteps()));
                        }
                        barDataSet = new BarDataSet(barEntry, "Steps");
                        barDataSet.setColor(R.color.purple_primary_600);
                        data = new BarData(barDataSet);

                        chart.setData(data);
                        chart.getDescription().setEnabled(false);
                        // allows to refresh the chart with the new data
                        chart.notifyDataSetChanged();
                        chart.invalidate();
                        break;

                    case "DISTANCE":
                        barEntry = new ArrayList<>();
                        for (int i = 0; i < allTravels.size(); i++) {
                            // for each travel the distance in km is inserted in the ArrayList of BarEntry
                            Double distanceInM = allTravels.get(i).getDistance() * 1000;
                            barEntry.add(new BarEntry(i + 1, distanceInM.intValue()));
                        }
                        barDataSet = new BarDataSet(barEntry, "Distance");
                        barDataSet.setColor(R.color.purple_primary_600);
                        data = new BarData(barDataSet);

                        chart.setData(data);
                        chart.getDescription().setEnabled(false);
                        // allows to refresh the chart with the new data
                        chart.notifyDataSetChanged();
                        chart.invalidate();
                        break;

                    case "DURATION":
                        barEntry = new ArrayList<>();
                        for (int i = 0; i < allTravels.size(); i++) {
                            // for each travel the duration is inserted in the ArrayList of BarEntry
                            barEntry.add(new BarEntry(i + 1,  allTravels.get(i).getTimeMillisec() / 1000));
                        }
                        barDataSet = new BarDataSet(barEntry, "Duration");
                        barDataSet.setColor(R.color.purple_primary_600);
                        data = new BarData(barDataSet);

                        chart.setData(data);
                        chart.getDescription().setEnabled(false);
                        // allows to refresh the chart with the new data
                        chart.notifyDataSetChanged();
                        chart.invalidate();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        return view;
    }

    private void setTotal(ArrayList<TravelModel> allTravels, View view)  {

        int steps = 0;
        double distances = 0.0;
        int timeMillisec = 0;

        for (TravelModel travel : allTravels) {
            steps += travel.getNbSteps();
            distances += travel.getDistance();
            timeMillisec += travel.getTimeMillisec();
        }
        tvSteps = view.findViewById(R.id.total_steps_text);
        tvDistance = view.findViewById(R.id.total_distance_text);
        tvTime = view.findViewById(R.id.total_time_text);

        tvSteps.setText(String.valueOf(steps));
        tvDistance.setText(String.format("%.2f", distances) + " km");


        String seconds =  String.valueOf((timeMillisec / 1000) % 60) ;
        String minutes =  addZero((timeMillisec / (1000*60)) % 60);
        String hours   =  addZero((timeMillisec / (1000*60*60)) % 24);

        tvTime.setText(hours + ":" + minutes + ":" + seconds);

    }

    private String addZero(int time) {

        if (time < 10) {
            return "0" + String.valueOf(time);
        }
        return  String.valueOf(time);
    }

}