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

import org.nuclos.common.collection.Pair;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.customcode.NuclosTimelimitRule;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.RuleInterface;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

// @Local
public interface TimelimitRuleFacadeLocal {

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
	 * @return String containing functions the user has to implement
	 */
	String getClassTemplate();

	/**
	 * Get all job rules that have to be executed for a given job id.
	 *
	 * @param oId Id of job.
	 * @return Timelimit rule names
	 */
	Collection<String> getJobRules(Object oId);

	/**
	 * executes all active timlimit rules.
	 * First we get the list of all relevant generic objects, then we execute the rule for each one in an own transaction.
	 * @return InvoiceInspectorResultRun
	 */
	void executeTimelimitRule(String sRuleName);

	/**
	 * prepare the rule instance and the rule interface for executing a timelimit rule
	 * @param ruleName the name of the rule to instantiate
	 * @param sessionId the sessionId of the job execution
	 * @return the instantiated rule instance and rule interface
	 */
	Pair<NuclosTimelimitRule, RuleInterface> prepareTimelimitRule(String ruleName, Integer sessionId);

	/**
	 * executes the getIntIds()-part of a given timelimit rule in a separate transaction
	 * @param ruleInstance the rule class
	 * @param ri the rule interface as parameter for executed method
	 * @return the return value of <code>ruleInstance.getIntIds(ri)</code>
	 */
	Collection<Integer> executeTimelimitRule(final NuclosTimelimitRule ruleInstance, final RuleInterface ri);

	/**
	 * executes the given timelimit rule with the genericobject with given id in a separate transaction.
	 * @param ruleInstance the rule class
	 * @param ri the rule interface as parameter for executed method
	 * @param iGenericObjectId the leased object id as parameter for executed method
	 */
	void executeTimelimitRule(final NuclosTimelimitRule ruleInstance, final RuleInterface ri, final Integer iGenericObjectId);

	/**
	 * @param sRuleName
	 * @throws CreateException
	 * @author corina.mandoki
	 */
	void executeRule(String sRuleName, Integer iSessionId);

}
