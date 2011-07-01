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
package org.nuclos.client.masterdata.valuelistprovider;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common2.exception.CommonBusinessException;

public class ParametersCollectableFieldsProvider implements CollectableFieldsProvider {

	private static final Logger log = Logger.getLogger(ParametersCollectableFieldsProvider.class);
	
	private final String parameterClassName = "showClass";
	private final String parameterValueName = "showValue";
	private final String default_parameterClassName = "java.lang.String";
	
	private List<String> params = null;
	private String showClass = default_parameterClassName;
	
	public ParametersCollectableFieldsProvider(){
		params = new ArrayList<String>();
	}

	@Override
	public void setParameter(String sName, Object oValue) {
		if(parameterClassName.equalsIgnoreCase(sName)){
			showClass = (String) oValue;
		} else {
			if(parameterValueName.equalsIgnoreCase(sName)){
				params.add((String)oValue);
			}
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");

		List<CollectableField> result = new ArrayList<CollectableField>();
		try {
			Class<?> clazz = Class.forName(this.showClass);
			Class<?>[] constructorAttrs = new Class[1];
			constructorAttrs[0] = java.lang.String.class;
			for (String i : this.params){
				result.add(new CollectableValueField(clazz.getConstructor(constructorAttrs).newInstance(i)));
			}			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new CommonBusinessException("Invalid parameter: "+e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new CommonBusinessException("Invalid parameter: "+e.getMessage());
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new CommonBusinessException("Invalid parameter: "+e.getMessage());
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new CommonBusinessException("Invalid parameter: "+e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new CommonBusinessException("Invalid parameter: "+e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new CommonBusinessException("Invalid parameter: "+e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new CommonBusinessException("Invalid parameter: "+e.getMessage());
		}

		Collections.sort(result);

		return result;	}

}
