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
package org.nuclos.server.ruleengine.valueobject;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a rule.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class RuleVO extends NuclosValueObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String sName;
	private String sDescription;
	private String sRuleSource;
	private Integer nucletId;
	private boolean bActive;
	private boolean bDebug;

	/**
	 * constructor to be called by server only
	 * @param sName rule name of underlying database record
	 * @param sDescription rule description of underlying database record
	 * @param sRuleSource rule sourcecode of underlying database record
	 */
	public RuleVO(NuclosValueObject nvo, String sName, String sDescription, String sRuleSource, Integer nucletId, boolean bActive, boolean bDebug) {
		super(nvo);
		this.sName = sName;
		this.sDescription = sDescription;
		this.sRuleSource = sRuleSource;
		this.nucletId = nucletId;
		this.bActive = bActive;
		this.bDebug = bDebug;
	}

	/**
	 * constructor to be called by client only
	 * @param sName rule name of underlying database record
	 * @param sDescription rule description of underlying database record
	 * @param sRuleSource rule sourcecode of underlying database record
	 */
	public RuleVO(String sName, String sDescription, String sRuleSource, Integer nucletId, boolean bActive) {
		super();
		this.sName = sName;
		this.sDescription = sDescription;
		this.sRuleSource = sRuleSource;
		this.nucletId = nucletId;
		this.bActive = bActive;
	}

	/**
	 * get rule name of underlying database record
	 * @return rule name of underlying database record
	 */
	public String getName() {
		return this.sName;
	}

	/**
	 * set rule name of underlying database record
	 * @param sName rule name of underlying database record
	 */
	public void setName(String sName) {
		this.sName = sName;
	}

	/**
	 * get rule description of underlying database record
	 * @return rule description of underlying database record
	 */
	public String getDescription() {
		return this.sDescription;
	}

	/**
	 * set rule description of underlying database record
	 * @param sDescription rule description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	/**
	 * get rule sourcecode of underlying database record
	 * @return rule sourcecode of underlying database record
	 */
	public String getRuleSource() {
		return this.sRuleSource;
	}

	/**
	 * set rule sourcecode of underlying database record
	 * @param sRuleSource rule sourcecode of underlying database record
	 */
	public void setRuleSource(String sRuleSource) {
		this.sRuleSource = sRuleSource;
	}

	public Integer getNucletId() {
		return nucletId;
	}

	public void setNucletId(Integer nucletId) {
		this.nucletId = nucletId;
	}

	/**
	 * @return Is this rule active?
	 */
	public boolean isActive() {
		return this.bActive;
	}

	/**
	 * sets the "active" state.
	 * @param bActive
	 */
	public void setActive(boolean bActive) {
		this.bActive = bActive;
	}

	public boolean isDebug() {
    	return bDebug;
    }

	public void setDebug(boolean bDebug) {
    	this.bDebug = bDebug;
    }

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		if (StringUtils.isNullOrEmpty(this.getName())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.name");
		}
		if (StringUtils.isNullOrEmpty(this.getDescription())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.description");
		}
		if (StringUtils.isNullOrEmpty(this.getRuleSource())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.rulesource");
		}
	}

	@Override
	public int hashCode() {
		return (getName() != null ? getName().hashCode() : 0);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof RuleVO) {
			final RuleVO that = (RuleVO) o;
			// rules are equal if there names are equal
			return this.getName().equals(that.getName());
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}	// class RuleVO
