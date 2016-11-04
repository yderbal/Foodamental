package com.foodamental.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.foodamental.R;
import com.foodamental.util.AlarmReceiver;

import java.util.Calendar;

/**
 * Activité pour les alertes
 */
public class AlertPage extends Activity {
    private Switch mySwitch;
    private TextView switchStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_page);

        switchStatus = (TextView) findViewById(R.id.switchStatus);
        mySwitch = (Switch) findViewById(R.id.switch1);
        SharedPreferences sharedPrefs = getSharedPreferences("com.mobileapp.smartapplocker", MODE_PRIVATE);
        mySwitch.setChecked(sharedPrefs.getBoolean("service_status", false));

        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    switchStatus.setText("Switch is currently ON");
                    SharedPreferences.Editor editor = getSharedPreferences("com.mobileapp.smartapplocker", MODE_PRIVATE).edit();
                    editor.putBoolean("service_status", mySwitch.isChecked());
                    editor.commit();
                    createNotification(AlertPage.this);
                } else {
                    switchStatus.setText("Switch is currently OFF");
                    SharedPreferences.Editor editor = getSharedPreferences("com.mobileapp.smartapplocker", MODE_PRIVATE).edit();
                    editor.putBoolean("service_status", mySwitch.isChecked());
                    editor.commit();
                    cancelNotification();
                }

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_main_page, menu);
        return true;
    }

    /**
     * Création de l'alarme
     * @param ctx
     */
    public static void createNotification(Context ctx ) {
        /*Intent intent = new Intent(this, Courses.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        Intent intent2 = new Intent(this, Recipes.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent2 = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent2, 0);
// build notification
// the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("Attention vos produits vont périmés!!")
                .setContentText("Choisir un des deux menus")
                .setSmallIcon(R.drawable.ic_launcher1)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.common_google_signin_btn_icon_light, "Frigo", pIntent)
                .addAction(R.drawable.common_google_signin_btn_icon_light, "Recettes", pIntent2)
                .build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(notifyId, n);*/
        Calendar sevendayalarm = Calendar.getInstance();

        //sevendayalarm.add(Calendar.DATE, 7);
        int interval = 10000;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, sevendayalarm.getTimeInMillis(), interval, pendingIntent);

    }

    /**
     * Annulation de l'alarme
     */
    public void cancelNotification() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(sender);
    }
}



