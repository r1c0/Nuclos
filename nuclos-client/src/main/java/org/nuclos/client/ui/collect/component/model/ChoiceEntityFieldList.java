//Copyright (C) 2011  Novabit Informationssysteme GmbH
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

import java.util.Set;

import org.nuclos.client.ui.model.ChoiceList;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * Encapsulates the lists of available and selected fields, resp.
 * The selected fields are shown as columns in the result table.
 * The selected fields are always in sync with the table column model, but not necessarily
 * with the table model's columns.
 * <p>
 * Formerly known as ResultFields.
 * </p>
 * @since Nuclos 3.1.01 this is a top-level class.
 */
public class ChoiceEntityFieldList extends ChoiceList<CollectableEntityField> {
	
	private Set<CollectableEntityField> fixed;
	
	public ChoiceEntityFieldList(Set<CollectableEntityField> fixed) {
		super();
		this.fixed = fixed;
	}
	
	public Set<CollectableEntityField> getFixed() {
		return fixed;
	}

}	// inner class Fields
