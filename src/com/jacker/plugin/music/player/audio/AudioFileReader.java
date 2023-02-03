package com.jacker.plugin.music.player.audio;

import com.jacker.plugin.music.player.model.TrackData;
import org.jaudiotagger.audio.generic.GenericAudioHeader;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AudioFileReader {
    protected static Charset defaultCharset = Charset.forName("iso8859-1");
    protected final Logger logger = Logger.getLogger(getClass().getName());

    public void read(File file, List<TrackData> list) {
        logger.log(Level.FINEST, "Reading file : {0}", file);
        TrackData track = read(file);
        list.add(track);
    }

    protected abstract TrackData readSingle(TrackData track);

    public TrackData read(File file) {
        TrackData track = new TrackData();
        track.setLocation(file.toURI().toString());
        return readSingle(track);
    }

    public abstract boolean isFileSupported(String ext);

    protected void copyHeaderFields(GenericAudioHeader header, TrackData trackData) {
        if (header != null && trackData != null) {
        	trackData.setTotalSamples(header.getTotalSamples());
        	trackData.setSampleRate(header.getSampleRateAsNumber());
        	trackData.setStartPosition(0);
        	trackData.setCodec(header.getFormat());
            trackData.setBitrate((int) header.getBitRateAsNumber());
        }
    }
}
