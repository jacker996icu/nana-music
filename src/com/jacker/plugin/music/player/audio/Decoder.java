package com.jacker.plugin.music.player.audio;

import com.jacker.plugin.music.player.model.TrackData;

import javax.sound.sampled.AudioFormat;
import java.util.logging.Logger;

public interface Decoder {
    Logger logger = Logger.getLogger(Decoder.class.getName());

    /**
     * Open the file and prepare for decoding.
     * This method sets the decoder to play the file from startIndex
     *
     * @param track The Track to open
     * @return true if file opened successfully
     */
    boolean open(TrackData track);

    /**
     * Get format of the PCM data. Usually it is 44100 kHz, 16 bit, signed,
     * little or big endian
     *
     * @return audio format of PCM data
     */
    AudioFormat getAudioFormat();

    void seekSample(long sample);

    /**
     * Decode chunk of PCM data and write to OutputStream
     *
     * @param buf Buffer for data
     * @return true if success
     */
    int decode(byte[] buf);

    void close();

}
