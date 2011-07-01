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

import javax.ejb.CreateException;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ServiceLocator;
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

	private static TimelimitRuleDelegate singleton;

	private TimelimitRuleFacadeRemote facade;

	public static synchronized TimelimitRuleDelegate getInstance() {
		if (singleton == null) {
			singleton = new TimelimitRuleDelegate();
		}
		return singleton;
	}

	private TimelimitRuleDelegate() {
	}

	/**
	 * gets the facade once for this object and stores it in a member variable.
	 */
	private TimelimitRuleFacadeRemote getTimelimitRuleFacade() throws NuclosFatalException {
		if (this.facade == null) {
			try {
				this.facade = ServiceLocator.getInstance().getFacade(TimelimitRuleFacadeRemote.class);
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return this.facade;
	}

	/**
	 * @return all timelimit rules defined in Nucleus.
	 */
	public Collection<RuleVO> getAllTimelimitRules() {
		try {
			return this.getTimelimitRuleFacade().getAllTimelimitRules();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void remove(MasterDataVO rulevo) throws CommonStaleVersionException, CommonRemoveException, CommonFinderException, NuclosFatalException, CommonCreateException, CommonPermissionException, NuclosBusinessRuleException, NuclosCompileException {
		try {
			getTimelimitRuleFacade().remove(rulevo);
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
			return getTimelimitRuleFacade().modify(mdcvo);
		}
		catch (RuntimeException ex) {
			throw new NuclosUpdateException(ex);
		}
	}

	public MasterDataVO create(MasterDataVO mdcvo) throws CommonBusinessException {
		try {
			return getTimelimitRuleFacade().create(mdcvo);
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
			return this.getTimelimitRuleFacade().getClassTemplate();
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
			this.getTimelimitRuleFacade().importTimelimitRules(collRuleVO);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CreateException e) {
			throw new NuclosFatalException(e);
		}
	}

	/**
	 * Compile the user defined code.
	 * @param ruleVO
	 * @throws NuclosCompileException
	 */
	public void check(MasterDataVO mdVO) throws NuclosCompileException {
		try {
			this.getTimelimitRuleFacade().check(mdVO);
		}
		catch (RuntimeException e) {
			throw new NuclosFatalException(e);
		}
	}
}	// class TimelimitRuleDelegate
