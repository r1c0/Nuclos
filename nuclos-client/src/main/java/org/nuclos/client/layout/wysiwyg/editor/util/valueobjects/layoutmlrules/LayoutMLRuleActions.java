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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules;

import java.io.Serializable;
import java.util.Vector;

/**
 * This class holds all {@link LayoutMLRuleAction} Objects.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class LayoutMLRuleActions implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** here are the LayoutMLRuleAction Objects stored */
	private Vector<LayoutMLRuleAction> allActions = new Vector<LayoutMLRuleAction>(1);

	/**
	 * @param layoutMLRuleAction the {@link LayoutMLRuleAction} to add
	 */
	public void addAction(LayoutMLRuleAction layoutMLRuleAction) {
		this.allActions.add(layoutMLRuleAction);
	}

	/**
	 * @return all {@link LayoutMLRuleAction} contained in this {@link LayoutMLRuleActions} Objects
	 */
	public Vector<LayoutMLRuleAction> getSingleActions() {
		return this.allActions;
	}
	
	/**
	 * @param layoutMLRuleAction that is to be checked for existence
	 * @return true if this {@link LayoutMLRuleAction} is contained
	 */
	public boolean doesContainAction(LayoutMLRuleAction layoutMLRuleAction){
		return allActions.contains(layoutMLRuleAction);
	}
	
	/**
	 * Method to remove a {@link LayoutMLRuleAction}
	 * @param layoutMLRuleAction the {@link LayoutMLRuleAction} to be removed
	 */
	public void removeActionFromActions(LayoutMLRuleAction layoutMLRuleAction){
		if (doesContainAction(layoutMLRuleAction))
			this.allActions.remove(layoutMLRuleAction);
			
	}
	
	/**
	 * Overwritten Method clone to create a new Instance of this Object<br>
	 * Calls {@link LayoutMLRuleAction#clone()}
	 * 
	 * @return new Instance of this Object
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		LayoutMLRuleActions layoutMLRuleActions = new LayoutMLRuleActions();

		for (LayoutMLRuleAction action : allActions) {
			layoutMLRuleActions.addAction((LayoutMLRuleAction)action.clone());
		}
		
		return layoutMLRuleActions;
	}

	/**
	 * Overwritten equals Method to check if the provided {@link LayoutMLRuleActions} contains the same Values
	 * 
	 * @param obj the Object to compare to this Object
	 * @return true if the content is equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LayoutMLRuleActions))
			return false;
		
		LayoutMLRuleActions actions = (LayoutMLRuleActions)obj;
				
		if (actions.allActions.size() != this.allActions.size())
			return false;
		
		if (!actions.getSingleActions().equals(allActions))
			return false;
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		int counter = 1;
		for (LayoutMLRuleAction singleAction: this.getSingleActions()){
			sb.append("Action No. " +counter+++ "\n");
			sb.append(singleAction.toString());
		}
		
		return sb.toString();
	}
	
	
}
