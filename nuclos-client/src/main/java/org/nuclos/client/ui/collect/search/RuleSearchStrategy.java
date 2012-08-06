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

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.rule.RuleCache;
import org.nuclos.client.rule.admin.CollectableRule;
import org.nuclos.client.rule.admin.RuleCollectController;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.collection.CollectionUtils;

public class RuleSearchStrategy extends CollectSearchStrategy<CollectableRule> {

	public RuleSearchStrategy() {
	}

	@Override
	public void search() {
		final RuleCollectController cc = getRuleCollectController();
		final MainFrameTab mft = cc.getTab();
		try {
			mft.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			List<CollectableRule> result = CollectionUtils.transform(RuleCache.getInstance().getAllRules(),
					new CollectableRule.MakeCollectable());
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

	private RuleCollectController getRuleCollectController() {
		return (RuleCollectController) getCollectController();
	}

}
