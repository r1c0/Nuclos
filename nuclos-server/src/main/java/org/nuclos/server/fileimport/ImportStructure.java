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
package org.nuclos.server.fileimport;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.genericobject.GenericObjectImportUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.fileimport.CommonParseException;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.common2.fileimport.parser.FileImportParserFactory;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Class representing an invoice structure definition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version	00.01.000
 */
public class ImportStructure {

	public static final int INSERT = 1;
	public static final int UPDATE = 2;
	public static final int DELETE = 4;

	private final FileImportParserFactory parserfactory = FileImportParserFactory.getInstance();

	private final String name;
	private final String sDelimiter;
	private final String sEncoding;
	private final Integer iHeaderLineCount;
	private final boolean bInsert;
	private final boolean bUpdate;
	private final boolean bDelete;

	private final String entityname;
	private final Integer iEntityId;

	private Map<String, Item> items;

	private final Set<String> stIdentifiers = new HashSet<String>();

	public ImportStructure(Integer iImportStructureId) {
		try {
			final MasterDataFacadeLocal mdfacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);

			final MasterDataVO mdcvoStructure = mdfacade.get(NuclosEntity.IMPORT.getEntityName(), iImportStructureId);

			this.name = (String) mdcvoStructure.getField("name");
			this.sDelimiter = (String) mdcvoStructure.getField("delimiter");
			this.sEncoding = (String) mdcvoStructure.getField("encoding");
			this.iHeaderLineCount = (Integer) mdcvoStructure.getField("headerlines");
			this.bInsert = (Boolean) mdcvoStructure.getField("insert");
			this.bUpdate = (Boolean) mdcvoStructure.getField("update");
			this.bDelete = (Boolean) mdcvoStructure.getField("delete");

			this.iEntityId = (Integer) mdcvoStructure.getField("entityId");

			this.items = new HashMap<String, Item>();
			final Collection<MasterDataVO> collAttributes = mdfacade.getDependantMasterData(NuclosEntity.IMPORTATTRIBUTE.getEntityName(), "import", iImportStructureId);

			final String entityname = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(this.iEntityId)).getEntity();
			this.entityname = entityname;
			final boolean isModule = Modules.getInstance().existModule(entityname);

			for (MasterDataVO mdcvo : collAttributes) {
				final Integer iIndex = (Integer) mdcvo.getField("fieldcolumn");
				final String attribute = (String) mdcvo.getField("attribute");
				final String format = (String) mdcvo.getField("parsestring");
				final boolean preserve = (Boolean) mdcvo.getField("preserve");
				final NuclosScript script = (NuclosScript) mdcvo.getField("script");
				final Class<?> clazz;

				String foreignentity = null;
				if (isModule) {
					AttributeCVO meta = AttributeCache.getInstance().getAttribute(entityname, attribute);
					clazz = meta.getJavaClass();
					foreignentity = meta.getExternalEntity();
				}
				else {
					MasterDataMetaFieldVO meta = MasterDataMetaCache.getInstance().getMetaData(entityname).getField(attribute);
					clazz = meta.getJavaClass();
					foreignentity = meta.getForeignEntity();
				}

				final Set<ForeignEntityIdentifier> foreignentityattributes = new HashSet<ImportStructure.ForeignEntityIdentifier>();

				if (foreignentity != null
					&& !attribute.equals(NuclosEOField.PROCESS.getMetaData().getField())
					&& !attribute.equals(NuclosEOField.STATE.getMetaData().getField())) {
					final boolean isFeModule = Modules.getInstance().existModule(foreignentity);

					final Collection<MasterDataVO> collForeignEntityIdentifiers = mdfacade.getDependantMasterData(NuclosEntity.IMPORTFEIDENTIFIER.getEntityName(), "importattribute", mdcvo.getIntId());

					for (MasterDataVO mdvo : collForeignEntityIdentifiers) {
						final Integer iColumn = (Integer) mdvo.getField("fieldcolumn");
						final String feattribute = (String) mdvo.getField("attribute");
						final String feformat = (String) mdvo.getField("parsestring");
						final Class<?> feclazz;

						if (isFeModule) {
							AttributeCVO meta = AttributeCache.getInstance().getAttribute(foreignentity, feattribute);
							feclazz = meta.getJavaClass();
						}
						else {
							MasterDataMetaFieldVO meta = MasterDataMetaCache.getInstance().getMetaData(foreignentity).getField(feattribute);
							feclazz = meta.getJavaClass();
						}

						foreignentityattributes.add(new ForeignEntityIdentifier(iColumn, foreignentity, feattribute, feclazz, feformat));
					}
				}
				else {
					foreignentity = null;
				}

				this.items.put(attribute, new ImportStructure.Item(iIndex, entityname, attribute, clazz, format, preserve, foreignentity, foreignentityattributes, script));
			}

			final Collection<MasterDataVO> collIdentifiers = mdfacade.getDependantMasterData(NuclosEntity.IMPORTIDENTIFIER.getEntityName(), "import", iImportStructureId);
			for (final MasterDataVO mdcvo : collIdentifiers) {
				this.stIdentifiers.add((String) mdcvo.getField("attribute"));
			}
		}
		catch (CommonPermissionException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public String getName() {
		return this.name;
	}

	public String getDelimiter() {
		return this.sDelimiter;
	}

	public String getEncoding() {
		return this.sEncoding;
	}

	public Integer getHeaderLineCount() {
		return this.iHeaderLineCount;
	}

	public Map<String, Item> getItems() {
		return this.items;
	}

	public Set<String> getIdentifiers() {
		return this.stIdentifiers;
	}

	public boolean isInsert() {
		return this.bInsert;
	}

	public boolean isUpdate() {
		return this.bUpdate;
	}

	public boolean isDelete() {
		return this.bDelete;
	}

	public String getEntityName() {
		return this.entityname;
	}

	public Integer getEntityId() {
		return this.iEntityId;
	}

	public Set<String> getForbiddenAttributeNames(ParameterProvider paramProvider) {
		return GenericObjectImportUtils.getForbiddenAttributeNames(paramProvider, isUpdate());
	}

	public int getImportSettings() {
		int result = 0;
		if (isInsert()) result |= INSERT;
		if (isUpdate()) result |= UPDATE;
		if (isDelete()) result |= DELETE;
		return result;
	}

	public class Item {
		private final Integer iColumn;
		private final String sEntityName;
		private final String sFieldName;
		private final Class<?> cls;
		private final String sFormat;
		private final boolean bPreserve;
		private final String foreignentity;
		private final Set<ForeignEntityIdentifier> feIdentifier;
		private final NuclosScript script;

		public Item(Integer iColumn, String sEntityName, String sFieldName, Class<?> cls, String sFormat, boolean bPreserve, String foreignentity, Set<ForeignEntityIdentifier> feIdentifier, NuclosScript script) {
			this.iColumn = iColumn;
			this.sFieldName = sFieldName;
			this.sEntityName = sEntityName;
			this.cls = cls;
			this.sFormat = sFormat;
			this.bPreserve = bPreserve;
			this.foreignentity = foreignentity;
			this.feIdentifier = feIdentifier;
			this.script = script;
		}

		public Integer getColumn() {
			return this.iColumn;
		}

		public String getFieldName() {
			return this.sFieldName;
		}

		public String getEntityName() {
			return this.sEntityName;
		}

		public boolean isPreserve() {
			return this.bPreserve;
		}

		public boolean isReferencing() {
			return !StringUtils.isNullOrEmpty(foreignentity);
		}

		public String getForeignEntityName() {
			return this.foreignentity;
		}

		public Set<ForeignEntityIdentifier> getForeignEntityIdentifiers() {
			return this.feIdentifier;
		}

		public NuclosScript getScript() {
			return script;
		}

		public Object parse(String sValue) throws NuclosFileImportException {
			try {
				if (!StringUtils.looksEmpty(sValue)) {
					return parserfactory.parse(this.cls, sValue, this.sFormat);
				}
				else {
					return null;
				}
			}
			catch (CommonParseException ex) {
				throw new NuclosFileImportException(StringUtils.getParameterizedExceptionMessage("import.structure.exception", this.getFieldName()), ex);
			}
		}
	}	// inner class Item


	public class ForeignEntityIdentifier {
		private final Integer iColumn;
		private final String sEntityName;
		private final String sFieldName;
		private final Class<?> cls;
		private final String sFormat;

		public ForeignEntityIdentifier(Integer iColumn, String sEntityName, String sFieldName, Class<?> cls, String sFormat) {
			this.iColumn = iColumn;
			this.sFieldName = sFieldName;
			this.sEntityName = sEntityName;
			this.cls = cls;
			this.sFormat = sFormat;
		}

		public Integer getColumn() {
			return this.iColumn;
		}

		public String getFieldName() {
			return this.sFieldName;
		}

		public String getEntityName() {
			return this.sEntityName;
		}

		public Object parse(String sValue) throws NuclosFileImportException {
			try {
				if (!StringUtils.looksEmpty(sValue)) {
					return parserfactory.parse(this.cls, sValue, this.sFormat);
				}
				else {
					return null;
				}
			}
			catch (CommonParseException ex) {
				throw new NuclosFileImportException(StringUtils.getParameterizedExceptionMessage("import.structure.exception", this.getFieldName()), ex);
			}
		}
	}
}  // class ImportStructure
