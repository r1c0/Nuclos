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

import org.nuclos.client.masterdata.CollectableMasterDataProxyListAdapter;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public class LayoutSearchStrategy extends MasterDataSearchStrategy {

	public LayoutSearchStrategy() {
	}

	@Override
	public CollectableMasterDataProxyListAdapter getSearchResult() throws CollectableFieldFormatException {
		final MasterDataCollectController mdc = getMasterCollectDataController();
		final CollectableSearchExpression clctexpr = new CollectableSearchExpression(getCollectableSearchCondition(),
				mdc.getResultController().getCollectableSortingSequence());
		clctexpr.setIncludingSystemData(ApplicationProperties.getInstance().isFunctionBlockDev() == Boolean.TRUE);
		final ProxyList<MasterDataWithDependantsVO> mdproxylst = mddelegate.getMasterDataProxyList(mdc.getEntityName(),
				clctexpr);
		return new CollectableMasterDataProxyListAdapter(mdproxylst, mdc.getCollectableEntity());
	}

}
