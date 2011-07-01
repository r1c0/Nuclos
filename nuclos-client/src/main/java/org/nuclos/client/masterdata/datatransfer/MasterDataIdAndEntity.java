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

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.nuclos.common2.LangUtils;

/**
 * a leased object id and a module.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class MasterDataIdAndEntity {
	private final Object oId;
	private final String sEntity;
	private final String sLabel;
	
	private final Action actRemove;
	
	/**
	 * @param oId
	 * @param sEntity
	 * @param sLabel an identifying label
	 */
	public MasterDataIdAndEntity(Object oId, String sEntity, String sLabel) {
		this(oId, sEntity, sLabel, null);
	}

	/**
	 * @param oId
	 * @param sEntity
	 * @param sLabel an identifying label
	 * @prarm actRemove may be null
	 */
	public MasterDataIdAndEntity(Object oId, String sEntity, String sLabel, Action actRemove) {
		this.oId = oId;
		this.sEntity = sEntity;
		this.sLabel = sLabel;
		this.actRemove = actRemove;
	}

	public static class DataFlavor extends java.awt.datatransfer.DataFlavor {
		public DataFlavor(String sEntity) {
			super(MasterDataIdAndEntity.class, sEntity);
		}

		/**
		 * Note that we have to override equals(DataFlavor) rather than equals(Object) here.
		 * @param that
		 * @return
		 */
		@Override
		public boolean equals(java.awt.datatransfer.DataFlavor that) {
			final boolean result;
			if (!super.equals(that)) {
				result = false;
			}
			else {
				result = LangUtils.equals(this.getHumanPresentableName(), that.getHumanPresentableName());
			}
			return result;
		}

		@Override
		public int hashCode() {
			// super.hashCode() is sufficient.
			return super.hashCode();
		}
	}

	public Object getId() {
		return this.oId;
	}

	public String getEntity() {
		return this.sEntity;
	}

	public String getLabel() {
		return this.sLabel;
	}
	
	public void removeFromSourceTree() {
		if (actRemove != null)
			actRemove.actionPerformed(new ActionEvent(this, -1, "removeFromSourceTree"));
	}

}	// class MasterDataIdAndEntity
