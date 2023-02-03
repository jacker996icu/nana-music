package com.jacker.plugin.music.statusbar;

import com.intellij.util.messages.Topic;

public interface MusicStatusChangeListener {
    Topic<MusicStatusChangeListener> MUSIC_STATUS_CHANGE_LISTENER_TOPIC = Topic.create("MusicStatusListener", MusicStatusChangeListener.class);

    void updateStatus(String title, String time);
}
