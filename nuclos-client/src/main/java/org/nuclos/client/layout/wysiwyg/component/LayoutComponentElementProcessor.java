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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.nuclos.api.ui.LayoutComponentFactory;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBoolean;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueColor;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueDimension;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueFont;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueInteger;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueScript;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueTranslations;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.main.Main;
import org.nuclos.client.nuclet.NucletComponentRepository;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This class creates a {@link WYSIWYGLAyoutComponent}
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:maik.stueker@nuclos.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class LayoutComponentElementProcessor implements ComponentProcessor, LayoutMLConstants {

	private final String sLayoutComponentFactoryClass;
	
	private LayoutComponentFactory lcf;
	
	// former Spring injection
	
	private NucletComponentRepository nucletComponentRepository;
	
	// end of former Spring injection
	
	public LayoutComponentElementProcessor(String sLayoutComponentFactoryClass) {
		this.sLayoutComponentFactoryClass = sLayoutComponentFactoryClass;
		
		setNucletComponentRepository(SpringApplicationContextHolder.getBean(NucletComponentRepository.class));
		init();
	}
	
	final void setNucletComponentRepository(NucletComponentRepository nucletComponentRepository) {
		this.nucletComponentRepository = nucletComponentRepository;
	}
	
	final NucletComponentRepository getNucletComponentRepository() {
		return nucletComponentRepository;
	}	
	
	void init() {
		for (LayoutComponentFactory lcf : getNucletComponentRepository().getLayoutComponentFactories()) {
			if (lcf.getClass().getName().equals(sLayoutComponentFactoryClass)) {
				this.lcf = lcf;
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
	 */
	@Override
	public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name, boolean bDefault) throws CommonBusinessException {
		if (lcf == null) {
			lcf = new DummyLayoutComponentFactory(sLayoutComponentFactoryClass);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(Main.getInstance().getMainFrame(), String.format("LayoutComponent %s is missing.", sLayoutComponentFactoryClass));
				}
			});
		}
		WYSIWYGLayoutComponent element = new WYSIWYGLayoutComponent(lcf);
		
		String sComponentName = lcf.getName() + "_" + iNumber;
		ComponentProperties properties = PropertyUtils.getEmptyProperties(element, metaInf);
		properties.setProperty(WYSIWYGLayoutComponent.PROPERTY_NAME, new PropertyValueString(sComponentName), String.class);
		element.setProperties(properties);
		
		for (String property : element.getProperties().getProperties().keySet()) {
			Object defValue = lcf.getDefaultPropertyValue(property);
			if (defValue != null) {
				PropertyValue<?> propertyValue = element.getProperties().getProperties().get(property);
				if (propertyValue == null) {
					throw new NullPointerException("propertyValue must not be null");
				}
				if (propertyValue instanceof PropertyValueBoolean) {
					((PropertyValueBoolean) propertyValue).setValue((Boolean) defValue);
				} else if (propertyValue instanceof PropertyValueString) {
					((PropertyValueString) propertyValue).setValue((String) defValue);
				} else if (propertyValue instanceof PropertyValueInteger) {
					((PropertyValueInteger) propertyValue).setValue((Integer) defValue);
				} else if (propertyValue instanceof PropertyValueDimension) {
					((PropertyValueDimension) propertyValue).setValue((Dimension) defValue);
				} else if (propertyValue instanceof PropertyValueColor) {
					((PropertyValueColor) propertyValue).setValue((Color) defValue);
				} else if (propertyValue instanceof PropertyValueFont) {
					Font fontNew = (Font) defValue;
					Font fontOld = element.getFont();
					int iRelativSize = fontNew.getSize() - fontOld.getSize();
					((PropertyValueFont) propertyValue).setValue(iRelativSize);
				} else if (propertyValue instanceof PropertyValueScript) {
					((PropertyValueScript) propertyValue).setValue((NuclosScript) defValue);
				} else if (propertyValue instanceof PropertyValueTranslations) {
					((PropertyValueTranslations) propertyValue).setValue((TranslationMap) defValue);
				} else if (propertyValue instanceof PropertyValueValuelistProvider) {
					((PropertyValueValuelistProvider) propertyValue).setValue((WYSIWYGValuelistProvider) defValue);
				} 
			}
			
		}

		((Component)element).setMinimumSize(DEFAULTVALUE_LAYOUTCOMPONENT_MINIMUMSIZE);
		return (Component)element;
	}
}
