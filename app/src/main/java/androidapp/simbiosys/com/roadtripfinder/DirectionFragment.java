package androidapp.simbiosys.com.roadtripfinder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
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
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionFragment extends Fragment implements OnMapReadyCallback {

    SearchView searchView;
    FloatingActionButton fab_Direction, fab_addMarker, fab_findRestaurant;
    GoogleMap mGooglemap;
    LatLng origin, destination;
    String Destination, DestinationName;
    Marker Waypoints;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direction, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchView = (SearchView) view.findViewById(R.id.searchView);
        fab_Direction = (FloatingActionButton) view.findViewById(R.id.fab_Direction);
        fab_addMarker = (FloatingActionButton) view.findViewById(R.id.fab_addMarker);
        fab_findRestaurant = (FloatingActionButton) view.findViewById(R.id.fab_findRestaurant);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AutoCompleteActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        fab_Direction.hide();
        fab_addMarker.hide();
        fab_findRestaurant.hide();
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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
        final Intent GetDestination = getActivity().getIntent();
        onActivityResult(1, 2, GetDestination);
        Destination = GetDestination.getStringExtra("PlaceLatlng");
        DestinationName = GetDestination.getStringExtra("PlaceName");
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
                    String url = getDirectionsUrl(origin, destination);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                    /**
                     *  Build new bounds for update camera to appropriate position
                     **/
                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                            .include(origin)
                            .include(destination)
                            .build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 20);
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
                StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                googlePlacesUrl.append("location=" + wayponitsLat + "," + wayponitsLng);
                googlePlacesUrl.append("&radius=" + "5000");
                googlePlacesUrl.append("&type=" + "restaurant");


            }
        });
    }

    public String getDirectionsUrl(LatLng start, LatLng end) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + start.latitude + "," + start.longitude + "&destination=" + end.latitude + "," + end.longitude;
        return url;
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpUrlDownload httpUrlDownload = new HttpUrlDownload();
                data = httpUrlDownload.downloadJSON(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
                System.out.println("do in background:" + routes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions polylineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                polylineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                polylineOptions.addAll(points);
                polylineOptions.width(10);
                // Changing the color polyline according to the mode
                polylineOptions.color(0xFF3399FF);
            }
            // Drawing polyline in the Google Map for the i-th route
            mGooglemap.addPolyline(polylineOptions);
        }
    }
}