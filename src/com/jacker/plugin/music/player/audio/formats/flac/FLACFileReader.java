package com.jacker.plugin.music.player.audio.formats.flac;

import com.jacker.plugin.music.player.audio.AudioFileReader;
import com.jacker.plugin.music.player.model.TrackData;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.generic.GenericAudioHeader;

public class FLACFileReader extends AudioFileReader {
    public TrackData readSingle(TrackData trackData) {
        try {
            FlacFileReader reader = new FlacFileReader();
            AudioFile af1 = reader.read(trackData.getFile());
            GenericAudioHeader audioHeader = (GenericAudioHeader) af1.getAudioHeader();
            copyHeaderFields(audioHeader, trackData);
        } catch (Exception e) {
            System.out.println("Couldn't read file: " + trackData.getFile());
        }
        return trackData;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("flac");
    }

}
