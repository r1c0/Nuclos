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
package org.nuclos.server.dblayer.impl.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nuclos.common.collection.Transformer;


/**
 * A helper class for building prepared strings.  A prepared string is a list
 * of string fragments or nested prepared strings (or prepared string builders)
 * interleaved with parameters.
 * 
 * Parameters are represented by {@link Parameter} objects.
 */
public class PreparedStringBuilder implements Serializable {
	
	public static PreparedStringBuilder valueOf(String s) {
		return new PreparedStringBuilder().append(s);
	}
	
	private final List<Object> list = new LinkedList<Object>();
	private boolean frozen = false;
	
	public PreparedStringBuilder() {
	}
	
	public PreparedStringBuilder(String s) {
		this();
		append(s);
	}
	
	public PreparedStringBuilder append(String string) {
		appendImpl(string);
		return this;
	}
	
	public PreparedStringBuilder appendf(String format, Object...args) {
		appendImpl(String.format(format, args));
		return this;
	}	
	
	public PreparedStringBuilder append(Parameter parameter) {
		appendImpl(parameter);
		return this;
	}
	
	public PreparedStringBuilder append(PreparedStringBuilder ps) {
		appendImpl(ps);
		return this;
	}
	
	public PreparedStringBuilder prepend(String string) {
		prependImpl(string);
		return this;
	}
	
	public PreparedStringBuilder prependf(String format, Object...args) {
		prependImpl(String.format(format, args));
		return this;
	}
	
	public PreparedStringBuilder prepend(Parameter parameter) {
		prependImpl(parameter);
		return this;
	}
	
	public PreparedStringBuilder prepend(PreparedStringBuilder ps) {
		prependImpl(ps);
		return this;
	}
	
	/**
	 * Freezes this builder object, so that {@code append} and {@code prepend}
	 * will throw an exception. Note that nested builder objects are still 
	 * mutable. 
	 */
	public PreparedStringBuilder freeze() {
		this.frozen = true;
		return this;
	}

	public PreparedString toPreparedString(Map<Parameter, ?> map) {
		return new PreparedString(getString(QUESTION_MARK), mapParameters(map).toArray());
	}
	
	public String getString(Transformer<Parameter, String> transformer) {
		return appendTo(new StringBuilder(), transformer).toString();
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		accumulateParameters(parameters);
		return parameters;
	}
	
	public List<Object> mapParameters(Map<Parameter, ?> map) {
		List<Object> mappedParameters = new ArrayList<Object>();
		int index = 0;
		for (Parameter param : getParameters()) {
			index++;
			Object value = param.value;
			if (value == null) {
				if (map == null || !map.containsKey(param))
					throw new IllegalArgumentException("No parameter found for parameter #" + index);
				value = map.get(param);
			}
			mappedParameters.add(value);
		}
		return mappedParameters;
	}
	
	@Override
	public String toString() {
		return getString(new ToStringTransformer());
	}
	
	private void appendImpl(Object obj) {
		checkFrozen();
		list.add(obj);
	}
	
	private void prependImpl(Object obj) {
		checkFrozen();
		list.add(0, obj);
	}
	
	private void checkFrozen() {
		if (frozen)
			throw new IllegalArgumentException();
	}
	
	private StringBuilder appendTo(StringBuilder sb, Transformer<Parameter, String> transformer) {
		for (Object obj : list) {
			if (obj instanceof Parameter) {
				sb.append(transformer.transform((Parameter) obj));
			} else if (obj instanceof PreparedStringBuilder) {
				((PreparedStringBuilder) obj).appendTo(sb, transformer);
			} else {
				sb.append(obj);
			}
		}
		return sb;
	}
	
	private void accumulateParameters(List<Parameter> parameters) {
		for (Object obj : list) {
			if (obj instanceof Parameter) {
				parameters.add((Parameter) obj);
			} else if (obj instanceof PreparedStringBuilder) {
				((PreparedStringBuilder) obj).accumulateParameters(parameters);
			}
		}	
	}
	
	public boolean isFrozen() {
		return frozen;
	}
	
	public static class Parameter implements Serializable {
		
		private Object value;
		
		public Parameter() {
		}
		
		public Parameter bind(Object value) {
			if (value == null)
				throw new IllegalArgumentException("Bind value must not be null");
			this.value = value;
			return this;
		}
	}
	
	public static PreparedStringBuilder concat(Object...args) {
		if (args.length == 1 && args[0] instanceof PreparedStringBuilder)
			return (PreparedStringBuilder) args[0];
		PreparedStringBuilder ps = new PreparedStringBuilder();
		for (Object obj : args) {
			if (obj instanceof String) {
				ps.append((String) obj);
			} else if (obj instanceof Parameter) {
				ps.append((Parameter) obj);
			} else if (obj instanceof PreparedStringBuilder) {
				ps.append((PreparedStringBuilder) obj);
			} else {
				throw new IllegalArgumentException("Illegal argument " + obj);
			}
		}
		return ps;
	}

	private static final Transformer<Parameter, String> QUESTION_MARK = new Transformer<Parameter, String>() {
		@Override
		public String transform(Parameter p) { return "?"; }
	};

	private static final class ToStringTransformer implements Transformer<Parameter, String> {
		int index = 1;
		@Override
		public String transform(Parameter p) {
			return "?" + index++;
		}
	}
}
