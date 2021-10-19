package com.example.hepiplant.dto;

public class AuthenticationResponseDto {

    private String jwt;

    public AuthenticationResponseDto() {
    }

    public AuthenticationResponseDto(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
