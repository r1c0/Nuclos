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

import java.awt.event.ActionListener;

import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;

class CollectableComponentModelAdapter extends EventAdapter implements CollectableComponentModelListener {

	CollectableComponentModelAdapter(CollectableComponentModelListener wrapped) {
		super(wrapped);
	}

	@Override
	public void collectableFieldChangedInModel(CollectableComponentModelEvent e) {
		final CollectableComponentModelListener l = (CollectableComponentModelListener) wrapped.get();
		if (l != null) {
			l.collectableFieldChangedInModel(e);
		}
	}

	@Override
	public void searchConditionChangedInModel(SearchComponentModelEvent e) {
		final CollectableComponentModelListener l = (CollectableComponentModelListener) wrapped.get();
		if (l != null) {
			l.searchConditionChangedInModel(e);
		}
	}

	@Override
	public void valueToBeChanged(DetailsComponentModelEvent e) {
		final CollectableComponentModelListener l = (CollectableComponentModelListener) wrapped.get();
		if (l != null) {
			l.valueToBeChanged(e);
		}
	}

}
