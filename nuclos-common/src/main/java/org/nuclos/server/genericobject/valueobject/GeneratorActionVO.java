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
package org.nuclos.server.genericobject.valueobject;

import java.util.Collection;

import org.nuclos.common.PropertiesMap;

/**
 * Value object representing a leased object generator action.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.000
 */
public class GeneratorActionVO implements java.io.Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Integer iId;
	private String sName;
	private String sLabel;
	private Integer iSourceModuleId;
	private Integer iTargetModuleId;
	private Integer iTargetProcessId;
	private Integer iParameterEntityId;
	private Integer iCaseTransitionId;
	private boolean blnGroupAttributes;
	private boolean blnCreateRelationBetweenObjects;

	private PropertiesMap mpProperties = null;

	private Collection<GeneratorUsageVO> collUsages;

	/**
	 * constructor to be called by server only
	 * @param iId id for generator action
	 * @param sName name for generator action
	 * @param sLabel label for generator action
	 * @param iSourceModuleId source module for generator action
	 * @param iTargetModuleId target module for generator action
	 * @param iTargetProcessId target process for generator action
	 */
	public GeneratorActionVO(
			Integer iId,
			String sName,
			String sLabel,
			Integer iSourceModuleId,
			Integer iTargetModuleId,
			Integer iTargetProcessId,
			Integer iParameterEntityId,
			Integer iCaseTransitionId,
			Collection<GeneratorUsageVO> collUsages) {
		this.iId = iId;
		this.sName = sName;
		this.sLabel = sLabel;
		this.iSourceModuleId = iSourceModuleId;
		this.iTargetModuleId = iTargetModuleId;
		this.iTargetProcessId = iTargetProcessId;
		this.iParameterEntityId = iParameterEntityId;
		this.iCaseTransitionId = iCaseTransitionId;
		this.collUsages = collUsages;
	}

	public boolean isGroupAttributes() {
		return blnGroupAttributes;
	}

	public void setGroupAttributes(boolean blnGroupAttributes) {
		this.blnGroupAttributes = blnGroupAttributes;
	}

	public boolean isCreateRelationBetweenObjects() {
		return blnCreateRelationBetweenObjects;
	}

	public void setCreateRelationBetweenObjects(boolean blnMakeRelationBetweenObjects) {
		this.blnCreateRelationBetweenObjects = blnMakeRelationBetweenObjects;
	}

	/**
	 * get id for generator action
	 * @return id for generator action
	 */
	public Integer getId() {
		return iId;
	}

	/**
	 * get name for generator action
	 * @return name for generator action
	 */
	public String getName() {
		return sName;
	}

	/**
	 * get label for generator action
	 * @return label for generator action
	 */
	public String getLabel() {
		return sLabel;
	}

	/**
	 * get source module for generator action
	 * @return source module for generator action
	 */
	public Integer getSourceModuleId() {
		return iSourceModuleId;
	}

	/**
	 * get target module for generator action
	 * @return target module for generator action
	 */
	public Integer getTargetModuleId() {
		return iTargetModuleId;
	}

	/**
	 * get target process for generator action
	 * @return target process for generator action
	 */
	public Integer getTargetProcessId() {
		return iTargetProcessId;
	}

	public Integer getParameterEntityId() {
		return iParameterEntityId;
	}

	public Collection<GeneratorUsageVO> getUsages() {
		return collUsages;
	}

	@Override
	public String toString() {
		return (this.getLabel() == null? this.getName() : this.getLabel());
	}

	public void setProperties(PropertiesMap mpProperties) {
		this.mpProperties = mpProperties;
	}

	public PropertiesMap getProperties() {
		return this.mpProperties;
	}

	public Integer getCaseTransitionId() {
		return iCaseTransitionId;
	}

	public void setCaseTransitionId(Integer caseTransitionId) {
		iCaseTransitionId = caseTransitionId;
	}
}
