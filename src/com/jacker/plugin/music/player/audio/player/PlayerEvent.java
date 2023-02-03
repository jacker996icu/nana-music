package com.jacker.plugin.music.player.audio.player;

public class PlayerEvent {
    public enum PlayerEventCode {
        FILE_OPENED, PLAYING_STARTED, PAUSED, STOPPED, SEEK_FINISHED
    }

    private PlayerEventCode eventCode;

    public PlayerEvent(PlayerEventCode eventCode) {
        this.eventCode = eventCode;
    }

    public PlayerEventCode getEventCode() {
        return eventCode;
    }
}
