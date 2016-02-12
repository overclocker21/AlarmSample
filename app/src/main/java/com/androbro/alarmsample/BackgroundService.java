package com.androbro.alarmsample;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by user on 2/11/2016.
 */
public class BackgroundService extends Service{

    //adding boolean control variable to check if the service was running or not
    private boolean isRunning;
    private Context context;
    private Thread backgroundThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.context = this;
        this.isRunning = false;
        this.backgroundThread = new Thread(myTask);
    }

    private Runnable myTask = new Runnable() {

        @Override
        public void run() {

            Intent broadcastIntent = new Intent();
            Weather weatherObject = new Weather();
            broadcastIntent.setAction(MainActivity.MyWebRequestReceiver.PROCESS_RESPONSE);
            //broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

            NodeList list = ((new WeatherHttpClient()).returnNodes());

            if (list != null && list.getLength() > 0) {
                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) list.item(i);
                        if (element.getNodeName().equals("station_id")) {

                            String stationId = element.getTextContent();
                            weatherObject.setStationId(stationId);
                            Log.i("STATION_ID", stationId);

                        } else if (element.getNodeName().equals("observation_time")) {

                            String observationTime = element.getTextContent();
                            weatherObject.setObservationTime(observationTime);
                            Log.i("OBSERVATION_TIME", observationTime);

                        } else if (element.getNodeName().equals("weather")) {

                            String weather = element.getTextContent();
                            weatherObject.setWeather(weather);
                            Log.i("WEATHER", weather);


                        } else if (element.getNodeName().equals("temperature_string")) {

                            String tempString = element.getTextContent();
                            weatherObject.setTemperature(tempString);
                            Log.i("TEMPERATURE", tempString);


                        } else if (element.getNodeName().equals("wind_string")) {

                            String windString = element.getTextContent();
                            weatherObject.setWind(windString);
                            Log.i("WIND", windString);
                        }
                    }
                }
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable("object", weatherObject);
            broadcastIntent.putExtras(bundle);

            sendBroadcast(broadcastIntent);

            Log.i("APPLICATION", "BACKGROUND SERVICE IS RUNNING");
            stopSelf();//stop service when it's done parsing
        }
    };

    @Override
    public void onDestroy() {
        this.isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning){
            this.isRunning = true;
            this.backgroundThread.start();
        }
        //we want to redeliver our intent if the app was forcestopped:
        return START_REDELIVER_INTENT;
    }
}





















