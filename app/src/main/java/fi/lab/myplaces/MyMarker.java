package fi.lab.myplaces;

import com.google.android.gms.maps.model.LatLng;

public class MyMarker {
    String title;
    LatLng latLng;

    public MyMarker(String title, LatLng latLng) {
        this.title = title;
        this.latLng = latLng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

}
