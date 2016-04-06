package androidapp.simbiosys.com.roadtripfinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

public class AutoCompleteActivity extends AppCompatActivity {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    String TAG = "AutoCompleteActivity";
    Button Set_Destination;
    TextView PlaceName, PlaceAddress, PlacePhone, PlaceWeb, PlaceLatlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete);
        PlaceName = (TextView) findViewById(R.id.PlaceName);
        PlaceAddress = (TextView) findViewById(R.id.PlaceAddress);
        PlacePhone = (TextView) findViewById(R.id.PlacePhone);
        PlaceWeb = (TextView) findViewById(R.id.PlaceWeb);
        PlaceLatlng = (TextView) findViewById(R.id.PlaceLatlng);
        Set_Destination = (Button) findViewById(R.id.SetDestination);
        AutoCompleteActivity();
        Set_Destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SetDestination = new Intent(AutoCompleteActivity.this, MainActivity.class);
                SetDestination.putExtra("PlaceLatlng", PlaceLatlng.getText().toString());
                SetDestination.putExtra("PlaceName", PlaceName.getText().toString());
                setResult(2, SetDestination);
                startActivity(SetDestination);
            }
        });
    }

    public void AutoCompleteActivity() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());

                PlaceName.setText(place.getName());
                PlaceAddress.setText(place.getAddress());
                PlacePhone.setText(place.getPhoneNumber());
                PlaceWeb.setText(place.getWebsiteUri().toString());
                PlaceLatlng.setText(place.getLatLng().toString());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
