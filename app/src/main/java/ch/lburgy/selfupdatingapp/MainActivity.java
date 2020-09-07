package ch.lburgy.selfupdatingapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ch.lburgy.selfupdatingapp.selfupdate.SelfUpdate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Optional code to show the current version of the app
        TextView versionCode = findViewById(R.id.versionCode);
        versionCode.setText(String.format("%d", BuildConfig.VERSION_CODE));
        TextView versionName = findViewById(R.id.versionName);
        versionName.setText(BuildConfig.VERSION_NAME);

        // Check if an update is available at startup
        if (savedInstanceState == null)
            SelfUpdate.checkUpdate(this, "burgyl", "SelfUpdatingApp");
    }
}