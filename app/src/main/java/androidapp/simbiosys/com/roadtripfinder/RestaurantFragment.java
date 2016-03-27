package androidapp.simbiosys.com.roadtripfinder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestaurantFragment extends Fragment {

    private static final String YELP_API_BASE_URL = "https://api.yelp.com/v2/search?term=";
    private static LatLng waypoint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        YelpAPIFactory apiFactory = new YelpAPIFactory("pSkp5Gq9mcrTMas942CYmg", "cJ2BeX3pOo5jwSjo3SfcyC35G3k", "5the2OhYNVhLq7vVnDY5EoeBzcHpqGT-", "1cOd8ge2amjnm0VH_ngl7_NgEdU");
        YelpAPI yelpAPI = apiFactory.createAPI();

        waypoint = new LatLng(37.77493, -122.419415);
        String url = getYelpApiBaseUrl(waypoint);

        return view;
    }

    public String getYelpApiBaseUrl(LatLng RestArea) {
        String url = YELP_API_BASE_URL + "food&ll=" + RestArea;
        return url;
    }

    public String DownloadJSONdata(String strUrl) {
        String Data = "";
        try {
            URL url = new URL(strUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String str = "";
            while ((str = bufferedReader.readLine()) != null) {
                stringBuilder.append(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Data;
    }

    private class ParseTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }

}