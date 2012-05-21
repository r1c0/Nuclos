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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * Helper class for dealing with rule code.
 */
public class RuleCodeGenerator<T> implements CodeGenerator {

	private static final Pattern NEW_LINES = Pattern.compile("\\n|\\r\\n?");

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

	private final AbstractRuleTemplateType<T> type;
	private final RuleVO ruleVO;
	private String cachedHeader;

	public RuleCodeGenerator(AbstractRuleTemplateType<T> type, RuleVO ruleVO) {
		this.type = type;
		this.ruleVO = ruleVO;
	}

	@Override
	public boolean isRecompileNecessary() {
		return true;
	}

	@Override
	public void writeSource(Writer writer, JavaSourceAsString src) throws IOException {
		writer.write("// DO NOT REMOVE THIS COMMENT (UP TO PACKAGE DECLARATION)");
		writer.write("\n// class=org.nuclos.server.customcode.codegenerator.RuleCodeGenerator");
		writer.write("\n// type=org.nuclos.server.ruleengine.valueobject.RuleVO");
		writer.write("\n// name=");
		writer.write(ruleVO.getRule());
		writer.write("\n// id=");
		writer.write(ruleVO.getId().toString());
		writer.write("\n// version=");
		writer.write(Integer.toString(ruleVO.getVersion()));
		writer.write("\n// modified=");
		writer.write(Long.toString(ruleVO.getChangedAt().getTime()));
		writer.write("\n// date=");
		writer.write(ruleVO.getChangedAt().toString());
		writer.write("\n// END\n");

		writer.write(src.getCharContent(true).toString());
	}

	@Override
	public Iterable<? extends JavaSourceAsString> getSourceFiles() {
		return Collections.singletonList(new RuleSourceAsString(getClassName(), getCode(), type.getEntityname(), ruleVO.getId().longValue(), getHeaderLineCount(), getHeaderOffset(), getLabel()));
	}

	@Override
	public byte[] postCompile(String name, byte[] bytecode) {
		if (ruleVO.isDebug()) {
			return ClassDebugAdapter.weaveDebugInterceptors(bytecode, getHeaderLineCount());
		}
		else {
			return bytecode;
		}
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(17, 37);
		builder.append(type.qualifier);
		builder.append(ruleVO.getId());
		builder.append(ruleVO.getVersion());
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RuleCodeGenerator<?>)) {
			return false;
		}
		else {
			RuleCodeGenerator<?> other = (RuleCodeGenerator<?>) obj;
			if (LangUtils.compare(this.type.qualifier, other.type.qualifier) == 0) {
				if (LangUtils.compare(this.ruleVO.getId(), other.ruleVO.getId()) == 0) {
					return LangUtils.compare(this.ruleVO.getVersion(), other.ruleVO.getVersion()) == 0;
				}
			}
			return false;
		}
	}

	public String getClassName() {
		return type.getClassName(ruleVO);
	}

	private String getCode() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getHeader());
		sb.append(ruleVO.getSource());
		sb.append(type.getFooter());
		return sb.toString();
	}

	private String getHeader() {
		if (cachedHeader == null) {
			cachedHeader = type.getHeader(ruleVO);
		}
		return cachedHeader;
	}

	public int getHeaderLineCount() {
		return countLines(getHeader());
	}

	private int getHeaderOffset() {
		return getHeader().length();
	}

	private String getLabel() {
		return MessageFormat.format(type.getLabel(), ruleVO.getRule());
	}

	private static int countLines(CharSequence cs) {
		int count = 0;
		Matcher matcher = NEW_LINES.matcher(cs);
		while (matcher.find())
			count++;
		return count;
	}

	/**
	 * A JavaFileObject used internally to represent generated Java code. Especially, it
	 * knows the line and position (delta) of the embedded Java fragment.
	 */
	public static class RuleSourceAsString extends JavaSourceAsString {

		private final String label;
		private final long lineDelta;
		private final long posDelta;


		protected RuleSourceAsString(String name, String source, String entity, long id , long lineDelta, long posDelta, String label) {
			super(name, source, entity, id);
			this.lineDelta = lineDelta;
			this.posDelta = posDelta;
			this.label = label;
		}

		public long getLineDelta() {
			return lineDelta;
		}

		public long getPositionDelta() {
			return posDelta;
		}

		@Override
		public String getLabel() {
			return label;
		}
	}

}
