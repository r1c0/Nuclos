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
package org.nuclos.server.statemodel.valueobject;

import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a state model.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class StateModelVO extends NuclosValueObject {

	private String sName;
	private String sDescription;
	private StateModelLayout layout;
	private String xmlLayout;

	public StateModelVO() {
		this(null, null, null, null);
	}

	/**
	 * constructor to be called by server only
	 * @param nvo
	 * @param sName model name of underlying database record
	 * @param sDescription model description of underlying database record
	 * @param layout model layout information of underlying database record
	 */
	public StateModelVO(NuclosValueObject nvo, String sName, String sDescription, StateModelLayout layout, String xml) {
		super(nvo);
		this.sName = sName;
		this.sDescription = sDescription;
		this.layout = layout;
		this.xmlLayout = xml;
	}

	/**
	 * constructor to be called by client only
	 * @param sName model name of underlying database record
	 * @param sDescription model description of underlying database record
	 * @param layout model layout information of underlying database record
	 */
	public StateModelVO(String sName, String sDescription, StateModelLayout layout, String xml) {
		super();
		this.sName = sName;
		this.sDescription = sDescription;
		this.layout = layout;
		this.xmlLayout = xml;
	}

	/**
	 * get model name of underlying database record
	 * @return model name of underlying database record
	 */
	public String getName() {
		return sName;
	}

	/**
	 * set model name of underlying database record
	 * @param sName model name of underlying database record
	 */
	public void setName(String sName) {
		this.sName = sName;
	}

	/**
	 * get model description of underlying database record
	 * @return model description of underlying database record
	 */
	public String getDescription() {
		return sDescription;
	}

	/**
	 * set model description of underlying database record
	 * @param sDescription model description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	/**
	 * get model layout of underlying database record
	 * @return model layout of underlying database record
	 */
	public StateModelLayout getLayout() {
		return this.layout;
	}

	/**
	 * set model layout of underlying database record
	 * @param layoutinfo model layout of underlying database record
	 */
	public void setLayout(StateModelLayout layoutinfo) {
		this.layout = layoutinfo;
	}
	
	public String getXMLLayout() {
		return this.xmlLayout;
	}
	
	public void setXMLLayout(String xml) {
		this.xmlLayout = xml;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",name=").append(getName());
		result.append("]");
		return result.toString();
	}

}	// class StateModelVO
