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
package org.nuclos.server.transfer.ejb3;

import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for XML Transfer Protocol functions.<br>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Local(XmlExportImportProtocolFacadeLocal.class)
@Remote(XmlExportImportProtocolFacadeRemote.class)
@Transactional(propagation=Propagation.REQUIRES_NEW)
public class XmlExportImportProtocolFacadeBean extends NuclosFacadeBean implements XmlExportImportProtocolFacadeLocal, XmlExportImportProtocolFacadeRemote {

	/**
	 * creates the protocol header for an export/import transaction
	 *
	 * @param sType
	 * @param sUserName
	 * @param dImportDate
	 * @param sImportEntityName
	 * @param sImportFileName
	 * @return id of protocol header
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 */
	@Override
	public Integer writeExportImportLogHeader(String sType, String sUserName, Date dImportDate, String sImportEntityName, String sImportFileName) throws CommonCreateException, CommonPermissionException, CreateException, NuclosBusinessRuleException {
		MasterDataMetaVO mdmvo = getMasterDataFacade().getMetaData(NuclosEntity.IMPORTEXPORT.getEntityName());
		MasterDataVO mdvo = new MasterDataVO(mdmvo, false);

		mdvo.setField("type", sType);
		mdvo.setField("user", sUserName);
		mdvo.setField("date", dImportDate);
		mdvo.setField("entity", sImportEntityName);
		mdvo.setField("filename", sImportFileName);

		return getMasterDataFacade().create(NuclosEntity.IMPORTEXPORT.getEntityName(), mdvo, null).getIntId();
	}

	/**
	 * creates a protocol entry for the given protocol header id
	 *
	 * @param iParentId
	 * @param sMessageLevel
	 * @param sImportAction
	 * @param sImportMessage
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 */
	@Override
	public void writeExportImportLogEntry(Integer iParentId, String sMessageLevel, String sImportAction, String sImportMessage, Integer iActionNumber) throws CommonCreateException, CommonPermissionException, CreateException, NuclosBusinessRuleException {
		if (iParentId == null) {
			return;
		}

		MasterDataMetaVO mdmvo = getMasterDataFacade().getMetaData(NuclosEntity.IMPORTEXPORTMESSAGES.getEntityName());
		MasterDataVO mdvo = new MasterDataVO(mdmvo, false);

		mdvo.setField("importexportId", iParentId);
		mdvo.setField("messagelevel", sMessageLevel);
		mdvo.setField("action", sImportAction);
		mdvo.setField("message", sImportMessage);
		mdvo.setField("actionnumber", iActionNumber);

		getMasterDataFacade().create(NuclosEntity.IMPORTEXPORTMESSAGES.getEntityName(), mdvo, null);
	}

	/**
	 * add content of xml file to protocol header of given id
	 *
	 * @param iParentId
	 * @param file
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws CreateException
	 * @throws CommonCreateException
	 * @throws CommonRemoveException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 */
	@Override
	public void addFile(Integer iParentId, byte[] ba) {
		if (iParentId == null) {
			return;
		}

		try {
			MasterDataMetaVO mdmvo = MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.IMPORTEXPORT.getEntityName());

			DataBaseHelper.execute(DbStatementUtils.updateValues(mdmvo.getDBEntity(), 
				"BLBXMLFILE", ba).where("INTID", iParentId));
		}
		catch (Exception e) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("importexport.protocol.error.adding.file", iParentId));
				//"XML Datei konnte nicht dem XML Transfer Protokoll mit der Id ["+iParentId+"] hinzugef\u00fcgt werden");
		}
	}

	/**
	 * returns the content of the xml file for the given protocol id
	 *
	 * @param iParentId
	 * @return org.nuclos.common2.File
	 */
	@Override
	public org.nuclos.common2.File getFile(Integer iParentId) {
		if (iParentId == null) {
			return null;
		}

		try {
			MasterDataMetaVO mdmvo = MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.IMPORTEXPORT.getEntityName());
			
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<byte[]> query = builder.createQuery(byte[].class);
			DbFrom t = query.from(mdmvo.getDBEntity()).alias(ProcessorFactorySingleton.BASE_ALIAS);			
			query.select(t.column("BLBXMLFILE", byte[].class));
			query.where(builder.equal(t.column("INTID", Integer.class), iParentId));
			
			byte[] ba = null;
			try {
				ba = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
			} catch (DbInvalidResultSizeException ex) {
				// nothing??
			}
			
			return new org.nuclos.common2.File("transfer.zip", ba);			
		}
		catch (Exception e) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("importexport.protocol.error.transfer.file", e));
				//"Fehler bei der \u00dcbertragung der Datei: "+e);
		}
	}
}
