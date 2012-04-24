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
package org.nuclos.client.masterdata;

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.CollectStateListener;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * <code>CollectController</code> for entity "Nuclet".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Maik.Stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class DbSourceCollectController extends MasterDataCollectController {
		
	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public DbSourceCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.DBSOURCE.getEntityName(), tabIfAny);
		this.getCollectStateModel().addCollectStateListener(new CollectStateListener() {
			
			@Override
			public void searchModeLeft(CollectStateEvent ev)
			    throws CommonBusinessException {
			}
			
			@Override
			public void searchModeEntered(CollectStateEvent ev)
			    throws CommonBusinessException {
			}
			
			@Override
			public void resultModeLeft(CollectStateEvent ev)
			    throws CommonBusinessException {
			}
			
			@Override
			public void resultModeEntered(CollectStateEvent ev)
			    throws CommonBusinessException {				
			}
			
			@Override
			public void detailsModeLeft(CollectStateEvent ev)
			    throws CommonBusinessException {
			}
			
			@Override
			public void detailsModeEntered(CollectStateEvent ev)
			    throws CommonBusinessException {
				if (ev.getNewCollectState().isDetailsModeNew()) {
					setCollectableFieldEnable(true);
				} else {
					setCollectableFieldEnable(false);
				}
			}
		});
	}
	
	private void setCollectableFieldEnable(boolean enable) {
		this.getDetailCollectableComponentsFor("dbtype").get(0).setEnabled(enable);
		this.getDetailCollectableComponentsFor("dbobject").get(0).setEnabled(enable);
	}

}	 
