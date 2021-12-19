package com.usi.mwc.justmove.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.database.DatabaseHandler;
import com.usi.mwc.justmove.model.TravelModel;
import com.usi.mwc.justmove.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {

    TextView tvSteps;
    TextView tvDistance;
    TextView tvTime;

    private LineChart lineChart;

    private DatabaseHandler db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // test to see how much space the graph will take
        View view = inflater.inflate(R.layout.fragment_statistics,container,false);
        LineChart chart=(LineChart) view.findViewById(R.id.chart_statistics);

        this.db = new DatabaseHandler(getContext());
        ArrayList<TravelModel> allTravels = db.getTravels();

        setTotal(allTravels, view);

        ArrayList<Entry> LineEntry=new ArrayList<>();
//        LineEntry.add(new Entry(2f,0));


        LineDataSet linedataset= new LineDataSet(LineEntry,"Steps");

        ArrayList<String> labels=new ArrayList<>();

        labels.add("0");
        labels.add("1");
        labels.add("2");

        XAxis xAxis=chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1.0f);
        xAxis.setXOffset(1f);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(31);

        YAxis y_axis_right=chart.getAxisRight();
        y_axis_right.setEnabled(false);
        


        LineData data=new LineData(linedataset);

        chart.setData(data);


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


        tvTime.setText(Utils.millisecToTimeFormat(timeMillisec));

    }

}