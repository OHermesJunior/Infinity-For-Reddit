package ml.docilealligator.infinityforreddit.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import ml.docilealligator.infinityforreddit.Activity.BaseActivity;
import ml.docilealligator.infinityforreddit.Activity.FilteredThingActivity;
import ml.docilealligator.infinityforreddit.Activity.MainActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Adapter.PostRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.CustomView.CustomToroContainer;
import ml.docilealligator.infinityforreddit.Event.ChangeAutoplayNsfwVideosEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeDefaultPostLayoutEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeMuteAutoplayingVideosEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeMuteNSFWVideoEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.Event.ChangePostLayoutEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeShowAbsoluteNumberOfVotesEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeShowElapsedTimeEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeStartAutoplayVisibleAreaOffsetEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeTimeFormatEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeVideoAutoplayEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeVoteButtonsPositionEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeWifiStatusEvent;
import ml.docilealligator.infinityforreddit.Event.PostUpdateEventToPostList;
import ml.docilealligator.infinityforreddit.Event.ShowDividerInCompactLayoutPreferenceEvent;
import ml.docilealligator.infinityforreddit.Event.ShowThumbnailOnTheRightInCompactLayoutEvent;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.Post.Post;
import ml.docilealligator.infinityforreddit.Post.PostDataSource;
import ml.docilealligator.infinityforreddit.Post.PostViewModel;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import retrofit2.Retrofit;

import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_NAME = "EN";
    public static final String EXTRA_USER_NAME = "EUN";
    public static final String EXTRA_USER_WHERE = "EUW";
    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_POST_TYPE = "EPT";
    public static final String EXTRA_FILTER = "EF";
    public static final int EXTRA_NO_FILTER = -2;
    public static final String EXTRA_ACCESS_TOKEN = "EAT";

    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String RECYCLER_VIEW_POSITION_STATE = "RVPS";

    @BindView(R.id.swipe_refresh_layout_post_fragment)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view_post_fragment)
    CustomToroContainer mPostRecyclerView;
    @BindView(R.id.fetch_post_info_linear_layout_post_fragment)
    LinearLayout mFetchPostInfoLinearLayout;
    @BindView(R.id.fetch_post_info_image_view_post_fragment)
    ImageView mFetchPostInfoImageView;
    @BindView(R.id.fetch_post_info_text_view_post_fragment)
    TextView mFetchPostInfoTextView;
    PostViewModel mPostViewModel;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    ExoCreator exoCreator;
    private RequestManager mGlide;
    private AppCompatActivity activity;
    private LinearLayoutManager mLinearLayoutManager;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private int postType;
    private boolean isInLazyMode = false;
    private boolean isLazyModePaused = false;
    private boolean hasPost = false;
    private PostRecyclerViewAdapter mAdapter;
    private RecyclerView.SmoothScroller smoothScroller;
    private Window window;
    private Handler lazyModeHandler;
    private LazyModeRunnable lazyModeRunnable;
    private CountDownTimer resumeLazyModeCountDownTimer;
    private float lazyModeInterval;
    private int postLayout;
    private SortType sortType;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPostRecyclerView.getAdapter() != null) {
            ((PostRecyclerViewAdapter) mPostRecyclerView.getAdapter()).setCanStartActivity(true);
        }
        if (isInLazyMode && isLazyModePaused) {
            resumeLazyMode(false);
        }
        if (mAdapter != null && mPostRecyclerView != null) {
            mPostRecyclerView.onWindowVisibilityChanged(View.VISIBLE);
        }
    }

    private boolean scrollPostsByCount(int count) {
        if (mLinearLayoutManager != null) {
            int pos = mLinearLayoutManager.findFirstVisibleItemPosition();
            int targetPosition = pos + count;
            mLinearLayoutManager.scrollToPositionWithOffset(targetPosition, 0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean handleKeyDown(int keyCode) {
        boolean volumeKeysNavigatePosts = mSharedPreferences.getBoolean(SharedPreferencesUtils.VOLUME_KEYS_NAVIGATE_POSTS, false);
        if (volumeKeysNavigatePosts) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return scrollPostsByCount(-1);
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return scrollPostsByCount(1);
            }
        }
        return false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        EventBus.getDefault().register(this);

        applyTheme();

        lazyModeHandler = new Handler();

        lazyModeInterval = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.LAZY_MODE_INTERVAL_KEY, "2.5"));

        smoothScroller = new LinearSmoothScroller(activity) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        window = activity.getWindow();

        Resources resources = getResources();

        if ((activity instanceof BaseActivity && ((BaseActivity) activity).isImmersiveInterface())) {
            mPostRecyclerView.setPadding(0, 0, 0, ((BaseActivity) activity).getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                mPostRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && !resources.getBoolean(R.bool.isTablet)) {
            mLinearLayoutManager = new LinearLayoutManager(activity);
            mPostRecyclerView.setLayoutManager(mLinearLayoutManager);
        } else {
            mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            mPostRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
            StaggeredGridLayoutManagerItemOffsetDecoration itemDecoration =
                    new StaggeredGridLayoutManagerItemOffsetDecoration(activity, R.dimen.staggeredLayoutManagerItemOffset);
            mPostRecyclerView.addItemDecoration(itemDecoration);
        }

        mGlide = Glide.with(activity);

        lazyModeRunnable = new LazyModeRunnable() {

            @Override
            public void run() {
                if (isInLazyMode && !isLazyModePaused) {
                    int nPosts = mAdapter.getItemCount();
                    if (getCurrentPosition() == -1) {
                        if (mLinearLayoutManager != null) {
                            setCurrentPosition(mLinearLayoutManager.findFirstVisibleItemPosition());
                        } else {
                            int[] into = new int[2];
                            setCurrentPosition(mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[1]);
                        }
                    }

                    if (getCurrentPosition() != RecyclerView.NO_POSITION && nPosts > getCurrentPosition()) {
                        incrementCurrentPosition();
                        smoothScroller.setTargetPosition(getCurrentPosition());
                        if (mLinearLayoutManager != null) {
                            mLinearLayoutManager.startSmoothScroll(smoothScroller);
                        } else {
                            mStaggeredGridLayoutManager.startSmoothScroll(smoothScroller);
                        }
                    }
                }
                lazyModeHandler.postDelayed(this, (long) (lazyModeInterval * 1000));
            }
        };

        resumeLazyModeCountDownTimer = new CountDownTimer((long) (lazyModeInterval * 1000), (long) (lazyModeInterval * 1000)) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                resumeLazyMode(true);
            }
        };

        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);

        if (savedInstanceState != null) {
            int recyclerViewPosition = savedInstanceState.getInt(RECYCLER_VIEW_POSITION_STATE);
            if (recyclerViewPosition > 0) {
                mPostRecyclerView.scrollToPosition(recyclerViewPosition);
            }

            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            if (isInLazyMode) {
                resumeLazyMode(false);
            }
        }

        mPostRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if (isInLazyMode) {
                pauseLazyMode(true);
            }
            return false;
        });

        if (activity instanceof MainActivity) {
            mPostRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        ((MainActivity) activity).postScrollDown();
                    } else if (dy < 0) {
                        ((MainActivity) activity).postScrollUp();
                    }

                }
            });
        } else if (activity instanceof ViewSubredditDetailActivity) {
            mPostRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        ((ViewSubredditDetailActivity) activity).contentScrollDown();
                    } else if (dy < 0) {
                        ((ViewSubredditDetailActivity) activity).contentScrollUp();
                    }

                }
            });
        }

        postType = getArguments().getInt(EXTRA_POST_TYPE);

        int filter = getArguments().getInt(EXTRA_FILTER);
        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        boolean nsfw = mSharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_KEY, false);
        int defaultPostLayout = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_POST_LAYOUT_KEY, "0"));
        Locale locale = getResources().getConfiguration().locale;

        if (postType == PostDataSource.TYPE_SEARCH) {
            String subredditName = getArguments().getString(EXTRA_NAME);
            String query = getArguments().getString(EXTRA_QUERY);

            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SEARCH_POST, SortType.Type.RELEVANCE.name());
            String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_SEARCH_POST, SortType.Time.ALL.name());
            sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_SEARCH_POST, defaultPostLayout);

            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit, mRedditDataRoomDatabase,
                    customThemeWrapper, locale, accessToken, postType, postLayout, true,
                    mSharedPreferences, exoCreator, new PostRecyclerViewAdapter.Callback() {
                        @Override
                        public void retryLoadingMore() {
                            mPostViewModel.retryLoadingMore();
                        }

                        @Override
                        public void typeChipClicked(int filter) {
                            Intent intent = new Intent(activity, FilteredThingActivity.class);
                            intent.putExtra(FilteredThingActivity.EXTRA_NAME, subredditName);
                            intent.putExtra(FilteredThingActivity.EXTRA_QUERY, query);
                            intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, postType);
                            intent.putExtra(FilteredThingActivity.EXTRA_FILTER, filter);
                            startActivity(intent);
                        }
                    });

            if (accessToken == null) {
                mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mRetrofit, null,
                        getResources().getConfiguration().locale, subredditName, query, postType,
                        sortType, filter, nsfw)).get(PostViewModel.class);
            } else {
                mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mOauthRetrofit, accessToken,
                        getResources().getConfiguration().locale, subredditName, query, postType,
                        sortType, filter, nsfw)).get(PostViewModel.class);
            }
        } else if (postType == PostDataSource.TYPE_SUBREDDIT) {
            String subredditName = getArguments().getString(EXTRA_NAME);
            String sort;
            String sortTime = null;

            boolean displaySubredditName = subredditName != null && (subredditName.equals("popular") || subredditName.equals("all"));
            if(displaySubredditName) {
                if(subredditName.equals("popular")) {
                    sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_POPULAR_POST, SortType.Type.HOT.name());
                    if(sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                        sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_POPULAR_POST, SortType.Time.ALL.name());
                    }
                    postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_POPULAR_POST, defaultPostLayout);
                } else {
                    sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_ALL_POST, SortType.Type.HOT.name());
                    if(sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                        sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_ALL_POST, SortType.Time.ALL.name());
                    }
                    postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_ALL_POST, defaultPostLayout);
                }
            } else {
                sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SUBREDDIT_POST_BASE + subredditName, SortType.Type.HOT.name());
                if(sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                    sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_SUBREDDIT_POST_BASE + subredditName, SortType.Time.ALL.name());
                }
                postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE + subredditName, defaultPostLayout);
            }

            if(sortTime != null) {
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }

            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit, mRedditDataRoomDatabase,
                    customThemeWrapper, locale, accessToken, postType, postLayout, displaySubredditName,
                    mSharedPreferences, exoCreator, new PostRecyclerViewAdapter.Callback() {
                        @Override
                        public void retryLoadingMore() {
                            mPostViewModel.retryLoadingMore();
                        }

                        @Override
                        public void typeChipClicked(int filter) {
                            Intent intent = new Intent(activity, FilteredThingActivity.class);
                            intent.putExtra(FilteredThingActivity.EXTRA_NAME, subredditName);
                            intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, postType);
                            intent.putExtra(FilteredThingActivity.EXTRA_FILTER, filter);
                            startActivity(intent);
                        }
                    });

            if (accessToken == null) {
                mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mRetrofit, accessToken,
                        getResources().getConfiguration().locale, subredditName, postType, sortType,
                        filter, nsfw)).get(PostViewModel.class);
            } else {
                mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mOauthRetrofit, accessToken,
                        getResources().getConfiguration().locale, subredditName, postType, sortType,
                        filter, nsfw)).get(PostViewModel.class);
            }
        } else if(postType == PostDataSource.TYPE_MULTI_REDDIT) {
            String multiRedditPath = getArguments().getString(EXTRA_NAME);
            String sort;
            String sortTime = null;

            sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_MULTI_REDDIT_POST_BASE + multiRedditPath,
                    SortType.Type.HOT.name());
            if(sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_MULTI_REDDIT_POST_BASE + multiRedditPath,
                        SortType.Time.ALL.name());
            }
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_MULTI_REDDIT_POST_BASE + multiRedditPath,
                    defaultPostLayout);

            if(sortTime != null) {
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }

            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit, mRedditDataRoomDatabase,
                    customThemeWrapper, locale, accessToken, postType, postLayout, true,
                    mSharedPreferences, exoCreator, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void retryLoadingMore() {
                    mPostViewModel.retryLoadingMore();
                }

                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredThingActivity.class);
                    intent.putExtra(FilteredThingActivity.EXTRA_NAME, multiRedditPath);
                    intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredThingActivity.EXTRA_FILTER, filter);
                    startActivity(intent);
                }
            });

            if (accessToken == null) {
                mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mRetrofit, null,
                        getResources().getConfiguration().locale, multiRedditPath, postType, sortType,
                        filter, nsfw)).get(PostViewModel.class);
            } else {
                mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mOauthRetrofit, accessToken,
                        getResources().getConfiguration().locale, multiRedditPath, postType, sortType,
                        filter, nsfw)).get(PostViewModel.class);
            }
        } else if (postType == PostDataSource.TYPE_USER) {
            String username = getArguments().getString(EXTRA_USER_NAME);
            String where = getArguments().getString(EXTRA_USER_WHERE);
            if (where != null && where.equals(PostDataSource.USER_WHERE_SUBMITTED)) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mFetchPostInfoLinearLayout.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mFetchPostInfoLinearLayout.setLayoutParams(params);
            }

            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_USER_POST_BASE + username, SortType.Type.NEW.name());
            if(sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_USER_POST_BASE + username, SortType.Time.ALL.name());
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_USER_POST_BASE + username, defaultPostLayout);

            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit, mRedditDataRoomDatabase,
                    customThemeWrapper, locale, accessToken, postType, postLayout, true,
                    mSharedPreferences, exoCreator, new PostRecyclerViewAdapter.Callback() {
                        @Override
                        public void retryLoadingMore() {
                            mPostViewModel.retryLoadingMore();
                        }

                        @Override
                        public void typeChipClicked(int filter) {
                            Intent intent = new Intent(activity, FilteredThingActivity.class);
                            intent.putExtra(FilteredThingActivity.EXTRA_NAME, username);
                            intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, postType);
                            intent.putExtra(FilteredThingActivity.EXTRA_USER_WHERE, where);
                            intent.putExtra(FilteredThingActivity.EXTRA_FILTER, filter);
                            startActivity(intent);
                        }
                    });

            if (accessToken == null) {
                mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mRetrofit, accessToken,
                        getResources().getConfiguration().locale, username, postType, sortType, where,
                        filter, nsfw)).get(PostViewModel.class);
            } else {
                mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mOauthRetrofit, accessToken,
                        getResources().getConfiguration().locale, username, postType, sortType, where,
                        filter, nsfw)).get(PostViewModel.class);
            }
        } else {
            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_BEST_POST, SortType.Type.BEST.name());
            if(sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_BEST_POST, SortType.Time.ALL.name());
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST, defaultPostLayout);

            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit, mRedditDataRoomDatabase,
                    customThemeWrapper, locale, accessToken, postType, postLayout, true,
                    mSharedPreferences, exoCreator, new PostRecyclerViewAdapter.Callback() {
                        @Override
                        public void retryLoadingMore() {
                            mPostViewModel.retryLoadingMore();
                        }

                        @Override
                        public void typeChipClicked(int filter) {
                            Intent intent = new Intent(activity, FilteredThingActivity.class);
                            intent.putExtra(FilteredThingActivity.EXTRA_NAME, activity.getString(R.string.best));
                            intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, postType);
                            intent.putExtra(FilteredThingActivity.EXTRA_FILTER, filter);
                            startActivity(intent);
                        }
                    });

            mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mOauthRetrofit, accessToken,
                    getResources().getConfiguration().locale, postType, sortType, filter, nsfw)).get(PostViewModel.class);
        }

        if (activity instanceof ActivityToolbarInterface) {
            ((ActivityToolbarInterface) activity).displaySortType();
        }

        mPostRecyclerView.setAdapter(mAdapter);
        mPostRecyclerView.setCacheManager(mAdapter);
        mPostRecyclerView.setPlayerInitializer(order -> {
            VolumeInfo volumeInfo = new VolumeInfo(true, 0f);
            return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
        });

        mPostViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> mAdapter.submitList(posts));

        mPostViewModel.hasPost().observe(getViewLifecycleOwner(), hasPost -> {
            this.hasPost = hasPost;
            mSwipeRefreshLayout.setRefreshing(false);
            if (hasPost) {
                mFetchPostInfoLinearLayout.setVisibility(View.GONE);
            } else {
                if (isInLazyMode) {
                    stopLazyMode();
                }

                mFetchPostInfoLinearLayout.setOnClickListener(view -> {
                });
                showErrorView(R.string.no_posts);
            }
        });

        mPostViewModel.getInitialLoadingState().observe(getViewLifecycleOwner(), networkState -> {
            if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mSwipeRefreshLayout.setRefreshing(false);
            } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mSwipeRefreshLayout.setRefreshing(false);
                mFetchPostInfoLinearLayout.setOnClickListener(view -> refresh());
                showErrorView(R.string.load_posts_error);
            } else {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mPostViewModel.getPaginationNetworkState().observe(getViewLifecycleOwner(), networkState -> mAdapter.setNetworkState(networkState));

        return rootView;
    }

    public void changeSortType(SortType sortType) {
        if (mFetchPostInfoLinearLayout.getVisibility() != View.GONE) {
            mFetchPostInfoLinearLayout.setVisibility(View.GONE);
            mGlide.clear(mFetchPostInfoImageView);
        }
        mAdapter.removeFooter();
        hasPost = false;
        if (isInLazyMode) {
            stopLazyMode();
        }
        this.sortType = sortType;
        mPostViewModel.changeSortType(sortType);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (AppCompatActivity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        if (mLinearLayoutManager != null) {
            outState.putInt(RECYCLER_VIEW_POSITION_STATE, mLinearLayoutManager.findFirstVisibleItemPosition());
        } else if (mStaggeredGridLayoutManager != null) {
            int[] into = new int[2];
            outState.putInt(RECYCLER_VIEW_POSITION_STATE,
                    mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[0]);
        }
    }

    @Override
    public void refresh() {
        mAdapter.removeFooter();
        mFetchPostInfoLinearLayout.setVisibility(View.GONE);
        hasPost = false;
        if (isInLazyMode) {
            stopLazyMode();
        }
        mPostViewModel.refresh();
    }

    private void showErrorView(int stringResId) {
        if (activity != null && isAdded()) {
            mSwipeRefreshLayout.setRefreshing(false);
            mFetchPostInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchPostInfoTextView.setText(stringResId);
            mGlide.load(R.drawable.error_image).into(mFetchPostInfoImageView);
        }
    }

    @Override
    public void changeNSFW(boolean nsfw) {
        mPostViewModel.changeNSFW(nsfw);
    }

    @Override
    public boolean startLazyMode() {
        if (!hasPost) {
            Toast.makeText(activity, R.string.no_posts_no_lazy_mode, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mAdapter != null && mAdapter.isAutoplay()) {
            mAdapter.setAutoplay(false);
            refreshAdapter();
        }

        isInLazyMode = true;
        isLazyModePaused = false;

        lazyModeInterval = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.LAZY_MODE_INTERVAL_KEY, "2.5"));
        lazyModeHandler.postDelayed(lazyModeRunnable, (long) (lazyModeInterval * 1000));
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toast.makeText(activity, getString(R.string.lazy_mode_start, lazyModeInterval),
                Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void stopLazyMode() {
        if (mAdapter != null) {
            String autoplayString = mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
            if (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ALWAYS_ON) ||
                    (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI) && Utils.isConnectedToWifi(activity))) {
                mAdapter.setAutoplay(true);
                refreshAdapter();
            }
        }
        isInLazyMode = false;
        isLazyModePaused = false;
        lazyModeRunnable.resetOldPosition();
        lazyModeHandler.removeCallbacks(lazyModeRunnable);
        resumeLazyModeCountDownTimer.cancel();
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toast.makeText(activity, getString(R.string.lazy_mode_stop), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void resumeLazyMode(boolean resumeNow) {
        if (isInLazyMode) {
            if (mAdapter != null && mAdapter.isAutoplay()) {
                mAdapter.setAutoplay(false);
                refreshAdapter();
            }
            isLazyModePaused = false;
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            lazyModeRunnable.resetOldPosition();

            if (resumeNow) {
                lazyModeHandler.post(lazyModeRunnable);
            } else {
                lazyModeHandler.postDelayed(lazyModeRunnable, (long) (lazyModeInterval * 1000));
            }
        }
    }

    @Override
    public void pauseLazyMode(boolean startTimer) {
        resumeLazyModeCountDownTimer.cancel();
        isInLazyMode = true;
        isLazyModePaused = true;
        lazyModeHandler.removeCallbacks(lazyModeRunnable);
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (startTimer) {
            resumeLazyModeCountDownTimer.start();
        }
    }

    @Override
    public boolean isInLazyMode() {
        return isInLazyMode;
    }

    @Override
    public void changePostLayout(int postLayout) {
        if (mAdapter != null) {
            mAdapter.setPostLayout(postLayout);
            refreshAdapter();
        }
    }

    @Override
    public void applyTheme() {
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(customThemeWrapper.getCircularProgressBarBackground());
        mSwipeRefreshLayout.setColorSchemeColors(customThemeWrapper.getColorAccent());
        mFetchPostInfoTextView.setTextColor(customThemeWrapper.getSecondaryTextColor());
    }

    @Subscribe
    public void onPostUpdateEvent(PostUpdateEventToPostList event) {
        PagedList<Post> posts = mAdapter.getCurrentList();
        if (posts != null && event.positionInList >= 0 && event.positionInList < posts.size()) {
            Post post = posts.get(event.positionInList);
            if (post != null && post.getFullName().equals(event.post.getFullName())) {
                post.setTitle(event.post.getTitle());
                post.setVoteType(event.post.getVoteType());
                post.setScore(event.post.getScore());
                post.setNSFW(event.post.isNSFW());
                post.setHidden(event.post.isHidden());
                post.setSpoiler(event.post.isSpoiler());
                post.setFlair(event.post.getFlair());
                post.setSaved(event.post.isSaved());
                mAdapter.notifyItemChanged(event.positionInList);
            }
        }
    }

    @Subscribe
    public void onChangeShowElapsedTimeEvent(ChangeShowElapsedTimeEvent event) {
        if (mAdapter != null) {
            mAdapter.setShowElapsedTime(event.showElapsedTime);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeTimeFormatEvent(ChangeTimeFormatEvent changeTimeFormatEvent) {
        if (mAdapter != null) {
            mAdapter.setTimeFormat(changeTimeFormatEvent.timeFormat);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeVoteButtonsPositionEvent(ChangeVoteButtonsPositionEvent event) {
        if (mAdapter != null) {
            mAdapter.setVoteButtonsPosition(event.voteButtonsOnTheRight);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeNSFWBlurEvent(ChangeNSFWBlurEvent event) {
        if (mAdapter != null) {
            mAdapter.setBlurNSFW(event.needBlurNSFW);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeSpoilerBlurEvent(ChangeSpoilerBlurEvent event) {
        if (mAdapter != null) {
            mAdapter.setBlurSpoiler(event.needBlurSpoiler);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangePostLayoutEvent(ChangePostLayoutEvent event) {
        changePostLayout(event.postLayout);
    }

    @Subscribe
    public void onShowDividerInCompactLayoutPreferenceEvent(ShowDividerInCompactLayoutPreferenceEvent event) {
        if (mAdapter != null) {
            mAdapter.setShowDividerInCompactLayout(event.showDividerInCompactLayout);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeDefaultPostLayoutEvent(ChangeDefaultPostLayoutEvent changeDefaultPostLayoutEvent) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            switch (postType) {
                case PostDataSource.TYPE_SUBREDDIT:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE + bundle.getString(EXTRA_NAME))) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout);
                    }
                    break;
                case PostDataSource.TYPE_USER:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_USER_POST_BASE + bundle.getString(EXTRA_USER_NAME))) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout);
                    }
                    break;
                case PostDataSource.TYPE_MULTI_REDDIT:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_MULTI_REDDIT_POST_BASE + bundle.getString(EXTRA_NAME))) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout);
                    }
                    break;
                case PostDataSource.TYPE_SEARCH:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_SEARCH_POST)) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout);
                    }
                    break;
                case PostDataSource.TYPE_FRONT_PAGE:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST)) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout);
                    }
                    break;
            }
        }
    }

    @Subscribe
    public void onChangeShowAbsoluteNumberOfVotesEvent(ChangeShowAbsoluteNumberOfVotesEvent changeShowAbsoluteNumberOfVotesEvent) {
        if (mAdapter != null) {
            mAdapter.setShowAbsoluteNumberOfVotes(changeShowAbsoluteNumberOfVotesEvent.showAbsoluteNumberOfVotes);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeVideoAutoplayEvent(ChangeVideoAutoplayEvent changeVideoAutoplayEvent) {
        if (mAdapter != null) {
            boolean autoplay = false;
            if (changeVideoAutoplayEvent.autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ALWAYS_ON)) {
                autoplay = true;
            } else if (changeVideoAutoplayEvent.autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
                autoplay = Utils.isConnectedToWifi(activity);
            }
            mAdapter.setAutoplay(autoplay);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeAutoplayNsfwVideosEvent(ChangeAutoplayNsfwVideosEvent changeAutoplayNsfwVideosEvent) {
        if (mAdapter != null) {
            mAdapter.setAutoplayNsfwVideos(changeAutoplayNsfwVideosEvent.autoplayNsfwVideos);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeMuteAutoplayingVideosEvent(ChangeMuteAutoplayingVideosEvent changeMuteAutoplayingVideosEvent) {
        if (mAdapter != null) {
            mAdapter.setMuteAutoplayingVideos(changeMuteAutoplayingVideosEvent.muteAutoplayingVideos);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeWifiStatusEvent(ChangeWifiStatusEvent changeWifiStatusEvent) {
        if (mAdapter != null) {
            String autoplay = mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
            if (autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
                mAdapter.setAutoplay(changeWifiStatusEvent.isConnectedToWifi);
                refreshAdapter();
            }
        }
    }

    @Subscribe
    public void onShowThumbnailOnTheRightInCompactLayoutEvent(ShowThumbnailOnTheRightInCompactLayoutEvent showThumbnailOnTheRightInCompactLayoutEvent) {
        if (mAdapter != null) {
            mAdapter.setShowThumbnailOnTheRightInCompactLayout(showThumbnailOnTheRightInCompactLayoutEvent.showThumbnailOnTheRightInCompactLayout);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeStartAutoplayVisibleAreaOffsetEvent(ChangeStartAutoplayVisibleAreaOffsetEvent changeStartAutoplayVisibleAreaOffsetEvent) {
        if (mAdapter != null) {
            mAdapter.setStartAutoplayVisibleAreaOffset(changeStartAutoplayVisibleAreaOffsetEvent.startAutoplayVisibleAreaOffset);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeMuteNSFWVideoEvent(ChangeMuteNSFWVideoEvent changeMuteNSFWVideoEvent) {
        if (mAdapter != null) {
            mAdapter.setMuteNSFWVideo(changeMuteNSFWVideoEvent.muteNSFWVideo);
            refreshAdapter();
        }
    }

    private void refreshAdapter() {
        int previousPosition = -1;
        if (mLinearLayoutManager != null) {
            previousPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        } else if (mStaggeredGridLayoutManager != null) {
            int[] into = new int[2];
            previousPosition = mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[0];
        }

        RecyclerView.LayoutManager layoutManager = mPostRecyclerView.getLayoutManager();
        mPostRecyclerView.setAdapter(null);
        mPostRecyclerView.setLayoutManager(null);
        mPostRecyclerView.setAdapter(mAdapter);
        mPostRecyclerView.setLayoutManager(layoutManager);

        if (previousPosition > 0) {
            mPostRecyclerView.scrollToPosition(previousPosition);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
            if (isInLazyMode) {
                lazyModeRunnable.resetOldPosition();
            }
        } else if (mStaggeredGridLayoutManager != null) {
            mStaggeredGridLayoutManager.scrollToPositionWithOffset(0, 0);
            if (isInLazyMode) {
                lazyModeRunnable.resetOldPosition();
            }
        }
    }

    public SortType getSortType() {
        return sortType;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isInLazyMode) {
            pauseLazyMode(false);
        }
        if (mAdapter != null && mPostRecyclerView != null) {
            mPostRecyclerView.onWindowVisibilityChanged(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private static abstract class LazyModeRunnable implements Runnable {
        private int currentPosition = -1;

        int getCurrentPosition() {
            return currentPosition;
        }

        void setCurrentPosition(int currentPosition) {
            this.currentPosition = currentPosition;
        }

        void incrementCurrentPosition() {
            currentPosition++;
        }

        void resetOldPosition() {
            currentPosition = -1;
        }
    }

    private static class StaggeredGridLayoutManagerItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        StaggeredGridLayoutManagerItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        StaggeredGridLayoutManagerItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();

            int spanIndex = layoutParams.getSpanIndex();

            int halfOffset = mItemOffset / 2;

            if (spanIndex == 0) {
                outRect.set(0, 0, halfOffset, 0);
            } else {
                outRect.set(halfOffset, 0, 0, 0);
            }
        }
    }
}