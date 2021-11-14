package com.example.hepiplant.dto;

public class UserStatisticsDto {

    private UserDto user;
    private long plantsAmount;
    private long postsAmount;
    private long salesOffersAmount;
    private long commentsAmount;

    public UserStatisticsDto() {
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public long getPlantsAmount() {
        return plantsAmount;
    }

    public void setPlantsAmount(long plantsAmount) {
        this.plantsAmount = plantsAmount;
    }

    public long getPostsAmount() {
        return postsAmount;
    }

    public void setPostsAmount(long postsAmount) {
        this.postsAmount = postsAmount;
    }

    public long getSalesOffersAmount() {
        return salesOffersAmount;
    }

    public void setSalesOffersAmount(long salesOffersAmount) {
        this.salesOffersAmount = salesOffersAmount;
    }

    public long getCommentsAmount() {
        return commentsAmount;
    }

    public void setCommentsAmount(long commentsAmount) {
        this.commentsAmount = commentsAmount;
    }
}
