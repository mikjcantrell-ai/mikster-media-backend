package com.mikstermedia.dto;

public class OAuth2LoginDTO {
    private String provider;
    private String idToken;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
