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

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBException;
import javax.ejb.Remote;

import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.common.valueobject.GeneratorRuleVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GeneratorVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

@Remote
public interface GeneratorFacadeRemote {

	/**
	 * @return all generator actions
	 */
	@RolesAllowed("Login")
	public abstract GeneratorVO getGeneratorActions()
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
   public GenericObjectVO generateGenericObject(Integer iSourceGenericObjectId, Integer parameterObjectId, GeneratorActionVO generatoractionvo)
   	throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException,
      CommonStaleVersionException, CommonValidationException;

   @RolesAllowed("Login")
   public Collection<Integer> generateGenericObjectFromMultipleSourcesWithAttributeGrouping(Collection<Integer> collSourceGenericObjectId,
       Integer parameterObjectId, GeneratorActionVO generatoractionvo)throws CommonPermissionException, CommonFinderException;
   
	/**
	 * Generates one target object and fires the generation rules for a collection of source objects
	 * @param collSourceGenericObjectId
	 * @param generatoractionvo
	 * @return
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws NuclosBusinessRuleException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 * @nucleus.permission mayWrite(generatoractionvo.getTargetModuleId())
	 */
	@RolesAllowed("Login")
	public abstract Integer generateGenericObjectFromMultipleSources(
		Collection<Integer> collSourceGenericObjectId,
		Integer parameterObjectId,
		GeneratorActionVO generatoractionvo) throws CommonFinderException,
		CommonPermissionException, NuclosBusinessException, NuclosBusinessRuleException,
		CommonStaleVersionException, CommonValidationException;

	/**
	 * update usages of rules
	 * @param generatorId
	 * @param usages
	 * @throws EJBException
	 */
	public abstract void updateRuleUsages(Integer generatorId,
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
	public abstract Collection<GeneratorRuleVO> getRuleUsages(Integer generatorId)
		throws CommonPermissionException;

}
