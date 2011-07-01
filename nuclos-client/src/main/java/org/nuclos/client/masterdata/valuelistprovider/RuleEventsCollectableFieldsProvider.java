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

import java.util.Collections;
import java.util.List;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.masterdata.MasterDataDelegate;

/**
 * <code>ValueListProvider</code> for "rule events". This is used in the rule editor by usages.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
public class RuleEventsCollectableFieldsProvider implements CollectableFieldsProvider {

	@Override
	public void setParameter(String sName, Object oValue) {
		//no parameters
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		java.util.Map<String, String> mpActions = MasterDataDelegate.getInstance().getRuleEventsWithLocaleResource();
		List<CollectableField> result = LocalizedCollectableValueField.makeListfromMap(mpActions);
		Collections.sort(result);
		return result;
		/*
		java.util.Map<Integer, String> mpActions = MasterDataDelegate.getInstance().getRuleEventsWithLocaleResource();
		
		List<CollectableField> result = new ArrayList<CollectableField>();
		
		for (Integer id : mpActions.keySet()) {
			result.add(new CollectableValueIdField(id, CommonLocaleDelegate.getMessage(mpActions.get(id), null)));
		}

		return result;
		*/
	}

}	// class RuleEventsCollectableFieldsProvider
