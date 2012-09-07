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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jfree.chart.JFreeChart;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation.StringResourceIdPair;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGChart;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyChartProperty.PropertyEditorChartProperty.PropertyStaticModel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGPropertySet;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.ui.ResourceIdMapper;
import org.nuclos.client.ui.collect.Chart;
import org.nuclos.client.ui.collect.Chart.ChartColumn;
import org.nuclos.client.ui.collect.Chart.ChartFunction;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

/**
 * This Class is for editing the {@link WYSIYWYGProperty} used for {@link WYSIWYGChart}.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:stefan.geiling@novabit.de">stefan.geiling</a>
 * @version 01.00.00
 */
public class PropertyChartPropertyDomainStep extends PanelWizardStep implements Closeable {

	class PropertyEditorString {

		private final String property;
		private final List<String> list;
		private final ResourceIdMapper<String> resourceIdMapper;
		private JComboBox comboBox;
		private Object value;
		//NUCLEUSINT-1159
		private final boolean insertable;
		
		/**
		 * The Constructor
		 * @param pairList
		 */
		public PropertyEditorString(String property, List<StringResourceIdPair> pairList, boolean insertable) {
			this.property = property;
			//NUCLEUSINT-1159
			this.insertable = insertable;
			
			if (pairList != null) {
				Map<String, String> map = CollectionUtils.transformPairsIntoMap(pairList);
				this.resourceIdMapper = new ResourceIdMapper<String>(map);
				this.list = new ArrayList<String>(map.keySet());
				// Sort collections by translation
				Collections.sort(this.list, resourceIdMapper);
			} else {
				this.list = null;
				this.resourceIdMapper = null;
			}
		}
		
		Object getEditorValue() {
			if (comboBox != null) {
				value = StringUtils.isNullOrEmpty((String)comboBox.getSelectedItem())?null:(String)comboBox.getSelectedItem();
			}
			return value;
		}
		void setEditorValue(Object value) {
			this.value = value;
			if (comboBox != null) {
				for (int i = 0; i < comboBox.getItemCount(); i++) {
					Object elem = comboBox.getModel().getElementAt(i);
					if (elem != null && elem.equals(value))	
						comboBox.setSelectedItem(value);
				}
			}
		}
		String getEditorProperty() {
			return this.property;
		}
		
		Component getComponent(boolean editable) {
			comboBox = new JComboBox();
			//NUCLEUSINT-1159
			if (insertable)
				comboBox.setEditable(true);
			//comboBox.setBorder(null);
			
			for (String item : list) {
				comboBox.addItem(item);
			}
			comboBox.setSelectedItem(value);
			
			comboBox.setRenderer(new DefaultListRenderer(resourceIdMapper));
			AutoCompleteDecorator.decorate(comboBox, resourceIdMapper);
			
			return comboBox;
		}
	}
	private Icon iconAdd = new ImageIcon(this.getClass().getClassLoader().getResource("org/nuclos/client/layout/wysiwyg/editor/ui/panels/icons/list-add.png"));
	private Icon iconRemove = new ImageIcon(this.getClass().getClassLoader().getResource("org/nuclos/client/layout/wysiwyg/editor/ui/panels/icons/list-remove.png"));

	private JFreeChart chart;
	private WYSIWYGChart wysiwygChart;
	
	private PropertyStaticModel model;
	
	private final WYSIYWYGProperty wysiywygProperty;
	
	private final List<PropertyEditorString> propertyEditorList = new ArrayList<PropertyEditorString>();
	
	private JPanel panel;
	
	public PropertyChartPropertyDomainStep(WYSIYWYGProperty wysiywygProperty) {
		this.wysiywygProperty = wysiywygProperty;
	}
	
	private StringBuffer combinedPrefixes = new StringBuffer("");
	
	@Override
	public void prepare() {
		super.prepare();
		
		chart = model.getChart();
		wysiwygChart = model.getWYSIWYGChart();
		
		String sPrefix = getChartProperty(Chart.PROPERTY_COMBINED_PREFIXES);
		combinedPrefixes = (sPrefix == null) ? new StringBuffer("") : new StringBuffer(sPrefix);
		
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        final ChartFunction chartFunction = getChartFunction();
		
		if (!chartFunction.isCombinedChart()) {
			panel.add(getPanelComponent(chartFunction, ""), BorderLayout.CENTER);
		} else {
			JPanel editorType = new JPanel();
			editorType.setLayout(new GridBagLayout());
			JLabel propTypeValue = new JLabel(
					//SpringLocaleDelegate.getInstance().getMessage("wysiwyg.chart.wizard.domain.value",
							"Diagramm hinzufügen:"/*)*/);
			editorType.add(propTypeValue, new GridBagConstraints(0, 0, 0, 1, 1D, 1D,
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
			final JComboBox propTypeComponent = new JComboBox(chartFunction.getCombinedChartFunctions());
			editorType.add(propTypeComponent, new GridBagConstraints(0, 1, 1, 1, 1D, 1D,
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 15, 0), 0, 0));

			final JTabbedPane tabbedPane = new JTabbedPane();

			JButton removeButton = new JButton(iconRemove);
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (tabbedPane.getSelectedIndex() != -1) {
						PanelComponent panelComponent
							= (PanelComponent)tabbedPane.getSelectedComponent();
						combinedPrefixes = new StringBuffer(
								combinedPrefixes.toString().replaceAll(panelComponent.prefix, ""));
						tabbedPane.remove(panelComponent);
					}
				}
			});
			editorType.add(removeButton, new GridBagConstraints(1, 1, 1, 1, 1D, 1D,
					GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
			JButton addButton = new JButton(iconAdd);
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ChartFunction cFunction = (ChartFunction)propTypeComponent.getSelectedItem();
					String prefix = cFunction.name() + "." + (Math.random() + "").replaceAll("\\.", "") + ":";

					combinedPrefixes.append(prefix);
					tabbedPane.add(cFunction.name(), getPanelComponent(cFunction, prefix));
					tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
				}
			});
			editorType.add(addButton, new GridBagConstraints(2, 1, 1, 1, 1D, 1D,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));

			String[] prefixes = combinedPrefixes.toString().split(":");
			for (String prefix : prefixes) {
				if (prefix.length() > 0) {
					try {
						ChartFunction cFunction = ChartFunction.valueOf(prefix.split("\\.")[0]);
						tabbedPane.add(cFunction.name(), getPanelComponent(cFunction, prefix + ":"));
					} catch (Exception e) {
						// ignore.
					}
				}
			}
		    panel.add(editorType, BorderLayout.NORTH);
		    panel.add(tabbedPane, BorderLayout.CENTER);
		}		
	}
	private class PanelComponent extends JScrollPane {
		final String prefix;
		public PanelComponent(String prefix, Component view) {
			super(view);
			this.prefix = prefix;
		}
	}
	private PanelComponent getPanelComponent(ChartFunction chartFunction, String prefix) {
		JPanel editor = new JPanel();
		editor.setLayout(new BorderLayout());
		
		JPanel valueEditor = new JPanel(new GridBagLayout());
		valueEditor.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Werte"
            )
        );
		
		JLabel propValue = new JLabel(
				SpringLocaleDelegate.getInstance().getMessage("wysiwyg.chart.wizard.domain.value",
						"Bestimmen Sie die Spalte, die die Werte enthält:"));
		valueEditor.add(propValue, new GridBagConstraints(0, 0, 1, 1, 1D, 1D,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
		
		ChartColumn[] valueColumns = chartFunction.getValueColumnDesc();
		for (int i = 0; i < valueColumns.length; i++) {
			ChartColumn chartColumn = valueColumns[i];
			PropertyEditorString propValueEditor = new PropertyEditorString(prefix + chartColumn.property,
					getFittingFieldnames(wysiwygChart.getEntityName(), chartColumn.clazz), true);
			propValueEditor.setEditorValue(getChartProperty(prefix + chartColumn.property));
			valueEditor.add(new JLabel(SpringLocaleDelegate.getInstance().getMessage("wysiwyg.chart.wizard.domain.column."
					+ chartColumn.property, "")), new GridBagConstraints(0, i+1, 1, 1, 1D, 1D,
							GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 10, 0), 0, 0));
			valueEditor.add(propValueEditor.getComponent(true), new GridBagConstraints(1, i+1, 1, 1, 1D, 1D,
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, -152, 10, 0), 0, 0));	
			propertyEditorList.add(propValueEditor);
		}
		
		editor.add(valueEditor, BorderLayout.NORTH);
		
		JPanel domainEditor = new JPanel(new GridBagLayout());
		domainEditor.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Domain"
            )
        );

		ChartColumn[] domainColumns = chartFunction.getDomainColumnDesc();
		if (domainColumns.length > 0) {
			JLabel propDomain = new JLabel(
					SpringLocaleDelegate.getInstance().getMessage("wysiwyg.chart.wizard.domain.compare",
							"Geben Sie hier die Spalte(n) für die vergleichenden Werte oder weitere Angaben an:"));
			domainEditor.add(propDomain, new GridBagConstraints(0, 0, 1, 1, 1D, 1D,
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
			
			int glue = 185;
			for (int i = 0; i < domainColumns.length; i++) {
				ChartColumn chartColumn = domainColumns[i];
				PropertyEditorString propDomainEditor = new PropertyEditorString(prefix + chartColumn.property,
						getFittingFieldnames(wysiwygChart.getEntityName(), chartColumn.clazz), true);
				propDomainEditor.setEditorValue(getChartProperty(prefix + chartColumn.property));
				domainEditor.add(new JLabel(SpringLocaleDelegate.getInstance().getMessage("wysiwyg.chart.wizard.domain.column."
						+ chartColumn.property, "")), new GridBagConstraints(0, i+1, 1, 1, 1D, 1D,
								GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 10, 0), 0, 0));
				domainEditor.add(propDomainEditor.getComponent(true), new GridBagConstraints(1, i+1, 1, 1, 1D, 1D,
						GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, -240, 10, 0), 0, 0));
				glue -= propDomainEditor.comboBox.getPreferredSize().height + 10; // add size.
				propertyEditorList.add(propDomainEditor);
			}
	
			domainEditor.add(new JPanel(), new GridBagConstraints(0,3, 1, 1, 1D, 1D,
					GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(glue < 0 ? 0: glue, 0, 10, 0), 0, 0));
	
			editor.add(domainEditor, BorderLayout.CENTER);
		}		
        editor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        PanelComponent pnlScroller = new PanelComponent(prefix, editor);
        pnlScroller.setPreferredSize(new Dimension(250, 80));
        pnlScroller.setAlignmentX(LEFT_ALIGNMENT);
        pnlScroller.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        return pnlScroller;
	}
	
	@Override
	public void init(WizardModel model) {
		super.init(model);
		
		this.model = (PropertyStaticModel) model;
	}
	
	@Override
	public Component getView() {
		return panel;
	}
	
	@Override
	public String getSummary() {
		return "Domain-Daten bearbeiten";
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100,100);
	}
	
	@Override
	public String getName() {
		return "Domain-Daten";
	}
	
	@Override
	public Icon getIcon() {
		return null;
	}
	
	private ChartFunction getChartFunction() {
		for (WYSIYWYGPropertySet propertySet : wysiywygProperty.properties) {
			if (propertySet.getPropertyName().equals(Chart.PROPERTY_TYPE))
				return ChartFunction.valueOf(propertySet.getPropertyValue());
		}
		return null;
	}

	private String getChartProperty(String prop) {
		for (WYSIYWYGPropertySet propertySet : ((PropertyChartProperty)wysiwygChart.getProperties().getProperty(WYSIWYGChart.PROPERTY_PROPERTIES)).getValue().properties) {
			if (propertySet.getPropertyName().equals(prop))
				return propertySet.getPropertyValue();
		}
		return null;
	}
	
	@Override
	public void applyState() throws InvalidStateException {
		for (PropertyEditorString propertyEditor : propertyEditorList) {
			wysiywygProperty.addWYSIYWYGPropertySet(
					new WYSIYWYGPropertySet(propertyEditor.getEditorProperty(), (String)propertyEditor.getEditorValue()));
		}
		
		wysiwygChart.setChartFromProperties(); // refresh
		
		List<Pair<String, String>> chartProperties = getChartFunction().getChartProperties(wysiwygChart.getChart(), wysiwygChart.getChart().getChartPanel().getChart());
		for (Pair<String, String> property : chartProperties) {
			if (!property.getX().equals(Chart.PROPERTY_TYPE)) {
				wysiywygProperty.addWYSIYWYGPropertySet(
						new WYSIYWYGPropertySet(property.getX(), property.getY()));
			}
		}
		wysiywygProperty.addWYSIYWYGPropertySet(
				new WYSIYWYGPropertySet(Chart.PROPERTY_COMBINED_PREFIXES, combinedPrefixes.toString()));
		
		WYSIWYGChart c = new WYSIWYGChart(wysiwygChart.getMetaInformation());
		c.setProperties(wysiwygChart.getProperties());
		c.setProperties(new PropertyChartProperty(c, wysiywygProperty));
		c.finalizeInitialLoading();
		
		model.setChart(c.getChart().getChartPanel().getChart());
		
		super.applyState();
	}
	
	@Override
	public boolean isComplete() {
		return true;
	}
	
	@Override
	public boolean isBusy() {
		return false;
	}
	
	@Override
	public void abortBusy() {
		close();
	}
	
	@Override
	public void close() {
		removeAll();
	}
	
	private List<StringResourceIdPair> getFittingFieldnames(String entityName, Class clazz){
		List<StringResourceIdPair> result = new ArrayList<StringResourceIdPair>();
 		Integer iModuleId;
		try {
			iModuleId = Modules.getInstance().getModuleIdByEntityName(entityName);
		}
		catch (NoSuchElementException ex) {
			iModuleId = null;
		}

		if (iModuleId != null) {
			for (AttributeCVO a : AttributeCache.getInstance().getAttributes()) {
				if (clazz.isAssignableFrom(a.getJavaClass())
						&& !a.getName().equalsIgnoreCase("genericObject"))
					result.add(new StringResourceIdPair(a.getName(), a.getResourceSIdForLabel()));
			}
		}
		else {
			MasterDataMetaVO metaVO = MetaDataCache.getInstance().getMetaData(entityName);
			for (String s : metaVO.getFieldNames()) {
				if (clazz.isAssignableFrom(metaVO.getField(s).getJavaClass())
						&& !s.equalsIgnoreCase("genericObject"))
					result.add(new StringResourceIdPair(s, null));
			}
		}

		return result;
	}
}
