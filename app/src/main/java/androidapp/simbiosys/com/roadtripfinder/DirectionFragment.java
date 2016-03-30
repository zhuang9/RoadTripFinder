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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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
    FloatingActionButton floatingActionButton;
    GoogleMap mGooglemap;
    LatLng origin, destination;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direction, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchView = (SearchView) view.findViewById(R.id.searchView);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AutoCompleteActivity.class);
                startActivity(intent);
            }
        });
        floatingActionButton.hide();
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
        String Destination = GetDestination.getStringExtra("PlaceLatlng");
        String DestinationName = GetDestination.getStringExtra("PlaceName");
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
            floatingActionButton.show();
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = getDirectionsUrl(origin, destination);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                    // Move camera to appropriate position
                    LatLng midPoint = new LatLng((origin.latitude + destination.latitude) / 2, (origin.longitude + destination.longitude) / 2);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(midPoint)         // Sets the center of the map to location user
                            .zoom(6)                  // Sets the zoom
                            .bearing(0)               // Sets the orientation of the camera
                            .tilt(0)                  // Sets the tilt of the camera
                            .build();                 // Creates a CameraPosition from the builder
                    mGooglemap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    floatingActionButton.hide();
                }
            });
            mGooglemap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                }
            });
        }
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
                // Fetching the data from web service
                data = downloadUrl(url[0]);
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

    public String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            data = stringBuffer.toString();
            bufferedReader.close();
        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            inputStream.close();
            urlConnection.disconnect();
        }
        System.out.println("url:" + strUrl + "---->   downloadurl:" + data);
        return data;
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
            MarkerOptions markerOptions = new MarkerOptions();

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