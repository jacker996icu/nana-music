package com.jacker.plugin.music.player.factory;

import com.jacker.plugin.music.player.audio.Decoder;
import com.jacker.plugin.music.player.audio.formats.ape.APEDecoder;
import com.jacker.plugin.music.player.audio.formats.flac.FLACDecoder;
import com.jacker.plugin.music.player.audio.formats.mp3.MP3Decoder;
import com.jacker.plugin.music.player.audio.formats.mp4.AACDecoder;
import com.jacker.plugin.music.player.audio.formats.mp4.MP4Demuxer;
import com.jacker.plugin.music.player.audio.formats.ogg.VorbisDecoder;
import com.jacker.plugin.music.player.audio.formats.tta.TTADecoder;
import com.jacker.plugin.music.player.audio.formats.uncompressed.PCMDecoder;
import com.jacker.plugin.music.player.audio.formats.wavpack.WavPackDecoder;
import com.jacker.plugin.music.player.model.TrackData;
import com.jacker.plugin.music.util.FileUtil;

import java.net.URI;
import java.util.HashMap;

public class DecoderFactory {
    private static final HashMap<String, Decoder> decoders = new HashMap<>();

    static {
        decoders.put("mp3", new MP3Decoder());
        decoders.put("ogg", new VorbisDecoder());
        PCMDecoder pcmDecoder = new PCMDecoder();
        decoders.put("wav", pcmDecoder);
        decoders.put("au", pcmDecoder);
        decoders.put("aiff", pcmDecoder);
        decoders.put("flac", new FLACDecoder());
        decoders.put("ape", new APEDecoder());
        decoders.put("wv", new WavPackDecoder());
        MP4Demuxer mp4Demuxer = new MP4Demuxer();
        decoders.put("mp4", mp4Demuxer);
        decoders.put("m4a", mp4Demuxer);
        decoders.put("tta", new TTADecoder());
        decoders.put("aac", new AACDecoder());

    }

    public static Decoder getDecoder(TrackData track) {
        URI location = track.getLocation();
        if (location == null) {
            return null;
        }
        String ext = FileUtil.getFileExt(location.toString()).toLowerCase();
        return decoders.get(ext);
    }

}
