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

import org.nuclos.client.common.AbstractDetailsSubFormController;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.dal.vo.EntityMetaDataVO;

public class SubFormFieldScriptContext extends SubformControllerScriptContext {

	private final CollectableEntityField field;

	public SubFormFieldScriptContext(CollectController<?> parent, AbstractDetailsSubFormController<?> sfc, Collectable c, CollectableEntityField field) {
		super(parent, sfc, c);
		this.field = field;
	}

	public String getField() {
		EntityMetaDataVO md = MetaDataClientProvider.getInstance().getEntity(field.getEntityName());
		return "#{" + md.getNuclet() + "." + md.getEntity() + "." + field.getName() + "}";
	}
}
