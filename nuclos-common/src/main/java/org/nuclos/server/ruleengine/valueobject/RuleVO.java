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
 * <p>
 * Attention: <em>Never</em> change/add a property here without adjusting 
 * {@link org.nuclos.client.rule.admin.CollectableRule.Entity}. 
 * </p>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class RuleVO extends NuclosValueObject {

	private String rule;
	private String sDescription;
	private String source;
	private Integer nucletId;
	private String nuclet;
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
		this.rule = sName;
		this.sDescription = sDescription;
		this.source = sRuleSource;
		this.nucletId = nucletId;
		this.bActive = bActive;
		this.bDebug = bDebug;
	}

	/**
	 * constructor to be called by client only
	 * @param rule rule name of underlying database record
	 * @param sDescription rule description of underlying database record
	 * @param sRuleSource rule sourcecode of underlying database record
	 */
	public RuleVO(String rule, String sDescription, String sRuleSource, Integer nucletId, boolean bActive) {
		super();
		this.rule = rule;
		this.sDescription = sDescription;
		this.source = sRuleSource;
		this.nucletId = nucletId;
		this.bActive = bActive;
	}
	
	public RuleVO(Integer id, Integer nucletId, int version, String ruleName, String description, String source, boolean active) {
		super(id, null, null, null, null, version);
		this.rule = ruleName;
		this.sDescription = description;
		this.source = source;
		this.nucletId = nucletId;
		this.bActive = bActive;
	}

	/**
	 * get rule name of underlying database record
	 * @return rule name of underlying database record
	 */
	public String getRule() {
		return this.rule;
	}

	/**
	 * set rule name of underlying database record
	 * @param rule rule name of underlying database record
	 */
	public void setRule(String rule) {
		this.rule = rule;
	}
	
	public String getNuclet() {
		return nuclet;
	}
	
	public void setNuclet(String nuclet) {
		this.nuclet = nuclet;
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
	public String getSource() {
		return this.source;
	}

	/**
	 * set rule sourcecode of underlying database record
	 * @param sRuleSource rule sourcecode of underlying database record
	 */
	public void setSource(String sRuleSource) {
		this.source = sRuleSource;
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
		if (StringUtils.isNullOrEmpty(getRule())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.name");
		}
		if (StringUtils.isNullOrEmpty(this.getDescription())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.description");
		}
		if (StringUtils.isNullOrEmpty(this.getSource())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.rulesource");
		}
	}

	@Override
	public int hashCode() {
		return (getRule() != null ? getRule().hashCode() : 0);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof RuleVO) {
			final RuleVO that = (RuleVO) o;
			// rules are equal if there names are equal
			return getRule().equals(that.getRule());
		}
		return false;
	}

	@Override
	public String toString() {
		return getRule();
	}
	
	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append("RuleVO[");
		result.append("id=").append(getId());
		result.append("name=").append(rule);
		if (bActive) {
			result.append(",active=").append(bActive);
		}
		if (bDebug) {
			result.append(",debug=").append(bDebug);
		}
		if (nucletId != null) {
			result.append(",nucletId=").append(nucletId);
		}
		if (source != null) {
			result.append(",src=").append(source);
		}
		result.append("]");
		return result.toString();
	}

}	// class RuleVO
