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
package org.nuclos.server.ruleengine.ejb3;

import java.util.Collection;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

// @Remote
public interface TimelimitRuleFacadeRemote {

	/**
	 * get all records
	 * @return Collection<RuleVO> all TimelimitRule definitions
	 * @throws CommonFinderException
	 */
	Collection<RuleVO> getAllTimelimitRules();

	/**
	 * get all active records
	 * @return Collection<RuleVO> all active TimelimitRule definitions
	 * @throws CommonFinderException
	 */
	Collection<RuleVO> getActiveTimelimitRules()
		throws CommonFinderException;

	/**
	 * create a new TimelimitRule definition in the database
	 * @param mdcvo containing the TimelimitRule
	 * @return same layout as value object
	 * @throws NuclosCompileException
	 * @throws CommonCreateException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 * @throws CommonPermissionException
	 */
	MasterDataVO create(MasterDataVO mdcvo)
		throws CommonCreateException, CommonFinderException,
		CommonRemoveException, CommonValidationException,
		CommonStaleVersionException, NuclosCompileException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * modify an existing TimelimitRule definition in the database
	 * @param mdcvo
	 * @return new TimelimitRule as value object
	 * @throws NuclosCompileException
	 * @throws CommonCreateException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 * @throws CommonPermissionException
	 */
	MasterDataVO modify(MasterDataVO mdcvo)
		throws CommonCreateException, CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		CommonValidationException, NuclosCompileException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * delete TimelimitRule definition from database
	 * @param mdcvo containing the TimelimitRule
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 */
	void remove(MasterDataVO mdcvo)
		throws CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException, NuclosCompileException;

	/**
	 * Check if compilation would be successful.
	 * @param mdcvo
	 * @throws NuclosCompileException
	 */
	void check(MasterDataVO mdcvo)
		throws NuclosCompileException;

	/**
	 * @return String containing functions the user has to implement
	 */
	String getClassTemplate();

	/**
	 * imports the given timelimit rules, adding new and overwriting existing. The other existing timelimit rules remain untouched.
	 * @param collRuleVO
	 * @throws CreateException
	 */
	void importTimelimitRules(Collection<RuleVO> collRuleVO) throws CommonBusinessException;

	/**
	 * executes all active timlimit rules.
	 * First we get the list of all relevant generic objects, then we execute the rule for each one in an own transaction.
	 * @return InvoiceInspectorResultRun
	 */
	void executeTimelimitRule(String sRuleName);

	/**
	 * @param sRuleName
	 * @throws CreateException
	 * @author corina.mandoki
	 */
	void executeRule(String sRuleName, Integer iSessionId);

}
