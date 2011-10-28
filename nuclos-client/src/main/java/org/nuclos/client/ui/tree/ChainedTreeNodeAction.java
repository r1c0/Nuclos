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
package org.nuclos.client.ui.tree;

import org.apache.log4j.Logger;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * <code>TreeNodeAction</code> that wraps a regular <code>Action</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class ChainedTreeNodeAction extends TreeNodeAction {

	private final Action act;

	public ChainedTreeNodeAction(String sActionCommand, String sName, Action act, JTree tree) {
		super(sActionCommand, sName, tree);
		this.act = act;
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		Logger.getLogger(ChainedTreeNodeAction.class).debug("ChainedTreeNodeAction.actionPerformed");
		act.actionPerformed(new ActionEvent(this.getJTree(), ev.getID(), (String) act.getValue(Action.ACTION_COMMAND_KEY),
				ev.getWhen(), ev.getModifiers()));
	}

}  // class ChainedTreeNodeAction
