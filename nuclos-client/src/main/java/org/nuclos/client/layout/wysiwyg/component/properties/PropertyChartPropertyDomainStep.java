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
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Spring;

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

	private JFreeChart chart;
	private WYSIWYGChart wysiwygChart;
	
	private PropertyStaticModel model;
	
	private final WYSIYWYGProperty wysiywygProperty;
	
	private final List<PropertyEditorString> propertyEditorList = new ArrayList<PropertyEditorString>();
	
	private JPanel panel;
	
	public PropertyChartPropertyDomainStep(WYSIYWYGProperty wysiywygProperty) {
		this.wysiywygProperty = wysiywygProperty;
	}
	
	@Override
	public void prepare() {
		super.prepare();
		
		chart = model.getChart();
		wysiwygChart = model.getWYSIWYGChart();

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
		
		ChartColumn[] valueColumns = getChartFunction().getValueColumnDesc();
		for (int i = 0; i < valueColumns.length; i++) {
			ChartColumn chartColumn = valueColumns[i];
			PropertyEditorString propValueEditor = new PropertyEditorString(chartColumn.property,
					getFittingFieldnames(wysiwygChart.getEntityName(), chartColumn.clazz), true);
			propValueEditor.setEditorValue(getChartProperty(chartColumn.property));
			valueEditor.add(propValueEditor.getComponent(true), new GridBagConstraints(0, i+1, 1, 1, 1D, 1D,
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 10, 0), 0, 0));	
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
		
		JLabel propDomain = new JLabel(
				SpringLocaleDelegate.getInstance().getMessage("wysiwyg.chart.wizard.domain.compare",
						"Geben Sie hier die Spalte(n) für die vergleichenden Werte an:"));
		domainEditor.add(propDomain, new GridBagConstraints(0, 0, 1, 1, 1D, 1D,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
		
		int glue = 185;
		ChartColumn[] domainColumns = getChartFunction().getDomainColumnDesc();
		for (int i = 0; i < domainColumns.length; i++) {
			ChartColumn chartColumn = domainColumns[i];
			PropertyEditorString propDomainEditor = new PropertyEditorString(chartColumn.property,
					getFittingFieldnames(wysiwygChart.getEntityName(), chartColumn.clazz), true);
			propDomainEditor.setEditorValue(getChartProperty(chartColumn.property));
			domainEditor.add(propDomainEditor.getComponent(true), new GridBagConstraints(0, i+1, 1, 1, 1D, 1D,
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 10, 0), 0, 0));
			glue -= propDomainEditor.comboBox.getPreferredSize().height + 10; // add size.
			propertyEditorList.add(propDomainEditor);
		}

		domainEditor.add(new JPanel(), new GridBagConstraints(0,3, 1, 1, 1D, 1D,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(glue < 0 ? 0: glue, 0, 10, 0), 0, 0));

		editor.add(domainEditor, BorderLayout.CENTER);
		
        editor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        JScrollPane pnlScroller = new JScrollPane(editor);
        pnlScroller.setPreferredSize(new Dimension(250, 80));
        pnlScroller.setAlignmentX(LEFT_ALIGNMENT);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(pnlScroller, BorderLayout.CENTER);
		
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
		
		List<Pair<String, String>> chartProperties = getChartFunction().getChartProperties(wysiwygChart.getChart().getChartPanel().getChart());
		for (Pair<String, String> property : chartProperties) {
			if (!property.getX().equals(Chart.PROPERTY_TYPE))
				wysiywygProperty.addWYSIYWYGPropertySet(
						new WYSIYWYGPropertySet(property.getX(), property.getY()));
		}
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
