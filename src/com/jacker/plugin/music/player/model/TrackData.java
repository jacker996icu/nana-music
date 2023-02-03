package com.jacker.plugin.music.player.model;

import com.jacker.plugin.music.util.TimeUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class TrackData {

    private int sampleRate;
    private int bitrate;
    private int subsongIndex;
    private long startPosition;
    private long totalSamples;
    private String locationString;
    private String codec;
    private String length;

    public TrackData() {
    }

    public TrackData(URI location, int subsongIndex) {
        locationString = location.toString();
        setSubsongIndex(subsongIndex);
    }

    public String getLength() {
        if (length == null)
            length = TimeUtil.samplesToTime(totalSamples, sampleRate);
        return length;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }


    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public void setSubsongIndex(int subsongIndex) {
        this.subsongIndex = subsongIndex;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(long totalSamples) {
        this.totalSamples = totalSamples;
        length = null;
    }

	public URI getLocation() {
		if (locationString != null) {
			try {
				return new URI(locationString);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

    public void setLocation(String location) {
        locationString = location;
    }

    public File getFile() {
        return new File(getLocation());
    }

    public boolean isFile() {
        return getLocation() != null;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec.intern();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackData trackData = (TrackData) o;

        return locationString.equals(trackData.locationString)
                && subsongIndex == trackData.subsongIndex;
    }

    @Override
    public int hashCode() {
        int result = subsongIndex;
        result = 31 * result + (locationString != null ? locationString.hashCode() : 0);
        return result;
    }

}
