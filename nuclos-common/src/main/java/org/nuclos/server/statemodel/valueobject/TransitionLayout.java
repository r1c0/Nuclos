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
package org.nuclos.server.statemodel.valueobject;

import java.io.Serializable;

/**
 * Transition layout.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class TransitionLayout implements Serializable {
	private static final long serialVersionUID = -5125540743088977640L;

	private int iConnectionStart;
	private int iConnectionEnd;
	private Integer iId;

	public TransitionLayout() {
		iConnectionStart = iConnectionEnd = -1;
	}

	public TransitionLayout(Integer iId, int iConnectionStart, int iConnectionEnd) {
		this.iId = iId;
		this.iConnectionStart = iConnectionStart;
		this.iConnectionEnd = iConnectionEnd;
	}

	// @todo try to eliminate - the id is most likely unused!
	public Integer getId() {
		return iId;
	}

	public int getConnectionStart() {
		return iConnectionStart;
	}

	public void setConnectionStart(int iConnectionStart) {
		this.iConnectionStart = iConnectionStart;
	}

	public int getConnectionEnd() {
		return iConnectionEnd;
	}

	public void setConnectionEnd(int iConnectionEnd) {
		this.iConnectionEnd = iConnectionEnd;
	}

	@Override
	public boolean equals(Object o) {
		TransitionLayout tl = (TransitionLayout) o;
		return this.iConnectionStart == tl.getConnectionStart() &&
				this.iConnectionEnd == tl.getConnectionEnd() &&
				this.iId.equals(tl.getId());
	}

}	// class TransitionLayout
