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
import java.util.Date;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
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
	public String getPrefix() {
		final StringBuilder writer = new StringBuilder();
		writer.append("// DO NOT REMOVE THIS COMMENT (UP TO PACKAGE DECLARATION)");
		writer.append("\n// class=org.nuclos.server.customcode.codegenerator.PlainCodeGenerator");
		writer.append("\n// type=org.nuclos.server.customcode.valueobject.CodeVO");
		writer.append("\n// name=");
		writer.append(codeVO.getName());
		writer.append("\n// id=");
		if (codeVO.getId() != null) {
			writer.append(codeVO.getId().toString());
		}
		writer.append("\n// version=");
		writer.append(Integer.toString(codeVO.getVersion()));
		writer.append("\n// modified=");
		final Date changed = codeVO.getChangedAt();
		if (changed != null) {
			writer.append(Long.toString(changed.getTime()));
		}
		writer.append("\n// date=");
		if (changed != null) {
			writer.append(changed.toString());
		}
		writer.append("\n// END\n");
		return writer.toString();
	}

	@Override
	public int getPrefixAndHeaderLineCount() {
		return StringUtils.countLines(getPrefix());
	}

	@Override
	public int getPrefixAndHeaderOffset() {
		return getPrefix().length();
	}

	@Override
	public void writeSource(Writer writer, JavaSourceAsString src) throws IOException {
		writer.write(src.getPrefix());
		writer.write(src.getSource());
	}

	@Override
	public Iterable<? extends JavaSourceAsString> getSourceFiles() {
		return Collections.singletonList(new JavaSourceAsString(
				codeVO.getName(), getPrefix(), codeVO.getSource(), 
				NuclosEntity.CODE.getEntityName(), codeVO.getId() == null ? null : codeVO.getId().longValue(),
				getPrefixAndHeaderLineCount(), getPrefixAndHeaderOffset()));
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
