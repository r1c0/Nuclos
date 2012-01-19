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
package org.nuclos.client.datasource.querybuilder;

import java.util.ArrayList;
import java.util.List;

import org.nuclos.server.report.valueobject.DatasourceParameterVO;


/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class QueryBuilderConstants {
	
	protected static final String SYSTEMID = "http://www.novabit.de/technologies/querybuilder/querybuildermodel.dtd";
	protected static final String RESOURCE_PATH = "org/nuclos/common/querybuilder/querybuildermodel.dtd";

	protected static final String COMPARISON_OPERATORS[] = {"=", "<", "<=", ">", ">=", "<>", "!=", "between", "like"};
	protected static final String LOGICAL_OPERATORS[] = {"and", "or", "not"};
	protected static final String DOCTYPE = "querybuildermodel";

	protected static final String TAG_HEADER = "header";
	protected static final String TAG_ENTITYOPTIONS = "entityoptions";
	protected static final String TAG_TABLES = "tables";
	protected static final String TAG_CONNECTORS = "connectors";
	protected static final String TAG_COLUMNS = "columns";
	protected static final String TAG_PARAMETERS = "parameters";

	protected static final String TAG_TABLE = "table";
	protected static final String TAG_CONNECTOR = "connector";
	protected static final String TAG_COLUMN = "column";
	protected static final String TAG_CONDITION = "condition";
	protected static final String TAG_PARAMETER = "parameter";
	protected static final String TAG_SQL = "sql";

	protected static final String TAG_VALUELISTPROVIDER = "valuelistprovider";
	protected static final String TAG_VALUELISTPROVIDER_PARAMETER = "vlpparameter";
	
	public static final String PARAMETER_TYPE_STRING = "java.lang.String";
	public static final String PARAMETER_TYPE_INTEGER = "java.lang.Integer";
	public static final String PARAMETER_TYPE_DOUBLE = "java.lang.Double";
	public static final String PARAMETER_TYPE_BOOLEAN = "java.lang.Boolean";
	public static final String PARAMETER_TYPE_DATE = "java.util.Date";
	
	public static final String PARAMETER_USERNAME_NAME = "username";
	public static final DatasourceParameterVO PARAMETER_USERNAME = new DatasourceParameterVO(null, PARAMETER_USERNAME_NAME, PARAMETER_TYPE_STRING, PARAMETER_USERNAME_NAME);
	
	public static final List<String> RECORDGRANT_SYSTEMPARAMETER_NAMES = new ArrayList<String>();
	public static final List<DatasourceParameterVO> RECORDGRANT_SYSTEMPARAMETERS = new ArrayList<DatasourceParameterVO>();
	static {
		RECORDGRANT_SYSTEMPARAMETER_NAMES.add(PARAMETER_USERNAME_NAME);
		RECORDGRANT_SYSTEMPARAMETERS.add(PARAMETER_USERNAME);
	}
	

}
