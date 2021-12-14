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

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    TextView tvSteps;
    TextView tvDistance;
    TextView tvTime;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LineChart lineChart;

    private DatabaseHandler db;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatisticsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatisticsFragment newInstance(String param1, String param2) {
        StatisticsFragment fragment = new StatisticsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }



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
        tvDistance.setText(String.valueOf(distances));


        String seconds =  String.valueOf((timeMillisec / 1000) % 60) ;
        String minutes =  String.valueOf((timeMillisec / (1000*60)) % 60);
        String hours   =  String.valueOf((timeMillisec / (1000*60*60)) % 24);

        tvTime.setText(hours + ":" + minutes + ":" + seconds);

    }

}