package to.epac.factorycraft.nexttrainanalyzer;

import static android.content.Context.NOTIFICATION_SERVICE;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.dnAdapter;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.lastUpdate;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.lineSelected;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.noData;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.pref;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.rawData;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.result;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.showRawData;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.upAdapter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataFetcher extends AsyncTask {
    private final int TRAIN_FOUND_ID = 2;

    private WeakReference<Context> context;

    private String sys_time = "無";

    private String data_in_raw = "";

    private ArrayList<Train> upTrainData0 = new ArrayList<>();
    private ArrayList<Train> dnTrainData0 = new ArrayList<>();

    public DataFetcher(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        int resId = context.get().getResources().getIdentifier(lineSelected.toLowerCase() + "_stations", "array", context.get().getPackageName());
        String[] stations = context.get().getResources().getStringArray(resId);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String ISOdate = df.format(new Date());

        upTrainData0.clear();
        dnTrainData0.clear();

        URL url;
        int i = 0;
        do {
            String receivedData = "";
            try {
                if (!pref.getBoolean("line_mode", true))
                    url = new URL("https://rt.data.gov.hk/v1/transport/mtr/getSchedule.php?" +
                            "line=" + lineSelected + "&sta=" + pref.getString("selected_station", "ADM") + "&lang=en");
                else
                    url = new URL("https://rt.data.gov.hk/v1/transport/mtr/getSchedule.php?" +
                            "line=" + lineSelected + "&sta=" + stations[i] + "&lang=en");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = br.readLine()) != null) {
                    receivedData += line;
                }

                JSONObject jsonObject = new JSONObject(receivedData);
                String status = jsonObject.getString("status");
                String message = jsonObject.getString("message");
                String url0 = "";
                try {
                    url0 = jsonObject.getString("url");
                } catch (Exception e) {
                }
                String curr_time = jsonObject.getString("curr_time");
                String sys_time = jsonObject.getString("sys_time");
                String isdelay = jsonObject.getString("isdelay");

                JSONObject jsonObject2 = jsonObject.getJSONObject("data");

                this.sys_time = sys_time;

                JSONObject jsonObject3;
                if (!pref.getBoolean("line_mode", true))
                    jsonObject3 = jsonObject2.getJSONObject(lineSelected + "-" + pref.getString("selected_station", "ADM"));
                else
                    jsonObject3 = jsonObject2.getJSONObject(lineSelected + "-" + stations[i]);

                String curr_time2 = jsonObject3.getString("curr_time");
                String sys_time2 = jsonObject3.getString("sys_time");

                JSONArray DIR = null;
                for (int k = 0; k < 2; k++) {
                    try {
                        // HUH has no UP trains
                        // TUM has no DN trains, this will result a JSONException and will be caught in catch block
                        if (k == 0) DIR = jsonObject3.getJSONArray("UP");
                        if (k == 1) DIR = jsonObject3.getJSONArray("DOWN");

                        for (int j = 0; j < DIR.length(); j++) {
                            JSONObject jsonObject4 = DIR.getJSONObject(j);
                            String ttnt = jsonObject4.getString("ttnt");
                            String valid = jsonObject4.getString("valid");
                            String plat = jsonObject4.getString("plat");
                            String time = jsonObject4.getString("time");
                            String source = jsonObject4.getString("source");
                            String dest = jsonObject4.getString("dest");
                            String seq = jsonObject4.getString("seq");
                            String timetype = "";
                            try {
                                timetype = jsonObject4.getString("timetype");
                            } catch (Exception e) {
                            }
                            String route = "";
                            try {
                                route = jsonObject4.getString("route");
                            } catch (Exception e) {
                            }

                            Train train;
                            if (!pref.getBoolean("line_mode", true))
                                train = new Train(k == 0 ? "UP" : "DN", pref.getString("selected_station", "ADM"), seq, time, dest, plat, ttnt, timetype, route);
                            else
                                train = new Train(k == 0 ? "UP" : "DN", stations[i], seq, time, dest, plat, ttnt, timetype, route);

                            if (k == 0)
                                upTrainData0.add(train);
                            else
                                dnTrainData0.add(train);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            data_in_raw = data_in_raw + receivedData + "\n\n";
            i++;

            if (!pref.getBoolean("line_mode", true)) break;
            if (pref.getBoolean("line_mode", true) && i >= stations.length) break;

        } while (i < stations.length);

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        // TODO - sort by station name here

        MainActivity.upTrainData.clear();
        MainActivity.dnTrainData.clear();
        MainActivity.upTrainData.addAll(upTrainData0);
        MainActivity.dnTrainData.addAll(dnTrainData0);

        upAdapter.notifyDataSetChanged();
        dnAdapter.notifyDataSetChanged();

        if (MainActivity.upTrainData.isEmpty() && MainActivity.dnTrainData.isEmpty() && !showRawData.isChecked())
            noData.setVisibility(View.VISIBLE);
        else
            noData.setVisibility(View.GONE);

        lastUpdate.setText("最後更新：" + sys_time);

        rawData.setText(data_in_raw);

        String msg = "";
        for (Train train : MainActivity.upTrainData) {
            if (train.getDest().equals(pref.getString("selected_dest", "ADM")))
                msg += "車站：" + Utils.getStationName(train.getStation()) + " 月台：" + train.getPlat() + " 時間：" + train.getTime() + "\n";
        }
        for (Train train : MainActivity.dnTrainData) {
            if (train.getDest().equals(pref.getString("selected_dest", "ADM")))
                msg += "車站：" + Utils.getStationName(train.getStation()) + " 月台：" + train.getPlat() + " 時間：" + train.getTime() + "\n";
        }

        if (!msg.isEmpty()) {
            result.setText("已找到往 " + Utils.getStationName(pref.getString("selected_dest", "ADM")) + " 列車");

            NotificationChannel channel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel(String.valueOf(TRAIN_FOUND_ID),
                        "Next Train Analyzer", NotificationManager.IMPORTANCE_HIGH);
            }

            NotificationManager notificationManager = (NotificationManager) context.get().getSystemService(NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context.get(), String.valueOf(TRAIN_FOUND_ID))
                    .setSmallIcon(R.drawable.sp1900_wr_nobgrd)
                    .setContentTitle("已找到往 " + Utils.getStationName(pref.getString("selected_dest", "ADM")) + " 列車")
                    .setContentText(msg)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    //.setContentTitle("Next Train Analyzer")
                    //.setContentText(contentTitle)
                    //.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setAutoCancel(true);

            Intent mIntent = new Intent(context.get(), MainActivity.class);
            PendingIntent contentIntent;
            contentIntent = PendingIntent.getActivity(context.get(),
                    0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(contentIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                notificationManager.createNotificationChannel(channel);

            Notification notification = builder.build();
            notificationManager.notify(TRAIN_FOUND_ID, notification);
        } else
            result.setText("沒有資料");
    }
}
