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
package org.nuclos.client.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.valuelistprovider.cache.CacheableCollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * Collectable fields provider retrieving its values from a given datasource with 
 * a specified set of datasource parameters.
 * 
 * Valid parameters for this collectable field provider are:
 * datasource - name of this datasource for use in detail mode
 * datasource-search - name of the datasource for use in search mode; if not specified, "datasource" is used there, too
 * fieldname - name of the result column of the datasource to use as the source of values
 * id-fieldname - name of the result column of the datasource to use as the source of ids
 * _searchmode - automatically set by the component, depending on the use of the component as search or details component
 * 
 * Every other parameter is given to the datasource as parameter for it. 
 * These parameters must be specified in the datasource to take effect.
 *  
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 * 
 * TODO: validating given parameters and return fields - depends on better interface for datasources
 * (e.g. information about contained result fields, parameters etc. without the need to parse XML)
 */
public class DatasourceBasedCollectableFieldsProvider implements CacheableCollectableFieldsProvider {

	private boolean bSearchmode = false;
	private String sValueFieldName = null;
	private String sIdFieldName = null;
	private DatasourceVO dsvo = null;
	private DatasourceVO dsvoSearch = null;
	private final Map<String, Object> mpParameters = new HashMap<String, Object>();
	private List<DatasourceParameterVO> collParameters;
	
	private final boolean isValuelistProviderDatasource;
	
	public DatasourceBasedCollectableFieldsProvider(boolean isValuelistProviderDatasource) {
		this.isValuelistProviderDatasource = isValuelistProviderDatasource;
	}
	
	public boolean isValuelistProviderDatasource() {
		return isValuelistProviderDatasource;
	}

	/**
	 * Every parameter except datasource and _searchmode will be transferred to the specified datasource.
	 */ 
	@Override
	public void setParameter(String sName, Object oValue) {
		if (isValuelistProviderDatasource) {
			if (sName.equals("valuelistProvider") && (dsvo == null || !dsvo.getName().equals(oValue))) {
				try {
					String sValuelistProviderDatasourceName = (String) oValue;
					dsvo = DatasourceDelegate.getInstance().getValuelistProviderByName(sValuelistProviderDatasourceName);
					collParameters = DatasourceDelegate.getInstance().getParametersFromXML(dsvo.getSource());
				}
				catch (Exception e) {
					throw new CommonFatalException(
							CommonLocaleDelegate.getInstance().getMessage(
									"datasource.collectable.fieldsprovider", "Fehler beim Laden der Datenquelle ''{0}'':\n", oValue), e);
					//"Fehler beim Laden der Datenquelle \"" + oValue + "\":\n", e);
				}
			}
		} else {
			if (sName.equals("datasource") && (dsvo == null || !dsvo.getName().equals(oValue))) {
				try {
					String sDatasourceName = (String) oValue;
					dsvo = DatasourceDelegate.getInstance().getDatasourceByName(sDatasourceName);
					collParameters = DatasourceDelegate.getInstance().getParametersFromXML(dsvo.getSource());
				}
				catch (CommonBusinessException e) {
					throw new CommonFatalException(
							CommonLocaleDelegate.getInstance().getMessage(
									"datasource.collectable.fieldsprovider", "Fehler beim Laden der Datenquelle ''{0}'':\n", oValue), e);
					//"Fehler beim Laden der Datenquelle \"" + oValue + "\":\n", e);
				}
			}
			else if (sName.equals("datasource-search") && (dsvoSearch == null || !dsvoSearch.getName().equals(oValue))) {
				try {
					String sDatasourceName = (String) oValue;
					dsvoSearch = DatasourceDelegate.getInstance().getDatasourceByName(sDatasourceName);
					collParameters = DatasourceDelegate.getInstance().getParametersFromXML(dsvoSearch.getSource());
				}
				catch (CommonBusinessException e) {
					throw new CommonFatalException(
							CommonLocaleDelegate.getInstance().getMessage(
									"datasource.collectable.fieldsprovider", "Fehler beim Laden der Datenquelle ''{0}'':\n", oValue), e);
						//"Fehler beim Laden der Datenquelle \"" + oValue + "\":\n", e);
				}
			}
		}
		
		if(sName.equals("fieldname")) {
			// TODO: validate existence
			sValueFieldName = (String) oValue;
		}
		else if(sName.equals("id-fieldname")) {
			// TODO: validate existence
			sIdFieldName = (String) oValue;
		}
		else if(sName.equals("_searchmode")) {
			bSearchmode = (Boolean) oValue;
		}
		// NUCLEUSINT-1077
		else if (collParameters != null) {
			for(DatasourceParameterVO dpvo : collParameters) {
				if(dpvo.getParameter().equals(sName)) {
					Object oConvertedValue = oValue;
					
					// convert non-String parameters to correct datatype
					// TODO: this must be possible to perform nicer or at least somewhere central...!
					if (oValue == null) {
						if(String.class.getName().equals(dpvo.getDatatype())) {
							oConvertedValue = "";
						}
						else if(Integer.class.getName().equals(dpvo.getDatatype())) {
							oConvertedValue = 0;
						}					
					}
					else if (!oValue.getClass().getName().equals(dpvo.getDatatype())) {
						if(String.class.getName().equals(dpvo.getDatatype())) {
							oConvertedValue = oValue.toString();
						}
						else if(Integer.class.getName().equals(dpvo.getDatatype())) {
							oConvertedValue = Integer.parseInt(oValue.toString());
						}
					}

					mpParameters.put(sName, oConvertedValue);
					break;
				}
			}
			
		}
	}

	@Override
	public Object getCacheKey() {
		DatasourceVO dsvo = getDatasourceVO();
		if (dsvo != null) {
			return Arrays.asList(
				dsvo.getName(),
				sValueFieldName,
				sIdFieldName,
				new HashMap<String, Object>(mpParameters));
		}
		return null;
	}
	
	public DatasourceVO getDatasourceVO() {
		return (bSearchmode && dsvoSearch != null)? dsvoSearch : dsvo;
	}
	
	/**
	 * This provider caches its entries as long as datasource and parameters stay the same.
	 * Perhaps this may not be correct in some cases. 
	 */
	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		List<CollectableField> lstFields = new ArrayList<CollectableField>();
		
		final DatasourceVO dsvoUsed = getDatasourceVO();
		if(dsvoUsed != null) {
			
			// Create copy and set all parameters not specified so far with null value to complete SQL statement in datasource
			Map<String, Object> queryParams = new HashMap<String, Object>(mpParameters);
			for(DatasourceParameterVO dpvo : collParameters) {
				if(queryParams.get(dpvo.getParameter()) == null) {
					queryParams.put(dpvo.getParameter(), null);
				}
			}
			
			// refresh list from datasource
			ResultVO result = DatasourceDelegate.getInstance().executeQuery(dsvoUsed.getSource(), queryParams, null);
			int iIndexValue = -1;
			int iIndexId = -1;
			List<ResultColumnVO> columns = result.getColumns();
			for (int iIndex = 0; iIndex < columns.size(); iIndex++) {
				ResultColumnVO rcvo = columns.get(iIndex);
				if(rcvo.getColumnLabel().equalsIgnoreCase(sValueFieldName)) {
					iIndexValue = iIndex;
				}
				// Note: the "else" here has the effect that the sIdFieldName lookup is skipped if
				// it's equal to sValueFieldName => no id index which (=> plain-vanilla value fields)
				else if(rcvo.getColumnLabel().equalsIgnoreCase(sIdFieldName)) {
					iIndexId = iIndex;
				}
			}
			
			for(Object[] oValue : result.getRows()) {
				if (oValue[iIndexValue] != null && (iIndexId == -1 || oValue[iIndexId] != null)) {
					if(iIndexId == -1) {
						lstFields.add(new CollectableValueField(oValue[iIndexValue]));
					}
					else {
						Integer iId = ((Number) (oValue[iIndexId])).intValue();
						lstFields.add(new CollectableValueIdField(iId, oValue[iIndexValue]));
					}
				}
			}
			// The result can be sorted by a SQL 'ORDER BY'.
			//
			// For example for value list provider used as a ComboBox in a subform,
			// the data source defined should contain an order by if needed.
			//
			// It should never been sorted here by name. (tp)
			// Plain wrong:
			// Collections.sort(lstFields);
		}
		return lstFields;
	}

	/**
     * @return the mpParameters
     */
    public Map<String, Object> getValueListParameter() {
	    return mpParameters;
    }
}
