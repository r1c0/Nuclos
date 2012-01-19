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
package org.nuclos.client.genericobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import org.nuclos.common.NuclosEOField;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils.GetFieldName;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils.HasType;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.client.common.SearchConditionSubFormController;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponent.CanDisplay;
import org.nuclos.client.ui.collect.search.SearchPanel;

/**
 * <code>SearchPanel</code> for <code>GenericObjectCollectController</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class GenericObjectSearchPanel extends SearchPanel {

	private Collection<CollectableComponent> additionalSearchComponents;

	public GenericObjectSearchPanel(Collection<CollectableComponent> additionalSearchComponents) {
		super();
		
		this.additionalSearchComponents = additionalSearchComponents;
	}
	
	/** @todo	*/
	public void setLayoutRoot() {

	}

	/**
	 * @param compRoot the edit component according to the LayoutML
	 * @return the edit component to be used in the Search panel. Default is <code>compRoot</code> itself.
	 *         Successors may build their own component/panel out of compRoot.
	 */
	public JComponent newEditComponent(JComponent compRoot) {
		return compRoot;
	}
	
	public Collection<CollectableComponent> getAdditionalSearchComponents() {
		return additionalSearchComponents;
	}
	
	@Override
	public void setSearchEditorVisible(boolean bVisible) {
		super.setSearchEditorVisible(bVisible);
		if (getAdditionalSearchComponents() != null) {
			for (CollectableComponent clctcomp : getAdditionalSearchComponents()) {
				clctcomp.getControlComponent().setVisible(!bVisible);
			}
		}
	}

	/**
	 * @param cond May be <code>null</code>.
	 * @return Can the given search condition be displayed in the search fields?
	 * 
	 * TODO: Make this protected again.
	 */
	@Override
	public boolean canDisplayConditionInFields(CollectableSearchCondition cond) {
		return SearchConditionUtils.trueIfNull(cond).accept(new CanDisplayConditionInFieldsVisitor(this));
	}

	/**
	 * Visitor that checks if a given search condition can be displayed in the search fields of a <code>GenericObjectCollectController</code>.
	 * All Visitor methods return a non-null Boolean value.
	 */
	static class CanDisplayConditionInFieldsVisitor extends SearchPanel.CanDisplayConditionInFieldsVisitor {

		CanDisplayConditionInFieldsVisitor(SearchPanel searchpanel) {
			super(searchpanel);
		}
		
		@Override
		public Boolean visitAtomicCondition(final AtomicCollectableSearchCondition atomiccond) throws RuntimeException {
			final Collection<CollectableComponent> collclctcomp = new ArrayList<CollectableComponent>();
			collclctcomp.addAll(searchpanel.getEditView().getCollectableComponentsFor(atomiccond.getFieldName()));
			for (CollectableComponent clctcomp : ((GenericObjectSearchPanel)searchpanel).getAdditionalSearchComponents()) {
				if (clctcomp.getFieldName().equals(atomiccond.getFieldName()))
				collclctcomp.add(clctcomp);
			}
			return !collclctcomp.isEmpty() && CollectionUtils.forall(collclctcomp, new CanDisplay(atomiccond));
		}

		@Override
		public Boolean visitCompositeCondition(CompositeCollectableSearchCondition compositecond) {
			final List<CollectableSubCondition> lstsubcond = CollectionUtils.selectInstancesOf(compositecond.getOperands(), CollectableSubCondition.class);
			final Predicate<CollectableSubCondition> predIsSubEntityNameUnique = PredicateUtils.transformedInputPredicate(new GetSubEntityName(), PredicateUtils.<String>isUnique());
			final List<AtomicCollectableSearchCondition> lstatomiccond = CollectionUtils.selectInstancesOf(compositecond.getOperands(), AtomicCollectableSearchCondition.class);

			return
					// composite conditions must be flat conjunctions (ANDed, not nested),
					(compositecond.getLogicalOperator() == LogicalOperator.AND) && !isNested(compositecond) &&
							// the atomic conditions they contain must be unique (in terms of attribute names),
							CollectionUtils.forall(lstatomiccond, PredicateUtils.transformedInputPredicate(new GetFieldName(), PredicateUtils.<String>isUnique())) &&
							// the atomic conditions themselves must be displayable,
							CollectionUtils.forall(lstatomiccond, new CanAtomicSearchConditionBeDisplayed()) &&
							// the subconditions they contain must be unique (in terms of subentity names) and must themselves be displayable:
							CollectionUtils.forall(lstsubcond, PredicateUtils.and(predIsSubEntityNameUnique, new CanSubConditionBeDisplayed())) &&
							// we have two state fields Status and Statusnumeral, we check here if max one of these is used
							hasMaximalOneStateCondition(lstatomiccond);
		}
		
		/**
		 * 
		 * @param lstatomiccond
		 * @return
		 */
		private static boolean hasMaximalOneStateCondition(List<AtomicCollectableSearchCondition> lstatomiccond) {
			boolean blnStatusnumeralFound = false;
			boolean blnStatusFound = false;
			
			for (AtomicCollectableSearchCondition clctcond : lstatomiccond) {
				if (NuclosEOField.STATENUMBER.getMetaData().getField().equals(clctcond.getFieldName())) {
					blnStatusnumeralFound = true;
				} else if (NuclosEOField.STATE.getMetaData().getField().equals(clctcond.getFieldName())) {
					blnStatusFound = true;
				}
			}
			
			return !(blnStatusFound && blnStatusnumeralFound);
		}

		/**
		 * @param compositecond
		 * @return Does the given composite condition contain nested composite conditions?
		 */
		private static boolean isNested(CompositeCollectableSearchCondition compositecond) {
			return CollectionUtils.exists(compositecond.getOperands(), new HasType(CollectableSearchCondition.TYPE_COMPOSITE));
		}

		@Override
		public Boolean visitSubCondition(CollectableSubCondition subcond) {
			return canSubConditionBeDisplayedInSearchFields(subcond);
		}

		@Override
		public Boolean visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
			/** @todo only under specific circumstances! */
			return true;
		}

		private static boolean canSubConditionBeDisplayedInSearchFields(CollectableSubCondition clctsubcond) {
			return SearchConditionSubFormController.canSubConditionBeDisplayed(clctsubcond);
		}

		private static class GetSubEntityName implements Transformer<CollectableSubCondition, String> {
			@Override
			public String transform(CollectableSubCondition subcond) {
				return subcond.getSubEntityName();
			}
		}

		private static class CanSubConditionBeDisplayed implements Predicate<CollectableSubCondition> {
			@Override
			public boolean evaluate(CollectableSubCondition subcond) {
				return canSubConditionBeDisplayedInSearchFields(subcond);
			}
		}

		private class CanAtomicSearchConditionBeDisplayed implements Predicate<AtomicCollectableSearchCondition> {
			@Override
			public boolean evaluate(AtomicCollectableSearchCondition atomiccond) {
				return visitAtomicCondition(atomiccond);
			}
		}

	}	// inner class CanDisplayConditionInFieldsVisitor

}	// class GenericObjectSearchPanel
