package com.shacham.amit.bringgmobileclientassignment;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationResult;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitionsIS";
    public static final String ENTERED = "Entered";
    public static final String EXITED = "Exited";
    public static final String UNKNOWN_TRANSITION = "Unknown Transition";
    public static final String RESPONSE = "response";

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofence error occurred");
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences);
            Log.i(TAG, geofenceTransitionDetails);

            sendDataBackToActivity(geofenceTransitionDetails);
        } else {
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private String getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences) {
        String geofenceTransitionString = getTransitionString(geofenceTransition);
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private void sendDataBackToActivity(String data) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(StatisticsActivity.SERVICE_RESULT, data);
        sendBroadcast(broadcastIntent);
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return ENTERED;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return EXITED;
            default:
                return UNKNOWN_TRANSITION;
        }
    }
}
