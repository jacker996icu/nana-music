package com.jacker.plugin.music.player.audio.formats.mp3;

import java.util.TreeSet;

class SeekTable {
    private final TreeSet<SeekPoint> points = new TreeSet<>();

    SeekTable() {
        points.add(new SeekPoint(0, 0));
    }

    public void add(int frame, long offset) {
        points.add(new SeekPoint(frame, offset));
    }

    public SeekPoint get(int frame) {
        if (frame < 0)
            frame = 0;
        return points.floor(new SeekPoint(frame, 0));
    }

    public static class SeekPoint implements Comparable<SeekPoint> {
        public int frame;
        public long offset;

        SeekPoint(int frame, long offset) {
            this.frame = frame;
            this.offset = offset;
        }

        @Override
        public int compareTo(SeekPoint seekPoint) {
            return Integer.compare(frame, seekPoint.frame);
        }
    }
}
