//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nuclos.client.ui.model.SimpleCollectionComboBoxModel;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;


/**
 * A specialization of SelectFixedColumnsPanel for also choosing 'columns'
 * from a pivot (key -> value) table.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class PivotPanel extends SelectFixedColumnsPanel {
	
	private static class Header extends JPanel {
		
		private final JCheckBox checkbox;
		
		private final List<JCheckBox> subformCbs = new ArrayList<JCheckBox>();
		
		private final List<JComboBox> keyCombos = new ArrayList<JComboBox>();
		
		private final List<JComboBox> valueCombos = new ArrayList<JComboBox>();
		
		private Header(Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields) {
			super(new GridBagLayout());
			// setPreferredSize(new Dimension(400, 200));
			final GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			
			checkbox = new JCheckBox("use some subforms as pivot tables in result list");
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 3;
			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(checkbox, c);
			setVisible(true);
			checkbox.setVisible(true);
			checkbox.setEnabled(true);
			checkbox.setSelected(false);
			
			// label
			JLabel label = new JLabel("pivot entity");
			c.gridy = 1;
			c.gridx = 0;
			c.weightx = 0.2;
			add(label, c);
			label = new JLabel("key column");
			c.gridx = 1;
			c.weightx = 0.2;
			add(label, c);
			label = new JLabel("value column");
			c.gridx = 2;
			c.weightx = 0.2;
			add(label, c);			
			
			//
			int gridy = 1;
			c.gridwidth = 1;
			c.ipadx = 3;
			c.ipady = 1;
			// c.weightx = 0.0;
			for (String subform: subFormFields.keySet()) {
				final Map<String, EntityFieldMetaDataVO> fields = subFormFields.get(subform);
				
				final JCheckBox cb = new JCheckBox(subform);
				subformCbs.add(cb);
				c.gridy = ++gridy;
				c.gridx = 0;
				add(cb, c);
				cb.setVisible(true);
				cb.setEnabled(true);
				cb.setSelected(false);
				
				JComboBox combo = mkCombo(fields);
				keyCombos.add(combo);
				c.gridx = 1;
				add(combo, c);
				combo.setVisible(true);
				combo.setEnabled(true);
				combo.setSelectedIndex(0);
				
				combo = mkCombo(fields);
				valueCombos.add(combo);
				c.gridx = 2;
				add(combo, c);
				combo.setVisible(true);
				combo.setEnabled(true);
				combo.setSelectedIndex(0);
			}
		}
		
		private static JComboBox mkCombo(Map<String, EntityFieldMetaDataVO> fields) {
			final ComboBoxModel model = new SimpleCollectionComboBoxModel<String>(new ArrayList<String>(fields.keySet()));
			final JComboBox result = new JComboBox(model);
			result.setVisible(true);
			result.setEnabled(true);
			return result;
		}
		
		public JCheckBox getCheckbox() {
			return checkbox;
		}
	}
	
	public PivotPanel(Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields) {
		super(new Header(subFormFields));		
	}
	
	public void addActionListener(ActionListener l) {
		if (getHeader() != null)
			getHeader().getCheckbox().addActionListener(l);
	}
	
	private Header getHeader() {
		return (Header) getHeaderComponent();
	}
		
}
