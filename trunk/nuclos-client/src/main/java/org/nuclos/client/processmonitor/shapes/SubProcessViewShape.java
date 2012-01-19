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
package org.nuclos.client.processmonitor.shapes;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

import org.nuclos.client.gef.AbstractShapeViewer;
import org.nuclos.server.common.InstanceConstants;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;

/**
 * Shape representing a subprocess (in a instance viewer).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class SubProcessViewShape extends SubProcessShape {
	
	private AbstractAction forwardAction;

	public SubProcessViewShape() {
		super();
	}

	public SubProcessViewShape(SubProcessVO subprocessvo) {
		super(subprocessvo);
	}

	@Override
	public void doubleClicked(JComponent parent) {
		if (forwardAction != null){
			forwardAction.actionPerformed(new ActionEvent(super.getStateVO(), 0, "showStatemodelStatus"));
		}
	}
	
	public void setForwardAction(AbstractAction forwardAction) {
		this.forwardAction = forwardAction;
	}
	
	/**
	 * 
	 * @param status
	 */
	public void showInstanceStatus(int status){
		switch (status){
		case InstanceConstants.STATUS_NOT_STARTED:
			bgColor = new Color(192, 192, 192);
			break;
		case InstanceConstants.STATUS_RUNNING_INTIME:
			bgColor = new Color(255, 255, 166);
			break;
		case InstanceConstants.STATUS_RUNNING_DELAYED:
			bgColor = new Color(255, 182, 108);
			break;
		case InstanceConstants.STATUS_ENDED_INTIME:
			bgColor = new Color(179, 255, 102);
			break;
		case InstanceConstants.STATUS_ENDED_DELAYED:
			bgColor = new Color(255, 128, 128);
			break;
		}
		
		if (getView() != null) {
			((AbstractShapeViewer) getView()).repaint();
		}
	}
}
