package io.github.froger.instamaterial.controllers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Albert on 16/9/2018.
 */

public class InstagramDataController {
    private static final String TAG = InstagramDataController.class.getSimpleName();
    //private static final String JUAN_API_TOKEN = "288757825.37f6a41.467aaa07edf34eed802f6f8a5ca46ede";
    private static final String API_TOKEN = "8590231715.8df6139.4299bc50b0204148af186b022052d7bc";

    private static final String BASE_URL = "https://api.instagram.com/v1/users/self/media/recent/?access_token=";

    public static void getUrls(Context context, final OnInstagramURLsResolved onInstagramURLsResolved) {
        String url = BASE_URL + API_TOKEN;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse the answers
                        ArrayList<String> urls = new ArrayList<String>();
                        try {
                            JSONObject json = new JSONObject(response.toString());
                            JSONArray data = (JSONArray) json.get("data");
                            urls = extractUrls(data);
                            onInstagramURLsResolved.onInstagramURLsResolved(urls);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "That didn't work!");
            }
        });

        VolleyController.getInstance(context).addToQueue(stringRequest);
    }

    private static ArrayList<String> extractUrls(JSONArray data) {
        ArrayList<String> urls = new ArrayList<String>();
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject images = (JSONObject) data.getJSONObject(i).get("images");
                JSONObject resolution = (JSONObject) images.get("standard_resolution");
                String url = resolution.getString("url");
                urls.add(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public interface OnInstagramURLsResolved {
        void onInstagramURLsResolved(ArrayList<String> urls);
    }
}
