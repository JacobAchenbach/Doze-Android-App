package cache.doze;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsMessage;

import java.util.Random;

import cache.doze.Activities.MainActivity;

/**
 * Created by Chris on 1/17/2018.
 */

public class MonitorSmsService extends Service {
    NotificationManager nMN;
    Notification.Builder runningNotification;
    NotificationCompat.Builder runningCompatNotification;
    private String[] randomSynonyms = {"Hey, you", "Circumspect!", "Alert!",
            "Attention!", "Notice!", "Heads Up!", "How Exciting!",
            "Good News!", "We Got You", ":)", "It Begins...", "\"Nice\"", "Doze is Cool and,"};

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getBaseContext();
        Intent notifIntent = new Intent(context, MainActivity.class);

        PendingIntent pi = PendingIntent.getActivity(context,0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        nMN = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        String randomMsg = randomSynonyms[new Random().nextInt(randomSynonyms.length)];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runningNotification = new Notification.Builder(this, MainActivity.CHANNEL_ID);

            runningNotification
                    .setOngoing(true)
                    .setContentTitle(randomMsg)
                    .setContentText("Doze is responding to your texts \uD83D\uDE34")
                    .setSmallIcon(R.drawable.app_icon)
                    .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .setContentIntent(pi)
                    .setAutoCancel(true);

            createChannel();

            Notification notification = runningNotification.build();
            startForeground(MainActivity.NOTIFICATION_ID, notification);
        } else {
            runningCompatNotification = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID);

            runningCompatNotification.setContentTitle(getString(R.string.app_name))
                    .setOngoing(true)
                    .setContentTitle(randomMsg)
                    .setContentText("Doze is responding to your texts \uD83D\uDE34")
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN);

            Notification notification = runningCompatNotification.build();
            startForeground(MainActivity.NOTIFICATION_ID, notification);
        }

        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver, intentFilter);

        // Let it continue running until it is stopped.
        return START_STICKY;
    }

    @TargetApi(26)
    private void createChannel(){
        NotificationChannel channel = new NotificationChannel(MainActivity.CHANNEL_ID,
                "Reply Service",
                NotificationManager.IMPORTANCE_HIGH);
        nMN.createNotificationChannel(channel);
        runningNotification.setChannelId(MainActivity.CHANNEL_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) nMN.deleteNotificationChannel(MainActivity.CHANNEL_ID);

        unregisterReceiver(receiver);
        getSharedPreferences(MainActivity.DEFAULT_PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(MainActivity.SERVICE_RUNNING, false).apply();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null || !action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))return;
            Bundle intentExtras = intent.getExtras();
            String smsBody = "";
            String address = "";
            String number = "";
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                smsBody += smsMessage.getMessageBody();
                address = smsMessage.getDisplayOriginatingAddress();
                number = smsMessage.getServiceCenterAddress();
            }

            MainActivity inst = new MainActivity();
            inst.messageReceived(address);
        }
    };

}
