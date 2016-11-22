package info.batiste.localtips;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by batiste on 17.11.16.
 */


interface TipLoadedListener {
    void tipLoaded(TipRepresentation tip);
}


public class TipRepresentation implements ValueEventListener {

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();;

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
    }
    @Override
    public void onCancelled(DatabaseError firebaseError) {

    }

    String key;
    Marker marker;
    GeoLocation location;
    Tip tip;
    TipLoadedListener listener;

}
