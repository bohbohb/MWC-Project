package com.usi.mwc.justmove.ui;

import static com.usi.mwc.justmove.utils.Utils.getDate;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.api.PublibikeAPIClient;
import com.usi.mwc.justmove.api.PublibikeAPIInterface;
import com.usi.mwc.justmove.database.DatabaseHandler;
import com.usi.mwc.justmove.listeners.StepCounterListener;
import com.usi.mwc.justmove.model.PointModel;
import com.usi.mwc.justmove.model.Station;
import com.usi.mwc.justmove.model.Stations;
import com.usi.mwc.justmove.model.TravelModel;
import com.usi.mwc.justmove.utils.Map;
import com.usi.mwc.justmove.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements OnMapReadyCallback {

    // Navigation arguments
    private MainFragmentArgs travelArg;

    // Constants
    private final double PROXIMITY_DISTANCE = 20.0;

    // Database handler
    private DatabaseHandler db;

    // UI components
    private Chronometer lblChronometer;
    private Button btnStart;
    private Button btnStop;
    private FloatingActionButton btnTakePublibike;
    private FloatingActionButton btnLeavePublibike;
    private TextView tvTravelDistance;
    private MapView mapView;
    private GoogleMap mGoogleMap;
    private TextView tvStepView;

    // Live data
    private final MutableLiveData<Double> travelLiveDistance = new MutableLiveData<>();
    private final MutableLiveData<List<Station>> allStationsLive = new MutableLiveData<>();
    private final MutableLiveData<Integer> stepLiveDate = new MutableLiveData<>();

    // Current travel variables
    private Long currentTravelId;
    private Long startPointId;
    private Location lastLocation;
    private TravelModel currentTravel;
    private boolean travelStarted;
    private Integer usingPB = 0;
    private Integer currentTravelUsePB = 0;
    private String lastStationName = "";
    private String startDate;

    // Permissions variables
    private Boolean mLocationPermissionsGranted = false;
    private Boolean mActivityPermissionsGranted = false;

    private Context ctx;
    private AlertDialog alertDialog;
    private NotificationManagerCompat notifManager;
    private Long timeWhenStopped = 0L;

    // Activity sensors variables
    private Sensor mSensorACC;
    private SensorManager mSensorManager;
    private SensorEventListener stepListener;

    @RequiresApi(api = VERSION_CODES.Q)
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
        stepLiveDate.observe(getViewLifecycleOwner(), stepInt -> tvStepView.setText(String.valueOf(stepInt)));

        mSensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
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
        allStationsLive.observe(getViewLifecycleOwner(), stations -> stations.forEach(s -> {
            if (mGoogleMap != null) {   // Add publibike markers on the map
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(s.getLatitude(), s.getLongitude()))
                        .title("Publibike")
                        .snippet(s.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                );
            }
        }));

        travelLiveDistance.setValue(0.0);
        travelLiveDistance.observe(getViewLifecycleOwner(), aDouble -> tvTravelDistance.setText(String.format("%s km", aDouble)));

        // Button to take a publibike
        btnTakePublibike.setOnClickListener(view -> {
            mSensorManager.unregisterListener(stepListener);
            usingPB = 1;
            currentTravelUsePB = 1;
            Snackbar pbStarted = Snackbar.make(view, "You took a Publibike", Snackbar.LENGTH_SHORT);
            pbStarted.show();
            btnTakePublibike.setVisibility(View.INVISIBLE);
            btnLeavePublibike.setVisibility(View.VISIBLE);
        });

        // Button to leave publibike
        btnLeavePublibike.setOnClickListener(view -> {
            usingPB = 0;
            mSensorManager.registerListener(stepListener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);
            Snackbar pbStopped = Snackbar.make(view, "You left a Publibike", Snackbar.LENGTH_SHORT);
            pbStopped.show();
            btnTakePublibike.setVisibility(View.INVISIBLE);
            btnLeavePublibike.setVisibility(View.INVISIBLE);
        });

        // Button to start a travel
        btnStart.setOnClickListener(view -> {
            toggleTravelButtons();
            currentTravel = initTravel();
            travelLiveDistance.setValue(currentTravel.getDistance());
            startDate = getDate();
            if (travelArg == null) {    // No travel loaded on the map
                mGoogleMap.clear();
                allStationsLive.setValue(allStationsLive.getValue());
            }
            if (mSensorACC != null)
                mSensorManager.registerListener(stepListener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);
            travelStarted = true;
            initChronometer(lblChronometer);
        });

        // Button to stop a travel
        btnStop.setOnClickListener(view -> {
            toggleTravelButtons();
            timeWhenStopped = lblChronometer.getBase() - SystemClock.elapsedRealtime(); // Time before Stop
            travelStarted = false;
            lblChronometer.stop();
            if (usingPB == 0) {
                mSensorManager.unregisterListener(stepListener);
            }
            showSaveDialog();
            Snackbar travelStopped = Snackbar.make(view, "Travel stopped", Snackbar.LENGTH_SHORT);
            travelStopped.show();
            btnTakePublibike.setVisibility(View.GONE);
        });

        // Load old travel
        if (travelArg != null)
            currentTravel = travelArg.getTravel();

        mapView.onCreate(savedInstanceState);

        // Request permissions
        getLocationPermissions();
        getActivityPermissions();

        // Start retrieving location
        startLocationManager();

        getStations(); // Get all publibike stations
        return root;
    }

    /**
     * Toggle the visibility of UI buttons
     */
    private void toggleTravelButtons() {
        if ((btnStart.getVisibility() == View.VISIBLE) && (btnStop.getVisibility() == View.INVISIBLE)) {
            btnStart.setVisibility(View.INVISIBLE);
            btnStop.setVisibility(View.VISIBLE);
        } else {
            btnStart.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Initialise the chronometer to track a travel
     * @param lblChronometer chronometer that has to be initialized
     */
    private void initChronometer(Chronometer lblChronometer) {
        lblChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        lblChronometer.start();
        lblChronometer.setOnChronometerTickListener(chronometer -> chronometer.setText(Utils.ticksToHHMMSS(chronometer)));
        lblChronometer.setBase(SystemClock.elapsedRealtime());
        lblChronometer.setText(R.string.chronoInitialString);
    }

    /**
     * Initialise new travel
     * @return new travel or empty travel existing in the database
     */
    private TravelModel initTravel() {
        TravelModel firstOrDefaultTravel = getFirstOrDefaultTravel();
        setStartingPoint();
        return firstOrDefaultTravel;
    }

    /**
     * Sets the starting point of a travel
     */
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

    /**
     * Initialise the Google Map
     */
    private void initMap() {
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    /**
     * Request location permissions
     */
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

    /**
     * Request activity permissions
     */
    @RequiresApi(api = VERSION_CODES.Q)
    private void getActivityPermissions() {
        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                mActivityPermissionsGranted = true;
            } else {
                System.out.println("Step counter requires activity permissions");
            }
        });
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
    }


    /**
     * Get default travel from db or create new
     * @return first empty travel or default
     */
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

    /**
     * Retrieve all Publibike stations
     */
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

    /**
     * Starts the location manager with 1s of update time
     */
    private void startLocationManager() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE);
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

    /**
     * Display the Google Map once ready
     * @param googleMap Google Map to display
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setMyLocationEnabled(true);
        allStationsLive.setValue(allStationsLive.getValue());
        if (this.currentTravel != null) {
            Map.drawPathOnMap(ctx, R.color.purple_200, currentTravel, mGoogleMap);
        }
        mapView.setVisibility(View.VISIBLE);
    }


    /**
     * location updater
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Location oldLocation = lastLocation;
            lastLocation = location;
            if (travelStarted) {    // A travel is started
                PointModel startingPoint = new PointModel(lastLocation.getLatitude(), lastLocation.getLongitude(), startPointId.intValue(), currentTravelId.intValue());
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
            if (mGoogleMap != null) { // Move Google Map camera
                Map.moveCamera(mGoogleMap, new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    };

    /**
     * Displays dialog to save a travel
     */
    private void showSaveDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.save_dialog_layout, null);

        EditText nameText = (EditText) dialogView.findViewById(R.id.tvTravelName);
        Button saveBtn = dialogView.findViewById(R.id.saveDialogBtn);

        saveBtn.setOnClickListener(view -> {
            currentTravel.setName(nameText.getText().toString());
            saveTravel();
            resetCurrentTravel();
            alertDialog.dismiss();
        });
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();
        alertDialog.setOnDismissListener(dialogInterface -> resetCurrentTravel());
        alertDialog.show();
    }

    /**
     * Restore default parameters for the current travel
     */
    private void resetCurrentTravel() {
        lblChronometer.setText(R.string.chronoInitialString);
        mGoogleMap.clear();
        allStationsLive.setValue(allStationsLive.getValue());
        travelLiveDistance.setValue(0.0);
        stepLiveDate.setValue(0);
        usingPB = 0;
        currentTravelUsePB = 0;
    }

    /**
     * Create notification channel
     */
    private void createNotificationChannel() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            String name = "NotifChannel";
            String descriptionText = "Channel for notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("NotifChannel", name, importance);
            channel.setDescription(descriptionText);
            NotificationManager notificationManager = (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Send notification when near publibike station
     * @param station near by station
     */
    private void sendProximityNotification(Station station) {
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(ctx, "NotifChannel")
                .setSmallIcon(R.drawable.ic_travel_tabbar_icon)
                .setContentTitle("You are close to a publibike station")
                .setContentText(String.format("%s publibike station is near by", station.getName()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notifManager.notify("Proximity Publibike", 100, notifBuilder.build());
    }

    /**
     * Verify proximity with publibike station and send notification if needed
     */
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


    /**
     * Save new travel
     */
    private void saveTravel() {
        double distance = Utils.getDistanceForTravel(currentTravel);
        String time = lblChronometer.getText().toString();
        TravelModel tmp = new TravelModel(currentTravelId.intValue(), currentTravel.getName(), distance, time, getDate(), startDate, currentTravel.getPoints(), stepLiveDate.getValue(), currentTravelUsePB);
        db.updateTravel(tmp);
        currentTravel.getPoints().forEach(p -> db.insertNewPoint(p));
    }
}

