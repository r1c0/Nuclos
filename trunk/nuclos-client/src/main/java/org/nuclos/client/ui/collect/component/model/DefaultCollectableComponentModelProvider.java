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
import java.util.Map;

/**
 * Default implementation of <code>CollectableComponentModelProvider</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class DefaultCollectableComponentModelProvider implements CollectableComponentModelProvider {

	private final Map<String, CollectableComponentModel> mpclctcompmodel;

	public DefaultCollectableComponentModelProvider(Map<String, CollectableComponentModel> mpclctcompmodel) {
		this.mpclctcompmodel = Collections.unmodifiableMap(mpclctcompmodel);
	}

	@Override
	public Collection<String> getFieldNames() {
		return this.mpclctcompmodel.keySet();
	}

	@Override
	public Collection<? extends CollectableComponentModel> getCollectableComponentModels() {
		return this.mpclctcompmodel.values();
	}

	@Override
	public CollectableComponentModel getCollectableComponentModelFor(String sFieldName) {
		return this.mpclctcompmodel.get(sFieldName);
	}

}	// class DefaultCollectableComponentModelProvider
