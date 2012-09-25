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

import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

import javax.swing.JPasswordField;

import org.apache.log4j.Logger;

/**
 * <code>JTextField</code> which may not be smaller than its preferred size, so all characters
 * are always visible.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CommonJPasswordField extends JPasswordField {

	private static final Logger LOG = Logger.getLogger(CommonJPasswordField.class);

	/**
	 * caches the column width so it needn't be recalculated every time.
	 */
	private int iColumnWidth;

	/**
	 * the character used to calculate the width needed by one column.
	 */
	private char cColumnWidthChar = 'm';

	public CommonJPasswordField() {
	}

	public CommonJPasswordField(int iColumns) {
		super(iColumns);
	}

	public CommonJPasswordField(String sText) {
		super(sText);
	}

	/**
	 * sets the minimum size equal to the preferred size in order to avoid
	 * GridBagLayout flaws.
	 * @return the value of the <code>preferredSize</code> property
	 */
	@Override
	public Dimension getMinimumSize() {
		return this.getPreferredSize();
	}

	/**
	 * sets the character used to calculate the width needed by one column.
	 * @param cColumnWidthChar
	 */
	public void setColumnWidthChar(char cColumnWidthChar) {
		this.cColumnWidthChar = cColumnWidthChar;
	}

	/**
	 * @return the width of one column
	 */
	@Override
	protected int getColumnWidth() {
		if (iColumnWidth == 0) {
			iColumnWidth = getFontMetrics(getFont()).charWidth(cColumnWidthChar);
		}
		return iColumnWidth;
	}

	/**
	 * NUCLEUSINT-1000
	 * Inserts the clipboard contents into the text.
	 */
	@Override
	public void paste() {
		if (this.isEditable()) {
			Clipboard clipboard = getToolkit().getSystemClipboard();
			try {
				// The MacOS MRJ doesn't convert \r to \n,
				// so do it here
				String selection = ((String) clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor)).replace('\r', '\n');
				if (selection.endsWith("\n")) {
					selection = selection.substring(0, selection.length()-1);
				}
				//NUCLEUSINT-1139
				replaceSelection(selection.trim()); // trim selection. @see NUCLOS-1112 
			}
			catch (Exception e) {
				getToolkit().beep();
				LOG.warn("Clipboard does not contain a string: " + e, e);
			}
		}
	}

}  // class CommonJTextField
