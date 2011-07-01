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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;

/**
 * Default implementation of CollectableComponentsProvider using a <code>MultiListMap</code> internally.
 * Maps a field name to the <code>CollectableComponent</code>s associated with that field name.
 * In a dialog, there may be more than one <code>CollectableComponent</code>s (views) for a single field (model),
 * especially when they occur in different tabs.
 * In general, the <code>CollectableComponent</code>s associated with the same field share a common <code>CollectableComponentModel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class DefaultCollectableComponentsProvider implements CollectableComponentsProvider {

	private final MultiListMap<String, CollectableComponent> mmp;
	
	//NUCLEUSINT-442
	private final List<CollectableComponent> clcLabels;

	/**
	 * creates an empty provider.
	 * @postcondition this.isEmpty()
	 */
	public DefaultCollectableComponentsProvider() {
		this(new MultiListHashMap<String, CollectableComponent>());
	}

	/**
	 * creates a provider out of a multi list map.
	 * @param mmp
	 */
	public DefaultCollectableComponentsProvider(MultiListMap<String, CollectableComponent> mmp) {
		super();
		this.mmp = mmp;
		this.clcLabels = new ArrayList<CollectableComponent>();
	}

	/**
	 * creates a provider containing the given components.
	 * @param aclctcomp
	 * @precondition aclctcomp != null
	 */
	public DefaultCollectableComponentsProvider(CollectableComponent... aclctcomp) {
		this();

		for (CollectableComponent clctcomp : aclctcomp) {
			this.addCollectableComponent(clctcomp);
		}
	}

	/**
	 * adds the given component to this provider.
	 * @param clctcomp
	 * @precondition clctcomp != null
	 * @postcondition this.getCollectableComponentsFor(clctcomp.getFieldName()).contains(clctcomp)
	 */
	public void addCollectableComponent(CollectableComponent clctcomp) {
		this.mmp.addValue(clctcomp.getFieldName(), clctcomp);

		assert this.getCollectableComponentsFor(clctcomp.getFieldName()).contains(clctcomp);
	}
	
	/**
	 * adds the given label to this provider.
	 * @param clcLabel
	 * @precondition clcLabel != null
	 * @postcondition this.clcLabel.contains(clcLabel)
	 * NUCLEUSINT-442
	 */
	public void addCollectableLabel(CollectableComponent clcLabel) {
		this.clcLabels.add(clcLabel);

		assert this.clcLabels.contains(clcLabel);
	}

	@Override
	public Collection<CollectableComponent> getCollectableComponents() {
		return this.mmp.getAllValues();
	}
	
	//NUCLEUSINT-442
	@Override
	public Collection<CollectableComponent> getCollectableLabels(){
		return this.clcLabels;
	}

	@Override
	public Collection<CollectableComponent> getCollectableComponentsFor(String sFieldName) {
		return this.mmp.getValues(sFieldName);
	}

}	// class DefaultCollectableComponentsProvider
