package io.github.froger.instamaterial;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;

import io.github.froger.instamaterial.controllers.QwantImageSearchController;
import io.github.froger.instamaterial.models.QwantImage;
import timber.log.Timber;

/**
 * Created by froger_mcs on 05.11.14.
 */
public class InstaMaterialApplication extends Application implements QwantImageSearchController.QwantImageSearchResolvedCallback {
    private final String TAG = InstaMaterialApplication.class.getSimpleName();
    private final QwantImageSearchController.QwantImageSearchResolvedCallback qwantImageSearchResolvedCallback = this;
    private QwantImageSearchController qwantImageSearchController;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());

        qwantImageSearchController = new QwantImageSearchController(this);
        String queryString = "rhino corpse dead";
        qwantImageSearchController.qwantImageSearchRequest(queryString, qwantImageSearchResolvedCallback);
    }

    @Override
    public void onQwantImageSearchResolved(ArrayList<QwantImage> qwantImagesArray) {
        for(int i = 0; i < qwantImagesArray.size(); ++i) {
            Log.e(TAG, qwantImagesArray.get(i).getMedia());
        }
    }
}
