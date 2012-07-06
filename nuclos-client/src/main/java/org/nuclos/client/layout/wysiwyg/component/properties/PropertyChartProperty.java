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
package org.nuclos.client.layout.wysiwyg.component.properties;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jfree.chart.JFreeChart;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_COMPONENT_PROPERTY_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGChart;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGUniversalComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.editor.PropertyEditor;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGPropertySet;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.pietschy.wizard.ButtonBar;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardStep;
import org.pietschy.wizard.models.StaticModel;
import org.pietschy.wizard.models.StaticModelOverview;
import org.xml.sax.Attributes;

/**
 * This Class is for editing the {@link WYSIYWYGProperty} used for {@link WYSIWYGUniversalComponent}.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:stefan.geiling@novabit.de">stefan.geiling</a>
 * @version 01.00.00
 */
public class PropertyChartProperty implements PropertyValue<WYSIYWYGProperty>, LayoutMLConstants {

	private WYSIYWYGProperty wysiwygProperty = null;
	private WYSIWYGComponent wysiwygComponent = null;

	/**
	 * Constructor
	 */
	public PropertyChartProperty(WYSIWYGComponent c) {
		this(c, new WYSIYWYGProperty());
	}
	/**
	 * Constructor
	 */
	public PropertyChartProperty(WYSIWYGComponent c, WYSIYWYGProperty p) {
		this.wysiwygComponent = c;
		this.wysiwygProperty = p;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorChartProperty(dialog);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorChartProperty(dialog);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public WYSIYWYGProperty getValue() {
		return wysiwygProperty;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(WYSIYWYGProperty value) {
		this.wysiwygProperty = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		if (ELEMENT_PROPERTY.equals(attributeName)){
			String propertyName = attributes.getValue(ATTRIBUTE_NAME);
			String propertyValue = attributes.getValue(ATTRIBUTE_VALUE);
			WYSIYWYGPropertySet newPropertySet = new WYSIYWYGPropertySet(propertyName, propertyValue);
			wysiwygProperty.addWYSIYWYGPropertySet(newPropertySet);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		PropertyChartProperty clonedPropertyCollectableComponentProperty = new PropertyChartProperty(wysiwygComponent);
		if (wysiwygProperty != null) {
			clonedPropertyCollectableComponentProperty.setValue((WYSIYWYGProperty) wysiwygProperty.clone());
		}
		return clonedPropertyCollectableComponentProperty;
	}
	
	/**
	 * This class launches the {@link PropertyEditor} to edit the {@link WYSIYWYGProperty}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:stefan.geiling@novabit.de">stefan.geiling</a>
	 * @version 01.00.00
	 */
	class PropertyEditorChartProperty extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private final PropertiesPanel dialog;
		public PropertyEditorChartProperty(PropertiesPanel dialog) {
			this.dialog = dialog;
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return getComponent(true);
		}

		/**
		 * @param editable should it be possible to edit the value?
		 * @return
		 */
		private Component getComponent(boolean editable) {
			JPanel panel = new JPanel();
			panel.setOpaque(true);
			panel.setBackground(Color.WHITE);
			panel.setLayout(new TableLayout(new double[][]{
					{
						InterfaceGuidelines.CELL_MARGIN_LEFT,
						TableLayout.FILL,
						TableLayout.PREFERRED,
						InterfaceGuidelines.MARGIN_RIGHT
					},
					{
						InterfaceGuidelines.CELL_MARGIN_TOP,
						TableLayout.PREFERRED,
						InterfaceGuidelines.CELL_MARGIN_BOTTOM
					}
			}));
			
			final JLabel label = new JLabel();
			setLabel(label,	getValue());

			JButton launchEditor = new JButton("...");
			
			// get props from dialog panel
			boolean bEnable = false;
			bEnable = ((PropertyValueString)dialog.getTemporaryValuesMap().get(WYSIWYGChart.PROPERTY_ENTITY)).getValue() != null;
			if (bEnable)
				bEnable = ((PropertyValueString)dialog.getTemporaryValuesMap().get(WYSIWYGChart.PROPERTY_FOREIGNKEY)).getValue() != null;
			launchEditor.setEnabled(bEnable);
		
			launchEditor.setPreferredSize(new Dimension(30, InterfaceGuidelines.CELL_BUTTON_MAXHEIGHT));
			launchEditor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					launchEditor(label);
 				}
			});
			TableLayoutConstraints constraint = new TableLayoutConstraints(2, 1);
			panel.add(launchEditor, constraint);
			panel.add(label, new TableLayoutConstraints(1,1));
			return panel;
		}
		
		/**
		 * Sets the Label for display in the {@link PropertiesPanel} to indicate a value is set
		 * @param label
		 * @param property
		 */
		private void setLabel(JLabel label, WYSIYWYGProperty property) {
			if (property != null && property.getSize() > 0) {
				label.setText(WYSIWYGStringsAndLabels.partedString(COLLECTABLE_COMPONENT_PROPERTY_EDITOR.PROPERTIES_DEFINED, String.valueOf(property.getSize())));
			}
			else {
				label.setText(COLLECTABLE_COMPONENT_PROPERTY_EDITOR.NO_PROPERTIES_DEFINED);
			}
		}

		public final class PropertyStaticModel extends StaticModel {
			private Wizard wizard;
			private JFreeChart chart;
			private WYSIWYGChart wysiwygChart;
			public PropertyStaticModel(JFreeChart chart, WYSIWYGChart wysiwygChart) {
				this.chart = chart;
				this.wysiwygChart = wysiwygChart;
			}
			public void setChart(JFreeChart chart) {
				this.chart = chart;
			}
			public JFreeChart getChart() {
				return this.chart;
			}
			public WYSIWYGChart getWYSIWYGChart() {
				return this.wysiwygChart;
			}
			
			@Override
			public JComponent getOverviewComponent() {
				return new StaticModelOverview(this);
			}
			@Override
			public void refreshModelState() {
				super.refreshModelState();
				
				if (wizard != null) {
					WizardStep activeStep = getActiveStep();
					wizard.getNextAction().setEnabled(activeStep != null && !isLastStep(activeStep) && activeStep.isComplete() && !activeStep.isBusy());
					wizard.getFinishAction().setEnabled(activeStep != null && isLastStep(activeStep) && activeStep.isComplete() && !activeStep.isBusy());
				}
			}
			
			public void setWizard(Wizard wizard) {
				this.wizard = wizard;
			}
		}

		private final class PropertyStaticModelOverview extends JPanel implements PropertyChangeListener {

			private StaticModel model;
			private HashMap<WizardStep, JLabel> labels = new HashMap<WizardStep, JLabel>();

			public PropertyStaticModelOverview(StaticModel model) {
				this.model = model;
				this.model.addPropertyChangeListener(this);
				setBackground(Color.WHITE);
				setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

				JLabel title = new JLabel(SpringLocaleDelegate.getInstance().getMessage(
						"wysiwyg.chart.wizard.overview", "\u00dcbersicht"));
				title.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
				title.setAlignmentX(0);
				title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getMaximumSize().height));
				add(title);
				int i = 1;
				for (Iterator<WizardStep> iter = model.stepIterator(); iter.hasNext();) {
					WizardStep step = iter.next();
					JLabel label = new JLabel("" + i++ + ". " + step.getName());
					label.setBackground(Color.GRAY.brighter());
					label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
					label.setAlignmentX(0);
					label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getMaximumSize().height));
					add(label);
					labels.put(step, label);
				}

				add(Box.createGlue());
			}

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("activeStep")) {
					JLabel old = labels.get(evt.getOldValue());
					if (old != null)
						formatInactive(old);

					JLabel label = labels.get(evt.getNewValue());
					formatActive(label);
					repaint();
				}
			}

			protected void formatActive(JLabel label) {
				label.setOpaque(true);
			}

			protected void formatInactive(JLabel label) {
				label.setOpaque(false);
			}

			public void setEnabled(Class<? extends WizardStep> clazz, boolean enabled) {
				for (WizardStep s : labels.keySet()) {
					if (clazz.isAssignableFrom(s.getClass())) {
						labels.get(s).setEnabled(enabled);
					}
				}
			}
		}
		
		/**
		 * This Method launches the {@link PropertyEditor}
		 * @param label
		 */
		public final void launchEditor(JLabel label){
			if (getValue() == null) {
				setValue(new WYSIYWYGProperty());
			}
			
			WYSIYWYGProperty returnWYSIWYGProperty = new WYSIYWYGProperty();
			
			//((WYSIWYGChart)wysiwygComponent).setChartFromProperties(); // refresh.
			JFreeChart chart = null;
			try {
				 chart = (JFreeChart) ((WYSIWYGChart)wysiwygComponent).getChart().getChartPanel().getChart().clone();
			    	
			} catch (Exception e) {
				// ignore
			}
			
			final WYSIYWYGProperty oldProperty = ((PropertyChartProperty)wysiwygComponent
					.getProperties().getProperty(WYSIWYGChart.PROPERTY_PROPERTIES)).getValue();
			try {
				// set props from dialog panel
				wysiwygComponent.setProperty(WYSIWYGChart.PROPERTY_ENTITY, (PropertyValueString)dialog.getTemporaryValuesMap().get(WYSIWYGChart.PROPERTY_ENTITY), null);
				wysiwygComponent.setProperty(WYSIWYGChart.PROPERTY_FOREIGNKEY, (PropertyValueString)dialog.getTemporaryValuesMap().get(WYSIWYGChart.PROPERTY_FOREIGNKEY), null);
			} catch (CommonBusinessException e) {
				// ignore
			}

			PropertyStaticModel model = new PropertyStaticModel(chart, (WYSIWYGChart)wysiwygComponent);
			
			model.add(new PropertyChartPropertyChartStep(returnWYSIWYGProperty));
		    model.add(new PropertyChartPropertyDomainStep(returnWYSIWYGProperty));
		    model.add(new PropertyChartPropertyGeneralStep(returnWYSIWYGProperty));

			model.setLastVisible(false);
			
			Wizard w = new Wizard(model) {
				@Override
				protected ButtonBar createButtonBar() {
					return super.createButtonBar();//new WizardButtonBar(this);
				}
			};
			model.setWizard(w);
			w.getNextAction().setEnabled(true);
			w.getCancelAction().setEnabled(true);
			w.setPreferredSize(new Dimension(672,500));
			
			w.showInDialog("Chart-Eigenschaften", 
					UIUtils.getFrameForComponent(wysiwygComponent.getParentEditor()), true);
			
			if (!w.wasCanceled())
				wysiwygProperty = returnWYSIWYGProperty;
			else
				wysiwygProperty = oldProperty;

			setLabel(label, wysiwygProperty);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return PropertyChartProperty.this;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return getComponent(false);
		}
	}
}
