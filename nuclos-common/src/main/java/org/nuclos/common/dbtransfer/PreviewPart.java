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
package org.nuclos.common.dbtransfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PreviewPart implements Serializable{
	private static final long serialVersionUID = 1L;

	public static final int NEW = 1, CHANGE = 2, DELETE = 3, WARNING = 4, FAULT = 5;
	
	private int type = 0;
	private String table;
	private String entity;
	private int dataRecords = 0;
	private int warning = 0;

	private List<String> statements = new ArrayList<String>();
	
	public void setType(int type) {
		this.type = type;
	}
	
	public void setTypeOnlyOneTime(int type) {
		if (this.type == 0)
			this.type = type;
	}
	
	public int getType() {
		return this.type;
	}

	public String getTable() {
    	return table;
    }

	public void setTable(String table) {
    	this.table = table;
    }
	
	public String getEntity() {
    	return entity;
    }

	public void setEntity(String entity) {
    	this.entity = entity;
    }
	
	public void addStatement(String statement) {
		this.statements.add(statement);
	}

	public List<String> getStatements() {
    	return statements;
    }

	public int getDataRecords() {
    	return dataRecords;
    }

	public void setDataRecords(int dataRecords) {
    	this.dataRecords = dataRecords;
    }
	
	public int getWarning() {
		return warning;
	}

	public void setWarning(int warning) {
		this.warning = warning;
	}

	@Override
    public String toString() {
	    return entity!=null?entity:""+"-"+table!=null?table:"";
    }
	
}
