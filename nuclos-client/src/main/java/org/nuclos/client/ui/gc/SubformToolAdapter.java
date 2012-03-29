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

import org.nuclos.client.ui.collect.SubForm.SubFormToolListener;

class SubformToolAdapter extends EventAdapter implements SubFormToolListener {

	SubformToolAdapter(SubFormToolListener wrapped) {
		super(wrapped);
	}

	@Override
	public void toolbarAction(String actionCommand) {
		final SubFormToolListener l = (SubFormToolListener) wrapped.get();
		if (l != null) {
			l.toolbarAction(actionCommand);
		}
	}

}
