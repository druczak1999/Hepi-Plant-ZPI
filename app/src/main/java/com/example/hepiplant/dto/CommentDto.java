package com.example.hepiplant.dto;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.hepiplant.helper.DateUtils;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CommentDto implements Serializable {

    private Long id;
    private String body;
    private String createdDate;
    private String updatedDate;
    private Long userId;
    private String username;
    private Long postId;

    public CommentDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LocalDateTime getCreatedLocalDate() {
        return DateUtils.convertToLocalDateTime(createdDate);
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
