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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class PlainCodeGenerator implements CodeGenerator {
	
	// Spring injection

	private NuclosJavaCompilerComponent nuclosJavaCompilerComponent;
	
	private ApplicationProperties applicationProperties;

	// End of Spring injection
	
	private final CodeVO codeVO;
	
	private JavaSourceAsString src;

	public PlainCodeGenerator(CodeVO codevo) {
		this.codeVO = codevo;
	}
	
	@PostConstruct
	final void init() {
		this.src = new JavaSourceAsString(
				codeVO.getName(), getPrefix(), codeVO.getSource(), 
				NuclosEntity.CODE.getEntityName(), codeVO.getId() == null ? null : codeVO.getId().longValue(),
				getPrefixAndHeaderLineCount(), getPrefixAndHeaderOffset());
	}
	
	@Autowired
	final void setNuclosJavaCompilerComponent(NuclosJavaCompilerComponent nuclosJavaCompilerComponent) {
		this.nuclosJavaCompilerComponent = nuclosJavaCompilerComponent;
	}
	
	@Autowired
	final void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@Override
	public boolean isRecompileNecessary() {
		return true;
	}
	
	@Override
	public String getPrefix() {
		final StringBuilder writer = new StringBuilder();
		if (applicationProperties.isSourceCodeScanning()) {
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
		}
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
	public File getJavaSrcFile(JavaSourceAsString srcobject) {
		return new File(nuclosJavaCompilerComponent.getSourceOutputPath(), srcobject.getPath());
	}

	@Override
	public void writeSource(Writer writer, JavaSourceAsString src) throws IOException {
		writer.write(src.getPrefix());
		writer.write(src.getSource());
	}

	@Override
	public Iterable<? extends JavaSourceAsString> getSourceFiles() {
		return Collections.singletonList(src);
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
	public int hashForManifest() {
		return codeVO.getId().intValue() + codeVO.getVersion();
	}

	@Override
	public int hashCode() {
		return src.getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CodeGenerator)) {
			return false;
		}
		final CodeGenerator other = (CodeGenerator) obj;
		final JavaSourceAsString firstOtherSrc;
		if (!other.isRecompileNecessary()) {
			return false;
		}
		firstOtherSrc = other.getSourceFiles().iterator().next();		
		return src.getName().equals(firstOtherSrc.getName());
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder("PlainCG[code=");
		result.append(codeVO.getName());
		result.append("]");
		return result.toString();
	}
	
}
