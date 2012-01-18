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
package org.nuclos.client.settings;

import javax.swing.DefaultComboBoxModel;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.Localizable;

/**
 * ComboBoxModel for generic KeyEnums.
 *
 * @author thomas.schiffmann
 */
public class KeyEnumComboBoxModel<T extends KeyEnum<?>> extends DefaultComboBoxModel {

	private static final long serialVersionUID = 3615778903043270342L;

	public KeyEnumComboBoxModel(T[] elements) {
		for (T element : elements) {
			addElement(new Element(element));
		}
	}

	public void setSelectedValue(T value) {
		super.setSelectedItem(new Element(value));
	}

	public T getSelectedValue() {
		return ((Element)getSelectedItem()).getValue();
	}

	private class Element {
		private final T value;

		public Element(T value) {
			super();
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		@Override
		public String toString() {
			if (value instanceof Localizable) {
				return CommonLocaleDelegate.getInstance().getText((Localizable)value);
			}
			else {
				return String.valueOf(value.getValue());
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Element other = (Element) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}


	}
}
