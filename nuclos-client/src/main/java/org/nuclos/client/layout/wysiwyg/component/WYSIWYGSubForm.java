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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.event.TableColumnModelExtListener;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_SUBFORM;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COMPONENT_POPUP;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent.PropertyClass;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent.PropertyFilter;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent.PropertySetMethod;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBoolean;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueFont;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueInitialSortingOrder;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueInteger;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueValuelistProvider;
import org.nuclos.client.layout.wysiwyg.datatransfer.TransferableComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.LayoutMLRuleEditorDialog;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.mouselistener.PropertiesDisplayMouseListener;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGInitialSortingOrder;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.Column;
import org.nuclos.client.ui.collect.SubFormFilter;
import org.nuclos.client.ui.collect.ToolTipsTableHeader;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.component.model.DefaultCollectableComponentModelProvider;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModelImpl;
import org.nuclos.client.ui.event.TableColumnModelAdapter;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * 
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class WYSIWYGSubForm extends JLayeredPane implements WYSIWYGComponent, MouseListener, WYSIWYGScriptComponent {
	
	private boolean isInitialLoading = true;

	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_ENTITY = PROPERTY_LABELS.ENTITY;
	public static final String PROPERTY_FOREIGNKEY = PROPERTY_LABELS.FOREIGNKEY;
	public static final String PROPERTY_TOOLBARORIENTATION = PROPERTY_LABELS.TOOLBARORIENTATION;
	public static final String PROPERTY_ENABLED = PROPERTY_LABELS.ENABLED;
	public static final String PROPERTY_OPAQUE = PROPERTY_LABELS.OPAQUE;
	public static final String PROPERTY_UNIQUEMASTERCOLUMN = PROPERTY_LABELS.UNIQUEMASTERCOLUMN;
	public static final String PROPERTY_CONTROLLERTYPE = PROPERTY_LABELS.CONTROLLERTYPE;
	public static final String PROPERTY_INITIALSORTINGORDER = PROPERTY_LABELS.INITIALSORTINGORDER;
	//NUCLEUSINT-390
	public static final String PROPERTY_PARENT_SUBFORM = PROPERTY_LABELS.PARENT_SUBFORM;
	public static final String PROPERTY_NEW_ENABLED = PROPERTY_LABELS.NEW_ENABLED;
	public static final String PROPERTY_EDIT_ENABLED = PROPERTY_LABELS.EDIT_ENABLED;
	public static final String PROPERTY_DELETE_ENABLED = PROPERTY_LABELS.DELETE_ENABLED;
	public static final String PROPERTY_CLONE_ENABLED = PROPERTY_LABELS.CLONE_ENABLED;
	public static final String PROPERTY_DYNAMIC_CELL_HEIGHTS_DEFAULT = PROPERTY_LABELS.DYNAMIC_CELL_HEIGHTS_DEFAULT;

	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{{PROPERTY_NAME, ATTRIBUTE_NAME}, {PROPERTY_ENTITY, ATTRIBUTE_ENTITY}, {PROPERTY_FOREIGNKEY, ATTRIBUTE_FOREIGNKEYFIELDTOPARENT}, {PROPERTY_TOOLBARORIENTATION, ATTRIBUTE_TOOLBARORIENTATION}, {PROPERTY_ENABLED, ATTRIBUTE_ENABLED}, {PROPERTY_UNIQUEMASTERCOLUMN, ATTRIBUTE_UNIQUEMASTERCOLUMN}, {PROPERTY_CONTROLLERTYPE, ATTRIBUTE_CONTROLLERTYPE}, {PROPERTY_PARENT_SUBFORM, ATTRIBUTE_PARENTSUBFORM}, {PROPERTY_DYNAMIC_CELL_HEIGHTS_DEFAULT, ATTRIBUTE_DYNAMIC_CELL_HEIGHTS_DEFAULT}};

	public static final String[][] PROPERTY_VALUES_STATIC = new String[][]{{PROPERTY_TOOLBARORIENTATION, ATTRIBUTEVALUE_HORIZONTAL, ATTRIBUTEVALUE_VERTICAL, ATTRIBUTEVALUE_HIDE}};

	private static final String[] PROPERTY_NAMES = new String[]{PROPERTY_NAME, 
		PROPERTY_FONT, PROPERTY_ENTITY, PROPERTY_FOREIGNKEY, PROPERTY_TOOLBARORIENTATION, PROPERTY_PREFFEREDSIZE, PROPERTY_ENABLED, PROPERTY_DYNAMIC_CELL_HEIGHTS_DEFAULT, PROPERTY_BACKGROUNDCOLOR, PROPERTY_BORDER, PROPERTY_UNIQUEMASTERCOLUMN, PROPERTY_CONTROLLERTYPE, PROPERTY_PARENT_SUBFORM,
		PROPERTY_NEW_ENABLED, PROPERTY_EDIT_ENABLED, PROPERTY_DELETE_ENABLED, PROPERTY_CLONE_ENABLED};

	private static final PropertyClass[] PROPERTY_CLASSES = new PropertyClass[]{
			new PropertyClass(PROPERTY_NAME, String.class),
			new PropertyClass(PROPERTY_ENTITY, String.class),
			new PropertyClass(PROPERTY_FOREIGNKEY, String.class),
			new PropertyClass(PROPERTY_TOOLBARORIENTATION, String.class),
			new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class),
			new PropertyClass(PROPERTY_ENABLED, boolean.class),
			new PropertyClass(PROPERTY_BACKGROUNDCOLOR, Color.class),
			new PropertyClass(PROPERTY_BORDER, Border.class),
			new PropertyClass(PROPERTY_UNIQUEMASTERCOLUMN, String.class),
			new PropertyClass(PROPERTY_CONTROLLERTYPE, String.class),
			new PropertyClass(PROPERTY_INITIALSORTINGORDER, WYSIWYGInitialSortingOrder.class),
			//NUCLEUSINT-390
			new PropertyClass(PROPERTY_PARENT_SUBFORM, String.class),
			new PropertyClass(PROPERTY_NEW_ENABLED, NuclosScript.class),
			new PropertyClass(PROPERTY_EDIT_ENABLED, NuclosScript.class),
			new PropertyClass(PROPERTY_DELETE_ENABLED, NuclosScript.class),
			new PropertyClass(PROPERTY_CLONE_ENABLED, NuclosScript.class),
			new PropertyClass(PROPERTY_DYNAMIC_CELL_HEIGHTS_DEFAULT, boolean.class),
			new PropertyClass(PROPERTY_FONT, Font.class)};

	private static final PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
			new PropertySetMethod(PROPERTY_NAME, "setName"),
			new PropertySetMethod(PROPERTY_ENTITY, "setEntity"),
			new PropertySetMethod(PROPERTY_FOREIGNKEY, "setForeignkey"),
			new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize"),
			new PropertySetMethod(PROPERTY_ENABLED, "setEnabled"),
			new PropertySetMethod(PROPERTY_BACKGROUNDCOLOR, "setBackground"),
			new PropertySetMethod(PROPERTY_BORDER, "setBorder"),
			new PropertySetMethod(PROPERTY_FONT, "setFont"), 
			new PropertySetMethod(PROPERTY_DESCRIPTION, "setToolTipText"),
			new PropertySetMethod(PROPERTY_TOOLBARORIENTATION, "setToolbarOrientation"),
			new PropertySetMethod(PROPERTY_INITIALSORTINGORDER, "setInitialSortingOrder")
			};

	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[]{
			new PropertyFilter(PROPERTY_NAME, ENABLED),
			new PropertyFilter(PROPERTY_ENTITY, ENABLED),
			new PropertyFilter(PROPERTY_FOREIGNKEY, ENABLED),
			new PropertyFilter(PROPERTY_TOOLBARORIENTATION, ENABLED),
			new PropertyFilter(PROPERTY_PREFFEREDSIZE, ENABLED),
			new PropertyFilter(PROPERTY_ENABLED, ENABLED),
			new PropertyFilter(PROPERTY_BACKGROUNDCOLOR, ENABLED),
			new PropertyFilter(PROPERTY_BORDER, ENABLED),
			new PropertyFilter(PROPERTY_UNIQUEMASTERCOLUMN, ENABLED),
			new PropertyFilter(PROPERTY_CONTROLLERTYPE, ENABLED),
			new PropertyFilter(PROPERTY_INITIALSORTINGORDER, DISABLED),
			//NUCLEUSINT-390
			new PropertyFilter(PROPERTY_PARENT_SUBFORM, ENABLED),
			new PropertyFilter(PROPERTY_NEW_ENABLED, ENABLED),
			new PropertyFilter(PROPERTY_EDIT_ENABLED, ENABLED),
			new PropertyFilter(PROPERTY_DELETE_ENABLED, ENABLED),
			new PropertyFilter(PROPERTY_FONT, ENABLED),
			new PropertyFilter(PROPERTY_CLONE_ENABLED, ENABLED),
			new PropertyFilter(PROPERTY_DYNAMIC_CELL_HEIGHTS_DEFAULT, ENABLED)
			};
	
	//NUCLEUSINT-413 unique mastercolumn is filled with the foreign keys defined for the used entity
	//NUCLEUSINT-390 parent subform
	public static final String[][] PROPERTY_VALUES_FROM_METAINFORMATION = new String[][]{{PROPERTY_ENTITY, WYSIWYGMetaInformation.META_ENTITY_NAMES}, {PROPERTY_FOREIGNKEY, WYSIWYGMetaInformation.META_ENTITY_FIELD_NAMES_REFERENCING},  {PROPERTY_UNIQUEMASTERCOLUMN, WYSIWYGMetaInformation.META_ENTITY_FIELD_NAMES_REFERENCING},  {PROPERTY_PARENT_SUBFORM, WYSIWYGMetaInformation.META_POSSIBLE_PARENT_SUBFORMS}};

	public static final String[][] PROPERTIES_TO_SCRIPT_ELEMENTS = new String[][]{{PROPERTY_NEW_ENABLED, ELEMENT_NEW_ENABLED}, 
		{PROPERTY_EDIT_ENABLED, ELEMENT_EDIT_ENABLED}, 
		{PROPERTY_DELETE_ENABLED, ELEMENT_DELETE_ENABLED}, 
		{PROPERTY_CLONE_ENABLED, ELEMENT_CLONE_ENABLED} };

	private ComponentProperties properties;

	private LayoutMLRules subformColumnRules = new LayoutMLRules();

	private WYSIWYGMetaInformation meta;
	
	private Rectangle lastViewPosition = null;

	private SubForm subform;
	
	private SortableCollectableTableModel<Collectable> model;

	private JLabel message = new JLabel();

	private HashMap<String, WYSIWYGSubFormColumn> columns = new HashMap<String, WYSIWYGSubFormColumn>();

	private EventListenerList listenerList = new EventListenerList();
	
	/**
	 * 
	 * @param meta the metainformation to be set
	 */
	public WYSIWYGSubForm(WYSIWYGMetaInformation meta) {
		this.meta = meta;
		this.message = new JLabel(COLLECTABLE_SUBFORM.LABEL_NO_ENTITY_ASSIGNED);
		this.message.setToolTipText(COLLECTABLE_SUBFORM.LABEL_NO_ENTITY_ASSIGNED);
		this.message.setHorizontalAlignment(JLabel.CENTER);
		this.message.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.message.addMouseListener(this);
		this.setLayout(new BorderLayout());
	}
	
	@Override
	protected void finalize() {
		// close Subform support
		subform.close();
		subform = null;
	}
	
	/**
	 * 
	 * @return the entity the subform is related to
	 */
	public String getEntityName() {
		return (String) getProperties().getProperty(PROPERTY_ENTITY).getValue();
	}
	
	/**
	 * 
	 * @return a {@link Collection} of {@link WYSIWYGSubFormColumn}
	 */
	public Collection<WYSIWYGSubFormColumn> getColumns() {
		return columns.values();
	}
	
	public List<WYSIWYGSubFormColumn> getColumnsInOrder() {
		List<WYSIWYGSubFormColumn> orderedColumns = new ArrayList<WYSIWYGSubFormColumn>(getColumns());
		Collections.sort(orderedColumns, new Comparator<WYSIWYGSubFormColumn>() {
			@Override
			public int compare(WYSIWYGSubFormColumn c1, WYSIWYGSubFormColumn c2) {
				return c1.getRelativeOrder() - c2.getRelativeOrder(); 
			}
		});
		return orderedColumns;
	}

	/**
	 * 
	 * @param name the name of the column field
	 * @param column the {@link WYSIWYGSubFormColumn} for the name
	 */
	public void addColumn(String name, WYSIWYGSubFormColumn column) {
		column.setRelativeOrder(this.columns.size());
		this.columns.put(name, column);
		// setSubFormFromProperties()
		// ... seems unnecessary . It is called from LayoutMLContentHandler once per column,
		// but setSubFormFromProperties() is also called afterwards in finalizeInitialLoading().
		// It doesn't contribute to the LayoutMLContentHandler's subformColumnMissing catch-clause,
		// because setSubFormFromProperties() handles all exception on its own.
	}
	
	public void removeColumn(String name){
		this.columns.remove(name);
	}
	
	/**
	 * 
	 * @return a {@link Dimension} with the size for the {@link JToolBar} 
	 */
	public Dimension getToolbarDimension() {
		if(subform != null) {
			Rectangle bounds = subform.getToolbarBounds();
			return new Dimension(bounds.width, bounds.height);
		}
		return null;
	}
	/**
	 * 
	 * @param x the x position of the cursor for finding the column
	 * @return null if there is no {@link WYSIWYGSubFormColumn} under the mouse, otherwise the {@link WYSIWYGSubFormColumn}
	 */
	public WYSIWYGSubFormColumn getColumnHeaderAtPoint(Point p) {
		if (subform != null) {
			if (subform.getJTable().getTableHeader().getBounds().contains(p))
			return getColumnAtX(p.x);
		}
		return null;		
	}
	
	/**
	 * 
	 * @param x the x position of the cursor for finding the column
	 * @return null if there is no {@link WYSIWYGSubFormColumn} under the mouse, otherwise the {@link WYSIWYGSubFormColumn}
	 */
	public WYSIWYGSubFormColumn getColumnAtX(int x) {
		if (subform != null) {
			//NUCLEUSINT-355
			int posx = 0;
			if (ATTRIBUTEVALUE_VERTICAL.equals(((PropertyValueString)getProperties().getProperty(PROPERTY_TOOLBARORIENTATION)).getValue())){
				// the left side for columns is beside the toolbar
				if (x < getToolbarDimension().width)
					return null;
			} else {
				int mousey = java.awt.MouseInfo.getPointerInfo().getLocation().y;
				int subformy = subform.getLocationOnScreen().y;
				int toolbarheight = subform.getToolbarBounds().height;
				subformy = subformy + toolbarheight;
				
				if (mousey < subformy) {
					return null;
				}
			} 
			
			for (int i = 0; i < subform.getJTable().getColumnModel().getColumnCount(); i++) {
				TableColumn tableColumn = subform.getJTable().getColumnModel().getColumn(i);
				if (posx < x && x < posx + tableColumn.getWidth()) {
					for (WYSIWYGSubFormColumn column : getColumns()) {
						if (column.getEntityField().getName().equals(tableColumn.getIdentifier()))
							return column;
					}
				}
				posx = posx + tableColumn.getWidth();
			}
		}
		return null;
	}

	/**
	 * Setup of the {@link WYSIWYGSubForm} does a complete render and refresh 
	 */
	public void setSubFormFromProperties() {
		if (this.isInitialLoading()){
			return;
		}
		//NUCLEUSINT-926
		if (subform != null)
			lastViewPosition = subform.getJTable().getVisibleRect();
		
		this.removeAll();
		if (subform != null) {
			subform.removeToolbarButtonMouseListener(this);
			subform.getJTable().getTableHeader().removeMouseListener(this);
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

				this.subform = new SubForm(entityname, orientation, foreignkey, true);

				boolean bEnabled = (Boolean) getProperties().getProperty(PROPERTY_ENABLED).getValue(boolean.class, this);
				this.subform.setEnabled(bEnabled);

				final String sControllerType = (String) getProperties().getProperty(PROPERTY_CONTROLLERTYPE).getValue();
				subform.setControllerType(sControllerType);

				final String sUniqueMasterColumnName = (String) getProperties().getProperty(PROPERTY_UNIQUEMASTERCOLUMN).getValue();
				subform.setUniqueMasterColumnName(sUniqueMasterColumnName);

				final Color background = (Color) getProperties().getProperty(PROPERTY_BACKGROUNDCOLOR).getValue(Color.class, this);
				subform.setBackground(background);

				final Border border = (Border) getProperties().getProperty(PROPERTY_BORDER).getValue(Border.class, this);
				subform.setBorder(border);

				final WYSIWYGInitialSortingOrder sortingOrder = (WYSIWYGInitialSortingOrder) getProperties().getProperty(PROPERTY_INITIALSORTINGORDER).getValue(WYSIWYGInitialSortingOrder.class, this);
				if (sortingOrder != null) {
					subform.setInitialSortingOrder(sortingOrder.getName(), sortingOrder.getSortingOrder());
				}

				NuclosCollectControllerFactory.getInstance().newDetailsSubFormController(subform, meta.getCollectableEntity().getName(), clctmodelprovider, new MainFrameTab(), this, Preferences.userRoot().node("tmp"), new EntityPreferences(), null);

				if (subform.getJTable().getModel() instanceof SortableCollectableTableModel) {
					this.model = (SortableCollectableTableModel<Collectable>) subform.getJTable().getModel();
					
					this.model.addSortingListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							SortKey firstSortKey = CollectionUtils.getFirst(model.getSortKeys());
							if (firstSortKey == null) {
								return;
							} else {
								String fieldName = model.getCollectableEntityField(firstSortKey.getColumn()).getName();
								boolean ascending = firstSortKey.getSortOrder() == SortOrder.ASCENDING;
								WYSIWYGInitialSortingOrder sortingOrder = new WYSIWYGInitialSortingOrder(fieldName, ascending ? LayoutMLConstants.ATTRIBUTEVALUE_ASCENDING : LayoutMLConstants.ATTRIBUTEVALUE_DESCENDING);
								if (properties != null) {
									PropertyValueInitialSortingOrder value = (PropertyValueInitialSortingOrder) properties.getProperty(PROPERTY_INITIALSORTINGORDER);
									if (value != null) {
										value.setValue(sortingOrder);
										try {
											properties.setProperty(PROPERTY_INITIALSORTINGORDER, value, null);
											setInitialSortingOrder(sortingOrder);
										} catch (CommonBusinessException e1) {
											Errors.getInstance().showExceptionDialog(null, e1);
										}
									} else {
										value = new PropertyValueInitialSortingOrder();
										value.setValue(sortingOrder);
										try {
											properties.setProperty(PROPERTY_INITIALSORTINGORDER, value, null);
											setInitialSortingOrder(sortingOrder);
										} catch (CommonBusinessException e1) {
											Errors.getInstance().showExceptionDialog(null, e1);
										}
									}
								}
							}
						}
					});
				} else {
					throw new NuclosFatalException("Unexpected table model type.");
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
												
						WYSIWYGSubFormColumn column = new WYSIWYGSubFormColumn(this, field);
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
				
				final TableLayoutUtil layoutUtil = getParentEditor().getTableLayoutUtil();
				for (Map.Entry<String, WYSIWYGSubFormColumn> e : columns.entrySet()) {
					final WYSIWYGSubFormColumn wc = e.getValue();
					String label = (String) wc.getProperties().getProperty(WYSIWYGSubFormColumn.PROPERTY_LABEL).getValue();
					Integer columnWidth = (Integer) wc.getProperties().getProperty(WYSIWYGSubFormColumn.PROPERTY_COLUMNWIDTH).getValue();
					String sNextFocusComponent = (String) wc.getProperties().getProperty(WYSIWYGSubFormColumn.PROPERTY_NEXTFOCUSCOMPONENT).getValue();
					Column c = new SubForm.Column(e.getKey(), label, null, true, true, true, 10, 10, columnWidth, sNextFocusComponent);

					//NUCLEUSINT-556
					wc.addMouseListener(new PropertiesDisplayMouseListener(wc, layoutUtil));
					subform.addColumn(c);
				}
				
				final JTable table = subform.getJTable();
				// fire change to update column labels
				table.tableChanged(new TableModelEvent(this.subform.getSubformRowHeader().getHeaderTable().getModel(), 
						TableModelEvent.HEADER_ROW));

				this.add(this.subform, BorderLayout.CENTER);

				subform.getToolbar().addMouseListener(this);


				for (MouseListener ml : table.getTableHeader().getMouseListeners()) {
					table.getTableHeader().removeMouseListener(ml);
				}
				table.getTableHeader().addMouseListener(this);
				try {
					//NUCLEUSINT-265
					final PropertiesDisplayMouseListener pdml = new PropertiesDisplayMouseListener(this, layoutUtil);
					subform.getSubformTable().addMouseListener(pdml);
					((JViewport) table.getParent()).addMouseListener(pdml);
					((JPanel) table.getTableHeader().getParent()).addMouseListener(pdml);
				} catch (ClassCastException e) {
					
				}
				subform.addToolbarButtonMouseListener(this);
				for(String s : subform.getToolbarFunctions())
					subform.setToolbarFunctionState(s, SubForm.ToolbarFunctionState.DISABLED);
				
				//NUCLEUSINT-926
				if (lastViewPosition != null)
					table.scrollRectToVisible(lastViewPosition);
				
				SubFormFilter subFormFilter = this.subform.getSubFormFilter();
				if (subFormFilter != null)
					subFormFilter.removeFiltering();
				subform.resetDefaultColumnWidths();
				
				table.getColumnModel().addColumnModelListener(new SubFormTableColumnModelListener(false));
				if (subform.getSubformRowHeader() != null) {
					subform.getSubformRowHeader().getHeaderTable().getColumnModel().addColumnModelListener(new SubFormTableColumnModelListener(true));
				}
			} else {
				this.subform = null;
				this.add(this.message, BorderLayout.CENTER);
			}
		} catch (Exception ex) {
			this.subform = null;
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
			if (subform != null) 
				addDragGestureListener(subform.getToolbar(), dgListener);
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
	 * Note: This listener implements SwingX's {@link TableColumnModelExtListener} to track width changes.
	 * In order to work, the column table model must support this SwingX extension! 
	 */
	private class SubFormTableColumnModelListener extends TableColumnModelAdapter implements TableColumnModelExtListener {
		
		private final boolean fixed;
		
		public SubFormTableColumnModelListener(boolean fixed) {
			this.fixed = fixed;
		}
		@Override
		public void columnPropertyChange(PropertyChangeEvent evt) {
			if ("preferredWidth".equals(evt.getPropertyName()) && subform.isUseCustomColumnWidths()) {
				TableColumn tc = (TableColumn) evt.getSource();
				WYSIWYGSubFormColumn column = findWYSIWYGSubFormColumnFor(tc);
				if (column != null) {
					// TODO: we skip columns with an empty width because we can't 
					// distinguish automatic and user-driven changes at the moment 
					Object currentValue = column.getProperties().getProperty(PROPERTY_LABELS.COLUMNWIDTH).getValue();
					if (currentValue != null) {
						try {
							column.setProperty(PROPERTY_LABELS.COLUMNWIDTH, new PropertyValueInteger(tc.getPreferredWidth()), int.class);
						} catch (CommonBusinessException e1) {
							Errors.getInstance().showExceptionDialog(null, e1);
						}
					}
				}
			}
		}
		@Override
		public void columnMoved(TableColumnModelEvent e) {
			if (e.getFromIndex() != e.getToIndex())
				updateRelativeColumnOrder();
		}
		
		@Override
		public void columnAdded(TableColumnModelEvent e) {
			updateRelativeColumnOrder();
		}
		
		@Override
		public void columnRemoved(TableColumnModelEvent e) {
			updateRelativeColumnOrder();
		}
	}
	
	private void updateRelativeColumnOrder() {
		List<TableColumn> tableColumns = new ArrayList<TableColumn>();
		if (subform.getSubformRowHeader() != null) {
			CollectionUtils.addAll(tableColumns, subform.getSubformRowHeader().getHeaderTable().getColumnModel().getColumns());
		}
		int fixed = tableColumns.size();
		CollectionUtils.addAll(tableColumns, subform.getJTable().getColumnModel().getColumns());
		for (int i = 0, n = tableColumns.size(); i < n; i++) {
			TableColumn tc = tableColumns.get(i);
			WYSIWYGSubFormColumn column = findWYSIWYGSubFormColumnFor(tc);
			if (column != null) {
				column.setRelativeOrder(i);
				// if (i < fixed)
				//     set fixed property of column !!!
			}
		}
	}
	
	private WYSIWYGSubFormColumn findWYSIWYGSubFormColumnFor(TableColumn tc) {
		return columns.get("" + tc.getIdentifier());
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

	/**
	 * 
	 * @param initialSortingOrder
	 */
	public void setInitialSortingOrder(WYSIWYGInitialSortingOrder initialSortingOrder) {
		if (initialSortingOrder != null) {
			if (model != null) {
				for (int i = 0; i < model.getColumnCount(); i++) {
					if (model.getCollectableEntityField(i).getName().equals(initialSortingOrder.getName())) {
						boolean ascending = false;
						if (LayoutMLConstants.ATTRIBUTEVALUE_ASCENDING.equals(initialSortingOrder.getSortingOrder()))
							ascending = true;

						((SortableCollectableTableModelImpl<?>) model).restoreSortingOrder(i, ascending);
						//NUCLEUSINT-563 no change descriptor - no content change (evtl related to NUCLEUSINT-149)
						if (((WYSIWYGComponent) this).getParentEditor().getTableLayoutPanel().getEditorChangeDescriptor() != null)
							((WYSIWYGComponent) this).getParentEditor().getTableLayoutPanel().getEditorChangeDescriptor().setContentChanged();
					}
				}
			}
		}
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
		this.setSubFormFromProperties();
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

		throw new CommonFatalException(ERROR_MESSAGES.PARENT_NO_WYSIWYG);
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
	 * @see javax.swing.JComponent#setFont(java.awt.Font)
	 */
	@Override
	public void setFont(Font font) {
		if (subform != null) {
			subform.setFont(font);
		}
	}

	@Override
	public Font getFont() {
		if (subform != null) {
			return subform.getFont();
		}
		return super.getFont();
	}
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getAdditionalContextMenuItems(int)
	 */
	@Override
	public List<JMenuItem> getAdditionalContextMenuItems(final int xClick) {
		List<JMenuItem> list = new ArrayList<JMenuItem>();

		WYSIWYGSubFormColumn column = getColumnAtX(xClick);
		if (column != null) {
			JMenuItem addLayoutMLRule = new JMenuItem(COMPONENT_POPUP.LABEL_EDIT_RULES_FOR_COMPONENT);
			addLayoutMLRule.setActionCommand(column.getName());
			addLayoutMLRule.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String sourceColumn = e.getActionCommand();
					addLayoutMLRuleToComponent(sourceColumn);
				}
			});

			list.add(addLayoutMLRule);
		}

		return list;
	}

	/**
	 * 
	 * @param sourceColumn
	 */
	protected void addLayoutMLRuleToComponent(String sourceColumn) {
		for (WYSIWYGSubFormColumn column : getColumns()) {
			if (column.getName().equals(sourceColumn)) {
				LayoutMLRuleEditorDialog ruleDialog = new LayoutMLRuleEditorDialog(column, getParentEditor());
				if (ruleDialog.getExitStatus() == LayoutMLRuleEditorDialog.EXIT_SAVE)
					getParentEditor().getTableLayoutUtil().notifyThatSomethingChanged();
			}
		}
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
		//this.columns.clear();
		setSubFormFromProperties();
	}

	/**
	 * 
	 * @param foreignkey
	 */
	public void setForeignkey(String foreignkey) {
		setSubFormFromProperties();
	}

	/**
	 * 
	 * @param orientation
	 */
	public void setToolbarOrientation(String orientation) {
		setSubFormFromProperties();
		attachSubformColumnRulesToSubformColumns();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (subform != null) {
			subform.setEnabled(enabled);
		}
	}

	public String getColumnLabel(String sColumnName) {
		if (this.subform != null) {
			TableModel mdl = this.subform.getSubformTable().getModel();
			if (mdl instanceof CollectableTableModel) {
				int idx = ((CollectableTableModel)mdl).findColumnByFieldName(sColumnName);
				if (idx != -1)
					return ((CollectableTableModel)mdl).getColumnName(idx);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public synchronized void mouseClicked(MouseEvent e) {
		for(MouseListener l : listenerList.getListeners(MouseListener.class)) {
			// NUCLEUSINT-556
			if(e.getComponent() instanceof ToolTipsTableHeader) {

				WYSIWYGSubFormColumn column = getColumnAtX(e.getX());
				if(column != null) {
					MouseListener[] listenerlist = ((Component) column).getMouseListeners();
					for(MouseListener listener : listenerlist) {
						if(listener instanceof PropertiesDisplayMouseListener) {
							listener.mouseClicked(e);
						}
					}

				}
			}
			else
				l.mouseClicked(e);

		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color bg) {
		if (subform != null) {
			subform.setBackground(bg);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setBorder(javax.swing.border.Border)
	 */
	@Override
	public void setBorder(Border border) {
		if (subform != null) {
			subform.setBorder(border);
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
		return subformColumnRules;
	}
	
	/**
	 * 
	 */
	public void attachSubformColumnRulesToSubformColumns(){
		for (LayoutMLRule singleRule : subformColumnRules.getRules()) {
			for (WYSIWYGSubFormColumn column : getColumns()) {
				if (singleRule.getLayoutMLRuleEventType().getSourceComponent().equals(column.getName()))
					column.getLayoutMLRulesIfCapable().addRule(singleRule);
			}
		}
	}

	/**
	 * This Method draws a small red box on the Component to indicate existing {@link LayoutMLRules}
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(subform != null) {
			String columnLabel = null;
			int buttonWidthAtTheLeftOfTableHeader = 22;
			for(WYSIWYGSubFormColumn column : getColumns()) {
				if(column.getLayoutMLRulesIfCapable().getRules().size() > 0) {
					columnLabel = column.getCollectableEntityField().getLabel();
					int posx = 0;
					int posy = 1;
					if(subform.getToolbarOrientation() == JToolBar.VERTICAL)
						posx = subform.getToolbarBounds().width + buttonWidthAtTheLeftOfTableHeader;
					else {
						posy = posy + subform.getToolbarBounds().height;
						posx = buttonWidthAtTheLeftOfTableHeader;
					}
					for(int i = 0; i < subform.getJTable().getColumnModel().getColumnCount(); i++) {
						TableColumn tableColumn = subform.getJTable().getColumnModel().getColumn(i);
						posx = posx + tableColumn.getWidth();
						if(columnLabel.equals(tableColumn.getHeaderValue())) {
							Graphics2D g2d = (Graphics2D) g;
							g2d.setColor(Color.RED);
							g2d.fillRect(posx - 10, posy, 10, 10);
						}
					}
				}
			}
		}
	}

	public boolean isInitialLoading() {
		return isInitialLoading;
	}
	
	public void finalizeInitialLoading() {
		this.isInitialLoading = false;
		this.setSubFormFromProperties();
	}
	
	/**
	 * Helpermethod to get all {@link WYSIWYGSubFormColumn} with a {@link PropertyValueValuelistProvider}
	 * from this {@link WYSIWYGSubForm}
	 * @return null if there is none, otherwise the names of the columns
	 */
	public List<String> getColumnsWithValueListProvider() {
		List<String> columnsWithVP = new ArrayList<String>();
		for (WYSIWYGSubFormColumn column : columns.values()) {
			WYSIWYGValuelistProvider value = (WYSIWYGValuelistProvider) column.getProperties().getProperty(PROPERTY_VALUELISTPROVIDER).getValue();
			if (value != null)
					if (!(StringUtils.isNullOrEmpty(value.getType())))
						columnsWithVP.add(column.getName());
		}
		
		columnsWithVP = CollectionUtils.sorted(columnsWithVP);
		
		if (columnsWithVP.size() == 0)
			columnsWithVP = null;
		
		return columnsWithVP;
	}
	
	/**
	 * @return a {@link List} with the Names of the {@link WYSIWYGSubFormColumn}
	 */
	public List<String> getColumnNames() {
		Collection<WYSIWYGSubFormColumn> columns = getColumns();
		List<String> cols = new ArrayList<String>();
		
		for (WYSIWYGSubFormColumn column : columns) {
			cols.add(column.getName());
		}
		
		if (cols.size() > 0)
			return CollectionUtils.sorted(cols);

		return null;
	}

	@Override
	public String[][] getPropertyScriptElementLink() {
		return PROPERTIES_TO_SCRIPT_ELEMENTS;
	}
}
