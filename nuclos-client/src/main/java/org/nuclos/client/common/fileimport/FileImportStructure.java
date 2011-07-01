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
package org.nuclos.client.common.fileimport;

import java.util.List;
import java.util.Vector;

import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * This class represents an import structure definition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class FileImportStructure {
	private String sTable;
	private String sKeyfield;
	private String sQualifier;
	private String sDelimiter;
	private Integer iHeaderlines;
	private List<FileImportStructureItem> lstFileImportStructureItems;

	public FileImportStructure(Integer iId) {
		try {
			final MasterDataDelegate mddelegate = MasterDataDelegate.getInstance();

			final MasterDataVO mdvoStructure = mddelegate.get(NuclosEntity.IMPORT.getEntityName(), iId);
			final MasterDataVO mdvoImportTarget = mddelegate.get("importtarget", mdvoStructure.getField("importtargetId"));
			this.sTable = (String) mdvoImportTarget.getField("table");
			this.sKeyfield = (String) mdvoImportTarget.getField("keyfield");
			this.sQualifier = (String) mdvoStructure.getField("qualifier");
			this.sDelimiter = (String) mdvoStructure.getField("delimiter");
			this.iHeaderlines = (Integer) mdvoStructure.getField("headerlines");

			this.lstFileImportStructureItems = new Vector<FileImportStructureItem>();
			/** @todo What the...?!? */
			for (int i = 0; i < 999; i++) {
				this.lstFileImportStructureItems.add(i, null);
			}

			for (EntityObjectVO mdvoStructureItem : mddelegate.getDependantMasterData("importfield", "import", iId)) {
				final Integer iIndex = mdvoStructureItem.getField("fieldcolumn", Integer.class);
				
				EntityObjectVO mdvoImportTargetField = null;
				for(EntityObjectVO mdvo : mddelegate.getDependantMasterData("importtargetfield", "importtarget", mdvoStructure.getField("importtargetId"))) {
				  if(mdvo.getId().equals(mdvoStructureItem.getField("importtargetfieldId", Long.class))) {
					  mdvoImportTargetField = mdvo;
					  break;
				  }
				}
				if(mdvoImportTargetField == null) {
					throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("", mdvoStructureItem.getField("importtargetfieldId", Integer.class)));//"Der Datensatz mit der Id " + mdvoStructureItem.getField("importtargetfieldId") +
							//" konnte in der Entit\u00e4t importtargetfield nicht gefunden werden.");
					
				}
				final String sFieldName = mdvoImportTargetField.getField("field", String.class);
				final String sClassName = mdvoImportTargetField.getField("datatype", String.class);
				final String sFormat = mdvoStructureItem.getField("parsestring", String.class);
				this.lstFileImportStructureItems.set(iIndex, new FileImportStructureItem(sFieldName, sClassName, sFormat));
			}
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalException(ex.getMessage(), ex);
		}
	}

	public String getTable() {
		return sTable;
	}

	public String getKeyfield() {
		return sKeyfield;
	}

	public String getQualifier() {
		return sQualifier;
	}

	public String getDelimiter() {
		return sDelimiter;
	}

	public Integer getHeaderlines() {
		return iHeaderlines;
	}

	public List<FileImportStructureItem> getItems() {
		return lstFileImportStructureItems;
	}
}
