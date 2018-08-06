/*
 * Created on Aug 31, 2009
 */
package org.digitalmediaserver.cuelib.id3;

import java.util.Properties;


/**
 * The Class ITunesPodcastFrame.
 */
public class ITunesPodcastFrame implements ID3Frame {

	private int totalFrameSize;
	private Properties flags = new Properties();
	private String payload;

	/**
	 * Instantiates a new iTunes podcast frame.
	 */
	public ITunesPodcastFrame() {
	}

	/**
	 * Instantiates a new iTunes podcast frame.
	 *
	 * @param totalFrameSize the total frame size
	 */
	public ITunesPodcastFrame(int totalFrameSize) {
		this.totalFrameSize = totalFrameSize;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
			.append("iTunes podcast frame [").append(totalFrameSize).append("]\n")
			.append("Flags: ").append(flags.toString()).append('\n')
			.append("Payload: ").append(payload);
		return builder.toString();
	}

	@Override
	public CanonicalFrameType getCanonicalFrameType() {
		return CanonicalFrameType.ITUNES_PODCAST;
	}

	@Override
	public Properties getFlags() {
		return this.flags;
	}

	@Override
	public int getTotalFrameSize() {
		return this.totalFrameSize;
	}

	/**
	 * Get the payload of this {@link ITunesPodcastFrame}.
	 *
	 * @return The payload of this {@link ITunesPodcastFrame}.
	 */
	public String getPayload() {
		return payload;
	}

	/**
	 * Set the payload of this {@link ITunesPodcastFrame}.
	 *
	 * @param payload The payload of this {@link ITunesPodcastFrame}.
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

}
