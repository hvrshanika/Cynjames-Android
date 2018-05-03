package au.com.cynjames.communication;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import au.com.cynjames.utils.GenericMethods;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpClientConnection;
import cz.msebera.android.httpclient.HttpConnectionMetrics;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpEntityEnclosingRequest;
import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.protocol.HttpRequestExecutor;
import cz.msebera.android.httpclient.util.EntityUtils;

public class HTTPHandler {

    private static final String BASE_URL = "http://www.cynjames-stage.trotic.com/webservice-one/";
    //"http://www.cynjames-stage.trotic.com/webservice-one/";
    //"http://www.cynjamestransport.com/webservice-one/";

    private static AsyncHttpClient httpClient;

    public static void post(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        getAsyncClient().post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void directionsRequest(Location origin, String destinations, RequestParams params, JsonHttpResponseHandler responseHandler) {
        String destinationsF = (destinations.replace(" ", "+")).replaceAll("[\u0000-\u001f]", "");
        if(origin != null){
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="+origin.getLatitude()+","+origin.getLongitude()+"&destinations="+destinationsF+"&key=AIzaSyBJRifeKCWQiYpEW1lG9M24zCGQJkHGnT0";
            getAsyncClient().post(url, params, responseHandler);
        }
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static AsyncHttpClient getAsyncClient() {
        if (httpClient == null) {
            httpClient = new AsyncHttpClient();
            httpClient.setMaxRetriesAndTimeout(1, 60000);
            httpClient.setResponseTimeout(60000);
        }
        return httpClient;
    }

    public static class ResponseManager extends JsonHttpResponseHandler {
        final ResponseListener responseListener;
        final Context context;

        public ResponseManager(ResponseListener responseListener, Context context, String msg) {
            this.responseListener = responseListener;
            this.context = context;
        }

        public void onStart() {
            super.onStart();
        }

        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            try {
                this.responseListener.onSuccess(response);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            super.onSuccess(statusCode,headers,response);
            Log.d("HTTPHandler", "On-Success JSONArray");
        }

        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            super.onSuccess(statusCode,headers,responseString);
            Log.d("HTTPHandler", "On-Success String");
        }

        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
            Log.d("Handler Fail String", responseString);
        }

        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
            super.onFailure(statusCode,headers,throwable,errorResponse);
            Log.d("Handler Fail JSONArray", String.valueOf(errorResponse));
        }

        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            super.onFailure(statusCode,headers,throwable,errorResponse);
            Log.d("Handler Fail JSONObject", String.valueOf(errorResponse));
        }

        public void onFinish() {
            super.onFinish();
        }
    }
}
