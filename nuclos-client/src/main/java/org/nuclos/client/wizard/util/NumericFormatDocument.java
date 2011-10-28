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
package org.nuclos.client.wizard.util;

import javax.swing.text.PlainDocument;

import org.apache.log4j.Logger;

public class NumericFormatDocument extends PlainDocument {

	private static final Logger LOG = Logger.getLogger(NumericFormatDocument.class);

	@Override
	public void insertString(int offset, String str, javax.swing.text.AttributeSet a)  throws javax.swing.text.BadLocationException {
		try {
			Integer.parseInt(str);
		}
		catch(NumberFormatException e) {
			LOG.debug("insertString: not a Integer: " + str + ": " + e);
			return;
		}
		super.insertString(offset, str, a);
    }
}