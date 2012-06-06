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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor.WYSIWYGLayoutEditorChangeDescriptor;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGLayoutControllingPanel;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.JSCROLLPANE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.JSPLITPANE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.WYSIWYGLAYOUT_EDITOR_PANEL;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGChart;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGScrollPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSplitPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGTabbedPane;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertiesPanel;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.util.DnDUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.LayoutMLRuleController;
import org.nuclos.client.layout.wysiwyg.editor.util.ScissorToggleButton;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.UndoRedoFunction;
import org.nuclos.client.layout.wysiwyg.editor.util.popupmenu.ComponentPopUp;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGInitialFocusComponent;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosValueListProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * The surrounding JPanel of every {@link TableLayoutPanel}.<br>
 * On the {@link TableLayoutPanel} the {@link WYSIWYGComponent} are placed.<br>
 * This Panel if for setting a BackgroundColor and for Borders.<br>
 * It also is used for adding a new {@link WYSIWYGLayoutEditorPanel} into the contained {@link TableLayoutPanel} (Layout in Layout Szenario).
 * 
 * @see TableLayoutPanel
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class WYSIWYGLayoutEditorPanel extends JPanel implements WYSIWYGComponent, MouseListener, LayoutMLConstants, WYSIWYGEditorModes {

	public static String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static String PROPERTY_VISIBLE = PROPERTY_LABELS.VISIBLE;
	public static String PROPERTY_OPAQUE = PROPERTY_LABELS.OPAQUE;

	/** internal property */
	public static String PROPERTY_MARGIN_TOP = "margin-top";
	public static String PROPERTY_MARGIN_LEFT = "margin-left";

	/** {@link #getPropertyAttributeLink()} */
	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{{PROPERTY_NAME, ATTRIBUTE_NAME}, {PROPERTY_VISIBLE, ATTRIBUTE_VISIBLE}, {PROPERTY_OPAQUE, ATTRIBUTE_OPAQUE}};

	/** {@link #getPropertyNames()} */
	private static String[] PROPERTY_NAMES = new String[]{PROPERTY_NAME, PROPERTY_PREFFEREDSIZE, PROPERTY_VISIBLE, PROPERTY_OPAQUE, PROPERTY_BACKGROUNDCOLOR, PROPERTY_BORDER, PROPERTY_FONT};

	/** {@link #getPropertyClasses()}	 */
	private static PropertyClass[] PROPERTY_CLASSES = new PropertyClass[]{
			new PropertyClass(PROPERTY_NAME, String.class),
			new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class),
			new PropertyClass(PROPERTY_BORDER, Border.class),
			new PropertyClass(PROPERTY_BACKGROUNDCOLOR, Color.class),
			new PropertyClass(PROPERTY_VISIBLE, boolean.class),
			new PropertyClass(PROPERTY_OPAQUE, boolean.class),
			new PropertyClass(PROPERTY_BORDER, Border.class),
			new PropertyClass(PROPERTY_FONT, Font.class)};

	/** {@link #getPropertySetMethods()} */
	private static PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
			new PropertySetMethod(PROPERTY_NAME, "setName"),
			new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize"),
			new PropertySetMethod(PROPERTY_BORDER, "setBorder"),
			new PropertySetMethod(PROPERTY_BACKGROUNDCOLOR, "setBackground"),
			new PropertySetMethod(PROPERTY_OPAQUE, "setOpaque"),
			new PropertySetMethod(PROPERTY_BORDER, "setBorder"),
			new PropertySetMethod(PROPERTY_DESCRIPTION, "setToolTipText"),
			new PropertySetMethod(PROPERTY_FONT, "setFont")};

	/** {@link #getPropertyFilters()} */
	private PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[]{
			new PropertyFilter(PROPERTY_NAME, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_PREFFEREDSIZE, DISABLED),
			new PropertyFilter(PROPERTY_BORDER, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_BACKGROUNDCOLOR, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_OPAQUE, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_VISIBLE, DISABLED),
			new PropertyFilter(PROPERTY_BORDER, STANDARD_MODE | EXPERT_MODE),
			new PropertyFilter(PROPERTY_FONT, STANDARD_MODE | EXPERT_MODE)};

	private ComponentProperties properties;

	private TableLayoutPanel tableLayoutPanel = new TableLayoutPanel();

	private WYSIWYGMetaInformation metaInf;

	private LayoutCell currentLayoutCell = null;

	private TableLayoutUtil currentTableLayoutUtil = null;

	private WYSIWYGComponent componentToMove = null;

	private WYSIWYGEditorsToolbar wysiwygEditorsToolbar = null;

	private UndoRedoFunction undoRedo = null;

	// private LayoutMLDependencies layoutMLDependencies = null;

	private WYSIWYGInitialFocusComponent initialFocusComponent;

	private ScissorToggleButton justAddToggleButton;

	public ScissorToggleButton getJustAddToggleButton() {
		return justAddToggleButton;
	}

	/**
	 * Constructor called from {@link WYSIWYGLayoutControllingPanel}
	 * @param metaInf
	 * @param wysiwygEditorsToolbar
	 * 
	 * @see WYSIWYGLayoutControllingPanel#setupPanel()
	 */
	public WYSIWYGLayoutEditorPanel(WYSIWYGMetaInformation metaInf, WYSIWYGEditorsToolbar wysiwygEditorsToolbar) {
		this(metaInf);
		this.wysiwygEditorsToolbar = wysiwygEditorsToolbar;
		undoRedo = new UndoRedoFunction(wysiwygEditorsToolbar);
		justAddToggleButton = new ScissorToggleButton(this);
		wysiwygEditorsToolbar.addComponentToToolbar(justAddToggleButton);
	}

	/**
	 * Constructor for every new Instance of this {@link WYSIWYGLayoutEditorPanel}
	 * @param metaInf
	 */
	public WYSIWYGLayoutEditorPanel(WYSIWYGMetaInformation metaInf) {
		this.metaInf = metaInf;
		this.properties = PropertyUtils.getEmptyProperties(this, metaInf);

		double[][] layout = {{TableLayout.FILL}, {TableLayout.FILL}};
		this.setLayout(new TableLayout(layout));
		this.add(tableLayoutPanel, "0,0");

		tableLayoutPanel.addMouseListener(this);
		DnDUtil.addDragGestureListener(this, this);
	}

	/**
	 * @return get the {@link WYSIWYGInitialFocusComponent}
	 */
	public WYSIWYGInitialFocusComponent getInitialFocusComponent() {
		return initialFocusComponent;
	}

	/**
	 * @param initialFocusComponent the {@link WYSIWYGInitialFocusComponent} to be set
	 */
	public void setInitialFocusComponent(WYSIWYGInitialFocusComponent initialFocusComponent) {
		this.initialFocusComponent = initialFocusComponent;
		this.getTableLayoutUtil().notifyThatSomethingChanged();
	}

	// public void setLayoutMLDependencies(LayoutMLDependencies layoutMLDependencies) {
	// if (this.getParentEditor() == null)
	// this.layoutMLDependencies = layoutMLDependencies;
	// else
	// this.getParentEditor().setLayoutMLDependencies(layoutMLDependencies);
	// }
	//
	// public LayoutMLDependencies getLayoutMLDependencies() {
	// if (this.getParentEditor() == null) {
	// if (this.layoutMLDependencies == null)
	// this.layoutMLDependencies = new LayoutMLDependencies(getMetaInformation());
	// return this.layoutMLDependencies;
	// } else
	// return this.getParentEditor().getLayoutMLDependencies();
	// }

	/**
	 * 
	 * @return the {@link WYSIWYGMetaInformation}
	 */
	public WYSIWYGMetaInformation getMetaInformation() {
		return this.metaInf;
	}

	/**
	 * Give Access to the {@link WYSIWYGEditorsToolbar}<br>
	 * Digging down to the Main {@link WYSIWYGLayoutEditorPanel}
	 * @return the (one and only) {@link WYSIWYGEditorsToolbar}
	 */
	public WYSIWYGEditorsToolbar getWYSIWYGEditorsToolbar() {
		if (this.getParentEditor() == null)
			return this.wysiwygEditorsToolbar;
		else
			return this.getParentEditor().getWYSIWYGEditorsToolbar();
	}

	/**
	 * @return the {@link TableLayoutUtil} 
	 */
	public TableLayoutUtil getTableLayoutUtil() {
		return this.tableLayoutPanel.getTableLayoutUtil();
	}

	/**
	 * @param chgDescriptor to register for notifying of Changes
	 */
	public void setWYSIWYGLayoutEditorChangeDescriptor(WYSIWYGLayoutEditorChangeDescriptor chgDescriptor) {
		tableLayoutPanel.setEditorChangeDescriptor(chgDescriptor);
	}

	/**
	 * If the {@link WYSIWYGLayoutEditorPanel} is in a Wrapping Component, this Method gets it.
	 * @return the {@link WYSIWYGComponent} around this {@link WYSIWYGLayoutEditorPanel}
	 */
	public WYSIWYGComponent getParentWrappingComponent() {
		if (getScrollPane() != null) {
			return getScrollPane();
		} else if (getTabbedPane() != null) {
			return getTabbedPane();
		} else if (getSplitPane() != null) {
			return getSplitPane();
		}

		return null;
	}

	/**
	 * @return the registered {@link UndoRedoFunction}, digging down to the Main {@link WYSIWYGLayoutEditorPanel}
	 */
	public UndoRedoFunction getUndoRedoFunction() {
		if (this.getParentEditor() == null) {
			return this.undoRedo;
		}
		return this.getParentEditor().getUndoRedoFunction();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getParentEditor()
	 */
	@Override
	public WYSIWYGLayoutEditorPanel getParentEditor() {
		if (getScrollPane() != null) {
			return getScrollPane().getParentEditor();
		} else if (getTabbedPane() != null) {
			return getTabbedPane().getParentEditor();
		} else if (getSplitPane() != null) {
			return getSplitPane().getParentEditor();
		} else {
			if (super.getParent() instanceof TableLayoutPanel) {
				return (WYSIWYGLayoutEditorPanel) super.getParent().getParent();
			}
		}
		return null;
	}

	/**
	 * @return If Editor is placed in a WYSIWYGScrollPane the WYSIWYGScrollPane would be returned. Otherwise return null.
	 */
	public WYSIWYGScrollPane getScrollPane() {
		if (super.getParent() instanceof JViewport) {
			if (super.getParent().getParent() instanceof WYSIWYGScrollPane) {
				return (WYSIWYGScrollPane) super.getParent().getParent();
			}
		}
		return null;
	}

	/**
	 * 
	 * @return If Editor is placed in a WYSIWYGTabbedPane the WYSIWYGTabbedPane would be returned. Otherwise return null.
	 */
	public WYSIWYGTabbedPane getTabbedPane() {
		if (super.getParent() instanceof WYSIWYGTabbedPane) {
			return (WYSIWYGTabbedPane) super.getParent();
		}
		return null;
	}

	/**
	 * 
	 * @return If Editor is placed in a WYSIWYGSplittPane the WYSIWYGSplittPane would be returned. Otherwise return null.
	 */
	public WYSIWYGSplitPane getSplitPane() {
		if (super.getParent() instanceof JSplitPane) {
			if (super.getParent().getParent() instanceof WYSIWYGSplitPane) {
				return (WYSIWYGSplitPane) super.getParent().getParent();
			}
		}
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
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getProperties()
	 */
	@Override
	public ComponentProperties getProperties() {
		return properties;
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
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyNames()
	 */
	@Override
	public String[] getPropertyNames() {
		return PROPERTY_NAMES;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperties(org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties)
	 */
	@Override
	public void setProperties(ComponentProperties properties) {
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperty(java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue, java.lang.Class)
	 */
	@Override
	public void setProperty(String property, PropertyValue<?> value, Class<?> valueClass) throws CommonBusinessException {
		properties.setProperty(property, value, valueClass);
		this.tableLayoutPanel.getTableLayoutUtil().notifyThatSomethingChanged();
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
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getAdditionalContextMenuItems(int)
	 */
	@Override
	public List<JMenuItem> getAdditionalContextMenuItems(int xClick) {
		List<JMenuItem> list = new ArrayList<JMenuItem>();

		if (getParentEditor() != null) {
			list.add(PropertyUtils.getStandartContextMenuEntryForProperties(WYSIWYGLAYOUT_EDITOR_PANEL.PROPERTY_LABEL, WYSIWYGLayoutEditorPanel.this, getParentEditor().getTableLayoutUtil()));
		} else {
			list.add(PropertyUtils.getStandartContextMenuEntryForProperties(WYSIWYGLAYOUT_EDITOR_PANEL.PROPERTY_LABEL, WYSIWYGLayoutEditorPanel.this, null));
		}
		if (getScrollPane() != null) {
			list.add(PropertyUtils.getStandartContextMenuEntryForProperties(JSCROLLPANE.PROPERTY_LABEL, getScrollPane(), getScrollPane().getParentEditor().getTableLayoutUtil()));
		}
		if (getSplitPane() != null) {
			list.add(PropertyUtils.getStandartContextMenuEntryForProperties(JSPLITPANE.PROPERTY_LABEL, getSplitPane(), getSplitPane().getParentEditor().getTableLayoutUtil()));
		}

		return list;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 1) {
			if(e.getPoint().x < InterfaceGuidelines.MARGIN_LEFT && e.getPoint().y < InterfaceGuidelines.MARGIN_TOP) {
				if (getParentEditor() != null) {
					PropertiesPanel.showPropertiesForComponent(WYSIWYGLayoutEditorPanel.this, getParentEditor().getTableLayoutUtil());
				} else {
					PropertiesPanel.showPropertiesForComponent(WYSIWYGLayoutEditorPanel.this, null);
				}
				if (getScrollPane() != null) {
					PropertiesPanel.showPropertiesForComponent(getScrollPane(),  getScrollPane().getParentEditor().getTableLayoutUtil());
				}
				if (getSplitPane() != null) {
					PropertiesPanel.showPropertiesForComponent(getSplitPane(), getSplitPane().getParentEditor().getTableLayoutUtil());
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			if ((e.getX() <= InterfaceGuidelines.MARGIN_LEFT && e.getY() < InterfaceGuidelines.MARGIN_TOP)) {
				ComponentPopUp popup = new ComponentPopUp(getTableLayoutUtil(), this);

				Point loc = tableLayoutPanel.getMousePosition().getLocation();
				if (loc == null) {
					loc = (Point)e.getLocationOnScreen().clone();
					SwingUtilities.convertPointFromScreen(loc, e.getComponent());
				}
				popup.showComponentPropertiesPopup(loc);
			}
		}
	}

	/**
	 * @return {@link TableLayoutPanel} embedded in this {@link WYSIWYGLayoutEditorPanel}
	 */
	public TableLayoutPanel getTableLayoutPanel() {
		return tableLayoutPanel;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesFromMetaInformation()
	 */
	@Override
	public String[][] getPropertyValuesFromMetaInformation() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesStatic()
	 */
	@Override
	public String[][] getPropertyValuesStatic() {
		return null;
	}

	/**
	 * This Method diggs down to the Main {@link WYSIWYGLayoutEditorPanel}.<br>
	 * There can be only one {@link LayoutCell} active.
	 * 
	 * 
	 * @param currentLayoutCell the {@link LayoutCell} to be set active
	 * @param currentTableLayoutUtil the {@link TableLayoutUtil} that is responsible for the {@link LayoutCell}
	 */
	public void setCurrentLayoutCell(LayoutCell currentLayoutCell, TableLayoutUtil currentTableLayoutUtil) {
		if (this.getParentEditor() == null) {
			this.currentLayoutCell = currentLayoutCell;
			this.currentTableLayoutUtil = currentTableLayoutUtil;
		} else {
			WYSIWYGLayoutEditorPanel parentPanel = this.getParentEditor();
			parentPanel.setCurrentLayoutCell(currentLayoutCell, currentTableLayoutUtil);
		}
	}

	/**
	 * This Method diggs down to the Main {@link WYSIWYGLayoutEditorPanel} to get the stored {@link TableLayoutUtil}.<br>
	 * 
	 * @see #setCurrentLayoutCell(LayoutCell, TableLayoutUtil)
	 * 
	 * @return the {@link TableLayoutUtil} 
	 */
	public TableLayoutUtil getCurrentTableLayoutUtil() {
		if (this.getParentEditor() == null) {
			return this.currentTableLayoutUtil;
		} else {
			WYSIWYGLayoutEditorPanel parentPanel = this.getParentEditor();
			return parentPanel.getCurrentTableLayoutUtil();
		}
	}

	/**
	 * This Method diggs down to the Main {@link WYSIWYGLayoutEditorPanel} to get the stored {@link LayoutCell}<br>
	 * 
	 * @see #setCurrentLayoutCell(LayoutCell, TableLayoutUtil)
	 * 
	 * @return
	 */
	public LayoutCell getCurrentLayoutCell() {
		if (this.getParentEditor() == null) {
			return this.currentLayoutCell;
		} else {
			WYSIWYGLayoutEditorPanel parentPanel = this.getParentEditor();
			return parentPanel.getCurrentLayoutCell();
		}
	}

	/**
	 * This Method marks a {@link WYSIWYGComponent} to be moved.<br>
	 * Since this is actually not done by Drag and Drop this way works fine.<br>
	 * The {@link WYSIWYGComponent} is passed to the main {@link WYSIWYGLayoutEditorPanel}.<br>
	 * This is needed because a {@link WYSIWYGComponent} can be moved from one Panel to another.
	 * 
	 * @param componentToMove the {@link WYSIWYGComponent} that will be marked for move
	 */
	public void setComponentToMove(WYSIWYGComponent componentToMove) {
		if (this.getParentEditor() == null) {
			this.componentToMove = componentToMove;

			/** there is no need to move the main editor panel */
			if (componentToMove == null || componentToMove.getParentEditor() == null) {
				return;
			}

			if (componentToMove instanceof WYSIWYGLayoutEditorPanel) {
				if (((WYSIWYGLayoutEditorPanel) componentToMove).getParentWrappingComponent() != null)
					componentToMove = ((WYSIWYGLayoutEditorPanel) componentToMove).getParentWrappingComponent();
			}

			// componentMoved = false;
		} else {
			WYSIWYGLayoutEditorPanel parentPanel = this.getParentEditor();
			parentPanel.setComponentToMove(componentToMove);
		}
	}

	/**
	 * This Method gets the {@link WYSIWYGComponent} that is marked for Movement.<br>
	 * Its a bit tricky, the {@link WYSIWYGComponent}s can be moved from one {@link WYSIWYGLayoutEditorPanel} to another.<br>
	 * So the {@link WYSIWYGComponent} is taken from the Main {@link WYSIWYGLayoutEditorPanel}.
	 * @return
	 */
	public WYSIWYGComponent getComponentToMove() {
		if (this.getParentEditor() == null) {
			// componentMoved = true;
			return this.componentToMove;
		} else {
			WYSIWYGLayoutEditorPanel parentPanel = this.getParentEditor();
			return parentPanel.getComponentToMove();
		}
	}

	/**
	 * This Method checks if the Component was moved...<br>
	 * Digging down to the main {@link WYSIWYGLayoutEditorPanel} because the {@link WYSIWYGComponent} is somewhere on a {@link WYSIWYGLayoutEditorPanel}. 
	 * @return true if it was moved
	 */
	public boolean wasComponentMoved() {
		if (this.getParentEditor() == null) {
			return (this.componentToMove == null);
		} else {
			return this.getParentEditor().wasComponentMoved();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getLayoutMLRulesIfCapable()
	 */
	@Override
	public LayoutMLRules getLayoutMLRulesIfCapable() {
		return null;
	}

	/**
	 * This Method is used for restoring {@link LayoutMLRules} from the LayoutML XML.
	 * 
	 * @see LayoutMLRulesProcessor#closeElement()
	 * 
	 * @param rules all the {@link LayoutMLRules} loaded from the LayoutML XML
	 * @param allWYSIWYGComponents all {@link WYSIWYGComponent} that were found during restore
	 */
	public synchronized void handoverRules(LayoutMLRules rules, Vector<WYSIWYGComponent> allWYSIWYGComponents) {
		LayoutMLRules collectingRules = null;
		String sourceComponent = null;
		String entity = null;
		String componentName = null;

		for (WYSIWYGComponent component : allWYSIWYGComponents) {
			if (component instanceof WYSIWYGSubForm)
				componentName = ((WYSIWYGSubForm) component).getEntityName();
			else
				componentName = (String) (component).getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_NAME).getValue();

			collectingRules = new LayoutMLRules();
			for (LayoutMLRule singleRule : rules.getRules()) {
				sourceComponent = singleRule.getLayoutMLRuleEventType().getSourceComponent();
				entity = singleRule.getLayoutMLRuleEventType().getEntity();
				if (entity == null) {
					// simple CollectableComponent
					if (componentName.equals(sourceComponent))
						collectingRules.addRule(singleRule);
				} else {
					// SubformColumn Rule
					if (componentName.equals(entity))
						collectingRules.addRule(singleRule);
				}
			}
			LayoutMLRuleController.attachRulesToComponent(component, collectingRules);
		}

		rules = null;
		allWYSIWYGComponents = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
	};

	// public synchronized void handoverDependencies(LayoutMLDependencies layoutMLDependencies) {
	// setLayoutMLDependencies(layoutMLDependencies);
	// }

	/**
	 * This Method collects all the Subform Entity Names
	 */
	public List<String> getSubFormEntityNames() {
		ArrayList<Object> result = new ArrayList<Object>();
		//NUCLEUSINT-419 get the main panel and then get all components... 
		WYSIWYGLayoutEditorPanel mainPanel = null;
		if (this.getParentEditor() == null)
			mainPanel = this;
		else
			mainPanel = this.getParentEditor();
		
		getWYSIWYGComponents(WYSIWYGChart.class, mainPanel, result);
		getWYSIWYGComponents(WYSIWYGSubForm.class, mainPanel, result);

		List<String> sorted = CollectionUtils.transform(result, new Transformer<Object, String>() {
			@Override
			public String transform(Object i) {
				if (i instanceof WYSIWYGChart) {
					return (String) ((WYSIWYGChart) i).getProperties().getProperty(WYSIWYGChart.PROPERTY_ENTITY).getValue();
				}
				if (i instanceof WYSIWYGSubForm) {
					return (String) ((WYSIWYGSubForm) i).getProperties().getProperty(WYSIWYGSubForm.PROPERTY_ENTITY).getValue();
				}
				return null;
			}
		});

		sorted = removeDuplicatesAndNullValues(sorted);
		
		Collections.sort(sorted, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		return sorted;
	}

	/**
	 * @return a {@link List} with all the Names of the Components 
	 */
	public List<String> getCollectableComponents() {
		ArrayList<Object> result = new ArrayList<Object>();
		//NUCLEUSINT-419 get the main panel and then get all components... 
		WYSIWYGLayoutEditorPanel mainPanel = null;
		if (this.getParentEditor() == null)
			mainPanel = this;
		else
			mainPanel = this.getParentEditor();
		
		getWYSIWYGComponents(WYSIWYGCollectableComponent.class,(mainPanel), result);

		List<String> sorted = CollectionUtils.transform(result, new Transformer<Object, String>() {
			@Override
			public String transform(Object i) {
				if (i instanceof WYSIWYGCollectableComponent && !(i instanceof WYSIWYGCollectableLabel)) {
					return (String) ((WYSIWYGCollectableComponent) i).getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_NAME).getValue();
				}
				return null;
			}
		});
		
		sorted = removeDuplicatesAndNullValues(sorted);

		Collections.sort(sorted, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1 != null && o2 != null) {
					return o1.compareToIgnoreCase(o2);
				} else if (o1 == null && o2 == null) {
					return 0;
				} else {
					return (o1 == null) ? -1 : 1;
				}
			}
		});
		return sorted;
	}
	
	public List<WYSIWYGComponent> getAllComponents() {
		ArrayList<WYSIWYGComponent> result = new ArrayList<WYSIWYGComponent>();
		
		WYSIWYGLayoutEditorPanel mainPanel = null;
		if (this.getParentEditor() == null)
			mainPanel = this;
		else
			mainPanel = this.getParentEditor();
		
		getWYSIWYGComponents(WYSIWYGComponent.class,(mainPanel), result);
		
		return result;
	}
	
	/**
	 * This Method gets a {@link WYSIWYGCollectableComponent} for the given EntityName
	 * @param entityName the Name to search for
	 * @return the {@link WYSIWYGCollectableComponent} for this entity, null if not found
	 */
	public WYSIWYGCollectableComponent getWYSIWYGComponentUsingEntity(String entityName) {
		List<WYSIWYGCollectableComponent> allComponents = new ArrayList<WYSIWYGCollectableComponent>();
		getWYSIWYGComponents(WYSIWYGCollectableComponent.class, this.getMainEditorPanel(), allComponents);
		
		for(WYSIWYGCollectableComponent component : allComponents) {
			if (entityName.equals(component.getName())) {
				if (!(component instanceof WYSIWYGCollectableLabel)) {
					return component;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * This Method gets a {@link WYSIWYGCollectableComponent} for the given EntityName
	 * @param entityName the Name to search for
	 * @return the {@link WYSIWYGCollectableComponent} for this entity, null if not found
	 */
	public WYSIWYGSubFormColumn getWYSIWYGSubformColumnUsingEntity(String entityName) {

		
		List<WYSIWYGSubForm> allSubforms = new ArrayList<WYSIWYGSubForm>();
		getWYSIWYGComponents(WYSIWYGSubForm.class, this.getMainEditorPanel(), allSubforms);
		
		for (WYSIWYGSubForm subform : allSubforms) {
			Collection<WYSIWYGSubFormColumn> columns = subform.getColumns();
			for (WYSIWYGSubFormColumn column : columns) {
				if (entityName.equals(column.getName()))
					return column;
			}
		}
		
		return null;
	}
	
	/**
	 * NUCLEUSINT-341
	 * @return a {@link List} with all the Names of the Components with a defined {@link WYSIWYGValuelistProvider}
	 */
	public List<String> getCollectableComponentsWithValuelistProvider() {
		ArrayList<Object> result = new ArrayList<Object>();
		getWYSIWYGComponentsWithValuelistProviders(this, result);

		List<String> sorted = CollectionUtils.transform(result, new Transformer<Object, String>() {
			@Override
			public String transform(Object i) {
				if (i instanceof WYSIWYGCollectableComponent && !(i instanceof WYSIWYGCollectableLabel)) {
					return (String) ((WYSIWYGCollectableComponent) i).getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_NAME).getValue();
				}
				return null;
			}
		});
		
		sorted = removeDuplicatesAndNullValues(sorted);

		Collections.sort(sorted, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1 != null && o2 != null) {
					return o1.compareToIgnoreCase(o2);
				} else if (o1 == null && o2 == null) {
					return 0;
				} else {
					return (o1 == null) ? -1 : 1;
				}
			}
		});
		return sorted;
	}
	
	/**
	 * Small Helpermethod for removing duplicates
	 * @param listToClear the List<String> with the values
	 * @return a new List<String> cleaned up
	 * NUCLEUSINT-406
	 */
	private List<String> removeDuplicatesAndNullValues(List<String> listToClear) {
		List<String> unique = new ArrayList<String>();

		for (String string : listToClear) {
			if (string != null)
				if (!unique.contains(string))
					unique.add(string);
		}
		return unique;
	}

	/**
	 * Collecting {@link WYSIWYGComponent} from the container
	 * NUCLEUSINT-419 digging deeper
	 * @param cls the {@link Class} the {@link WYSIWYGComponent} must be
	 * @param container the container to get the {@link WYSIWYGComponent} from
	 * @param list the {@link List} the {@link WYSIWYGComponent} matching the citeria are put in
	 */
	public <T> void getWYSIWYGComponents(Class<T> cls, Container container, List<? super T> list) {
		for (Component c : container.getComponents()) {
			if (cls.isAssignableFrom(c.getClass())) {
				list.add(cls.cast(c));
			}
			if (c instanceof WYSIWYGLayoutEditorPanel) {
				getWYSIWYGComponents(cls, ((WYSIWYGLayoutEditorPanel)c).getTableLayoutPanel(), list);
			} else if (c instanceof TableLayoutPanel) {
				getWYSIWYGComponents(cls, ((TableLayoutPanel)c), list);
			} else if (c instanceof WYSIWYGTabbedPane) {
				getWYSIWYGComponents(cls, ((WYSIWYGTabbedPane)c), list);
			}else if (c instanceof WYSIWYGSplitPane) {
				getWYSIWYGComponents(cls, ((WYSIWYGSplitPane)c), list);
			}else if (c instanceof JSplitPane) {
				// finding components in a splitpane did not work
				getWYSIWYGComponents(cls, ((JSplitPane)c), list);
			}else if (c instanceof WYSIWYGScrollPane) {
				getWYSIWYGComponents(cls, ((WYSIWYGScrollPane)c), list);
			}else if (c instanceof JViewport) {
				// finding components in a scrollpane did not work
				getWYSIWYGComponents(cls, ((JViewport)c), list);
			}
		}
	}
	
	/**
	 * NUCLEUSINT-341
	 * NUCLEUSINT-419 digging deeper
	 * Collecting {@link WYSIWYGComponent} with a defined {@link WYSIWYGValuelistProvider}
	 * @param container
	 * @param list
	 */
	public void getWYSIWYGComponentsWithValuelistProviders(Container container, List<Object> list) {
		for (Component c : container.getComponents()) {
			if (c instanceof WYSIWYGComponent) {
				PropertyValue<?> value = ((WYSIWYGComponent) c).getProperties().getProperty(PROPERTY_VALUELISTPROVIDER);
				if (value != null)
					if (value.getValue() != null)
						if (!(StringUtils.isNullOrEmpty(((WYSIWYGValuelistProvider) value.getValue()).getType())))
							list.add(c);
				for (PropertyClass pc : ((WYSIWYGComponent) c).getPropertyClasses()) {
					if (pc instanceof NuclosValueListProvider) {
						value = ((WYSIWYGComponent) c).getProperties().getProperty(pc.getName());
						if (value != null)
							if (value.getValue() != null)
								if (!(StringUtils.isNullOrEmpty(((WYSIWYGValuelistProvider) value.getValue()).getType())))
									list.add(c);
					}
				}
			}
			if (c instanceof WYSIWYGLayoutEditorPanel) {
				getWYSIWYGComponentsWithValuelistProviders(((WYSIWYGLayoutEditorPanel) c).getTableLayoutPanel(), list);
			} else if (c instanceof TableLayoutPanel) {
				getWYSIWYGComponentsWithValuelistProviders(((TableLayoutPanel) c), list);
			} else if (c instanceof WYSIWYGTabbedPane) {
				getWYSIWYGComponentsWithValuelistProviders(((WYSIWYGTabbedPane) c), list);
			} else if (c instanceof WYSIWYGSplitPane) {
				getWYSIWYGComponentsWithValuelistProviders(((WYSIWYGSplitPane) c), list);
			} else if (c instanceof JSplitPane) {
				// finding components in a splitpane did not work
				getWYSIWYGComponentsWithValuelistProviders(((JSplitPane)c), list);
			} else if (c instanceof WYSIWYGScrollPane) {
				getWYSIWYGComponentsWithValuelistProviders(((WYSIWYGScrollPane) c), list);
			} else if (c instanceof JViewport) {
				// finding components in a scrollpane did not work
				getWYSIWYGComponentsWithValuelistProviders(((JViewport)c), list);
			}
		}
	}

	/**
	 * @return the {@link WYSIWYGLayoutControllingPanel}
	 * @throws NuclosBusinessException if no {@link WYSIWYGLayoutControllingPanel} found
	 */
	public WYSIWYGLayoutControllingPanel getController() throws NuclosBusinessException {
		Container c = this;
		while (c.getParent() != null) {
			if (c.getParent() instanceof WYSIWYGLayoutControllingPanel) {
				return (WYSIWYGLayoutControllingPanel) c.getParent();
			} else {
				c = c.getParent();
			}
		}
		throw new NuclosBusinessException(WYSIWYGLAYOUT_EDITOR_PANEL.ERRORMESSAGE_PANEL_HAS_NO_LAYOUT);
	}

	/**
	 * This Method checks if slicing (automatic cutting of new {@link LayoutCell}s) is active
	 * @return true if it is disabled and {@link WYSIWYGComponent} are just added, false if not - so new {@link LayoutCell}s are created
	 */
	public boolean isJustAddEnabled() {
		if (this.getParentEditor() == null) {
			return justAddToggleButton.isScissorEnabled();
		} else {
			WYSIWYGLayoutEditorPanel parentPanel = this.getParentEditor();
			return parentPanel.isJustAddEnabled();
		}
	}

	/**
	 * This Method sets the Toggle for Slicing
	 * 
	 * @see #isJustAddEnabled()
	 * 
	 * @param value
	 */
	public void setJustAddEnabled(boolean value) {
		if (this.getParentEditor() == null) {
			justAddToggleButton.setScissorEnabled(value);
		} else {
			WYSIWYGLayoutEditorPanel parentPanel = this.getParentEditor();
			parentPanel.setJustAddEnabled(value);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyFilters()
	 */
	@Override
	public PropertyFilter[] getPropertyFilters() {
		return PROPERTY_FILTERS;
	}

	/**
	 * Some Properties make no sense for the Main {@link WYSIWYGLayoutEditorPanel} like invisible and not opaque...
	 * This Properties are enabled here... 
	 */
	public void enablePropertiesForInlinePanels() {
		// FIX NUCLEUSINT-254
		for (PropertyFilter filter : PROPERTY_FILTERS) {
			if (filter.getName() == PROPERTY_VISIBLE) {
				filter.setMode(EXPERT_MODE);
			} else if (filter.getName() == PROPERTY_PREFFEREDSIZE) {
				filter.setMode(STANDARD_MODE | EXPERT_MODE);
			}

		}
	}
	
	/**
	 * NUCLEUSINT-421
	 * @return the main {@link WYSIWYGLayoutEditorPanel}
	 */
	public WYSIWYGLayoutEditorPanel getMainEditorPanel(){
		if (this.getParentEditor() != null)
			return getParentEditor().getMainEditorPanel();
		else
			return this;
	}
}
