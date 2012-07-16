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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.editor;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.VALUELIST_PROVIDER_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGParameter;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.querybuilder.DatasourceUtils;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;

/**
 *
 *
 *
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ValueListProviderEditor extends JDialog implements SaveAndCancelButtonPanelControllable {

	private static final Logger LOG = Logger.getLogger(ValueListProviderEditor.class);

	private final int height = 300;
	private final int width = 650;

	private final JLabel lblType = new JLabel(VALUELIST_PROVIDER_EDITOR.LABEL_VALUELIST_PROVIDER_NAME);
	private final JComboBox cbxType = new JComboBox();
	private final JLabel lblIdField = new JLabel(VALUELIST_PROVIDER_EDITOR.LABEL_PARAMETER_ID);
	private final JComboBox cbxIdField = new JComboBox();
	private final JLabel lblNameField = new JLabel(VALUELIST_PROVIDER_EDITOR.LABEL_PARAMETER_NAME);
	private final JComboBox cbxNameField = new JComboBox();
	private final JLabel lblDefaultMarkerField = new JLabel(VALUELIST_PROVIDER_EDITOR.LABEL_PARAMETER_DEFAULTMARKER);
	private final JComboBox cbxDefaultMarkerField = new JComboBox();

	private final JLabel lblEntity = new JLabel(VALUELIST_PROVIDER_EDITOR.LABEL_ENTITY);
	private final JTextField tfEntity = new JTextField();
	private final JLabel lblField = new JLabel(VALUELIST_PROVIDER_EDITOR.LABEL_FIELD);
	private final JTextField tfField = new JTextField();

	private final JPanel parameterContainer = new JPanel();;
	private final JPanel valuecontainer = new JPanel();

	private WYSIWYGValuelistProvider wysiwygStaticValuelistProvider;

	private WYSIWYGValuelistProvider backupWYSIWYGStaticValuelistProvider;

	public static WYSIWYGValuelistProvider returnValuelistProvider;

	private static final Logger log = Logger.getLogger(ValueListProviderEditor.class);

	private JButton btnAddParameter = new JButton(VALUELIST_PROVIDER_EDITOR.BUTTON_ADD_PARAMETER);;

	public static final String DATASOURCE_IDFIELD = "id-fieldname";
	public static final String DATASOURCE_NAMEFIELD = "fieldname";
	public static final String DATASOURCE_DEFAULTMARKERFIELD = "default-fieldname";
	public static final String DATASOURCE_VALUELISTPROVIDER = "valuelistProvider";

	/**
	 *
	 * @param wysiwygStaticValuelistProvider
	 */
	private ValueListProviderEditor(WYSIWYGComponent c, WYSIWYGValuelistProvider wysiwygStaticValuelistProvider) {
		parameterContainer.setLayout(new TableLayout(new double[][]{{TableLayout.FILL}, {}}));

		this.setIconImage(NuclosIcons.getInstance().getScaledDialogIcon(48).getImage());
		//NUCLEUSINT-312
		setTitle(VALUELIST_PROVIDER_EDITOR.TITLE_VALUELIST_PROVIDER_EDITOR);
		//TODO align relative to parent Component
		this.wysiwygStaticValuelistProvider = wysiwygStaticValuelistProvider;
		double[][] layout = {{InterfaceGuidelines.MARGIN_LEFT, TableLayout.FILL, InterfaceGuidelines.MARGIN_RIGHT}, {InterfaceGuidelines.MARGIN_TOP, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, TableLayout.PREFERRED}};
		this.setLayout(new TableLayout(layout));

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		cbxType.setPreferredSize(new Dimension(250, 20));
		cbxType.setEditable(true);
		cbxType.setRenderer(new BasicComboBoxRenderer(){

			@Override
			public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
				if ((VALUELIST_PROVIDER_EDITOR.SEPARATOR_DATASOURCES.equals(((JLabel)c).getText())) ||
						 VALUELIST_PROVIDER_EDITOR.SEPARATOR_SYSTEM.equals(((JLabel)c).getText())) {
					c.setEnabled(false);
					c.setBackground(list.getBackground());
				} else {
					c.setEnabled(true);
				}
				return c;
			}
		});
		cbxType.setModel(new DefaultComboBoxModel(){

			@Override
			public void setSelectedItem(Object anObject) {
				if (VALUELIST_PROVIDER_EDITOR.SEPARATOR_DATASOURCES.equals(anObject) ||
						VALUELIST_PROVIDER_EDITOR.SEPARATOR_SYSTEM.equals(anObject)){
					return;
				}
				super.setSelectedItem(anObject);
			}
		});

		initTypes();
		if (!StringUtils.looksEmpty(this.wysiwygStaticValuelistProvider.getType())) {
			// Find input in list and select it...
			for (int i  = 0; i < cbxType.getItemCount(); i++) {
				String sType = this.wysiwygStaticValuelistProvider.getType();
				if (sType.endsWith(ValuelistProviderVO.SUFFIX)) {
					sType = sType.substring(0, sType.lastIndexOf(ValuelistProviderVO.SUFFIX));
				}
				if (sType.equals(cbxType.getItemAt(i).toString())) {
					cbxType.setSelectedIndex(i);
				}
			}
		}

		((JTextField)cbxType.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {changeValueForType();}
			@Override
			public void insertUpdate(DocumentEvent e) {changeValueForType();}
			@Override
			public void changedUpdate(DocumentEvent e) {changeValueForType();}
		});
		cbxType.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {changeValueForType();}
		});

		/**
		 * the valuelist provider type panel
		 */
		valuecontainer.setLayout(new TableLayout(new double[][]{{TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED},
				{TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}}));
		valuecontainer.add(lblType, "0,0");
		TableLayoutConstraints constraint = new TableLayoutConstraints(2, 0, 2, 0, TableLayout.FULL, TableLayout.CENTER);
		valuecontainer.add(cbxType, constraint);
		cbxIdField.setPreferredSize(new Dimension(250, 20));
		cbxNameField.setPreferredSize(new Dimension(250, 20));
		cbxDefaultMarkerField.setPreferredSize(new Dimension(250, 20));
		tfEntity.setPreferredSize(new Dimension(250, 20));
		tfField.setPreferredSize(new Dimension(250, 20));

		btnAddParameter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addParameterPanelIntoPanel(null, true, true);
			}
		});
		valuecontainer.add(btnAddParameter, "4,0");

		initFields();
		this.add(valuecontainer, "1,1");

		/**
		 * the parameters
		 */
		JScrollPane scrollbar = new JScrollPane(parameterContainer);
		scrollbar.getVerticalScrollBar().setVisible(true);
		scrollbar.getVerticalScrollBar().setUnitIncrement(20);
		this.add(scrollbar, "1,3");

		/**
		 * save and cancel
		 */

		JButton deleteValueList = new JButton(VALUELIST_PROVIDER_EDITOR.BUTTON_DELETE_VALUELIST_PROVIDER);
		deleteValueList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearValuelistProviderForComponent();
			}

		});

		constraint = new TableLayoutConstraints(0, 4, 2, 4);
		ArrayList<AbstractButton> buttons = new ArrayList<AbstractButton>(1);
		buttons.add(deleteValueList);
		this.add(new SaveAndCancelButtonPanel(parameterContainer.getBackground(), this, buttons), constraint);

		try {
			backupWYSIWYGStaticValuelistProvider = (WYSIWYGValuelistProvider) wysiwygStaticValuelistProvider.clone();
		} catch (CloneNotSupportedException e1) {
			/** nothing to do, does support clone() */
		}

//		for (WYSIYWYGParameter wysiwygParameter : this.wysiwygStaticValuelistProvider.getAllWYSIYWYGParameter()) {
//			if (isSelectedTypeFromDatasource()) {
//				addParameterPanelIntoPanel(wysiwygParameter, false, false);
//			} else {
//				addParameterPanelIntoPanel(wysiwygParameter, true, true);
//			}
//		}

		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - width) / 2;
		int y = (screenSize.height - height) / 2;

		this.setBounds(x, y, width, height);
		// get the editor to where it belongs, not on the other screen...
		if (c != null)
			setLocationRelativeTo(c.getParentEditor().getMainEditorPanel().getTopLevelAncestor());
		this.setModal(true);
		this.setVisible(true);
	}

	private void initTypes() {
		//NUCLEUSINT-1043
		cbxType.addItem(this.wysiwygStaticValuelistProvider.getType());
		cbxType.addItem(VALUELIST_PROVIDER_EDITOR.SEPARATOR_SYSTEM);
		cbxType.addItem("status");
		cbxType.addItem("parameters");
		try {
			Collection<ValuelistProviderVO> vpVOs = DatasourceDelegate.getInstance().getAllValuelistProvider();
			List<ValuelistProviderVO> sortedVPVOs = new ArrayList<ValuelistProviderVO>(vpVOs);
			Collections.sort(sortedVPVOs, new Comparator<ValuelistProviderVO>() {
				@Override
				public int compare(ValuelistProviderVO o1, ValuelistProviderVO o2) {
					return o1.getName().compareTo(o2.getName());
				}});
			if (!sortedVPVOs.isEmpty()) {
				cbxType.addItem(VALUELIST_PROVIDER_EDITOR.SEPARATOR_DATASOURCES);
			}
			for (ValuelistProviderVO vpVO : sortedVPVOs) {
				if (vpVO.getValid() != null && vpVO.getValid())
					cbxType.addItem(vpVO);
			}
		}
		catch(CommonPermissionException e) {
			Errors.getInstance().showExceptionDialog(this, e);
		}
	}

	private boolean isSelectedTypeFromDatasource() {
		return cbxType.getEditor().getItem() != null &&
			cbxType.getEditor().getItem() instanceof ValuelistProviderVO;
	}

	private void initFields() {
		boolean showNameField = false;
		boolean showIdField = false;
		boolean showDefaultMarkerField = false;

		//remove parameter
		parameterContainer.removeAll();

		if (isSelectedTypeFromDatasource()) {
			showIdField = true;
			showNameField = true;
			showDefaultMarkerField = true;

			btnAddParameter.setEnabled(false);

			//remove id fields and name fields
			cbxIdField.removeAllItems();
			cbxNameField.removeAllItems();
			cbxDefaultMarkerField.removeAllItems();
			cbxDefaultMarkerField.addItem("");

			tfEntity.setText("");
			tfField.setText("");

			if (wysiwygStaticValuelistProvider.isEntityAndFieldAvaiable()) {
				tfEntity.setText(wysiwygStaticValuelistProvider.getEntity());
				tfField.setText(wysiwygStaticValuelistProvider.getField());
			}

			ValuelistProviderVO vpVO = (ValuelistProviderVO) cbxType.getEditor().getItem();
			try {
				// add empty id field (for valuelist provider that should generate plain value fields)
				cbxIdField.addItem("");
				// add id fiels and name fields
				for (String sColumn : DatasourceUtils.getColumnsWithoutQuotes(
					DatasourceDelegate.getInstance().getColumnsFromXml(vpVO.getSource()))) {
					cbxIdField.addItem(sColumn);
					cbxNameField.addItem(sColumn);
					cbxDefaultMarkerField.addItem(sColumn);
				}

				// sync parameters
				List<DatasourceParameterVO> lstParameterVOs = DatasourceDelegate.getInstance().getParametersFromXML(vpVO.getSource());
				Collections.sort(lstParameterVOs, new Comparator<DatasourceParameterVO>() {
					@Override
					public int compare(DatasourceParameterVO o1,
						DatasourceParameterVO o2) {
						return o2.getParameter().compareToIgnoreCase(o1.getParameter());
					}
				});
				List<WYSIYWYGParameter> lstWYSIYWYGParameterToRemove = new ArrayList<WYSIYWYGParameter>();
				for (WYSIYWYGParameter wysiwygParameter : wysiwygStaticValuelistProvider.getAllWYSIYWYGParameter()) {
					if (DATASOURCE_IDFIELD.equals(wysiwygParameter.getParameterName())) {
						cbxIdField.setSelectedItem(extractFieldName(wysiwygParameter.getParameterValue()));
					} else if (DATASOURCE_NAMEFIELD.equals(wysiwygParameter.getParameterName())) {
						cbxNameField.setSelectedItem(extractFieldName(wysiwygParameter.getParameterValue()));
					} else if (DATASOURCE_DEFAULTMARKERFIELD.equals(wysiwygParameter.getParameterName())) {
						cbxDefaultMarkerField.setSelectedItem(extractFieldName(wysiwygParameter.getParameterValue()));
					} else if (DATASOURCE_VALUELISTPROVIDER.equals(wysiwygParameter.getParameterName())) {
						/* do not show in editor */
					} else {
						boolean wysiwygParamFound = false;
						for (DatasourceParameterVO p : lstParameterVOs) {
							if (p.getParameter().equals(wysiwygParameter.getParameterName())) {
								wysiwygParamFound = true;
							}
						}
						if (!wysiwygParamFound) {
							lstWYSIYWYGParameterToRemove.add(wysiwygParameter);
						}
					}
				}
				for (WYSIYWYGParameter wysiwygParameterToRemove : lstWYSIYWYGParameterToRemove) {
					wysiwygStaticValuelistProvider.removeWYSIYWYGParameter(wysiwygParameterToRemove);
				}
				for (DatasourceParameterVO paramVO : lstParameterVOs) {
					WYSIYWYGParameter wysiwygParameter = null;
					for (WYSIYWYGParameter p : wysiwygStaticValuelistProvider.getAllWYSIYWYGParameterSorted(true)) {
						if (paramVO.getParameter().equals(p.getParameterName())) {
							wysiwygParameter = p;
						}
					}
					if (wysiwygParameter == null) {
						wysiwygParameter = new WYSIYWYGParameter();
						wysiwygParameter.setParameterName(paramVO.getParameter());
						wysiwygStaticValuelistProvider.addWYSIYWYGParameter(wysiwygParameter);
					}
					addParameterPanelIntoPanel(wysiwygParameter, false, false);
				}

			} catch(CommonBusinessException e) {
				Errors.getInstance().showExceptionDialog(this, e);
			}
		} else {
			// no ValuelistProvierVO seletected
			btnAddParameter.setEnabled(true);
			for (WYSIYWYGParameter wysiwygParameter : wysiwygStaticValuelistProvider.getAllWYSIYWYGParameterSorted(true)) {
				addParameterPanelIntoPanel(wysiwygParameter, true, true);
			}
		}

		if (showIdField) {
			valuecontainer.add(lblIdField, "0,2");
			valuecontainer.add(cbxIdField, "2,2,l,c");
		} else {
			valuecontainer.remove(lblIdField);
			valuecontainer.remove(cbxIdField);
		}

		if (showNameField) {
			valuecontainer.add(lblNameField, "0,4");
			valuecontainer.add(cbxNameField, "2,4,l,c");
		} else {
			valuecontainer.remove(lblNameField);
			valuecontainer.remove(cbxNameField);
		}

		if (showDefaultMarkerField) {
			valuecontainer.add(lblDefaultMarkerField, "0,6");
			valuecontainer.add(cbxDefaultMarkerField, "2,6,l,c");
		} else {
			valuecontainer.remove(lblDefaultMarkerField);
			valuecontainer.remove(cbxDefaultMarkerField);
		}

		if (wysiwygStaticValuelistProvider.isEntityAndFieldAvaiable()) {
			valuecontainer.add(lblEntity, "0,8");
			valuecontainer.add(tfEntity, "2,8,l,c");
			valuecontainer.add(lblField, "0,10");
			valuecontainer.add(tfField, "2,10,l,c");
		}

		parameterContainer.updateUI();
		valuecontainer.updateUI();
	}
	
	private static String extractFieldName(String value) {
		// extract label if no alias is set. we strip something like T1."strname" @see NUCLOS-645
		if (value != null) {
			int idxDot = value.indexOf(".");
			if (idxDot != -1)
				value = value.substring(idxDot + 1);
			value = value.replaceAll("\"", "");				
		}
		return value;
	}

	/**
	 * This Method shows the Editor. <br>
	 * Works like {@link JOptionPane#showInputDialog(Object)}
	 *
	 * @param wysiwygStaticValuelistProvider the Value
	 * @return
	 */
	public static WYSIWYGValuelistProvider showEditor(WYSIWYGValuelistProvider wysiwygStaticValuelistProvider) {
		return showEditor(null, wysiwygStaticValuelistProvider);
	}

	public static WYSIWYGValuelistProvider showEditor(WYSIWYGComponent component, WYSIWYGValuelistProvider wysiwygStaticValuelistProvider) {
		new ValueListProviderEditor(component, wysiwygStaticValuelistProvider);
		//NUCLEUSINT-811
		if (component != null) {
			//validation only within wysiwyg editor
			try {
				validateValuelistProvider(component, returnValuelistProvider);
			} catch (CommonBusinessException e) {
				// Display the Validation Message and redisplay the Valuelistprovidereditor
				Errors.getInstance().showExceptionDialog(null, e);
				new ValueListProviderEditor(component, wysiwygStaticValuelistProvider);
			}
		}
		return returnValuelistProvider;
	}

	/**
	 * This Method validates the {@link AttributeCVO} against the Values set for the Valuelistprovider.
	 *
	 * @param component the {@link WYSIWYGComponent} for comparison
	 * @param valuelistProvider the {@link WYSIWYGValuelistProvider} to check
	 * @throws CommonBusinessException if validation is not successful
	 * NUCLEUSINT-811
	 */
	private static void validateValuelistProvider(WYSIWYGComponent component, WYSIWYGValuelistProvider valuelistProvider) throws CommonBusinessException {
		if(valuelistProvider != null && "parameters".equals(valuelistProvider.getType())) {
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

	/**
	 * Remove the {@link WYSIWYGValuelistProvider}.
	 */
	private final void clearValuelistProviderForComponent() {
		boolean isEntityAndFieldAvaiable = wysiwygStaticValuelistProvider.isEntityAndFieldAvaiable();
		wysiwygStaticValuelistProvider = null;
		backupWYSIWYGStaticValuelistProvider = null;
		// NUCLEUSINT-669
		returnValuelistProvider = new WYSIWYGValuelistProvider(isEntityAndFieldAvaiable);
		this.dispose();
	}

	/**
	 * Sets the Type for the {@link WYSIWYGValuelistProvider}
	 */
	private final void changeValueForType() {
		if (cbxType.getEditor().getItem() != null) {
			if (isSelectedTypeFromDatasource()){
				wysiwygStaticValuelistProvider.setType(cbxType.getEditor().getItem().toString() + ValuelistProviderVO.SUFFIX);
			} else {
				wysiwygStaticValuelistProvider.setType(cbxType.getEditor().getItem().toString());
			}
		} else {
			wysiwygStaticValuelistProvider.setType("");
		}
		initFields();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performCancelAction()
	 */
	@Override
	public void performCancelAction() {
		wysiwygStaticValuelistProvider = null;
		wysiwygStaticValuelistProvider = backupWYSIWYGStaticValuelistProvider;
		returnValuelistProvider = backupWYSIWYGStaticValuelistProvider;
		this.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performSaveAction()
	 */
	@Override
	public void performSaveAction() {
		if (wysiwygStaticValuelistProvider.isEntityAndFieldAvaiable()) {
			if (StringUtils.looksEmpty(tfEntity.getText()) || StringUtils.looksEmpty(tfField.getText())) {
				JOptionPane.showMessageDialog(this, VALUELIST_PROVIDER_EDITOR.MESSAGE_ENTITYAND_FIELD_NOT_NULL);
				return;
			}
			wysiwygStaticValuelistProvider.setEntity(tfEntity.getText());
			wysiwygStaticValuelistProvider.setField(tfField.getText());
		}

		if (isSelectedTypeFromDatasource()) {
			WYSIYWYGParameter paramValuelistProviderDatasource = null;
			WYSIYWYGParameter parameterIdField = null;
			WYSIYWYGParameter parameterNameField = null;
			WYSIYWYGParameter parameterDefaultMarkerField = null;

			for (WYSIYWYGParameter wysiywygParameter : wysiwygStaticValuelistProvider.getAllWYSIYWYGParameter()) {
				if (DATASOURCE_VALUELISTPROVIDER.equals(wysiywygParameter.getParameterName())) {
					paramValuelistProviderDatasource = wysiywygParameter;
				} else if (DATASOURCE_IDFIELD.equals(wysiywygParameter.getParameterName())) {
					parameterIdField = wysiywygParameter;
				} else if (DATASOURCE_NAMEFIELD.equals(wysiywygParameter.getParameterName())) {
					parameterNameField = wysiywygParameter;
				} else if (DATASOURCE_DEFAULTMARKERFIELD.equals(wysiywygParameter.getParameterName())) {
					parameterDefaultMarkerField = wysiywygParameter;
				}
			}

			if (parameterIdField != null) {
				parameterIdField.setParameterValue(cbxIdField.getSelectedItem().toString());
			} else {
				parameterIdField = new WYSIYWYGParameter();
				parameterIdField.setParameterName(DATASOURCE_IDFIELD);
				parameterIdField.setParameterValue(cbxIdField.getSelectedItem().toString());
				wysiwygStaticValuelistProvider.addWYSIYWYGParameter(parameterIdField);
			}

			if (parameterNameField != null) {
				parameterNameField.setParameterValue(cbxNameField.getSelectedItem().toString());
			} else {
				parameterNameField = new WYSIYWYGParameter();
				parameterNameField.setParameterName(DATASOURCE_NAMEFIELD);
				parameterNameField.setParameterValue(cbxNameField.getSelectedItem().toString());
				wysiwygStaticValuelistProvider.addWYSIYWYGParameter(parameterNameField);
			}

			if (!"".equals(cbxDefaultMarkerField.getSelectedItem())) {
				if (parameterDefaultMarkerField != null) {
					parameterDefaultMarkerField.setParameterValue(cbxDefaultMarkerField.getSelectedItem().toString());
				} else {
					parameterDefaultMarkerField = new WYSIYWYGParameter();
					parameterDefaultMarkerField.setParameterName(DATASOURCE_DEFAULTMARKERFIELD);
					parameterDefaultMarkerField.setParameterValue(cbxDefaultMarkerField.getSelectedItem().toString());
					wysiwygStaticValuelistProvider.addWYSIYWYGParameter(parameterDefaultMarkerField);
				}
			}
			else if (parameterDefaultMarkerField != null) {
				wysiwygStaticValuelistProvider.removeWYSIYWYGParameter(parameterDefaultMarkerField);
			}

			String sDatasource = wysiwygStaticValuelistProvider.getType();
			sDatasource = sDatasource.substring(0, sDatasource.lastIndexOf(ValuelistProviderVO.SUFFIX));
			if (paramValuelistProviderDatasource != null) {
				paramValuelistProviderDatasource.setParameterValue(sDatasource);
			} else {
				paramValuelistProviderDatasource = new WYSIYWYGParameter();
				paramValuelistProviderDatasource.setParameterName(DATASOURCE_VALUELISTPROVIDER);
				paramValuelistProviderDatasource.setParameterValue(sDatasource);
				wysiwygStaticValuelistProvider.addWYSIYWYGParameter(paramValuelistProviderDatasource);
			}
		}
		returnValuelistProvider = wysiwygStaticValuelistProvider;
		if (returnValuelistProvider.getType().equals(""))
			performCancelAction();
		else
			this.dispose();
	}

	/**
	 * This Method removes a {@link ParameterPanel} from the {@link ValueListProviderEditor}.
	 * @param parameterPanel to be removed
	 */
	public void removeParameterFromPanel(ParameterPanel parameterPanel) {
		this.wysiwygStaticValuelistProvider.removeWYSIYWYGParameter(parameterPanel.getWYSIWYGParameter());
		TableLayout tablelayout = (TableLayout) parameterContainer.getLayout();
		TableLayoutConstraints constraint = tablelayout.getConstraints(parameterPanel);
		int row = constraint.row1;
		if (row - 1 < 0)
			row = 0;
		else
			row = row - 1;

		parameterContainer.remove(parameterPanel);
		removeOneRow(row);
		parameterContainer.updateUI();
	}

	/**
	 * Add a new Parameter to the {@link ValueListProviderEditor}.
	 * @param wysiwygParameter the {@link WYSIYWYGParameter} which will be added (and wrapped in a {@link ParameterPanel})
	 */
	public void addParameterPanelIntoPanel(WYSIYWYGParameter wysiwygParameter, boolean showButtons, boolean enableName) {
		WYSIYWYGParameter newParameter = wysiwygParameter;
		if (newParameter == null) {
			newParameter = new WYSIYWYGParameter();
			wysiwygStaticValuelistProvider.addWYSIYWYGParameter(newParameter);
		}
		ParameterPanel newPanel = new ParameterPanel(newParameter, showButtons, enableName);

		expandLayout();
		parameterContainer.add(newPanel, "0,0");
		parameterContainer.updateUI();
	}

	/**
	 * Small Helpermethod for expanding the Layout to be capable to add another {@link ParameterPanel}<br>
	 * Called by {@link #addParameterPanelIntoPanel(WYSIYWYGParameter)}
	 */
	private void expandLayout() {
		TableLayout tablelayout = (TableLayout) parameterContainer.getLayout();
		tablelayout.insertRow(0, InterfaceGuidelines.MARGIN_BETWEEN);
		tablelayout.insertRow(0, TableLayout.PREFERRED);
	}

	/**
	 * Removes a row from the Layout<br>
	 * Called by {@link #removeParameterFromPanel(ParameterPanel)}
	 * @param row
	 */
	private void removeOneRow(int row) {
		TableLayout tablelayout = (TableLayout) parameterContainer.getLayout();
		tablelayout.deleteRow(row);
		tablelayout.deleteRow(row);
	}

	/**
	 * This Class wraps a {@link WYSIYWYGParameter}.
	 *
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 *
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	class ParameterPanel extends JPanel implements AddRemoveButtonControllable {

		WYSIYWYGParameter wysiwygParameter = null;
		private JTextField txtName = null;
		private JTextField txtValue = null;

		/**
		 * @param wysiwygParameter the {@link WYSIYWYGParameter} to be represented by {@link ParameterPanel}
		 */
		public ParameterPanel(WYSIYWYGParameter wysiwygParameter, boolean showButtons, boolean enableName) {
			this.wysiwygParameter = wysiwygParameter;
			double[][] layout = {{InterfaceGuidelines.MARGIN_LEFT, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}, {InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}};

			this.setLayout(new TableLayout(layout));
			JLabel lblName = new JLabel("Parameter Name:");
			JLabel lblValue = new JLabel("Parameter Value:");

			txtName = new JTextField(15);
			txtName.setEnabled(enableName);
			txtName.setText(wysiwygParameter.getParameterName());
			txtName.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
					changeValueForName();
				}
			});

			txtValue = new JTextField(15);
			txtValue.setText(wysiwygParameter.getParameterValue());
			txtValue.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
					changeValueForValue();
				}
			});

			this.add(lblName, "1,1");
			TableLayoutConstraints constraint = new TableLayoutConstraints(3, 1, 3, 1, TableLayout.FULL, TableLayout.CENTER);
			this.add(txtName, constraint);
			this.add(lblValue, "5,1");
			constraint = new TableLayoutConstraints(7, 1, 7, 1, TableLayout.FULL, TableLayout.CENTER);
			this.add(txtValue, constraint);

			if (showButtons){
				this.add(new AddRemoveRowsFromPanel(this.getBackground(), this), "9,1");
			}
		}

		/**
		 * This method changes the Parametervalue from the Textfield
		 */
		protected void changeValueForValue() {
			this.wysiwygParameter.setParameterValue(this.txtValue.getText());
			log.debug(this.wysiwygParameter.getParameterValue());
		}

		/**
		 * This method changes the Parametername from the Textfield
		 */
		protected void changeValueForName() {
			this.wysiwygParameter.setParameterName(this.txtName.getText());
			log.debug(this.wysiwygParameter.getParameterName());
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performAddAction()
		 */
		@Override
		public void performAddAction() {
			ValueListProviderEditor.this.addParameterPanelIntoPanel(null, true, true);
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performRemoveAction()
		 */
		@Override
		public void performRemoveAction() {
			ValueListProviderEditor.this.removeParameterFromPanel(this);
		}

		/**
		 * @return the {@link WYSIYWYGParameter} attached to this {@link ParameterPanel}
		 */
		public WYSIYWYGParameter getWYSIWYGParameter() {
			return this.wysiwygParameter;
		}
	}
}
