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
package org.nuclos.client.statemodel.models;

import java.io.Serializable;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

/**
 * The model for <code>NotePropertiesPanel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 01.00.00
 */
public class NotePropertiesPanelModel implements Serializable {

	private static final Logger LOG = Logger.getLogger(NotePropertiesPanelModel.class);

	public static final String PROPERTY_NOTE_TEXT = "NoteText";

	public final Document docText = new DefaultStyledDocument();

	public String getText() {
		String sResult = "";
		try {
			sResult = docText.getText(0, docText.getLength());
		}
		catch (BadLocationException e) {
			// this should never happens
			LOG.warn("getText failed: " + e, e);
		}
		return sResult;
	}

	public void setText(String sName) {
		try {
			this.docText.remove(0, docText.getLength());
			this.docText.insertString(0, sName, null);
		}
		catch (BadLocationException e) {
			// this should never happens
			LOG.warn("setText failed: " + e, e);
		}
	}
}
