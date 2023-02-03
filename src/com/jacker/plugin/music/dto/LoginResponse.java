package com.jacker.plugin.music.dto;

public class LoginResponse {

    private Integer code;

    private Profile profile;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "code=" + code +
                ", profile=" + profile +
                '}';
    }
}
