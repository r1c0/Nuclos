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
package org.nuclos.client.ui.collect.detail;

import java.lang.ref.WeakReference;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.CollectController.CollectableEventListener;
import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * CollectableEventListener for refreshing the current collectable of the CollectController, that opened another ColelctController
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	thomas.schiffmann
 * @version 01.00.00
 */
public class DetailsCollectableEventListener extends CollectStateAdapter implements CollectableEventListener {

	private static final Logger LOG = Logger.getLogger(CollectController.class);

	private final WeakReference<CollectController<?>> sourceController;
	private final WeakReference<CollectController<?>> targetController;

	public DetailsCollectableEventListener(CollectController<?> sourceController, CollectController<?> targetController) {
		this.sourceController = new WeakReference<CollectController<?>>(sourceController);
		this.targetController = new WeakReference<CollectController<?>>(targetController);
		sourceController.getCollectStateModel().addCollectStateListener(this);
		targetController.getCollectStateModel().addCollectStateListener(this);
	}

	@Override
	public void handleCollectableEvent(Collectable collectable, MessageType messageType) {
		CollectController<?> ctlsource = sourceController.get();
		if (ctlsource != null) {
			if (!messageType.equals(MessageType.REFRESH_DONE)) {
				try {
					if (!ctlsource.changesArePending()) {
						ctlsource.refreshCurrentCollectable();
					}
	            }
	            catch(CommonBusinessException e) {
	            	LOG.error("handleCollectableEvent failed: " + e, e);
	            }
			}
		}
		else {
			CollectController<?> ctltarget = targetController.get();
			if (ctltarget != null) {
				ctltarget.removeCollectableEventListener(this);
				ctltarget.getCollectStateModel().removeCollectStateListener(this);
			}
		}
	}

	@Override
    public void detailsModeLeft(CollectStateEvent ev) throws CommonBusinessException {
		if (ev.hasOuterStateChanged()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					CollectController<?> ctlsource = sourceController.get();
					CollectController<?> ctltarget = targetController.get();
					if (ctlsource != null) {
						ctlsource.getCollectStateModel().removeCollectStateListener(DetailsCollectableEventListener.this);
					}
				    if (ctltarget != null) {
				    	ctltarget.getCollectStateModel().removeCollectStateListener(DetailsCollectableEventListener.this);
				    	ctltarget.removeCollectableEventListener(DetailsCollectableEventListener.this);
				    }
				}
			});
		}
    }

}
