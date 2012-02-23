//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dblayer.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;

/**
 * Parser for the expression language used in field references.
 *  
 * @author Thomas Pasch
 * @since Nuclos 3.2.01
 * @see org.nuclos.common.dal.vo.EntityFieldMetaDataVO#getForeignEntityField()
 */
public class ForeignEntityFieldParser implements Iterable<IFieldRef> {
	
	private static final Logger LOG = Logger.getLogger(ForeignEntityFieldParser.class);
	
	private static final class FieldRef implements IFieldRef {
		
		private final String content;
		
		private final boolean constant;
		
		private FieldRef(String content, boolean constant) {
			this.content = content;
			this.constant = constant;
		}

		@Override
		public boolean isConstant() {
			return constant;
		}

		@Override
		public String getContent() {
			return content;
		}
		
		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			result.append("FieldRef['").append(content);
			result.append("', ").append(constant).append(']');
			return result.toString();
		}
		
	}
	
	private static final Pattern REF_PATTERN = Pattern.compile("\\$\\{(\\p{Alpha}[\\p{Alnum}_]*)\\}", Pattern.MULTILINE);
	
	private static final Pattern OLD_SIMPLE_REF_PATTERN = Pattern.compile("\\p{Alpha}[\\p{Alnum}_]*");
	
	private static final String DEFAULT_FOREIGN_ENTITY_FIELD = "${name}";
	
	// 
	
	private final String ffieldname;
	
	public ForeignEntityFieldParser(EntityFieldMetaDataVO foreignEntityField) {
		if (foreignEntityField == null) {
			throw new NullPointerException();
		}
		// this.foreignEntityField = foreignEntityField;
		
		String fef = null;
		if (foreignEntityField.getForeignEntity() != null)
			fef = foreignEntityField.getForeignEntityField();
		else if (foreignEntityField.getLookupEntity() != null)
			fef = foreignEntityField.getLookupEntityField();
		
		if (fef == null) {
			LOG.warn("Null/empty foreignEntityField in expression in " + foreignEntityField 
					+ " is deprecated, use " + DEFAULT_FOREIGN_ENTITY_FIELD + " instead!");
			this.ffieldname = DEFAULT_FOREIGN_ENTITY_FIELD;
		}
		else if (OLD_SIMPLE_REF_PATTERN.matcher(fef).matches()) {
			LOG.warn("Old style foreignEntityField expression '" + fef + "' in "
					+ foreignEntityField + " is deprecated, please enclose it: ${" + fef + "}");
			this.ffieldname = "${" + fef + "}";
		}
		else {
			this.ffieldname = fef;
		}
	}

	@Override
	public Iterator<IFieldRef> iterator() {
		return new FieldRefIterator(ffieldname);
	}

	private static class FieldRefIterator implements Iterator<IFieldRef> {
	
		private final String ffieldName;
		
		private final Matcher matcher;
		
		private final int len;
		
		private int index = 0;
		
		private IFieldRef next = null;
		
		private FieldRefIterator(String ffieldName) {
			this.ffieldName = ffieldName;
			this.len = ffieldName.length();
			this.matcher = REF_PATTERN.matcher(ffieldName);
		}
		
		@Override
		public boolean hasNext() {
			final boolean result;
			if (matcher.find(index)) {
				final int s = matcher.start();
				if (s > index) {
					next = new FieldRef(ffieldName.substring(index, s), true);
					index = s;
					result = true;
				}
				else {
					next = new FieldRef(matcher.group(1), false);
					index = matcher.end();
					result = true;
				}
			} else if (index < len) {
				next = new FieldRef(ffieldName.substring(index), true);
				index = len;
				result = true;
			} else {
				next = null;
				result = false;
			}
			return result;
		}
	
		@Override
		public IFieldRef next() {
			if (next == null && !hasNext()) {
				throw new NoSuchElementException();
			}
			final IFieldRef result = next;
			next = null;
			return result;
		}
	
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public static void main(String[] args) {
		// final String test = "prefix${a}${test} -\n ${main} hiho ${error} ->";
		// final String test = "${a}$b{";
		final String test = "test";
		System.out.println("Parse: '" + test + "'");
		final EntityFieldMetaDataVO field = new EntityFieldMetaDataVO();
		field.setForeignEntityField(test);
		field.setForeignEntity("foreignEntity");
		field.setField("fieldRefToForeignEntityField");
		for (IFieldRef r: new ForeignEntityFieldParser(field)) {
			System.out.println(r);
		}
		System.out.println("ForeignEntityFieldParser main ended");
	}

}
