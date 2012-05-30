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
package org.nuclos.client.scripting.context;

import org.nuclos.api.context.ScriptContext;
import org.nuclos.client.customcode.CodeDelegate;
import org.nuclos.common.expressions.ExpressionEvaluator;
import org.nuclos.common.expressions.ExpressionParser;

public abstract class AbstractScriptContext implements ScriptContext, ExpressionEvaluator {

	@Override
	public Object propertyMissing(String name) {
		if (name.startsWith("#F")) {
			// function call without parameters
			return methodMissing(name, new Object[]{});
		}
		return ExpressionParser.parse(name, this);
	}

	@Override
	public void propertyMissing(String name, Object value) {
		throw new UnsupportedOperationException("expressions are read only");
	}

	@Override
	public Object methodMissing(String name, Object args) {
		String function = ExpressionParser.parse(name);
		return CodeDelegate.getInstance().invokeFunction(function, (Object[])args);
	}
}
