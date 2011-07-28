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

import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.Localizable;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common.security.Permission;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Value object representing a report definition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class ReportVO extends NuclosValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum OutputType implements KeyEnum<String>, Localizable {
		// Note that the order of these constants is significant.

		/** single report */
		SINGLE("Single", "reportOutputType.single"),

		/** collective report: multiple reports that go to different files, but are run as one report. */
		COLLECTIVE("Collective", "reportOutputType.collective"),

		/** Excel report: multiple reports go to different sheets in the same Excel file */
		EXCEL("Excel", "reportOutputType.excel");

		private final String value;
		private final String resourceId;
		
		private OutputType(String value, String resourceId) {
			this.value = value;
			this.resourceId = resourceId;
		}
		
		@Override
		public String getValue() {
			return this.value;
		}
		
		@Override
		public String getResourceId() {
			return resourceId;
		}
	}	// enum OutputType

	public static enum ReportType implements KeyEnum<Integer>, Localizable {
		REPORT(0, "reportType.report"),
		FORM(1, "reportType.form");

		private final Integer id;
		private final String resourceId;

		private ReportType(int id, String resourceId) {
			this.id = id;
			this.resourceId = resourceId;
		}

		@Override
		public Integer getValue() {
			return id;
		}
		
		@Override
		public String getResourceId() {
			return resourceId;
		}
	}

	private ReportType type;
	private Integer iDataSourceId;
	private String sName;
	private String sDescription;
	private String sOutputFileName;
	private OutputType outputType;

	/**
	 * Convenience constructor for output of search result lists; used by client only.
	 * @param sName
	 */
	public ReportVO(String sName) {
		this(null, sName, null, null, OutputType.SINGLE);
	}

	/**
	 * constructor used by client only
	 * @param iType
	 * @param sName
	 * @param sDescription
	 * @param iDataSourceId
	 */
	public ReportVO(ReportType type, String sName, String sDescription, Integer iDataSourceId, OutputType outputType) {

		super();

		this.type = type;
		this.sName = sName;
		this.sDescription = sDescription;
		this.iDataSourceId = iDataSourceId;
		this.outputType = outputType;
	}

	/**
	 * constructor used by server only
	 * @param nvo contains the common fields.
	 * @param iType
	 * @param sName
	 * @param sDescription
	 * @param iDataSourceId
	 * @param permission
	 * @precondition permission != null
	 */
	public ReportVO(NuclosValueObject nvo, ReportType type, String sName, String sDescription, Integer iDataSourceId,
			String sOutputtype, Permission permission) {

		super(nvo);

		this.type = type;
		this.sName = sName;
		this.sDescription = sDescription;
		this.iDataSourceId = iDataSourceId;
		this.outputType = KeyEnum.Utils.findEnum(OutputType.class, sOutputtype);
	}

	/**
	 * creates a report vo from a suitable masterdata cvo.
	 * @param mdvo
	 */
	public ReportVO(MasterDataVO mdvo) {
		this(mdvo.getNuclosValueObject(),
				null,
				(String) mdvo.getField("name"),
				(String) mdvo.getField("description"),
				(Integer) mdvo.getField("datasourceId"),
				(String) mdvo.getField("outputtype"),
				Permission.NONE);
	}

	public ReportType getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(ReportType type) {
		this.type = type;
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

	public Integer getDatasourceId() {
		return iDataSourceId;
	}

	public void setDatasourceId(Integer iDatasourceId) {
		this.iDataSourceId = iDatasourceId;
	}

	public OutputType getOutputType() {
		return outputType;
	}

	public void setOutputType(OutputType outputtype) {
		this.outputType = outputtype;
	}

	public String getOutputFileName() {
		return sOutputFileName;
	}

	public void setOutputFileName(String sOutputFileName) {
		this.sOutputFileName = sOutputFileName;
	}

	@Override
	public void validate() throws CommonValidationException {
		if (StringUtils.isNullOrEmpty(this.getName())) {
			throw new CommonValidationException("report.error.validation.value");
		}
		if (StringUtils.isNullOrEmpty(this.getDescription())) {
			throw new CommonValidationException("report.error.validation.description");
		}
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",name=").append(getName());
		result.append(",dsId=").append(getDatasourceId());
		result.append("]");
		return result.toString();
	}

}	// class ReportVO
