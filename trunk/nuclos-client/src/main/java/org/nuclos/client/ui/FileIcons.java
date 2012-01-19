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
package org.nuclos.client.ui;

import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;

import org.nuclos.common2.File;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Icons for files.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class FileIcons {
	private static final Logger log = Logger.getLogger(FileIcons.class);

	private FileIcons() {
		// do nothing
	}

	/**
	 * @param sFileType
	 * @return the Icon suitable to the given file type.
	 * @precondition sFileType != null
	 */
	public static ImageIcon getIcon(String sFileType) {
		if(sFileType == null) {
			throw new NullArgumentException("sFileType");
		}
		final String sIconFileName = "org/nuclos/client/ui/images/file/" + getIconFileName(sFileType);
		final URL url = FileIcons.class.getClassLoader().getResource(sIconFileName);
		if(url == null) {
			throw new CommonFatalException(StringUtils.getParameterizedExceptionMessage("FileIcons.1", sIconFileName));//"Bilddatei nicht gefunden: " + sIconFileName);
		}
		return new ImageIcon(url);
	}

	private static String getIconFileName(String sFileType) {
		final String sFile;
		if(sFileType == null || sFileType.equals(File.TYPE_UNKNOWN)) {
			sFile = "generic";
		}
		else if(sFileType.equals(File.TYPE_DOC)) {
			sFile = "word";
		}
		else if(sFileType.equals(File.TYPE_XLS)) {
			sFile = "excel";
		}
		else if(sFileType.equals(File.TYPE_PPT)) {
			sFile = "powerpoint";
		}
		else if(sFileType.equals(File.TYPE_PDF)) {
			sFile = "pdf";
		}
		else if(sFileType.equals(File.TYPE_TXT)) {
			sFile = "text";
		}
		else {
			throw new CommonFatalException(StringUtils.getParameterizedExceptionMessage("FileIcons.2", sFileType));//"Unbekannter Dateityp: " + sFileType);
		}
		final String result = sFile + "-file.png";
		log.debug("Bilddatei: " + result);
		return result;
	}

}  // class FileIcons
