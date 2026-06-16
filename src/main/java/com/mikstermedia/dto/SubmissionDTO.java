package com.mikstermedia.dto;

import jakarta.validation.constraints.NotBlank;

public class SubmissionDTO {
    @NotBlank private String trackTitle;
    @NotBlank private String artistName;
    @NotBlank private String streamUrl;
    @NotBlank private String platformType;
    private String toolsDeclared;
    private String submitterEmail;
    private String videoUrl;
    private String artistWebsite;

    public String getTrackTitle()     { return trackTitle; }
    public void setTrackTitle(String trackTitle) { this.trackTitle = trackTitle; }
    public String getArtistName()     { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getStreamUrl()      { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
    public String getPlatformType()   { return platformType; }
    public void setPlatformType(String platformType) { this.platformType = platformType; }
    public String getToolsDeclared()  { return toolsDeclared; }
    public void setToolsDeclared(String toolsDeclared) { this.toolsDeclared = toolsDeclared; }
    public String getSubmitterEmail() { return submitterEmail; }
    public void setSubmitterEmail(String submitterEmail) { this.submitterEmail = submitterEmail; }
    public String getVideoUrl()       { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getArtistWebsite()  { return artistWebsite; }
    public void setArtistWebsite(String artistWebsite) { this.artistWebsite = artistWebsite; }
}
