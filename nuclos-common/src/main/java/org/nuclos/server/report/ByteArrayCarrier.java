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
package org.nuclos.server.report;

import java.io.Serializable;

/**
 * Used to transport a byte array as an object derived class (for reading and writing to BLOB columns by EJB).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 * not_deprecated Both EJB 2.x CMP and the new master data mechanism are able to write byte[] natively.
 * Note that a byte[] is an Object. The class of byte[] can be expressed statically as "byte[].class"
 * and dynamically as Class.forName("[B").
 */
// mtj: somebody said "Deprecated" w/o fixing access - forget it for the moment
public class ByteArrayCarrier implements Serializable {
	private byte[] data;

	public ByteArrayCarrier(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return this.data;
	}
}
