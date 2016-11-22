package info.batiste.localtips;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class NewTipActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map = null;
    Marker marker = null;
    LatLng latlng = null;
    DatabaseReference mDatabase;
    EditText text = null;
    ImageView mImageView = null;
    StorageReference storageRef = null;
    File photoFile = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    File currentPhoto;
    Uri photoURI;
    static final int PERMISSIONS_REQUEST_GPS = 18;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "Ready");
        map = googleMap;
        LatLng ZURICH = new LatLng(47.3769, 8.54169);
        marker = map.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .position(ZURICH)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add a new Tip");
        setContentView(R.layout.activity_new_tip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        text = (EditText) findViewById(R.id.description);
        mImageView = (ImageView) findViewById(R.id.imageview);

        setSupportActionBar(toolbar);

        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://localtips-149515.appspot.com");
        mDatabase = FirebaseDatabase.getInstance().getReference();


        FloatingActionButton takephoto = (FloatingActionButton) findViewById(R.id.takephoto);
        takephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        FloatingActionButton save = (FloatingActionButton) findViewById(R.id.savetip);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // create
            Log.d("SaveTip", "click");
            final Tip newTip = new Tip();
            newTip.description = text.getText().toString();

            if(photoFile != null) {
                newTip.image = photoFile.getName();
            }

            if(latlng != null) {
                newTip.lat = latlng.latitude;
                newTip.lng = latlng.longitude;
            }
            DatabaseReference newRef = mDatabase.child("tips").push();
            newRef.setValue(newTip);

            if(latlng != null) {
                GeoFire geoloc = new GeoFire(mDatabase.child("geolocation"));
                geoloc.setLocation(newRef.getKey(), new GeoLocation(latlng.latitude, latlng.longitude));
            }

            finish();
            }
        });

        MapFragment map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    // Geoloc stuff
    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            //makeUseOfNewLocation(location);
            Log.d("onLocationChanged", location.toString());

            if (map != null) {
                Log.d("onLocationChanged", "Map ready");
                latlng = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 19.0f));
                marker.setPosition(latlng);
            }
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

    private void dispatchTakePictureIntent() {
        Log.d("PictureIntent", "start");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
                currentPhoto = photoFile;
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("PictureIntent", "IOERROR when creating the image file" + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "info.batiste.localtips.fileprovider",
                        photoFile);
                Log.e("PictureIntent", "photoURI: " + photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            } else {
                Log.e("PictureIntent", "Image not created");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            if (data != null) {
                // The default Android camera application returns a non-null intent
                // only when passing back a thumbnail in the returned Intent.
                // If you pass EXTRA_OUTPUT with a URI to write to, it will return a null intent
                // and the picture is in the URI that you passed in.
                Bundle extras = data.getExtras();
                if(extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    mImageView.setImageBitmap(imageBitmap);
                }
            }

            if (photoURI != null) {
                //mImageView.setImageBitmap(Bitmap.createBitmap(photoURI));
                Log.d("mImageView.setImageURI", photoURI.toString());
                mImageView.setImageURI(photoURI);
            }

            if (currentPhoto != null) {
                setPic(currentPhoto);
            }
        }
    }

    private void setPic(File photoFile) {
        // Get the dimensions of the View
        ImageView mImageView = (ImageView) findViewById(R.id.imageview);

        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        Log.d("setPic", "image view W:" + targetW);
        Log.d("setPic", "image view H:" + targetH);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        if (photoFile.exists())
            Log.d("setPic", "Image exist");
        else {
            Log.e("setPic", "Image doesn't exist");
            return;
        }

        StorageReference imagesRef = storageRef.child("images").child(photoFile.getName());
        Log.d("setPic", "imagesRef.putFile");
        imagesRef.putFile(photoURI);

        String path = photoFile.getAbsolutePath();
        Log.e("setPic", path);

        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.d("setPic", "photoW:" + photoW);
        Log.d("setPic", "photoH:" + photoH);

        // Determine how much to scale down the image
        int scaleFactor = targetW / photoW;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        //bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);

        int activityHeight = this.getWindow().getDecorView().getHeight();
        int height = (int)Math.max(activityHeight / 3.5, 250);

        Bitmap newbitMap = scaleCropToFit(bitmap, targetW, height);

        mImageView.setImageBitmap(newbitMap);
    }

    public static Bitmap scaleCropToFit(Bitmap original, int targetWidth, int targetHeight){
        //Need to scale the image, keeping the aspect ration first
        int width = original.getWidth();
        int height = original.getHeight();

        float widthScale = (float) targetWidth / (float) width;
        float heightScale = (float) targetHeight / (float) height;
        float scaledWidth;
        float scaledHeight;

        int startY = 0;
        int startX = 0;

        if (widthScale > heightScale) {
            scaledWidth = targetWidth;
            scaledHeight = height * widthScale;
            //crop height by...
            startY = (int) ((scaledHeight - targetHeight) / 2);
        } else {
            scaledHeight = targetHeight;
            scaledWidth = width * heightScale;
            //crop width by..
            startX = (int) ((scaledWidth - targetWidth) / 2);
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(original, (int) scaledWidth, (int) scaledHeight, true);

        Bitmap resizedBitmap = Bitmap.createBitmap(scaledBitmap, startX, startY, targetWidth, targetHeight);
        return resizedBitmap;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        if (!image.mkdirs()) {
            Log.e("createImageFile", "Directory not created");
        }

        return image;
    }

}

