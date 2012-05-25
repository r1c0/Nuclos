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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.customcode.codegenerator.CodeGenerator.JavaSourceAsString;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Helper class for dealing with rule code.
 */
@Configurable
public class RuleCodeGenerator<T> implements CodeGenerator {

	public static abstract class AbstractRuleTemplateType<T> {

		private final String qualifier;
		private final Class<T> targetClass;

		protected AbstractRuleTemplateType(String qualifier, Class<T> targetClass) {
			this.qualifier = qualifier;
			this.targetClass = targetClass;
		}

		public final String getQualifier() {
			return qualifier;
		}

		public final String getClassName(RuleVO ruleVO) {
			return getQualifier() + "_" + ruleVO.getId();
		}

		public final Class<T> getTargetClass() {
			return targetClass;
		}

		public final String getHeader(RuleVO ruleVO) {
			final StringBuilder sb = new StringBuilder();
			for (String s : getImports()) {
				sb.append("import ").append(s).append(";\n");
			}
			sb.append(getHeaderImpl(ruleVO));
			sb.append("\n// BEGIN RULE\n");
			return sb.toString();
		}

		protected List<String> getWebserviceImports() {
			List<String> result = new ArrayList<String>();
			for (MasterDataVO webservice : RuleCache.getInstance().getWebservices()) {
				result.add(WsdlCodeGenerator.DEFAULT_PACKAGE_WEBSERVICES + "." + WsdlCodeGenerator.getServiceName(webservice.getField("name", String.class)) + ".*");
			}
			return result;
		}

		public abstract String getFooter();

		protected abstract List<String> getImports();

		protected abstract String getHeaderImpl(RuleVO ruleVO);

		public abstract String getLabel();

		public abstract String getEntityname();
	}

	// Spring injection

	private NuclosJavaCompilerComponent nuclosJavaCompilerComponent;
	
	// End of Spring injection
	
	private final AbstractRuleTemplateType<T> type;
	
	private final RuleVO ruleVO;
	
	private final RuleSourceAsString src;
	
	private String cachedHeader;

	public RuleCodeGenerator(AbstractRuleTemplateType<T> type, RuleVO ruleVO) {
		this.type = type;
		this.ruleVO = ruleVO;
		this.src = new RuleSourceAsString(getClassName(), 
				getPrefix(), getHeader(), ruleVO.getSource(), type.getFooter(), 
				type.getEntityname(), ruleVO.getId().longValue(), 
				getPrefixAndHeaderLineCount(), getPrefixAndHeaderOffset(), getLabel());
	}

	@Autowired
	final void setNuclosJavaCompilerComponent(NuclosJavaCompilerComponent nuclosJavaCompilerComponent) {
		this.nuclosJavaCompilerComponent = nuclosJavaCompilerComponent;
	}
	
	@Override
	public boolean isRecompileNecessary() {
		return true;
	}

	@Override
	public String getPrefix() {
		final StringBuilder writer = new StringBuilder();
		writer.append("// DO NOT REMOVE THIS COMMENT (UP TO PACKAGE DECLARATION)");
		writer.append("\n// class=org.nuclos.server.customcode.codegenerator.RuleCodeGenerator");
		writer.append("\n// type=org.nuclos.server.ruleengine.valueobject.RuleVO");
		writer.append("\n// name=");
		writer.append(ruleVO.getRule());
		writer.append("\n// classname=");
		writer.append(getClassName());
		writer.append("\n// id=");
		if (ruleVO.getId() != null) {
			writer.append(ruleVO.getId().toString());
		}
		writer.append("\n// version=");
		writer.append(Integer.toString(ruleVO.getVersion()));
		writer.append("\n// modified=");
		final Date changed = ruleVO.getChangedAt();
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
	public File getJavaSrcFile(JavaSourceAsString srcobject) {
		return new File(nuclosJavaCompilerComponent.getSourceOutputPath(), srcobject.getPath());
	}

	@Override
	public void writeSource(Writer writer, JavaSourceAsString s) throws IOException {
		final RuleSourceAsString src = (RuleSourceAsString) s;
		writer.write(src.getPrefix());
		writer.write(src.getHeader());
		writer.write(src.getSource());
		writer.write(src.getFooter());
	}

	@Override
	public Iterable<? extends JavaSourceAsString> getSourceFiles() {
		return Collections.singletonList(src);
	}

	@Override
	public byte[] postCompile(String name, byte[] bytecode) {
		if (ruleVO.isDebug()) {
			return ClassDebugAdapter.weaveDebugInterceptors(bytecode, getPrefixAndHeaderLineCount());
		}
		else {
			return bytecode;
		}
	}

	@Override
	public int hashForManifest() {
		return ruleVO.getId().intValue() + ruleVO.getVersion();
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
		final StringBuilder result = new StringBuilder("RuleCG[rule=");
		result.append(ruleVO.getRule());
		result.append(",class=").append(getClassName());
		result.append("]");
		return result.toString();
	}
	
	public String getClassName() {
		return type.getClassName(ruleVO);
	}

	private String getHeader() {
		if (cachedHeader == null) {
			cachedHeader = type.getHeader(ruleVO);
		}
		return cachedHeader;
	}

	@Override
	public int getPrefixAndHeaderLineCount() {
		return StringUtils.countLines(getPrefix()) + StringUtils.countLines(getHeader());
	}

	@Override
	public int getPrefixAndHeaderOffset() {
		return getPrefix().length() + getHeader().length();
	}

	private String getLabel() {
		return MessageFormat.format(type.getLabel(), ruleVO.getRule());
	}

	/**
	 * A JavaFileObject used internally to represent generated Java code. Especially, it
	 * knows the line and position (delta) of the embedded Java fragment.
	 */
	public static class RuleSourceAsString extends JavaSourceAsString {

		private final String header;
		
		private final String footer;
		
		private final String label;
		
		protected RuleSourceAsString(String name, String prefix, 
				String header, String source, String footer, 
				String entity, long id , long lineDelta, long posDelta, String label) {
			super(name, prefix, source, entity, id, lineDelta, posDelta);
			this.header = header;
			this.footer = footer;
			this.label = label;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return getPrefix() + header + getSource() + footer;
		}
		
		public String getHeader() {
			return header;
		}
		
		public String getFooter() {
			return footer;
		}

		@Override
		public String getLabel() {
			return label;
		}
	}

}
