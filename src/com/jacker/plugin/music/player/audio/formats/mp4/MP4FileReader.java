package com.jacker.plugin.music.player.audio.formats.mp4;

import com.jacker.plugin.music.player.audio.AudioFileReader;
import com.jacker.plugin.music.player.model.TrackData;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.audio.mp4.Mp4FileReader;

public class MP4FileReader extends AudioFileReader {
    @Override
    public TrackData readSingle(TrackData track) {
        Mp4FileReader reader = new Mp4FileReader();
        try {
            org.jaudiotagger.audio.AudioFile audioFile = reader.read(track.getFile());
            copyHeaderFields((GenericAudioHeader) audioFile.getAudioHeader(), track);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Couldn't read file: " + track.getFile());
        }

        return track;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return (ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("m4a"));
    }

}
