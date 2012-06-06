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

import java.awt.Component;
import java.awt.Dimension;
import java.io.Closeable;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.editor.ChartEditor;
import org.jfree.chart.editor.ChartEditorManager;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGChart;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyChartProperty.PropertyEditorChartProperty.PropertyStaticModel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGPropertySet;
import org.nuclos.client.ui.collect.Chart;
import org.nuclos.client.ui.collect.Chart.ChartFunction;
import org.nuclos.common.collection.Pair;
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
public class PropertyChartPropertyGeneralStep extends PanelWizardStep implements Closeable {

	private JFreeChart chart;
	private final WYSIYWYGProperty wysiywygProperty;
	
	private PropertyStaticModel model;
	
	private ChartEditor chartEditor;
	
	public PropertyChartPropertyGeneralStep(WYSIYWYGProperty wysiywygProperty) {
		this.wysiywygProperty = wysiywygProperty;
	}
	
	private ChartFunction getChartFunction() {
		for (WYSIYWYGPropertySet propertySet : wysiywygProperty.properties) {
			if (propertySet.getPropertyName().equals(Chart.PROPERTY_TYPE))
				return ChartFunction.valueOf(propertySet.getPropertyValue());
		}
		return null;
	}
	
	@Override
	public void prepare() {
		super.prepare();
		
		chart = model.getChart();		
		chartEditor = ChartEditorManager.getChartEditor(chart);
	}
	
	@Override
	public void init(WizardModel model) {
		super.init(model);
		
		this.model = (PropertyStaticModel)model;
	}
	
	@Override
	public Component getView() {
		return (JPanel)chartEditor;
	}
	
	@Override
	public String getSummary() {
		return "Eigenschaften bearbeiten";
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100,100);
	}
	
	@Override
	public String getName() {
		return "Eigenschaften";
	}
	
	@Override
	public Icon getIcon() {
		return null;
	}
	
	@Override
	public void applyState() throws InvalidStateException {
		chartEditor.updateChart(chart);
		
		List<Pair<String, String>> chartProperties = getChartFunction().getChartProperties(chart);
		for (Pair<String, String> property : chartProperties) {
			wysiywygProperty.addWYSIYWYGPropertySet(
					new WYSIYWYGPropertySet(property.getX(), property.getY()));
		}
		
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
}
