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

import javax.swing.JComponent;

/**
 * Event occuring on a <code>CollectableComponent</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CollectableComponentEvent extends java.util.EventObject {

	/**
	 * @param clctcompSource the <code>CollectableComponent</code> that triggered this event.
	 * @precondition clctcompSource != null
	 */
	public CollectableComponentEvent(CollectableComponent clctcompSource) {
		super(clctcompSource);
	}
	
	/**
	 * Needed for LookupEvent in object generation (working step) with parameter object.
	 */
	protected CollectableComponentEvent(JComponent comp) {
		super(comp);
	}

	/**
	 * @return the <code>CollectableComponent</code> that triggered this event.
	 * @postcondition result != null
	 */
	public CollectableComponent getCollectableComponent() {
		return (CollectableComponent) this.getSource();
	}

}  // class CollectableComponentEvent
