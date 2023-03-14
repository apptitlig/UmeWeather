package com.matilda.umeweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Implementation of App Widget functionality.
 */
public class Weather extends AppWidgetProvider {
    private static final String MY_BUTTTON_START = "myButtonStart";

    public Weather() throws XmlPullParserException {
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            UpdateWidget(context);
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        UpdateWidget(context);
    };

    static void UpdateWidget(Context context){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather);
        ComponentName localComponentName = new ComponentName(context, Weather.class);
        try {
            String s = GetData();
            views.setTextViewText(R.id.appwidget_text, s);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, Receiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Get the layout for the App Widget and attach an on-click listener to the button
        //views = new RemoteViews(context.getPackageName(), R.layout.weather);
        views.setOnClickPendingIntent(R.id.relativelayout, pendingIntent);
        // Tell the AppWidgetManager to perform an update on the current App Widget
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    private static String GetData() throws IOException, InterruptedException {
        AtomicReference<String> returnvalue = new AtomicReference<>("Failed");
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            try {
                String urlString = "http://130.239.117.8/TFE-vadertjanst/Service1.asmx/Temp";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Read response into InputStream and parse as XML
                InputStream responseStream = conn.getInputStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(responseStream);
                doc.getDocumentElement().normalize();

                returnvalue.set(doc.getDocumentElement().getTextContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executorService.shutdown(); // close the executor and don't accept new tasks

        while (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {}
        return returnvalue.get();
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}