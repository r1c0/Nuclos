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
package org.nuclos.common.fileimport;

/**
 * Value object for usgae definitions of import structures
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class NuclosFileImportStructureUsage {

	private Integer order;
	private String structure;

	public NuclosFileImportStructureUsage(Integer order, String structure) {
		this.order = order;
		this.structure = structure;
	}

	public Integer getOrder() {
    	return order;
    }

	public void setOrder(Integer order) {
    	this.order = order;
    }

	public String getStructure() {
    	return structure;
    }

	public void setStructure(String structure) {
    	this.structure = structure;
    }
}
