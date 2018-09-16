package io.github.froger.instamaterial.ui.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.controllers.GoogleVisionController;
import io.github.froger.instamaterial.controllers.VolleyController;
import io.github.froger.instamaterial.helpers.QwantImageSearchHelper;
import io.github.froger.instamaterial.models.QwantImage;
import io.github.froger.instamaterial.ui.activity.MainActivity;
import io.github.froger.instamaterial.ui.activity.NewsData;
import io.github.froger.instamaterial.ui.view.LoadingFeedItemView;

/**
 * Created by froger_mcs on 05.11.14.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    public static final String ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button";

    public static final int VIEW_TYPE_DEFAULT = 1;
    public static final int VIEW_TYPE_LOADER = 2;

    private final List<FeedItem> feedItems = new ArrayList<>();

    private Context context;
    private OnFeedItemClickListener onFeedItemClickListener;

    private boolean showLoadingView = false;
    private ProgressDialog dialog;
    private Handler handler = new Handler();

    public FeedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DEFAULT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
            CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);
            setupClickableViews(view, cellFeedViewHolder);
            return cellFeedViewHolder;
        } else if (viewType == VIEW_TYPE_LOADER) {
            LoadingFeedItemView view = new LoadingFeedItemView(context);
            view.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            );
            return new LoadingCellFeedViewHolder(view);
        }

        return null;
    }

    private void setupClickableViews(final View view, final CellFeedViewHolder cellFeedViewHolder) {
        cellFeedViewHolder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onMoreClick(v, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.ivFeedCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                feedItems.get(adapterPosition).likesCount++;
                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showLikedSnackbar();
                }
            }
        });
        cellFeedViewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                feedItems.get(adapterPosition).likesCount++;
                notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showLikedSnackbar();
                }
            }
        });
        cellFeedViewHolder.btnBSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(context, "",
                        "Analizando imagen", true);

                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                String URL = feedItems.get(adapterPosition).URL;
                String text = feedItems.get(adapterPosition).text;

                addBSide(adapterPosition, URL, text);
            }
        });
        cellFeedViewHolder.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onProfileClick(view);
            }
        });
    }

    private void addBSide(final int adapterPosition, final String image, final String text) {
        final NewsData newsData = new NewsData();
        GoogleVisionController.getInstance(context).getLabels(image,
                new GoogleVisionController.OnImageResponse() {
                    @Override
                    public void onImageResponse(List<AnnotateImageResponse> responses) {
                        if (responses != null && !responses.isEmpty() &&
                                responses.get(0).getLabelAnnotations() != null &&
                                !responses.get(0).getLabelAnnotations().isEmpty()) {

                            String description = "";
                            String orig_description = "";
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("rhino", "murdered without horn");
                            map.put("tiger", "murdered");
                            map.put("elephant", "killed");
                            map.put("lion", "dead");
                            map.put("zebra", "dead");
                            map.put("leech", "cosmetics");
                            map.put("snail", "cosmetics experiment acid");

                            for (EntityAnnotation x : responses.get(0).getLabelAnnotations()) {
                                String tag = x.getDescription().toLowerCase();
                                if (map.containsKey(tag)) {
                                    description += tag + " " + map.get(tag);
                                    orig_description += tag + "";
                                }
                            }

                            // String description = responses.get(0).getLabelAnnotations().get(0).getDescription();

                            /** if (description.contains("rhino"))
                                description += " murdered without horn";
                            if (description.contains("tiger"))
                                description += " murdered";
                            if (description.contains("elephant"))
                                description += " killed";
                            if (description.contains("lion") || description.contains("zebra"))
                                description += " dead";
                            if (description.contains("lion") || description.contains("zebra"))
                                description += " dead";
                            if (description.contains("leech"))
                                description += " cosmetics";
                            if (description.contains("snail"))
                                description = "snail cosmetics experiment acid";
                            **/


                            Log.e("TAG", description);
                            Log.e("TAG", orig_description);

                            final String finalDescription = orig_description;
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (dialog != null) {
                                                dialog.cancel();
                                            }

                                            dialog = ProgressDialog.show(context, "",
                                                    "Buscando cara B de:\n" + finalDescription, true);
                                        }
                                    }, 500);
                                }
                            });

                            if (text.contains("#bside") || text.contains("#lacarab")) {
                                QwantImageSearchHelper.qwantImageSearchRequest(context,
                                        description, new QwantImageSearchHelper.QwantImageSearchResolvedCallback() {
                                            @Override
                                            public void onQwantImageSearchResolved(ArrayList<QwantImage> qwantImages) {
                                                feedItems.get(adapterPosition).URL = qwantImages.get(0).getMedia();

                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (dialog != null) {
                                                            dialog.cancel();
                                                        }
                                                        notifyItemChanged(adapterPosition);
                                                    }
                                                }, 2500);
                                            }
                                        });

                                ArrayList<String> tags = new ArrayList<String>();
                                for(String word : description.split(" ")) {
                                    tags.add(word);
                                }

                                newsData.getUrls(context, tags,
                                        new NewsData.OnNewsURLsResolved() {
                                    @Override
                                    public void onNewsURLsResolved(ArrayList<String> urls) {
                                        feedItems.get(adapterPosition).text = urls.toString();
                                        notifyItemChanged(adapterPosition);
                                        Log.e("TAG", urls.toString());
                                    }
                                });
                            }
                        }
                    }
                });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ((CellFeedViewHolder) viewHolder).bindView(feedItems.get(position));

        if (getItemViewType(position) == VIEW_TYPE_LOADER) {
            bindLoadingFeedItem((LoadingCellFeedViewHolder) viewHolder);
        }
    }

    private void bindLoadingFeedItem(final LoadingCellFeedViewHolder holder) {
        holder.loadingFeedItemView.setOnLoadingFinishedListener(new LoadingFeedItemView.OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                showLoadingView = false;
                notifyItemChanged(0);
            }
        });
        holder.loadingFeedItemView.startLoading();
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoadingView && position == 0) {
            return VIEW_TYPE_LOADER;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public void updateItems(String[] urls, String[] texts, String[] usernameArray, boolean animated) {

        feedItems.clear();
        for (int i = 0; i < urls.length; ++i) {
            Random r = new Random();
            int random_likes = r.nextInt(80 - 3) + 3;
            String text = "";
            if (i < texts.length) text = texts[i];
            else text = texts[0];
            feedItems.add(new FeedItem(urls[i],text,usernameArray[r.nextInt(5)],random_likes, false));
        }
        if (animated) {
            notifyItemRangeInserted(0, feedItems.size());
        } else {
            notifyDataSetChanged();
        }
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public void showLoadingView() {
        showLoadingView = true;
        notifyItemChanged(0);
    }

    public interface OnFeedItemClickListener {
        void onCommentsClick(View v, int position);

        void onMoreClick(View v, int position);

        void onProfileClick(View v);
    }

    public static class CellFeedViewHolder extends RecyclerView.ViewHolder {
        private final static String TAG = CellFeedViewHolder.class.getSimpleName();
        private final String[] tagArray;

        @BindView(R.id.tvFeedUser)
        TextView tvFeedUser;
        @BindView(R.id.ivFeedCenter)
        ImageView ivFeedCenter;
        @BindView(R.id.tvFeedBottom)
        TextView tvFeedBottom;
        @BindView(R.id.btnComments)
        ImageButton btnComments;
        @BindView(R.id.btnLike)
        ImageButton btnLike;
        @BindView(R.id.btnMore)
        ImageButton btnMore;
        @BindView(R.id.btnBSide)
        ImageButton btnBSide;
        @BindView(R.id.vBgLike)
        View vBgLike;
        @BindView(R.id.ivLike)
        ImageView ivLike;
        @BindView(R.id.tsLikesCounter)
        TextSwitcher tsLikesCounter;
        @BindView(R.id.ivUserProfile)
        ImageView ivUserProfile;
        @BindView(R.id.vImageRoot)
        FrameLayout vImageRoot;

        FeedItem feedItem;
        View view;

        public CellFeedViewHolder(View view) {
            super(view);
            this.view = view;

            tagArray = view.getContext().getResources().getStringArray(R.array.feed_tag);
            ButterKnife.bind(this, view);
        }

        public void bindView(FeedItem feedItem) {
            this.feedItem = feedItem;
            int adapterPosition = getAdapterPosition();
            int tagPos = adapterPosition % tagArray.length;

            loadImage(feedItem.URL);
            tvFeedUser.setText(feedItem.username);
            tvFeedBottom.setText(feedItem.text);
            btnLike.setImageResource(feedItem.isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline_grey);
            tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                    R.plurals.likes_count, feedItem.likesCount, feedItem.likesCount
            ));
        }

        private void loadImage(String url) {
            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            ivFeedCenter.setImageBitmap(bitmap);
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            Log.e("TAG", "Error downloading image");
                            Log.e("TAG", "" + error.toString());
                        }
                    });
            VolleyController.getInstance(view.getContext()).addToQueue(request);
        }

        public FeedItem getFeedItem() {
            return feedItem;
        }
    }

    public static class LoadingCellFeedViewHolder extends CellFeedViewHolder {
        LoadingFeedItemView loadingFeedItemView;

        public LoadingCellFeedViewHolder(LoadingFeedItemView view) {
            super(view);
            this.loadingFeedItemView = view;
        }

        @Override
        public void bindView(FeedItem feedItem) {
            super.bindView(feedItem);
        }
    }

    public static class FeedItem {
        public String URL;
        public String text;
        public String username;
        public int likesCount;
        public boolean isLiked;

        public FeedItem(String url, String text, String username, int likesCount, boolean isLiked) {
            this.URL = url;
            this.text = text;
            this.username = username;
            this.likesCount = likesCount;
            this.isLiked = isLiked;
        }
    }
}
