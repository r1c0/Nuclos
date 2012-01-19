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

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * A super class with default implementation for <code>CollectableComponent</code> 
 * like MnemonicCollectablePanel.
 * Created for compatibility purposes. Will be user in LayoutMLParser.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version	01.00.00
 */
public class DelegatingCollectablePanel extends AbstractCollectableComponent {

	private static final Logger log = Logger.getLogger(DelegatingCollectablePanel.class);

	protected CollectableFieldsProvider valueListProvider;

	protected DelegatingCollectablePanel(CollectableEntityField clctef, JComponent comp, boolean bSearchable) {
		super(clctef, comp, bSearchable);
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		// TODO Auto-generated method stub
	}

	@Override
	public CollectableField getFieldFromView()
			throws CollectableFieldFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean needValueListProvider(){
		return false;
	}

	public CollectableFieldsProvider getValueListProvider() {
		return this.valueListProvider;
	}

	public void setValueListProvider(CollectableFieldsProvider clctfsprovider) {
		this.valueListProvider = clctfsprovider;
	}

	/**
	 * refreshes the list of values by asking the value list provider.
	 * If no value list provider was set, the model will be empty.
	 * @throws CommonBusinessException
	 */
	public void refreshValueList() throws CommonBusinessException {
		log.debug("CollectableComboBox.refreshValueList called for field " + this.getFieldName());
		//final Collection<CollectableField> collclctf = (valueListProvider == null) ? Collections.<CollectableField>emptyList() : valueListProvider.getCollectableFields();
		//this.setComboBoxModel(collclctf);
	}
	
	public void setVisibleControl(boolean visible){
		
	}

	public void setVisibleLabel(boolean visible){
		
	}
	
}
