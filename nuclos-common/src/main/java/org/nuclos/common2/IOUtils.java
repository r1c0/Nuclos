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
package org.nuclos.common2;

import org.nuclos.common.NuclosMarshalledInputStream;
import org.nuclos.common2.exception.CommonFatalException;
import java.io.*;
import java.io.File;
import java.nio.channels.FileChannel;

/**
 * Utility methods for input/output.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class IOUtils {

	private IOUtils() {
	}

	/**
	 * reads the contents of a text file, using the default encoding.
	 *
	 * @param file a File that must have a size < 2GB.
	 * @return a String containing the contents of the file.
	 * @throws java.io.IOException
	 * @todo the returned String does not contain any newlines - correct this!
	 * @todo move ElisaConsole.readFromTextFile to this class
	 * @todo call readFromTextFile( File file, String sEncoding)
	 */
	public static String readFromTextFile(File file) throws IOException {
		final FileReader fr = new FileReader(file);
		try {
			final BufferedReader br = new BufferedReader(fr);
			try {
				final StringBuffer sb = new StringBuffer();
				while (true) {
					final String sLine = br.readLine();
					if (sLine == null) {
						break;
					}
					sb.append(sLine);
				}
				return sb.toString();
			}
			finally {
				br.close();
			}
		}
		finally {
			fr.close();
		}
	}


	/**
	 * Reads the text contents of the given input stream with the specified encoding.
	 *
	 * @param file a File that must have a size < 2GB.
	 * @param encoding encoding of the resulting String
	 * @return a String containing the contents of the file.
	 * @throws java.io.IOException
	 */
	public static String readFromTextFile(File file, String encoding) throws IOException, UnsupportedEncodingException {
		return readFromTextStream(new FileInputStream(file), encoding);
	}

	/**
	 * Reads the text contents of a given input stream with the specified encoding.
	 *
	 * @param is a input stream
	 * @param encoding encoding of the resulting String
	 * @return a String containing the contents of the file.
	 * @throws java.io.IOException
	 */
	public static String readFromTextStream(InputStream is, String encoding) throws IOException, UnsupportedEncodingException {
		final InputStreamReader isr = encoding == null ? new InputStreamReader(is) :new InputStreamReader(is, encoding);
		try {
			final BufferedReader br = new BufferedReader(isr);
			try {
				final StringBuilder sb = new StringBuilder();
				while (true) {
					final String sLine = br.readLine();
					if (sLine == null) {
						break;
					}
					sb.append(sLine);
					sb.append('\n');
				}
				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}
				return sb.toString();
			}
			finally {
				br.close();
			}
		}
		finally {
			isr.close();
		}
	}
	/**
	 *
	 * @param file the file to write
	 * @param sText String to write in file
	 * @param sEncoding the used charecter encoding
	 */
	public static void writeToTextFile(File file, String sText, String sEncoding) throws IOException, UnsupportedEncodingException {
		final Writer osw = new OutputStreamWriter(new FileOutputStream(file), sEncoding);
		try {
			final Writer bw = new BufferedWriter(osw);
			try {
				bw.write(sText);
			}
			finally {
				bw.close();
			}
		}
		finally {
			osw.close();
		}
	}

	/**
	 * reads the contents of a binary file.
	 *
	 * @param file a File that must have a size < 2GB.
	 * @return a byte[] containing the contents of the file.
	 * @throws java.io.IOException
	 */
	public static byte[] readFromBinaryFile(File file) throws IOException {
		final FileInputStream fis = new FileInputStream(file);
		try {
			final BufferedInputStream bis = new BufferedInputStream(fis);
			try {
				final long lFileSize = file.length();
				if (lFileSize > Integer.MAX_VALUE) {
					throw new CommonFatalException(StringUtils.getParameterizedExceptionMessage("ioutils.file.error", file.getAbsolutePath()));
						//"Die Datei " + file.getAbsolutePath() + " ist zu gro\u00df. getContentsOfBinaryFile() kann Dateien > 2GB nicht lesen.");
				}
				final int iFileSize = (int) lFileSize;
				final byte[] result = new byte[iFileSize];
				final int iBytesRead = bis.read(result);
				if (iBytesRead != iFileSize) {
					throw new CommonFatalException(StringUtils.getParameterizedExceptionMessage("ioutils.read.file.error", file.getAbsolutePath()));
						//"Die Datei " + file.getAbsolutePath() + " konnte nicht vollst\u00e4ndig gelesen werden.");
				}
				return result;
			}
			finally {
				bis.close();
			}
		}
		finally {
			fis.close();
		}
	}

	/**
	 * writes the given byte array to the given binary file.
	 *
	 * @param file
	 * @throws java.io.IOException
	 */
	public static void writeToBinaryFile(File file, byte[] ab) throws IOException {
		final FileOutputStream fos = new FileOutputStream(file);
		try {
			final BufferedOutputStream bos = new BufferedOutputStream(fos);
			try {
				bos.write(ab);
			}
			finally {
				bos.close();
			}
		}
		finally {
			fos.close();
		}
	}

	/**
	 * @return the default directory for temporary files, as specified in the System Property "java.io.tmpdir".
	 */
	public static File getDefaultTempDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	/**
	 * @param fileIn
	 * @param fileOut
	 * @throws IOException
	 */
	public static void copyFile(File fileIn, File fileOut) throws IOException {
		FileChannel sourceChannel = null;
		FileChannel destinationChannel = null;
		try {
			sourceChannel = new FileInputStream(fileIn).getChannel();
			destinationChannel = new FileOutputStream(fileOut).getChannel();
			sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		}
		finally {
			try {
				if (sourceChannel != null) {
					sourceChannel.close();
				}
			}
			finally {
				if (destinationChannel != null) {
					destinationChannel.close();
				}
			}
		}
	}

	/**
	 * @param o must be serializable.
	 * @return a byte array containing the serialized object.
	 * @throws IOException
	 */
	public static byte[] toByteArray(Object o) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(baos);
		try {
			oos.writeObject(o);
		}
		finally {
			oos.close();
		}
		return baos.toByteArray();
	}

	/**
	 *
	 * @param ab a byte array containing a serialized object.
	 * @return the object deserialized from <code>ab</code>.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object fromByteArray(final byte[] ab) throws IOException, ClassNotFoundException {
		final ObjectInputStream ois = new NuclosMarshalledInputStream(new ByteArrayInputStream(ab));
		final Object result;
		try {
			result = ois.readObject();
		}
		finally {
			ois.close();
		}
		return result;
	}

}  // class IOUtils
