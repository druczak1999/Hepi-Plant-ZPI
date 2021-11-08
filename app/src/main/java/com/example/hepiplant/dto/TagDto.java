package com.example.hepiplant.dto;

import java.util.Set;

public class TagDto {

    private Long id;
    private String name;
    private Set<Long> posts;
    private Set<Long> salesOffer;

    public TagDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Long> getPosts() {
        return posts;
    }

    public void setPosts(Set<Long> posts) {
        this.posts = posts;
    }

    public Set<Long> getSalesOffer() {
        return salesOffer;
    }

    public void setSalesOffer(Set<Long> salesOffer) {
        this.salesOffer = salesOffer;
    }
}
