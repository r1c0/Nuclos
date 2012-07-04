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
package org.nuclos.client.layout.wysiwyg.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;

import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOptions;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;

/**
 * 
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:maik.stueker@novabit.de">maik.stueker</a>
 * @version 01.00.00
 */
public class WYSIWYGCollectableOptionGroup extends WYSIWYGUniversalComponent {
	
	public WYSIWYGCollectableOptionGroup(WYSIWYGMetaInformation meta) {
		super(meta);
		
		propertiesToAttributes.remove(PROPERTY_SHOWONLY);
		propertyFilters.put(PROPERTY_SHOWONLY, new PropertyFilter(PROPERTY_SHOWONLY, DISABLED));
		
		propertyNames.add(PROPERTY_OPTIONS);
		
		propertyClasses.put(PROPERTY_OPTIONS, new PropertyClass(PROPERTY_OPTIONS, WYSIWYGOptions.class));
		propertySetMethods.put(PROPERTY_OPTIONS, new PropertySetMethod(PROPERTY_OPTIONS, "setOptions"));
		propertyFilters.put(PROPERTY_OPTIONS, new PropertyFilter(PROPERTY_OPTIONS, ENABLED));
		propertyFilters.put(PROPERTY_NAME, new PropertyFilter(PROPERTY_NAME, ENABLED));
		
		propertyFilters.put(PROPERTY_VALUELISTPROVIDER, new PropertyFilter(PROPERTY_VALUELISTPROVIDER, DISABLED));
		propertyFilters.put(PROPERTY_CONTROLTYPECLASS, new PropertyFilter(PROPERTY_CONTROLTYPECLASS, DISABLED));
		propertyFilters.put(PROPERTY_LABEL, new PropertyFilter(PROPERTY_LABEL, DISABLED));
		propertyFilters.put(PROPERTY_ROWS, new PropertyFilter(PROPERTY_ROWS, DISABLED));
		propertyFilters.put(PROPERTY_COLUMNS, new PropertyFilter(PROPERTY_COLUMNS, DISABLED));
		//NUCLEUSINT-385
		propertyFilters.put(WYSIWYGUniversalComponent.PROPERTY_CONTROLTYPE, new PropertyFilter(WYSIWYGUniversalComponent.PROPERTY_CONTROLTYPE, DISABLED));
		propertyFilters.put(WYSIWYGUniversalComponent.PROPERTY_CONTROLTYPECLASS, new PropertyFilter(WYSIWYGUniversalComponent.PROPERTY_CONTROLTYPECLASS, DISABLED));
		//NUCLEUSINT-403
		propertyFilters.put(PROPERTY_OPAQUE, new PropertyFilter(PROPERTY_OPAQUE, DISABLED));
		
		propertyFilters.put(PROPERTY_INSERTABLE, new PropertyFilter(PROPERTY_INSERTABLE, DISABLED));
		propertyFilters.put(PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyFilter(PROPERTY_FILL_CONTROL_HORIZONTALLY, DISABLED));
	}
	
	@Override
	public int getControlType() {
		return CollectableComponentTypes.TYPE_OPTIONGROUP;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent#getAdditionalContextMenuItems(int)
	 */
	@Override
	public List<JMenuItem> getAdditionalContextMenuItems(int xClick) {
		List<JMenuItem> list = new ArrayList<JMenuItem>();
		return list;
	}
	
	public void setOptions(WYSIWYGOptions options) {
		super.render();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGUniversalComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
		//NUCLEUSINT-385
		/**
		 * Needed to overwrite, because the WYSIWYGUniversalComponent does validation on Properties that are disabled for the OptionGroup
		 */
	}
	
	
}
