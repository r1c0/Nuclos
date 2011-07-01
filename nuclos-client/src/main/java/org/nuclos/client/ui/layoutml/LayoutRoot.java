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
package org.nuclos.client.ui.layoutml;

import org.nuclos.client.ui.collect.*;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.*;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;
import org.nuclos.common2.EntityAndFieldName;
import org.apache.log4j.Logger;
import javax.swing.*;
import java.util.*;

/**
 * represents the output of the LayoutML parser.
 * Contains the root panel and the maps of collectable components, their models and subforms.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo this would better be called LayoutMLParser.Output or LayoutMLParser.Result
 * @todo generify <Clctcompmodel extends CollectableComponentModel> CollectableComponentModelProvider
 */
public class LayoutRoot implements CollectableComponentsProvider, CollectableComponentModelProvider {

	private static final Logger log = Logger.getLogger(LayoutRoot.class);

	/**
	 * true: Search components, false: Details components
	 */
	private final boolean bForSearch;

	/**
	 * the root component.
	 * @invariant != null
	 */
	private final JComponent compRoot;

	/**
	 * maps a field name to the associated CollectableComponents.
	 * @invariant != null
	 */
	private final CollectableComponentsProvider clctcompprovider;

	/**
	 * maps a field name to the associated CollectableComponentModel.
	 * @invariant != null
	 */
	private final Map<String, CollectableComponentModel> mpclctcompmodel;

	/**
	 * maps an entity name to the associated subform.
	 */
	private final Map<String, SubForm> mpsubform;

	/**
	 * <code>MultiMap</code> of parsed inter-field dependencies.
	 * Maps a field name <code>x</code> to a <code>Collection</code> of field names that <code>x</code> depends on.
	 */
	private final MultiListMap<String, String> mmpDependencies;

	private List<String> lstOrderedFieldNames;

	private final EntityAndFieldName eafnInitialFocus;

	/**
	 * creates the <code>LayoutRoot</code> out of its components.
	 * @param bForSearch
	 * @param compRoot
	 * @param clctcompprovider
	 * @param mpsubform
	 * @param mmpDependencies
	 * @param eafnInitialFocus the entity name and field name of the component that is to get the focus initially.
	 * @precondition compRoot != null
	 * @precondition clctcompprovider != null
	 * @precondition mpclctcompmodel != null
	//	 * @precondition clctcompprovider.keySet().equals(mpclctcompmodel.keySet())
	 */
	public LayoutRoot(boolean bForSearch, JComponent compRoot, CollectableComponentsProvider clctcompprovider,
			Map<String, CollectableComponentModel> mpclctcompmodel,
			Map<String, SubForm> mpsubform, MultiListMap<String, String> mmpDependencies,
			EntityAndFieldName eafnInitialFocus) {

		// @todo check consistency of the field names or create the model out of the view right here.
//		if(!clctcompprovider.keySet().equals(mpclctcompmodel.keySet())) {
//			throw new IllegalArgumentException("clctcompprovider and mpclctcompmodel do not match.");
//		}

		this.bForSearch = bForSearch;
		this.compRoot = compRoot;
		this.clctcompprovider = clctcompprovider;
		this.mpclctcompmodel = mpclctcompmodel;
		this.mpsubform = mpsubform;
		this.mmpDependencies = mmpDependencies;
		this.eafnInitialFocus = eafnInitialFocus;
	}

	/**
	 * @return a new empty <code>LayoutRoot</code>.
	 * @param bForSearch
	 */
	public static LayoutRoot newEmptyLayoutRoot(boolean bForSearch) {
		return new LayoutRoot(bForSearch, new JPanel(), new DefaultCollectableComponentsProvider(),
				new HashMap<String, CollectableComponentModel>(0), new HashMap<String, SubForm>(),
				new MultiListHashMap<String, String>(), new EntityAndFieldName((String) null, null));
	}

	public boolean isForSearch() {
		return this.bForSearch;
	}

	public boolean isForDetails() {
		return !this.isForSearch();
	}

	/**
	 * @return
	 * @precondition this.isForSearch()
	 */
	public SearchEditModel getSearchEditModel() {
		if (!this.isForSearch()) {
			throw new IllegalStateException("this.isForSearch()");
		}
		return new DefaultSearchEditModel(this.getCollectableComponents());
	}

	/**
	 * @return
	 * @precondition this.isForDetails()
	 */
	public DetailsEditModel getDetailsEditModel() {
		if (!this.isForDetails()) {
			throw new IllegalStateException("this.isForDetails()");
		}
		return new DefaultDetailsEditModel(this.getCollectableComponents());
	}

	/**
	 * @return the root component generated from the LayoutML parser.
	 * @postcondition result != null
	 */
	public JComponent getRootComponent() {
		return this.compRoot;
	}

	/**
	 * @return the <code>CollectableComponent</code>s that were constructed by the parser.
	 * @postcondition result != null
	 */
	@Override
	public Collection<CollectableComponent> getCollectableComponents() {
		return this.clctcompprovider.getCollectableComponents();
	}
	
	/**
	 * @return the <code>CollectableComponent Labels</code>s that were constructed by the parser.
	 * @postcondition result != null
	 * NUCLEUSINT-442
	 */
	@Override
	public Collection<CollectableComponent> getCollectableLabels() {
		return this.clctcompprovider.getCollectableLabels();
	}

	/**
	 * @param sFieldName
	 * @return the <code>CollectableComponent</code>s with the given field name that were constructed by the parser.
	 * @postcondition result != null
	 */
	@Override
	public Collection<CollectableComponent> getCollectableComponentsFor(String sFieldName) {
		return this.clctcompprovider.getCollectableComponentsFor(sFieldName);
	}

	/**
	 * @return Map<String sFieldName, CollectableComponentModel> the map of parsed/constructed collectable component models.
	 * Maps a field name to a <code>CollectableComponentModel</code>.
	 * @deprecated LayoutRoot implements CollectableComponentModelProvider - no need to return a map here.
	 */
	@Deprecated
	public Map<String, ? extends CollectableComponentModel> getMapOfCollectableComponentModels() {
		return this.mpclctcompmodel;
	}

	@Override
	public Collection<? extends CollectableComponentModel> getCollectableComponentModels() {
		return Collections.unmodifiableCollection(this.mpclctcompmodel.values());
	}

	/**
	 * @param sFieldName
	 * @return the <code>CollectableComponentModel</code> with the given field name.
	 */
	@Override
	public CollectableComponentModel getCollectableComponentModelFor(String sFieldName) {
		return this.mpclctcompmodel.get(sFieldName);
	}

	/**
	 * @return Map<String sEntityName, SubForm> the map of parsed/constructed subforms.
	 */
	public Map<String, SubForm> getMapOfSubForms() {
		return this.mpsubform;
	}

	/**
	 * @return an unordered and unmodifiable <code>Collection</code> of the parsed field names.
	 */
	@Override
	public Collection<String> getFieldNames() {
		return Collections.unmodifiableCollection(this.mpclctcompmodel.keySet());
	}

	/**
	 * @return List<String> an ordered unmodifiable <code>List</code> of the parsed field names.
	 * The order respects the dependencies between fields:
	 * The order is such that if <code>o1</code> depends on <code>o2</code>,
	 * <code>o1</code> comes after <code>o2</code> in the list.
	 */
	public List<String> getOrderedFieldNames() {
		if (this.lstOrderedFieldNames == null) {
			final List<String> lst = new ArrayList<String>(this.getFieldNames());
			Collections.sort(lst, new DependencyComparator(this.mmpDependencies));
			this.lstOrderedFieldNames = Collections.unmodifiableList(lst);
			/** @todo OPTIMIZE: It would be okay to forget the multimap of dependencies here. */
		}
		return this.lstOrderedFieldNames;
	}

	/**
	 * @return the entity name and field name of the component, if any, that is to get the focus initially.
	 * <code>null</code> means there is no such component.
	 */
	public EntityAndFieldName getInitialFocusEntityAndFieldName() {
		return this.eafnInitialFocus;
	}

	/**
	 * Comparator for field names that takes the dependencies into account.
	 */
	private static class DependencyComparator implements Comparator<String> {

		private final MultiListMap<String, String> mmpDependencies;

		DependencyComparator(MultiListMap<String, String> mmpDependencies) {
			this.mmpDependencies = mmpDependencies;
		}

		/**
		 * The following rules are applied (in this order):
		 * 1. sField1Name > sField2Name if there is a dependency sField1Name -> sField2Name
		 * 2. sField1Name < sField2Name if there is a dependency sField2Name -> sField1Name
		 * 3. sField1Name > sField2Name if there is a dependency sField1Name -> anything
		 * 4. sField1Name < sField2Name if there is a dependency sField2Name -> anything
		 * 5. sField1Name == sField2Name otherwise.
		 * @param sField1Name
		 * @param sField2Name
		 */
		@Override
		public int compare(String sField1Name, String sField2Name) {
			final int result;
			if (dependsOn(sField1Name, sField2Name)) {
				result = 1;
			}
			else if (dependsOn(sField2Name, sField1Name)) {
				result = -1;
			}
			else {
				if (isDependant(sField1Name)) {
					// sField1Name is potentially greater than sField2Name.
					result = 1;
				}
				else if (isDependant(sField2Name)) {
					// sField1Name is potentially less than sField2Name.
					result = -1;
				}
				else {
					// both elements are neutral. Their order doesn't matter.
					result = 0;
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("compare(" + sField1Name + "," + sField2Name + ") = " + result);
			}
			return result;
		}

		/**
		 * @param sField1Name
		 * @param sField2Name
		 * @return Does <code>sField1Name</code> depend on <code>sField2Name</code>?
		 */
		private boolean dependsOn(String sField1Name, String sField2Name) {
			return this.mmpDependencies.getValues(sField1Name).contains(sField2Name);
		}

		/**
		 * @param sFieldName
		 * @return Is <code>sFieldName</code> dependant at all, that is:
		 * Does <code>sFieldName</code> occur on any left side of a dependency?
		 */
		private boolean isDependant(String sFieldName) {
			return this.mmpDependencies.containsKey(sFieldName);
		}

	}	// inner class DependencyComparator



}	// class LayoutRoot
