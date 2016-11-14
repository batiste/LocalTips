package info.batiste.localtips;

import android.Manifest;
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
import android.widget.ImageView;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.net.Uri;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });



        // Use GPS location data

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_GPS);

        } else {
            // Acquire a reference to the system Location Manager
            Log.d("onCreate", "requestLocationUpdates");
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public final int PERMISSIONS_REQUEST_GPS = 18;

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

            // other 'case' lines to check for other
            // permissions this app might request
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

    static final int REQUEST_IMAGE_CAPTURE = 1;
    File currentPhoto;
    Uri photoURI;

    private void dispatchTakePictureIntent() {
        Log.d("PictureIntent", "start");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
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

            ImageView mImageView = (ImageView) findViewById(R.id.imageview);
            if(data != null) {
                // The default Android camera application returns a non-null intent
                // only when passing back a thumbnail in the returned Intent.
                // If you pass EXTRA_OUTPUT with a URI to write to, it will return a null intent
                // and the picture is in the URI that you passed in.
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mImageView.setImageBitmap(imageBitmap);
            }

            if(photoURI != null) {
                //mImageView.setImageBitmap(Bitmap.createBitmap(photoURI));
                Log.d("mImageView.setImageURI", photoURI.toString());
                mImageView.setImageURI(photoURI);
            }

            if(currentPhoto != null) {
                setPic(currentPhoto);
            }
        }
    }

    String mCurrentPhotoPath;

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

        if(photoFile.exists())
            Log.d("setPic", "Image exist");
        else {
            Log.e("setPic", "Image doesn't exist");
            return;
        }

        String path = photoFile.getAbsolutePath();
        Log.e("setPic", path);

        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.d("setPic", "photoW:" + photoW);
        Log.d("setPic", "photoH:" + photoH);

        // Determine how much to scale down the image
        int scaleFactor = photoW / targetW;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        mImageView.setImageBitmap(bitmap);
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
