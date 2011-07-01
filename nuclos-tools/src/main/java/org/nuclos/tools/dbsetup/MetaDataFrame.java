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

import java.util.Collection;

import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTreeTable;
import org.nuclos.server.dblayer.structure.DbArtifact;

@SuppressWarnings("serial")
public class MetaDataFrame extends JXFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JXTreeTable treeTable;
	
	private final MetaDataTreeTableModel metaDataModel;

	public MetaDataFrame(String title, Collection<? extends DbArtifact> artifacts) {
		super("Nuclos DbSetupTool - " + title, false);
		
		this.metaDataModel = new MetaDataTreeTableModel(artifacts);
		
		treeTable = new JXTreeTable();
		treeTable.setTreeTableModel(metaDataModel);
		
		getContentPane().add(new JScrollPane(treeTable));
	}
}
