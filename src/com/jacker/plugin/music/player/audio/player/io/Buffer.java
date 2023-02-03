package com.jacker.plugin.music.player.audio.player.io;

import com.jacker.plugin.music.player.model.TrackData;

import javax.sound.sampled.AudioFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Buffer {
    private final RingBuffer buffer;
    private final BlockingQueue<NextEntry> trackQueue = new LinkedBlockingDeque<>();
    private final Queue<Integer> when = new LinkedList<>();
    private int bytesLeft = 0;

    public Buffer(int size) {
        buffer = new RingBuffer(size);
    }

    public void write(byte[] b, int off, int len) {
        buffer.put(b, off, len);
    }

    public void addNextTrack(TrackData track, AudioFormat format, long startSample, boolean forced) {
        int bytesLeft = available();
        for (Integer left : when) {
            bytesLeft -= left;
        }
        if (trackQueue.isEmpty())
            this.bytesLeft = bytesLeft;
        else
            when.add(bytesLeft);
        trackQueue.add(new NextEntry(track, format, startSample, forced));
    }

    public NextEntry pollNextTrack() {
        NextEntry nextEntry = null;
        try {
            nextEntry = trackQueue.take();
            buffer.setEOF(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!when.isEmpty()) {
            bytesLeft = when.poll();
        } else {
            bytesLeft = -1;
        }
        return nextEntry;
    }

    public int read(byte[] b, int off, int len) {
        if (bytesLeft > 0) {
            if (bytesLeft < len) {
                len = bytesLeft;
            }
            bytesLeft -= len;
        } else if (bytesLeft == 0) {
            return -1;
        }
        return buffer.get(b, off, len);
    }

    public synchronized int available() {
        return buffer.getAvailable();
    }

    public int size() {
        return buffer.size();
    }

    public void flush() {
        buffer.empty();
    }

    public static class NextEntry {
        public TrackData track;
        public AudioFormat format;
        public long startSample;
        public boolean forced;

        NextEntry(TrackData track, AudioFormat format, long startSample, boolean forced) {
            this.track = track;
            this.format = format;
            this.startSample = startSample;
            this.forced = forced;
        }
    }
}
