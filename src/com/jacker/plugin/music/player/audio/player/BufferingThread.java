package com.jacker.plugin.music.player.audio.player;

import com.jacker.plugin.music.player.audio.Decoder;
import com.jacker.plugin.music.player.audio.player.io.Buffer;
import com.jacker.plugin.music.player.model.TrackData;
import com.jacker.plugin.music.player.factory.DecoderFactory;


import java.util.logging.Logger;

public class BufferingThread extends Actor implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Object lock = new Object();
    private TrackData currentTrack;
    private TrackData nextTrack;
    private Decoder decoder;
    private boolean active;

    private final Buffer buffer;
    private final PlayingThread playingThread;
    private boolean stopAfterCurrent = false;

    public BufferingThread(Buffer buffer, PlayingThread playingThread) {
        this.buffer = buffer;
        this.playingThread = playingThread;
    }

    @Override
    public void process(Message message) {
        Object[] params = message.getParams();
        switch (message) {
            case OPEN:
                if (params.length > 0 && params[0] instanceof TrackData) {
                    TrackData track = (TrackData) params[0];
                    pause(true);
                    open(track, true);
                }
                break;
            case SEEK:
                if (params.length > 0 && params[0] instanceof Long) {
                    Long sample = (Long) params[0];
                    seek(sample);
                }
                break;
            case STOP:
                stop(true);
                break;
        }
    }

    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        byte[] buf = new byte[65536];
        int len;
        while (true) {
            synchronized (lock) {
                try {
                    while (!active) {
                        lock.wait();
                    }
                    if (decoder == null) {
                        stop(false);
                        continue;
                    }

                    while (active) {
                        if (nextTrack != null) {
                            if (stopAfterCurrent) {
                                stop(false);
                                stopAfterCurrent = false;
                                continue;
                            }
                            open(nextTrack, false);
                            nextTrack = null;
                            continue;
                        }

                        len = decoder.decode(buf);

                        if (len == -1) {
                            nextTrack = null;
                            stop(false);
                            continue;
                        }

                        buffer.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop(boolean flush) {
        logger.fine("Stop buffering");
        nextTrack = null;
        pause(flush);
        buffer.addNextTrack(null, null, -1, false);
        if (decoder != null) {
            decoder.close();
        }
        decoder = null;
    }

    private void pause(boolean flush) {
        active = false;
        if (flush)
            buffer.flush();
    }

    private void start() {
        active = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public synchronized void open(TrackData trackData, boolean forced) {
        if (decoder != null) {
            decoder.close();
        }

        if (trackData != null) {
            logger.fine("Opening track " + trackData.getLocation());

            if (trackData.isFile() && !trackData.getFile().exists()) {
                stop(false);
                return;
            }
            decoder = DecoderFactory.getDecoder(trackData);
            currentTrack = trackData;

            if (decoder == null || !decoder.open(trackData)) {
                currentTrack = null;
                stop(false);
                return;
            }

            buffer.addNextTrack(currentTrack, decoder.getAudioFormat(), -1, forced);

            if (trackData.getStartPosition() > 0)
                decoder.seekSample(trackData.getStartPosition());

            start();
            logger.fine("Finished opening track");
            if (forced)
                playingThread.send(Message.FLUSH);
            playingThread.send(Message.PLAY);
        }
    }

    public void seek(long sample) {
        boolean oldState = active;
        pause(true);

        if (decoder != null) {
            decoder.seekSample(currentTrack.getStartPosition() + sample);
            buffer.addNextTrack(currentTrack, decoder.getAudioFormat(), sample, true);
            if (oldState) {
                start();
            }
        }
    }

}
