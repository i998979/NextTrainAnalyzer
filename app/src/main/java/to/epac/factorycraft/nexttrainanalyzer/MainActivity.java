package to.epac.factorycraft.nexttrainanalyzer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("NextTrainAnalyzer", MODE_PRIVATE);

        bgrdSearch = findViewById(R.id.bgrdSearch);
        line = findViewById(R.id.line);
        lastUpdate = findViewById(R.id.lastUpdate);
        refresh = findViewById(R.id.refresh);

        checkMode = findViewById(R.id.checkMode);
        lineMode = findViewById(R.id.lineMode);
        stationMode = findViewById(R.id.stationMode);
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
                        // Clear search result to avoid confusion
                        result.setText("載入中...");

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

                        // Retrieve the data
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
}