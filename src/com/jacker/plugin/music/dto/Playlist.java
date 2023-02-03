package com.jacker.plugin.music.dto;

import java.util.List;

public class Playlist {

    private Long id;

    private String name;

    private String coverImgUrl;

    private Long trackCount;

    private List<Track> tracks;

    private List<TrackId> trackIds;

    private Boolean subscribed;

    private Boolean userId;

    public Playlist() {
    }

    public Playlist(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static class TrackId {

        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
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

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public Long getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(Long trackCount) {
        this.trackCount = trackCount;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public List<TrackId> getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(List<TrackId> trackIds) {
        this.trackIds = trackIds;
    }

    public Boolean getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    public Boolean getUserId() {
        return userId;
    }

    public void setUserId(Boolean userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return getName() + "[" + getTrackCount() + "]";
    }
}
