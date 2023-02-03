package com.jacker.plugin.music.dto;

import java.util.List;

public class PlaylistDetailResponse {

    private Integer code;

    private Playlist playlist;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }
}
