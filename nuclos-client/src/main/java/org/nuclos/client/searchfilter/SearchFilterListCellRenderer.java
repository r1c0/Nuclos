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
package org.nuclos.client.searchfilter;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import org.nuclos.client.main.Main;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;

public class SearchFilterListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList lst, Object oValue, int index, boolean bSelected, boolean bCellHasFocus) {
		final JComponent result = (JComponent) super.getListCellRendererComponent(lst, oValue, index, bSelected, bCellHasFocus);
		String sToolTip = null;
		if (oValue != null && oValue instanceof SearchFilter) {
			final SearchFilter filter = (SearchFilter) oValue;

			if (result instanceof JLabel && !StringUtils.isNullOrEmpty(filter.getLabelResourceId())) {
				((JLabel) result).setText(CommonLocaleDelegate.getTextFallback(filter.getLabelResourceId(), filter.getName()));
			}

			if (!StringUtils.isNullOrEmpty(filter.getDescriptionResourceId())) {
				sToolTip = CommonLocaleDelegate.getTextFallback(filter.getDescriptionResourceId(), filter.getDescriptionResourceId());
			}
			else {
				sToolTip = filter.getDescription();
			}

			if (filter.getOwner() != null && !(filter.getOwner().equals(Main.getMainController().getUserName()))) {
				sToolTip = sToolTip + " (" + filter.getOwner() + ")";
			}

			result.setToolTipText(sToolTip);
		}
		return result;
	}
}
