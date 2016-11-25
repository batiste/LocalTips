package info.batiste.localtips;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import java.util.Hashtable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, TipLoadedListener {

    GoogleMap map;
    Marker marker;
    LatLng latlng;
    GeoQuery geoQuery;
    static final int PERMISSIONS_REQUEST_GPS = 18;
    DatabaseReference mDatabase;
    StorageReference storageRef = null;
    Hashtable <String, TipRepresentation> locations = null;
    TipListAdapter tiplist_adapter;
    boolean firstCameraUpdate = true;
    TipRepresentation selectedTip;
    Marker selectedMarker;
    ProgressDialog progress;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "Ready");
        map = googleMap;
        LatLng ZURICH = new LatLng(47.3769, 8.54169);
        map.setOnMarkerClickListener(this);
        marker = map.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_us))
                        .position(ZURICH)
        );
    }

    @Override
    public boolean onMarkerClick(final Marker clickedMarker) {
        Log.d("onMarkerClick", "click");
        if (!marker.equals(clickedMarker))
        {
            String key = (String) clickedMarker.getTag();
            if(locations.containsKey(key)) {
                TipRepresentation representation = locations.get(key);
                if(selectedTip != null) {
                    selectedTip.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }
                selectedTip = representation;
                if(representation.tip != null) {
                    LatLng _latlng = new LatLng(representation.location.latitude, representation.location.longitude);
                    map.animateCamera(CameraUpdateFactory.newLatLng(_latlng));
                    selectedTip.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    Log.d("onMarkerClick", representation.tip.description);
                } else {
                    Log.d("onMarkerClick", "tip not loaded yet");
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Tips around you");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        MapFragment fragmentmap = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        fragmentmap.getMapAsync(this);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading Location");
        progress.setMessage("Waiting location...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        setSupportActionBar(toolbar);
        locations = new Hashtable <String, TipRepresentation> ();

        FloatingActionButton takephoto = (FloatingActionButton) findViewById(R.id.newtip);
        takephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), NewTipActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("EXTRA_LATLNG", latlng);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://localtips-149515.appspot.com");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tiplist_adapter = new TipListAdapter(locations);
        ListView tiplist = (ListView) findViewById(R.id.tiplist);
        tiplist.setAdapter(tiplist_adapter);

        tiplist.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                TipRepresentation tip = tiplist_adapter.getItem(position);
                if(selectedTip != null) {
                    selectedTip.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }
                selectedTip = tip;
                selectedTip.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                LatLng _latlng = new LatLng(tip.location.latitude, tip.location.longitude);
                map.animateCamera(CameraUpdateFactory.newLatLng(_latlng));
            }
        });

        FloatingActionButton center = (FloatingActionButton) findViewById(R.id.centermap);
        center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(latlng == null) {
                    return;
                }
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17.0f));
            }
        });

        //center.setOnClickListener();

        // Use GPS location data
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_GPS);

        } else {
            // Acquire a reference to the system Location Manager
            Log.d("onCreate", "requestLocationUpdates");
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void tipLoaded(TipRepresentation tip) {
        tiplist_adapter.updateData(locations);
        runOnUiThread(new Runnable() {
            public void run() {
                tiplist_adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Acquire a reference to the system Location Manager
                    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Log.d("onRequestPermissions", "requestLocationUpdates");

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    // Geoloc stuff
    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            //makeUseOfNewLocation(location);
            Log.d("onLocationChanged", location.toString());
            latlng = new LatLng(location.getLatitude(), location.getLongitude());

            if(geoQuery == null) {
                // GeoFire
                setupGeoQuery();
            } else {
                geoQuery.setCenter(new GeoLocation(latlng.latitude, latlng.longitude));
            }

            if (map != null) {
                Log.d("onLocationChanged", "Map ready");
                marker.setPosition(latlng);
                if(selectedTip != null) {
                    return;
                }
                if(firstCameraUpdate) {
                    progress.hide();
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17.0f));
                    firstCameraUpdate = false;
                } else {
                    map.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                }
            }
        }

        public void setupGeoQuery() {
            // GeoFire
            GeoFire geoloc = new GeoFire(mDatabase.child("geolocation"));
            geoQuery = geoloc.queryAtLocation(new GeoLocation(latlng.latitude, latlng.longitude), 0.6); // km
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {

                    System.out.println(
                            String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                    if(locations.containsKey(key)) {
                        Log.d("locationListener", "Already there");
                        return;
                    }

                    LatLng position = new LatLng(location.latitude, location.longitude);

                    Marker mark = map.addMarker(
                            new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                    .position(position));
                    mark.setTag(key);

                    TipRepresentation newRepr = new TipRepresentation(key, location, mark, MainActivity.this);
                    locations.put(key, newRepr);
                    tiplist_adapter.updateData(locations);

                }
                @Override
                public void onKeyExited(String key) {
                    // this will never be called as the object is destroyed
                    if(locations.containsKey(key)) {
                        TipRepresentation tip = locations.get(key);
                        tip.marker.remove();
                        locations.remove(key);
                        tiplist_adapter.updateData(locations);
                    }
                    System.out.println(String.format("Key %s is no longer in the search area", key));
                }
                @Override
                public void onGeoQueryError(DatabaseError error) {
                    System.err.println("There was an error with this query: " + error);
                }
                @Override
                public void onGeoQueryReady() {
                    System.out.println("All initial data has been loaded and events have been fired!");
                }
                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                }
            });
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Location", Integer.toString(status));
            Log.d("Location", extras.toString());
        }

        public void onProviderEnabled(String provider) {
            Log.d("onProviderEnabled", "1");
        }

        public void onProviderDisabled(String provider) {
            Log.d("onProviderDisabled", "1");
        }
    };

}
