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

package org.nuclos.tools.dbsetup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.util.ArtifactToStringVisitor;

@SuppressWarnings("serial")
public class MetaDataTreeTableModel extends AbstractTreeTableModel {

	private final String rootDummy;
	private final List<DbArtifact> artifacts;

	public MetaDataTreeTableModel(Collection<? extends DbArtifact> artifacts) {
		this.rootDummy = new String("Metadata");
		this.artifacts = new ArrayList<DbArtifact>(artifacts);
	}

	@Override
	public Object getRoot() {
		return rootDummy;
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(Object node, int column) {
		if (node instanceof DbArtifact) {
			DbArtifact a = (DbArtifact) node;
			switch (column) {
			case 0:
				return a.accept(new ArtifactToStringVisitor());
			case 1:
				return a.accept(new ArtifactDetailsToStringVisitor());
			}
		}
		return null;
	}

	@Override
	public Object getChild(Object parent, int index) {
		List<? extends DbArtifact> childList = getChildList(parent);		
		return (childList != null) ? childList.get(index) : null;
	}

	@Override
	public int getChildCount(Object parent) {
		List<? extends DbArtifact> childList = getChildList(parent);		
		return (childList != null) ? childList.size() : 0;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		List<? extends DbArtifact> childList = getChildList(parent);		
		return (childList != null) ? childList.indexOf(child) : -1;
	}
	
	@SuppressWarnings("unchecked")
	private List<? extends DbArtifact> getChildList(Object parent) {
		if (parent == rootDummy) {
			return this.artifacts;
		} else if (parent instanceof List<?>) {
			return (List<? extends DbArtifact>) parent;
		} else if (parent instanceof DbTable) {
			return ((DbTable) parent).getTableArtifacts();
		}
		return null;
	}
}