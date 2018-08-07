package org.digitalmediaserver.cuelib.id3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.digitalmediaserver.cuelib.util.Utils;


/**
 * An abstract ID3v2 reader.
 *
 * @author Nadahar
 */
public abstract class AbstractID3v2Reader implements ID3Reader {

	@Override
	public boolean hasTag(File file) throws IOException {
		try (FileInputStream input = new FileInputStream(file)) {
			if (input.read() == 'I' && input.read() == 'D' && input.read() == '3') {
				int majorVersion = input.read();
				int revision = input.read();
				if (!isVersionValid(majorVersion, revision)) {
					return false;
				}
				// Skip flags
				Utils.skipOrThrow(input, 1);

				// Check for valid length bytes
				for (int index = 0; index < 4; index++) {
					int sizeByte = input.read();
					if (sizeByte >= 128) {  // Top bit cannot be used.
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * Evaluates if the version is valid for this ID3v2 tag.
	 *
	 * @param majorVersion the major version value.
	 * @param revision the revision value.
	 * @return {@code true} if the version is valid for this ID3v2 tag,
	 *         {@code false} otherwise.
	 */
	protected abstract boolean isVersionValid(int majorVersion, int revision);
}
