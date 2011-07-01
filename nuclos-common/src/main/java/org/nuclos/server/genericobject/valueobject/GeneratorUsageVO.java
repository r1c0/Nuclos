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
package org.nuclos.server.genericobject.valueobject;

import java.io.Serializable;

/**
 * Value object representing a leased object generator action usage.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class GeneratorUsageVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String sStateMnemonic;
	private final Integer iProcessId;

	public GeneratorUsageVO(String sStateMnemonic, Integer iProcessId) {
		this.sStateMnemonic = sStateMnemonic;
		this.iProcessId = iProcessId;
	}

	public String getStateMnemonic() {
		return sStateMnemonic;
	}

	public Integer getProcessId() {
		return iProcessId;
	}

}	// class GeneratorUsageVO
