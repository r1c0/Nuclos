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
package org.nuclos.client.explorer;

import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;

import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MasterDataModuleDelegate;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class ProzessWizardPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JSplitPane splitPane;	
	
	JScrollPane scrollTree;	
	JTree treeForcast;
	
	JPanel panelRight;
	JLabel lbSourceModule;
	JLabel lbTargetModule;
	JScrollPane paneSource;
	JScrollPane paneTarget;
	JList lstSource;
	JList lstTarget;
	JTextField tfSource;
	JTextField tfTarget;

	public ProzessWizardPanel()  {
		super();
		
	}
	
	public Collection<MasterDataVO> buildTrees() {
		
		Collection<MasterDataVO> colGeneration = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.GENERATION.getEntityName());
		for(MasterDataVO voGeneration : colGeneration) {
			String sSourceModule = (String)voGeneration.getField("sourceModule");
			String sTargetModule = (String)voGeneration.getField("targetModule");
			Integer iSourceModule = (Integer)voGeneration.getField("sourceModuleId");
			Integer iTargetModule = (Integer)voGeneration.getField("targetModuleId");
			
			MasterDataVO voSourceModule = Modules.getInstance().getModuleById(iSourceModule);
			if(Boolean.TRUE.equals((Boolean)voSourceModule.getField("showrelations"))) {
				continue;
			}
			voSourceModule.setField("showrelations", true);
			
			voSourceModule.setField("treeview", "${"+NuclosEOField.STATE.getMetaData().getField()+"}");
			
			
			
			//@TODO find a better way... modules are no longer supported for writing!
			try {
				MasterDataModuleDelegate.getInstance().update(NuclosEntity.MODULE.getEntityName(), voSourceModule, null);
			}
			catch(CommonBusinessException e) {
				e.printStackTrace();
			}
		}
		return colGeneration;
		
	}

}
