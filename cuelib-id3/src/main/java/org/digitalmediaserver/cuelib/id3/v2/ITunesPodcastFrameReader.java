/*
 * Created on Aug 31, 2009
 */
package org.digitalmediaserver.cuelib.id3.v2;

import java.io.IOException;
import java.io.InputStream;
import org.digitalmediaserver.cuelib.id3.ITunesPodcastFrame;


/**
 * The Class ITunesPodcastFrameReader.
 */
public class ITunesPodcastFrameReader implements FrameReader {

	private final int headerSize;

	/**
	 * Instantiates a new i tunes podcast frame reader.
	 *
	 * @param headerSize the header size
	 */
	public ITunesPodcastFrameReader(int headerSize) {
		this.headerSize = headerSize;
	}

	@Override
	public ITunesPodcastFrame readFrameBody(
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException, MalformedFrameException {
		ITunesPodcastFrame result = new ITunesPodcastFrame(this.headerSize + size);
		StringBuilder payloadBuilder = new StringBuilder();
		for (int index = 0; index < 4; index++) {
			payloadBuilder.append(Integer.toHexString(input.read()));
		}
		result.setPayload(payloadBuilder.toString());
		return result;
	}

}
