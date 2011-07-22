//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect.search;

import java.util.List;
import java.util.Observable;

import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ProxyList;

/**
 * A specialization of SearchWorker to use with GenericObjectCollectController.
 * 
 * @since Nuclos 3.1.01 this is a top level class.
 * @author Thomas Pasch (refactoring)
 */
public final class GenericObjectObservableSearchWorker extends Observable implements SearchWorker<CollectableGenericObjectWithDependants> {
	
	private final GenericObjectCollectController cc;
	
	public GenericObjectObservableSearchWorker(GenericObjectCollectController cc) {
		this.cc = cc;
	}
	
	@Override
	public void startSearch() throws CommonBusinessException {
		/** @todo maybe this could be done already in the CollectController? */
		final GenericObjectCollectController cc = getGenericObjectCollectController();
		cc.makeConsistent(true);
		cc.removePreviousChangeListenersForResultTableVerticalScrollBar();
	}

	@Override
	public ProxyList<CollectableGenericObjectWithDependants> getResult() throws CommonBusinessException {
		final GenericObjectCollectController cc = getGenericObjectCollectController();
		return cc.getSearchStrategy().getSearchResult();
	}

	@Override
	public void finishSearch(List<CollectableGenericObjectWithDependants> lstclctResult) {
		final GenericObjectCollectController cc = getGenericObjectCollectController();
		cc.getSearchStrategy().setCollectableProxyList((ProxyList<CollectableGenericObjectWithDependants>) lstclctResult);
		cc.fillResultPanel(lstclctResult, lstclctResult.size(), false);
		cc.setupChangeListenerForResultTableVerticalScrollBar();
		super.setChanged();
		super.notifyObservers("search finished");
	}
	
	private GenericObjectCollectController getGenericObjectCollectController() {
		return cc;
	}
}

