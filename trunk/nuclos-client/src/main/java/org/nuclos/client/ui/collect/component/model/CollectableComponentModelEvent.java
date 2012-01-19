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

import javax.swing.event.ChangeEvent;

import org.nuclos.common.collect.collectable.CollectableField;

/**
 * An event that may occur on a <code>CollectableComponentModel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class CollectableComponentModelEvent extends ChangeEvent {

	private final CollectableField clctfOldValue;

	private final CollectableField clctfNewValue;

	/**
	 * @param source
	 * @param clctfOldValue
	 * @param clctfNewValue
	 */
	public CollectableComponentModelEvent(CollectableComponentModel source, CollectableField clctfOldValue, CollectableField clctfNewValue) {
		super(source);
		this.clctfOldValue = clctfOldValue;
		this.clctfNewValue = clctfNewValue;
	}

	public CollectableComponentModel getCollectableComponentModel() {
		return (CollectableComponentModel) this.getSource();
	}

	public CollectableField getOldValue() {
		final CollectableField result = this.clctfOldValue;
		if(result == null) {
			throw new IllegalStateException();
		}
		return result;
	}

	public CollectableField getNewValue() {
		final CollectableField result = this.clctfNewValue;
		if (result == null) {
			throw new IllegalStateException();
		}
		return result;
	}

	/**
	 * CollectableComponentModelEvents may be delivered even if the value did not change, that is
	 * the old value is equal to the new value. This method can be used to determine if the value
	 * has changed.
	 * @return Is the new value different from the old value?
	 */
	public boolean collectableFieldHasChanged() {
		if (clctfOldValue == null) {
			return clctfNewValue != null;
		} else {
			return !clctfOldValue.equals(clctfNewValue, false);
		}
	}
	
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("CollectableComponentModelEvent[old=").append(clctfOldValue);
		result.append(", new=").append(clctfNewValue);
		result.append("]");
		return result.toString();
	}

}  // class CollectableComponentModelEvent
