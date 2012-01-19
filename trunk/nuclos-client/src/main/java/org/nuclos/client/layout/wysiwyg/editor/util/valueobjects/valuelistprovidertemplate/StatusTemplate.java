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

import java.util.ArrayList;
import java.util.Vector;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValueListEditorCapable;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGParameter;

public class StatusTemplate extends WYSIWYGValueListTemplates implements WYSIWYGValueListEditorCapable {
	private String name = "status";
	private String[] values = new String[]{"module", "process"};
	private Integer[] usage = new Integer[] {1,1};
	
	@Override
	public Vector<WYSIYWYGParameter> getPossibleParameters() {
		return super.getPossibleParameters(values, usage);
	}
	
	@Override
	public  ArrayList<String> getValidValuesForParameter(String[] parameter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValueListProviderType() {
		return super.getValueListProviderType(name);
	}

	@Override
	public void validate(WYSIWYGValuelistProvider valuelistProvider, WYSIWYGComponent component) throws CommonBusinessException {}
}
