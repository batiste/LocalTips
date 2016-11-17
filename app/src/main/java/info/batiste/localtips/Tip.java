package info.batiste.localtips;

/**
 * Created by batiste on 16.11.16.
 */

public class Tip {

    public Tip() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Tip(String description, Double lat, Double lng, String image) {
        this.description = description;
        this.image = image;
        this.lat = lat;
        this.lng = lng;
    }

    public String description;
    public Double lat;
    public Double lng;

    public String image;

}
