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
/*
 * Created on 19.11.2009
 */
package org.nuclos.client.main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class AnimationTimer  extends Timer implements ActionListener {
	private static final int ANIM_STEPS_PER_SEC = 16;
	private final boolean horizontal;
	private boolean show = false;
	private boolean working = false;
	private boolean animate;
	
	private int min, max, step;

	private Component client;
	
	private long lastCalledMillis;
	
	public AnimationTimer(Component client, int min, int max, int totalAnimTimeMs, boolean horizontal, boolean animate) {
		super(animate ? Math.max(totalAnimTimeMs * ANIM_STEPS_PER_SEC / 1000, 1) : 1, null);
		step = animate ? Math.max(((max - min) / (1000 / ANIM_STEPS_PER_SEC)), 2) : max;
		this.client = client;
		this.horizontal = horizontal;
		this.animate = animate;
		addActionListener(this);
		
		this.min = min;
		this.max = max;
	}
	
	public void setSize(int min, int max) {
		step = animate ? Math.max(((max - min) / (1000 / ANIM_STEPS_PER_SEC)), 2) : max;
		this.min = min;
		this.max = max;
	}

	public void setShow(boolean show) {
		this.show = show;
		working = true;
		lastCalledMillis = -1;
	}
	
	public boolean getShow() {
		return show;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(!working)
			return;
		
		Window containingWindow = SwingUtilities.getWindowAncestor(client);
		if(containingWindow == null)
			return;
		
		int w = horizontal ? client.getWidth() : client.getHeight();
		int wn = w;
		int mystep = step;
		
		long currentMillis = System.currentTimeMillis();
		if(lastCalledMillis != -1) {
			int delta = (int) Math.max(currentMillis - lastCalledMillis, 1);
			double dd = 1000.0 / delta;
			mystep = animate ? Math.max(Math.max(((max - min) / (int) (Math.max(Math.round(dd), 1))), 2), step) : max;
		}
		lastCalledMillis = currentMillis;
		
		if(!show && w > min) 
			wn = Math.max(min, w - mystep);
		else if(show && w < max) 
			wn = Math.min(max, w + mystep);
		
		if(wn != w) {
			if(horizontal)
				client.setPreferredSize(new Dimension(wn, client.getHeight()));
			else
				client.setPreferredSize(new Dimension(client.getWidth(), wn));
			client.invalidate();
			containingWindow.validate();
		}
		else
			working = false;
	}
}
