package com.jacker.plugin.music.dto;

import java.util.List;

public class SongUrlResponse {

    private Long code;

    private List<Track> data;

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public List<Track> getData() {
        return data;
    }

    public void setData(List<Track> data) {
        this.data = data;
    }
}
