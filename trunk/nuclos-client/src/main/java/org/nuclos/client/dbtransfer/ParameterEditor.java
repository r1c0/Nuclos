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
package org.nuclos.client.dbtransfer;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nuclos.client.ui.Icons;
import org.nuclos.common2.StringUtils;

public class ParameterEditor {
	
	public final static Icon COMPARE_ICON_NEW = Icons.getInstance().getIconNew16();
	public final static Icon COMPARE_ICON_DELETED = Icons.getInstance().getIconRealDelete16();
	public final static Icon COMPARE_ICON_VALUE_CHANGED = Icons.getInstance().getIconEdit16();
	public final static Icon COMPARE_ICON_VALUE_NOT_CHANGED = Icons.getInstance().getIconEmpty16();
	
	public final static String COMPARE_DESCRIPTION_NEW = getMessage("dbtransfer.import.parameterpanel.9", "Konfigurationsdatei enth\u00e4lt diesen Parameter zus\u00e4tzlich.");
	public final static String COMPARE_DESCRIPTION_DELETED = getMessage("dbtransfer.import.parameterpanel.10", "In Konfigurationsdatei nicht mehr enthalten.");
	public final static String COMPARE_DESCRIPTION_VALUE_CHANGED = getMessage("dbtransfer.import.parameterpanel.11", "Konfigurationsdatei enth\u00e4tlt einen anderen Wert.");
	public final static String COMPARE_DESCRIPTION_VALUE_NOT_CHANGED = getMessage("dbtransfer.import.parameterpanel.12", "Unver\u00e4ndert.");
	
	private final ParameterComparison parameter;
	
	private final JLabel lbParam;
	private final JRadioButton rbCurrent = new JRadioButton();
	private final JRadioButton rbIncoming = new JRadioButton();
	private final JRadioButton rbOther = new JRadioButton();
	private final JTextField tfParameter = new JTextField();
	
	private final Color cDeactivatedBack = new Color(230,230,230);
	private final Color cDeleteBack = new Color(255,190,190);
	
	private final Collection<ChangeListener> changeListener = new ArrayList<ChangeListener>();
	
	public ParameterEditor(ParameterComparison parameterComparison) {
		parameter = parameterComparison;
		final Icon iconCompare;
		final String sCompare;
		
		if (parameter.isNew()) {
			sCompare = COMPARE_DESCRIPTION_NEW;
			iconCompare = COMPARE_ICON_NEW;
			tfParameter.setBackground(cDeactivatedBack);
			rbIncoming.setToolTipText(getMessage("dbtransfer.import.parameterpanel.13", "Parameter wird angelegt."));
			rbOther.setToolTipText(getMessage("dbtransfer.import.parameterpanel.14", "Parameter anlegen und neuen Wert setzen."));
			rbIncoming.setSelected(true);
			rbCurrent.setVisible(false);
			
		} else if (parameter.isDeleted()){
			sCompare = COMPARE_DESCRIPTION_DELETED;
			iconCompare = COMPARE_ICON_DELETED;
			tfParameter.setBackground(cDeleteBack);
			rbIncoming.setToolTipText(getMessage("dbtransfer.import.parameterpanel.15", "Parameter wird entfernt!"));
			rbCurrent.setToolTipText(getMessage("dbtransfer.import.parameterpanel.16", "Parameter behalten."));
			rbOther.setToolTipText(getMessage("dbtransfer.import.parameterpanel.17", "Parameter behalten und neuen Wert setzen."));
			rbIncoming.setSelected(true);
			
		} else if (parameter.isValueChanged()) {
			sCompare = COMPARE_DESCRIPTION_VALUE_CHANGED;
			iconCompare = COMPARE_ICON_VALUE_CHANGED;
			tfParameter.setBackground(cDeactivatedBack);
			rbIncoming.setSelected(true);
			
		} else {
			sCompare = COMPARE_DESCRIPTION_VALUE_NOT_CHANGED;
			iconCompare = COMPARE_ICON_VALUE_NOT_CHANGED;
			tfParameter.setBackground(cDeactivatedBack);
			rbCurrent.setSelected(true);
			rbIncoming.setVisible(false);
			
		}
		
		lbParam = new JLabel(this.parameter.getField("name", String.class));
		lbParam.setToolTipText("<html><b>" + getMessage("dbtransfer.import.parameterpanel.6", "Vergleich von Aktueller- und Importkonfiguration") +": " + sCompare + "</b><br>" +
			    getMessage("dbtransfer.import.parameterpanel.8", "Parameter Beschreibung") + ": " + StringUtils.emptyIfNull(parameter.getField("description", String.class)) + 
				"</html>");
		lbParam.setIcon(iconCompare);
		tfParameter.setText(this.parameter.getField("value", String.class));
		tfParameter.setEditable(false);
		
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(rbCurrent);
		btnGroup.add(rbIncoming);
		btnGroup.add(rbOther);
		rbCurrent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentValue();
			}
		});
		rbIncoming.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setIncomingValue();
			}
		});
		rbOther.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setOtherValue(tfParameter.getText());
			}
		});
	}
	
	public ParameterComparison getParameter() {
		return parameter;
	}
	
	public String getName() {
		return parameter.getField("name", String.class);
	}
	
	public void reset(boolean toCurrent) {
		if (toCurrent) {
			setCurrentValue();
		} else {
			setIncomingValue();
		}
	}
	
	public void addChangeListener(ChangeListener listener) {
		changeListener.add(listener);
	}
	
	private void notifyChangeListener() {
		for (ChangeListener listener : changeListener) {
			listener.stateChanged(new ChangeEvent(this));
		}
	}
	
	public void addToStepContent(JPanel panel, int iRow) {
		panel.add(lbParam, "1,"+iRow);
		//panel.add(new JSeparator(JSeparator.VERTICAL), "2,"+iRow);
		panel.add(rbCurrent, "3,"+iRow+",l,c");
		panel.add(rbIncoming, "4,"+iRow+",l,c");
		panel.add(rbOther, "5,"+iRow+",l,c");
		panel.add(tfParameter, "6,"+iRow);
	}
	
	public Dimension getLabelPrefferedSize() {
		return lbParam.getPreferredSize();
	}
	
	public void setCurrentValue() {
		// wenn der status "neu" ist gibt es keine m\u00f6glichkeiten den aktuellen wert zu w\u00e4hlen.
		// deshalb wechsel auf importierter wert
		if (parameter.isNew()) {
			setIncomingValue();
		} else {
			rbCurrent.setSelected(true);
			tfParameter.setText(parameter.getCurrentValue());
			tfParameter.setCaretPosition(0);
			tfParameter.setEditable(false);
			tfParameter.setBackground(cDeactivatedBack);
		}
		notifyChangeListener();
	}
	
	public void setIncomingValue() {
		// wenn der status "unver\u00e4ndert" ist gibt es keine m\u00f6glichkeit den importierten wert zu w\u00e4hlen.
		// deshalb wechsel auf aktuell
		if (!parameter.isValueChanged()) {
			setCurrentValue();
		} else {
			rbIncoming.setSelected(true);
			tfParameter.setText(parameter.getField("value", String.class));
			tfParameter.setCaretPosition(0);
			tfParameter.setEditable(false);
			if (parameter.isDeleted())
				tfParameter.setBackground(cDeleteBack);
			else
				tfParameter.setBackground(cDeactivatedBack);
		}
		notifyChangeListener();
	}
	
	public void setOtherValue(String sValue) {
		rbOther.setSelected(true);
		tfParameter.setText(sValue);
		tfParameter.setCaretPosition(0);
		tfParameter.setEditable(true);
		tfParameter.setBackground(Color.WHITE);
		notifyChangeListener();
	}
	
	public boolean isCurrentValue() {
		return rbCurrent.isSelected();
	}
	
	public boolean isIncomingValue() {
		return rbIncoming.isSelected();
	}
	
	public boolean isOtherValue() {
		return rbOther.isSelected();
	}
	
	public String getValue() {
		return tfParameter.getText();
	}
}
