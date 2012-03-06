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
import java.util.List;

import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.datasource.admin.AbstractDatasourceCollectController;
import org.nuclos.client.datasource.admin.CollectableDataSource;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.collection.CollectionUtils;

public class RecordGrantSearchStrategy extends CollectSearchStrategy<CollectableDataSource> {

	private final DatasourceDelegate datasourcedelegate = DatasourceDelegate.getInstance();

	public RecordGrantSearchStrategy() {
	}

	@Override
	public void search() {
		final AbstractDatasourceCollectController cc = getAbstractDatasourceCollectController();
		final MainFrameTab mft = cc.getTab();
		try {
			mft.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			List<CollectableDataSource> result = CollectionUtils.transform(this.datasourcedelegate.getAllRecordGrant(),
					new CollectableDataSource.MakeCollectable());
			if (getCollectableIdListCondition() != null) {
				result = CollectionUtils.applyFilter(result, new CollectableIdPredicate(getCollectableIdListCondition().getIds()));
			}
			cc.fillResultPanel(result);
		} catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(mft, null, ex);
		} finally {
			mft.setCursor(Cursor.getDefaultCursor());
		}
	}

	private AbstractDatasourceCollectController getAbstractDatasourceCollectController() {
		return (AbstractDatasourceCollectController) getCollectController();
	}

}
