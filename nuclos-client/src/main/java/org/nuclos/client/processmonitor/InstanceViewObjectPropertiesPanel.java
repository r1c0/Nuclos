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
package org.nuclos.client.processmonitor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.nuclos.client.ui.RuntimeTextField;

/**
 * Panel containing the properties of a subprocess object.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class InstanceViewObjectPropertiesPanel extends JPanel {
	
	private final JTextField tfIdentifier = new JTextField();
	
	private final JTextField tfPlanStart = new JTextField();
	private final JTextField tfPlanEnd = new JTextField();
	private final RuntimeTextField tfPlanRuntime = new RuntimeTextField();
	
	private final JTextField tfRealStart = new JTextField();
	private final JTextField tfRealEnd = new JTextField();
	private final RuntimeTextField tfRealRuntime = new RuntimeTextField();

	public InstanceViewObjectPropertiesPanel() {
		super(new BorderLayout());
		final JTabbedPane tabpn = new JTabbedPane();
		this.add(tabpn, BorderLayout.CENTER);
		tabpn.addTab("Objekt", newInstanceViewObjectPropertiesPanel());
	}

	/**
	 * @return a new panel containing the basic properties for a subprocessobject.
	 */
	private JPanel newInstanceViewObjectPropertiesPanel() {
		final JPanel pnlProperties = new JPanel(new GridBagLayout());
		
		tfIdentifier.setEditable(false);		
		
		tfPlanStart.setEditable(false);
		tfPlanEnd.setEditable(false);
		tfPlanRuntime.setEditable(false);
		
		tfRealStart.setEditable(false);
		tfRealEnd.setEditable(false);
		tfRealRuntime.setEditable(false);
		
		pnlProperties.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		// add the components to the panel
		// y++ for a new row
		int y = 1;
		
		pnlProperties.add(new JLabel("Objekt Identifizierer"),
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlProperties.add(tfIdentifier,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		
		pnlProperties.add(new JLabel("Plan Start"),
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlProperties.add(tfPlanStart,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		
		pnlProperties.add(new JLabel("Plan Ende"),
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlProperties.add(tfPlanEnd,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		
		pnlProperties.add(new JLabel("Plan Durchlaufzeit"),
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlProperties.add(tfPlanRuntime,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		
		pnlProperties.add(new JLabel("Ist Start"),
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlProperties.add(tfRealStart,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		
		pnlProperties.add(new JLabel("Ist Ende"),
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlProperties.add(tfRealEnd,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		
		pnlProperties.add(new JLabel("Ist Durchlaufzeit"),
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlProperties.add(tfRealRuntime,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		
		
		
		// auffueller
		pnlProperties.add(new JLabel(""),
				new GridBagConstraints(1, y++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						new Insets(2, 5, 0, 0), 0, 0));
		
		return pnlProperties;
	}
	
	public void setIdentifier(String sIdentifier){
		this.tfIdentifier.setText(sIdentifier);
	}
	
	public void setPlanStart(String sPlanStart){
		this.tfPlanStart.setText(sPlanStart);
	}
	
	public void setPlanEnd(String sPlanEnd){
		this.tfPlanEnd.setText(sPlanEnd);
	}
	
	public void setPlanRuntime(double dPlanRuntime){
		this.tfPlanRuntime.setMillis(dPlanRuntime);
	}
	
	public void setRealStart(String sRealStart){
		this.tfRealStart.setText(sRealStart);
	}
	
	public void setRealEnd(String sRealEnd){
		this.tfRealEnd.setText(sRealEnd);
	}
	
	public void setRealRuntime(double dRealRuntime){
		this.tfRealRuntime.setMillis(dRealRuntime);
	}
	
	public void clear(){
		this.tfIdentifier.setText("");

		this.tfPlanStart.setText("");
		this.tfPlanEnd.setText("");
		this.tfPlanRuntime.setText("");
		
		this.tfRealStart.setText("");
		this.tfRealEnd.setText("");
		this.tfRealRuntime.setText("");
	}
}
