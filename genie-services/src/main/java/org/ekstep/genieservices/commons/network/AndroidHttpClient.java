package org.ekstep.genieservices.commons.network;

import android.content.Context;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.ekstep.genieservices.commons.AndroidLogger;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.network.auth.BasicAuthenticator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by mathew on 20/4/17.
 */

public class AndroidHttpClient extends AndroidNetworkConnectivity implements IHttpClient {

    private OkHttpClient httpClient;
    private Request.Builder requestBuilder;

    private Authenticator authenticator;

    @Override
    public Void createRequest(String url) {
        this.requestBuilder = new Request.Builder().url(url);
        return null;
    }

    @Override
    public Void setHeaders(Map<String, String> headers) {
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> keyValuePair = (Map.Entry) it.next();
            this.requestBuilder.addHeader(keyValuePair.getKey(), keyValuePair.getValue());
        }
        return null;
    }

    @Override
    public ApiResponse doGet(String requestBody) throws IOException {
        requestBuilder.get();
        Request request = requestBuilder.build();
        Response response = httpClient.newCall(request).execute();
        return new ApiResponse(response.isSuccessful(), response.body() != null ? response.body().string() : "");
    }

    @Override
    public ApiResponse doPost(String requestBody) throws IOException {
        requestBuilder.post(RequestBody.create(MediaType.parse("application/json"), requestBody));
        Request request = requestBuilder.build();
        Response response = httpClient.newCall(request).execute();
        return new ApiResponse(response.isSuccessful(), response.body() != null ? response.body().string() : "");
    }

    public AndroidHttpClient(AppContext<Context, AndroidLogger> appContext, Authenticator authenticator) {
        super(appContext);
    }

    @Override
    public Void createClient() {
        httpClient = new OkHttpClient();
        return null;
    }

    @Override
    public Void setTimeouts(int connectionTimeout, int readTimeout) {
        httpClient.setConnectTimeout(NetworkConstants.NETWORK_CONNECT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        httpClient.setReadTimeout(NetworkConstants.NETWORK_READ_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        return null;
    }

    @Override
    public Void handleAuth() {
        httpClient.setAuthenticator(authenticator);
        return null;
    }

}
