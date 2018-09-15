package io.github.froger.instamaterial.ui.activity;

import android.content.Context;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import butterknife.BindView;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.ui.adapter.UserProfileAdapter;

/**
 * Created by Albert on 16/9/2018.
 */

public class InstagramData {

    @BindView(R.id.text)
    private TextView mTextView;

    private UserProfileAdapter adapter;

    public void setAdapter(UserProfileAdapter adapter) {
        this.adapter = adapter;
    }

    public void setUrl(ArrayList<String> urls) {
        this.adapter.setPhotos(urls);
    }

    public void getUrls(Context context) {

        // access token
        String token = "288757825.37f6a41.467aaa07edf34eed802f6f8a5ca46ede";

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://api.instagram.com/v1/users/self/media/recent/?access_token="
                + token;

        // Request a string response from the provided URL.
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setUrl(urls);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private ArrayList<String> extractUrls(JSONArray data) {
        ArrayList<String> urls = new ArrayList<String>();
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject images = (JSONObject) data.getJSONObject(i).get("images");
                JSONObject resolution = (JSONObject) images.get("standard_resolution");
                String url = resolution.get("url").toString();
                urls.add(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}
