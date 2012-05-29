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
package org.nuclos.common.expressions;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.api.expressions.InvalidExpressionException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;

public abstract class ExpressionParser {

	private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\#\\{([^}]*)\\}");
	private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\#F\\{([^}]*)\\}");

	public static Object parse(String expression, ExpressionEvaluator eval) {
		String[] parts = parseExpression(expression);
		if (parts.length == 2) {
			return eval.evaluate(new EntityExpression(parts[0], parts[1]));
		}
		else if (parts.length == 3) {
			return eval.evaluate(new FieldValueExpression(parts[0], parts[1], parts[2]));
		}
		else if (parts.length == 4) {
			if ("value".equals(parts[3])) {
				return eval.evaluate(new FieldValueExpression(parts[0], parts[1], parts[2]));
			}
			else if ("id".equals(parts[3])) {
				return eval.evaluate(new FieldIdExpression(parts[0], parts[1], parts[2]));
			}
			else if ("object".equals(parts[3])) {
				return eval.evaluate(new FieldRefObjectExpression(parts[0], parts[1], parts[2]));
			}
		}
		throw new InvalidExpressionException(expression);
	}

	private static String[] parseExpression(String expression) {
		Matcher m = EXPRESSION_PATTERN.matcher(expression);
		if (m.matches()) {
			String s = expression.substring(2, expression.length() - 1);
			return s.split("\\.");
		}
		else {
			throw new InvalidExpressionException(expression);
		}
	}

	public static String getExpression(EntityMetaDataVO meta) {
		return MessageFormat.format("#'{'{0}.{1}\'}'", meta.getNuclet(), meta.getEntity());
	}

	public static String getExpression(EntityMetaDataVO meta, EntityFieldMetaDataVO field) {
		return MessageFormat.format("#'{'{0}.{1}.{2}\'}'", meta.getNuclet(), meta.getEntity(), field.getField());
	}

	public static String parse(String expression) {
		Matcher m = FUNCTION_PATTERN.matcher(expression);
		if (m.matches()) {
			return expression.substring(3, expression.length() - 1);
		}
		else {
			throw new InvalidExpressionException(expression);
		}
	}
}
