package com.jacker.plugin.music.player.audio.formats.ape;

import com.jacker.plugin.music.player.audio.Decoder;
import com.jacker.plugin.music.player.model.TrackData;
import davaguine.jmac.decoder.IAPEDecompress;
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.JMACException;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

public class APEDecoder implements Decoder {
    static {
        System.setProperty("jmac.NATIVE", "true");
    }

    private IAPEDecompress decoder;
    private static final int BLOCKS_PER_DECODE = 4096 * 2;
    private int blockAlign;
    private TrackData track;

    public boolean open(TrackData track) {
        this.track = track;
        try {
            logger.fine("Opening file: " + track.getFile());
            File apeInputFile = File.createFile(track.getFile().getAbsolutePath(), "r");
            decoder = IAPEDecompress.CreateIAPEDecompress(apeInputFile);
            blockAlign = decoder.getApeInfoBlockAlign();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        return false;
    }

    public AudioFormat getAudioFormat() {
        return new AudioFormat(decoder.getApeInfoSampleRate(),
                decoder.getApeInfoBitsPerSample(),
                decoder.getApeInfoChannels(),
                true,
                false);
    }

    public void seekSample(long sample) {
        try {
            if (decoder.getApeInfoDecompressCurrentBlock() != sample) {
                decoder.Seek((int) sample);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int decode(byte[] buf) {
        try {
            int blocksDecoded = decoder.GetData(buf, BLOCKS_PER_DECODE);
            track.setBitrate(decoder.getApeInfoDecompressCurrentBitRate());
            if (blocksDecoded <= 0)
                return -1;
            return blocksDecoded * blockAlign;
        } catch (IOException | JMACException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void close() {
        try {
            if (decoder != null) {
                track.setBitrate(decoder.getApeInfoAverageBitrate());
                decoder.getApeInfoIoSource().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
