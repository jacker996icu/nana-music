package com.jacker.plugin.music.dto;

import java.util.List;

public class Track {

    private Long id;

    private String name;

    private String url;

    private Long br;

    private Long size;

    private List<Artist> ar;

    private Album al;

    private Long dt;

    /**
     * 是否有音源
     */
    private Boolean canPlay = true;

    /**
     * 是否试听
     */
    private Boolean tryPlay = false;

    private Boolean currentPlay;

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

    public List<Artist> getAr() {
        return ar;
    }

    public void setAr(List<Artist> ar) {
        this.ar = ar;
    }

    public Album getAl() {
        return al;
    }

    public void setAl(Album al) {
        this.al = al;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getBr() {
        return br;
    }

    public void setBr(Long br) {
        this.br = br;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getDt() {
        return dt;
    }

    public void setDt(Long dt) {
        this.dt = dt;
    }

    public Boolean getCanPlay() {
        return canPlay;
    }

    public void setCanPlay(Boolean canPlay) {
        this.canPlay = canPlay;
    }

    public Boolean getTryPlay() {
        return tryPlay;
    }

    public void setTryPlay(Boolean tryPlay) {
        this.tryPlay = tryPlay;
    }

    public Boolean getCurrentPlay() {
        return currentPlay;
    }

    public void setCurrentPlay(Boolean currentPlay) {
        this.currentPlay = currentPlay;
    }

    @Override
    public String toString() {
        if (getTryPlay()) {
            return getName() + " [vip]";
        }
        return getName();
    }
}
