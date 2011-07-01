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

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Icons for option panes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public final class JOptionPaneIcons {

	private static final String[] asFileNames = new String[4];

	static {
		asFileNames[JOptionPane.INFORMATION_MESSAGE] = "Inform";
		asFileNames[JOptionPane.QUESTION_MESSAGE] = "Question";
		asFileNames[JOptionPane.WARNING_MESSAGE] = "Warn";
		asFileNames[JOptionPane.ERROR_MESSAGE] = "Error";
	}

	public static String getFileName(int iLookAndFeel, int iMessageType) {
		final String sFileName = asFileNames[iMessageType];
		final String sPackageName = LookAndFeel.getPlafPackageName(iLookAndFeel);

		return sPackageName.replace('.', "/".charAt(0)) + "/icons/" + sFileName + ".gif";
	}

	public static ImageIcon getImageIcon(int iLookAndFeel, int iMessageType) {
		ImageIcon result = null;

		if (iMessageType != JOptionPane.PLAIN_MESSAGE) {
			result = new ImageIcon(JOptionPaneIcons.class.getClassLoader().getResource(getFileName(iLookAndFeel, iMessageType)));
		}

		return result;
	}

}	// class JOptionPaneIcons
