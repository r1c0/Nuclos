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
package org.nuclos.client.statemodel.panels.rights;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.multimap.MultiListHashMap;

public abstract class RightTransfer implements Transferable{
	
	public abstract RoleRights getAllRoleRights();
	
	public abstract RoleRight getRoleRight(Integer role);
	
	public abstract void setAllRoleRights(RoleRights rr);
	
	public abstract void setRoleRight(RoleRight rr);
	
	/**
	 * 
	 * holder for all role rights
	 */
	public static class RoleRights {
		public final Map<Integer, RoleRight> rights = new HashMap<Integer, RightTransfer.RoleRight>();
		public final Set<Integer> mandatoryFields = new HashSet<Integer>();
		public final Set<Pair<String, String>> mandatoryColumns = new HashSet<Pair<String, String>>();
		public final Set<Integer> rightsEnabled = new HashSet<Integer>();
	}
	
	/**
	 * 
	 * holder for rights from one role
	 */
	public static class RoleRight {
		public final Map<Integer, Boolean> groupRights = new HashMap<Integer, Boolean>();
		public final Map<Integer, Boolean> attributeRights = new HashMap<Integer, Boolean>();
		/** for saving to statevo: */
		public final Set<Integer> groupIsSubform = new HashSet<Integer>();
		public final Map<Integer, String> groupNames = new HashMap<Integer, String>();
		public final MultiListHashMap<Integer, Pair<Integer, String>> subformColumns = new MultiListHashMap<Integer, Pair<Integer, String>>();
	}
	
	public static class OneRoleRightsDataFlavor extends DataFlavor {
		public final static OneRoleRightsDataFlavor flavor = new OneRoleRightsDataFlavor();
		
		public OneRoleRightsDataFlavor() {
			super(RoleRight.class, "OneRoleRights");
		}
	}
	
	public static class AllRoleRightsDataFlavor extends DataFlavor {
		public final static AllRoleRightsDataFlavor flavor = new AllRoleRightsDataFlavor();
		
		public AllRoleRightsDataFlavor() {
			super(RoleRights.class, "AllRoleRights");
		}
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{RightTransfer.AllRoleRightsDataFlavor.flavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor instanceof RightTransfer.AllRoleRightsDataFlavor;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return getAllRoleRights();
	}
}
