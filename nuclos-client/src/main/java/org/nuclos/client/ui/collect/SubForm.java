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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InvocationEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.jdesktop.swingx.event.TableColumnModelExtListener;
import org.nuclos.api.context.ScriptContext;
import org.nuclos.client.common.FocusActionListener;
import org.nuclos.client.common.Utils;
import org.nuclos.client.scripting.ScriptEvaluator;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SizeKnownListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.URIMouseAdapter;
import org.nuclos.client.ui.collect.FixedColumnRowHeader.HeaderTable;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.collect.component.CollectableComponentTableCellEditor;
import org.nuclos.client.ui.collect.component.CollectableComponentType;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.client.ui.collect.component.DefaultCollectableComponentFactory;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponentWithVLP;
import org.nuclos.client.ui.collect.component.LookupEvent;
import org.nuclos.client.ui.collect.component.LookupListener;
import org.nuclos.client.ui.collect.component.TableCellCursor;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.collect.model.CollectableEntityFieldBasedTableModel;
import org.nuclos.client.ui.event.PopupMenuMouseAdapter;
import org.nuclos.client.ui.event.TableColumnModelAdapter;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.client.ui.popupmenu.JPopupMenuFactory;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.table.SortableTableModelEvent;
import org.nuclos.client.ui.table.TableCellEditorProvider;
import org.nuclos.client.ui.table.TableCellRendererProvider;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFieldNotInModelException;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Subform for displaying/editing dependant <code>Collectable</code>s.
 *
 * Changes as of 2010-08-12: The subform's toolbar is no longer publicly
 * accessible! If your client-code needs toolbar-events, add a SubFormToolListener
 * on the subForm itself, which will be notifies about ALL toolbar events at once.
 *
 * Toolbar button states can be set via setToolbarFunctionState.
 *
 * If you need to add another toolbar button, pass the button along with its
 * action command to addToolbarFunction. The given action command will be propagated
 * the same way as the internal default commands.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@SuppressWarnings("serial")
public class SubForm extends JPanel 
	implements TableCellRendererProvider, ActionListener, Closeable {

	private static final Logger LOG = Logger.getLogger(SubForm.class);

	public static interface SubFormToolListener extends EventListener {
		void toolbarAction(String actionCommand);
	};

	public static enum ToolbarFunction {
		NEW {
	        @Override
	        public AbstractButton createButton() {
	        	JButton res = new JButton(Icons.getInstance().getIconNew16());
	        	res.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
	        			"SubForm.7","Neuen Datensatz anlegen"));
	        	res.setActionCommand(name());
		        return res;
	        }
			@Override
			public JMenuItem createMenuItem() {
				JMenuItem res = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
						"SubForm.7","Neuen Datensatz anlegen"), Icons.getInstance().getIconNew16());
				res.setActionCommand(name());
				return res;
			}
        },
		REMOVE {
	        @Override
	        public AbstractButton createButton() {
	        	JButton res = new JButton(Icons.getInstance().getIconDelete16());
	        	res.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
	        			"SubForm.1","Ausgew\u00e4hlten Datensatz l\u00f6schen"));
	        	res.setActionCommand(name());
		        return res;
	        }
			@Override
			public JMenuItem createMenuItem() {
				JMenuItem res = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
						"SubForm.1","Ausgew\u00e4hlten Datensatz l\u00f6schen"), Icons.getInstance().getIconDelete16());
				res.setActionCommand(name());
				return res;
			}
        },
		MULTIEDIT {
	        @Override
	        public AbstractButton createButton() {
	        	JButton res = new JButton(Icons.getInstance().getIconMultiEdit16());
	        	res.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
	        			"SubForm.6","Mehrere Datens\u00e4tze hinzuf\u00fcgen/l\u00f6schen"));
	        	res.setActionCommand(name());
		        return res;
	        }
			@Override
			public JMenuItem createMenuItem() {
				JMenuItem res = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
						"SubForm.6","Mehrere Datens\u00e4tze hinzuf\u00fcgen/l\u00f6schen"), Icons.getInstance().getIconMultiEdit16());
				res.setActionCommand(name());
				return res;
			}
        },
		FILTER {
	        @Override
	        public AbstractButton createButton() {
	        	JToggleButton res = new JToggleButton(Icons.getInstance().getIconFilter16());
	        	res.setSize(16, 16);
	        	res.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
	        			"SubForm.5","Datens\u00e4tze filtern"));
	        	res.setActionCommand(name());
		        return res;
	        }
			@Override
			public JMenuItem createMenuItem() {
				JCheckBoxMenuItem res = new JCheckBoxMenuItem(SpringLocaleDelegate.getInstance().getMessage(
						"SubForm.5","Datens\u00e4tze filtern"), Icons.getInstance().getIconFilter16());
				res.setActionCommand(name());
				return res;
			}
        },
		TRANSFER {
	        @Override
	        public AbstractButton createButton() {
	        	JToggleButton res = new JToggleButton(Icons.getInstance().getIconCopy16());
	        	res.setSize(16, 16);
	        	res.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
	        			"SubForm.ToolbarFunction.TRANSFER", "Datensatz für alle Entit\u00e4ten \u00fcbernehmen"));
	        	res.setActionCommand(name());
		        return res;
	        }
			@Override
			public JMenuItem createMenuItem() {
				JCheckBoxMenuItem res = new JCheckBoxMenuItem(SpringLocaleDelegate.getInstance().getMessage(
						"SubForm.ToolbarFunction.TRANSFER", "Datensatz für alle Entit\u00e4ten \u00fcbernehmen"), Icons.getInstance().getIconCopy16());
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
	};

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

	/* the minimum row height for the table(s). */
	public static final int MIN_ROWHEIGHT = 20;
	private static final Color LAYER_BUSY_COLOR = new Color(128, 128, 128, 128);

	private static final Logger  log = Logger.getLogger(SubForm.class);

	private JXLayer<JComponent>	 layer;
	private AtomicInteger	     lockCount	= new AtomicInteger(0);

	private HashMap<String, AbstractButton>   toolbarButtons;
	private List<String>					  toolbarOrder;
	private HashMap<String, JMenuItem>		  toolbarMenuItems;

	private final JToolBar       toolbar;

	private final JPanel         contentPane = new JPanel(new BorderLayout());
	private JScrollPane    scrollPane = new JScrollPane();

	private SubFormTable   subformtbl;

	private SubFormFilter        subformfilter;

	private final String         entityName;
	private final String         foreignKeyFieldToParent;

	private final List<LookupListener>		lookupListener = new ArrayList<LookupListener>();

	protected final List<FocusActionListener> lstFocusActionListener
		= new ArrayList<FocusActionListener>();

	/**
	 * NUCLOSINT-63: To display the size of subform list in the corresponding tab.
	 */
	private SizeKnownListener sizeKnownListener;

	/**
	 * maps column names to columns
	 */
	private final Map<String, Column> mpColumns = new LinkedHashMap<String, Column>();
	private final Map<CollectableEntityField, TableCellRenderer> mpColumnRenderer 
		= new HashMap<CollectableEntityField, TableCellRenderer>();
	private final Map<CollectableEntityField, CollectableComponentTableCellEditor> mpStaticColumnEditors 
		= new HashMap<CollectableEntityField, CollectableComponentTableCellEditor>();

	private List<ChangeListener> lstchangelistener = new LinkedList<ChangeListener>();

	private String uniqueMasterColumnName;

	private String sSubFormParent;

	private String sControllerType;

	private String sInitialSortingColumn;
	private String sInitialSortingOrder;

	/**
	 * Use custom column widths? This will always be true as soon as the user changed one or more column width
	 * the first time.
	 */
	private boolean useCustomColumnWidths;

	private TableModelListener tblmdllistener;
	private TableColumnModelListener columnmodellistener;

	private SubformRowHeader rowHeader;

	private CollectableComponentFactory collectableComponentFactory;

	private List<SubFormToolListener> listeners;

	private PopupMenuMouseAdapter popupMenuAdapter;
	
	private final List<Pair<JComponent,MouseListener>> myMouseListener = new ArrayList<Pair<JComponent,MouseListener>>();
	
	private boolean closed = false;
	
	private boolean readonly = false;
	
	private NuclosScript newEnabledScript;
	private NuclosScript editEnabledScript;
	private NuclosScript deleteEnabledScript;
	private NuclosScript cloneEnabledScript;
	
	/**
	 * @param entityName
	 * @param iToolBarOrientation @see JToolbar#setOrientation
	 * @precondition entityName != null
	 * @postcondition this.getForeignKeyFieldToParent() == null
	 */
	public SubForm(String sEntityName, int iToolBarOrientation) {
		this(sEntityName, iToolBarOrientation, null);

		assert this.getForeignKeyFieldToParent() == null;
	}

	/**
	 * @param entityName
	 * @param toolBarOrientation @see JToolbar#setOrientation
	 * @param foreignKeyFieldToParent Needs only be specified if not unique. @see #getForeignKeyFieldToParent()
	 * @precondition entityName != null
	 * @postcondition this.getForeignKeyFieldToParent() == foreignKeyFieldToParent
	 */
	public SubForm(String entityName, int toolBarOrientation, String foreignKeyFieldToParent) {
		super(new GridLayout(1, 1));
		this.toolbar = UIUtils.createNonFloatableToolBar(toolBarOrientation);

		this.listeners = new ArrayList<SubFormToolListener>();
		subformtbl = new SubFormTable(this);
		subformtbl.addMouseListener(new SubFormPopupMenuMouseAdapter(subformtbl));
		subformtbl.addMouseListener(new DoubleClickMouseAdapter());
		subformtbl.addMouseMotionListener(new URIMouseAdapter());
		subformtbl.addMouseListener(new URIMouseAdapter());
		contentPane.add(scrollPane, BorderLayout.CENTER);

		if (entityName == null) {
			throw new NullArgumentException("entityName");
		}
		this.entityName = entityName;
		if (toolBarOrientation == -1) {
			this.toolbar.setVisible(false);
		} else {
			this.toolbar.setOrientation(toolBarOrientation);
		}
		this.foreignKeyFieldToParent = foreignKeyFieldToParent;
		this.collectableComponentFactory = CollectableComponentFactory.getInstance();
		layer = new JXLayer<JComponent>(contentPane, new TranslucentLockableUI());
		layer.setName("JXLayerGlasspane");
		add(layer);

		toolbarButtons = new HashMap<String, AbstractButton>();
		toolbarMenuItems = new HashMap<String, JMenuItem>();
		toolbarOrder = new ArrayList<String>();
		for(ToolbarFunction func : ToolbarFunction.values()) {
			AbstractButton button = func.createButton();
			JMenuItem mi = func.createMenuItem();
			toolbarButtons.put(func.name(), button);
			toolbarMenuItems.put(func.name(), mi);
			toolbar.add(button);
			button.addActionListener(this);
			mi.addActionListener(this);
			toolbarOrder.add(func.name());
		}

		setToolbarFunctionState(ToolbarFunction.REMOVE, ToolbarFunctionState.DISABLED);
		setToolbarFunctionState(ToolbarFunction.MULTIEDIT, ToolbarFunctionState.HIDDEN);
		setToolbarFunctionState(ToolbarFunction.FILTER, ToolbarFunctionState.HIDDEN);

		this.init();

		assert this.getForeignKeyFieldToParent() == foreignKeyFieldToParent;
	}
	
	@Override
	public final void close() {
		// Close is needed for avoiding memory leaks
		// If you want to change something here, please consult me (tp).  
		if (!closed) {
			LOG.info("close(): " + this);
			if (rowHeader != null) {
				rowHeader.close();
			}
			rowHeader = null;
			if (subformtbl != null) {
				subformtbl.close();
			}
			subformtbl = null;
			if (subformfilter != null) {
				subformfilter.close();
			}
			subformfilter = null;
			for (Pair<JComponent,MouseListener> p: myMouseListener) {
				p.getX().removeMouseListener(p.getY());
			}
			myMouseListener.clear();
			
			// Partial fix for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7079260
			popupMenuAdapter = null;
			scrollPane = null;
			mpStaticColumnEditors.clear();
			lstFocusActionListener.clear();
			
			closed = true;
		}
	}
	
	public void addColumnModelListener(TableColumnModelListener tblcolumnlistener) {
		subformtbl.getColumnModel().addColumnModelListener(tblcolumnlistener);
	}

	public void addSubFormToolListener(SubFormToolListener l) {
		listeners.add(l);
	}

	public void removeSubFormToolListener(SubFormToolListener l) {
		listeners.remove(l);
	}

	public void removeAllSubFormToolListeners() {
		listeners.clear();
	}

	public JScrollPane getSubformScrollPane() {
		return this.scrollPane;
	}
	
	public void actionPerformed(String actionCommand) {
		if(actionCommand != null)
			for(SubFormToolListener l : new ArrayList<SubFormToolListener>(listeners))
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

	public JToolBar getToolbar() {
		return toolbar;
	}

	public int getToolbarOrientation() {
		return toolbar.getOrientation();
	}

	public Rectangle getToolbarBounds() {
		return toolbar.getBounds();
	}

	public Collection<Column> getColumns() {
		return this.mpColumns.values();
	}

	public Collection<String> getColumnNames() {
		return this.mpColumns.keySet();
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

	private void init() {
		contentPane.add(toolbar,
			toolbar.getOrientation() == JToolBar.HORIZONTAL
			? BorderLayout.NORTH
			: BorderLayout.WEST);

		// Configure table
		scrollPane.getViewport().setBackground(subformtbl.getBackground());
		subformtbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		subformtbl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		subformtbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.getViewport().setView(subformtbl);
		JLabel labCorner = new JLabel();
		labCorner.setEnabled(false);
		labCorner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
		labCorner.setBackground(Color.LIGHT_GRAY);
		scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, labCorner);

		rowHeader = createTableRowHeader(subformtbl, scrollPane);
		subformtbl.setRowHeaderTable(rowHeader);
		
		// subformtbl.addMouseListener(newToolbarContextMenuListener(subformtbl, subformtbl));
		addToolbarMouseListener(subformtbl, subformtbl, subformtbl);
		// scrollPane.getViewport().addMouseListener(newToolbarContextMenuListener(scrollPane.getViewport(), subformtbl));
		addToolbarMouseListener(scrollPane.getViewport(), scrollPane.getViewport(), subformtbl);		
	}
	
	private void addToolbarMouseListener(JComponent src, JComponent parent, JTable table) {
		final MouseListener ml = newToolbarContextMenuListener(parent, table);
		src.addMouseListener(ml);
		myMouseListener.add(new Pair<JComponent,MouseListener>(src, ml));
	}
	
	/**
	 * @Deprecated Never use this directly, instead use {@link #addToolbarMenuItems(List)}.
	 */
	private MouseListener newToolbarContextMenuListener(final JComponent parent, final JTable table) {
		MouseListener res = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mev) {
				if (SwingUtilities.isRightMouseButton(mev)) {
					List<JComponent> items = new ArrayList<JComponent>();
					addToolbarMenuItems(items);
					if (items.isEmpty())
						return;
					
					JPopupMenu popup = new JPopupMenu();
					for (JComponent c : items)
						popup.add(c);
					popup.show(parent, mev.getX(), mev.getY());
				}
				if (SwingUtilities.isLeftMouseButton(mev) && mev.getClickCount() == 2) {
					int row = table.rowAtPoint(mev.getPoint());
					int column = table.columnAtPoint(mev.getPoint());
					LOG.info(StringUtils.concat("Doubleclick on subform: column=",column,",row=",row));
					if (row == -1 || column == -1) {
						if (toolbarMenuItems.get(ToolbarFunction.NEW.name()).isEnabled()) {
							actionPerformed(ToolbarFunction.NEW.name());
						}
					}
				}
			}
			
		};
		return res;
	}

	public final void setupTableFilter(CollectableFieldsProviderFactory collectableFieldsProviderFactory) {
		// get columnmodels of fixed and external tables
		TableColumnModel fixedColumnModel = getSubformRowHeader().getHeaderTable().getColumnModel();
		TableColumnModel externalColumnModel = getSubformRowHeader().getExternalTable().getColumnModel();

		// get fixed and external tables
		JTable fixedTable = getSubformRowHeader().getHeaderTable();
		SubFormTable externalTable = getSubformRowHeader().getExternalTable();

		// setup subform filter
		subformfilter = new SubFormFilter(this, fixedTable, fixedColumnModel, externalTable, externalColumnModel, 
				(JToggleButton) toolbarButtons.get(ToolbarFunction.FILTER.name()), 
				(JCheckBoxMenuItem) toolbarMenuItems.get(ToolbarFunction.FILTER.name()), collectableFieldsProviderFactory);

		// add subformfilter for fixed columns
		Component corner = scrollPane.getCorner(JScrollPane.UPPER_LEFT_CORNER );
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.setName("CornerPanel");
		panel2.add(corner, BorderLayout.CENTER);
		panel2.add(subformfilter.getFixedSubFormFilter(), BorderLayout.NORTH);
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, panel2);

		// add subformfilter for external table
		Component origColumnHeader = scrollPane.getColumnHeader().getView();
		JPanel panel1 = new JPanel(new BorderLayout());
		panel1.setName("ColumnHeader");
		panel1.add(origColumnHeader, BorderLayout.CENTER);
		panel1.add(subformfilter.getExternalSubFormFilter(), BorderLayout.NORTH);
		scrollPane.setColumnHeaderView(panel1);
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

	public SubFormFilter getSubFormFilter() {
		return this.subformfilter;
	}

	public void storeTableFilter(String parentEntityName) {
		getSubFormFilter().storeTableFilter(parentEntityName);
	}

	public void loadTableFilter(String parentEntityName) {
		getSubFormFilter().loadTableFilter(parentEntityName);
	}

	/**
	 * @param tbl the table to add the header to
	 * @param scrlpnTable @todo what is this?
	 */
	protected SubformRowHeader createTableRowHeader(final SubFormTable tbl, JScrollPane scrlpnTable) {
		final SubformRowHeader res = new SubformRowHeader(tbl, scrlpnTable);
		return res;
	}

	public void setTableRowHeader(SubformRowHeader newTableRowHeader) {
		this.rowHeader = newTableRowHeader;
		this.subformtbl.setRowHeaderTable(rowHeader);
		newTableRowHeader.setExternalTable(subformtbl, scrollPane);
		
		// rowHeader.getHeaderTable().addMouseListener(newToolbarContextMenuListener(rowHeader.getHeaderTable(), rowHeader.getHeaderTable()));
		addToolbarMouseListener(rowHeader.getHeaderTable(), rowHeader.getHeaderTable(), rowHeader.getHeaderTable());
		// scrollPane.getRowHeader().addMouseListener(newToolbarContextMenuListener(scrollPane.getRowHeader(), rowHeader.getHeaderTable()));
		addToolbarMouseListener(scrollPane.getRowHeader(), scrollPane.getRowHeader(), rowHeader.getHeaderTable());
	}

	public SubformRowHeader getSubformRowHeader() {
		return this.rowHeader;
	}

	public JTable getJTable() {
		return this.subformtbl;
	}

	public void setRowHeight(int iHeight) {
		this.subformtbl.setRowHeight(iHeight);
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension result = super.getMinimumSize();
		result.height = Math.max(result.height, 100);

		return result;
	}

	/**
	 * @return the name of this subform's entity.
	 * @postcondition result != null
	 */
	public String getEntityName() {
		return this.entityName;
	}

	/**
	 * @return the foreign key field that references the parent object. May be null for convenience. In that case,
	 * the SubFormController will try to find the field from the field meta information. This is only possible if
	 * there is only one field in the subform's entity that references the parent entity.
	 */
	public final String getForeignKeyFieldToParent() {
		return this.foreignKeyFieldToParent;
	}


	/**
	 * Method needed for WYSIWYG Editor
	 * NUCLEUSINT-265
	 * @return
	 */
	public SubFormTable getSubformTable() {
		return this.subformtbl;
	}

	public TableCellEditorProvider getTableCellEditorProvider() {
		return this.subformtbl.getTableCellEditorProvider();
	}

	public void setTableCellEditorProvider(TableCellEditorProvider celleditorprovider) {
		this.subformtbl.setTableCellEditorProvider(celleditorprovider);
	}

	public TableCellRendererProvider getTableCellRendererProvider() {
		return this.subformtbl.getTableCellRendererProvider();
	}

	public void setTableCellRendererProvider(TableCellRendererProvider cellrendererprovider) {
		this.subformtbl.setTableCellRendererProvider(cellrendererprovider);
	}

	public void endEditing() {
		if (subformtbl.getCellEditor() != null) {
			subformtbl.getCellEditor().stopCellEditing();
		}
		this.rowHeader.endEditing();
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		enabled = enabled && !readonly;
		super.setEnabled(enabled);
		setToolbarFunctionState(ToolbarFunction.NEW,
				enabled ? ToolbarFunctionState.ACTIVE : ToolbarFunctionState.DISABLED);
		setToolbarFunctionState(ToolbarFunction.MULTIEDIT,
			uniqueMasterColumnName != null
			? (enabled ? ToolbarFunctionState.ACTIVE : ToolbarFunctionState.DISABLED)
			: ToolbarFunctionState.HIDDEN);
		setToolbarFunctionState(ToolbarFunction.TRANSFER,
			toolbarButtons.get(ToolbarFunction.TRANSFER.name()).isVisible()
			? (enabled ? ToolbarFunctionState.ACTIVE : ToolbarFunctionState.DISABLED)
			: ToolbarFunctionState.HIDDEN);
	}
	
	public void setNewEnabled(ScriptContext sc) {
		boolean enabled = isEnabled();
		if (enabled && getNewEnabledScript() != null) {
			Object o = ScriptEvaluator.getInstance().eval(getNewEnabledScript(), sc, enabled);
			if (o instanceof Boolean) {
				enabled = (Boolean) o;
			}
		}
		setToolbarFunctionState(ToolbarFunction.NEW, enabled ? ToolbarFunctionState.ACTIVE : ToolbarFunctionState.DISABLED);
	}
	
	public void setReadOnly(boolean readonly) {
		this.readonly = readonly;
	}
	
	public boolean isReadOnly() {
		return readonly;
	}

	public void setCollectableComponentFactory(CollectableComponentFactory collectableComponentFactory) {
		this.collectableComponentFactory = collectableComponentFactory;
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
	 * fires a <code>ChangeEvent</code> whenever the model of this <code>SubForm</code> changes.
	 */
	public synchronized void fireStateChanged() {
		if(layer == null || (layer != null && !((LockableUI) layer.getUI()).isLocked())){
			final ChangeEvent ev = new ChangeEvent(this);
			for (ChangeListener changelistener : lstchangelistener) {
				changelistener.stateChanged(ev);
			}
		}
	}

	public void fireFocusGained() {
		AWTEvent event = EventQueue.getCurrentEvent();
		if(event instanceof KeyEvent) {
			if(getJTable().getModel().getRowCount() > 0) {
				getJTable().editCellAt(0, 0);
				getSubformTable().changeSelection(0, 0, false, false);
			}
			else if(getJTable().getModel().getRowCount() == 0) {
				for(FocusActionListener fal : getFocusActionLister()) {
					fal.focusAction(new EventObject(this));
					if (getJTable().editCellAt(0, 0)) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								Component editor = getJTable().getEditorComponent();
								editor.requestFocusInWindow();
							}
						});

					}
				}
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

	/**
	 * Handling of intial sorting order (from layoutml).
	 *
	 * @deprecated Sorting order is persisted into user preferences, so why is this? (tp)
	 */
	public String getInitialSortingColumn() {
		return this.sInitialSortingColumn;
	}

	public String getInitialSortingOrder() {
		return this.sInitialSortingOrder;
	}

	public void setInitialSortingOrder(String sColumnName, String sInitialSortingOrder) {
		this.sInitialSortingColumn = sColumnName;
		this.sInitialSortingOrder = sInitialSortingOrder;
	}

	/**
	 * @param sColumnName
	 * @return the <code>CollectableComponentType</code> of the column with the given name (default: null).
	 */
	public CollectableComponentType getCollectableComponentType(String sColumnName, boolean bSearchable) {
		final Column column = this.getColumn(sColumnName);
		CollectableComponentType result = (column != null) ? column.getCollectableComponentType() : null;

		return result;
	}

	/**
	 * @param sColumnName
	 * @return Is the column with the given name visible? (default: true)
	 */
	public boolean isColumnVisible(String sColumnName) {
		final Column column = this.getColumn(sColumnName);
		return (column == null) || column.isVisible();
	}

	/**
	 * @param sColumnName
	 * @return Is the column with the given name enabled? (default: true)
	 */
	public boolean isColumnEnabled(String sColumnName) {
		final Column column = this.getColumn(sColumnName);
		return (column == null) ? NuclosEOField.getByField(sColumnName) == null : column.isEnabled();
	}

	/**
	 * @param sColumnName
	 * @return Is the column with the given name insertable? (default: false)
	 */
	public boolean isColumnInsertable(String sColumnName) {
		final Column column = this.getColumn(sColumnName);
		return (column != null) && column.isInsertable();
	}

	public String getColumnLabel(String sColumnName) {
		final Column column = this.getColumn(sColumnName);
		return (column != null) ? column.getLabel() : null;
	}

	/**
	 * Returns the column width from the layout, or null if unspecified.
	 */
	public Integer getColumnWidth(String cColumnName) {
		final Column column = this.getColumn(cColumnName);
		return (column != null) ? column.getWidth() : null;
	}

	/**
	 * @param sColumnName
	 * @return Collection<TransferLookedUpValueAction> the TransferLookedUpValueActions defined for the column with the given name (default: empty collection).
	 */
	public Collection<TransferLookedUpValueAction> getTransferLookedUpValueActions(String sColumnName) {
		// look-up actions belong to the column that perform the look-up (i.e. the LOV/combobox)
		final Column column = this.getColumn(sColumnName);
		return (column != null) ? column.getTransferLookedUpValueActions() : Collections.<TransferLookedUpValueAction>emptySet();
	}

	/**
	 * @param sColumnName
	 * @return Collection<ClearAction> the ClearActions defined for the column with the given name (default: empty collection).
	 */
	public Collection<ClearAction> getClearActions(String sColumnName) {
		// clear actions belong to the column that perform the look-up (i.e. the LOV/combobox)
		final Column column = this.getColumn(sColumnName);
		return (column != null) ? column.getClearActions() : Collections.<ClearAction>emptySet();
	}

	/**
	 * @param sColumnName
	 * @return Collection<RefreshValueListAction> the RefreshValueListActions defined for the column with the given name (default: empty collection).
	 */
	public Collection<RefreshValueListAction> getRefreshValueListActions(String sColumnName) {
		// refresh actions belong to the column that needs the refresh (_not_ to the column that triggers the refresh)
		final Column column = this.getColumn(sColumnName);
		return (column != null) ? column.getRefreshValueListActions() : Collections.<RefreshValueListAction>emptySet();
	}

	public CollectableFieldsProvider getValueListProvider(String sColumnName) {
		final Column column = this.getColumn(sColumnName);
		return (column != null) ? column.getValueListProvider() : null;
	}

	public SizeKnownListener getSizeKnownListener() {
		return sizeKnownListener;
	}

	public void setSizeKnownListener(SizeKnownListener skl) {
		sizeKnownListener = skl;
	}

	/**
	 * @see #getUniqueMasterColumnName()
	 */
	public void setUniqueMasterColumnName(String uniqueMasterColumnName) {
		this.uniqueMasterColumnName = uniqueMasterColumnName;

		if(uniqueMasterColumnName != null)
			setToolbarFunctionState(ToolbarFunction.MULTIEDIT, isEnabled() ? ToolbarFunctionState.ACTIVE : ToolbarFunctionState.DISABLED);
		else
			setToolbarFunctionState(ToolbarFunction.MULTIEDIT,  ToolbarFunctionState.HIDDEN);
	}

	/**
	 * @return the name of the unique master column, if any. This is used for the multi edit facility.
	 */
	public String getUniqueMasterColumnName() {
		return this.uniqueMasterColumnName;
	}


	/**
	 * set the parent subform for this subform, if the data of this subform depends
	 * on the selected row of the parent subform instead on the curent collectable
	 * @param sSubFormParent
	 */
	public void setParentSubForm(String sSubFormParent) {
		this.sSubFormParent = sSubFormParent;
	}

	/**
	 * @return parent subform for this subform
	 */
	public String getParentSubForm() {
		return this.sSubFormParent;
	}

	/**
	 * sets the controller type for this subform. This is used to create an appropriate controller for this subform.
	 * @param sControllerType
	 */
	public void setControllerType(String sControllerType) {
		this.sControllerType = sControllerType;
	}

	/**
	 * @return the controller type for this subform. This is used to create an appropriate controller for this subform.
	 * null == "default": Use default controller. Otherwise use a special controller.
	 */
	public String getControllerType() {
		return this.sControllerType;
	}

	/**
	 * @return this subform's toolbar containing the buttons for adding or deleting rows. The toolbar may be customized
	 * for specific needs.
	 */
//	public JToolBar getToolBar() {
//		return this.toolbar;
//	}

	public PopupMenuMouseAdapter getPopupMenuAdapter() {
		return popupMenuAdapter;
	}

	public void setPopupMenuAdapter(PopupMenuMouseAdapter popupMenuAdapter) {
		this.popupMenuAdapter = popupMenuAdapter;
	}

	@Override
    public final TableCellRenderer getTableCellRenderer(CollectableEntityField clctef) {
		return this.mpColumnRenderer.get(clctef);
	}

	public final void setupTableCellRenderers(CollectableEntity clcte, CollectableEntityFieldBasedTableModel subformtblmdl, CollectableFieldsProviderFactory clctfproviderfactory, boolean bSearchable) {
		// setup a table cell renderer for each column:
		for (int iColumnNr = 0; iColumnNr < subformtblmdl.getColumnCount(); iColumnNr++) {
			final CollectableEntityField clctef = subformtblmdl.getCollectableEntityField(iColumnNr);

			final String sColumnName = clctef.getName();
			final CollectableComponentType clctcomptype;
			if (sColumnName.equals(this.getForeignKeyFieldToParent())) {
				// in some cases a column for this foreign key field is created. But if it is a ComboBox, refreshValueList() later will load all values from server... bad thing...
				clctcomptype = new CollectableComponentType(CollectableComponentTypes.TYPE_LISTOFVALUES, null);
			} else {
				clctcomptype = this.getCollectableComponentType(sColumnName, bSearchable);
			}

			final CollectableComponent clctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(clcte, sColumnName, clctcomptype, bSearchable);
			if (/*clctef.isIdField() && clctef.isReferencing()&&*/clctcomp instanceof LabeledCollectableComponentWithVLP && !isDynamicTableCellEditorNeeded(clctef.getName())) {
				LabeledCollectableComponentWithVLP clctWithVLP = (LabeledCollectableComponentWithVLP) clctcomp;
				CollectableFieldsProvider valuelistprovider = getValueListProvider(sColumnName);
				if (valuelistprovider == null) {
					valuelistprovider = clctfproviderfactory.newDefaultCollectableFieldsProvider(clcte.getName(), clctWithVLP.getFieldName());
				}
				clctWithVLP.setValueListProvider(valuelistprovider);
				clctWithVLP.refreshValueList(true);
			}
			final Column subformcolumn = this.getColumn(sColumnName);
			if (subformcolumn != null) {
				final Integer iRows = subformcolumn.getRows();
				if (iRows != null) {
					clctcomp.setRows(iRows);
				}
				final Integer iColumns = subformcolumn.getColumns();
				if (iColumns != null) {
					clctcomp.setColumns(iColumns);
				}
			}
			clctcomp.setToolTipText(clctef.getDescription());
			
			final JComponent c = clctcomp.getJComponent();
			if (c instanceof LabeledComponent) {
				((LabeledComponent) c).getLabeledComponentSupport().setColorProvider(null);
			}
			mpColumnRenderer.put(clctef, clctcomp.getTableCellRenderer(true));
		}
	}

	private CollectableComponentType getTypeFromClassField(SubFormTableModel tableModel, String fieldname, int rowIndex) {
		int typeId = CollectableComponentTypes.TYPE_TEXTFIELD;

		JTable table = getJTable();
		Object oValue = table.getModel().getValueAt(rowIndex, tableModel.findColumnByFieldName(fieldname));

		try {
			typeId = CollectableUtils.getCollectableComponentTypeForClass(Class.forName(oValue.toString()));
		}
		catch(ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}

		return new CollectableComponentType(typeId,null);
	}

	private Class<?> getClassFromFieldname(SubFormTableModel tableModel, String fieldName, int rowIndex) throws ClassNotFoundException {
		JTable table = getJTable();
		Object oValue = table.getModel().getValueAt(rowIndex, tableModel.findColumnByFieldName(fieldName));

		return Class.forName(oValue.toString());
	}

	/**
	 * sets all column widths to user preferences; set optimal width if no preferences yet saved
	 * @param tableColumnWidthsFromPreferences
	 */
	public final void setColumnWidths(List<Integer> tableColumnWidthsFromPreferences) {
		useCustomColumnWidths = !tableColumnWidthsFromPreferences.isEmpty() && tableColumnWidthsFromPreferences.size() <= subformtbl.getColumnCount();
		if (useCustomColumnWidths) {
			assert(tableColumnWidthsFromPreferences.size() <= subformtbl.getColumnCount());
			final Enumeration<TableColumn> enumeration = subformtbl.getColumnModel().getColumns();
			int iColumn = 0;
			while (enumeration.hasMoreElements()) {
				final TableColumn column = enumeration.nextElement();
				final int iPreferredCellWidth;
				if (iColumn < tableColumnWidthsFromPreferences.size()) {
					// known column
					iPreferredCellWidth = tableColumnWidthsFromPreferences.get(iColumn++);
				} else {
					// new column
					iPreferredCellWidth = getDefaultColumnWidth(column, iColumn++);
				}
				column.setPreferredWidth(iPreferredCellWidth);
				column.setWidth(iPreferredCellWidth);
			}
		}
		else {
			resetDefaultColumnWidths();
		}
	}

	public final void resetDefaultColumnWidths() {
		for (int iColumn = 0; iColumn < subformtbl.getColumnCount(); iColumn++) {
			final TableColumn tableColumn = subformtbl.getColumnModel().getColumn(iColumn);
			final int width = getDefaultColumnWidth(tableColumn, iColumn);
			tableColumn.setPreferredWidth(width);
			tableColumn.setWidth(width);
		}
		useCustomColumnWidths = false;
		subformtbl.revalidate();
	}
	
	private int getDefaultColumnWidth(TableColumn tc, int iColumn) {
		final Integer preferredCellWidth = getColumnWidth("" + tc.getIdentifier());
		final int width = (preferredCellWidth != null)
			? preferredCellWidth
			: Math.max(TableUtils.getPreferredColumnWidth(subformtbl, iColumn, 50, TableUtils.TABLE_INSETS), subformtbl.getSubFormModel().getMinimumColumnWidth(iColumn));
		return width;
	}

	public final void setupTableModelListener() {
		this.tblmdllistener = new TableModelListener() {
			@Override
            public void tableChanged(TableModelEvent ev) {
				// recalculate the optimal column widths when custom column widths are not used yet and
				// a row is inserted (the first time):
				//@todo: this here is entered not only the first time a row is entered but also on startup (sort fires a tableDataChanged)
				if (!useCustomColumnWidths) {
					// Now this is an example for an API that sucks :(
					final boolean bRowsInserted = (ev.getType() == TableModelEvent.INSERT) ||
							// @todo: The first condition (INSERT) is clear, but what for the second (complete UPDATE)?
							(ev.getType() == TableModelEvent.UPDATE && ev.getColumn() == TableModelEvent.ALL_COLUMNS && ev.getLastRow() == Integer.MAX_VALUE);
					if (bRowsInserted) {
						resetDefaultColumnWidths();
						LOG.debug("Custom column widths should be used here.");	// Setting them manually.");
					}
				}
				// TableModelEvents caused by sorting don't change the subform state:
				if (!(ev instanceof SortableTableModelEvent)) {
					fireStateChanged();
				}
			}
		};
		subformtbl.getModel().addTableModelListener(this.tblmdllistener);
	}

	public void removeTableModelListener() {
		subformtbl.getModel().removeTableModelListener(this.tblmdllistener);
		this.tblmdllistener = null;
	}

	public final void setupColumnModelListener() {
		this.columnmodellistener = new TableColumnModelAdapter() {
			@Override
            public void columnMoved(TableColumnModelEvent ev) {
				// workaround for JTable flaw:
				if (subformtbl.isEditing())
					subformtbl.getCellEditor().stopCellEditing();
			}
			@Override
			public void columnMarginChanged(ChangeEvent e) {
				useCustomColumnWidths = true;
			};
		};
		subformtbl.getColumnModel().addColumnModelListener(this.columnmodellistener);
	}

	public void removeColumnModelListener() {
		subformtbl.getColumnModel().removeColumnModelListener(this.columnmodellistener);
		this.columnmodellistener = null;
	}

	/**
	 * Implementation of <code>TableCellEditorProvider</code>.
	 * @param tbl
	 * @param iRow row of the table (not the table model)
	 * @param clcte
	 * @param clctefTarget
	 * @param subformtblmdl
	 * @param bSearchable
	 * @param prefs
	 * @param getCollectableFieldsProviderFactory
	 * @param parameterProvider
	 * @return a <code>TableCellEditor</code> for columns that need a dynamic <code>TableCellEditor</code>.
	 * <code>null</code> for all other columns.
	 * @todo move back to SubFormController
	 */
	public TableCellEditor getTableCellEditor(JTable tbl, int iRow, CollectableEntity clcte, CollectableEntityField clctefTarget,
			final SubFormTableModel subformtblmdl, boolean bSearchable, Preferences prefs,
			CollectableFieldsProviderFactory getCollectableFieldsProviderFactory, SubFormParameterProvider parameterProvider) {
		CollectableComponentTableCellEditor result;

		final String sColumnNameTarget = clctefTarget.getName();

		if (sColumnNameTarget.equals("entityfieldDefault")) {

			try {
				CollectableEntityField newEntityField =
					new DefaultCollectableEntityField(
						clctefTarget.getName(),
						getClassFromFieldname(subformtblmdl, "datatype", iRow),
						clctefTarget.getLabel(),
						clctefTarget.getDescription(),
						clctefTarget.getMaxLength(),
						clctefTarget.getPrecision(),
						clctefTarget.isNullable(),
						clctefTarget.getFieldType(),
						clctefTarget.getFormatInput(),
						clctefTarget.getFormatOutput(),
						clcte.getName(),
						clctefTarget.getDefaultComponentType());

				CollectableComponentType type = getTypeFromClassField(subformtblmdl, "datatype", iRow);
				CollectableComponent clctcomp = DefaultCollectableComponentFactory.getInstance().newCollectableComponent(newEntityField, type, bSearchable);

				result = createTableCellEditor(clctcomp);
			}
			catch (ClassNotFoundException ex){
				result = this.mpStaticColumnEditors.get(clctefTarget);
			}
		}
		else if (!this.isDynamicTableCellEditorNeeded(sColumnNameTarget)) {
			result = this.mpStaticColumnEditors.get(clctefTarget);
		}
		else {
			// We need a dynamic CellEditor:
			result = this.newTableCellEditor(clcte, sColumnNameTarget, bSearchable, prefs, subformtblmdl);

			final CollectableComponent ccmp = result.getCollectableComponent();
			// CollectableComponent may be LOV when validity tolerance mode is activated
			if(ccmp instanceof LabeledCollectableComponentWithVLP) {
				final LabeledCollectableComponentWithVLP clctWithVLP = (LabeledCollectableComponentWithVLP) ccmp;

				final Collection<RefreshValueListAction> collRefreshValueListActions = getRefreshValueListActions(sColumnNameTarget);
				assert !collRefreshValueListActions.isEmpty();

				// check that the value list provider was not set:
				assert clctWithVLP.getValueListProvider() == null;

				// set the value list provider (dynamically):
				CollectableFieldsProvider valuelistprovider = this.getValueListProvider(sColumnNameTarget);
				if (valuelistprovider == null) {
					// If no provider was set, use the dependant provider for dynamic cell editors by default:
					valuelistprovider = getCollectableFieldsProviderFactory.newDependantCollectableFieldsProvider(this.getEntityName(), clctWithVLP.getEntityField().getName());
				}
				clctWithVLP.setValueListProvider(valuelistprovider);

				// set parameters:
				for (RefreshValueListAction rvlact : collRefreshValueListActions) {
					setParameterForRefreshValueListAction(rvlact, iRow, clctWithVLP, subformtblmdl, parameterProvider);
				}

				// refresh value list:
				clctWithVLP.refreshValueList(false);
			}
		}

		return result;
	}

	public static void setParameterForRefreshValueListAction(RefreshValueListAction rvlact, int iRow,
		LabeledCollectableComponentWithVLP clctWithVLP, SubFormTableModel subformtblmdl, SubFormParameterProvider parameterProvider) {
		final String sParentComponentName = rvlact.getParentComponentName();
		final String sParentComponentEntityName = rvlact.getParentComponentEntityName();
		final CollectableField clctfParent = parameterProvider.getParameterForRefreshValueList(subformtblmdl, iRow, sParentComponentName, sParentComponentEntityName);
		Object oValue = (clctfParent.getFieldType() == CollectableField.TYPE_VALUEIDFIELD) ? clctfParent.getValueId() : clctfParent.getValue();
		clctWithVLP.getValueListProvider().setParameter(rvlact.getParameterNameForSourceComponent(), oValue);
	}

	/**
	 * create a Teblecelleditor for the given CollectableEntityField
	 * @param clcte
	 * @param sColumnName
	 * @param bSearchable
	 * @param prefs
	 * @param subformtblmdl
	 * @return the cell editor
	 */
	protected final CollectableComponentTableCellEditor newTableCellEditor(CollectableEntity clcte,
			String sColumnName, final boolean bSearchable, Preferences prefs, final SubFormTableModel subformtblmdl) {

		final CollectableComponent clctcomp = newCollectableComponent(clcte, sColumnName, bSearchable, prefs);
		final CollectableComponentTableCellEditor result = createTableCellEditor(clctcomp);

		// implement TransferLookedUpValueActions for LOVs:
		if (clctcomp instanceof CollectableListOfValues) {
			final CollectableListOfValues clctlov = (CollectableListOfValues) clctcomp;
			for (LookupListener lookup : lookupListener) {
				clctlov.addLookupListener(lookup);
			}

			final Collection<TransferLookedUpValueAction> collTransferValueActions = getTransferLookedUpValueActions(sColumnName);
			if (!collTransferValueActions.isEmpty()) {
				clctlov.addLookupListener(new LookupListener() {
					@Override
					public void lookupSuccessful(LookupEvent ev) {
						transferLookedUpValues(ev.getSelectedCollectable(), subformtblmdl, bSearchable, subformtbl.getEditingRow(), collTransferValueActions);
					}

					@Override
                    public int getPriority() {
	                    return 1;
                    }
				});
			}

			final Collection<ClearAction> collClearActions = getClearActions(sColumnName);
			if (!collClearActions.isEmpty()) {
				clctlov.addLookupListener(new LookupListener() {
					@Override
					public void lookupSuccessful(LookupEvent ev) {
						clearValues(subformtblmdl, subformtbl.getEditingRow(), collClearActions);
					}

					@Override
                    public int getPriority() {
	                    return 1;
                    }
				});
			}
		//} else if (clctcomp instanceof CollectableComboBox) {
		} else {
			final Collection<ClearAction> collClearActions = getClearActions(sColumnName);
			final Collection<TransferLookedUpValueAction> collTransferValueActions = getTransferLookedUpValueActions(sColumnName);
			if (!collClearActions.isEmpty() || !collTransferValueActions.isEmpty() ) {
				// Better alternative: result.addCellEditorListener(new CellEditorListener()) with overridden editingStopped(ChangeEvent e)
				// However, that solution had some issues with the save action and checkbox values which are not resolved...
				result.addCollectableComponentModelListener(new CollectableComponentModelListener() {
					@Override
					public void valueToBeChanged(DetailsComponentModelEvent ev) {
					}

					@Override
					public void searchConditionChangedInModel(SearchComponentModelEvent ev) {
					}

					@Override
					public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
						if (!collClearActions.isEmpty()) {
							clearValues(subformtblmdl, result.getLastEditingRow(), collClearActions);
						}
						if (!collTransferValueActions.isEmpty()) {
							Object id = null;
							try {
								CollectableField value = clctcomp.getField();
								if (value instanceof CollectableValueIdField) {
									id = ((CollectableValueIdField) value).getValueId();
								}
							} catch(CollectableFieldFormatException e1) {
								LOG.warn("collectableFieldChangedInModel failed: " + e1, e1);
							}
							Collectable clct = null;
							String referencedEntity = clctcomp.getEntityField().getReferencedEntityName();
							if (referencedEntity != null) {
								try {
									clct = Utils.getReferencedCollectable(getEntityName(), clctcomp.getFieldName(), id);
								} catch (CommonBusinessException ex) {
									log.error(ex);
								}
							}
							transferLookedUpValues(clct, subformtblmdl, bSearchable, result.getLastEditingRow(), collTransferValueActions);
						}
					}
				});
			}
		}
		return result;
	}

	/**
	 * create a CollectableComponent for the given EntityField
	 * @param clcte
	 * @param sColumnName
	 * @param bSearchable
	 * @param prefs
	 * @return the newly created collectable component
	 */
	private CollectableComponent newCollectableComponent(CollectableEntity clcte, String sColumnName,
			boolean bSearchable, Preferences prefs) {
		final CollectableComponentType clctcomptype = getCollectableComponentType(sColumnName, bSearchable);

		final CollectableComponent result = collectableComponentFactory.newCollectableComponent(clcte, sColumnName, clctcomptype, bSearchable);
		result.setInsertable(bSearchable || isColumnInsertable(sColumnName));
		result.setPreferences(prefs);

		return result;
	}

	/**
	 * create an Tablecelleditor for the given component
	 * @param clctcomp
	 * @return the newly created table cell editor
	 */
	private CollectableComponentTableCellEditor createTableCellEditor(final CollectableComponent clctcomp) {
		final CollectableComponentTableCellEditor result = new CollectableComponentTableCellEditor(clctcomp, clctcomp.isSearchComponent());

		result.addCollectableComponentModelListener(getCollectableTableCellEditorChangeListener());

		return result;
	}

	public CollectableComponentModelAdapter getCollectableTableCellEditorChangeListener() {
		return new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			if (ev.collectableFieldHasChanged()) {
				fireStateChanged();
			}
		}

		@Override
		public void searchConditionChangedInModel(SearchComponentModelEvent ev) {
			fireStateChanged();
		}};
	}

	public final void setupStaticTableCellEditors(final JTable tbl, boolean bSearchable, Preferences prefs,
			final SubFormTableModel subformtblmdl, CollectableFieldsProviderFactory clctfproviderfactory,
			String sParentEntityName, CollectableEntity clcte) {
		// setup a table cell editor for each column:
		// if commented out for NUCLOSINT-1425 (ts, tp):
		// if (this.isEnabled()) 
		{
			final String sForeignKeyFieldName = this.getForeignKeyFieldName(sParentEntityName, clcte);

			for (int iColumnNr = 0; iColumnNr < subformtblmdl.getColumnCount(); iColumnNr++) {

				final CollectableEntityField clctef = subformtblmdl.getCollectableEntityField(iColumnNr);
				final String sColumnName = clctef.getName();

				if (!this.isDynamicTableCellEditorNeeded(sColumnName)) {
					// It may be wrong not to set a cell editor if the subform is invisible or disabled.
					// If the subform is shown or enabled later, the cell editor may be missing.
					// Note that we do not set a cell editor jfor the foreign key field name, as this is never shown.
					assert this.isColumnVisible(sColumnName);
					assert !sColumnName.equals(sForeignKeyFieldName);
					// A static CellEditor is sufficient and can be set right here:
					final CollectableComponentTableCellEditor clctcompcelleditor =
							this.newTableCellEditor(clcte, sColumnName, bSearchable, prefs, subformtblmdl);

					// set a (static) value list provider for ComboBoxes:
					final CollectableComponent clctcomp = clctcompcelleditor.getCollectableComponent();
					clctcomp.setEnabled(isColumnEnabled(sColumnName));
					clctcomp.setToolTipText(clctef.getDescription());
					if (clctcomp instanceof LabeledCollectableComponentWithVLP) {
						final LabeledCollectableComponentWithVLP clctWithVLP = (LabeledCollectableComponentWithVLP) clctcomp;
						assert clctWithVLP.getValueListProvider() == null;

						CollectableFieldsProvider valuelistprovider = getValueListProvider(sColumnName);
						if (valuelistprovider == null) {
							// If no provider was set, use the default provider for static cell editors by default:
							valuelistprovider = clctfproviderfactory.newDefaultCollectableFieldsProvider(clcte.getName(), clctWithVLP.getFieldName());
						}
						clctWithVLP.setValueListProvider(valuelistprovider);
						clctWithVLP.refreshValueList(true);
					}
					mpStaticColumnEditors.put(clctef, clctcompcelleditor);
				}
			}
		}
	}

	/**
	 * Transfers the looked-up values.
	 */
	public static void transferLookedUpValues(Collectable clctSelected, SubFormTableModel subformtblmdl, boolean isSearchable, int iRow, Collection<TransferLookedUpValueAction> collTransferValueActions) {
		// transfer the looked up values:
		for (TransferLookedUpValueAction act : collTransferValueActions) {
			final String sSourceFieldName = act.getSourceFieldName();
			final int iTargetColumn = subformtblmdl.findColumnByFieldName(act.getTargetComponentName());
			if (iTargetColumn == -1) {
				throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage(
						"SubForm.2","Das Unterformular enth\u00e4lt keine Spalte namens {0}", act.getTargetComponentName()));
			}

			final CollectableEntityField clctefTarget = subformtblmdl.getCollectableEntityField(iTargetColumn);
			final Object oValue;
			if (clctSelected == null) {
				oValue = subformtblmdl.getNullValue(clctefTarget);
			}
			else {
				final CollectableField clctfValue = clctSelected.getField(sSourceFieldName);
				assert clctfValue != null;
				oValue = isSearchable ? getSearchConditionForValue(clctefTarget, clctfValue) : clctfValue;
			}
			if (iRow >= 0) {
				subformtblmdl.setValueAt(oValue, iRow, iTargetColumn);
			}
		}
	}

	/**
	 * Clears the given values.
	 */
	private static void clearValues(SubFormTableModel subformtblmdl, int iRow, Collection<ClearAction> collClearActions) {
		// transfer the looked up values:
		for (ClearAction act : collClearActions) {
			final int iTargetColumn = subformtblmdl.findColumnByFieldName(act.getTargetComponentName());
			if (iTargetColumn == -1) {
				throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage(
						"SubForm.2","Das Unterformular enth\u00e4lt keine Spalte namens {0}", act.getTargetComponentName()));
			}
			final CollectableEntityField clctefTarget = subformtblmdl.getCollectableEntityField(iTargetColumn);
			final Object oValue = subformtblmdl.getNullValue(clctefTarget);
			if (iRow >= 0) {
				subformtblmdl.setValueAt(oValue, iRow, iTargetColumn);
			}
		}
	}

	/**
	 * @param clctef
	 * @param clctfValue
	 * @return
	 * @precondition clctfValue != null
	 * @postcondition clctfValue.isNull() --> result == null
	 */
	private static CollectableComparison getSearchConditionForValue(CollectableEntityField clctef, CollectableField clctfValue) {
		if (clctfValue == null) {
			throw new NullArgumentException("clctfValue");
		}
		return clctfValue.isNull() ? null : new CollectableComparison(clctef, ComparisonOperator.EQUAL, clctfValue);
	}

	/**
	 * @param sColumnName
	 * @return Is a dynamic TableCellEditor needed for the column with the given name?
	 * A dynamic TableCellEditor is needed if there is a refresh-possible-values action for this column.
	 */
	public final boolean isDynamicTableCellEditorNeeded(String sColumnName) {
		return !getRefreshValueListActions(sColumnName).isEmpty();
	}

	/**
	 * @return the name of the foreign key field referencing the parent entity
	 * @postcondition result != null
	 */
	public final String getForeignKeyFieldName(String sParentEntityName, CollectableEntity clcte) {

		// Use the field referencing the parent entity from the subform, if any:
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
								"SubForm.4","Das Unterformular f\u00fcr die Entit\u00e4t \"{0}\" enth\u00e4lt mehr als ein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t \"{1}\" referenziert:\n\t{2}\n\t{3}\nBitte geben Sie das Feld im Layout explizit an.", clcte.getName(), sParentEntityName, result, sFieldName);
						throw new CommonFatalException(sMessage);
					}
				}
			}
		}

		if (result == null) {
			throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage(
					"SubForm.3","Das Unterformular f\u00fcr die Entit\u00e4t \"{0}\" enth\u00e4lt kein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t \"{1}\" referenziert.\nBitte geben Sie das Feld im Layout explizit an.", clcte.getName(), sParentEntityName));
		}
		assert result != null;
		return result;
	}



	/**
	 * inner class SubForm.Table.
	 */
	public static class SubFormTable extends CommonJTable
			implements Closeable {

		@Override
		public boolean isRequestFocusEnabled() {
			return true;
		}
		public static class SubFormTableRowSorter extends TableRowSorter<TableModel> {
			SubFormTableRowSorter(TableModel model) {
				super(model);
			}

			@Override
			public boolean isSortable(int column) {
				// At the moment, sorting still is performed via the SortableTableModel
				return false;
			}
		}

		/**
		 * TODO: It would be very nice to have a static inner class here. However,
		 * 		 when I tried this (with a reference to the TableModel in the constructor)
		 * 		 I trashed the WYSIWYG subform editor (e.g. properties is complex Subform 
		 * 		 cases (Accelingua)). (tp)
		 */
		private class SubformTableColumnModel extends DefaultTableColumnModel
				implements Closeable {
			
			private boolean closed = false;

			private SubformTableColumnModel(TableModel model) {
			}

			@Override
			public void close() {
				// Close is needed for avoiding memory leaks
				// If you want to change something here, please consult me (tp).  
				if (!closed) {
					LOG.info("close() SubformTableColumnModel: " + this);
					tableColumns.clear();
					closed = true;
				}
			}

			@Override
			public void addColumn(TableColumn column) {
				if (getModel() instanceof SubFormTableModel) {
					// NUCLEUSINT-742: the identifier of the column is now the entity field name
					// (instead of the localized label)
					String fieldName = ((SubFormTableModel) getModel()).getColumnFieldName(column.getModelIndex());
					column.setIdentifier(fieldName);
				}
				super.addColumn(column);
			}

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				super.propertyChange(evt);
				fireColumnPropertyChange(evt);
			}

			protected void fireColumnPropertyChange(PropertyChangeEvent evt) {
				Object[] listeners = listenerList.getListenerList();
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == TableColumnModelListener.class
							&& listeners[i + 1] instanceof TableColumnModelExtListener) {
						((TableColumnModelExtListener) listeners[i + 1]).columnPropertyChange(evt);
					}
				}
			}
		}

		private TableCellEditorProvider celleditorprovider;
		private TableCellRendererProvider cellrendererprovider;
		private SubForm subform;
		private boolean calculateRowHeight;

		private SubformRowHeader rowheader;

		private boolean newRowOnNext = false;

		// private SubformTableColumnModel myTableColumnModel;
		
		private boolean closed = false;

		public SubFormTable() {
			setCellSelectionEnabled(true);
		}

		private SubFormTable(SubForm sub){
			this();
			this.subform = sub;
		}

		public SubForm getSubForm() {
			return subform;
		}

		@Override
		public void close() {
			// Close is needed for avoiding memory leaks
			// If you want to change something here, please consult me (tp).
			if (!closed) {
				LOG.info("close() SubFormTable: " + this);
				if (getModel() instanceof SubformTableColumnModel) {
					((SubformTableColumnModel) getModel()).close();
				}
				if (getColumnModel() instanceof SubformTableColumnModel) {
					((SubformTableColumnModel) getColumnModel()).close();
				}
				// not allowed by swing
				// setModel(null);
				if (rowheader != null) {
					rowheader.close();
				}
				rowheader = null;
				closed = true;
			}
		}

		@Override
		public void changeSelection(int rowIndex, final int columnIndex, boolean toggle, boolean extend) {
			final AWTEvent event = EventQueue.getCurrentEvent();
			if (event instanceof KeyEvent) {
				final KeyEvent ke = (KeyEvent)event;
	            if(ke.isShiftDown() || ke.isControlDown()) {
	            	newRowOnNext = false;
	            	if (columnIndex  == getColumnCount() - 1)
	            		rowIndex = rowIndex + 1 == getRowCount() ? 0 : rowIndex + 1;
	            }
			}
			changeSelection(rowIndex, columnIndex, toggle, extend, false);

			int colCount = getColumnCount();
			if(columnIndex == colCount-1) {
				if (event instanceof KeyEvent) {
					final KeyEvent ke = (KeyEvent)event;
		            if(!ke.isShiftDown() && !ke.isControlDown())
		            	newRowOnNext = true;
		            else
		            	newRowOnNext = false;
				} else
					newRowOnNext = true;
			}				
		}
		public void changeSelection(final int rowIndex, final int columnIndex, boolean toggle, boolean extend, final boolean fixed) {
			super.changeSelection(rowIndex, columnIndex, toggle, extend);
			final AWTEvent event = EventQueue.getCurrentEvent();
			if(event instanceof KeyEvent || event instanceof InvocationEvent) {
				if(newRowOnNext) {
					if(getRowCount() == 1 && !(event instanceof InvocationEvent)) {
						final KeyEvent ke = (KeyEvent)event;
			            if(!ke.isShiftDown() && !ke.isControlDown()) {
							for(FocusActionListener fal : subform.getFocusActionLister()) {
								fal.focusAction(new EventObject(this));
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										if (editCellAt(rowIndex + 1, columnIndex)) {
											Component editor = getEditorComponent();
											if(editor != null)
												editor.requestFocusInWindow();
										}
									}
								});
							}
			            }
					}
					newRowOnNext = false;
				}
				int colCount = getColumnCount();
				if(columnIndex == colCount-1) {
					if (event instanceof KeyEvent) {
						final KeyEvent ke = (KeyEvent)event;
			            if(!ke.isShiftDown() && !ke.isControlDown())
			            	newRowOnNext = true;
			            else
			            	newRowOnNext = false;
					} else
						newRowOnNext = true;
				}	
				boolean bShift = false;
				if (event instanceof KeyEvent) {
					final KeyEvent ke = (KeyEvent)event;
					if(ke.isShiftDown() || ke.isControlDown()) {
						bShift = true;
					}
				}
				if (!fixed && (columnIndex == 0|| (columnIndex == getColumnCount() - 1 && bShift))) {
					SubformRowHeader rowHeader = getSubForm().getSubformRowHeader();
					boolean blnHasFixedRows = (rowHeader != null && rowHeader.getHeaderTable().getColumnCount() > 1);
					if (blnHasFixedRows) {
						if (!(rowHeader.getHeaderTable() instanceof HeaderTable))
							if (event instanceof KeyEvent) {
								final KeyEvent ke = (KeyEvent)event;
								if(!ke.isShiftDown() && !ke.isControlDown()) {
									rowHeader.getHeaderTable().changeSelection(rowIndex, 0, false, false);
								} else {
									rowHeader.getHeaderTable().changeSelection(rowIndex, rowHeader.getHeaderTable().getColumnCount() - 1, false, false);
								}
							}
							else
								rowHeader.getHeaderTable().changeSelection(rowIndex, 0, false, false);
						else
							if (event instanceof KeyEvent) {
								final KeyEvent ke = (KeyEvent)event;
								if(!ke.isShiftDown() && !ke.isControlDown()) {
									((HeaderTable)rowHeader.getHeaderTable()).changeSelection(rowIndex, 0, false, false, true);
								} else {
									((HeaderTable)rowHeader.getHeaderTable()).changeSelection(rowIndex, rowHeader.getHeaderTable().getColumnCount() - 1, false, false, true);
								}
							}
							else
								((HeaderTable)rowHeader.getHeaderTable()).changeSelection(rowIndex, 0, false, false, true);
						return;
					}
				} 

				if(isCellEditable(rowIndex, columnIndex)) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (event instanceof KeyEvent) {
								final KeyEvent ke = (KeyEvent)event;
					            if(!ke.isShiftDown() && !ke.isControlDown()) {
									if (rowIndex == 0 && columnIndex == 0
											&& !fixed)
										return;
					            }
							} else {
								if (rowIndex == 0 && columnIndex == 0
										&& !fixed)
									return;
							}
							if (editCellAt(rowIndex, columnIndex)) {
								Component editor = getEditorComponent();
								if(editor != null)
									editor.requestFocusInWindow();
							}
						}
					});
				}
				else {
					final int rowCol[] = getNextEditableCell(this, rowIndex, columnIndex, bShift);
					if (isCellEditable(rowCol[0], rowCol[1])) {
						if(!fixed && event instanceof KeyEvent && ((KeyEvent)event).isConsumed())
							return;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								if (editCellAt(rowCol[0], rowCol[1])) {
									Component editor = getEditorComponent();
									if(editor != null) {
										editor.requestFocusInWindow();
										if(rowCol[0] < getRowCount())
											changeSelection(rowCol[0], rowCol[1], false, false);
									}
								}
							}
						});
					}
					else {
						if(newRowOnNext) {
							for(FocusActionListener fal : subform.getFocusActionLister()) {
								fal.focusAction(new EventObject(this));
								final int col[] = getNextEditableCell(this, rowIndex, 0, bShift);
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										if (editCellAt(rowIndex + 1, col[1])) {
											final int rowI = rowIndex;
											Component editor = getEditorComponent();
											if(editor != null)
												editor.requestFocusInWindow();
											changeSelection(rowI, col[1], false, false);
										}
									}
								});
							}
							newRowOnNext = false;
						}
					}
				}
			}

		}

		private int[] getNextEditableCell(JTable table, int row, int col, boolean bReverse) {
			int rowCol[] = {row,col};
			int colCount = getColumnCount();
			boolean colFound = false;
			if (!bReverse) {
				for(int i = col; i < colCount; i++) {
					if(table.isCellEditable(row, i)) {
						colFound = true;
						rowCol[1] = i;
						break;
					}
				}
				if(!colFound) {
					row++;
					if(row >= getRowCount())
						return rowCol;
					for(int i = 0; i < col; i++) {
						if(table.isCellEditable(row, i)) {
							rowCol[0] = row;
							rowCol[1] = i;
							break;
						}
					}
				}
			} else {
				for(int i = col; i > 0; i--) {
					if(table.isCellEditable(row, i)) {
						colFound = true;
						rowCol[1] = i;
						break;
					}
				}
				if(!colFound) {
					row--;
					if(row <= 0)
						return rowCol;
					for(int i = colCount; i > 0; i--) {
						if(table.isCellEditable(row, i)) {
							rowCol[0] = row;
							rowCol[1] = i;
							break;
						}
					}
				}
			}

			return rowCol;
		}
		
		@Override
		protected SubformTableColumnModel createDefaultColumnModel() {
			return new SubformTableColumnModel(getModel());
		}

		public void setRowHeaderTable(SubformRowHeader rowheader) {
			this.rowheader = rowheader;
			// NUCLEUSINT-299: focus mysteriously remained in the
			// toolbar or wherever. Force to the table, when the
			// row header gets a mouse-press.
			rowheader.getHeaderTable().addMouseListener(new MouseAdapter() {
                           @Override
                           public void mousePressed(MouseEvent e) {
                              requestFocus();
                           }});
		}

		public final SubFormTableModel getSubFormModel() {
			return (SubFormTableModel) super.getModel();
		}

		@Override
		public void setBackground(Color color) {
			super.setBackground(color);
			if (rowheader != null && rowheader.getHeaderTable() != null) {
				this.rowheader.getHeaderTable().setBackground(color);
				final Container parent = rowheader.getHeaderTable().getParent();
				if (parent instanceof JViewport) {
					parent.setBackground(color);
				}
			}
		}

		@Override
		public void setTableHeader(JTableHeader tblheader) {
			super.setTableHeader(tblheader);
			configureEnclosingScrollPane();
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			if (getModel() instanceof SubFormTableModel) {
				final int iModelColumn = getColumnModel().getColumn(column).getModelIndex();
				final CollectableEntityField clctefTarget = ((SubFormTableModel) getModel()).getCollectableEntityField(iModelColumn);

				if (clctefTarget.getName().equals("entityfieldDefault")) {
					String datatype = getSubFormModel().getValueAt(row, getSubFormModel().findColumnByFieldName("datatype")).toString();
					return !StringUtils.isNullOrEmpty(datatype) && datatype.startsWith("java");
				}
			}

			return super.isCellEditable(row, column);
		}

		@Override
		public TableCellRenderer getCellRenderer(int iRow, int iColumn) {
			TableCellRenderer result = null;

			if (cellrendererprovider != null && getModel() instanceof SubFormTableModel) {

				final int iModelColumn = getColumnModel().getColumn(iColumn).getModelIndex();
				final CollectableEntityField clctefTarget = ((SubFormTableModel) getModel()).getCollectableEntityField(iModelColumn);

				if (clctefTarget.getName().equals("entityfieldDefault")) {
					String datatype = getSubFormModel().getValueAt(iRow, getSubFormModel().findColumnByFieldName("datatype")).toString();

					if (!StringUtils.isNullOrEmpty(datatype) ) {
						try {
							final Class<?> clazz = Class.forName(getSubFormModel().getValueAt(iRow, getSubFormModel().findColumnByFieldName("datatype")).toString());
							final CollectableEntityField newEntityField =
								new DefaultCollectableEntityField(
									clctefTarget.getName(),
									clazz,
									clctefTarget.getLabel(),
									clctefTarget.getDescription(),
									clctefTarget.getMaxLength(),
									clctefTarget.getPrecision(),
									clctefTarget.isNullable(),
									clctefTarget.getFieldType(),
									clctefTarget.getFormatInput(),
									clctefTarget.getFormatOutput(),
									subform.entityName,
									clctefTarget.getDefaultComponentType());

							return getCellRendererFromClassField(newEntityField, "datatype", iRow);
						}
						catch(ClassNotFoundException e) {
							throw new CommonFatalException(e);
						}
					}
				}
				result = cellrendererprovider.getTableCellRenderer(clctefTarget);
			}

			if (result == null) {
				result = super.getCellRenderer(iRow, iColumn);
			}
			return result;
		}

		private TableCellRenderer getCellRendererFromClassField(CollectableEntityField entityField, String classFieldname, int iRow) {
			int typeId = CollectableComponentTypes.TYPE_TEXTFIELD;

			Object oValue = getSubFormModel().getValueAt(iRow, getSubFormModel().findColumnByFieldName(classFieldname));

			try {
				typeId = CollectableUtils.getCollectableComponentTypeForClass(Class.forName(oValue.toString()));
			}
			catch(ClassNotFoundException e) {
				throw new CommonFatalException(e);
			}

			CollectableComponent comp = CollectableComponentFactory.getInstance().newCollectableComponent(entityField, new CollectableComponentType(typeId,null), false);
			return comp.getTableCellRenderer(true);
		}

		public TableCellEditorProvider getTableCellEditorProvider() {
			return this.celleditorprovider;
		}

		public TableCellRendererProvider getTableCellRendererProvider() {
			return this.cellrendererprovider;
		}

		public void setTableCellEditorProvider(TableCellEditorProvider celleditorprovider) {
			this.celleditorprovider = celleditorprovider;
		}

		public void setTableCellRendererProvider(TableCellRendererProvider cellrendererprovider) {
			this.cellrendererprovider = cellrendererprovider;
		}

		@Override
		public TableCellEditor getCellEditor(int iRow, int iColumn) {
			TableCellEditor result = null;

			if (celleditorprovider != null && getModel() instanceof SubFormTableModel) {

				final int iModelColumn = getColumnModel().getColumn(iColumn).getModelIndex();
				final CollectableEntityField clctefTarget = ((SubFormTableModel) getModel()).getCollectableEntityField(iModelColumn);
				try {
					result = celleditorprovider.getTableCellEditor(this, iRow, clctefTarget);
				}
				catch(NuclosFieldNotInModelException e) {
					// expected exception
					LOG.info("getCellEditor: " + e);
					result = null;
				}
			}
			if (result == null) {
				result = super.getCellEditor(iRow, iColumn);
			}
			return result;
		}

		/**
		 * ensures that this table always fills its enclosing viewport (if any) vertically.
		 * This is useful default behavior (and thus should be defined in JTable already) because of the following reasons:
		 * <ul>
		 * 	<li>allows dropping into the empty space of a table.</li>
		 * 	<li>allows opening a popup menu in the empty space of a table.</li>
		 * 	<li>paints the background color of the table in the empty space, rather than that of the viewport.</li>
		 * </ul>
		 * The alternative each time is to define a transfer handler, a popup menu listener and the background of the enclosing
		 * viewport separately, but that is somewhat awkward.
		 * Code copied from JList.
		 * @todo if this behavior is needed in other places (which it is likely), move to CommonJTable (as optional behavior)
		 */
		@Override
		public boolean getScrollableTracksViewportHeight() {
			if (getParent() instanceof JViewport) {
				return (getParent().getHeight() > getPreferredSize().height);
			}
			return false;
		}

		/**
		 * set the row height in one row (used by the ElisaCollectableTextArea)
		 * @param iRow
		 * @param iRowHeight
		 */
		public void setRowHeightFromTextArea(int iRow, int iRowHeight) {

			if (this.getRowHeight(iRow) != iRowHeight) {
				if (this.getRowCount() > iRow) {
					this.setRowHeight(iRow, iRowHeight);
				}
			}
			if (this.rowheader != null) {
				this.rowheader.setRowHeightInRow(iRow, iRowHeight);
			}
		}

		@Override
		public void setModel(TableModel dataModel) {
			super.setModel(dataModel);
			setRowSorter(new SubFormTableRowSorter(dataModel));
		}
		
		@Override
		public void setColumnModel(TableColumnModel columnModel) {
			final TableColumnModel old = getColumnModel();
			if (old instanceof SubformTableColumnModel) {
				((SubformTableColumnModel) old).close();
			}
			super.setColumnModel(columnModel);
		}

		public boolean isCalculateRowHeight() {
			return calculateRowHeight;
		}

		public void setCalculateRowHeight(boolean calculateRowHeight) {
			this.calculateRowHeight = calculateRowHeight;
		}

	}

	public boolean isUseCustomColumnWidths() {
		return this.useCustomColumnWidths;
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

	public NuclosScript getNewEnabledScript() {
		return newEnabledScript;
	}

	public void setNewEnabledScript(NuclosScript newEnabledScript) {
		this.newEnabledScript = newEnabledScript;
	}

	public NuclosScript getEditEnabledScript() {
		return editEnabledScript;
	}

	public void setEditEnabledScript(NuclosScript editEnabledScript) {
		this.editEnabledScript = editEnabledScript;
	}

	public NuclosScript getDeleteEnabledScript() {
		return deleteEnabledScript;
	}

	public void setDeleteEnabledScript(NuclosScript deleteEnabledScript) {
		this.deleteEnabledScript = deleteEnabledScript;
	}

	public NuclosScript getCloneEnabledScript() {
		return cloneEnabledScript;
	}

	public void setCloneEnabledScript(NuclosScript cloneEnabledScript) {
		this.cloneEnabledScript = cloneEnabledScript;
	}

	/**
	 * <code>TableModel</code> that can be used in a <code>SubForm</code>.
	 */
	public static interface SubFormTableModel extends CollectableEntityFieldBasedTableModel {
		/**
		 * @param clctef
		 * @return a null value that can be set in a table cell for the given entity field.
		 */
		Object getNullValue(CollectableEntityField clctef);

		/**
		 * determines the (visible) columns of this model.
		 * Each column is represented by a <code>CollectableEntityField</code>.
		 * @param lstclctefColumns List<CollectableEntityField>
		 */
		void setColumns(List<? extends CollectableEntityField> lstclctefColumns);

		/**
		 * @param columnIndex
		 * @return an unique identifier for the column (usually the name of the entity field).
		 */
	    public String getColumnFieldName(int columnIndex);

	    /**
	     * @param columnIndex
		 * @return minimum column width based on class
	     */
	    public int getMinimumColumnWidth(int columnIndex);

		/**
		 * @param sFieldName
		 * @return the index of the column with the given fieldname. -1 if none was found.
		 */
		int findColumnByFieldName(String sFieldName);

		void remove(int iRow);

		void remove(int[] rows);

	}	// interface SubFormTableModel


	/**
	 * inner class TransferLookedUpValueAction.
	 * @todo it might be better to define all of these actions in collect.action.
	 */
	public static class TransferLookedUpValueAction {
		private final String sTargetComponentName;
		private final String sSourceFieldName;

		public TransferLookedUpValueAction(String sTargetComponentName, String sSourceFieldName) {
			this.sTargetComponentName = sTargetComponentName;
			this.sSourceFieldName = sSourceFieldName;
		}

		public String getTargetComponentName() {
			return this.sTargetComponentName;
		}

		public String getSourceFieldName() {
			return this.sSourceFieldName;
		}
	}	// inner class TransferLookedUpValueAction

	/**
	 * inner class ClearAction.
	 */
	public static class ClearAction {
		private final String sTargetComponentName;

		public ClearAction(String sTargetComponentName) {
			this.sTargetComponentName = sTargetComponentName;
		}

		public String getTargetComponentName() {
			return this.sTargetComponentName;
		}
	}

	public static class RefreshValueListAction {
		private final String sTargetComponentName;
		private final String sParentComponentEntityName;
		private final String sParentComponentName;
		private final String sParameterNameForSourceComponent;

		/**
		 * @param sTargetComponentName
		 * @param sParentComponentEntityName the entity name of the subform (shared by target component and parent component)
		 *   or the entity name of the form (in which case the parent component lies in the form, outside the subform).
		 * @param sParentComponentName
		 * @param sParameterNameForSourceComponent the name of the parameter in the valuelistprovider for the source component.
		 */
		public RefreshValueListAction(String sTargetComponentName, String sParentComponentEntityName,
				String sParentComponentName, String sParameterNameForSourceComponent) {
			this.sTargetComponentName = sTargetComponentName;
			this.sParentComponentEntityName = sParentComponentEntityName;
			this.sParentComponentName = sParentComponentName;
			this.sParameterNameForSourceComponent = sParameterNameForSourceComponent;
		}

		public String getTargetComponentName() {
			return this.sTargetComponentName;
		}

		public String getParentComponentEntityName() {
			return this.sParentComponentEntityName;
		}

		public String getParentComponentName() {
			return this.sParentComponentName;
		}

		public String getParameterNameForSourceComponent() {
			return this.sParameterNameForSourceComponent;
		}
	}	// inner class RefreshValueListAction

	/**
	 * A column in a <code>SubForm</code>.
	 */
	public static class Column {
		private final String sName;
		private String sLabel;
		private final CollectableComponentType clctcomptype;
		private final boolean bVisible;
		private final boolean bEnabled;
		private final boolean bInsertable;
		private final Integer iRows;
		private final Integer iColumns;
		private final Integer width;
		private final Integer initialPosition;

		/**
		 * Collection<TransferLookedUpValueAction>
		 */
		private Collection<TransferLookedUpValueAction> collTransferLookedUpValueActions;

		/**
		 * Collection<ClearAction>
		 */
		private Collection<ClearAction> collClearActions;

		/**
		 * Collection<RefreshValueListAction>
		 */
		private Collection<RefreshValueListAction> collRefreshValueListActions;

		private CollectableFieldsProvider valuelistprovider;

		public Column(String sName) {
			this(sName, null, new CollectableComponentType(null, null), true, true, false, null, null);
		}

		public Column(String sName, String sLabel, CollectableComponentType clctcomptype, boolean bVisible, boolean bEnabled,
				boolean bInsertable, Integer iRows, Integer iColumns) {
			this(sName, sLabel, clctcomptype, bVisible, bEnabled, bInsertable, iRows, iColumns, null);
		}

		public Column(String sName, String sLabel, CollectableComponentType clctcomptype, boolean bVisible, boolean bEnabled,
			boolean bInsertable, Integer iRows, Integer iColumns, Integer width) {
			this.sName = sName;
			this.sLabel = sLabel;
			this.clctcomptype = clctcomptype;
			this.bVisible = bVisible;
			this.bEnabled = bEnabled;
			this.bInsertable = bInsertable;
			this.iRows = iRows;
			this.iColumns = iColumns;
			this.width = width;
			this.initialPosition = null;
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

		public CollectableComponentType getCollectableComponentType() {
			return this.clctcomptype;
		}

		public boolean isVisible() {
			return this.bVisible;
		}

		public boolean isEnabled() {
			return this.bEnabled;
		}

		public boolean isInsertable() {
			return this.bInsertable;
		}

		/**
		 * @return the number of rows used in the renderer and editor
		 */
		public Integer getRows() {
			return this.iRows;
		}

		/**
		 * @return the number of columns used in the renderer and editor
		 */
		public Integer getColumns() {
			return this.iColumns;
		}

		/**
		 * Returns the preferred width of this column.
		 */
		public Integer getWidth() {
			return width;
		}

		/**
		 * Returns the initial position of this column (relative to the other columns).
		 */
		public Integer getInitialPosition() {
			return initialPosition;
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

		public Collection<TransferLookedUpValueAction> getTransferLookedUpValueActions() {
			return (collTransferLookedUpValueActions != null) ? Collections.unmodifiableCollection(collTransferLookedUpValueActions) : Collections.<TransferLookedUpValueAction>emptyList();
		}

		public void addTransferLookedUpValueAction(TransferLookedUpValueAction act) {
			if (collTransferLookedUpValueActions == null) {
				collTransferLookedUpValueActions = new LinkedList<TransferLookedUpValueAction>();
			}
			collTransferLookedUpValueActions.add(act);
		}

		public Collection<ClearAction> getClearActions() {
			return collClearActions != null ? Collections.unmodifiableCollection(collClearActions) : Collections.<ClearAction>emptyList();
		}

		public void addClearAction(ClearAction act) {
			if (collClearActions == null) {
				collClearActions = new LinkedList<ClearAction>();
			}
			collClearActions.add(act);
		}

		public Collection<RefreshValueListAction> getRefreshValueListActions() {
			return collRefreshValueListActions != null ? Collections.unmodifiableCollection(collRefreshValueListActions) : Collections.<RefreshValueListAction>emptyList();
		}

		public void addRefreshValueListAction(RefreshValueListAction act) {
			if (collRefreshValueListActions == null) {
				collRefreshValueListActions = new LinkedList<RefreshValueListAction>();
			}
			collRefreshValueListActions.add(act);
		}

		public void setValueListProvider(CollectableFieldsProvider valuelistprovider) {
			this.valuelistprovider = valuelistprovider;
		}

		public CollectableFieldsProvider getValueListProvider() {
			return this.valuelistprovider;
		}
	}	// inner class Column

	private class SubFormPopupMenuMouseAdapter extends PopupMenuMouseAdapter {

		public SubFormPopupMenuMouseAdapter(JTable table) {
			super(table);
		}

		@Override
		public void doPopup(MouseEvent e) {
			JTable table = getJTable();
			int col = table.columnAtPoint(e.getPoint());
			int row = table.rowAtPoint(e.getPoint());
			if(col != -1 && row != -1) {
				TableCellEditor tce = table.getCellEditor(row, col);
				JPopupMenu menu = null;
				if(tce instanceof CollectableComponentTableCellEditor) {
					CollectableComponent clctcmp = ((CollectableComponentTableCellEditor) tce).getCollectableComponent();
					if(clctcmp instanceof JPopupMenuFactory) {
						Object value = table.getModel().getValueAt(row, table.convertColumnIndexToModel(col));
						tce.getTableCellEditorComponent(table, value, false, row, col);	// contained field is set without triggering listeners
						clctcmp.getJComponent().setEnabled(table.isCellEditable(row, col));
						menu = ((JPopupMenuFactory) clctcmp).newJPopupMenu();
					}
				}
				if (menu != null) {
					table.editCellAt(row, col, e);
					menu.show(table, e.getX(), e.getY());
				}
				else if (getPopupMenuAdapter() != null) {
					getPopupMenuAdapter().doPopup(e);
				}
			}
		}
	};

	private class DoubleClickMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
		    	if (e.getClickCount() == 2) {
		    		JTable table = getJTable();
		    		int col = table.columnAtPoint(e.getPoint());
					int row = table.rowAtPoint(e.getPoint());
					if (col != -1 && row != -1) {
						TableCellEditor tce = table.getCellEditor(row, col);
						if(tce instanceof CollectableComponentTableCellEditor) {
							CollectableComponent clctcmp = ((CollectableComponentTableCellEditor) tce).getCollectableComponent();
							if (clctcmp instanceof MouseListener) {
								Object value = table.getModel().getValueAt(row, table.convertColumnIndexToModel(col));
								tce.getTableCellEditorComponent(table, value, false, row, col);	// contained field is set without triggering listeners
								((MouseListener) clctcmp).mouseClicked(e);
							}
						}
					}
		    	}
		    }
		}
	}

}	// class SubForm
