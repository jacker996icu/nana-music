package com.jacker.plugin.music.player.audio.formats.mp4;

import com.jacker.plugin.music.player.audio.Decoder;
import com.jacker.plugin.music.player.model.TrackData;

import javax.sound.sampled.AudioFormat;

public class MP4Demuxer implements Decoder {
    private final Decoder alacDecoder = new ALACDecoder();
    private final Decoder aacDecoder = new MP4Decoder();

    private Decoder decoder;

    @Override
    public boolean open(TrackData track) {
        String codec = track.getCodec();
        if ("AAC".equals(codec)) {
            decoder = aacDecoder;
        } else if ("Apple Lossless".equals(codec)) {
            decoder = alacDecoder;
        } else {
            return false;
        }
        return decoder.open(track);
    }

    @Override
    public AudioFormat getAudioFormat() {
        return decoder.getAudioFormat();
    }

    @Override
    public void seekSample(long sample) {
        decoder.seekSample(sample);
    }

    @Override
    public int decode(byte[] buf) {
        return decoder.decode(buf);
    }

    @Override
    public void close() {
        decoder.close();
    }
}
