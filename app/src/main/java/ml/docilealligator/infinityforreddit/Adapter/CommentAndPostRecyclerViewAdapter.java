package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.SuperscriptSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.libRG.CustomTextView;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.santalu.aspectratioimageview.AspectRatioImageView;

import org.commonmark.ext.gfm.tables.TableBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.CacheManager;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import io.noties.markwon.simple.ext.SimpleExtPlugin;
import io.noties.markwon.urlprocessor.UrlProcessorRelativeToAbsolute;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Activity.CommentActivity;
import ml.docilealligator.infinityforreddit.Activity.FilteredThingActivity;
import ml.docilealligator.infinityforreddit.Activity.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.AsyncTask.LoadSubredditIconAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.LoadUserDataAsyncTask;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.CommentMoreBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.CopyTextBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.ShareLinkBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Comment.Comment;
import ml.docilealligator.infinityforreddit.Comment.FetchComment;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.CustomView.AspectRatioGifImageView;
import ml.docilealligator.infinityforreddit.CustomView.MarkwonLinearLayoutManager;
import ml.docilealligator.infinityforreddit.Post.Post;
import ml.docilealligator.infinityforreddit.Post.PostDataSource;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.Utils.APIUtils;
import ml.docilealligator.infinityforreddit.Utils.GlideImageGetter;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import ml.docilealligator.infinityforreddit.VoteThing;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

import static ml.docilealligator.infinityforreddit.Activity.CommentActivity.WRITE_COMMENT_REQUEST_CODE;

public class CommentAndPostRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements CacheManager {
    private static final int VIEW_TYPE_POST_DETAIL_VIDEO_AUTOPLAY = 1;
    private static final int VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW = 2;
    private static final int VIEW_TYPE_POST_DETAIL_IMAGE_AND_GIF_AUTOPLAY = 3;
    private static final int VIEW_TYPE_POST_DETAIL_LINK = 4;
    private static final int VIEW_TYPE_POST_DETAIL_NO_PREVIEW_LINK = 5;
    private static final int VIEW_TYPE_POST_DETAIL_TEXT_TYPE = 6;
    private static final int VIEW_TYPE_FIRST_LOADING = 7;
    private static final int VIEW_TYPE_FIRST_LOADING_FAILED = 8;
    private static final int VIEW_TYPE_NO_COMMENT_PLACEHOLDER = 9;
    private static final int VIEW_TYPE_COMMENT = 10;
    private static final int VIEW_TYPE_COMMENT_FULLY_COLLAPSED = 11;
    private static final int VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS = 12;
    private static final int VIEW_TYPE_IS_LOADING_MORE_COMMENTS = 13;
    private static final int VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED = 14;
    private static final int VIEW_TYPE_VIEW_ALL_COMMENTS = 15;

    private AppCompatActivity mActivity;
    private Retrofit mRetrofit;
    private Retrofit mOauthRetrofit;
    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private RequestManager mGlide;
    private Markwon mPostDetailMarkwon;
    private Markwon mCommentMarkwon;
    private final MarkwonAdapter mMarkwonAdapter;
    private String mAccessToken;
    private String mAccountName;
    private Post mPost;
    private ArrayList<Comment> mVisibleComments;
    private String mSubredditNamePrefixed;
    private Locale mLocale;
    private String mSingleCommentId;
    private boolean mIsSingleCommentThreadMode;
    private boolean mNeedBlurNsfw;
    private boolean mNeedBlurSpoiler;
    private boolean mVoteButtonsOnTheRight;
    private boolean mShowElapsedTime;
    private String mTimeFormatPattern;
    private boolean mExpandChildren;
    private boolean mCommentToolbarHidden;
    private boolean mCommentToolbarHideOnClick;
    private boolean mSwapTapAndLong;
    private boolean mShowCommentDivider;
    private boolean mShowAbsoluteNumberOfVotes;
    private boolean mAutoplay = false;
    private boolean mAutoplayNsfwVideos;
    private boolean mMuteAutoplayingVideos;
    private boolean mFullyCollapseComment;
    private double mStartAutoplayVisibleAreaOffset;
    private boolean mMuteNSFWVideo;
    private CommentRecyclerViewAdapterCallback mCommentRecyclerViewAdapterCallback;
    private boolean isInitiallyLoading;
    private boolean isInitiallyLoadingFailed;
    private boolean mHasMoreComments;
    private boolean loadMoreCommentsFailed;

    private int mColorPrimaryLightTheme;
    private int mColorAccent;
    private int mCircularProgressBarBackgroundColor;
    private int mCardViewColor;
    private int mSecondaryTextColor;
    private int mPostTitleColor;
    private int mPrimaryTextColor;
    private int mCommentTextColor;
    private int mCommentBackgroundColor;
    private int mPostTypeBackgroundColor;
    private int mPostTypeTextColor;
    private int mDividerColor;
    private int mSubredditColor;
    private int mUsernameColor;
    private int mSubmitterColor;
    private int mModeratorColor;
    private int mAuthorFlairTextColor;
    private int mSpoilerBackgroundColor;
    private int mSpoilerTextColor;
    private int mFlairBackgroundColor;
    private int mFlairTextColor;
    private int mNSFWBackgroundColor;
    private int mNSFWTextColor;
    private int mArchivedTintColor;
    private int mLockedTintColor;
    private int mCrosspostTintColor;
    private int mNoPreviewLinkBackgroundColor;
    private int mUpvotedColor;
    private int mDownvotedColor;
    private int mCommentVerticalBarColor1;
    private int mCommentVerticalBarColor2;
    private int mCommentVerticalBarColor3;
    private int mCommentVerticalBarColor4;
    private int mCommentVerticalBarColor5;
    private int mCommentVerticalBarColor6;
    private int mCommentVerticalBarColor7;
    private int mSingleCommentThreadBackgroundColor;
    private int mVoteAndReplyUnavailableVoteButtonColor;
    private int mButtonTextColor;
    private int mPostIconAndInfoColor;
    private int mCommentIconAndInfoColor;
    private int mFullyCollapsedCommentBackgroundColor;
    private int mAwardedCommentBackgroundColor;

    private Drawable mCommentIcon;
    private float mScale;
    private ShareLinkBottomSheetFragment mShareLinkBottomSheetFragment;
    private CopyTextBottomSheetFragment mCopyTextBottomSheetFragment;
    private ExoCreator mExoCreator;

    public CommentAndPostRecyclerViewAdapter(AppCompatActivity activity, CustomThemeWrapper customThemeWrapper,
                                             Retrofit retrofit, Retrofit oauthRetrofit,
                                             RedditDataRoomDatabase redditDataRoomDatabase, RequestManager glide,
                                             String accessToken, String accountName, Post post, Locale locale,
                                             String singleCommentId, boolean isSingleCommentThreadMode,
                                             SharedPreferences sharedPreferences, ExoCreator exoCreator,
                                             CommentRecyclerViewAdapterCallback commentRecyclerViewAdapterCallback) {
        mActivity = activity;
        mRetrofit = retrofit;
        mOauthRetrofit = oauthRetrofit;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mGlide = glide;
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        int markdownColor = customThemeWrapper.getPostContentColor();
        int linkColor = customThemeWrapper.getLinkColor();
        mPostDetailMarkwon = Markwon.builder(mActivity)
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                        textView.setTextColor(markdownColor);
                        textView.setOnLongClickListener(view -> {
                            Bundle bundle = new Bundle();
                            bundle.putString(CopyTextBottomSheetFragment.EXTRA_RAW_TEXT, mPost.getSelfTextPlain());
                            bundle.putString(CopyTextBottomSheetFragment.EXTRA_MARKDOWN, mPost.getSelfText());
                            mCopyTextBottomSheetFragment.setArguments(bundle);
                            mCopyTextBottomSheetFragment.show(mActivity.getSupportFragmentManager(), mCopyTextBottomSheetFragment.getTag());
                            return true;
                        });
                    }

                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(link));
                            } else {
                                intent.setData(uri);
                            }
                            intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                            mActivity.startActivity(intent);
                        }).urlProcessor(new UrlProcessorRelativeToAbsolute("https://www.reddit.com"));
                    }

                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder.linkColor(linkColor);
                    }
                })
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(SimpleExtPlugin.create(plugin ->
                                plugin.addExtension(1, '^', (configuration, props) -> {
                                    return new SuperscriptSpan();
                                })
                        )
                )
                .usePlugin(TableEntryPlugin.create(mActivity))
                .build();
        mCommentMarkwon = Markwon.builder(mActivity)
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(link));
                            } else {
                                intent.setData(uri);
                            }
                            intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                            mActivity.startActivity(intent);
                        }).urlProcessor(new UrlProcessorRelativeToAbsolute("https://www.reddit.com"));
                    }

                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder.linkColor(linkColor);
                    }
                })
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(SimpleExtPlugin.create(plugin ->
                                plugin.addExtension(1, '^', (configuration, props) -> {
                                    return new SuperscriptSpan();
                                })
                        )
                )
                .build();
        mMarkwonAdapter = MarkwonAdapter.builder(R.layout.adapter_default_entry, R.id.text)
                .include(TableBlock.class, TableEntry.create(builder -> builder
                        .tableLayout(R.layout.adapter_table_block, R.id.table_layout)
                        .textLayoutIsRoot(R.layout.view_table_entry_cell)))
                .build();
        mAccessToken = accessToken;
        mAccountName = accountName;
        mPost = post;
        mVisibleComments = new ArrayList<>();
        mSubredditNamePrefixed = post.getSubredditNamePrefixed();
        mLocale = locale;
        mSingleCommentId = singleCommentId;
        mIsSingleCommentThreadMode = isSingleCommentThreadMode;

        mNeedBlurNsfw = sharedPreferences.getBoolean(SharedPreferencesUtils.BLUR_NSFW_KEY, true);
        mNeedBlurSpoiler = sharedPreferences.getBoolean(SharedPreferencesUtils.BLUR_SPOILER_KEY, false);
        mVoteButtonsOnTheRight = sharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
        mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
        mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
        mExpandChildren = !sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_TOP_LEVEL_COMMENTS_FIRST, false);
        mCommentToolbarHidden = sharedPreferences.getBoolean(SharedPreferencesUtils.COMMENT_TOOLBAR_HIDDEN, false);
        mCommentToolbarHideOnClick = sharedPreferences.getBoolean(SharedPreferencesUtils.COMMENT_TOOLBAR_HIDE_ON_CLICK, true);
        mSwapTapAndLong = sharedPreferences.getBoolean(SharedPreferencesUtils.SWAP_TAP_AND_LONG_COMMENTS, false);
        mShowCommentDivider = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_COMMENT_DIVIDER, false);
        mShowAbsoluteNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES, true);

        String autoplayString = sharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
        if (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ALWAYS_ON)) {
            mAutoplay = true;
        } else if (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
            mAutoplay = Utils.isConnectedToWifi(activity);
        }
        mAutoplayNsfwVideos = sharedPreferences.getBoolean(SharedPreferencesUtils.AUTOPLAY_NSFW_VIDEOS, true);
        mMuteAutoplayingVideos = sharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_AUTOPLAYING_VIDEOS, true);
        mFullyCollapseComment = sharedPreferences.getBoolean(SharedPreferencesUtils.FULLY_COLLAPSE_COMMENT, false);

        Resources resources = activity.getResources();
        mStartAutoplayVisibleAreaOffset = resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                sharedPreferences.getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_PORTRAIT, 75) / 100.0 :
                sharedPreferences.getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_LANDSCAPE, 50) / 100.0;

        mMuteNSFWVideo = sharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_NSFW_VIDEO, false);

        mCommentRecyclerViewAdapterCallback = commentRecyclerViewAdapterCallback;
        isInitiallyLoading = true;
        isInitiallyLoadingFailed = false;
        mHasMoreComments = false;
        loadMoreCommentsFailed = false;
        mScale = resources.getDisplayMetrics().density;

        mColorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        mColorAccent = customThemeWrapper.getColorAccent();
        mCircularProgressBarBackgroundColor = customThemeWrapper.getCircularProgressBarBackground();
        mCardViewColor = customThemeWrapper.getCardViewBackgroundColor();
        mPostTitleColor = customThemeWrapper.getPostTitleColor();
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mCommentTextColor = customThemeWrapper.getCommentColor();
        mDividerColor = customThemeWrapper.getDividerColor();
        mCommentBackgroundColor = customThemeWrapper.getCommentBackgroundColor();
        mPostTypeBackgroundColor = customThemeWrapper.getPostTypeBackgroundColor();
        mPostTypeTextColor = customThemeWrapper.getPostTypeTextColor();
        mSubmitterColor = customThemeWrapper.getSubmitter();
        mModeratorColor = customThemeWrapper.getModerator();
        mAuthorFlairTextColor = customThemeWrapper.getAuthorFlairTextColor();
        mSpoilerBackgroundColor = customThemeWrapper.getSpoilerBackgroundColor();
        mSpoilerTextColor = customThemeWrapper.getSpoilerTextColor();
        mNSFWBackgroundColor = customThemeWrapper.getNsfwBackgroundColor();
        mNSFWTextColor = customThemeWrapper.getNsfwTextColor();
        mArchivedTintColor = customThemeWrapper.getArchivedIconTint();
        mLockedTintColor = customThemeWrapper.getLockedIconTint();
        mCrosspostTintColor = customThemeWrapper.getCrosspostIconTint();
        mNoPreviewLinkBackgroundColor = customThemeWrapper.getNoPreviewLinkBackgroundColor();
        mFlairBackgroundColor = customThemeWrapper.getFlairBackgroundColor();
        mFlairTextColor = customThemeWrapper.getFlairTextColor();
        mSubredditColor = customThemeWrapper.getSubreddit();
        mUsernameColor = customThemeWrapper.getUsername();
        mUpvotedColor = customThemeWrapper.getUpvoted();
        mDownvotedColor = customThemeWrapper.getDownvoted();
        mCommentVerticalBarColor1 = customThemeWrapper.getCommentVerticalBarColor1();
        mCommentVerticalBarColor2 = customThemeWrapper.getCommentVerticalBarColor2();
        mCommentVerticalBarColor3 = customThemeWrapper.getCommentVerticalBarColor3();
        mCommentVerticalBarColor4 = customThemeWrapper.getCommentVerticalBarColor4();
        mCommentVerticalBarColor5 = customThemeWrapper.getCommentVerticalBarColor5();
        mCommentVerticalBarColor6 = customThemeWrapper.getCommentVerticalBarColor6();
        mCommentVerticalBarColor7 = customThemeWrapper.getCommentVerticalBarColor7();
        mSingleCommentThreadBackgroundColor = customThemeWrapper.getSingleCommentThreadBackgroundColor();
        mVoteAndReplyUnavailableVoteButtonColor = customThemeWrapper.getVoteAndReplyUnavailableButtonColor();
        mButtonTextColor = customThemeWrapper.getButtonTextColor();
        mPostIconAndInfoColor = customThemeWrapper.getPostIconAndInfoColor();
        mCommentIconAndInfoColor = customThemeWrapper.getCommentIconAndInfoColor();
        mFullyCollapsedCommentBackgroundColor = customThemeWrapper.getFullyCollapsedCommentBackgroundColor();
        mAwardedCommentBackgroundColor = customThemeWrapper.getAwardedCommentBackgroundColor();

        mCommentIcon = activity.getDrawable(R.drawable.ic_comment_grey_24dp);
        if (mCommentIcon != null) {
            DrawableCompat.setTint(mCommentIcon, mPostIconAndInfoColor);
        }

        mShareLinkBottomSheetFragment = new ShareLinkBottomSheetFragment();
        mCopyTextBottomSheetFragment = new CopyTextBottomSheetFragment();
        mExoCreator = exoCreator;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            switch (mPost.getPostType()) {
                case Post.VIDEO_TYPE:
                    if (mAutoplay) {
                        if (!mAutoplayNsfwVideos && mPost.isNSFW()) {
                            return VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW;
                        }
                        return VIEW_TYPE_POST_DETAIL_VIDEO_AUTOPLAY;
                    } else {
                        return VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW;
                    }
                case Post.GIF_TYPE:
                    if (mAutoplay) {
                        if (!mAutoplayNsfwVideos && mPost.isNSFW()) {
                            return VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW;
                        }
                        return VIEW_TYPE_POST_DETAIL_IMAGE_AND_GIF_AUTOPLAY;
                    } else {
                        return VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW;
                    }
                case Post.IMAGE_TYPE:
                    return VIEW_TYPE_POST_DETAIL_IMAGE_AND_GIF_AUTOPLAY;
                case Post.LINK_TYPE:
                    return VIEW_TYPE_POST_DETAIL_LINK;
                case Post.NO_PREVIEW_LINK_TYPE:
                    return VIEW_TYPE_POST_DETAIL_NO_PREVIEW_LINK;
                default:
                    return VIEW_TYPE_POST_DETAIL_TEXT_TYPE;

            }
        }

        if (mVisibleComments.size() == 0) {
            if (position == 1) {
                if (isInitiallyLoading) {
                    return VIEW_TYPE_FIRST_LOADING;
                } else if (isInitiallyLoadingFailed) {
                    return VIEW_TYPE_FIRST_LOADING_FAILED;
                } else {
                    return VIEW_TYPE_NO_COMMENT_PLACEHOLDER;
                }
            }
        }

        if (mIsSingleCommentThreadMode) {
            if (position == 1) {
                return VIEW_TYPE_VIEW_ALL_COMMENTS;
            }

            if (position == mVisibleComments.size() + 2) {
                if (mHasMoreComments) {
                    return VIEW_TYPE_IS_LOADING_MORE_COMMENTS;
                } else {
                    return VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED;
                }
            }

            Comment comment = mVisibleComments.get(position - 2);
            if (!comment.isPlaceHolder()) {
                if (mFullyCollapseComment && comment.hasReply() && !comment.isExpanded() && comment.hasExpandedBefore()) {
                    return VIEW_TYPE_COMMENT_FULLY_COLLAPSED;
                }
                return VIEW_TYPE_COMMENT;
            } else {
                return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
            }
        } else {
            if (position == mVisibleComments.size() + 1) {
                if (mHasMoreComments) {
                    return VIEW_TYPE_IS_LOADING_MORE_COMMENTS;
                } else {
                    return VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED;
                }
            }

            Comment comment = mVisibleComments.get(position - 1);
            if (!comment.isPlaceHolder()) {
                if (mFullyCollapseComment && comment.hasReply() && !comment.isExpanded() && comment.hasExpandedBefore()) {
                    return VIEW_TYPE_COMMENT_FULLY_COLLAPSED;
                }
                return VIEW_TYPE_COMMENT;
            } else {
                return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_POST_DETAIL_VIDEO_AUTOPLAY:
                return new PostDetailVideoAutoplayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_detail_video_autoplay, parent, false));
            case VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW:
                return new PostDetailVideoAndGifPreviewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_detail_video_and_gif_preview, parent, false));
            case VIEW_TYPE_POST_DETAIL_IMAGE_AND_GIF_AUTOPLAY:
                return new PostDetailImageAndGifAutoplayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_detail_image_and_gif_autoplay, parent, false));
            case VIEW_TYPE_POST_DETAIL_LINK:
                return new PostDetailLinkViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_detail_link, parent, false));
            case VIEW_TYPE_POST_DETAIL_NO_PREVIEW_LINK:
                return new PostDetailNoPreviewLinkViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_detail_no_preview_link, parent, false));
            case VIEW_TYPE_POST_DETAIL_TEXT_TYPE:
                return new PostDetailTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_detail_text, parent, false));
            case VIEW_TYPE_FIRST_LOADING:
                return new LoadCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_comments, parent, false));
            case VIEW_TYPE_FIRST_LOADING_FAILED:
                return new LoadCommentsFailedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_comments_failed_placeholder, parent, false));
            case VIEW_TYPE_NO_COMMENT_PLACEHOLDER:
                return new NoCommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_no_comment_placeholder, parent, false));
            case VIEW_TYPE_COMMENT:
                return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
            case VIEW_TYPE_COMMENT_FULLY_COLLAPSED:
                return new CommentFullyCollapsedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_fully_collapsed, parent, false));
            case VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS:
                return new LoadMoreChildCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more_comments_placeholder, parent, false));
            case VIEW_TYPE_IS_LOADING_MORE_COMMENTS:
                return new IsLoadingMoreCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_footer_loading, parent, false));
            case VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED:
                return new LoadMoreCommentsFailedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_footer_error, parent, false));
            default:
                return new ViewAllCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_all_comments, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostDetailBaseViewHolder) {
            ((PostDetailBaseViewHolder) holder).mTitleTextView.setText(mPost.getTitle());
            if (mPost.getSubredditNamePrefixed().startsWith("u/")) {
                if (mPost.getAuthorIconUrl() == null) {
                    String authorName = mPost.getAuthor().equals("[deleted]") ? mPost.getSubredditNamePrefixed().substring(2) : mPost.getAuthor();
                    new LoadUserDataAsyncTask(mRedditDataRoomDatabase.userDao(), authorName, mOauthRetrofit, iconImageUrl -> {
                        if (mActivity != null && getItemCount() > 0) {
                            if (iconImageUrl == null || iconImageUrl.equals("")) {
                                mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                        .into(((PostDetailBaseViewHolder) holder).mIconGifImageView);
                            } else {
                                mGlide.load(iconImageUrl)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                        .into(((PostDetailBaseViewHolder) holder).mIconGifImageView);
                            }

                            if (holder.getAdapterPosition() >= 0) {
                                mPost.setAuthorIconUrl(iconImageUrl);
                            }
                        }
                    }).execute();
                } else if (!mPost.getAuthorIconUrl().equals("")) {
                    mGlide.load(mPost.getAuthorIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((PostDetailBaseViewHolder) holder).mIconGifImageView);
                } else {
                    mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((PostDetailBaseViewHolder) holder).mIconGifImageView);
                }
            } else {
                if (mPost.getSubredditIconUrl() == null) {
                    new LoadSubredditIconAsyncTask(
                            mRedditDataRoomDatabase, mPost.getSubredditNamePrefixed().substring(2),
                            mRetrofit, iconImageUrl -> {
                        if (iconImageUrl == null || iconImageUrl.equals("")) {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(((PostDetailBaseViewHolder) holder).mIconGifImageView);
                        } else {
                            mGlide.load(iconImageUrl)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((PostDetailBaseViewHolder) holder).mIconGifImageView);
                        }

                        mPost.setSubredditIconUrl(iconImageUrl);
                    }).execute();
                } else if (!mPost.getSubredditIconUrl().equals("")) {
                    mGlide.load(mPost.getSubredditIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((PostDetailBaseViewHolder) holder).mIconGifImageView);
                } else {
                    mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((PostDetailBaseViewHolder) holder).mIconGifImageView);
                }
            }

            if (mPost.getAuthorFlairHTML() != null && !mPost.getAuthorFlairHTML().equals("")) {
                ((PostDetailBaseViewHolder) holder).mAuthorFlairTextView.setVisibility(View.VISIBLE);
                Utils.setHTMLWithImageToTextView(((PostDetailBaseViewHolder) holder).mAuthorFlairTextView, mPost.getAuthorFlairHTML());
            } else if (mPost.getAuthorFlair() != null && !mPost.getAuthorFlair().equals("")) {
                ((PostDetailBaseViewHolder) holder).mAuthorFlairTextView.setVisibility(View.VISIBLE);
                ((PostDetailBaseViewHolder) holder).mAuthorFlairTextView.setText(mPost.getAuthorFlair());
            }

            switch (mPost.getVoteType()) {
                case 1:
                    //Upvote
                    ((PostDetailBaseViewHolder) holder).mUpvoteButton.setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                    ((PostDetailBaseViewHolder) holder).mScoreTextView.setTextColor(mUpvotedColor);
                    break;
                case -1:
                    //Downvote
                    ((PostDetailBaseViewHolder) holder).mDownvoteButton.setColorFilter(mDownvotedColor, PorterDuff.Mode.SRC_IN);
                    ((PostDetailBaseViewHolder) holder).mScoreTextView.setTextColor(mDownvotedColor);
                    break;
                case 0:
                    ((PostDetailBaseViewHolder) holder).mUpvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    ((PostDetailBaseViewHolder) holder).mDownvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    ((PostDetailBaseViewHolder) holder).mScoreTextView.setTextColor(mPostIconAndInfoColor);
            }

            if (mPost.isArchived()) {
                ((PostDetailBaseViewHolder) holder).mUpvoteButton
                        .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
                ((PostDetailBaseViewHolder) holder).mDownvoteButton
                        .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
            }

            if (mPost.isCrosspost()) {
                ((PostDetailBaseViewHolder) holder).mCrosspostImageView.setVisibility(View.VISIBLE);
            }

            ((PostDetailBaseViewHolder) holder).mSubredditTextView.setText(mPost.getSubredditNamePrefixed());
            ((PostDetailBaseViewHolder) holder).mUserTextView.setText(mPost.getAuthorNamePrefixed());

            if (mShowElapsedTime) {
                ((PostDetailBaseViewHolder) holder).mPostTimeTextView.setText(
                        Utils.getElapsedTime(mActivity, mPost.getPostTimeMillis()));
            } else {
                ((PostDetailBaseViewHolder) holder).mPostTimeTextView.setText(Utils.getFormattedTime(mLocale, mPost.getPostTimeMillis(), mTimeFormatPattern));
            }

            if (mPost.isArchived()) {
                ((PostDetailBaseViewHolder) holder).mArchivedImageView.setVisibility(View.VISIBLE);
            }

            if (mPost.isLocked()) {
                ((PostDetailBaseViewHolder) holder).mLockedImageView.setVisibility(View.VISIBLE);
            }

            if (mPost.isSpoiler()) {
                ((PostDetailBaseViewHolder) holder).mSpoilerTextView.setVisibility(View.VISIBLE);
            }

            if (mPost.getFlair() != null && !mPost.getFlair().equals("")) {
                ((PostDetailBaseViewHolder) holder).mFlairTextView.setVisibility(View.VISIBLE);
                Spannable flairHTML;
                GlideImageGetter glideImageGetter = new GlideImageGetter(((PostDetailBaseViewHolder) holder).mFlairTextView);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    flairHTML = (Spannable) Html.fromHtml(mPost.getFlair(), Html.FROM_HTML_MODE_LEGACY, glideImageGetter, null);
                } else {
                    flairHTML = (Spannable) Html.fromHtml(mPost.getFlair(), glideImageGetter, null);
                }
                ((PostDetailBaseViewHolder) holder).mFlairTextView.setText(flairHTML);
            }

            if (mPost.getAwards() != null && !mPost.getAwards().equals("")) {
                ((PostDetailBaseViewHolder) holder).mAwardsTextView.setVisibility(View.VISIBLE);
                Utils.setHTMLWithImageToTextView(((PostDetailBaseViewHolder) holder).mAwardsTextView, mPost.getAwards());
            }

            if (mPost.isNSFW()) {
                ((PostDetailBaseViewHolder) holder).mNSFWTextView.setVisibility(View.VISIBLE);
            } else {
                ((PostDetailBaseViewHolder) holder).mNSFWTextView.setVisibility(View.GONE);
            }

            ((PostDetailBaseViewHolder) holder).mScoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, mPost.getScore() + mPost.getVoteType()));

            ((PostDetailBaseViewHolder) holder).commentsCountTextView.setText(Integer.toString(mPost.getNComments()));

            if (mPost.isSaved()) {
                ((PostDetailBaseViewHolder) holder).mSaveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
            } else {
                ((PostDetailBaseViewHolder) holder).mSaveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
            }

            if (holder instanceof PostDetailVideoAutoplayViewHolder) {
                ((PostDetailVideoAutoplayViewHolder) holder).aspectRatioFrameLayout.setAspectRatio((float) mPost.getPreviewWidth() / mPost.getPreviewHeight());
                ((PostDetailVideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.VISIBLE);
                mGlide.load(mPost.getPreviewUrl()).apply(RequestOptions.noTransformation()).into(((PostDetailVideoAutoplayViewHolder) holder).previewImageView);
                ((PostDetailVideoAutoplayViewHolder) holder).setVolume(mMuteAutoplayingVideos || (mPost.isNSFW() && mMuteNSFWVideo) ? 0f : 1f);
                ((PostDetailVideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(mPost.getVideoUrl()));
            } else if (holder instanceof PostDetailVideoAndGifPreviewHolder) {
                if (mPost.getPostType() == Post.GIF_TYPE) {
                    ((PostDetailVideoAndGifPreviewHolder) holder).mTypeTextView.setText(mActivity.getString(R.string.gif));
                } else {
                    ((PostDetailVideoAndGifPreviewHolder) holder).mTypeTextView.setText(mActivity.getString(R.string.video));
                }
                ((PostDetailVideoAndGifPreviewHolder) holder).mImageView.setRatio((float) mPost.getPreviewHeight() / (float) mPost.getPreviewWidth());
                loadImage((PostDetailVideoAndGifPreviewHolder) holder);
            } else if (holder instanceof PostDetailImageAndGifAutoplayViewHolder) {
                if (mPost.getPostType() == Post.GIF_TYPE) {
                    ((PostDetailImageAndGifAutoplayViewHolder) holder).mTypeTextView.setText(mActivity.getString(R.string.gif));
                } else {
                    ((PostDetailImageAndGifAutoplayViewHolder) holder).mTypeTextView.setText(mActivity.getString(R.string.image));
                }

                if (mPost.getPreviewWidth() <= 0 || mPost.getPreviewHeight() <= 0) {
                    ((PostDetailImageAndGifAutoplayViewHolder) holder).mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ((PostDetailImageAndGifAutoplayViewHolder) holder).mImageView.getLayoutParams().height = (int) (400 * mScale);
                }

                ((PostDetailImageAndGifAutoplayViewHolder) holder).mImageView.setRatio((float) mPost.getPreviewHeight() / (float) mPost.getPreviewWidth());
                loadImage((PostDetailImageAndGifAutoplayViewHolder) holder);
            } else if (holder instanceof PostDetailLinkViewHolder) {
                String domain = Uri.parse(mPost.getUrl()).getHost();
                ((PostDetailLinkViewHolder) holder).mLinkTextView.setText(domain);
                ((PostDetailLinkViewHolder) holder).mImageView.setRatio((float) mPost.getPreviewHeight() / (float) mPost.getPreviewWidth());
                loadImage((PostDetailLinkViewHolder) holder);
            } else if (holder instanceof PostDetailNoPreviewLinkViewHolder) {
                String noPreviewLinkDomain = Uri.parse(mPost.getUrl()).getHost();
                ((PostDetailNoPreviewLinkViewHolder) holder).mLinkTextView.setText(noPreviewLinkDomain);

                if (mPost.getSelfText() != null && !mPost.getSelfText().equals("")) {
                    ((PostDetailNoPreviewLinkViewHolder) holder).mContentMarkdownView.setVisibility(View.VISIBLE);
                    LinearLayoutManager linearLayoutManager = new MarkwonLinearLayoutManager(mActivity, new MarkwonLinearLayoutManager.HorizontalScrollViewScrolledListener() {
                        @Override
                        public void onScrolledLeft() {
                            ((ViewPostDetailActivity) mActivity).lockSwipeRightToGoBack();
                        }

                        @Override
                        public void onScrolledRight() {
                            ((ViewPostDetailActivity) mActivity).unlockSwipeRightToGoBack();
                        }
                    });
                    ((PostDetailNoPreviewLinkViewHolder) holder).mContentMarkdownView.setLayoutManager(linearLayoutManager);
                    ((PostDetailNoPreviewLinkViewHolder) holder).mContentMarkdownView.setAdapter(mMarkwonAdapter);
                    mMarkwonAdapter.setMarkdown(mPostDetailMarkwon, mPost.getSelfText());
                    mMarkwonAdapter.notifyDataSetChanged();
                }
            } else if (holder instanceof PostDetailTextViewHolder) {
                if (mPost.getSelfText() != null && !mPost.getSelfText().equals("")) {
                    ((PostDetailTextViewHolder) holder).mContentMarkdownView.setVisibility(View.VISIBLE);
                    LinearLayoutManager linearLayoutManager = new MarkwonLinearLayoutManager(mActivity, new MarkwonLinearLayoutManager.HorizontalScrollViewScrolledListener() {
                        @Override
                        public void onScrolledLeft() {
                            ((ViewPostDetailActivity) mActivity).lockSwipeRightToGoBack();
                        }

                        @Override
                        public void onScrolledRight() {
                            ((ViewPostDetailActivity) mActivity).unlockSwipeRightToGoBack();
                        }
                    });
                    ((PostDetailTextViewHolder) holder).mContentMarkdownView.setLayoutManager(linearLayoutManager);
                    ((PostDetailTextViewHolder) holder).mContentMarkdownView.setAdapter(mMarkwonAdapter);
                    mMarkwonAdapter.setMarkdown(mPostDetailMarkwon, mPost.getSelfText());
                    mMarkwonAdapter.notifyDataSetChanged();
                }
            }
        } else if (holder instanceof CommentViewHolder) {
            Comment comment;
            if (mIsSingleCommentThreadMode) {
                comment = mVisibleComments.get(holder.getAdapterPosition() - 2);
            } else {
                comment = mVisibleComments.get(holder.getAdapterPosition() - 1);
            }

            if (mIsSingleCommentThreadMode && comment.getId().equals(mSingleCommentId)) {
                ((CommentViewHolder) holder).itemView.setBackgroundColor(mSingleCommentThreadBackgroundColor);
            } else if (comment.getAwards() != null && !comment.getAwards().equals("")) {
                ((CommentViewHolder) holder).itemView.setBackgroundColor(mAwardedCommentBackgroundColor);
            }

            String authorPrefixed = "u/" + "asdasdfasfadfafasfasfsafasdfasdfasfjk as df adsf asf a sfas f";
            ((CommentViewHolder) holder).authorTextView.setText(authorPrefixed);
            ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
            ((CommentViewHolder) holder).authorFlairTextView.setText("asfafaf");

            /*if (comment.getAuthorFlairHTML() != null && !comment.getAuthorFlairHTML().equals("")) {
                ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                Utils.setHTMLWithImageToTextView(((CommentViewHolder) holder).authorFlairTextView, comment.getAuthorFlairHTML());
            } else if (comment.getAuthorFlair() != null && !comment.getAuthorFlair().equals("")) {
                ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                ((CommentViewHolder) holder).authorFlairTextView.setText(comment.getAuthorFlair());
            }*/

            if (comment.isSubmitter()) {
                ((CommentViewHolder) holder).authorTextView.setTextColor(mSubmitterColor);
                Drawable submitterDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_mic_14dp, mSubmitterColor);
                ((CommentViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                        submitterDrawable, null, null, null);
            } else if (comment.isModerator()) {
                ((CommentViewHolder) holder).authorTextView.setTextColor(mModeratorColor);
                Drawable moderatorDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_verified_user_14dp, mModeratorColor);
                ((CommentViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                        moderatorDrawable, null, null, null);
            }

            if (mShowElapsedTime) {
                ((CommentViewHolder) holder).commentTimeTextView.setText(
                        Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
            } else {
                ((CommentViewHolder) holder).commentTimeTextView.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
            }

            if (mCommentToolbarHidden) {
                ((CommentViewHolder) holder).bottomConstraintLayout.getLayoutParams().height = 0;
                ((CommentViewHolder) holder).topScoreTextView.setVisibility(View.VISIBLE);
            } else {
                ((CommentViewHolder) holder).bottomConstraintLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                ((CommentViewHolder) holder).topScoreTextView.setVisibility(View.GONE);
            }

            if (comment.getAwards() != null && !comment.getAwards().equals("")) {
                ((CommentViewHolder) holder).awardsTextView.setVisibility(View.VISIBLE);
                Spannable awardsHTML;
                GlideImageGetter glideImageGetter = new GlideImageGetter(((CommentViewHolder) holder).awardsTextView);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    awardsHTML = (Spannable) Html.fromHtml(comment.getAwards(), Html.FROM_HTML_MODE_LEGACY, glideImageGetter, null);
                } else {
                    awardsHTML = (Spannable) Html.fromHtml(comment.getAwards(), glideImageGetter, null);
                }
                ((CommentViewHolder) holder).awardsTextView.setText(awardsHTML);
            }

            mCommentMarkwon.setMarkdown(((CommentViewHolder) holder).commentMarkdownView, comment.getCommentMarkdown());
            ((CommentViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                    comment.getScore() + comment.getVoteType()));
            ((CommentViewHolder) holder).topScoreTextView.setText(mActivity.getString(R.string.top_score,
                    Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                    comment.getScore() + comment.getVoteType())));

            ((CommentViewHolder) holder).itemView.setPadding(comment.getDepth() * 8, 0, 0, 0);
            if (comment.getDepth() > 0) {
                switch (comment.getDepth() % 7) {
                    case 0:
                        ((CommentViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor7);
                        break;
                    case 1:
                        ((CommentViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor1);
                        break;
                    case 2:
                        ((CommentViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor2);
                        break;
                    case 3:
                        ((CommentViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor3);
                        break;
                    case 4:
                        ((CommentViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor4);
                        break;
                    case 5:
                        ((CommentViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor5);
                        break;
                    case 6:
                        ((CommentViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor6);
                        break;
                }
                ViewGroup.LayoutParams params = ((CommentViewHolder) holder).verticalBlock.getLayoutParams();
                params.width = 8;
                ((CommentViewHolder) holder).verticalBlock.setLayoutParams(params);
            }

            if (comment.hasReply()) {
                if (comment.isExpanded()) {
                    ((CommentViewHolder) holder).expandButton.setImageResource(R.drawable.ic_expand_less_grey_24dp);
                } else {
                    ((CommentViewHolder) holder).expandButton.setImageResource(R.drawable.ic_expand_more_grey_24dp);
                }
                ((CommentViewHolder) holder).expandButton.setVisibility(View.VISIBLE);
            }

            switch (comment.getVoteType()) {
                case Comment.VOTE_TYPE_UPVOTE:
                    ((CommentViewHolder) holder).upvoteButton
                            .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    ((CommentViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                    break;
                case Comment.VOTE_TYPE_DOWNVOTE:
                    ((CommentViewHolder) holder).downvoteButton
                            .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    ((CommentViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                    break;
            }

            if (mPost.isArchived()) {
                ((CommentViewHolder) holder).replyButton
                        .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                android.graphics.PorterDuff.Mode.SRC_IN);
                ((CommentViewHolder) holder).upvoteButton
                        .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                android.graphics.PorterDuff.Mode.SRC_IN);
                ((CommentViewHolder) holder).downvoteButton
                        .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                android.graphics.PorterDuff.Mode.SRC_IN);
            }

            if (mPost.isLocked()) {
                ((CommentViewHolder) holder).replyButton
                        .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                android.graphics.PorterDuff.Mode.SRC_IN);
            }

            if (comment.isSaved()) {
                ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
            } else {
                ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
            }
        } else if (holder instanceof CommentFullyCollapsedViewHolder) {
            Comment comment;
            if (mIsSingleCommentThreadMode) {
                comment = mVisibleComments.get(holder.getAdapterPosition() - 2);
            } else {
                comment = mVisibleComments.get(holder.getAdapterPosition() - 1);
            }

            String authorWithPrefix = "u/" + comment.getAuthor();
            ((CommentFullyCollapsedViewHolder) holder).usernameTextView.setText(authorWithPrefix);
            if (mShowElapsedTime) {
                ((CommentFullyCollapsedViewHolder) holder).commentTimeTextView.setText(Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
            } else {
                ((CommentFullyCollapsedViewHolder) holder).commentTimeTextView.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
            }
            ((CommentFullyCollapsedViewHolder) holder).scoreTextView.setText(mActivity.getString(R.string.top_score,
                    Utils.getNVotes(mShowAbsoluteNumberOfVotes, comment.getScore() + comment.getVoteType())));

            ((CommentFullyCollapsedViewHolder) holder).itemView.setPadding(comment.getDepth() * 8, 0, 0, 0);
            if (comment.getDepth() > 0) {
                switch (comment.getDepth() % 7) {
                    case 0:
                        ((CommentFullyCollapsedViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor7);
                        break;
                    case 1:
                        ((CommentFullyCollapsedViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor1);
                        break;
                    case 2:
                        ((CommentFullyCollapsedViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor2);
                        break;
                    case 3:
                        ((CommentFullyCollapsedViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor3);
                        break;
                    case 4:
                        ((CommentFullyCollapsedViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor4);
                        break;
                    case 5:
                        ((CommentFullyCollapsedViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor5);
                        break;
                    case 6:
                        ((CommentFullyCollapsedViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor6);
                        break;
                }
                ViewGroup.LayoutParams params = ((CommentFullyCollapsedViewHolder) holder).verticalBlock.getLayoutParams();
                params.width = 8;
                ((CommentFullyCollapsedViewHolder) holder).verticalBlock.setLayoutParams(params);
            }
        } else if (holder instanceof LoadMoreChildCommentsViewHolder) {
            Comment placeholder;
            placeholder = mIsSingleCommentThreadMode ? mVisibleComments.get(holder.getAdapterPosition() - 2)
                    : mVisibleComments.get(holder.getAdapterPosition() - 1);

            ((LoadMoreChildCommentsViewHolder) holder).itemView.setPadding(placeholder.getDepth() * 8, 0, 0, 0);
            if (placeholder.getDepth() > 0) {
                switch (placeholder.getDepth() % 7) {
                    case 0:
                        ((LoadMoreChildCommentsViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor7);
                        break;
                    case 1:
                        ((LoadMoreChildCommentsViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor1);
                        break;
                    case 2:
                        ((LoadMoreChildCommentsViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor2);
                        break;
                    case 3:
                        ((LoadMoreChildCommentsViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor3);
                        break;
                    case 4:
                        ((LoadMoreChildCommentsViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor4);
                        break;
                    case 5:
                        ((LoadMoreChildCommentsViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor5);
                        break;
                    case 6:
                        ((LoadMoreChildCommentsViewHolder) holder).verticalBlock
                                .setBackgroundColor(mCommentVerticalBarColor6);
                        break;
                }

                ViewGroup.LayoutParams params = ((LoadMoreChildCommentsViewHolder) holder).verticalBlock.getLayoutParams();
                params.width = 8;
                ((LoadMoreChildCommentsViewHolder) holder).verticalBlock.setLayoutParams(params);
            }

            if (placeholder.isLoadingMoreChildren()) {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.loading);
            } else if (placeholder.isLoadMoreChildrenFailed()) {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments_failed);
            } else {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments);
            }

            ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setOnClickListener(view -> {
                int commentPosition = mIsSingleCommentThreadMode ? holder.getAdapterPosition() - 2 : holder.getAdapterPosition() - 1;
                int parentPosition = getParentPosition(commentPosition);
                if (parentPosition >= 0) {
                    Comment parentComment = mVisibleComments.get(parentPosition);

                    mVisibleComments.get(commentPosition).setLoadingMoreChildren(true);
                    mVisibleComments.get(commentPosition).setLoadMoreChildrenFailed(false);
                    ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.loading);

                    Retrofit retrofit = mAccessToken == null ? mRetrofit : mOauthRetrofit;
                    FetchComment.fetchMoreComment(retrofit, mAccessToken, parentComment.getMoreChildrenFullnames(),
                            parentComment.getMoreChildrenStartingIndex(), parentComment.getDepth() + 1,
                            mExpandChildren, mLocale,
                            new FetchComment.FetchMoreCommentListener() {
                                @Override
                                public void onFetchMoreCommentSuccess(ArrayList<Comment> expandedComments,
                                                                      int childrenStartingIndex) {
                                    if (mVisibleComments.size() > parentPosition
                                            && parentComment.getFullName().equals(mVisibleComments.get(parentPosition).getFullName())) {
                                        if (mVisibleComments.get(parentPosition).isExpanded()) {
                                            if (mVisibleComments.get(parentPosition).getChildren().size() > childrenStartingIndex) {
                                                mVisibleComments.get(parentPosition).setMoreChildrenStartingIndex(childrenStartingIndex);
                                                mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                        .setLoadingMoreChildren(false);
                                                mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                        .setLoadMoreChildrenFailed(false);

                                                int placeholderPosition = commentPosition;
                                                if (mVisibleComments.get(commentPosition).getFullName().equals(parentComment.getFullName())) {
                                                    for (int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                        if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                            placeholderPosition = i;
                                                            break;
                                                        }
                                                    }
                                                }

                                                mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(false);
                                                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments);

                                                mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                if (mIsSingleCommentThreadMode) {
                                                    notifyItemRangeInserted(placeholderPosition + 2, expandedComments.size());
                                                } else {
                                                    notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                }
                                            } else {
                                                mVisibleComments.get(parentPosition).getChildren()
                                                        .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                                mVisibleComments.get(parentPosition).removeMoreChildrenFullnames();

                                                int placeholderPosition = commentPosition;
                                                if (mVisibleComments.get(commentPosition).getFullName().equals(parentComment.getFullName())) {
                                                    for (int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                        if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                            placeholderPosition = i;
                                                            break;
                                                        }
                                                    }
                                                }

                                                mVisibleComments.remove(placeholderPosition);
                                                if (mIsSingleCommentThreadMode) {
                                                    notifyItemRemoved(placeholderPosition + 2);
                                                } else {
                                                    notifyItemRemoved(placeholderPosition + 1);
                                                }

                                                mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                if (mIsSingleCommentThreadMode) {
                                                    notifyItemRangeInserted(placeholderPosition + 2, expandedComments.size());
                                                } else {
                                                    notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                }
                                            }
                                        } else {
                                            if (mVisibleComments.get(parentPosition).hasReply() && mVisibleComments.get(parentPosition).getChildren().size() <= childrenStartingIndex) {
                                                mVisibleComments.get(parentPosition).getChildren()
                                                        .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                                mVisibleComments.get(parentPosition).removeMoreChildrenFullnames();
                                            }
                                        }

                                        mVisibleComments.get(parentPosition).addChildren(expandedComments);
                                    } else {
                                        for (int i = 0; i < mVisibleComments.size(); i++) {
                                            if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                if (mVisibleComments.get(i).isExpanded()) {
                                                    int placeholderPosition = i + mVisibleComments.get(i).getChildren().size();

                                                    if (!mVisibleComments.get(i).getFullName()
                                                            .equals(mVisibleComments.get(placeholderPosition).getFullName())) {
                                                        for (int j = i + 1; j < mVisibleComments.size(); j++) {
                                                            if (mVisibleComments.get(j).getFullName().equals(mVisibleComments.get(i).getFullName())) {
                                                                placeholderPosition = j;
                                                            }
                                                        }
                                                    }

                                                    mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                    mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(false);
                                                    ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments);

                                                    mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                    if (mIsSingleCommentThreadMode) {
                                                        notifyItemRangeInserted(placeholderPosition + 2, expandedComments.size());
                                                    } else {
                                                        notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                    }
                                                }

                                                mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                        .setLoadingMoreChildren(false);
                                                mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                        .setLoadMoreChildrenFailed(false);
                                                mVisibleComments.get(i).addChildren(expandedComments);

                                                break;
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onFetchMoreCommentFailed() {
                                    if (parentPosition < mVisibleComments.size()
                                            && parentComment.getFullName().equals(mVisibleComments.get(parentPosition).getFullName())) {
                                        if (mVisibleComments.get(parentPosition).isExpanded()) {
                                            int commentPosition = mIsSingleCommentThreadMode ? holder.getAdapterPosition() - 2 : holder.getAdapterPosition() - 1;
                                            int placeholderPosition = commentPosition;
                                            if (commentPosition >= mVisibleComments.size() || commentPosition < 0 || !mVisibleComments.get(commentPosition).getFullName().equals(parentComment.getFullName())) {
                                                for (int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                    if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                        placeholderPosition = i;
                                                        break;
                                                    }
                                                }
                                            }

                                            mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                            mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(true);
                                            ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                                        }

                                        mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                .setLoadingMoreChildren(false);
                                        mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                .setLoadMoreChildrenFailed(true);
                                    } else {
                                        for (int i = 0; i < mVisibleComments.size(); i++) {
                                            if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                if (mVisibleComments.get(i).isExpanded()) {
                                                    int placeholderPosition = i + mVisibleComments.get(i).getChildren().size();
                                                    if (!mVisibleComments.get(placeholderPosition).getFullName().equals(mVisibleComments.get(i).getFullName())) {
                                                        for (int j = i + 1; j < mVisibleComments.size(); j++) {
                                                            if (mVisibleComments.get(j).getFullName().equals(mVisibleComments.get(i).getFullName())) {
                                                                placeholderPosition = j;
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                    mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(true);
                                                    ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                                                }

                                                mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1).setLoadingMoreChildren(false);
                                                mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1).setLoadMoreChildrenFailed(true);

                                                break;
                                            }
                                        }
                                    }
                                }
                            });
                }
            });
        }
    }

    private void loadImage(PostDetailBaseViewHolder holder) {
        if (holder instanceof PostDetailImageAndGifAutoplayViewHolder) {
            String url = mAutoplay && mPost.getPostType() == Post.GIF_TYPE ? mPost.getUrl() : mPost.getPreviewUrl();
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(url)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((PostDetailImageAndGifAutoplayViewHolder) holder).mLoadImageProgressBar.setVisibility(View.GONE);
                            ((PostDetailImageAndGifAutoplayViewHolder) holder).mLoadImageErrorTextView.setVisibility(View.VISIBLE);
                            ((PostDetailImageAndGifAutoplayViewHolder) holder).mLoadImageErrorTextView.setOnClickListener(view -> {
                                ((PostDetailImageAndGifAutoplayViewHolder) holder).mLoadImageProgressBar.setVisibility(View.VISIBLE);
                                ((PostDetailImageAndGifAutoplayViewHolder) holder).mLoadImageErrorTextView.setVisibility(View.GONE);
                                loadImage(holder);
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((PostDetailImageAndGifAutoplayViewHolder) holder).mLoadWrapper.setVisibility(View.GONE);
                            return false;
                        }
                    });

            if ((mPost.isNSFW() && mNeedBlurNsfw) || (mPost.isSpoiler() && mNeedBlurSpoiler)) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostDetailImageAndGifAutoplayViewHolder) holder).mImageView);
            } else {
                imageRequestBuilder.apply(RequestOptions.noTransformation()).into(((PostDetailImageAndGifAutoplayViewHolder) holder).mImageView);
            }
        } else if (holder instanceof PostDetailVideoAndGifPreviewHolder) {
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(mPost.getPreviewUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((PostDetailVideoAndGifPreviewHolder) holder).mLoadImageProgressBar.setVisibility(View.GONE);
                            ((PostDetailVideoAndGifPreviewHolder) holder).mLoadImageErrorTextView.setVisibility(View.VISIBLE);
                            ((PostDetailVideoAndGifPreviewHolder) holder).mLoadImageErrorTextView.setOnClickListener(view -> {
                                ((PostDetailVideoAndGifPreviewHolder) holder).mLoadImageProgressBar.setVisibility(View.VISIBLE);
                                ((PostDetailVideoAndGifPreviewHolder) holder).mLoadImageErrorTextView.setVisibility(View.GONE);
                                loadImage(holder);
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((PostDetailVideoAndGifPreviewHolder) holder).mLoadWrapper.setVisibility(View.GONE);
                            return false;
                        }
                    });

            if ((mPost.isNSFW() && mNeedBlurNsfw) || (mPost.isSpoiler() && mNeedBlurSpoiler)) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostDetailVideoAndGifPreviewHolder) holder).mImageView);
            } else {
                imageRequestBuilder.apply(RequestOptions.noTransformation()).into(((PostDetailVideoAndGifPreviewHolder) holder).mImageView);
            }
        } else if (holder instanceof PostDetailLinkViewHolder) {
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(mPost.getPreviewUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((PostDetailLinkViewHolder) holder).mLoadImageProgressBar.setVisibility(View.GONE);
                            ((PostDetailLinkViewHolder) holder).mLoadImageErrorTextView.setVisibility(View.VISIBLE);
                            ((PostDetailLinkViewHolder) holder).mLoadImageErrorTextView.setOnClickListener(view -> {
                                ((PostDetailLinkViewHolder) holder).mLoadImageProgressBar.setVisibility(View.VISIBLE);
                                ((PostDetailLinkViewHolder) holder).mLoadImageErrorTextView.setVisibility(View.GONE);
                                loadImage(holder);
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((PostDetailLinkViewHolder) holder).mLoadWrapper.setVisibility(View.GONE);
                            return false;
                        }
                    });

            if ((mPost.isNSFW() && mNeedBlurNsfw) || (mPost.isSpoiler() && mNeedBlurSpoiler)) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostDetailLinkViewHolder) holder).mImageView);
            } else {
                imageRequestBuilder.apply(RequestOptions.noTransformation()).into(((PostDetailLinkViewHolder) holder).mImageView);
            }
        }
    }

    public void updatePost(Post post) {
        mPost = post;
        notifyItemChanged(0);
    }

    private int getParentPosition(int position) {
        if (position >= 0 && position < mVisibleComments.size()) {
            int childDepth = mVisibleComments.get(position).getDepth();
            for (int i = position; i >= 0; i--) {
                if (mVisibleComments.get(i).getDepth() < childDepth) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void expandChildren(ArrayList<Comment> comments, ArrayList<Comment> newList, int position) {
        if (comments != null && comments.size() > 0) {
            newList.addAll(position, comments);
            for (int i = 0; i < comments.size(); i++) {
                position++;
                if (comments.get(i).getChildren() != null && comments.get(i).getChildren().size() > 0) {
                    expandChildren(comments.get(i).getChildren(), newList, position);
                    position = position + comments.get(i).getChildren().size();
                }
                comments.get(i).setExpanded(true);
            }
        }
    }

    private void collapseChildren(int position) {
        mVisibleComments.get(position).setExpanded(false);
        int depth = mVisibleComments.get(position).getDepth();
        int allChildrenSize = 0;
        for (int i = position + 1; i < mVisibleComments.size(); i++) {
            if (mVisibleComments.get(i).getDepth() > depth) {
                allChildrenSize++;
            } else {
                break;
            }
        }

        mVisibleComments.subList(position + 1, position + 1 + allChildrenSize).clear();
        if (mIsSingleCommentThreadMode) {
            if (mFullyCollapseComment) {
                notifyItemChanged(position + 2);
            }
            notifyItemRangeRemoved(position + 3, allChildrenSize);
        } else {
            if (mFullyCollapseComment) {
                notifyItemChanged(position + 1);
            }
            notifyItemRangeRemoved(position + 2, allChildrenSize);
        }
    }

    public void addComments(@NonNull ArrayList<Comment> comments, boolean hasMoreComments) {
        if (mVisibleComments.size() == 0) {
            isInitiallyLoading = false;
            isInitiallyLoadingFailed = false;
            if (comments.size() == 0) {
                notifyItemChanged(1);
            } else {
                notifyItemRemoved(1);
            }
        }

        int sizeBefore = mVisibleComments.size();
        mVisibleComments.addAll(comments);
        if (mIsSingleCommentThreadMode) {
            notifyItemRangeInserted(sizeBefore + 2, comments.size());
        } else {
            notifyItemRangeInserted(sizeBefore + 1, comments.size());
        }

        if (mHasMoreComments != hasMoreComments) {
            if (hasMoreComments) {
                if (mIsSingleCommentThreadMode) {
                    notifyItemInserted(mVisibleComments.size() + 2);
                } else {
                    notifyItemInserted(mVisibleComments.size() + 1);
                }
            } else {
                if (mIsSingleCommentThreadMode) {
                    notifyItemRemoved(mVisibleComments.size() + 2);
                } else {
                    notifyItemRemoved(mVisibleComments.size() + 1);
                }
            }
        }
        mHasMoreComments = hasMoreComments;
    }

    public void addComment(Comment comment) {
        if (mVisibleComments.size() == 0 || isInitiallyLoadingFailed) {
            notifyItemRemoved(1);
        }

        mVisibleComments.add(0, comment);

        if (isInitiallyLoading) {
            notifyItemInserted(2);
        } else {
            notifyItemInserted(1);
        }
    }

    public void addChildComment(Comment comment, String parentFullname, int parentPosition) {
        if (!parentFullname.equals(mVisibleComments.get(parentPosition).getFullName())) {
            for (int i = 0; i < mVisibleComments.size(); i++) {
                if (parentFullname.equals(mVisibleComments.get(i).getFullName())) {
                    parentPosition = i;
                    break;
                }
            }
        }

        mVisibleComments.get(parentPosition).addChild(comment);
        mVisibleComments.get(parentPosition).setHasReply(true);
        if (!mVisibleComments.get(parentPosition).isExpanded()) {
            ArrayList<Comment> newList = new ArrayList<>();
            expandChildren(mVisibleComments.get(parentPosition).getChildren(), newList, 0);
            mVisibleComments.get(parentPosition).setExpanded(true);
            mVisibleComments.addAll(parentPosition + 1, newList);
            if (mIsSingleCommentThreadMode) {
                notifyItemChanged(parentPosition + 2);
                notifyItemRangeInserted(parentPosition + 3, newList.size());
            } else {
                notifyItemChanged(parentPosition + 1);
                notifyItemRangeInserted(parentPosition + 2, newList.size());
            }
        } else {
            mVisibleComments.add(parentPosition + 1, comment);
            if (mIsSingleCommentThreadMode) {
                notifyItemInserted(parentPosition + 3);
            } else {
                notifyItemInserted(parentPosition + 2);
            }
        }
    }

    public void setSingleComment(String singleCommentId, boolean isSingleCommentThreadMode) {
        mSingleCommentId = singleCommentId;
        mIsSingleCommentThreadMode = isSingleCommentThreadMode;
    }

    public ArrayList<Comment> getVisibleComments() {
        return mVisibleComments;
    }

    public void initiallyLoading() {
        if (mVisibleComments.size() != 0) {
            int previousSize = mVisibleComments.size();
            mVisibleComments.clear();
            if (mIsSingleCommentThreadMode) {
                notifyItemRangeRemoved(1, previousSize + 1);
            } else {
                notifyItemRangeRemoved(1, previousSize);
            }
        }

        if (isInitiallyLoading || isInitiallyLoadingFailed || mVisibleComments.size() == 0) {
            isInitiallyLoading = true;
            isInitiallyLoadingFailed = false;
            notifyItemChanged(1);
        } else {
            isInitiallyLoading = true;
            isInitiallyLoadingFailed = false;
            notifyItemInserted(1);
        }
    }

    public void initiallyLoadCommentsFailed() {
        isInitiallyLoading = false;
        isInitiallyLoadingFailed = true;
        notifyItemChanged(1);
    }

    public void loadMoreCommentsFailed() {
        loadMoreCommentsFailed = true;
        if (mIsSingleCommentThreadMode) {
            notifyItemChanged(mVisibleComments.size() + 2);
        } else {
            notifyItemChanged(mVisibleComments.size() + 1);
        }
    }

    public void editComment(String commentAuthor, String commentContentMarkdown, int position) {
        if (commentAuthor != null)
            mVisibleComments.get(position).setAuthor(commentAuthor);

        mVisibleComments.get(position).setCommentMarkdown(commentContentMarkdown);
        if (mIsSingleCommentThreadMode) {
            notifyItemChanged(position + 2);
        } else {
            notifyItemChanged(position + 1);
        }
    }

    public void deleteComment(int position) {
        if (mVisibleComments != null && position >= 0 && position < mVisibleComments.size()) {
            if (mVisibleComments.get(position).hasReply()) {
                mVisibleComments.get(position).setAuthor("[deleted]");
                mVisibleComments.get(position).setCommentMarkdown("[deleted]");
                if (mIsSingleCommentThreadMode) {
                    notifyItemChanged(position + 2);
                } else {
                    notifyItemChanged(position + 1);
                }
            } else {
                mVisibleComments.remove(position);
                if (mIsSingleCommentThreadMode) {
                    notifyItemRemoved(position + 2);
                } else {
                    notifyItemRemoved(position + 1);
                }
            }
        }
    }

    public void setBlurNSFW(boolean needBlurNSFW) {
        mNeedBlurNsfw = needBlurNSFW;
    }

    public void setBlurSpoiler(boolean needBlurSpoiler) {
        mNeedBlurSpoiler = needBlurSpoiler;
    }

    public int getNextParentCommentPosition(int currentPosition) {
        if (mVisibleComments != null && !mVisibleComments.isEmpty()) {
            if (mIsSingleCommentThreadMode) {
                for (int i = currentPosition + 1; i - 2 < mVisibleComments.size() && i - 2 >= 0; i++) {
                    if (mVisibleComments.get(i - 2).getDepth() == 0) {
                        return i;
                    }
                }
            } else {
                for (int i = currentPosition + 1; i - 1 < mVisibleComments.size(); i++) {
                    if (mVisibleComments.get(i - 1).getDepth() == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public int getPreviousParentCommentPosition(int currentPosition) {
        if (mVisibleComments != null && !mVisibleComments.isEmpty()) {
            if (mIsSingleCommentThreadMode) {
                for (int i = currentPosition + 1; i - 2 >= 0; i--) {
                    if (mVisibleComments.get(i - 2).getDepth() == 0) {
                        return i;
                    }
                }
            } else {
                for (int i = currentPosition - 1; i - 1 >= 0; i--) {
                    if (mVisibleComments.get(i - 1).getDepth() == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public void setAutoplay(boolean autoplay) {
        mAutoplay = autoplay;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).authorTextView.setTextColor(mUsernameColor);
            ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            ((CommentViewHolder) holder).awardsTextView.setText("");
            ((CommentViewHolder) holder).awardsTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).expandButton.setVisibility(View.GONE);
            ((CommentViewHolder) holder).upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).scoreTextView.setTextColor(mCommentIconAndInfoColor);
            ((CommentViewHolder) holder).downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).replyButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ViewGroup.LayoutParams params = ((CommentViewHolder) holder).verticalBlock.getLayoutParams();
            params.width = 0;
            ((CommentViewHolder) holder).verticalBlock.setLayoutParams(params);
            ((CommentViewHolder) holder).itemView.setPadding(0, 0, 0, 0);
            ((CommentViewHolder) holder).itemView.setBackgroundColor(mCommentBackgroundColor);
        } else if (holder instanceof CommentFullyCollapsedViewHolder) {
            ViewGroup.LayoutParams params = ((CommentFullyCollapsedViewHolder) holder).verticalBlock.getLayoutParams();
            params.width = 0;
            ((CommentFullyCollapsedViewHolder) holder).verticalBlock.setLayoutParams(params);
            ((CommentFullyCollapsedViewHolder) holder).itemView.setPadding(0, 0, 0, 0);
        } else if (holder instanceof PostDetailBaseViewHolder) {
            ((PostDetailBaseViewHolder) holder).mUpvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((PostDetailBaseViewHolder) holder).mScoreTextView.setTextColor(mPostIconAndInfoColor);
            ((PostDetailBaseViewHolder) holder).mDownvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((PostDetailBaseViewHolder) holder).mFlairTextView.setVisibility(View.GONE);
            ((PostDetailBaseViewHolder) holder).mSpoilerTextView.setVisibility(View.GONE);
            ((PostDetailBaseViewHolder) holder).mNSFWTextView.setVisibility(View.GONE);

            if (holder instanceof PostDetailVideoAutoplayViewHolder) {
                ((PostDetailVideoAutoplayViewHolder) holder).muteButton.setVisibility(View.GONE);
                ((PostDetailVideoAutoplayViewHolder) holder).resetVolume();
                mGlide.clear(((PostDetailVideoAutoplayViewHolder) holder).previewImageView);
                ((PostDetailVideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.GONE);
            } else if (holder instanceof PostDetailVideoAndGifPreviewHolder) {
                mGlide.clear(((PostDetailVideoAndGifPreviewHolder) holder).mImageView);
            } else if (holder instanceof PostDetailImageAndGifAutoplayViewHolder) {
                mGlide.clear(((PostDetailImageAndGifAutoplayViewHolder) holder).mImageView);
            } else if (holder instanceof PostDetailLinkViewHolder) {
                mGlide.clear(((PostDetailLinkViewHolder) holder).mImageView);
            }
        } else if (holder instanceof LoadMoreChildCommentsViewHolder) {
            ((LoadMoreChildCommentsViewHolder) holder).itemView.setPadding(0, 0, 0, 0);
            ViewGroup.LayoutParams params = ((LoadMoreChildCommentsViewHolder) holder).verticalBlock.getLayoutParams();
            params.width = 0;
            ((LoadMoreChildCommentsViewHolder) holder).verticalBlock.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        if (isInitiallyLoading || isInitiallyLoadingFailed || mVisibleComments.size() == 0) {
            return 2;
        }

        if (mHasMoreComments || loadMoreCommentsFailed) {
            if (mIsSingleCommentThreadMode) {
                return mVisibleComments.size() + 3;
            } else {
                return mVisibleComments.size() + 2;
            }
        }

        if (mIsSingleCommentThreadMode) {
            return mVisibleComments.size() + 2;
        } else {
            return mVisibleComments.size() + 1;
        }
    }

    @Nullable
    @Override
    public Object getKeyForOrder(int order) {
        return mPost;
    }

    @Nullable
    @Override
    public Integer getOrderForKey(@NonNull Object key) {
        return 0;
    }

    public interface CommentRecyclerViewAdapterCallback {
        void updatePost(Post post);

        void retryFetchingComments();

        void retryFetchingMoreComments();
    }

    class PostDetailBaseViewHolder extends RecyclerView.ViewHolder {
        AspectRatioGifImageView mIconGifImageView;
        TextView mSubredditTextView;
        TextView mUserTextView;
        TextView mAuthorFlairTextView;
        TextView mPostTimeTextView;
        TextView mTitleTextView;
        CustomTextView mTypeTextView;
        ImageView mCrosspostImageView;
        ImageView mArchivedImageView;
        ImageView mLockedImageView;
        CustomTextView mNSFWTextView;
        CustomTextView mSpoilerTextView;
        CustomTextView mFlairTextView;
        TextView mAwardsTextView;
        ConstraintLayout mBottomConstraintLayout;
        ImageView mUpvoteButton;
        TextView mScoreTextView;
        ImageView mDownvoteButton;
        TextView commentsCountTextView;
        ImageView mSaveButton;
        ImageView mShareButton;

        PostDetailBaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setBaseView(AspectRatioGifImageView mIconGifImageView,
                                TextView mSubredditTextView,
                                TextView mUserTextView,
                                TextView mAuthorFlairTextView,
                                TextView mPostTimeTextView,
                                TextView mTitleTextView,
                                CustomTextView mTypeTextView,
                                ImageView mCrosspostImageView,
                                ImageView mArchivedImageView,
                                ImageView mLockedImageView,
                                CustomTextView mNSFWTextView,
                                CustomTextView mSpoilerTextView,
                                CustomTextView mFlairTextView,
                                TextView mAwardsTextView,
                                ConstraintLayout mBottomConstraintLayout,
                                ImageView mUpvoteButton,
                                TextView mScoreTextView,
                                ImageView mDownvoteButton,
                                TextView commentsCountTextView,
                                ImageView mSaveButton,
                                ImageView mShareButton) {
            this.mIconGifImageView = mIconGifImageView;
            this.mSubredditTextView = mSubredditTextView;
            this.mUserTextView = mUserTextView;
            this.mAuthorFlairTextView = mAuthorFlairTextView;
            this.mPostTimeTextView = mPostTimeTextView;
            this.mTitleTextView = mTitleTextView;
            this.mTypeTextView = mTypeTextView;
            this.mCrosspostImageView = mCrosspostImageView;
            this.mArchivedImageView = mArchivedImageView;
            this.mLockedImageView = mLockedImageView;
            this.mNSFWTextView = mNSFWTextView;
            this.mSpoilerTextView = mSpoilerTextView;
            this.mFlairTextView = mFlairTextView;
            this.mAwardsTextView = mAwardsTextView;
            this.mBottomConstraintLayout = mBottomConstraintLayout;
            this.mUpvoteButton = mUpvoteButton;
            this.mScoreTextView = mScoreTextView;
            this.mDownvoteButton = mDownvoteButton;
            this.commentsCountTextView = commentsCountTextView;
            this.mSaveButton = mSaveButton;
            this.mShareButton = mShareButton;

            mIconGifImageView.setOnClickListener(view -> mSubredditTextView.performClick());

            mSubredditTextView.setOnClickListener(view -> {
                Intent intent;
                if (mPost.getSubredditNamePrefixed().equals("u/" + mPost.getAuthor())) {
                    intent = new Intent(mActivity, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mPost.getAuthor());
                } else {
                    intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                            mPost.getSubredditNamePrefixed().substring(2));
                }
                mActivity.startActivity(intent);
            });

            mUserTextView.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mPost.getAuthor());
                mActivity.startActivity(intent);
            });

            mAuthorFlairTextView.setOnClickListener(view -> mUserTextView.performClick());

            mCrosspostImageView.setOnClickListener(view -> {
                Intent crosspostIntent = new Intent(mActivity, ViewPostDetailActivity.class);
                crosspostIntent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, mPost.getCrosspostParentId());
                mActivity.startActivity(crosspostIntent);
            });

            mTypeTextView.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, FilteredThingActivity.class);
                intent.putExtra(FilteredThingActivity.EXTRA_NAME, mSubredditNamePrefixed.substring(2));
                intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                intent.putExtra(FilteredThingActivity.EXTRA_FILTER, mPost.getPostType());
                mActivity.startActivity(intent);
            });

            mNSFWTextView.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, FilteredThingActivity.class);
                intent.putExtra(FilteredThingActivity.EXTRA_NAME, mSubredditNamePrefixed.substring(2));
                intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                intent.putExtra(FilteredThingActivity.EXTRA_FILTER, Post.NSFW_TYPE);
                mActivity.startActivity(intent);
            });

            mUpvoteButton.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                ColorFilter previousUpvoteButtonColorFilter = mUpvoteButton.getColorFilter();
                ColorFilter previousDownvoteButtonColorFilter = mDownvoteButton.getColorFilter();
                int previousScoreTextViewColor = mScoreTextView.getCurrentTextColor();

                int previousVoteType = mPost.getVoteType();
                String newVoteType;

                mDownvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                if (previousVoteType != 1) {
                    //Not upvoted before
                    mPost.setVoteType(1);
                    newVoteType = APIUtils.DIR_UPVOTE;
                    mUpvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    mScoreTextView.setTextColor(mUpvotedColor);
                } else {
                    //Upvoted before
                    mPost.setVoteType(0);
                    newVoteType = APIUtils.DIR_UNVOTE;
                    mUpvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    mScoreTextView.setTextColor(mPostIconAndInfoColor);
                }

                mScoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                        mPost.getScore() + mPost.getVoteType()));

                mCommentRecyclerViewAdapterCallback.updatePost(mPost);

                VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                            mPost.setVoteType(1);
                            mUpvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            mScoreTextView.setTextColor(mUpvotedColor);
                        } else {
                            mPost.setVoteType(0);
                            mUpvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            mScoreTextView.setTextColor(mPostIconAndInfoColor);
                        }

                        mDownvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        mScoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                mPost.getScore() + mPost.getVoteType()));

                        mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                        mPost.setVoteType(previousVoteType);
                        mScoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                mPost.getScore() + previousVoteType));
                        mUpvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                        mDownvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                        mScoreTextView.setTextColor(previousScoreTextViewColor);

                        mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                    }
                }, mPost.getFullName(), newVoteType);
            });

            mDownvoteButton.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                ColorFilter previousUpvoteButtonColorFilter = mUpvoteButton.getColorFilter();
                ColorFilter previousDownvoteButtonColorFilter = mDownvoteButton.getColorFilter();
                int previousScoreTextViewColor = mScoreTextView.getCurrentTextColor();

                int previousVoteType = mPost.getVoteType();
                String newVoteType;

                mUpvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                if (previousVoteType != -1) {
                    //Not upvoted before
                    mPost.setVoteType(-1);
                    newVoteType = APIUtils.DIR_DOWNVOTE;
                    mDownvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    mScoreTextView.setTextColor(mDownvotedColor);
                } else {
                    //Upvoted before
                    mPost.setVoteType(0);
                    newVoteType = APIUtils.DIR_UNVOTE;
                    mDownvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    mScoreTextView.setTextColor(mPostIconAndInfoColor);
                }

                mScoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                        mPost.getScore() + mPost.getVoteType()));

                mCommentRecyclerViewAdapterCallback.updatePost(mPost);

                VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                            mPost.setVoteType(-1);
                            mDownvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            mScoreTextView.setTextColor(mDownvotedColor);
                        } else {
                            mPost.setVoteType(0);
                            mDownvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            mScoreTextView.setTextColor(mPostIconAndInfoColor);
                        }

                        mUpvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        mScoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                mPost.getScore() + mPost.getVoteType()));

                        mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                        mPost.setVoteType(previousVoteType);
                        mScoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                mPost.getScore() + previousVoteType));
                        mUpvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                        mDownvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                        mScoreTextView.setTextColor(previousScoreTextViewColor);

                        mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                    }
                }, mPost.getFullName(), newVoteType);
            });

            commentsCountTextView.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_reply_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPost.isLocked()) {
                    Toast.makeText(mActivity, R.string.locked_post_comment_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(mActivity, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mPost.getFullName());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_MARKDOWN_KEY, mPost.getTitle());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, mPost.getSelfText());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, mPost.getSelfTextPlain());
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, false);
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, 0);
                mActivity.startActivityForResult(intent, WRITE_COMMENT_REQUEST_CODE);
            });

            mSaveButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPost.isSaved()) {
                    mSaveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                    SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                            new SaveThing.SaveThingListener() {
                                @Override
                                public void success() {
                                    mPost.setSaved(false);
                                    mSaveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                    Toast.makeText(mActivity, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                    mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                                }

                                @Override
                                public void failed() {
                                    mPost.setSaved(true);
                                    mSaveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                    Toast.makeText(mActivity, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
                                    mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                                }
                            });
                } else {
                    mSaveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                    SaveThing.saveThing(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                            new SaveThing.SaveThingListener() {
                                @Override
                                public void success() {
                                    mPost.setSaved(true);
                                    mSaveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                    Toast.makeText(mActivity, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                    mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                                }

                                @Override
                                public void failed() {
                                    mPost.setSaved(false);
                                    mSaveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                    Toast.makeText(mActivity, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                    mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                                }
                            });
                }
            });

            mShareButton.setOnClickListener(view -> {
                Bundle bundle = new Bundle();
                bundle.putString(ShareLinkBottomSheetFragment.EXTRA_POST_LINK, mPost.getPermalink());
                if (mPost.getPostType() != Post.TEXT_TYPE) {
                    bundle.putInt(ShareLinkBottomSheetFragment.EXTRA_MEDIA_TYPE, mPost.getPostType());
                    switch (mPost.getPostType()) {
                        case Post.IMAGE_TYPE:
                        case Post.GIF_TYPE:
                        case Post.LINK_TYPE:
                        case Post.NO_PREVIEW_LINK_TYPE:
                            bundle.putString(ShareLinkBottomSheetFragment.EXTRA_MEDIA_LINK, mPost.getUrl());
                            break;
                        case Post.VIDEO_TYPE:
                            bundle.putString(ShareLinkBottomSheetFragment.EXTRA_MEDIA_LINK, mPost.getVideoDownloadUrl());
                            break;
                    }
                }
                mShareLinkBottomSheetFragment.setArguments(bundle);
                mShareLinkBottomSheetFragment.show(mActivity.getSupportFragmentManager(), mShareLinkBottomSheetFragment.getTag());
            });

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mBottomConstraintLayout);
                constraintSet.clear(mUpvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(mScoreTextView.getId(), ConstraintSet.START);
                constraintSet.clear(mDownvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(mSaveButton.getId(), ConstraintSet.END);
                constraintSet.clear(mShareButton.getId(), ConstraintSet.END);
                constraintSet.connect(mUpvoteButton.getId(), ConstraintSet.END, mScoreTextView.getId(), ConstraintSet.START);
                constraintSet.connect(mScoreTextView.getId(), ConstraintSet.END, mDownvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(mDownvoteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.START, mSaveButton.getId(), ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.END, mUpvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(mSaveButton.getId(), ConstraintSet.START, mShareButton.getId(), ConstraintSet.END);
                constraintSet.connect(mShareButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.setHorizontalBias(commentsCountTextView.getId(), 0);
                constraintSet.applyTo(mBottomConstraintLayout);
            }

            itemView.setBackgroundColor(mCardViewColor);
            mSubredditTextView.setTextColor(mSubredditColor);
            mUserTextView.setTextColor(mUsernameColor);
            mAuthorFlairTextView.setTextColor(mAuthorFlairTextColor);
            mPostTimeTextView.setTextColor(mSecondaryTextColor);
            mTitleTextView.setTextColor(mPostTitleColor);
            mTypeTextView.setBackgroundColor(mPostTypeBackgroundColor);
            mTypeTextView.setBorderColor(mPostTypeBackgroundColor);
            mTypeTextView.setTextColor(mPostTypeTextColor);
            mSpoilerTextView.setBackgroundColor(mSpoilerBackgroundColor);
            mSpoilerTextView.setBorderColor(mSpoilerBackgroundColor);
            mSpoilerTextView.setTextColor(mSpoilerTextColor);
            mNSFWTextView.setBackgroundColor(mNSFWBackgroundColor);
            mNSFWTextView.setBorderColor(mNSFWBackgroundColor);
            mNSFWTextView.setTextColor(mNSFWTextColor);
            mFlairTextView.setBackgroundColor(mFlairBackgroundColor);
            mFlairTextView.setBorderColor(mFlairBackgroundColor);
            mFlairTextView.setTextColor(mFlairTextColor);
            mArchivedImageView.setColorFilter(mArchivedTintColor, PorterDuff.Mode.SRC_IN);
            mLockedImageView.setColorFilter(mLockedTintColor, PorterDuff.Mode.SRC_IN);
            mCrosspostImageView.setColorFilter(mCrosspostTintColor, PorterDuff.Mode.SRC_IN);
            mAwardsTextView.setTextColor(mSecondaryTextColor);
            mUpvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            mScoreTextView.setTextColor(mPostIconAndInfoColor);
            mDownvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            commentsCountTextView.setTextColor(mPostIconAndInfoColor);
            commentsCountTextView.setCompoundDrawablesWithIntrinsicBounds(mCommentIcon, null, null, null);
            mSaveButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            mShareButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    class PostDetailVideoAutoplayViewHolder extends PostDetailBaseViewHolder implements ToroPlayer {
        @BindView(R.id.icon_gif_image_view_item_post_detail_video_autoplay)
        AspectRatioGifImageView mIconGifImageView;
        @BindView(R.id.subreddit_text_view_item_post_detail_video_autoplay)
        TextView mSubredditTextView;
        @BindView(R.id.user_text_view_item_post_detail_video_autoplay)
        TextView mUserTextView;
        @BindView(R.id.author_flair_text_view_item_post_detail_video_autoplay)
        TextView mAuthorFlairTextView;
        @BindView(R.id.post_time_text_view_item_post_detail_video_autoplay)
        TextView mPostTimeTextView;
        @BindView(R.id.title_text_view_item_post_detail_video_autoplay)
        TextView mTitleTextView;
        @BindView(R.id.type_text_view_item_post_detail_video_autoplay)
        CustomTextView mTypeTextView;
        @BindView(R.id.crosspost_image_view_item_post_detail_video_autoplay)
        ImageView mCrosspostImageView;
        @BindView(R.id.archived_image_view_item_post_detail_video_autoplay)
        ImageView mArchivedImageView;
        @BindView(R.id.locked_image_view_item_post_detail_video_autoplay)
        ImageView mLockedImageView;
        @BindView(R.id.nsfw_text_view_item_post_detail_video_autoplay)
        CustomTextView mNSFWTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_detail_video_autoplay)
        CustomTextView mSpoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_detail_video_autoplay)
        CustomTextView mFlairTextView;
        @BindView(R.id.awards_text_view_item_post_detail_video_autoplay)
        TextView mAwardsTextView;
        @BindView(R.id.aspect_ratio_frame_layout_item_post_detail_video_autoplay)
        AspectRatioFrameLayout aspectRatioFrameLayout;
        @BindView(R.id.player_view_item_post_detail_video_autoplay)
        PlayerView playerView;
        @BindView(R.id.preview_image_view_item_post_detail_video_autoplay)
        GifImageView previewImageView;
        @BindView(R.id.mute_exo_playback_control_view)
        ImageView muteButton;
        @BindView(R.id.fullscreen_exo_playback_control_view)
        ImageView fullscreenButton;
        @BindView(R.id.bottom_constraint_layout_item_post_detail_video_autoplay)
        ConstraintLayout mBottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_detail_video_autoplay)
        ImageView mUpvoteButton;
        @BindView(R.id.score_text_view_item_post_detail_video_autoplay)
        TextView mScoreTextView;
        @BindView(R.id.minus_button_item_post_detail_video_autoplay)
        ImageView mDownvoteButton;
        @BindView(R.id.comments_count_item_post_detail_video_autoplay)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_detail_video_autoplay)
        ImageView mSaveButton;
        @BindView(R.id.share_button_item_post_detail_video_autoplay)
        ImageView mShareButton;

        @Nullable
        ExoPlayerViewHelper helper;
        private Uri mediaUri;
        private float volume;

        public PostDetailVideoAutoplayViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(mIconGifImageView,
                    mSubredditTextView,
                    mUserTextView,
                    mAuthorFlairTextView,
                    mPostTimeTextView,
                    mTitleTextView,
                    mTypeTextView,
                    mCrosspostImageView,
                    mArchivedImageView,
                    mLockedImageView,
                    mNSFWTextView,
                    mSpoilerTextView,
                    mFlairTextView,
                    mAwardsTextView,
                    mBottomConstraintLayout,
                    mUpvoteButton,
                    mScoreTextView,
                    mDownvoteButton,
                    commentsCountTextView,
                    mSaveButton,
                    mShareButton);

            aspectRatioFrameLayout.setOnClickListener(null);

            muteButton.setOnClickListener(view -> {
                if (helper != null) {
                    if (helper.getVolume() != 0) {
                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_mute_white_rounded_18dp));
                        helper.setVolume(0f);
                        volume = 0f;
                    } else {
                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_unmute_white_rounded_18dp));
                        helper.setVolume(1f);
                        volume = 1f;
                    }
                }
            });

            fullscreenButton.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                intent.setData(Uri.parse(mPost.getVideoUrl()));
                intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, mPost.getVideoDownloadUrl());
                intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, mPost.getSubredditName());
                intent.putExtra(ViewVideoActivity.EXTRA_ID, mPost.getId());
                intent.putExtra(ViewVideoActivity.EXTRA_POST_TITLE, mPost.getTitle());
                intent.putExtra(ViewVideoActivity.EXTRA_PROGRESS_SECONDS, helper.getLatestPlaybackInfo().getResumePosition());
                intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                mActivity.startActivity(intent);
            });
        }

        void bindVideoUri(Uri videoUri) {
            mediaUri = videoUri;
        }

        void setVolume(float volume) {
            this.volume = volume;
        }

        void resetVolume() {
            volume = 0f;
        }

        @NonNull
        @Override
        public View getPlayerView() {
            return playerView;
        }

        @NonNull
        @Override
        public PlaybackInfo getCurrentPlaybackInfo() {
            return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
        }

        @Override
        public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
            if (helper == null) {
                helper = new ExoPlayerViewHelper(this, mediaUri, null, mExoCreator);
                helper.addEventListener(new Playable.EventListener() {
                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                        if (!trackGroups.isEmpty()) {
                            for (int i = 0; i < trackGroups.length; i++) {
                                String mimeType = trackGroups.get(i).getFormat(0).sampleMimeType;
                                if (mimeType != null && mimeType.contains("audio")) {
                                    helper.setVolume(volume);
                                    muteButton.setVisibility(View.VISIBLE);
                                    if (volume != 0f) {
                                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_unmute_white_rounded_18dp));
                                    } else {
                                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_mute_white_rounded_18dp));
                                    }
                                    break;
                                }
                            }
                        } else {
                            muteButton.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onMetadata(Metadata metadata) {

                    }

                    @Override
                    public void onCues(List<Cue> cues) {

                    }

                    @Override
                    public void onRenderedFirstFrame() {
                        mGlide.clear(previewImageView);
                        previewImageView.setVisibility(View.GONE);
                    }
                });
            }
            helper.initialize(container, playbackInfo);
        }

        @Override
        public void play() {
            if (helper != null) helper.play();
        }

        @Override
        public void pause() {
            if (helper != null) helper.pause();
        }

        @Override
        public boolean isPlaying() {
            return helper != null && helper.isPlaying();
        }

        @Override
        public void release() {
            if (helper != null) {
                helper.release();
                helper = null;
            }
        }

        @Override
        public boolean wantsToPlay() {
            return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= mStartAutoplayVisibleAreaOffset;
        }

        @Override
        public int getPlayerOrder() {
            return getAdapterPosition();
        }
    }

    class PostDetailVideoAndGifPreviewHolder extends PostDetailBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_detail_video_and_gif_preview)
        AspectRatioGifImageView mIconGifImageView;
        @BindView(R.id.subreddit_text_view_item_post_detail_video_and_gif_preview)
        TextView mSubredditTextView;
        @BindView(R.id.user_text_view_item_post_detail_video_and_gif_preview)
        TextView mUserTextView;
        @BindView(R.id.author_flair_text_view_item_post_detail_video_and_gif_preview)
        TextView mAuthorFlairTextView;
        @BindView(R.id.post_time_text_view_item_post_detail_video_and_gif_preview)
        TextView mPostTimeTextView;
        @BindView(R.id.title_text_view_item_post_detail_video_and_gif_preview)
        TextView mTitleTextView;
        @BindView(R.id.type_text_view_item_post_detail_video_and_gif_preview)
        CustomTextView mTypeTextView;
        @BindView(R.id.crosspost_image_view_item_post_detail_video_and_gif_preview)
        ImageView mCrosspostImageView;
        @BindView(R.id.archived_image_view_item_post_detail_video_and_gif_preview)
        ImageView mArchivedImageView;
        @BindView(R.id.locked_image_view_item_post_detail_video_and_gif_preview)
        ImageView mLockedImageView;
        @BindView(R.id.nsfw_text_view_item_post_detail_video_and_gif_preview)
        CustomTextView mNSFWTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_detail_video_and_gif_preview)
        CustomTextView mSpoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_detail_video_and_gif_preview)
        CustomTextView mFlairTextView;
        @BindView(R.id.awards_text_view_item_post_detail_video_and_gif_preview)
        TextView mAwardsTextView;
        @BindView(R.id.load_wrapper_item_post_detail_video_and_gif_preview)
        RelativeLayout mLoadWrapper;
        @BindView(R.id.progress_bar_item_post_detail_video_and_gif_preview)
        ProgressBar mLoadImageProgressBar;
        @BindView(R.id.load_image_error_text_view_item_post_detail_video_and_gif_preview)
        TextView mLoadImageErrorTextView;
        @BindView(R.id.image_view_item_post_detail_video_and_gif_preview)
        AspectRatioImageView mImageView;
        @BindView(R.id.bottom_constraint_layout_item_post_detail_video_and_gif_preview)
        ConstraintLayout mBottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_detail_video_and_gif_preview)
        ImageView mUpvoteButton;
        @BindView(R.id.score_text_view_item_post_detail_video_and_gif_preview)
        TextView mScoreTextView;
        @BindView(R.id.minus_button_item_post_detail_video_and_gif_preview)
        ImageView mDownvoteButton;
        @BindView(R.id.comments_count_item_post_detail_video_and_gif_preview)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_detail_video_and_gif_preview)
        ImageView mSaveButton;
        @BindView(R.id.share_button_item_post_detail_video_and_gif_preview)
        ImageView mShareButton;

        PostDetailVideoAndGifPreviewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(mIconGifImageView,
                    mSubredditTextView,
                    mUserTextView,
                    mAuthorFlairTextView,
                    mPostTimeTextView,
                    mTitleTextView,
                    mTypeTextView,
                    mCrosspostImageView,
                    mArchivedImageView,
                    mLockedImageView,
                    mNSFWTextView,
                    mSpoilerTextView,
                    mFlairTextView,
                    mAwardsTextView,
                    mBottomConstraintLayout,
                    mUpvoteButton,
                    mScoreTextView,
                    mDownvoteButton,
                    commentsCountTextView,
                    mSaveButton,
                    mShareButton);

            mLoadImageProgressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            mLoadImageErrorTextView.setTextColor(mPrimaryTextColor);

            mImageView.setOnClickListener(view -> {
                if (mPost.getPostType() == Post.VIDEO_TYPE) {
                    Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                    intent.setData(Uri.parse(mPost.getVideoUrl()));
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, mPost.getVideoDownloadUrl());
                    intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, mPost.getSubredditName());
                    intent.putExtra(ViewVideoActivity.EXTRA_ID, mPost.getId());
                    intent.putExtra(ViewVideoActivity.EXTRA_POST_TITLE, mPost.getTitle());
                    intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                    mActivity.startActivity(intent);
                } else if (mPost.getPostType() == Post.GIF_TYPE) {
                    Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                    intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, mPost.getSubredditName()
                            + "-" + mPost.getId() + ".gif");
                    intent.putExtra(ViewImageOrGifActivity.GIF_URL_KEY, mPost.getVideoUrl());
                    intent.putExtra(ViewImageOrGifActivity.POST_TITLE_KEY, mPost.getTitle());
                    mActivity.startActivity(intent);
                }
            });
        }
    }

    class PostDetailImageAndGifAutoplayViewHolder extends PostDetailBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_detail_image_and_gif_autoplay)
        AspectRatioGifImageView mIconGifImageView;
        @BindView(R.id.subreddit_text_view_item_post_detail_image_and_gif_autoplay)
        TextView mSubredditTextView;
        @BindView(R.id.user_text_view_item_post_detail_image_and_gif_autoplay)
        TextView mUserTextView;
        @BindView(R.id.author_flair_text_view_item_post_detail_image_and_gif_autoplay)
        TextView mAuthorFlairTextView;
        @BindView(R.id.post_time_text_view_item_post_detail_image_and_gif_autoplay)
        TextView mPostTimeTextView;
        @BindView(R.id.title_text_view_item_post_detail_image_and_gif_autoplay)
        TextView mTitleTextView;
        @BindView(R.id.type_text_view_item_post_detail_image_and_gif_autoplay)
        CustomTextView mTypeTextView;
        @BindView(R.id.crosspost_image_view_item_post_detail_image_and_gif_autoplay)
        ImageView mCrosspostImageView;
        @BindView(R.id.archived_image_view_item_post_detail_image_and_gif_autoplay)
        ImageView mArchivedImageView;
        @BindView(R.id.locked_image_view_item_post_detail_image_and_gif_autoplay)
        ImageView mLockedImageView;
        @BindView(R.id.nsfw_text_view_item_post_detail_image_and_gif_autoplay)
        CustomTextView mNSFWTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_detail_image_and_gif_autoplay)
        CustomTextView mSpoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_detail_image_and_gif_autoplay)
        CustomTextView mFlairTextView;
        @BindView(R.id.awards_text_view_item_post_detail_image_and_gif_autoplay)
        TextView mAwardsTextView;
        @BindView(R.id.image_view_wrapper_item_post_detail_image_and_gif_autoplay)
        RelativeLayout mRelativeLayout;
        @BindView(R.id.load_wrapper_item_post_detail_image_and_gif_autoplay)
        RelativeLayout mLoadWrapper;
        @BindView(R.id.progress_bar_item_post_detail_image_and_gif_autoplay)
        ProgressBar mLoadImageProgressBar;
        @BindView(R.id.load_image_error_text_view_item_post_detail_image_and_gif_autoplay)
        TextView mLoadImageErrorTextView;
        @BindView(R.id.image_view_item_post_detail_image_and_gif_autoplay)
        AspectRatioImageView mImageView;
        @BindView(R.id.bottom_constraint_layout_item_post_detail_image_and_gif_autoplay)
        ConstraintLayout mBottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_detail_image_and_gif_autoplay)
        ImageView mUpvoteButton;
        @BindView(R.id.score_text_view_item_post_detail_image_and_gif_autoplay)
        TextView mScoreTextView;
        @BindView(R.id.minus_button_item_post_detail_image_and_gif_autoplay)
        ImageView mDownvoteButton;
        @BindView(R.id.comments_count_item_post_detail_image_and_gif_autoplay)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_detail_image_and_gif_autoplay)
        ImageView mSaveButton;
        @BindView(R.id.share_button_item_post_detail_image_and_gif_autoplay)
        ImageView mShareButton;

        PostDetailImageAndGifAutoplayViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(mIconGifImageView,
                    mSubredditTextView,
                    mUserTextView,
                    mAuthorFlairTextView,
                    mPostTimeTextView,
                    mTitleTextView,
                    mTypeTextView,
                    mCrosspostImageView,
                    mArchivedImageView,
                    mLockedImageView,
                    mNSFWTextView,
                    mSpoilerTextView,
                    mFlairTextView,
                    mAwardsTextView,
                    mBottomConstraintLayout,
                    mUpvoteButton,
                    mScoreTextView,
                    mDownvoteButton,
                    commentsCountTextView,
                    mSaveButton,
                    mShareButton);

            mLoadImageProgressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            mLoadImageErrorTextView.setTextColor(mPrimaryTextColor);

            mImageView.setOnClickListener(view -> {
                if (mPost.getPostType() == Post.IMAGE_TYPE) {
                    Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                    intent.putExtra(ViewImageOrGifActivity.IMAGE_URL_KEY, mPost.getUrl());
                    intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, mPost.getSubredditNamePrefixed().substring(2)
                            + "-" + mPost.getId().substring(3) + ".jpg");
                    intent.putExtra(ViewImageOrGifActivity.POST_TITLE_KEY, mPost.getTitle());
                    mActivity.startActivity(intent);
                } else if (mPost.getPostType() == Post.GIF_TYPE) {
                    Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                    intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, mPost.getSubredditName()
                            + "-" + mPost.getId() + ".gif");
                    intent.putExtra(ViewImageOrGifActivity.GIF_URL_KEY, mPost.getVideoUrl());
                    intent.putExtra(ViewImageOrGifActivity.POST_TITLE_KEY, mPost.getTitle());
                    mActivity.startActivity(intent);
                }
            });
        }
    }

    class PostDetailLinkViewHolder extends PostDetailBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_detail_link)
        AspectRatioGifImageView mIconGifImageView;
        @BindView(R.id.subreddit_text_view_item_post_detail_link)
        TextView mSubredditTextView;
        @BindView(R.id.user_text_view_item_post_detail_link)
        TextView mUserTextView;
        @BindView(R.id.author_flair_text_view_item_post_detail_link)
        TextView mAuthorFlairTextView;
        @BindView(R.id.post_time_text_view_item_post_detail_link)
        TextView mPostTimeTextView;
        @BindView(R.id.title_text_view_item_post_detail_link)
        TextView mTitleTextView;
        @BindView(R.id.type_text_view_item_post_detail_link)
        CustomTextView mTypeTextView;
        @BindView(R.id.crosspost_image_view_item_post_detail_link)
        ImageView mCrosspostImageView;
        @BindView(R.id.archived_image_view_item_post_detail_link)
        ImageView mArchivedImageView;
        @BindView(R.id.locked_image_view_item_post_detail_link)
        ImageView mLockedImageView;
        @BindView(R.id.nsfw_text_view_item_post_detail_link)
        CustomTextView mNSFWTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_detail_link)
        CustomTextView mSpoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_detail_link)
        CustomTextView mFlairTextView;
        @BindView(R.id.awards_text_view_item_post_detail_link)
        TextView mAwardsTextView;
        @BindView(R.id.link_text_view_item_post_detail_link)
        TextView mLinkTextView;
        @BindView(R.id.image_view_wrapper_item_post_detail_link)
        RelativeLayout mRelativeLayout;
        @BindView(R.id.load_wrapper_item_post_detail_link)
        RelativeLayout mLoadWrapper;
        @BindView(R.id.progress_bar_item_post_detail_link)
        ProgressBar mLoadImageProgressBar;
        @BindView(R.id.load_image_error_text_view_item_post_detail_link)
        TextView mLoadImageErrorTextView;
        @BindView(R.id.image_view_item_post_detail_link)
        AspectRatioImageView mImageView;
        @BindView(R.id.bottom_constraint_layout_item_post_detail_link)
        ConstraintLayout mBottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_detail_link)
        ImageView mUpvoteButton;
        @BindView(R.id.score_text_view_item_post_detail_link)
        TextView mScoreTextView;
        @BindView(R.id.minus_button_item_post_detail_link)
        ImageView mDownvoteButton;
        @BindView(R.id.comments_count_item_post_detail_link)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_detail_link)
        ImageView mSaveButton;
        @BindView(R.id.share_button_item_post_detail_link)
        ImageView mShareButton;

        PostDetailLinkViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(mIconGifImageView,
                    mSubredditTextView,
                    mUserTextView,
                    mAuthorFlairTextView,
                    mPostTimeTextView,
                    mTitleTextView,
                    mTypeTextView,
                    mCrosspostImageView,
                    mArchivedImageView,
                    mLockedImageView,
                    mNSFWTextView,
                    mSpoilerTextView,
                    mFlairTextView,
                    mAwardsTextView,
                    mBottomConstraintLayout,
                    mUpvoteButton,
                    mScoreTextView,
                    mDownvoteButton,
                    commentsCountTextView,
                    mSaveButton,
                    mShareButton);

            mLinkTextView.setTextColor(mSecondaryTextColor);
            mLoadImageProgressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            mLoadImageErrorTextView.setTextColor(mPrimaryTextColor);

            mImageView.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                Uri uri = Uri.parse(mPost.getUrl());
                if (uri.getScheme() == null && uri.getHost() == null) {
                    intent.setData(LinkResolverActivity.getRedditUriByPath(mPost.getUrl()));
                } else {
                    intent.setData(uri);
                }
                intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                mActivity.startActivity(intent);
            });
        }
    }

    class PostDetailNoPreviewLinkViewHolder extends PostDetailBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_detail_no_preview_link)
        AspectRatioGifImageView mIconGifImageView;
        @BindView(R.id.subreddit_text_view_item_post_detail_no_preview_link)
        TextView mSubredditTextView;
        @BindView(R.id.user_text_view_item_post_detail_no_preview_link)
        TextView mUserTextView;
        @BindView(R.id.author_flair_text_view_item_post_detail_no_preview_link)
        TextView mAuthorFlairTextView;
        @BindView(R.id.post_time_text_view_item_post_detail_no_preview_link)
        TextView mPostTimeTextView;
        @BindView(R.id.title_text_view_item_post_detail_no_preview_link)
        TextView mTitleTextView;
        @BindView(R.id.content_markdown_view_item_post_detail_no_preview_link)
        RecyclerView mContentMarkdownView;
        @BindView(R.id.type_text_view_item_post_detail_no_preview_link)
        CustomTextView mTypeTextView;
        @BindView(R.id.crosspost_image_view_item_post_detail_no_preview_link)
        ImageView mCrosspostImageView;
        @BindView(R.id.archived_image_view_item_post_detail_no_preview_link)
        ImageView mArchivedImageView;
        @BindView(R.id.locked_image_view_item_post_detail_no_preview_link)
        ImageView mLockedImageView;
        @BindView(R.id.nsfw_text_view_item_post_detail_no_preview_link)
        CustomTextView mNSFWTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_detail_no_preview_link)
        CustomTextView mSpoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_detail_no_preview_link)
        CustomTextView mFlairTextView;
        @BindView(R.id.awards_text_view_item_post_detail_no_preview_link)
        TextView mAwardsTextView;
        @BindView(R.id.link_text_view_item_post_detail_no_preview_link)
        TextView mLinkTextView;
        @BindView(R.id.image_view_no_preview_link_item_post_detail_no_preview_link)
        ImageView mNoPreviewLinkImageView;
        @BindView(R.id.bottom_constraint_layout_item_post_detail_no_preview_link)
        ConstraintLayout mBottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_detail_no_preview_link)
        ImageView mUpvoteButton;
        @BindView(R.id.score_text_view_item_post_detail_no_preview_link)
        TextView mScoreTextView;
        @BindView(R.id.minus_button_item_post_detail_no_preview_link)
        ImageView mDownvoteButton;
        @BindView(R.id.comments_count_item_post_detail_no_preview_link)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_detail_no_preview_link)
        ImageView mSaveButton;
        @BindView(R.id.share_button_item_post_detail_no_preview_link)
        ImageView mShareButton;

        PostDetailNoPreviewLinkViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(mIconGifImageView,
                    mSubredditTextView,
                    mUserTextView,
                    mAuthorFlairTextView,
                    mPostTimeTextView,
                    mTitleTextView,
                    mTypeTextView,
                    mCrosspostImageView,
                    mArchivedImageView,
                    mLockedImageView,
                    mNSFWTextView,
                    mSpoilerTextView,
                    mFlairTextView,
                    mAwardsTextView,
                    mBottomConstraintLayout,
                    mUpvoteButton,
                    mScoreTextView,
                    mDownvoteButton,
                    commentsCountTextView,
                    mSaveButton,
                    mShareButton);

            mLinkTextView.setTextColor(mSecondaryTextColor);
            mNoPreviewLinkImageView.setBackgroundColor(mNoPreviewLinkBackgroundColor);

            mNoPreviewLinkImageView.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                Uri uri = Uri.parse(mPost.getUrl());
                if (uri.getScheme() == null && uri.getHost() == null) {
                    intent.setData(LinkResolverActivity.getRedditUriByPath(mPost.getUrl()));
                } else {
                    intent.setData(uri);
                }
                intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                mActivity.startActivity(intent);
            });
        }
    }

    class PostDetailTextViewHolder extends PostDetailBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_detail_text)
        AspectRatioGifImageView mIconGifImageView;
        @BindView(R.id.subreddit_text_view_item_post_detail_text)
        TextView mSubredditTextView;
        @BindView(R.id.user_text_view_item_post_detail_text)
        TextView mUserTextView;
        @BindView(R.id.author_flair_text_view_item_post_detail_text)
        TextView mAuthorFlairTextView;
        @BindView(R.id.post_time_text_view_item_post_detail_text)
        TextView mPostTimeTextView;
        @BindView(R.id.title_text_view_item_post_detail_text)
        TextView mTitleTextView;
        @BindView(R.id.content_markdown_view_item_post_detail_text)
        RecyclerView mContentMarkdownView;
        @BindView(R.id.type_text_view_item_post_detail_text)
        CustomTextView mTypeTextView;
        @BindView(R.id.crosspost_image_view_item_post_detail_text)
        ImageView mCrosspostImageView;
        @BindView(R.id.archived_image_view_item_post_detail_text)
        ImageView mArchivedImageView;
        @BindView(R.id.locked_image_view_item_post_detail_text)
        ImageView mLockedImageView;
        @BindView(R.id.nsfw_text_view_item_post_detail_text)
        CustomTextView mNSFWTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_detail_text)
        CustomTextView mSpoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_detail_text)
        CustomTextView mFlairTextView;
        @BindView(R.id.awards_text_view_item_post_detail_text)
        TextView mAwardsTextView;
        @BindView(R.id.bottom_constraint_layout_item_post_detail_text)
        ConstraintLayout mBottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_detail_text)
        ImageView mUpvoteButton;
        @BindView(R.id.score_text_view_item_post_detail_text)
        TextView mScoreTextView;
        @BindView(R.id.minus_button_item_post_detail_text)
        ImageView mDownvoteButton;
        @BindView(R.id.comments_count_item_post_detail_text)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_detail_text)
        ImageView mSaveButton;
        @BindView(R.id.share_button_item_post_detail_text)
        ImageView mShareButton;

        PostDetailTextViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(mIconGifImageView,
                    mSubredditTextView,
                    mUserTextView,
                    mAuthorFlairTextView,
                    mPostTimeTextView,
                    mTitleTextView,
                    mTypeTextView,
                    mCrosspostImageView,
                    mArchivedImageView,
                    mLockedImageView,
                    mNSFWTextView,
                    mSpoilerTextView,
                    mFlairTextView,
                    mAwardsTextView,
                    mBottomConstraintLayout,
                    mUpvoteButton,
                    mScoreTextView,
                    mDownvoteButton,
                    commentsCountTextView,
                    mSaveButton,
                    mShareButton);
        }
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.linear_layout_item_comment)
        LinearLayout linearLayout;
        @BindView(R.id.author_text_view_item_post_comment)
        TextView authorTextView;
        @BindView(R.id.author_flair_text_view_item_post_comment)
        TextView authorFlairTextView;
        @BindView(R.id.comment_time_text_view_item_post_comment)
        TextView commentTimeTextView;
        @BindView(R.id.top_score_text_view_item_post_comment)
        TextView topScoreTextView;
        @BindView(R.id.awards_text_view_item_comment)
        TextView awardsTextView;
        @BindView(R.id.comment_markdown_view_item_post_comment)
        TextView commentMarkdownView;
        @BindView(R.id.bottom_constraint_layout_item_post_comment)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.up_vote_button_item_post_comment)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_comment)
        TextView scoreTextView;
        @BindView(R.id.down_vote_button_item_post_comment)
        ImageView downvoteButton;
        @BindView(R.id.more_button_item_post_comment)
        ImageView moreButton;
        @BindView(R.id.save_button_item_post_comment)
        ImageView saveButton;
        @BindView(R.id.expand_button_item_post_comment)
        ImageView expandButton;
        @BindView(R.id.reply_button_item_post_comment)
        ImageView replyButton;
        @BindView(R.id.vertical_block_item_post_comment)
        View verticalBlock;
        @BindView(R.id.divider_item_comment)
        View commentDivider;

        CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(bottomConstraintLayout);
                constraintSet.clear(upvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(scoreTextView.getId(), ConstraintSet.START);
                constraintSet.clear(downvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(expandButton.getId(), ConstraintSet.END);
                constraintSet.clear(saveButton.getId(), ConstraintSet.END);
                constraintSet.clear(replyButton.getId(), ConstraintSet.END);
                constraintSet.connect(upvoteButton.getId(), ConstraintSet.END, scoreTextView.getId(), ConstraintSet.START);
                constraintSet.connect(scoreTextView.getId(), ConstraintSet.END, downvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(downvoteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(moreButton.getId(), ConstraintSet.START, expandButton.getId(), ConstraintSet.END);
                constraintSet.connect(expandButton.getId(), ConstraintSet.START, saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(saveButton.getId(), ConstraintSet.START, replyButton.getId(), ConstraintSet.END);
                constraintSet.connect(replyButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.setHorizontalBias(moreButton.getId(), 0);
                constraintSet.applyTo(bottomConstraintLayout);
            }

            if (mShowCommentDivider) {
                commentDivider.setBackgroundColor(mDividerColor);
                commentDivider.setVisibility(View.VISIBLE);
            }

            itemView.setBackgroundColor(mCommentBackgroundColor);
            authorTextView.setTextColor(mUsernameColor);
            commentTimeTextView.setTextColor(mSecondaryTextColor);
            commentMarkdownView.setTextColor(mCommentTextColor);
            authorFlairTextView.setTextColor(mAuthorFlairTextColor);
            topScoreTextView.setTextColor(mSecondaryTextColor);
            awardsTextView.setTextColor(mSecondaryTextColor);
            commentDivider.setBackgroundColor(mDividerColor);
            upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            scoreTextView.setTextColor(mCommentIconAndInfoColor);
            downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            moreButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            expandButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            saveButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            replyButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

            authorFlairTextView.setOnClickListener(view -> authorTextView.performClick());

            moreButton.setOnClickListener(view -> {
                Comment comment = getCurrentComment(this);
                Bundle bundle = new Bundle();
                if (!mPost.isArchived() && !mPost.isLocked() && comment.getAuthor().equals(mAccountName)) {
                    bundle.putString(CommentMoreBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                }
                bundle.putParcelable(CommentMoreBottomSheetFragment.EXTRA_COMMENT, comment);
                if (mIsSingleCommentThreadMode) {
                    bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getAdapterPosition() - 2);
                } else {
                    bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getAdapterPosition() - 1);
                }
                CommentMoreBottomSheetFragment commentMoreBottomSheetFragment = new CommentMoreBottomSheetFragment();
                commentMoreBottomSheetFragment.setArguments(bundle);
                commentMoreBottomSheetFragment.show(mActivity.getSupportFragmentManager(), commentMoreBottomSheetFragment.getTag());
            });

            replyButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_reply_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPost.isLocked()) {
                    Toast.makeText(mActivity, R.string.locked_post_reply_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getCurrentComment(this);

                Intent intent = new Intent(mActivity, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, comment.getDepth() + 1);
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_MARKDOWN_KEY, comment.getCommentMarkdown());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_KEY, comment.getCommentRawText());
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, comment.getFullName());
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, true);

                int parentPosition = mIsSingleCommentThreadMode ? getAdapterPosition() - 2 : getAdapterPosition() - 1;
                intent.putExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, parentPosition);
                mActivity.startActivityForResult(intent, CommentActivity.WRITE_COMMENT_REQUEST_CODE);
            });

            upvoteButton.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getCurrentComment(this);
                int previousVoteType = comment.getVoteType();
                String newVoteType;

                downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                if (previousVoteType != Comment.VOTE_TYPE_UPVOTE) {
                    //Not upvoted before
                    comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                    newVoteType = APIUtils.DIR_UPVOTE;
                    upvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    scoreTextView.setTextColor(mUpvotedColor);
                } else {
                    //Upvoted before
                    comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                    newVoteType = APIUtils.DIR_UNVOTE;
                    upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    scoreTextView.setTextColor(mCommentIconAndInfoColor);
                }

                scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                        comment.getScore() + comment.getVoteType()));
                topScoreTextView.setText(mActivity.getString(R.string.top_score,
                        Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                        comment.getScore() + comment.getVoteType())));

                VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position) {
                        if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                            comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                            upvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            scoreTextView.setTextColor(mUpvotedColor);
                        } else {
                            comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                            upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            scoreTextView.setTextColor(mCommentIconAndInfoColor);
                        }

                        downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                        topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType())));
                    }

                    @Override
                    public void onVoteThingFail(int position) {
                    }
                }, comment.getFullName(), newVoteType, getAdapterPosition());
            });

            downvoteButton.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getCurrentComment(this);
                int previousVoteType = comment.getVoteType();
                String newVoteType;

                upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                if (previousVoteType != Comment.VOTE_TYPE_DOWNVOTE) {
                    //Not downvoted before
                    comment.setVoteType(Comment.VOTE_TYPE_DOWNVOTE);
                    newVoteType = APIUtils.DIR_DOWNVOTE;
                    downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    scoreTextView.setTextColor(mDownvotedColor);
                } else {
                    //Downvoted before
                    comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                    newVoteType = APIUtils.DIR_UNVOTE;
                    downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    scoreTextView.setTextColor(mCommentIconAndInfoColor);
                }

                scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                        comment.getScore() + comment.getVoteType()));
                topScoreTextView.setText(mActivity.getString(R.string.top_score,
                        Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                        comment.getScore() + comment.getVoteType())));

                VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position1) {
                        if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                            comment.setVoteType(Comment.VOTE_TYPE_DOWNVOTE);
                            downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            scoreTextView.setTextColor(mDownvotedColor);
                        } else {
                            comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                            downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            scoreTextView.setTextColor(mCommentIconAndInfoColor);
                        }

                        upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                        topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType())));
                    }

                    @Override
                    public void onVoteThingFail(int position1) {
                    }
                }, comment.getFullName(), newVoteType, getAdapterPosition());
            });

            saveButton.setOnClickListener(view -> {
                Comment comment = getCurrentComment(this);
                if (comment.isSaved()) {
                    comment.setSaved(false);
                    SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                        @Override
                        public void success() {
                            comment.setSaved(false);
                            saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                            Toast.makeText(mActivity, R.string.comment_unsaved_success, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            comment.setSaved(true);
                            saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                            Toast.makeText(mActivity, R.string.comment_unsaved_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    comment.setSaved(true);
                    SaveThing.saveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                        @Override
                        public void success() {
                            comment.setSaved(true);
                            saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                            Toast.makeText(mActivity, R.string.comment_saved_success, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            comment.setSaved(false);
                            saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                            Toast.makeText(mActivity, R.string.comment_saved_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            authorTextView.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, getCurrentComment(this).getAuthor());
                mActivity.startActivity(intent);
            });

            expandButton.setOnClickListener(view -> {
                if (expandButton.getVisibility() == View.VISIBLE) {
                    int commentPosition = mIsSingleCommentThreadMode ? getAdapterPosition() - 2 : getAdapterPosition() - 1;
                    if (commentPosition >= 0 && commentPosition < mVisibleComments.size()) {
                        Comment comment = getCurrentComment(this);
                        if (mVisibleComments.get(commentPosition).isExpanded()) {
                            collapseChildren(commentPosition);
                            expandButton.setImageResource(R.drawable.ic_expand_more_grey_24dp);
                        } else {
                            comment.setExpanded(true);
                            ArrayList<Comment> newList = new ArrayList<>();
                            expandChildren(mVisibleComments.get(commentPosition).getChildren(), newList, 0);
                            mVisibleComments.get(commentPosition).setExpanded(true);
                            mVisibleComments.addAll(commentPosition + 1, newList);

                            if (mIsSingleCommentThreadMode) {
                                notifyItemRangeInserted(commentPosition + 3, newList.size());
                            } else {
                                notifyItemRangeInserted(commentPosition + 2, newList.size());
                            }
                            expandButton.setImageResource(R.drawable.ic_expand_less_grey_24dp);
                        }
                    }
                }
            });

            if (mSwapTapAndLong) {
                if (mCommentToolbarHideOnClick) {
                    View.OnLongClickListener hideToolbarOnLongClickListener = view -> hideToolbar();
                    linearLayout.setOnLongClickListener(hideToolbarOnLongClickListener);
                    commentMarkdownView.setOnLongClickListener(hideToolbarOnLongClickListener);
                    commentTimeTextView.setOnLongClickListener(hideToolbarOnLongClickListener);
                }
                View.OnClickListener expandCommentsOnClickListener = view -> expandComments();
                commentMarkdownView.setOnClickListener(expandCommentsOnClickListener);
                itemView.setOnClickListener(expandCommentsOnClickListener);
            } else {
                if (mCommentToolbarHideOnClick) {
                    View.OnClickListener hideToolbarOnClickListener = view -> hideToolbar();
                    linearLayout.setOnClickListener(hideToolbarOnClickListener);
                    commentMarkdownView.setOnClickListener(hideToolbarOnClickListener);
                    commentTimeTextView.setOnClickListener(hideToolbarOnClickListener);
                }
                View.OnLongClickListener expandsCommentsOnLongClickListener = view -> expandComments();
                commentMarkdownView.setOnLongClickListener(expandsCommentsOnLongClickListener);
                itemView.setOnLongClickListener(expandsCommentsOnLongClickListener);
            }
        }

        private boolean expandComments() {
            expandButton.performClick();
            return true;
        }

        private boolean hideToolbar() {
            if (bottomConstraintLayout.getLayoutParams().height == 0) {
                bottomConstraintLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                topScoreTextView.setVisibility(View.GONE);
                ((ViewPostDetailActivity) mActivity).delayTransition();
            } else {
                ((ViewPostDetailActivity) mActivity).delayTransition();
                bottomConstraintLayout.getLayoutParams().height = 0;
                topScoreTextView.setVisibility(View.VISIBLE);
            }
            return true;
        }
    }

    private Comment getCurrentComment(RecyclerView.ViewHolder holder) {
        Comment comment;
        if (mIsSingleCommentThreadMode) {
            comment = mVisibleComments.get(holder.getAdapterPosition() - 2);
        } else {
            comment = mVisibleComments.get(holder.getAdapterPosition() - 1);
        }

        return comment;
    }

    class CommentFullyCollapsedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vertical_block_item_comment_fully_collapsed)
        View verticalBlock;
        @BindView(R.id.user_name_text_view_item_comment_fully_collapsed)
        TextView usernameTextView;
        @BindView(R.id.score_text_view_item_comment_fully_collapsed)
        TextView scoreTextView;
        @BindView(R.id.time_text_view_item_comment_fully_collapsed)
        TextView commentTimeTextView;
        @BindView(R.id.divider_item_comment_fully_collapsed)
        View commentDivider;

        public CommentFullyCollapsedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setBackgroundColor(mFullyCollapsedCommentBackgroundColor);
            usernameTextView.setTextColor(mUsernameColor);
            scoreTextView.setTextColor(mSecondaryTextColor);
            commentTimeTextView.setTextColor(mSecondaryTextColor);

            if (mShowCommentDivider) {
                commentDivider.setBackgroundColor(mDividerColor);
                commentDivider.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(view -> {
                int commentPosition = mIsSingleCommentThreadMode ? getAdapterPosition() - 2 : getAdapterPosition() - 1;
                if (commentPosition >= 0 && commentPosition < mVisibleComments.size()) {
                    Comment comment = getCurrentComment(this);
                    comment.setExpanded(true);
                    ArrayList<Comment> newList = new ArrayList<>();
                    expandChildren(mVisibleComments.get(commentPosition).getChildren(), newList, 0);
                    mVisibleComments.get(commentPosition).setExpanded(true);
                    mVisibleComments.addAll(commentPosition + 1, newList);

                    if (mIsSingleCommentThreadMode) {
                        notifyItemChanged(commentPosition + 2);
                        notifyItemRangeInserted(commentPosition + 3, newList.size());
                    } else {
                        notifyItemChanged(commentPosition + 1);
                        notifyItemRangeInserted(commentPosition + 2, newList.size());
                    }
                }
            });

            itemView.setOnLongClickListener(view -> {
                itemView.performClick();
                return true;
            });
        }
    }

    class LoadMoreChildCommentsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vertical_block_item_load_more_comments)
        View verticalBlock;
        @BindView(R.id.placeholder_text_view_item_load_more_comments)
        TextView placeholderTextView;
        @BindView(R.id.divider_item_load_more_comments_placeholder)
        View commentDivider;

        LoadMoreChildCommentsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (mShowCommentDivider) {
                commentDivider.setVisibility(View.VISIBLE);
            }

            itemView.setBackgroundColor(mCommentBackgroundColor);
            placeholderTextView.setTextColor(mPrimaryTextColor);
            commentDivider.setBackgroundColor(mDividerColor);
        }
    }

    class LoadCommentsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.comment_progress_bar_item_load_comments)
        CircleProgressBar circleProgressBar;

        LoadCommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            circleProgressBar.setBackgroundTintList(ColorStateList.valueOf(mCircularProgressBarBackgroundColor));
            circleProgressBar.setColorSchemeColors(mColorAccent);
        }
    }

    class LoadCommentsFailedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_load_comments_failed_placeholder)
        TextView errorTextView;

        LoadCommentsFailedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> mCommentRecyclerViewAdapterCallback.retryFetchingComments());
            errorTextView.setTextColor(mSecondaryTextColor);
        }
    }

    class NoCommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_no_comment_placeholder)
        TextView errorTextView;

        NoCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            errorTextView.setTextColor(mSecondaryTextColor);
        }
    }

    class IsLoadingMoreCommentsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar_item_comment_footer_loading)
        ProgressBar progressbar;

        IsLoadingMoreCommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            progressbar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
        }
    }

    class LoadMoreCommentsFailedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_comment_footer_error)
        TextView errorTextView;
        @BindView(R.id.retry_button_item_comment_footer_error)
        Button retryButton;

        LoadMoreCommentsFailedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            errorTextView.setText(R.string.load_comments_failed);
            retryButton.setOnClickListener(view -> mCommentRecyclerViewAdapterCallback.retryFetchingMoreComments());
            errorTextView.setTextColor(mSecondaryTextColor);
            retryButton.setBackgroundTintList(ColorStateList.valueOf(mColorPrimaryLightTheme));
            retryButton.setTextColor(mButtonTextColor);
        }
    }

    class ViewAllCommentsViewHolder extends RecyclerView.ViewHolder {

        ViewAllCommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(view -> {
                if (mActivity != null && mActivity instanceof ViewPostDetailActivity) {
                    mIsSingleCommentThreadMode = false;
                    mSingleCommentId = null;
                    ((ViewPostDetailActivity) mActivity).changeToSingleThreadMode();
                }

                itemView.setBackgroundColor(mCommentBackgroundColor);
                ((TextView) itemView).setTextColor(mColorAccent);
            });
        }
    }
}
