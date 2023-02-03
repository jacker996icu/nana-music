package com.jacker.plugin.music.player;

import com.jacker.plugin.music.player.audio.AudioFileReader;
import com.jacker.plugin.music.player.audio.player.Player;
import com.jacker.plugin.music.player.audio.player.PlayerListener;
import com.jacker.plugin.music.player.model.TrackData;
import com.jacker.plugin.music.player.factory.AuditFileReaderFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 基于 tulskiy.musique 的播放器
 *
 */
public class MusicPlayer {
    /**
     * PlayerHolder
     */
    private static class PlayerHolder {
        private static final Player PLAYER = new Player();
    }

    /**
     * 获取真实的player本体
     *
     * @return player本体
     */
    public Player getCurrentPlayer() {
        return PlayerHolder.PLAYER;
    }

    /**
     * music track
     */
    private TrackData trackData = null;

    private MusicPlayer() {
    }

    private static class MusicPlayerHolder {
        private static final MusicPlayer MUSIC_PLAYER = new MusicPlayer();
    }

    public static MusicPlayer getInstancePlayer() {
        return MusicPlayerHolder.MUSIC_PLAYER;
    }

    /**
     * 是否正在播放
     *
     * @return true/false
     */
    public boolean isPlaying() {
        return getCurrentPlayer().isPlaying();
    }

    /**
     * 添加事件监听器
     *
     * @param listener 监听器
     */
    public void addListener(PlayerListener listener) {
        getCurrentPlayer().addListener(listener);
    }

    /**
     * 加载音乐
     *
     * @param path 音乐文件路径
     * @return 加载成功/失败
     */
    public boolean loadMusicSrc(String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        trackData = null;
        try {
            File songFile = new File(path);
            AudioFileReader audioFileReader = AuditFileReaderFactory.getAudioFileReader(songFile.getName());
            if (audioFileReader == null) {
                return false;
            }
            trackData = audioFileReader.read(songFile);
            return trackData != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 打开
     */
    public void openMusic() {
        if (trackData != null) {
            getCurrentPlayer().open(trackData);
        }
    }

    /**
     * 暂停
     */
    public void playOrPause() {
        getCurrentPlayer().pause();
    }

    /**
     * 停止
     */
    public void stop() {
        getCurrentPlayer().stop();
    }
}
