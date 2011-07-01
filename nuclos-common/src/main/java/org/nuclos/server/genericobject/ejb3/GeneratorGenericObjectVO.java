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
package org.nuclos.server.genericobject.ejb3;

import java.util.Collections;
import java.util.Set;

/**
 * Value object representing a leased object to generate.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.000
 */
public class GeneratorGenericObjectVO {

	private final Set<GeneratorGenericObjectAttributeVO> collAttributes;

	/**
	 * default constructor to be called by server only
	 */
	public GeneratorGenericObjectVO() {
		this(Collections.<GeneratorGenericObjectAttributeVO>emptySet());
	}

	/**
	 * constructor to be called by server only
	 * @param colAttributes collection of attributes
	 */
	GeneratorGenericObjectVO(Set<GeneratorGenericObjectAttributeVO> colAttributes) {
		this.collAttributes = colAttributes;
	}

	/**
	 * get collection of attributes
	 * @return collection of attributes
	 */
	public Set<GeneratorGenericObjectAttributeVO> getAttributes() {
		return this.collAttributes;
	}
}
