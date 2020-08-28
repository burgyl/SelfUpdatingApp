package ch.lburgy.selfupdatingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ch.lburgy.selfupdatingapp.selfupdate.SelfUpdate;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_INSTALL = 0;

    private SelfUpdate selfUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Optional code to show the current version of the app
        TextView versionCode = findViewById(R.id.versionCode);
        versionCode.setText(String.format("%d", BuildConfig.VERSION_CODE));
        TextView versionName = findViewById(R.id.versionName);
        versionName.setText(BuildConfig.VERSION_NAME);

        if (savedInstanceState == null) {
            selfUpdate = new SelfUpdate(this, REQUEST_CODE_INSTALL);
            selfUpdate.checkUpdate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_INSTALL)
            selfUpdate.deleteFile();
        super.onActivityResult(requestCode, resultCode, data);
    }
}