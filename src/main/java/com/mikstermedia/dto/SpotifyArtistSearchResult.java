package com.mikstermedia.dto;

public class SpotifyArtistSearchResult {
    private String spotifyId;
    private String name;
    private String imageUrl;
    private String profileUrl;
    private String genres;
    private int popularity;
    private boolean alreadyImported;

    public SpotifyArtistSearchResult() {}

    public SpotifyArtistSearchResult(String spotifyId, String name, String imageUrl, String profileUrl, String genres, int popularity) {
        this.spotifyId = spotifyId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.profileUrl = profileUrl;
        this.genres = genres;
        this.popularity = popularity;
    }

    public String getSpotifyId() { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public int getPopularity() { return popularity; }
    public void setPopularity(int popularity) { this.popularity = popularity; }

    public boolean isAlreadyImported() { return alreadyImported; }
    public void setAlreadyImported(boolean alreadyImported) { this.alreadyImported = alreadyImported; }
}
