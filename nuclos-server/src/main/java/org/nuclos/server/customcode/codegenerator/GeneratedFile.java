//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.server.customcode.codegenerator;

import java.io.File;
import java.util.Date;

/**
 * @author Thomas Pasch
 */
class GeneratedFile {
	
	private File file;
	
	private String name;
	
	private String targetClassName;
	
	private String type;
	
	private String generatorClass;
	
	private long id = -1L;
	
	private int version = -1;
	
	private long modified;
	
	private char[] content;
	
	GeneratedFile() {
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	String getType() {
		return type;
	}

	void setType(String type) {
		this.type = type;
	}

	String getGeneratorClass() {
		return generatorClass;
	}

	void setGeneratorClass(String generatorClass) {
		this.generatorClass = generatorClass;
	}

	long getId() {
		return id;
	}

	void setId(long id) {
		this.id = id;
	}

	int getVersion() {
		return version;
	}

	void setVersion(int version) {
		this.version = version;
	}

	long getModified() {
		return modified;
	}

	void setModified(long modified) {
		this.modified = modified;
	}

	char[] getContent() {
		return content;
	}

	void setContent(char[] content) {
		this.content = content;
	}

	File getFile() {
		return file;
	}

	void setFile(File file) {
		this.file = file;
	}
	
	String getTargetClassName() {
		return targetClassName;
	}

	void setTargetClassName(String targetClassName) {
		this.targetClassName = targetClassName;
	}

	String getPrefix() {
		final StringBuilder writer = new StringBuilder();
		writer.append("// DO NOT REMOVE THIS COMMENT (UP TO PACKAGE DECLARATION)");
		writer.append("\n// class=").append(getGeneratorClass());
		writer.append("\n// type=").append(getType());
		writer.append("\n// name=").append(getName());
		if (getTargetClassName() != null) {
			writer.append("\n// classname=").append(getTargetClassName());
		}
		writer.append("\n// id=").append(getId());
		writer.append("\n// version=").append(getVersion());
		writer.append("\n// modified=").append(getModified());
		writer.append("\n// date=").append(new Date(getModified()));
		writer.append("\n// END\n");
		return writer.toString();
	}

}
