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

/**
 * Value object representing a datasource parameter.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class DatasourceParameterVO extends NuclosValueObject {

	private Integer iDatasourceId;
	private String sParameter;
	private String sDescription;
	private String sDatatype;
	private DatasourceParameterValuelistproviderVO valuelistprovider;

	protected DatasourceParameterVO(Integer iId, Integer iDatasourceId, String sParameter, String sDatatype, String sDescription, java.util.Date dCreated, String sCreated, java.util.Date dChanged, String sChanged, Integer iVersion) {
		super(iId, dCreated, sCreated, dChanged, sChanged, iVersion);
		setDatasourceId(iDatasourceId);
		setDatatype(sDatatype);
		setParameter(sParameter);
		setDescription(sDescription);
	}

	public DatasourceParameterVO(Integer iDatasourceId, String sParameter, String sDatatype, String sDescription) {
		super();
		setDatasourceId(iDatasourceId);
		setParameter(sParameter);
		setDescription(sDescription);
		setDatatype(sDatatype);
	}

	public Integer getDatasourceId() {
		return iDatasourceId;
	}

	public void setDatasourceId(Integer iDatasourceId) {
		this.iDatasourceId = iDatasourceId;
	}

	public String getParameter() {
		return sParameter;
	}

	public void setParameter(String sParameter) {
		this.sParameter = sParameter;
	}

	public String getDatatype() {
		return sDatatype;
	}

	public void setDatatype(String sDatatype) {
		this.sDatatype = sDatatype;
	}

	public String getDescription() {
		return sDescription;
	}

	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	public DatasourceParameterValuelistproviderVO getValueListProvider() {
		return this.valuelistprovider;
	}

	public void setValueListProvider(DatasourceParameterValuelistproviderVO valuelistprovider) {
		this.valuelistprovider = valuelistprovider;
	}

	@Override
	public void validate() throws CommonValidationException {
		if (getDescription() != null && getDescription().trim().equals("")) {
			setDescription(null);
		}
		if (getParameter() == null || getParameter().trim().equals("")) {
			throw new CommonValidationException("datasource.parameter.error.validation.name");//"Ung\u00fcltige Parameter Definition\nDas Feld \"Parameter Name\" darf nicht leer sein.");
		}

		if (getParameter().indexOf("'") >= 0 || getParameter().indexOf("\"") >= 0 ||
				getParameter().indexOf(" ") >= 0 || getParameter().indexOf("$") >= 0 ||
				getParameter().indexOf("!") >= 0 || getParameter().indexOf("%") >= 0 ||
				getParameter().indexOf("_") >= 0) {
			throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("datasource.parameter.error.validation.value", getParameter()));
				//"Der Parameter " + getParameter() + " enth\u00e4lt ung\u00fcltige Zeichen.");
		}

		if (getDatatype() == null || getDatatype().trim().equals("")) {
			throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("datasource.parameter.error.validation.definition", getParameter()));
				//"Ung\u00fcltige Definition des Parameters " + getParameter() + "\nDas Feld \"Datentyp\" darf nicht leer sein.");
		}
	}
	
	@Override
	public String toString() {
		return  StringUtils.emptyIfNull(this.sParameter);
	}
}
