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
package org.nuclos.client.processmonitor;

import java.io.Serializable;

import org.nuclos.server.processmonitor.valueobject.SubProcessVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * @author Marc.Finke
 * 
 * represents a Subprocess
 * holds the value object for subprocess
 * and the value object for the layout
 *
 */
public class SubProcess implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SubProcessVO subprocessVO;

	/**
	 * 
	 */
	public SubProcess(StateModelVO stateModelVO) {
		subprocessVO = new SubProcessVO(new Integer(-1), null, null, null, null, "", "", "", "", null, null, null, null, null);
		subprocessVO.setStateModelVO(stateModelVO);
	}	
	
	@Override
	public String toString() {
		return subprocessVO.getStatename();
	}

	public StateModelVO getStateModelVO() {
		return subprocessVO.getStateModelVO();
	}
	
	public void setSubProcessVO(SubProcessVO subVO) {
		this.subprocessVO = subVO;
	}
	
	public SubProcessVO getSubProcessVO() {
		return subprocessVO;
	}

}
