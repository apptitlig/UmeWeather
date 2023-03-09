package com.example.umeweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d("Matilda", "updateAppWidget");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather);
        views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context, MY_BUTTTON_START));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, Weather.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d("Matilda", "onUpdate" + appWidgetIds.length);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("Matilda", "onReceive");
        UpdateWidget(context);
    };

    private void UpdateWidget(Context context){
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
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    private String GetData() throws IOException, InterruptedException {
        AtomicReference<String> returnvalue = new AtomicReference<>("Failed");
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            try {
                Log.d("Matilda", "GetData");

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