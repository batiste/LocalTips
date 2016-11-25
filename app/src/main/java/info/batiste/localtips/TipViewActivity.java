package info.batiste.localtips;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class TipViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    LatLng latlng;
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("View tip");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "Ready");
        map = googleMap;
        if(latlng == null) {
            latlng = new LatLng(47.3769, 8.54169); // Zurich
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17.0f));
        marker = map.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_us))
                        .position(latlng)
        );
    }

}
