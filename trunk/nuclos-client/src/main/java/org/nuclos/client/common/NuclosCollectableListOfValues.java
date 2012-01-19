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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.datasource.NuclosSearchConditionUtils;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;

/**
 * A <code>CollectableListOfValues</code> in Nucleus. The LOV (referencing) listener is installed right here.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class NuclosCollectableListOfValues extends CollectableListOfValues {

	/**
	 * Defines whether the opened search controller filters its entries for validity and active flag.
	 */
	public static final String PROPERTY_FILTER_VALIDITY = "filterValidity";
	
	private boolean bFilterValidity = false;
	protected Map<String, Object> parameter = new HashMap<String, Object>();

	/**
	 * @param clctef
	 * @param bSearchable
	 * @precondition clctef != null
	 * @precondition clctef.isIdField()
	 * @precondition clctef.isReferencing()
	 * @postcondition this.getReferencingListener() != null
	 */
	public NuclosCollectableListOfValues(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, bSearchable);
		this.setReferencingListener(NuclosLOVListener.getInstance());
	}

	/**
	 * Defines whether the opened search controller shall filter its entries for validity and active flag.
	 * @param bFilter
	 */
	public void setFilterValidity(boolean bFilter) {
		bFilterValidity = bFilter;
	}

	public boolean getFilterValidity() {
		return bFilterValidity;
	}

	@Override
    public void applyParameters() throws CommonBusinessException {
		super.applyParameters();
		String sDatasourceName = (String) this.getProperty("datasource");
		if(sDatasourceName != null && hasValidDatasourceParameters()){
			this.setCollectableSearchCondition(NuclosSearchConditionUtils.initPlainSubCondition(sDatasourceName, this.parameter));
		} else {
			this.setCollectableSearchCondition(null);
		}
	}

	private boolean hasValidDatasourceParameters() {
		String sDatasourceParameterNames = (String) this.getProperty("datasource_parameter");
		StringTokenizer st = new StringTokenizer(sDatasourceParameterNames, ",");
		String paramName;
		while(st.hasMoreElements()){
			paramName = (String)st.nextElement();
			if(paramName == null || !parameter.containsKey(paramName) || parameter.get(paramName) == null){
				return false;
			}
		}
		return true;
	}

	@Override
    public void setParameter(String sName, Object oValue) {
		super.setParameter(sName, oValue);
		parameter.put(sName, oValue);
	}
}	// class NuclosCollectableListOfValues
