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
package org.nuclos.client.statemodel;

import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * Value object for rules in a certain order.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class SortedRuleVO {

	private Integer iOrder;
	private Integer iId;
	private String  sName;
	private String  sDescription;
	private boolean bRunAfterwards = false;
	private String  sClassname;
	
	public SortedRuleVO(RuleVO vo) {
		iOrder = new Integer(-1);
		iId = vo.getId();
		sName = vo.getRule();
		sDescription = vo.getDescription();
	}

	public SortedRuleVO(EventSupportSourceVO vo, int order, boolean runAfterwards) {
		iOrder = order;
		iId = vo.getId();
		sName = vo.getName();
		sDescription = vo.getDescription();
		sClassname = vo.getClassname();
		this.bRunAfterwards = runAfterwards;
	}

	
	public SortedRuleVO(Integer iId, String name, String description, Integer iOrder, boolean bRunAfterwards) {
		this.iOrder = iOrder;
		this.iId = iId;
		sName = name;
		sDescription = description;
		this.bRunAfterwards = bRunAfterwards;
	}

	public SortedRuleVO(StateModelRuleVO stateModelRuleVO) {
		this.iId = stateModelRuleVO.getId();
		this.sName = stateModelRuleVO.getName();
		this.sDescription = stateModelRuleVO.getDescription();
	}

	public SortedRuleVO(StateModelEventSupportVO stateModelRuleVO) {
		this.iId = null;
		this.sName = stateModelRuleVO.getName();
		this.sDescription = stateModelRuleVO.getDescription();
		this.sClassname = stateModelRuleVO.getClassname();
	}

	
	public Integer getOrder() {
		return iOrder;
	}

	public void setOrder(Integer iOrder) {
		this.iOrder = iOrder;
	}

	public Integer getId() {
		return iId;
	}

	public void setId(Integer iId) {
		this.iId = iId;
	}

	public String getName() {
		return sName;
	}

	public void setName(String sName) {
		this.sName = sName;
	}

	public String getDescription() {
		return sDescription;
	}

	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	public boolean isRunAfterwards() {
		return bRunAfterwards;
	}

	public void setRunAfterwards(boolean bRunAfterwards) {
		this.bRunAfterwards = bRunAfterwards;
	}

	public String getClassname() {
		return sClassname;
	}

	public void setClassname(String sClassname) {
		this.sClassname = sClassname;
	}

	@Override
	public String toString() {		
		return this.sName;
	}
	
	
	
}
