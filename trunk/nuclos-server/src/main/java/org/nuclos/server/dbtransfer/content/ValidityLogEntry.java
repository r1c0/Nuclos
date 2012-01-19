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
package org.nuclos.server.dbtransfer.content;

public class ValidityLogEntry {

	public StringBuffer sbCritical = new StringBuffer();
	public StringBuffer sbWarning = new StringBuffer();
	
	public void newWarningLine(String warning) {
		if (sbWarning.length() > 0) sbWarning.append("<br />");
		sbWarning.append(warning);
	}
	
	public void newCriticalLine(String critical) {
		if (sbCritical.length() > 0) sbCritical.append("<br />");
		sbCritical.append(critical);
	}
}
