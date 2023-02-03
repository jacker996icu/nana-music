package com.jacker.plugin.music.player.audio.formats.ape;

import com.jacker.plugin.music.player.audio.AudioFileReader;
import com.jacker.plugin.music.player.model.TrackData;
import davaguine.jmac.info.APEFileInfo;
import davaguine.jmac.info.APEHeader;
import davaguine.jmac.info.ID3Tag;
import davaguine.jmac.tools.RandomAccessFile;

public class APEFileReader extends AudioFileReader {

    public TrackData readSingle(TrackData track) {
        try {
            ID3Tag.setDefaultEncoding(defaultCharset.name());
            RandomAccessFile ras = new RandomAccessFile(track.getFile(), "r");
            APEHeader header = new APEHeader(ras);
            APEFileInfo fileInfo = new APEFileInfo();
            header.Analyze(fileInfo);
            parseInfo(track, fileInfo);
            ras.close();
            return track;
        } catch (Exception e) {
            System.out.println("Couldn't read file: " + track.getFile());
        }
        return null;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("ape");
    }

    private void parseInfo(TrackData trackData, APEFileInfo fileInfo) {
    	trackData.setSampleRate(fileInfo.nSampleRate);
    	trackData.setTotalSamples(fileInfo.nTotalBlocks);
    	trackData.setStartPosition(0);
    	trackData.setCodec("Monkey's Audio");
    	trackData.setBitrate(fileInfo.nAverageBitrate);
    }

}
