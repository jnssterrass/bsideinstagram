package io.github.froger.instamaterial.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.controllers.VolleyController;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;

    private String[] imagesArray;

    public ViewPagerAdapter(Context context, String[] imagesArray) {
        Log.e("TAG", "Hello");
        this.context = context;

        this.imagesArray = imagesArray;
    }

    @Override
    public int getCount() {
        return imagesArray.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.custom_layout, null);

        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        ImageRequest request = new ImageRequest(imagesArray[position % imagesArray.length],
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        //ivFeedCenter.setImageBitmap(bitmap);
                        imageView.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", "Error downloading image");
                        Log.e("TAG", "" + error.toString());
                    }
                });
        VolleyController.getInstance(view.getContext()).addToQueue(request);

        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);
    }
}