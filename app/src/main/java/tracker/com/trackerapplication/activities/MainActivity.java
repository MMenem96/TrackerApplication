package tracker.com.trackerapplication.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Date;

import tracker.com.trackerapplication.R;
import tracker.com.trackerapplication.services.LocationTracker;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 999;
    private EditText etMinutes;
    private Button btnStart, btnStop;
    LocationTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Init all views
        initViews();
    }

    private void initViews() {
        etMinutes = findViewById(R.id.et_interval);
        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);
        btnStop = findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(this);


        if (checkPermissions()) {
            //Everything is there to start the service.
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        } else {
            //Needs to request the permission.
            btnStart.setEnabled(false);
            btnStop.setEnabled(false);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                if (isMinutesValid()) {
                    tracker = new LocationTracker(MainActivity.this, Long.valueOf(etMinutes.getText().toString().trim()) * 1000 * 60);
                    btnStop.setEnabled(true);
                    btnStart.setEnabled(false);
                    Toast.makeText(MainActivity.this, "Started Tracking!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_stop:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                tracker.stopUsingLocation();
                Toast.makeText(MainActivity.this, "Stopped Tracking!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean isMinutesValid() {
        if (!TextUtils.isEmpty(etMinutes.getText())) {
            try {
                int num = Integer.parseInt(etMinutes.getText().toString());
                return true;
            } catch (NumberFormatException e) {
                etMinutes.setError("Invalid!");
                return false;
            }
        } else {
            etMinutes.setError("Empty!");
            return false;
        }
    }

    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled.
                // receive empty arrays.
                Toast.makeText(this, "Permission Request is cancelled", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission is granted", Toast.LENGTH_SHORT).show();
                //start service
                btnStart.setEnabled(true);
            } else {
                //Permission is denied
                Toast.makeText(this, "Permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tracker != null) {
            tracker.stopUsingLocation();
        }
    }

    public void submitLocationToServer(Location location) {
// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.3/tracker/trackerscript.php?text=" + new Date() + " Latitude: " + location.getLatitude()
                + " Longitude: " + location.getLongitude();

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.e("Response", "Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.getMessage());
                Toast.makeText(MainActivity.this, "That didn't work!", Toast.LENGTH_SHORT).show();
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
