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

/**
 * TODO: There is also org.nuclos.common2.File.
 * Perhaps there should be only one. (tp)
 */
public class NuclosFile implements Serializable {
		
	private String fileName;
	private byte[] fileContents;
	
	public NuclosFile(String fileName, byte[] fileContents) {
		super();
		this.fileName = fileName;
		this.fileContents = fileContents;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public byte[] getFileContents() {
		return fileContents;
	}
	public void setFileContents(byte[] fileContents) {
		this.fileContents = fileContents;
	}
	
	
}
