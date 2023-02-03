package com.jacker.plugin.music.player.audio.player;

import com.jacker.plugin.music.player.audio.player.io.AudioOutput;
import com.jacker.plugin.music.player.audio.player.io.Buffer;
import com.jacker.plugin.music.player.model.TrackData;
import com.jacker.plugin.music.player.util.AudioMath;

import javax.sound.sampled.AudioFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jacker.plugin.music.player.audio.player.PlayerEvent.PlayerEventCode;

public class PlayingThread extends Actor implements Runnable {
    public final Logger logger = Logger.getLogger(getClass().getName());
    private static final int BUFFER_SIZE = AudioOutput.BUFFER_SIZE;

    private AudioFormat format;
    private final Player player;
    private final Buffer buffer;
    private final Object lock = new Object();
    private final AudioOutput output = new AudioOutput();
    private TrackData currentTrack;
    private long currentByte;
    private boolean active = false;

    public PlayingThread(Player player, Buffer buffer) {
        this.player = player;
        this.buffer = buffer;
    }

    @Override
    public void process(Message message) {
        switch (message) {
            case PAUSE:
                setState(!active);
                break;
            case PLAY:
                setState(true);
                break;
            case STOP:
                stop();
                break;
            case FLUSH:
                output.flush();
                break;
        }
    }

    private void stop() {
        output.flush();
        setState(false);
        output.close();
        player.fireEvent(PlayerEventCode.STOPPED);
    }

    private void setState(boolean newState) {
        if (active != newState) {
            active = newState;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        byte[] buf = new byte[BUFFER_SIZE];
        while (true) {

                try {
                    synchronized (lock) {
                        while (!active) {
                            if (output.isOpen()){
                                player.fireEvent(PlayerEventCode.PAUSED);
                            }
                            output.stop();
                            System.gc();
                            lock.wait();
                        }
                    }
                    output.start();
                    player.fireEvent(PlayerEventCode.PLAYING_STARTED);
                    out : while (active) {
                        int len = buffer.read(buf, 0, BUFFER_SIZE);
                        while (len == -1) {
                            if (!openNext()) {
                                stop();
                                break out;
                            }
                            len = buffer.read(buf, 0, BUFFER_SIZE);
                        }
                        currentByte += len;
                        output.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Exception while playing. Stopping now", e);
                    currentTrack = null;
                    stop();
                }
        }
    }

    private boolean openNext() {
        try {
            logger.fine("Getting next track");
            Buffer.NextEntry nextEntry = buffer.pollNextTrack();
            if (nextEntry.track == null) {
                return false;
            }
            currentTrack = nextEntry.track;
            if (nextEntry.forced) {
                output.flush();
            }
            format = nextEntry.format;
            output.init(format);
            if (nextEntry.startSample >= 0) {
                currentByte = AudioMath.samplesToBytes(nextEntry.startSample, format.getFrameSize());
                player.fireEvent(PlayerEventCode.SEEK_FINISHED);
            } else {
                currentByte = 0;
                player.fireEvent(PlayerEventCode.FILE_OPENED);
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not open next track", e);
            return false;
        }
    }

    public TrackData getCurrentTrack() {
        return currentTrack;
    }

    public boolean isActive() {
        return active;
    }

    public long getCurrentSample() {
        if (format != null) {
            return AudioMath.bytesToSamples(currentByte, format.getFrameSize());
        } else return 0;
    }

}
