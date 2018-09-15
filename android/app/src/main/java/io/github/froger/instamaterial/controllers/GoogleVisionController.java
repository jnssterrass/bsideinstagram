package io.github.froger.instamaterial.controllers;

import android.content.Context;
import android.util.Log;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by LaQuay on 15/09/2018.
 */

public class GoogleVisionController {
    private static final String API_KEY = "AIzaSyC42wohzdrPOKR8iMqptG27h4Og3rATtpk";
    private static GoogleVisionController instance;
    private final String TAG = GoogleVisionController.class.getSimpleName();
    private final Vision vision;
    private Context ctx;

    private GoogleVisionController(Context ctx) {
        this.ctx = ctx;

        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer(API_KEY));

        vision = visionBuilder.build();
    }

    public static GoogleVisionController getInstance(Context ctx) {
        if (instance == null) {
            createInstance(ctx);
        }
        return instance;
    }

    private synchronized static void createInstance(Context ctx) {
        if (instance == null) {
            instance = new GoogleVisionController(ctx);
        }
    }

    public void getLabels(String url) {
        Image image = new Image().setSource(new ImageSource().setImageUri(url));
        Feature desiredFeature = new Feature();
        desiredFeature.setType("LABEL_DETECTION");

        AnnotateImageRequest request = new AnnotateImageRequest();
        request.setImage(image);
        request.setFeatures(Arrays.asList(desiredFeature));

        final BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
        batchRequest.setRequests(Arrays.asList(request));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BatchAnnotateImagesResponse batchResponse = vision.images().annotate(batchRequest).execute();

                    List<AnnotateImageResponse> responses = batchResponse.getResponses();

                    for (int i = 0; i < responses.size(); ++i) {
                        Log.e(TAG, responses.get(i).toPrettyString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
