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
package org.nuclos.tools.ruledoc.javaToHtml;

import java.io.*;

public class IoUtilities {
	private IoUtilities() {
		//no instance available
	}

	public static byte[] readBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		copyStream(inputStream, byteOut);
		return byteOut.toByteArray();
	}

	public static void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4096];
		while (true) {
			int bytesRead = in.read(buffer);
			if (bytesRead == -1) {
				break;
			}
			out.write(buffer, 0, bytesRead);
		}
	}

	public static void close(OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.close();
			}
			catch (IOException e) {
				//nothing to do
			}
		}
	}

	public static void close(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			}
			catch (IOException e) {
				//nothing to do
			}
		}
	}

	public static void close(Writer writer) {
		if (writer != null) {
			try {
				writer.close();
			}
			catch (IOException e) {
				//nothing to do
			}
		}
	}

	public static void close(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			}
			catch (IOException e) {
				//nothing to do
			}
		}
	}

	public static void copy(File sourceFile, File destinationFile) throws IOException {
		if (!ensureFoldersExist(destinationFile.getParentFile())) {
			throw new IOException("Unable to create necessary output directory "
					+ destinationFile.getParentFile());
		}
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(sourceFile));
			bos = new BufferedOutputStream(new FileOutputStream(destinationFile));
			copyStream(bis, bos);
		}
		finally {
			close(bis);
			close(bos);
		}
	}

	public static boolean ensureFoldersExist(File folder) {
		if (folder.exists()) {
			return true;
		}
		return folder.mkdirs();
	}

	public static File exchangeFileExtension(File file, String newFileExtension) {
		String fileName = file.getAbsolutePath();
		int index = fileName.lastIndexOf('.');
		if (index == -1) {
			throw new IllegalStateException("Unable to determine file extension from file name '"
					+ fileName
					+ "'");
		}
		return new File(fileName.substring(0, index + 1) + newFileExtension);
	}
}
