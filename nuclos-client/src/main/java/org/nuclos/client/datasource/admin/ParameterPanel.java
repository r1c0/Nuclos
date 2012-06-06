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
package org.nuclos.client.datasource.admin;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.valuelistprovider.GenericObjectCollectableFieldsProviderFactory;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProviderFactory;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.labeled.LabeledComponentSupport;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;

/**
 * Panel for administrating data source parameters.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ParameterPanel extends JPanel {

	private static final Logger LOG = Logger.getLogger(ParameterPanel.class);
	
	private static final int iComponentWidth = 200;

	private final Map<String, JComponent> mpFields = CollectionUtils.newHashMap();
	
	private final ChangeListener changeListener;

	/**
	 *
	 * @param lstParams
	 */
	public ParameterPanel(List<DatasourceParameterVO> lstParams) {
		this(lstParams, null);
	}
	/**
	 *
	 * @param lstParams
	 * @param chgListener
	 */
	public ParameterPanel(List<DatasourceParameterVO> lstParams, ChangeListener chgListener) {
		super(new GridBagLayout());

		this.changeListener = chgListener;
		
		//NUCLEUSINT-182/NUCLEUSiNT-577
		Collections.sort(lstParams, new Comparator<DatasourceParameterVO>() {
			@Override
			public int compare(DatasourceParameterVO o1, DatasourceParameterVO o2) {
				if (o1 != null && o2 != null)
					return StringUtils.emptyIfNull(o1.getParameter()).compareTo(StringUtils.emptyIfNull(o2.getParameter()));
				return 0;
			}
		});

		int iRow = 0;
		for (DatasourceParameterVO paramvo : lstParams) {

			final JLabel lab = new JLabel(paramvo.getDescription() == null || paramvo.getDescription().equals("") ? paramvo.getParameter() : paramvo.getDescription());
			add(lab, new GridBagConstraints(0, iRow, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(4, 4, 4, 4), 0, 0));

			final JComponent comp;
			if (paramvo.getValueListProvider() != null) {
				String type = paramvo.getValueListProvider().getType();

				CollectableFieldsProvider vlp;
				try {
					vlp = GenericObjectCollectableFieldsProviderFactory.newFactory(null).newCustomCollectableFieldsProvider(type, null, null);
				}
				catch (NuclosFatalException ex) {
					vlp = MasterDataCollectableFieldsProviderFactory.newFactory(null).newCustomCollectableFieldsProvider(type, null, null);
				}

				for (Map.Entry<String, String> param : paramvo.getValueListProvider().getParameters().entrySet()) {
					vlp.setParameter(param.getKey(), param.getValue());
				}

				List<CollectableField> lst;
				try {
					lst = vlp.getCollectableFields();
				}
				catch(CommonBusinessException e) {
					Errors.getInstance().showExceptionDialog(this, e);
					lst = new ArrayList<CollectableField>();
				}

				Collections.sort(lst, new Comparator<CollectableField>() {
					@Override
					public int compare(CollectableField clctf1, CollectableField clctf2) {
						return LangUtils.compareComparables(clctf1, clctf2);
					}
				});

				final DefaultComboBoxModel model = new DefaultComboBoxModel(new Vector<CollectableField>(lst));
				final JComboBox cmbbx = new JComboBox(model);
				if (lst.size() > 0) {
					cmbbx.setSelectedIndex(0);
				}
				cmbbx.setName(paramvo.getParameter());
				comp = cmbbx;
			}
			else {
				if (paramvo.getDatatype().equals("java.lang.String")) {
					final JTextField tf = new JTextField();
					tf.setName(paramvo.getParameter());
					comp = tf;
				}
				else if (paramvo.getDatatype().equals("java.lang.Boolean")) {
					final Boolean[] abValues = {Boolean.TRUE, Boolean.FALSE};
					final JComboBox cmbbx = new JComboBox(abValues);
					cmbbx.setSelectedIndex(0);
					cmbbx.setName(paramvo.getParameter());
					comp = cmbbx;
				}
				else if (paramvo.getDatatype().equals("java.lang.Double")) {
					final JTextField tf = new JFormattedTextField(new NumberFormatter(NumberFormat.getNumberInstance()));
					tf.setName(paramvo.getParameter());
					comp = tf;
				}
				else if (paramvo.getDatatype().equals("java.lang.Integer")) {
					//final JTextField tf = new JFormattedTextField(new NumberFormatter(NumberFormat.getIntegerInstance()));
					final JTextField tf = new JTextField();
					tf.setName(paramvo.getParameter());
					comp = tf;
				}
				else if (paramvo.getDatatype().equals("java.util.Date")) {
					final LabeledComponentSupport support = new LabeledComponentSupport();
					final DateChooser datechooser = new DateChooser(support);
					datechooser.setName(paramvo.getParameter());
					comp = datechooser;
				}
				else {
					comp = null;
				}
			}
			
			if (comp != null) {
				comp.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						if (changeListener != null)
							changeListener.stateChanged(new ChangeEvent(ParameterPanel.this));
					}
				});
				comp.addKeyListener(new KeyAdapter() {
					@Override
					public void keyTyped(KeyEvent e) {
						if (e.getKeyChar() == '\n' && changeListener != null)
							changeListener.stateChanged(new ChangeEvent(ParameterPanel.this));
					}
				});
				if (comp instanceof JComboBox) {
					((JComboBox)comp).addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							if (changeListener != null)
								changeListener.stateChanged(new ChangeEvent(ParameterPanel.this));
						}
					});
				}
				comp.setPreferredSize(new Dimension(iComponentWidth, comp.getPreferredSize().height));
			}
			add(comp, new GridBagConstraints(1, iRow, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
					new Insets(4, 4, 4, 4), 0, 0));
			if (iRow == 0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							comp.requestFocusInWindow();
						}
						catch (Exception e) {
							LOG.error("ParameterPanel failed: " + e, e);
						}
					}
				});
			}
			mpFields.put(paramvo.getParameter(), comp);
			iRow++;
		}
	}

	/**
	 *
	 * @param sParameter
	 * @return Jcomponent fitting for input of parameter
	 */
	public JComponent getComponentForParameter(String sParameter) {
		for (JComponent comp : mpFields.values()) {
			if (comp.getName().equals(sParameter)) {
				return comp;
			}
		}
		return null;
	}

	public void fillParameterMap(Collection<DatasourceParameterVO> lstParams, Map<String, Object> mpParams) throws CommonValidationException {
		for (DatasourceParameterVO paramvo : lstParams) {
			final JComponent comp = this.getComponentForParameter(paramvo.getParameter());
			try {
				if (comp != null) {
					if (paramvo.getValueListProvider() != null) {
						CollectableField selectedCollectableField = (CollectableField)((JComboBox) comp).getSelectedItem();
						mpParams.put(paramvo.getParameter(), selectedCollectableField.getValue());
						if (selectedCollectableField.getFieldType() == CollectableField.TYPE_VALUEIDFIELD) {
							mpParams.put(paramvo.getParameter() + "Id", selectedCollectableField.getValueId());
						}
					}
					else {
						if (paramvo.getDatatype().equals("java.lang.String")) {
							String text = ((JTextField) comp).getText();
							if (text != null) {
								text = text.trim();
							}
							mpParams.put(paramvo.getParameter(), "".equals(text) ? null : text);
						}
						else if (paramvo.getDatatype().equals("java.lang.Boolean")) {
							// Physically there are no boolean parameters in the database
							// So convert them to numbers instead
							mpParams.put(paramvo.getParameter(), ((JComboBox) comp).getSelectedItem().equals(Boolean.TRUE) ? 1 : 0);
						}
						else if (paramvo.getDatatype().equals("java.lang.Double")) {
							String text = ((JTextField) comp).getText();
							if (text != null) {
								text = text.trim();
							}
							mpParams.put(paramvo.getParameter(), "".equals(text) ? null : new Double(((JTextField) comp).getText()));
						}
						else if (paramvo.getDatatype().equals("java.lang.Integer")) {
							String text = ((JTextField) comp).getText();
							if (text != null) {
								text = text.trim();
							}
							mpParams.put(paramvo.getParameter(), "".equals(text) ? null : new Integer(((JTextField) comp).getText()));
						}
						else if (paramvo.getDatatype().equals("java.util.Date")) {
							// todo add validation of date format
							mpParams.put(paramvo.getParameter(), ((DateChooser) comp).getDate());
						}
					}
				}
			}
			catch (NumberFormatException ex) {
				throw new CommonValidationException(SpringLocaleDelegate.getInstance().getMessage(
						"ParameterPanel.1", "''{0}'' ist keine g\u00fcltige Zahl.", ((JTextField) comp).getText()), ex);
			}
		}
	}
}	// class ParameterPanel
