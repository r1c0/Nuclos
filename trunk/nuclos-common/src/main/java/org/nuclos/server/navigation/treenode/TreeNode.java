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
package org.nuclos.server.navigation.treenode;

import org.nuclos.common2.exception.CommonFinderException;
import java.io.Serializable;
import java.util.List;

/**
 * A generic tree node.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public interface TreeNode extends Serializable {

	/**
	 * @return the unique identifier, if any, of the object represented by this node.
	 */
	Object getId();

	/**
	 * @return the entity name of the object represented by this node, if applicable.
	 */
	String getEntityName();

	/**
	 * @return the label of this node, which is usually displayed to the user.
	 * @postcondition result != null
	 */
	String getLabel();

	/**
	 * @return the description, if any, of this node, which is usually displayed to the user as a tooltip.
	 */
	String getDescription();

	String getIdentifier();

	/**
	 * @return the list of sub nodes. If this.hasSubNodes() == null, the sub nodes haven't been loaded yet,
	 *   and they will be loaded here.
	 * @postcondition result != null
	 * @postcondition this.hasSubNodes() != null
	 * @see #hasSubNodes()
	 */
	List<? extends TreeNode> getSubNodes();

	/**
	 * @return Does this node have sub nodes? <code>null</code> means: "unknown - the sub nodes haven't been loaded yet".
	 */
	Boolean hasSubNodes();

	/**
	 * @postcondition this.hasSubNodes() == null
	 */
	void removeSubNodes();

	/**
	 * refreshes this node's contents.
	 * @deprecated use refreshed() instead.
	 * @see #refreshed()
	 * @see #implementsNewRefreshMethod()
	 */
	@Deprecated
	void refresh();

	/**
	 * @return Does this node implement the new refresh method (called "refreshed")? If so, it will be used,
	 * otherwise, the old refresh() method will be used for compatibility.
	 * @see #refreshed()
	 */
	boolean implementsNewRefreshMethod();

	/**
	 * @return a copy of this node containing the current version of the represented object.
	 * @postcondition result != null
	 * @throws CommonFinderException if the object represented by this node no longer exists.
	 */
	TreeNode refreshed() throws CommonFinderException;

	/**
	 * @return Does this node need a parent? If a node needs a parent, it cannot be the root node of a tree.
	 */
	boolean needsParent();

}	// class TreeNode
