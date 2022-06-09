package to.epac.factorycraft.nexttrainanalyzer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences pref;
    public static String lineSelected = "EAL";

    protected static ImageButton bgrdSearch;
    protected static ImageButton line;
    protected static TextView lastUpdate;
    protected static ImageButton refresh;

    protected static RadioGroup checkMode;
    protected static RadioButton lineMode;
    protected static RadioButton stationMode;
    protected static ImageButton locate;
    protected static Switch showRawData;

    protected static LinearLayout destLayout;
    protected static Spinner selectedDest;
    protected static LinearLayout stationModeLayout;
    protected static Spinner selectedStation;

    protected static TextView result;

    protected static TextView rawData;
    protected static TextView noData;

    protected static RecyclerView trainDataUp;
    protected static RecyclerView trainDataDn;
    protected static TrainAdapter upAdapter;
    protected static TrainAdapter dnAdapter;


    public static ArrayList<Train> upTrainData = new ArrayList<>();
    public static ArrayList<Train> dnTrainData = new ArrayList<>();
    public static ArrayList<Station> stations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("NextTrainAnalyzer", MODE_PRIVATE);
        lineSelected = pref.getString("selected_line", "EAL");

        bgrdSearch = findViewById(R.id.bgrdSearch);
        line = findViewById(R.id.line);
        lastUpdate = findViewById(R.id.lastUpdate);
        refresh = findViewById(R.id.refresh);

        checkMode = findViewById(R.id.checkMode);
        lineMode = findViewById(R.id.lineMode);
        stationMode = findViewById(R.id.stationMode);
        locate = findViewById(R.id.locate);
        showRawData = findViewById(R.id.showRawData);

        destLayout = findViewById(R.id.destLayout);
        selectedDest = findViewById(R.id.selectedDest);
        stationModeLayout = findViewById(R.id.stationModeLayout);
        selectedStation = findViewById(R.id.selectedStation);

        result = findViewById(R.id.result);

        rawData = findViewById(R.id.rawData);
        noData = findViewById(R.id.noData);

        trainDataUp = findViewById(R.id.trainDataUp);
        trainDataDn = findViewById(R.id.trainDataDn);


        upAdapter = new TrainAdapter(this, upTrainData);
        trainDataUp.setLayoutManager(new LinearLayoutManager(this));
        trainDataUp.setAdapter(upAdapter);

        dnAdapter = new TrainAdapter(this, dnTrainData);
        trainDataDn.setLayoutManager(new LinearLayoutManager(this));
        trainDataDn.setAdapter(dnAdapter);


        Utils.loadStationCoordinates(this);

        bgrdSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanService.class);

            if (!Utils.isScanning(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("tagg", "API-26 OREO+ startForegroundService()");
                    startForegroundService(intent);
                } else {
                    Log.d("tagg", "API-26 OREO- startService()");
                    startService(intent);
                }
            } else
                stopService(intent);

            if (Utils.isScanning(this))
                bgrdSearch.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_pause_circle_outline_black_24dp));
            else
                bgrdSearch.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_play_circle_outline_black_24dp));
        });

        line.setOnClickListener(view -> {
            AlertDialog.Builder lineDialog = new AlertDialog.Builder(this);
            lineDialog.setTitle("請選擇綫路")
                    .setItems(R.array.lines_long, (dialogInterface, i) -> {
                        result.setText("載入中...");

                        setSelectedLine(i);

                        DataFetcher process = new DataFetcher(this);
                        process.execute();
                    }).show();
        });

        refresh.setOnClickListener(view -> {
            result.setText("載入中...");

            DataFetcher process = new DataFetcher(MainActivity.this);
            process.execute();
        });

        checkMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.lineMode) {
                stationModeLayout.setVisibility(View.INVISIBLE);
                pref.edit().putBoolean("line_mode", true).apply();
            }
            if (checkedId == R.id.stationMode) {
                stationModeLayout.setVisibility(View.VISIBLE);
                pref.edit().putBoolean("line_mode", false).apply();
            }
        });

        locate.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
                }
            } else {
                // Ask user to enable GPS
                Utils.enableLocation(this);

                // Choose a valid location provider
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                String provider = LocationManager.PASSIVE_PROVIDER;
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    provider = LocationManager.NETWORK_PROVIDER;
                else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    provider = LocationManager.GPS_PROVIDER;

                // Mess with location
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                // Request for the new location
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setInterval(60000);
                locationRequest.setFastestInterval(5000);
                locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                    }
                };
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

                // Get the last location and do what we want
                String p = provider;
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                // Initialize station variables by assigning the first station from the list
                                Station closest = stations.get(0);
                                Location cLoc = new Location(p);
                                cLoc.setLatitude(closest.getLatitude());
                                cLoc.setLongitude(closest.getLongitude());

                                // Calculate distances
                                for (Station sta : Utils.getValidStations(this, stations)) {
                                    Location staLoc = new Location(sta.getId());
                                    staLoc.setLatitude(sta.getLatitude());
                                    staLoc.setLongitude(sta.getLongitude());

                                    // If the distance between current location and the station location < old one, update old one to new one
                                    if (location.distanceTo(staLoc) < location.distanceTo(cLoc)) {
                                        closest = sta;
                                        cLoc.setLatitude(staLoc.getLatitude());
                                        cLoc.setLongitude(staLoc.getLongitude());
                                    }
                                }

                                // Get the line id
                                String lne = Utils.getStationInLine(MainActivity.this, closest.getId());
                                int i = Utils.getArrayIdFromString(MainActivity.this, "lines", lne);

                                // Update the selected line
                                result.setText("載入中...");
                                setSelectedLine(i);

                                // Update selected station to the closest one
                                selectedStation.setSelection(Utils.getArrayIdFromString(MainActivity.this, lne + "_stations", closest.getId()));
                                pref.edit().putString("selected_station", closest.getId()).apply();

                                // Retrieve data
                                DataFetcher process = new DataFetcher(MainActivity.this);
                                process.execute();
                            }
                        });
            }
        });

        showRawData.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rawData.setVisibility(View.VISIBLE);
                trainDataUp.setItemViewCacheSize(View.GONE);
                trainDataDn.setItemViewCacheSize(View.GONE);

                noData.setVisibility(View.GONE);
            } else {
                rawData.setVisibility(View.GONE);

                if (upTrainData.isEmpty() && dnTrainData.isEmpty())
                    noData.setVisibility(View.VISIBLE);
                else {
                    trainDataUp.setVisibility(View.VISIBLE);
                    trainDataDn.setVisibility(View.VISIBLE);
                }
            }
        });

        selectedDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int arrayId = getResources().getIdentifier(lineSelected.toLowerCase() + "_stations", "array", getPackageName());

                pref.edit().putString("selected_dest", getResources().getStringArray(arrayId)[selectedDest.getSelectedItemPosition()]).apply();

                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        selectedStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int arrayId = getResources().getIdentifier(lineSelected.toLowerCase() + "_stations", "array", getPackageName());

                pref.edit().putString("selected_station", getResources().getStringArray(arrayId)[selectedStation.getSelectedItemPosition()]).apply();

                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Initial loading
        if (Utils.isScanning(this))
            bgrdSearch.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_pause_circle_outline_black_24dp));
        else
            bgrdSearch.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_play_circle_outline_black_24dp));

        // Get corresponding string from yml
        int drawableId = getResources().getIdentifier(lineSelected.toLowerCase() + "_line", "drawable", getPackageName());
        int colorId = getResources().getIdentifier(lineSelected.toLowerCase(), "color", getPackageName());
        int arrayId = getResources().getIdentifier(lineSelected.toLowerCase() + "_stations_long", "array", getPackageName());
        int staId = getResources().getIdentifier(lineSelected.toLowerCase() + "_stations", "array", getPackageName());
        List<String> staName = Arrays.asList(getResources().getStringArray(staId));

        // Set line image, change layout color, change spinner content
        line.setImageDrawable(AppCompatResources.getDrawable(this, drawableId));
        destLayout.setBackgroundColor(ContextCompat.getColor(this, colorId));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(arrayId));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        selectedDest.setAdapter(adapter);
        selectedDest.setSelection(staName.indexOf(pref.getString("selected_dest", "ADM")));
        selectedStation.setAdapter(adapter);
        selectedStation.setSelection(staName.indexOf(pref.getString("selected_station", "ADM")));

        checkMode.check(pref.getBoolean("line_mode", true) ? R.id.lineMode : R.id.stationMode);

        // Show loading text
        result.setText("載入中...");

        DataFetcher process = new DataFetcher(this);
        process.execute();
    }

    private void setSelectedLine(int i) {
        // Change the saved line and set the selected station to 0
        lineSelected = getResources().getStringArray(R.array.lines)[i];

        int drawableId = getResources().getIdentifier(lineSelected.toLowerCase() + "_line", "drawable", getPackageName());
        int colorId = getResources().getIdentifier(lineSelected.toLowerCase(), "color", getPackageName());
        int arrayId = getResources().getIdentifier(lineSelected.toLowerCase() + "_stations_long", "array", getPackageName());
        int array0Id = getResources().getIdentifier(lineSelected.toLowerCase() + "_stations", "array", getPackageName());

        pref.edit().putString("selected_line", lineSelected)
                .putString("selected_dest", getResources().getStringArray(array0Id)[0])
                .putString("selected_station", getResources().getStringArray(array0Id)[0]).apply();

        // Set line image, change layout color, change spinner content
        line.setImageDrawable(AppCompatResources.getDrawable(this, drawableId));
        destLayout.setBackgroundColor(ContextCompat.getColor(this, colorId));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(arrayId));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectedDest.setAdapter(adapter);
        selectedStation.setAdapter(adapter);
    }
}