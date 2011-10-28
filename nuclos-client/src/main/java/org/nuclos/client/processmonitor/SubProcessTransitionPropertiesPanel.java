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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.nuclos.client.ui.Errors;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common.NuclosBusinessException;

/**
 * Panel containing the properties of a transition between subprocess's.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:marc.finke@novabit.de">Marc Finke</a>
 * @version 01.00.00
 */

public class SubProcessTransitionPropertiesPanel extends JPanel {

	private final SubProcessTransitionPanelModel model = new SubProcessTransitionPanelModel();

	public SubProcessTransitionPropertiesPanel() {
		super(new BorderLayout());
		final JTabbedPane tabpn = new JTabbedPane();
		this.add(tabpn, BorderLayout.CENTER);
		tabpn.addTab("Eigenschaften Teil-Prozess \u00dcbergang", newTransitionBasicPropertiesPanel());
	}

	/**
	 * @return a new panel containing the basic properties for a transtion.
	 */
	private JPanel newTransitionBasicPropertiesPanel() {
		final JPanel pnlStateProperties = new JPanel(new GridBagLayout());
		pnlStateProperties.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final JLabel labName = new JLabel("Name");
		final JTextField tfName = new JTextField();
		labName.setAlignmentY((float) 0.0);
		labName.setHorizontalAlignment(SwingConstants.LEADING);
		labName.setHorizontalTextPosition(SwingConstants.TRAILING);
		labName.setLabelFor(tfName);
		labName.setVerticalAlignment(SwingConstants.CENTER);
		labName.setVerticalTextPosition(SwingConstants.CENTER);

		tfName.setAlignmentX((float) 0.0);
		tfName.setAlignmentY((float) 0.0);
		tfName.setPreferredSize(new Dimension(100, 21));
		//tfName.setDocument(model.docName);
		
		final JLabel labInputObject = new JLabel("Start wenn Status erreicht");
		final JComboBox cmbInputObject = new JComboBox();
		labInputObject.setAlignmentY((float) 0.0);
		labInputObject.setHorizontalAlignment(SwingConstants.LEADING);
		labInputObject.setHorizontalTextPosition(SwingConstants.TRAILING);
		labInputObject.setLabelFor(cmbInputObject);
		labInputObject.setVerticalAlignment(SwingConstants.CENTER);
		labInputObject.setVerticalTextPosition(SwingConstants.CENTER);

		cmbInputObject.setAlignmentX((float) 0.0);
		cmbInputObject.setAlignmentY((float) 0.0);
		cmbInputObject.setPreferredSize(new Dimension(100, 21));
		cmbInputObject.setModel(model.modelStates);
		cmbInputObject.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// only when not loading states!
				if (!model.isLoadingStates()){
					Object obj = cmbInputObject.getSelectedItem();
					if(obj instanceof TransitionSubProcess) {
						stateHasChanged((TransitionSubProcess)obj);
					}
					else {
						clear();
					}
				}
			}
			
		});

		final JLabel labOutputObject = new JLabel("Objektgenerierer f\u00fcr n\u00e4chsten Teilprozess");
		final JButton jbtOutputObject = new JButton("\u00f6ffnen...");
		labOutputObject.setAlignmentY((float) 0.0);
		labOutputObject.setHorizontalAlignment(SwingConstants.LEADING);
		labOutputObject.setHorizontalTextPosition(SwingConstants.TRAILING);
		labOutputObject.setLabelFor(jbtOutputObject);
		labOutputObject.setVerticalAlignment(SwingConstants.CENTER);
		labOutputObject.setVerticalTextPosition(SwingConstants.CENTER);

		jbtOutputObject.setAlignmentX((float) 0.0);
		jbtOutputObject.setAlignmentY((float) 0.0);
		jbtOutputObject.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int iResult = model.openObjectGeneration();
					switch (iResult){
					case SubProcessTransitionPanelModel.GENERATIONID_NOT_SET:
						JOptionPane.showMessageDialog(SubProcessTransitionPropertiesPanel.this, "Ein Objektgenerierer ist noch nicht vorhanden, bitte speichern Sie das Prozessmodell.");
						break;
					case SubProcessTransitionPanelModel.GENERATIONSTATEID_NOT_SET:
						JOptionPane.showMessageDialog(SubProcessTransitionPropertiesPanel.this, "Ein Objektgenerierer ist noch nicht vorhanden, bitte w\u00e4hlen Sie einen Status am dem der Generierer geh\u00e4ngt werden soll.");
						break;
					case SubProcessTransitionPanelModel.SUBPROCESSTRANSITIONID_NOT_SET:
						JOptionPane.showMessageDialog(SubProcessTransitionPropertiesPanel.this, "Ein Objektgenerierer ist noch nicht vorhanden, bitte speichern Sie das Prozessmodell.");
						break;
					}
				} catch (NuclosBusinessException e1) {
					Errors.getInstance().showExceptionDialog(SubProcessTransitionPropertiesPanel.this, e1);
				} catch (CommonPermissionException e1) {
					JOptionPane.showMessageDialog(SubProcessTransitionPropertiesPanel.this, "Sie verf\u00fcgen leider nicht \u00fcber die n\u00f6tigen Rechte!");
				} catch (CommonFatalException e1) {
					Errors.getInstance().showExceptionDialog(SubProcessTransitionPropertiesPanel.this, e1);
				} catch (CommonBusinessException e1) {
					Errors.getInstance().showExceptionDialog(SubProcessTransitionPropertiesPanel.this, e1);
				}
			}
		});
		
		final JLabel labDescription = new JLabel("Beschreibung");
		final JTextArea taDescription = new JTextArea();
		labDescription.setAlignmentY((float) 0.0);
		labDescription.setHorizontalAlignment(SwingConstants.LEADING);
		labDescription.setHorizontalTextPosition(SwingConstants.TRAILING);
		labDescription.setIconTextGap(4);
		labDescription.setLabelFor(taDescription);
		labDescription.setVerticalAlignment(SwingConstants.TOP);
		labDescription.setVerticalTextPosition(SwingConstants.TOP);

		taDescription.setAlignmentX((float) 0.0);
		taDescription.setAlignmentY((float) 0.0);
		taDescription.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		taDescription.setText("");
		//taDescription.setDocument(model.docDescription);
		taDescription.setFont(tfName.getFont());
		taDescription.setLineWrap(true);
		

		pnlStateProperties.setMaximumSize(new Dimension(2147483647, 2147483647));

		final JScrollPane scrlpn = new JScrollPane();
		scrlpn.getViewport().add(taDescription);
		scrlpn.setAutoscrolls(true);
		scrlpn.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.setAlignmentX((float) 0.0);
		this.setAlignmentY((float) 0.0);
		
		int y = 1;

		pnlStateProperties.add(labName,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(tfName,
				new GridBagConstraints(1, y++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		
		pnlStateProperties.add(labInputObject,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(cmbInputObject,
				new GridBagConstraints(1, y++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		
		pnlStateProperties.add(labOutputObject,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(jbtOutputObject,
				new GridBagConstraints(1, y++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(2, 5, 0, 0), 0, 0));
		
		// Bemerkung ganz unten
		pnlStateProperties.add(labDescription,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(scrlpn,
				new GridBagConstraints(1, y++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						new Insets(2, 5, 0, 0), 0, 0));
		return pnlStateProperties;
	}
	
	/*
	 * set the attributes from state to transition
	 * when a state was selected in the combobox
	 */
	protected void stateHasChanged(TransitionSubProcess tsp) {
		model.setGeneratorStateId(tsp.getStateModelVO().getId());
	}


	public SubProcessTransitionPanelModel getModel() {
		return model;
	}
	
	
	protected void clear() {
		model.clear();
	}

		

}	// class StatePropertiesPanel
