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
package org.nuclos.server.dbtransfer;

import java.util.List;

import javax.ejb.Remote;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.dbtransfer.Transfer;
import org.nuclos.common.dbtransfer.TransferNuclet;
import org.nuclos.common.dbtransfer.TransferOption;

@Remote
public interface TransferFacadeRemote {

	/**
	 * creates a file for configuration transfer.
	 *
	 * @exportOptions table filters
	 * @return the file content as byte array
	 * @throws NuclosBusinessException 
	 */
	public byte[] createTransferFile(Long nucletId, TransferOption.Map exportOptions) throws NuclosBusinessException;

	/**
	 * @param bytes the content of a transfer file
	 * @return a <code>Transfer</code> object describing how the
	 * current configuration would change if the transfer is executed
	 * @throws NuclosBusinessException 
	 */
	public Transfer prepareTransfer(boolean isNuclon, byte[] bytes) throws NuclosBusinessException;

	/**
	 * execute a transfer
	 *
	 * @param transfer
	 * @return a message object informing the client about success or failure
	 * @throws NuclosBusinessException 
	 */
	public Transfer.Result runTransfer(Transfer transfer) throws NuclosBusinessException;
	
	public String getDatabaseType();
	
	/**
	 * 
	 * @return
	 */
	public List<TransferNuclet> getAvaiableNuclets();
}
