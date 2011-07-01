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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;

import org.apache.commons.lang.NullArgumentException;

/**
 * Class encapsulating a file with its name and contents. This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version	00.01.000
 */
public class File implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TYPE_PDF = "PDF";
	public static final String TYPE_XLS = "XLS";
	public static final String TYPE_DOC = "DOC";
	public static final String TYPE_PPT = "PPT";
	public static final String TYPE_TXT = "TXT";
	public static final String TYPE_UNKNOWN = "XXX";

	private final String sFileName;
	private final String sFileType;
	/** @todo make this one private final again */
	protected byte[] contents;

	/**
	 * creates a File object with the given name and contents. Note that this does not create a real file (on disk),
	 * it's just a placeholder!
	 * @param sFileName
	 * @param abContents
	 * @precondition sFileName != null
	 * @precondition abContents != null
	 */
	public File(String sFileName, byte[] abContents) {
		this(sFileName);
		if (abContents == null) {
			throw new NullArgumentException("abContents");
		}
		this.contents = abContents;
	}

	/**
	 * creates a File object without contents. May be used by successors to implement lazy loading of contents.
	 * @param sFileName
	 * @precondition sFileName != null
	 * @postcondition this.contents == null
	 */
	protected File(String sFileName) {
		if (sFileName == null) {
			throw new NullArgumentException("sFileName");
		}
		this.sFileName = sFileName;
		this.sFileType = getFiletype(getExtension(sFileName));
		assert this.contents == null;
	}

	public String getFilename() {
		return this.sFileName;
	}

	/**
	 * @todo add postcondition result != null
	 * @return
	 */
	public byte[] getContents() {
		return this.contents;
	}

	public String getFiletype() {
		return this.sFileType;
	}

	@Override
	public boolean equals(Object o) {
		final boolean result;
		if(!(o instanceof File)) {
			result = false;
		}
		else if (o == null) {
			result = false;
		}
		else if (this == o) {
			result = true;
		}
		else {
			final File that = (File) o;
			result = LangUtils.equals(this.getFilename(), that.getFilename()) &&
					LangUtils.equals(this.getFiletype(), that.getFiletype()) &&
					org.apache.commons.lang.ArrayUtils.isEquals(this.getContents(), that.getContents());
		}
		return result;
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.getFilename());
		// We can safely ignore the file type and the contents here.
	}

	@Override
	public String toString() {
		return this.sFileName;
	}

	/**
	 * @param sFilename
	 * @return the extension of the given file name, if any.
	 */
	public static String getExtension(String sFilename) {
		if (sFilename == null) {
			throw new NullArgumentException("sFileName");
		}
		final int iPosition = sFilename.lastIndexOf('.');
		return (iPosition == -1) ? null : sFilename.substring(iPosition + 1);
	}

	/**
	 * @param sExtension
	 * @return the file type of the given extension
	 */
	public static String getFiletype(String sExtension) {
		String sFiletype = TYPE_UNKNOWN;
		if (sExtension != null) {
			final String sUpperCaseExtension = sExtension.toUpperCase();
			if ("PDF".equals(sUpperCaseExtension)) {
				sFiletype = TYPE_PDF;
			}
			else if ("XLS".equals(sUpperCaseExtension)) {
				sFiletype = TYPE_XLS;
			}
			else if ("DOC".equals(sUpperCaseExtension)) {
				sFiletype = TYPE_DOC;
			}
			else if ("PPT".equals(sUpperCaseExtension)) {
				sFiletype = TYPE_PPT;
			}
			else if ("TXT".equals(sUpperCaseExtension)) {
				sFiletype = TYPE_TXT;
			}
			else if ("ASC".equals(sUpperCaseExtension)) {
				sFiletype = TYPE_TXT;
			}
			else if ("CSV".equals(sUpperCaseExtension)) {
				sFiletype = TYPE_TXT;
			}
		}
		assert sFiletype != null;
		return sFiletype;
	}
	
	/**
	 * Simple Method for copying files with FileChannel
	 * FIX ELISA-6498
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	 public static void copyFile(java.io.File in, java.io.File out) throws IOException { 
	        FileChannel inChannel = new FileInputStream(in).getChannel(); 
	        FileChannel outChannel = new FileOutputStream(out).getChannel(); 
	        try { 
	            inChannel.transferTo(0, inChannel.size(), outChannel); 
	        } catch (IOException e) { 
	            throw e; 
	        } finally { 
	            if (inChannel != null) 
	                inChannel.close(); 
	            if (outChannel != null) 
	                outChannel.close(); 
	        } 
	    } 
}
