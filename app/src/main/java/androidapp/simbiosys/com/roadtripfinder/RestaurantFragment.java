package androidapp.simbiosys.com.roadtripfinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RestaurantFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);

        String CONSUMER_KEY = "pSkp5Gq9mcrTMas942CYmg";
        String CONSUMER_SECRET = "cJ2BeX3pOo5jwSjo3SfcyC35G3k";
        String TOKEN = "mMC50rDvgYgfV-uIJFtj0mSr8B-hTepR";
        String TOKEN_SECRET = "FfEALeCm9-Cy4MoOc-G_ZfOfEEQ";

        YelpAPICLI yelpApiCli = new YelpAPICLI();
        new JCommander(yelpApiCli);

        //YelpAPI yelpAPI = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
        //queryAPI(yelpAPI, yelpApiCli);

        return view;
    }

    private static void queryAPI(YelpAPI yelpApi, YelpAPICLI yelpApiCli) {
        String searchResponseJSON =
                yelpApi.searchForBusinessesByLocation(yelpApiCli.term, yelpApiCli.location);

        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(searchResponseJSON);
        } catch (ParseException pe) {
            System.out.println("Error: could not parse JSON response:");
            System.out.println(searchResponseJSON);
            System.exit(1);
        }

        JSONArray businesses = (JSONArray) response.get("businesses");
        JSONObject firstBusiness = (JSONObject) businesses.get(0);
        String firstBusinessID = firstBusiness.get("id").toString();
        System.out.println(String.format(
                "%s businesses found, querying business info for the top result \"%s\" ...",
                businesses.size(), firstBusinessID));

        // Select the first business and display business details
        String businessResponseJSON = yelpApi.searchByBusinessId(firstBusinessID.toString());
        System.out.println(String.format("Result for business \"%s\" found:", firstBusinessID));
        System.out.println(businessResponseJSON);
    }

    public static class YelpAPICLI {
        @Parameter(names = {"-q", "--term"}, description = "Search Query Term")
        public String term = "dinner";

        @Parameter(names = {"-l", "--location"}, description = "Location to be Queried")
        public String location = "San Francisco, CA";
    }
}