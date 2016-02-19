package androidapp.simbiosys.com.roadtripfinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LogInActivity extends AppCompatActivity {

    EditText userID, password;
    Button LogIn, newAccount, skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        userID = (EditText) findViewById(R.id.userID);
        password = (EditText) findViewById(R.id.password);
        LogIn = (Button) findViewById(R.id.LogIn);
        newAccount = (Button) findViewById(R.id.newAccount);
        skip = (Button) findViewById(R.id.skip);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivity = new Intent(LogInActivity.this, MainActivity.class);
                startActivity(mainActivity);
            }
        });
    }
}
