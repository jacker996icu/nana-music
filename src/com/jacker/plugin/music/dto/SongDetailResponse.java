package com.jacker.plugin.music.dto;

import java.util.List;

public class SongDetailResponse {

    private Integer code;

    private List<Track> songs;

    private List<Privilege> privileges;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List<Track> getSongs() {
        return songs;
    }

    public void setSongs(List<Track> songs) {
        this.songs = songs;
    }

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }
}
