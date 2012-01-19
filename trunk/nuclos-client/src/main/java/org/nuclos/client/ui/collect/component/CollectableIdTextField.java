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

import org.apache.log4j.Logger;
import org.nuclos.client.ui.CommonJTextField;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.labeled.LabeledTextField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * <code>CollectableComponent</code> that presents an id field in a <code>JTextField</code>.
 * If non-searchable, the component is always read-only, that is the contents cannot be changed directly,
 * only via <code>setField()</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class CollectableIdTextField extends CollectableTextComponent {
	
	private static final Logger LOG = Logger.getLogger(CollectableIdTextField.class);

	/**
	 * the value id "remembered in the view", as the JTextField only holds the value.
	 */
	private Object oValueId;

	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableIdTextField(CollectableEntityField clctef) {
		this(clctef, false);

		assert this.isDetailsComponent();
	}

	public CollectableIdTextField(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new LabeledTextField(clctef.isNullable(), clctef.getJavaClass(), clctef.getFormatInput(), bSearchable), bSearchable);

		if (!clctef.isIdField()) {
			throw new CommonFatalException(StringUtils.getParameterizedExceptionMessage("collectable.idtextfield.exception.1", clctef.getName()));
				//"CollectableIdTextField " + clctef.getName() + " kann nur Id-Felder anzeigen.");
		}

		this.getJTextField().setEditable(bSearchable);
	}

	public CommonJTextField getJTextField() {
		return (CommonJTextField) this.getJTextComponent();
	}

	@Override
	public void setColumns(int iColumns) {
		this.getJTextField().setColumns(iColumns);
	}

	@Override
	public void setEnabled(boolean bEnabled) {
		if (bEnabled && !isSearchComponent()) {
			final String sMessage = StringUtils.getParameterizedExceptionMessage("collectable.idtextfield.exception.2", getEntityField().getName());
				//"CollectableIdTextField " + getEntityField().getName() + " darf nicht aktiviert (enabled) werden.";
			throw new CommonFatalException(sMessage);
		}
		super.setEnabled(bEnabled);
	}

	@Override
	public void setComparisonOperator(ComparisonOperator compop) {
		super.setComparisonOperator(compop);

		if (compop.getOperandCount() < 2) {
			this.runLocked(new Runnable() {
				@Override
                public void run() {
					try {
						getJTextComponent().setText(null);
					}
					catch (Exception e) {
						LOG.error("CollectableTextField.setComparisionOperator: " + e, e);
					}
				}
			});
		}
	}

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		return new CollectableValueIdField(this.oValueId, super.getFieldFromView().getValue());
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		super.updateView(clctfValue);

		this.oValueId = clctfValue.getValueId();
	}

	/**
	 * Implementation of <code>CollectableComponentModelListener</code>.
	 * This event is (and must be) ignored for a searchable text field.
	 * @param ev
	 */
	@Override
	public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
		if (this.isSearchComponent()) {
			// the text is set in searchConditionChangedInModel, but the value id is set here:
			this.oValueId = ev.getNewValue().getValueId();
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
		super.searchConditionChangedInModel(ev);
		// Note that the value id is set in collectableFieldChangedInModel.
	}

}  // class CollectableIdTextField
