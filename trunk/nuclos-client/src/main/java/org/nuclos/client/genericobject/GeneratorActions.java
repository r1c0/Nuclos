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
package org.nuclos.client.genericobject;

import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeRemote;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GeneratorVO;

/**
 * Singleton for the GeneratorActions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class GeneratorActions {
	private static final Logger log = Logger.getLogger(GeneratorActions.class);

	private static GeneratorActions singleton;

	private final GeneratorVO generatorcvo;

	private static synchronized GeneratorActions getInstance() {
		if (singleton == null) {
			singleton = new GeneratorActions();
		}
		return singleton;
	}

	private GeneratorActions() {
		try {
			this.generatorcvo = ServiceLocator.getInstance().getFacade(GeneratorFacadeRemote.class).getGeneratorActions();
		}
		catch (Exception ex) {
			final String sMessage = "Error reading the GeneratorActions";//"Fehler beim Lesen der GeneratorActions.";
			throw new NuclosFatalException(sMessage, ex);
		}
	}

	/**
	 * @param iModuleId
	 * @param iStateNumeral
	 * @param iProcessId
	 * @return List<GeneratorActionVO> the actions allowed for the given parameters.
	 * @postcondition result != null
	 */
	public static List<GeneratorActionVO> getActions(Integer iModuleId, Integer iStateNumeral, Integer iProcessId) {
		/** @todo replace sStateMnemonic with iStateMnemonic in GeneratorVO */
		List<GeneratorActionVO> result = getInstance().generatorcvo.getGeneratorActions(iModuleId, iStateNumeral, iProcessId);
		result = CollectionUtils.sorted(result, new Comparator<GeneratorActionVO>() {

			@Override
			public int compare(GeneratorActionVO o1, GeneratorActionVO o2) {
				return o1.toString().toUpperCase().compareTo(o2.toString().toUpperCase());
			}			
			
		});
		assert result != null;
		return result;
	}

	/**
	 * invalidates the cache so  the generator actions are reread the next time.
	 */
	public static void invalidateCache() {
		log.debug("invalidateCache");
		singleton = null;
	}

}	// class GeneratorActions
