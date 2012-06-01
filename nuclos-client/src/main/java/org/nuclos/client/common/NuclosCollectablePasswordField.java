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
package org.nuclos.client.common;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.ui.CommonJPasswordField;
import org.nuclos.client.ui.collect.component.CollectablePasswordField;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * NUCLEUSINT-1142
 * @author hartmut.beckschulze
 * @version 01.00.00
 */
public class NuclosCollectablePasswordField extends CollectablePasswordField {

	public NuclosCollectablePasswordField(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, bSearchable);
	}

	// For behaviour as table cell renderer
	@Override
	public void setEnabled(boolean bEnabled) {
		this.getJTextComponent().setEditable(bEnabled);
	}


}	// class NuclosCollectablePasswordField
