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

import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.client.ui.collect.component.model.SearchComponentModel;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;

/**
 * <code>CollectableComponent</code> that delegates all methods to a wrapped <code>CollectableComponent</code> given in the ctor.
 * This is useful to create decorated CollectableComponents.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public abstract class DelegatingCollectableComponent implements CollectableComponent {

	/**
	 * @return the wrapped <code>CollectableComponent</code>.
	 * @postcondition result != null
	 */
	protected abstract CollectableComponent getWrappedCollectableComponent();

	/**
	 * @return the <code>JComponent</code> holding the view. Typically a panel that consists of the wrapped
	 * <code>CollectableComponent</code>'s <code>JComponent</code>, plus some decoration, such as a button or a label etc.
	 */
	@Override
	public abstract JComponent getJComponent();

	@Override
	public JComponent getControlComponent() {
		return this.getWrappedCollectableComponent().getControlComponent();
	}

	@Override
	public JComponent getFocusableComponent() {
		return this.getWrappedCollectableComponent().getFocusableComponent();
	}

	@Override
	public CollectableEntityField getEntityField() {
		return this.getWrappedCollectableComponent().getEntityField();
	}

	@Override
	public String getFieldName() {
		return this.getWrappedCollectableComponent().getFieldName();
	}

	@Override
	public CollectableField getField() throws CollectableFieldFormatException {
		return this.getWrappedCollectableComponent().getField();
	}

	@Override
	public void setField(CollectableField clctfValue) {
		this.getWrappedCollectableComponent().setField(clctfValue);
	}

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		return this.getWrappedCollectableComponent().getFieldFromView();
	}

	@Override
	public void clear() {
		this.getWrappedCollectableComponent().clear();
	}

	@Override
	public CollectableComponentModel getModel() {
		return this.getWrappedCollectableComponent().getModel();
	}

	/**
	 * @deprecated Use constructor to initialize the model. 
	 * 		The model itself shouldn't be changed after construction of the view.
	 */
	@Override
	public void setModel(CollectableComponentModel clctcompmodel) {
		this.getWrappedCollectableComponent().setModel(clctcompmodel);
	}

	@Override
	public SearchComponentModel getSearchModel() {
		return this.getWrappedCollectableComponent().getSearchModel();
	}

	@Override
	public DetailsComponentModel getDetailsModel() {
		return this.getWrappedCollectableComponent().getDetailsModel();
	}

	@Override
	public CollectableSearchCondition getSearchCondition() throws CollectableFieldFormatException {
		return this.getWrappedCollectableComponent().getSearchCondition();
	}

	@Override
	public boolean canDisplay(CollectableSearchCondition cond) {
		return this.getWrappedCollectableComponent().canDisplay(cond);
	}

	@Override
	public void setCollectableEntity(CollectableEntity clcte) {
		this.getWrappedCollectableComponent().setCollectableEntity(clcte);
	}

	@Override
	public boolean isConsistent() {
		return this.getWrappedCollectableComponent().isConsistent();
	}

	@Override
	public boolean isSearchComponent() {
		return this.getWrappedCollectableComponent().isSearchComponent();
	}

	@Override
	public boolean isDetailsComponent() {
		return this.getWrappedCollectableComponent().isDetailsComponent();
	}

	@Override
	public boolean isMultiEditable() {
		return this.getWrappedCollectableComponent().isMultiEditable();
	}

	@Override
	public void makeConsistent() throws CollectableFieldFormatException {
		this.getWrappedCollectableComponent().makeConsistent();
	}

	@Override
	public ReferencingListener getReferencingListener() {
		return this.getWrappedCollectableComponent().getReferencingListener();
	}

	@Override
	public void setColumns(int iColumns) {
		this.getWrappedCollectableComponent().setColumns(iColumns);
	}

	@Override
	public void setRows(int iRows) {
		this.getWrappedCollectableComponent().setRows(iRows);
	}

	@Override
	public void setEnabled(boolean bEnabled) {
		this.getJComponent().setEnabled(bEnabled);
		this.getWrappedCollectableComponent().setEnabled(bEnabled);
	}

	@Override
	public boolean isEnabledByInitial() {
		return this.getWrappedCollectableComponent().isEnabledByInitial();
	}

	@Override
	public void setEnabledByInitial(boolean bEnabled) {
		this.getWrappedCollectableComponent().setEnabledByInitial(bEnabled);
	}

	@Override
	public void setOpaque(boolean bOpaque) {
		this.getJComponent().setOpaque(bOpaque);
		this.getWrappedCollectableComponent().setOpaque(bOpaque);
	}

	@Override
	public void setVisible(boolean bVisible) {
		this.getJComponent().setVisible(bVisible);
		this.getWrappedCollectableComponent().setVisible(bVisible);
	}

	@Override
	public void setToolTipText(String sToolTipText) {
		this.getJComponent().setToolTipText(sToolTipText);
		this.getWrappedCollectableComponent().setToolTipText(sToolTipText);
	}

	@Override
	public void setLabelText(String sLabel) {
		this.getWrappedCollectableComponent().setLabelText(sLabel);
	}

	@Override
	public void setMnemonic(char c) {
		this.getWrappedCollectableComponent().setMnemonic(c);
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		this.getWrappedCollectableComponent().setInsertable(bInsertable);
	}

	@Override
	public void setFillControlHorizontally(boolean bFill) {
		/** @todo ? */
	}

	@Override
	public Preferences getPreferences() {
		return this.getWrappedCollectableComponent().getPreferences();
	}

	@Override
	public void setPreferences(Preferences prefs) {
		this.getWrappedCollectableComponent().setPreferences(prefs);
	}

	@Override
	public TableCellRenderer getTableCellRenderer(boolean subform) {
		return this.getWrappedCollectableComponent().getTableCellRenderer(subform);
	}

	@Override
	public Object getProperty(String sName) {
		return this.getWrappedCollectableComponent().getProperty(sName);
	}

	@Override
	public void setProperty(String sName, Object oValue) {
		this.getWrappedCollectableComponent().setProperty(sName, oValue);
	}

	@Override
	public Map<String, Object> getProperties() {
		return this.getWrappedCollectableComponent().getProperties();
	}

	/**
	 * CollectableComponents are equal iff they are identical. This behavior may not be changed by subclasses.
	 * @param o
	 * @postcondition result == (this == o)
	 */
	@Override
	public final boolean equals(Object o) {
		// Note that "result = this.getWrappedCollectableComponent().equals(o)" doesn't work here!
		final boolean result = super.equals(o);
		assert result == (this == o);
		return result;
	}

	/**
	 * @see #equals(Object)
	 */
	@Override
	public final int hashCode() {
		return super.hashCode();
	}

}  // class DelegatingCollectableComponent
