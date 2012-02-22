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
package org.nuclos.client.common;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.CollectableGenericObjectEntityForAllAttributes;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;

/**
 * Collectable text field which can be enhanced by a number of search peers.
 * In a search panel it can create a search expression of the form "<thisAttribute> fulfills condition OR <peer1> fulfills condition OR ...".
 * The context menu of this component displays an option to toggle this functionality.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class NuclosCollectableTextFieldWithSearchPeers extends CollectableTextField {

	private static final Logger LOG = Logger.getLogger(NuclosCollectableTextFieldWithSearchPeers.class);
	
	/** @todo Potential NPE when property "search-peers" is omitted */
	private Collection<String> collPeers = null;
	private boolean bUsePeers = false;

	public NuclosCollectableTextFieldWithSearchPeers(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, bSearchable);
	}

	@Override
	public void setProperty(String sName, Object oValue) {
		super.setProperty(sName, oValue);

		if (sName.equals("search-peers")) {
			setPeers((String) oValue);
		}
	}

	@Override
	public JPopupMenu newJPopupMenu() {
		final JPopupMenu result;

		if (this.isSearchComponent()) {
			result = super.newJPopupMenu();
			result.addSeparator();

			final Action act = new AbstractAction(SpringLocaleDelegate.getInstance().getMessage(
					"NuclosCollectableTextFieldWithSearchPeers.1", "Suche erweitern")) {
				@Override
				public void actionPerformed(ActionEvent ev) {
					bUsePeers = !bUsePeers;
					updateSearchConditionInModel();
				}
			};

			final JCheckBoxMenuItem mi = new JCheckBoxMenuItem(act);
			result.add(mi);

			mi.setState(bUsePeers);
		}
		else {
			result = null;
		}
		return result;
	}

	/**
	 * Parse comma separated list of peers and store them.
	 * @param sPeers
	 * @precondition sPeers != null
	 */
	private void setPeers(String sPeers) {
		final String[] asPeers = sPeers.split(",");
		this.collPeers = CollectionUtils.asSet(asPeers);
	}

	/**
	 * Build an OR condition from all given peers.
	 */
	@Override
	protected CollectableSearchCondition getSearchConditionFromView() throws CollectableFieldFormatException {

		// create an OR condition and put all the peers into it
		// Note that this algorithm requires an atomic condition:
		final AtomicCollectableSearchCondition atomiccondSuper = (AtomicCollectableSearchCondition) super.getSearchConditionFromView();

		// Note that atomiccondSuper.getComparisonOperator() can differ from this.getComparisonOperator() -
		// we must ignore this.getComparisonOperator() here!

		final CollectableSearchCondition result;
		if (bUsePeers && atomiccondSuper != null) {
			final CompositeCollectableSearchCondition condOr = new CompositeCollectableSearchCondition(LogicalOperator.OR);
			condOr.addOperand(atomiccondSuper);
			for (String sPeer : collPeers) {
				// @todo make generic - replace with the entity this component already knows:
				final CollectableEntityField clctefPeer = CollectableGenericObjectEntityForAllAttributes.getInstance("ALL_ATTRIBUTES").getEntityField(sPeer);
				condOr.addOperand(SearchConditionUtils.getConditionForPeer(clctefPeer, atomiccondSuper));
			}
			result = condOr;
		}
		else {
			result = atomiccondSuper;
		}

		return result;
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
				try {
					final CollectableSearchCondition cond = ev.getSearchComponentModel().getSearchCondition();
					final AtomicCollectableSearchCondition atomiccond = getFirstAtomicSearchCondition(cond);
	
					modelToView(atomiccond, getJTextComponent());
	
					bUsePeers = (cond instanceof CompositeCollectableSearchCondition);
				}
				catch (Exception e) {
					LOG.error("searchConditionChangedInModel failed: " + e, e);
				}
			}
		});
	}

	private AtomicCollectableSearchCondition getFirstAtomicSearchCondition(CollectableSearchCondition cond) {
		final AtomicCollectableSearchCondition result;

		if (cond instanceof CompositeCollectableSearchCondition) {
			result = getFirstAtomicSearchCondition(((CompositeCollectableSearchCondition) cond).getOperands().get(0));
		}
		else if (cond instanceof AtomicCollectableSearchCondition) {
			result = (AtomicCollectableSearchCondition) cond;
		}
		else {
			result = null;
		}

		return result;
	}

}	// class NuclosCollectableTextFieldWithSearchPeers
