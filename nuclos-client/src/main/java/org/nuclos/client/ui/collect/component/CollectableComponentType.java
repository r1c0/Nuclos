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

/**
 * The type of a CollectableComponent can be given as either an enumerated control type (constant) or as
 * a Java class. This class encapsulates the two.
 * @todo naming ("CollectableComponentType" vs. "ControlType")
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 * @invariant (this.getEnumeratedControlType() == null) || (this.getControlTypeClass() == null)
 */
public class CollectableComponentType {

	private final Integer iEnumeratedControlType;

	private final Class<CollectableComponent> clsclctcomp;

	/**
	 * @param iEnumeratedControlType may be given as a hint which component to create. May be <code>null</code>.
	 * @param clsclctcomp the class of the component to create. May be <code>null</code>.
	 * @precondition (iEnumeratedControlType == null) || (clsclctcomp == null)
	 * @todo replace this ctor with one for each case?
	 */
	public CollectableComponentType(Integer iEnumeratedControlType, Class<CollectableComponent> clsclctcomp) {
		if (!((iEnumeratedControlType == null) || (clsclctcomp == null))) {
			throw new IllegalArgumentException("iEnumeratedControlType and clscltcomp could not be at the same time specified.");//"iEnumeratedControlType und clscltcomp k\u00f6nnen nicht gleichzeitig angegeben werden."
		}
		this.iEnumeratedControlType = iEnumeratedControlType;
		this.clsclctcomp = clsclctcomp;
	}

	public Integer getEnumeratedControlType() {
		return this.iEnumeratedControlType;
	}

	public Class<CollectableComponent> getControlTypeClass() {
		return this.clsclctcomp;
	}

}  // class CollectableComponentType
