package com.jacker.plugin.music.player.audio.formats.ogg;

import com.jacker.plugin.music.player.audio.Decoder;
import com.jacker.plugin.music.player.model.TrackData;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.VorbisFile;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

public class VorbisDecoder implements Decoder {
    private VorbisFile vorbisFile;
    private AudioFormat audioFormat;
    private boolean streaming = false;
    private TrackData track;
    private int oldBitrate;

    public boolean open(TrackData trackData) {
        try {
            this.track = trackData;
            logger.fine("Opening file: " + trackData.getFile());
            vorbisFile = new VorbisFile(trackData.getFile().getAbsolutePath());
            streaming = false;
            oldBitrate = trackData.getBitrate();
            Info info = vorbisFile.getInfo()[0];
            trackData.setSampleRate(info.rate);
            audioFormat = new AudioFormat(info.rate, 16, info.channels, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void seekSample(long sample) {
        vorbisFile.pcm_seek(sample);
    }

    public int decode(byte[] buf) {
        int ret = vorbisFile.read(buf, buf.length);
        track.setBitrate(vorbisFile.bitrate_instant() / 1000);
        if (ret <= 0) {
            //it's a stream, open it again
            if (streaming) {
                if (!open(track))
                    return -1;
                else
                    return 0;
            }
            return -1;
        }
        return ret;
    }

    public void close() {
        try {
            if (vorbisFile != null) {
                vorbisFile.close();
                track.setBitrate(oldBitrate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
