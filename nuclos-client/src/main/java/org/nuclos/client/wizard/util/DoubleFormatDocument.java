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
import org.nuclos.common2.SpringLocaleDelegate;

public class DoubleFormatDocument extends PlainDocument {

	private static final Logger LOG = Logger.getLogger(DoubleFormatDocument.class);

	@Override
	public void insertString(int offset, String str, javax.swing.text.AttributeSet a)  throws javax.swing.text.BadLocationException {
		try {
			String lang = SpringLocaleDelegate.getInstance().getLocale().getLanguage();
			if(str.length() == 1) {
				String comma = ",";
				if("de".equals(lang))
					comma = ".";
				if(str.indexOf(comma) >= 0)
					return;
				String format = str.replace(',', '.');
				
				if(format.endsWith("."))
					format += "0";
				Double.parseDouble(format);
			}
			else if(str.length() > 1) {
				if("de".equals(lang)) {
					str = str.replace('.', ',');
				}
			}
		}
		catch(NumberFormatException e) {
			LOG.info("insertString: " + e);
			return;
		}
		super.insertString(offset, str, a);
    }
}
