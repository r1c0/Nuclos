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
package org.nuclos.client.ui.collect.search;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.relation.EntityRelationShipCollectController;
import org.nuclos.client.relation.EntityRelationshipModel;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class EntityRelationShipSearchStrategy extends CollectSearchStrategy<EntityRelationshipModel> {

	public EntityRelationShipSearchStrategy() {
	}

	@Override
	public void search() {
		final EntityRelationShipCollectController cc = getEntityRelationShipCollectController();
		final MainFrameTab mft = cc.getMainFrameTab();
		try {
			mft.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Collection<MasterDataVO> colVO = MasterDataDelegate.getInstance().getMasterData(
					NuclosEntity.ENTITYRELATION.getEntityName());

			List<EntityRelationshipModel> lstModel = new ArrayList<EntityRelationshipModel>();
			for (MasterDataVO vo : colVO) {
				EntityRelationshipModel model = cc.findCollectableById(NuclosEntity.ENTITYRELATION.getEntityName(), vo.getId());
				lstModel.add(model);
			}

			cc.fillResultPanel(lstModel);
		} catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(mft, null, ex);
		} finally {
			mft.setCursor(Cursor.getDefaultCursor());
		}
	}

	private EntityRelationShipCollectController getEntityRelationShipCollectController() {
		return (EntityRelationShipCollectController) getCollectController();
	}

}
