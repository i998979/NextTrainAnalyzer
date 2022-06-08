package to.epac.factorycraft.nexttrainanalyzer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentSender;
import android.util.Log;

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

public class Utils {

    public static String getStationName(String name) {
        if (name.equals("ADM")) return "金鐘";
        if (name.equals("EXC")) return "會展";
        if (name.equals("HUH")) return "紅磡";
        if (name.equals("MKK")) return "旺角東";
        if (name.equals("KOT")) return "九龍塘";
        if (name.equals("TAW")) return "大圍";
        if (name.equals("SHT")) return "沙田";
        if (name.equals("FOT")) return "火炭";
        if (name.equals("RAC")) return "馬場";
        if (name.equals("UNI")) return "大學";
        if (name.equals("TAP")) return "大埔墟";
        if (name.equals("TWO")) return "太和";
        if (name.equals("FAN")) return "粉嶺";
        if (name.equals("SHS")) return "上水";
        if (name.equals("LOW")) return "羅湖";
        if (name.equals("LMC")) return "落馬洲";

        if (name.equals("WKS")) return "烏溪沙";
        if (name.equals("MOS")) return "馬鞍山";
        if (name.equals("HEO")) return "恆安";
        if (name.equals("TSH")) return "大水坑";
        if (name.equals("SHM")) return "石門";
        if (name.equals("CIO")) return "第一城";
        if (name.equals("STW")) return "沙田圍";
        if (name.equals("CKT")) return "車公廟";
        if (name.equals("TAW")) return "大圍";
        if (name.equals("HIK")) return "顯徑";
        if (name.equals("DIH")) return "鑽石山";
        if (name.equals("KAT")) return "啟德";
        if (name.equals("SUW")) return "宋皇臺";
        if (name.equals("TKW")) return "土瓜灣";
        if (name.equals("HOM")) return "何文田";
        if (name.equals("HUH")) return "紅磡";
        if (name.equals("ETS")) return "尖東";
        if (name.equals("AUS")) return "柯士甸";
        if (name.equals("NAC")) return "南昌";
        if (name.equals("MEF")) return "美孚";
        if (name.equals("TWW")) return "荃灣西";
        if (name.equals("KSR")) return "錦上路";
        if (name.equals("YUL")) return "元朗";
        if (name.equals("LOP")) return "朗屏";
        if (name.equals("TIS")) return "天水圍";
        if (name.equals("SIH")) return "兆康";
        if (name.equals("TUM")) return "屯門";

        if (name.equals("HOK")) return "香港";
        if (name.equals("KOW")) return "九龍";
        if (name.equals("OLY")) return "奧運";
        if (name.equals("NAC")) return "南昌";
        if (name.equals("LAK")) return "茘景";
        if (name.equals("TSY")) return "青衣";
        if (name.equals("SUN")) return "欣澳";
        if (name.equals("TUC")) return "東涌";


        if (name.equals("NOP")) return "北角";
        if (name.equals("QUB")) return "鰂魚涌";
        if (name.equals("YAT")) return "油塘";
        if (name.equals("TIK")) return "調景嶺";
        if (name.equals("TKO")) return "將軍澳";
        if (name.equals("HAH")) return "坑口";
        if (name.equals("POA")) return "寶琳";
        if (name.equals("LHP")) return "康城";

        if (name.equals("HOK")) return "香港";
        if (name.equals("KOW")) return "九龍";
        if (name.equals("TSY")) return "青衣";
        if (name.equals("AIR")) return "機場";
        if (name.equals("AWE")) return "博覽館";

        return "紅磡";
    }

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

    public static boolean isScanning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ScanService.class.getName().equals(service.service.getClassName()))
                return true;
        }

        return false;
    }

    public static void loadLocation(Context context) {
        MainActivity.stations.clear();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open("hrStations.json")));
            String data = "";
            String line;
            while ((line = br.readLine()) != null) {
                Log.e("code", line);
                data += line;
            }

            JSONObject jsonObject = new JSONObject(data);
            JSONArray stations = jsonObject.getJSONArray("stations");

            for (int i = 0; i < stations.length(); i++) {
                JSONObject jsonObject1 = stations.getJSONObject(i);
                String id = jsonObject1.getString("alias");
                String name = jsonObject1.getString("nameEN");
                String coordinate = jsonObject1.getString("coordinate");

                double lon = Double.parseDouble(coordinate.split(",")[0]);
                double lat = Double.parseDouble(coordinate.split(",")[1]);

                Station station = new Station(name, id, lon, lat);
                MainActivity.stations.add(station);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enableLocation(Activity activity) {
        LocationRequest mLocationRequest = new LocationRequest();
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
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(activity, 1002);
                            } catch (IntentSender.SendIntentException e) {
                            } catch (ClassCastException e) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }
}
