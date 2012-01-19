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
public class RuleEngineGenerationVO extends NuclosValueObject {

	private Integer iRuleId;
	private Integer iGenerationId;
	private Integer iOrder;
	private Boolean bRunAfterwards;

	/**
	 * 	constructor to be called by server only
	 * @param id
	 * @param generationId
	 * @param ruleId
	 * @param order
	 * @param createdAt
	 * @param createdBy
	 * @param changedAt
	 * @param changedBy
	 * @param version
	 */
	public RuleEngineGenerationVO(Integer id, Integer generationId, Integer ruleId, Integer order, Boolean bRunAfterwards
			, java.util.Date createdAt, String createdBy, java.util.Date changedAt, String changedBy, Integer version) {
		super(id, createdAt, createdBy, changedAt, changedBy, version);

		this.iGenerationId = generationId;
		this.iRuleId = ruleId;
		this.iOrder = order;
		this.bRunAfterwards = bRunAfterwards;
	}

	public RuleEngineGenerationVO(NuclosValueObject evo, Integer generationId, Integer ruleId, Integer order, Boolean bRunAfterwards) {
		super(evo);
		this.iGenerationId = generationId;
		this.iRuleId = ruleId;
		this.iOrder = order;
		this.bRunAfterwards = bRunAfterwards;
	}

	@Override
	public String toString() {
		return " ID: " + this.getId() + " Generation Id: " + this.getGenerationId()
				+ " Rule Id: " + this.getRuleId();
	}

	public Integer getGenerationId() {
		return iGenerationId;
	}

	protected void setGenerationId(Integer generationId) {
		iGenerationId = generationId;
	}

	public Integer getOrder() {
		return iOrder;
	}

	protected void setOrder(Integer order) {
		iOrder = order;
	}

	public Integer getRuleId() {
		return iRuleId;
	}

	protected void setRuleId(Integer ruleId) {
		iRuleId = ruleId;
	}

	public Boolean isRunAfterwards() {
		return bRunAfterwards;
	}

	public void setRunAfterwards(Boolean bRunAfterwards) {
		this.bRunAfterwards = bRunAfterwards;
	}
	
	

}	// class RuleVO
