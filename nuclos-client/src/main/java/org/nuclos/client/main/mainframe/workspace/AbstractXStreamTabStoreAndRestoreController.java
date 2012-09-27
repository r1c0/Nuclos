//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.client.main.mainframe.workspace;

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.common2.XStreamSupport;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @author Thomas Pasch
 */
public abstract class AbstractXStreamTabStoreAndRestoreController<T> implements ITabStoreController, ITabRestoreController {
	
	protected AbstractXStreamTabStoreAndRestoreController() {
	}

	@Override
	public void restoreFromPreferences(String preferencesXML, MainFrameTab tab) throws Exception {
		if (preferencesXML != null) {
			final XStream xstream = XStreamSupport.getInstance().getXStream();
			final T state = (T) xstream.fromXML(preferencesXML);
			restoreFromState(state, tab);
		}
	}

	@Override
	public String getPreferencesXML() {
		final T state = getState();
		final XStream xstream = XStreamSupport.getInstance().getXStream();
		return xstream.toXML(state);
	}
	
	protected abstract void restoreFromState(T state, MainFrameTab tab) throws Exception;
	
	protected abstract T getState();
}
