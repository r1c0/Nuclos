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
package org.nuclos.common.valueobject;

import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.querybuilder.DatasourceUtils;
import org.nuclos.common.querybuilder.DatasourceXMLParser;
import org.nuclos.common.querybuilder.DatasourceXMLParser.XMLColumn;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.server.report.valueobject.DatasourceVO;

/**
 * DatasourceVO XML validator. Checks if the configured datasource could be used as a sub select statement.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version	01.00.00
 */
public class DatasourceVOValidator {

	protected static final Logger log = Logger.getLogger(DatasourceVOValidator.class);
	
	private final String intidAlias;
	private final DatasourceXMLParser.Result parseresult;

	public DatasourceVOValidator(DatasourceVO dVO) throws NuclosFatalException, NuclosDatasourceException{
		this(dVO, "ds_intid");
	}

	public DatasourceVOValidator(DatasourceVO dVO, String intidAlias) throws NuclosFatalException, NuclosDatasourceException{
		this.parseresult = parseDatasourceXML(dVO.getSource());
		this.intidAlias = intidAlias;
	}
	
	private DatasourceXMLParser.Result parseDatasourceXML(String sDatasourceXML) throws NuclosFatalException, NuclosDatasourceException {
		return DatasourceXMLParser.parse(sDatasourceXML);
	}
	
	/**
	 * 
	 * @return true if the configured datasource returns only an intid column.
	 */
	public boolean isValidIntIdSubSelect(List<String> columns) {
		boolean isValidResult = false;
		if(parseresult.isModelUsed()){
			isValidResult = hasSingleIntIdColumn() && !hasParameters();
		} else {
			isValidResult = isValidIntIdStringSubSelect(columns);
		}
		return isValidResult;
	}

	private boolean hasSingleIntIdColumn() {
		for(XMLColumn xmlColumn : parseresult.getLstColumns()){
			if(xmlColumn.getColumn() != null && xmlColumn.isVisible() && xmlColumn.getColumn().trim().equalsIgnoreCase("INTID")){			
				return true;
			}
		}
		return false;
	}
	
	private boolean hasParameters(){
		return parseresult.getLstParameters() != null && parseresult.getLstParameters().size() > 0;
	}
	
	public String getQueryString() {
		return parseresult.getQueryStringFromXml();
	}
	
	private boolean isValidIntIdStringSubSelect(List<String> columns) {
		boolean result = false; 
		for (String column : DatasourceUtils.getColumnsWithoutQuotes(columns)) {
			if (column.toLowerCase().contains(this.intidAlias)) {
				result = true;
				break;
			}
		}
		return result;
	}

}
