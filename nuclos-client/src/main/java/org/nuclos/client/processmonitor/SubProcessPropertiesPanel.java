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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.nuclos.client.ui.DateTimeSeries;
import org.nuclos.server.processmonitor.valueobject.ProcessStateRuntimeFormatVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessUsageCriteriaVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * Panel containing the properties of a subprocess.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:marc.finke@novabit.de">Marc Finke</a>
 * @version 01.00.00
 */

public class SubProcessPropertiesPanel extends JPanel {

	private final SubProcessPanelModel model = new SubProcessPanelModel();


	public SubProcessPropertiesPanel() {
		super(new BorderLayout());
		final JTabbedPane tabpn = new JTabbedPane();
		this.add(tabpn, BorderLayout.CENTER);
		tabpn.addTab("Eigenschaften Teil-Prozess", newStateBasicPropertiesPanel());
	}

	/**
	 * @return a new panel containing the basic properties for a subprocess.
	 */
	private JPanel newStateBasicPropertiesPanel() {
		final JPanel pnlStateProperties = new JPanel(new GridBagLayout());
		pnlStateProperties.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final JLabel labModule = new JLabel("Teil-Prozess");
		final JComboBox cmbModule = new JComboBox(model.modelSubProcessStateModel);
		labModule.setAlignmentY((float) 0.0);
		labModule.setHorizontalAlignment(SwingConstants.LEADING);
		labModule.setHorizontalTextPosition(SwingConstants.TRAILING);
		labModule.setLabelFor(cmbModule);
		labModule.setVerticalAlignment(SwingConstants.CENTER);
		labModule.setVerticalTextPosition(SwingConstants.CENTER);
		
		cmbModule.setAlignmentY((float) 0.0);
		cmbModule.setAlignmentX((float) 0.0);
		
		cmbModule.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				Object obj = cmbModule.getSelectedItem();
				if(obj instanceof SubProcess) {
					stateHasChanged((SubProcess)obj);
				}
				else {
					clear();
				}
			}
			
		});

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
//		tfName.setPreferredSize(new Dimension(100, 21));
		tfName.setDocument(model.docName);
		tfName.setEnabled(false);
		
		final JLabel labSubProcessUsageCriteria = new JLabel("Verwendung (Modul / Aktion)");
		final JComboBox cmbSubProcessUsageCriteria = new JComboBox(model.modelSubProcessUsageCriteria);
		labSubProcessUsageCriteria.setAlignmentY((float) 0.0);
		labSubProcessUsageCriteria.setHorizontalAlignment(SwingConstants.LEADING);
		labSubProcessUsageCriteria.setHorizontalTextPosition(SwingConstants.TRAILING);
		labSubProcessUsageCriteria.setLabelFor(cmbSubProcessUsageCriteria);
		labSubProcessUsageCriteria.setVerticalAlignment(SwingConstants.CENTER);
		labSubProcessUsageCriteria.setVerticalTextPosition(SwingConstants.CENTER);
		
		cmbSubProcessUsageCriteria.setAlignmentY((float) 0.0);
		cmbSubProcessUsageCriteria.setAlignmentX((float) 0.0);
		
		cmbSubProcessUsageCriteria.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				Object obj = cmbSubProcessUsageCriteria.getSelectedItem();
				if(obj instanceof SubProcessUsageCriteriaVO) {
					model.setSubProcessUsageCriteria(((SubProcessUsageCriteriaVO)obj).getId());
				}
			}
			
		});
		
		final JLabel labGuarantor = new JLabel("Teilprozess Name");
		final JTextField tfGuarantor = new JTextField();
		labGuarantor.setAlignmentY((float) 0.0);
		labGuarantor.setHorizontalAlignment(SwingConstants.LEADING);
		labGuarantor.setHorizontalTextPosition(SwingConstants.TRAILING);
		labGuarantor.setLabelFor(tfGuarantor);
		labGuarantor.setVerticalAlignment(SwingConstants.CENTER);
		labGuarantor.setVerticalTextPosition(SwingConstants.CENTER);

		tfGuarantor.setAlignmentX((float) 0.0);
		tfGuarantor.setAlignmentY((float) 0.0);
//		tfGuarantor.setPreferredSize(new Dimension(100, 21));
		tfGuarantor.setDocument(model.docGuarantor);
		tfGuarantor.setText("Verantwortlicher");
		
		
		final JLabel labSecondGuarantor = new JLabel("Teilprozess Beschreibung");
		final JTextField tfSecondGuarantor = new JTextField();
		labSecondGuarantor.setAlignmentY((float) 0.0);
		labSecondGuarantor.setHorizontalAlignment(SwingConstants.LEADING);
		labSecondGuarantor.setHorizontalTextPosition(SwingConstants.TRAILING);
		labSecondGuarantor.setLabelFor(tfSecondGuarantor);
		labSecondGuarantor.setVerticalAlignment(SwingConstants.CENTER);
		labSecondGuarantor.setVerticalTextPosition(SwingConstants.CENTER);

		tfSecondGuarantor.setAlignmentX((float) 0.0);
		tfSecondGuarantor.setAlignmentY((float) 0.0);
//		tfSecondGuarantor.setPreferredSize(new Dimension(100, 21));
		tfSecondGuarantor.setDocument(model.docSecondGuarantor);
		tfSecondGuarantor.setText("Stellvertreter");		
		
		final JLabel labSupervisor = new JLabel("-/-");
		final JTextField tfSupervisor = new JTextField();
		labSupervisor.setAlignmentY((float) 0.0);
		labSupervisor.setHorizontalAlignment(SwingConstants.LEADING);
		labSupervisor.setHorizontalTextPosition(SwingConstants.TRAILING);
		labSupervisor.setLabelFor(tfSupervisor);
		labSupervisor.setVerticalAlignment(SwingConstants.CENTER);
		labSupervisor.setVerticalTextPosition(SwingConstants.CENTER);

		tfSupervisor.setAlignmentX((float) 0.0);
		tfSupervisor.setAlignmentY((float) 0.0);
//		tfSupervisor.setPreferredSize(new Dimension(100, 21));
		tfSupervisor.setDocument(model.docSupervisor);
		tfSupervisor.setText("Vorgesetzter");
		
		final JLabel labPlanStartSeries = new JLabel("Plan Start");
		final DateTimeSeries seriesPlanStart = new DateTimeSeries(true);
		labPlanStartSeries.setAlignmentY((float) 0.0);
		labPlanStartSeries.setHorizontalAlignment(SwingConstants.LEADING);
		labPlanStartSeries.setHorizontalTextPosition(SwingConstants.TRAILING);
		labPlanStartSeries.setLabelFor(seriesPlanStart);
		labPlanStartSeries.setVerticalAlignment(SwingConstants.CENTER);
		labPlanStartSeries.setVerticalTextPosition(SwingConstants.CENTER);

		seriesPlanStart.setAlignmentX((float) 0.0);
		seriesPlanStart.setAlignmentY((float) 0.0);	
		seriesPlanStart.getJTextField().setDocument(model.docPlanStartSeries);
		
		final JLabel labPlanEndSeries = new JLabel("Plan Ende");
		final DateTimeSeries seriesPlanEnd = new DateTimeSeries(true);
		labPlanEndSeries.setAlignmentY((float) 0.0);
		labPlanEndSeries.setHorizontalAlignment(SwingConstants.LEADING);
		labPlanEndSeries.setHorizontalTextPosition(SwingConstants.TRAILING);
		labPlanEndSeries.setLabelFor(seriesPlanStart);
		labPlanEndSeries.setVerticalAlignment(SwingConstants.CENTER);
		labPlanEndSeries.setVerticalTextPosition(SwingConstants.CENTER);

		seriesPlanEnd.setAlignmentX((float) 0.0);
		seriesPlanEnd.setAlignmentY((float) 0.0);	
		seriesPlanEnd.getJTextField().setDocument(model.docPlanEndSeries);
		
		final JLabel labRuntime = new JLabel("Durchlaufzeit");
		final JTextField tfRuntime = new JTextField();
		labRuntime.setAlignmentY((float) 0.0);
		labRuntime.setHorizontalAlignment(SwingConstants.LEADING);
		labRuntime.setHorizontalTextPosition(SwingConstants.TRAILING);
		labRuntime.setLabelFor(tfRuntime);
		labRuntime.setVerticalAlignment(SwingConstants.CENTER);
		labRuntime.setVerticalTextPosition(SwingConstants.CENTER);
		
		tfRuntime.setAlignmentX((float) 0.0);
		tfRuntime.setAlignmentY((float) 0.0);
//		tfRuntime.setPreferredSize(new Dimension(100, 21));
		tfRuntime.setText("");
		tfRuntime.setDocument(model.docRuntime);
		
		final JComboBox cmbRuntimeFormat = new JComboBox(model.modelRuntimeFormat);
		cmbRuntimeFormat.setAlignmentY((float) 0.0);
		cmbRuntimeFormat.setAlignmentX((float) 0.0);
		
		cmbRuntimeFormat.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				Object obj = cmbRuntimeFormat.getSelectedItem();
				if(obj instanceof ProcessStateRuntimeFormatVO) {
					runtimeFormatHasChanged((ProcessStateRuntimeFormatVO)obj);
				}
				else {
					clear();
				}
			}
			
		});
				
		final JLabel labOriginalSystem = new JLabel("Original System");
		final JTextField tfOriginalSystem = new JTextField();
		labOriginalSystem.setAlignmentY((float) 0.0);
		labOriginalSystem.setHorizontalAlignment(SwingConstants.LEADING);
		labOriginalSystem.setHorizontalTextPosition(SwingConstants.TRAILING);
		labOriginalSystem.setLabelFor(tfOriginalSystem);
		labOriginalSystem.setVerticalAlignment(SwingConstants.CENTER);
		labOriginalSystem.setVerticalTextPosition(SwingConstants.CENTER);
		tfOriginalSystem.setAlignmentX((float) 0.0);
		tfOriginalSystem.setAlignmentY((float) 0.0);
//		tfOriginalSystem.setPreferredSize(new Dimension(100, 21));
		tfOriginalSystem.setText("");
		tfOriginalSystem.setDocument(model.docOriginalSystem);
		
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
		taDescription.setDocument(model.docDescription);
		taDescription.setFont(tfName.getFont());
		taDescription.setLineWrap(true);
		taDescription.setEnabled(false);

		pnlStateProperties.setMaximumSize(new Dimension(2147483647, 2147483647));

		final JScrollPane scrlpn = new JScrollPane();
		scrlpn.getViewport().add(taDescription);
		scrlpn.setAutoscrolls(true);
		scrlpn.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.setAlignmentX((float) 0.0);
		this.setAlignmentY((float) 0.0);
		
		// add the components to the panel
		// y++ for a new row
		int y = 1;

		pnlStateProperties.add(labModule,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(cmbModule,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));		
//		pnlStateProperties.add(labName,
//				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
//						new Insets(2, 0, 0, 5), 0, 0));
//		pnlStateProperties.add(tfName,
//				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
//						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labSubProcessUsageCriteria,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(cmbSubProcessUsageCriteria,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));	
		pnlStateProperties.add(labGuarantor,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(tfGuarantor,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labSecondGuarantor,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(tfSecondGuarantor,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labSupervisor,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(tfSupervisor,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labPlanStartSeries,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(seriesPlanStart,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labPlanEndSeries,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(seriesPlanEnd,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labRuntime,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(tfRuntime,
				new GridBagConstraints(1, y, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(cmbRuntimeFormat,
				new GridBagConstraints(2, y++, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labOriginalSystem,
				new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(tfOriginalSystem,
				new GridBagConstraints(1, y++, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
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
	 * @return the model for subprocess
	 */
	public SubProcessPanelModel getModel() {
		return model;
	}
	
	/*
	 * set the attributes from statemodel to subprocess
	 * when a statemodel was selected in the combobox
	 */
	protected void stateHasChanged(SubProcess subProcess) {
		StateModelVO voState = subProcess.getStateModelVO();
		model.setName(voState.getName());
		model.setDescription(voState.getDescription());
		model.setSubProcessStateModel(voState.getId());
		model.getSubProcessUsageCriteriaForComboBox();
	}
	
	protected void runtimeFormatHasChanged(ProcessStateRuntimeFormatVO formatVO){
		model.setRuntimeFormat(formatVO.getValue());
	}
	
	protected void clear() {
		model.clear();
	}
		

}	// class StatePropertiesPanel
