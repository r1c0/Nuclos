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

import java.util.Collections;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.customcode.valueobject.CodeVO;

public class PlainCodeGenerator implements CodeGenerator {

	private final CodeVO valueobject;

	public PlainCodeGenerator(CodeVO codevo) {
		this.valueobject = codevo;
	}
	
	@Override
	public boolean isRecompileNecessary() {
		return true;
	}

	@Override
	public Iterable<? extends JavaSourceAsString> getSourceFiles() {
		return Collections.singletonList(new JavaSourceAsString(this.valueobject.getName(), this.valueobject.getSource(), NuclosEntity.CODE.getEntityName(), valueobject.getId() == null ? null : valueobject.getId().longValue()));
	}

	@Override
	public byte[] postCompile(String name, byte[] bytecode) {
		if (valueobject.isDebug()) {
			return ClassDebugAdapter.weaveDebugInterceptors(bytecode, 0);
		}
		else {
			return bytecode;
		}
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(17, 37);
		builder.append(valueobject.getId());
		builder.append(valueobject.getVersion());
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PlainCodeGenerator)) {
			return false;
		}
		else {
			PlainCodeGenerator other = (PlainCodeGenerator) obj;
			if (LangUtils.compare(this.valueobject.getId(), other.valueobject.getId()) == 0) {
				return LangUtils.compare(this.valueobject.getVersion(), other.valueobject.getVersion()) == 0;
			}
			else {
				return false;
			}
		}
	}
}
