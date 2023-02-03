package com.jacker.plugin.music.dto;

public class Profile {

    private Long userId;

    private String nickname;

    private String avatarUrl;

    private String backgroundUrl;

    private String signature;

    private Long followeds;

    private Long follows;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Long getFolloweds() {
        return followeds;
    }

    public void setFolloweds(Long followeds) {
        this.followeds = followeds;
    }

    public Long getFollows() {
        return follows;
    }

    public void setFollows(Long follows) {
        this.follows = follows;
    }
}
