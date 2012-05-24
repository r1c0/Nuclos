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
package org.nuclos.client.ui;

import java.awt.Component;
import java.awt.Container;

import org.nuclos.common.WorkspaceDescription.EntityPreferences;

public class LayoutComponentUtils {

	public static <C extends Component> C setPreferences(EntityPreferences ep, C c) {
		if (c instanceof LayoutComponentHolder) {
			if (c.getName() != null) {
				((LayoutComponentHolder) c).getLayoutComponent().setPreferences(ep.getLayoutComponentPreferences(c.getName()));
			}
		} else if (c instanceof Container) {
			for (Component child : ((Container)c).getComponents()) {
				setPreferences(ep, child);
			}
		}
		return c;
	}
}
