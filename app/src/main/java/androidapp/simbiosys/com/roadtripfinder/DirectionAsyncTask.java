package androidapp.simbiosys.com.roadtripfinder;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhehuang on 4/29/16.
 */
public class DirectionAsyncTask extends AsyncTask<Object, Void, String> {

    GoogleMap googleMap;

    @Override
    protected String doInBackground(Object... inputObj) {
        String GoogleMapsDirectionData = null;
        try {
            googleMap = (GoogleMap) inputObj[0];
            String url = (String) inputObj[1];
            HttpUrlDownload httpUrlDownload = new HttpUrlDownload();
            GoogleMapsDirectionData = httpUrlDownload.downloadJSON(url);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return GoogleMapsDirectionData;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        DirectionDisplayTask directionDisplayTask = new DirectionDisplayTask();
        Object[] toPass = new Object[2];
        toPass[0] = googleMap;
        toPass[1] = result;
        directionDisplayTask.execute(toPass);
    }

    private class DirectionDisplayTask extends AsyncTask<Object, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(Object... inputObj) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                googleMap = (GoogleMap) inputObj[0];
                jObject = new JSONObject((String) inputObj[1]);
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
            googleMap.addPolyline(polylineOptions);
        }
    }

    public class DirectionsJSONParser {
        /**
         * Receives a JSONObject and returns a list of lists containing latitude and
         * longitude
         */

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {
                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<HashMap<String, String>>();

                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }
            return routes;
        }

        /**
         * Method to decode polyline points Courtesy :
         * /decoding-polylines-from-google-maps-direction-api-with-java
         */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }
}