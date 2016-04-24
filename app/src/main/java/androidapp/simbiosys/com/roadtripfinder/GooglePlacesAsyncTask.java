package androidapp.simbiosys.com.roadtripfinder;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.util.HashMap;
import java.util.List;

/**
 * Created by zhehuang on 4/22/16.
 */
public class GooglePlacesAsyncTask extends AsyncTask<Object, Integer, String> {
    String GooglePlacesData = null;
    GoogleMap googleMap;

    @Override
    protected String doInBackground(Object... inputObj) {
        try {
            googleMap = (GoogleMap) inputObj[0];
            String GooglePlaceNearbyUrl = (String) inputObj[1];
        } catch (Exception e) {
            Log.d("Google Place Read Task", e.toString());
        }
        return GooglePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    public class PlacesDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>> {

        @Override
        protected List<HashMap<String, String>> doInBackground(Object... params) {
            return null;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            super.onPostExecute(hashMaps);
        }
    }
}
