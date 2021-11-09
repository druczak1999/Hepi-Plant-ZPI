package com.example.hepiplant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("AlarmTest","on receive");
        NotificationCompat.Builder builder= new NotificationCompat.Builder(context,"notifyHepiPlant")
                .setSmallIcon(R.drawable.kwiatek)
                .setContentTitle(intent.getExtras().getString("eventName"))
                .setContentText(intent.getExtras().getString("eventDescription"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify((int) intent.getExtras().getLong("eventId"),builder.build());
    }
}
