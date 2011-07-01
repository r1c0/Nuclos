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
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;

/**
 * Component that displays and lets the user edit a <code>CollectableField</code>.
 * Contains all properties that are common to all <code>CollectableComponents</code>.
 * Some properties are ignored by some components.
 * @invariant this.isDetailsComponent() <--> !this.isSearchComponent()
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public interface CollectableComponent {

	/**
	 * @return the encapsulated <code>JComponent</code>
	 * @postcondition result != null
	 */
	JComponent getJComponent();

	/**
	 * @return the meta information about the field to be collected in this component.
	 * @postcondition result != null
	 */
	CollectableEntityField getEntityField();

	/**
	 * @return the name of the <code>CollectableField</code> this component presents
	 */
	String getFieldName();

	/**
	 * @return Is this component to be used to specify a search criteria? That is:
	 * Is this component to be used in a "Search" panel (as opposed to a "Details" panel)?
	 */
	boolean isSearchComponent();

	/**
	 * @return Is this component to be used to display/enter values? That is:
	 * Is this component to be used in a "Details" panel (as opposed to a "Search" panel)?
	 */
	boolean isDetailsComponent();

	/**
	 * @return Is this component to be used to display/enter multiple values? That is:
	 * Is this component to be used in a "Details" panel (as opposed to a "Search" panel)
	 * in "multi edit" mode?
	 */
	boolean isMultiEditable();

	/**
	 * @return Is this component enabled by initial? That is:
	 * Is this component enabled or disabled e.g. by layoutml.
	 */
	boolean isEnabledByInitial();

	/**
	 * @return the model of this component
	 */
	CollectableComponentModel getModel();

	/**
	 * @return the Search model of this (Search) component
	 * @precondition this.isSearchComponent()
	 */
	SearchComponentModel getSearchModel();

	/**
	 * @return the Details model of this (Details) component
	 * @precondition this.isDetailsComponent()
	 */
	DetailsComponentModel getDetailsModel();

	/**
	 * sets the model of this component.
	 * @param clctcompmodel
	 * @deprecated
	 * @todo Use constructor to initialize the model. The model itself shouldn't be changed after construction of the view.
	 */
	@Deprecated
	void setModel(CollectableComponentModel clctcompmodel);

	/**
	 * @return Is the model consistent with the view?
	 */
	boolean isConsistent();

	/**
	 * If the model is different from the view, updates the model with the value in the view.
	 * If the value in the view is invalid, the model is cleared, and a CollectableFieldFormatException is thrown.
	 * The view is locked during the execution of this method, to prevent recursive updates
	 * between view and model.
	 * @throws CollectableFieldFormatException if the value in the view is invalid.
	 * @postcondition isConsistent()
	 */
	void makeConsistent() throws CollectableFieldFormatException;

	/**
	 * makes the model consistent with the view by calling makeConsistent() and returns the field from the model.
	 * @return For a <code>NULL</code> value, <code>result.isNull()</code> is true.
	 * @postcondition result != null
	 */
	CollectableField getField() throws CollectableFieldFormatException;

	/**
	 * sets the value of this component. To be more precise: sets the value of this component's model.
	 * Fires a <code>CollectableComponentModelEvent</code> if the value has changed.<br>
	 * Note that there is NO postcondition <code>this.getField().equals(clctfValue)</code>
	 * as this is not possible for all components, esp. when setting "" in text fields.
	 * @param clctfValue contains the value.
	 * For a <code>NULL</code> value, <code>clctf.isNull()</code> must be true.
	 * @see CollectableEntityField#getNullField()
	 * @precondition clctf != null
	 */
	void setField(CollectableField clctfValue);

	/**
	 * @return the value (as a CollectableField) from the view. The model isn't changed.
	 * @throws CollectableFieldFormatException if the value in the view cannot be parsed.
	 */
	CollectableField getFieldFromView() throws CollectableFieldFormatException;

	/**
	 * clears the field by calling <code>this.getModel().clear()</code>.
	 * @postcondition this.getField().isNull()
	 * @postcondition this.isSearchComponent() -> (this.getSearchCondition() == null)
	 */
	void clear();

	/**
	 * shows or hides this component.
	 * @param bVisible
	 */
	void setVisible(boolean bVisible);

	/**
	 * enables this component, that is makes it accessible (or not).
	 * @param bEnabled
	 */
	void setEnabled(boolean bEnabled);
	
	/**
	 * enables this component, that is makes it accessible (or not).
	 * @param bEnabled
	 */
	void setScalable(boolean bScalable);

	/**
	 * enables this component by initial, that is makes it accessible (or not).
	 * @param bEnabled
	 */
	void setEnabledByInitial(boolean bEnabled);

	/**
	 * makes the component insertable (or not). At the moment, this applies for comboboxes only.
	 * @todo rename to setRestrictedToValueList
	 * @param bInsertable Can new values (apart from this component's given list of values) be inserted?
	 */
	void setInsertable(boolean bInsertable);

	/**
	 * sets the text for the contained label, if any.
	 * @param sLabel
	 */
	void setLabelText(String sLabel);

	/**
	 * sets the mnemonic for this component (or a contained label), if applicable.
	 * @param c
	 */
	void setMnemonic(char c);

	/**
	 * sets the number of columns for this component, if applicable.
	 * @param iColumns
	 */
	void setColumns(int iColumns);

	/**
	 * sets the number of rows for this component, if reasonable.
	 * Note that this doesn't apply to comboboxes.
	 * @param iRows
	 */
	void setRows(int iRows);

	/**
	 * sets the tooltip text for this component.
	 * @param sToolTipText
	 */
	void setToolTipText(String sToolTipText);

	/**
	 * makes the component opaque (or transparent).
	 * @param bOpaque
	 */
	void setOpaque(boolean bOpaque);

	/**
	 * @param bFill Fill the control horizontally? If false, the control is right aligned.
	 */
	void setFillControlHorizontally(boolean bFill);

	/**
	 * @return this <code>CollectableComponent</code>'s control component, that is the component
	 * that contains the value.
	 */
	JComponent getControlComponent();

	/**
	 * @return this <code>CollectableComponent</code>'s focusable component, that is the component
	 * that gains or looses the input focus.
	 */
	JComponent getFocusableComponent();

	/**
	 * makes the model consistent with the view by calling makeConsistent() and returns the search condition from the model.
	 * @return the search condition contained in this component.
	 * @precondition this.isSearchComponent()
	 * @throws CollectableFieldFormatException if this component's field contains a bad value.
	 * @todo return AtomicCollectableSearchCondition?
	 */
	CollectableSearchCondition getSearchCondition() throws CollectableFieldFormatException;

	/**
	 * @param cond
	 * @return Can this component display the given condition?
	 */
	boolean canDisplay(CollectableSearchCondition cond);

	/**
	 * sets the collectable entity that this.getEntityField() belongs to.
	 * Needed to display/edit a <code>ComparisonWithOtherField</code> in this component.
	 * @param clcte May be <code>null</code>.
	 * @precondition clcte != null --> clcte.getFieldNames().contains(this.getEntityField().getName())
	 */
	void setCollectableEntity(CollectableEntity clcte);

	/**
	 * @precondition this.isReferencing()
	 * @return the referencing listener, if any, for this component.
	 */
	ReferencingListener getReferencingListener();

	/**
	 * Some components (eg. CollectableFileChooser) need to read/write preferences. These can be set here.
	 * The implementation of this method is optional, that is the component may do nothing if it doesn't
	 * need preferences.
	 * @param prefs
	 */
	void setPreferences(Preferences prefs);

	/**
	 * @return the <code>Preferences</code> (if any) for this component.
	 */
	Preferences getPreferences();

	/**
	 * @return a TableCellRenderer to paint this component inside a table cell (for display only, not for editing).
	 * @todo refactor Currently, you have to create a CollectableComponent first in order to call getTableCellRenderer().
	 * At least, provide a static method that performs these two steps.
	 */
	TableCellRenderer getTableCellRenderer();

	/**
	 * @param sName
	 * @return the value of the dynamic property with the given name, if any.
	 */
	Object getProperty(String sName);

	/**
	 * sets the dynamic property with the given name to the given value.
	 * Properties can be used to customize an individual <code>CollectableComponent</code>.
	 * @param sName
	 * @param oValue
	 * @postcondition LangUtils.equals(this.getProperty(sName), oValue)
	 */
	void setProperty(String sName, Object oValue);

	/**
	 * @return the Map of properties for this <code>CollectableComponent</code>.
	 * @postcondition result != null
	 */
	public Map<String, Object> getProperties();

	/**
	 * CollectableComponents are equal iff they are identical. This behavior may not be changed by subclasses.
	 * @param o
	 * @postcondition result == (this == o)
	 */
	@Override
    public boolean equals(Object o);

	/**
	 * @see #equals(Object)
	 */
	@Override
    public int hashCode();

	/**
	 * Listens to events generated by components that contain a reference to another entity ("foreign key fields").
	 */
	public interface ReferencingListener {

		/**
		 * shows the details of the object referenced by the component given in the event.
		 * @param ev
		 */
		void showDetails(CollectableComponentEvent ev);

	}	// interface ReferencingListener

	/**
	 * Transformer: GetModel
	 */
	public static class GetModel implements Transformer<CollectableComponent, CollectableComponentModel> {
		@Override
        public CollectableComponentModel transform(CollectableComponent clctcomp) {
			return clctcomp.getModel();
		}
	}

	/**
	 * Predicate: CanDisplay
	 */
	public static class CanDisplay implements Predicate<CollectableComponent> {
		private final AtomicCollectableSearchCondition atomiccond;

		public CanDisplay(AtomicCollectableSearchCondition atomiccond) {
			this.atomiccond = atomiccond;
		}

		@Override
        public boolean evaluate(CollectableComponent clctcomp) {
			return clctcomp.canDisplay(atomiccond);
		}
	}	

}  // class CollectableComponent
