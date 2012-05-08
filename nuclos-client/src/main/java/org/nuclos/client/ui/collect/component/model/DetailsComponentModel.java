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

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;

import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;

/**
 * A <code>CollectableComponentModel</code> for editable (non-searchable) components.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class DetailsComponentModel extends CollectableComponentModel {

	private static final Logger LOG = Logger.getLogger(DetailsComponentModel.class);

	private static class MultiEdit {
		private boolean bValueToBeChanged;
		private CollectableField clctfCommonValue;
	}

	private MultiEdit multiedit;
	
	private boolean mandatory = false;
	
	private boolean mandatoryAdded = false;

	public DetailsComponentModel(CollectableEntityField clctef) {
		super(clctef);

		assert !this.isSearchModel();
		assert !this.isMultiEditable();
	}

	/**
	 * sets the value of this component.
	 * Note that a <code>CollectableComponentEvent</code> is fired even if the model did not change.
	 * That's because there may be an inconsistent <code>CollectableComponent</code> listening to
	 * this. In that case, the component (view) must be updated anyway.
	 * @param clctfValue The field type must match the field type of <code>getEntityField</code>.
	 * @precondition clctfValue != null
	 * @postcondition this.getField().equals(clctfValue)
	 */
	@Override
	public void setField(final CollectableField clctfValue) {
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				try {
					DetailsComponentModel.this.setField(clctfValue, true);
					assert getField().equals(clctfValue) : "field=" + getField() + " value is " + clctfValue;
				}
				catch (Exception e) {
					LOG.error("setField failed: " + e, e);
				}
			}
		});		
	}

	/**
	 * clears the model and notifies the listeners.
	 * @postcondition this.getField().isNull()
	 */
	@Override
	public void clear() {
		this.setField(this.getEntityField().getNullField());
		assert this.getField().isNull();
	}

	@Override
	public boolean isSearchModel() {
		return false;
	}

	/**
	 * @return Is this model multi editable, that means: Can this model be used in a multi edit scenario?
	 */
	@Override
	public boolean isMultiEditable() {
		return this.multiedit != null;
	}

	/**
	 * @param bMultiEditable
	 * @postcondition this.isMultiEditable() <--> bMultiEditable
	 */
	public void setMultiEditable(boolean bMultiEditable) {
		if(bMultiEditable != this.isMultiEditable()) {
			this.multiedit = bMultiEditable ? new MultiEdit() : null;
		}
		assert this.isMultiEditable() == bMultiEditable;
	}

	/**
	 * @return Is the value of this model to be changed?
	 * @precondition this.isMultiEditable()
	 */
	public boolean isValueToBeChanged() {
		if(!this.isMultiEditable()) {
			throw new IllegalStateException("multiEditable");
		}
		return this.multiedit.bValueToBeChanged;
	}

	/**
	 * needed to indicate that the value of a multi editable component model is to be changed,
	 * when there is no common value.
	 * @param bValueToBeChanged Is the value of this model to be changed?
	 * @precondition this.isMultiEditable()
	 * @postcondition this.isValueToBeChanged() <--> bValueToBeChanged
	 */
	public void setValueToBeChanged(boolean bValueToBeChanged) {
		if (!this.isMultiEditable()) {
			throw new IllegalStateException("multiEditable");
		}
		this.multiedit.bValueToBeChanged = bValueToBeChanged;

		LOG.debug("setValueToBeChanged: " + bValueToBeChanged + " - field: " + this.getEntityField().getName());

		this.fireValueToBeChanged(bValueToBeChanged);

		assert this.isValueToBeChanged() == bValueToBeChanged;
	}

	private void fireValueToBeChanged(boolean bValueToBeChanged) {
		getModelHelper().fireValueToBeChanged(this, bValueToBeChanged);
	}

	@Override
	protected void setDirty() {
		if(this.isMultiEditable()) {
			this.setValueToBeChanged(true);
		}
	}

	/**
	 * @return Has this model a common value?
	 * @precondition this.isMultiEditable()
	 */
	public boolean hasCommonValue() {
		if (!this.isMultiEditable()) {
			throw new IllegalStateException("multiEditable");
		}
		return this.multiedit.clctfCommonValue != null;
	}

	/**
	 * @return the common value of this model, if any.
	 * @precondition this.isMultiEditable()
	 * @precondition this.hasCommonValue()
	 */
	public CollectableField getCommonValue() {
		if (!this.isMultiEditable()) {
			throw new IllegalStateException("multiEditable");
		}
		if (!this.hasCommonValue()) {
			throw new IllegalStateException("commonValue");
		}
		return this.multiedit.clctfCommonValue;
	}

	/**
	 * sets the common value of this model.
	 * @param clctfCommonValue
	 * @precondition this.isMultiEditable()
	 * @postcondition this.hasCommonValue();
	 * @postcondition this.getCommonValue().equals(clctfCommonValue);
	 */
	public void setCommonValue(CollectableField clctfCommonValue) {
		if (!this.isMultiEditable()) {
			throw new IllegalStateException("multiEditable");
		}
		if(clctfCommonValue == null) {
			throw new NullArgumentException("clctfCommonValue");
		}
		this.multiedit.clctfCommonValue = clctfCommonValue;

		assert this.hasCommonValue();
		assert this.getCommonValue().equals(clctfCommonValue);
	}

	/**
	 * @postcondition !this.hasCommonValue()
	 */
	public void unsetCommonValue() {
		this.multiedit.clctfCommonValue = null;

		assert !this.hasCommonValue();
	}

	/**
	 * assigns the state of that model to this model.
	 * @param that
	 * @postcondition this.isMultiEditable() <--> that.isMultiEditable()
	 * @postcondition this.isMultiEditable() --> (this.hasCommonValue() <--> that.hasCommonValue())
	 * @postcondition (this.isMultiEditable() && this.hasCommonValue()) --> (this.getCommonValue().equals(that.getCommonValue()))
	 * @postcondition this.getField().equals(that.getField())
	 */
	public void assign(DetailsComponentModel that) {
		this.setMultiEditable(that.isMultiEditable());

		if(this.isMultiEditable()) {
			this.multiedit.clctfCommonValue = that.multiedit.clctfCommonValue;
		}

		this.setFieldInitial(that.getField());

		if(this.isMultiEditable()) {
			this.setValueToBeChanged(that.isValueToBeChanged());
		}
		
		this.setMandatory(that.isMandatory());
		this.setMandatoryAdded(that.isMandatoryAdded());

		assert this.isMultiEditable() == that.isMultiEditable();
		assert !this.isMultiEditable() || (this.hasCommonValue() == that.hasCommonValue());
		assert !(this.isMultiEditable() && this.hasCommonValue()) || (this.getCommonValue().equals(that.getCommonValue()));
		assert this.getField().equals(that.getField());
	}

	/**
	 * @return the mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * @param mandatory the mandatory to set
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * @return the mandatoryAdded
	 */
	public boolean isMandatoryAdded() {
		return mandatoryAdded;
	}

	/**
	 * @param mandatoryAdded the mandatoryAdded to set
	 */
	public void setMandatoryAdded(boolean mandatoryAdded) {
		this.mandatoryAdded = mandatoryAdded;
	}
	
	

}  // class DetailsComponentModel
