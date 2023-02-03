package com.jacker.plugin.music.player.audio.formats.tta;

import com.jacker.plugin.music.player.audio.AudioFileReader;
import com.jacker.plugin.music.player.model.TrackData;
import com.tulskiy.tta.TTA_Decoder;
import com.tulskiy.tta.TTA_info;

import java.io.FileInputStream;
import java.util.logging.Level;

public class TTAFileReader extends AudioFileReader {

    @Override
    protected TrackData readSingle(TrackData trackData) {
        try {
            TTA_info info = new TTA_info();
            new TTA_Decoder(new FileInputStream(trackData.getFile())).read_tta_header(info);
            trackData.setCodec("True Audio");
            trackData.setSampleRate(info.sps);
            trackData.setTotalSamples(info.samples);
            trackData.setBitrate(info.bitrate);
            return trackData;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error reading file " + trackData.getFile(), e);
        }

        return null;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return "tta".equalsIgnoreCase(ext);
    }
}
