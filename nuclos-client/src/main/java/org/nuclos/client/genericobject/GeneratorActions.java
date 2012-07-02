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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
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

	private static GeneratorActions INSTANCE;
	
	//
	
	// Spring injection
	
	private GeneratorFacadeRemote generatorFacadeRemote;
	
	// end of Spring injection

	private final AtomicReference<GeneratorVO> generatorcvo = new AtomicReference<GeneratorVO>();

	GeneratorActions() {
		INSTANCE = this;
	}

	private static GeneratorActions getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		if (INSTANCE.generatorcvo.get() == null) {
			try {
				INSTANCE.generatorcvo.set(INSTANCE.generatorFacadeRemote.getGeneratorActions());
			}
			catch (Exception ex) {
				final String sMessage = "Error reading the GeneratorActions";//"Fehler beim Lesen der GeneratorActions.";
				throw new NuclosFatalException(sMessage, ex);
			}
		}
		return INSTANCE;
	}

	public final void setGeneratorFacadeRemote(GeneratorFacadeRemote generatorFacadeRemote) {
		this.generatorFacadeRemote = generatorFacadeRemote;
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
		final List<GeneratorActionVO> result = getInstance().generatorcvo.get().getGeneratorActions(
				iModuleId, iStateNumeral, iProcessId);
		assert result != null;
		return sort(result);
	}
	
	public static List<GeneratorActionVO> sort(Collection<GeneratorActionVO> lst) {
		if (lst == null)
			return null;
		
		return CollectionUtils.sorted(lst, new Comparator<GeneratorActionVO>() {

			@Override
			public int compare(GeneratorActionVO o1, GeneratorActionVO o2) {
				return o1.toString().toUpperCase().compareTo(o2.toString().toUpperCase());
			}			
			
		});
	}

	/**
	 * gets a list of generator actions by module.
	 * @param iModuleId source module id
	 * @return List<GeneratorActionVO> list of generator actions
	 * @postcondition result != null
	 */
	public static List<GeneratorActionVO> getGeneratorActions(Integer iModuleId) {
		List<GeneratorActionVO> result = getInstance().generatorcvo.get().getGeneratorActions(iModuleId);
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
	 * gets a generator action by id.
	 * @param iGeneratorActionId the generator action id
	 * @return GeneratorActionVO the generator action
	 * @postcondition result != null
	 */
	public static GeneratorActionVO getGeneratorAction(Integer iGeneratorActionId) {
		GeneratorActionVO result = getInstance().generatorcvo.get().getGeneratorAction(iGeneratorActionId);
		
		//assert result != null;
		return result;
	}

	/**
	 * gets a generator action by name.
	 * @param sGeneratorName the generator action name
	 * @return GeneratorActionVO the generator action
	 * @postcondition result != null
	 */
	public static GeneratorActionVO getGeneratorAction(String sGeneratorName) {
		GeneratorActionVO result = getInstance().generatorcvo.get().getGeneratorAction(sGeneratorName);
		
		//assert result != null;
		return result;
	}

	/**
	 * invalidates the cache so  the generator actions are reread the next time.
	 */
	public static void invalidateCache() {
		log.debug("invalidateCache");
		getInstance().generatorcvo.set(null);
	}

}	// class GeneratorActions
