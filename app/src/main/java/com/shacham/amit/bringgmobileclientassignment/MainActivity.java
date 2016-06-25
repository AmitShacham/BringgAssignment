package com.shacham.amit.bringgmobileclientassignment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    public static final String SHARED_PREFS_WORK_ADDRESS = "shared_prefs_work_address";
    public static final String WORK_ADDRESS = "work_address";
    public static final String WORK_ADDRESS_LAT_LNG = "work_address_lat_lng";

    private Address mAddress;
    private String mAddressString;

    private EditText mWorkAddressEditText;
    private Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initListeners();

        if (doesWorkAddressExist()) {
            fillWorkAddressEditText();
        }
    }

    private void initViews() {
        mWorkAddressEditText = (EditText) findViewById(R.id.work_address_edit_text);
        mSubmitButton = (Button) findViewById(R.id.work_address_submit_button);
    }

    private void initListeners() {
        mSubmitButton.setOnClickListener(this);
    }

    private boolean doesWorkAddressExist() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String defaultValue = "Default";
        mAddressString = sharedPreferences.getString(SHARED_PREFS_WORK_ADDRESS, defaultValue);

        return !mAddressString.equals(defaultValue);
    }

    private void fillWorkAddressEditText() {
        mWorkAddressEditText.setText(mAddressString);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.work_address_submit_button:
                LatLng latLng = getLocationFromEditText();
                if (latLng == null) {
                    Toast.makeText(this, "Invalid work address", Toast.LENGTH_SHORT).show();
                } else  {
                    saveWorkAddress();
                    startStatisticsActivity(latLng);
                }
                break;
        }
    }

    public LatLng getLocationFromEditText() {
        Geocoder coder = new Geocoder(this);
        List<Address> addresses;
        LatLng results;

        try {
            addresses = coder.getFromLocationName(mWorkAddressEditText.getText().toString(), 5);
            if (addresses == null || addresses.size() == 0) {
                return null;
            }
            mAddress = addresses.get(0);
            results = new LatLng(mAddress.getLatitude(), mAddress.getLongitude());
        } catch (IOException e) {
            return null;
        }

        return results;
    }

    private void saveWorkAddress() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFS_WORK_ADDRESS, mAddress.getAddressLine(0));
        editor.apply();
    }

    private void startStatisticsActivity(LatLng latLng) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra(WORK_ADDRESS_LAT_LNG, latLng);
        intent.putExtra(WORK_ADDRESS, mAddress);
        startActivity(intent);
    }
}
