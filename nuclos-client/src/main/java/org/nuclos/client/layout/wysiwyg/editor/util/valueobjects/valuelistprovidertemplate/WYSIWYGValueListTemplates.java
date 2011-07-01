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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.valuelistprovidertemplate;

import java.util.Vector;

import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGParameter;

public class WYSIWYGValueListTemplates{
	
	public static int PARAMETER = 0;
	public static int DATASOURCENAME = 1;

	protected Vector<WYSIYWYGParameter> getPossibleParameters(String[] values, Integer[] usage) {
		Vector<WYSIYWYGParameter> returnValue = new Vector<WYSIYWYGParameter>();
		assert values.length == usage.length;
		WYSIYWYGParameter newParameter = null;
		for (int i = 0 ; i < values.length ; i++) {
			newParameter = new WYSIYWYGParameter();
			newParameter.setParameterName(values[i]);
			newParameter.setMaximumNumberOfUsage(usage[i]);
			returnValue.add(newParameter);
		}

		return returnValue;
	}

	protected String getValueListProviderType(String name) {
		return name;
	}
}
