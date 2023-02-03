package com.jacker.plugin.music.player.audio.formats.tta;

import com.jacker.plugin.music.player.audio.Decoder;
import com.jacker.plugin.music.player.model.TrackData;
import com.tulskiy.tta.TTA_Decoder;
import com.tulskiy.tta.TTA_info;

import javax.sound.sampled.AudioFormat;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

public class TTADecoder implements Decoder {
    private TTA_Decoder decoder;
    private AudioFormat fmt;
    private TTA_info info;
    private TrackData track;

    @Override
    public boolean open(TrackData track) {
        this.track = track;
        try {
            decoder = new TTA_Decoder(new FileInputStream(track.getFile()));

            info = decoder.init_get_info(0);
            fmt = new AudioFormat(info.sps, info.bps, info.nch, true, false);

            return true;
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "File not found", e);
        }
        return false;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return fmt;
    }

    @Override
    public void seekSample(long sample) {
        decoder.set_position((int) sample);
    }

    @Override
    public int decode(byte[] buf) {
        try {
            track.setBitrate(decoder.get_current_bitrate());
            return decoder.process_stream(buf);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Decoder Error", e);
            return -1;
        }
    }

    @Override
    public void close() {
        track.setBitrate(info.bitrate);
        if (decoder != null)
            try {
                decoder.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not close decoder", e);
            }
        decoder = null;
        info = null;
    }
}
