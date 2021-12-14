package com.usi.mwc.justmove.ui;

import static com.usi.mwc.justmove.utils.Utils.getDate;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.api.PublibikeAPIClient;
import com.usi.mwc.justmove.api.PublibikeAPIInterface;
import com.usi.mwc.justmove.database.DatabaseHandler;
import com.usi.mwc.justmove.model.InterestPointModel;
import com.usi.mwc.justmove.model.PointModel;
import com.usi.mwc.justmove.model.Stations;
import com.usi.mwc.justmove.model.TravelModel;
import com.usi.mwc.justmove.utils.Map;
import com.usi.mwc.justmove.utils.Utils;

import java.util.ArrayList;

public class MainFragment extends Fragment implements OnMapReadyCallback {

    private DatabaseHandler db;
    private Long timeWhenStopped = 0L;
    private Chronometer lblChronometer;
    private Button btnStart;
    private Button btnStop;
    private TextView tvTravelDistance;
    private MapView mapView;
    private GoogleMap mGoogleMap;

    private MutableLiveData<Double> travelLiveDistance = new MutableLiveData<>();
    private MutableLiveData<Integer> travelLiveInterestPoints = new MutableLiveData<>();

    private Long currentTravelId;
    private Long startPointId;
    private ArrayList<InterestPointModel> currentTravelInterestPoints;
    private Location lastLocation;
    private TravelModel currentTravel;
    private boolean travelStarted;

    private int interestPointId = 0;
    private int lastInterestPointId = -1;

    private MainFragmentArgs travelArg;
    private Boolean mLocationPermissionsGranted = false;
    private Context ctx;
    private LocationManager locationManager;

    private AlertDialog alertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        this.db = new DatabaseHandler(getContext());
        if (getArguments() != null) {
            travelArg = MainFragmentArgs.fromBundle(getArguments());
        }
        ctx = getContext();

        lblChronometer = (Chronometer) root.findViewById(R.id.lblChrono);
        btnStart = (Button) root.findViewById(R.id.btnStart);
        btnStop = (Button) root.findViewById(R.id.btnStop);
        tvTravelDistance = (TextView) root.findViewById(R.id.tvTravelDistance);
        mapView = (MapView) root.findViewById(R.id.mapTravel);

        btnStart.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.INVISIBLE);

        travelLiveDistance.setValue(0.0);
        travelLiveDistance.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                tvTravelDistance.setText(String.valueOf(aDouble));
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTravelButtons();
                currentTravel = initTravel();
                travelLiveDistance.setValue(currentTravel.getDistance());
                travelLiveInterestPoints.setValue(currentTravelInterestPoints.size());
                if (travelArg.getTravel() == null)
                    mGoogleMap.clear();
                travelStarted = true;
                initChronometer(lblChronometer);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTravelButtons();
                timeWhenStopped = lblChronometer.getBase() - SystemClock.elapsedRealtime(); // Before Stop
                travelStarted = false;
                lblChronometer.stop();
                showSaveDialog();
                Snackbar travelStopped = Snackbar.make(view, "Travel stopped", Snackbar.LENGTH_SHORT);
                travelStopped.show();
            }
        });

        // FOR TESTING ONLY
//        this.db.insertNewTravel(new TravelModel(1, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));

        mapView.onCreate(savedInstanceState);
        getLocationPermissions();
        startLocationManager();

        getStations();
        return root;
    }

    private void toggleTravelButtons() {
        if ((btnStart.getVisibility() == View.VISIBLE) && (btnStop.getVisibility() == View.INVISIBLE)) {
            btnStart.setVisibility(View.INVISIBLE);
            btnStop.setVisibility(View.VISIBLE);
        } else {
            btnStart.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.INVISIBLE);
        }
    }

    private void initChronometer(Chronometer lblChronometer) {
        lblChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        lblChronometer.start();
        lblChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                chronometer.setText(Utils.ticksToHHMMSS(chronometer));
            }
        });
        lblChronometer.setBase(SystemClock.elapsedRealtime());
        lblChronometer.setText("00:00:00");
    }

    private TravelModel initTravel() {

        interestPointId = 0;
        lastInterestPointId = -1;

        TravelModel firstOrDefaultTravel = getFirstOrDefaultTravel();
        loadInterestPoints();
        setStartingPoint();
        return firstOrDefaultTravel;
    }

    private void setStartingPoint() {
        PointModel startPoint = db.getFirstPointTravel(currentTravelId.intValue());
        if (startPoint == null) {
            startPointId = db.insertNewPoint(
                    new PointModel(lastLocation.getLatitude(), lastLocation.getLongitude(), currentTravelId.intValue())
            );
        } else {
            startPointId = Long.valueOf(startPoint.getId());
        }
    }

    private void initMap() {
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    private void getLocationPermissions() {
        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                Toast.makeText(ctx, "Map requires location permissions", Toast.LENGTH_LONG).show();
            }
        });
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void loadInterestPoints() {
        if (currentTravelInterestPoints == null) {
            currentTravelInterestPoints = new ArrayList<>();
        }
        currentTravelInterestPoints.clear();
        if (travelArg != null && travelArg.getTravel() != null) {
            ArrayList<InterestPointModel> markers = db.getInterestPointsForTravel(travelArg.getTravel().getId());
            markers.forEach(m -> currentTravelInterestPoints.add(new InterestPointModel(
                    m.getId(),
                    m.getName(),
                    m.getLat(),
                    m.getLon(),
                    m.getIdTravel()
            )));
            travelLiveInterestPoints.setValue(markers.size());
        }
    }

    private TravelModel getFirstOrDefaultTravel() {
        TravelModel firstOrDefault;
        ArrayList<TravelModel> oldTravels = db.getEmptyTravel();
        if (oldTravels.isEmpty()) {
            firstOrDefault = new TravelModel();
            currentTravelId = db.insertNewTravel(firstOrDefault);

        } else {
            firstOrDefault = oldTravels.get(0);
            firstOrDefault.getPoints().clear();
            currentTravelId = Long.valueOf(firstOrDefault.getId());
        }
        return firstOrDefault;
    }

    public void getStations() {
        PublibikeAPIInterface service = PublibikeAPIClient.getRetrofitInstance().create(PublibikeAPIInterface.class);
        Call<Stations> call= service.getStationsData();
        call.enqueue(new Callback<Stations>() {
            @Override
            public void onResponse(Call<Stations> call, Response<Stations> response) {
                System.out.println(response.body().getStations());
            }

            @Override
            public void onFailure(Call<Stations> call, Throwable t) {
                Toast.makeText(getContext(), "Unable to retrieve stations from API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startLocationManager() {
        this.locationManager = (LocationManager) getActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE);
        try {
            // Request location updates
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    0f,
                    locationListener
            );
        } catch (SecurityException ex) {
            Log.d("Location exception", ex.toString());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setMyLocationEnabled(true);
        // TODO : Convert to Java once implemented
//        if (this::currentTravel.isInitialized) {
//            drawPathOnMap(ctx, R.color.red_600, currentTravel, mGoogleMap!!);
//            drawMarkersOnMap(mGoogleMap!!, currentTravelInterestPoints);
//        }
        mapView.setVisibility(View.VISIBLE);
    }


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Location oldLocation = lastLocation;
            lastLocation = location;
            if (travelStarted) {
                PointModel startingPoint = new PointModel(
                        lastLocation.getLatitude(),
                        lastLocation.getLongitude(),
                        startPointId.intValue(),
                        currentTravelId.intValue()
                );
                currentTravel.getPoints().add(startingPoint);
                startPointId = startPointId + 1;
                travelLiveDistance.setValue(Double.parseDouble(String.format(
                        "%.2f", travelLiveDistance.getValue() + (
                                Utils.distanceKM(
                                        new PointModel(oldLocation.getLatitude(), oldLocation.getLongitude()),
                                        new PointModel(lastLocation.getLatitude(), lastLocation.getLongitude())
                                )
                        )
                        ))
                );
                Map.drawPathOnMap(ctx, R.color.purple_700, currentTravel, mGoogleMap);
                // TODO : Implement notifications
//                checkProximityAndNotify()
            }
            if (mGoogleMap != null) {
                Map.moveCamera(mGoogleMap, new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    private void showSaveDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.save_dialog_layout, null);

        EditText nameText = (EditText) dialogView.findViewById(R.id.tvTravelName);
        Button saveBtn = dialogView.findViewById(R.id.saveDialogBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTravel.setName(nameText.getText().toString());
                saveTravel();
//                btnSave.isEnabled = false
                lblChronometer.setText(R.string.chronoInitialString);
                mGoogleMap.clear();
                travelLiveDistance.setValue(0.0);
                travelLiveInterestPoints.setValue(0);
                currentTravelInterestPoints = new ArrayList<>();
                alertDialog.dismiss();
            }
        });
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }



    private void saveTravel() {
        double distance = Utils.getDistanceForTravel(currentTravel);
        String time = lblChronometer.getText().toString();
        // TODO: Set correct number of stes and publibike
        TravelModel tmp = new TravelModel(currentTravelId.intValue(), currentTravel.getName(), "", distance, time, getDate(), currentTravel.getPoints(), 0, 0);
        db.updateTravel(tmp);
        currentTravel.getPoints().forEach(p -> db.insertNewPoint(p));
        currentTravelInterestPoints.forEach(ip -> db.insertNewInterestPoint(ip));
    }
}