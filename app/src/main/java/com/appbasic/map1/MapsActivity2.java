package com.appbasic.map1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager = null;
    EditText startplace, endplace;
    Button searchbutton,searchbutton2;
    GoogleApiClient mGoogleApiClient;
    static Context mContext;
    RequestQueue requestqueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(), "oncreate", Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_maps);
        mContext = this;

        requestqueue= Volley.newRequestQueue(this);

        startplace = (EditText) findViewById(R.id.startplace);
        endplace = (EditText) findViewById(R.id.endplace);
        searchbutton = (Button) findViewById(R.id.searchbutton);
        searchbutton2=(Button)findViewById(R.id.searchbutton2);

        searchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String searchlocation = startplace.getText().toString();
                List<Address> addressList = null;
                Geocoder gncd = new Geocoder(mContext);
                try {
                    addressList = gncd.getFromLocationName(searchlocation, 1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Address address=addressList.get(0);
                LatLng latlng=new LatLng(address.getLatitude(),address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latlng).title(searchlocation));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                Toast.makeText(getApplicationContext(),address.getLatitude()+" "+address.getLongitude(),Toast.LENGTH_LONG).show();

            }
        });

        searchbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startplacestr = startplace.getText().toString();
                String endplacestr = endplace.getText().toString();

                GetPolyLines(startplacestr, endplacestr);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void GetPolyLines(String place1, String place2) {
        Toast.makeText(getApplicationContext(), "working", Toast.LENGTH_SHORT).show();
        String Origin = place1;
        String Dest = place2;
        String URL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + Origin + "&destination=" + Dest;
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, null, new com.android.volley.Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("onResponce", "---");

                try {
                    JSONArray routes = response.getJSONArray("routes");


                    JSONObject first = routes.getJSONObject(0);


                    JSONArray legs = first.getJSONArray("legs");

                    Log.d("legs", "---" + legs.length());


                    JSONObject second = legs.getJSONObject(0);


                    JSONArray steps = second.getJSONArray("steps");


                    //Polyline polyline;


                    PolylineOptions polylineOptions = new PolylineOptions();

                    polylineOptions.width(10);

                    polylineOptions.color(Color.parseColor("#f53c45"));


                    for (int i = 0; i < steps.length(); i++) {

                        JSONObject obj = steps.getJSONObject(i);
                        JSONObject line = obj.getJSONObject("polyline");
                        String lineStr = line.getString("points");
                        // polyList.add(lineStr);
                        polylineOptions.addAll(PolyUtil.decode(lineStr));
                    }


                    //steps.length();

                    // Toast.makeText(getApplicationContext(),""+steps.length(),Toast.LENGTH_SHORT).show();
                    mMap.clear();
                    mMap.addPolyline(polylineOptions);


                } catch (JSONException e) {


                    e.printStackTrace();
                }


            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ccccc", "nnnnn");
            }
        });


        //MySingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);
// Adding request to request queue
        //  AppController.getInstance().addToRequestQueue(objectRequest);
        requestqueue.add(objectRequest);

    }
    @Override
    protected void onResume() {
        initializeLocationManager();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 50, MapsActivity2.this);
        super.onResume();
    }

    private void initializeLocationManager() {

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Toast.makeText(getApplicationContext(), "onMapReady", Toast.LENGTH_SHORT).show();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
               // buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
           // buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "onLocationChanged", Toast.LENGTH_SHORT).show();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("C.P");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        float zoomLevel = 16.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));



      /*  //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }*/
    }/*  //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }*/

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
