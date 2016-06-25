package com.shacham.amit.bringgmobileclientassignment;

import android.app.Activity;
import android.content.Intent;
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
    public static final String SHARED_PREFS_WORK_ADDRESS_LAT_LNG = "shared_prefs_work_address_lat_lng";

    private Address mAddress;

    private EditText mWorkAddressEditText;
    private Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initListeners();
    }

    private void initViews() {
        mWorkAddressEditText = (EditText) findViewById(R.id.work_address_edit_text);
        mSubmitButton = (Button) findViewById(R.id.work_address_submit_button);
    }

    private void initListeners() {
        mSubmitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.work_address_submit_button:
                LatLng latLng = getLocationFromEditText();
                if (latLng == null) {
                    Toast.makeText(this, "Invalid work address", Toast.LENGTH_SHORT).show();
                } else  {
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
            if (addresses == null) {
                return null;
            }
            mAddress = addresses.get(0);
            results = new LatLng(mAddress.getLatitude(), mAddress.getLongitude());
        } catch (IOException e) {
            return null;
        }

        return results;
    }

    private void startStatisticsActivity(LatLng latLng) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra(SHARED_PREFS_WORK_ADDRESS_LAT_LNG, latLng);
        intent.putExtra(SHARED_PREFS_WORK_ADDRESS, mAddress);
        startActivity(intent);
    }
}
