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
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Collections;

import javax.annotation.PostConstruct;

import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator.AbstractRuleTemplateType;
import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator.RuleSourceAsString;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeBean;
import org.nuclos.server.ruleengine.ejb3.TimelimitRuleFacadeBean;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Thomas Pasch
 */
@Configurable
public class OnDiskCodeGenerator implements CodeGenerator {
	
	// Spring injection

	private NuclosJavaCompilerComponent nuclosJavaCompilerComponent;
	
	// End of Spring injection
	
	private final GeneratedFile gf;
	
	private JavaSourceAsString src;
	
	public OnDiskCodeGenerator(GeneratedFile gf) {
		this.gf = gf;
	}

	@Autowired
	final void setNuclosJavaCompilerComponent(NuclosJavaCompilerComponent nuclosJavaCompilerComponent) {
		this.nuclosJavaCompilerComponent = nuclosJavaCompilerComponent;
	}
	
	@PostConstruct
	final void init() {
		if ("org.nuclos.server.ruleengine.valueobject.RuleVO".equals(gf.getType())) {
			final String source = new String(gf.getContent());
			final Integer id = Integer.valueOf((int) gf.getId());
			final RuleVO dummy = new RuleVO(id, null, gf.getVersion(), gf.getName(), "", source, true);
			
			final AbstractRuleTemplateType<?> type;
			if (gf.getFile().getName().startsWith("Rule_")) {
				type = new RuleEngineFacadeBean.RuleTemplateType();
			}
			else {
				type = new TimelimitRuleFacadeBean.TimelimitRuleCodeTemplate();
			}
			
			src = new RuleSourceAsString(
					gf.getTargetClassName(), 
					getPrefix(), type.getHeader(dummy), source, type.getFooter(), 
					"OnDiskCodeGenerator", gf.getId(),
					getPrefixAndHeaderLineCount(), getPrefixAndHeaderOffset(),
					MessageFormat.format(type.getLabel(), dummy.getRule()));
		}
		else {
			src = new JavaSourceAsString(
				gf.getName(), 
				getPrefix(), new String(gf.getContent()), 
				"OnDiskCodeGenerator", gf.getId(),
				getPrefixAndHeaderLineCount(), getPrefixAndHeaderOffset());
		}
	}
	
	@Override
	public boolean isRecompileNecessary() {
		return true;
	}

	@Override
	public Iterable<? extends JavaSourceAsString> getSourceFiles() {
		return Collections.singletonList(src);
	}

	@Override
	public byte[] postCompile(String name, byte[] bytecode) {
		return bytecode;
	}

	@Override
	public String getPrefix() {
		return gf.getPrefix();
	}

	@Override
	public int getPrefixAndHeaderLineCount() {
		return 0;
	}

	@Override
	public int getPrefixAndHeaderOffset() {
		return 0;
	}

	@Override
	public File getJavaSrcFile(JavaSourceAsString src) {
		return new File(nuclosJavaCompilerComponent.getSourceOutputPath(), src.getPath());
	}

	@Override
	public void writeSource(Writer writer, JavaSourceAsString src) throws IOException {
		writer.write(src.getPrefix());
		writer.write(src.getSource());
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
		final StringBuilder result = new StringBuilder("OnDiskCG[code=");
		result.append(gf.getName());
		result.append(",file=").append(gf.getFile());
		result.append("]");
		return result.toString();
	}
	
}
