package com.jacker.plugin.music.dto;

import java.util.List;

public class PlaylistResponse {

    private Integer code;

    private List<Playlist> playlist;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List<Playlist> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<Playlist> playlist) {
        this.playlist = playlist;
    }
}
