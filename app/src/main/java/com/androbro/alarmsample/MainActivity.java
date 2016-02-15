package com.androbro.alarmsample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private Context context;
    private MyWebRequestReceiver receiver;
    private SharedPreferences sharedpreferences;
    private TextView stationIdTV;
    private TextView observationTimeTV;
    private TextView weatherTV;
    private TextView temperatureTV;
    private TextView windTV;

    private static final String PREFERENCE_FILE = "myprefs" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;//it's a good practice to store current activity in a context variable

        stationIdTV = (TextView) findViewById(R.id.station_id);
        observationTimeTV = (TextView) findViewById(R.id.observation_time);
        weatherTV = (TextView) findViewById(R.id.weather);
        temperatureTV = (TextView) findViewById(R.id.temperature);
        windTV = (TextView) findViewById(R.id.wind);

        sharedpreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);

        //when the app first starts sometimes it takes couple of seconds to parse data, fill up the
        //shared prefs file and set up the values. So here I put default values stating that information
        //is being updated. After that the AlarmManager schedules the service to run and update data.
        String stationId = sharedpreferences.getString("station", "Updating..");
        String observationTime = sharedpreferences.getString("observation", "Updating..");
        String weather = sharedpreferences.getString("weather", "Updating..");
        String tempString = sharedpreferences.getString("temperature", "Updating..");
        String windString = sharedpreferences.getString("wind", "Updating..");

        stationIdTV.setText(stationId);
        observationTimeTV.setText(observationTime);
        weatherTV.setText(weather);
        temperatureTV.setText(tempString);
        windTV.setText(windString);

        //registering receiver dynamically:
        //also, it will be possible for the reciever to register on_boot_complete broadcasts
        //and start the service once the device completes booting(in case user restarts it)
        //I didn't implement restarting service after the device has been rebooted. It's just a
        //note for myself to consider in future:)
        IntentFilter filter = new IntentFilter(MyWebRequestReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyWebRequestReceiver();
        registerReceiver(receiver, filter);

        //setting up AlarmManager to schedule a service to run every hour through intent to receiver
        //that will trigger that service
            Intent alarm = new Intent(this.context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, alarm, 0);
            //setting up Alarm service to run every 5 sec
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                    5000, pendingIntent);
    }


    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    //create inner receiver class
    public class MyWebRequestReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.androbro.alarmsample.action.PROCESS_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(PROCESS_RESPONSE)){

                Weather receivedWeather = (Weather) intent.getSerializableExtra("object");

                String stationId = receivedWeather.getStationId();
                String observationTime = receivedWeather.getObservationTime();
                String weather = receivedWeather.getWeather();
                String tempString = receivedWeather.getTemperature();
                String windString = receivedWeather.getWind();

                //setting up values right away after the service completes parsing data
                //for the first time. And also checking if the returned object's field is equal to
                //null. If it is, then something happened with the connection, so we don't need
                //to store it in SharedPrefs file. We check just stationId, because if it's null
                //then others will be null too.
                if (stationId == null){
                    Toast.makeText(getApplicationContext(), "No internet connection, last updated data loaded",
                            Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    stationIdTV.setText(stationId);
                    observationTimeTV.setText(observationTime);
                    weatherTV.setText(weather);
                    temperatureTV.setText(tempString);
                    windTV.setText(windString);
                }

                Toast.makeText(getApplicationContext(), "Data updated!", Toast.LENGTH_SHORT).show();

                //next, data will be updated in shared preferences every hour.
                sharedpreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString("station",stationId);
                editor.putString("observation", observationTime);
                editor.putString("weather", weather);
                editor.putString("temperature", tempString);
                editor.putString("wind", windString);
                editor.apply();
            }
        }
    }
}
