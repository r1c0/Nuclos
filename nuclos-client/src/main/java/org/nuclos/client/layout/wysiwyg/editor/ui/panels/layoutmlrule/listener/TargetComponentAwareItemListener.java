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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.listener;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;
import org.nuclos.common2.StringUtils;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGParameter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.valuelistprovidertemplate.ParametersTemplate;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.valuelistprovidertemplate.StatusTemplate;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;

/**
 * {@link ItemListener} containing the logic for the dependency between 
 * entity, targetcomponent and parameter-for-sourcecomponent.
 * 
 * It searches for possible Parameters of the {@link WYSIWYGValuelistProvider} attached to the targetcomponent.
 * 
 * @author hartmut.beckschulze
 *
 */
public class TargetComponentAwareItemListener implements ItemListener {

	private static final Logger LOG = Logger.getLogger(TargetComponentAwareItemListener.class);

	private JComboBox						parameterForSourceComponent	= null;
	private WYSIWYGLayoutEditorPanel	editorPanel							= null;
	private LayoutMLRuleAction			layoutMLRuleAction				= null;

	/**
	 * 
	 * @param parameterForSourceComponent
	 * @param entity
	 * @param layoutMLRuleAction
	 * @param editorPanel
	 */
	public TargetComponentAwareItemListener(
		JComboBox parameterForSourceComponent, JComboBox entity,
		LayoutMLRuleAction layoutMLRuleAction,
		WYSIWYGLayoutEditorPanel editorPanel) {
		this.parameterForSourceComponent = parameterForSourceComponent;
		this.editorPanel = editorPanel;
		this.layoutMLRuleAction = layoutMLRuleAction;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		String targetComponent = (String) e.getItem();
		if(e.getStateChange() == ItemEvent.SELECTED) {
			initParameterForSourceComponent(targetComponent);
			if(!"".equals(targetComponent)) {
				initParameterForSourceComponent(targetComponent);
				layoutMLRuleAction.setTargetComponent(targetComponent);
			}
		}
	}

	public void initParameterForSourceComponent(String targetComponent) {
		parameterForSourceComponent.removeAllItems();
		PropertyValueValuelistProvider vp = null;
		try {
			// there are no subforms, just working on collectablecomponents
			if(!StringUtils.isNullOrEmpty(targetComponent)) {
				if(!StringUtils.isNullOrEmpty(layoutMLRuleAction.getEntity())) {
					// its a subform... do it the other way
					WYSIWYGSubFormColumn column = editorPanel.getWYSIWYGSubformColumnUsingEntity(targetComponent);
					if (column != null)
						vp = (PropertyValueValuelistProvider) column.getProperties().getProperty(WYSIWYGComponent.PROPERTY_VALUELISTPROVIDER);
				}
				else {
					// its a collectable component
					WYSIWYGCollectableComponent component = editorPanel.getWYSIWYGComponentUsingEntity(targetComponent);
					if (component != null)
						vp = (PropertyValueValuelistProvider) component.getProperties().getProperty(WYSIWYGComponent.PROPERTY_VALUELISTPROVIDER);
				}
				if(vp == null) {
					parameterForSourceComponent.addItem("");
					parameterForSourceComponent.setSelectedItem("");
				} else if(vp.getValue() != null) {
					if(vp.getValue().getType() != null) {
						String vpType = vp.getValue().getType();
						if(vpType.contains(".ds")) {
							// its a ds valuelistprovider
							Collection<ValuelistProviderVO> dsVps = DatasourceDelegate.getInstance().getAllValuelistProvider();

							for(ValuelistProviderVO dsvp : dsVps) {
								if(vpType.equals(dsvp.getName() + ".ds")) {
									List<DatasourceParameterVO> lstParameterVOs = DatasourceDelegate.getInstance().getParametersFromXML(dsvp.getSource());
									for(DatasourceParameterVO parameter : lstParameterVOs) {
										parameterForSourceComponent.addItem(parameter.getParameter());
									}
									parameterForSourceComponent.addItem("");

									if(!StringUtils.isNullOrEmpty(layoutMLRuleAction.getParameterForSourceComponent())) {
										// there is something to restore
										parameterForSourceComponent.setSelectedItem(layoutMLRuleAction.getParameterForSourceComponent());
									} else {
										// defaulting
										parameterForSourceComponent.setSelectedItem("");
									}
								}
							}
						}
						else {
							// its no datasource valuelistprovider
							Vector<WYSIYWYGParameter> parameters = null;
							if(vpType.equals(new ParametersTemplate().getValueListProviderType())) {
								parameters = new ParametersTemplate().getPossibleParameters();
							} else if(vpType.equals(new StatusTemplate().getValueListProviderType())) {
								parameters = new StatusTemplate().getPossibleParameters();
							}

							if(parameters != null) {
								// there is a template for this vp
								for(WYSIYWYGParameter parameter : parameters) {
									parameterForSourceComponent.addItem(parameter.getParameterName());
								}
								parameterForSourceComponent.addItem("");
								parameterForSourceComponent.setSelectedItem("");
							}
							else {
								// there is no template for this vp, so user has to
								// enter the value by hand...
								parameterForSourceComponent.setEditable(true);
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			LOG.warn("initParameterForSourceComponent failed: " + e, e);
		}
	}
}
