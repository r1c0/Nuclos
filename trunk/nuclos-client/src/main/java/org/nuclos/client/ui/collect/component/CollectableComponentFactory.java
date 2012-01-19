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

import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * Factory for <code>CollectableComponent</code>s. This is used to create <code>CollectableComponent</code>s
 * in the framework.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public abstract class CollectableComponentFactory {

	private static CollectableComponentFactory singleton;

	protected CollectableComponentFactory() {
		// do nothing
	}

	/**
	 * sets the global instance of the <code>CollectableComponentFactory</code>. A custom factory can be used to create
	 * custom <code>CollectableComponent</code>s in the framework.
	 * @param factory
	 */
	public static synchronized void setInstance(CollectableComponentFactory factory) {
		singleton = factory;
	}

	/**
	 * @return the global instance of the <code>CollectableComponentFactory</code>
	 */
	public static synchronized CollectableComponentFactory getInstance() {
		if (singleton == null) {
			singleton = new DefaultCollectableComponentFactory();
		}
		return singleton;
	}

	/**
	 * creates a <code>CollectableComponent</code> for the given entity field. The "control type" may be given as a hint.
	 * This method can be overridden so the resulting component can be given the ability to display a {@CollectableComparisonWithOtherField}.
	 * @param clcte the field's entity
	 * @param sFieldName the field's name
	 * @param clctcomptype may be <null>, which has the same effect as new CollectableComponentType(null, null).
	 * @return a suitable <code>CollectableComponent</code> for the given entity field.
	 * @precondition clctef != null
	 */
	public CollectableComponent newCollectableComponent(CollectableEntity clcte,
			String sFieldName, CollectableComponentType clctcomptype, boolean bSearchable) {
		return newCollectableComponent(clcte.getEntityField(sFieldName), clctcomptype, bSearchable);
	}

	/**
	 * creates a <code>CollectableComponent</code> for the given entity field. The "control type" may be given as a hint.
	 * @param clctef
	 * @param clctcomptype may be <null>, which has the same effect as new CollectableComponentType(null, null).
	 * @return a suitable <code>CollectableComponent</code> for the given entity field.
	 * @precondition clctef != null
	 */
	public abstract CollectableComponent newCollectableComponent(CollectableEntityField clctef,
			CollectableComponentType clctcomptype, boolean bSearchable);

}  // class CollectableComponentFactory
