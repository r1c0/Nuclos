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
package org.nuclos.client.processmonitor;

import org.nuclos.client.processmonitor.shapes.SubProcessShape;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;

/**
 * SubProcess entry.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:marc.finke@novabit.de">Marc Finke</a>
 * @version 01.00.00
 */
public class SubProcessEntry {
	
	// the value object of a Subprocess
	SubProcessVO vo;
	// the shape in the editor
	SubProcessShape shape;

	public SubProcessEntry() {
		vo = null;
		shape = null;
	}

	public SubProcessVO getVo() {
		return vo;
	}

	public void setVo(SubProcessVO vo) {
		this.vo = vo;
	}

	public SubProcessShape getShape() {
		return shape;
	}

	public void setShape(SubProcessShape shape) {
		this.shape = shape;
	}
}
