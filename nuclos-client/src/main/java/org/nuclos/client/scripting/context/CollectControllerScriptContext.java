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
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.expressions.EntityExpression;
import org.nuclos.common.expressions.ExpressionEvaluator;
import org.nuclos.common.expressions.FieldIdExpression;
import org.nuclos.common.expressions.FieldRefObjectExpression;
import org.nuclos.common.expressions.FieldValueExpression;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonValidationException;

public class CollectControllerScriptContext extends AbstractScriptContext implements ExpressionEvaluator {

	private final CollectController<?> controller;
	private final List<DetailsSubFormController<?>> sfcs;

	public CollectControllerScriptContext(CollectController<?> controller, List<DetailsSubFormController<?>> sfcs) {
		this.controller = controller;
		this.sfcs = sfcs;
	}

	@Override
	public Object evaluate(FieldValueExpression exp) {
		for (CollectableComponent comp : controller.getDetailCollectableComponentsFor(exp.getField())) {
			try {
				return comp.getField().getValue();
			} catch (CollectableFieldFormatException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public Long evaluate(FieldIdExpression exp) {
		for (CollectableComponent comp : controller.getDetailCollectableComponentsFor(exp.getField())) {
			try {
				return IdUtils.toLongId(comp.getField().getValueId());
			} catch (CollectableFieldFormatException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public ScriptContext evaluate(FieldRefObjectExpression exp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ScriptContext> evaluate(EntityExpression exp) {
		for (DetailsSubFormController<?> ctl : sfcs) {
			if (ctl.getEntityAndForeignKeyFieldName().getEntityName().equals(exp.getEntity())) {
				List<ScriptContext> contexts = new ArrayList<ScriptContext>();

				try {
					for (Collectable clct : ctl.getCollectables(false, false, false)) {
						contexts.add(new SubformControllerScriptContext(controller, ctl, clct));
					}
				} catch (CommonValidationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return contexts;
			}
		}
		return null;
	}
}
