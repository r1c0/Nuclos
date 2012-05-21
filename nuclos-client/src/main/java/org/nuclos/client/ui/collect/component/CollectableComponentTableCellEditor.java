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

import java.awt.Component;
import java.util.Date;

import javax.swing.AbstractCellEditor;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelHelper;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.collect.component.verifier.DateInputVerifier;
import org.nuclos.client.ui.collect.component.verifier.FloatAndDoubleInputVerifier;
import org.nuclos.client.ui.collect.component.verifier.TrueInputVerifier;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;

/**
 * <code>TableCellEditor</code> for a <code>CollectableComponent</code>. This makes it possible to edit
 * any <code>CollectableField</code> in a <code>JTable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class CollectableComponentTableCellEditor extends AbstractCellEditor implements TableCellEditor, CollectableComponentModelListener {

	private static final Logger log = Logger.getLogger(CollectableComponentTableCellEditor.class);

	private CollectableComponent clctcomp;
	private final CollectableEntityField clctef;
	private final CollectableComponentModelHelper clctcompmodelhelper = new CollectableComponentModelHelper();
	private final boolean bSearchable;
	private CollectableSearchCondition clctcond;
	private CollectableField clctfValue;
	private int editingRow = -1;

	/**
	 * creates a <code>TableCellEditor</code> that lazily creates a suitable <code>CollectableComponent</code>
	 * according to <code>clctef</code>.
	 * @param clctef
	 * @postcondition !this.isSearchable()
	 */
	public CollectableComponentTableCellEditor(CollectableEntityField clctef) {
		if (clctef == null) {
			throw new NullArgumentException("clctef");
		}
		this.clctef = clctef;
		this.bSearchable = false;
		this.editorDateInputVerifier = new DateInputVerifier(clctef);
		this.floatAndDoubleInputVerifier = new FloatAndDoubleInputVerifier(clctef);
		this.trueInputVerifier = new TrueInputVerifier();
		
		assert !this.isSearchable();
	}

	/**
	 * creates a <code>TableCellEditor</code> that uses the given <code>CollectableComponent</code>.
	 * @param clctcomp
	 * @postcondition !this.isSearchable()
	 * @deprecated This for backwards compatibility with LindaCollectableComponentTableCellEditor only.
	 */
	@Deprecated
	public CollectableComponentTableCellEditor(CollectableComponent clctcomp) {
		this(clctcomp, false);
		assert !this.isSearchable();
	}

	/**
	 * creates a <code>TableCellEditor</code> that uses the given <code>CollectableComponent</code>.
	 * @param clctcomp
	 * @param bSearchable
	 * @postcondition this.isSearchable() <--> bSearchable
	 */
	public CollectableComponentTableCellEditor(CollectableComponent clctcomp, boolean bSearchable) {
		if (clctcomp == null) {
			throw new NullArgumentException("clctcomp");
		}
		this.clctcomp = clctcomp;
		this.clctcomp.getModel().addCollectableComponentModelListener(this);
		this.clctef = clctcomp.getEntityField();
		this.bSearchable = bSearchable;
		this.editorDateInputVerifier = new DateInputVerifier(clctef);
		this.floatAndDoubleInputVerifier = new FloatAndDoubleInputVerifier(clctef);
		this.trueInputVerifier = new TrueInputVerifier();

		assert this.isSearchable() == bSearchable;
	}

	public boolean isSearchable() {
		return this.bSearchable;
	}

	public CollectableComponent getCollectableComponent() {
		if (this.clctcomp == null) {
			// create the component lazily:
			this.clctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(this.clctef, null, false);
			/** @todo add preferences to clctcomp */
			this.clctcomp.getModel().addCollectableComponentModelListener(this);
		}

		// don't show the component's label:
		this.clctcomp.setLabelText(null);

		return this.clctcomp;
	}

	@Override
    public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
		/** @todo ChangeListener would be cleaner here. */
		if (ev.collectableFieldHasChanged()) {
			// pass the event on:
			this.fireCollectableComponentModelChanged(ev);
		}
	}

	@Override
    public void searchConditionChangedInModel(SearchComponentModelEvent ev) {
		this.fireSearchableCollectableComponentModelChanged(ev);
	}

	@Override
    public void valueToBeChanged(DetailsComponentModelEvent ev) {
		/** @todo Is it right to do nothing here? (Probably: "yes, because this is never called") */
		// do nothing
	}

	public void addCollectableComponentModelListener(CollectableComponentModelListener listener) {
		clctcompmodelhelper.addCollectableComponentModelListener(listener);
	}

	public void removeCollectableComponentModelListener(CollectableComponentModelListener listener) {
		clctcompmodelhelper.removeCollectableComponentModelListener(listener);
	}

	private void fireCollectableComponentModelChanged(CollectableComponentModelEvent ev) {
		clctcompmodelhelper.fireCollectableFieldChanged(ev.getCollectableComponentModel(), ev.getOldValue(), ev.getNewValue());
	}

	private void fireSearchableCollectableComponentModelChanged(SearchComponentModelEvent ev) {
		clctcompmodelhelper.fireSearchConditionChanged(ev.getSearchComponentModel());
	}

	@Override
    public Object getCellEditorValue() {
		return this.isSearchable() ? (Object) this.clctcond : (Object) this.clctfValue;
	}

	@Override
	public boolean stopCellEditing() {
		//noinspection LocalCanBeFinal
		boolean result;
		try {
			this.setValue();
			result = true;
			this.fireEditingStopped();
		}
		catch (CollectableFieldFormatException ex) {
			log.debug("stopCellEditing: Invalid value in cell.", ex);
			result = false;
			/** @todo returning false doesn't seem to work for BasicTableUI. */
		}
		return result;
	}

	@Override
	public void cancelCellEditing() {
		this.resetValue();
		this.fireEditingCanceled();
	}

	private void setValue() throws CollectableFieldFormatException {
		if (this.isSearchable()) {
			this.clctcond = this.getCollectableComponent().getSearchCondition();
		}
		else {
			try {
				this.getCollectableComponent().makeConsistent();
			}
			catch (CollectableFieldFormatException ex) {
				log.warn("validation failed", ex);
				Errors.getInstance().showExceptionDialog(clctcomp.getJComponent(),
					new CommonValidationException(StringUtils.getParameterizedExceptionMessage("field.invalid.value", clctef.getLabel())));//"Das Feld \"" + clctef.getLabel() + "\" hat keinen g\u00fcltigen Wert."));
			}
			this.clctfValue = this.getCollectableComponent().getField();
		}
	}

	private void resetValue() {
		if (this.isSearchable()) {
			this.clctcond = null;
		}
		else {
			this.clctfValue = null;
		}
	}

	@Override
    public Component getTableCellEditorComponent(final JTable tbl, Object oValue, boolean bSelected, final int iRow, final int iColumn) {
		final CollectableComponent clctcomp = this.getCollectableComponent();
		log.debug("getTableCellEditorComponent - row: " + iRow + " - column: " + iColumn + " - component name: " + clctcomp.getFieldName() + " - selected: " + bSelected);

		this.editingRow = iRow;

		clctcomp.getModel().removeCollectableComponentModelListener(this);

		if (this.isSearchable()) {
			clctcomp.getSearchModel().setSearchCondition((CollectableSearchCondition) oValue);
		}
		else {
			clctcomp.getModel().setField((CollectableField) oValue);
		}

		clctcomp.getModel().addCollectableComponentModelListener(this);

		// a datefield gets a inputverifier
		/** @todo this needs to be called only once for the component - move to getCollectableComponent/ctor! */
		/** @todo no input verifier for searchable components? */
		if (!this.isSearchable() && clctcomp instanceof CollectableDateChooser) {
			final CollectableDateChooser clctdatechooser = (CollectableDateChooser) clctcomp;
			clctdatechooser.getDateChooser().getJTextField().setInputVerifier(editorDateInputVerifier);
		}
		if (!this.isSearchable() && clctcomp instanceof CollectableTextField) {
			final CollectableTextField clcttextfield = (CollectableTextField)clctcomp;
			clcttextfield.getJTextComponent().setInputVerifier(floatAndDoubleInputVerifier);
		}
		if (!this.isSearchable() && clctcomp instanceof CollectableComboBox) {
			final CollectableComboBox clctcombobox = (CollectableComboBox)clctcomp;
			((JTextField)clctcombobox.getJComboBox().getEditor().getEditorComponent()).setInputVerifier(trueInputVerifier);
		}
		if (!this.isSearchable() && clctcomp instanceof CollectableListOfValues) {
			final CollectableListOfValues clctlov = (CollectableListOfValues)clctcomp;
			clctlov.getJTextField().setInputVerifier(trueInputVerifier);
		}
		
		JComponent result = this.clctcomp.getJComponent();
		if (result instanceof LabeledComponent) {
			/** @todo find a better solution */
			result = ((LabeledComponent) result).getControlComponent();
		}
		else if (result instanceof JCheckBox) {
			final JCheckBox chkbx = (JCheckBox) result;
			chkbx.setHorizontalAlignment(JCheckBox.CENTER);
			/** @todo setting the colors here doesn't work as bSelected is false when clicking in a nonselected row. */
//			result.setBackground(bSelected ? tbl.getSelectionBackground() : tbl.getBackground());
//			result.setForeground(bSelected ? tbl.getSelectionForeground() : tbl.getForeground());
		}
		
		return result;
	}

	public int getLastEditingRow() {
		return editingRow;
	}

	/**
	 * Verifies the input of the datechooser
	 * It has two states: checkstate = true or false
	 * In this way it is possible to show a ExceptionDialog
	 * (the ExceptionDialog needs the focus to show it self)
	 */
	private final InputVerifier editorDateInputVerifier;

	/**
	 * Verifies the input of the text field with integer or double values
	 * It has two states: checkstate = true or false
	 * In this way it is possible to show a ExceptionDialog
	 * (the ExceptionDialog needs the focus to show it self)
	 */
	private final InputVerifier floatAndDoubleInputVerifier;
	
	/**
	 * Verifies the input of the text field
	 * It has two states: checkstate = true or false
	 * In this way it is possible to show a ExceptionDialog
	 * (the ExceptionDialog needs the focus to show it self)
	 */
	private final InputVerifier trueInputVerifier;

}  // class CollectableComponentTableCellEditor
