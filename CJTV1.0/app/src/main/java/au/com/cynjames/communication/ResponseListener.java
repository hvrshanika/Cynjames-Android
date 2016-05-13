package au.com.cynjames.communication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by eleos on 5/13/2016.
 */
public interface ResponseListener {

    void onSuccess(JSONObject jSONObject) throws JSONException;

}
