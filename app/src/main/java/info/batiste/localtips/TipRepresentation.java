package info.batiste.localtips;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

/**
 * Created by batiste on 17.11.16.
 */


interface TipLoadedListener {
    void tipLoaded(TipRepresentation tip);
}


public class TipRepresentation implements ValueEventListener {

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://localtips-149515.appspot.com");

    public TipRepresentation(String key, GeoLocation location, Marker marker, TipLoadedListener listener) {
        this.key = key;
        this.location = location;
        this.marker = marker;
        this.listener = listener;
        DatabaseReference ref = mDatabase.child("tips").child(key);
        ref.addValueEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot snapshot) {
        System.out.println("Got the data");
        HashMap tiphash = (HashMap) snapshot.getValue();
        System.out.println(tiphash);
        this.tip = new Tip(
                (String) tiphash.get("description"),
                (Double) tiphash.get("lat"),
                (Double) tiphash.get("lng"),
                (String) tiphash.get("image"));

        listener.tipLoaded(this);

        if(tip.image != null && tip.image != "") {
            StorageReference imageRef = storageRef.child("images").child(tip.image);
            final long ONE_MEGABYTE = 1024 * 1024;
            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Log.d("Image loaded", tip.image);
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    listener.tipLoaded(TipRepresentation.this);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d("Image loading error", tip.image);
                }
            });
        }

        //imageRef.get
    }
    @Override
    public void onCancelled(DatabaseError firebaseError) {

    }

    String key;
    Marker marker;
    GeoLocation location;
    Tip tip;
    TipLoadedListener listener;
    Bitmap bitmap;

}
