package info.batiste.localtips;


/**
 * Created by batiste on 16.11.16.
 */

public class Tip {

    public Tip() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Tip(String description, String category, Double lat, Double lng, String image, Long creationDate) {
        this.description = description;
        this.image = image;
        this.lat = lat;
        this.lng = lng;
        this.category = category;
        this.creationDate = creationDate;
    }

    public String description;
    public String category;
    public Double lat;
    public Double lng;
    public String image;
    public Long creationDate;

}
