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
package org.nuclos.client.explorer.node.rule;

import java.util.Collection;
import java.util.List;

import org.nuclos.client.rule.RuleCache;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.server.customcode.valueobject.CodeVO;

public class LibraryTreeNode extends AbstractRuleTreeNode {

	public LibraryTreeNode(String sLabel, String sDescription) {
		super(null, sLabel, sDescription, null, RuleNodeType.DIRECTORY);
	}

	@Override
	public void refresh() {
		setSubNodes(getTreeNodes(RuleCache.getInstance().getAllCodes()));
	}

	@Override
	public boolean isInsertRuleAllowd() {
		return false;
	}

	private static List<CodeTreeNode> getTreeNodes(Collection<CodeVO> codes) {
		return CollectionUtils.transform(codes, new Transformer<CodeVO, CodeTreeNode>() {
			@Override
			public CodeTreeNode transform(CodeVO i) {
				return new CodeTreeNode(i);
			}
		});
	}

}
