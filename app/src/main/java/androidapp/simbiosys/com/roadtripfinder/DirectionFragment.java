package androidapp.simbiosys.com.roadtripfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DirectionFragment extends Fragment implements OnMapReadyCallback {

    FloatingActionButton fab_Direction, fab_addMarker, fab_findRestaurant;
    GoogleMap mGooglemap;
    LatLng origin, destination;
    String Destination, DestinationName, type, radius;
    Marker Waypoints;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direction, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fab_Direction = (FloatingActionButton) view.findViewById(R.id.fab_Direction);
        fab_addMarker = (FloatingActionButton) view.findViewById(R.id.fab_addMarker);
        fab_findRestaurant = (FloatingActionButton) view.findViewById(R.id.fab_findRestaurant);
        fab_Direction.hide();
        fab_addMarker.hide();
        fab_findRestaurant.hide();
        return view;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        /**
         * Requesting Permissions at Run Time
         **/
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        /**
         * Fetching search place information from AutoCompleteActivity
         **/
        mGooglemap = googleMap;
        final Intent getDestination = getActivity().getIntent();
        onActivityResult(1, 2, getDestination);
        Destination = getDestination.getStringExtra("PlaceLatlng");
        DestinationName = getDestination.getStringExtra("PlaceName");
        /**
         * Initialize Google Maps
         **/
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location LastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        origin = new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude());

        if (Destination == null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(origin)            // Sets the center of the map to location user
                    .zoom(12)                  // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera
                    .tilt(0)                   // Sets the tilt of the camera
                    .build();                  // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            /**
             * Parse latitude and longitude from received data
             **/
            Destination = Destination.replaceAll("[^\\.0123456789,-]", "");
            String DestinationLatlng[] = Destination.split(",");
            Double DestinationLat = Double.parseDouble(DestinationLatlng[0]);
            Double DestinationLng = Double.parseDouble(DestinationLatlng[1]);
            destination = new LatLng(DestinationLat, DestinationLng);

            googleMap.addMarker(new MarkerOptions().position(destination));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(destination)       // Sets the center of the map to location user
                    .zoom(12)                  // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera
                    .tilt(0)                   // Sets the tilt of the camera
                    .build();                  // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            Toast.makeText(getActivity(), "Your destination is: " + DestinationName, Toast.LENGTH_LONG).show();
            /**
             * Get direction from current location to destination
             **/
            fab_Direction.show();
            fab_Direction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    StringBuilder GoogleDirectionURL = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
                    GoogleDirectionURL.append("origin=" + origin.latitude + "," + origin.longitude);
                    GoogleDirectionURL.append("&destination=" + destination.latitude + "," + destination.longitude);

                    DirectionAsyncTask directionAsyncTask = new DirectionAsyncTask();
                    Object[] toPass = new Object[2];
                    toPass[0] = mGooglemap;
                    toPass[1] = GoogleDirectionURL.toString();
                    directionAsyncTask.execute(toPass);
                    /**
                     *  Build new bounds for update camera to appropriate position
                     **/
                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                            .include(origin)
                            .include(destination)
                            .build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 3);
                    mGooglemap.moveCamera(cameraUpdate);
                    mGooglemap.animateCamera(cameraUpdate);
                    fab_Direction.hide();
                    fab_findRestaurant.hide();
                    fab_addMarker.show();
                }
            });

            fab_addMarker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(getContext(), "Click Google Maps to add a marker for restaurant search.", Toast.LENGTH_LONG).show();
                    mGooglemap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            if (Waypoints != null) {
                                Waypoints.remove();
                            }
                            Waypoints = mGooglemap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("Waypoints")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .draggable(true));
                            Waypoints.showInfoWindow();
                            fab_Direction.hide();
                            fab_addMarker.hide();
                            fab_findRestaurant.show();
                        }
                    });
                }
            });
        }

        fab_findRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String WayponitsLatlng = Waypoints.getPosition().toString();
                WayponitsLatlng = WayponitsLatlng.replaceAll("[^\\.0123456789,-]", "");
                String wayponitsLatlng[] = WayponitsLatlng.split(",");
                Double wayponitsLat = Double.parseDouble(wayponitsLatlng[0]);
                Double wayponitsLng = Double.parseDouble(wayponitsLatlng[1]);
                Toast.makeText(getContext(), "You select place is: " + wayponitsLat + ", " + wayponitsLng, Toast.LENGTH_LONG).show();

                SharedPreferences searchSetting = getActivity().getSharedPreferences("searchSetting", Context.MODE_PRIVATE);

                type = searchSetting.getString("type", "restaurant");
                radius = searchSetting.getString("radius", "1500");

                StringBuilder GooglePlacesURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                GooglePlacesURL.append("location=" + wayponitsLat + "," + wayponitsLng);
                if (radius == null) {
                    GooglePlacesURL.append("&radius=" + "2000");
                } else {
                    GooglePlacesURL.append("&radius=" + radius);
                }
                if (type == null) {
                    GooglePlacesURL.append("&type=" + "restaurant");
                } else {
                    GooglePlacesURL.append("&type=" + type);
                }
                GooglePlacesURL.append("&sensor=true");
                GooglePlacesURL.append("&key=AIzaSyDLsgqw-i-uHJeGkLTZs3qCZQCd4ASaV84");

                GooglePlacesAsyncTask googlePlacesAsyncTask = new GooglePlacesAsyncTask();
                Object[] toPass = new Object[2];
                toPass[0] = mGooglemap;
                toPass[1] = GooglePlacesURL.toString();
                googlePlacesAsyncTask.execute(toPass);
            }
        });
    }
}