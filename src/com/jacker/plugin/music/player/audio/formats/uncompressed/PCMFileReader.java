package com.jacker.plugin.music.player.audio.formats.uncompressed;

import com.jacker.plugin.music.player.audio.AudioFileReader;
import com.jacker.plugin.music.player.model.TrackData;
import com.jacker.plugin.music.util.FileUtil;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class PCMFileReader extends AudioFileReader {

    public TrackData readSingle(TrackData trackData) {
        File file = trackData.getFile();

        try {
            AudioFileFormat format = AudioSystem.getAudioFileFormat(file);
            trackData.setStartPosition(0);
            AudioFormat audioFormat = format.getFormat();
            trackData.setSampleRate((int) audioFormat.getSampleRate());
            trackData.setTotalSamples(format.getFrameLength());
            trackData.setCodec(FileUtil.getFileExt(file).toUpperCase());
            if (format.getFrameLength() > 0)
            	trackData.setBitrate((int) (format.getByteLength() / format.getFrameLength() * audioFormat.getSampleRate() / 100));
        } catch (Exception e) {
            System.out.println("Couldn't read file: " + trackData.getFile());
        }
        return trackData;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("wav") || ext.equalsIgnoreCase("au")
               || ext.equalsIgnoreCase("aiff");
    }

}
