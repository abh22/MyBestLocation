package com.example.mybestlocation.ui.slideshow;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mybestlocation.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng selectedLocation;
    // to check if a specific location was passed from HomeFragment
    private boolean isSpecificLocationPassed = false;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (!isSpecificLocationPassed) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                selectedLocation = currentLatLng;

                // Clear any existing markers and add new one
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                        .position(currentLatLng)
                        .title(getAddressFromLocation(currentLatLng)));

                // Move camera to current location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));

                // Stop location updates after getting the first location
                locationManager.removeUpdates(this);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(requireContext(), "Please enable GPS", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize location manager
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        // NEW: Check if a specific location was passed from HomeFragment
        if (getArguments() != null) {
            double latitude = getArguments().getDouble("latitude", 0);
            double longitude = getArguments().getDouble("longitude", 0);

            // If valid coordinates were passed, set the specific location flag
            if (latitude != 0 && longitude != 0) {
                isSpecificLocationPassed = true;
                selectedLocation = new LatLng(latitude, longitude);
            }
        }
        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Add confirm button click listener
        view.findViewById(R.id.btnConfirmLocation).setOnClickListener(v -> {
            if (selectedLocation != null) {
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", selectedLocation.latitude);
                bundle.putDouble("longitude", selectedLocation.longitude);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_mapFragment_to_slideshowFragment, bundle);
            }
        });

        return view;
    }

    private String getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Current Location";
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // NEW: If a specific location was passed, show its marker
        if (isSpecificLocationPassed && selectedLocation != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(selectedLocation)
                    .title(getAddressFromLocation(selectedLocation)));
// Move camera to the specific location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15f));
        } else {
            // Existing location permission and current location logic
            if (hasLocationPermission()) {
                enableMyLocation();
            } else {
                requestLocationPermission();
            }
        }



        // Set up map click listener for when user wants to change location
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            selectedLocation = latLng;
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(getAddressFromLocation(latLng)));
        });
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);


            // Request location updates using GPS provider
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,  // 5 seconds
                    10,    // 10 meters
                    locationListener
            );

            // Also request network provider for faster initial location
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,  // 5 seconds
                    1,    // 1 meter
                    locationListener
            );

            // Get last known location immediately
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation != null) {
                locationListener.onLocationChanged(lastKnownLocation);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}