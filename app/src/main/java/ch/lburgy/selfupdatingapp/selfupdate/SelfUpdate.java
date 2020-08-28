package ch.lburgy.selfupdatingapp.selfupdate;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import ch.lburgy.selfupdatingapp.BuildConfig;
import ch.lburgy.selfupdatingapp.R;
import ch.lburgy.selfupdatingapp.selfupdate.github.Asset;
import ch.lburgy.selfupdatingapp.selfupdate.github.Release;
import okhttp3.Interceptor;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SelfUpdate {

    private static final String FILENAME_APK = "update.apk";
    private static final String CONTENT_TYPE_APK = "application/vnd.android.package-archive";

    private static AppCompatActivity activity;
    private static SelfUpdateHttpClient httpClient;

    public static void checkUpdate(AppCompatActivity activity, final String url) {
        SelfUpdate.activity = activity;
        httpClient = new SelfUpdateHttpClient(activity);

        if (!httpClient.isConnectedToInternet()) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Release release = null;
                try {
                    release = httpClient.getLastRelease(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (release != null) {
                    try {
                        if (Integer.parseInt(release.getTag_name()) > BuildConfig.VERSION_CODE)
                            showUpdateAvailable(release);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static void showUpdateAvailable(final Release release) {
        String downloadUrl = null;
        for (Asset asset : release.getAssets()) {
            if (asset.getContent_type().equals(CONTENT_TYPE_APK)) {
                downloadUrl = asset.getBrowser_download_url();
                break;
            }
        }
        if (downloadUrl == null) return;

        final String finalDownloadUrl = downloadUrl;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View content = activity.getLayoutInflater().inflate(R.layout.content_dialog_show_update, null);
                TextView version = content.findViewById(R.id.version);
                version.setText(String.format("%s :", release.getName()));
                TextView changelog = content.findViewById(R.id.changelog);
                changelog.setText(release.getBody());

                new AlertDialog.Builder(activity)
                        .setTitle(activity.getResources().getString(R.string.dialog_update_title))
                        .setView(content)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                updateApp(finalDownloadUrl);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
    }

    private static void updateApp(String downloadUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            downloadUpdate(downloadUrl);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
            activity.startActivity(browserIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void downloadUpdate(final String url) {
        View content = activity.getLayoutInflater().inflate(R.layout.content_dialog_download, null);

        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(activity.getResources().getString(R.string.dialog_downloading_title))
                .setView(content)
                .setCancelable(false)
                .show();

        final ProgressBar progressBar = content.findViewById(R.id.progressBar);
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                final int progress = Math.round(((float) bytesRead / contentLength) * 100);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                    }
                });
                if (done) alertDialog.dismiss();
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
                    File apkFile = httpClient.download(url, FILENAME_APK, interceptor);
                    if (apkFile != null) installAPK(apkFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void installAPK(File apkFile) {
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", apkFile);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, CONTENT_TYPE_APK);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(i);
    }
}
