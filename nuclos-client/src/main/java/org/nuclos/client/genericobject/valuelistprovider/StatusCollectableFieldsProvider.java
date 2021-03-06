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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * Value list provider to get all states for the given module entity and process.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 00.01.000
 */
public class StatusCollectableFieldsProvider implements CollectableFieldsProvider {
	private static final Logger log = Logger.getLogger(StatusCollectableFieldsProvider.class);

	private Integer iModuleId;
	private Integer iProcessId;

	/**
	 * valid parameters:
	 * <ul>
	 *   <li>"module" = name of module entity</li>
	 *   <li>"process" = process id</li>
	 * </ul>
	 * @param sName parameter name
	 * @param oValue parameter value
	 */
	@Override
	public void setParameter(String sName, Object oValue) {
		log.debug("setParameter - sName = " + sName + " - oValue = " + oValue);
		if (sName.equals("module")) {
			try {
				final String sEntityName = (String) oValue;
				iModuleId = Modules.getInstance().getModuleIdByEntityName(sEntityName);
				if (iModuleId == null) {
					throw new IllegalArgumentException("oValue");
				}
			}
			catch (Exception ex) {
				throw new NuclosFatalException(SpringLocaleDelegate.getInstance().getMessage(
						"ProcessCollectableFieldsProvider.1", "Der Parameter \"module\" muss den Namen einer Modul-Entit\u00e4t enthalten.\n\"{0}\" ist keine g\u00fcltige Modul-Entit\u00e4t.", oValue), ex);
			}
		}
		else if (sName.equals("process")) {
			iProcessId = (Integer) oValue;
		}
		else {
			// ignore
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields - iModuleId = " + iModuleId);
		log.debug("getCollectableFields - iProcessId = " + iProcessId);
		
		final Map<Integer, StateVO> mpState = new HashMap<Integer, StateVO>();
		final List<CollectableField> result = new ArrayList<CollectableField>();
		
		if (iProcessId != null) {
			Integer iStateModelId = StateDelegate.getInstance().getStateModelId(new UsageCriteria(iModuleId, iProcessId, null, null));
			
			for (StateVO statevo : StateDelegate.getInstance().getStatesByModel(iStateModelId)) {
				mpState.put(statevo.getId(), statevo);
			}
		}
		else {
			for (StateVO statevo : StateDelegate.getInstance().getStatesByModule(iModuleId)) {
				if (!mpState.containsKey(statevo.getId())) {
					// add states only once
					mpState.put(statevo.getId(), statevo);
				}
			}
		}
		
		for (StateVO statevo : mpState.values()) {
			result.add(new CollectableValueIdField(statevo.getId(), statevo.getStatename()));
		}

		Collections.sort(result);
		return result;
	}

}	// class ProcessCollectableFieldsProvider
