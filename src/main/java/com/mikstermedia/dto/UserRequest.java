package com.mikstermedia.dto;

public class UserRequest {
    private String username;
    private String password;
    private String email;
    private String role;
    private String displayName;
    private Boolean active;

    public String getUsername()    { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword()    { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail()       { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole()        { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Boolean getActive()     { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
