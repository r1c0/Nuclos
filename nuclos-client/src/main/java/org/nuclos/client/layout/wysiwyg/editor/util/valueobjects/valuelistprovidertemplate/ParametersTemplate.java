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

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValueListEditorCapable;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGParameter;

public class ParametersTemplate extends WYSIWYGValueListTemplates implements WYSIWYGValueListEditorCapable {
	private String showClass = "showClass";
	private String[] values = new String[]{showClass, "showValue"};
	private Integer[] usage = new Integer[] {1,null};
	private String name = "parameters";
	
	@Override
	public Vector<WYSIYWYGParameter> getPossibleParameters() {
		return super.getPossibleParameters(values, usage);
	}
	
	@Override
	public ArrayList<String> getValidValuesForParameter(String[] parameter) {
		ArrayList<String> validvalues = new ArrayList<String>();
		if (showClass.equals(parameter[0])){
			validvalues.add("java.lang.String");
			validvalues.add("java.lang.Integer");
			validvalues.add("java.util.Date");
			validvalues.add("java.lang.Boolean");
		}
		
		return validvalues;
	}

	@Override
	public String getValueListProviderType() {
		return super.getValueListProviderType(name);
	}

	@Override
	public void validate(WYSIWYGValuelistProvider valuelistProvider, WYSIWYGComponent component) throws CommonBusinessException {
		Vector<WYSIYWYGParameter> parameters = valuelistProvider.getAllWYSIYWYGParameter();
		boolean showClassFound = false;
		WYSIWYGMetaInformation metainf = component.getParentEditor().getMetaInformation();
		Class<?> javaclass = null;
		if (component instanceof WYSIWYGSubFormColumn) {
			CollectableEntityField field = ((WYSIWYGSubFormColumn) component).getEntityField();
			javaclass = field.getJavaClass();
		} else {
			String name = ((PropertyValueString) component.getProperties().getProperty(
			WYSIWYGCollectableComponent.PROPERTY_NAME)).getValue();
			javaclass = metainf.getDatatypeForAttribute(name);
		}

		for (WYSIYWYGParameter parameter : parameters) {
			if("showClass".equals(parameter.getParameterName())) {
				if(!javaclass.getName().equals(parameter.getParameterValue())) {
					String exception = WYSIWYGStringsAndLabels.partedString(
						WYSIWYGStringsAndLabels.VALUELIST_PROVIDER_EDITOR.VALIDATIONEXCEPTION_PARAMETERS_1,valuelistProvider.getType(), javaclass.getName(),javaclass.getName(),javaclass.getName());
					throw new CommonBusinessException(exception);
				}
				showClassFound = true;
			}
		}

		if (!showClassFound) {
			if (!javaclass.getName().equals("java.lang.String")) {
				String exception = WYSIWYGStringsAndLabels.partedString(WYSIWYGStringsAndLabels.VALUELIST_PROVIDER_EDITOR.VALIDATIONEXCEPTION_PARAMETERS_2,valuelistProvider.getType(), javaclass.getName(),javaclass.getName());
				throw new CommonBusinessException(exception);
			}
		}
	}
}
