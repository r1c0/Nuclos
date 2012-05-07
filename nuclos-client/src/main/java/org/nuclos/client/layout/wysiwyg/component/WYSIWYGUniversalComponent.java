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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.client.common.NuclosCollectableComponentFactory;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_COMPONENT;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.util.DnDUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOption;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOptions;
import org.nuclos.client.ui.collect.component.CollectableComponentType;
import org.nuclos.client.ui.collect.component.CollectableIdTextField;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.client.ui.collect.component.CollectableOptionGroup;
import org.nuclos.client.ui.collect.component.DelegatingCollectablePanel;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponent;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.common.NuclosBusinessException;

/**
 * WYSIWYG-Component for all Collectable Component types.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 */
public class WYSIWYGUniversalComponent extends WYSIWYGCollectableComponent {
	
	//NUCLEUSINT-288
	public static final String ATTRIBUTEVALUE_LABEL_AND_CONTROL = "label and control";
	
	private JLabel messageLabel = new JLabel(COLLECTABLE_COMPONENT.STRING_WAITINGFORMETA);
	
	public static final String[][] PROPERTY_VALUES_STATIC = new String[][] {
		{PROPERTY_CONTROLTYPE, 
			ATTRIBUTEVALUE_CHECKBOX, ATTRIBUTEVALUE_COMBOBOX, ATTRIBUTEVALUE_DATECHOOSER, ATTRIBUTEVALUE_FILECHOOSER, 
			ATTRIBUTEVALUE_IDTEXTFIELD, ATTRIBUTEVALUE_LISTOFVALUES,ATTRIBUTEVALUE_OPTIONGROUP, ATTRIBUTEVALUE_TEXTAREA},
			//NUCLEUSINT-404 null value for show-only for defaulting component
			//NUCLEUSINT-288 added ATTRIBUTEVALUE_LABEL_AND_CONTROL
			{PROPERTY_SHOWONLY, ATTRIBUTEVALUE_LABEL_AND_CONTROL, ATTRIBUTEVALUE_CONTROL, ATTRIBUTEVALUE_BROWSEBUTTON, ATTRIBUTEVALUE_LABEL}
	};
	
	private org.nuclos.client.ui.collect.component.CollectableComponent collectableComponent;
	
	private WYSIWYGMetaInformation meta;
	
	public WYSIWYGUniversalComponent(WYSIWYGMetaInformation meta) {
		this.meta = meta;
		
		propertyNames.add(PROPERTY_CONTROLTYPE);
		
		propertiesToAttributes.put(PROPERTY_SHOWONLY, ATTRIBUTE_SHOWONLY);
		propertiesToAttributes.put(PROPERTY_CONTROLTYPE, ATTRIBUTE_CONTROLTYPE);
		
		propertySetMethods.put(PROPERTY_NAME, new PropertySetMethod(PROPERTY_NAME, "refresh"));
		propertySetMethods.put(PROPERTY_ROWS, new PropertySetMethod(PROPERTY_ROWS, "setRows"));
		propertySetMethods.put(PROPERTY_CONTROLTYPECLASS, new PropertySetMethod(PROPERTY_CONTROLTYPECLASS, "refresh"));
		propertySetMethods.put(PROPERTY_CONTROLTYPE, new PropertySetMethod(PROPERTY_CONTROLTYPE, "refresh"));
		propertySetMethods.put(PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertySetMethod(PROPERTY_FILL_CONTROL_HORIZONTALLY, "setFillControlHorizontally"));
		propertySetMethods.put(PROPERTY_LABEL, new PropertySetMethod(PROPERTY_LABEL, "setLabelText"));
		propertySetMethods.put(PROPERTY_COLUMNS, new PropertySetMethod(PROPERTY_COLUMNS, "setColumns"));
		
		propertyFilters.put(PROPERTY_NAME, new PropertyFilter(PROPERTY_NAME, EXPERT_MODE));
		propertyFilters.put(PROPERTY_VALUELISTPROVIDER, new PropertyFilter(PROPERTY_VALUELISTPROVIDER, EXPERT_MODE));
		propertyFilters.put(PROPERTY_CONTROLTYPECLASS, new PropertyFilter(PROPERTY_CONTROLTYPECLASS, EXPERT_MODE));
		//NUCLEUSINT-460 disable setting causing problems
		propertyFilters.put(PROPERTY_CONTROLTYPE, new PropertyFilter(PROPERTY_CONTROLTYPE, DISABLED));
		propertyFilters.put(PROPERTY_SHOWONLY, new PropertyFilter(PROPERTY_SHOWONLY, DISABLED));
		propertyFilters.put(PROPERTY_COLLECTABLECOMPONENTPROPERTY, new PropertyFilter(PROPERTY_COLLECTABLECOMPONENTPROPERTY, EXPERT_MODE));
		
		this.setLayout(new BorderLayout());
		this.messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.messageLabel.addMouseListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent#render()
	 */
	@Override
	protected void render() {		
		this.removeMouseListener();
		this.collectableComponent = null;
		this.removeAll();
		super.setBorder(null);
		if (properties != null && properties.getProperty(PROPERTY_NAME).getValue() != null) {
			// parser logic
			final String sFieldName = (String)properties.getProperty(PROPERTY_NAME).getValue();
			final String sControlTypeClass = (String)properties.getProperty(PROPERTY_CONTROLTYPECLASS).getValue();
			
			final CollectableEntity clcte = meta.getCollectableEntity();
			final CollectableEntityField clctef = meta.getEntityField(clcte.getName(), sFieldName);
			
			Integer iEnumeratedControlType = (getControlType()==-1)?clctef.getDefaultCollectableComponentType():getControlType();
			Class<org.nuclos.client.ui.collect.component.CollectableComponent> clsclctcomp = null;
			if (sControlTypeClass != null) {
				try {
					iEnumeratedControlType = null;
					/** @todo explicitly check that the class implements CollectableComponent */
					clsclctcomp = (Class<org.nuclos.client.ui.collect.component.CollectableComponent>) Class.forName(sControlTypeClass);
				}
				catch (ClassNotFoundException ex) {
					// @SuppressWarnings("unused")
					final String sMessage = "Unbekannte Klasse: " + sControlTypeClass;
					//throw new SAXException(sMessage, ex);
				}
			}
			final CollectableComponentType clctcomptype = new CollectableComponentType(iEnumeratedControlType, clsclctcomp);

			final org.nuclos.client.ui.collect.component.CollectableComponent clctcomp = NuclosCollectableComponentFactory.getInstance().newCollectableComponent(clcte, sFieldName, clctcomptype, false);
			
			// enabled:
			final boolean bEnabled = (Boolean)properties.getProperty(PROPERTY_ENABLED).getValue(boolean.class, this);
			clctcomp.setEnabled(bEnabled);
			clctcomp.setEnabledByInitial(bEnabled);

			// Id text fields may never be enabled except in search mode!
			if (clctcomp instanceof CollectableIdTextField) {
				clctcomp.setEnabled(false);
				clctcomp.setEnabledByInitial(false);
			}

			// insertable:
			clctcomp.setInsertable((Boolean)properties.getProperty(PROPERTY_INSERTABLE).getValue(boolean.class, this));

			// visible:
			clctcomp.setVisible(true);//(Boolean)properties.getProperty(PROPERTY_VISIBLE).getValue(boolean.class, this));

			if (true) {//(Boolean)properties.getProperty(PROPERTY_VISIBLE).getValue(boolean.class, this)) {
				// show-only:
				boolean bHideLabel = false;
				boolean bHideControl = false;
				boolean bShowBrowseButtonOnly = false;
				final String sShowOnly = (String)properties.getProperty(PROPERTY_SHOWONLY).getValue();
				if (sShowOnly != null) {
					if (sShowOnly.equals(ATTRIBUTEVALUE_LABEL)) {
						bHideControl = true;
					} else if (sShowOnly.equals(ATTRIBUTEVALUE_CONTROL)) {
						bHideLabel = true;
					} else if (sShowOnly.equals(ATTRIBUTEVALUE_BROWSEBUTTON)) {
						bShowBrowseButtonOnly = true;
						bHideLabel = true;
					} else if (sShowOnly.equals(ATTRIBUTEVALUE_LABEL_AND_CONTROL)) {
						//NUCLEUSINT-288
						bHideControl = false;
						bShowBrowseButtonOnly = false;
						bHideLabel = false;
					}
				} else {
					bHideControl = false;
					bShowBrowseButtonOnly = false;
					bHideLabel = false;
				}
				if (clctcomp instanceof LabeledCollectableComponent) {
					final LabeledComponent labcomp = ((LabeledCollectableComponent) clctcomp).getLabeledComponent();
					labcomp.getJLabel().setVisible(!bHideLabel);
					labcomp.getControlComponent().setVisible(!bHideControl);

					if (bShowBrowseButtonOnly && (clctcomp instanceof CollectableListOfValues)) {
						final CollectableListOfValues clctlov = (CollectableListOfValues) clctcomp;
						clctlov.setBrowseButtonVisibleOnly(true);
					}
				}
				
				if (clctcomp instanceof DelegatingCollectablePanel) {
					final DelegatingCollectablePanel delpnl = (DelegatingCollectablePanel) clctcomp;
					delpnl.setVisibleLabel(!bHideLabel);
					delpnl.setVisibleControl(!bHideControl);
				}
				
				// label:
				if (!bHideLabel) {
					String sLabel = (String)properties.getProperty(PROPERTY_LABEL).getValue();
					if (sLabel == null) {
						sLabel = clctef.getLabel();
					}
					clctcomp.setLabelText(sLabel);
				}
			}	// if (bVisible)

			// fill-control-horizontally:
			Boolean bFillControlHorizontally = (Boolean)properties.getProperty(PROPERTY_FILL_CONTROL_HORIZONTALLY).getValue();
			if (bFillControlHorizontally != null) {
				clctcomp.setFillControlHorizontally(bFillControlHorizontally);
			}

			// rows:
			final Integer iRows = (Integer)properties.getProperty(PROPERTY_ROWS).getValue();
			if (iRows != null) {
				clctcomp.setRows(iRows);
			}
			else {
				clctcomp.setRows(DEFAULTVALUE_TEXTAREA_ROWS);
			}

			// columns:
			Integer iColumns = (Integer)properties.getProperty(PROPERTY_COLUMNS).getValue();
			if (iColumns == null) {
				iColumns = DEFAULTVALUE_TEXTFIELD_COLUMNS;
			}
			if (iColumns != null) {
				clctcomp.setColumns(iColumns);
			}

			// mnemonic:
			final String sMnemonic = (String)properties.getProperty(PROPERTY_MNEMONIC).getValue();
			if (sMnemonic != null && sMnemonic.length() > 0) {
				clctcomp.setMnemonic(sMnemonic.charAt(0));
			}

			clctcomp.setToolTipText(clctef.getDescription());
			// set default tooltip from the field's description
			// may be overwritten by LayoutML description element
			if (properties.getProperty(PROPERTY_DESCRIPTION) != null) {
				String desc = (String)properties.getProperty(PROPERTY_DESCRIPTION).getValue();
				if (!StringUtils.isNullOrEmpty(desc)) {
					clctcomp.setToolTipText(desc);
				}
			}
				

			// opaqueness / transparency:
			clctcomp.setOpaque((Boolean)properties.getProperty(PROPERTY_OPAQUE).getValue(boolean.class, this));
			
			// options
			if (properties.getProperty(PROPERTY_OPTIONS) != null) {
				WYSIWYGOptions options = (WYSIWYGOptions)properties.getProperty(PROPERTY_OPTIONS).getValue();
				if (options != null) {
					List<String[]> lstOptions = new ArrayList<String[]>();
					
					for (WYSIWYGOption option : options.getAllOptionValues()) {
						final String[] asOptions = new String[3];
						asOptions[0] = option.getValue();
						asOptions[1] = option.getLabel();
						asOptions[2] = StringUtils.emptyIfNull(option.getMnemonic());
						lstOptions.add(asOptions);
					}
					
					if (clctcomp instanceof CollectableOptionGroup) {
						final CollectableOptionGroup group = (CollectableOptionGroup) clctcomp;
						if (lstOptions != null) {
							group.setOptions(lstOptions);
						}
						group.setDefaultOption(options.getDefaultValue());
						if (WYSIWYGOptions.ORIENTATION_HORIZONTAL.equals(options.getOrientation())) {
							group.setOrientation(SwingConstants.HORIZONTAL);
						}
						else if (WYSIWYGOptions.ORIENTATION_VERTICAL.equals(options.getOrientation())) {
							group.setOrientation(SwingConstants.VERTICAL);
						}
					}
				}
			}
			
			clctcomp.getJComponent().setBorder((Border)properties.getProperty(PROPERTY_BORDER).getValue(Border.class, this));
			clctcomp.getJComponent().setBackground((Color)properties.getProperty(PROPERTY_BACKGROUNDCOLOR).getValue(Color.class, this));
			clctcomp.getJComponent().setPreferredSize((Dimension)properties.getProperty(PROPERTY_PREFFEREDSIZE).getValue(Dimension.class, this));
			
			this.add(clctcomp.getJComponent(), BorderLayout.CENTER);
			this.collectableComponent = clctcomp;
		}
		else {
			super.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			this.add(messageLabel, BorderLayout.CENTER);
		}
		this.addMouseListener();
		DnDUtil.addDragGestureListener(this, this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent#getPropertyValuesStatic()
	 */
	@Override
	public String[][] getPropertyValuesStatic() {
		return PROPERTY_VALUES_STATIC;
	}

	public int getControlType() {
		return getControlType(this.properties.getProperty(PROPERTY_CONTROLTYPE));
	}
	
	private int getControlType(PropertyValue<?> property) {	
		String sControlType = null;
		if (properties.getProperty(PROPERTY_CONTROLTYPE) != null){
			//NUCLEUSINT-460
			if (property != null){
				sControlType = (String)property.getValue();
			}
		}
		
		if (sControlType == null)
			return -1;
		
		if (sControlType.equals(ATTRIBUTEVALUE_CHECKBOX))
			return CollectableComponentTypes.TYPE_CHECKBOX;
		if (sControlType.equals(ATTRIBUTEVALUE_COMBOBOX))
			return CollectableComponentTypes.TYPE_COMBOBOX;
		if (sControlType.equals(ATTRIBUTEVALUE_DATECHOOSER))
			return CollectableComponentTypes.TYPE_DATECHOOSER;
		if (sControlType.equals(ATTRIBUTEVALUE_HYPERLINK))
			return CollectableComponentTypes.TYPE_HYPERLINK;
		if (sControlType.equals(ATTRIBUTEVALUE_EMAIL))
			return CollectableComponentTypes.TYPE_EMAIL;
		if (sControlType.equals(ATTRIBUTEVALUE_FILECHOOSER))
			return CollectableComponentTypes.TYPE_FILECHOOSER;
		if (sControlType.equals(ATTRIBUTEVALUE_IDTEXTFIELD))
			return CollectableComponentTypes.TYPE_IDTEXTFIELD;
		if (sControlType.equals(ATTRIBUTEVALUE_LISTOFVALUES))
			return CollectableComponentTypes.TYPE_LISTOFVALUES;
		if (sControlType.equals(ATTRIBUTEVALUE_OPTIONGROUP))
			return CollectableComponentTypes.TYPE_OPTIONGROUP;
		if (sControlType.equals(ATTRIBUTEVALUE_TEXTAREA))
			return CollectableComponentTypes.TYPE_TEXTAREA;

		return -1;

	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
		PropertyUtils.validatePreferredSize((Dimension) values.get(PROPERTY_PREFFEREDSIZE).getValue(), 10, 10);

		/** checking controltype and controltypeclass 
		 * NUCLEUSINT-269
		 * */
		String sFieldName = (String) values.get(PROPERTY_NAME).getValue();
		String sControlTypeClass = (String) values.get(PROPERTY_CONTROLTYPECLASS).getValue();

		CollectableEntity clcte = meta.getCollectableEntity();
		CollectableEntityField clctef = meta.getEntityField(clcte.getName(), sFieldName);

		Integer iEnumeratedControlType = (getControlType(values.get(PROPERTY_CONTROLTYPE)) == -1) ? clctef.getDefaultCollectableComponentType() : getControlType(values.get(PROPERTY_CONTROLTYPE));
		Class<org.nuclos.client.ui.collect.component.CollectableComponent> clsclctcomp = null;
		if (sControlTypeClass != null) {
			iEnumeratedControlType = null;
			try {
				clsclctcomp = (Class<org.nuclos.client.ui.collect.component.CollectableComponent>) Class.forName(sControlTypeClass);
			} catch (ClassNotFoundException ex) {
				throw new NuclosBusinessException(WYSIWYGStringsAndLabels.partedString(COLLECTABLE_COMPONENT.PROPERTY_VALIDATION_MESSAGE_CONTROLTYPECLASS_NOT_FOUND, sControlTypeClass));
			}
		}
		CollectableComponentType clctcomptype = new CollectableComponentType(iEnumeratedControlType, clsclctcomp);

		try {
			NuclosCollectableComponentFactory.getInstance().newCollectableComponent(clcte, sFieldName, clctcomptype, false);
		} catch (CommonFatalException ex) {
			ex.printStackTrace();
			String controlType = (String) values.get(PROPERTY_CONTROLTYPE).getValue();
			throw new NuclosBusinessException(WYSIWYGStringsAndLabels.partedString(COLLECTABLE_COMPONENT.PROPERTY_VALIDATION_MESSAGE_CONTROLTYPE_NOT_VALID, controlType));
		}
		/** checking controltype and controltypeclass */
	}

	public void refresh(String s) {
		this.render();
	}
	
	public void setColumns(int columns) {
		if (collectableComponent != null) {
			collectableComponent.setColumns(columns);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (collectableComponent != null) {
			collectableComponent.setEnabled(enabled);
		}
	}

	public void setFillControlHorizontally(boolean fill) {
		if (collectableComponent != null) {
			collectableComponent.setFillControlHorizontally(fill);
		}
	}

	public void setInsertable(boolean insertable) {
		if (collectableComponent != null) {
			collectableComponent.setInsertable(insertable);
		}
	}

	public void setLabelText(String label) {
		if (collectableComponent != null) {
			collectableComponent.setLabelText(label);
		}
	}

	public void setMnemonic(char c) {
		if (collectableComponent != null) {
			collectableComponent.setMnemonic(c);
		}
	}

	public void setRows(int rows) {
		if (collectableComponent != null) {
			collectableComponent.setRows(rows);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setOpaque(boolean)
	 */
	@Override
	public void setOpaque(boolean opaque) {
		if (collectableComponent != null) {
			collectableComponent.setOpaque(opaque);
		}
	}

	@Override
	public void setToolTipText(String desc) {
		if (collectableComponent != null && desc != null) {
			super.setToolTipText(desc);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color bg) {
		if (collectableComponent != null) {
			collectableComponent.getJComponent().setBackground(bg);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setBorder(javax.swing.border.Border)
	 */
	@Override
	public void setBorder(Border border) {
		if (collectableComponent != null) {
			collectableComponent.getJComponent().setBorder(border);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setFont(java.awt.Font)
	 */
	@Override
	public void setFont(Font font) {
		if (collectableComponent != null) {
			collectableComponent.getJComponent().setFont(font);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
	 */
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		super.setPreferredSize(preferredSize);
		if (collectableComponent != null) {
			collectableComponent.getJComponent().setPreferredSize(preferredSize);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setMinimumSize(java.awt.Dimension)
	 */
	@Override
	public void setMinimumSize(Dimension minimumSize) {
		super.setMinimumSize(minimumSize);
		if (collectableComponent != null) {
			collectableComponent.getJComponent().setMinimumSize(minimumSize);
		}
	}
}
