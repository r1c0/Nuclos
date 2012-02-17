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
package org.nuclos.common.attribute;

import org.nuclos.common2.CommonLocaleDelegate;

import java.util.NoSuchElementException;

/**
 * Component type of an attribute.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public enum ComponentType {
	TEXTFIELD(CommonLocaleDelegate.getInstance().getMessage("ComponentType.5","Textfeld"), false, true, false),
	DROPDOWN(CommonLocaleDelegate.getInstance().getMessage("ComponentType.1","Auswahlliste"), false, false, false),
	COMBOBOX(CommonLocaleDelegate.getInstance().getMessage("ComponentType.2","ComboBox (editierbar)"), false, true, true),
	LISTOFVALUES(CommonLocaleDelegate.getInstance().getMessage("ComponentType.4","Suchfeld"), true, false, false);
	LOOKUP(CommonLocaleDelegate.getMessage("ComponentType.1","Nachschlagefeld"), true, true, false);

	private final String sLabel;
	private final boolean bSearchable;
	private final boolean bModifiable;
	private final boolean bInsertable;

	private ComponentType(String sLabel, boolean bSearchable, boolean bModifiable, boolean bInsertable) {
		this.sLabel = sLabel;
		this.bSearchable = bSearchable;
		this.bModifiable = bModifiable;
		this.bInsertable = bInsertable;
	}

	private String getLabel() {
		return this.sLabel;
	}

	public boolean isSearchable() {
		return this.bSearchable;
	}

	public boolean isModifiable() {
		return this.bModifiable;
	}

	public boolean isInsertable() {
		return this.bInsertable;
	}

	@Override
	public String toString() {
		return this.getLabel();
	}

	/**
	 * @param bSearchable
	 * @param bModifiable
	 * @param bInsertable
	 * @return the ComponentType with the given combination of flags.
	 * @throws NoSuchElementException if there is no such ComponentType.
	 */
	public static ComponentType findByFlags(boolean bSearchable, boolean bModifiable, boolean bInsertable) {
		for (ComponentType componenttype : ComponentType.values()) {
			if (componenttype.isSearchable() == bSearchable && componenttype.isModifiable() == bModifiable && componenttype.isInsertable() == bInsertable)
			{
				return componenttype;
			}
		}
		throw new NoSuchElementException(CommonLocaleDelegate.getInstance().getMessage(
				"ComponentType.3","Es gibt keinen passenden Komponenten-Typ f\u00fcr diese Kombination."));
	}

}	// enum ComponentType
