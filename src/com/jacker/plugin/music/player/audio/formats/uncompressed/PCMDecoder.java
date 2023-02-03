package com.jacker.plugin.music.player.audio.formats.uncompressed;

import com.jacker.plugin.music.player.audio.Decoder;
import com.jacker.plugin.music.player.model.TrackData;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class PCMDecoder implements Decoder {
    private AudioInputStream audioInputStream;
    private TrackData inputFile;

    public boolean open(TrackData track) {
        try {
            logger.fine("Opening file: " + track.getFile());
            this.inputFile = track;
            audioInputStream = AudioSystem.getAudioInputStream(track.getFile());
            audioInputStream = AudioSystem.getAudioInputStream(new AudioFormat(audioInputStream.getFormat().getSampleRate(), audioInputStream.getFormat().getSampleSizeInBits(), audioInputStream.getFormat().getChannels(), true, false), audioInputStream);
            return true;
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public AudioFormat getAudioFormat() {
        return audioInputStream.getFormat();
    }

    public void seekSample(long sample) {
        open(inputFile);
        try {
            long toSkip = sample * audioInputStream.getFormat().getFrameSize();
            long skipped = 0;
            while (skipped < toSkip) {
                long b = audioInputStream.skip(toSkip - skipped);
                if (b == 0) break;
                skipped += b;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int decode(byte[] buf) {
        try {
            return audioInputStream.read(buf, 0, buf.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void close() {
        try {
            if (audioInputStream != null)
                audioInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
