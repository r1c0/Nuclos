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
package org.nuclos.client.layout.wysiwyg.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_CHART;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyChartProperty;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBoolean;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.datatransfer.TransferableComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.mouselistener.PropertiesDisplayMouseListener;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGPropertySet;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.Chart;
import org.nuclos.client.ui.collect.Chart.Column;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.component.model.DefaultCollectableComponentModelProvider;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * 
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:stefan.geiling@novabit.de">stefan.geiling</a>
 * @version 01.00.00
 */
public class WYSIWYGChart extends JLayeredPane implements WYSIWYGComponent, MouseListener, WYSIWYGScriptComponent {

	private boolean isInitialLoading = true;

	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_ENTITY = PROPERTY_LABELS.ENTITY;
	public static final String PROPERTY_FOREIGNKEY = PROPERTY_LABELS.FOREIGNKEY;
	public static final String PROPERTY_TOOLBARORIENTATION = PROPERTY_LABELS.TOOLBARORIENTATION;
	public static final String PROPERTY_ENABLED = PROPERTY_LABELS.ENABLED;
	public static final String PROPERTY_OPAQUE = PROPERTY_LABELS.OPAQUE;
	public static final String PROPERTY_PROPERTIES = PROPERTY_LABELS.COLLECTABLECOMPONENTPROPERTY;

	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{{PROPERTY_NAME, ATTRIBUTE_NAME}, {PROPERTY_ENTITY, ATTRIBUTE_ENTITY}, {PROPERTY_ENABLED, ATTRIBUTE_ENABLED}, {PROPERTY_FOREIGNKEY, ATTRIBUTE_FOREIGNKEYFIELDTOPARENT}, {PROPERTY_TOOLBARORIENTATION, ATTRIBUTE_TOOLBARORIENTATION}};

	public static final String[][] PROPERTY_VALUES_STATIC = new String[][]{{PROPERTY_TOOLBARORIENTATION, ATTRIBUTEVALUE_HORIZONTAL, ATTRIBUTEVALUE_VERTICAL, ATTRIBUTEVALUE_HIDE}};

	private static final String[] PROPERTY_NAMES = new String[]{PROPERTY_NAME, PROPERTY_ENTITY, PROPERTY_FOREIGNKEY, PROPERTY_TOOLBARORIENTATION, PROPERTY_PREFFEREDSIZE, PROPERTY_ENABLED, PROPERTY_BORDER, PROPERTY_PROPERTIES};

	private static final PropertyClass[] PROPERTY_CLASSES = new PropertyClass[]{
			new PropertyClass(PROPERTY_NAME, String.class),
			new PropertyClass(PROPERTY_ENTITY, String.class),
			new PropertyClass(PROPERTY_FOREIGNKEY, String.class),
			new PropertyClass(PROPERTY_ENABLED, boolean.class),
			new PropertyClass(PROPERTY_TOOLBARORIENTATION, String.class),
			new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class),
			new PropertyClass(PROPERTY_BORDER, Border.class),
			new PropertyClass(PROPERTY_PROPERTIES, PropertyChartProperty.class)};

	private static final PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
			new PropertySetMethod(PROPERTY_NAME, "setName"),
			new PropertySetMethod(PROPERTY_ENTITY, "setEntity"),
			new PropertySetMethod(PROPERTY_ENABLED, "setEnabled"),
			new PropertySetMethod(PROPERTY_FOREIGNKEY, "setForeignkey"),
			new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize"),
			new PropertySetMethod(PROPERTY_BORDER, "setBorder"),
			new PropertySetMethod(PROPERTY_DESCRIPTION, "setToolTipText"),
			new PropertySetMethod(PROPERTY_TOOLBARORIENTATION, "setToolbarOrientation"),
			new PropertySetMethod(PROPERTY_PROPERTIES, "setProperties"),
			};

	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[]{
			new PropertyFilter(PROPERTY_NAME, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_ENTITY, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_ENABLED, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_FOREIGNKEY, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_TOOLBARORIENTATION, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_PREFFEREDSIZE, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_BORDER, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_PROPERTIES, STANDARD_MODE | EXPERT_MODE)
			};
	
	public static final String[][] PROPERTY_VALUES_FROM_METAINFORMATION = new String[][]{{PROPERTY_ENTITY, WYSIWYGMetaInformation.META_ENTITY_NAMES}, {PROPERTY_FOREIGNKEY, WYSIWYGMetaInformation.META_ENTITY_FIELD_NAMES_REFERENCING}};

	public static final String[][] PROPERTIES_TO_SCRIPT_ELEMENTS = new String[][]{};

	private ComponentProperties properties;

	private WYSIWYGMetaInformation meta;
	
	private Rectangle lastViewPosition = null;
	
	private Chart chart;

	private CollectableTableModel<Collectable> model;

	private JLabel message = new JLabel();

	private HashMap<String, WYSIWYGChartColumn> columns = new HashMap<String, WYSIWYGChartColumn>();

	/**
	 * 
	 * @param meta the metainformation to be set
	 */
	public WYSIWYGChart(WYSIWYGMetaInformation meta) {
		this.meta = meta;
		this.message = new JLabel(COLLECTABLE_CHART.LABEL_NO_ENTITY_ASSIGNED);
		this.message.setToolTipText(COLLECTABLE_CHART.LABEL_NO_ENTITY_ASSIGNED);
		this.message.setHorizontalAlignment(JLabel.CENTER);
		this.message.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.message.addMouseListener(this);
		this.setLayout(new BorderLayout());
	}
	
	@Override
	protected void finalize() {
		// close Chart support
		chart.close();
		chart = null;
	}
	
	public Chart getChart() {
		return this.chart;
	}
	
	/**
	 * 
	 * @return the entity the chart is related to
	 */
	public String getEntityName() {
		return (String) getProperties().getProperty(PROPERTY_ENTITY).getValue();
	}
	
	/**
	 * 
	 * @return a {@link Collection} of {@link WYSIWYGChartColumn}
	 */
	public Collection<WYSIWYGChartColumn> getColumns() {
		return columns.values();
	}
	
	/**
	 * 
	 * @param name the name of the column field
	 * @param column the {@link WYSIWYGChartColumn} for the name
	 */
	public void addColumn(String name, WYSIWYGChartColumn column) {
		this.columns.put(name, column);
		// setChartFromProperties()
		// ... seems unnecessary . It is called from LayoutMLContentHandler once per column,
		// but setChartFromProperties() is also called afterwards in finalizeInitialLoading().
		// It doesn't contribute to the LayoutMLContentHandler's chartColumnMissing catch-clause,
		// because setChartFromProperties() handles all exception on its own.
	}
	
	public void removeColumn(String name){
		this.columns.remove(name);
	}
	
	/**
	 * 
	 * @return a {@link Dimension} with the size for the {@link JToolBar} 
	 */
	public Dimension getToolbarDimension() {
		if(chart != null) {
			Rectangle bounds = chart.getToolbarBounds();
			return new Dimension(bounds.width, bounds.height);
		}
		return null;
	}
	
	public WYSIWYGMetaInformation getMetaInformation() {
		return this.meta;
	}
	
	/**
	 * Setup of the {@link WYSIWYGChart} does a complete render and refresh 
	 */
	public void setChartFromProperties() {
		if (this.isInitialLoading()){
			return;
		}
		//NUCLEUSINT-926
		if (chart != null)
			lastViewPosition = chart.getVisibleRect();
		
		this.removeAll();
		if (chart != null) {
			chart.removeToolbarButtonMouseListener(this);
		}
		//NUCLEUSINT-803
		String entityname = null;
		String foreignkey = null;
		if(getProperties() != null) {
			entityname = (String) getProperties().getProperty(PROPERTY_ENTITY).getValue();
			foreignkey = (String) getProperties().getProperty(PROPERTY_FOREIGNKEY).getValue();
		}
		try {
			if (entityname != null) {
				CollectableComponentModelProvider clctmodelprovider = getCollectableComponentModelProvider();

				int orientation = JToolBar.VERTICAL;
				String sorientation = (String) getProperties().getProperty(PROPERTY_TOOLBARORIENTATION).getValue();
				if (!StringUtils.isNullOrEmpty(sorientation)) {
					orientation = sorientation.equals(ATTRIBUTEVALUE_HIDE) ? -1 : 
						(sorientation.equals(ATTRIBUTEVALUE_VERTICAL) ? JToolBar.VERTICAL : JToolBar.HORIZONTAL);
				}

				this.chart = new Chart(entityname, orientation, foreignkey, true, false);

				boolean bEnabled = (Boolean) getProperties().getProperty(PROPERTY_ENABLED).getValue(boolean.class, this);
				this.chart.setEnabled(bEnabled);

				final Border border = (Border) getProperties().getProperty(PROPERTY_BORDER).getValue(Border.class, this);
				chart.setBorder(border);
				
				PropertyChartProperty chartProperty = (PropertyChartProperty)getProperties().getProperty(PROPERTY_PROPERTIES);
				if (chartProperty != null) {
					WYSIYWYGProperty wysiywygProperty = (WYSIYWYGProperty)chartProperty.getValue();
					for (WYSIYWYGPropertySet propertySet : wysiywygProperty.properties) {
						this.chart.setProperty(propertySet.getPropertyName(), propertySet.getPropertyValue());
					}
				}
				
				NuclosCollectControllerFactory.getInstance().newDetailsSubFormController(chart.getSubForm(), meta.getCollectableEntity().getName(), clctmodelprovider, new MainFrameTab(), this, Preferences.userRoot().node("tmp"), new EntityPreferences(), null);

				if (chart.getSubForm().getJTable().getModel() instanceof CollectableTableModel) {
					this.model = (CollectableTableModel<Collectable>) chart.getSubForm().getJTable().getModel();
				} else {
					throw new NuclosFatalException("Unexpected table model type.");
				}

				if (chart.getSubForm().getJTable().getModel() instanceof AbstractTableModel) {
					try {
						((AbstractTableModel)chart.getSubForm().getJTable().getModel()).fireTableDataChanged();
					} catch (Exception e) {
						e.printStackTrace();
						// ignore here.
					}
				}
				
				for (int i = 0; i < model.getColumnCount(); i++) {
					CollectableEntityField field = model.getCollectableEntityField(i);
					//NUCLEUSINT-401 there is no need to setup the columns every time... this does reset the properties and destroys changes...
					if (columns.get(field.getName()) == null) {
						boolean ignore = false;
						if (NuclosEOField.getByField(field.getName()) != null) {
							switch (NuclosEOField.getByField(field.getName())) {
							case CHANGEDAT :
							case CHANGEDBY :
							case CREATEDAT : 
							case CREATEDBY : 
							case LOGGICALDELETED : 
							case ORIGIN :
							case SYSTEMIDENTIFIER : 
							case PROCESS : 
							case STATE : 
							case STATEICON :
							case STATENUMBER : 
								ignore = true;
							}
						}
						if (ignore) {
							continue;
						}
												
						WYSIWYGChartColumn column = new WYSIWYGChartColumn(this, field);
						column.setProperties(PropertyUtils.getEmptyProperties(column, this.meta));

						PropertyValueString value = new PropertyValueString(field.getName());
						try {
							column.getProperties().setProperty(WYSIWYGSubFormColumn.PROPERTY_NAME, value, null);
							column.getProperties().setProperty(WYSIWYGSubFormColumn.PROPERTY_DEFAULTVALUES, new PropertyValueBoolean(true), null);
						} catch (CommonBusinessException e) {
							Errors.getInstance().showExceptionDialog(null, e);
						}
						columns.put(field.getName(), column);
					}
				}
				
				if (getParentEditor() != null) {
					final TableLayoutUtil layoutUtil = getParentEditor().getTableLayoutUtil();
					for (Map.Entry<String, WYSIWYGChartColumn> e : columns.entrySet()) {
						final WYSIWYGChartColumn wc = e.getValue();
						String label = (String) wc.getProperties().getProperty(WYSIWYGChartColumn.PROPERTY_LABEL).getValue();
						Column c = new Chart.Column(e.getKey(), label);
	
						//NUCLEUSINT-556
						wc.addMouseListener(new PropertiesDisplayMouseListener(wc, layoutUtil));
						chart.addColumn(c);
					}
	
					this.add(this.chart, BorderLayout.CENTER);

					chart.getToolbar().addMouseListener(this);
					
					try {
						//NUCLEUSINT-265
						final PropertiesDisplayMouseListener pdml = new PropertiesDisplayMouseListener(this, layoutUtil);
						chart.getToolbar().addMouseListener(pdml);
						chart.getChartPanel().addMouseListener(pdml);
						((JViewport) chart.getParent()).addMouseListener(pdml);
					} catch (ClassCastException e) {
						
					}
					chart.addToolbarButtonMouseListener(this);
					for(Object s : chart.getToolbarFunctions())
						chart.setToolbarFunctionState((String)s, Chart.ToolbarFunctionState.DISABLED);
					
					//NUCLEUSINT-926
					if (lastViewPosition != null)
						chart.scrollRectToVisible(lastViewPosition);
				}
			} else {
				this.chart = null;
				this.add(this.message, BorderLayout.CENTER);
			}
		} catch (Exception ex) {
			this.chart = null;
			this.message.setText(ex.getMessage());
			this.message.setToolTipText(ex.getMessage());
			this.add(this.message, BorderLayout.CENTER);
		} 
		addDragGestureListener(this, this);
	}

	
	public static class MouseDragGestureListener implements DragGestureListener {
		private final WYSIWYGComponent component;
		private final WYSIWYGComponent wysiwygComponent;
		
		public MouseDragGestureListener(final WYSIWYGComponent component, final WYSIWYGComponent wysiwygComponent) {
			this.component = component;
			this.wysiwygComponent = wysiwygComponent;
		}
		
		public JComponent getComponent() {
			return (JComponent)component;
		}
		
		public void dragGestureRecognized(DragGestureEvent e) {
   	    	WYSIWYGComponent item = (wysiwygComponent != null) ? wysiwygComponent: findWYSIWYGComponent(e.getComponent());
   	 		if (item != null) {	 
   	 			final WYSIWYGLayoutEditorPanel parent = component.getParentEditor();
   	 			if (parent != null && !parent.getTableLayoutPanel().isResizeDragPerformed()) {
   	 				parent.setComponentToMove(item);
   	 				parent.getTableLayoutPanel().initGlassPane((Component)item, e.getDragOrigin());
   	 				
    	 			try {
    	 				// NUCLEUSINT-496
	 					e.startDrag(null, new TransferableComponent(item), new DragSourceListener() {
							@Override
							public void dropActionChanged(DragSourceDragEvent dsde) {
							}
							@Override
							public void dragOver(DragSourceDragEvent dsde) {
							}
							@Override
							public void dragExit(DragSourceEvent dse) {
							}
							@Override
							public void dragEnter(DragSourceDragEvent dsde) {
							}
							@Override
							public void dragDropEnd(DragSourceDropEvent dsde) {
			   	 				parent.getTableLayoutPanel().hideGlassPane();
							}
						});
    	 			} catch (InvalidDnDOperationException ex) {
    	 				//do nothing
    	 			}
   	 			}
   	 		}
   	     }
	}

	public void addDragGestureListener(final WYSIWYGComponent component, final WYSIWYGComponent wysiwygComponent) {
		MouseDragGestureListener dgListener = new MouseDragGestureListener(component, wysiwygComponent);

   	  	// component, action, listener
   	  	if (component instanceof JComponent)
   		  addDragGestureListener((JComponent)component, dgListener);
	}


	private void addDragGestureListener(final JComponent c,final MouseDragGestureListener dgListener) {
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
			     c, DnDConstants.ACTION_COPY_OR_MOVE, dgListener );
		/*Component[] comps = c.getComponents();
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof JComponent)
				addDragGestureListener((JComponent)comps[i], dgListener, ml);
		}*/
		if (c == this) {
			if (message != null)
				addDragGestureListener(message, dgListener);
			if (chart != null) 
				addDragGestureListener(chart.getToolbar(), dgListener);
		}
	}
	
	private static WYSIWYGComponent findWYSIWYGComponent(Object o) {
		if (o instanceof WYSIWYGComponent)
			return (WYSIWYGComponent)o;
		
		if (o instanceof Container) {
			return findWYSIWYGComponent(((Container)o).getParent());
		}
		return null;
	}

	/**
	 * 
	 * @return {@link CollectableComponentModelProvider}
	 */
	private CollectableComponentModelProvider getCollectableComponentModelProvider() {
		HashMap<String, CollectableComponentModel> map = new HashMap<String, CollectableComponentModel>();

		for (String s : meta.getCollectableEntity().getFieldNames()) {
			map.put(s, CollectableComponentModel.newCollectableComponentModel(meta.getCollectableEntity().getEntityField(s), false));
		}
		return new DefaultCollectableComponentModelProvider(map);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getProperties()
	 */
	@Override
	public ComponentProperties getProperties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperties(org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties)
	 */
	@Override
	public void setProperties(ComponentProperties properties) {
		this.properties = properties;
		this.setChartFromProperties();
	}
	public void setProperties(PropertyChartProperty properties) {
		try {
			this.properties.setProperty(PROPERTY_PROPERTIES, properties, null);
		} catch (CommonBusinessException e) {
			// ignore...
		}
		this.setChartFromProperties();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperty(java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue, java.lang.Class)
	 */
	@Override
	public void setProperty(String property, PropertyValue<?> value, Class<?> valueClass) throws CommonBusinessException {
		properties.setProperty(property, value, valueClass);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyNames()
	 */
	@Override
	public String[] getPropertyNames() {
		return PROPERTY_NAMES;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertySetMethods()
	 */
	@Override
	public PropertySetMethod[] getPropertySetMethods() {
		return PROPERTY_SETMETHODS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyClasses()
	 */
	@Override
	public PropertyClass[] getPropertyClasses() {
		return PROPERTY_CLASSES;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getParentEditor()
	 */
	@Override
	public WYSIWYGLayoutEditorPanel getParentEditor() {
		if (super.getParent() instanceof TableLayoutPanel) {
			return (WYSIWYGLayoutEditorPanel) super.getParent().getParent();
		}

		//throw new CommonFatalException(ERROR_MESSAGES.PARENT_NO_WYSIWYG);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyAttributeLink()
	 */
	@Override
	public String[][] getPropertyAttributeLink() {
		return PROPERTIES_TO_LAYOUTML_ATTRIBUTES;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getAdditionalContextMenuItems(int)
	 */
	@Override
	public List<JMenuItem> getAdditionalContextMenuItems(final int xClick) {
		List<JMenuItem> list = new ArrayList<JMenuItem>();

		return list;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesFromMetaInformation()
	 */
	@Override
	public String[][] getPropertyValuesFromMetaInformation() {
		return PROPERTY_VALUES_FROM_METAINFORMATION;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesStatic()
	 */
	@Override
	public String[][] getPropertyValuesStatic() {
		return PROPERTY_VALUES_STATIC;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
		
		
	};

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyFilters()
	 */
	@Override
	public PropertyFilter[] getPropertyFilters() {
		return PROPERTY_FILTERS;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public synchronized void addMouseListener(MouseListener l) {
		listenerList.add(MouseListener.class, l);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#removeMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public synchronized void removeMouseListener(MouseListener l) {
		listenerList.remove(MouseListener.class, l);
	}

	/**
	 * 
	 * @param entityname
	 */
	public void setEntity(String entityname) {
		setChartFromProperties();
	}

	/**
	 * 
	 * @param foreignkey
	 */
	public void setForeignkey(String foreignkey) {
		setChartFromProperties();
	}

	/**
	 * 
	 * @param orientation
	 */
	public void setToolbarOrientation(String orientation) {
		setChartFromProperties();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (chart != null) {
			chart.setEnabled(enabled);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public synchronized void mouseClicked(MouseEvent e) {
		for(MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mouseClicked(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color bg) {
		if (chart != null) {
			chart.setBackground(bg);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setBorder(javax.swing.border.Border)
	 */
	@Override
	public void setBorder(Border border) {
		if (chart != null) {
			chart.setBorder(border);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		for (MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mouseEntered(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		for (MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mouseExited(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		for (MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mousePressed(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		for (MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mouseReleased(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getLayoutMLRulesIfCapable()
	 */
	@Override
	public LayoutMLRules getLayoutMLRulesIfCapable() {
		return new LayoutMLRules();
	}

	/**
	 * This Method draws a small red box on the Component to indicate existing {@link LayoutMLRules}
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

	public boolean isInitialLoading() {
		return isInitialLoading;
	}
	
	public void finalizeInitialLoading() {
		this.isInitialLoading = false;
		this.setChartFromProperties();
	}

	@Override
	public String[][] getPropertyScriptElementLink() {
		return PROPERTIES_TO_SCRIPT_ELEMENTS;
	}
}
