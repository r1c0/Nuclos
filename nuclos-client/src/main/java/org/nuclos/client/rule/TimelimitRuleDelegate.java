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
package org.nuclos.client.rule;

import java.util.Collection;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.ejb3.TimelimitRuleFacadeRemote;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * Delegate for <code>TimelimitRuleFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:uwe.allner@novabit.de">Uwe Allner</a>
 * @version 01.00.00
 */
public class TimelimitRuleDelegate {

	private static TimelimitRuleDelegate INSTANCE;
	
	// Spring injection

	private TimelimitRuleFacadeRemote timelimitRuleFacadeRemote;
	
	// end of Spring injection

	public static TimelimitRuleDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	TimelimitRuleDelegate() {
		INSTANCE = this;
	}
	
	public final void setTimelimitRuleFacadeRemote(TimelimitRuleFacadeRemote timelimitRuleFacadeRemote) {
		this.timelimitRuleFacadeRemote = timelimitRuleFacadeRemote;
	}

	/**
	 * @return all timelimit rules defined in Nucleus.
	 */
	public Collection<RuleVO> getAllTimelimitRules() {
		try {
			return timelimitRuleFacadeRemote.getAllTimelimitRules();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void remove(MasterDataVO rulevo) throws CommonStaleVersionException, CommonRemoveException, CommonFinderException, NuclosFatalException, CommonCreateException, CommonPermissionException, NuclosBusinessRuleException, NuclosCompileException {
		try {
			timelimitRuleFacadeRemote.remove(rulevo);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param mdcvo
	 * @return
	 * @throws CommonBusinessException
	 */
	public MasterDataVO update(MasterDataVO mdcvo) throws CommonBusinessException {
		try {
			return timelimitRuleFacadeRemote.modify(mdcvo);
		}
		catch (RuntimeException ex) {
			throw new NuclosUpdateException(ex);
		}
	}

	public MasterDataVO create(MasterDataVO mdcvo) throws CommonBusinessException {
		try {
			return timelimitRuleFacadeRemote.create(mdcvo);
		}
		catch (RuntimeException ex) {
			throw new NuclosBusinessException(ex);
		}
	}

	/**
	 * Returns a template for new rules to display in the rule editor.
	 */
	public String getClassTemplate() throws NuclosBusinessException {
		try {
			return timelimitRuleFacadeRemote.getClassTemplate();
		}
		catch (RuntimeException e) {
			throw new NuclosBusinessException(e);
		}
	}

	/**
	 * imports the given TimelimitRules, adding new and overwriting existing. The other existing are untouched.
	 * @param collRuleVO
	 * @throws CommonBusinessException
	 */
	public void importTimelimitRules(Collection<RuleVO> collRuleVO) throws CommonBusinessException {
		try {
			timelimitRuleFacadeRemote.importTimelimitRules(collRuleVO);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * Compile the user defined code.
	 * @param ruleVO
	 * @throws NuclosCompileException
	 */
	public void check(MasterDataVO mdVO) throws NuclosCompileException {
		try {
			timelimitRuleFacadeRemote.check(mdVO);
		}
		catch (RuntimeException e) {
			throw new NuclosFatalException(e);
		}
	}
}	// class TimelimitRuleDelegate
