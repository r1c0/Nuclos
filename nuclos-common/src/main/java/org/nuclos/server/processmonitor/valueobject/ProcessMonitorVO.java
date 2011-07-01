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
/**
 * 
 */
package org.nuclos.server.processmonitor.valueobject;

import java.util.Date;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.statemodel.valueobject.StateModelLayout;

/**
 * @author Marc.Finke
 *
 * value object of a Process 
 * name
 * description
 * and layout of the image (Subprocess-Shapes and Transitions)
 */
public class ProcessMonitorVO extends NuclosValueObject {
	
	
	private String name;
	private String description;
	private StateModelLayout layout;

	/**
	 * 
	 */
	public ProcessMonitorVO() {
		// TODO Auto-generated constructor stub
		this(null, null);
	}

	public ProcessMonitorVO(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}
	
	public ProcessMonitorVO(NuclosValueObject valueObject, String name, String description, StateModelLayout layout) {
		super(valueObject);
		this.name = name;
		this.description = description;
		this.layout = layout;
	}

	
	/**
	 * @param id
	 * @param dateCreatedAt
	 * @param createdBy
	 * @param dateChangedAt
	 * @param changedBy
	 * @param version
	 */
	public ProcessMonitorVO(Integer id, Date dateCreatedAt, String createdBy,
			Date dateChangedAt, String changedBy, Integer version) {
		super(id, dateCreatedAt, createdBy, dateChangedAt, changedBy, version);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param clct
	 */
	public ProcessMonitorVO(Collectable clct) {
		super(clct);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param that
	 */
	public ProcessMonitorVO(NuclosValueObject that) {
		super(that);
		// TODO Auto-generated constructor stub
	}

	public void setLayout(StateModelLayout layout) {
		this.layout = layout;
	}
	
	public StateModelLayout getLayout() {
		return layout;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
