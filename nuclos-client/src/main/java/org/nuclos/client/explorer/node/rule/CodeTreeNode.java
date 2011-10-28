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

import org.nuclos.server.customcode.valueobject.CodeVO;

public class CodeTreeNode extends AbstractRuleTreeNode {

	private final CodeVO obj;

	public CodeTreeNode(CodeVO obj) {
		super(obj.getId(), obj.getName(), obj.getDescription(), null, RuleNodeType.LIBRARY);
		this.obj = obj;
	}

	@Override
	public void refresh() { }

	@Override
	public boolean isInsertRuleAllowd() {
		return false;
	}

	public boolean isActive() {
		return obj.isActive();
	}

	public CodeVO getCodeVO() {
		return obj;
	}
}
