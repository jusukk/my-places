package fi.lab.myplaces;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private SearchView searchView;
    private GoogleMap map;
    private Marker clickMarker;
    private Button buttonSave;
    private Button buttonDelete;
    private EditText editTextName;
    private LatLng markerPosition;

    public static List<MyMarker> myPlaces = new ArrayList<>();

    DBHandler dbHandler;

    // When map is ready
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            map = googleMap;
            map.getUiSettings().setAllGesturesEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

            dbHandler = new DBHandler(getActivity());

            // // DATABASE READ then draw all places markers and move to the first
            if(readPlacesFromDb() && !myPlaces.isEmpty()) {
                drawMyPlaces();
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(myPlaces.get(0).getLatLng()));
            }
            // Else move to Lahti
            if (myPlaces.isEmpty()) {
                LatLng lahti = new LatLng(60.98, 25.66);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(lahti));
            }

            // Save button: save a new place to the list and redraw
            buttonSave.setOnClickListener(v -> {
                if (markerPosition != null) {
                    // If list is empty
                    if (!myPlaces.isEmpty()) {
                        // If the place is already on the list, update its name
                        for (int i=0; i<myPlaces.size(); i++) {
                            if (myPlaces.get(i).getLatLng().equals(markerPosition)) {
                                MyMarker myMarker = new MyMarker(editTextName.getText().toString(), myPlaces.get(i).getLatLng());
                                myPlaces.set(i, myMarker);

                                drawMyPlaces();

                                // DATABASE WRITE
                                if(writePlacesToDb()) Toast.makeText(getActivity(),"Save OK!",Toast.LENGTH_SHORT).show();
                                else Toast.makeText(getActivity(),"Save Failed!",Toast.LENGTH_SHORT).show();

                                return;
                            }
                        }
                        // Else add the place to the list
                        MyMarker myMarker = new MyMarker(editTextName.getText().toString(), markerPosition);
                        myPlaces.add(myMarker);

                    }
                    // Else add the place to the list
                    else {
                        MyMarker myMarker = new MyMarker(editTextName.getText().toString(), markerPosition);
                        myPlaces.add(myMarker);
                    }
                    drawMyPlaces();

                    // DATABASE WRITE
                    if(writePlacesToDb()) Toast.makeText(getActivity(),"Save OK!",Toast.LENGTH_SHORT).show();
                    else Toast.makeText(getActivity(),"Save Failed!",Toast.LENGTH_SHORT).show();


                }
                else Toast.makeText(getActivity(),"No marker selected!",Toast.LENGTH_SHORT).show();
            });

            // Delete button: remove a selected place from the list with same latlng and redraw
            buttonDelete.setOnClickListener(v -> {
                if (markerPosition != null) {
                    for (int i=0; i<myPlaces.size(); i++) {
                        if(myPlaces.get(i).getLatLng().equals(markerPosition)) {
                            myPlaces.remove(i);
                            drawMyPlaces();

                            // DATABASE WRITE
                            if(writePlacesToDb()) Toast.makeText(getActivity(),"Save OK!",Toast.LENGTH_SHORT).show();
                            else Toast.makeText(getActivity(),"Save Failed!",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else Toast.makeText(getActivity(),"No markers selected!",Toast.LENGTH_SHORT).show();
            });

            // Map click: remove the old marker and add a new marker on the click position
            map.setOnMapClickListener((p) -> {
                if (clickMarker != null) clickMarker.remove();
                clickMarker = map.addMarker(new MarkerOptions().position(p));
                markerPosition = clickMarker.getPosition();
            });

            // Marker click: store position and title
            map.setOnMarkerClickListener(marker -> {
                if (clickMarker != null) clickMarker.remove();
                markerPosition = marker.getPosition();
                return false;
            });

        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        buttonSave = requireActivity().findViewById(R.id.buttonSave);
        buttonDelete = requireActivity().findViewById(R.id.buttonDie);
        editTextName = requireActivity().findViewById(R.id.editTextName);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get the SupportMapFragment and request notification when the map is ready to be used
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        // Search view
        searchView = requireActivity().findViewById(R.id.searchViewMap);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                // initializing a geo coder.
                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    // Get location name and adding that location to address list.
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(addressList.size()>0) {
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    // move position
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    // Clear the address list, we only need first
                    addressList.clear();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    // DATABASE  Read & Write
    private boolean readPlacesFromDb(){
        myPlaces.clear();
        List<MyMarker> lc = dbHandler.getAllPlaces();
        myPlaces.addAll(lc);

        return true;
    }
    private boolean writePlacesToDb(){
        dbHandler.clearPlaces();
        for (MyMarker myMarker : myPlaces) {
            dbHandler.addPlace(myMarker);
        }
        return true;
    }


    // Create map markers from myPlaces list
    private void drawMyPlaces() {
        map.clear();
        for (int i=0; i<myPlaces.size(); i++) {
            map.addMarker(new MarkerOptions()
                    .position(myPlaces.get(i).getLatLng())
                    .title(myPlaces.get(i).getTitle())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        // Reset values
        markerPosition = null;
        editTextName.setText(null);
    }

}