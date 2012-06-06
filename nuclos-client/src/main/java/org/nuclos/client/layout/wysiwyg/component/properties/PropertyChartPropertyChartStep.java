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
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Closeable;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGChart;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyChartProperty.PropertyEditorChartProperty.PropertyStaticModel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGPropertySet;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.collect.Chart;
import org.nuclos.client.ui.collect.Chart.ChartFunction;
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
public class PropertyChartPropertyChartStep extends PanelWizardStep implements Closeable {

	class ChartTypeEntry {
		  private final String type;
		  private final String title;
		  private final ImageIcon image;

		  public ChartTypeEntry(String title, ImageIcon image, String type) {
		    this.title = title;
		    this.image = image;
		    
		    this.type = type;
		  }
		  public String getTitle() {
		    return title;
		  }
		  public ImageIcon getImage() {
		    return image;
		  }
		  public String getType() {
		    return type;
		  }
	}
	
	private ChartTypeEntry[] chartTypes;

	private PropertyStaticModel model;
	
	private final WYSIYWYGProperty wysiywygProperty;
	
	private ChartFunction chartFunction;
	
	private JList list;
	private JPanel panel;
	
	public PropertyChartPropertyChartStep(WYSIYWYGProperty wysiywygProperty) {
		this.wysiywygProperty = wysiywygProperty;
		
		chartTypes = new ChartTypeEntry[] {
				new ChartTypeEntry("BarChart", Icons.getInstance().getBarChartIcon(), ChartFunction.BarChart.name()),
				new ChartTypeEntry("LineChart", Icons.getInstance().getLineChartIcon(), ChartFunction.LineChart.name()),
				new ChartTypeEntry("PieChart", Icons.getInstance().getPieChartIcon(), ChartFunction.PieChart.name()),
				new ChartTypeEntry("TimeSeriesChart", Icons.getInstance().getTimeSeriesChartIcon(), ChartFunction.TimeSeriesChart.name()),
				new ChartTypeEntry("XYSeriesChart", Icons.getInstance().getXYSeriesChartIcon(), ChartFunction.XYSeriesChart.name())
			};
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
				
		list = new JList(chartTypes) {
			
			@Override
			public int getScrollableUnitIncrement(Rectangle visibleRect,
                                                  int orientation,
                                                  int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL &&
                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0))  {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(
                                visibleRect, orientation, direction);
            }
        };
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setCellRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JPanel btnPanel = new JPanel(new BorderLayout());
				btnPanel.setOpaque(false);
				
				JButton btn = new JButton(((ChartTypeEntry) value).getImage());
				btn.setBorderPainted(false);
				btn.setContentAreaFilled(true);
				
				if (isSelected && index >= 0) {
					btn.setOpaque(true);
					btn.setBackground(NuclosThemeSettings.BACKGROUND_COLOR4);
					btnPanel.setBorder(BorderFactory.createLineBorder(NuclosThemeSettings.BACKGROUND_ROOTPANE, 1));
				} else {
					btn.setOpaque(false);
					btnPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				}
				btnPanel.add(btn, BorderLayout.CENTER);
				
				return btnPanel;
			}
		});
        list.setVisibleRowCount(-1);
        
        chartFunction = getChartFunction();
        if (chartFunction != null) {
        	for (int i = 0; i < chartTypes.length; i++) {
				if (chartTypes[i].getType().equals(chartFunction.name())) {
		        	list.setSelectedValue(chartTypes[i], true);
		        	break;
				}
			}
        }
        
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(listScroller, BorderLayout.CENTER);
	}
	
	@Override
	public void init(WizardModel model) {
		super.init(model);
		
		this.model = (PropertyStaticModel) model;

		// store chart type.
		for (WYSIYWYGPropertySet propertySet : ((PropertyChartProperty)this.model.getWYSIWYGChart().getProperties().getProperty(WYSIWYGChart.PROPERTY_PROPERTIES)).getValue().properties) {
			if (propertySet.getPropertyName().equals(Chart.PROPERTY_TYPE)) {
				wysiywygProperty.addWYSIYWYGPropertySet(
						new WYSIYWYGPropertySet(Chart.PROPERTY_TYPE,
								ChartFunction.valueOf(propertySet.getPropertyValue()).name()));
			}
		}
	}
	
	@Override
	public Component getView() {
		return panel;
	}
	
	@Override
	public String getSummary() {
		return "Chart-Typ auswählen";
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100,100);
	}
	
	@Override
	public String getName() {
		return "Chart-Typ";
	}
	
	@Override
	public Icon getIcon() {
		return null;
	}
	
	@Override
	public void applyState() throws InvalidStateException {
		wysiywygProperty.addWYSIYWYGPropertySet(
				new WYSIYWYGPropertySet(Chart.PROPERTY_TYPE,
						((ChartTypeEntry)list.getSelectedValue()).getType()));
		
		super.applyState();
	}
	
	@Override
	public boolean isComplete() {
		return list.getSelectedValue() != null;
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
