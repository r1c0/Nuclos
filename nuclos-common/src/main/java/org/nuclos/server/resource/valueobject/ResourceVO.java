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
package org.nuclos.server.resource.valueobject;

import java.util.Date;

import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a resource.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 00.01.000
 */
public class ResourceVO extends NuclosValueObject {
	
	// Resources internally used by Nucleus 
	public static final String NCL_SEARCH_ICON = "NCL_SEARCH_ICON";
	public static final String NCL_MAINFRAME_TITLE = "NCL_MAINFRAME_TITLE";
	public static final String NCL_DIALOG_ICONS = "NCL_DIALOG_ICONS";
	public static final String NCL_SEARCH_TEMPLATE = "NCL_SEARCH_TEMPLATE";

	private String sName;
	private String sDescription;
	private String sFileName;
	private Boolean bSystemResource;
	
	/**
	 * constructor to be called by server only
	 * @param sName resource name of underlying database record
	 * @param resourceType resource type of underlying database record
	 * @param sDescription resource description of underlying database record
	 * @param sFileName resource file name of underlying database record
	 */
	public ResourceVO(NuclosValueObject nvo, String sName, String sDescription,
		String sFileName, Boolean bSystemResource) {
		super(nvo);
		this.sName = sName;
		this.sDescription = sDescription;
		this.sFileName = sFileName;
		this.bSystemResource = bSystemResource;
	}

	/**
	 * constructor to be called by server only
	 * @param sName resource name of underlying database record
	 * @param resourceType resource type of underlying database record
	 * @param sDescription resource description of underlying database record
	 * @param sFileName resource file name of underlying database record
	 */
	public ResourceVO(Integer iId, Date dateCreatedAt, String sCreatedBy, Date dateChangedAt, String sChangedBy, Integer iVersion, String sName, String sDescription, String sFileName, Boolean bSystemResource) {
		super(iId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion);
		this.sName = sName;
		this.sDescription = sDescription;
		this.sFileName = sFileName;
		this.bSystemResource = bSystemResource;
	}

	/**
	 * get resource name of underlying database record
	 * @return resource name of underlying database record
	 */
	public String getName() {
		return this.sName;
	}

	/**
	 * set resource name of underlying database record
	 * @param sName resource name of underlying database record
	 */
	public void setName(String sName) {
		this.sName = sName;
	}

	/**
	 * get resource description of underlying database record
	 * @return resource description of underlying database record
	 */
	public String getDescription() {
		return this.sDescription;
	}
	
	/**
	 * set resource description of underlying database record
	 * @param sDescription resource description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	/**
	 * get resource file name of underlying database record
	 * @return resource file name of underlying database record
	 */
	public String getFileName() {
		return this.sFileName;
	}

	/**
	 * set resource file name of underlying database record
	 * @param sFileName resource file name of underlying database record
	 */
	public void setFileName(String sFileName) {
		this.sFileName = sFileName;
	}

	/**
	 * @return is this resource a system resource?
	 */
	public boolean isSystemResource() {
		return this.bSystemResource;
	}


	@Override
	public int hashCode() {
		return (getName() != null ? getName().hashCode() : 0);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ResourceVO) {
			final ResourceVO that = (ResourceVO) o;
			// resources are equal if there names are equal
			return this.getName().equals(that.getName());
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}	// class ResourceVO
