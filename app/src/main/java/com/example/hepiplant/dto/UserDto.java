package com.example.hepiplant.dto;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Set;

public class UserDto extends JSONObject implements Serializable {

    private Long id;
    private String username;
    private String uid;
    private String email;
    private boolean notifications;
    private String hourOfNotifications;
    private Set<String> roles;

    public UserDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public String getHourOfNotifications() {
        return hourOfNotifications;
    }

    public void setHourOfNotifications(String hourOfNotifications) {
        this.hourOfNotifications = hourOfNotifications;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
