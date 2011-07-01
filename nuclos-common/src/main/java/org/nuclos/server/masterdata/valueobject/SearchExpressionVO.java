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

import org.nuclos.server.common.valueobject.NuclosValueObject;

public class SearchExpressionVO extends NuclosValueObject {

	
	private String sFieldName;
	private String sOperator;
	private String sFieldValue;
	private String sFieldValueId;
	
	
	public SearchExpressionVO(String sFieldName,String sOperator, String sFieldValue) {
		this.sFieldName = sFieldName;
		this.sOperator = sOperator;
		this.sFieldValue = sFieldValue;
	}
	public SearchExpressionVO(String sFieldName,String sOperator, String sFieldValue,String sFieldValueId) {
		this.sFieldName = sFieldName;
		this.sOperator = sOperator;
		this.sFieldValue = sFieldValue;
		this.sFieldValueId = sFieldValueId;
	}
	
	
	public String getFieldName() {
		return sFieldName;
	}
	public void setFieldName(String fieldName) {
		sFieldName = fieldName;
	}
	public String getOperator() {
		return sOperator;
	}
	public void setOperator(String operator) {
		sOperator = operator;
	}
	public String getFieldValue() {
		return sFieldValue;
	}
	public void setFieldValue(String fieldValue) {
		sFieldValue = fieldValue;
	}
	public String getFieldValueId() {
		return sFieldValueId;
	}
	public void setFieldValueId(String fieldValueId) {
		sFieldValueId = fieldValueId;
	}
}
