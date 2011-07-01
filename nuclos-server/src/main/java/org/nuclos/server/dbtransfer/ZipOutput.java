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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipOutput {

	private ZipOutputStream out;

	ZipOutput(OutputStream out) {
		this.out = new ZipOutputStream(out);
	}

	void addEntry(String name, byte[] bytes) {
		ZipEntry e = new ZipEntry(name);
		try {
			out.putNextEntry(e);
			out.write(bytes);
			out.closeEntry();
			out.flush();
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	void addEntry(String name, String content) {
		try {
			addEntry(name, content.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	void close() {
		try {
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
