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
package org.nuclos.client.ui.collect;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.nuclos.client.common.FocusActionListener;
import org.nuclos.client.common.SearchConditionSubFormController.SearchConditionTableModelImpl;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.datasource.admin.ParameterPanel;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.component.LookupListener;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.client.ui.event.PopupMenuMouseAdapter;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Chart for displaying/editing dependant <code>Collectable</code>s.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@SuppressWarnings("serial")
public class Chart extends JPanel 
	implements ActionListener, Closeable {
	
	public static final String PROPERTY_TYPE = "type";
	public static final String PROPERTY_SHOW_TITLE = "showTitle";
	public static final String PROPERTY_TITLE = "title";
	public static final String PROPERTY_TITLE_FONT = "titleFont";
	public static final String PROPERTY_TITLE_COLOR = "titleColor";
	public static final String PROPERTY_BORDER = "border";
	public static final String PROPERTY_BORDER_COLOR = "borderColor";
	public static final String PROPERTY_BORDER_VISIBLE = "borderVisible";
	public static final String PROPERTY_DOMAIN_COLUMN = "domainColumn";
	public static final String PROPERTY_DOMAIN_LABEL = "domainLabel";
	public static final String PROPERTY_DOMAIN_LABEL_FONT = "domainLabelFont";
	public static final String PROPERTY_DOMAIN_LABEL_COLOR = "domainLabelColor";
	public static final String PROPERTY_DOMAIN_TICK_LABELS_VISIBLE = "domainTickLabelsVisible";
	public static final String PROPERTY_DOMAIN_TICK_MARKS_VISIBLE = "domainTickMarksVisible";
	public static final String PROPERTY_DOMAIN_AUTORANGE = "domainAutoRange";
	public static final String PROPERTY_DOMAIN_LOWERBOUND = "domainLowerBound";
	public static final String PROPERTY_DOMAIN_UPPERBOUND = "domainUpperBound";
	public static final String PROPERTY_RANGE_COLUMN = "rangeColumn";
	public static final String PROPERTY_RANGE_LABEL = "rangeLabel";
	public static final String PROPERTY_RANGE_LABEL_FONT = "rangeLabelFont";
	public static final String PROPERTY_RANGE_LABEL_COLOR = "rangeLabelColor";
	public static final String PROPERTY_RANGE_TICK_LABELS_VISIBLE = "rangeTickLabelsVisible";
	public static final String PROPERTY_RANGE_TICK_MARKS_VISIBLE = "rangeTickMarksVisible";
	public static final String PROPERTY_RANGE_AUTORANGE = "rangeAutoRange";
	public static final String PROPERTY_RANGE_LOWERBOUND = "rangeLowerBound";
	public static final String PROPERTY_RANGE_UPPERBOUND = "rangeUpperBound";
	public static final String PROPERTY_VALUE_COLUMN = "valueColumn";
	public static final String PROPERTY_DRAW_ANTIALIASED = "drawAntiAliased";
	public static final String PROPERTY_BACKGROUND = "background";
	public static final String PROPERTY_PLOT_BACKGROUND = "plotbackground";
	public static final String PROPERTY_PLOT_ORIENTATION = "plotOrientation";
	
	private static final Logger LOG = Logger.getLogger(Chart.class);
	
	public static class ChartColumn {
		public final Class clazz;
		public final String property;
		public final String label;
		public ChartColumn(String property, String label, Class clazz) {
			this.property = property;
			this.label = label;
			this.clazz = clazz;
		}
	}

	public static enum ChartFunction {
		BarChart {
			@Override
			protected Dataset createDataset(Chart chart) {
				DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
				
				try {
					JTable tbl = chart.getSubForm().getJTable();
					TableColumn cValue = tbl.getColumn(chart.getProperty(PROPERTY_VALUE_COLUMN, String.class));
					TableColumn cDomainAxis = tbl.getColumn(chart.getProperty(PROPERTY_DOMAIN_COLUMN, String.class));
					TableColumn cRangeAxis = tbl.getColumn(chart.getProperty(PROPERTY_RANGE_COLUMN, String.class));
					if (tbl.getModel() instanceof SearchConditionTableModelImpl) {
						defaultcategorydataset.addValue(0, "", "");
						return defaultcategorydataset;
					}
					if (tbl.getColumnCount() > 0)
						if (tbl.getRowCount() <= 0) {
							defaultcategorydataset.addValue(0, "", "");
						} else {
							for (int j = 0; j < tbl.getRowCount(); j++) {
								CollectableField vValue = (CollectableField)tbl.getValueAt(j, cValue.getModelIndex());
								CollectableField vDomainAxis = (CollectableField)tbl.getValueAt(j, cDomainAxis.getModelIndex());
								CollectableField vRangeAxis = (CollectableField)tbl.getValueAt(j, cRangeAxis.getModelIndex());
								defaultcategorydataset.addValue(vValue == null || vValue.getValue() == null ? 0 : (Number)vValue.getValue(),
									vDomainAxis == null || vDomainAxis.getValue() == null ? "" : (Comparable)vDomainAxis.getValue(), vRangeAxis == null || vRangeAxis.getValue() == null ? "" : (Comparable)vRangeAxis.getValue());
							}
						}
				} catch (Exception e) {
					//...
				}
				
				return defaultcategorydataset;
	        }
			@Override
			public JFreeChart createChart(Chart chart) {
				CategoryDataset dataset = (CategoryDataset)createDataset(chart);
				JFreeChart jfreechart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);
				ChartUtilities.applyCurrentTheme(jfreechart);
			    
				setCommonChartProperties(chart, jfreechart);

				if (jfreechart.getPlot() == null || !(jfreechart.getPlot() instanceof CategoryPlot))
					return jfreechart;
				
				// set properties for category plot
				((CategoryPlot)jfreechart.getPlot()).setOrientation(
						chart.getProperty(PROPERTY_PLOT_ORIENTATION, String.class, PlotOrientation.HORIZONTAL.toString())
							.equals(PlotOrientation.VERTICAL.toString()) ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL);
				// set properties for domain axis
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setLabel(chart.getProperty(PROPERTY_DOMAIN_LABEL, String.class));
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setLabelFont(chart.getProperty(PROPERTY_DOMAIN_LABEL_FONT, Font.class));
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setLabelPaint(chart.getProperty(PROPERTY_DOMAIN_LABEL_COLOR, Color.class, Color.BLACK));
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setTickLabelsVisible(chart.getProperty(PROPERTY_DOMAIN_TICK_LABELS_VISIBLE, Boolean.class, Boolean.TRUE));
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setTickMarksVisible(chart.getProperty(PROPERTY_DOMAIN_TICK_MARKS_VISIBLE, Boolean.class, Boolean.TRUE));
				// set properties for range axis
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setLabel(chart.getProperty(PROPERTY_RANGE_LABEL, String.class));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setLabelFont(chart.getProperty(PROPERTY_RANGE_LABEL_FONT, Font.class));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setLabelPaint(chart.getProperty(PROPERTY_RANGE_LABEL_COLOR, Color.class, Color.BLACK));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setTickLabelsVisible(chart.getProperty(PROPERTY_RANGE_TICK_LABELS_VISIBLE, Boolean.class, Boolean.TRUE));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setTickMarksVisible(chart.getProperty(PROPERTY_RANGE_TICK_MARKS_VISIBLE, Boolean.class, Boolean.TRUE));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setAutoRange(chart.getProperty(PROPERTY_RANGE_AUTORANGE, Boolean.class, Boolean.TRUE));
				if (!((CategoryPlot)jfreechart.getPlot()).getRangeAxis().isAutoRange()) {
					((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setLowerBound(chart.getProperty(PROPERTY_RANGE_LOWERBOUND, Double.class, 0D));
					((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setUpperBound(chart.getProperty(PROPERTY_RANGE_UPPERBOUND, Double.class, 0D));
				}
				
				return jfreechart;
			}
			@Override
			public List<Pair<String, String>> getChartProperties(JFreeChart chart) {
				List<Pair<String, String>> result = new ArrayList<Pair<String,String>>();
				
				result.addAll(getCommonChartProperties(chart));

				if (chart.getPlot() == null || !(chart.getPlot() instanceof CategoryPlot))
					return result;
				
				// set properties for category plot
				result.add(new Pair<String, String>(Chart.PROPERTY_PLOT_ORIENTATION, "" + ((CategoryPlot)chart.getPlot()).getOrientation().toString()));
				// set properties for domain axis
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LABEL, "" + ((CategoryPlot)chart.getPlot()).getDomainAxis().getLabel()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LABEL_FONT, "" + fontToString(((CategoryPlot)chart.getPlot()).getDomainAxis().getLabelFont())));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LABEL_COLOR, "" + ((Color)((CategoryPlot)chart.getPlot()).getDomainAxis().getLabelPaint()).getRGB()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_TICK_LABELS_VISIBLE, "" + ((CategoryPlot)chart.getPlot()).getDomainAxis().isTickLabelsVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_TICK_MARKS_VISIBLE, "" + ((CategoryPlot)chart.getPlot()).getDomainAxis().isTickMarksVisible()));
				// set properties for range axis
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LABEL, "" + ((CategoryPlot)chart.getPlot()).getRangeAxis().getLabel()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LABEL_FONT, "" + fontToString(((CategoryPlot)chart.getPlot()).getRangeAxis().getLabelFont())));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LABEL_COLOR, "" + ((Color)((CategoryPlot)chart.getPlot()).getRangeAxis().getLabelPaint()).getRGB()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_TICK_LABELS_VISIBLE, "" + ((CategoryPlot)chart.getPlot()).getRangeAxis().isTickLabelsVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_TICK_MARKS_VISIBLE, "" + ((CategoryPlot)chart.getPlot()).getRangeAxis().isTickMarksVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_AUTORANGE, "" + ((CategoryPlot)chart.getPlot()).getRangeAxis().isAutoRange()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LOWERBOUND, "" + ((CategoryPlot)chart.getPlot()).getRangeAxis().getLowerBound()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_UPPERBOUND, "" + ((CategoryPlot)chart.getPlot()).getRangeAxis().getUpperBound()));
				
				return result;
			}
			
			@Override
			public ChartColumn[] getValueColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_VALUE_COLUMN, Chart.PROPERTY_VALUE_COLUMN, Number.class)
				};
			}
			@Override
			public ChartColumn[] getDomainColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_DOMAIN_COLUMN, Chart.PROPERTY_DOMAIN_COLUMN, Comparable.class),
						new ChartColumn(Chart.PROPERTY_RANGE_COLUMN, Chart.PROPERTY_RANGE_COLUMN, Comparable.class)
				};
			}
		},
		PieChart {
			@Override
			protected Dataset createDataset(Chart chart) {
				DefaultPieDataset defaultpiedataset = new DefaultPieDataset();
				
				try {
					JTable tbl = chart.getSubForm().getJTable();
					TableColumn cValue = tbl.getColumn(chart.getProperty(PROPERTY_VALUE_COLUMN, String.class));
					TableColumn cDomainAxis = tbl.getColumn(chart.getProperty(PROPERTY_DOMAIN_COLUMN, String.class));
					if (tbl.getModel() instanceof SearchConditionTableModelImpl) {
						defaultpiedataset.setValue("", 0);
						return defaultpiedataset;
					}
					if (tbl.getColumnCount() > 0)
						if (tbl.getRowCount() <= 0) {
							defaultpiedataset.setValue("", 0);
						} else {
							for (int j = 0; j < tbl.getRowCount(); j++) {
								CollectableField vValue = (CollectableField)tbl.getValueAt(j, cValue.getModelIndex());
								CollectableField vXAxis = (CollectableField)tbl.getValueAt(j, cDomainAxis.getModelIndex());
								defaultpiedataset.setValue(vXAxis == null || vXAxis.getValue() == null ? "" : (Comparable)vXAxis.getValue(), vValue == null || vValue.getValue() == null ? 0 : (Number)vValue.getValue());
							}
						}
				} catch (Exception e) {
					//...
				}
				
				return defaultpiedataset;
	        }
			@Override
			public JFreeChart createChart(Chart chart) {
				PieDataset dataset = (PieDataset)createDataset(chart);
				JFreeChart jfreechart = ChartFactory.createPieChart("", dataset, true, false, false);
				ChartUtilities.applyCurrentTheme(jfreechart);
			    
				setCommonChartProperties(chart, jfreechart);

				if (jfreechart.getPlot() == null || !(jfreechart.getPlot() instanceof PiePlot))
					return jfreechart;
				
				// set properties for pie plot
		        
				// nothing to do.
				
				return jfreechart;
			}
			@Override
			public List<Pair<String, String>> getChartProperties(JFreeChart chart) {
				List<Pair<String, String>> result = new ArrayList<Pair<String,String>>();
				
				result.addAll(getCommonChartProperties(chart));

				if (chart.getPlot() == null || !(chart.getPlot() instanceof PiePlot))
					return result;
				
				// set properties for pie plot
				
				// nothing to do.
				
				return result;
			}
			
			@Override
			public ChartColumn[] getValueColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_VALUE_COLUMN, Chart.PROPERTY_VALUE_COLUMN, Number.class)
				};
			}
			@Override
			public ChartColumn[] getDomainColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_DOMAIN_COLUMN, Chart.PROPERTY_DOMAIN_COLUMN, Comparable.class)
				};
			}
		},
		LineChart {
			@Override
			protected Dataset createDataset(Chart chart) {
				DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
				
				try {
					JTable tbl = chart.getSubForm().getJTable();
					TableColumn cValue = tbl.getColumn(chart.getProperty(PROPERTY_VALUE_COLUMN, String.class));
					TableColumn cDomainAxis = tbl.getColumn(chart.getProperty(PROPERTY_DOMAIN_COLUMN, String.class));
					TableColumn cRangeAxis = tbl.getColumn(chart.getProperty(PROPERTY_RANGE_COLUMN, String.class));
					if (tbl.getModel() instanceof SearchConditionTableModelImpl) {
						defaultcategorydataset.addValue(0, "", "");
						return defaultcategorydataset;
					}
					if (tbl.getColumnCount() > 0)
						if (tbl.getRowCount() <= 0) {
							defaultcategorydataset.addValue(0, "", "");
						} else {
							for (int j = 0; j < tbl.getRowCount(); j++) {
								CollectableField vValue = (CollectableField)tbl.getValueAt(j, cValue.getModelIndex());
								CollectableField vDomainAxis = (CollectableField)tbl.getValueAt(j, cDomainAxis.getModelIndex());
								CollectableField vRangeAxis = (CollectableField)tbl.getValueAt(j, cRangeAxis.getModelIndex());
								defaultcategorydataset.addValue(vValue == null ? 0 : (Number)vValue.getValue(),
									vDomainAxis == null || vDomainAxis.getValue() == null ? "" : (Comparable)vDomainAxis.getValue(), vRangeAxis == null || vRangeAxis.getValue() == null ? "" : (Comparable)vRangeAxis.getValue());
							}
						}
				} catch (Exception e) {
					//...
				}
				
				return defaultcategorydataset;
	        }
			@Override
			public JFreeChart createChart(Chart chart) {
				CategoryDataset dataset = (CategoryDataset)createDataset(chart);
				JFreeChart jfreechart = ChartFactory.createLineChart("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);
				ChartUtilities.applyCurrentTheme(jfreechart);
			    
				setCommonChartProperties(chart, jfreechart);

				if (jfreechart.getPlot() == null || !(jfreechart.getPlot() instanceof CategoryPlot))
					return jfreechart;
				
				// set properties for category plot
				((CategoryPlot)jfreechart.getPlot()).setOrientation(
						chart.getProperty(PROPERTY_PLOT_ORIENTATION, String.class, PlotOrientation.HORIZONTAL.toString())
							.equals(PlotOrientation.VERTICAL.toString()) ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL);
				// set properties for domain axis
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setLabel(chart.getProperty(PROPERTY_DOMAIN_LABEL, String.class));
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setLabelFont(chart.getProperty(PROPERTY_DOMAIN_LABEL_FONT, Font.class));
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setLabelPaint(chart.getProperty(PROPERTY_DOMAIN_LABEL_COLOR, Color.class, Color.BLACK));
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setTickLabelsVisible(chart.getProperty(PROPERTY_DOMAIN_TICK_LABELS_VISIBLE, Boolean.class, Boolean.TRUE));
				((CategoryPlot)jfreechart.getPlot()).getDomainAxis().setTickMarksVisible(chart.getProperty(PROPERTY_DOMAIN_TICK_MARKS_VISIBLE, Boolean.class, Boolean.TRUE));
				// set properties for range axis
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setLabel(chart.getProperty(PROPERTY_RANGE_LABEL, String.class));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setLabelFont(chart.getProperty(PROPERTY_RANGE_LABEL_FONT, Font.class));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setLabelPaint(chart.getProperty(PROPERTY_RANGE_LABEL_COLOR, Color.class, Color.BLACK));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setTickLabelsVisible(chart.getProperty(PROPERTY_RANGE_TICK_LABELS_VISIBLE, Boolean.class, Boolean.TRUE));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setTickMarksVisible(chart.getProperty(PROPERTY_RANGE_TICK_MARKS_VISIBLE, Boolean.class, Boolean.TRUE));
				((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setAutoRange(chart.getProperty(PROPERTY_RANGE_AUTORANGE, Boolean.class, Boolean.TRUE));
				if (!((CategoryPlot)jfreechart.getPlot()).getRangeAxis().isAutoRange()) {
					((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setLowerBound(chart.getProperty(PROPERTY_RANGE_LOWERBOUND, Double.class, 0D));
					((CategoryPlot)jfreechart.getPlot()).getRangeAxis().setUpperBound(chart.getProperty(PROPERTY_RANGE_UPPERBOUND, Double.class, 0D));
				}
				
				return jfreechart;
			}
			@Override
			public List<Pair<String, String>> getChartProperties(JFreeChart chart) {
				List<Pair<String, String>> result = new ArrayList<Pair<String,String>>();
				
				result.addAll(getCommonChartProperties(chart));

				if (chart.getPlot() == null || !(chart.getPlot() instanceof PiePlot))
					return result;
				
				// set properties for pie plot
				
				// nothing to do.
				
				return result;
			}
			
			@Override
			public ChartColumn[] getValueColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_VALUE_COLUMN, Chart.PROPERTY_VALUE_COLUMN, Number.class)
				};
			}
			@Override
			public ChartColumn[] getDomainColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_DOMAIN_COLUMN, Chart.PROPERTY_DOMAIN_COLUMN, Comparable.class),
						new ChartColumn(Chart.PROPERTY_RANGE_COLUMN, Chart.PROPERTY_RANGE_COLUMN, Comparable.class)
				};
			}
		},
		TimeSeriesChart {
			@Override
			protected Dataset createDataset(Chart chart) {
				TimeSeriesCollection timeseriesdataset = new TimeSeriesCollection();
				
		       try {
					JTable tbl = chart.getSubForm().getJTable();
					TableColumn cValue = tbl.getColumn(chart.getProperty(PROPERTY_VALUE_COLUMN, String.class));
					TableColumn cDomainAxis = tbl.getColumn(chart.getProperty(PROPERTY_DOMAIN_COLUMN, String.class));
					if (tbl.getModel() instanceof SearchConditionTableModelImpl) {
						return timeseriesdataset;
					}
					if (tbl.getColumnCount() > 0)
						if (tbl.getRowCount() <= 0) {
							; // nothing
						} else {
							final TimeSeries timeSeries = new TimeSeries("", Second.class);
							
							for (int j = 0; j < tbl.getRowCount(); j++) {
								CollectableField vValue = (CollectableField)tbl.getValueAt(j, cValue.getModelIndex());
								CollectableField vXAxis = (CollectableField)tbl.getValueAt(j, cDomainAxis.getModelIndex());
								if (vXAxis != null || vXAxis.getValue() != null)
									timeSeries.addOrUpdate(new Second((Date)vXAxis.getValue()), vValue == null || vValue.getValue() == null ? 0 : (Number)vValue.getValue());
							}
							
							timeseriesdataset.addSeries(timeSeries);
						}
				} catch (Exception e) {
					//...
				}
				
				return timeseriesdataset;
	        }
			@Override
			public JFreeChart createChart(Chart chart) {
				XYDataset dataset = (XYDataset)createDataset(chart);
				JFreeChart jfreechart = ChartFactory.createTimeSeriesChart("", "", "",  dataset, true, false, false);

				ChartUtilities.applyCurrentTheme(jfreechart);
			    
				setCommonChartProperties(chart, jfreechart);

				if (jfreechart.getPlot() == null || !(jfreechart.getPlot() instanceof XYPlot))
					return jfreechart;
				
				// set properties for xy plot
				((XYPlot)jfreechart.getPlot()).setOrientation(
						chart.getProperty(PROPERTY_PLOT_ORIENTATION, String.class, PlotOrientation.HORIZONTAL.toString())
							.equals(PlotOrientation.VERTICAL.toString()) ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL);
				// set properties for domain axis
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setLabel(chart.getProperty(PROPERTY_DOMAIN_LABEL, String.class));
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setLabelFont(chart.getProperty(PROPERTY_DOMAIN_LABEL_FONT, Font.class));
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setLabelPaint(chart.getProperty(PROPERTY_DOMAIN_LABEL_COLOR, Color.class, Color.BLACK));
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setTickLabelsVisible(chart.getProperty(PROPERTY_DOMAIN_TICK_LABELS_VISIBLE, Boolean.class, Boolean.TRUE));
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setTickMarksVisible(chart.getProperty(PROPERTY_DOMAIN_TICK_MARKS_VISIBLE, Boolean.class, Boolean.TRUE));
				// set properties for range axis
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setLabel(chart.getProperty(PROPERTY_RANGE_LABEL, String.class));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setLabelFont(chart.getProperty(PROPERTY_RANGE_LABEL_FONT, Font.class));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setLabelPaint(chart.getProperty(PROPERTY_RANGE_LABEL_COLOR, Color.class, Color.BLACK));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setTickLabelsVisible(chart.getProperty(PROPERTY_RANGE_TICK_LABELS_VISIBLE, Boolean.class, Boolean.TRUE));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setTickMarksVisible(chart.getProperty(PROPERTY_RANGE_TICK_MARKS_VISIBLE, Boolean.class, Boolean.TRUE));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setAutoRange(chart.getProperty(PROPERTY_RANGE_AUTORANGE, Boolean.class, Boolean.TRUE));
				if (!((XYPlot)jfreechart.getPlot()).getRangeAxis().isAutoRange()) {
					((XYPlot)jfreechart.getPlot()).getRangeAxis().setLowerBound(chart.getProperty(PROPERTY_RANGE_LOWERBOUND, Double.class, 0D));
					((XYPlot)jfreechart.getPlot()).getRangeAxis().setUpperBound(chart.getProperty(PROPERTY_RANGE_UPPERBOUND, Double.class, 0D));
				}
				
				return jfreechart;
			}
			@Override
			public List<Pair<String, String>> getChartProperties(JFreeChart chart) {
				List<Pair<String, String>> result = new ArrayList<Pair<String,String>>();
				
				result.addAll(getCommonChartProperties(chart));

				if (chart.getPlot() == null || !(chart.getPlot() instanceof XYPlot))
					return result;
				
				// set properties for xy plot
				result.add(new Pair<String, String>(Chart.PROPERTY_PLOT_ORIENTATION, "" + ((XYPlot)chart.getPlot()).getOrientation().toString()));
				// set properties for domain axis
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LABEL, "" + ((XYPlot)chart.getPlot()).getDomainAxis().getLabel()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LABEL_FONT, "" + fontToString(((XYPlot)chart.getPlot()).getDomainAxis().getLabelFont())));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LABEL_COLOR, "" + ((Color)((XYPlot)chart.getPlot()).getDomainAxis().getLabelPaint()).getRGB()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_TICK_LABELS_VISIBLE, "" + ((XYPlot)chart.getPlot()).getDomainAxis().isTickLabelsVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_TICK_MARKS_VISIBLE, "" + ((XYPlot)chart.getPlot()).getDomainAxis().isTickMarksVisible()));
				// set properties for range axis
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LABEL, "" + ((XYPlot)chart.getPlot()).getRangeAxis().getLabel()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LABEL_FONT, "" + fontToString(((XYPlot)chart.getPlot()).getRangeAxis().getLabelFont())));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LABEL_COLOR, "" + ((Color)((XYPlot)chart.getPlot()).getRangeAxis().getLabelPaint()).getRGB()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_TICK_LABELS_VISIBLE, "" + ((XYPlot)chart.getPlot()).getRangeAxis().isTickLabelsVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_TICK_MARKS_VISIBLE, "" + ((XYPlot)chart.getPlot()).getRangeAxis().isTickMarksVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_AUTORANGE, "" + ((XYPlot)chart.getPlot()).getRangeAxis().isAutoRange()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LOWERBOUND, "" + ((XYPlot)chart.getPlot()).getRangeAxis().getLowerBound()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_UPPERBOUND, "" + ((XYPlot)chart.getPlot()).getRangeAxis().getUpperBound()));
				
				return result;
			}
			
			@Override
			public ChartColumn[] getValueColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_VALUE_COLUMN, Chart.PROPERTY_VALUE_COLUMN, Number.class)
				};
			}
			@Override
			public ChartColumn[] getDomainColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_DOMAIN_COLUMN, Chart.PROPERTY_DOMAIN_COLUMN, Date.class)
				};
			}
		},
		XYSeriesChart {
			@Override
			protected Dataset createDataset(Chart chart) {
				XYSeriesCollection xyseriesdataset = new XYSeriesCollection();
				
		       try {
					JTable tbl = chart.getSubForm().getJTable();
					TableColumn cValue = tbl.getColumn(chart.getProperty(PROPERTY_VALUE_COLUMN, String.class));
					TableColumn cDomainAxis = tbl.getColumn(chart.getProperty(PROPERTY_DOMAIN_COLUMN, String.class));
					if (tbl.getModel() instanceof SearchConditionTableModelImpl) {
						return xyseriesdataset;
					}
					if (tbl.getColumnCount() > 0)
						if (tbl.getRowCount() <= 0) {
							; // nothing
						} else {
							final XYSeries xySeries = new XYSeries("", false, true);
							
							for (int j = 0; j < tbl.getRowCount(); j++) {
								CollectableField vValue = (CollectableField)tbl.getValueAt(j, cValue.getModelIndex());
								CollectableField vXAxis = (CollectableField)tbl.getValueAt(j, cDomainAxis.getModelIndex());
								xySeries.addOrUpdate(vXAxis == null || vXAxis.getValue() == null ? 0 : (Number)vXAxis.getValue(),vValue == null || vValue.getValue() == null ? 0 : (Number)vValue.getValue());
							}
							
							xyseriesdataset.addSeries(xySeries);
						}
				} catch (Exception e) {
					//...
				}
				
				return xyseriesdataset;
	        }
			@Override
			public JFreeChart createChart(Chart chart) {
				XYDataset dataset = (XYDataset)createDataset(chart);
				JFreeChart jfreechart = ChartFactory.createXYLineChart("", "", "",  dataset, PlotOrientation.VERTICAL, true, false, false);

				ChartUtilities.applyCurrentTheme(jfreechart);
			    
				setCommonChartProperties(chart, jfreechart);

				if (jfreechart.getPlot() == null || !(jfreechart.getPlot() instanceof XYPlot))
					return jfreechart;
				
				// set properties for xy plot
				((XYPlot)jfreechart.getPlot()).setOrientation(
						chart.getProperty(PROPERTY_PLOT_ORIENTATION, String.class, PlotOrientation.HORIZONTAL.toString())
							.equals(PlotOrientation.VERTICAL.toString()) ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL);
				// set properties for domain axis
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setLabel(chart.getProperty(PROPERTY_DOMAIN_LABEL, String.class));
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setLabelFont(chart.getProperty(PROPERTY_DOMAIN_LABEL_FONT, Font.class));
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setLabelPaint(chart.getProperty(PROPERTY_DOMAIN_LABEL_COLOR, Color.class, Color.BLACK));
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setTickLabelsVisible(chart.getProperty(PROPERTY_DOMAIN_TICK_LABELS_VISIBLE, Boolean.class, Boolean.TRUE));
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setTickMarksVisible(chart.getProperty(PROPERTY_DOMAIN_TICK_MARKS_VISIBLE, Boolean.class, Boolean.TRUE));
				((XYPlot)jfreechart.getPlot()).getDomainAxis().setAutoRange(chart.getProperty(PROPERTY_DOMAIN_AUTORANGE, Boolean.class, Boolean.TRUE));
				if (!((XYPlot)jfreechart.getPlot()).getDomainAxis().isAutoRange()) {
					((XYPlot)jfreechart.getPlot()).getDomainAxis().setLowerBound(chart.getProperty(PROPERTY_DOMAIN_LOWERBOUND, Double.class, 0D));
					((XYPlot)jfreechart.getPlot()).getDomainAxis().setUpperBound(chart.getProperty(PROPERTY_DOMAIN_UPPERBOUND, Double.class, 0D));
				}
				// set properties for range axis
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setLabel(chart.getProperty(PROPERTY_RANGE_LABEL, String.class));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setLabelFont(chart.getProperty(PROPERTY_RANGE_LABEL_FONT, Font.class));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setLabelPaint(chart.getProperty(PROPERTY_RANGE_LABEL_COLOR, Color.class, Color.BLACK));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setTickLabelsVisible(chart.getProperty(PROPERTY_RANGE_TICK_LABELS_VISIBLE, Boolean.class, Boolean.TRUE));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setTickMarksVisible(chart.getProperty(PROPERTY_RANGE_TICK_MARKS_VISIBLE, Boolean.class, Boolean.TRUE));
				((XYPlot)jfreechart.getPlot()).getRangeAxis().setAutoRange(chart.getProperty(PROPERTY_RANGE_AUTORANGE, Boolean.class, Boolean.TRUE));
				if (!((XYPlot)jfreechart.getPlot()).getRangeAxis().isAutoRange()) {
					((XYPlot)jfreechart.getPlot()).getRangeAxis().setLowerBound(chart.getProperty(PROPERTY_RANGE_LOWERBOUND, Double.class, 0D));
					((XYPlot)jfreechart.getPlot()).getRangeAxis().setUpperBound(chart.getProperty(PROPERTY_RANGE_UPPERBOUND, Double.class, 0D));
				}
				
				return jfreechart;
			}
			@Override
			public List<Pair<String, String>> getChartProperties(JFreeChart chart) {
				List<Pair<String, String>> result = new ArrayList<Pair<String,String>>();
				
				result.addAll(getCommonChartProperties(chart));

				if (chart.getPlot() == null || !(chart.getPlot() instanceof XYPlot))
					return result;
				
				// set properties for xy plot
				result.add(new Pair<String, String>(Chart.PROPERTY_PLOT_ORIENTATION, "" + ((XYPlot)chart.getPlot()).getOrientation().toString()));
				// set properties for domain axis
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LABEL, "" + ((XYPlot)chart.getPlot()).getDomainAxis().getLabel()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LABEL_FONT, "" + fontToString(((XYPlot)chart.getPlot()).getDomainAxis().getLabelFont())));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LABEL_COLOR, "" + ((Color)((XYPlot)chart.getPlot()).getDomainAxis().getLabelPaint()).getRGB()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_TICK_LABELS_VISIBLE, "" + ((XYPlot)chart.getPlot()).getDomainAxis().isTickLabelsVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_TICK_MARKS_VISIBLE, "" + ((XYPlot)chart.getPlot()).getDomainAxis().isTickMarksVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_AUTORANGE, "" + ((XYPlot)chart.getPlot()).getDomainAxis().isAutoRange()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_LOWERBOUND, "" + ((XYPlot)chart.getPlot()).getDomainAxis().getLowerBound()));
				result.add(new Pair<String, String>(Chart.PROPERTY_DOMAIN_UPPERBOUND, "" + ((XYPlot)chart.getPlot()).getDomainAxis().getUpperBound()));
				// set properties for range axis
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LABEL, "" + ((XYPlot)chart.getPlot()).getRangeAxis().getLabel()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LABEL_FONT, "" + fontToString(((XYPlot)chart.getPlot()).getRangeAxis().getLabelFont())));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LABEL_COLOR, "" + ((Color)((XYPlot)chart.getPlot()).getRangeAxis().getLabelPaint()).getRGB()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_TICK_LABELS_VISIBLE, "" + ((XYPlot)chart.getPlot()).getRangeAxis().isTickLabelsVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_TICK_MARKS_VISIBLE, "" + ((XYPlot)chart.getPlot()).getRangeAxis().isTickMarksVisible()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_AUTORANGE, "" + ((XYPlot)chart.getPlot()).getRangeAxis().isAutoRange()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_LOWERBOUND, "" + ((XYPlot)chart.getPlot()).getRangeAxis().getLowerBound()));
				result.add(new Pair<String, String>(Chart.PROPERTY_RANGE_UPPERBOUND, "" + ((XYPlot)chart.getPlot()).getRangeAxis().getUpperBound()));
				
				return result;
			}
			
			@Override
			public ChartColumn[] getValueColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_VALUE_COLUMN, Chart.PROPERTY_VALUE_COLUMN, Number.class)
				};
			}
			@Override
			public ChartColumn[] getDomainColumnDesc() {
				return new ChartColumn[] {
						new ChartColumn(Chart.PROPERTY_DOMAIN_COLUMN, Chart.PROPERTY_DOMAIN_COLUMN, Number.class)
				};
			}
		};

		public abstract JFreeChart createChart(Chart chart);
		protected abstract Dataset createDataset(Chart chart);
		
		public abstract ChartColumn[] getValueColumnDesc();
		public abstract ChartColumn[] getDomainColumnDesc();
		
		public abstract List<Pair<String, String>> getChartProperties(JFreeChart chart);
		
		private static List<Pair<String, String>> getCommonChartProperties(JFreeChart chart) {
			List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
			
			result.add(new Pair<String, String>(Chart.PROPERTY_SHOW_TITLE, "" + (chart.getTitle() != null)));
			if (chart.getTitle() != null) {
				result.add(new Pair<String, String>(Chart.PROPERTY_TITLE, chart.getTitle().getText()));
				result.add(new Pair<String, String>(Chart.PROPERTY_TITLE_FONT, "" + fontToString(chart.getTitle().getFont())));
				result.add(new Pair<String, String>(Chart.PROPERTY_TITLE_COLOR, "" + ((Color)chart.getTitle().getPaint()).getRGB()));
			}
			
			result.add(new Pair<String, String>(Chart.PROPERTY_BACKGROUND, "" + ((Color)chart.getBackgroundPaint()).getRGB()));
			result.add(new Pair<String, String>(Chart.PROPERTY_DRAW_ANTIALIASED, "" + chart.getAntiAlias()));

			result.add(new Pair<String, String>(Chart.PROPERTY_BORDER_VISIBLE, "" + chart.isBorderVisible()));
			if (chart.isBorderVisible()) {
				result.add(new Pair<String, String>(Chart.PROPERTY_BORDER, "" + strokeToString(chart.getBorderStroke())));
				result.add(new Pair<String, String>(Chart.PROPERTY_BORDER_COLOR, "" + ((Color)chart.getBorderPaint()).getRGB()));
			}

			result.add(new Pair<String, String>(Chart.PROPERTY_PLOT_BACKGROUND, "" + ((Color)chart.getPlot().getBackgroundPaint()).getRGB()));

			return result;
		}
		private static void setCommonChartProperties(Chart chart, JFreeChart jfreechart) {
			if (chart.getProperty(PROPERTY_SHOW_TITLE, Boolean.class, Boolean.TRUE)) {
				jfreechart.getTitle().setText(chart.getProperty(PROPERTY_TITLE, String.class));
				jfreechart.getTitle().setFont(chart.getProperty(PROPERTY_TITLE_FONT, Font.class));
				jfreechart.getTitle().setPaint(chart.getProperty(PROPERTY_TITLE_COLOR, Color.class));
			}
			jfreechart.setBackgroundPaint(chart.getProperty(PROPERTY_BACKGROUND, Color.class));
			jfreechart.setAntiAlias(chart.getProperty(PROPERTY_DRAW_ANTIALIASED, Boolean.class, Boolean.TRUE));
			
			jfreechart.setBorderVisible(chart.getProperty(PROPERTY_BORDER_VISIBLE, Boolean.class, Boolean.FALSE));
			if (jfreechart.isBorderVisible()) {
				jfreechart.setBorderStroke(chart.getProperty(PROPERTY_BORDER, Stroke.class));
				jfreechart.setBorderPaint(chart.getProperty(PROPERTY_BORDER_COLOR, Color.class));
			}

			final Plot plot = jfreechart.getPlot();
	        plot.setBackgroundPaint(chart.getProperty(PROPERTY_PLOT_BACKGROUND, Color.class));
		}
	}

	public static interface ChartToolListener extends EventListener {
		void toolbarAction(String actionCommand);
	}

	public static enum ToolbarFunction {
		SAVE {
	        @Override
	        public AbstractButton createButton() {
	        	JButton res = new JButton(Icons.getInstance().getIconSave16());
	        	res.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
	        			"Chart.7","Diagramm speichern"));
	        	res.setActionCommand(name());
		        return res;
	        }
			@Override
			public JMenuItem createMenuItem() {
				JMenuItem res = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
						"Chart.7","Diagramm speichern"), Icons.getInstance().getIconSave16());
				res.setActionCommand(name());
				return res;
			}
        },
		PRINT {
	        @Override
	        public AbstractButton createButton() {
	        	JButton res = new JButton(Icons.getInstance().getIconPrint16());
	        	res.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
	        			"Chart.1","Diagramm drucken"));
	        	res.setActionCommand(ChartPanel.PRINT_COMMAND);
		        return res;
	        }
			@Override
			public JMenuItem createMenuItem() {
				JMenuItem res = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
						"Chart.1","Diagramm drucken"), Icons.getInstance().getIconPrint16());
				res.setActionCommand(ChartPanel.PRINT_COMMAND);
				return res;
			}
        },
		SHOW {
	        @Override
	        public AbstractButton createButton() {
	        	JButton res = new JButton(Icons.getInstance().getIconShowList());
	        	res.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
	        			"Chart.2","Diagrammdaten anzeigen"));
	        	res.setActionCommand(name());
		        return res;
	        }
			@Override
			public JMenuItem createMenuItem() {
				JMenuItem res = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
						"Chart.2","Diagrammdaten anzeigen"), Icons.getInstance().getIconShowList());
				res.setActionCommand(name());
				return res;
			}
        };

		public abstract AbstractButton createButton();
		
		public abstract JMenuItem createMenuItem();

		public static ToolbarFunction fromCommandString(String actionCommand) {
			try {
				return ToolbarFunction.valueOf(actionCommand);
			}
			catch(Exception e) {
				LOG.info("fromCommandString failed on " + actionCommand, e);
				return null;
			}
		}
	}

	public static enum ToolbarFunctionState {
		ACTIVE(true, true),
        DISABLED(false, true),
        HIDDEN(false, false);

		private boolean isEnabled;
		private boolean isVisible;

		private ToolbarFunctionState(boolean isEnabled, boolean isVisible) {
	        this.isEnabled = isEnabled;
	        this.isVisible = isVisible;
        }

		public void set(AbstractButton a) {
			a.setEnabled(isEnabled);
			a.setVisible(isVisible);
		}
		
		public void set(JMenuItem mi) {
			mi.setEnabled(isEnabled);
			mi.setVisible(isVisible);
		}
	}

	private static final Color LAYER_BUSY_COLOR = new Color(128, 128, 128, 128);

	private static final Logger  log = Logger.getLogger(Chart.class);
	
	private JXLayer<JComponent>	 layer;
	private AtomicInteger	     lockCount	= new AtomicInteger(0);

	private HashMap<String, AbstractButton>   toolbarButtons;
	private List<String>					  toolbarOrder;
	private HashMap<String, JMenuItem>		  toolbarMenuItems;

	/**
	 * Can't be final because it must be set to null in close() to avoid memeory leaks. (tp)
	 */
	private JToolBar toolbar;

	/**
	 * Can't be final because it must be set to null in close() to avoid memeory leaks. (tp)
	 */
	private final JPanel contentPane = new JPanel(new BorderLayout());

	private final JComponent scrollPane;
	
	private ChartPanel panel;

	private final String         entityName;
	private final String         foreignKeyFieldToParent;
	
	private final Map<String, Object> properties = new HashMap<String, Object>();

	private final List<LookupListener>		lookupListener = new ArrayList<LookupListener>();

	protected final List<FocusActionListener> lstFocusActionListener
		= new ArrayList<FocusActionListener>();

	private List<ChangeListener> lstchangelistener = new LinkedList<ChangeListener>();

	private List<ChartToolListener> listeners;

	private PopupMenuMouseAdapter popupMenuAdapter;
	
	private SubForm subform;
	
	/**
	 * maps column names to columns
	 */
	private final Map<String, Column> mpColumns = new LinkedHashMap<String, Column>();
	private boolean closed = false;
	private boolean readonly = false;
	private final boolean bFromProperties;
	private final boolean bSearchable;
	
	/**
	 * @param entityName
	 * @param iToolBarOrientation @see JToolbar#setOrientation
	 * @precondition entityName != null
	 * @postcondition this.getForeignKeyFieldToParent() == null
	 */
	public Chart(String sEntityName, boolean useScrollPane, int iToolBarOrientation) {
		this(sEntityName, useScrollPane, iToolBarOrientation, null);
		
		assert this.getForeignKeyFieldToParent() == null;
	}
	/**
	 * @param entityName
	 * @param toolBarOrientation @see JToolbar#setOrientation
	 * @param foreignKeyFieldToParent Needs only be specified if not unique. @see #getForeignKeyFieldToParent()
	 * @precondition entityName != null
	 * @postcondition this.getForeignKeyFieldToParent() == foreignKeyFieldToParent
	 */
	public Chart(String entityName, boolean useScrollPane, int toolBarOrientation, String foreignKeyFieldToParent) {
		this(entityName, useScrollPane, toolBarOrientation, foreignKeyFieldToParent, false, false);
	}

	/**
	 * @param entityName
	 * @param toolBarOrientation @see JToolbar#setOrientation
	 * @param foreignKeyFieldToParent Needs only be specified if not unique. @see #getForeignKeyFieldToParent()
	 * @precondition entityName != null
	 * @postcondition this.getForeignKeyFieldToParent() == foreignKeyFieldToParent
	 */
	public Chart(String entityName, boolean useScrollPane, int toolBarOrientation, String foreignKeyFieldToParent, boolean bFromProperties, boolean bSearchable) {
		super(new GridLayout(1, 1));

		this.bFromProperties = bFromProperties;
		this.bSearchable = bSearchable;
		
		this.subform = new SubForm(entityName, -1, foreignKeyFieldToParent, bFromProperties) {
			private TableModelListener tblmdllistener;
			public void setupTableModelListener() {
				// set all columns visble.
				if (!(getSubformTable().getModel() instanceof CollectableTableModel<?>))
						return;
				
				CollectableEntity colEntity = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(subform.getEntityName());
				List<CollectableEntityField> lstclctefColumns = new ArrayList<CollectableEntityField>();
				for (String sFieldName : colEntity.getFieldNames()) {
					if (!sFieldName.equalsIgnoreCase("genericObject"))
						lstclctefColumns.add(colEntity.getEntityField(sFieldName));
				}
				((CollectableTableModel<?>) getSubformTable().getModel()).setColumns(lstclctefColumns);

				this.tblmdllistener = new TableModelListener() {
					boolean enabled = Chart.this.isEnabled();
					@Override
		            public void tableChanged(TableModelEvent ev) {
						Chart.this.setEnabled((subform.getJTable().getModel() instanceof SearchConditionTableModelImpl) ? false : enabled);
						
						String type = getProperty(PROPERTY_TYPE, String.class);
						if (type != null) {
							JFreeChart chart = ChartFunction.valueOf(type).createChart(Chart.this);
							panel.setChart(chart);
							panel.repaint();
						}
					}
				};
				
				getSubformTable().getModel().addTableModelListener(this.tblmdllistener);
			}

			public void removeTableModelListener() {
				getSubformTable().getModel().removeTableModelListener(this.tblmdllistener);
				this.tblmdllistener = null;
			}
		};
		
		if (useScrollPane)
			this.scrollPane = new JScrollPane();
		else
			this.scrollPane = new JPanel(new BorderLayout());
		
		this.toolbar = UIUtils.createNonFloatableToolBar(toolBarOrientation);

		this.listeners = new ArrayList<ChartToolListener>();

		if (entityName == null) {
			throw new NullArgumentException("entityName");
		}
		this.entityName = entityName;

		String sDataSource = entityName.substring(MasterDataMetaVO.CHART_ENTITY_PREFIX.length()).toLowerCase();
		
		try {
			DatasourceVO datasourceVO = DatasourceDelegate.getInstance().getChartByName(sDataSource);
			if (datasourceVO != null) {
				final List<DatasourceParameterVO> lstParams = 
						DatasourceDelegate.getInstance().getParametersFromXML(datasourceVO.getSource());
				for (Iterator iterator = lstParams.iterator(); iterator.hasNext();) {
					DatasourceParameterVO datasourceParameterVO = (DatasourceParameterVO) iterator.next();
					if (datasourceParameterVO.getParameter().equals("genericObject"))
						iterator.remove();
				}
				if (!lstParams.isEmpty() && !bSearchable && !bFromProperties) {
					JPanel pnl = new JPanel(new BorderLayout());
					ParameterPanel pnlParameters = new ParameterPanel(lstParams, new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							if (e != null && e.getSource() instanceof ParameterPanel) {
								try {
									Map<String, Object> mpParams = new HashMap<String, Object>();
									((ParameterPanel)e.getSource()).fillParameterMap(lstParams, mpParams);
									subform.setMapParams(mpParams);
								} catch (Exception e2) {
									//ignore.
								}
							}
						}
					});
					pnl.add(pnlParameters, BorderLayout.NORTH);
					
					JSplitPane splitPane = new JSplitPane(
							toolBarOrientation == -1 ? JSplitPane.VERTICAL_SPLIT : toolBarOrientation, pnl, scrollPane);
					contentPane.add(splitPane, BorderLayout.CENTER);
				} else
					contentPane.add(scrollPane, BorderLayout.CENTER);
			} else {
				contentPane.add(scrollPane, BorderLayout.CENTER);
			}
		} catch (Exception e) {
			throw new NuclosFatalException("init failed for datasource " + sDataSource, e);
		}
		
		if (toolBarOrientation == -1) {
			this.toolbar.setVisible(false);
		} else {
			this.toolbar.setOrientation(toolBarOrientation);
		}
		this.foreignKeyFieldToParent = foreignKeyFieldToParent;
		
		layer = new JXLayer<JComponent>(contentPane, new TranslucentLockableUI());
		layer.setName("JXLayerGlasspane");
		add(layer);

		this.init(useScrollPane);

		toolbarButtons = new HashMap<String, AbstractButton>();
		toolbarMenuItems = new HashMap<String, JMenuItem>();
		toolbarOrder = new ArrayList<String>();
		for(ToolbarFunction func : ToolbarFunction.values()) {
			AbstractButton button = func.createButton();
			JMenuItem mi = func.createMenuItem();
			toolbarButtons.put(func.name(), button);
			toolbarMenuItems.put(func.name(), mi);
			toolbar.add(button);
			if (func.equals(ToolbarFunction.PRINT)) {
				button.addActionListener(panel);
				mi.addActionListener(panel);
			} else {
				button.addActionListener(this);
				mi.addActionListener(this);
			}
			toolbarOrder.add(func.name());
		}
		
		subform.setReadOnly(true);
		setEnabled(!bFromProperties && !bSearchable);
		
		addChartToolListener(new ChartToolListener() {
			
			@Override
			public void toolbarAction(String actionCommand) {
				if (actionCommand.equals(ToolbarFunction.SHOW.name())) {
					actionCommandShow();
				} else if (actionCommand.equals(ToolbarFunction.SAVE.name())) {
					actionCommandSave();
				}
			}
		});
		
		assert this.getForeignKeyFieldToParent() == foreignKeyFieldToParent;
	}
	
	private void actionCommandShow() {
		final JOptionPane optpn = new JOptionPane(subform, JOptionPane.PLAIN_MESSAGE, JOptionPane.CLOSED_OPTION);

		// perform the dialog:
		final JDialog dlg = optpn.createDialog(panel, "Chart-Daten anzeigen");
		dlg.setModal(true);
		dlg.setResizable(true);
		dlg.pack();
		dlg.setLocationRelativeTo(panel);
		dlg.setVisible(true);
	}
	private void actionCommandSave() {
		try {
			JFileChooser fileChooser = new JFileChooser();
	    	FileFilter filter = new FileNameExtensionFilter(
	    			SpringLocaleDelegate.getInstance().getMessage("filenameextensionfilter.1",
	    			"Bildformate") + " (*.svg, *.png, *.jpg, *.jpeg)", "svg", "png", "jpg", "jpeg"); // @todo i18n
	        fileChooser.addChoosableFileFilter(filter);

	        int option = fileChooser.showSaveDialog(this);
	        if (option == JFileChooser.APPROVE_OPTION) {
	            String filename = fileChooser.getSelectedFile().getPath();
	            if (fileChooser.getSelectedFile().getName().lastIndexOf(".") == -1) {
	               filename = filename + ".png";
	            }
	            
	            if (filename.toLowerCase().endsWith(".png"))
		            ChartUtilities.saveChartAsPNG(new File(filename), getChartPanel().getChart(),
		                    getChartPanel().getWidth(), getChartPanel().getHeight());
	            else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg"))
		            ChartUtilities.saveChartAsJPEG(new File(filename), getChartPanel().getChart(),
		            		getChartPanel().getWidth(), getChartPanel().getHeight());
	            else if (filename.toLowerCase().endsWith(".svg")) {
	            	// Get a DOMImplementation and create an XML document
	                DOMImplementation domImpl =
	                    GenericDOMImplementation.getDOMImplementation();
	                Document document = domImpl.createDocument(null, "svg", null);

	                // Create an instance of the SVG Generator
	                SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

	                // draw the chart in the SVG generator
	                JFreeChart chart = getChartPanel().getChart();
	                chart.draw(svgGenerator, getChartPanel().getBounds());

	                // Write svg file
	                OutputStream outputStream = new FileOutputStream(filename);
	                Writer out = new OutputStreamWriter(outputStream, "UTF-8");
	                svgGenerator.stream(out, true /* use css */);						
	                outputStream.flush();
	                outputStream.close();
	            }
	        }
		} catch (Exception e) {
			throw new NuclosFatalException(e.getMessage());
		}
	}
	
	public SubForm getSubForm() {
		return this.subform;
	}
	
	@Override
	public final void close() {
		// Close is needed for avoiding memory leaks
		// If you want to change something here, please consult me (tp).  
		if (!closed) {
			LOG.debug("close(): " + this);
			
			// Partial fix for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7079260
			popupMenuAdapter = null;

			toolbar = null;
			
			lstchangelistener.clear();
			lstFocusActionListener.clear();
			listeners.clear();
			
			closed = true;
		}
	}
	
	public void addChartToolListener(ChartToolListener l) {
		listeners.add(l);
	}

	public void removeChartToolListener(ChartToolListener l) {
		listeners.remove(l);
	}

	public void removeAllChartToolListeners() {
		listeners.clear();
	}
	
	public void actionPerformed(String actionCommand) {
		if(actionCommand != null)
			for(ChartToolListener l : new ArrayList<ChartToolListener>(listeners))
				l.toolbarAction(actionCommand);
	}

	@Override
    public void actionPerformed(ActionEvent e) {
		String actionCommand = StringUtils.nullIfEmpty(e.getActionCommand());
		actionPerformed(actionCommand);
	}

	public void setToolbarFunctionState(ToolbarFunction func, ToolbarFunctionState state) {
		setToolbarFunctionState(func.name(), state);
	}

	public void setToolbarFunctionState(String toolbarActionCommand, ToolbarFunctionState state) {
		AbstractButton button = toolbarButtons.get(toolbarActionCommand);
		if(button != null)
			state.set(button);
		JMenuItem mi = toolbarMenuItems.get(toolbarActionCommand);
		if (mi != null)
			state.set(mi);
	}

	public void addToolbarFunction(String actionCommand, AbstractButton button, JMenuItem mi, int pos) {
		if(toolbarButtons.containsKey(actionCommand))
			toolbar.remove(toolbarButtons.get(actionCommand));
		toolbarButtons.put(actionCommand, button);
		button.setActionCommand(actionCommand);
		button.addActionListener(this);
		toolbar.add(button, pos);
		toolbar.validate();
		
		if (toolbarMenuItems.containsKey(actionCommand))
			toolbarOrder.remove(actionCommand);
		toolbarMenuItems.put(actionCommand, mi);
		mi.setActionCommand(actionCommand);
		mi.addActionListener(this);
		toolbarOrder.add(pos, actionCommand);
	}


	//--- needed by the wysiwyg editor only -----------------------------------
	public void addToolbarButtonMouseListener(MouseListener l) {
		for(AbstractButton b : toolbarButtons.values())
			b.addMouseListener(l);
	}

	public void removeToolbarButtonMouseListener(MouseListener l) {
		for(AbstractButton b : toolbarButtons.values())
			b.removeMouseListener(l);
	}
	
	public ChartPanel getChartPanel() {
		return panel;
	}

	public JToolBar getToolbar() {
		return toolbar;
	}

	public int getToolbarOrientation() {
		return toolbar.getOrientation();
	}

	public Rectangle getToolbarBounds() {
		return toolbar.getBounds();
	}

	public Set<String> getToolbarFunctions() {
		return toolbarButtons.keySet();
	}

	public AbstractButton getToolbarButton(String function) {
		return toolbarButtons.get(function);
	}
	//--- end needed by the wysiwyg editor only --------------------------------


	// class TranslucentLockableUI --->
	private class TranslucentLockableUI extends LockableUI {
		@Override
		protected void paintLayer(Graphics2D g2, JXLayer<? extends JComponent> l) {
			super.paintLayer(g2, l);
			if(isLocked()) {
				g2.setColor(LAYER_BUSY_COLOR);
				g2.fillRect(0, 0, l.getWidth(), l.getHeight());
			}
		}
	} // class TranslucentLockableUI

	public void setLockedLayer() {
		if(lockCount.incrementAndGet() == 1)
			if(layer != null && !((LockableUI) layer.getUI()).isLocked())
				((LockableUI) layer.getUI()).setLocked(true);
	}

	public void restoreUnLockedLayer() {
		if(lockCount.decrementAndGet() == 0)
			if(layer != null && ((LockableUI) layer.getUI()).isLocked())
				((LockableUI) layer.getUI()).setLocked(false);
	}

	public void forceUnlockFrame() {
		lockCount.set(0);
		((LockableUI) layer.getUI()).setLocked(false);
	}

	private void init(boolean useScrollPane) {
		contentPane.add(toolbar,
			toolbar.getOrientation() == JToolBar.HORIZONTAL
			? BorderLayout.NORTH
			: BorderLayout.WEST);

		// Configure table
		JFreeChart chart = new JFreeChart(new Plot() {
			
			@Override
			public String getPlotType() {
				return null;
			}
			
			@Override
			public void draw(Graphics2D arg0, Rectangle2D arg1, Point2D arg2,
					PlotState arg3, PlotRenderingInfo arg4) {
			}
		});

		panel = new ChartPanel(chart, false, false, true, false, false);
		if (bFromProperties) {
			panel.setPopupMenu(null);
		}
		
		if (!useScrollPane)
			scrollPane.add(panel, BorderLayout.CENTER); 
		else {
			((JScrollPane)scrollPane).getViewport().setBackground(panel.getBackground());
			((JScrollPane)scrollPane).getViewport().setView(panel);

			JLabel labCorner = new JLabel();
			labCorner.setEnabled(false);
			labCorner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
			labCorner.setBackground(Color.LIGHT_GRAY);
			((JScrollPane)scrollPane).setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, labCorner);	
		}
	}
	
	/**
	 * do not store items permanent!
	 * @param result
	 */
	public void addToolbarMenuItems(List<JComponent> result) {
		for (String actionCommand : toolbarOrder) {
			result.add(toolbarMenuItems.get(actionCommand));
		}
	}

	/**
	 * @return the name of this chart's entity.
	 * @postcondition result != null
	 */
	public String getEntityName() {
		return this.entityName;
	}

	/**
	 * @return the foreign key field that references the parent object. May be null for convenience.
	 */
	public final String getForeignKeyFieldToParent() {
		return this.foreignKeyFieldToParent;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		enabled = enabled && !readonly;
		super.setEnabled(enabled);
		
		if (bFromProperties || bSearchable) {
			enabled = false;
		}
		setToolbarFunctionState(ToolbarFunction.SAVE,
				enabled ? ToolbarFunctionState.ACTIVE : ToolbarFunctionState.DISABLED);
		setToolbarFunctionState(ToolbarFunction.PRINT,
				enabled ? ToolbarFunctionState.ACTIVE : ToolbarFunctionState.DISABLED);
		setToolbarFunctionState(ToolbarFunction.SHOW,
				enabled ? ToolbarFunctionState.ACTIVE : ToolbarFunctionState.DISABLED);
	}
	
	public void setReadOnly(boolean readonly) {
		this.readonly = readonly;
	}
	
	public boolean isReadOnly() {
		return readonly;
	}
	
	/**
	 * @param listener
	 */
	public synchronized void addChangeListener(ChangeListener listener) {
		this.lstchangelistener.add(listener);
	}

	/**
	 * @param listener
	 */
	public synchronized void removeChangeListener(ChangeListener listener) {
		this.lstchangelistener.remove(listener);
	}

	/**
	 * fires a <code>ChangeEvent</code> whenever the model of this <code>Chart</code> changes.
	 */
	public synchronized void fireStateChanged() {
		if(layer == null || (layer != null && !((LockableUI) layer.getUI()).isLocked())){
			final ChangeEvent ev = new ChangeEvent(this);
			for (ChangeListener changelistener : lstchangelistener) {
				changelistener.stateChanged(ev);
			}
		}
	}

	/**
	 * @param column
	 */
	public void addColumn(Column column) {
		this.mpColumns.put(column.getName(), column);
	}

	public Column getColumn(String sColumnName) {
		return this.mpColumns.get(sColumnName);
	}

	public String getColumnLabel(String sColumnName) {
		final Column column = this.getColumn(sColumnName);
		return (column != null) ? column.getLabel() : null;
	}

	public PopupMenuMouseAdapter getPopupMenuAdapter() {
		return popupMenuAdapter;
	}

	public void setPopupMenuAdapter(PopupMenuMouseAdapter popupMenuAdapter) {
		this.popupMenuAdapter = popupMenuAdapter;
	}
	
	/**
	 * @param sName
	 * @return the value of the dynamic property with the given name, if any.
	 */
	public <T> T getProperty(String sName, Class<T> clazz) {
		return getProperty(sName, clazz, null);
	}
	/**
	 * @param sName
	 * @return the value of the dynamic property with the given name, if any.
	 */
	public <T> T getProperty(String sName, Class<T> clazz, T defaultValue) {
		try {
			if (clazz.equals(Color.class)) {
				String p = getProperty(sName, String.class, "");
				if (p == null)
					return (T)defaultValue;
				return (T)colorFromString(p);
			} else if (clazz.equals(Font.class)) {
				String p = getProperty(sName, String.class, "");
				if (p == null)
					return (T)defaultValue;
				return (T)fontFromString(p);
			} else if (clazz.equals(Stroke.class)) {
				String p = getProperty(sName, String.class, "");
				if (p == null)
					return (T)defaultValue;
				return (T)strokeFromString(p);
			} else if (clazz.equals(Integer.class)) {
				String p = getProperty(sName, String.class, null);
				if (p == null)
					return (T)defaultValue;
				return (T)new Integer(p);
			} else if (clazz.equals(Double.class)) {
				String p = getProperty(sName, String.class, null);
				if (p == null)
					return (T)defaultValue;
				return (T)new Double(p);
			} else if (clazz.equals(Boolean.class)) {
				String p = getProperty(sName, String.class, null);
				if (p == null)
					return (T)defaultValue;
				return (T)new Boolean(p);
			}
			if (properties.get(sName) == null)
				return (T)defaultValue;
			return (T)properties.get(sName);	
		} catch (Exception e) {
			return (T)defaultValue;
		}
	}
	
	private static Font fontFromString(String font) {
		return Font.decode(font);
	}
	private static String fontToString(Font font) {
		String  strStyle;

        if (font.isBold()) {
            strStyle = font.isItalic() ? "bolditalic" : "bold";
        } else {
            strStyle = font.isItalic() ? "italic" : "plain";
        }

        return font.getName() + "-" + strStyle + "-" + font.getSize();
    }
	private static Color colorFromString(String color) {
		return new Color(Integer.parseInt(color));
	}
	private static Stroke strokeFromString(String stroke) {
		Stroke result = null;
		String[] str = stroke.split("-"); 
		if (str.length == 6) {
			result = new BasicStroke(Float.parseFloat(str[0]),
					Integer.parseInt(str[1]),Integer.parseInt(str[2]),
					Float.parseFloat(str[3]),
					stringToFloatArray(str[4], ","),
					Float.parseFloat(str[5]));
		}
		return result;
	}
	private static String strokeToString(Stroke stroke) {
		if (stroke instanceof BasicStroke) {
			return ((BasicStroke)stroke).getLineWidth() + "-" + 
					((BasicStroke)stroke).getEndCap() + "-" + 
					((BasicStroke)stroke).getLineJoin() + "-" + 
					((BasicStroke)stroke).getMiterLimit() + "-" + 
					floatArrayToString(((BasicStroke)stroke).getDashArray(), ",") + "-" +
					((BasicStroke)stroke).getDashPhase();
		}
		return "";
	}
	private static float[] stringToFloatArray(String arg, String separator) {
		String[] floats = arg.split(separator);
		float[] result = new float[floats.length];
		for (int i = 0; i < floats.length; i++) {
			result[i] = Float.parseFloat(floats[i]);
		}
	    return result;
	}
	private static String floatArrayToString(float[] a, String separator) {
	    StringBuffer result = new StringBuffer();
	    if (a.length > 0) {
	        result.append(a[0]);
	        for (int i=1; i<a.length; i++) {
	            result.append(separator);
	            result.append(a[i]);
	        }
	    }
	    return result.toString();
	}

	/**
	 * sets the dynamic property with the given name to the given value.
	 * Properties can be used to customize an individual <code>CollectableComponent</code>.
	 * @param sName
	 * @param oValue
	 * @postcondition LangUtils.equals(this.getProperty(sName), oValue)
	 */
	public void setProperty(String sName, Object oValue) {
		properties.put(sName, oValue);
	}

	/**
	 * @return the Map of properties for this <code>CollectableComponent</code>.
	 * @postcondition result != null
	 */
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
	/**
	 * @return the name of the foreign key field referencing the parent entity
	 * @postcondition result != null
	 */
	public final String getForeignKeyFieldName(String sParentEntityName, CollectableEntity clcte) {

		// Use the field referencing the parent entity from the chart, if any:
		String result = this.getForeignKeyFieldToParent();

		if (result == null) {
			// Default: Find the field referencing the parent entity from the meta data.
			// If more than one field applies, throw an exception:
			for (String sFieldName : clcte.getFieldNames()) {
				if (sParentEntityName.equals(clcte.getEntityField(sFieldName).getReferencedEntityName())) {
					if (result == null) {
						// this is the foreign key field:
						result = sFieldName;
					}
					else {
						final String sMessage = SpringLocaleDelegate.getInstance().getMessage(
								"Chart.4","Das Diagramm f\u00fcr die Entit\u00e4t \"{0}\" enth\u00e4lt mehr als ein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t \"{1}\" referenziert:\n\t{2}\n\t{3}\nBitte geben Sie das Feld im Layout explizit an.", clcte.getName(), sParentEntityName, result, sFieldName);
						throw new CommonFatalException(sMessage);
					}
				}
			}
		}

		if (result == null) {
			throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage(
					"Chart.3","Das Diagramm f\u00fcr die Entit\u00e4t \"{0}\" enth\u00e4lt kein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t \"{1}\" referenziert.\nBitte geben Sie das Feld im Layout explizit an.", clcte.getName(), sParentEntityName));
		}
		assert result != null;
		return result;
	}

	/**
     * @param lookupListener the LookupListener to remove
     */
    public void removeLookupListener(LookupListener lookupListener) {
	    this.lookupListener.remove(lookupListener);
    }

	/**
     * @param lookupListener the LookupListener to add
     */
    public void addLookupListener(LookupListener lookupListener) {
	    this.lookupListener.add(lookupListener);
    }

    public void addFocusActionListener(FocusActionListener fal) {
    	lstFocusActionListener.add(fal);
    }

    public void removeFocusActionListener(FocusActionListener fal) {
    	lstFocusActionListener.remove(fal);
    }

    public List<FocusActionListener> getFocusActionLister() {
    	return lstFocusActionListener;
    }
	/**
	 * A column in a <code>Chart</code>.
	 */
	public static class Column {
		private final String sName;
		private String sLabel;
		
		private CollectableFieldsProvider valuelistprovider;

		public Column(String sName) {
			this(sName, null);
		}

		public Column(String sName, String sLabel) {
			this.sName = sName;
			this.sLabel = sLabel;
		}

		public String getName() {
			return this.sName;
		}

		public String getLabel() {
			return this.sLabel;
		}

		public void setLabel(String label) {
			this.sLabel = label;
		}

		@Override
		public boolean equals(Object oValue) {
			if (this == oValue) {
				return true;
			}
			if (!(oValue instanceof Column)) {
				return false;
			}
			return LangUtils.equals(getName(), ((Column) oValue).getName());
		}

		@Override
		public int hashCode() {
			return LangUtils.hashCode(getName());
		}

		public void setValueListProvider(CollectableFieldsProvider valuelistprovider) {
			this.valuelistprovider = valuelistprovider;
		}

		public CollectableFieldsProvider getValueListProvider() {
			return this.valuelistprovider;
		}
	}	// inner class Column

}	// class Chart
