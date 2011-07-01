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
package org.nuclos.client.genericobject.datatransfer;

import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.genericobject.CollectableGenericObject;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collection.Predicate;

/**
 * A generic object id together with its module and process (if any).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GenericObjectIdModuleProcess {

	private final int iGenericObjectId;
	private final int iModuleId;
	private final String sGenericObjectIdentifier;
	private final Integer iProcessId;
	private final String sContents;

	public GenericObjectIdModuleProcess(int iGenericObjectId, int iModuleId, Integer iProcessId, String sGenericObjectIdentifier) {
		this.iGenericObjectId = iGenericObjectId;
		this.iModuleId = iModuleId;
		this.iProcessId = iProcessId;
		this.sGenericObjectIdentifier = sGenericObjectIdentifier;
		this.sContents = sGenericObjectIdentifier;
	}

	/**
	 * @param clctlo
	 * @param sContents a String representation compatible with Excel (optional).
	 */
	public GenericObjectIdModuleProcess(CollectableGenericObject clctlo, String sGenericObjectIdentifier, String sContents) {
		this.iGenericObjectId = clctlo.getId();
		this.iModuleId = clctlo.getGenericObjectCVO().getModuleId();

		final ParameterProvider paramprovider = ClientParameterProvider.getInstance();
		final String sFieldNameProcess = NuclosEOField.PROCESS.getMetaData().getField();
		if (CollectableGenericObjectEntity.getByModuleId(this.iModuleId).getFieldNames().contains(sFieldNameProcess)) {
			this.iProcessId = (Integer) clctlo.getField(sFieldNameProcess).getValueId();
		}
		else {
			this.iProcessId = null;
		}

		final String sFieldNameIdentifier = NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField();
		this.sGenericObjectIdentifier = (sGenericObjectIdentifier == null? (String) clctlo.getValue(sFieldNameIdentifier) : sGenericObjectIdentifier);
		this.sContents = sContents;
	}

	public static class DataFlavor extends java.awt.datatransfer.DataFlavor {
		public DataFlavor() {
			super(GenericObjectIdModuleProcess.class, "generic object");
		}
	}

	/**
	 * @return the id of this generic object.
	 */
	public int getGenericObjectId() {
		return this.iGenericObjectId;
	}

	/**
	 * @return the id of the module this generic object belongs to.
	 */
	public int getModuleId() {
		return this.iModuleId;
	}

	/**
	 * @return the process id (if any).
	 */
	public Integer getProcessId() {
		return this.iProcessId;
	}

	/**
	 * @return the human readable identifier
	 */
	public String getGenericObjectIdentifier() {
		return this.sGenericObjectIdentifier;
	}

	/**
	 * @return a String representation compatible with Excel so we can copy & paste generic objects to Excel.
	 */
	@Override
	public String toString() {
		return this.sContents;
	}

	public static class HasModuleId implements Predicate<GenericObjectIdModuleProcess> {
		private final int iModuleId;

		public HasModuleId(int iModuleId) {
			this.iModuleId = iModuleId;
		}

		@Override
		public boolean evaluate(GenericObjectIdModuleProcess goimp) {
			return goimp.getModuleId() == iModuleId;
		}
	}

}	// class GenericObjectIdModuleProcess
