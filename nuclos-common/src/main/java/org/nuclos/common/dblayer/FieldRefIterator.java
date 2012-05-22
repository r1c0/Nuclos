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
package org.nuclos.common.dblayer;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the expression language used in field references.
 *  
 * @author Thomas Pasch
 * @since Nuclos 3.3.0
 * @see org.nuclos.server.dblayer.util.ForeignEntityFieldParser
 */
public class FieldRefIterator implements Iterator<IFieldRef> {

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
	
	//
	
	private final String string;
	
	private final Matcher matcher;
	
	private final int len;
	
	private int index = 0;
	
	private IFieldRef next = null;
	
	public FieldRefIterator(Pattern pattern, String string) {
		this.string = string;
		this.len = string.length();
		this.matcher = pattern.matcher(string);
	}
	
	@Override
	public boolean hasNext() {
		final boolean result;
		if (matcher.find(index)) {
			final int s = matcher.start();
			if (s > index) {
				next = new FieldRef(string.substring(index, s), true);
				index = s;
				result = true;
			}
			else {
				next = new FieldRef(matcher.group(1), false);
				index = matcher.end();
				result = true;
			}
		} else if (index < len) {
			next = new FieldRef(string.substring(index), true);
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
