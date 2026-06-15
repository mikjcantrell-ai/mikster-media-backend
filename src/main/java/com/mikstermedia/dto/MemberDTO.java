package com.mikstermedia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class MemberDTO {
    @NotBlank
    private String displayName;

    @Email @NotBlank
    private String email;

    private String username;
    private String membershipTier;
    private String primaryAiTool;
    private String genreInterest;
    private boolean newsletterOptIn;
    private String password;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMembershipTier() { return membershipTier; }
    public void setMembershipTier(String membershipTier) { this.membershipTier = membershipTier; }
    public String getPrimaryAiTool() { return primaryAiTool; }
    public void setPrimaryAiTool(String primaryAiTool) { this.primaryAiTool = primaryAiTool; }
    public String getGenreInterest() { return genreInterest; }
    public void setGenreInterest(String genreInterest) { this.genreInterest = genreInterest; }
    public boolean isNewsletterOptIn() { return newsletterOptIn; }
    public void setNewsletterOptIn(boolean newsletterOptIn) { this.newsletterOptIn = newsletterOptIn; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
