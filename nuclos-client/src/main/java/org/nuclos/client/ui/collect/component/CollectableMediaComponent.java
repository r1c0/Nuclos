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
package org.nuclos.client.ui.collect.component;

import javax.swing.JLabel;

import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.labeled.LabeledMediaComponent;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;

/**
 * This class was meant to be a common ancestor of both CollectableTextField and CollectableTextArea.
 * But, CollectableTextArea is not derived from LabeledCollectableComponent.
 * So, this class is not needed at the moment, but might be usable when there will be a need
 * for other text components (like JFormattedTextField, JPasswordField).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class CollectableMediaComponent extends LabeledCollectableComponent {

	protected CollectableMediaComponent(CollectableEntityField clctef, LabeledMediaComponent labeledmediacomp, boolean bSearchable) {
		super(clctef, labeledmediacomp, bSearchable);
	}

	protected LabeledMediaComponent getLabeledMediaComponent() {
		return (LabeledMediaComponent) this.getJComponent();
	}

	protected JLabel getMediaComponent() {
		return this.getLabeledMediaComponent().getJMediaComponent();
	}

	@Override
	public boolean hasComparisonOperator() {
		return false;
	}

	@Override
	protected void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
		this.getMediaComponent().addMouseListener(popupmenulistener);
	}

	@Override
	protected CollectableSearchCondition getSearchConditionFromView() throws CollectableFieldFormatException {
		return null; 
	}

	/**
	 * Implementation of <code>CollectableComponentModelListener</code>.
	 * @param ev
	 */
	@Override
	public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
		if (this.isSearchComponent()) {
			// simply ignore this event
		}
		else {
			super.collectableFieldChangedInModel(ev);
		}
	}

	/**
	 * Implementation of <code>CollectableComponentModelListener</code>.
	 * @param ev
	 */
	@Override
	public void searchConditionChangedInModel(final SearchComponentModelEvent ev) {
		// media can not be searched		
	}
}	// class CollectableTextComponent
