package com.google.ar.sceneform.samples.gltf;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.List;


public class SystemWorker extends Worker {

    private Context context;

    // Background Logic Class

    // Allows us to keep decreasing the needs of the dragon even when the app is closed

    public SystemWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        // Making sure that the app isn't in the foreground
        if(!checkAppActive("com.google.ar.sceneform.samples.gltf")) {

            // Creating a new notifyManager which allows us to send notifications
            NotifyManager notifyManager = new NotifyManager(context);

            notifyManager.createNotificationChannel();

            // Creating the persistanceManager because we need to have an unique notification id for each message
            PersistenceManager persistenceManager = new PersistenceManager(context);

            int notificationId = persistenceManager.getInt("notification_id", 0);

            // Dragon name is required for sending nice messages
            String dragonName = persistenceManager.getString("dragon_name", "Error");

            int hunger = persistenceManager.getInt("hunger", 0);
            int social = persistenceManager.getInt("social", 0);
            int sleep = persistenceManager.getInt("sleep", 0);
            int fun = persistenceManager.getInt("fun", 0);

            // Background Logic

            if(hunger > 0) {
                hunger -= 10;
            }

            if(hunger < 0) {
                hunger = 0;
            }

            if(social > 0) {
                social -= 15;
            }

            if(social < 0) {
                social = 0;
            }
            if(fun < 0) {
                social = 0;
            }

            // Send notification if certain values are passed
            // We are incrementing the values, because each notification requires an unique id
            if(hunger <= 50) {

                if(hunger <= 20) {
                    // Send urgent notification
                    notifyManager.sendNotification(notificationId, dragonName + " " + context.getResources().getString(R.string.notification_hunger_title), context.getResources().getString(R.string.notification_hunger_urgent));

                } else {
                    // Send normal notification
                    notifyManager.sendNotification(notificationId, dragonName + " " +  context.getResources().getString(R.string.notification_hunger_title), context.getResources().getString(R.string.notification_hunger_normal));
                }
                notificationId++;

            }

            if(social <= 50) {
                if(social <= 20) {
                    // Send urgent notification
                    notifyManager.sendNotification(notificationId, dragonName + " " + context.getResources().getString(R.string.notification_social_title), context.getResources().getString(R.string.notification_social_urgent));
                } else {
                    // Send normal notification
                    notifyManager.sendNotification(notificationId, dragonName + " " + context.getResources().getString(R.string.notification_social_title), context.getResources().getString(R.string.notification_social_normal));
                }
                notificationId++;
            }
            if (fun <= 50){
                if(fun <= 20){
                    // Send urgend notification
                    notifyManager.sendNotification(notificationId, dragonName + " " + "is bored" , "Better play a little with your dragon");
                } else {
                    //Send normal notification
                    notifyManager.sendNotification(notificationId, dragonName + " " + "is bored", "Wanna play a little with your dragon?");
                }
                notificationId++;
            }
            if(sleep >= 100) {
                sleep = 100;
            } else {
                sleep += 10;
            }

            // Save values
            persistenceManager.saveInt("hunger", hunger);
            persistenceManager.saveInt("social", social);
            persistenceManager.saveInt("sleep", sleep);
            persistenceManager.saveInt("fun", fun);

            // Saving the unique id. So that we can start with the newest value again when the app is triggering this logic
            persistenceManager.saveInt("notification_id", notificationId);

            Log.i("Dragon Update", "Updated Needs");
            Log.i("Dragon Update", "Hunger: " + hunger);
            Log.i("Dragon Update", "Social: " + social);
            Log.i("Dragon Update", "Sleep: " + sleep);
            Log.i("Dragon Update", "Fun: " + fun);



        } else {

            Log.i("Dragon Update", "App is active!");

        }

        return Result.success();
    }

    /**
     * Checks if the app is active or not
     * We didn't rely on the lifecycle method onDestroy(), because I read multiple articles that said that android might struggle to
     * call onDestroy when the app is closed rapidly or the suddenly restarts. With this function however we get the list of running processes
     * and check by the unique name of the app if it is running or not
     * https://stackoverflow.com/questions/26879951/how-to-know-if-my-application-is-in-foreground-or-background-android/30882260
     * @param appPackageName
     * @return
     */
    private boolean checkAppActive(String appPackageName) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = appPackageName;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                //                Log.e("app",appPackageName);
                return true;
            }
        }
        return false;

    }



}


