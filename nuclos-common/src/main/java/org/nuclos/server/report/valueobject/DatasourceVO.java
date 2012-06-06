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
package org.nuclos.server.report.valueobject;

import java.util.Date;

import org.nuclos.common.ModuleProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Value object representing a datasource.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class DatasourceVO extends NuclosValueObject {

	/** @todo refactor permissions - use typesafe enum */
	public static final int PERMISSION_NONE = 0;
	public static final int PERMISSION_READONLY = 1;
	public static final int PERMISSION_READWRITE = 2;

	/**
	 * Constants for ORDER BY clause of column definitions.
	 */
	public static enum OrderBy {
		NONE("keine"),
		ASCENDING("Aufsteigend"),
		DESCENDING("Absteigend");

		private final String sLabel;

		OrderBy(String sLabel) {
			this.sLabel = sLabel;
		}

		public String getLabel() {
			return this.sLabel;
		}

		@Override
		public String toString() {
			return this.getLabel();
		}

	}	// enum OrderBy

	/**
	 * Constants for GROUP BY clause of column definitions.
	 */
	public static enum GroupBy {
		NONE("keine"),
		GROUP("Gruppe"),
		SUM("Summe"),
		MIN("Minimum"),
		MAX("Maximum"),
		COUNT("Anzahl"),
		AVERAGE("Mittelwert");

		private final String sLabel;

		GroupBy(String sLabel) {
			this.sLabel = sLabel;
		}

		public String getLabel() {
			return this.sLabel;
		}

		@Override
		public String toString() {
			return this.getLabel();
		}

	}	// enum GroupBy

	private String sName;
	private String sDescription;
	private Boolean bValid;
	private String sDatasourceXML;
	private Integer nucletId;
	private final int iPermission;

	/**
	 * constructor used by server only
	 * @param evo contains the common fields.
	 * @param sName
	 * @param sDescription
	 * @param sDatasourceXML
	 * @param iPermission permission of actual principal for datasource
	 */
	public DatasourceVO(NuclosValueObject evo, String sName, String sDescription, Boolean bValid, String sDatasourceXML, Integer nucletId,
			int iPermission) {
		super(evo);
		setName(sName);
		setDescription(sDescription);
		setValid(bValid);
		setSource(sDatasourceXML);
		setNucletId(nucletId);
		this.iPermission = iPermission;
	}

	/**
	 * constructor used by client only
	 * @param sName
	 * @param sDescription
	 * @param sDatasourceXML
	 */
	public DatasourceVO(String sName, String sDescription, String sDatasourceXML, Boolean bValid, Integer nucletId) {
		super();
		setName(sName);
		setDescription(sDescription);
		setValid(bValid);
		setSource(sDatasourceXML);
		setNucletId(nucletId);
		this.iPermission = PERMISSION_NONE;
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

	public void setValid(Boolean bValid) {
		this.bValid = bValid;
	}

	public Boolean getValid() {
		return this.bValid;
	}

	public String getSource() {
		return sDatasourceXML;
	}

	public void setSource(String sDatasourceXML) {
		this.sDatasourceXML = sDatasourceXML;
	}

	public Integer getNucletId() {
		return nucletId;
	}

	public void setNucletId(Integer nucletId) {
		this.nucletId = nucletId;
	}
	
	/**
	 * @deprecated Only here to make BeanPropertyCollectableField.getProperty happy. 
	 * 		This is e.g. needed when defining a dynamic entity. (Thomas Pasch)
	 */
	public String getNuclet() {
		if (nucletId == null) return null;
		return nucletId.toString();
	}
	
	/**
	 * @deprecated Only here to make CollectableDataSource.setCollectableFieldUsingBeanProperty
	 * 		and AbstractCollectableBean.setProperty happy. This is e.g. needed when defining
	 * 		a dynamic entity. (Thomas Pasch)
	 */
	public void setNuclet(String nuclet) {
		// do nothing
	}

	public int getPermission() {
		return iPermission;
	}

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		if (StringUtils.isNullOrEmpty(this.getName())) {
			throw new CommonValidationException("datasource.error.validation.value");
		}
		if (StringUtils.isNullOrEmpty(this.getDescription())) {
			throw new CommonValidationException("datasource.error.validation.description");
		}
		if (StringUtils.isNullOrEmpty(this.getSource())) {
			throw new CommonValidationException("datasource.error.validation.datasourcexml");
		}
		for (MasterDataVO md : SpringApplicationContextHolder.getBean(ModuleProvider.class).getModules()) {
			if (this.getName().equals(md.getField("entity"))) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("datasource.error.validation.name", this.getName()));
//					"Es existiert ein Modul mit dem Namen: "+this.getDatasource()
//					+ " - der Name der Datenquelle darf nicht gleich dem Namen eines Moduls sein");
			}
		}

	}

	@Override
	public String toString(){
		return sName;
	}
	
	// dummy fields.
	public void setCreatedAt(Date createdAt) {
	}

	public void setCreatedBy(String createdBy) {
	}

	public void setChangedAt(Date changedAt) {
	}

	public void setChangedBy(String changedBy) {
	}

}	// class DatasourceVO
