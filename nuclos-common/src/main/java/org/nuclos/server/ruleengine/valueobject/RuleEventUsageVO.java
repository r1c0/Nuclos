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

import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a rule event assignment.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 00.01.000
 */
public class RuleEventUsageVO extends NuclosValueObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String DELETE_EVENT = "Delete";
	public static String DELETE_AFTER_EVENT = "Delete.after";
	public static String SAVE_EVENT = "Save";
	public static String SAVE_AFTER_EVENT = "Save.after";
	public static String USER_EVENT = "User";
	
	private String sEvent;
	private String sEntity;
	private Integer iRuleId;
	private String sRule;
	private Integer iOrder;

	/**
	 * constructor to be called by server only
	 * @param iId primary key of underlying database record
	 * @param iEventId id of event
	 * @param sEvent name of event (joined)
	 * @param iEntityId id of entity
	 * @param sEntity name of entity (joined)
	 * @param iRuleId id of rule
	 * @param sRule name of rule (joined)
	 * @param iOrder order number
	 * @param dCreated creation date of underlying database record
	 * @param sCreated creator of underlying database record
	 * @param dChanged last changed date of underlying database record
	 * @param sChanged last changer of underlying database record
	 * @param iVersion version for underlying database record
	 */
	public RuleEventUsageVO(Integer iId, String sEvent, String sEntity, Integer iRuleId,
			String sRule, Integer iOrder, java.util.Date dCreated, String sCreated, java.util.Date dChanged, String sChanged,
			Integer iVersion) {
		super(iId, dCreated, sCreated, dChanged, sChanged, iVersion);
		this.sEvent = sEvent;
		this.sEntity = sEntity;
		this.iRuleId = iRuleId;
		this.sRule = sRule;
		this.iOrder = iOrder;
	}

	/**
	 * constructor to be called by server only
	 * @param iEventId id of event
	 * @param iEntityId id of entity
	 * @param iRuleId id of rule
	 * @param iOrder order number
	 */
	public RuleEventUsageVO(String sEvent, String sEntity, Integer iRuleId, Integer iOrder) {
		super();
		this.sEvent = sEvent;
		this.sEntity = sEntity;
		this.iRuleId = iRuleId;
		this.iOrder = iOrder;
	}

	/**
	 * get event name of underlying database record (joined)
	 * @return event name of underlying database record
	 */
	public String getEvent() {
		return sEvent;
	}

	/**
	 * set event name. Note that this value is never written to the database but is needed for the client for consistency reasons.
	 * @param sEvent
	 */
	public void setEvent(String sEvent) {
		this.sEvent = sEvent;
	}

	/**
	 * get entity name of underlying database record (joined)
	 * @return entity name of underlying database record
	 */
	public String getEntity() {
		return sEntity;
	}

	/**
	 * set entity name. Note that this value is never written to the database but is needed for the client for consistency reasons.
	 * @param sEntity
	 */
	public void setEntity(String sEntity) {
		this.sEntity = sEntity;
	}

	/**
	 * get rule id of underlying database record
	 * @return rule id of underlying database record
	 */
	public Integer getRuleId() {
		return iRuleId;
	}

	/**
	 * set rule id of underlying database record
	 * @param iRuleId rule id of underlying database record
	 */
	public void setRuleId(Integer iRuleId) {
		this.iRuleId = iRuleId;
	}

	/**
	 * get rule name of underlying database record (joined)
	 * @return rule name of underlying database record
	 */
	public String getRule() {
		return sRule;
	}

	/**
	 * set rule name. Note that this value is never written to the database but is needed for the client for consistency reasons.
	 * @param sRule
	 */
	public void setRule(String sRule) {
		this.sRule = sRule;
	}

	/**
	 * get order of underlying database record
	 * @return order of underlying database record
	 */
	public Integer getOrder() {
		return iOrder;
	}

	/**
	 * set order of underlying database record
	 * @param iOrder order of underlying database record
	 */
	public void setOrder(Integer iOrder) {
		this.iOrder = iOrder;
	}

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		if (this.getEvent() == null) {
			throw new CommonValidationException("ruleengine.error.validation.event.eventid");
		}
		if (this.getEntity() == null) {
			throw new CommonValidationException("ruleengine.error.validation.event.entityid");
		}
		if (this.getRuleId() == null) {
			throw new CommonValidationException("ruleengine.error.validation.event.ruleid");
		}
		if (this.getOrder() == null) {
			throw new CommonValidationException("ruleengine.error.validation.event.order");
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",entity=").append(getEntity());
		result.append(",event=").append(getEntity());
		result.append(",rule=").append(getRule());
		result.append(",order=").append(getOrder());
		result.append("]");
		return result.toString();
	}

}
