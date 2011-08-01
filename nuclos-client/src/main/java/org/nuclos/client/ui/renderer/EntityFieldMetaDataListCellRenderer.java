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
package org.nuclos.client.ui.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;

/**
 * This avoids toString rendering for EntityFieldMetaDataVO lists.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class EntityFieldMetaDataListCellRenderer extends JLabel implements ListCellRenderer {
	
	private final ListModel model;
	
	public EntityFieldMetaDataListCellRenderer(ListModel model) {
		this.model = model;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		if (index >= 0 && index < model.getSize()) {
			final EntityFieldMetaDataVO fieldVo = (EntityFieldMetaDataVO) model.getElementAt(index);
			setText(fieldVo.getField());
		}
        return this;
	}

}
