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

import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GeneratorUsageVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;

// @Local
public interface GeneratorFacadeLocal {

	/**
	 * generate a new generic object from an existing generic object (copying attributes)
	 *
	 * @param iSourceGenericObjectId source generic object id to generate from
	 * @param sGenerator						name of object generation to determine what to do
	 * @return id of generated generic object (if exactly one object was generated)
	 */
	@RolesAllowed("Login")
	Long generateGenericObject(
			Long iSourceObjectId, String sGenerator)
		throws CommonFinderException, CommonPermissionException,
		CommonStaleVersionException, CommonValidationException;

	/**
	 * generate one or more generic objects from an existing generic object (copying selected attributes and subforms)
	 *
	 * @param loccvoSource source generic object to generate from
	 * @param sGenerator	 name of generator action to determine what to do
	 * @return id of generated generic object (if exactly one object was generated)
	 */
	@RolesAllowed("Login")
	Long generateGenericObject(
		RuleObjectContainerCVO loccvoSource, String sGenerator)
		throws CommonFinderException, CommonPermissionException,
		CommonStaleVersionException, CommonValidationException;

	/**
	 * transfers (copies) a specified set of attributes from one generic object to another.
	 * Called from within rules.
	 * Attention: because this is called within a rule, the source genericobject and its attributes were not saved until now ->
	 * the consequence is, that the old attribute values of the source genericobject were transfered to the target genericobject
	 * this is very ugly -> todo
	 *
	 * @param iSourceGenericObjectId source generic object id to transfer data from
	 * @param iTargetGenericObjectId target generic object id to transfer data to
	 * @param asAttributes Array of attribute names to specify transferred data
	 * @precondition asAttributes != null
	 */
	void transferGenericObjectData(GenericObjectVO govoSource,
		Integer iTargetGenericObjectId, String[][] asAttributes);

	/**
	 * get generator usages for specified GeneratorId
	 * @param iGeneratorId
	 * @return Collection<GeneratorUsageVO>
	 * @throws CommonFatalException
	 */
	Collection<GeneratorUsageVO> getGeneratorUsages(Integer id)
		throws CommonFatalException;

	EntityObjectVO generateGenericObjectWithoutCheckingPermission(Long iSourceObjectId, GeneratorActionVO generatoractionvo)
		throws CommonFinderException, CommonPermissionException,
		CommonStaleVersionException, CommonValidationException ;
}
