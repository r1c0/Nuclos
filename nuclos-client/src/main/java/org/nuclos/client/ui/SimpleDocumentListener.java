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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Easy document listener that responds the same way for any document changes, whether insert, update or remove.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public abstract class SimpleDocumentListener implements DocumentListener {

	/**
	 * This method is called on any document change, whether insert, update or remove.
	 * @param ev
	 */
	public abstract void documentChanged(DocumentEvent ev);

	@Override
	public final void changedUpdate(DocumentEvent ev) {
		this.documentChanged(ev);
	}

	@Override
	public final void insertUpdate(DocumentEvent ev) {
		this.documentChanged(ev);
	}

	@Override
	public final void removeUpdate(DocumentEvent ev) {
		this.documentChanged(ev);
	}

}  // class SimpleDocumentListener
