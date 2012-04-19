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

import java.util.ArrayList;
import java.util.List;

import org.nuclos.api.context.ScriptContext;
import org.nuclos.client.common.AbstractDetailsSubFormController;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.expressions.EntityExpression;
import org.nuclos.common.expressions.ExpressionEvaluator;
import org.nuclos.common.expressions.ExpressionParser;
import org.nuclos.common.expressions.FieldIdExpression;
import org.nuclos.common.expressions.FieldRefObjectExpression;
import org.nuclos.common.expressions.FieldValueExpression;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonValidationException;

public class SubformControllerScriptContext implements ScriptContext, ExpressionEvaluator {

	private final AbstractDetailsSubFormController<?> sfc;
	private final Collectable c;

	public SubformControllerScriptContext(AbstractDetailsSubFormController<?> sfc, Collectable c) {
		this.sfc = sfc;
		this.c = c;
	}

	@Override
	public Object evaluate(FieldValueExpression exp) {
		return c.getValue(exp.getField());
	}

	@Override
	public Long evaluate(FieldIdExpression exp) {
		return IdUtils.toLongId(c.getValueId(exp.getField()));
	}

	@Override
	public ScriptContext evaluate(FieldRefObjectExpression exp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ScriptContext> evaluate(EntityExpression exp) {
		if (sfc instanceof MasterDataSubFormController) {
			MasterDataSubFormController msfc = (MasterDataSubFormController) sfc;
			for (DetailsSubFormController<?> ctl : msfc.getChildSubFormController()) {
				if (ctl.getEntityAndForeignKeyFieldName().getEntityName().equals(exp.getEntity())) {
					List<ScriptContext> contexts = new ArrayList<ScriptContext>();
					try {
						for (Collectable clct : ctl.getCollectables(false, false, false)) {
							contexts.add(new SubformControllerScriptContext(ctl, clct));
						}
					} catch (CommonValidationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return contexts;
				}
			}
		}
		return null;
	}

	@Override
	public Object propertyMissing(String name) {
		return ExpressionParser.parse(name, this);
	}

	@Override
	public void propertyMissing(String name, Object value) {
		throw new UnsupportedOperationException("expressions are read only");
	}
}
