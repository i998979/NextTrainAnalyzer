package to.epac.factorycraft.nexttrainanalyzer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentSender;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    /**
     * Get the station's display id from its id
     *
     * @param id Id of the station
     * @return Display name of the station
     */
    public static String getStationName(String id) {
        if (id.equals("ADM")) return "金鐘";
        if (id.equals("EXC")) return "會展";
        if (id.equals("HUH")) return "紅磡";
        if (id.equals("MKK")) return "旺角東";
        if (id.equals("KOT")) return "九龍塘";
        if (id.equals("TAW")) return "大圍";
        if (id.equals("SHT")) return "沙田";
        if (id.equals("FOT")) return "火炭";
        if (id.equals("RAC")) return "馬場";
        if (id.equals("UNI")) return "大學";
        if (id.equals("TAP")) return "大埔墟";
        if (id.equals("TWO")) return "太和";
        if (id.equals("FAN")) return "粉嶺";
        if (id.equals("SHS")) return "上水";
        if (id.equals("LOW")) return "羅湖";
        if (id.equals("LMC")) return "落馬洲";

        if (id.equals("WKS")) return "烏溪沙";
        if (id.equals("MOS")) return "馬鞍山";
        if (id.equals("HEO")) return "恆安";
        if (id.equals("TSH")) return "大水坑";
        if (id.equals("SHM")) return "石門";
        if (id.equals("CIO")) return "第一城";
        if (id.equals("STW")) return "沙田圍";
        if (id.equals("CKT")) return "車公廟";
        if (id.equals("TAW")) return "大圍";
        if (id.equals("HIK")) return "顯徑";
        if (id.equals("DIH")) return "鑽石山";
        if (id.equals("KAT")) return "啟德";
        if (id.equals("SUW")) return "宋皇臺";
        if (id.equals("TKW")) return "土瓜灣";
        if (id.equals("HOM")) return "何文田";
        if (id.equals("HUH")) return "紅磡";
        if (id.equals("ETS")) return "尖東";
        if (id.equals("AUS")) return "柯士甸";
        if (id.equals("NAC")) return "南昌";
        if (id.equals("MEF")) return "美孚";
        if (id.equals("TWW")) return "荃灣西";
        if (id.equals("KSR")) return "錦上路";
        if (id.equals("YUL")) return "元朗";
        if (id.equals("LOP")) return "朗屏";
        if (id.equals("TIS")) return "天水圍";
        if (id.equals("SIH")) return "兆康";
        if (id.equals("TUM")) return "屯門";

        if (id.equals("HOK")) return "香港";
        if (id.equals("KOW")) return "九龍";
        if (id.equals("OLY")) return "奧運";
        if (id.equals("NAC")) return "南昌";
        if (id.equals("LAK")) return "茘景";
        if (id.equals("TSY")) return "青衣";
        if (id.equals("SUN")) return "欣澳";
        if (id.equals("TUC")) return "東涌";


        if (id.equals("NOP")) return "北角";
        if (id.equals("QUB")) return "鰂魚涌";
        if (id.equals("YAT")) return "油塘";
        if (id.equals("TIK")) return "調景嶺";
        if (id.equals("TKO")) return "將軍澳";
        if (id.equals("HAH")) return "坑口";
        if (id.equals("POA")) return "寶琳";
        if (id.equals("LHP")) return "康城";

        if (id.equals("HOK")) return "香港";
        if (id.equals("KOW")) return "九龍";
        if (id.equals("TSY")) return "青衣";
        if (id.equals("AIR")) return "機場";
        if (id.equals("AWE")) return "博覽館";

        return "紅磡";
    }

    /**
     * Get stations with NextTrain data available
     *
     * @param context     Context of the application
     * @param stationList List of all stations' name
     * @return List of the stations that has NextTrain data available
     */
    public static List<Station> getValidStations(Context context, List<Station> stationList) {
        List<Station> list = new ArrayList<>();

        // Get lines' array
        int lineId = context.getResources().getIdentifier("lines", "array", context.getPackageName());
        String[] lines = context.getResources().getStringArray(lineId);

        // Loop through all lines
        for (String line : lines) {
            // Get stations' array
            int stationId = context.getResources().getIdentifier(line.toLowerCase() + "_stations", "array", context.getPackageName());
            String[] stations = context.getResources().getStringArray(stationId);

            // Loop through all stations in the list
            for (Station station : stationList) {
                String id = station.getId();
                // Loop through all stations in the array
                for (String sta : stations) {
                    // If the station in list was found in the array, means this station has NextTrain data
                    if (id.equals(sta)) list.add(station);
                }
            }
        }

        return list;
    }

    /**
     * Get which line is the specified station is at
     *
     * @param context Context of the application
     * @param station Station id want to check
     * @return Id of the line
     */
    public static String getStationInLine(Context context, String station) {
        // Get lines' array
        int arrayId = context.getResources().getIdentifier("lines", "array", context.getPackageName());
        String[] lines = context.getResources().getStringArray(arrayId);

        // Loop through all lines
        for (String line : lines) {
            // Get stations' array
            int staId = context.getResources().getIdentifier(line.toLowerCase() + "_stations", "array", context.getPackageName());
            String[] stations = context.getResources().getStringArray(staId);

            // Loop through all stations
            for (String sta : stations) {
                // If the station name match, return the line
                if (sta.equalsIgnoreCase(station)) return line;
            }
        }

        return null;
    }

    /**
     * Get id of specified data in the specified array
     *
     * @param context Context of the application
     * @param array   Name of the array
     * @param string  Data want to find in the array
     * @return Id of the data
     */
    public static int getArrayIdFromString(Context context, String array, String string) {
        // Get the specified array
        int arrayId = context.getResources().getIdentifier(array.toLowerCase(), "array", context.getPackageName());
        String[] arr = context.getResources().getStringArray(arrayId);

        // Loop through the whole array
        for (int i = 0; i < arr.length; i++) {
            // If the element is what we want
            if (arr[i].equals(string))
                // Return the id
                return i;
        }

        return -1;
    }

    /**
     * Check whether the ScanService is running or not
     *
     * @param context Context of the application
     * @return IS ScanService running or not
     */
    public static boolean isScanning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ScanService.class.getName().equals(service.service.getClassName()))
                return true;
        }

        return false;
    }

    /**
     * Load station coordinates from json
     *
     * @param context Context of the application
     */
    public static void loadStationCoordinates(Context context) {
        MainActivity.stations.clear();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open("hrStations.json")));
            String data = "";
            String line;
            while ((line = br.readLine()) != null) {
                data += line;
            }

            JSONObject jsonObject = new JSONObject(data);
            JSONArray stations = jsonObject.getJSONArray("stations");

            for (int i = 0; i < stations.length(); i++) {
                JSONObject jsonObject1 = stations.getJSONObject(i);
                String id = jsonObject1.getString("alias");
                String name = jsonObject1.getString("nameEN");
                String coordinate = jsonObject1.getString("coordinate");

                double lat = Double.parseDouble(coordinate.split(",")[0]);
                double lng = Double.parseDouble(coordinate.split(",")[1]);

                Station station = new Station(name, id, lat, lng);
                MainActivity.stations.add(station);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
     * <p>
     * Prompt and ask user to enable location
     *
     * @param activity Activity to run this
     */
    public static void enableLocation(Activity activity) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build());
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(activity, 1002);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }
}
