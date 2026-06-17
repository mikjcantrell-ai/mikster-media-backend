package com.mikstermedia.dto;

public class SpotifySearchResult {
    private String spotifyId;
    private String title;
    private String artist;
    private String album;
    private String albumImageUrl;   // used by AiDiscoveryService / SpotifyController
    private String imageUrl;        // alias used by SpotifyService
    private String spotifyUrl;
    private String embedUrl;
    private int durationMs;
    private int popularity;
    private String releaseDate;
    private String publishedAt;     // YouTube ISO timestamp
    private String platformSource;  // "Spotify" or "YouTube"
    private String primaryArtistId;
    private boolean alreadyImported;
    private String youtubeUrl;         // optional YouTube link carried alongside a Spotify result

    public SpotifySearchResult() {}

    public SpotifySearchResult(String spotifyId, String title, String artist, String album,
                               String imageUrl, String spotifyUrl, String embedUrl,
                               int durationMs, int popularity) {
        this.spotifyId    = spotifyId;
        this.title        = title;
        this.artist       = artist;
        this.album        = album;
        this.imageUrl     = imageUrl;
        this.albumImageUrl = imageUrl;  // keep both in sync
        this.spotifyUrl   = spotifyUrl;
        this.embedUrl     = embedUrl;
        this.durationMs   = durationMs;
        this.popularity   = popularity;
    }

    public String getSpotifyId()    { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }
    public String getTitle()        { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist()       { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getAlbum()        { return album; }
    public void setAlbum(String album) { this.album = album; }
    public String getAlbumImageUrl() { return albumImageUrl != null ? albumImageUrl : imageUrl; }
    public void setAlbumImageUrl(String albumImageUrl) { this.albumImageUrl = albumImageUrl; this.imageUrl = albumImageUrl; }
    public String getImageUrl()     { return imageUrl != null ? imageUrl : albumImageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; this.albumImageUrl = imageUrl; }
    public String getSpotifyUrl()   { return spotifyUrl; }
    public void setSpotifyUrl(String spotifyUrl) { this.spotifyUrl = spotifyUrl; }
    public String getEmbedUrl()     { return embedUrl; }
    public void setEmbedUrl(String embedUrl) { this.embedUrl = embedUrl; }
    public int getDurationMs()      { return durationMs; }
    public void setDurationMs(int durationMs) { this.durationMs = durationMs; }
    public int getPopularity()      { return popularity; }
    public void setPopularity(int popularity) { this.popularity = popularity; }
    public String getReleaseDate()  { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public String getPublishedAt()  { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    public String getPlatformSource() { return platformSource; }
    public void setPlatformSource(String platformSource) { this.platformSource = platformSource; }
    public String getPrimaryArtistId() { return primaryArtistId; }
    public void setPrimaryArtistId(String primaryArtistId) { this.primaryArtistId = primaryArtistId; }
    public boolean isAlreadyImported() { return alreadyImported; }
    public void setAlreadyImported(boolean alreadyImported) { this.alreadyImported = alreadyImported; }
    public String getYoutubeUrl()   { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }
}
