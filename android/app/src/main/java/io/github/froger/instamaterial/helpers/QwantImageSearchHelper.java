package io.github.froger.instamaterial.helpers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.froger.instamaterial.controllers.VolleyController;
import io.github.froger.instamaterial.models.QwantImage;

public class QwantImageSearchHelper {
    private static final String TAG = QwantImageSearchHelper.class.getSimpleName();

    public static void qwantImageSearchRequest(Context context, String queryString, final QwantImageSearchResolvedCallback qwantImageSearchResolvedCallback) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.qwant.com")
                .appendPath("api")
                .appendPath("search")
                .appendPath("images")
                .appendQueryParameter("q", queryString)
                .appendQueryParameter("count", "1")
                .appendQueryParameter("t", "images")
                .appendQueryParameter("uiv", "4");
        String url = builder.build().toString();

        Log.e(TAG, url);

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<QwantImage> qwantImageSearchArray = parseQwantImageSearchJSON(response);
                        qwantImageSearchResolvedCallback.onQwantImageSearchResolved(qwantImageSearchArray);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        VolleyController.getInstance(context).addToQueue(jsonObjectRequest);
    }

    private static ArrayList<QwantImage> parseQwantImageSearchJSON(JSONObject qwantImageSearchJSONObject) {
        ArrayList<QwantImage> qwantImageSearchArray = new ArrayList<QwantImage>();

        try {
            JSONArray qwantImagesArray = qwantImageSearchJSONObject.getJSONObject("data").getJSONObject("result").getJSONArray("items");

            for (int i = 0; i < qwantImagesArray.length(); i++) {
                JSONObject qwantImageObject = qwantImagesArray.getJSONObject(i);
                String qwantImageURLString = qwantImageObject.getString("media");

                QwantImage qwantImage = new QwantImage();
                qwantImage.setMedia(qwantImageURLString);

                qwantImageSearchArray.add(qwantImage);
            }
            return qwantImageSearchArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return qwantImageSearchArray;
    }

    public interface QwantImageSearchResolvedCallback {
        void onQwantImageSearchResolved(ArrayList<QwantImage> qwantImages);
    }
}
