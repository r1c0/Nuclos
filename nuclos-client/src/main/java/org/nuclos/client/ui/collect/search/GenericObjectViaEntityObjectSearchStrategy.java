//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Observer;

import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.dal.DalSupportForGO;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.entityobject.CollectableEntityObjectProxyListAdapter;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.common.CollectableEntityFieldWithEntityForExternal;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;

public class GenericObjectViaEntityObjectSearchStrategy extends CollectSearchStrategy<CollectableGenericObjectWithDependants> {

	// static

	private static final Logger LOG = Logger.getLogger(GenericObjectViaEntityObjectSearchStrategy.class);

	private final class MyProxyListAdapter implements ProxyList<CollectableGenericObjectWithDependants> {

		private final class MyListIterator implements ListIterator<CollectableGenericObjectWithDependants> {

			private final ListIterator<CollectableEntityObject> iter;

			public MyListIterator(ListIterator<CollectableEntityObject> iter) {
				this.iter = iter;
			}

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public CollectableGenericObjectWithDependants next() {
				return eo2Go(iter.next());
			}

			@Override
			public boolean hasPrevious() {
				return iter.hasPrevious();
			}

			@Override
			public CollectableGenericObjectWithDependants previous() {
				return eo2Go(iter.previous());
			}

			@Override
			public int nextIndex() {
				return iter.nextIndex();
			}

			@Override
			public int previousIndex() {
				return iter.previousIndex();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(CollectableGenericObjectWithDependants e) {
				iter.set(go2Eo(e));
			}

			@Override
			public void add(CollectableGenericObjectWithDependants e) {
				throw new UnsupportedOperationException();
			}

		}

		private final CollectableEntityObjectProxyListAdapter list;

		private MyProxyListAdapter(CollectableEntityObjectProxyListAdapter list) {
			this.list = list;
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public boolean isEmpty() {
			return list.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof CollectableGenericObjectWithDependants)) return false;
			final CollectableGenericObjectWithDependants go = (CollectableGenericObjectWithDependants) o;
			return getIndexById(go.getId()) >= 0;
		}

		@Override
		public Object[] toArray() {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean add(CollectableGenericObjectWithDependants e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for (Object i: c) {
				if (!contains(i)) return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends CollectableGenericObjectWithDependants> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(int index, Collection<? extends CollectableGenericObjectWithDependants> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			list.clear();
		}

		@Override
		public CollectableGenericObjectWithDependants get(int index) {
			return eo2Go(list.get(index));
		}

		@Override
		public CollectableGenericObjectWithDependants set(int index, CollectableGenericObjectWithDependants element) {
			return eo2Go(list.set(index, go2Eo(element)));
		}

		@Override
		public void add(int index, CollectableGenericObjectWithDependants element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public CollectableGenericObjectWithDependants remove(int index) {
			return eo2Go(list.remove(index));
		}

		@Override
		public int indexOf(Object o) {
			if (!(o instanceof CollectableGenericObjectWithDependants)) return -1;
			final CollectableGenericObjectWithDependants go = (CollectableGenericObjectWithDependants) o;
			return getIndexById(go.getId());
		}

		@Override
		public int lastIndexOf(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ListIterator<CollectableGenericObjectWithDependants> listIterator() {
			return new MyListIterator(list.listIterator());
		}

		@Override
		public ListIterator<CollectableGenericObjectWithDependants> listIterator(int index) {
			return new MyListIterator(list.listIterator(index));
		}

		@Override
		public List<CollectableGenericObjectWithDependants> subList(int fromIndex, int toIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void fetchDataIfNecessary(int iIndex, ChangeListener changelistener) {
			list.fetchDataIfNecessary(iIndex, changelistener);
		}

		@Override
		public int getLastIndexRead() {
			return list.getLastIndexRead();
		}

		@Override
		public boolean hasObjectBeenReadForIndex(int index) {
			return list.hasObjectBeenReadForIndex(index);
		}

		@Override
		public Iterator<CollectableGenericObjectWithDependants> iterator() {
			return listIterator();
		}

		@Override
		public int getIndexById(Object oId) {
			return list.getIndexById(IdUtils.toLongId(oId));
		}

	}

	// instance

	private final EntityObjectDelegate lodelegate = EntityObjectDelegate.getInstance();

	private final CollectableEOEntity meta;

	private ProxyList<CollectableGenericObjectWithDependants> proxylstclct;

	private boolean includeSubModules = false;

	// contructor

	public GenericObjectViaEntityObjectSearchStrategy(CollectableEOEntity meta) {
		this.meta = meta;
	}

	protected final GenericObjectCollectController getGenericObjectController() {
		return (GenericObjectCollectController) getCollectController();
	}

	/**
	 * @return List<Collectable>
	 * @throws CollectableFieldFormatException
	 * @postcondition result != null
	 * @own-thread
	 *
	 * TODO: Make this private again.
	 */
	@Override
	public ProxyList<CollectableGenericObjectWithDependants> getSearchResult() throws CollectableFieldFormatException {
		final CollectableGenericObjectSearchExpression clctexprInternal = getInternalSearchExpression();
		clctexprInternal.setValueListProviderDatasource(getValueListProviderDatasource());
		clctexprInternal.setValueListProviderDatasourceParameter(getValueListProviderDatasourceParameter());
		LOG.debug("Interne Suchbedingung: " + clctexprInternal.getSearchCondition());

		includeSubModules = false;
		final String baseEntity = meta.getName();
		final Collection<EntityFieldMetaDataVO> fields = new ArrayList<EntityFieldMetaDataVO>();
		for (CollectableEntityField f: getCollectController().getResultController().getFields().getSelectedFields()) {
			// We need a EntityFieldMetaDataVO from the CollectableEntityField.

			// Case 0: Wrapped CollectableEntityField: unwrap.
			if (f instanceof CollectableEntityFieldWithEntityForExternal) {
				f = ((CollectableEntityFieldWithEntityForExternal) f).getField();
			}
			// Case 1: CollectableEOEntityField: already there!
			if (f instanceof CollectableEOEntityField) {
				final CollectableEOEntityField field = (CollectableEOEntityField) f;
				fields.add(field.getMeta());
			}
			// Case 2: Use MetaDataProv
			else {
				final MetaDataProvider mdProv = MetaDataClientProvider.getInstance();
				fields.add(mdProv.getEntityField(f.getEntityName(), f.getName()));
			}
			if (!baseEntity.equals(f.getEntityName())) {
				includeSubModules = true;
			}
		}

		// TODO: OPTIMIZATION: only selected and/or required attributes should be loaded here!
		final ProxyList<EntityObjectVO> proxylstlovwdvo = lodelegate.getEntityObjectProxyList(
				IdUtils.toLongId(getGenericObjectController().getModuleId()),
				clctexprInternal, fields, getCollectController().getCustomUsage());

		return new MyProxyListAdapter(new CollectableEntityObjectProxyListAdapter(proxylstlovwdvo, meta));
	}

	@Override
	public SearchWorker<CollectableGenericObjectWithDependants> getSearchWorker() {
		return new GenericObjectObservableSearchWorker(getGenericObjectController());
	}

	@Override
	public SearchWorker<CollectableGenericObjectWithDependants> getSearchWorker(List<Observer> lstObservers) {
		GenericObjectObservableSearchWorker observableSearchWorker = new GenericObjectObservableSearchWorker(getGenericObjectController());
		for (Observer observer : lstObservers)
			observableSearchWorker.addObserver(observer);
		return observableSearchWorker;
	}

	@Override
	public void search() throws CommonBusinessException {
		this.search(false);
	}

	@Override
	public void search(boolean bRefreshOnly) throws CommonBusinessException {
		LOG.debug("START search");
		final GenericObjectCollectController cc = getGenericObjectController();

		cc.makeConsistent(true);
		cc.removePreviousChangeListenersForResultTableVerticalScrollBar();
		final ProxyList<CollectableGenericObjectWithDependants> lstclctResult = getSearchResult();
		setCollectableProxyList(lstclctResult);
		cc.fillResultPanel(lstclctResult, lstclctResult.size(), false);
		cc.setupChangeListenerForResultTableVerticalScrollBar();
		LOG.debug("FINISHED search");
	}

	/**
	 * @return the internal version of the collectable search condition, that is used for performing the actual search.
	 *         Includes the selected global search filter's internal search condition (if any).
	 * @throws CollectableFieldFormatException
	 */
	protected final CollectableSearchCondition getInternalSearchCondition() throws CollectableFieldFormatException {
		if (getCollectableIdListCondition() != null) {
			return getCollectableIdListCondition();
		}
		final CompositeCollectableSearchCondition compositecond = new CompositeCollectableSearchCondition(
				LogicalOperator.AND);

		final CollectableSearchCondition clctcond = GenericObjectClientUtils.getInternalSearchCondition(
				getGenericObjectController().getModuleId(), getCollectableSearchCondition());
		if (clctcond != null)
			compositecond.addOperand(clctcond);

		return SearchConditionUtils.simplified(compositecond);
	}

	/**
	 * @return search expression containing the internal version of the collectable search condition,
	 *         that is used for performing the actual search, and the sorting sequence.
	 * @throws CollectableFieldFormatException
	 */
	@Override
	public CollectableGenericObjectSearchExpression getInternalSearchExpression()
			throws CollectableFieldFormatException {
		final GenericObjectCollectController cc = getGenericObjectController();
		CollectableGenericObjectSearchExpression clctGOSearchExpression = new CollectableGenericObjectSearchExpression(
				getInternalSearchCondition(), cc.getResultController().getCollectableSortingSequence(),
				cc.getSearchDeleted());
		return clctGOSearchExpression;
	}

	private CollectableEntityObject go2Eo(CollectableGenericObjectWithDependants go) {
		return new CollectableEntityObject(meta,
				DalSupportForGO.wrapGenericObjectVO(go.getGenericObjectCVO(), meta));
	}

	private CollectableGenericObjectWithDependants eo2Go(CollectableEntityObject eo) {
		if (eo == null) {
			LOG.warn("eo2Go: CollectableEntityObject is null");
			return null;
		}
		return CollectableGenericObjectWithDependants.newCollectableGenericObjectWithDependants(
				DalSupportForGO.getGenericObjectWithDependantsVO(eo.getEntityObjectVO(), meta));
	}

	/**
	 * TODO: Make this protected again.
	 *
	 * @deprecated Move to ObservableSearchWorker???
	 */
	@Override
	public void setCollectableProxyList(ProxyList<CollectableGenericObjectWithDependants> proxylstclct) {
		this.proxylstclct = proxylstclct;
	}

	// @todo move to ResultController or ResultPanel?
	@Override
	public ProxyList<CollectableGenericObjectWithDependants> getCollectableProxyList() {
		return proxylstclct;
	}

	/*
	 * The following methods have been defined only for a subset of strategies.
	 */

	@Override
	public boolean getIncludeSubModulesForSearch() {
		return includeSubModules;
	}

}
