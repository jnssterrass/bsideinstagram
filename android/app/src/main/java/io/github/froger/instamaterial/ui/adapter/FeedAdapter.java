package io.github.froger.instamaterial.ui.adapter;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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

import com.google.api.services.vision.v1.model.AnnotateImageResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.controllers.GoogleVisionController;
import io.github.froger.instamaterial.helpers.QwantImageSearchHelper;
import io.github.froger.instamaterial.models.QwantImage;
import io.github.froger.instamaterial.ui.activity.MainActivity;
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
        // Not used because when liked, all re-draws and re-download
        /*cellFeedViewHolder.viewPager.setOnTouchListener(new View.OnTouchListener() {
                    private boolean moved;
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            moved = false;
                        }
                        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                            moved = true;
                        }
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            if (!moved) {
                                view.performClick();
                            }
                        }

                        return false;
                    }
                }
        );*/
        cellFeedViewHolder.viewPager.setOnClickListener(new View.OnClickListener() {
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
        cellFeedViewHolder.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onProfileClick(view);
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

    public void updateItems(boolean animated) {
        feedItems.clear();
        feedItems.addAll(Arrays.asList(
                new FeedItem(33, false),
                new FeedItem(1, false),
                new FeedItem(223, false),
                new FeedItem(2, false),
                new FeedItem(6, false),
                new FeedItem(8, false),
                new FeedItem(99, false)
        ));
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
        private final String[] textArray;
        private final String[] imageArray;
        private final String[] tagArray;

        @BindView(R.id.viewPager)
        ViewPager viewPager;
        @BindView(R.id.tvFeedBottom)
        TextView tvFeedBottom;
        @BindView(R.id.btnComments)
        ImageButton btnComments;
        @BindView(R.id.btnLike)
        ImageButton btnLike;
        @BindView(R.id.btnMore)
        ImageButton btnMore;
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
            textArray = view.getContext().getResources().getStringArray(R.array.feed_text);
            imageArray = view.getContext().getResources().getStringArray(R.array.feed_image);
            tagArray = view.getContext().getResources().getStringArray(R.array.feed_tag);
            ButterKnife.bind(this, view);
        }

        public void bindView(FeedItem feedItem) {
            this.feedItem = feedItem;
            int adapterPosition = getAdapterPosition();
            int textPos = adapterPosition % textArray.length;
            int imagePos = adapterPosition % imageArray.length;
            int tagPos = adapterPosition % tagArray.length;

            final ArrayList<String> images = new ArrayList<>();
            images.add(imageArray[imagePos]);
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(view.getContext(), images);
            viewPager.setAdapter(viewPagerAdapter);

            TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
            tabLayout.setupWithViewPager(viewPager, true);

            // TODO Dont comment this if you want to have slide
            String text = textArray[tagPos];
            Log.e("TAG", text + " --- #bside=" + text.contains("#bside"));
            Log.e("TAG", text + " --- #lacarab=" + text.contains("#lacarab"));
            addBSide(images, text);

            tvFeedBottom.setText(text);
            btnLike.setImageResource(feedItem.isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline_grey);
            tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                    R.plurals.likes_count, feedItem.likesCount, feedItem.likesCount
            ));
        }

        private void addBSide(final ArrayList<String> images, final String text) {
            GoogleVisionController.getInstance(view.getContext()).getLabels(images.get(0),
                    new GoogleVisionController.OnImageResponse() {
                        @Override
                        public void onImageResponse(List<AnnotateImageResponse> responses) {
                            if (responses != null && !responses.isEmpty() &&
                                    responses.get(0).getLabelAnnotations() != null &&
                                    !responses.get(0).getLabelAnnotations().isEmpty()) {
                                String description = responses.get(0).getLabelAnnotations().get(0).getDescription();
                                if (responses.get(0).getLabelAnnotations().size() > 1) {
                                    description += " " + responses.get(0).getLabelAnnotations().get(1).getDescription();
                                }
                                if (responses.get(0).getLabelAnnotations().size() > 2) {
                                    description += " " + responses.get(0).getLabelAnnotations().get(2).getDescription();
                                }

                                if (text.contains("#bside") || text.contains("#lacarab")) {
                                    QwantImageSearchHelper.qwantImageSearchRequest(view.getContext(),
                                            description, new QwantImageSearchHelper.QwantImageSearchResolvedCallback() {
                                                @Override
                                                public void onQwantImageSearchResolved(ArrayList<QwantImage> qwantImages) {
                                                    images.add(qwantImages.get(0).getMedia());

                                                    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(view.getContext(), images);
                                                    viewPager.setAdapter(viewPagerAdapter);
                                                }
                                            });
                                }
                            }
                        }
                    });
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
        public int likesCount;
        public boolean isLiked;

        public FeedItem(int likesCount, boolean isLiked) {
            this.likesCount = likesCount;
            this.isLiked = isLiked;
        }
    }
}
