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
package org.nuclos.client.statemodel.admin;

import java.util.Properties;
import java.util.StringTokenizer;

import com.mxgraph.model.mxCell;

public class StateModelUtils {
	
	public Properties getStyleAsProperties(mxCell cell) {
		Properties prop = new Properties();
		
		String style = cell.getStyle();
		StringTokenizer st = new StringTokenizer(style, ";");
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			StringTokenizer st1 = new StringTokenizer(token, "=");
			prop.put(st1.nextToken(), st1.nextToken());
		}
		
		return prop;
	}

}
