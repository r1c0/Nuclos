package org.nuclos.client.ui.collect.search;

import java.util.List;
import java.util.Observable;

import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ProxyList;

/**
 * A specialization of SearchWorker to use with MasterDataCollectController.
 * 
 * @since Nuclos 3.1.01 this is a top level class.
 * @author Thomas Pasch (refactoring)
 */
public class MasterDataObservableSearchWorker extends Observable implements
		SearchWorker<CollectableMasterDataWithDependants> {
	
	private final MasterDataCollectController cc;

	public MasterDataObservableSearchWorker(MasterDataCollectController cc) {
		this.cc = cc;
	}

	@Override
	public void startSearch() throws CommonBusinessException {
		final MasterDataCollectController cc = getMasterDataCollectController();
		cc.makeConsistent(true);
		cc.removePreviousChangeListenersForResultTableVerticalScrollBar();
	}

	@Override
	public ProxyList<CollectableMasterDataWithDependants> getResult()
			throws CommonBusinessException {
		return getMasterDataCollectController().getSearchStrategy().getSearchResult();
	}

	@Override
	public void finishSearch(List<CollectableMasterDataWithDependants> lstclctResult) {
		final MasterDataCollectController cc = getMasterDataCollectController();
		
		cc.getSearchStrategy().setCollectableProxyList(
				(ProxyList<CollectableMasterDataWithDependants>) lstclctResult);
		cc.fillResultPanel(lstclctResult, lstclctResult.size(), false);
		cc.setupChangeListenerForResultTableVerticalScrollBar();
		super.setChanged();
		super.notifyObservers("search finished");
	}
	
	private MasterDataCollectController getMasterDataCollectController() {
		return cc;
	}
}
