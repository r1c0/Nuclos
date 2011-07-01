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
//package org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmldependencies;
//
//import info.clearthought.layout.TableLayout;
//import info.clearthought.layout.TableLayoutConstraints;
//
//import java.awt.Component;
//import java.awt.Dimension;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//import javax.swing.JButton;
//import javax.swing.JComboBox;
//import javax.swing.JDialog;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTextField;
//
//import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
//import org.nuclos.client.layout.wysiwyg.component.LayoutMLDependencies;
//import org.nuclos.client.layout.wysiwyg.component.LayoutMLDependency;
//import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.AddRemoveRowsFromPanel;
//import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.AddRemoveRowsFromPanel.AddRemoveButtonControllable;
//import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
//import org.nuclos.server.attribute.valueobject.AttributeCVO;
//
//@SuppressWarnings("serial")
//public class LayoutMLDependendiesEditorPanel extends JDialog {
//
//	private int width = 600;
//	private int height = 300;
//
//	private LayoutMLDependencies layoutMLDependencies;
//	private JPanel dependenciesPanel = null;
//	private WYSIWYGMetaInformation metaInf = null;
//
//	public LayoutMLDependendiesEditorPanel(LayoutMLDependencies layoutMLDependencies) {
//		this.layoutMLDependencies = layoutMLDependencies;
//		this.metaInf = layoutMLDependencies.getWYSIWYGMetaInformation();
//		double[][] editorsPanelLayout = {{InterfaceGuidelines.MARGIN_LEFT, TableLayout.FILL, InterfaceGuidelines.MARGIN_RIGHT}, {InterfaceGuidelines.MARGIN_TOP, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BOTTOM}};
//		this.setLayout(new TableLayout(editorsPanelLayout));
//
//		dependenciesPanel = new JPanel();
//		double[][] dependenciesPanelLayout = {{InterfaceGuidelines.MARGIN_LEFT, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_RIGHT}, {}};
//		dependenciesPanel.setLayout(new TableLayout(dependenciesPanelLayout));
//		JScrollPane scrollbar = new JScrollPane(dependenciesPanel);
//		scrollbar.getVerticalScrollBar().setUnitIncrement(20);
//
//		populateDependenciesPanel();
//
//		TableLayoutConstraints constraint = new TableLayoutConstraints();
//		constraint.col1 = 1;
//		constraint.col2 = 1;
//		constraint.row1 = 1;
//		constraint.row2 = 1;
//		constraint.hAlign = TableLayout.FULL;
//		constraint.vAlign = TableLayout.FULL;
//
//		this.add(scrollbar, constraint);
//
//		JButton cancel = new JButton("cancel");
//		cancel.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				cancelAction();
//			}
//		});
//
//		constraint = new TableLayoutConstraints();
//		constraint.col1 = 1;
//		constraint.col2 = 1;
//		constraint.row1 = 3;
//		constraint.row2 = 3;
//		constraint.hAlign = TableLayout.RIGHT;
//		constraint.vAlign = TableLayout.CENTER;
//
//		this.add(cancel, constraint);
//
//		JButton save = new JButton("save");
//		save.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				saveDependencies();
//			}
//		});
//
//		constraint = new TableLayoutConstraints();
//		constraint.col1 = 1;
//		constraint.col2 = 1;
//		constraint.row1 = 3;
//		constraint.row2 = 3;
//		constraint.hAlign = TableLayout.LEFT;
//		constraint.vAlign = TableLayout.CENTER;
//
//		this.add(save, constraint);
//
//		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
//		int x = (screenSize.width - width) / 2;
//		int y = (screenSize.height - height) / 2;
//		this.setBounds(x, y, width, height);
//
//		setVisible(true);
//	}
//
//	private final void cancelAction() {
//		this.dispose();
//	}
//
//	private final void saveDependencies() {
//		Component[] dependencies = dependenciesPanel.getComponents();
//		LayoutMLDependency layoutMLDependency = null;
//		for (int i = 0; i < dependencies.length; i++) {
//			if (dependencies[i] instanceof DependencyPanel) {
//				layoutMLDependency = ((DependencyPanel) dependencies[i]).getLayoutMLDependency();
//				layoutMLDependency.setDependendField(((DependencyPanel) dependencies[i]).getDependentFieldCombobox().getSelectedItem().toString());
//				layoutMLDependency.setDependsOnField(((DependencyPanel) dependencies[i]).getDependsOnFieldCombobox().getSelectedItem().toString());
//				if (!this.layoutMLDependencies.doesContainDependency(layoutMLDependency))
//					this.layoutMLDependencies.addDependency(layoutMLDependency);
//			}
//		}
//
//		this.dispose();
//	}
//
//	private void populateDependenciesPanel() {
//		if (layoutMLDependencies.getAllDependencies().size() > 0) {
//			for (LayoutMLDependency layoutMLDependency : layoutMLDependencies.getAllDependencies()) {
//				addAnotherDependency(layoutMLDependency);
//			}
//		} else {
//			addAnotherDependency(new LayoutMLDependency());
//		}
//	}
//
//	private void addAnotherDependency(LayoutMLDependency layoutMLDependency) {
//		TableLayout layout = (TableLayout) dependenciesPanel.getLayout();
//
//		DependencyPanel dependencyPanel = new DependencyPanel(layoutMLDependency);
//
//		layout.insertRow(0, TableLayout.PREFERRED);
//		dependenciesPanel.add(dependencyPanel, "1,0");
//		layout.insertRow(0, InterfaceGuidelines.MARGIN_BETWEEN);
//
//		Component[] dependencies = dependenciesPanel.getComponents();
//		doButtonControl(dependencies);
//	}
//
//	private void doButtonControl(Component[] dependencies) {
//		for (int i = 0; i < dependencies.length; i++) {
//			if (dependencies[i] instanceof DependencyPanel) {
//				DependencyPanel panel = ((DependencyPanel) dependencies[i]);
//
//				if (dependencies.length == 1) {
//					panel.getAddRemoveRowsFromPanel().enableAddButton();
//					panel.getAddRemoveRowsFromPanel().disableDeleteButton();
//				} else {
//					panel.getAddRemoveRowsFromPanel().disableAddButton();
//					panel.getAddRemoveRowsFromPanel().enableDeleteButton();
//					if (i == 0)
//						panel.getAddRemoveRowsFromPanel().enableAddButton();
//				}
//			}
//		}
//	}
//
//	private void removeDependencyPanel(DependencyPanel dependencyPanel) {
//		TableLayout layout = (TableLayout) dependenciesPanel.getLayout();
//		TableLayoutConstraints constraint = layout.getConstraints(dependencyPanel);
//
//		layoutMLDependencies.removeDependencyFromDependencies(dependencyPanel.getLayoutMLDependency());
//		dependenciesPanel.remove(dependencyPanel);
//		int row = constraint.row1;
//
//		if (row - 1 < 0)
//			row = 0;
//		else
//			row = row - 1;
//
//		layout.deleteRow(row);
//		layout.deleteRow(row);
//
//		Component[] dependencies = dependenciesPanel.getComponents();
//		doButtonControl(dependencies);
//
//		dependenciesPanel.updateUI();
//	}
//
//	public class DependencyPanel extends JPanel implements AddRemoveButtonControllable {
//		private JLabel dependentField = null;
//		private JLabel dependsOnField = null;
//		private JComboBox cbxdependentField = null;
//		private JComboBox cbxdependsOnField = null;
//		private AddRemoveRowsFromPanel addRemoveRowsFromPanel = null;
//		private LayoutMLDependency layoutMLDependency;
//
//		public DependencyPanel(LayoutMLDependency layoutMLDependency) {
//			this.layoutMLDependency = layoutMLDependency;
//			double[][] dependencyPanelLayout = {{TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}, {TableLayout.PREFERRED}};
//			this.setLayout(new TableLayout(dependencyPanelLayout));
//
//			dependentField = new JLabel("Dependent Field:");
//			cbxdependentField = new JComboBox();
//			cbxdependentField = populateWithMetaInf(cbxdependentField);
//			dependsOnField = new JLabel("Field it does depend on:");
//			cbxdependsOnField = new JComboBox();
//			cbxdependsOnField = populateWithMetaInf(cbxdependsOnField);
//
//			/** 0, 0 */
//			this.add(getDependentField(), "0,0");
//			/** 2, 0 */
//			this.add(getDependentFieldCombobox(), "2,0");
//			/** 4, 0 */
//			this.add(getDependsOnField(), "4,0");
//			/** 6, 0 */
//			this.add(getDependsOnFieldCombobox(), "6,0");
//			/** 8,0 */
//			this.addRemoveRowsFromPanel = new AddRemoveRowsFromPanel(this.getBackground(), this);
//			this.add(addRemoveRowsFromPanel, "8,0");
//		}
//
//		private JComboBox populateWithMetaInf(JComboBox combobox) {
//			for (AttributeCVO ds : metaInf.getMetaGO()) {
//				combobox.addItem(ds.getName());
//			}
//			combobox.setSelectedIndex(0);
//			return combobox;
//		}
//
//		public JLabel getDependentField() {
//			return dependentField;
//		}
//		public JLabel getDependsOnField() {
//			return dependsOnField;
//		}
//		public JComboBox getDependentFieldCombobox() {
//			return cbxdependentField;
//		}
//		public JComboBox getDependsOnFieldCombobox() {
//			return cbxdependsOnField;
//		}
//
//		public void performAddAction() {
//			addAnotherDependency(new LayoutMLDependency());
//			dependenciesPanel.updateUI();
//		}
//
//		public void performRemoveAction() {
//			removeDependencyPanel(this);
//		}
//
//		public AddRemoveRowsFromPanel getAddRemoveRowsFromPanel() {
//			return this.addRemoveRowsFromPanel;
//		}
//
//		public LayoutMLDependency getLayoutMLDependency() {
//			return layoutMLDependency;
//		}
//	}
//}
