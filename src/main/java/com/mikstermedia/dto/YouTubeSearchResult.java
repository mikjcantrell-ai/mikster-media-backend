package com.mikstermedia.dto;

public class YouTubeSearchResult {
    private String youtubeId;
    private String title;
    private String channelTitle;
    private String thumbnailUrl;
    private String youtubeUrl;
    private String embedUrl;
    private long views;
    private String publishedAt;
    private boolean alreadyImported;

    public YouTubeSearchResult() {}
    public YouTubeSearchResult(String youtubeId, String title, String channelTitle,
                               String thumbnailUrl, String youtubeUrl, String embedUrl,
                               long views, String publishedAt) {
        this.youtubeId = youtubeId;
        this.title = title;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.youtubeUrl = youtubeUrl;
        this.embedUrl = embedUrl;
        this.views = views;
        this.publishedAt = publishedAt;
    }

    public String getYoutubeId() { return youtubeId; }
    public void setYoutubeId(String youtubeId) { this.youtubeId = youtubeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getChannelTitle() { return channelTitle; }
    public void setChannelTitle(String channelTitle) { this.channelTitle = channelTitle; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }
    public String getEmbedUrl() { return embedUrl; }
    public void setEmbedUrl(String embedUrl) { this.embedUrl = embedUrl; }
    public long getViews() { return views; }
    public void setViews(long views) { this.views = views; }
    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    public boolean isAlreadyImported() { return alreadyImported; }
    public void setAlreadyImported(boolean alreadyImported) { this.alreadyImported = alreadyImported; }
}
