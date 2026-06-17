package com.mikstermedia.dto;

import jakarta.validation.constraints.NotBlank;

public class SubmissionDTO {

    // ── Track fields ──────────────────────────────────────────────────────────
    @NotBlank private String trackTitle;
    @NotBlank private String streamUrl;
    @NotBlank private String platformType;
    private String toolsDeclared;
    private String videoUrl;

    // ── Artist profile fields ─────────────────────────────────────────────────
    @NotBlank private String artistName;
    private String artistBio;
    private String artistCountry;
    private String artistPrimaryGenre;
    private String artistImageUrl;
    private String artistProfileUrl;
    private String artistWebsite;

    // ── Submission meta ───────────────────────────────────────────────────────
    private String submitterEmail;

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String getTrackTitle()        { return trackTitle; }
    public void setTrackTitle(String v)  { this.trackTitle = v; }

    public String getStreamUrl()         { return streamUrl; }
    public void setStreamUrl(String v)   { this.streamUrl = v; }

    public String getPlatformType()      { return platformType; }
    public void setPlatformType(String v){ this.platformType = v; }

    public String getToolsDeclared()     { return toolsDeclared; }
    public void setToolsDeclared(String v){ this.toolsDeclared = v; }

    public String getVideoUrl()          { return videoUrl; }
    public void setVideoUrl(String v)    { this.videoUrl = v; }

    public String getArtistName()        { return artistName; }
    public void setArtistName(String v)  { this.artistName = v; }

    public String getArtistBio()         { return artistBio; }
    public void setArtistBio(String v)   { this.artistBio = v; }

    public String getArtistCountry()     { return artistCountry; }
    public void setArtistCountry(String v){ this.artistCountry = v; }

    public String getArtistPrimaryGenre()      { return artistPrimaryGenre; }
    public void setArtistPrimaryGenre(String v) { this.artistPrimaryGenre = v; }

    public String getArtistImageUrl()    { return artistImageUrl; }
    public void setArtistImageUrl(String v){ this.artistImageUrl = v; }

    public String getArtistProfileUrl()  { return artistProfileUrl; }
    public void setArtistProfileUrl(String v){ this.artistProfileUrl = v; }

    public String getArtistWebsite()     { return artistWebsite; }
    public void setArtistWebsite(String v){ this.artistWebsite = v; }

    public String getSubmitterEmail()    { return submitterEmail; }
    public void setSubmitterEmail(String v){ this.submitterEmail = v; }
}
