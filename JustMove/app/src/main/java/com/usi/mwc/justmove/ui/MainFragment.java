package com.usi.mwc.justmove.ui;

import static com.usi.mwc.justmove.utils.Utils.getDate;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.api.PublibikeAPIClient;
import com.usi.mwc.justmove.api.PublibikeAPIInterface;
import com.usi.mwc.justmove.database.DatabaseHandler;
import com.usi.mwc.justmove.listeners.StepCounterListener;
import com.usi.mwc.justmove.model.InterestPointModel;
import com.usi.mwc.justmove.model.PointModel;
import com.usi.mwc.justmove.model.Station;
import com.usi.mwc.justmove.model.Stations;
import com.usi.mwc.justmove.model.TravelModel;
import com.usi.mwc.justmove.utils.Map;
import com.usi.mwc.justmove.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements OnMapReadyCallback {

    private DatabaseHandler db;
    private Long timeWhenStopped = 0L;
    private Chronometer lblChronometer;
    private Button btnStart;
    private Button btnStop;
    private FloatingActionButton btnTakePublibike;
    private FloatingActionButton btnLeavePublibike;
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
    private Integer usingPB = 0;
    private Integer currentTravelUsePB = 0;

    private int interestPointId = 0;
    private int lastInterestPointId = -1;

    private MainFragmentArgs travelArg;
    private Boolean mLocationPermissionsGranted = false;
    private Boolean mActivityPermissionsGranted = false;
    private Context ctx;
    private LocationManager locationManager;

    private AlertDialog alertDialog;

    private MutableLiveData<List<Station>> allStationsLive = new MutableLiveData<>();
    private String lastStationName = "";
    private NotificationManagerCompat notifManager;
    private final double PROXIMITY_DISTANCE = 20.0;
    private MutableLiveData<Integer> stepLiveDate = new MutableLiveData<>();

    private TextView tvStepView;

    private Sensor mSensorACC;
    private SensorManager mSensorManager;

    private SensorEventListener stepListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        this.db = new DatabaseHandler(getContext());
        if (getArguments() != null && !getArguments().isEmpty()) {
            travelArg = MainFragmentArgs.fromBundle(getArguments());
        }
        ctx = getContext();
        createNotificationChannel();
        notifManager = NotificationManagerCompat.from(ctx);

        tvStepView = (TextView) root.findViewById(R.id.text_steps);

        btnTakePublibike = (FloatingActionButton) root.findViewById(R.id.btnTakePublibike);
        btnLeavePublibike = (FloatingActionButton) root.findViewById(R.id.btnLeavePublibike);

        stepLiveDate.setValue(0);
        stepLiveDate.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer stepInt) {

                tvStepView.setText(String.valueOf(stepInt));

            }
        });

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorACC = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        stepListener = new StepCounterListener(stepLiveDate);

        lblChronometer = (Chronometer) root.findViewById(R.id.lblChrono);
        btnStart = (Button) root.findViewById(R.id.btnStart);
        btnStop = (Button) root.findViewById(R.id.btnStop);
        tvTravelDistance = (TextView) root.findViewById(R.id.tvTravelDistance);
        mapView = (MapView) root.findViewById(R.id.mapTravel);

        btnStart.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.INVISIBLE);

        allStationsLive.setValue(new ArrayList<>());
        allStationsLive.observe(getViewLifecycleOwner(), new Observer<List<Station>>() {
            @Override
            public void onChanged(List<Station> stations) {
                stations.forEach(s -> {
                    mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(s.getLatitude(), s.getLongitude()))
                            .title(s.getName())
                    );
                });
            }
        });

        travelLiveDistance.setValue(0.0);
        travelLiveDistance.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                tvTravelDistance.setText(String.valueOf(aDouble));
            }
        });

        btnTakePublibike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Stop step counter
                mSensorManager.unregisterListener(stepListener);
                usingPB = 1;
                currentTravelUsePB = 1;
                Snackbar pbStarted = Snackbar.make(view, "You took a Publibike", Snackbar.LENGTH_SHORT);
                pbStarted.show();
                btnTakePublibike.setVisibility(View.INVISIBLE);
                btnLeavePublibike.setVisibility(View.VISIBLE);
            }
        });

        btnLeavePublibike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Stop step counter
                usingPB = 0;
                mSensorManager.registerListener(stepListener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);
                Snackbar pbStopped = Snackbar.make(view, "You left a Publibike", Snackbar.LENGTH_SHORT);
                pbStopped.show();
                btnTakePublibike.setVisibility(View.INVISIBLE);
                btnLeavePublibike.setVisibility(View.INVISIBLE);
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTravelButtons();
                currentTravel = initTravel();
                travelLiveDistance.setValue(currentTravel.getDistance());
                travelLiveInterestPoints.setValue(currentTravelInterestPoints.size());
                if (travelArg == null) {
                    mGoogleMap.clear();
                    allStationsLive.setValue(allStationsLive.getValue());
                }
                if (mSensorACC != null)
                    mSensorManager.registerListener(stepListener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);
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
                if (usingPB == 0) {
                    mSensorManager.unregisterListener(stepListener);
                }
                showSaveDialog();
                Snackbar travelStopped = Snackbar.make(view, "Travel stopped", Snackbar.LENGTH_SHORT);
                travelStopped.show();
                btnTakePublibike.setVisibility(View.GONE);
            }
        });

        // FOR TESTING ONLY
//        this.db.insertNewTravel(new TravelModel(1, "Test", "Test", 0.0, "Test", "Test", new ArrayList<>()));


        if (travelArg != null)
            currentTravel = travelArg.getTravel();

//                val markers = db.getInterestPointsForBalade(baladeArg.balade!!.id)
//        currentBaladeInterestPoints = ArrayList()
//        markers.forEach{
//            currentBaladeInterestPoints.add(InterestPointModel(it.id, it.name, it.lat, it.lon, it.idBalade))
//        }
//        baladeLiveInterestPoints.value = markers.size

        mapView.onCreate(savedInstanceState);
        getLocationPermissions();
        getActivityPermissions();
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

    private void getActivityPermissions() {
        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                mActivityPermissionsGranted = true;
            } else {
                Toast.makeText(ctx, "Step counter requires activity permissions", Toast.LENGTH_LONG).show();
            }
        });
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
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
            public void onResponse(Call<Stations> call, Response<Stations> responseStations) {
                allStationsLive.setValue(responseStations.body().getStations());
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
        if (this.currentTravel != null) {
            Map.drawPathOnMap(ctx, R.color.purple_200, currentTravel, mGoogleMap);
        }
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
                checkProximityAndNotify();
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
                lblChronometer.setText(R.string.chronoInitialString);
                mGoogleMap.clear();
                allStationsLive.setValue(allStationsLive.getValue());
                travelLiveDistance.setValue(0.0);
                travelLiveInterestPoints.setValue(0);
                stepLiveDate.setValue(0);
                currentTravelInterestPoints = new ArrayList<>();
                usingPB = 0;
                currentTravelUsePB = 0;
                alertDialog.dismiss();
            }
        });
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                lblChronometer.setText(R.string.chronoInitialString);
                mGoogleMap.clear();
                allStationsLive.setValue(allStationsLive.getValue());
                travelLiveDistance.setValue(0.0);
                travelLiveInterestPoints.setValue(0);
                stepLiveDate.setValue(0);
                currentTravelInterestPoints = new ArrayList<>();
                usingPB = 0;
                currentTravelUsePB = 0;
            }
        });
        alertDialog.show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "NotifChannel";
            String descriptionText = "Channel for notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("NotifChannel", name, importance);
            channel.setDescription(descriptionText);
            NotificationManager notificationManager = (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendProximityNotification(Station station) {
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(ctx, "NotifChannel")
                .setSmallIcon(R.drawable.ic_travel_tabbar_icon)
                .setContentTitle("You are close to a publibike station")
                .setContentText(String.format("%s publibike station is near by", station.getName()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notifManager.notify("Proximity Publibike", 100, notifBuilder.build());
    }

    private void checkProximityAndNotify() {
        allStationsLive.getValue().forEach(s -> {
            double d = Utils.distancePersonPoint(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), new LatLng(s.getLatitude(), s.getLongitude()));
            if (d < PROXIMITY_DISTANCE && !lastStationName.equals(s.getName()) && usingPB == 0) {
                lastStationName = s.getName();
                sendProximityNotification(s);
                btnTakePublibike.setVisibility(View.VISIBLE);
            } else if (d > PROXIMITY_DISTANCE && lastStationName.equals(s.getName())) {
                btnTakePublibike.setVisibility(View.GONE);
            }
        });
    }


    private void saveTravel() {
        double distance = Utils.getDistanceForTravel(currentTravel);
        String time = lblChronometer.getText().toString();
        // TODO: Set correct number of steps and publibike
        TravelModel tmp = new TravelModel(currentTravelId.intValue(), currentTravel.getName(), distance, time, getDate(), currentTravel.getPoints(), stepLiveDate.getValue(), currentTravelUsePB);
        db.updateTravel(tmp);
        currentTravel.getPoints().forEach(p -> db.insertNewPoint(p));
        currentTravelInterestPoints.forEach(ip -> db.insertNewInterestPoint(ip));
    }
}

