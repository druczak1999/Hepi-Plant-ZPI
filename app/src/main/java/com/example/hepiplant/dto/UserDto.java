package com.example.hepiplant.dto;


import com.example.hepiplant.dto.enums.Permission;

import org.json.JSONObject;

import java.io.Serializable;

public class UserDto extends JSONObject implements Serializable {

    private Long id;
    private String username;
    private String uid;
    private String email;
    private Permission permission;

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

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
