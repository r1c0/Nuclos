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
package org.nuclos.client.ui.collect;

import java.util.Collection;

import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.CommonLocaleDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * common controller for the Search and Details panels.
 */
@Configurable(preConstruction=true)
public abstract class CommonController<Clct extends Collectable> {
	
	private boolean bChangeListenersAdded;
	
	private final CollectController<Clct> cc;
	
	private CommonLocaleDelegate cld;
	
	public CommonController(CollectController<Clct> cc) {
		this.cc = cc;
	}
	
	@Autowired
	void setCommonLocaleDelegate(CommonLocaleDelegate cld) {
		this.cld = cld;
	}
	
	protected CommonLocaleDelegate getCommonLocaleDelegate() {
		return cld;
	}
	
	protected final CollectController<Clct> getCollectController() {
		return cc;
	}

	protected abstract boolean isSearchPanel();

	protected abstract Collection<? extends CollectableComponentModel> getCollectableComponentModels();

	protected abstract CollectableComponentModelListener getCollectableComponentModelListener();

	protected abstract void addAdditionalChangeListeners();

	protected abstract void removeAdditionalChangeListeners();

	/**
	 * @return Have the change listeners for the Details tab been added?
	 */
	protected boolean getChangeListenersAdded() {
		return this.bChangeListenersAdded;
	}

	/**
	 * adds the change listeners
	 * @precondition !this.getChangeListenersAdded()
	 * @postcondition this.getChangeListenersAdded()
	 */
	protected void addChangeListeners() {
		if (this.getChangeListenersAdded()) {
			// TODO don\u00b4t throw an exception yet. CR has to fix another problem. then the exception is the right thing.
			// But what problem?! Should we just try this? UA
			return;//throw new IllegalStateException();
		}
		this.addCollectableComponentModelListeners();
		this.addAdditionalChangeListeners();
		this.bChangeListenersAdded = true;

		assert this.getChangeListenersAdded();
	}

	/**
	 * removes the change listeners for the details tab
	 * @postcondition !this.getChangeListenersAdded()
	 */
	protected void removeChangeListeners() {
		if (this.getChangeListenersAdded()) {
			this.removeCollectableComponentModelListeners();
			this.removeAdditionalChangeListeners();
			this.bChangeListenersAdded = false;
		}

		assert !this.getChangeListenersAdded();
	}

	/**
	 * adds the collectable component model listeners for the Details tab.
	 */
	protected final void addCollectableComponentModelListeners() {
		for (CollectableComponentModel clctcompmodel : this.getCollectableComponentModels()) {
			clctcompmodel.addCollectableComponentModelListener(this.getCollectableComponentModelListener());
		}
	}

	/**
	 * removes the collectable component model listeners for the Details tab.
	 * If no listeners are installed, no listeners will be removed. That's all.
	 */
	protected final void removeCollectableComponentModelListeners() {
		for (CollectableComponentModel clctcompmodel : this.getCollectableComponentModels()) {
			clctcompmodel.removeCollectableComponentModelListener(this.getCollectableComponentModelListener());
		}
	}

}	// class CommonController
