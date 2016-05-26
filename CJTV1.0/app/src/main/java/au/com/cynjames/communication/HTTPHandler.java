package au.com.cynjames.communication;

import android.app.ProgressDialog;
import android.content.Context;

import com.loopj.android.http.*;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.cynjames.utils.GenericMethods;
import cz.msebera.android.httpclient.Header;

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
            httpClient.setMaxRetriesAndTimeout(1, 20000);
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
            progressDialog.show();
        }

        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            try {
                this.responseListener.onSuccess(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //progressDialog.dismiss();
        }

        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
            //progressDialog.dismiss();
        }

        public void onFinish() {
            super.onFinish();
            progressDialog.dismiss();
        }
    }

}
