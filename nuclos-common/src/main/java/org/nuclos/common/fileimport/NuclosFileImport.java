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

import java.util.ArrayList;
import java.util.List;

import org.nuclos.common.NuclosFile;

/**
 * Value object for fileimport
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class NuclosFileImport {

	private NuclosFile file;

	private List<NuclosFileImportStructureUsage> structures = new ArrayList<NuclosFileImportStructureUsage>();
	
	private ImportMode mode = ImportMode.NUCLOSIMPORT;
	
	private Boolean atomic = true;
	
	private String description;

	public NuclosFileImport(NuclosFile file) {
		this.file = file;
	}

	public NuclosFile getFile() {
    	return file;
    }

	public void setFile(NuclosFile file) {
    	this.file = file;
    }

	public List<NuclosFileImportStructureUsage> getStructures() {
    	return structures;
    }

	public void addStructure(NuclosFileImportStructureUsage structure) {
		this.structures.add(structure);
	}
	
	public ImportMode getMode() {
		return mode;
	}
	
	public void setMode(ImportMode mode) {
		this.mode = mode;
	}
	
	public Boolean getAtomic() {
		return atomic;
	}
	
	public void setAtomic(Boolean atomic) {
		this.atomic = atomic;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
