package ch.lburgy.selfupdatingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import ch.lburgy.selfupdatingapp.github.Release;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

public class MyHttpClient {

    private static final String URL_APP_REPO = "https://api.github.com/repos/burgyL/SelfUpdatingApp/releases/latest";

    private OkHttpClient simpleOkHttpClient;
    private HttpClient simpleHttpClient;
    private final Context context;

    public MyHttpClient(Context context) {
        this.context = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            simpleOkHttpClient = new OkHttpClient();
        } else {
            simpleHttpClient = HttpClientBuilder.create().build();
        }
    }

    public Release getLastRelease() throws HttpException, IOException, NoInternetConnectionException {
        String body = get(URL_APP_REPO);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Release> jsonAdapter = moshi.adapter(Release.class);
        return jsonAdapter.fromJson(body);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
        String result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                result = responseBody.string();
            } catch (IOException e) {
                return null;
            }
        } else {
            HttpUriRequest request = new HttpGet(url);

            HttpResponse response = null;
            try {
                response = simpleHttpClient.execute(request);
            } catch (IOException e) {
                if (e.getClass() == UnknownHostException.class)
                    throw (UnknownHostException) e;
                e.printStackTrace();
            }

            int httpCode = response.getStatusLine().getStatusCode();
            if (httpCode != 200) throw new HttpException(httpCode);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    result = EntityUtils.toString(entity);
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return result;
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
