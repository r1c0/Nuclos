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
package org.nuclos.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public interface LafParameter<T> {
	
	public static final String VALUE_POSITION_TOP = "nuclos_LAF_Position_top";
	public static final String VALUE_POSITION_BOTTOM = "nuclos_LAF_Position_bottom";
	
	public static final Map<String, LafParameter<?>> PARAMETERS = new HashMap<String, LafParameter<?>>();
	
	static final LafParameterStorage[] ALL_STORAGES = {LafParameterStorage.SYSTEMPARAMETER, LafParameterStorage.WORKSPACE, LafParameterStorage.ENTITY};
		
	public static final LafParameter<Boolean> nuclos_LAF_Details_Overlay = new LafParameterImpl<Boolean>("nuclos_LAF_Details_Overlay", false, ALL_STORAGES);
	public static final LafParameter<Integer> nuclos_LAF_Result_Dynamic_Actions_Fixed_Height = new LafParameterImpl<Integer>("nuclos_LAF_Result_Dynamic_Actions_Fixed_Height", -1, ALL_STORAGES);
	public static final LafParameter<String> nuclos_LAF_Result_Dynamic_Actions_Position = new LafParameterImpl<String>("nuclos_LAF_Result_Dynamic_Actions_Position", VALUE_POSITION_BOTTOM, 
			new String[]{VALUE_POSITION_TOP, VALUE_POSITION_BOTTOM}, ALL_STORAGES);
	public static final LafParameter<String> nuclos_LAF_Result_Selection_Buttons_Position = new LafParameterImpl<String>("nuclos_LAF_Result_Selection_Buttons_Position", VALUE_POSITION_BOTTOM, 
			new String[]{VALUE_POSITION_TOP, VALUE_POSITION_BOTTOM}, ALL_STORAGES);
	
	public String toString();
	
	public String getName();
	
	public T getDefault();
	
	public Class<T> getParameterClass();
	
	public T[] getFixedValueList();
	
	public T parse(String value);
	
	public boolean isStoragePossible(LafParameterStorage storage);
	
	public static class LafParameterImpl<T> implements LafParameter<T>, Serializable {
		
		private static final long serialVersionUID = 2560045731145642607L;

		private static final Logger LOG = Logger.getLogger(LafParameterImpl.class);
		
		private final String name;
		
		private final T defaultValue;
		
		private final LafParameterStorage[] storages;
		
		private final T[] fixedValueList;
		
		public LafParameterImpl(String name, T defaultValue, LafParameterStorage...storages) {
			this(name, defaultValue, null, storages);
		}

		public LafParameterImpl(String name, T defaultValue, T[] fixedValueList, LafParameterStorage...storages) {
			assert defaultValue != null;
			
			this.name = name;
			this.defaultValue = defaultValue;
			this.fixedValueList = fixedValueList;
			this.storages = storages;
			PARAMETERS.put(name, this);
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public String getName() {
			return name;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Class<T> getParameterClass() {
			return (Class<T>) defaultValue.getClass();
		}
		
		public T getDefault() {
			return defaultValue;
		}
		
		@Override
		public T[] getFixedValueList() {
			return fixedValueList;
		}
		
		public boolean isStoragePossible(LafParameterStorage storage) {
			for (int i = 0; i < storages.length; i++) {
				if (storages[i] == storage) {
					return true;
				}
			}
			return false;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T parse(String value) {
			if (value == null)
				return null;
			
			try {
				if (getDefault().getClass() == String.class) {
					return (T) value;
				} else if (getDefault().getClass() == boolean.class || getDefault().getClass() == Boolean.class) {
					return (T) new Boolean(Boolean.parseBoolean(value));
				} else if (getDefault().getClass() == int.class || getDefault().getClass() == Integer.class) {
					return (T) new Integer(Integer.parseInt(value));
				} else {
					LOG.error(String.format("Parameter %s: Parse not implemented!", this, value));
					return null;
				}
			} catch (Exception ex) {
				LOG.error(String.format("Parameter %s: Value %s could not be cast!", this, value), ex);
				return null;
			}
		}	

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} 
			if (obj instanceof LafParameterImpl<?>) {
				return name.equals(((LafParameterImpl<?>) obj).getName());
			}
			return super.equals(obj);
		}	
		
	}
}
