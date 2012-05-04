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

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.Closeable;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.DatasourceBasedCollectableFieldsProvider;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.NuclosDropTargetVisitor;
import org.nuclos.client.common.OneDropNuclosDropTargetListener;
import org.nuclos.client.common.WorkspaceUtils;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.client.genericobject.CollectableGenericObject;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonClientWorkerSelfExecutable;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.FrameUtils;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.ListOfValues;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.SimpleDocumentListener;
import org.nuclos.client.ui.TopController;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.component.CollectableCheckBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.client.ui.collect.component.EntityListOfValues;
import org.nuclos.client.ui.collect.component.ICollectableListOfValues;
import org.nuclos.client.ui.collect.component.model.ChoiceEntityFieldList;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.client.ui.collect.component.model.SearchComponentModel;
import org.nuclos.client.ui.collect.detail.DetailsController;
import org.nuclos.client.ui.collect.detail.DetailsPanel;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModelImpl;
import org.nuclos.client.ui.collect.result.ResultController;
import org.nuclos.client.ui.collect.result.ResultPanel;
import org.nuclos.client.ui.collect.result.SearchResultStrategy;
import org.nuclos.client.ui.collect.search.ISearchStrategy;
import org.nuclos.client.ui.collect.search.SearchController;
import org.nuclos.client.ui.collect.search.SearchPanel;
import org.nuclos.client.ui.labeled.LabeledComboBox;
import org.nuclos.client.ui.labeled.LabeledDateChooser;
import org.nuclos.client.ui.labeled.LabeledListOfValues;
import org.nuclos.client.ui.labeled.LabeledTextArea;
import org.nuclos.client.ui.labeled.LabeledTextField;
import org.nuclos.client.ui.multiaction.MultiActionProgressPanel;
import org.nuclos.client.ui.table.SortableTableModel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.PivotJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;


/**
 * Controller for collecting data (German: "Daten erfassen").
 * Contains the necessary logic to search for, view and edit objects (or database rows).
 * <br>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Knows how to select, read, write and delete objects from a persistent store. (Model aspect)</li>
 *   <li>Knows the structure (meta data) of the objects to collect. (Model aspect, CollectableEntity)</li>
 *   <li>Knows how to display and edit objects. (View aspect)</li>
 *   <li>Contains the view, consisting of
 *     <ul>
 *       <li>an JInternalFrame (this is fixed behavior).</li>
 *       <li>a CollectPanel inside the frame.</li>
 *     </ul>
 *   <li>Defines a strategy that is responsible for showing partially loaded objects in the result table
 *       and for completing these objects when they need to be shown completely (in the Details).</li>
 *   <li>Defines a "workflow" consisting of Search, Result and Details with their respective panels, as defined in
 *       CollectPanel.</li>
 *   <li>To be short, has much too much responsibilities...</li>
 * </ul>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * TODO try to split up some of the responsibilities in separate classes, but don't sacrifice flexibility, don't make
 * things even more complicated and don't mess up the views with controller code. That's not an easy task...
 */
public abstract class CollectController<Clct extends Collectable> extends TopController 
	implements NuclosDropTargetVisitor, Closeable {

	private static final Logger LOG = Logger.getLogger(CollectController.class);

	/**
	 * the parent component for this controller
	 * @deprecated Use {@link #getParent()}
	 */
	@Deprecated
	protected final JComponent parent;

	/**
	 * the internal frame for this controller
	 */
	private MainFrameTab ifrm;

	/**
	 * the CollectPanel for this controller
	 * TODO Why is this an extra field inside the frame?
	 */
	private WeakReference<CollectPanel<Clct>> pnlCollect;

	/**
	 * the model used for synchronizing the navigation buttons with the selection in the result table and the result table model.
	 */
	private CollectNavigationModel navigationmodel;

	/**
	 * the state model that encapsulates the states and transitions for the collecting process.
	 * TODO move to CollectPanel
	 */
	private CollectStateModel<Clct> statemodel;

	/**
	 * the collectable entity for this controller
	 */
	private final CollectableEntity clcte;

	private int iLockCount = 0;

	protected boolean bIsLastTabDetailsModeMultiViewOrEdit = false;

	protected MouseListener foreignKeyMouseListenerForTableDoubleClick;

	protected CollectableFieldsProviderCache valueListProviderCache = new CollectableFieldsProviderCache();

	/**
	 * @see #isSearchChangedIgnored()
	 */
	private boolean bSearchChangedIgnored;

	/**
	 * @see #isDetailsChangedIgnored()
	 */
	private boolean bDetailsChangedIgnored;

	/**
	 * the source of the last invocation of detailsChanged
	 */
	private Object oSourceOfLastDetailsChange;

	/**
	 * used internally while the frame is closing.
	 */
	private boolean bFrameMayBeClosed;

	private List<CollectableEventListener> collectableListeners = new LinkedList<CollectableEventListener>();

	private final SearchController<Clct> ctlSearch = new SearchController<Clct>(this);

	/**
	 * TODO: Not every CollectController has a ResultController associated with it.
	 * 		Hence, this field should appear more 'upward' in the hierarchy.
	 *      (Thomas Pasch)
	 */
	private final ResultController<Clct> ctlResult;

	private final DetailsController<Clct> ctlDetails = new DetailsController<Clct>(this);

	private ISearchStrategy<Clct> ss;

	private final Map<String, Serializable> context = new HashMap<String, Serializable>();
	
	private InvokeWithInputRequiredSupport invokeWithInputRequiredSupport;
	
	private boolean closed = false;

	/**
	 * Messages for Collectable events
	 */
	public static enum MessageType {
		REFRESH_DONE,
		REFRESH_DONE_DIRECTLY,
		EDIT_DONE,
		STATECHANGE_DONE,
		DELETE_DONE,
		NEW_DONE,
		CLCT_LEFT/* extend as needed */
	}

	/**
	 * Listener Interface for Collectable events
	 */
	public static interface CollectableEventListener {
		public void handleCollectableEvent(Collectable collectable, MessageType messageType);
	}

	CollectableEventListener mandatoryResetEventListener = new CollectableEventListener() {
		@Override
		public void handleCollectableEvent(Collectable collectable,	MessageType messageType) {
			switch (messageType) {
			case REFRESH_DONE_DIRECTLY :
			case STATECHANGE_DONE :
			case DELETE_DONE :
			case CLCT_LEFT :
				resetCollectableComponentModelsInDetailsMandatory();
				resetCollectableComponentModelsInDetailsMandatoryAdded();
				break;
			}
		}
	};

	private ActionListener navigationChangeListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			broadcastCollectableEvent(getSelectedCollectable(), MessageType.CLCT_LEFT);
		}
	};

	protected FocusListener collectableComponentSearchFocusListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {}
		@Override
		public void focusGained(FocusEvent e) {
			CollectController.this.setDefaultButton();
		}
	};

	/**
	 * action: New
	 */
	private final Action actNew = new CommonAbstractAction(getSpringLocaleDelegate().getMessage("CollectController.39","Neu"), 
			Icons.getInstance().getIconNew16(),
			getSpringLocaleDelegate().getMessage("CollectController.23","Neuen Datensatz erfassen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			if (askAndSaveIfNecessary()) {
				cmdEnterNewMode();
			}
		}
	};

	/**
	 * action: Save
	 */
	private final Action actSave = new CommonAbstractAction(getSpringLocaleDelegate().getMessage("CollectController.40","Speichern"), 
			Icons.getInstance().getIconSave16(),
			getSpringLocaleDelegate().getMessage("CollectController.6","\u00c4nderungen an diesem Datensatz speichern")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdSave();
		}
	};

	/**
	 * action: Bookmark
	 */
	private final Action actBookmark = new CommonAbstractAction(getSpringLocaleDelegate().getMessage("CollectController.105","Lesezeichen setzen"), 
			Icons.getInstance().getIconBookmark16(), 
			getSpringLocaleDelegate().getMessage("CollectController.106","Lesezeichen auf den Start Tabs setzen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdBookmarkSelectedCollectable();
		}

		@Override
		public boolean isEnabled() {
			return CollectController.this.getCollectState().isDetailsModeViewOrEdit() || (CollectController.this.getCollectState().isResultMode() && CollectController.this.getSelectedCollectables().size() == 1);
		}

	};

	/**
	 * action: Bookmark
	 */
	private final Action actCopyCells = new CommonAbstractAction(getSpringLocaleDelegate().getMessage("ResultPanel.13","Kopiere markierte Zellen"), 
			Icons.getInstance().getIconCopy16(), 
			getSpringLocaleDelegate().getMessage("ResultPanel.13","Kopiere markierte Zellen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			UIUtils.copyCells(getResultTable());
		}

	};

	/**
	 * action: Bookmark
	 */
	private final Action actCopyRows = new CommonAbstractAction(getSpringLocaleDelegate().getMessage("ResultPanel.14","Kopiere markierte Zeilen"), 
			Icons.getInstance().getIconCopy16(), 
			getSpringLocaleDelegate().getMessage("ResultPanel.14","Kopiere markierte Zeilen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			UIUtils.copyRows(getResultTable());
		}

	};


	/**
	 * action: Open in new tab
	 */
	private final Action actOpenInNewTab = new CommonAbstractAction(getSpringLocaleDelegate().getMessage("CollectController.107","In neuem Tab \u00f6ffnen"), 
			Icons.getInstance().getIconOpenInNewTab16(), 
			getSpringLocaleDelegate().getMessage("CollectController.108","Details in neuem Tab \u00f6ffnen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdOpenSelectedCollectableInNewTab();
		}

		@Override
		public boolean isEnabled() {
			return CollectController.this.getCollectState().isDetailsModeViewOrEdit() || (CollectController.this.getCollectState().isResultMode());
		}

	};

	/**
	 * action: Clone
	 */
	private final Action actClone = new CommonAbstractAction(getSpringLocaleDelegate().getMessage("CollectController.38","Klonen"), 
			Icons.getInstance().getIconClone16(),
			getSpringLocaleDelegate().getMessage("CollectController.7","Ausgew\u00e4hlten Datensatz klonen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			if (askAndSaveIfNecessary()) {
				cmdCloneSelectedCollectable();
			}
		}
	};

	/**
	 * action: Refresh
	 */
	private final Action actRefreshCurrentCollectable = new CommonAbstractAction(getSpringLocaleDelegate().getMessage("CollectController.37","Aktualisieren"),
			Icons.getInstance().getIconRefresh16(), 
			getSpringLocaleDelegate().getMessage("CollectController.4","Aktualisieren (Datensatz neu laden und \u00c4nderungen verwerfen)")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdRefreshCurrentCollectable();
		}
	};

	/**
	 * action: First
	 */
	private final Action actFirst = new CommonAbstractAction(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconFirstWhite16(), DetailsPanel.recordNavIconSize),
		null) { //SpringLocaleDelegate.getMessage("CollectController.33","Zum ersten Datensatz springen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdFirst();
		}
	};

	/**
	 * action: Last
	 */
	private final Action actLast = new CommonAbstractAction(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconLastWhite16(), DetailsPanel.recordNavIconSize),
		null) { //SpringLocaleDelegate.getMessage("CollectController.34","Zum letzten Datensatz springen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdLast();
		}
	};

	/**
	 * action: Previous
	 */
	private final Action actPrevious = new CommonAbstractAction(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconPreviousWhite16(), DetailsPanel.recordNavIconSize),
		null) { //SpringLocaleDelegate.getMessage("CollectController.36","Zum vorigen Datensatz springen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdPrevious();
		}
	};

	/**
	 * action: Next
	 */
	private final Action actNext = new CommonAbstractAction(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconNextWhite16(), DetailsPanel.recordNavIconSize),
		null) { //SpringLocaleDelegate.getMessage("CollectController.35","Zum n\u00e4chsten Datensatz springen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdNext();
		}
	};

	protected final DocumentListener documentlistenerDetailsChanged = new SimpleDocumentListener() {
		@Override
		public void documentChanged(DocumentEvent ev) {
			CollectController.this.detailsChanged(ev.getDocument());
		}
	};

	protected final ItemListener itemlistenerDetailsChanged = new ItemListener() {
		@Override
        public void itemStateChanged(ItemEvent ev) {
			CollectController.this.detailsChanged(ev.getSource());
		}
	};

	protected final ChangeListener changelistenerDetailsChanged = new ChangeListener() {
		@Override
        public void stateChanged(ChangeEvent ev) {
			CollectController.this.detailsChanged(ev.getSource());
		}
	};

	protected final TableModelListener tblmdllistenerDetailsChanged = new TableModelListener() {
		@Override
        public void tableChanged(TableModelEvent ev) {
			CollectController.this.detailsChanged(ev.getSource());
		}
	};

	protected final ChangeListener changelistenerSearchChanged = new ChangeListener() {
		@Override
        public void stateChanged(ChangeEvent ev) {
			CollectController.this.searchChanged(ev.getSource());
		}
	};

	/**
	 * constructs a new CollectController.<br>
	 * <em>Important: The constructor of the derived controller class must call
	 * <ol>
	 * <li>setInternalFrame()
	 * <li>initialize()
	 * </ol>
	 * in this order.</em>
	 * @param parent
	 */
	protected CollectController(JComponent parent, CollectableEntity clcte, ResultController<Clct> rc) {
		super(parent);
		this.parent = parent;
		this.clcte = clcte;
		this.ctlResult = rc;
		this.ctlResult.setCollectController(this);
	}

	/**
	 * Don't make this public!
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected CollectController(JComponent parent, CollectableEntity clcte) {
		this(parent, clcte, new ResultController<Clct>(clcte, new SearchResultStrategy<Clct>()));
	}
	
	@Autowired
	void setInvokeWithInputRequiredSupport(InvokeWithInputRequiredSupport invokeWithInputRequiredSupport) {
		Assert.notNull(invokeWithInputRequiredSupport);
		this.invokeWithInputRequiredSupport = invokeWithInputRequiredSupport;
	}

	public final ISearchStrategy<Clct> getSearchStrategy() {
		return ss;
	}

	public final void setSearchStrategy(ISearchStrategy<Clct> ss) {
		this.ss = ss;
	}

	/**
	 * is entity of this controller transferable, that means that collectables
	 * of this entity can be exported and imported. Default is NOT transferable!
	 *
	 * TODO: Make this protected again.
	 */
	public boolean isTransferable() {
		return false;
	}

	/**
	 * @deprecated I would wish we could avoid method.
	 * 		Not every CollectController has a ResultController associated with it.
	 * 		Hence, this field should appear more 'upward' in the hierarchy.
	 */
	public final ResultController<Clct> getResultController() {
		return ctlResult;
	}

	/**
	 * @deprecated I would wish we could avoid method.
	 * 		Not every CollectController has a ResultController associated with it.
	 * 		Hence, this field should appear more 'upward' in the hierarchy.
	 */
	public final ChoiceEntityFieldList getFields() {
		return ctlResult.getFields();
	}

	/**
	 * @deprecated Move to SearchController.
	 */
	public Collection<SearchComponentModel> getSearchCollectableComponentModels() {
		return this.ctlSearch.getCollectableComponentModels();
	}

	public List<CollectableComponent> getDetailCollectableComponentsFor(String sFieldName) {
		return (List<CollectableComponent>) getDetailsPanel().getEditView().getCollectableComponentsFor(sFieldName);
	}

	/**
	 * must be called in constructor of derived classes.
	 * TODO refactor!
	 * @param pnlCollect the CollectPanel to be used.
	 */
	protected void initialize(CollectPanel<Clct> pnlCollect){
		if (pnlCollect == null) {
			throw new NullPointerException();
		}
		// set the name of the internal frame to this CollectController's entity name (for GUI testing purposes):
		if (this.getFrame() != null) {
			this.getFrame().setName("ifrm" + StringUtils.capitalized(clcte.getName()));
		}

		this.setCollectPanel(pnlCollect);
		getResultController().initializeFields();
		SortableCollectableTableModel<Clct> model = newResultTableModel();
		getResultController().setModel(model);
		// setup sorted fields and sorting order from preferences
		final List<SortKey> sortKeys = readColumnOrderFromPreferences();
		if (model.getColumnCount() > 0) {
			try {
				model.setSortKeys(sortKeys, false);
			} catch (IllegalArgumentException e) {
				// sortKeys contains invalid column index, ignore
			}
		}
		this.getCollectStateModel().addCollectStateListener(new DefaultCollectStateListener());
		this.getResultPanel().setupTableCellRenderers(getResultTable());
		getResultController().setColumnWidths(getResultTable());
		this.addCollectableEventListener(mandatoryResetEventListener);

		if (this.isSearchPanelAvailable()) {
			this.ctlSearch.setupSearchEditor();
		}

		// TODO all actions that are specific to the Details tab must be disabled here and in detailsModeLeft()
		this.getSaveAction().setEnabled(false);
		setupDragDrop();
	}

	protected void setupDragDrop() {
		OneDropNuclosDropTargetListener listener = new OneDropNuclosDropTargetListener(this, ClientParameterProvider.getInstance().getIntValue(ParameterProvider.KEY_DRAG_CURSOR_HOLDING_TIME, 600));
		DropTarget drop = new DropTarget(this.getResultTable(), listener);
		drop.setActive(true);
	}

	/**
	 * @return the user preferences node for this
	 */
	public Preferences getPreferences() {
		return this.getUserPreferencesRoot().node("collect").node("entity").node(this.getEntityName());
	}

	/**
	 *
	 * @return
	 */
	public EntityPreferences getEntityPreferences() {
		return MainFrame.getWorkspaceDescription().getEntityPreferences(this.getEntityName());
	}

	/**
	 * @return the application specific user preferences root. Note that this method serves only for the implementation
	 *         of getPreferences. Always use getPreferences when you want to store CollectController-specific preferences.
	 */
	protected abstract Preferences getUserPreferencesRoot();

	/**
	 * @return the "New" action
	 *
	 * TODO: Move to ResultPanel and make protected again.
	 */
	public final Action getNewAction() {
		return this.actNew;
	}

	/**
	 * @return the "New with search values" action
	 *
	 * @deprecated Move to SearchController and make protected again.
	 */
	public final Action getNewWithSearchValuesAction() {
		return ctlSearch.getNewWithSearchValuesAction();
	}

	/**
	 * @return the "Save" action
	 *
	 * TODO: Make protected again.
	 */
	public final Action getSaveAction() {
		return this.actSave;
	}

	/**
	 * @return the "Refresh current collectable" action
	 *
	 * TODO: Move to DetailsController???
	 * TODO: Make protected again.
	 */
	public final Action getRefreshCurrentCollectableAction() {
		return this.actRefreshCurrentCollectable;
	}

	/**
	 * @return the "Search" action
	 *
	 * @deprecated Move to SearchController.
	 */
	protected final Action getSearchAction() {
		return ctlSearch.getSearchAction();
	}

	/**
	 * @return the "Open in new tab" action
	 *
	 * TODO: Make protected again.
	 */
	public final Action getOpenInNewTabAction() {
		return this.actOpenInNewTab;
	}

	/**
	 * TODO: Make protected again.
	 */
	public final Action getCopyCellsAction() {
		return this.actCopyCells;
	}

	/**
	 * TODO: Make protected again.
	 */
	public final Action getCopyRowsAction() {
		return this.actCopyRows;
	}

	/**
	 * @return the "Bookmark" action
	 *
	 * TODO: Move to ResultPanel and make protected again.
	 */
	public final Action getBookmarkAction() {
		return this.actBookmark;
	}

	/**
	 * @return the "Clone" action
	 *
	 * TODO: Move to ResultPanel and make protected again.
	 */
	public final Action getCloneAction() {
		return this.actClone;
	}

	/**
	 * @return the "First" action
	 *
	 * TODO: Make protected again.
	 */
	public final Action getFirstAction() {
		return this.actFirst;
	}

	/**
	 * @return the "Last" action
	 *
	 * TODO: Make protected again.
	 */
	public final Action getLastAction() {
		return this.actLast;
	}

	/**
	 * @return the "Previous" action
	 *
	 * TODO: Make protected again.
	 */
	public final Action getPreviousAction() {
		return this.actPrevious;
	}

	/**
	 * @return the "Next" action
	 *
	 * TODO: Make protected again.
	 */
	public final Action getNextAction() {
		return this.actNext;
	}

	/**
	 *
	 * @param sortImmediately
	 */
	public void restoreColumnOrderFromPreferences(boolean sortImmediately) {
		getResultTableModel().setSortKeys(readColumnOrderFromPreferences(), sortImmediately);
	}

	/**
	 * Reads the user-preferences for the sorting order.
	 */
	protected List<SortKey> readColumnOrderFromPreferences() {
		return readColumnOrderFromPreferences(getResultTableModel());
	}

	/**
	 * Reads the user-preferences for the sorting order.
	 */
	protected List<SortKey> readColumnOrderFromPreferences(final SortableCollectableTableModel<Clct> tblmdl) {
		return WorkspaceUtils.getSortKeys(getEntityPreferences(),
				new WorkspaceUtils.IColumnIndexRecolver() {
					@Override
					public int getColumnIndex(String columnIdentifier) {
						try {
							return tblmdl.findColumnByFieldName(columnIdentifier);
						} catch (ClassCastException cce) {
							LOG.error("ResultTableModel is not sortable", cce);
							return -1;
						}
					}
				});
	}

	/**
	 * displays the current search condition in the Search panel's status bar.
	 *
	 * @deprecated Move to SearchController.
	 */
	protected final void cmdDisplayCurrentSearchConditionInSearchPanelStatusBar() {
		UIUtils.runShortCommand(this.getFrame(), new CommonRunnable() {
			@Override
            public void run() {
				ctlSearch.displayCurrentSearchConditionInSearchPanelStatusBar();
			}
		});
	}

	/**
	 * @return <code>CollectableFieldsProviderFactory</code> for the search editor. This is used to
	 *         display the list of possible values in the dropdown for atomic search nodes.
	 *         The default implementation returns null. To enable the search editor, a subclass must return
	 *         a valid factory here.
	 *
	 * @deprecated Move to SearchController and make protected again.
	 */
	public CollectableFieldsProviderFactory getCollectableFieldsProviderFactoryForSearchEditor() {
		return null;
	}

	/**
	 * @deprecated Move to SearchController and make protected again.
	 */
	public Collection<CollectableEntityField> getAdditionalSearchFields() {
		return null;
	}

	/**
	 * @return <code>MultiActionProgressPanel</code> for the MultiObjectsActionController.
	 *
	 * TODO: Make protected again.
	 */
	public MultiActionProgressPanel getMultiActionProgressPanel(int iCount) {
		return new MultiActionProgressPanel(iCount);
	}

	/**
	 * @return a new internal frame for this controller.
	 */
	protected abstract MainFrameTab newInternalFrame();

	/**
	 * Locks or unlocks the frame, i.e. makes it (im)possible for the user to trigger any action on it.
	 * This is mostly used for background processes
	 * @param bLock lock if true, unlock else
	 * TODO rename to setFrameLocked
	 */
	public void lockFrame(boolean bLock) {
		if(bLock) {
			iLockCount++;
			if(iLockCount > 0) {
				UIUtils.showWaitCursorForFrame(this.getFrame(), true);
				CollectController.this.setTitle(getFrame().getTitle() + " (" + getSpringLocaleDelegate().getMessage(
						"CollectController.19","In Bearbeitung") + ")");
			}
		}
		else {
			iLockCount--;
			if(iLockCount == 0) {
				UIUtils.showWaitCursorForFrame(this.getFrame(), false);
				CollectController.this.setTitle();
			}
		}
	}

	/**
	 * Remove the lock on a frame (glasspane etc.) forcefully, mostly in case of error
	 */
	public void forceUnlockFrame() {
		iLockCount = 0;
		UIUtils.showWaitCursorForFrame(this.getFrame(), false);
		CollectController.this.setTitle(getFrame().getTitle());
	}

	/**
	 * Checks wether user action is prohibited or not, i.e. a background process on this object is running or not.
	 * @return
	 * TODO rename to isFrameLocked
	 * TODO Is this needed at all? It's never used.
	 */
	public boolean isLocked() {
		return iLockCount == 0;
	}

	/**
	 * @param sTitle
	 * @return a new internal frame for this controller, as specified by <code>newInternalFrame()</code>, with the given title.
	 */
	protected final MainFrameTab newInternalFrame(String sTitle) {
		final MainFrameTab result = newInternalFrame();
		result.setTitle(sTitle);
		return result;
	}

	/**
	 * TODO refactor
	 * @param pnlCollect
	 */
	private void setCollectPanel(CollectPanel<Clct> pnlCollect) {
		this.pnlCollect = new WeakReference<CollectPanel<Clct>>(pnlCollect);

		this.statemodel = new CollectStateModel<Clct>(this.getCollectPanel(), this);

		// initialize "New" and "Clone" actions:
		this.getNewAction().setEnabled(CollectController.this.isNewAllowed());
		this.getNewWithSearchValuesAction().setEnabled(false);
		this.getCloneAction().setEnabled(false);

		if (this.isSearchPanelAvailable()) {
			this.ctlSearch.setupSearchPanel();
		}

		this.ctlResult.setupResultPanel();

		this.ctlDetails.setupDetailsPanel();
	}

	/**
	 * Command: view the selected Collectable(s).
	 */
	public void cmdViewSelectedCollectables() {
		if (this.multipleCollectablesSelected()) {
			this.cmdEnterMultiViewMode();
		}
		else {
			this.cmdEnterViewMode();
		}
	}

	/**
	 * @return Is more than one Collectable (row) selected in the Result table?
	 *
	 * TODO: move to navigation model? Or ResultController?
	 * TODO: Make this protected again.
	 */
	public final boolean multipleCollectablesSelected() {
		return this.getResultTable().getSelectedRowCount() > 1;
	}

	/**
	 * sets the internal frame to be used
	 * @param ifrm
	 */
	protected void setInternalFrame(final MainFrameTab ifrm, boolean addToParent) {
		this.ifrm = ifrm;

		// prevent that the frame is closed when changes are pending:
		ifrm.addVetoableChangeListener(new VetoableChangeListener() {
			@Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
				if (evt.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY)) {
					final Boolean bOldValue = (Boolean) evt.getOldValue();
					final Boolean bNewValue = (Boolean) evt.getNewValue();

					if (bOldValue == Boolean.FALSE && bNewValue == Boolean.TRUE) {
						// We need bFrameMayBeClosed as a member variable here, as it is set in
						// cmdFrameClosing and must be checked here.
						// JInternalFrame.setClosed() first sends a frame closing event, then a vetoable change event.
						// Note that this is totally weird. See JInternalFrame.setClosed()
						if (!CollectController.this.bFrameMayBeClosed) {
							throw new PropertyVetoException("do not close", evt);
						}
					}
				}
			}
		});

		// override close behavior:

		ifrm.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public void tabSelected(MainFrameTab tab) {
				setDefaultButton();
			}
			@Override
			public boolean tabClosing(MainFrameTab tab) throws CommonBusinessException {
				return askAndSaveIfNecessary();
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				CollectController.this.close();
				ifrm.removeMainFrameTabListener(this);
			}
		});

		if (addToParent) {
			// TODO this probably doesn't belong here - requires that the parent is a Container, and that shouldn't be.
			this.parent.add(ifrm);
		}

		// sets the default window state for the frame:
		//this.setDefaultWindowState(ifrm);
	}

	protected void setDefaultButton() {
		if (CollectController.this.getSearchPanel().getRootPane() != null)
			CollectController.this.getSearchPanel().getRootPane().setDefaultButton(CollectController.this.getSearchPanel().btnSearch);
	}
	
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Called when the internal frame is closed. Releases all resources held by the controller.
	 * This is the right place to remove all listeners.
	 */
	@Override
	public void close() {
		if (!closed) {
			LOG.info("close(): " + this);
			
			// Search panel:
			if (this.isSearchPanelAvailable()) {
				this.ctlSearch.close();
			}
	
			// Result panel:
			this.ctlResult.close();
	
			// Details panel:
			this.ctlDetails.close();
			
			// Partial fix for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7079260
			collectableComponentSearchFocusListener = null;
			
			closed = true;
		}
	}

	@Override
	public void writePreferencesWhileClosing() {
		this.close();
	}

	/**
	 * @return Does this <code>CollectController</code> have a Search panel?
	 */
	public final boolean isSearchPanelAvailable() {
		return this.getCollectPanel().containsSearchPanel();
	}

	/**
	 * @return the source of the last invocation of detailsChanged.
	 * TODO move to DetailsController
	 */
	protected final Object getSourceOfLastDetailsChange() {
		return this.oSourceOfLastDetailsChange;
	}

	/**
	 * resets the source of the last invocation of detailsChanged. It is important to do this in order to prevent
	 * memory leaks, esp. when using dynamic layouts.
	 * TODO move to DetailsController
	 */
	protected final void resetSourceOfLastDetailsChange() {
		this.oSourceOfLastDetailsChange = null;
	}

	/**
	 * sets the detailsChangedIgnored property.
	 * @param bDetailsChangedIgnored
	 * @postcondition isDetailsChangedIgnored() == bDetailsChangedIgnored
	 * @see #isDetailsChangedIgnored()
	 * TODO move to DetailsController
	 */
	public final void setDetailsChangedIgnored(boolean bDetailsChangedIgnored) {
		this.bDetailsChangedIgnored = bDetailsChangedIgnored;
	}

	/**
	 * @return Is detailsChanged() ignored? If so, detailsChanged() won't be called when the values of
	 *         <code>CollectableComponent</code>s change.
	 * TODO move to DetailsController
	 */
	public final boolean isDetailsChangedIgnored() {
		return this.bDetailsChangedIgnored;
	}

	/**
	 * Notification: the details have changed
	 * @param oSource the source that caused the change
	 *
	 * @deprecated Move to DetailsController and make protected again.
	 */
	public final void detailsChanged(final Object oSource) {
		if (!isDetailsChangedIgnored()) {
			CollectController.this.ctlDetails.updateStatusBarIfNecessary();

			// this must be run later as the following actions might add or remove collectable component
			// listeners, or other listeners that caused the event to be fired:
			// TODO runCommandLater is critical here - synchronous execution (runCommand) would be desirable!
			UIUtils.runShortCommandLater(getFrame(), new CommonRunnable() {
				@Override
                public void run() throws CommonBusinessException {
					CollectController.this.oSourceOfLastDetailsChange = oSource;
					switch (statemodel.getDetailsMode()) {
						case CollectState.DETAILSMODE_VIEW:
							setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_EDIT);
							break;
						case CollectState.DETAILSMODE_NEW:
							setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_CHANGED);
							break;
						case CollectState.DETAILSMODE_NEW_SEARCHVALUE:
							setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_CHANGED);
							break;
						case CollectState.DETAILSMODE_MULTIVIEW:
							setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_MULTIEDIT);
							break;
						default:
							// do nothing
							LOG.debug("detailsChanged fired in details mode " + getCollectStateModel().getDetailsMode());
					}
				}
			});
		}
	}

	/**
	 * @deprecated Move to DetailsController.
	 */
	protected final DetailsComponentModel getDetailsComponentModel(String sFieldName){
		for (DetailsComponentModel componentModel : CollectController.this.ctlDetails.getCollectableComponentModels()){
			if (componentModel.getFieldName().equals(sFieldName)){
				return componentModel;
			}
		}

		throw new CommonFatalException("Field with name "+sFieldName+" not found!");
	}

	/**
	 * sets the searchChangedIgnored property.
	 * @param bSearchChangedIgnored
	 * @postcondition isSearchChangedIgnored() == bSearchChangedIgnored
	 * @see #isSearchChangedIgnored()
	 * TODO move to SearchController
	 */
	protected final void setSearchChangedIgnored(boolean bSearchChangedIgnored) {
		this.bSearchChangedIgnored = bSearchChangedIgnored;
	}

	/**
	 * @return Is searchChanged() ignored? If so, searchChanged() won't be called when the values of
	 *         <code>CollectableComponent</code>s change.
	 * TODO move to SearchController
	 */
	protected final boolean isSearchChangedIgnored() {
		return this.bSearchChangedIgnored;
	}

	/**
	 * Notification: the search have changed
	 * @param oSource the source that caused the change
	 *
	 * @deprecated Move to SearchController and make protected again.
	 */
	public final void searchChanged(Object oSource) {
		if (!isSearchChangedIgnored()) {
			// set status bar in Search panel according to the current search condition:
			CollectController.this.cmdDisplayCurrentSearchConditionInSearchPanelStatusBar();

			// "new with search values" action:
			if (oSource != null && isNewAllowed()) {
				getNewWithSearchValuesAction().setEnabled(true);
			}
		}
	}

	/**
	 * @return the <code>EditView</code> in the Search panel.
	 */
	protected final EditView getSearchEditView() {
		return this.getSearchPanel().getEditView();
	}

	/**
	 * @return the <code>EditView</code> in the Details panel.
	 */
	protected final EditView getDetailsEditView() {
		return this.getDetailsPanel().getEditView();
	}

	/**
	 * @param bSearch true = Search panel, false = Details panel
	 * @return the <code>EditView</code> in the Search or Details panel.
	 */
	protected final EditView getEditView(boolean bSearch) {
		return bSearch ? this.getSearchEditView() : this.getDetailsEditView();
	}

	/**
	 * makes the views of the given components consistent with their models.
	 * @param bSearch
	 * @param sFieldName
	 * @throws CollectableFieldFormatException
	 * TODO inline - after reconsidering makeConsistent() vs. stopEditing()
	 */
	public final void makeConsistent(boolean bSearch, String sFieldName) throws CollectableFieldFormatException {
		this.getEditView(bSearch).makeConsistent(sFieldName);
	}

	/**
	 * makes the views of the given components consistent with their models.
	 * @param bSearch
	 * @throws CollectableFieldFormatException
	 * TODO inline - but @see LeasedObjectCollectController.
	 */
	public void makeConsistent(boolean bSearch) throws CollectableFieldFormatException {
		// make the model consistent with the view:
		this.getEditView(bSearch).makeConsistent();
	}

	/**
	 * @return Have the change listeners for the Details tab been added?
	 * TODO move to DetailsController
	 */
	protected final boolean changeListenersForDetailsAdded() {
		return this.ctlDetails.getChangeListenersAdded();
	}

	/**
	 * adds the change listeners for the Details tab
	 * @precondition !this.changeListenersForDetailsAdded()
	 * @postcondition this.changeListenersForDetailsAdded()
	 * TODO move to DetailsController
	 */
	protected final void addChangeListenersForDetails() {
		this.ctlDetails.addChangeListeners();

		assert this.changeListenersForDetailsAdded();
	}

	/**
	 * removes the change listeners for the details tab
	 * @postcondition !this.changeListenersForDetailsAdded()
	 * TODO move to DetailsController
	 */
	protected final void removeChangeListenersForDetails() {
		this.ctlDetails.removeChangeListeners();

		assert !this.changeListenersForDetailsAdded();
	}

	/**
	 * adds additional change listeners for the Details tab.
	 * Default implementation: do nothing.
	 * Derived classes may override this method to add change listeners for additional
	 * (non-collectable) components.
	 *
	 * @deprecated Move to DetailsController and make protected again.
	 */
	public void addAdditionalChangeListenersForDetails() {
		// optional - do nothing here
	}

	/**
	 * removes additional change listeners for the Details tab.
	 * Default implementation: do nothing.
	 * Derived classes may override this method to remove change listeners for additional
	 * (non-collectable) components.
	 *
	 * @deprecated Move to DetailsController and make protected again.
	 */
	public void removeAdditionalChangeListenersForDetails() {
		// optional - do nothing here
	}

	/**
	 * @return Have the change listeners for the Search tab been added?
	 *
	 * @deprecated Move to SearchController.
	 */
	protected final boolean changeListenersForSearchAdded() {
		return this.ctlSearch.getChangeListenersAdded();
	}

	/**
	 * adds the change listeners for the Search tab
	 * @precondition !this.changeListenersForSearchAdded()
	 * @postcondition this.changeListenersForSearchAdded()
	 * @deprecated Move to SearchController.
	 */
	protected final void addChangeListenersForSearch() {
		this.ctlSearch.addChangeListeners();

		assert this.changeListenersForSearchAdded();
	}

	/**
	 * removes the change listeners for the search tab
	 * @postcondition !this.changeListenersForSearchAdded()
	 *
	 * @deprecated Move to SearchController.
	 */
	protected final void removeChangeListenersForSearch() {
		this.ctlSearch.removeChangeListeners();

		assert !this.changeListenersForSearchAdded();
	}

	/**
	 * adds additional change listeners for the Search tab.
	 * Default implementation: do nothing.
	 * Derived classes may override this method to add change listeners for additional
	 * (non-collectable) components.
	 *
	 *  @deprecated Move to SearchController
	 */
	public void addAdditionalChangeListenersForSearch() {
		// optional - do nothing here
	}

	/**
	 * removes additional change listeners for the Search tab.
	 * Default implementation: do nothing.
	 * Derived classes may override this method to remove change listeners for additional
	 * (non-collectable) components.
	 *
	 * @deprecated Move to SearchController and make protected again.
	 */
	public void removeAdditionalChangeListenersForSearch() {
		// optional - do nothing here
	}

	/**
	 * the regular entry point: start in Search tab
	 */
	public final void runSearch() throws CommonBusinessException {
		this.runSearch(true);
	}

	/**
	 * the regular entry point: start in Search tab
	 */
	public final void runSearch(boolean selectTab) throws CommonBusinessException {
		this.setCollectState(CollectState.OUTERSTATE_SEARCH, CollectState.SEARCHMODE_UNSYNCHED);

		if (selectTab) {
			this.showFrame();
		}
	}

	public final void runSearchWith(CollectableSearchCondition cond) throws CommonBusinessException {
		this.setCollectState(CollectState.OUTERSTATE_SEARCH, CollectState.SEARCHMODE_UNSYNCHED);
		if (cond != null) {
			this.setCollectableSearchConditionInSearchPanel(cond);
		}
		this.showFrame();
	}

	/**
	 * alternative entry point: lookup a <code>Collectable</code> (in a foreign entity).
	 */
	public void runLookupCollectable(final ICollectableListOfValues clctlovSource) throws CommonBusinessException {

		// show the internal frame in the front of the modal layer:
		final MainFrameTab ifrm = this.getFrame();

		ifrm.setVisible(true);

		if (!clctlovSource.isSearchComponent()) {
			String label = getSpringLocaleDelegate().getMessage("CollectController.41","Auswahl übernehmen");
			String description = getSpringLocaleDelegate().getMessage(
					"CollectController.42","Findet die Übernahme in einem Unterformular statt werden mittels Mehrfachauswahl zusätzliche Datensätze im Unterformular erzeugt.");
			if (clctlovSource instanceof CollectableListOfValues) {
				final CollectableListOfValues clov = (CollectableListOfValues) clctlovSource;
				if (clov.getValueListProvider() instanceof DatasourceBasedCollectableFieldsProvider) {
					ss.setValueListProviderDatasource(((DatasourceBasedCollectableFieldsProvider) clov.getValueListProvider()).getDatasourceVO());
					ss.setValueListProviderDatasourceParameter(((DatasourceBasedCollectableFieldsProvider) clov.getValueListProvider()).getValueListParameter());
				} else if (clov.getValueListProvider() instanceof CollectableFieldsProviderCache.CachingCollectableFieldsProvider) {
					CollectableFieldsProvider delegate = ((CollectableFieldsProviderCache.CachingCollectableFieldsProvider) clov.getValueListProvider()).getDelegate();
					if (delegate instanceof DatasourceBasedCollectableFieldsProvider) {
						ss.setValueListProviderDatasource(((DatasourceBasedCollectableFieldsProvider)delegate).getDatasourceVO());
						ss.setValueListProviderDatasourceParameter(((DatasourceBasedCollectableFieldsProvider)delegate).getValueListParameter());
					}
				}
			}
			else if (clctlovSource instanceof EntityListOfValues) {
				label = getSpringLocaleDelegate().getMessage("CollectController.lookup.generation","Objekte ausw\u00e4hlen");
				description = getSpringLocaleDelegate().getMessage("CollectController.lookup.generation.description", "Parameterobjekte für die Objektgenerierung ausw\u00e4hlen.");
			}
			final JMenuItem miPopupApplySelection = new JMenuItem(label);
			miPopupApplySelection.setToolTipText(description);

			getResultPanel().popupmenuRow.removeAll();//.addSeparator();
			getResultPanel().popupmenuRow.add(miPopupApplySelection);
			miPopupApplySelection.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					acceptLookedUpCollectable(clctlovSource);
					getFrame().dispose();
				}
			});
		}

		// remove mouse listener for double click in table:
		getResultPanel().removeDoubleClickMouseListener(this.getMouseListenerForTableDoubleClick());

		// add alternative mouse listener for foreign key lookup:
		foreignKeyMouseListenerForTableDoubleClick = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (SwingUtilities.isLeftMouseButton(ev) && ev.getClickCount() == 2) {
					acceptLookedUpCollectable(clctlovSource);
					getFrame().dispose();
				}
			}
		};

		getResultPanel().addDoubleClickMouseListener(foreignKeyMouseListenerForTableDoubleClick);

		getResultTable().getActionMap().put(KeyBindingProvider.EDIT_2.getKey(), new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				acceptLookedUpCollectable(clctlovSource);
				getFrame().dispose();
			}
		});

		if (this.isSearchPanelAvailable()) {
			if (ss.getValueListProviderDatasource() != null) {
				this.runViewAll();
			} else {
				this.runSearch();
			}
		}
		else {
			this.runViewAll();
		}

	    final Boolean modalLookup = (Boolean) clctlovSource.getProperty(ICollectableListOfValues.PROPERTY_MODAL_LOOKUP);
		if (Boolean.TRUE.equals(modalLookup)) {
			JDialog d = new JDialog(Main.getInstance().getMainFrame(), ifrm.getTitle(), true);
			FrameUtils.externalizeIntoWindow(ifrm, d);
			d.pack();
			d.setVisible(true);
		}
	}

	/**
	 * alternative entry point: enter new object
	 */
	public final void runNew() throws CommonBusinessException {
		this.runNew(true);
	}

	/**
	 * alternative entry point: enter new object
	 */
	public final void runNew(boolean selectTab) throws CommonBusinessException {
		this.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW);

		if (selectTab) {
			this.showFrame();
		}
	}

	public final void runNewWith(Clct clct) throws CommonBusinessException {
		this.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW);
		if (clct != null) {
			this.unsafeFillDetailsPanel(clct);
		}
		this.showFrame();
	}

	/**
	 * alternative entry point: view single object in Details
	 * @param clct the object to view in Details
	 * @precondition clct != null
	 * @precondition isCollectableComplete(clct)
	 * @see #readCollectable(Collectable)
	 */
	public final void runViewSingleCollectable(Clct clct) {
		runViewSingleCollectable(clct, true);
	}

	/**
	 * alternative entry point: view single object in Details
	 * @param clct the object to view in Details
	 * @precondition clct != null
	 * @precondition isCollectableComplete(clct)
	 * @see #readCollectable(Collectable)
	 */
	public final void runViewSingleCollectable(Clct clct, boolean bShow) {
		if (clct == null) {
			throw new NullArgumentException("clct");
		}
		if (!ss.isCollectableComplete(clct)) {
			throw new IllegalArgumentException("clct");
		}
		this.viewSingleCollectable(clct);
		if (bShow)
			this.showFrame();
	}

	/**
	 * alternative entry point: view single object in Details.
	 * runs the Controller starting in Details view displaying the object with the given id.
	 * @param oId
	 * @throws CommonBusinessException
	 */
	public final void runViewSingleCollectableWithId(Object oId) throws CommonBusinessException {
		this.runViewSingleCollectableWithId(oId, true);
	}

	/**
	 * alternative entry point: view single object in Details.
	 * runs the Controller starting in Details view displaying the object with the given id.
	 * @param oId
	 * @throws CommonBusinessException
	 */
	public final void runViewSingleCollectableWithId(Object oId, boolean bShow) throws CommonBusinessException {
		runViewSingleCollectable(findCollectableById(getEntityName(), oId), bShow);
	}

	public final void runViewMultipleCollectablesWithIds(List<Object> ids) throws CommonBusinessException {
		getSearchStrategy().setCollectableIdListCondition(new CollectableIdListCondition(ids));
		fillResultPanel(getSearchStrategy().getSearchResult());
		getResultTable().getSelectionModel().addSelectionInterval(0, getResultTable().getRowCount() - 1);
		setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_MULTIVIEW);
		showFrame();
	}

	/**
	 * Show the frame for the first time.
	 * @deprecated This method is misused as a listener. Use an InternalFrameListener or CollectableStateListener instead.
	 */
	@Deprecated
	protected void showFrame() {
		this.getFrame().setVisible(true);
	}

	/**
	 * @param clct
	 * @precondition clct != null
	 * @precondition isCollectableComplete(clct)
	 */
	private void viewSingleCollectable(Clct clct) {
		if (clct == null) {
			throw new NullArgumentException("clct");
		}
		if (!ss.isCollectableComplete(clct)) {
			throw new IllegalArgumentException("clct");
		}
		// fill result table:
		this.fillResultPanel(new ArrayList<Clct>(Collections.singletonList(clct)));

		getCollectPanel().getResultPanel().getResultTable().setRowSelectionInterval(0, 0);
		// select the one result row

		this.cmdEnterViewMode();
	}

	/**
	 * alternative entry point: view all (in Results tab)
	 * TODO refactor using runViewResults
	 */
	public final void runViewAll() throws CommonBusinessException {
		this.runViewAll(true);
	}

	/**
	 * alternative entry point: view all (in Results tab)
	 * TODO refactor using runViewResults
	 */
	public final void runViewAll(boolean selectTab) throws CommonBusinessException {
		this.getCollectPanel().setTabbedPaneEnabledAt(CollectState.OUTERSTATE_DETAILS, false);

		this.viewAll();

		this.setCollectState(CollectState.OUTERSTATE_RESULT, CollectState.RESULTMODE_NOSELECTION);

		if (selectTab) {
			this.showFrame();
		}
	}

	/**
	 * views all records in the Results tab. Calls search() with initial (or missing) search condition.
	 * TODO refactor
	 */
	protected void viewAll() throws CommonBusinessException {
		ss.search();
	}

	/**
	 * alternative entry point: view all (in Results tab)
	 * @precondition this.isSearchPanelAvailable()
	 * 
	 * TODO: Argument should be List<Long>.
	 */
	public final void runViewResults(List<? extends Object> oIds) throws CommonBusinessException {
		getResultController().getSearchResultStrategy().viewList(oIds);
		this.setCollectState(CollectState.OUTERSTATE_RESULT, CollectState.RESULTMODE_NOSELECTION);
		this.showFrame();
	}

	/**
	 * alternative entry point: view all (in Results tab)
	 * @precondition this.isSearchPanelAvailable()
	 */
	public final void runViewResults(CollectableSearchCondition cond) throws CommonBusinessException {
		if (!this.isSearchPanelAvailable()) {
			throw new IllegalStateException("this.isSearchPanelAvailable()");
		}
		this.getCollectPanel().setTabbedPaneEnabledAt(CollectState.OUTERSTATE_DETAILS, false);
		this.setCollectableSearchConditionInSearchPanel(cond);
		ss.search();
		this.setCollectState(CollectState.OUTERSTATE_RESULT, CollectState.RESULTMODE_NOSELECTION);
		this.showFrame();
	}

	/**
	 * alternative entry point: view search result (in Results tab)
	 * @precondition this.isSearchPanelAvailable()
	 *
	 * @deprecated Move to ResultController.
	 */
	public final void runViewResults(final ICollectableListOfValues clctlovSource) throws CommonBusinessException {
		// remove mouse listener for double click in table:
		final JTable tbl = this.getResultTable();
		tbl.removeMouseListener(this.getMouseListenerForTableDoubleClick());

		// add alternative mouse listener for foreign key lookup:
		foreignKeyMouseListenerForTableDoubleClick = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (SwingUtilities.isLeftMouseButton(ev) && ev.getClickCount() == 2) {
					try {
						final Collectable clctSelected = CollectController.this.getCompleteSelectedCollectable();
						// TODO assert clctSelected != null ?
						if (clctSelected != null) {
							clctlovSource.acceptLookedUpCollectable(clctSelected);

							// remove the mouse listener after it has done its job:
							// tbl.removeMouseListener(this);

							// TODO may whatever mouselistener was installed should be removed from the table in "close()"

							// Note that Controller.close() is called implicitly here:
							getFrame().dispose();
						}
					}
					catch (Exception ex) {
						Errors.getInstance().showExceptionDialog(getFrame(), ex);
					}
				}
			}
		};

		tbl.addMouseListener(foreignKeyMouseListenerForTableDoubleClick);
		if (!this.isSearchPanelAvailable()) {
			throw new IllegalStateException("this.isSearchPanelAvailable()");
		}
		this.setCollectableSearchConditionInSearchPanel(clctlovSource.getCollectableSearchCondition());
		this.showFrame();
		getResultController().getSearchResultStrategy().cmdSearch();
		this.getCollectPanel().setTabbedPaneEnabledAt(CollectState.OUTERSTATE_DETAILS, false);
		this.getCollectPanel().setTabbedPaneEnabledAt(CollectState.OUTERSTATE_SEARCH, false);
	}

	/**
	 * @param bMakeConsistent make the search component models consistent with their components? That is: transfer the
	 * search condition from the view to the model if they differ? This parameter is passed to #getCollectableSearchConditionFromSearchFields.
	 * @return the search condition taken from the search fields or the search editor. May be <code>null</code>.
	 * @postcondition !this.isSearchPanelAvailable() --> (result == null)
	 * @postcondition result == null || result.isSyntacticallyCorrect()
	 * @throws CollectableFieldFormatException if the search condition is not syntactically correct.
	 * TODO this.isSearchPanelAvailable() should be a precondition here
	 *
	 * @deprecated Move to SearchController and make protected again.
	 */
	public final CollectableSearchCondition getCollectableSearchConditionFromSearchPanel(boolean bMakeConsistent) throws CollectableFieldFormatException {
		final CollectableSearchCondition result = !this.isSearchPanelAvailable() ? null :
				(getSearchPanel().isSearchEditorVisible() || this.getImportedSearchCondition() != null ? getMixedSearchCondition(bMakeConsistent) : this.getCollectableSearchConditionFromSearchFields(bMakeConsistent));

		if (result != null && !result.isSyntacticallyCorrect()) {
			// TODO an InvalidSearchConditionException would be better here
			throw new CollectableFieldFormatException("Die Suchbedingung ist unvollst\u00e4ndig.");
		}

		LangUtils.implies(!this.isSearchPanelAvailable(), result == null);
		assert result == null || result.isSyntacticallyCorrect();
		return result;
	}

	private CollectableSearchCondition importedSearchCondition = null;

	protected void setImportedSearchCondition(CollectableSearchCondition pImportedSearchCondition){
		this.importedSearchCondition = pImportedSearchCondition;
	}

	protected CollectableSearchCondition getImportedSearchCondition(){
		return this.importedSearchCondition;
	}

	protected void removeImportedSearchConditionWithStatus() {
		setImportedSearchConditionWithStatus(null);
	}

	protected void setImportedSearchConditionWithStatus(CollectableSearchCondition searchCondition) {
		this.setImportedSearchCondition(searchCondition);
		setDisplayMixedSearchConditionForSearchEditor(searchCondition != null);
		cmdDisplayCurrentSearchConditionInSearchPanelStatusBar();
	}

	private CollectableSearchCondition getMixedSearchCondition(boolean bMakeConsistent) throws CollectableFieldFormatException {
		CollectableSearchCondition editorSearchCondition = getSearchPanel().getSearchEditorPanel().getSearchCondition();
		CollectableSearchCondition fieldsSearchCondition = this.getCollectableSearchConditionFromSearchFields(bMakeConsistent);
		if(this.getImportedSearchCondition() != null){
			if(editorSearchCondition == null && fieldsSearchCondition == null){
				return this.getImportedSearchCondition();
			}
			CompositeCollectableSearchCondition mixedSearchCondition = new CompositeCollectableSearchCondition(LogicalOperator.AND);
			mixedSearchCondition.addOperand(this.getImportedSearchCondition());
			setDisplayMixedSearchConditionForSearchEditor(true);
			if(editorSearchCondition != null){
				mixedSearchCondition.addOperand(editorSearchCondition);
			} else {
				if(fieldsSearchCondition != null){
					mixedSearchCondition.addOperand(fieldsSearchCondition);
				}
			}
			return mixedSearchCondition;
		} else {
			return editorSearchCondition;
		}
	}

	/**
	 * @deprecated Move to SearchController.
	 */
	public void setDisplayMixedSearchConditionForSearchEditor(boolean isMixedSearchCondition){
		this.ctlSearch.setDisplayMixedSearchCondition(isMixedSearchCondition);
	}

	/**
	 * @return the search condition to display (in the status bar). By default, this is the search condition given by
	 * getCollectableSearchCondition(), with labels sorted in ascending order. May be <code>null</code>.
	 * @throws CollectableFieldFormatException
	 *
	 * @deprecated Move to SearchController and make protected again.
	 */
	public CollectableSearchCondition getCollectableSearchConditionToDisplay() throws CollectableFieldFormatException {
		return SearchConditionUtils.sortedByLabels(ss.getCollectableSearchCondition());
	}

	/**
	 * sets the given search condition in the search panel.
	 * If the search editor is visible, sets the search condition there.
	 * Otherwise, tries to set the search condition in the search fields.
	 * If the search condition cannot be displayed in the search fields,
	 * the search editor is shown and the search condition is set there.
	 * @param cond
	 * @precondition this.isSearchPanelAvailable()
	 * TODO move to SearchPanel
	 */
	protected final void setCollectableSearchConditionInSearchPanel(CollectableSearchCondition pCond) {
		if (!this.isSearchPanelAvailable()) {
			throw new IllegalStateException("this.isSearchPanelAvailable");
		}
		CollectableSearchCondition cond = removeAndSetPlainSubCondition(pCond);
		if(cond == null){
			return;
		}
		if (!getSearchPanel().isSearchEditorVisible() && !getSearchPanel().canDisplayConditionInFields(cond)) {
			getSearchPanel().setSearchEditorVisible(true);
		}
		if (getSearchPanel().isSearchEditorVisible()) {
			getSearchPanel().getSearchEditorPanel().setSortedSearchCondition(cond);
		}
		else {
			try {
				this.setSearchFieldsAccordingToSearchCondition(cond, true);
			}
			catch (Exception ex) {
				LOG.info("Suchbedingung kann nicht in der Maske dargestellt werden. Daher wird der Sucheditor aktiviert.", ex);
				getSearchPanel().setSearchEditorVisible(true);
				getSearchPanel().getSearchEditorPanel().setSortedSearchCondition(cond);
			}
		}
	}

	private CollectableSearchCondition removeAndSetPlainSubCondition(CollectableSearchCondition cond) {
		if(cond instanceof PlainSubCondition){
			this.setImportedSearchConditionWithStatus(cond);
			return null;
		}
		if(cond instanceof CompositeCollectableSearchCondition){
			List<CollectableSearchCondition> notPlainOperands = new ArrayList<CollectableSearchCondition>();
			List<CollectableSearchCondition> cOperands = ((CompositeCollectableSearchCondition)cond).getOperands();
			for(CollectableSearchCondition operand : cOperands){
				if(operand instanceof PlainSubCondition){
					this.setImportedSearchCondition(operand);
				} else {
					notPlainOperands.add(operand);
				}
			}
			if(notPlainOperands.size() > 1){
				return new CompositeCollectableSearchCondition(((CompositeCollectableSearchCondition)cond).getLogicalOperator(), notPlainOperands);
			} else {
				return notPlainOperands.get(0);
			}
		}
		return cond;
	}

	/**
	 * @param bMakeConsistent make the search component models consistent with their components? That is: transfer the
	 * search condition from the view to the model if they differ?
	 * @return the search condition contained in the search panel's fields.
	 * Subclasses may include non-collectable components and/or subforms here.
	 * @precondition this.isSearchPanelAvailable()
	 * @postcondition result == null || result.isSyntacticallyCorrect()
	 *
	 * @deprecated Move to SearchController or SearchPanel and make protected again.
	 */
	public CollectableSearchCondition getCollectableSearchConditionFromSearchFields(boolean bMakeConsistent) throws CollectableFieldFormatException {
		if (!this.isSearchPanelAvailable()) {
			throw new IllegalStateException("!this.isSearchPanelAvailable()");
		}
		if (bMakeConsistent) {
			this.makeConsistent(true);
		}

		final CollectableSearchCondition result = this.getSearchConditionFromModel();

		assert result == null || result.isSyntacticallyCorrect();

		return result;
	}

	/**
	 * @return the search condition contained in the search panel's model.
	 * @postcondition !this.isSearchPanelAvailable() --> (result == null)
	 * @postcondition result == null || result.isSyntacticallyCorrect()
	 */
	protected final CollectableSearchCondition getSearchConditionFromModel() {
		final CollectableSearchCondition result = this.isSearchPanelAvailable() ? this.getSearchPanel().getEditModel().getSearchCondition() : null;

		LangUtils.implies(!this.isSearchPanelAvailable(), result == null);
		assert result == null || result.isSyntacticallyCorrect();

		return result;
	}

	/**
	 * sets the search fields (as opposed to the search editor) according to the given search condition.
	 * @param cond
	 * @param bClearSearchFields Clear all search fields before setting the search condition?
	 *
	 * @deprecated Move to SearchController and make protected again.
	 */
	public void setSearchFieldsAccordingToSearchCondition(CollectableSearchCondition cond,
			boolean bClearSearchFields) throws CommonBusinessException {
		this._setSearchFieldsAccordingToSearchCondition(cond, bClearSearchFields);
	}

	/**
	 * sets the search fields (as opposed to the search editor) according to the given search condition.
	 * This is <code>CollectController</code>'s default implementation of <code>setSearchFieldsAccordingToSearchCondition</code>.
	 * @param cond
	 * @param bClearSearchFields
	 * @throws CommonBusinessException
	 */
	private void _setSearchFieldsAccordingToSearchCondition(CollectableSearchCondition cond, boolean bClearSearchFields) throws CommonBusinessException {
		if (cond == null) {
			this.clearSearchFields();
		}
		else {
			/** TODO check if the search condition can be displayed in the fields at all,
			 * eg. isBasicSearchCondition() - isComplexSearchCondition().
			 * For the moment, we assume isBasicSearchCondition(). A basic search condition would be a
			 * conjunction, which may be nested. Prohibited are NOT, OR.
			 */

			if (bClearSearchFields) {
				// TODO optimize: only clear those search fields that are not contained in the search condition
				this.clearSearchFields();
			}

			cond.accept(new SetSearchFieldsVisitor());
		}
	}

	// TODO refactor - this should not be a method here
	protected void _setSearchFieldsAccordingToSubCondition(CollectableSubCondition subcond) throws CommonBusinessException {
		throw new NotImplementedException("subconditions");
	}

	// TODO refactor - this should not be a method here
	protected void _setSearchFieldsAccordingToReferencingSearchCondition(ReferencingCollectableSearchCondition refcond) throws CommonBusinessException {
		throw new NotImplementedException("referencing conditions");
	}

	/**
	 * inner class SetSearchFieldsVisitor
	 */
	private class SetSearchFieldsVisitor implements Visitor<Void, CommonBusinessException> {

		@Override
        public Void visitTrueCondition(TrueCondition truecond) throws CommonBusinessException {
			throw new IllegalArgumentException("truecond");
		}

		@Override
        public Void visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) throws CommonBusinessException {
			final String sFieldName = atomiccond.getFieldName();
			final SearchComponentModel clctcompmodel = getSearchPanel().getEditModel().getCollectableComponentModelFor(sFieldName);
			if (clctcompmodel == null) {
				String sLabel;
				try {
					sLabel = "mit dem Namen \"" + getCollectableEntity().getEntityField(sFieldName).getLabel() +
							"\" (interner Name: \"" + sFieldName + "\")";
				}
				catch (CommonFatalException ex) {
					sLabel = "mit dem internen Namen \"" + sFieldName + "\"";
				}
				// TODO use more specific exception
				throw new CommonBusinessException("Ein Feld " + sLabel + " ist nicht in der Maske vorhanden.");
			}
			clctcompmodel.setSearchCondition(atomiccond);
			return null;
		}

		@Override
        public Void visitCompositeCondition(CompositeCollectableSearchCondition compositecond) throws CommonBusinessException {
			if (compositecond.getLogicalOperator() != LogicalOperator.AND) {
				throw new CommonFatalException("Dieser logische Operator ist hier nicht erlaubt: " + compositecond.getLogicalOperator());
			}
			for (CollectableSearchCondition condChild : compositecond.getOperands()) {
				CollectController.this._setSearchFieldsAccordingToSearchCondition(condChild, false);
			}
			return null;
		}

		@Override
        public Void visitSubCondition(CollectableSubCondition subcond) throws CommonBusinessException {
			CollectController.this._setSearchFieldsAccordingToSubCondition(subcond);
			return null;
		}

		@Override
        public Void visitPivotJoinCondition(PivotJoinCondition joincond) throws CommonBusinessException {
			// TODO: fix
			// CollectController.this._setSearchFieldsAccordingToSubCondition(joincond);
			return null;
		}

		@Override
        public Void visitRefJoinCondition(RefJoinCondition joincond) throws CommonBusinessException {
			// TODO: fix
			// CollectController.this._setSearchFieldsAccordingToSubCondition(joincond);
			return null;
		}

		@Override
        public Void visitReferencingCondition(ReferencingCollectableSearchCondition refcond) throws CommonBusinessException {
			CollectController.this._setSearchFieldsAccordingToReferencingSearchCondition(refcond);
			return null;
		}

		@Override
        public Void visitIdCondition(CollectableIdCondition idcond) throws CommonBusinessException {
			throw new CommonFatalException("Id-Bedingungen k\u00f6nnen nicht in der Suchmaske dargestellt werden.");
		}

		@Override
        public Void visitIdListCondition(
            CollectableIdListCondition collectableIdListCondition)
            throws CommonBusinessException {
			throw new CommonFatalException("Id-Bedingungen k\u00f6nnen nicht in der Suchmaske dargestellt werden.");
        }

	}	// inner class SetSearchFieldsVisitor

	// TODO don't define those keybindings here
	protected void setupShortcutsForTabs(MainFrameTab ifrm) {
		final Action actSelectSearchTab = new AbstractAction() {

			@Override
            public void actionPerformed(ActionEvent ev) {
				if (getCollectPanel().isTabbedPaneEnabledAt(CollectPanel.TAB_SEARCH)) {
					getCollectPanel().setTabbedPaneSelectedComponent(getSearchPanel());
				}
			}
		};

		final Action actSelectResultTab = new AbstractAction() {

			@Override
            public void actionPerformed(ActionEvent ev) {
				if (getCollectPanel().isTabbedPaneEnabledAt(CollectPanel.TAB_RESULT)) {
					getCollectPanel().setTabbedPaneSelectedComponent(getResultPanel());
				}
			}
		};

		final Action actSelectDetailsTab = new AbstractAction() {

			@Override
            public void actionPerformed(ActionEvent ev) {
				if (getCollectPanel().isTabbedPaneEnabledAt(CollectPanel.TAB_DETAILS)) {
					getCollectPanel().setTabbedPaneSelectedComponent(getDetailsPanel());
				}
			}
		};

		final String sKeySelectSearchTab = "SelectSearchTab";
		ifrm.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), sKeySelectSearchTab);
		ifrm.getRootPane().getActionMap().put(sKeySelectSearchTab, actSelectSearchTab);

		final String sKeySelectResultTab = "SelectResultTab";
		ifrm.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), sKeySelectResultTab);
		ifrm.getRootPane().getActionMap().put(sKeySelectResultTab, actSelectResultTab);

		/**
		 * inner class SelectTabAction
		 */
		class SelectTabAction extends AbstractAction {

			private int iDirection;

			/**
			 * @param iDirection -1 for previous tab, +1 for next tab
			 */
			SelectTabAction(int iDirection) {
				this.iDirection = iDirection;
			}

			@Override
            public void actionPerformed(ActionEvent ev) {
				int iExternalTabIndex = getCollectPanel().getTabbedPaneSelectedIndex();

				// try to find the next enabled tab in the given direction.
				// If the selected tab is the only enabled tab, do nothing:
				for (int i = 0; i < getCollectPanel().getTabCount() - 1; ++i) {
					int iInternalTabIndex = getCollectPanel().getTabIndexOf(iExternalTabIndex);
					iInternalTabIndex = (iInternalTabIndex + iDirection + getCollectPanel().getTabCount()) % getCollectPanel().getTabCount();
					iExternalTabIndex = getCollectPanel().getExternalTabIndexOf(iInternalTabIndex);
					if (getCollectPanel().isTabbedPaneEnabledAt(iExternalTabIndex)) {
						getCollectPanel().setTabbedPaneSelectedIndex(iExternalTabIndex);
						break;
					}
				}
			}
		}	// inner class SelectTabAction

		final String sKeySelectPreviousTab = "SelectPreviousTab";
		ifrm.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK), sKeySelectPreviousTab);
		ifrm.getRootPane().getActionMap().put(sKeySelectPreviousTab, new SelectTabAction(-1));

		final String sKeySelectNextTab = "SelectNextTab";
		ifrm.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK), sKeySelectNextTab);
		ifrm.getRootPane().getActionMap().put(sKeySelectNextTab, new SelectTabAction(+1));
	}

	/**
	 * Command: the frame is closing.
	 */
	protected void cmdFrameClosing() {
		if (this.stopEditingInDetails()) {
			this.bFrameMayBeClosed = this.askAndSaveIfNecessary();
			if (this.bFrameMayBeClosed) {
				this.getFrame().dispose();
			}
		}
	}

	/**
	 * command: switch to New mode
	 */
	protected void cmdEnterNewMode() {
		UIUtils.runCommand(this.getFrame(), new Runnable() {
			@Override
            public void run() {
				enterNewMode();
			}
		});
	}

	/**
	 * command: switch to New mode with search values
	 *
	 * @deprecated Move to SearchController and make protected again.
	 */
	public void cmdEnterNewModeWithSearchValues() {
		UIUtils.runCommand(this.getFrame(), new Runnable() {
			@Override
            public void run() {
				enterNewModeWithSearchValues();
			}
		});
	}

	/**
	 * command: Bookmark selected Collectable
	 */
	protected void cmdBookmarkSelectedCollectable() {
		UIUtils.runCommand(this.getFrame(), new CommonRunnable() {
			@Override
            public void run() throws CommonBusinessException {
				bookmarkSelectedCollectable();
			}
		});
	}

	protected void bookmarkSelectedCollectable() throws CommonBusinessException {
		Main.getInstance().getMainFrame().addBookmark(getEntityName(), (Integer) getSelectedCollectableId(), getLabelForStartTab());
	}

	/**
	 * command: Open selected Collectable in new tab
	 */
	protected void cmdOpenSelectedCollectableInNewTab() {
		this.getFrame().lockLayerBusy();
		openSelectedCollectableInNewTab();
	}

	protected void openSelectedCollectableInNewTab() {
		String entity = getEntityName();
		final JTabbedPane openInTabbed;
		if (MainFrame.isPredefinedEntityOpenLocationSet(entity))
			openInTabbed = MainFrame.getPredefinedEntityOpenLocation(entity);
		else
			openInTabbed = MainFrame.getTabbedPane(CollectController.this.getFrame());

		final List<Clct> selectedList = getSelectedCollectables();
		final List<Thread> loadingThreads = new ArrayList<Thread>();

		final int openQuestionCount = 5;
		final int size = selectedList.size();

		for (int i = 0; i < size; i++) {
			final int currentIndex = i;
			final Object id = selectedList.get(i).getId();

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					UIUtils.invokeOnDispatchThread(new Runnable() {
						@Override
						public void run() {
							try {
								if (currentIndex == openQuestionCount && size >= openQuestionCount*2) {
									int res = JOptionPane.showConfirmDialog(openInTabbed,
											getSpringLocaleDelegate().getMessage(
													"CollectController.openInNewTab.1","Es wurden bereits {0} Tabs geöffnet. Möchten Sie die weiteren {1} Tabs auch noch öffnen?", openQuestionCount, (size-openQuestionCount)),
											getSpringLocaleDelegate().getMessage(
													"CollectController.openInNewTab.2","Wirklich alle selektierten Datensätze in neuen Tabs öffnen?"),
											JOptionPane.YES_NO_OPTION,
											JOptionPane.QUESTION_MESSAGE);

									if (res == JOptionPane.NO_OPTION) {
										getFrame().unlockLayer();
										loadingThreads.clear();
										return;
									}
								}

								MainFrameTab tab = new MainFrameTab();
								openInTabbed.add(tab);

								NuclosCollectController<?> clct = NuclosCollectControllerFactory.getInstance().newCollectController(openInTabbed, getEntityName(), tab);
								getMainController().initMainFrameTab(clct, tab);
								tab.postAdd();

								clct.runViewSingleCollectableWithId(id, false);

								loadingThreads.remove(0);
								if (loadingThreads.size() > 0) {
									loadingThreads.get(0).start();
								} else {
									getFrame().unlockLayer();
								}
							} catch(Exception e) {
								throw new NuclosFatalException(e);
							}
						}
					});
				}
			}, "CollectController.openSelectedCollectableInNewTab");
			loadingThreads.add(t);
		}

		if (loadingThreads.size() > 0) {
			loadingThreads.get(0).start();
		} else {
			this.getFrame().unlockLayer();
		}
	}

	/**
	 * command: Clone selected Collectable
	 */
	protected void cmdCloneSelectedCollectable() {
		UIUtils.runCommand(this.getFrame(), new CommonRunnable() {
			@Override
            public void run() throws CommonBusinessException {
				cloneSelectedCollectable();
			}
		});
	}

	/**
	 * switches to "New" mode and fills the Details panel with the contents of the selected Collectable.
	 * @throws CommonBusinessException
	 */
	protected void cloneSelectedCollectable() throws CommonBusinessException {
		final Clct clctBackup = this.getCompleteSelectedCollectable();
		this.enterNewMode();
		this.unsafeFillDetailsPanel(clctBackup);
	}

	/**
	 * switches to New mode
	 */
	private void enterNewMode() {
		broadcastCollectableEvent(getSelectedCollectable(), MessageType.CLCT_LEFT);
		if (this.stopEditingInDetails()) {
			try {
				this.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW);
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(this.getFrame(), ex);
			}
		}
	}

	/**
	 * switches to New mode with search values
	 */
	private void enterNewModeWithSearchValues() {
		if (this.stopEditingInDetails()) {
			try {
				this.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_SEARCHVALUE);
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(this.getFrame(), ex);
			}
		}
	}

	/**
	 * command: switch to View mode
	 */
	protected void cmdEnterViewMode() {
		UIUtils.runCommand(this.getFrame(), new CommonRunnable() {
			@Override
            public void run() throws CommonBusinessException {
				enterViewMode();
			}
		});
	}

	/**
	 * switches to View mode
	 */
	private void enterViewMode() throws CommonBusinessException {
		if (!isReadAllowed(getSelectedCollectable())) {
			throw new CommonBusinessException("Lesen des Datensatzes nicht erlaubt.");
		}
		this.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_VIEW);
	}

	protected void cmdEnterMultiViewMode() {
		UIUtils.runCommand(this.getFrame(), new CommonRunnable() {
			@Override
            public void run() throws CommonBusinessException {
				enterMultiViewMode();
			}
		});
	}

	private void enterMultiViewMode() throws CommonBusinessException {
		if (!isReadAllowed(getSelectedCollectables())) {
			throw new CommonBusinessException("Mindestens einer der ausgew\u00e4hlten Datens\u00e4tze darf nicht gelesen werden.");
		}
		setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_MULTIVIEW);
	}

	/**
	 * @return the selected <code>Collectable</code>, if any, from the table model.
	 * Note that there is no selected <code>Collectable</code> in New mode.
	 * Note that the result might be incomplete, that means, some fields might be missing.
	 * TODO consider inlining or renaming this method. It's too easy to forget that the result might be incomplete.
	 *
	 * @deprecated Use getResultController().getSelectedCollectableFromTableModel()
	 */
	protected final Clct getSelectedCollectable() {
		return getResultController().getSelectedCollectableFromTableModel();
	}

	/**
	 * @return the id of the selected <code>Collectable</code>, if any, from the table model.
	 */
	public final Object getSelectedCollectableId() {
		final Collectable clctSelected = this.getSelectedCollectable();
		return (clctSelected == null ? null : clctSelected.getId());
	}

	/**
	 * the complete selected <code>Collectable</code>, if any. If the currently selected Collectable in the table model isn't complete
	 * (that is: partially loaded), the <code>Collectable</code> is reloaded completely and written back to the table model, before it is returned.
	 * Therefore, consecutive calls won't reload the returned <code>Collectable</code> again.
	 * Note that this method has the side-effect of replacing the selected Collectable in the ResultTableModel.
	 * @postcondition (result != null) --> isCollectableComplete(result)
	 * @throws CommonBusinessException
	 */
	protected final Clct getCompleteSelectedCollectable() throws CommonBusinessException {
		return getCompleteSelectedCollectable(false);
	}

	/**
	 * same as <code>getCompleteSelectedCollectable()</code> but with option to load the collectable without dependants
	 * @param blnWithoutDependants
	 * @return
	 * @throws CommonBusinessException
	 */
	protected final Clct getCompleteSelectedCollectable(boolean blnWithoutDependants) throws CommonBusinessException {
		final Clct clct = getResultController().getSelectedCollectableFromTableModel();
		final Clct result;
		if (clct == null || ss.isCollectableComplete(clct)) {
			result = clct;
		}
		else {
			result = this.readCollectable(clct, blnWithoutDependants);
			getResultController().replaceSelectedCollectableInTableModel(result);
		}
		assert !(result != null) || ss.isCollectableComplete(result);
		// Note that the postcondition "result == this.getSelectedCollectableFromTableModel()" is not possible for some
		// implementations of the CollectController that use an UnmodifiableListAdapter around a ProxyList for the TableModel.
		return result;
	}

	/**
	 * @return the selected (possibly incomplete) <code>Collectable</code>s.
	 * @postcondition result != null
	 * @see #isCollectableComplete(Collectable)
	 */
	public final List<Clct> getSelectedCollectables() {
		final List<Integer> lstSelectedRowNumbers = CollectionUtils.asList(this.getResultTable().getSelectedRows());
		final List<Clct> result = CollectionUtils.transform(lstSelectedRowNumbers, new Transformer<Integer, Clct>() {
			@Override
            public Clct transform(Integer iRowNo) {
				return getResultTableModel().getCollectable(iRowNo);
			}
		});
		assert result != null;
		return result;
	}

	/**
	 * @return the selected <code>Collectable</code>s, all of which are complete.
	 * @postcondition result != null
	 * @see #isCollectableComplete(Collectable)
	 */
	public final List<Clct> getCompleteSelectedCollectables() throws CommonBusinessException {
		final List<Clct> result = new ArrayList<Clct>(getSearchStrategy().getCompleteCollectablesStrategy().getCompleteCollectables(this.getSelectedCollectables()));
		getResultController().replaceCollectablesInTableModel(result);

		assert result != null;
		return result;
	}

	/**
	 * command: refresh current <code>Collectable</code>
	 * TODO this action is "overloaded". It's at least refresh and cancel in one...
	 */
	protected void cmdRefreshCurrentCollectable() {
		LOG.debug("START cmdRefreshCurrentCollectable");
		assert this.statemodel.getOuterState() == CollectState.OUTERSTATE_DETAILS;

		// try to get changes from table cell editors:
		// Ignore the result - refresh is always performed, even if stopEditingInDetails fails
		this.stopEditingInDetails();

		UIUtils.runShortCommand(this.getFrame(), new CommonRunnable() {
			@Override
            public void run() {
				try {
					final int iDetailsMode = statemodel.getDetailsMode();

					switch (iDetailsMode) {
						case CollectState.DETAILSMODE_VIEW:
							refreshCurrentCollectable();
							broadcastCollectableEvent(getSelectedCollectable(), MessageType.REFRESH_DONE_DIRECTLY);
							break;

						case CollectState.DETAILSMODE_EDIT:
							if (askAndSaveIfNecessary()) {
								refreshCurrentCollectable();
								broadcastCollectableEvent(getSelectedCollectable(), MessageType.REFRESH_DONE_DIRECTLY);
							}
							break;

						case CollectState.DETAILSMODE_NEW_CHANGED:
							if (askAndSaveIfNecessary()) {
								enterNewMode();
							}
							break;

						case CollectState.DETAILSMODE_MULTIVIEW:
						case CollectState.DETAILSMODE_MULTIEDIT:
							if (askAndSaveIfNecessary()) {
								enterMultiViewMode();
							}
							break;

						default:
							assert false;
					}
				}
				catch (CommonBusinessException ex) {
					final String sErrorMsg = "Der Datensatz konnte nicht neu geladen werden.";
					Errors.getInstance().showExceptionDialog(getFrame(), sErrorMsg, ex);
				}
			}
		});
		LOG.debug("FINISHED cmdRefreshCurrentCollectable");
	}

	/**
	 * Get the currently viewed collectable again from the database and display it.
	 * This is done in a background thread.
	 * @throws CommonBusinessException
	 */
	public void refreshCurrentCollectable() throws CommonBusinessException {
		refreshCurrentCollectable(true);
	}

	/**
	 * Get the currently viewed collectable again from the database and display it.
	 * This is done in a background thread.
	 * @throws CommonBusinessException
	 */
	public void refreshCurrentCollectable(boolean withMultiThreader) throws CommonBusinessException {
		assert this.statemodel.getOuterState() == CollectState.OUTERSTATE_DETAILS;
		assert CollectState.isDetailsModeViewOrEdit(statemodel.getDetailsMode());

		final CommonClientWorkerSelfExecutable clientWorker = new CommonClientWorkerAdapter<Clct>(CollectController.this) {
			private Clct clct;

			@Override
			public void init() throws CommonBusinessException {
				if (!CollectController.this.isRefreshSelectedCollectableAllowed()) {
					throw new CommonPermissionException("Aktualisieren ist nicht erlaubt.");
				}
			}
			@Override
			public void work() throws CommonBusinessException {
				// clear cache before refreshing
				valueListProviderCache.clear();
				// reread the selected object from the database:
				clct = CollectController.this.readSelectedCollectable();
			}
			@Override
			public void paint() throws CommonBusinessException {
				if(clct == null) return;

				// replace the selected object in the result list:
				getResultController().replaceSelectedCollectableInTableModel(clct);

				CollectController.this.enterViewMode();

				broadcastCollectableEvent(clct, MessageType.REFRESH_DONE);
			}
		};

		if (withMultiThreader) {
			CommonMultiThreader.getInstance().execute(clientWorker);
		} else {
			clientWorker.runInCallerThread();
		}
	}

	/**
	 * registers a CollectableEventListener
	 * @param CollectableEventListener
	 */
	public final void addCollectableEventListener(CollectableEventListener l) {
		if (!collectableListeners.contains(l)) {
			collectableListeners.add(l);
		}
	}

	/**
	 * removes the given CollectableEventListener
	 * @param CollectableEventListener
	 */
	public final void removeCollectableEventListener(CollectableEventListener l) {
		collectableListeners.remove(l);
	}

	/**
	 * fires a handleCollectableEvent for all registered listeners
	 * @param Clct
	 * @param MessageType
	 */
	protected void broadcastCollectableEvent(Clct collectable, MessageType messageType) {
		for(CollectableEventListener l : new ArrayList<CollectableEventListener>(collectableListeners)) {
			l.handleCollectableEvent(collectable, messageType);
		}
	}

	/**
	 * (re)reads the <code>Collectable</code> that is selected in the result list from its "data source"
	 * (typically the server). Nothing else.
	 * @return the current value of the selected Collectable, as returned by the server.
	 * TODO make this final when readCollectable is refactored.
	 * @postcondition result != null
	 * @postcondition isCollectableComplete(result)
	 */
	protected Clct readSelectedCollectable() throws CommonBusinessException {
		final Clct clct = this.getSelectedCollectable();
		if (clct == null) {
			throw new IllegalStateException("Kein Objekt ausgew\u00e4hlt.");
		}
		final Clct result = this.readCollectable(clct);
		assert result != null;
		assert ss.isCollectableComplete(result);
		return result;
	}

	/**
	 * (re)reads the given <code>Collectable</code> from its "data source" (typically the server).
	 * The default implementation uses {@link #findCollectableById(String, Object)}.
	 * Successors shouldn't redefine this method unless they can hold objects of more than one entity
	 * (such as "general search").
	 * @param clct the Collectable to be read from the server. May be incomplete.
	 * @return the complete Collectable, as returned by the server.
	 * @precondition clct != null
	 * @postcondition result != null
	 * @postcondition isCollectableComplete(result)
	 */
	public Clct readCollectable(Clct clct) throws CommonBusinessException {
		return readCollectable(clct, false);
	}

	/**
	 * same as <code>readCollectable(Clct clct)</code> but with option to read the collectable without dependants
	 * @param clct the Collectable to be read from the server. May be incomplete.
	 * @param blnWithoutDependants
	 * @return the complete Collectable, as returned by the server.
	 * @throws CommonBusinessException
	 */
	public Clct readCollectable(Clct clct, boolean blnWithoutDependants) throws CommonBusinessException {
		if (blnWithoutDependants) {
			return this.findCollectableByIdWithoutDependants(this.getEntityName(), clct.getId());
		} else {
			return this.findCollectableById(this.getEntityName(), clct.getId());
		}
	}

	/**
	 * finds the Collectable from the given id, fetching it from the underlying data store.
	 * @param sEntity
	 * @param oId
	 * @return the Collectable with the given entity and id.
	 * @throws CommonBusinessException
	 * @precondition sEntity != null
	 * @precondition oId != null
	 * @postcondition result != null
	 * @postcondition isCollectableComplete(result)
	 */
	protected abstract Clct findCollectableById(String sEntity, Object oId) throws CommonBusinessException;

	/**
	 * finds the Collectable from the given id, fetching it from the underlying data store.
	 * @param sEntity
	 * @param oId
	 * @return the Collectable with the given entity and id, but without dependants!
	 * @throws CommonBusinessException
	 */
	protected abstract Clct findCollectableByIdWithoutDependants(String sEntity, Object oId) throws CommonBusinessException;

	/**
	 * returns the version of the given entity
	 * @param sEntity
	 * @param oId
	 * @throws CommonBusinessException
	 */
	protected Integer getVersionOfCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		return EntityObjectDelegate.getInstance().getVersion(sEntity, IdUtils.toLongId(oId));
	}

	/**
	 * TODO: Make protected again.
	 */
	public void setCollectState(int iTab, int iMode) throws CommonBusinessException {
		this.statemodel.setCollectState(iTab, iMode);
	}

	/**
	 * @return the <code>CollectableEntity</code> of the objects that are to be collected with this controller.
	 * There are cases (eg. general search) where those objects have different entities. In these cases, this method
	 * returns the most common entity. Note that there must be such a common entity for the <code>CollectController</code>
	 * to work properly.
	 * @see #getCollectableEntityForDetails()
	 * TODO add postcondition result != null
	 */
	public CollectableEntity getCollectableEntity() {
		return this.clcte;
	}

	/**
	 * @return This default implementation returns <code>this.getCollectableEntity()</code>.
	 * There are cases (eg. general search) where the objects have different entities. In these cases, successors
	 * must implement this method by returning the entity of the object currently edited in Details mode (EDIT or NEW).
	 * @see #getCollectableEntity()
	 */
	protected CollectableEntity getCollectableEntityForDetails() {
		return this.getCollectableEntity();
	}

	/**
	 * fills the collectable component models in the details panel with the values in <code>clct</code>.
	 * This method is safe as to detailsChanged(), that means detailsChanged() will not be called through filling the panel.
	 * @param clct
	 * @precondition clct != null
	 * @precondition isCollectableComplete(clct)
	 * TODO move to DetailsController or DetailsPanel
	 */
	protected final void safeFillDetailsPanel(Clct clct) throws CommonBusinessException {
		if (clct == null) {
			throw new NullArgumentException("clct");
		}
		if (!ss.isCollectableComplete(clct)) {
			throw new IllegalArgumentException("clct");
		}
		final boolean bWasDetailsChangedIgnored = this.isDetailsChangedIgnored();
		this.setDetailsChangedIgnored(true);
		try {
			this.unsafeFillDetailsPanel(clct);
		}
		finally {
			this.setDetailsChangedIgnored(bWasDetailsChangedIgnored);
			this.highlightMandatory();
		}
	}

	/**
	 * mandatory fields are highlighted with a special color.
	 */
	protected void highlightMandatory() {
		Set<String> mandatoryfields = new HashSet<String>();
		for (EntityFieldMetaDataVO efMeta : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(getEntityName()).values()) {
			if (!efMeta.isNullable()) {
				mandatoryfields.add(efMeta.getField());
			}
		}
		setCollectableComponentModelsInDetailsMandatory(mandatoryfields);
	}

	/**
	 * fills the collectable component models in the details panel with the values in <code>clct</code>.
	 * This method is unsafe as to the change listeners, that means the change listeners should be removed
	 * before calling this method.
	 * @param clct
	 * @precondition clct != null
	 * @precondition isCollectableComplete(clct)
	 * TODO move to DetailsController or DetailsPanel
	 */
	protected void unsafeFillDetailsPanel(Clct clct) throws CommonBusinessException {
		if (clct == null) {
			throw new NullArgumentException("clct");
		}
		if (!ss.isCollectableComplete(clct)) {
			throw new IllegalArgumentException("clct");
		}
		for (DetailsComponentModel clctcompmodel : getDetailsPanel().getEditModel().getCollectableComponentModels()) {
			clctcompmodel.setField(clct.getField(clctcompmodel.getFieldName()));
		}
	}

	/**
	 * fills the collectable component models in the details panel with the common values in <code>collclct</code>.
	 * This method is safe as to detailsChanged(), that means detailsChanged() will not be called through filling the panel.
	 * @param collclct the Collectables to edit.
	 * TODO move to DetailsController or DetailsPanel
	 */
	protected final void safeFillMultiEditDetailsPanel(Collection<Clct> collclct) throws CommonBusinessException {
		final boolean bWasDetailsChangedIgnored = this.isDetailsChangedIgnored();
		this.setDetailsChangedIgnored(true);
		try {
			this.unsafeFillMultiEditDetailsPanel(collclct);
		}
		finally {
			this.setDetailsChangedIgnored(bWasDetailsChangedIgnored);
		}
	}

	/**
	 * fills the collectable component models in the details panel with the common values in <code>collclct</code>.
	 * This method is unsafe as to the change listeners, that means the change listeners should be removed
	 * before calling this method.
	 * @param collclct the <code>Collectable</code>s to edit.
	 * TODO move to DetailsController or DetailsPanel
	 */
	protected void unsafeFillMultiEditDetailsPanel(Collection<Clct> collclct) throws CommonBusinessException {
		for (DetailsComponentModel clctcompmodel : getDetailsPanel().getEditModel().getCollectableComponentModels()) {
			assert clctcompmodel.isMultiEditable();
			final String sFieldName = clctcompmodel.getFieldName();
			final CollectableField clctfCommonValue = CollectableUtils.getCommonValue(collclct, sFieldName);
			if (clctfCommonValue == null) {
				clctcompmodel.unsetCommonValue();
				clctcompmodel.clear();
			}
			else {
				clctcompmodel.setCommonValue(clctfCommonValue);
				clctcompmodel.setField(clctfCommonValue);
			}
			clctcompmodel.setValueToBeChanged(false);
		}
	}

	/**
	 * calls <code>setMultiEditable(bMultiEditable)</code> on all collectable component models in the Details tab.
	 * @param bMultiEditable
	 * TODO move to DetailsController or DetailsPanel
	 */
	protected void setCollectableComponentModelsInDetailsPanelMultiEditable(boolean bMultiEditable) {
		for (DetailsComponentModel clctcompmodel : getDetailsPanel().getEditModel().getCollectableComponentModels()) {
			clctcompmodel.setMultiEditable(bMultiEditable);
		}
	}

	/**
	 *
	 * @param mandatoryfields
	 */
	protected void setCollectableComponentModelsInDetailsMandatory(Set<String> mandatoryfields) {
		for (DetailsComponentModel clctcompmodel : getDetailsPanel().getEditModel().getCollectableComponentModels()) {
			if (mandatoryfields.contains(clctcompmodel.getEntityField().getName())) {
				clctcompmodel.setMandatory(true);
			}
		}
	}

	/**
	 *
	 * @param mandatoryfields
	 */
	protected void setCollectableComponentModelsInDetailsMandatoryAdded(Set<String> mandatoryfields) {
		for (DetailsComponentModel clctcompmodel : getDetailsPanel().getEditModel().getCollectableComponentModels()) {
			if (mandatoryfields.contains(clctcompmodel.getEntityField().getName())) {
				clctcompmodel.setMandatoryAdded(true);
			}
		}
	}

	protected void resetCollectableComponentModelsInDetailsMandatory() {
		for (DetailsComponentModel clctcompmodel : getDetailsPanel().getEditModel().getCollectableComponentModels()) {
			clctcompmodel.setMandatory(false);
		}
	}

	protected void resetCollectableComponentModelsInDetailsMandatoryAdded() {
		for (DetailsComponentModel clctcompmodel : getDetailsPanel().getEditModel().getCollectableComponentModels()) {
			clctcompmodel.setMandatoryAdded(false);
		}
	}

	/**
	 * reads the field values contained in the specified edit panel into <code>clct</code>.
	 * @param clct
	 * @param bSearchTab Read values from Search panel? Otherwise read values from Details panel.
	 * @throws CollectableValidationException
	 * @precondition clct != null
	 * TODO move to SearchPanel / DetailsPanel (or EditView)
	 */
	protected void readValuesFromEditPanel(Clct clct, boolean bSearchTab) throws CollectableValidationException {
		if (clct == null) {
			throw new NullArgumentException("clct");
		}
		for (CollectableComponent clctcomp : this.getEditView(bSearchTab).getCollectableComponents()) {
			final String sFieldName = clctcomp.getFieldName();
			final CollectableField clctf;
			try {
				clctf = clctcomp.getField();
			}
			catch (CollectableFieldFormatException ex) {
				final CollectableEntityField clctef = this.getCollectableEntity().getEntityField(sFieldName);
				throw new CollectableValidationException(clctef, ex);
			}
			catch (Exception ex) {
				final String sMessage = "Fehler beim Lesen des Felds " + sFieldName + ".";
				throw new CommonFatalException(sMessage, ex);
			}
			clct.setField(sFieldName, clctf);
		}
	}

	/**
	 * updates the current <code>Collectable</code>: fills it with the values from the Details panel, validates it
	 * and updates it in the database. The following methods are called (in this order):
	 * <ol>
	 * <it>this.getCollectStateModel().getEditedCollectable()</it>
	 * <it>this.readValuesFromEditPanel(Collectable, false)</it>
	 * <it>this.validate(Collectable)</it>
	 * <it>this.updateCurrentCollectable(Collectable)</it>
	 * </ol>
	 *
	 * tsc: parameter in old signature <code>Clct updateCurrentCollectable(boolean dbUpdate)</code> was
	 *      useless, because a lot of business logic in subsequent updateCurrentCollectable()-methods was
	 *      skipped by setting the parameter to false.
	 *
	 * @return the updated <code>Collectable</code>, as returned by the server.
	 * @throws CommonBusinessException
	 * @postcondition result != null
	 * @postcondition isCollectableComplete(result)
	 */
	protected Clct updateCurrentCollectable() throws CommonBusinessException {
		final boolean bWasDetailsChangedIgnored = this.isDetailsChangedIgnored();
		this.setDetailsChangedIgnored(true);
		try {
			final Clct clctCurrent = this.getCollectStateModel().getEditedCollectable();
			assert clctCurrent != null;
			this.readValuesFromEditPanel(clctCurrent, false);
			this.prepareCollectableForSaving(clctCurrent, this.getCollectableEntityForDetails());
			this.validate(clctCurrent);
			final Clct result = this.updateCurrentCollectable(clctCurrent);
			assert result != null;
			assert ss.isCollectableComplete(result);
			return result;
		}
		finally {
			this.setDetailsChangedIgnored(bWasDetailsChangedIgnored);
		}
	}

	/**
	 * updates the currently edited <code>Collectable</code> in the database.
	 * @param clctCurrent the currently edited <code>Collectable</code>. It is filled from the
	 * edit panel and validated before this method is called.
	 * @return the updated <code>Collectable</code>, as returned by the server
	 * @precondition clctCurrent != null
	 * @postcondition result != null
	 * @postcondition isCollectableComplete(result)
	 */
	protected Clct updateCurrentCollectable(Clct clctCurrent) throws CommonBusinessException {
		final Clct result = this.updateCollectable(clctCurrent, null);
		assert result != null;
		assert ss.isCollectableComplete(result);
		return result;
	}

	/**
	 * updates the given <code>Collectable</code> in the database.
	 * @param clct the <code>Collectable</code> to update.
	 * @param oAdditionalData Optional additional data that might be needed for storing, eg. dependant records.
	 * @return the updated <code>Collectable</code>, as returned by the server
	 * @precondition clct != null
	 * @postcondition result != null
	 * @postcondition isCollectableComplete(result)
	 */
	protected abstract Clct updateCollectable(Clct clct, Object oAdditionalData)
			throws CommonBusinessException;

	/**
	 * inserts the currently edited (new) Collectable into the database
	 * @param clctNew is filled from the edit panel and validated before this method is called.
	 * @return the inserted <code>Collectable</code>, as returned by the server.
	 * @precondition clctNew.getId() == null
	 * @postcondition result != null;
	 * @postcondition isCollectableComplete(result)
	 */
	protected abstract Clct insertCollectable(Clct clctNew) throws CommonBusinessException;

	/**
	 * creates a new Collectable, fills it with the values from the Details panel, validates it
	 * and inserts it in the database. The following methods are called (in this order):
	 * <ol>
	 * <it>this.newCollectable()</it>
	 * <it>this.readValuesFromEditPanel(Collectable, false)</it>
	 * <it>this.validate(Collectable)</it>
	 * <it>this.insertCollectable(Collectable)</it>
	 * </ol>
	 * @return the inserted <code>Collectable</code>, as returned by the server.
	 * @throws CommonBusinessException
	 * @postcondition result != null
	 * @postcondition isCollectableComplete(result)
	 */
	private Clct insertCollectable() throws CommonBusinessException {
		final boolean bWasDetailsChangedIgnored = this.isDetailsChangedIgnored();
		this.setDetailsChangedIgnored(true);
		try {
			final Clct clctNew = this.newCollectable();
			this.readValuesFromEditPanel(clctNew, false);
			this.prepareCollectableForSaving(clctNew, this.getCollectableEntityForDetails());
			this.validate(clctNew);
			final Clct result = this.insertCollectable(clctNew);
			assert result != null;
			assert ss.isCollectableComplete(result);
			return result;
		}
		finally {
			this.setDetailsChangedIgnored(bWasDetailsChangedIgnored);
		}
	}

	/**
	 * This method is called when a Collectable is about to be saved (inserted or updated), just before it is validated.
	 * Default implementation: Do nothing.
	 * Successors may change the given <code>Collectable</code> in order to prepare it for saving. A common application
	 * is to change Booleans from <code>null</code> to <code>false</code>.
	 * @param clctCurrent the object that is about to be saved
	 * @param clcteCurrent the <code>CollectableEntity</code> of <code>clctCurrent</code>
	 * @precondition clctCurrent != null
	 * @precondition isCollectableComplete(clctCurrent)
	 * @precondition clcteCurrent != null
	 */
	protected void prepareCollectableForSaving(Clct clctCurrent, CollectableEntity clcteCurrent) {
		// do nothing here
	}

	/**
	 * validates the given <code>Collectable</code>.
	 * This is called before the <code>clct</code> is stored.
	 * The default implementation here calls <code>clct.validate(this.getCollectableEntityForDetails())</code>.
	 * @param clct
	 * @throws CommonBusinessException
	 * @precondition clct != null
	 * @precondition isCollectableComplete(clct)
	 */
	protected void validate(Clct clct) throws CommonBusinessException {
		if (clct == null) {
			throw new NullArgumentException("clct");
		}
		if (!ss.isCollectableComplete(clct)) {
			throw new IllegalArgumentException("clct");
		}
		clct.validate(this.getCollectableEntityForDetails());
	}

	/**
	 * stops editing in the Details panel.
	 * Derived classes may stop editing on fields, TableCellEditors etc. here
	 * @return Has the editing been stopped?
	 */
	protected boolean stopEditingInDetails() {
		// do nothing here
		return true;
	}

	/**
	 * stops editing in the Search panel.
	 * Derived classes may stop editing on fields, TableCellEditors etc. here
	 * @return Has the editing been stopped?
	 *
	 * TODO: Make this protected again.
	 */
	public boolean stopEditingInSearch() {
		// do nothing here
		return true;
	}

	/**
	 * Command: Save
	 * TODO add precondition this.getCollectState().isDetailsMode()?
	 */
	private void cmdSave() {
		final boolean bWasDetailsChangedIgnored = this.isDetailsChangedIgnored();
		// detailsChanged must be ignored here as stopEditingInDetails might cause detailsChanged to be fired:
		this.setDetailsChangedIgnored(true);
		try {
			if (!this.stopEditingInDetails()) {
				// TODO show error message
			}
			else {
				final String sMessage1 = getSpringLocaleDelegate().getMessage(
						"CollectController.12","Der Datensatz konnte nicht gespeichert werden");
				try {
					try {
						this.save();
					} catch (Exception ex) {
						if (!handleSpecialException(ex)) {
							throw ex;
						}
					}
				}
				catch (CollectableValidationException ex) {
					handleCollectableValidationException(ex, sMessage1);
				}
				catch (CommonPermissionException ex) {
					final String sErrorMsg = "Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, " + "um diesen Datensatz zu speichern.";
					Errors.getInstance().showExceptionDialog(this.getFrame(), sErrorMsg, ex);
				}
				catch (CommonStaleVersionException ex) {
					final String sMessage = sMessage1 + ", " + "da er zwischenzeitlich von einem anderen Benutzer ge\u00e4ndert wurde.\n" +
							"Sie m\u00fcssen den Datensatz neu laden und Ihre \u00c4nderungen dann erneut durchf\u00fchren.\n\n" +
							getSpringLocaleDelegate().getMessage("CollectController.25","Soll der Datensatz jetzt neu geladen werden?");
					final int iBtn = JOptionPane.showConfirmDialog(this.getFrame(), sMessage, 
							getSpringLocaleDelegate().getMessage("CollectController.9","Datensatz ge\u00e4ndert"),
							JOptionPane.OK_CANCEL_OPTION);
					if (iBtn == JOptionPane.OK_OPTION) {
						try {
							this.refreshCurrentCollectable();
						}
						catch (CommonBusinessException ex2) {
							Errors.getInstance().showExceptionDialog(this.getFrame(), ex2);
						}
					}
				}
				catch (CommonBusinessException ex) {
					try {
						handleSaveException(ex, sMessage1);
					}
					catch (CommonFinderException ex2) {
						final String sErrorMsg = sMessage1 + ", da er zwischenzeitlich von einem anderen Benutzer gel\u00f6scht wurde.";
						Errors.getInstance().showExceptionDialog(this.getFrame(), sErrorMsg, ex2);
					}
					catch (CommonBusinessException ex2) {
						Errors.getInstance().showExceptionDialog(this.getFrame(), sMessage1 + ".", ex2);
					}
				}
				catch (Exception ex) {
					Errors.getInstance().showExceptionDialog(this.getFrame(), ex);
				}
				catch (Error error) {
					Errors.getInstance().getCriticalErrorHandler().handleCriticalError(this.getFrame(), error);
				}
			}
		}
		finally {
			this.setDetailsChangedIgnored(bWasDetailsChangedIgnored);
		}
	}

	protected boolean handleSpecialException(Exception ex) {
		if (ex instanceof UserCancelledException) {
			// simply do nothing
			getContext().clear();
			return true;
		}
		return false;
	}

	protected void handleCollectableValidationException(CollectableValidationException ex, String sMessage1) {
		Errors.getInstance().showExceptionDialog(this.getFrame(), sMessage1 + ".", ex);

		// set focus to questionable field (if any):
		final CollectableEntityField clctefInvalid = ex.getCollectableEntityField();
		if (clctefInvalid != null) {
			final Collection<CollectableComponent> collclctcomp = getDetailsPanel().getEditView().getCollectableComponentsFor(clctefInvalid.getName());
			if (!collclctcomp.isEmpty()) {
				final CollectableComponent clctcomp = collclctcomp.iterator().next();
				clctcomp.getControlComponent().requestFocusInWindow();
			}
		}
	}

	/**
	 * called to handle a <code>CommonBusinessException</code> occuring in <code>save()</code>.
	 * Default implementation: just <code>throw ex</code>. Successors may define custom exception handling here.
	 * @param ex
	 * @param sMessage1 first part of the message ("record could not be saved")
	 * @throws CommonBusinessException
	 */
	protected void handleSaveException(CommonBusinessException ex, String sMessage1) throws CommonBusinessException {
		if (ex instanceof CollectableValidationException) {
			handleCollectableValidationException((CollectableValidationException) ex, sMessage1);
		} else {
			throw ex;
		}
	}

	/**
	 * updates or inserts the record shown in details, depending on the current collect mode.
	 * @throws CommonFatalException
	 * @throws CommonPermissionException
	 * TODO wait cursor in cmdSave
	 */
	public void save() throws CommonBusinessException {
		if (!isSaveAllowed()) {
			throw new CommonPermissionException("Speichern ist nicht erlaubt.");
		}
		Clct clct = null;
		try {
			UIUtils.setWaitCursor(this.getFrame());

			LOG.debug("START save");
			MessageType mt;
			switch (this.statemodel.getDetailsMode()) {
				case CollectState.DETAILSMODE_EDIT:
					LOG.debug("START save updateCurrentCollectable");
					clct = this.updateCurrentCollectable();
					LOG.debug("FINISHED save updateCurrentCollectable");

					// update the selected collectable in the table model:
					getResultController().replaceSelectedCollectableInTableModel(clct);

					LOG.debug("START save enterViewMode");
					this.enterViewMode();
					LOG.debug("FINISHED save enterViewMode");
					mt = MessageType.EDIT_DONE;
					break;

				case CollectState.DETAILSMODE_NEW_CHANGED:
					clct = this.insertCollectable();

					// jump to view mode:
					this.viewSingleCollectable(clct);
					// CollectController.this.setCollectState(CollectStateModel.OUTERSTATE_DETAILS, CollectStateModel.DETAILSMODE_VIEW);
					/** TODO switching the state isn't enough, we have to show values updated by the server. */
					/** TODO this is not right! Probably should be view(), as in update() (see above) */
					/** TODO the table model must be updated as well. */
					mt = MessageType.NEW_DONE;
					break;

				case CollectState.DETAILSMODE_MULTIEDIT:
					final int iCount = CollectController.this.getResultTable().getSelectedRowCount();
					new UpdateSelectedCollectablesController<Clct>(this).run(getMultiActionProgressPanel(iCount));

					// do nothing else here. UpdateSelectedCollectablesController is executed in its own thread.
					mt = MessageType.EDIT_DONE;
					break;

				default:
					throw new CommonFatalException("Speichern kann nur bei Bearbeitung, Neueingabe oder Sammelbearbeitung durchgef\u00fchrt werden.");
			}
			broadcastCollectableEvent(clct, mt);
		} catch (CommonBusinessException cbe) {
			if (!handleSpecialException(cbe)) {
				throw cbe;
			}
		} finally {
			this.getFrame().setCursor(null);
			LOG.debug("FINISHED save");
		}
	}

	/**
	 * @param clct the Collectable that will be updated as part of a multiple update. This method should not alter it.
	 * @return additional data (if any) needed for multiple updates (usually, that is data dependant on the given Collectable).
	 * The default implementation returns <code>null</code>.
	 * @throws CommonBusinessException on errors like validation.
	 * TODO move to DetailsController?
	 */
	protected Object getAdditionalDataForMultiUpdate(Clct clct) throws CommonBusinessException {
		return null;
	}

	/**
	 * deletes the selected <code>Collectable</code> permanently
	 * @throws CommonBusinessException
	 */
	protected final void deleteSelectedCollectable() throws CommonBusinessException {
		this.deleteCollectable(this.getSelectedCollectable());
	}

	/**
	 * deletes the given <code>Collectable</code> permanently.
	 * @param clct
	 * @throws CommonBusinessException
	 * @precondition clct != null
	 */
	protected abstract void deleteCollectable(Clct clct) throws CommonBusinessException;

	/**
	 * deletes the given <code>Collectable</code>.
	 * @throws CommonPermissionException if deletion of the given <code>Collectable</code> is not allowed for the current user.
	 * @precondition clct != null
	 */
	protected final void checkedDeleteCollectable(Clct clct) throws CommonBusinessException {
		if (!isDeleteAllowed(clct)) {
			throw new CommonPermissionException("L\u00f6schen ist nicht erlaubt.");
		}
		this.deleteCollectable(clct);
		this.getResultTableModel().remove(clct);
		broadcastCollectableEvent(clct, MessageType.DELETE_DONE);
		getResultController().getSearchResultStrategy().refreshResult();
	}

	/**
	 * deletes the selected <code>Collectable</code>.
	 * @throws CommonPermissionException if deletion of the selected <code>Collectable</code> is not allowed for the current user.
	 *
	 * TODO: Make this protected again.
	 */
	public final void checkedDeleteSelectedCollectable() throws CommonBusinessException {
		Clct collectable = this.getCompleteSelectedCollectable();
		this.checkedDeleteCollectable(collectable);
	}

	/**
	 * command: delete current collectable in details<br>
	 * Deletes the current collectable in Details mode.
	 *
	 * @deprecated Move to DetailsController and make private again.
	 */
	public void cmdDeleteCurrentCollectableInDetails() {
		assert this.getCollectStateModel().getCollectState().equals(new CollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_VIEW));

		if (this.stopEditingInDetails()) {
			final String sMessage = getSpringLocaleDelegate().getMessage("GenericObjectCollectController.74","Soll der angezeigte Datensatz ({0}) wirklich gel\u00f6scht werden?", getSelectedCollectable().getIdentifierLabel());
			final int iBtn = JOptionPane.showConfirmDialog(this.getFrame(), sMessage, 
					getSpringLocaleDelegate().getMessage("GenericObjectCollectController.26","Datensatz l\u00f6schen"), JOptionPane.YES_NO_OPTION);

			if (iBtn == JOptionPane.OK_OPTION) {
				UIUtils.runCommand(this.getFrame(), new Runnable() {
					@Override
                    public void run() {
						cmdDeleteCurrentCollectableInDetailsImpl();
					}
				});
			}
		}
	}

	/**
	 * command: delete current collectable in details<br>
	 * Deletes the current collectable in Details mode.
	 *
	 * @deprecated Move to DetailsController and make private.
	 */
	public void cmdDeleteCurrentCollectableInDetailsImpl() {
		try {
			// try to find next or previous object:
			final JTable tblResult = getResultTable();
			final int iSelectedRow = tblResult.getSelectedRow();
			if (iSelectedRow < 0) {
				throw new IllegalStateException();
			}

			final int iNewSelectedRow;
			if (iSelectedRow < tblResult.getRowCount() - 1) {
				// the selected row is not the last row: select the next row
				iNewSelectedRow = iSelectedRow;
			}
			else if (iSelectedRow > 0) {
				// the selected row is not the first row: select the previous row
				iNewSelectedRow = iSelectedRow - 1;
			}
			else {
				// the selected row is the single row: don't select a row
				assert tblResult.getRowCount() == 1;
				assert iSelectedRow == 0;
				iNewSelectedRow = -1;
			}

			checkedDeleteSelectedCollectable();
			getSearchStrategy().search(true);

			if (iNewSelectedRow == -1) {
				tblResult.getSelectionModel().clearSelection();
				// switch to new mode:
				getResultController().getSearchResultStrategy().refreshResult();
			}
			else {
				tblResult.getSelectionModel().setSelectionInterval(iNewSelectedRow, iNewSelectedRow);
				// go into view mode again:
				enterViewMode();
			}
		}
		catch (CommonPermissionException ex) {
			final String sErrorMessage = getSpringLocaleDelegate().getMessage(
					"GenericObjectCollectController.69","Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, um diesen Datensatz zu l\u00f6schen.");
			Errors.getInstance().showExceptionDialog(getFrame(), sErrorMessage, ex);
		}
		catch (CommonBusinessException ex) {
			if (!handleSpecialException(ex)) {
				Errors.getInstance().showExceptionDialog(getFrame(), 
						getSpringLocaleDelegate().getMessage(
								"GenericObjectCollectController.31","Der Datensatz konnte nicht gel\u00f6scht werden."), ex);
			}
		}
	}

	/**
	 * @return Is multithreading enabled?
	 * TODO remove this method when the transition to multithreading is done ;)
	 *
	 * @deprecated Move to ResultController hierarchy and make protected again.
	 *   It would be far better, if the class (hierarchy) would known that search
	 *   should be single- or multi-threaded.
	 */
	public boolean isMultiThreadingEnabled() {
		return false;
	}

	/**
	 * shortcut for <code>this.getCollectPanel().getResultPanel().getResultTable()</code>.
	 *
	 * @deprecated Move this to ResultController.
	 */
	public final JTable getResultTable() {
		return this.getResultPanel().getResultTable();
	}

	/**
	 * @deprecated Move this to ResultController.
	 */
	protected final MouseListener getMouseListenerForTableDoubleClick() {
		return getResultController().getTableDblClickML();
	}

	protected final MouseListener getForeignKeyMouseListenerForTableDoubleClick() {
		return this.foreignKeyMouseListenerForTableDoubleClick;
	}

	/**
	 * @return the table model containing the results of the last search.
	 *
	 * @deprecated Move this to ResultController and make protected again.
	 */
	public final SortableCollectableTableModel<Clct> getResultTableModel() {
		return (SortableCollectableTableModel<Clct>) this.getResultTable().getModel();
	}

	/**
	 * This method adds a mouse listener to the table model. Be sure to remove it when the
	 * table model is no longer in use.
	 * It also sorts the returned TableModel by a given column if declared in preferences.
	 *
	 * @return a new collectable table model.
	 */
	protected SortableCollectableTableModel<Clct> newResultTableModel() {
		final SortableCollectableTableModel<Clct> result = new SortableCollectableTableModelImpl<Clct>(getEntityName());
		result.setColumns(getFields().getSelectedFields());

		// setup sorted fields and sorting order from preferences
		List<SortKey> sortKeys = readColumnOrderFromPreferences(result);
		if (result.getColumnCount() > 0) {
			try {
				result.setSortKeys(sortKeys, false);
			} catch (IllegalArgumentException e) {
				// sortKeys contains invalid column index, ignore
			}
		}

		TableUtils.addMouseListenerForSortingToTableHeader(this.getResultTable(), result);
		return result;
	}

	/**
	 * fills the result panel with the results from the current search.
	 * @param lstclct List<Collectable>: the results from the current search.
	 */
	public final void fillResultPanel(List<Clct> lstclct) {
		this.fillResultPanel(lstclct, lstclct.size(), true);
	}

	/**
	 * fills the result panel with the results from the current search.
	 * @param lstclct List<Collectable>: the results from the current search.
	 * @param iTotalNumberOfRecords The total number of records found. If the result was truncated, this is higher
	 * than lstclct.size().
	 * @precondition iTotalNumberOfRecords >= lstclct.size()
	 *
	 * TODO: Make this protected again.
	 */
	public final void fillResultPanel(List<Clct> lstclct, int iTotalNumberOfRecords, boolean bSortInitially) {
		if (iTotalNumberOfRecords < lstclct.size()) {
			throw new IllegalArgumentException("iTotalNumberOfRecords");
		}
		final boolean bResultTruncated = (iTotalNumberOfRecords > lstclct.size());

		final SortableCollectableTableModel<Clct> tblmodel = getResultTableModel();
		tblmodel.setCollectables(lstclct);
		if (bSortInitially) {
			tblmodel.sort();
		}

		final JTable tblResult = getResultTable();

		//setColumnWidths(tblResult);

		// set collect navigation model (for navigation buttons):
		if (navigationmodel != null)
			navigationmodel.removeChangeListener(navigationChangeListener);
		navigationmodel = new CollectNavigationModel(tblResult.getModel(), tblResult.getSelectionModel());
		navigationmodel.addChangeListener(navigationChangeListener);

		ctlResult.setStatusBar(tblResult, bResultTruncated, iTotalNumberOfRecords);
	}

	/**
	 * TODO: Make this protected again.
	 */
	public void writeColumnOrderToPreferences(){
		TableModel resultTableModel = this.getResultTable().getModel();
		// NUCLEUSINT-1045
		if (resultTableModel instanceof SortableTableModel) {
			WorkspaceUtils.setSortKeys(getEntityPreferences(), ((SortableTableModel) resultTableModel).getSortKeys(), new WorkspaceUtils.IColumnNameResolver() {
				@Override
				public String getColumnName(int iColumn) {
					return getResultTableModel().getCollectableEntityField(iColumn).getName();
				}
			});
		}
	}

	protected CollectNavigationModel getCollectNavigationModel() {
		return this.navigationmodel;
	}

	private void cmdFirst() {
		if (this.askAndSaveIfNecessary()) {
			CollectController.this.getCollectNavigationModel().selectFirstElement();
			this.cmdEnterViewMode();
		}
	}

	private void cmdLast() {
		if (this.askAndSaveIfNecessary()) {
			CollectController.this.getCollectNavigationModel().selectLastElement();
			this.cmdEnterViewMode();
		}
	}

	private void cmdPrevious() {
		if (this.askAndSaveIfNecessary()) {
			CollectController.this.getCollectNavigationModel().selectPreviousElement();
			this.cmdEnterViewMode();
		}
	}

	private void cmdNext() {
		if (this.askAndSaveIfNecessary()) {
			CollectController.this.getCollectNavigationModel().selectNextElement();
			this.cmdEnterViewMode();
		}
	}

	/**
	 * asks the user to save the current record if necessary, so that it can be abandoned afterwards.
	 * @return can the action be performed?
	 */
	@Override
	public boolean askAndSaveIfNecessary() {
		boolean result = true;

		if (this.changesArePending()) {
			try {
				MainFrame.setSelectedTab(this.getFrame());
			} catch (Exception e) {
				// TODO TABS: Ein Overlay Tab kann der MainFrame noch nicht finden... Quickfix try-catch
				LOG.error(e.getMessage(), e);
			}
			final String sMsg = getSpringLocaleDelegate().getMessage(
					"CollectController.14","Der Datensatz wurde ge\u00e4ndert.") + "\n" + getSpringLocaleDelegate().getMessage(
							"CollectController.32","Wenn Sie jetzt nicht speichern, werden diese \u00c4nderungen verloren gehen.") + "\n" + 
							getSpringLocaleDelegate().getMessage("CollectController.20","Jetzt speichern?");

			final int iBtn = JOptionPane.showConfirmDialog(this.getFrame(), sMsg, getSpringLocaleDelegate().getMessage(
					"CollectController.10","Datensatz ge\u00e4ndert"),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			result = (iBtn != JOptionPane.CANCEL_OPTION);

			if (iBtn == JOptionPane.YES_OPTION) {
				try {
					// cmdSave cannot be used here, because it does not throw any non-fatal exceptions, which can be used to prevent closing of the window.
					// To solve the problem otherwise, handleSaveExeption is called here, which could provide additional behaviour like focus faulty fields...
					this.save();
				}
				catch (CommonPermissionException ex) {
					result = false;
					final String sMessage = getSpringLocaleDelegate().getMessage(
							"CollectController.24","Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, um dieses Objekt zu speichern.");
					Errors.getInstance().showExceptionDialog(this.getFrame(), sMessage, ex);
				}
				catch (CommonBusinessException ex) {
					result = false;
					final String sMessage1 = getSpringLocaleDelegate().getMessage(
							"CollectController.13","Der Datensatz konnte nicht gespeichert werden");
					try {
						handleSaveException(ex, sMessage1);
					}
					catch (CommonFinderException ex2) {
						final String sErrorMsg = getSpringLocaleDelegate().getMessage(
								"CollectController.1",", da er zwischenzeitlich von einem anderen Benutzer gel\u00f6scht wurde.");
						Errors.getInstance().showExceptionDialog(this.getFrame(), sErrorMsg, ex2);
					}
					catch (CommonBusinessException ex2) {
						Errors.getInstance().showExceptionDialog(this.getFrame(), sMessage1 + ".", ex2);
					}
					//final String sMessage = "Der Datensatz konnte nicht gespeichert werden.";
					//Errors.getInstance().showExceptionDialog(this.getFrame(), sMessage, ex);
				}
				catch (CommonFatalException ex) {
					result = false;
					final String sMessage = getSpringLocaleDelegate().getMessage(
							"CollectController.11","Der Datensatz konnte nicht gespeichert werden.");
					Errors.getInstance().showExceptionDialog(this.getFrame(), sMessage, ex);
				}
			}
		}

		return result;
	}	// askAndSaveIfNecessary

	/**
	 * @return Are user changes pending (need to be saved)?
	 * NUCLEUSINT-1159
	 */
	public boolean changesArePending() {
		return this.statemodel.changesArePending();
	}

	/**
	 * @return Is the "Save" action allowed? Default: true. May be overridden by subclasses.
	 * TODO consider giving the current collectable (id) here
	 */
	protected boolean isSaveAllowed() {
		return true;
	}

	/**
	 * @return Is the "New" action allowed? Default: <code>isSaveAllowed()</code>.
	 *         May be overridden by subclasses.
	 */
	protected boolean isNewAllowed() {
		return isSaveAllowed();
	}

	/**
	 * @return Is the "Clone" action allowed? Default: <code>isNewAllowed()</code>.
	 *         May be overridden by subclasses.
	 */
	protected boolean isCloneAllowed() {
		return isNewAllowed();
	}

	/**
	 * @return Is the "Delete" action allowed? Default: true. May be overridden by subclasses.
	 */
	protected boolean isDeleteSelectedCollectableAllowed() {
		return isDeleteAllowed(getSelectedCollectable());
	}

	/**
	 * @return Is the "Delete" action for the given Collectable allowed? Default: true. May be overridden by subclasses.
	 * @precondition clct != null
	 */
	protected boolean isDeleteAllowed(Clct clct) {
		return true;
	}

	/**
	 * @return Is the "Read" action for the given Collectable allowed? Default: true. May be overridden by subclasses.
	 * @precondition clct != null
	 */
	protected boolean isReadAllowed(Clct clct) {
		return true;
	}

	/**
	 * @return Is the "Read" action for the given set of Collectables allowed? Default: true. May be overridden by subclasses.
	 * @precondition clct != null
	 */
	protected boolean isReadAllowed(List <Clct> lsClct) {
		return true;
	}

	/**
	 * @return Are the "Navigation" actions allowed? Default: true. May be overridden by subclasses.
	 */
	protected boolean isNavigationAllowed() {
		return true;
	}

	/**
	 * @return Is viewing/editing multiple Collectables allowed?
	 * TODO Default should be false here.
	 * TODO Document what needs to be done for implementing MultiEdit in subclasses
	 */
	protected boolean isMultiEditAllowed() {
		return true;
	}

	/**
	 * @return Is the "Refresh" action allowed for the selected Collectable? Default: true. May be overridden by subclasses.
	 */
	protected boolean isRefreshSelectedCollectableAllowed() {
		return true;
	}

	/**
	 * @return the CollectStateModel used internally.
	 *
	 * TODO: Can this be protected? Can this move to SearchController?
	 */
	public final CollectStateModel<Clct> getCollectStateModel() {
		return this.statemodel;
	}

	/**
	 * @return the current collect state (consisting of an outer and an inner state).
	 */
	public CollectState getCollectState() {
		return this.getCollectStateModel().getCollectState();
	}

	/**
	 * This method is called by <code>cmdClearSearchCondition</code>, that is when the user clicks
	 * the "Clear Search Fields" button. The default implementation calls clearSearchFields()
	 * and adjusts the search editor, if visible.
	 * TODO move to SearchPanel
	 */
	protected void clearSearchCondition() {
		this.clearSearchFields();

		if (this.getSearchPanel().isSearchEditorVisible()) {
			this.getSearchPanel().getSearchEditorPanel().setSearchCondition(null);
		}
	}

	/**
	 * clears all search fields, suppressing calls to searchChanged() for each field and calling searchChanged(null)
	 * once at the end of the operation.
	 * TODO move to SearchPanel
	 */
	protected void clearSearchFields() {
		final boolean bWasSearchChangedIgnored = this.isSearchChangedIgnored();
		this.setSearchChangedIgnored(true);
		try {
			this._clearSearchFields();
		}
		finally {
			this.setSearchChangedIgnored(bWasSearchChangedIgnored);
		}
		// trigger an event for the completed search condition:
		this.searchChanged(null);
	}

	/**
	 * clears all search fields.
	 * TODO move to SearchPanel
	 */
	protected void _clearSearchFields() {
		for (CollectableComponent clctcomp : getSearchPanel().getEditView().getCollectableComponents()) {
			clctcomp.clear();
		}
	}

	/**
	 * Command: clear search condition
	 *
	 * @deprecated Move to SearchController and make private again.
	 */
	public void cmdClearSearchCondition() {
		try {
			this.stopEditingInSearch();

			this.removeImportedSearchConditionWithStatus();
			this.clearSearchCondition();

			ctlSearch.getNewWithSearchValuesAction().setEnabled(false);

			// set to unsynched state even if no value was changed:
			this.setCollectState(CollectState.OUTERSTATE_SEARCH, CollectState.SEARCHMODE_UNSYNCHED);
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(this.getFrame(), null, ex);
		}
	}

	public final MainFrameTab getFrame() {
		return ifrm;
	}

	/**
	 * TODO: Tidy this up (together with {@link #setInternalFrame(MainFrameTab, boolean)}.
	 */
	protected final void setFrame(MainFrameTab ifrm) {
		this.ifrm = ifrm;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	/**
	 * @return the CollectPanel, containing the Search-, Result- and DetailsPanels.
	 */
	public CollectPanel<Clct> getCollectPanel() {
		final CollectPanel<Clct> result = pnlCollect.get();
		assert result != null || isClosed();
		return result;
	}

	/**
	 * TODO: add precondition this.isSearchPanelAvailable()
	 *
	 * @deprecated move to ResultController hierarchy.
	 */
	public SearchPanel getSearchPanel() {
		return this.getCollectPanel().getSearchPanel();
	}

	/**
	 * @deprecated Move this to ResultController.
	 */
	public ResultPanel<Clct> getResultPanel() {
		return this.getCollectPanel().getResultPanel();
	}

	/**
	 * @deprecated Move this to DetailsController.
	 */
	public DetailsPanel getDetailsPanel() {
		return this.getCollectPanel().getDetailsPanel();
	}

	/**
	 * @return the (internal) entity name.
	 */
	public String getEntityName() {
		return this.getCollectableEntity().getName();
	}

	/**
	 * @return the (external) entity name, as presented to the user. It is shown in the title bar.
	 */
	protected abstract String getEntityLabel();

	/**
	 * @param iTab
	 * @param iMode
	 * @return the title for the given tab and mode, to display in the title bar
	 */
	protected String getTitle(int iTab, int iMode) {
		// TODO move these constants to CollectState
		final String[] asTabs = {"Suche", "Ergebnis", "Details"};
		final String[] asDetailsMode = {
				"Undefiniert", "Details", "Bearbeiten", "Neueingabe", "Neueingabe (Ge\u00e4ndert)", "Sammelbearbeitung",
				"Sammelbearbeitung (Ge\u00e4ndert)"
		};

		String sPrefix;
		String sSuffix = "";
		final String sMode;

		switch (iTab) {
			case CollectState.OUTERSTATE_DETAILS:
				sPrefix = this.getEntityLabel();
				sMode = asDetailsMode[iMode];
				if (CollectState.isDetailsModeViewOrEdit(iMode)) {
					final String sIdentifier = this.getSelectedCollectable().getIdentifierLabel();
					if (sIdentifier == null) {
						throw new CommonFatalException("Identifier == null");
					}
					sPrefix += " \"" + sIdentifier + "\"";
				}
				else if (CollectState.isDetailsModeMultiViewOrEdit(iMode)) {
					sSuffix = " von " + this.getSelectedCollectables().size() + " Objekten";
				}
				break;
			default:
				sPrefix = this.getEntityLabel();
				sMode = asTabs[iTab];
		}

		return sPrefix + " - " + sMode + sSuffix;
	}

	/**
	 * sets the title of this controller's frame depending on the current CollectState.
	 */
	protected void setTitle() {
		this.setTitle(this.getCollectState().getOuterState(), this.getCollectState().getInnerState());
	}

	/**
	 * sets the title of this controller's frame depending on the given CollectState.
	 * @param iTab
	 * @param iMode
	 */
	final void setTitle(int iTab, int iMode) {
		this.setTitle(this.getTitle(iTab, iMode));
	}

	/**
	 * sets the title of this controller's frame.
	 * @param sTitle
	 */
	protected void setTitle(String sTitle) {
		this.getFrame().setTitle(sTitle);
	}

	/**
	 * @return the message to display in the status bar when changes im multi edit mode have occured.
	 * @precondition this.getCollectStateModel().getCollectState().isDetailsModeMultiViewOrEdit()
	 *
	 * TODO: inline - after refactoring LOCC.getMultiEditChangeString()
	 *
	 * TODO: Make this protected again.
	 */
	public String getMultiEditChangeString() {
		if (!this.getCollectStateModel().getCollectState().isDetailsModeMultiViewOrEdit()) {
			throw new IllegalStateException();
		}

		return this.getDetailsPanel().getMultiEditChangeMessage();
	}

	/**
	 * @return a new <code>Collectable</code>. All fields are set to null values.
	 *         To set the fields to their default values, <code>CollectableUtils.setDefaultValues</code> must be called
	 *         explicitly afterwards. Alternatively, <code>newCollectableWithDefaultValues()</code> may be used.
	 * @postcondition result != null
	 * @postcondition isCollectableComplete(result)
	 */
	public abstract Clct newCollectable();

	/**
	 * @param currclct
	 * @return the current collectable filled with the values which are set in the search panel
	 * @throws CommonBusinessException
	 *
	 * @deprecated Move to SearchController.
	 */
	protected Clct newCollectableWithSearchValues(Clct currclct) throws CommonBusinessException {
		Collection<SearchComponentModel> collscm = ctlSearch.getCollectableComponentModels();

		for (SearchComponentModel scm : collscm) {
			List<CollectableComponent> lsclctcomp = (List<CollectableComponent>) getDetailsPanel().getEditView().getCollectableComponentsFor(scm.getFieldName());

			if (lsclctcomp.isEmpty()) {
				continue;
			}

			// get model of first found component
			DetailsComponentModel dcm = lsclctcomp.get(0).getDetailsModel();

			boolean bSetAllowed = true;
			for (CollectableComponent clctcomp : lsclctcomp) {
				if (!isSetAllowedForClctComponent(clctcomp)) {
					bSetAllowed = false;
				}
			}

			if (bSetAllowed) {
				dcm.setField(scm.getField());
				currclct.setField(dcm.getFieldName(), scm.getField());
				if (scm.getField().getValue() != null) {
					detailsChanged(dcm);
				}
			}
		}

		return currclct;
	}

	/**
	 * fill subforms of the current collectable with the values which are set
	 * in the coresponding search panel subforms
	 * @throws CommonBusinessException
	 */
	protected void newCollectableWithDependantSearchValues() throws CommonBusinessException {
	}

	/**
	 * @param clctcomp - collectablecomponent of the details panel
	 * this method should only be used to check whether it is allowed to adopt a value from the
	 * search panel to the details panel
	 */
	protected boolean isSetAllowedForClctComponent(CollectableComponent clctcomp) {
		boolean bEnabled = true;
		JComponent jcomp = clctcomp.getJComponent();

		if (jcomp instanceof LabeledTextField) {
			if (!((LabeledTextField)jcomp).getTextField().isEditable()) {
				bEnabled = false;
			}
		}
		else if (jcomp instanceof LabeledComboBox) {
			if (!((LabeledComboBox)jcomp).getJComboBox().isEnabled()) {
				bEnabled = false;
			}
		}
		else if (jcomp instanceof LabeledListOfValues) {
			if (!((ListOfValues)((LabeledListOfValues)jcomp).getControlComponent()).getJTextField().isEnabled()) {
				bEnabled = false;
			}
		}
		else if (jcomp instanceof LabeledTextArea) {
			if (!((LabeledTextArea)jcomp).getJTextArea().isEditable()) {
				bEnabled = false;
			}
		}
		else if (jcomp instanceof LabeledDateChooser) {
			if (!((LabeledDateChooser)jcomp).getJTextComponent().isEditable()) {
				bEnabled = false;
			}
		}
		else if (clctcomp instanceof CollectableCheckBox) {
			if (!((CollectableCheckBox)clctcomp).getJCheckBox().isEnabled()) {
				bEnabled = false;
			}
		}
		// fallback - maybe the list above has to be adjusted
		if (!jcomp.isEnabled()) {
			bEnabled = false;
		}

		return bEnabled;
	}

	/**
	 * @return a new <code>Collectable</code>, filled with default values (according to the default values defined in its entity).
	 * If there are no reasonable default values for a specific entity (esp. masterdata entities), this is the place to do it.
	 * @postcondition result != null
	 * @postcondition isCollectableComplete(result)
	 */
	protected Clct newCollectableWithDefaultValues() {
		final Clct result = CollectController.this.newCollectable();
		CollectableUtils.setDefaultValues(result, CollectController.this.getCollectableEntity());
		assert result != null;
		assert ss.isCollectableComplete(result);
		return result;
	}

	/**
	 * makes sure the given list of selected fields is non-empty. If the list is empty, this method adds one field to it.
	 * This is to avoid a seemingly empty search result, which might be irritating to the user.
	 * @param clcte
	 * @param lstclctefSelected
	 * @precondition clcte != null
	 * @precondition lstclctefSelected != null
	 * @postcondition !lstclctefSelected.isEmpty()
	 *
	 * TODO: Make this protected again.
	 */
	public void makeSureSelectedFieldsAreNonEmpty(CollectableEntity clcte, List<CollectableEntityField> lstclctefSelected) {
		if (lstclctefSelected.isEmpty()) {

			List<String> fieldNames = new ArrayList<String>();
			try {
				List<EntityFieldMetaDataVO> fields = CollectionUtils.sorted(
						MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(clcte.getName()).values(),
						new Comparator<EntityFieldMetaDataVO>() {
							@Override
							public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
								Integer order1 = (o1.getOrder()==null)?0:o1.getOrder();
								Integer order2 = (o2.getOrder()==null)?0:o2.getOrder();
								return order1.compareTo(order2);
							}
						});

				for (EntityFieldMetaDataVO efMeta : fields) {
					if (StringUtils.isNullOrEmpty(efMeta.getCalcFunction())) {
						fieldNames.add(efMeta.getField());
					}
				}
			} catch (Exception ex) {
				LOG.warn("No entity fields for entity " + clcte.getName(), ex);
			}
			if (fieldNames.isEmpty()) {
				fieldNames.addAll(clcte.getFieldNames());
			}

			CollectableEntityField sysStateIcon = null;
			CollectableEntityField sysStateNumber = null;
			CollectableEntityField sysStateName = null;

			Set<String> clcteFieldNames = clcte.getFieldNames();
			for (String field : fieldNames) {
				if (!clcteFieldNames.contains(field)) {
					LOG.warn("Field " + field + " in collectable entity " + clcte.getName() + " does not exists");
					continue;
				}

				CollectableEntityField clctef = clcte.getEntityField(field);
				boolean select = true;
				if (NuclosEOField.getByField(field) != null) {
					select = false;
					switch (NuclosEOField.getByField(field)) {
					case STATEICON :
						sysStateIcon = clctef;
						break;
					case STATENUMBER :
						sysStateNumber = clctef;
						break;
					case STATE :
						sysStateName = clctef;
						break;
					}
				}
				if (select) {
					lstclctefSelected.add(clctef);
				}
			}

			if (sysStateIcon != null)
				lstclctefSelected.add(sysStateIcon);
			if (sysStateNumber != null)
				lstclctefSelected.add(sysStateNumber);
			if (sysStateName != null)
				lstclctefSelected.add(sysStateName);

			if (lstclctefSelected.isEmpty()) {
				// show any (the first, random) field:
				if (clcte.getFieldNames().isEmpty()) {
					throw new CommonFatalException("Die Entit\u00e4t \"" + clcte.getName() + "\" enth\u00e4lt keine Felder.");
				}
				else {
					final String sRandomFieldName = clcte.getFieldNames().iterator().next();
					lstclctefSelected.add(getResultController().getCollectableEntityFieldForResult(clcte, sRandomFieldName));
				}
			}
		}
	}

	@Override
	public JComponent getParent() {
		return parent;
	}

	/**
	 * used by <code>DefaultCollectStateListener</code>:
	 * On entering <code>CollectState.DETAILSMODE_VIEW</code> the collectable would be loaded completely.
	 * You can choose with or without denpendants.
	 * If your CollectController can lazy loads dependants (performance improvement) your must return true.
	 *
	 * See also:
	 * <code>findCollectableById(String sEntity, Object oId)</code>
	 * <code>findCollectableByIdWithoutDependants(String sEntity, Object oId)</code>
	 * @return true to disable dependants loading on <code>CollectState.DETAILSMODE_VIEW</code>
	 */
	protected abstract boolean isDetailsModeViewLoadingWithoutDependants();

	private class DefaultCollectStateListener extends CollectStateAdapter {

		@Override
		public void searchModeEntered(CollectStateEvent ev) throws CommonBusinessException {
			CollectController.this.addChangeListenersForSearch();
			CollectController.this.cmdDisplayCurrentSearchConditionInSearchPanelStatusBar();
		}

		@Override
		public void searchModeLeft(CollectStateEvent ev) throws CommonBusinessException {
			CollectController.this.removeChangeListenersForSearch();
		}

		@Override
		public void resultModeEntered(final CollectStateEvent ev) throws CommonBusinessException {
			// This should be invoked from the dispatch thread because it works on the model
			if (!EventQueue.isDispatchThread()) {
				LOG.warn("resultModeEntered invoked outside of the AWT dispatch thread");
			}
			// However, at the moment, it is still sometimes triggered from other threads. Especially it
			// is triggered concurrently by MultiActions.
			// The NUCLEUSINT-622 workaround used invokeLater(). But delaying the Runnable leads to other
			// problems (and in rare cases exceptions, cf. NUCLOSINT-850). The reason is simple:
			// The CollectStateEvent parameter encodes view information (e.g. how many rows are selected)
			// but there is no guarantee that this still holds when Runnable is "invoked later".
			// So, new workaround is to perform the action instantly on the EDT (invokeAndWait).
			UIUtils.invokeOnDispatchThread(new Runnable() {
				@Override
				public void run() {
					int iResultMode = ev.getNewCollectState().getInnerState();
					boolean bOneRowSelected = (iResultMode == CollectState.RESULTMODE_SINGLESELECTION);
					boolean bMoreThanOneRowsSelected = (iResultMode == CollectState.RESULTMODE_MULTISELECTION);
					boolean bRowsSelected = bOneRowSelected || bMoreThanOneRowsSelected;

					getResultController().getEditSelectedCollectablesAction().setEnabled(bOneRowSelected || (bMoreThanOneRowsSelected && isMultiEditAllowed()));
					getCloneAction().setEnabled(bOneRowSelected && isCloneAllowed());
					getResultController().getDeleteSelectedCollectablesAction().setEnabled(bRowsSelected && isDeleteSelectedCollectableAllowed());

					// If the selection changes and we are in the result tab, the details panel is no longer in sync:
					// TODO alternative: sync details panel when details tab is pressed (let the customer decide).
					getCollectPanel().setTabbedPaneEnabledAt(CollectState.OUTERSTATE_DETAILS, false);

					// Give the result table the focus so the user can start scrolling with the arrow keys:
					getResultTable().requestFocusInWindow();
				}
			});
		}

		@Override
		public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
			final int iDetailsMode = ev.getNewCollectState().getInnerState();

			switch (iDetailsMode) {
				case CollectState.DETAILSMODE_VIEW:
					CollectController.this.setCollectableComponentModelsInDetailsPanelMultiEditable(false);
					Clct clct = CollectController.this.getCompleteSelectedCollectable(CollectController.this.isDetailsModeViewLoadingWithoutDependants());
					CollectController.this.safeFillDetailsPanel(clct);
					if (clct != null && clct.getId() != null && clct.getId() instanceof Integer) {
						String label = getLabelForStartTab();
						Main.getInstance().getMainFrame().addHistory(getEntityName(), (Integer) clct.getId(), label);
					}
					break;

				case CollectState.DETAILSMODE_EDIT:
					break;

				case CollectState.DETAILSMODE_NEW:
					CollectController.this.setCollectableComponentModelsInDetailsPanelMultiEditable(false);
					Clct clctNew = CollectController.this.newCollectableWithDefaultValues();

					CollectController.this.safeFillDetailsPanel(clctNew);
					break;

				case CollectState.DETAILSMODE_NEW_SEARCHVALUE:
					CollectController.this.setCollectableComponentModelsInDetailsPanelMultiEditable(false);
					Clct clctNewSearchValues = CollectController.this.newCollectableWithDefaultValues();
					CollectController.this.safeFillDetailsPanel(clctNewSearchValues);

					//if(iOldModeOuterState == CollectState.OUTERSTATE_SEARCH && transferSearchPanelData()) {
						// transfer field data
						clctNewSearchValues = CollectController.this.newCollectableWithSearchValues(clctNewSearchValues);
						// transfer subform data
						CollectController.this.newCollectableWithDependantSearchValues();
					//}
					break;

				case CollectState.DETAILSMODE_NEW_CHANGED:
					break;

				case CollectState.DETAILSMODE_MULTIVIEW:
					CollectController.this.setCollectableComponentModelsInDetailsPanelMultiEditable(true);
					CollectController.this.safeFillMultiEditDetailsPanel(CollectController.this.getCompleteSelectedCollectables());
					break;

				case CollectState.DETAILSMODE_MULTIEDIT:
					break;
			}	// switch

			UIUtils.invokeOnDispatchThread(new Runnable() {
				@Override
				public void run() {
					setToolbarButtonsForDetailsMode(iDetailsMode);
					CollectController.this.getDetailsPanel().showToolbar(true);
					performDetailsModeEntered();
				}
			});
		}

		/**
		 * @deprecated Move to DetailsController and make protected again.
		 */
		public void performDetailsModeEntered() {
			DetailsPanel dp = getDetailsPanel();
			dp.setStatusBarText(" ");
			ctlDetails.updateStatusBarIfNecessary();
			addChangeListenersForDetails();

			if(getCollectState().isDetailsModeMultiViewOrEdit()) {
				bIsLastTabDetailsModeMultiViewOrEdit = true;
			}
			else {
				bIsLastTabDetailsModeMultiViewOrEdit = false;
			}

			ctlDetails.displayCurrentRecordNumberInDetailsPanelStatusBar();

		}

		public void setToolbarButtonsForDetailsMode(final int iDetailsMode) {
			final CollectPanel<Clct> pnlCollect = CollectController.this.getCollectPanel();
			final DetailsPanel pnlDetails = pnlCollect.getDetailsPanel();
			// enable/disable toolbar buttons:

			// "refresh/cancel" button:
			final boolean bDisguiseRefreshButton = CollectState.isDetailsModeNew(iDetailsMode) || CollectState.isDetailsModeMultiViewOrEdit(iDetailsMode);
			pnlDetails.disguiseRefreshButton(bDisguiseRefreshButton);

			// "save" action:
			CollectController.this.getSaveAction().setEnabled(CollectController.this.changesArePending() && CollectController.this.isSaveAllowed());

			// "refresh current collectable" action:
			final boolean bRefreshEnabled = ((iDetailsMode == CollectState.DETAILSMODE_VIEW) || CollectState.isDetailsModeChangesPending(iDetailsMode)) && CollectController.this.isRefreshSelectedCollectableAllowed();
			CollectController.this.getRefreshCurrentCollectableAction().setEnabled(bRefreshEnabled);

			final boolean bViewingExistingRecord = (iDetailsMode == CollectState.DETAILSMODE_VIEW);

			// "delete" action:
			setDeleteActionEnabled(bViewingExistingRecord && CollectController.this.isDeleteSelectedCollectableAllowed());

			// "clone" action:
			CollectController.this.getCloneAction().setEnabled(bViewingExistingRecord && CollectController.this.isCloneAllowed());

			// navigation actions:
			final boolean bNavigationEnabled = bViewingExistingRecord && CollectController.this.isNavigationAllowed();

			final CollectNavigationModel collectNavigationModel = CollectController.this.getCollectNavigationModel();
			assert !bNavigationEnabled || collectNavigationModel != null;

			CollectController.this.getFirstAction().setEnabled(bNavigationEnabled && !collectNavigationModel.isFirstElementSelected());
			CollectController.this.getLastAction().setEnabled(bNavigationEnabled && !collectNavigationModel.isLastElementSelected());
			CollectController.this.getPreviousAction().setEnabled(bNavigationEnabled && !collectNavigationModel.isFirstElementSelected());
			CollectController.this.getNextAction().setEnabled(bNavigationEnabled && !collectNavigationModel.isLastElementSelected());

			CollectController.this.getDetailsPanel().setStatusBarText(" ");
			CollectController.this.ctlDetails.updateStatusBarIfNecessary();

			CollectController.this.addChangeListenersForDetails();

			if(CollectController.this.getCollectState().isDetailsModeMultiViewOrEdit()) {
				CollectController.this.bIsLastTabDetailsModeMultiViewOrEdit = true;
			}
			else {
				CollectController.this.bIsLastTabDetailsModeMultiViewOrEdit = false;
			}

			CollectController.this.ctlDetails.displayCurrentRecordNumberInDetailsPanelStatusBar();

//			SwingUtilities.invokeLater(new Runnable() {
//
//				public void run() {
//					// Workaround for ELISA-6851
//					if (CollectController.this.getDetailsPanel() != null) {
//						CollectController.this.getDetailsPanel().requestFocusInWindow();
//					}
//				}
//			});

		}

		@Override
		public void detailsModeLeft(CollectStateEvent ev) {
			final int iOuterState = ev.getNewCollectState().getOuterState();

			switch (iOuterState) {
				case CollectState.OUTERSTATE_RESULT :
				case CollectState.OUTERSTATE_SEARCH :
				case CollectState.OUTERSTATE_UNDEFINED :
					broadcastCollectableEvent(getSelectedCollectable(), MessageType.CLCT_LEFT);
					break;
			}

			// ensure we don't have more than one listener for each component:
			CollectController.this.removeChangeListenersForDetails();

			// TODO all actions that are specific to the Details tab must be disabled here and initially
			UIUtils.invokeOnDispatchThread(new Runnable() {
				@Override
				public void run() {
					CollectController.this.getSaveAction().setEnabled(false);
					CollectController.this.getDetailsPanel().showToolbar(false);
				}
			});
		}

	}	// inner class DefaultCollectStateListener

	protected String getLabelForStartTab() {
		String result = null;

		switch (CollectController.this.getCollectState().getOuterState()) {
			case CollectState.OUTERSTATE_DETAILS:
				if (this.getCollectState().isDetailsModeViewOrEdit()) {
					result = this.getSelectedCollectable().getIdentifierLabel();
				}
				break;
			case CollectState.OUTERSTATE_RESULT:
				if (this.getSelectedCollectables().size() == 1) {
					result = this.getSelectedCollectable().getIdentifierLabel();
				}
				break;
		}

		if (result == null) {
			return this.getEntityLabel() + " (" + this.getSelectedCollectableId() + ")";
		} else {
			return result.trim();
		}
	}

	public void enableToolbarButtonsForDetailsMode(final int iDetailsMode) {
		final CollectPanel<Clct> pnlCollect = this.getCollectPanel();
		final DetailsPanel pnlDetails = pnlCollect.getDetailsPanel();
		// enable/disable toolbar buttons:

		// "refresh/cancel" button:
		final boolean bDisguiseRefreshButton = CollectState.isDetailsModeNew(iDetailsMode) || CollectState.isDetailsModeMultiViewOrEdit(iDetailsMode);
		pnlDetails.disguiseRefreshButton(bDisguiseRefreshButton);

		// "save" action:
		this.getSaveAction().setEnabled(this.changesArePending() && this.isSaveAllowed());

		// "refresh current collectable" action:
		final boolean bRefreshEnabled = ((iDetailsMode == CollectState.DETAILSMODE_VIEW) || CollectState.isDetailsModeChangesPending(iDetailsMode)) && this.isRefreshSelectedCollectableAllowed();
		this.getRefreshCurrentCollectableAction().setEnabled(bRefreshEnabled);

		final boolean bViewingExistingRecord = (iDetailsMode == CollectState.DETAILSMODE_VIEW);

		// "delete" action:
		setDeleteActionEnabled(bViewingExistingRecord && this.isDeleteSelectedCollectableAllowed());

		// "clone" action:
		CollectController.this.getCloneAction().setEnabled(bViewingExistingRecord && CollectController.this.isCloneAllowed());

		// navigation actions:
		final boolean bNavigationEnabled = bViewingExistingRecord && this.isNavigationAllowed();

		final CollectNavigationModel collectNavigationModel = this.getCollectNavigationModel();
		assert !bNavigationEnabled || collectNavigationModel != null;

		this.getFirstAction().setEnabled(bNavigationEnabled && !collectNavigationModel.isFirstElementSelected());
		this.getLastAction().setEnabled(bNavigationEnabled && !collectNavigationModel.isLastElementSelected());
		this.getPreviousAction().setEnabled(bNavigationEnabled && !collectNavigationModel.isFirstElementSelected());
		this.getNextAction().setEnabled(bNavigationEnabled && !collectNavigationModel.isLastElementSelected());
	}

	public void disableToolbarButtons() {
		this.getSaveAction().setEnabled(false);
		//this.getFirstAction().setEnabled(false);
		//this.getLastAction().setEnabled(false);
		//this.getPreviousAction().setEnabled(false);
		//this.getNextAction().setEnabled(false);
		this.getCloneAction().setEnabled(false);
		this.ctlDetails.getDeleteCurrentCollectableAction().setEnabled(false);
		this.getRefreshCurrentCollectableAction().setEnabled(false);
	}

	protected void setDeleteActionEnabled(boolean enabled) {
		this.ctlDetails.getDeleteCurrentCollectableAction().setEnabled(enabled);
	}

	private void acceptLookedUpCollectable(final ICollectableListOfValues clctlovSource) {
		try {
			Collectable first = null;
			List<Collectable> additionalCollectables = new ArrayList<Collectable>();
			for (Collectable clct : getSelectedCollectables()) {
				if (clct != null) {
					if (first == null) {
						first = clct;
					} else {
						additionalCollectables.add(clct);
					}
				}
			}
			if (first != null) {
				clctlovSource.acceptLookedUpCollectable(first, additionalCollectables);
			}
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(getFrame(), ex);
		}
	}

	@Override
	public void visitDragEnter(DropTargetDragEvent dtde) {}

	@Override
	public void visitDragExit(DropTargetEvent dte) {}

	@Override
	public void visitDragOver(DropTargetDragEvent dtde) {
		Point here = dtde.getLocation();
		openDetailsPanel(here);
	}

	@Override
	public void visitDrop(DropTargetDropEvent dtde) {}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}

	private void openDetailsPanel(Point here) {
		int hereRow = getResultTable().rowAtPoint(here);

		CollectableTableModel<Collectable> model = (CollectableTableModel<Collectable>) getResultTable().getModel();

		final Collectable clctSelected = model.getCollectable(hereRow);
		try {
			if (clctSelected != null) {
				if (Modules.getInstance().isModuleEntity(getEntityName())) {
					final CollectableGenericObject clctloSelected = (CollectableGenericObject) clctSelected;
					// we must reload the partially loaded object:
					final int iModuleId = clctloSelected .getGenericObjectCVO().getModuleId();
					GenericObjectClientUtils.showDetails(MainFrame.getPredefinedEntityOpenLocation(MetaDataClientProvider.getInstance().getEntity(new Long(iModuleId)).getEntity()), iModuleId, clctloSelected.getId());
				}
				else {
					final CollectableMasterDataWithDependants clctmdSelected = (CollectableMasterDataWithDependants) clctSelected;
					getMainController().showDetails(clctmdSelected.getCollectableEntity().getName(), clctmdSelected.getId());
				}
			}
		}
		catch(Exception e) {
			throw new NuclosFatalException(e);
		}
	}

	protected Map<String, Serializable> getContext() {
		return context;
	}

	protected void invoke(CommonRunnable runnable) throws CommonBusinessException {
		invokeWithInputRequiredSupport.invoke(runnable, getContext(), getFrame());
	}

	protected DetailsController<Clct> getDetailsConroller() {
		return ctlDetails;
	}
}	// class CollectController
