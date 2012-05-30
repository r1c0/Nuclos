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

import java.util.List;

import org.nuclos.api.context.ScriptContext;
import org.nuclos.common.expressions.EntityExpression;
import org.nuclos.common.expressions.ExpressionEvaluator;
import org.nuclos.common.expressions.FieldIdExpression;
import org.nuclos.common.expressions.FieldRefObjectExpression;
import org.nuclos.common.expressions.FieldValueExpression;

public class NullCollectableScriptContext extends AbstractScriptContext implements ExpressionEvaluator {

	@Override
	public Object evaluate(FieldValueExpression exp) {
		return null;
	}

	@Override
	public Long evaluate(FieldIdExpression exp) {
		return null;
	}

	@Override
	public ScriptContext evaluate(FieldRefObjectExpression exp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ScriptContext> evaluate(EntityExpression exp) {
		throw new UnsupportedOperationException();
	}
}