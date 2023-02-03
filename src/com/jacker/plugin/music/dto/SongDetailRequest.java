package com.jacker.plugin.music.dto;

public class SongDetailRequest {

    private Long id;

    public SongDetailRequest() {
    }

    public SongDetailRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
