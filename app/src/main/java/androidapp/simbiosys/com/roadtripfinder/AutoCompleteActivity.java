package androidapp.simbiosys.com.roadtripfinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class AutoCompleteActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String LOG_TAG = "MainActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView autoCompleteTextView;
    private TextView PlaceName;
    private TextView PlaceAddress;
    private TextView PlacePhone;
    private TextView PlaceWeb;
    private TextView PlaceLatlng;
    private Button GetDestination;
    private GoogleApiClient googleApiClient;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private static final LatLngBounds BOUNDS_BAY_AREA = new LatLngBounds(new LatLng(37.197668, -122.404449), new LatLng(38.913779, -120.989512));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete);
        googleApiClient = new GoogleApiClient.Builder(AutoCompleteActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoComplete);
        autoCompleteTextView.setThreshold(3);
        PlaceName = (TextView) findViewById(R.id.PlaceName);
        PlaceAddress = (TextView) findViewById(R.id.PlaceAddress);
        PlacePhone = (TextView) findViewById(R.id.PlacePhone);
        PlaceWeb = (TextView) findViewById(R.id.PlaceWeb);
        PlaceLatlng = (TextView) findViewById(R.id.PlaceLatlng);
        GetDestination = (Button) findViewById(R.id.GetDestination);
        autoCompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_BAY_AREA, null);
        autoCompleteTextView.setAdapter(placeAutocompleteAdapter);
        GetDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SetDestination = new Intent(AutoCompleteActivity.this, MainActivity.class);
                SetDestination.putExtra("PlaceLatlng", PlaceLatlng.toString());
                setResult(2, SetDestination);
                startActivity(SetDestination);
            }
        });
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = placeAutocompleteAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);

            PlaceName.setText(Html.fromHtml(place.getName() + ""));
            PlaceAddress.setText(Html.fromHtml(place.getAddress() + ""));
            PlacePhone.setText(Html.fromHtml(place.getPhoneNumber() + ""));
            PlaceWeb.setText(place.getWebsiteUri() + "");
            PlaceLatlng.setText(Html.fromHtml(String.valueOf(place.getLatLng())) + "");
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        placeAutocompleteAdapter.setGoogleApiClient(googleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: " + connectionResult.getErrorCode());
        Toast.makeText(this, "Google Places API connection failed with error code:" + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        placeAutocompleteAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }
}