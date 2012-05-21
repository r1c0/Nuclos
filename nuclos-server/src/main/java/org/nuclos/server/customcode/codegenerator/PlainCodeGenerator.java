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
package org.nuclos.server.customcode.codegenerator;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.customcode.codegenerator.CodeGenerator.JavaSourceAsString;
import org.nuclos.server.customcode.valueobject.CodeVO;

public class PlainCodeGenerator implements CodeGenerator {

	private final CodeVO codeVO;

	public PlainCodeGenerator(CodeVO codevo) {
		this.codeVO = codevo;
	}
	
	@Override
	public boolean isRecompileNecessary() {
		return true;
	}

	@Override
	public void writeSource(Writer writer, JavaSourceAsString src) throws IOException {
		writer.write("// DO NOT REMOVE THIS COMMENT (UP TO PACKAGE DECLARATION)");
		writer.write("\n// class=org.nuclos.server.customcode.codegenerator.PlainCodeGenerator");
		writer.write("\n// type=org.nuclos.server.customcode.valueobject.CodeVO");
		writer.write("\n// name=");
		writer.write(codeVO.getName());
		writer.write("\n// id=");
		writer.write(codeVO.getId().toString());
		writer.write("\n// version=");
		writer.write(Integer.toString(codeVO.getVersion()));
		writer.write("\n// modified=");
		writer.write(Long.toString(codeVO.getChangedAt().getTime()));
		writer.write("\n// date=");
		writer.write(codeVO.getChangedAt().toString());
		writer.write("\n// END\n");

		writer.write(src.getCharContent(true).toString());
	}

	@Override
	public Iterable<? extends JavaSourceAsString> getSourceFiles() {
		return Collections.singletonList(new JavaSourceAsString(this.codeVO.getName(), this.codeVO.getSource(), NuclosEntity.CODE.getEntityName(), codeVO.getId() == null ? null : codeVO.getId().longValue()));
	}

	@Override
	public byte[] postCompile(String name, byte[] bytecode) {
		if (codeVO.isDebug()) {
			return ClassDebugAdapter.weaveDebugInterceptors(bytecode, 0);
		}
		else {
			return bytecode;
		}
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(17, 37);
		builder.append(codeVO.getId());
		builder.append(codeVO.getVersion());
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PlainCodeGenerator)) {
			return false;
		}
		else {
			PlainCodeGenerator other = (PlainCodeGenerator) obj;
			if (LangUtils.compare(this.codeVO.getId(), other.codeVO.getId()) == 0) {
				return LangUtils.compare(this.codeVO.getVersion(), other.codeVO.getVersion()) == 0;
			}
			else {
				return false;
			}
		}
	}
}
