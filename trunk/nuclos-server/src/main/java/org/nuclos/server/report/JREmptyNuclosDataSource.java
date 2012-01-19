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

import org.nuclos.server.report.api.JRNuclosDataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class JREmptyNuclosDataSource implements JRNuclosDataSource {

	private int size;
	private int index;

	public JREmptyNuclosDataSource(int size) {
		this.size = 1;
	}

	@Override
	public JRNuclosDataSource getDataSource(String name) {
		return new JREmptyNuclosDataSource(this.size);
	}

	@Override
	public JRNuclosDataSource getDataSource(String name, Object[][] paramArray) {
		return new JREmptyNuclosDataSource(this.size);
	}

	@Override
	public Object getFieldValue(JRField arg0) throws JRException {
		return null;
	}

	@Override
	public boolean next() throws JRException {
		if (index < size) {
			index ++;
			return true;
		}
		return false;
	}

}
