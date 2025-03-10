package ml.docilealligator.infinityforreddit.Post;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import ml.docilealligator.infinityforreddit.Utils.APIUtils;

/**
 * Created by alex on 3/1/18.
 */

public class Post implements Parcelable {
    public static final int NSFW_TYPE = -1;
    public static final int TEXT_TYPE = 0;
    public static final int IMAGE_TYPE = 1;
    public static final int LINK_TYPE = 2;
    public static final int VIDEO_TYPE = 3;
    public static final int GIF_TYPE = 4;
    public static final int NO_PREVIEW_LINK_TYPE = 5;
    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
    private String id;
    private String fullName;
    private String subredditName;
    private String subredditNamePrefixed;
    private String subredditIconUrl;
    private String author;
    private String authorNamePrefixed;
    private String authorIconUrl;
    private String authorFlair;
    private String authorFlairHTML;
    private String title;
    private String selfText;
    private String selfTextPlain;
    private String selfTextPlainTrimmed;
    private String previewUrl;
    private String thumbnailPreviewUrl;
    private String url;
    private String videoUrl;
    private String videoDownloadUrl;
    private String permalink;
    private String flair;
    private String awards;
    private int nAwards;
    private long postTimeMillis;
    private int score;
    private int postType;
    private int voteType;
    private int previewWidth;
    private int previewHeight;
    private int nComments;
    private boolean hidden;
    private boolean spoiler;
    private boolean nsfw;
    private boolean stickied;
    private boolean archived;
    private boolean locked;
    private boolean saved;
    private boolean isCrosspost;
    private String crosspostParentId;

    public Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
                String author, String authorFlair, String authorFlairHTML,
                long postTimeMillis, String title, String previewUrl, String thumbnailPreviewUrl,
                String permalink, int score, int postType, int voteType, int nComments, String flair,
                String awards, int nAwards, boolean hidden, boolean spoiler, boolean nsfw, boolean stickied,
                boolean archived, boolean locked, boolean saved, boolean isCrosspost) {
        this.id = id;
        this.fullName = fullName;
        this.subredditName = subredditName;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
        this.authorFlair = authorFlair;
        this.authorFlairHTML = authorFlairHTML;
        this.postTimeMillis = postTimeMillis;
        this.title = title;
        this.previewUrl = previewUrl;
        this.thumbnailPreviewUrl = thumbnailPreviewUrl;
        this.permalink = APIUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.nComments = nComments;
        this.flair = flair;
        this.awards = awards;
        this.nAwards = nAwards;
        this.hidden = hidden;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.locked = locked;
        this.saved = saved;
        this.isCrosspost = isCrosspost;
    }

    public Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
                String author, String authorFlair, String authorFlairHTML,
                long postTimeMillis, String title, String previewUrl, String thumbnailPreviewUrl,
                String url, String permalink, int score, int postType, int voteType, int nComments,
                String flair, String awards, int nAwards, boolean hidden, boolean spoiler, boolean nsfw,
                boolean stickied, boolean archived, boolean locked, boolean saved, boolean isCrosspost) {
        this.id = id;
        this.fullName = fullName;
        this.subredditName = subredditName;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
        this.authorFlair = authorFlair;
        this.authorFlairHTML = authorFlairHTML;
        this.postTimeMillis = postTimeMillis;
        this.title = title;
        this.previewUrl = previewUrl;
        this.thumbnailPreviewUrl = thumbnailPreviewUrl;
        this.url = url;
        this.permalink = APIUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.nComments = nComments;
        this.flair = flair;
        this.awards = awards;
        this.nAwards = nAwards;
        this.hidden = hidden;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.locked = locked;
        this.saved = saved;
        this.isCrosspost = isCrosspost;
    }

    public Post(String id, String fullName, String subredditName, String subredditNamePrefixed,
                String author, String authorFlair, String authorFlairHTML,
                long postTimeMillis, String title, String permalink, int score, int postType,
                int voteType, int nComments, String flair, String awards, int nAwards, boolean hidden,
                boolean spoiler, boolean nsfw, boolean stickied, boolean archived, boolean locked,
                boolean saved, boolean isCrosspost) {
        this.id = id;
        this.fullName = fullName;
        this.subredditName = subredditName;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
        this.authorFlair = authorFlair;
        this.authorFlairHTML = authorFlairHTML;
        this.postTimeMillis = postTimeMillis;
        this.title = title;
        this.permalink = APIUtils.API_BASE_URI + permalink;
        this.score = score;
        this.postType = postType;
        this.voteType = voteType;
        this.nComments = nComments;
        this.flair = flair;
        this.awards = awards;
        this.nAwards = nAwards;
        this.hidden = hidden;
        this.spoiler = spoiler;
        this.nsfw = nsfw;
        this.stickied = stickied;
        this.archived = archived;
        this.locked = locked;
        this.saved = saved;
        this.isCrosspost = isCrosspost;
    }

    protected Post(Parcel in) {
        id = in.readString();
        fullName = in.readString();
        subredditName = in.readString();
        subredditNamePrefixed = in.readString();
        subredditIconUrl = in.readString();
        author = in.readString();
        authorNamePrefixed = in.readString();
        authorFlair = in.readString();
        authorFlairHTML = in.readString();
        authorIconUrl = in.readString();
        postTimeMillis = in.readLong();
        title = in.readString();
        selfText = in.readString();
        selfTextPlain = in.readString();
        selfTextPlainTrimmed = in.readString();
        previewUrl = in.readString();
        thumbnailPreviewUrl = in.readString();
        url = in.readString();
        videoUrl = in.readString();
        videoDownloadUrl = in.readString();
        permalink = in.readString();
        flair = in.readString();
        awards = in.readString();
        nAwards = in.readInt();
        score = in.readInt();
        postType = in.readInt();
        voteType = in.readInt();
        previewWidth = in.readInt();
        previewHeight = in.readInt();
        nComments = in.readInt();
        hidden = in.readByte() != 0;
        spoiler = in.readByte() != 0;
        nsfw = in.readByte() != 0;
        stickied = in.readByte() != 0;
        archived = in.readByte() != 0;
        locked = in.readByte() != 0;
        saved = in.readByte() != 0;
        isCrosspost = in.readByte() != 0;
        crosspostParentId = in.readString();
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSubredditName() {
        return subredditName;
    }

    public String getSubredditNamePrefixed() {
        return subredditNamePrefixed;
    }

    public String getSubredditIconUrl() {
        return subredditIconUrl;
    }

    public void setSubredditIconUrl(String subredditIconUrl) {
        this.subredditIconUrl = subredditIconUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
        this.authorNamePrefixed = "u/" + author;
    }

    public String getAuthorNamePrefixed() {
        return authorNamePrefixed;
    }

    public String getAuthorFlair() {
        return authorFlair;
    }

    public String getAuthorFlairHTML() {
        return authorFlairHTML;
    }

    public String getAuthorIconUrl() {
        return authorIconUrl;
    }

    public void setAuthorIconUrl(String authorIconUrl) {
        this.authorIconUrl = authorIconUrl;
    }

    public long getPostTimeMillis() {
        return postTimeMillis;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSelfText() {
        return selfText;
    }

    public void setSelfText(String selfText) {
        this.selfText = selfText;
    }

    public String getSelfTextPlain() {
        return selfTextPlain;
    }

    public void setSelfTextPlain(String selfTextPlain) {
        this.selfTextPlain = selfTextPlain;
    }

    public String getSelfTextPlainTrimmed() {
        return selfTextPlainTrimmed;
    }

    public void setSelfTextPlainTrimmed(String selfTextPlainTrimmed) {
        this.selfTextPlainTrimmed = selfTextPlainTrimmed;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getThumbnailPreviewUrl() {
        return thumbnailPreviewUrl;
    }

    public void setThumbnailPreviewUrl(String thumbnailPreviewUrl) {
        this.thumbnailPreviewUrl = thumbnailPreviewUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoDownloadUrl() {
        return videoDownloadUrl;
    }

    public void setVideoDownloadUrl(String videoDownloadUrl) {
        this.videoDownloadUrl = videoDownloadUrl;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getFlair() {
        return flair;
    }

    public void setFlair(String flair) {
        this.flair = flair;
    }

    public String getAwards() {
        return awards;
    }

    public int getnAwards() {
        return nAwards;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getPostType() {
        return postType;
    }

    public void setPostType(int postType) {
        this.postType = postType;
    }

    public int getVoteType() {
        return voteType;
    }

    public void setVoteType(int voteType) {
        this.voteType = voteType;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public int getNComments() {
        return nComments;
    }

    public void setNComments(int nComments) {
        this.nComments = nComments;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isSpoiler() {
        return spoiler;
    }

    public void setSpoiler(boolean spoiler) {
        this.spoiler = spoiler;
    }

    public boolean isNSFW() {
        return nsfw;
    }

    public void setNSFW(boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isStickied() {
        return stickied;
    }

    public boolean isArchived() {
        return archived;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isCrosspost() {
        return isCrosspost;
    }

    public String getCrosspostParentId() {
        return crosspostParentId;
    }

    public void setCrosspostParentId(String crosspostParentId) {
        this.crosspostParentId = crosspostParentId;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(fullName);
        parcel.writeString(subredditName);
        parcel.writeString(subredditNamePrefixed);
        parcel.writeString(subredditIconUrl);
        parcel.writeString(author);
        parcel.writeString(authorNamePrefixed);
        parcel.writeString(authorFlair);
        parcel.writeString(authorFlairHTML);
        parcel.writeString(authorIconUrl);
        parcel.writeLong(postTimeMillis);
        parcel.writeString(title);
        parcel.writeString(selfText);
        parcel.writeString(selfTextPlain);
        parcel.writeString(selfTextPlainTrimmed);
        parcel.writeString(previewUrl);
        parcel.writeString(thumbnailPreviewUrl);
        parcel.writeString(url);
        parcel.writeString(videoUrl);
        parcel.writeString(videoDownloadUrl);
        parcel.writeString(permalink);
        parcel.writeString(flair);
        parcel.writeString(awards);
        parcel.writeInt(nAwards);
        parcel.writeInt(score);
        parcel.writeInt(postType);
        parcel.writeInt(voteType);
        parcel.writeInt(previewWidth);
        parcel.writeInt(previewHeight);
        parcel.writeInt(nComments);
        parcel.writeByte((byte) (hidden ? 1 : 0));
        parcel.writeByte((byte) (spoiler ? 1 : 0));
        parcel.writeByte((byte) (nsfw ? 1 : 0));
        parcel.writeByte((byte) (stickied ? 1 : 0));
        parcel.writeByte((byte) (archived ? 1 : 0));
        parcel.writeByte((byte) (locked ? 1 : 0));
        parcel.writeByte((byte) (saved ? 1 : 0));
        parcel.writeByte((byte) (isCrosspost ? 1 : 0));
        parcel.writeString(crosspostParentId);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Post)) {
            return false;
        }
        return ((Post) obj).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
