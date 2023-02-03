package com.jacker.plugin.music.player.audio.formats.mp3;

import com.jacker.plugin.music.player.audio.AudioFileReader;
import com.jacker.plugin.music.player.model.TrackData;
import davaguine.jmac.info.ID3Tag;
import org.jaudiotagger.audio.mp3.LameFrame;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.audio.mp3.XingFrame;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

public class MP3FileReader extends AudioFileReader {
    private static final int GAPLESS_DELAY = 529;

    public TrackData readSingle(TrackData trackData) {
        TextEncoding.getInstanceOf().setDefaultNonUnicode(defaultCharset.name());
        ID3Tag.setDefaultEncoding(defaultCharset.name());
        MP3File mp3File = null;
        try {
            mp3File = new MP3File(trackData.getFile(), MP3File.LOAD_ALL, true);
        } catch (Exception ignored) {
            System.out.println("Couldn't read file: " + trackData.getFile());
        }

        if (mp3File != null) {
            MP3AudioHeader mp3AudioHeader = mp3File.getMP3AudioHeader();
            copyHeaderFields(mp3AudioHeader, trackData);

            long totalSamples = trackData.getTotalSamples();
            int enc_delay = GAPLESS_DELAY;

            XingFrame xingFrame = mp3AudioHeader.getXingFrame();
            if (xingFrame != null) {
                LameFrame lameFrame = xingFrame.getLameFrame();
                if (lameFrame != null) {
                    long length = totalSamples;
                    enc_delay += lameFrame.getEncDelay();
                    int enc_padding = lameFrame.getEncPadding() - GAPLESS_DELAY;
                    if (enc_padding < length)
                        length -= enc_padding;

                    if (totalSamples > length)
                        totalSamples = length;
                } else {
                    totalSamples += GAPLESS_DELAY;
                }
            }

            totalSamples -= enc_delay;
            trackData.setTotalSamples(totalSamples);
        }

        return trackData;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("mp3");
    }

}
