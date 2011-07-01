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
package org.nuclos.client.livesearch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.Timer;

import org.jdesktop.swingx.painter.BusyPainter;

public class BusyComponent extends JComponent implements ActionListener {
	private Dimension    fixedSize;
	private int          pointsize;
	private BusyPainter         bp;
	private Timer timer;
	public BusyComponent(int pointsize, int radius) {
		this.pointsize = pointsize;
		fixedSize = new Dimension((radius + pointsize) * 2, (radius + pointsize) * 2);
		bp = new BusyPainter();
		bp.setPointShape(new Ellipse2D.Double(0, 0, pointsize, pointsize));
		bp.setTrajectory(new Ellipse2D.Double(0, 0, radius, radius));
		bp.setBaseColor(Color.GRAY);
		bp.setHighlightColor(Color.RED);
		timer = new Timer(100, this);
	}
	
	@Override public Dimension getPreferredSize() { return fixedSize; }
	@Override public Dimension getMinimumSize()   { return fixedSize; }
	@Override public Dimension getMaximumSize()   { return fixedSize; }
	
	@Override
    public void paint(Graphics g) {
		Dimension d = getSize();
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, d.width, d.height);
		
		if(timer.isRunning()) {
			Graphics2D g3 = (Graphics2D) g2.create();
			g3.translate(pointsize, pointsize);
			bp.paint(g3, this, d.width / 2, d.height / 2);
		}
	}

	@Override
    public void actionPerformed(ActionEvent e) {
		bp.setFrame((bp.getFrame() + 1) % 8);
	    repaint();
    }

	public void start() {
		if(!timer.isRunning())
			timer.start();
	}
	
	public void stop() {
		if(timer.isRunning()) {
			timer.stop();
			repaint();
		}
	}
}
