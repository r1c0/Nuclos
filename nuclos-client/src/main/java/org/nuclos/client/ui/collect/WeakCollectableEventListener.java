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
package org.nuclos.client.ui.collect;

import java.lang.ref.WeakReference;

import org.nuclos.client.ui.collect.CollectController.CollectableEventListener;
import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.common.collect.collectable.Collectable;

public class WeakCollectableEventListener implements CollectableEventListener {

	private final WeakReference<CollectableEventListener> ref;

	public WeakCollectableEventListener(CollectableEventListener l) {
		super();
		this.ref = new WeakReference<CollectableEventListener>(l);
	}

	@Override
	public void handleCollectableEvent(Collectable collectable, MessageType messageType) {
		if (ref.get() != null) {
			ref.get().handleCollectableEvent(collectable, messageType);
		}
	}

}
