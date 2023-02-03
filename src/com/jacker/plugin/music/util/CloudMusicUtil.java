package com.jacker.plugin.music.util;

import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.jacker.plugin.music.dto.*;
import com.jacker.plugin.music.manager.ViewManager;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.setting.MusicPersistentConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CloudMusicUtil {

    public static String cookie;

    public static String csrf;

    public static String musicU;

    private static Long uid;

    public static boolean checkLogin() {
        if (StringUtils.isBlank(cookie)) {
            return false;
        }
        PlaylistResponse playlistResponse = getPlaylist(30, 0);
        return playlistResponse != null && CollectionUtils.isNotEmpty(playlistResponse.getPlaylist());
    }

    public static PlaylistResponse getPlaylist(int limit, int offset) {
        // 获取歌单
        String playlistUrl = "http://music.163.com/api/user/playlist?uid=" + uid + "&limit=" + limit + "&offset=" + offset;
        Map<String, String> headers = getHeaders(cookie);
        Pair<String, Header[]> playlistResponse;
        try {
            playlistResponse = HttpClientUtil.post(playlistUrl, null, headers);
        } catch (Exception e) {
            return null;
        }
        return JackSonUtil.convertJsonToBean(playlistResponse.getKey(), PlaylistResponse.class);
    }

    public static List<Track> getSongs(Long playlistId) {
        // 获取歌单详情
        String playlistDetailUrl = "http://music.163.com/api/v6/playlist/detail?id=" + playlistId;
        Map<String, String> headers = getHeaders(cookie);
        Pair<String, Header[]> playlistDetailResponse;
        try {
            playlistDetailResponse = HttpClientUtil.post(playlistDetailUrl, null, headers);
            PlaylistDetailResponse convertJsonToBean = JackSonUtil.convertJsonToBean(playlistDetailResponse.getKey(), PlaylistDetailResponse.class);
            if (convertJsonToBean != null && CollectionUtils.isNotEmpty(convertJsonToBean.getPlaylist().getTrackIds())) {
                List<Playlist.TrackId> trackIds = convertJsonToBean.getPlaylist().getTrackIds();
                List<SongDetailRequest> requests = Lists.newArrayList();
                for (Playlist.TrackId trackId : trackIds) {
                    requests.add(new SongDetailRequest(trackId.getId()));
                }
                String songDetailUrl = "http://music.163.com/weapi/v3/song/detail";
                Map<String, String> up = Maps.newHashMap();
                up.put("c", JackSonUtil.convertJson(requests));
                up.put("csrf_token", csrf);
                Pair<String, Header[]> songDetailResponsePair = HttpClientUtil.post(songDetailUrl, CryptoUtil.getDataWapi(up), headers);
                SongDetailResponse songDetailResponse = JackSonUtil.convertJsonToBean(songDetailResponsePair.getKey(), SongDetailResponse.class);
                if (songDetailResponse != null) {
                    List<Track> songs = songDetailResponse.getSongs();
                    List<Privilege> privileges = songDetailResponse.getPrivileges();
                    for (int i = 0; i < privileges.size(); i++) {
                        Privilege privilege = privileges.get(i);
                        // 无音源
                        if (Objects.equals(privilege.getSt(), -200)) {
                            songs.get(i).setCanPlay(false);
                        }
                        // 试听，未购买，没有云存储
                        if ((Objects.equals(privilege.getFee(), 1) || Objects.equals(privilege.getFee(), 4))
                                && Objects.equals(privilege.getPayed(), 0)
                                && Objects.equals(privilege.getCs(), false)) {
                            songs.get(i).setTryPlay(true);
                        }
                    }
                    return songs;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Track> getRadioSongs() {
        // 获取歌单详情
        String radioUrl = "https://music.163.com/weapi/v1/radio/get";
        Map<String, String> headers = getHeaders(cookie);
        Pair<String, Header[]> radioListResponse;
        try {
            radioListResponse = HttpClientUtil.post(radioUrl, null, headers);
            RadioResponse convertJsonToBean = JackSonUtil.convertJsonToBean(radioListResponse.getKey(), RadioResponse.class);
            if (convertJsonToBean != null && CollectionUtils.isNotEmpty(convertJsonToBean.getData())) {
                return convertJsonToBean.getData().stream().map(RadioResponse.RadioTrack::getTrack).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSongUrl(Long songId) {
        // 歌曲地址
        String ids = String.valueOf(songId);
        String songUrl = "http://interface3.music.163.com/eapi/song/enhance/player/url";
        String urlParam = "/api/song/enhance/player/url";
        Map<String, Object> up1 = Maps.newHashMap();
        up1.put("ids", "[" + ids + "]");
        up1.put("br", "9900");
        Map<String, String> headers = getHeaders(cookie);
        headers.put("appver", "8.0.0");
        headers.put("versioncode", "140");
        headers.put("buildver", "1629524860");
        headers.put("resolution", "1920x1080");
        headers.put("os", "pc");
        String requestId = System.currentTimeMillis() + "_" + CryptoUtil.zfill(String.valueOf(ThreadLocalRandom.current().nextInt(1000)), 4);
        headers.put("requestId", requestId);
        headers.put("__csrf", csrf);
        headers.put("MUSIC_U", musicU);
        up1.put("header", headers);
        Pair<String, Header[]> songUrlResponsePair;
        try {
            songUrlResponsePair = HttpClientUtil.post(songUrl, CryptoUtil.getDataEapi(urlParam, up1), headers);
            SongUrlResponse songUrlResponse = JackSonUtil.convertJsonToBean(songUrlResponsePair.getKey(), SongUrlResponse.class);
            if (songUrlResponse != null) {
                return songUrlResponse.getData().get(0).getUrl();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean cellphoneLogin(MusicConfig config, Project project) {
        String cellphone = config.getCellphone();
        String password = MusicPersistentConfig.getInstance().getPassword(cellphone);
        if (StringUtils.isBlank(password)) {
            return false;
        }
        String url = "http://music.163.com/weapi/login/cellphone?csrf_token=";
        Map<String, String> params = Maps.newHashMap();
        params.put("phone", cellphone);
        params.put("countrycode", "86");
        String pwd = CryptoUtil.md5(password);
        params.put("password", pwd);
        params.put("rememberLogin", "true");
        params.put("csrf_token", csrf);
        Map<String, String> headers = getHeaders("os=pc; appver=2.9.7");
        HashMap<String, String> data = CryptoUtil.getDataWapi(params);
        Pair<String, Header[]> response = null;
        try {
            response = HttpClientUtil.post(url, data, headers);
        } catch (Exception e) {
            MessageUtils.getInstance(project).showWarnMsg(PropertiesUtils.getInfo("request.failed"));
        }
        if (response == null) {
            MessageUtils.getInstance(project).showWarnMsg(PropertiesUtils.getInfo("request.failed"));
            return false;
        }
        String body = response.getKey();
        Header[] responseHeaders = response.getValue();
        LoginResponse loginResponse = JackSonUtil.convertJsonToBean(body, LoginResponse.class);
        if (loginResponse == null) {
            MessageUtils.getInstance(project).showWarnMsg(PropertiesUtils.getInfo("request.failed"));
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Header header : responseHeaders) {
            if ("Set-Cookie".equals(header.getName())) {
                String value = header.getValue();
                String cookie = value.split(";")[0];
                stringBuilder.append(cookie).append(";");
                String[] split = cookie.split("=");
                if ("__csrf".equals(split[0])) {
                    csrf = split[1];
                } else if ("MUSIC_U".equals(split[0])) {
                    musicU = split[1];
                }
            }
        }
        cookie = stringBuilder.toString();
        uid = loginResponse.getProfile().getUserId();
        ViewManager.updateMusicPanel(project, loginResponse.getProfile());
        MessageUtils.getInstance(project).showInfoMsg(PropertiesUtils.getInfo("login.success"));
        return true;
    }

    @NotNull
    private static Map<String, String> getHeaders(String cookie) {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Referer", "https://music.163.com");
        headers.put("Accept", "*/*");
        headers.put("Cache-Control", "no-cache");
        headers.put("Connection", "keep-alive");
        headers.put("Host", "music.163.com");
        headers.put("Accept-Language", "zh-CN,en-US;q=0.7,en;q=0.3");
        headers.put("DNT", "1");
        headers.put("Pragma", "no-cache");
        // 一定要没有Cookie时一定要加
        headers.put("Cookie", cookie);
        return headers;
    }

    public static void logout() {
        cookie = null;
        uid = null;
        csrf = "";
        musicU = "";
    }

    public static boolean isLogin() {
        return StringUtils.isNotBlank(cookie) && Objects.nonNull(uid);
    }
}
