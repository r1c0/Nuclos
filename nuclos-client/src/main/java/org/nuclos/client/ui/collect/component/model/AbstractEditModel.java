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
package org.nuclos.client.ui.collect.component.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.client.ui.collect.component.CollectableComponent;

/**
 * Default implementation of <code>EditModel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public abstract class AbstractEditModel<Clctcompmodel extends CollectableComponentModel>
		implements EditModel {

	private final Map<String, Clctcompmodel> mpclctcompmodel = new HashMap<String, Clctcompmodel>();

	public AbstractEditModel(Collection<CollectableComponent> collclctcomp) {
		for (CollectableComponent clctcomp : collclctcomp) {
			this.mpclctcompmodel.put(clctcomp.getFieldName(), (Clctcompmodel) clctcomp.getModel());
		}
	}

	@Override
	public Collection<String> getFieldNames() {
		return Collections.unmodifiableCollection(this.mpclctcompmodel.keySet());
	}

	@Override
	public Collection<Clctcompmodel> getCollectableComponentModels() {
		return Collections.unmodifiableCollection(this.mpclctcompmodel.values());
	}

	@Override
	public Clctcompmodel getCollectableComponentModelFor(String sFieldName) {
		return this.mpclctcompmodel.get(sFieldName);
	}

}	// class AbstractEditModel
