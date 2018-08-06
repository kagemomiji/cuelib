/*
 * Cuelib library for manipulating cue sheets.
 * Copyright (C) 2007-2009 Jan-Willem van den Broek
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.digitalmediaserver.cuelib.id3.v2.r00;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.digitalmediaserver.cuelib.id3.AbstractID3v2Reader;
import org.digitalmediaserver.cuelib.id3.ID3Tag;
import org.digitalmediaserver.cuelib.id3.ID3Version;
import org.digitalmediaserver.cuelib.id3.v2.MalformedFrameException;
import org.digitalmediaserver.cuelib.id3.v2.UnsupportedEncodingException;
import org.digitalmediaserver.cuelib.id3.v2.UnsynchedInputStream;


/**
 * The Class ID3v2r00Reader.
 */
public class ID3v2r00Reader extends AbstractID3v2Reader {

	@Override
	protected boolean isVersionValid(int majorVersion, int revision) {
		return majorVersion == 2 && revision == 0;
	}

	@Override
	public ID3Tag read(File file) throws IOException, UnsupportedEncodingException, MalformedFrameException {
		ID3Tag tag = new ID3Tag();

		try (FileInputStream input = new FileInputStream(file)) {
			if (input.read() == 'I' && input.read() == 'D' && input.read() == '3') {
				int majorVersion = input.read();
				int revision = input.read();
				if (majorVersion == 2 && revision == 0) {
					tag.setVersion(ID3Version.ID3v2r2);
					tag.setRevision(0);
					int flags = input.read();
					boolean unsyncUsed = (flags & 128) == 128;
					tag.getFlags().setProperty(ID3Tag.UNSYNC_USED, Boolean.toString(unsyncUsed));
					boolean compressionUsed = (flags & 64) == 64;
					tag.getFlags().setProperty(ID3Tag.COMPRESSION_USED, Boolean.toString(compressionUsed));
					int size = 0;
					for (int index = 0; index < 4; index++) {
						int sizeByte = input.read();
						if (sizeByte >= 128) {
							size = -1;
							break;
						}
						size = size * 128 + sizeByte;
					}
					if (size >= 0) {
						tag.setDeclaredSize(size);

						// Now to read the frames.
						InputStream frameInputStream;
						if (unsyncUsed) {
							frameInputStream = new UnsynchedInputStream(input);
						} else {
							frameInputStream = input;
						}
						FramesReader frameReader = new FramesReader();
						frameReader.readFrames(tag, frameInputStream, size);
					} else {
						// TODO Emit warning.
						// Invalid size byte encountered. Not a valid ID3 tag.
						tag = null;
					}
				} else {
					// TODO Emit warning.
					// Version and revision combination not supported.
					tag = null;
				}
			} else {
				// TODO Emit warning?
				// No valid tag found.
				tag = null;
			}
		}

		return tag;
	}
}
