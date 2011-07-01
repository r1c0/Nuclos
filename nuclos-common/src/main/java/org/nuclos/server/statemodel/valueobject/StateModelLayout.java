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

import org.nuclos.common.collection.CollectionUtils;

import java.io.Serializable;
import java.util.*;

/**
 * The layout of a StateModel. Serialized for persistence. Though this is bad style in general,
 * a state model layout contains no critical data.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class StateModelLayout implements Serializable {
	private static final long serialVersionUID = 8097817704354917878L;

	/**
	 * Map<Integer iStateId, StateLayout>
	 */
	private final Map<Integer, StateLayout> states = CollectionUtils.newHashMap();

	/**
	 * Map<Integer iTransitionId, TransitionLayout>
	 */
	private final Map<Integer, TransitionLayout> transitions = CollectionUtils.newHashMap();

	/**
	 * List<NoteLayout>
	 */
	private final List<NoteLayout> notes = new ArrayList<NoteLayout>();

	public StateModelLayout() {
	}

	public StateLayout getStateLayout(Integer iStateId) {
		return this.states.get(iStateId);
	}

	public TransitionLayout getTransitionLayout(Integer iTransitionId) {
		return this.transitions.get(iTransitionId);
	}

	public void insertStateLayout(Integer iStateId, StateLayout layout) {
		this.states.put(iStateId, layout);
	}

	public void insertTransitionLayout(Integer iTransitionId, TransitionLayout layout) {
		this.transitions.put(iTransitionId, layout);
	}

	public void updateState(Integer iStateId, double dX, double dY, double dWidth, double dHeight) {
		final StateLayout statelayout = this.getStateLayout(iStateId);
		if (statelayout != null) {
			statelayout.setX(dX);
			statelayout.setY(dY);
			statelayout.setWidth(dWidth);
			statelayout.setHeight(dHeight);
		}
	}

	public void updateTransition(Integer iTransitionId, int iStart, int iEnd) {
		final TransitionLayout transitionlayout = this.getTransitionLayout(iTransitionId);
		if (transitionlayout != null) {
			transitionlayout.setConnectionStart(iStart);
			transitionlayout.setConnectionEnd(iEnd);
		}
	}

	public void updateStateId(Integer iOldStateId, Integer iNewStateId) {
		final StateLayout statelayout = this.getStateLayout(iOldStateId);
		this.insertStateLayout(iNewStateId, new StateLayout(statelayout.getX(), statelayout.getY(), statelayout.getWidth(), statelayout.getHeight()));
		this.removeState(iOldStateId);
	}

	public void updateTransitionId(Integer iOldTransitionId, Integer iNewTransitionId) {
		final TransitionLayout transitionlayout = this.getTransitionLayout(iOldTransitionId);
		this.insertTransitionLayout(iNewTransitionId, new TransitionLayout(iNewTransitionId, transitionlayout.getConnectionStart(), transitionlayout.getConnectionEnd()));
		this.removeTransition(iOldTransitionId);
	}

	public void removeState(Integer iStateId) {
		this.states.remove(iStateId);
	}

	public void removeTransition(Integer iTransitionId) {
		this.transitions.remove(iTransitionId);
	}

	public List<NoteLayout> getNotes() {
		return this.notes;
	}

}	// class StateModelLayout
