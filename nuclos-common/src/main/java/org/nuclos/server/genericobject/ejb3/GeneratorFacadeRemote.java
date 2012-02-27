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
package org.nuclos.server.genericobject.ejb3;

import java.util.Collection;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.GeneratorRuleVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GeneratorVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

// @Remote
public interface GeneratorFacadeRemote {

	/**
	 * @return all generator actions
	 */
	@RolesAllowed("Login")
	GeneratorVO getGeneratorActions()
		throws CommonPermissionException;

	/**
    * generate one or more generic objects from an existing generic object (copying selected attributes and subforms)
    *
    * @param iSourceGenericObjectId source generic object id to generate from
    * @param generatoractionvo		 generator action value object to determine what to do
    * @return id of generated generic object (if exactly one object was generated)
    * @nucleus.permission mayWrite(generatoractionvo.getTargetModuleId())
    */
   @RolesAllowed("Login")
   GenerationResult generateGenericObject(Long iSourceObjectId, Long parameterObjectId, GeneratorActionVO generatoractionvo)
   	throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException;

   @RolesAllowed("Login")
   Map<String, Collection<EntityObjectVO>> groupObjects(Collection<Long> sourceIds, GeneratorActionVO generatoractionvo);

   @RolesAllowed("Login")
   GenerationResult generateGenericObject(Collection<EntityObjectVO> sourceObjects, Long parameterObjectId, GeneratorActionVO generatoractionvo)
   	throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException;

	/**
	 * update usages of rules
	 * @param generatorId
	 * @param usages
	 * @throws EJBException
	 */
	void updateRuleUsages(Integer generatorId,
		Collection<GeneratorRuleVO> usages) throws NuclosBusinessRuleException,
		CommonCreateException, CommonPermissionException,
		CommonStaleVersionException, CommonRemoveException,
		CommonFinderException;

	/**
	 * get rule usages for specified GeneratorId
	 * @param iGeneratorId
	 * @return
	 * @throws CommonPermissionException
	 */
	Collection<GeneratorRuleVO> getRuleUsages(Integer generatorId)
		throws CommonPermissionException;

}
