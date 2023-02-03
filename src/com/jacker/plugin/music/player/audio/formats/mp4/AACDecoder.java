package com.jacker.plugin.music.player.audio.formats.mp4;

import com.jacker.plugin.music.player.model.TrackData;
import com.jacker.plugin.music.player.util.AudioMath;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class AACDecoder implements com.jacker.plugin.music.player.audio.Decoder {
    private static final int BUFFER_SIZE = 3000;

    private AudioFormat audioFormat;
    private Decoder decoder;
    private ADTSDemultiplexer adts;
    private SampleBuffer buffer;
    private InputStream in;
    private TrackData track;

    @Override
    public boolean open(TrackData trackData) {
        this.track = trackData;
        try {
            in = new BufferedInputStream(new FileInputStream(trackData.getFile()), BUFFER_SIZE);
            adts = new ADTSDemultiplexer(in);
            audioFormat = new AudioFormat(adts.getSampleFrequency(), 16, adts.getChannelCount(), true, true);
            decoder = new Decoder(adts.getDecoderSpecificInfo());
            buffer = new SampleBuffer();
            trackData.setSampleRate(adts.getSampleFrequency());
            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not open AAC stream", e);
        }
        return false;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    @Override
    public void seekSample(long sample) {
    }

    @Override
    public int decode(byte[] buf) {
        try {
            byte[] data = adts.readNextFrame();
            decoder.decodeFrame(data, buffer);
            int length = buffer.getData().length;
            System.arraycopy(buffer.getData(), 0, buf, 0, length);
            track.setBitrate((int) (data.length * 8 / AudioMath.bytesToMillis(length, audioFormat)));
            return length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void close() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not close AAC stream", e);
            }
        }

        decoder = null;
        adts = null;
        buffer = null;
    }
}
