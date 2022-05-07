package to.epac.factorycraft.nexttrainanalyzer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ImageButton bgrd_search;

    protected static ImageButton line;
    protected static String line_selected;

    protected static TextView last_update;
    ImageButton refresh;

    protected static RadioGroup check_mode;
    protected static RadioButton line_mode;
    protected static RadioButton station_mode;

    protected static Switch show_raw_data;

    LinearLayout dest_layout;
    Spinner selected_dest;
    Spinner selected_station;
    LinearLayout station_mode_layout;

    protected static String dest_selected;
    protected static String station_selected;

    protected static TextView result;

    protected static TextView no_data;
    protected static TextView raw_data;

    protected static RecyclerView train_datas_up;
    protected static RecyclerView train_datas_dn;
    protected static TrainAdapter up_adapter;
    protected static TrainAdapter dn_adapter;

    protected static ArrayList<Train> up_train_datas = new ArrayList<>();
    protected static ArrayList<Train> dn_train_datas = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bgrd_search = findViewById(R.id.bgrd_search);

        line = findViewById(R.id.line);
        line_selected = "TML";

        last_update = findViewById(R.id.last_update);
        refresh = findViewById(R.id.refresh);

        check_mode = findViewById(R.id.check_mode);
        line_mode = findViewById(R.id.line_mode);
        station_mode = findViewById(R.id.station_mode);

        show_raw_data = findViewById(R.id.show_raw_data);

        dest_layout = findViewById(R.id.dest_layout);
        selected_dest = findViewById(R.id.selected_dest);
        selected_dest.setSelection(18);
        dest_selected = "NAC";
        selected_station = findViewById(R.id.selected_station);
        selected_station.setSelection(25);
        station_selected = "SIH";
        station_mode_layout = findViewById(R.id.station_mode_layout);

        result = findViewById(R.id.result);

        no_data = findViewById(R.id.no_data);
        raw_data = findViewById(R.id.raw_data);
        train_datas_up = findViewById(R.id.train_datas_up);
        train_datas_dn = findViewById(R.id.train_datas_dn);

        up_adapter = new TrainAdapter(this, up_train_datas);
        train_datas_up.setLayoutManager(new LinearLayoutManager(this));
        train_datas_up.setAdapter(up_adapter);
        dn_adapter = new TrainAdapter(this, dn_train_datas);
        train_datas_dn.setLayoutManager(new LinearLayoutManager(this));
        train_datas_dn.setAdapter(dn_adapter);

        bgrd_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScanService.class);

                if (!isServiceRunning(ScanService.class)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Log.d("tagg", "API-26 OREO+ startForegroundService()");
                        startForegroundService(intent);
                    } else {
                        Log.d("tagg", "API-26 OREO- startService()");
                        startService(intent);
                    }
                } else {
                    stopService(intent);
                    //NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    //manager.cancel(NotificationProvider.IS_RUNNING_ID);
                }

                if (isServiceRunning(ScanService.class))
                    bgrd_search.setImageDrawable(getDrawable(R.drawable.ic_pause_circle_outline_black_24dp));
                else
                    bgrd_search.setImageDrawable(getDrawable(R.drawable.ic_play_circle_outline_black_24dp));
            }
        });

        line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder lineDialog = new AlertDialog.Builder(MainActivity.this);
                lineDialog.setTitle("請選擇綫路")
                        .setItems(R.array.lines_long, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                line_selected = getResources().getStringArray(R.array.lines)[which];

                                int drawableId = getResources().getIdentifier(line_selected.toLowerCase() + "_line", "drawable", getPackageName());
                                int colorId = getResources().getIdentifier(line_selected.toLowerCase(), "color", getPackageName());
                                int arrayId = getResources().getIdentifier(line_selected.toLowerCase() + "_stations_long", "array", getPackageName());

                                line.setImageDrawable(getDrawable(drawableId));
                                dest_layout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, colorId));
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        MainActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(arrayId));
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                selected_dest.setAdapter(adapter);
                                selected_station.setAdapter(adapter);

                                result.setText("載入中...");
                                DataFetcher process = new DataFetcher(getApplicationContext());
                                process.execute();
                            }
                        });
                lineDialog.show();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.setText("載入中...");
                DataFetcher process = new DataFetcher(MainActivity.this);
                process.execute();
            }
        });

        check_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.line_mode) {
                    station_mode_layout.setVisibility(View.INVISIBLE);
                }
                if (checkedId == R.id.station_mode) {
                    station_mode_layout.setVisibility(View.VISIBLE);
                }
            }
        });

        show_raw_data.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    raw_data.setVisibility(View.VISIBLE);
                    train_datas_up.setItemViewCacheSize(View.GONE);
                    train_datas_dn.setItemViewCacheSize(View.GONE);

                    no_data.setVisibility(View.GONE);
                } else {
                    raw_data.setVisibility(View.GONE);
                    train_datas_up.setItemViewCacheSize(View.VISIBLE);
                    train_datas_dn.setItemViewCacheSize(View.VISIBLE);

                    if (up_train_datas.isEmpty() && dn_train_datas.isEmpty())
                        no_data.setVisibility(View.VISIBLE);
                }
            }
        });

        selected_dest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int arrayId = getResources().getIdentifier(line_selected.toLowerCase() + "_stations", "array", getPackageName());
                dest_selected = getResources().getStringArray(arrayId)[selected_dest.getSelectedItemPosition()];
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        selected_station.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int arrayId = getResources().getIdentifier(line_selected.toLowerCase() + "_stations", "array", getPackageName());
                station_selected = getResources().getStringArray(arrayId)[selected_station.getSelectedItemPosition()];
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (isServiceRunning(ScanService.class))
            bgrd_search.setImageDrawable(getDrawable(R.drawable.ic_pause_circle_outline_black_24dp));
        else
            bgrd_search.setImageDrawable(getDrawable(R.drawable.ic_play_circle_outline_black_24dp));

        result.setText("載入中...");
        // TODO - try ASyncTask to do periodically
        DataFetcher process = new DataFetcher(getApplicationContext());
        process.execute();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
}
