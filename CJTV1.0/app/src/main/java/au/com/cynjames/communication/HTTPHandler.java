package au.com.cynjames.communication;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;

import com.loopj.android.http.*;
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

    private static final String BASE_URL = "http://www.cynjamestransport.com.au/webservice-one/";

    private static AsyncHttpClient httpClient;

    public static void post(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        getAsyncClient().post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static AsyncHttpClient getAsyncClient() {
        if (httpClient == null) {
            httpClient = new AsyncHttpClient();
            httpClient.setMaxRetriesAndTimeout(1, 30000);
        }
        return httpClient;
    }

    public static class ResponseManager extends JsonHttpResponseHandler {
        final ResponseListener responseListener;
        final Context context;
        ProgressDialog progressDialog;

        public ResponseManager(ResponseListener responseListener, Context context, String msg) {
            this.responseListener = responseListener;
            this.context = context;
            progressDialog = GenericMethods.getProgressDialog(context, msg);
        }

        public void onStart() {
            super.onStart();
            try {
                progressDialog.show();
            } catch (WindowManager.BadTokenException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            try {
                this.responseListener.onSuccess(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                progressDialog.dismiss();
            } catch (WindowManager.BadTokenException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
            try {
                progressDialog.dismiss();
            } catch (WindowManager.BadTokenException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("Failed", responseString);
        }

        public void onFinish() {
            super.onFinish();
            try {
                progressDialog.dismiss();
            } catch (WindowManager.BadTokenException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
