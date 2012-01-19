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
package org.nuclos.tools.translation;

public class FileData {

	private int translationCalls;
   private boolean hasDelegateImport;
   private Integer firstImportStartIndex;
   private Integer translationImportStartIndex;
   private Integer translationImportEndIndex;

	public int getTranslationCalls() {
		return translationCalls;
	}

	public void setTranslationCalls(int translationCalls) {
		this.translationCalls = translationCalls;
	}

	public boolean hasDelegateImport() {
		return hasDelegateImport;
	}

	public void setHasDelegateImport(boolean hasDelegateImport) {
		this.hasDelegateImport = hasDelegateImport;
	}

	public Integer getFirstImportStartIndex() {
		return firstImportStartIndex;
	}

	public void setFirstImportStartIndex(Integer firstImportStartIndex) {
		this.firstImportStartIndex = firstImportStartIndex;
	}

	public Integer getTranslationImportStartIndex() {
		return translationImportStartIndex;
	}

	public void setTranslationImportStartIndex(Integer translationImportStartIndex) {
		this.translationImportStartIndex = translationImportStartIndex;
	}

	public Integer getTranslationImportEndIndex() {
		return translationImportEndIndex;
	}

	public void setTranslationImportEndIndex(Integer translationImportEndIndex) {
		this.translationImportEndIndex = translationImportEndIndex;
	}
}
