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
package org.nuclos.client.masterdata.valuelistprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * <code>ValueListProvider</code> for "all actions". This is used in the role administration dialog where all actions
 * must be displayed, regardless of user rights.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
public class RoleActionsCollectableFieldsProvider implements CollectableFieldsProvider {

	@Override
	public void setParameter(String sName, Object oValue) {
		//no parameters
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		List<CollectableField> result = new ArrayList<CollectableField>();
		CollectableSearchExpression cse = new CollectableSearchExpression(null);
		cse.setIncludingSystemData(true);
		ProxyList<MasterDataWithDependantsVO> mdvos = MasterDataDelegate.getInstance().getInstance().getMasterDataProxyList(
			NuclosEntity.ACTION.getEntityName(), cse);
		for (MasterDataWithDependantsVO mdvo : mdvos) {
			String action = mdvo.getField("action", String.class);
			String label = mdvo.getField("name", String.class);
			String resId = mdvo.getField("labelres", String.class);
			if (resId != null) {
				label = CommonLocaleDelegate.getTextFallback(resId, label);
			}
			result.add(new LocalizedCollectableValueField(action, label));
		}
		Collections.sort(result);
		return result;
	}

}	// class RoleActionsCollectableFieldsProvider
