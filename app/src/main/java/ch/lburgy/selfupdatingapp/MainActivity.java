package ch.lburgy.selfupdatingapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import ch.lburgy.selfupdatingapp.github.Asset;
import ch.lburgy.selfupdatingapp.github.Release;
import okhttp3.Interceptor;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String FILENAME_APK = "update.apk";
    private static final String CONTENT_TYPE_APK = "application/vnd.android.package-archive";
    private static final int REQUEST_CODE_INSTALL = 0;

    private MyHttpClient httpClient;
    private File apkFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView versionCode = findViewById(R.id.versionCode);
        versionCode.setText(String.format("%d", BuildConfig.VERSION_CODE));
        TextView versionName = findViewById(R.id.versionName);
        versionName.setText(BuildConfig.VERSION_NAME);

        httpClient = new MyHttpClient(MainActivity.this);

        if (savedInstanceState == null)
            checkUpdate();
    }

    private void checkUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Release release = null;
                try {
                    release = httpClient.getLastRelease();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final Release finalRelease = release;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalRelease != null) {
                            try {
                                int lastVersionCode = Integer.parseInt(finalRelease.getTag_name());
                                if (lastVersionCode > BuildConfig.VERSION_CODE) {
                                    showUpdateAvailable(finalRelease);
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void showUpdateAvailable(final Release release) {
        View content = getLayoutInflater().inflate(R.layout.content_dialog_show_update, null);
        TextView version = content.findViewById(R.id.version);
        version.setText(String.format("%s :", release.getName()));
        TextView changelog = content.findViewById(R.id.changelog);
        changelog.setText(release.getBody());
        new AlertDialog.Builder(this)
                .setTitle("Do you want to update the app ?")
                .setView(content)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateApp(release);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void updateApp(Release release) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Asset asset : release.getAssets()) {
                if (asset.getContent_type().equals(CONTENT_TYPE_APK)) {
                    downloadUpdate(asset.getBrowser_download_url());
                    break;
                }
            }
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(release.getHtml_url()));
            startActivity(browserIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void downloadUpdate(final String url) {
        View content = getLayoutInflater().inflate(R.layout.content_dialog_download, null);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Downloading the update")
                .setView(content)
                .setCancelable(false)
                .show();

        final ProgressBar progressBar = content.findViewById(R.id.progressBar);
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                final int progress = Math.round(((float) bytesRead / contentLength) * 100);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                    }
                });
                if (done) alertDialog.cancel();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                Interceptor interceptor = new Interceptor() {
                    @NotNull
                    @Override
                    public Response intercept(@NotNull Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                                .build();
                    }
                };

                try {
                    apkFile = httpClient.download(url, FILENAME_APK, interceptor);
                    if (apkFile != null) installAPK();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void installAPK() {
        Uri uri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", apkFile);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, CONTENT_TYPE_APK);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(i, REQUEST_CODE_INSTALL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_INSTALL)
            apkFile.delete();
        super.onActivityResult(requestCode, resultCode, data);
    }
}