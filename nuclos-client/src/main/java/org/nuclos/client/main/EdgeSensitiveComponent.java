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

public interface EdgeSensitiveComponent {
	public enum Orientation {
		LEFT {
			@Override
			public int getOffset(int bx, int by, int w, int h, int px, int py) {
				if(py < by || py > by + h)
					return -1;
				return px - bx;
			}
		},
		RIGHT {
			@Override
			public int getOffset(int bx, int by, int w, int h, int px, int py) {
				if(py < by || py > by + h)
					return -1;
				return bx + w - px;
			}
		},
		TOP {
			@Override
			public int getOffset(int bx, int by, int w, int h, int px, int py) {
				if(px < bx || px > bx + w)
					return -1;
				return py - by;
			}
		},
		BOTTOM {
			@Override
			public int getOffset(int bx, int by, int w, int h, int px, int py) {
				if(px < bx || px > bx + w)
					return -1;
				return by + h - py;
			}
		};
		/**
		 * BoundsX, BoundsY, Width, Height define the outer container, PointX
		 * and PointY are the current mouse position.
		 * Result is the relative distance to the relating edge.
		 */
		public abstract int getOffset(int bx, int by, int w, int h, int px, int py);
	};
	public Orientation getOrientation();
	public int getHotspotSize();
	public void mouseAt(int offsetFromEdge);
}
