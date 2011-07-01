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

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.ByteArrayCarrier;

/**
 * Value object representing a subreport template.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 00.01.000
 */
public class SubreportVO extends NuclosValueObject {

	private Integer reportOutputId;
	private String parameter;
	private String sourcefileName;
	private ByteArrayCarrier sourcefileContent;
	private ByteArrayCarrier reportCLS;

	/**
	 * constructor used by client only
	 * @param iType
	 * @param sName
	 * @param sDescription
	 * @param iDataSourceId
	 */
	public SubreportVO(NuclosValueObject nvo, Integer reportOutputId, String parameter, String sourcefileName, ByteArrayCarrier sourcefileContent, ByteArrayCarrier reportCLS) {
		super(nvo);

		this.reportOutputId = reportOutputId;
		this.parameter = parameter;
		this.sourcefileName = sourcefileName;
		this.sourcefileContent = sourcefileContent;
		this.reportCLS = reportCLS;
	}

	/**
	 * creates a report vo from a suitable masterdata cvo.
	 * @param mdvo
	 */
	public SubreportVO(MasterDataVO mdvo) {
		this(mdvo.getNuclosValueObject(),
				(Integer) mdvo.getField("reportoutputId"),
				(String) mdvo.getField("parametername"),
				(String) mdvo.getField("sourcefilename"),
				(ByteArrayCarrier) mdvo.getField("sourcefileContent"),
				(ByteArrayCarrier) mdvo.getField("reportCLS"));
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getSourcefileName() {
		return sourcefileName;
	}

	public void setSourcefileName(String sourcefileName) {
		this.sourcefileName = sourcefileName;
	}

	public ByteArrayCarrier getSourcefileContent() {
		return sourcefileContent;
	}

	public void setSourcefileContent(ByteArrayCarrier sourcefileContent) {
		this.sourcefileContent = sourcefileContent;
	}

	public ByteArrayCarrier getReportCLS() {
		return reportCLS;
	}

	public void setReportCLS(ByteArrayCarrier reportCLS) {
		this.reportCLS = reportCLS;
	}

	public Integer getReportOutputId() {
		return reportOutputId;
	}

	@Override
	public void validate() throws CommonValidationException {
		if (StringUtils.isNullOrEmpty(this.getParameter())) {
			throw new CommonValidationException("report.error.validation.value");
		}
		if (StringUtils.isNullOrEmpty(this.getSourcefileName())) {
			throw new CommonValidationException("report.error.validation.description");
		}
	}

}	// class ReportVO
