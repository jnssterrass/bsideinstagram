package io.github.froger.instamaterial.ui.activity;

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

import io.github.froger.instamaterial.controllers.VolleyController;

/**
 * Created by Albert on 16/9/2018.
 */

public class NewsData {
    private static final String TAG = NewsData.class.getSimpleName();
    private static final String API_TOKEN = "483691f548e840fabc097139f3aa6e6a";
    private static final String BASE_URL = "https://newsapi.org/v2/everything?";
    private static final String LANGUAGE = "en";

    public static void getUrls(Context context, ArrayList<String> tags, final OnNewsURLsResolved onNewsURLsResolved) {
        String q = tags.get(0);
        for (int i = 1; i < tags.size(); i++) {
                q += " AND " + tags.get(i);
        }
        String url = BASE_URL + "q=" + q + "&language=" + LANGUAGE + "&apiKey=" + API_TOKEN;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<String> urls;
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray data = (JSONArray) json.get("articles");
                            urls = extractUrls(data);
                            onNewsURLsResolved.onNewsURLsResolved(urls);
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
                JSONObject article = data.getJSONObject(i);
                String url = article.getString("url");
                urls.add(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public interface OnNewsURLsResolved {
        void onNewsURLsResolved(ArrayList<String> urls);
    }
}