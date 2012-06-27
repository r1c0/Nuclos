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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.GeneratorRuleVO;
import org.nuclos.server.genericobject.ejb3.GenerationResult;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeRemote;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GeneratorVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

/**
 * Delegate class for object generation. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class GeneratorDelegate {

	private static GeneratorDelegate INSTANCE;
	
	//
	
	// Spring injection

	private GeneratorFacadeRemote generatorFacadeRemote;
	
	// end of Spring injection

	GeneratorDelegate() {
		INSTANCE = this;
	}

	public static GeneratorDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setGeneratorFacadeRemote(GeneratorFacadeRemote generatorFacadeRemote) {
		this.generatorFacadeRemote = generatorFacadeRemote;
	}

	/**
	 * get all generator actions
	 *
	 * @return collection of generator actions
	 */
	public GeneratorVO getGeneratorActions() throws CommonPermissionException {
		try {
			return generatorFacadeRemote.getGeneratorActions();
		} catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * generate one or more leased objects from an existing leased object
	 * (copying selected attributes and subforms)
	 *
	 * @param iSourceGenericObjectId
	 *            source leased object id to generate from
	 * @param generatoractionvo
	 *            generator action value object to determine what to do
	 * @return id of generated leased object (if exactly one object was
	 *         generated)
	 */
	public GenerationResult generateGenericObject(Long iSourceGenericObjectId, Long parameterObjectId, GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, NuclosBusinessException, CommonStaleVersionException, CommonValidationException {
		try {
			return generatorFacadeRemote.generateGenericObject(iSourceGenericObjectId, parameterObjectId, generatoractionvo);
		} catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public Map<String, Collection<EntityObjectVO>> groupObjects(Collection<Long> sources, GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, NuclosBusinessException, CommonStaleVersionException, CommonValidationException {
		try {
			return generatorFacadeRemote.groupObjects(new ArrayList<Long>(sources), generatoractionvo);
		} catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public GenerationResult generateGenericObject(Collection<EntityObjectVO> sources, Long parameterObjectId, GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, NuclosBusinessException, CommonStaleVersionException, CommonValidationException {
		try {
			return generatorFacadeRemote.generateGenericObject(sources, parameterObjectId, generatoractionvo);
		} catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}

	}

	public void updateRuleUsages(Integer iGeneratorId, Collection<GeneratorRuleVO> colUsages) throws NuclosBusinessRuleException, CommonCreateException, CommonPermissionException, CommonStaleVersionException, CommonRemoveException, CommonFinderException {
		try {
			generatorFacadeRemote.updateRuleUsages(iGeneratorId, colUsages);
		} catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public Collection<GeneratorRuleVO> getRuleUsages(Integer iGeneratorId) throws CommonPermissionException {
		try {
			return generatorFacadeRemote.getRuleUsages(iGeneratorId);
		} catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

} // class GeneratorDelegate
