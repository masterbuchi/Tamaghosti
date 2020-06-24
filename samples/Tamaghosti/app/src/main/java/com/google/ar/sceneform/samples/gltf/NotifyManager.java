package com.google.ar.sceneform.samples.gltf;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotifyManager {

    private Context context;

    public NotifyManager(Context context) {

        this.context = context;

    }


    public void sendNotification(int id, String title, String text) {

        // https://developer.android.com/training/notify-user/build-notification

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, ArActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Opens Ar Activity by tapping on the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Tamagotchi Channel")
                .setSmallIcon(R.drawable.dragontransparent)     // Icon Logo
                .setContentTitle(title)                         // Initialize Title
                .setContentText(text)                           // Initialize Text
                .setAutoCancel(true)                            // Deletes the notification when the user taps on it
                .setContentIntent(pendingIntent)                // Adding On Tap Logic
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        showNotification(id, builder);
    }

    public void createNotificationChannel() {

        // https://developer.android.com/training/notify-user/build-notification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            // Creating a notification channel, which allows us to send notifications there
            NotificationChannel channel = new NotificationChannel("Tamagotchi Channel", name, importance);
            channel.setDescription(description);

            // Register the notificationChannel at the notificationManager
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(int id, NotificationCompat.Builder builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Notification id must be unique. Therefore we have an id that gets incremented and saved afterwards each time it is called
        notificationManager.notify(id, builder.build());
    }




}
