package to.epac.factorycraft.nexttrainanalyzer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ScanService extends Service {
    public static final int FOREGROUND_ID = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(fetch_data, 5000);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(FOREGROUND_ID))
                .setSmallIcon(R.drawable.sp1900_wr_nobgrd)
                .setContentTitle("Next Train Analyzer")
                .setContentText("背景搜尋進行中...")
                .setPriority(Notification.PRIORITY_MAX);

        Intent mIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_ID + "", "Next Train Analyzer", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(FOREGROUND_ID + "");
        }

        Log.d("tagg", "startForeground, id: " + FOREGROUND_ID);
        startForeground(FOREGROUND_ID, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(fetch_data);
        stopForeground(true);

        super.onDestroy();
    }

    private Handler handler = new Handler();
    private Runnable fetch_data = new Runnable() {
        public void run() {
            DataFetcher process = new DataFetcher(ScanService.this);
            process.execute();

            handler.postDelayed(this, 10 * 1000);
        }
    };
}
