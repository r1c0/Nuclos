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
package org.nuclos.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.log4j.Logger;
import org.nuclos.common2.IOUtils;

public class MarshalledValue implements Externalizable {

	private static final Logger LOG = Logger.getLogger(MarshalledValue.class);

	private static final long serialVersionUID = -1527598981234110311L;
	
	private byte[]	data;
	
	public Object get() {
		try {
			return IOUtils.fromByteArray(data);
		}
		catch(Exception e) {
			throw new NuclosFatalException(e);
		}
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int length = in.readInt();
		data = null;
		if(length > 0) {
			data = new byte[length];
			in.readFully(data);
		}
		//hashCode = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
	}
}
