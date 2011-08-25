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
package org.nuclos.client.ui.collect.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.common.DatasourceBasedCollectableFieldsProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.ListOfValues;
import org.nuclos.client.ui.ListOfValues.QuickSearchResulting;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.labeled.LabeledListOfValues;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache.CachingCollectableFieldsProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.format.FormattingTransformer;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.masterdata.ejb3.EntityFacadeRemote;

/**
 * A <code>CollectableComponent</code> that presents a value in a <code>ListOfValues</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableListOfValues extends LabeledCollectableComponentWithVLP implements ICollectableListOfValues {

	private static final Logger log = Logger.getLogger(CollectableListOfValues.class);

	/**
	 * the value id "remembered in the view", as the JTextField only holds the value.
	 */
	private Object oValueId;

	private final List<LookupListener> lstLookupListeners = new LinkedList<LookupListener>();

	private DocumentListener documentlistener;

	/**
	 * inner class <code>CollectableListOfValues.Event</code>.
	 */
	public static class Event extends CollectableComponentEvent {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param clctlovSource the <code>CollectableListOfValues</code> that triggered this event.
		 * @precondition clctlovSource != null
		 */
		public Event(CollectableListOfValues clctlovSource) {
			super(clctlovSource);
		}

		/**
		 * @return the <code>CollectableListOfValues</code> that triggered this event.
		 * @postcondition result != null
		 */
		public CollectableListOfValues getCollectableListOfValues() {
			return (CollectableListOfValues) this.getCollectableComponent();
		}

	}	// inner class Event

	/**
	 * inner class <code>LOVListener</code>.
	 */
	public interface LOVListener extends ReferencingListener {

		/**
		 * performs a lookup on the entity referenced by this component.
		 * The default action is to let the user perform a search within this entity and pick one object.
		 * @param ev
		 */
		public void lookup(Event ev);

		/**
		 * performs a search and shows search results on the entity referenced by this component using a search condition.
		 * The default action is to let the user pick one object in the shown search result.
		 * @param ev
		 */
		public void viewSearchResults(Event ev);
	}	// inner class LOVListener

	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableListOfValues(CollectableEntityField clctef) {
		this(clctef, false);

		assert this.isDetailsComponent();
	}

	/**
	 * @param clctef
	 * @param bSearchable
	 * @precondition clctef != null
	 * @precondition clctef.isIdField()
	 * @precondition clctef.isReferencing()
	 */
	public CollectableListOfValues(final CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new LabeledListOfValues(), bSearchable);

		if (clctef == null) {
			throw new NullArgumentException("clctef");
		}
		if (!clctef.isIdField()) {
			throw new IllegalArgumentException(StringUtils.getParameterizedExceptionMessage("collectable.listofvalues.exception.1", clctef.getName()));
				//"Das Feld \"" + clctef.getName() + "\" ist kein Id-Feld und kann daher nicht in einem LOV (Suchfeld) dargestellt werden.");
		}
		if (!clctef.isReferencing()) {
			throw new IllegalArgumentException(StringUtils.getParameterizedExceptionMessage("collectable.listofvalues.exception.2", clctef.getName()));
				//"Das Feld \"" + clctef.getName() + "\" ist kein Fremdschl\u00fcssel-Feld und kann daher nicht in einem LOV (Suchfeld) dargestellt werden.");
		}
		assert !this.isInsertable();
		this.setInsertable(this.isSearchComponent());

		getListOfValues().setQuickSearchResulting(new QuickSearchResulting() {
			@Override
			protected List<CollectableValueIdField> getQuickSearchResult(String inputString) {
				Integer vlpId = null;
				Map<String, Object> vlpParameter = null;
				CollectableFieldsProvider provider = getValueListProvider();
				if (provider instanceof CachingCollectableFieldsProvider) {
					provider = ((CachingCollectableFieldsProvider) provider).getDelegate();
				}
				if (provider instanceof DatasourceBasedCollectableFieldsProvider) {
					DatasourceBasedCollectableFieldsProvider dsProvider = ((DatasourceBasedCollectableFieldsProvider) provider);
					if (dsProvider.isValuelistProviderDatasource()) {
						vlpId = dsProvider.getDatasourceVO().getId();
						vlpParameter = dsProvider.getValueListParameter();
					}
				}

				return CollectableListOfValues.getQuickSearchResult(clctef, inputString, vlpId, vlpParameter);
			}
		});

		this.setupLookupListener();

		this.getListOfValues().setQuickSearchEnabled(enableQuickSearch(clctef));

		CollectableEntity clcte = clctef.getCollectableEntity();
		EntityFieldMetaDataVO efMeta = MetaDataClientProvider.getInstance().getEntityField(clcte.getName(), clctef.getName());
		final EntityMetaDataVO eMetaForeign = MetaDataClientProvider.getInstance().getEntity(efMeta.getForeignEntity());

		this.getListOfValues().setQuickSearchSelectedListener(new ListOfValues.QuickSearchSelectedListener() {
			@Override
			public void actionPerformed(CollectableValueIdField itemSelected) {
				if (itemSelected == null) {
					CollectableListOfValues.this.clearListOfValues();
				} else {
					try {
						if (eMetaForeign.isStateModel()) {
							CollectableListOfValues.this.acceptLookedUpCollectable(CollectableGenericObjectWithDependants.newCollectableGenericObject(
								GenericObjectDelegate.getInstance().get((Integer) itemSelected.getValueId())));
						} else {
							CollectableListOfValues.this.acceptLookedUpCollectable(new CollectableMasterData(
								new CollectableMasterDataEntity(MetaDataCache.getInstance().getMetaData(clctef.getReferencedEntityName())),
								MasterDataDelegate.getInstance().get(clctef.getReferencedEntityName(), itemSelected.getValueId())));
						}
					} catch(Exception e) {
						Errors.getInstance().showExceptionDialog(CollectableListOfValues.this.getListOfValues(), e);
					}
				}
			}
		});
		this.getListOfValues().setQuickSearchCanceledListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CollectableListOfValues.this.setViewLocked(true);
				CollectableListOfValues.this.modelToView();
				CollectableListOfValues.this.setViewLocked(false);
			}
		});
		if (this.isSearchComponent()) {
			this.getListOfValues().setSearchOnLostFocus(false);
		}

		assert this.isInsertable() == this.isSearchComponent();
	}

	/**
	 *
	 * @param clctef
	 * @return
	 */
	private static boolean enableQuickSearch(final CollectableEntityField clctef) {
		return true;
	}

	/**
	 *
	 * @param clctef
	 * @param inputString
	 * @param dsvo
	 * @param collectableFieldsProvider
	 * @return
	 */
	private static List<CollectableValueIdField> getQuickSearchResult(final CollectableEntityField clctef, final String inputString, Integer vlpId, Map<String, Object> vlpParameter) {

		CollectableEntity clcte = clctef.getCollectableEntity();
		if (clcte == null)
			return Collections.emptyList();

		return ServiceLocator.getInstance().getFacade(EntityFacadeRemote.class).getQuickSearchResult(clcte.getName(), clctef.getName(), inputString, vlpId, vlpParameter, QUICKSEARCH_MAX);
	}

	/**
	 * adds a "Clear" entry to the popup menu, for a non-searchable component.
	 */
	@Override
	public JPopupMenu newJPopupMenu() {
		final JPopupMenu result = super.newJPopupMenu();
		if (!this.isSearchComponent()) {
			final JMenuItem miClear = new JMenuItem(CommonLocaleDelegate.getMessage("CollectableFileNameChooserBase.1","Zur\u00fccksetzen"));
			boolean bClearEnabled;
			try {
				bClearEnabled = this.getBrowseButton().isEnabled() && (this.getField().getValueId() != null);
			}
			catch (CollectableFieldFormatException ex) {
				bClearEnabled = false;
			}
			miClear.setEnabled(bClearEnabled);

			miClear.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					CollectableListOfValues.this.clearListOfValues();
				}
			});
			result.add(miClear);
		}
		return result;
	}

	private void clearListOfValues() {
		clear();
		fireLookupSuccessful(new LookupEvent(CollectableListOfValues.this, null, null));
	}

	@Override
	protected void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
		this.getJTextField().addMouseListener(popupmenulistener);
		// add a listener to the button because the textfield may not be visible for some instances:
		this.getBrowseButton().addMouseListener(popupmenulistener);
	}

	/**
	 * sets up the listeners for "lookup".
	 */
	private void setupLookupListener() {
		this.getBrowseButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				if(CollectableListOfValues.this.getCollectableSearchCondition() != null){
					fireViewSearchResults();
				} else {
					fireLookup();
				}
			}
		});
	}

	/**
	 * @return the internal control component
	 */
	public ListOfValues getListOfValues() {
		return ((LabeledListOfValues) this.getLabeledComponent()).getListOfValues();
	}

	@Override
	public void setColumns(int iColumns) {
		this.getListOfValues().setColumns(iColumns);
	}

	/**
	 * @deprecated Not used in tabbed GUI any more. (Thomas Pasch)
	 */
	public JButton getBrowseButton() {
		return this.getListOfValues().getBrowseButton();
	}

	/**
	 * @return the textfield that is part of the component.
	 */
	public JTextField getJTextField() {
		return this.getListOfValues().getJTextField();
	}

	@Override
	public JComponent getFocusableComponent() {
		return this.getJTextField();
	}

	/**
	 * @return Is this component insertable? That means: Is the textfield editable?
	 */
	public boolean isInsertable() {
		return (this.documentlistener != null);
	}

	/**
	 * insertable means: it is possible to enter the value in the textfield directly. In this case,
	 * the value id is set to null.
	 * @param bInsertable
	 */
	@Override
	public void setInsertable(boolean bInsertable) {
		if (this.isInsertable() != bInsertable) {
			/** @todo respect "enabled" property */
			this.getJTextField().setEditable(bInsertable);
			if (bInsertable) {
				this.registerDocumentListener();
			}
			else {
				this.unregisterDocumentListener();
			}
		}
		assert this.isInsertable() == bInsertable;
	}

	@Override
	public void setEnabled(boolean bEnabled) {
		if (this.isSearchComponent()) {
			/** @todo respect "insertable" property */
			super.setEnabled(bEnabled);
		}
		else {
			getListOfValues().getBrowseButton().setEnabled(bEnabled);
			getListOfValues().getJTextField().setEditable(bEnabled);
		}
	}

	/**
	 * @deprecated Not used in tabbed GUI. (Thomas Pasch)
	 */
	public void setBrowseButtonVisibleOnly(boolean bVisible) {
		this.getJTextField().setVisible(!bVisible);
	}

	private void registerDocumentListener() {
		if (this.documentlistener != null) {
			throw new IllegalStateException();
		}

		this.documentlistener = newDocumentListenerForTextComponentWithComparisonOperator();

		this.getJTextField().getDocument().addDocumentListener(this.documentlistener);
	}

	private void unregisterDocumentListener() {
		if (this.documentlistener == null) {
			throw new IllegalStateException();
		}
		this.getJTextField().getDocument().removeDocumentListener(this.documentlistener);
		this.documentlistener = null;
	}

	@Override
	public boolean hasComparisonOperator() {
		return true;
	}

	@Override
	public void setComparisonOperator(ComparisonOperator compop) {
		super.setComparisonOperator(compop);

		if (compop.getOperandCount() < 2) {
			this.runLocked(new Runnable() {
				@Override
				public void run() {
					getJTextField().setText(null);
				}
			});
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		if (this.oValueId == null) {
			return new CollectableValueIdField(this.oValueId, null);
		} else {
			return new CollectableValueIdField(this.oValueId, CollectableTextComponentHelper.write(this.getJTextField(), this.getEntityField()).getValue());
		}
	}

	@Override
	protected CollectableSearchCondition getSearchConditionFromView() throws CollectableFieldFormatException {
		return this.getSearchConditionFromViewImpl(this.getJTextField().getText());
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		this.getJTextField().setText(LangUtils.toString(clctfValue));

		// ensure the start of the text is visible (instead of the end) when the text is too long
		// to be fully displayed:
		this.getJTextField().setCaretPosition(0);

		this.adjustAppearance();

		this.oValueId = clctfValue.getValueId();
	}

	@Override
	protected void adjustBackground() {
//		this.getJTextField().setBackground(this.getBackgroundColor());
	}

	/**
	 * Implementation of <code>CollectableComponentModelListener</code>.
	 * This event is (and must be) ignored for a searchable text field.
	 * @param ev
	 */
	@Override
	public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
		if (this.isSearchComponent()) {
			// the text is set in searchConditionChangedInModel, but the value id is set here:
			this.oValueId = ev.getNewValue().getValueId();
		}
		else {
			super.collectableFieldChangedInModel(ev);
		}
	}

	/**
	 * Implementation of <code>CollectableComponentModelListener</code>.
	 * @param ev
	 */
	@Override
	public void searchConditionChangedInModel(final SearchComponentModelEvent ev) {
		// update the view:
		this.runLocked(new Runnable() {
			@Override
			public void run() {
				// Note: CollectableTextComponent itself can only handle atomic search conditions.
				// If the following class cast should ever fail for a special text field, redefine searchConditionChangedInModel in your subclass:
				final AtomicCollectableSearchCondition atomiccond = (AtomicCollectableSearchCondition) ev.getSearchComponentModel().getSearchCondition();

				modelToView(atomiccond, CollectableListOfValues.this.getJTextField());
				// Note that the value id is set in collectableFieldChangedInModel.
			}
		});
	}

	private void fireLookup() {
		final ReferencingListener reflistener = this.getReferencingListener();
		if (reflistener != null && reflistener instanceof LOVListener) {
			final LOVListener lovlistener = (LOVListener) reflistener;
			lovlistener.lookup(new Event(this));
		}
	}

	private void fireViewSearchResults() {
		final ReferencingListener reflistener = this.getReferencingListener();
		if (reflistener != null && reflistener instanceof LOVListener) {
			final LOVListener lovlistener = (LOVListener) reflistener;
			lovlistener.viewSearchResults(new Event(this));
		}
	}

	/**
	 * @see org.nuclos.client.ui.collect.component.ICollectableListOfValues#acceptLookedUpCollectable(org.nuclos.common.collect.collectable.Collectable)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void acceptLookedUpCollectable(Collectable clctLookedUp) {
		this.acceptLookedUpCollectable(clctLookedUp, this.getEntityField().getReferencedEntityFieldName(), null);
	}

	/**
	 * @see org.nuclos.client.ui.collect.component.ICollectableListOfValues#acceptLookedUpCollectable(org.nuclos.common.collect.collectable.Collectable, java.util.List)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void acceptLookedUpCollectable(Collectable clctLookedUp, List<Collectable> additionalCollectables) {
		this.acceptLookedUpCollectable(clctLookedUp, this.getEntityField().getReferencedEntityFieldName(), additionalCollectables);
	}

	/**
	 * accepts the given <code>Collectable</code>, that was selected by the user in a lookup operation.
	 * Notifies all registered <code>LookupListener</code>s.
	 * @param clctLookedUp
	 * @param sReferencedEntityFieldName name of the field to use as the value.
	 * @param additionalCollectables
	 * @precondition clctLookedUp != null
	 * @precondition clctLookedUp.isComplete()
	 */
	protected void acceptLookedUpCollectable(final Collectable clctLookedUp, String sReferencedEntityFieldName, List<Collectable> additionalCollectables) {
		if (clctLookedUp == null) {
			throw new NullArgumentException("clctLookedUp");
		}

		Object oForeignValue;
		try {
			if (sReferencedEntityFieldName.contains("${")) {
				oForeignValue = StringUtils.replaceParameters(sReferencedEntityFieldName, new FormattingTransformer() {
					@Override
					protected Object getValue(String field) {
						return clctLookedUp.getValue(field);
					}

					@Override
					protected String getEntity() {
						return getEntityField().getReferencedEntityName();
					}
				});
			}
			else {
				oForeignValue = clctLookedUp.getValue(sReferencedEntityFieldName);
			}

		}
		catch (Exception ex) {
			log.warn("acceptLookedUpCollectable: foreign value could not be found.");
			oForeignValue = null;
		}
		this.setField(new CollectableValueIdField(clctLookedUp.getId(), oForeignValue));
		this.fireLookupSuccessful(new LookupEvent(this, clctLookedUp, additionalCollectables));
	}

	/**
	 * @see org.nuclos.client.ui.collect.component.ICollectableListOfValues#addLookupListener(org.nuclos.client.ui.collect.component.LookupListener)
	 */
	@Override
	public synchronized void addLookupListener(LookupListener listener) {
		this.lstLookupListeners.add(listener);
	}

	/**
	 * @see org.nuclos.client.ui.collect.component.ICollectableListOfValues#removeLookupListener(org.nuclos.client.ui.collect.component.LookupListener)
	 */
	@Override
	public synchronized void removeLookupListener(LookupListener listener) {
		this.lstLookupListeners.remove(listener);
	}

	/**
	 * notifies all registered LookupListeners
	 * @param ev
	 */
	protected void fireLookupSuccessful(LookupEvent ev) {
		Collections.sort(lstLookupListeners, new Comparator<LookupListener>() {
			@Override
            public int compare(LookupListener o1, LookupListener o2) {
	            return Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
            }});

		for (LookupListener listener : lstLookupListeners) {
			listener.lookupSuccessful(ev);
		}
	}

	protected CollectableSearchCondition cSearchCondition;

	/**
	 * @see org.nuclos.client.ui.collect.component.ICollectableListOfValues#getCollectableSearchCondition()
	 */
	@Override
	public CollectableSearchCondition getCollectableSearchCondition(){
		return cSearchCondition;
	}

	protected void setCollectableSearchCondition(CollectableSearchCondition searchCondition){
		this.cSearchCondition = searchCondition;
	}

	@Override
	public void refreshValueList(boolean async) {
		// do nothing
	}

	@Override
	public void refreshValueList() {
		refreshValueList(false);
	}

}	// class CollectableListOfValues
