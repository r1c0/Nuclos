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
package org.nuclos.server.masterdata.valueobject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterDataWithDependantsVOWrapper extends MasterDataWithDependantsVO {

	//only wrapper data exist
	public final static int IS_WRAPPED = 0;
	//both wrapper and native data exist
	public final static int IS_MAPPED = 1;
	//only native data exist
	public final static int IS_NATIVE = 2;

	/**
	 * Map<String, Object>
	 */
	private Map<String, Object> wrapperFields;
	private List<String> wrapperFieldNames;
	private int type = -1;

	public MasterDataWithDependantsVOWrapper(MasterDataVO mdvo, DependantMasterDataMap mpDependants, List<String> pWrapperFieldNames) {
		super(mdvo, mpDependants);
		this.wrapperFields = new HashMap<String, Object>();
		this.wrapperFieldNames = pWrapperFieldNames;
	}

	public MasterDataWithDependantsVOWrapper(MasterDataVO mdvo, DependantMasterDataMap mpDependants, List<String> pWrapperFieldNames, Map<String, Object> pWrapperFields) {
		this(mdvo, mpDependants, pWrapperFieldNames);
		wrapperFields = pWrapperFields;
	}

	public Map<String, Object> getWrapperFields() {
		return wrapperFields;
	}

	public void addWrapperField(String key, Object value){
		this.wrapperFields.put(key, value);
	}

	public boolean hasEqualWrapperFields(){
		boolean equal = true;
		Object wrapperField = null;
		Object nativeField = null;
		for(String fieldName : wrapperFieldNames){
			wrapperField = wrapperFields.get(fieldName);
			nativeField = getField(fieldName);
			if(wrapperField == null || nativeField == null || !wrapperField.equals(nativeField)){
				return false;
			}
		}
		return equal;
	}

	public void replaceNativeFields(){
		Object wrapperField = null;
		for(String fieldName : wrapperFieldNames){
			wrapperField = wrapperFields.get(fieldName);
			if(wrapperField != null){
				setField(fieldName, wrapperField);
			}
		}
	}

	public void setWrapperFields(Map<String, Object> wrapperFields) {
		this.wrapperFields = wrapperFields;
	}

	public boolean isWrapped() {
		return (getType() == IS_WRAPPED);
	}

	public boolean isMapped(){
		return (getType() == IS_MAPPED);
	}

	public boolean isNative(){
		return (getType() == IS_NATIVE);
	}

	public int getType() {
		return type;
	}

	private void setType(int type) {
		this.type = type;
	}

	public void setIsMapped(){
		setType(IS_MAPPED);
	}

	public void setIsWrapped(){
		setType(IS_WRAPPED);
	}

	public void setIsNative(){
		setType(IS_NATIVE);
	}

	public List<String> getMappedFields() {
		return wrapperFieldNames;
	}
	
	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append("MdwdVOWrapped[id=").append(getId());
		if (isChanged()) {
			result.append(",changed=").append(isChanged());
		}
		if (isSystemRecord()) {
			result.append(",sr=").append(isSystemRecord());
		}
		result.append(",fields=").append(getFields());
		result.append(",type").append(type);
		result.append(",wrapped=").append(wrapperFields);
		final DependantMasterDataMap deps = getMdDependants();
		if (deps != null && !deps.isEmpty()) {
			result.append(",deps=").append(deps);
		}
		final DependantMasterDataMap deps2 = getDependants();
		if (deps2 != null && !deps2.isEmpty()) {
			result.append(",deps2=").append(deps2);
		}
		result.append("]");
		return result.toString();
	}
}
