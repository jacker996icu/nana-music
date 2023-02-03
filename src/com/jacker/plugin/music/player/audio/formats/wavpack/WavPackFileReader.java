package com.jacker.plugin.music.player.audio.formats.wavpack;

import com.jacker.plugin.music.player.audio.AudioFileReader;
import com.jacker.plugin.music.player.model.TrackData;
import com.wavpack.decoder.WavPackUtils;
import com.wavpack.decoder.WavpackContext;

import java.io.RandomAccessFile;

public class WavPackFileReader extends AudioFileReader {

    public TrackData readSingle(TrackData trackData) {
        try {
            RandomAccessFile raf = new RandomAccessFile(trackData.getFile(), "r");
            WavpackContext wpc = WavPackUtils.WavpackOpenFileInput(raf);
            trackData.setTotalSamples(WavPackUtils.WavpackGetNumSamples(wpc));
            trackData.setSampleRate((int) WavPackUtils.WavpackGetSampleRate(wpc));
            trackData.setBitrate((int) (raf.length() / trackData.getTotalSamples() / 1000 * 8));
            trackData.setCodec("WavPack");
            raf.close();
        } catch (Exception e) {
            System.out.println("Couldn't read file: " + trackData.getFile());
        }
        return trackData;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("wv");
    }

}
