//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
//
package org.nuclos.client.ui.gc;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class DocumentAdapter extends EventAdapter implements DocumentListener {

	DocumentAdapter(DocumentListener wrapped) {
		super(wrapped);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		final DocumentListener l = (DocumentListener) wrapped.get();
		if (l != null) {
			l.insertUpdate(e);
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		final DocumentListener l = (DocumentListener) wrapped.get();
		if (l != null) {
			l.removeUpdate(e);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		final DocumentListener l = (DocumentListener) wrapped.get();
		if (l != null) {
			l.changedUpdate(e);
		}
	}

}
