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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.fileimport.ImportMode;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.fileimport.FileImportResult;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.fileimport.ImportStructure.ForeignEntityIdentifier;
import org.nuclos.server.fileimport.ImportStructure.Item;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Base class for object imports
 *
 * Provide structure definitions, facades and caches for object import implementations
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public abstract class AbstractImport {

	private final GenericObjectDocumentFile file;

	private final ImportContext context;

	private final ImportLogger logger;

	private final List<ImportStructure> structures;

	private final boolean atomic;

	private List<String> importedEntities = new ArrayList<String>();

	private Map<String, Set<String>> keyDefinitions = new HashMap<String, Set<String>>();

	private Map<String, Integer> importSettings = new HashMap<String, Integer>();

	private Map<String, Map<ImportObjectKey, ImportObject>> data = new HashMap<String, Map<ImportObjectKey, ImportObject>>();

	private final Map<String, Map<String, Integer>> processCache = new HashMap<String, Map<String,Integer>>();

	private final Map<String, Map<Integer, Map<String, Integer>>> stateCache = new HashMap<String, Map<Integer, Map<String, Integer>>>();

	protected final String ATTRIBUTENAME_STATE = NuclosEOField.STATE.getMetaData().getField();
	protected final String ATTRIBUTENAME_PROCESS = NuclosEOField.PROCESS.getMetaData().getField();

	protected AbstractImport(GenericObjectDocumentFile file, ImportContext context, List<ImportStructure> structures, ImportLogger logger, boolean atomic) {
		this.file = file;
		this.context = context;
		this.structures = structures;
		this.logger = logger;
		this.atomic = atomic;

		for (ImportStructure is : structures) {
			if (!importedEntities.contains(is.getEntityName())) {
				importedEntities.add(is.getEntityName());
				importSettings.put(is.getEntityName(), is.getImportSettings());
			}
		}

		for (final ImportStructure definition : this.structures) {
			if (!keyDefinitions.containsKey(definition.getEntityName())) {
				keyDefinitions.put(definition.getEntityName(), definition.getIdentifiers());
			}

			if (Modules.getInstance().existModule(definition.getEntityName())) {
				Integer moduleId = Modules.getInstance().getModuleIdByEntityName(definition.getEntityName());

				// cache processes
				Collection<MasterDataVO> processes = mdFacade.getDependantMasterData(NuclosEntity.PROCESS.getEntityName(), "module", moduleId);

				if (processes != null) {
					Map<String, Integer> processIdsByName = new HashMap<String, Integer>();
					for (MasterDataVO process : processes) {
						processIdsByName.put((String)process.getField("name"), process.getIntId());
					}
					processCache.put(definition.getEntityName(), processIdsByName);
				}

				// cache states
				if (Modules.getInstance().getUsesStateModel(moduleId)) {
					Collection<MasterDataVO> statemodelUsages = mdFacade.getDependantMasterData(NuclosEntity.STATEMODELUSAGE.getEntityName(), "nuclos_module", moduleId);

					Map<Integer, Map<String, Integer>> statesByProcess = new HashMap<Integer, Map<String, Integer>>();
					for (MasterDataVO statemodelUsage : statemodelUsages) {
						Map<String, Integer> stateIdsByName = new HashMap<String, Integer>();

						Collection<MasterDataVO> states = mdFacade.getDependantMasterData(NuclosEntity.STATE.getEntityName(), "model", statemodelUsage.getField("statemodelId"));
						for (MasterDataVO state : states) {
							stateIdsByName.put((String)state.getField("name"), state.getIntId());
						}
						statesByProcess.put((Integer)statemodelUsage.getField("processId"), stateIdsByName);
					}
					stateCache.put(definition.getEntityName(), statesByProcess);
				}
			}

			for (final Item item : definition.getItems().values()) {
				if (item.isReferencing()) {
					Set<String> foreignkey = new HashSet<String>();

					for (ForeignEntityIdentifier fei : item.getForeignEntityIdentifiers()) {
						foreignkey.add(fei.getFieldName());
					}

					if (!keyDefinitions.containsKey(item.getForeignEntityName())) {
						keyDefinitions.put(item.getForeignEntityName(), foreignkey);
					}
				}
			}
		}

		// setup context
		for (String entity : keyDefinitions.keySet()) {
			data.put(entity, new HashMap<ImportObjectKey, ImportObject>());
		}
	}

	/**
	 * Do the actual import.
	 *
	 * @return A list of <code>FileImportResult</code>s that contain the information about how many objects were inserted, updated, deleted.
	 * @throws NuclosFileImportException
	 */
	public abstract List<FileImportResult> doImport() throws NuclosFileImportException;

	public GenericObjectDocumentFile getFile() {
		return file;
	}

	public ImportContext getContext() {
		return context;
	}

	public ImportLogger getLogger() {
		return logger;
	}

	public List<ImportStructure> getStructures() {
		return structures;
	}

	public boolean isAtomic() {
		return atomic;
	}

	public List<String> getImportedEntities() {
		return importedEntities;
	}

	public Map<String, Set<String>> getKeyDefinitions() {
		return keyDefinitions;
	}

	public Map<String, Integer> getImportSettings() {
		return importSettings;
	}

	public Map<String, Map<ImportObjectKey, ImportObject>> getData() {
		return data;
	}

	public Map<String, Map<String, Integer>> getProcessCache() {
		return processCache;
	}

	public Map<String, Map<Integer, Map<String, Integer>>> getStateCache() {
		return stateCache;
	}

	private final GenericObjectFacadeLocal goFacade = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);

	protected GenericObjectFacadeLocal getGenericObjectFacade() {
		return this.goFacade;
	}

	private final MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);

	protected MasterDataFacadeLocal getMasterDataFacade() {
		return this.mdFacade;
	}

	protected void checkInterrupted() throws NuclosFileImportException {
		if (getContext().isInterrupted()) {
			throw new NuclosFileImportException("import.notification.stopped");
		}
	}

	/**
	 * Add new parsed object to context data
	 *
	 * @param importObject imported object
	 */
	public void putObject(ImportObject importObject) {
		if (importObject.isEmpty()) {
			return;
		}

		if (!getData().get(importObject.getEntityname()).containsKey(importObject.getKey())) {
			for (Map.Entry<String, ImportObject> entry : importObject.getReferences().entrySet()) {
				if (getData().get(entry.getValue().getEntityname()).containsKey(entry.getValue().getKey())) {
					entry.setValue(getData().get(entry.getValue().getEntityname()).get(entry.getValue().getKey()));
				}
				else {
					getData().get(entry.getValue().getEntityname()).put(entry.getValue().getKey(), entry.getValue());
				}
			}
			getData().get(importObject.getEntityname()).put(importObject.getKey(), importObject);
		}
		else {
			// update references in imported object
			for (Map.Entry<String, ImportObject> entry : importObject.getReferences().entrySet()) {
				if (getData().get(entry.getValue().getEntityname()).containsKey(entry.getValue().getKey())) {
					entry.setValue(getData().get(entry.getValue().getEntityname()).get(entry.getValue().getKey()));
				}
				else {
					getData().get(entry.getValue().getEntityname()).put(entry.getValue().getKey(), entry.getValue());
				}
			}
			// merge attributes and references into existing object
			getData().get(importObject.getEntityname()).get(importObject.getKey()).getAttributes().putAll(importObject.getAttributes());
			getData().get(importObject.getEntityname()).get(importObject.getKey()).getReferences().putAll(importObject.getReferences());
		}
	}

	/**
	 * Parse line and create new import object
	 *
	 * @param importDefinition import structure definition for imported line
	 * @param asLineValues parsed csv line
	 */
	protected ImportObject getObject(ImportStructure importDefinition, String[] asLineValues, int lineNumber) throws NuclosFileImportException {
		String entityname = importDefinition.getEntityName();
		Map<String, Object> attributes = new HashMap<String, Object>();
		Map<String, ImportObject> references = new HashMap<String, ImportObject>();

		for (org.nuclos.server.fileimport.ImportStructure.Item item : importDefinition.getItems().values()) {
			final String sKey = item.getFieldName();

			if (item.isReferencing()) {
				final ImportObject reference = getReferencedObject(importDefinition, asLineValues, sKey, lineNumber);
				if (!reference.isEmpty()) {
					// check if reference is already in context data
					ImportObject contextReferenced = getData().get(item.getForeignEntityName()).get(reference.getKey());
					if (contextReferenced != null) {
						references.put(sKey, contextReferenced);
					}
					else {
						references.put(sKey, reference);
					}
				}
			}
			else {
				if (item.getColumn() <= asLineValues.length) {
					final Object oValue = item.parse(asLineValues[item.getColumn() - 1]);
					attributes.put(item.getFieldName(), oValue);
				}
				else {
					throw new NuclosFileImportException(StringUtils.getParameterizedExceptionMessage("import.exception.indexoutofbounds", item.getColumn(), asLineValues.length));
				}
			}
		}
		final ImportObject result = new ImportObject(entityname, getKey(importDefinition, attributes, entityname, references), attributes, lineNumber, references);
		return result;
	}

	/**
	 * Create new referenced import object from parsed line
	 *
	 * @param importDefinition import structure definition for imported line
	 * @param asLineValues parsed csv line
	 * @param referencingFieldname referencing field name
	 */
	protected ImportObject getReferencedObject(ImportStructure importDefinition, String[] asLineValues, String referencingFieldname, int lineNumber) throws NuclosFileImportException {
		final Collection<ImportStructure.ForeignEntityIdentifier> feIdentifiers = importDefinition.getItems().get(referencingFieldname).getForeignEntityIdentifiers();

		final String entityname = feIdentifiers.iterator().next().getEntityName();
		final Map<String, Object> keyMap = new HashMap<String, Object>();

		for (ImportStructure.ForeignEntityIdentifier feIdentifier : feIdentifiers) {
			final String sKey = feIdentifier.getFieldName();
			if (feIdentifier.getColumn() <= asLineValues.length) {
				final Object oValue = feIdentifier.parse(asLineValues[feIdentifier.getColumn() - 1]);
				keyMap.put(sKey, oValue);
			}
			else {
				throw new NuclosFileImportException(StringUtils.getParameterizedExceptionMessage("import.exception.indexoutofbounds", feIdentifier.getColumn(), asLineValues.length));
			}
		}

		final ImportObject result = new ImportObject(entityname, getForeignKey(importDefinition, keyMap, importDefinition.getEntityName(), referencingFieldname), keyMap, lineNumber);
		return result;
	}

	/**
	 * Create primary key from parsed values
	 *
	 * @param parsed attribute values
	 * @param entityname entity name
	 * @param references foreign keys
	 */
	protected ImportObjectKey getKey(ImportStructure definition, Map<String, Object> attributes, String entityname, Map<String, ImportObject> references) {
		Map<String, Object> keyMap = new HashMap<String, Object>();
		for (String identifier : definition.getIdentifiers()) {
			if (definition.getItems().get(identifier).isReferencing()) {
				keyMap.put(identifier, references.get(identifier));
			}
			else {
				keyMap.put(identifier, attributes.get(identifier));
			}
		}

		// no identifiers set -> generate random unique id
		if (definition.getIdentifiers().size() == 0) {
			keyMap.put("temp", UUID.randomUUID());
		}
		return new ImportObjectKey(keyMap);
	}

	/**
	 * Create foreign key by parsed values
	 *
	 * @param attribute values
	 * @param referencingEntityname referencing entity name
	 * @param referencingFieldname referencing field name
	 */
	protected ImportObjectKey getForeignKey(ImportStructure importDefinition, Map<String, Object> attributes, String referencingEntityname, String referencingFieldname) {
		Map<String, Object> keyMap = new HashMap<String, Object>();
		Set<ForeignEntityIdentifier> identifiers = importDefinition.getItems().get(referencingFieldname).getForeignEntityIdentifiers();
		for (ForeignEntityIdentifier entry : identifiers) {
			keyMap.put(entry.getFieldName(), attributes.get(entry.getFieldName()));
		}
		return new ImportObjectKey(keyMap);
	}

	/**
	 * Create a new import instance by checking the importfile's mode.
	 *
	 * @param importfileId Id of file to import.
	 * @param correlationId CorrelationId to notify clients, if any.
	 * @return Instance of <code>AbstractImport</code>
	 * @throws NuclosFileImportException
	 */
	public static AbstractImport newInstance(ImportMode mode, GenericObjectDocumentFile file, ImportContext context, List<ImportStructure> structures, ImportLogger logger, boolean atomic) throws NuclosFileImportException {
		AbstractImport instance;
		if (ImportMode.NUCLOSIMPORT.equals(mode)) {
			instance = new NuclosImport(file, context, structures, logger, atomic);
		}
		else if (ImportMode.DBIMPORT.equals(mode)) {
			instance = new DbImport(file, context, structures, logger, atomic);
		}
		else {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("import.exception.modenotimplemented", mode.toString()));
		}
		return instance;
	}
}
