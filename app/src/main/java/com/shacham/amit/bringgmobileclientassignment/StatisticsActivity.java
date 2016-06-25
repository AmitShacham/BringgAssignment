package com.shacham.amit.bringgmobileclientassignment;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

public class StatisticsActivity extends Activity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "StatisticsActivity";

    private static final int GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile = 1.6 km
    private static final int GEOFENCE_EXPIRATION_IN_MILLISECONDS = 12 * 60 * 60 * 1000; // 12 Hours

    private ArrayList<DayStatistics> mData;
    private GoogleApiClient mGoogleApiClient;
    private Address mWorkAddress;
    private LatLng mWorkAddressLatLng;
    private Geofence mGeofence;
    private PendingIntent mGeofencePendingIntent;
    private SharedPreferences mSharedPreferences;
    private boolean mGeofenceAdded;

    private TextView mTitleTextView;
    private ListView mStatisticsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mGeofencePendingIntent = null;
        mSharedPreferences = getPreferences(MODE_PRIVATE);

        Intent intent = getIntent();
        mWorkAddress = intent.getParcelableExtra(MainActivity.WORK_ADDRESS);
        mWorkAddressLatLng = intent.getParcelableExtra(MainActivity.WORK_ADDRESS_LAT_LNG);
        mGeofenceAdded = false;

        initViews();
        setScreenTitle();
        initListAdapter();
        initGooglePlayServices();
        populateGeofenceList();
    }

    private void initViews() {
        mTitleTextView = (TextView) findViewById(R.id.statistics_title_text_view);
        mStatisticsListView = (ListView) findViewById(R.id.statistics_list_view);
    }

    private void setScreenTitle() {
        mTitleTextView.setText(getString(R.string.statistics_title, mWorkAddress.getAddressLine(0)));
    }

    // TODO: Data is empty
    private void initListAdapter() {
        mData = new ArrayList<>();
        DayStatistics dayStatistics = new DayStatistics(new Date(), new Date(), new Date());
        mData.add(dayStatistics);
        dayStatistics = new DayStatistics(new Date(), new Date(), new Date());
        mData.add(dayStatistics);
        dayStatistics = new DayStatistics(new Date(), new Date(), new Date());
        mData.add(dayStatistics);

        StatisticsListAdapter customAdapter = new StatisticsListAdapter(this, R.layout.statistics_list_item, mData);
        mStatisticsListView.setAdapter(customAdapter);
    }

    private void populateGeofenceList() {
        mGeofence = new Geofence.Builder()
                .setRequestId(mWorkAddress.getAddressLine(0))
                .setCircularRegion(mWorkAddressLatLng.latitude, mWorkAddressLatLng.longitude, GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private void initGeoFencing() {
        if (mGeofenceAdded) {
            return;
        }

        mGeofenceAdded = true;
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent());
        } catch (SecurityException securityException) {
            Log.e(TAG, "You need to use ACCESS_FINE_LOCATION for geofences");
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(mGeofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Google Play Services
     **/
    private void initGooglePlayServices() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        initGeoFencing();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }
}
