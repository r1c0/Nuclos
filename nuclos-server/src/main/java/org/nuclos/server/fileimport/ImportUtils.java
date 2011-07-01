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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.csvparser.CSVParser;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.MakeEntityObjectValueIdField;
import org.nuclos.common.fileimport.ImportMode;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.fileimport.ImportStructure.ForeignEntityIdentifier;
import org.nuclos.server.fileimport.ImportStructure.Item;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * Utility class for various static import helper operations.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ImportUtils {

	private ImportUtils() {
	}

	public static void validateImportStructure(MasterDataWithDependantsVO importstructure) throws CommonValidationException {
		importstructure.validate(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.IMPORT.getEntityName()));

		if (importstructure.getField("entity") == null) {
			throw new CommonValidationException("import.validation.structure.entity.mandatory");
		}
		String entity = (String) importstructure.getField("entity");

		if (importstructure.getField("mode") == null) {
			throw new CommonValidationException("import.validation.structure.mode.mandatory");
		}
		ImportMode mode = KeyEnum.Utils.findEnum(ImportMode.class, (String) importstructure.getField("mode"));

		for (EntityObjectVO attribute : importstructure.getDependants().getData(NuclosEntity.IMPORTATTRIBUTE.getEntityName())) {
			if (attribute.getField("attribute", String.class) == null || attribute.isFlagRemoved()) {
				continue;
			}
			String attributename = (String)attribute.getField("attribute", String.class);
			if (attributename.equals(NuclosEOField.STATE.getMetaData().getField()) && mode.equals(ImportMode.NUCLOSIMPORT)) {
				throw new CommonValidationException("import.validation.structure.statenotallowed");
			}

			String foreignentity = MetaDataServerProvider.getInstance().getEntityField(entity, attributename).getForeignEntity();
			if (foreignentity != null) {
				int count = 0;
				for (EntityObjectVO fei : attribute.getDependants().getData(NuclosEntity.IMPORTFEIDENTIFIER.getEntityName())) {
					if (fei.getField("attribute", String.class) == null || fei.isFlagRemoved()) {
						continue;
					}
					count++;
					boolean exists = false;
					for (EntityFieldMetaDataVO foreignfield : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(foreignentity).values()) {
						if (foreignfield.getField().equals(fei.getField("attribute", String.class))) {
							exists = true;
						}
					}
					if (!exists) {
						throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("import.validation.structure.foreignfielddoesnotexist", fei.getField("attribute", String.class), foreignentity, attributename));
					}
				}
				/*if (count == 0) {
					throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("import.validation.structure.feidentifierrequired", attributename));
				}*/
			}
		}

		int count = 0;
		for (EntityObjectVO identifier : importstructure.getDependants().getData(NuclosEntity.IMPORTIDENTIFIER.getEntityName())) {
			if (identifier.isFlagRemoved()) {
				continue;
			}
			boolean isImported = false;
			String field = null;

			if (identifier.getField("attribute", String.class) == null) {
				continue;
			}
			else {
				field = (String)identifier.getField("attribute", String.class);
			}
			count++;

			for (EntityObjectVO attribute : importstructure.getDependants().getData(NuclosEntity.IMPORTATTRIBUTE.getEntityName())) {
				if (attribute.isFlagRemoved() || attribute.getField("attribute", String.class) == null) {
					continue;
				}
				else if (field.equals(attribute.getField("attribute", String.class))) {
					isImported = true;
				}
			}

			if (!isImported) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("import.validation.structure.identifiernotimported", field));
			}
		}
		boolean updateOrDelete = (importstructure.getField("update") == null ? false : importstructure.getField("update", Boolean.class))
			|| (importstructure.getField("delete") == null ? false : importstructure.getField("delete", Boolean.class));
		if (updateOrDelete && count == 0) {
			throw new CommonValidationException("import.validation.structure.identifierrequired");
		}
	}

	public static void validateFileImport(MasterDataWithDependantsVO fileimport) throws CommonValidationException {
		fileimport.validate(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.IMPORTFILE.getEntityName()));

		if (fileimport.getField("mode") == null) {
			throw new CommonValidationException("import.validation.importfile.mode");
		}
		ImportMode mode = org.nuclos.common2.KeyEnum.Utils.findEnum(ImportMode.class, (String)fileimport.getField("mode"));

		GenericObjectDocumentFile file = (GenericObjectDocumentFile)fileimport.getField("name");
		if (!file.getFilename().toUpperCase().endsWith(".CSV")) {
			throw new CommonValidationException("import.validation.importfile.csv");
		}

		List<ImportStructure> importDefinitions = new ArrayList<ImportStructure>();
		for (EntityObjectVO usage : fileimport.getDependants().getData(NuclosEntity.IMPORTUSAGE.getEntityName())) {
			if (usage.isFlagRemoved() || usage.getField("importId", Integer.class) == null) {
				continue;
			}

			Integer importstructureId = (Integer) usage.getField("importId", Integer.class);

			MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			MasterDataWithDependantsVO importstructure = null;
			try {
				importstructure = mdFacade.getWithDependants(NuclosEntity.IMPORT.getEntityName(), importstructureId);
				importDefinitions.add(new ImportStructure(importstructureId));
			}
			catch(Exception e) {
				throw new NuclosFatalException(e);
			}

			try {
				ImportUtils.validateImportStructure(importstructure);
			}
			catch (CommonValidationException ex) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("import.validation.fileimport.invalidstructure", importstructure.getField("name")), ex);
			}

			ImportMode strucutremode = org.nuclos.common2.KeyEnum.Utils.findEnum(ImportMode.class, (String)importstructure.getField("mode"));

			if (!mode.equals(strucutremode)) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("import.validation.fileimport.structuremodewrong", importstructure.getField("name")));
			}
		}

		Map<String, Integer> importSettings = new HashMap<String, Integer>();
		Map<String, Set<String>> keyDefinitions = new HashMap<String, Set<String>>();

		for (ImportStructure is : importDefinitions) {
			if (!importSettings.containsKey(is.getEntityName())) {
				importSettings.put(is.getEntityName(), is.getImportSettings());
			}
			else if ((importSettings.get(is.getEntityName()) & is.getImportSettings()) != is.getImportSettings()) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("import.validation.importfile.differentsettings", is.getEntityName()));
			}

			if (!keyDefinitions.containsKey(is.getEntityName())) {
				keyDefinitions.put(is.getEntityName(), is.getIdentifiers());
			}
			else if (!keyDefinitions.get(is.getEntityName()).equals(is.getIdentifiers())) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("import.validation.importfile.differentkeydefinitions", is.getEntityName()));
			}

			for (final Item item : is.getItems().values()) {
				if (item.isReferencing()) {
					Set<String> foreignkey = new HashSet<String>();

					for (ForeignEntityIdentifier fei : item.getForeignEntityIdentifiers()) {
						foreignkey.add(fei.getFieldName());
					}

					if (!keyDefinitions.containsKey(item.getForeignEntityName())) {
						keyDefinitions.put(item.getForeignEntityName(), foreignkey);
					}
					else if (!keyDefinitions.get(item.getForeignEntityName()).equals(foreignkey)) {
						throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("import.validation.importfile.differentkeydefinitions", item.getForeignEntityName()));
					}
				}
			}
		}
	}

	public static int skipHeaderLines(CSVParser parser, int headerlines) throws IOException {
		for (int iLine = 0; iLine < headerlines; iLine++) {
			parser.getLine();
		}
		return headerlines;
	}

	public static int countLines(GenericObjectDocumentFile importfile, String delimiter) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(importfile.getContents())));

		try {
			// we have to use the csv parser to respect quotes and so on...
			CSVParser parser;
			if (!StringUtils.looksEmpty(delimiter)) {
				parser = new CSVParser(reader, delimiter.charAt(0));
			}
			else {
				parser = new CSVParser(reader);
			}

			int result = 0;
			while (parser.getLine() != null) {
				result++;
			}
			return result;
		}
		finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public static CollectableSearchCondition getSearchCondition(String entity, Map<String, Set<String>> definitions, ImportObject io) throws NuclosBusinessException {
		if (definitions == null || !definitions.containsKey(entity) || definitions.get(entity) == null || definitions.get(entity).size() == 0) {
			throw new NuclosBusinessException("import.utils.exception.1");//"Keine Schl\u00fcsseldefinition vorhanden.");
		}

		List<CollectableSearchCondition> conditions = new ArrayList<CollectableSearchCondition>();

		boolean isModule = Modules.getInstance().existModule(entity);

		for (String attribute : definitions.get(entity)) {
			CollectableEntityField cef;

			if (isModule) {
				cef = new CollectableGenericObjectEntityField(
					AttributeCache.getInstance().getAttribute(entity, attribute),
					MetaDataServerProvider.getInstance().getEntityField(entity, attribute)
					);
			}
			else {
				cef = (new CollectableMasterDataEntity(MasterDataMetaCache.getInstance().getMetaData(entity))).getEntityField(attribute);
			}

			CollectableSearchCondition cond;
			if (cef.isReferencing()) {
				if (io.getReferences() != null) {
					if (io.getReferences().get(attribute) != null) {
						if (io.getReferences().get(attribute).getValueObject() != null && io.getReferences().get(attribute).getValueObject().getId() != null) {
							Object referencedId = io.getReferences().get(attribute).getValueObject().getId();

							cond = new CollectableComparison(cef, ComparisonOperator.EQUAL, new CollectableValueIdField(referencedId, io.getAttributes().get(attribute)));
						}
						else {
							cond = new CollectableIdCondition(0);
						}
					}
					else {
						cond = new CollectableIsNullCondition(cef);
					}
				}
				else if (io.getAttributes().containsKey(attribute)) {
					cond = new CollectableComparison(cef, ComparisonOperator.EQUAL, new CollectableValueField(io.getAttributes().get(attribute)));
				}
				else {
					cond = new CollectableIsNullCondition(cef);
				}
			}
			else {
				if (io.getAttributes().get(attribute) != null) {
					cond = new CollectableComparison(cef, ComparisonOperator.EQUAL, new CollectableValueField(io.getAttributes().get(attribute)));
				}
				else {
					cond = new CollectableIsNullCondition(cef);
				}
			}
			conditions.add(cond);
		}

		if (conditions.size() > 1) {
			CompositeCollectableSearchCondition composite = new CompositeCollectableSearchCondition(LogicalOperator.AND);
			composite.addAllOperands(conditions);
			return composite;
		}
		else {
			return conditions.get(0);
		}


	}

	public static EntityObjectVO getNewObject(String entity) {
		EntityObjectVO result = new EntityObjectVO();
		result.initFields(1, 1);
		result.setEntity(entity);
		result.getFields().put(NuclosEOField.LOGGICALDELETED.getMetaData().getField(), false);
		return result;
	}

	public static List<Long> getObjectIdsByEntity(String entity) {
		if (Modules.getInstance().existModule(entity)) {
			GenericObjectFacadeLocal goFacade = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
			return CollectionUtils.transform(goFacade.getGenericObjectIds(Modules.getInstance().getModuleIdByEntityName(entity), (CollectableSearchCondition)null), new Transformer<Integer, Long>() {
				@Override
				public Long transform(Integer i) {
					return LangUtils.convertId(i);
				}
			});
		}
		else {
			MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			return CollectionUtils.transform(mdFacade.getMasterDataIds(entity), new Transformer<Object, Long>() {
				@Override
				public Long transform(Object i) {
					return LangUtils.convertId((Integer)i);
				}
			});
		}
	}

	public static Object getReferenceValue(String foreignentityfield, EntityObjectVO vo) {
		MakeEntityObjectValueIdField transformer;
		if (!StringUtils.isNullOrEmpty(foreignentityfield)) {
			transformer = new MakeEntityObjectValueIdField(foreignentityfield);
		}
		else {
			transformer = new MakeEntityObjectValueIdField();
		}
		return transformer.transform(vo).getValue();
	}
}
