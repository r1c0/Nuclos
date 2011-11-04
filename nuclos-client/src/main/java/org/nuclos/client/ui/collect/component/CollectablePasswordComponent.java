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

import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.labeled.LabeledTextComponent;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;

/**
 * NUCLEUSINT-1142
 * @author hartmut.beckschulze
 * @version 1.0.0
 */
public abstract class CollectablePasswordComponent extends LabeledCollectableComponent {

	private static final Logger LOG = Logger.getLogger(CollectablePasswordComponent.class);
	
	protected CollectablePasswordComponent(CollectableEntityField clctef, LabeledTextComponent labeledtextcomp, boolean bSearchable) {
		super(clctef, labeledtextcomp, bSearchable);

		this.getJTextComponent().getDocument().addDocumentListener(newDocumentListenerForTextComponentWithComparisonOperator());
	}

	protected LabeledTextComponent getLabeledTextComponent() {
		return (LabeledTextComponent) this.getJComponent();
	}

	protected JTextComponent getJTextComponent() {
		return this.getLabeledTextComponent().getJTextComponent();
	}

	@Override
	public boolean hasComparisonOperator() {
		return true;
	}

	@Override
	protected void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
		this.getJTextComponent().addMouseListener(popupmenulistener);
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		final CollectableFieldFormat clctfformat = CollectableFieldFormat.getInstance(this.getEntityField().getJavaClass());
		final String sText = clctfformat.format(getEntityField().getFormatOutput(), clctfValue.getValue());

		final JTextComponent textcomp = this.getJTextComponent();
		textcomp.setText(sText);
		// ensure the start of the text is visible (instead of the end) when the text is too long
		// to be fully displayed:
		textcomp.setCaretPosition(0);

		this.adjustAppearance();
	}

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		return CollectableTextComponentHelper.write(this.getJTextComponent(), this.getEntityField());
	}

	@Override
	protected CollectableSearchCondition getSearchConditionFromView() throws CollectableFieldFormatException {
		return this.getSearchConditionFromViewImpl(this.getJTextComponent().getText());
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
		// update the view:
		this.runLocked(new Runnable() {
			@Override
			public void run() {
				try {
					// Note: CollectableTextComponent itself can only handle atomic search conditions.
					// If the following class cast should ever fail for a special text field, redefine searchConditionChangedInModel in your subclass:
					final AtomicCollectableSearchCondition atomiccond = (AtomicCollectableSearchCondition) ev.getSearchComponentModel().getSearchCondition();
	
					modelToView(atomiccond, CollectablePasswordComponent.this.getJTextComponent());
				}
				catch (Exception e) {
					LOG.error("CollectablePassword.searchConditionChangedInModel: " + e, e);
				}
			}
		});
	}

	/**
	 * The "enabled" property of <code>CollectableTextComponent</code> is mapped to the
	 * "editable" property of <code>LabeledTextComponent</code> to ensure that copy (as in "copy&paste")
	 * works on disabled text components.
	 * @param bEnabled
	 */
	@Override
	public void setEnabled(boolean bEnabled) {
		this.getLabeledTextComponent().setEditable(bEnabled);
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		/** @todo check this */
//		this.getJTextComponent().setEditable(bInsertable);
	}

}	// class CollectableTextComponent
