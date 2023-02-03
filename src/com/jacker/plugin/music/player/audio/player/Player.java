package com.jacker.plugin.music.player.audio.player;

import com.jacker.plugin.music.player.audio.player.io.Buffer;
import com.jacker.plugin.music.player.model.TrackData;

import java.util.ArrayList;
import java.util.logging.Logger;

import static com.jacker.plugin.music.player.audio.player.Actor.Message;

public class Player {
    public final Logger logger = Logger.getLogger(getClass().getName());
    private static final int BUFFER_SIZE = (int) Math.pow(2, 18);

    private final PlayingThread playingThread;
    private final BufferingThread bufferingThread;
    private final ArrayList<PlayerListener> listeners = new ArrayList<>();

    public Player() {
        Buffer buffer = new Buffer(BUFFER_SIZE);
        playingThread = new PlayingThread(this, buffer);
        Thread t1 = new Thread(playingThread, "Playing Thread");
        t1.setPriority(Thread.MAX_PRIORITY);
        t1.start();
        bufferingThread = new BufferingThread(buffer, playingThread);
        new Thread(bufferingThread, "Buffer Thread").start();
    }

    public void open(TrackData track) {
        bufferingThread.send(Message.OPEN, track);
    }

    public void pause() {
        playingThread.send(Message.PAUSE);
    }

    public void stop() {
        bufferingThread.send(Message.STOP);
    }

    public void seek(long sample) {
        bufferingThread.send(Message.SEEK, sample);
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }

    public long getCurrentSample() {
        return playingThread.getCurrentSample();
    }

    public TrackData getTrack() {
        return playingThread.getCurrentTrack();
    }

    public boolean isPlaying() {
        return playingThread.isActive() && getTrack() != null;
    }

    synchronized void fireEvent(PlayerEvent.PlayerEventCode event) {
        logger.fine("Player Event: " + event);
        PlayerEvent e = new PlayerEvent(event);
        for (PlayerListener listener : listeners) {
            listener.onEvent(e);
        }
    }
}
