package com.jacker.plugin.music.player.util;

import javax.sound.sampled.AudioFormat;

public class AudioMath {
    public static long bytesToSamples(long bytes, int frameSize) {
        return bytes / frameSize;
    }

    public static long samplesToBytes(long samples, int frameSize) {
        return samples * frameSize;
    }

    public static double samplesToMillis(long samples, int sampleRate) {
        return (double) samples / sampleRate * 1000;
    }

    public static double bytesToMillis(long bytes, AudioFormat fmt) {
        long l = AudioMath.bytesToSamples(bytes, fmt.getFrameSize());
        return samplesToMillis(l, (int) fmt.getSampleRate());
    }
}
