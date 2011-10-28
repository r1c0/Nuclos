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

package org.nuclos.client.ui.resplan;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.LabelProvider;

public class DefaultResPlanRendererProvider extends LabelProvider {

	public DefaultResPlanRendererProvider() {
		super(JLabel.CENTER);
	}
	
	@Override
	protected void configureVisuals(CellContext context) {
		super.configureVisuals(context);
		rendererComponent.setOpaque(true);
		rendererComponent.setVerticalTextPosition(SwingConstants.TOP);
	}
}
