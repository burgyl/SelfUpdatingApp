package ch.lburgy.selfupdatingapp.selfupdate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import ch.lburgy.selfupdatingapp.selfupdate.github.Release;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SelfUpdateHttpClient {

    private OkHttpClient simpleOkHttpClient;
    private final Context context;

    public SelfUpdateHttpClient(Context context) {
        this.context = context;
        simpleOkHttpClient = new OkHttpClient();
    }

    public Release getLastRelease(String url) throws HttpException, IOException, NoInternetConnectionException {
        String body = get(url);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Release> jsonAdapter = moshi.adapter(Release.class);
        return jsonAdapter.fromJson(body);
    }

    public File download(String url, String filename, Interceptor networkInterceptor) throws HttpException, UnknownHostException, NoInternetConnectionException, InterruptedIOException {
        if (!isConnectedToInternet()) throw new NoInternetConnectionException();

        OkHttpClient okHttpClient = simpleOkHttpClient;
        if (networkInterceptor != null)
            okHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(networkInterceptor).build();

        Request request = new Request.Builder().url(url).build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            if (e.getClass() == UnknownHostException.class)
                throw (UnknownHostException) e;
            else if (e.getClass() == InterruptedIOException.class)
                throw (InterruptedIOException) e;
            e.printStackTrace();
        }

        if (!response.isSuccessful()) {
            throw new HttpException(response.code());
        }

        ResponseBody responseBody = response.body();
        if (responseBody == null) return null;

        try {
            File file = new java.io.File((context.getApplicationContext().getFileStreamPath(filename).getPath()));
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(response.body().source());
            sink.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String get(String url) throws HttpException, UnknownHostException, NoInternetConnectionException, InterruptedIOException {
        if (!isConnectedToInternet()) throw new NoInternetConnectionException();

        Request request = new Request.Builder().url(url).build();

        Response response = null;
        try {
            response = simpleOkHttpClient.newCall(request).execute();
        } catch (IOException e) {
            if (e.getClass() == UnknownHostException.class)
                throw (UnknownHostException) e;
            else if (e.getClass() == InterruptedIOException.class)
                throw (InterruptedIOException) e;
            e.printStackTrace();
        }

        if (!response.isSuccessful()) throw new HttpException(response.code());

        ResponseBody responseBody = response.body();
        if (responseBody == null) return null;

        try {
            return responseBody.string();
        } catch (IOException e) {
            // nothing
        }
        return null;
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            for (NetworkInfo networkInfo : info)
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
        }
        return false;
    }

    public static class NoInternetConnectionException extends Exception {
    }

    public static class HttpException extends Exception {
        private final int code;

        public HttpException(int code) {
            super("HTTP Exception " + code);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
