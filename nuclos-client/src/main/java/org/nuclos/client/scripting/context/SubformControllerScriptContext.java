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
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.expressions.EntityExpression;
import org.nuclos.common.expressions.ExpressionEvaluator;
import org.nuclos.common.expressions.FieldIdExpression;
import org.nuclos.common.expressions.FieldRefObjectExpression;
import org.nuclos.common.expressions.FieldValueExpression;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonValidationException;

public class SubformControllerScriptContext extends AbstractScriptContext implements ExpressionEvaluator {

	private final CollectController<?> parent;
	private final AbstractDetailsSubFormController<?> parentSfc;
	private final AbstractDetailsSubFormController<?> sfc;
	private final Collectable c;

	public SubformControllerScriptContext(CollectController<?> parent, AbstractDetailsSubFormController<?> parentSfc, AbstractDetailsSubFormController<?> sfc, Collectable c) {
		this.parent = parent;
		this.parentSfc = parentSfc;
		this.sfc = sfc;
		this.c = c;
	}

	public SubformControllerScriptContext(CollectController<?> parent, AbstractDetailsSubFormController<?> sfc, Collectable c) {
		this(parent, null, sfc, c);
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
		if (!sfc.getEntityAndForeignKeyFieldName().getEntityName().equals(exp.getEntity())) {
			throw new UnsupportedOperationException("Context reference expressions require current entity as source entity.");
		}

		EntityFieldMetaDataVO fieldmeta = MetaDataClientProvider.getInstance().getEntityField(exp.getEntity(), exp.getField());
		if (fieldmeta.getForeignEntity() == null) {
			throw new UnsupportedOperationException("Context reference expressions require a reference field.");
		}

		if (this.parent != null && this.parent.getEntityName().equals(fieldmeta.getForeignEntity()) && sfc.getEntityAndForeignKeyFieldName().getFieldName().equals(exp.getField())) {
			return new CollectControllerScriptContext(parent, this.parent.getDetailsConroller().getSubFormControllers());
		}
		else if (this.parentSfc != null && this.parentSfc.getEntityAndForeignKeyFieldName().getEntityName().equals(fieldmeta.getForeignEntity()) && sfc.getEntityAndForeignKeyFieldName().getFieldName().equals(exp.getField())) {
			return new SubformControllerScriptContext(this.parent, this.parentSfc, this.parentSfc.getCollectableTableModel().getRow(this.parentSfc.getSubForm().getJTable().getSelectionModel().getLeadSelectionIndex()));
		}
		throw new UnsupportedOperationException("Context reference expressions are only allowed for parent or parent subform references.");
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
							contexts.add(new SubformControllerScriptContext(this.parent, sfc, ctl, clct));
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
}
