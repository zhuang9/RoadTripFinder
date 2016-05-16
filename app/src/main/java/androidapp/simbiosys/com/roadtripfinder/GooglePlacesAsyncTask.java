package androidapp.simbiosys.com.roadtripfinder;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhehuang on 4/28/16.
 */
public class GooglePlacesAsyncTask extends AsyncTask<Object, Void, String> {

    GoogleMap googleMap;

    @Override
    protected String doInBackground(Object... inputObj) {
        String GooglePlacesData = null;
        try {
            googleMap = (GoogleMap) inputObj[0];
            String googlePlacesUrl = (String) inputObj[1];
            HttpUrlDownload httpUrlDownload = new HttpUrlDownload();
            GooglePlacesData = httpUrlDownload.downloadJSON(googlePlacesUrl);
        } catch (Exception e) {
            Log.d("Google Place Read Task", e.toString());
        }
        return GooglePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        PlacesDisplayTask placesDisplayTask = new PlacesDisplayTask();
        Object[] toPass = new Object[2];
        toPass[0] = googleMap;
        toPass[1] = result;
        placesDisplayTask.execute(toPass);
    }

    public class PlacesDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>> {

        @Override
        protected List<HashMap<String, String>> doInBackground(Object... inputObj) {

            List<HashMap<String, String>> googlePlacesList = null;
            GooglePlacesParser googlePlacesParser = new GooglePlacesParser();
            JSONObject googlePlacesJson;

            try {
                googleMap = (GoogleMap) inputObj[0];
                googlePlacesJson = new JSONObject((String) inputObj[1]);
                googlePlacesList = googlePlacesParser.parse(googlePlacesJson);
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return googlePlacesList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            for (int i = 0; i < list.size(); i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> GooglePlaceList = list.get(i);
                double lat = Double.parseDouble(GooglePlaceList.get("lat"));
                double lng = Double.parseDouble(GooglePlaceList.get("lng"));
                String PlaceName = GooglePlaceList.get("place_name");
                String OpenNow = GooglePlaceList.get("open_now");
                String vicinity = GooglePlaceList.get("vicinity");
                String PriceLevel = GooglePlaceList.get("prince_level");
                String rating = GooglePlaceList.get("rating");
                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(PlaceName + " : " + OpenNow + "; " + "rating(0-5): " + rating);
                markerOptions.snippet(vicinity + "; " + "Price Level (0-5): " + PriceLevel);
                googleMap.addMarker(markerOptions);
            }
        }
    }

    public class GooglePlacesParser {

        public List<HashMap<String, String>> parse(JSONObject jsonObject) {
            JSONArray jsonArray = null;
            try {
                jsonArray = jsonObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return getPlaces(jsonArray);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
            int placesCount = jsonArray.length();
            List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> placeMap = null;

            for (int i = 0; i < placesCount; i++) {
                try {
                    placeMap = getPlace((JSONObject) jsonArray.get(i));
                    placesList.add(placeMap);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return placesList;
        }

        private HashMap<String, String> getPlace(JSONObject GooglePlaceJson) {
            HashMap<String, String> GooglePlaceHashMap = new HashMap<String, String>();
            String PlaceName = "N/A";
            String OpenNow = "N/A";
            String vicinity = "N/A";
            String latitude = "";
            String longitude = "";
            String PriceLevel = "N/A";
            String rating = "N/A";

            try {
                if (!GooglePlaceJson.isNull("name")) {
                    PlaceName = GooglePlaceJson.getString("name");
                }
                if (!GooglePlaceJson.isNull(("opening_hours"))) {
                    OpenNow = GooglePlaceJson.getJSONObject("opening_hours").getString("open_now");
                }
                if (!GooglePlaceJson.isNull("vicinity")) {
                    vicinity = GooglePlaceJson.getString("vicinity");
                }
                if (!GooglePlaceJson.isNull("price_level")) {
                    PriceLevel = GooglePlaceJson.getString("price_level");
                }
                if (!GooglePlaceJson.isNull("rating")) {
                    rating = GooglePlaceJson.getString("rating");
                }
                if (OpenNow == "true") {
                    GooglePlaceHashMap.put("open_now", "Open");
                } else {
                    GooglePlaceHashMap.put("open_now", "Closed");
                }
                latitude = GooglePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = GooglePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
                GooglePlaceHashMap.put("place_name", PlaceName);
                GooglePlaceHashMap.put("vicinity", vicinity);
                GooglePlaceHashMap.put("lat", latitude);
                GooglePlaceHashMap.put("lng", longitude);
                GooglePlaceHashMap.put("prince_level", PriceLevel);
                GooglePlaceHashMap.put("rating", rating);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return GooglePlaceHashMap;
        }
    }
}