package com.shacham.amit.bringgmobileclientassignment;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class StatisticsActivity extends Activity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "StatisticsActivity";

    private static final int GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile = 1.6 km
    private static final int GEOFENCE_EXPIRATION_IN_MILLISECONDS = 12 * 60 * 60 * 1000; // 12 Hours
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 200;
    public static final String SERVICE_RECEIVER = "service_receiver";
    public static final String SERVICE_RESULT = "service_result";
    private static final String SHARED_PREFS_DATA = "shared_prefs_data";

    private ArrayList<String> mData;
    private GoogleApiClient mGoogleApiClient;
    private Address mWorkAddress;
    private LatLng mWorkAddressLatLng;
    private Geofence mGeofence;
    private PendingIntent mGeofencePendingIntent;
    private SharedPreferences mSharedPreferences;
    private boolean mGeofenceAdded;
    private Date mWorkDate;

    private TextView mTitleTextView;
    private ListView mStatisticsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mGeofencePendingIntent = null;

        // Get data from shared prefs, if exists:
        mSharedPreferences = getPreferences(MODE_PRIVATE);
        Set<String> set = mSharedPreferences.getStringSet(SHARED_PREFS_DATA, null);
        mData = (set == null) ? new ArrayList<String>() : new ArrayList<>(set);

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

    private void initListAdapter() {
        ArrayList<DayStatistics> statisticsList = new ArrayList<>();
        for (String workDay : mData) {
            String[] dataSplit = workDay.split(";");
            try {
                Date date = StatisticsListAdapter.DATE_FORMAT.parse(dataSplit[0]);
                DayStatistics dayStatistics = new DayStatistics(date, Long.parseLong(dataSplit[1]));
                statisticsList.add(dayStatistics);
            } catch (ParseException e) {
                Log.e(TAG, "Bad date format: " + dataSplit[0]);
            }
        }

        StatisticsListAdapter customAdapter = new StatisticsListAdapter(this, R.layout.statistics_list_item, statisticsList);
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
        checkForPermission();
    }

    private void checkForPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addGeofences();
                } else {
                    Toast.makeText(this, "App can't show statistics without location permission", Toast.LENGTH_SHORT).show();
                    exitApp();
                }
            }
        }
    }

    private void addGeofences() {
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent());
        } catch (SecurityException securityException) {
            Log.e(TAG, "You need to use ACCESS_FINE_LOCATION for geofences");
        }
    }

    private void exitApp() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
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

        ResultsReceiver callback = new ResultsReceiver();
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        intent.putExtra(SERVICE_RECEIVER, callback);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public class ResultsReceiver implements Parcelable {
        private int mResult;

        public void onReceiveResult(Bundle data) {
            mResult = data.getInt(SERVICE_RESULT);
            handleGeofenceResult(mResult);
        }

        protected ResultsReceiver() {
        }

        public final Creator<ResultsReceiver> CREATOR = new Creator<ResultsReceiver>() {
            @Override
            public ResultsReceiver createFromParcel(Parcel in) {
                return new ResultsReceiver();
            }

            @Override
            public ResultsReceiver[] newArray(int size) {
                return new ResultsReceiver[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mResult);
        }
    }

    public void handleGeofenceResult(int result) {
        switch (result) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                mWorkDate = new Date();
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                if (mWorkDate != null) {
                    String workDate = StatisticsListAdapter.DATE_FORMAT.format(mWorkDate);
                    String totalWorkTime = String.valueOf(getTotalWorkTimeInHours(mWorkDate, new Date()));
                    mData.add(workDate + ";" + totalWorkTime);
                    saveDataToSharedPrefs();
                }
                break;
        }
    }

    private void saveDataToSharedPrefs() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Set<String> set = new HashSet<>(mData);
        editor.putStringSet(SHARED_PREFS_DATA, set);
        editor.apply();
    }

    private long getTotalWorkTimeInHours(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        return minutes / 60;
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
