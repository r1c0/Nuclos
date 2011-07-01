//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.server.dbtransfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ZipInput {

	private ZipInputStream in;

	ZipInput(InputStream in) {
		this.in = new ZipInputStream(in);
	}

	ZipEntry getNextEntry() {
		try {
			return in.getNextEntry();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	byte[] readEntry() {
		try {
			byte[] bytes = getBytes(in, 8192);
			in.closeEntry();
			return bytes;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	String readStringEntry() {
		byte[] bytes = readEntry();
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	void close() {
		try {
			in.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] getBytes(InputStream in, int minBufferSize)
		throws IOException
	{
		LinkedList<byte[]> bufs = new LinkedList<byte[]>();
		byte[] buf = new byte[Math.max(in.available(), minBufferSize)];
		int offset = 0, size = 0;
		for (int n; (n = in.read(buf, offset, buf.length - offset)) > 0;) {
			offset += n;
			if (offset == buf.length) {
				bufs.add(buf);
				size += offset;
				buf = new byte[Math.max(in.available(), minBufferSize)];
				offset = 0;
			}
		}
		if (offset == 0 && bufs.size() == 1)
			return bufs.get(0);
		else {
			bufs.add(buf);
			size += offset;
			buf = new byte[size];
			for (byte[] b : bufs) {
				System.arraycopy(
					b, 0, buf, buf.length - size, Math.min(b.length, size));
				size -= b.length;
			}
			return buf;
		}
	}

}
