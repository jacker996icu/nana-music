package com.jacker.plugin.music.dto;

import java.util.List;
import java.util.Objects;

public class RadioResponse {

    private Integer code;

    private List<RadioTrack> data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List<RadioTrack> getData() {
        return data;
    }

    public void setData(List<RadioTrack> data) {
        this.data = data;
    }

    public static class RadioTrack {
        private Long id;

        private String name;

        private List<Artist> artists;

        private Album album;

        private Long duration;

        private Boolean starred;

        private Privilege privilege;

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

        public List<Artist> getArtists() {
            return artists;
        }

        public void setArtists(List<Artist> artists) {
            this.artists = artists;
        }

        public Album getAlbum() {
            return album;
        }

        public void setAlbum(Album album) {
            this.album = album;
        }

        public Long getDuration() {
            return duration;
        }

        public void setDuration(Long duration) {
            this.duration = duration;
        }

        public Boolean getStarred() {
            return starred;
        }

        public void setStarred(Boolean starred) {
            this.starred = starred;
        }

        public Privilege getPrivilege() {
            return privilege;
        }

        public void setPrivilege(Privilege privilege) {
            this.privilege = privilege;
        }

        public Track getTrack() {
            Track track = new Track();
            track.setId(id);
            track.setName(name);
            track.setAr(artists);
            track.setAl(album);
            track.setDt(duration);
            // 无音源
            if (Objects.equals(privilege.getSt(), -200)) {
                track.setCanPlay(false);
            }
            // 试听，未购买，没有云存储
            if ((Objects.equals(privilege.getFee(), 1) || Objects.equals(privilege.getFee(), 4))
                    && Objects.equals(privilege.getPayed(), 0)
                    && Objects.equals(privilege.getCs(), false)) {
                track.setTryPlay(true);
            }
            return track;
        }
    }


}
