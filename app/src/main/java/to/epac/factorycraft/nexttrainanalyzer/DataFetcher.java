package to.epac.factorycraft.nexttrainanalyzer;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.NOTIFICATION_SERVICE;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.check_mode;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.dest_selected;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.dn_adapter;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.dn_train_datas;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.last_update;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.line_selected;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.no_data;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.raw_data;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.result;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.show_raw_data;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.station_selected;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.up_adapter;
import static to.epac.factorycraft.nexttrainanalyzer.MainActivity.up_train_datas;

public class DataFetcher extends AsyncTask {
    private final int TRAIN_FOUND_ID = 2;

    private Context context;

    private String sys_time = "無";

    private String data_in_raw = "";

    private ArrayList<Train> t_up_train_datas = new ArrayList<>();
    private ArrayList<Train> t_dn_train_datas = new ArrayList<>();

    public DataFetcher(Context context) {
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        int resId = context.getResources().getIdentifier(line_selected.toLowerCase() + "_stations", "array", context.getPackageName());
        String[] stations = context.getResources().getStringArray(resId);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String ISOdate = df.format(new Date());

        t_up_train_datas.clear();
        t_dn_train_datas.clear();

        URL url;
        int i = 0;
        do {
            String received_data = "";
            try {

                if (check_mode.getCheckedRadioButtonId() == R.id.station_mode)
                    url = new URL("https://rt.data.gov.hk/v1/transport/mtr/getSchedule.php?" +
                            "line=" + line_selected + "&sta=" + station_selected + "&lang=en");
                else
                    url = new URL("https://rt.data.gov.hk/v1/transport/mtr/getSchedule.php?" +
                            "line=" + line_selected + "&sta=" + stations[i] + "&lang=en");

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    if (line == null) break;
                    received_data = received_data + line;
                }

                JSONObject jsonObject = new JSONObject(received_data);
                String status = jsonObject.getString("status");
                String message = jsonObject.getString("message");
                String curr_time = jsonObject.getString("curr_time");
                String sys_time = jsonObject.getString("sys_time");
                String isdelay = jsonObject.getString("isdelay");
                JSONObject jsonObject2 = jsonObject.getJSONObject("data");

                this.sys_time = sys_time;

                JSONObject jsonObject3;
                if (check_mode.getCheckedRadioButtonId() == R.id.station_mode)
                    jsonObject3 = jsonObject2.getJSONObject(line_selected + "-" + station_selected);
                else
                    jsonObject3 = jsonObject2.getJSONObject(line_selected + "-" + stations[i]);

                String curr_time2 = jsonObject3.getString("curr_time");
                String sys_time2 = jsonObject3.getString("sys_time");

                JSONArray DIR = null;
                for (int k = 0; k < 2; k++) {

                    try {
                        // HUH has no UP trains
                        // TUM has no DN trains, this will result a JSONException and will be catched in catch block
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

                            Train train;
                            if (k == 0) {
                                if (check_mode.getCheckedRadioButtonId() == R.id.station_mode)
                                    train = new Train("UP", station_selected, seq, time, dest, plat, ttnt);
                                else
                                    train = new Train("UP", stations[i], seq, time, dest, plat, ttnt);

                                t_up_train_datas.add(train);
                            }
                            else {
                                if (check_mode.getCheckedRadioButtonId() == R.id.station_mode)
                                    train = new Train("DN", station_selected, seq, time, dest, plat, ttnt);
                                else {
                                    train = new Train("DN", stations[i], seq, time, dest, plat, ttnt);
                                }
                                t_dn_train_datas.add(train);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            data_in_raw = data_in_raw + received_data + "\n\n";
            i++;

            if (check_mode.getCheckedRadioButtonId() == R.id.station_mode) break;
            if (check_mode.getCheckedRadioButtonId() == R.id.line_mode && i >= stations.length) break;

        } while (i < stations.length);

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        // TODO - sort by station name here

        up_train_datas.clear();
        dn_train_datas.clear();
        up_train_datas.addAll(t_up_train_datas);
        dn_train_datas.addAll(t_dn_train_datas);

        up_adapter.notifyDataSetChanged();
        dn_adapter.notifyDataSetChanged();

        if (up_train_datas.isEmpty() && dn_train_datas.isEmpty() && !show_raw_data.isChecked()) no_data.setVisibility(View.VISIBLE);
        else no_data.setVisibility(View.GONE);

        last_update.setText("最後更新：" + sys_time);

        raw_data.setText(data_in_raw);

        String msg = "";
        for(Train train : up_train_datas) {
            if (train.getDest().equals(dest_selected))
                msg += "車站：" + Utils.getStationName(train.getStation()) + " 月台：" + train.getPlat() + " 時間：" + train.getTime() + "\n";
        }
        for(Train train : dn_train_datas) {
            if (train.getDest().equals(dest_selected))
                msg += "車站：" + Utils.getStationName(train.getStation()) + " 月台：" + train.getPlat() + " 時間：" + train.getTime() + "\n";
        }

        if (!msg.isEmpty()) {
            result.setText("已找到往 " + Utils.getStationName(dest_selected) + " 列車");

            NotificationChannel channel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel(String.valueOf(TRAIN_FOUND_ID),
                        "Next Train Analyzer", NotificationManager.IMPORTANCE_HIGH);
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, String.valueOf(TRAIN_FOUND_ID))
                    .setSmallIcon(R.drawable.sp1900_wr_nobgrd)
                    .setContentTitle("已找到往 " + Utils.getStationName(dest_selected) + " 列車")
                    .setContentText(msg)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    //.setContentTitle("Next Train Analyzer")
                    //.setContentText(contentTitle)
                    //.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setAutoCancel(true);

            Intent mIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(channel);
            }
            Notification notification = builder.build();
            notificationManager.notify(TRAIN_FOUND_ID, notification);
        }
        else
            result.setText("沒有資料");
    }
}
