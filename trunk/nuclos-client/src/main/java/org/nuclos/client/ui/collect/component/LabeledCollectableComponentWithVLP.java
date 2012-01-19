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

import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common2.exception.CommonBusinessException;

public abstract class LabeledCollectableComponentWithVLP extends LabeledCollectableComponent implements Parameterisable, CollectableComponentWithValueListProvider {

	protected CollectableFieldsProvider valueListProvider;

	protected LabeledCollectableComponentWithVLP(CollectableEntityField clctef,
        LabeledComponent labcomp, boolean bSearchable) {
	    super(clctef, labcomp, bSearchable);
    }

	/**
	 * sets the value list provider that provides the dropdown values for this combobox.
	 * @param clctfsprovider
	 */
	@Override
    public void setValueListProvider(CollectableFieldsProvider clctfsprovider) {
		valueListProvider = clctfsprovider;
	}

	/**
	 * @return the value list provider that provides the dropdown values for this combobox.
	 */
	@Override
    public CollectableFieldsProvider getValueListProvider() {
		return valueListProvider;
	}

	/**
	 * refreshes the list of values by asking the value list provider.
	 * If no value list provider was set, the model will be empty.
	 */
	@Override
	public abstract void refreshValueList(boolean async);

	@Override
	public void refreshValueList() {
		refreshValueList(false);
	}

	@Override
    public void setParameter(String sName, Object oValue) {
		getValueListProvider().setParameter(sName, oValue);
	}

	@Override
    public void applyParameters() throws CommonBusinessException {
		refreshValueList(false);
	}
}
