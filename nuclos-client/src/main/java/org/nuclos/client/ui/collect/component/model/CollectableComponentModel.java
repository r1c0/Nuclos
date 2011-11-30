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
package org.nuclos.client.ui.collect.component.model;

import java.util.Date;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.NuclosImage;

/**
 * Model for a <code>CollectableComponent</code>. A model can be shared by more than one
 * <code>CollectableComponent</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public abstract class CollectableComponentModel {

	protected final CollectableComponentModelHelper helper = new CollectableComponentModelHelper();
	private CollectableEntityField clctef;
	private CollectableField clctfValue;
	
	private boolean isInitializing = false;

	/**
	 * package private ctor. Use <code>newCollectableComponentModel()</code> to create an instance of this class.
	 * @param clctef the <code>CollectableEntityField</code> describing the field this model represents.
	 */
	CollectableComponentModel(CollectableEntityField clctef) {
		this.clctef = clctef;
		this.clctfValue = clctef.getNullField();
	}

	/**
	 * @param clctef the <code>CollectableEntityField</code> describing the field this model represents.
	 * @param bSearchable Is this component to be used to enter a search condition?
	 * @return a new <code>CollectableComponentModel</code>.
	 * @postcondition result.isSearchModel() <--> bSearchable
	 */
	public static CollectableComponentModel newCollectableComponentModel(CollectableEntityField clctef, boolean bSearchable) {
		final CollectableComponentModel result;
		if (bSearchable) {
			result = new SearchComponentModel(clctef);
		}
		else {
			result = new DetailsComponentModel(clctef);
		}
		assert result.isSearchModel() == bSearchable;

		return result;
	}

	/**
	 * @return the <code>CollectableEntityField</code> describing the field this model represents.
	 */
	public CollectableEntityField getEntityField() {
		return this.clctef;
	}

	/**
	 * @return the name of this field. Shortcut for <code>this.getEntityField().getName()</code>.
	 */
	public String getFieldName() {
		return this.clctef.getName();
	}

	/**
	 * @return Is this component to be used to enter a search condition?
	 */
	public abstract boolean isSearchModel();

	/**
	 * @return Is this component to be used to enter/display a value?
	 */
	public final boolean isDetailsModel() {
		return !this.isSearchModel();
	}

	/**
	 * @return Is this component to be used to enter/display a value in a multiedit scenario?
	 */
	public abstract boolean isMultiEditable();

	/**
	 * @return the value of this component.
	 * @postcondition result != null
	 */
	public CollectableField getField() {
		return this.clctfValue;
	}

	/**
	 * sets the "collectableField" (the composite value) of this component.
	 * Note that a <code>CollectableComponentEvent</code> is fired even if the model did not change.
	 * That's because there may be an inconsistent <code>CollectableComponent</code> listening to
	 * this. In that case, the component (view) must be updated anyway.
	 * @todo This could be changed (maybe this would mean an optimization) in the future. getField() should
	 * throw an InconsistentStateException rather than return the old value if there is an inconsistent view. Only
	 * then we would have to notify the listeners even if the value did not change.
	 * @param clctfValue The field type must match the field type of <code>getEntityField</code>.
	 * @postcondition this.getField().equals(clctfValue)
	 */
	public abstract void setField(CollectableField clctfValue);
	
	public void setFieldInitial(CollectableField clctValue) {
		isInitializing = true;
		setField(clctValue);
		isInitializing = false;
	}

	/**
	 * sets the "collectableField" (the composite value) of this component and optionally notifies the listeners.
	 * Note that a <code>CollectableComponentEvent</code> is fired even if the model did not change.
	 * That's because there may be an inconsistent <code>CollectableComponent</code> listening to
	 * this. In that case, the component (view) must be updated anyway.
	 * @todo @see #setField(CollectableField clctfValue)
	 * @param clctfValue The field type must match the field type of <code>getEntityField</code>.
	 * @param bNotifyListeners Are listeners to be notified?
	 * @precondition clctfValue != null
	 * @postcondition this.getField().equals(clctfValue)
	 */
	void setField(CollectableField clctfValue, boolean bNotifyListeners) {
		this.setField(clctfValue, bNotifyListeners, false);
	}
	
	public void setFieldInitial(CollectableField clctValue, boolean bNotifyListeners) {
		isInitializing = true;
		setField(clctValue, bNotifyListeners, false);
		isInitializing = false;
	}

	/**
	 * sets the "collectableField" (the composite value) of this component and optionally notifies the listeners.
	 * Note that a <code>CollectableComponentEvent</code> is fired even if the model did not change.
	 * That's because there may be an inconsistent <code>CollectableComponent</code> listening to
	 * this. In that case, the component (view) must be updated anyway.
	 * @todo @see #setField(CollectableField clctfValue)
	 * @param clctfValue The field type must match the field type of <code>getEntityField</code>.
	 * @param bNotifyListeners Are listeners to be notified?
	 * @precondition clctfValue != null
	 * @postcondition this.getField().equals(clctfValue)
	 */
	void setField(CollectableField clctfValue, boolean bNotifyListeners, boolean bDirty) {
		if (clctfValue == null) {
			throw new NullArgumentException("clctfValue");
		}
		if (!bDirty) {
			try {
				CollectableUtils.validateFieldType(clctfValue, this.getEntityField());
				if (!(clctfValue instanceof CollectableValueIdField)) {
					if (this.getEntityField().getJavaClass().equals(Date.class) && clctfValue.getValue() != null && 
						clctfValue.getValue().equals(RelativeDate.today().toString())) {
						//ok
					}
					else if(this.getEntityField().getJavaClass().isAssignableFrom(NuclosImage.class)) {
						// special behavior
					}
					else {
						CollectableUtils.validateValueClass(clctfValue, this.getEntityField());
					}
				}
			}
			catch (CollectableFieldValidationException ex) {
				throw new CommonFatalException(ex);
			}
		}
		final CollectableField clctfOldValue = this.clctfValue;
		this.clctfValue = clctfValue;

		this.setDirty();

		if (bNotifyListeners) {
			this.fireFieldChanged(clctfOldValue, clctfValue);
		}
		assert this.getField().equals(clctfValue);
	}
	
	public void setFieldInitial(CollectableField clctfValue, boolean bNotifyListeners, boolean bDirty) {
		isInitializing = true;
		setField(clctfValue, bNotifyListeners, bDirty);
		isInitializing = false;
	}

	/**
	 * called when the value (the CollectableField) of this model is changed.
	 */
	protected void setDirty() {
		// default: do nothing
	}

	/**
	 * clears the model and notifies the listeners.
	 * @postcondition this.getField().isNull()
	 */
	public abstract void clear();

	/**
	 * adds a listener that gets notified every time the value of this model changes.
	 * @param listener
	 */
	public void addCollectableComponentModelListener(CollectableComponentModelListener listener) {
		this.helper.addCollectableComponentModelListener(listener);
	}

	/**
	 * @see #addCollectableComponentModelListener
	 * @param listener
	 */
	public void removeCollectableComponentModelListener(CollectableComponentModelListener listener) {
		this.helper.removeCollectableComponentModelListener(listener);
	}

	void fireFieldChanged(CollectableField clctfOldValue, CollectableField clctfNewValue) {
		helper.fireCollectableFieldChanged(this, clctfOldValue, clctfNewValue);
	}

	/**
	 * Predicate HasFieldName
	 */
	public static class HasFieldName implements Predicate<CollectableComponentModel> {
		private final String sFieldName;

		public HasFieldName(String sFieldName) {
			this.sFieldName = sFieldName;
		}

		@Override
		public boolean evaluate(CollectableComponentModel clctcompmodel) {
			return clctcompmodel.getFieldName().equals(sFieldName);
		}
	}
	
	public boolean isInitializing() {
		return isInitializing;
	}
	
	@Override
	public String toString() {
		final ToStringBuilder result = new ToStringBuilder(this).append(clctef).append(clctfValue);
		return result.toString();
	}

}  // class CollectableComponentModel
