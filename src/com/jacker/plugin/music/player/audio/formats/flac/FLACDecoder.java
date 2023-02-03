package com.jacker.plugin.music.player.audio.formats.flac;

import com.jacker.plugin.music.player.audio.Decoder;
import com.jacker.plugin.music.player.model.TrackData;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.io.RandomFileInputStream;
import org.kc7bfi.jflac.metadata.Metadata;
import org.kc7bfi.jflac.metadata.SeekTable;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FLACDecoder implements Decoder {
    private RandomAccessFile inputFile;
    private StreamInfo streamInfo;
    private SeekTable seekTable;
    private org.kc7bfi.jflac.FLACDecoder decoder;
    private ByteData byteData = new ByteData(0);
    private int offset = -1;

    public synchronized boolean open(TrackData track) {
        try {
            logger.fine("Opening file: " + track.getFile());
            inputFile = new RandomAccessFile(track.getFile(), "r");
            decoder = new org.kc7bfi.jflac.FLACDecoder(new RandomFileInputStream(inputFile));
            parseMetadata();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public AudioFormat getAudioFormat() {
        return streamInfo.getAudioFormat();
    }

    private void parseMetadata() {
        streamInfo = null;
        try {
            Metadata[] metadata = decoder.readMetadata();
            for (Metadata m : metadata) {
                if (m instanceof StreamInfo)
                    streamInfo = (StreamInfo) m;
                else if (m instanceof SeekTable)
                    seekTable = (SeekTable) m;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekSample(long sample) {
        decoder.flush();
//        if (ogg) {
//            seekOgg(sample);
//        } else {
        seekFlac(sample);
//        }
        decoder.flush();

    }

    public int decode(byte[] buf) {
        try {
            if (offset != -1) {
                int len = byteData.getLen() - offset;
                System.arraycopy(byteData.getData(), offset, buf, 0, len);
                offset = -1;
                return len;
            }
            Frame readFrame = decoder.readNextFrame();
            if (readFrame == null) {
                return -1;
            }
            byteData.setData(buf);
            decoder.decodeFrame(readFrame, byteData);
            return byteData.getLen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void close() {
        try {
            if (inputFile != null)
                inputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void seekFlac(long target_sample) {
        long lower_bound, upper_bound = 0, lower_bound_sample, upper_bound_sample, this_frame_sample;
        long pos;
        int i;
        int approx_bytes_per_frame;
        boolean first_seek = true;
        long total_samples = streamInfo.getTotalSamples();
        int min_blocksize = streamInfo.getMinBlockSize();
        int max_blocksize = streamInfo.getMaxBlockSize();
        int max_framesize = streamInfo.getMaxFrameSize();
        int min_framesize = streamInfo.getMinFrameSize();
        int channels = streamInfo.getChannels();
        int bps = streamInfo.getBitsPerSample();

        /* we are just guessing here */
        if (max_framesize > 0)
            approx_bytes_per_frame = (max_framesize + min_framesize) / 2 + 1;
        else if (min_blocksize == max_blocksize && min_blocksize > 0) {
            approx_bytes_per_frame = min_blocksize * channels * bps / 8 + 64;
        } else
            approx_bytes_per_frame = 4096 * channels * bps / 8 + 64;

        lower_bound = 0;
        lower_bound_sample = 0;
        try {
            upper_bound = inputFile.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        upper_bound_sample = total_samples > 0 ? total_samples : target_sample /*estimate it*/;

        if (seekTable != null) {
            long new_lower_bound = lower_bound;
            long new_upper_bound = upper_bound;
            long new_lower_bound_sample = lower_bound_sample;
            long new_upper_bound_sample = upper_bound_sample;

            /* find the closest seekPosition point <= target_sample, if it exists */
            for (i = seekTable.numberOfPoints() - 1; i >= 0; i--) {
                if (seekTable.getSeekPoint(i).getFrameSamples() > 0 && /* defense against bad seekpoints */
                        (total_samples <= 0 || seekTable.getSeekPoint(i).getSampleNumber() < total_samples) && /* defense against bad seekpoints */
                        seekTable.getSeekPoint(i).getSampleNumber() <= target_sample)
                    break;
            }
            if (i >= 0) { /* i.e. we found a suitable seekPosition point... */
                new_lower_bound = seekTable.getSeekPoint(i).getStreamOffset();
                new_lower_bound_sample = seekTable.getSeekPoint(i).getSampleNumber();
            }

            /* find the closest seekPosition point > target_sample, if it exists */
            for (i = 0; i < seekTable.numberOfPoints(); i++) {
                if (seekTable.getSeekPoint(i).getFrameSamples() > 0 && /* defense against bad seekpoints */
                        (total_samples <= 0 || seekTable.getSeekPoint(i).getSampleNumber() < total_samples) && /* defense against bad seekpoints */
                        seekTable.getSeekPoint(i).getSampleNumber() > target_sample)
                    break;
            }
            if (i < seekTable.numberOfPoints()) { /* i.e. we found a suitable seekPosition point... */
                new_upper_bound = seekTable.getSeekPoint(i).getStreamOffset();
                new_upper_bound_sample = seekTable.getSeekPoint(i).getSampleNumber();
            }
            /* final protection against unsorted seekPosition tables; keep original values if bogus */
            if (new_upper_bound >= new_lower_bound) {
                lower_bound = new_lower_bound;
                upper_bound = new_upper_bound;
                lower_bound_sample = new_lower_bound_sample;
                upper_bound_sample = new_upper_bound_sample;
            }
        }

        if (upper_bound_sample == lower_bound_sample)
            upper_bound_sample++;

        while (true) {
            try {
                /* check if the bounds are still ok */
                if (lower_bound_sample >= upper_bound_sample || lower_bound > upper_bound) {
                    return;
                }

                pos = (long) (lower_bound + ((double) (target_sample - lower_bound_sample) / (double) (upper_bound_sample - lower_bound_sample) * (double) (upper_bound - lower_bound)) - approx_bytes_per_frame);

                if (pos >= upper_bound)
                    pos = upper_bound - 1;
                if (pos < lower_bound)
                    pos = lower_bound;
//                System.out.println("Seek to: " + pos);
                inputFile.seek(pos);
//                decoder.getBitInputStream().skipBitsNoCRC(1);
                decoder.getBitInputStream().reset();

                Frame frame = decoder.readNextFrame();
//                System.out.println("Found: " + frame.header.sampleNumber);
                if (frame.header.sampleNumber <= target_sample &&
                        target_sample <= frame.header.sampleNumber + frame.header.blockSize) {
//                    System.out.println("Done seeking");
                    offset = (int) (target_sample - frame.header.sampleNumber) * frame.header.channels * frame.header.bitsPerSample / 8;
                    byteData = decoder.decodeFrame(frame, byteData);
                    break;
                }
                /* our write callback will change the state when it gets to the target frame */
                /* actually, we could have got_a_frame if our decoder is at FLAC__STREAM_DECODER_END_OF_STREAM so we need to check for that also */

                this_frame_sample = frame.header.sampleNumber;

                if (decoder.getSamplesDecoded() == 0 || (this_frame_sample + frame.header.blockSize >= upper_bound_sample && !first_seek)) {
                    if (pos == lower_bound) {
                        /* can't move back any more than the first frame, something is fatally wrong */
                        System.err.printf("FLAC Decoder: Seek to %d error. %d samples overrun, sorry\n", target_sample, this_frame_sample - target_sample);
                        return;
                    }
                    /* our last move backwards wasn't big enough, try again */
                    approx_bytes_per_frame = approx_bytes_per_frame != 0 ? approx_bytes_per_frame * 2 : 16;
                    continue;
                }
                /* allow one seekPosition over upper bound, so we can get a correct upper_bound_sample for streams with unknown total_samples */
                first_seek = false;

                /* make sure we are not seeking in corrupted stream */
                if (this_frame_sample < lower_bound_sample) {
                    System.err.println("FLAC Decoder: Seek error. This frame sample is lower than lower bound sample");
                    return;
                }

                /* we need to narrow the search */
                if (target_sample < this_frame_sample) {
                    upper_bound_sample = this_frame_sample + frame.header.blockSize;
                    /*@@@@@@ what will decode position be if at end of stream? */
                    upper_bound = inputFile.getFilePointer() - decoder.getBitInputStream().getInputBytesUnconsumed();
                    approx_bytes_per_frame = (int) (2 * (upper_bound - pos) / 3 + 16);
                } else { /* target_sample >= this_frame_sample + this frame's blocksize */
                    lower_bound_sample = this_frame_sample + frame.header.blockSize;
                    lower_bound = inputFile.getFilePointer() - decoder.getBitInputStream().getInputBytesUnconsumed();
                    approx_bytes_per_frame = (int) (2 * (lower_bound - pos) / 3 + 16);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
