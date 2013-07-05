/*
 * Copyright 2010-2013 Capgemini
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */
package org.xmlfield.core.impl.dom.cleanup;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream which remove invalid numeric entities from an XML input
 * stream. Invalid values are replaced by the 'unknown character'.
 * <p>
 * This input stream does not try to understand encoding and process only ASCII
 * values. As a result, it should work with UTF-8 and ASCII streams bu will not
 * work with UTF-16 streams.
 * 
 * @author Nicolas Richeton
 * 
 */
public class EntitySanitizingInputStream extends InputStream {
	/**
	 * Maximum length for read ahead.
	 */
	static int MAX_ENTITY_SIZE = 15;
	/**
	 * the generic unknown entity buffer.
	 */
	static int[] readHeadUndefined = new int[] { '&', '#', '6', '5', '5', '3',
			'3', ';' };

	/**
	 * Current buffer. Contains null if no character has been read ahead of the
	 * stream.
	 */
	int[] readAheadBuffer = null;
	int readAheadCount = 0;
	int readAheadPosition = 0;

	private InputStream wrappedInputstream;

	/**
	 * Wrap an input stream with XMLentity sanitizing.
	 * 
	 * @param xmlStream
	 *            The original xml input stream
	 */
	public EntitySanitizingInputStream(InputStream xmlStream) {
		this.wrappedInputstream = xmlStream;
	}

	@Override
	public int available() throws IOException {
		if (readAheadCount == 0) {
			return super.available();
		}

		return readAheadCount - readAheadPosition + super.available();
	}

	@Override
	public void close() throws IOException {
		wrappedInputstream.close();
	}

	@Override
	public int read() throws IOException {

		// If the readAheadBuffer exists, return data from the buffer.
		if (readAheadCount > 0) {
			int current = readAheadBuffer[readAheadPosition];
			readAheadPosition++;
			// If we reach the end of the buffer, cleanup and return to the
			// standard behavior.
			if (readAheadPosition == readAheadCount) {
				readAheadCount = 0;
				readAheadPosition = 0;
				readAheadBuffer = null;
			}
			return current;
		}

		int current = wrappedInputstream.read();

		// If we just read the begining of an entity, start reading ahead.
		if (current == '&') {
			readAheadBuffer = new int[MAX_ENTITY_SIZE];
			readAheadBuffer[0] = current;
			current = 0;
			readAheadCount++;

			// Read until buffer is full / no more data available / end of the
			// entity
			while (readAheadCount < MAX_ENTITY_SIZE && current != -1
					&& current != ';') {
				current = wrappedInputstream.read();
				readAheadBuffer[readAheadCount] = current;
				readAheadCount++;

				// Not an entity number ? -> stop reading.
				if (!(readAheadBuffer[1] == '#')) {
					break;
				}

				// Reached the end of the entity
				if (readAheadCount > 3 && current == ';') {
					// Get the entity value
					StringBuilder number = new StringBuilder();
					for (int i = 2; i < readAheadCount - 1; i++) {
						number.append((char) readAheadBuffer[i]);
					}

					try {
						int entity = Integer.parseInt(number.toString());

						// If entity is in invalid range, replace by the
						// 'unknown entity"
						if (!(entity == 9 || entity == 10 || entity == 13
								|| entity >= 32 && entity <= 55295
								|| entity >= 57344 && entity <= 65533
								|| entity == 55296 || entity >= 56320
								&& entity <= 56319 || entity == 57343)) {
							readAheadBuffer = readHeadUndefined;
							readAheadCount = readAheadBuffer.length;

						}

					} catch (NumberFormatException e) {
						// this was not a number, the XML is probably invalid.
						// Nothing to do, just start to output the buffer.
					}
				}

			}

			current = readAheadBuffer[0];
			readAheadPosition++;
		}
		return current;
	}

	@Override
	public synchronized void reset() throws IOException {
		wrappedInputstream.reset();
	}

}
