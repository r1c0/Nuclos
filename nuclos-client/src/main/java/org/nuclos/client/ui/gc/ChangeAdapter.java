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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class ChangeAdapter extends EventAdapter implements ChangeListener {

	ChangeAdapter(ChangeListener wrapped) {
		super(wrapped);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		final ChangeListener l = (ChangeListener) wrapped.get();
		if (l != null) {
			l.stateChanged(e);
		}
	}

}
