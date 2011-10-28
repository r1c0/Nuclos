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
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.report.ByteArrayCarrier;

/**
 * Value object representing a report output definition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @version 01.00.00
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 */
public class ReportOutputVO extends NuclosValueObject {

	public static enum Destination implements KeyEnum<String>, Localizable {

		SCREEN("Screen", "reportDestination.screen"),
		FILE("File", "reportDestination.file");

		private final String value;
		private final String resourceId;

		Destination(String value, String resourceId) {
			this.value = value;
			this.resourceId = resourceId;
		}

		public String getLabel() {
			return this.value;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return this.getLabel();
		}

		@Override
        public String getResourceId() {
	        return resourceId;
        }
	}	// enum Destination


	public static enum Format implements KeyEnum<String> {

		PDF, XLS, CSV, DOC, TSV;

		@Override
		public String getValue() {
			return name();
		}
	}

	private Integer iReportId;
	private Format format;
	private Destination destination;
	private String sParameter;
	private String sSourceFile;

	private ByteArrayCarrier oSourceFileContent;

	private Integer iDatasourceId;
	private String sDatasource;

	private String sSheetName;

	private String sDescription;
	private ByteArrayCarrier oReportCLS;

	private String locale;

	private boolean bFirstOfMany = true;
	private boolean bLastOfMany = true;

	public ReportOutputVO(NuclosValueObject nvo, Integer iReportId, Format format, Destination destination,
			String sParameter, String sSourceFile, ByteArrayCarrier oReportCLS, ByteArrayCarrier oSourceFileContent,
			Integer iDatasourceId, String sDatasource, String sSheetname, String sDescription, String locale) {

		super(nvo);

		this.iReportId = iReportId;
		this.format = format;
		this.destination = destination;
		this.sParameter = sParameter;
		this.sSourceFile = sSourceFile;

		this.oReportCLS = oReportCLS;
		this.oSourceFileContent = oSourceFileContent;
		this.iDatasourceId = iDatasourceId;
		this.sDatasource = sDatasource;
		this.sSheetName = sSheetname;
		this.sDescription = sDescription;

		this.locale = locale;
	}

	/**
	 * Convenience constructor for printing search results; used by client only.
	 * @param sParameter
	 */
	public ReportOutputVO(Format format, Destination destination, String sParameter) {
		this(new NuclosValueObject(), null, format, destination, sParameter, null, null, null, null, null, null, null, null);
	}

	public Integer getReportId() {
		return iReportId;
	}

	public void setReportId(Integer iReportId) {
		this.iReportId = iReportId;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public Format getFormat() {
		return format;
	}

	public void setFormat(Format format) {
		this.format = format;
	}

	public String getParameter() {
		return sParameter;
	}

	public void setParameter(String sParameter) {
		this.sParameter = sParameter;
	}

	public String getSourceFile() {
		return sSourceFile;
	}

	public void setSourceFile(String sSourceFile) {
		this.sSourceFile = sSourceFile;
	}

	public ByteArrayCarrier getReportCLS() {
		return oReportCLS;
	}

	public void setReportCLS(ByteArrayCarrier oReportCLS) {
		this.oReportCLS = oReportCLS;
	}

	public Integer getDatasourceId() {
		return iDatasourceId;
	}

	public void setDatasourceId(Integer iDatasourceId) {
		this.iDatasourceId = iDatasourceId;
	}

	public String getDatasource() {
		return sDatasource;
	}

	public void setDatasource(String sDatasource) {
		this.sDatasource = sDatasource;
	}

	public String getSheetname() {
		return sSheetName;
	}

	public void setSheetname(String sSheetname) {
		this.sSheetName = sSheetname;
	}

	public ByteArrayCarrier getSourceFileContent() {
		return this.oSourceFileContent;
	}

	public void setSourceFileContent(ByteArrayCarrier oSourceFileContent) {
		this.oSourceFileContent = oSourceFileContent;
	}

	public boolean isFirstOfMany() {
		return bFirstOfMany;
	}

	public void setIsFirstOfMany(boolean bIsLastOfMany) {
		this.bFirstOfMany = bIsLastOfMany;
	}

	public boolean isLastOfMany() {
		return bLastOfMany;
	}

	public void setIsLastOfMany(boolean bIsLastOfMany) {
		this.bLastOfMany = bIsLastOfMany;
	}

	public String getDescription() {
		return sDescription;
	}

	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",srcFile=").append(getSourceFile());
		result.append(",ds=").append(getDatasource());
		result.append(",format=").append(getFormat());
		result.append(",locale=").append(getLocale());
		result.append("]");
		return result.toString();
	}

}	// class ReportOutputVO
