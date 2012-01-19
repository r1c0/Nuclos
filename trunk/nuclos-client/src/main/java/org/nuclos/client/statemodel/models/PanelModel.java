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
package org.nuclos.client.statemodel.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

/**
 * Panel model for state machine.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class PanelModel implements Serializable {

	protected Vector<PropertyChangeListener> listener = new Vector<PropertyChangeListener>();

	public PanelModel() {
	}

	public PanelModel(PropertyChangeListener changeListener) {
		listener.add(changeListener);
	}

	public void addChangeListener(PropertyChangeListener changeListener) {
		listener.add(changeListener);
	}

	public void removeChangeListener(PropertyChangeListener changeListener) {
		listener.remove(changeListener);
	}

	public void removeAllListener() {
		listener.removeAllElements();
	}

	public void firePropertyChangeEvent(Object source, String name, String oldValue, String newValue) {
		for (Iterator<PropertyChangeListener> i = listener.iterator(); i.hasNext();) {
			PropertyChangeListener changeListener = i.next();
			changeListener.propertyChange(new PropertyChangeEvent(source, name, oldValue, newValue));
		}
	}
}
