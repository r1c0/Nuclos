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
package org.nuclos.server.statemodel.valueobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.server.genericobject.Modules;

/**
 * class for getting initial states and state models by usage usagecriteria.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class StateModelUsages implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A state (model) usage.
	 */
	public static class StateModelUsage implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Integer iStateModel;
		private final Integer iInitialState;
		private final UsageCriteria usagecriteria;

		public StateModelUsage(Integer iStateModel, Integer iInitialState, UsageCriteria usagecriteria) {
			this.iStateModel = iStateModel;
			this.iInitialState = iInitialState;
			this.usagecriteria = usagecriteria;
		}

		public Integer getStateModelId() {
			return iStateModel;
		}

		public Integer getInitialStateId() {
			return iInitialState;
		}

		public UsageCriteria getUsageCriteria() {
			return this.usagecriteria;
		}

	}	// inner class StateModelUsage

	/**
	 * @todo Is the order in this list relevant?
	 */
	private final List<StateModelUsage> lstStateUsage = new LinkedList<StateModelUsage>();

	public StateModelUsages() {
	}

	public void add(StateModelUsage stateModelUsage) {
		lstStateUsage.add(stateModelUsage);
	}

	public Integer getStateModel(UsageCriteria usagecriteria) {
		final StateModelUsage stateModelUsage = this.getStateModelUsage(usagecriteria);
		if (stateModelUsage == null) {
			throw new NuclosFatalException();
		}
		return stateModelUsage.getStateModelId();
	}

	/**
	 * @param usagecriteria
	 * @return
	 * @precondition usagecriteria != null
	 * @precondition usagecriteria.getModuleId() != null
	 */
	public Integer getInitialStateId(UsageCriteria usagecriteria) {
		if (usagecriteria == null) {
			throw new NullArgumentException("usagecriteria");
		}
		if (usagecriteria.getModuleId() == null) {
			throw new NullArgumentException("usagecriteria.getModuleId()");
		}
		final StateModelUsage stateModelUsage = this.getStateModelUsage(usagecriteria);
		if (stateModelUsage == null) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("statemodel.usages.error.null", Modules.getInstance().getEntityLabelByModuleId(usagecriteria.getModuleId()), usagecriteria));
				//"F\u00fcr die Verwendung des Moduls " + Modules.getInstance().getEntityLabelByModuleId(usagecriteria.getModuleId()) + " " + usagecriteria + " existiert kein Statusmodell.");
		}
		return stateModelUsage.getInitialStateId();
	}

//	public StateModelUsage getUsage(UsageCriteria usagecriteria) {
//		/** @todo need to implement correct search algorithm here */
//		StateModelUsage result = null;
//		for (java.util.Iterator i = lstStateUsage.iterator(); i.hasNext();) {
//			StateModelUsage stateUsage = (StateModelUsage) i.next();
//			if (stateUsage.iModule.equals(usagecriteria.getModuleId())) {
//				result = stateUsage;
//			}
//		}
//		return result;
//	}

	/**
	 * @todo refactor: use this algorithm in UsageCriteria.getBestMatchingUsageCriteria - it's clearer.
	 * <br>
	 * @param usagecriteria
	 * @return the usage for the state model with the given usagecriteria
	 */
	public StateModelUsage getStateModelUsage(final UsageCriteria usagecriteria) {
		// 1. find matching usages (candidates):
		final List<StateModelUsage> lstCandidates = CollectionUtils.select(lstStateUsage, new Predicate<StateModelUsage>() {
			@Override
			public boolean evaluate(StateModelUsage o) {
				return o.getUsageCriteria().isMatchFor(usagecriteria);
			}
		});

		final StateModelUsage result;
		if (lstCandidates.isEmpty()) {
			/** @todo rather throw an exception here */
			result = null;
		}
		else {
			// 2. These candidates are totally ordered with respect to isLessOrEqual(). The result is the greatest of these
			// candidates.
			result = Collections.max(lstCandidates, new Comparator<StateModelUsage>() {
				@Override
				public int compare(StateModelUsage su1, StateModelUsage su2) {
					return su1.getUsageCriteria().compareTo(su2.getUsageCriteria());
				}
			});
		}
		return result;
	}

	public List<Integer> getStateModelIdsByModuleId(final Integer iModuleId) {
		final List<Integer> result = new ArrayList<Integer>();

		for (StateModelUsage smo : lstStateUsage) {
			if (iModuleId.equals(smo.getUsageCriteria().getModuleId())) {
				result.add(smo.getStateModelId());
			}
		}

		return result;
	}
	
	public List<UsageCriteria> getUsageCriteriaByStateModelId(final Integer iStateModelId) {
		final List<UsageCriteria> result = new ArrayList<UsageCriteria>();
		
		for (StateModelUsage smo : lstStateUsage) {
			if (iStateModelId.equals(smo.getStateModelId())) {
				result.add(smo.getUsageCriteria());
			}
		}
		
		return result;
	}

}	// class StateModelUsages
