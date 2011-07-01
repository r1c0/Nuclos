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
/**
 * 
 */
package org.nuclos.client.processmonitor.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

import org.nuclos.client.gef.shapes.OvalShape;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * @author Marc.Finke
 * ignore this class
 *
 */
public class OrShape extends OvalShape {
	
	StateVO stateVO;

	/**
	 * @param centerX
	 * @param centerY
	 * @param horzRadius
	 * @param vertRadius
	 */
	public OrShape(double centerX, double centerY, double horzRadius,
			double vertRadius) {
		super(centerX, centerY, horzRadius, vertRadius);
		
		setConnectable(true);
		setResizeable(true);
		setColor(Color.blue);
	}
	
	public OrShape() {
		this(0d, 0d, 20d, 20d);
	}
	
	@Override
	public void afterCreate() {
		stateVO = new StateVO(new Integer(-getId()), null, "Neuer Zweig", "", null);
		super.afterCreate();		
	}
	
	public StateVO getStateVO() {
		return stateVO;
	}
	
	@Override
	public void paint(Graphics2D gfx) {
		
		super.paint(gfx);
		Color col = gfx.getColor();
		
		gfx.setColor(Color.white);		
		gfx.drawString("OR", (int)this.centerX-8, (int)this.centerY+5);
		gfx.setColor(col);
				
		
	}

}
