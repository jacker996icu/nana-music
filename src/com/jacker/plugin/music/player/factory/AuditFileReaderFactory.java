package com.jacker.plugin.music.player.factory;

import com.jacker.plugin.music.player.audio.AudioFileReader;
import com.jacker.plugin.music.player.audio.formats.ape.APEFileReader;
import com.jacker.plugin.music.player.audio.formats.flac.FLACFileReader;
import com.jacker.plugin.music.player.audio.formats.mp3.MP3FileReader;
import com.jacker.plugin.music.player.audio.formats.mp4.MP4FileReader;
import com.jacker.plugin.music.player.audio.formats.ogg.OGGFileReader;
import com.jacker.plugin.music.player.audio.formats.tta.TTAFileReader;
import com.jacker.plugin.music.player.audio.formats.uncompressed.PCMFileReader;
import com.jacker.plugin.music.player.audio.formats.wavpack.WavPackFileReader;
import com.jacker.plugin.music.util.FileUtil;

import java.util.ArrayList;

public class AuditFileReaderFactory {
    private static final ArrayList<AudioFileReader> readers;

    static {
        readers = new ArrayList<>();
        readers.add(new MP3FileReader());
        readers.add(new APEFileReader());
        readers.add(new FLACFileReader());
        readers.add(new OGGFileReader());
        readers.add(new PCMFileReader());
        readers.add(new WavPackFileReader());
        readers.add(new MP4FileReader());
        readers.add(new TTAFileReader());
    }

    public static AudioFileReader getAudioFileReader(String fileName) {
        String ext = FileUtil.getFileExt(fileName);
        for (AudioFileReader reader : readers) {
            if (reader.isFileSupported(ext))
                return reader;
        }
        return null;
    }

}
