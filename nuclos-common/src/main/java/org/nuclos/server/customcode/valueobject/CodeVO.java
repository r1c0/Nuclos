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
package org.nuclos.server.customcode.valueobject;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

public class CodeVO extends NuclosValueObject {

	private static final long serialVersionUID = 1L;
	private String sName;
	private String sDescription;
	private String sSource;
	private boolean bActive;
	private boolean bDebug;
	private Integer nucletId;

	/**
	 * constructor to be called by server only
	 * @param sName rule name of underlying database record
	 * @param sDescription rule description of underlying database record
	 * @param sSource rule sourcecode of underlying database record
	 */
	public CodeVO(NuclosValueObject nvo, String sName, String sDescription, String sSource, boolean bActive, boolean bDebug, Integer nucletId) {
		super(nvo);
		this.sName = sName;
		this.sDescription = sDescription;
		this.sSource = sSource;
		this.bActive = bActive;
		this.bDebug = bDebug;
		this.nucletId = nucletId;
	}

	/**
	 * constructor to be called by client only
	 * @param sName rule name of underlying database record
	 * @param sDescription rule description of underlying database record
	 * @param sSource rule sourcecode of underlying database record
	 */
	public CodeVO(String sName, String sDescription, String sSource, boolean bActive) {
		super();
		this.sName = sName;
		this.sDescription = sDescription;
		this.sSource = sSource;
		this.bActive = bActive;
	}

	/**
	 * get name of underlying database record
	 * @return name of underlying database record
	 */
	public String getName() {
		return this.sName;
	}

	/**
	 * set name of underlying database record
	 * @param sName name of underlying database record
	 */
	public void setName(String sName) {
		this.sName = sName;
	}

	/**
	 * get description of underlying database record
	 * @return description of underlying database record
	 */
	public String getDescription() {
		return this.sDescription;
	}

	/**
	 * set description of underlying database record
	 * @param sDescription description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	/**
	 * get sourcecode of underlying database record
	 * @return sourcecode of underlying database record
	 */
	public String getSource() {
		return this.sSource;
	}

	/**
	 * set sourcecode of underlying database record
	 * @param sSource sourcecode of underlying database record
	 */
	public void setSource(String sSource) {
		this.sSource = sSource;
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

	public Integer getNucletId() {
		return nucletId;
	}

	public void setNucletId(Integer nucletId) {
		this.nucletId = nucletId;
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
		if (StringUtils.isNullOrEmpty(this.getSource())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.rulesource");
		}
	}

	@Override
	public int hashCode() {
		return (getName() != null ? getName().hashCode() : 0);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CodeVO) {
			final CodeVO that = (CodeVO) o;
			// code artifacts are equal if there names are equal
			return this.getName().equals(that.getName());
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
