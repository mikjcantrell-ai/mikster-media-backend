package com.mikstermedia.dto;

import java.time.LocalDate;

public class TrackDTO {
    private String title;
    private String creator;
    private String mediaUrl;
    private String platformSource;
    private String aiToolsUsed;
    private String genre;
    private boolean featuredStatus;
    private String embedUrl;
    private String videoUrl;
    private String aiSourceUrl;
    private String imageUrl;
    private String promptRecipe;
    private Integer spotifyPopularity;
    private Integer lastFmScrobbles;
    private Long youtubeViews;
    private Integer sunoPlays;
    private Integer udioPlays;
    private Integer tiktokViews;
    private Integer chartmetricScore;
    private String releaseDate;
    private String album;
    private String spotifyId;

    public String getTitle()           { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCreator()         { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getMediaUrl()        { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getPlatformSource()  { return platformSource; }
    public void setPlatformSource(String platformSource) { this.platformSource = platformSource; }
    public String getAiToolsUsed()     { return aiToolsUsed; }
    public void setAiToolsUsed(String aiToolsUsed) { this.aiToolsUsed = aiToolsUsed; }
    public String getGenre()           { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public boolean isFeaturedStatus()  { return featuredStatus; }
    public void setFeaturedStatus(boolean featuredStatus) { this.featuredStatus = featuredStatus; }
    public String getEmbedUrl()        { return embedUrl; }
    public void setEmbedUrl(String embedUrl) { this.embedUrl = embedUrl; }
    public String getVideoUrl()        { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getAiSourceUrl()     { return aiSourceUrl; }
    public void setAiSourceUrl(String aiSourceUrl) { this.aiSourceUrl = aiSourceUrl; }
    public String getImageUrl()        { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPromptRecipe()    { return promptRecipe; }
    public void setPromptRecipe(String promptRecipe) { this.promptRecipe = promptRecipe; }
    public Integer getSpotifyPopularity() { return spotifyPopularity; }
    public void setSpotifyPopularity(Integer spotifyPopularity) { this.spotifyPopularity = spotifyPopularity; }
    public Integer getLastFmScrobbles() { return lastFmScrobbles; }
    public void setLastFmScrobbles(Integer lastFmScrobbles) { this.lastFmScrobbles = lastFmScrobbles; }
    public Long getYoutubeViews()      { return youtubeViews; }
    public void setYoutubeViews(Long youtubeViews) { this.youtubeViews = youtubeViews; }
    public Integer getSunoPlays()      { return sunoPlays; }
    public void setSunoPlays(Integer sunoPlays) { this.sunoPlays = sunoPlays; }
    public Integer getUdioPlays()      { return udioPlays; }
    public void setUdioPlays(Integer udioPlays) { this.udioPlays = udioPlays; }
    public Integer getTiktokViews()       { return tiktokViews; }
    public void setTiktokViews(Integer tiktokViews) { this.tiktokViews = tiktokViews; }
    public Integer getChartmetricScore() { return chartmetricScore; }
    public void setChartmetricScore(Integer chartmetricScore) { this.chartmetricScore = chartmetricScore; }
    public String getReleaseDate()     { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public String getAlbum()           { return album; }
    public void setAlbum(String album) { this.album = album; }
    public String getSpotifyId()       { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }
}
