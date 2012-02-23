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
package org.nuclos.client.genericobject.valuelistprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.common.UsageCriteria;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * Value list provider to get all state numerals for a certain module.
 * This value list provider is used only in search masks.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">Ramin Goettlich</a>
 * @version 00.01.000
 */
public class StateCollectableFieldsProvider implements CollectableFieldsProvider {
	private static final Logger log = Logger.getLogger(StateCollectableFieldsProvider.class);

	private UsageCriteria usagecriteria;

	/**
	 * valid parameters:
	 * <ul>
	 *   <li>"service" = service id</li>
	 *   <li>"_searchmode" = collectable in search mask?
	 * </ul>
	 * @param sName parameter name
	 * @param oValue parameter value
	 */
	@Override
	public void setParameter(String sName, Object oValue) {
		StateCollectableFieldsProvider.log.debug("setParameter - sName = " + sName + " - oValue = " + oValue);
		if (sName.equals("module")) {
			this.usagecriteria = new UsageCriteria((Integer) oValue, null, null);
		} else if (sName.equals("usagecriteria")) {
			this.usagecriteria = (UsageCriteria) oValue;
		} else {
			// ignore
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() {
		Collection<StateVO> colSearchStates;
		if (usagecriteria.getProcessId() == null) {
			colSearchStates = StateDelegate.getInstance().getStatesByModule(usagecriteria.getModuleId());
		} else {
			colSearchStates = StateDelegate.getInstance().getStatesByModel(StateDelegate.getInstance().getStateModelId(usagecriteria));
		}	
		
		StateCollectableFieldsProvider.log.debug("getCollectableFields - iModuleId/iProcessId = " + this.usagecriteria.getModuleId() + "/" + this.usagecriteria.getProcessId());

		final List<CollectableField> result = new ArrayList<CollectableField>();
		for (StateVO statevo : colSearchStates) {
			result.add(new CollectableValueField(statevo));
		}

		Collections.sort(result);
		return result;
	}

}	// class StateCollectableFieldsProvider
