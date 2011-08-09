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
package org.nuclos.client.masterdata.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;

import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * <code>MasterDataVO</code> as a <code>Transferable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class MasterDataVOTransferable implements Transferable {

	private final DataFlavor[] aflavors;
	private final MasterDataVO mdvo;
	private final MasterDataIdAndEntity mdide;
	private final String sText;
	/**
	 * List<MasterDataVOTransferable>
	 */
	private final List<MasterDataIdAndEntity> lstimp;

	public static class MasterDataVODataFlavor extends DataFlavor {
		public MasterDataVODataFlavor(String sEntity) {
			super(MasterDataVO.class, sEntity);
		}
	}	// inner class MasterDataVODataFlavor

	/**
	 * creates a <code>Transferable</code> for a <code>MasterDataVO</code>
	 * @param mdvo
	 */
	public MasterDataVOTransferable(CollectableMasterDataEntity clcte, MasterDataVO mdvo) {
		this.mdvo = mdvo;
		this.lstimp = null;
		final Collectable clct = new CollectableMasterData(clcte, this.mdvo);
		/** @todo this is a lot of overhead for the identifier field... */
		final String sEntityName = clcte.getName();
		this.mdide = new MasterDataIdAndEntity(mdvo.getId(), sEntityName, clct.getIdentifierLabel());
		this.sText = clct.getIdentifierLabel();

		this.aflavors = new DataFlavor[] {
				new MasterDataVODataFlavor(sEntityName),
				MasterDataIdAndEntity.dataFlavor, DataFlavor.stringFlavor
		};
	}

	/**
	 * creates a <code>Transferable</code> for a master data id / entity
	 * @param oId
	 * @param sEntity
	 * @param sText May be null.
	 */
	public MasterDataVOTransferable(Object oId, String sEntity, String sText) {
		this(oId, sEntity, sText, null);
	}

	/**
	 * creates a <code>Transferable</code> for a master data id / entity
	 * @param oId
	 * @param sEntity
	 * @param sText May be null.
	 * @param actRemove may be null
	 */
	public MasterDataVOTransferable(Object oId, String sEntity, String sText, Action actRemove) {
		this.mdvo = null;
		this.mdide = new MasterDataIdAndEntity(oId, sEntity, sText, actRemove);
		this.sText = sText;
		this.lstimp = null;

		final List<DataFlavor> lstFlavors = new LinkedList<DataFlavor>();
		lstFlavors.add(MasterDataIdAndEntity.dataFlavor);
		if (sText != null) {
			lstFlavors.add(DataFlavor.stringFlavor);
		}
		this.aflavors = new DataFlavor[lstFlavors.size()];
		lstFlavors.toArray(this.aflavors);
	}

	public MasterDataVOTransferable(List<MasterDataIdAndEntity> lstimp, String sEntity) {
		this.lstimp = lstimp;
		this.mdvo = null;
		this.sText = null;
		this.mdide = null;

		final List<DataFlavor> lstflavors = new ArrayList<DataFlavor>();
		lstflavors.add(new MasterDataIdAndEntity.DataFlavor());
		if (this.lstimp.size() == 1) {
			lstflavors.add(new MasterDataIdAndEntity.DataFlavor());
		}
		lstflavors.add(DataFlavor.stringFlavor);
		this.aflavors = lstflavors.toArray(new DataFlavor[0]);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		Object result;

		if (flavor instanceof MasterDataVODataFlavor) {
			result = this.mdvo;
		}
		else if (flavor instanceof MasterDataIdAndEntity.DataFlavor) {
			if(this.lstimp != null){
				result = this.lstimp;
			} else {
				result = this.mdide;
			}
		}
		else {
			if (flavor.equals(DataFlavor.stringFlavor)) {
				result = this.sText;
			}
			else {
				throw new UnsupportedFlavorException(flavor);
			}
		}

		return result;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return this.aflavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		final DataFlavor[] flavors = getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}

}	// class MasterDataVOTransferable
