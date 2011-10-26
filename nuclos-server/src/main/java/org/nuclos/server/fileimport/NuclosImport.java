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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.fileimport.ImportResult;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.fileimport.FileImportResult;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.fileimport.ImportStructure.Item;
import org.nuclos.server.fileimport.ejb3.ImportFacadeLocal;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;


/**
 * <code>NuclosImport</code> imports object from a CSV file into nuclos by using facades.
 * Does an import "with executing rules".
 *
 * This class reads the file, merges file and database data and finally performs the update of the database.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class NuclosImport extends AbstractImport {

	private static final Logger nucloslog = Logger.getLogger(NuclosImport.class);

	/**
	 * Stores all existing object id's of all imported entities to find out the
	 * data that should be deleted.
	 */
	private Map<String, List<Long>> dataToDelete = new HashMap<String, List<Long>>();

	private ImportProgressNotifier notifier;

	public NuclosImport(GenericObjectDocumentFile file, ImportContext context, List<ImportStructure> structures, ImportLogger logger, boolean atomic) {
		super(file, context, structures, logger, atomic);
	}

	/**
	 * Import file with given filename.
	 */
	@Override
	public List<FileImportResult> doImport() throws NuclosFileImportException {
		try {
			final List<FileImportResult> result = new ArrayList<FileImportResult>();

			this.notifier = new ImportProgressNotifier(getContext().getCorrelationId());
			this.notifier.start();

			int i = 0;
	        try {
		        i = ImportUtils.countLines(getFile(), getStructures().get(0).getDelimiter());
	        }
	        catch(IOException e1) {
	        	nucloslog.error(e1);
	        	this.notifier.setMessage("import.notification.progresscalculationerror");
	        }

	        this.notifier.setNextStep(i * getStructures().size(), 20);

			for (ImportStructure importstructure : getStructures()) {
				this.notifier.setMessage(StringUtils.getParameterizedExceptionMessage("import.notification.parsing", importstructure.getName()));

				ImportFileLineIterator lines;
				try {
					lines = new ImportFileLineIterator(getFile(), importstructure.getHeaderLineCount(), importstructure.getDelimiter());
				}
				catch(IOException e) {
					throw new NuclosFileImportException(e);
				}

				while (lines.hasNext()) {
					checkInterrupted();
					String[] asLineValues = null;
					try {
						asLineValues = lines.next();
					}
					catch (RuntimeException ex) {
						nucloslog.error(ex);

						getLogger().error(lines.getCurrentLine(), "import.exception.read", ex);
						if (isAtomic()) {
							throw new NuclosFileImportException("import.notification.error");
						}
					}

					try {
						putObject(getObject(importstructure, asLineValues, lines.getCurrentLine()));
					}
					catch (Exception ex) {
						nucloslog.error(ex);

						getLogger().error(lines.getCurrentLine(), "import.exception.read", ex);
						if (isAtomic()) {
							throw new NuclosFileImportException("import.notification.error");
						}
					}

					this.notifier.increment();
				}
			}


			i = 0;
			for (Map.Entry<String, Map<ImportObjectKey, ImportObject>> entity : getData().entrySet()) {
				i += entity.getValue().size();
			}
			this.notifier.setNextStep(i, 50);

			merge();

			i = 0;
			for (String entity : getImportedEntities()) {
				i += getInsertOrUpdateCount(entity);
				i += dataToDelete.get(entity).size();
			}
			this.notifier.setNextStep(i, 100);

			for (String entity : getImportedEntities()) {
				this.notifier.setMessage(StringUtils.getParameterizedExceptionMessage("import.notification.updating", entity));
				setAttributeValues(entity);
				result.add(save(entity));
			}

			StringBuilder summary = new StringBuilder();
			if (getLogger().getErrorcount() > 0) {
				summary.append(getLogger().info(StringUtils.getParameterizedExceptionMessage("import.logging.errors", getLogger().getErrorcount())) + System.getProperty("line.separator"));
			}
			for (FileImportResult fir : result) {
				summary.append(getLogger().info(StringUtils.getParameterizedExceptionMessage("import.logging.result", fir.getEntity(), fir.getInserted(), fir.getUpdated(), fir.getDeleted())) + System.getProperty("line.separator"));
			}
			ServiceLocator.getInstance().getFacade(ImportFacadeLocal.class).setImportResult(getContext().getImportfileId(), getLogger().getErrorcount() > 0 ? ImportResult.INCOMPLETE : ImportResult.OK, summary.toString());
			this.notifier.finish();

			return result;
		}
		catch (NuclosFileImportException ex) {
			ServiceLocator.getInstance().getFacade(ImportFacadeLocal.class).setImportResult(getContext().getImportfileId(), ImportResult.ERROR, getLogger().localize(ex.getMessage()));
			if (this.notifier != null) {
				this.notifier.stop(ex.getMessage());
			}
			throw ex;
		}
	}

	private int getInsertOrUpdateCount(String entityname) {
		int i = 0;
		for (ImportObject io : getData().get(entityname).values()) {
			if (io.getValueObject() != null) {
				if ((getImportSettings().get(entityname) & ImportStructure.INSERT) == ImportStructure.INSERT
					&& io.getValueObject().getId() == null) {
					i++;
				}
				else if ((getImportSettings().get(entityname) & ImportStructure.UPDATE) == ImportStructure.UPDATE
					&& io.getValueObject().getId() != null) {
					i++;
				}
			}
		}
		return i;
	}

	/**
	 * Merge complete import data with persisted data in memory
	 */
	public void merge() throws NuclosFileImportException {
		List<String> merged = new ArrayList<String>();
		for (String entityname : getImportedEntities()) {
			mergeEntity(entityname, merged);
		}
	}

	/**
	 * Merge entity with persisted data
	 *
	 * @param entityname name of entity
	 * @param merged tracking already merged entities
	 */
	private void mergeEntity(String entityname, List<String> merged) throws NuclosFileImportException {
		Map<ImportObjectKey, ImportObject> entity = getData().get(entityname);
		merged.add(entityname);

		if (getImportedEntities().contains(entityname)) {
			for (ImportStructure structure : getStructures()) {
				for (Item item : structure.getItems().values()) {
					if (item.isReferencing() && !merged.contains(item.getForeignEntityName())) {
						mergeEntity(item.getForeignEntityName(), merged);
					}
				}
			}
		}

		this.notifier.setMessage(StringUtils.getParameterizedExceptionMessage("import.notification.merging", entityname));

		if (getImportedEntities().contains(entityname)) {
			// do not merge if no primary key combination is defined
			if (getKeyDefinitions().get(entityname).size() == 0 && (getImportSettings().get(entityname) & ImportStructure.INSERT) == ImportStructure.INSERT) {
				for (ImportObject io : entity.values()) {
					checkInterrupted();
					io.setValueObject(ImportUtils.getNewObject(entityname));
					this.notifier.increment();
				}
				this.dataToDelete.put(entityname, new ArrayList<Long>());
			}
			else {
				// Check import settings
				List<Long> objectsToDelete = new ArrayList<Long>();
				if ((getImportSettings().get(entityname) & ImportStructure.DELETE) == ImportStructure.DELETE) {
					objectsToDelete.addAll(ImportUtils.getObjectIdsByEntity(entityname));
				}

				for (Map.Entry<ImportObjectKey, ImportObject> object : entity.entrySet()) {
					checkInterrupted();
					ImportObject io = object.getValue();
					EntityObjectVO vo = null;
					try {
						 vo = getExistingVO(entityname, getKeyDefinitions(), io);
					}
					catch (Exception ex) {
						getLogger().error(io.getLineNumber(), ex.getMessage(), ex);
						if (isAtomic()) {
							throw new NuclosFileImportException("import.notification.error");
						}
					}

					if (vo == null) {
						if ((getImportSettings().get(entityname) & ImportStructure.INSERT) == ImportStructure.INSERT) {
							io.setValueObject(ImportUtils.getNewObject(entityname));
						}
					}
					else {
						io.setValueObject(vo);
						if ((getImportSettings().get(entityname) & ImportStructure.DELETE) == ImportStructure.DELETE) {
							objectsToDelete.remove(vo.getId());
						}
					}
					this.notifier.increment();
				}
				this.dataToDelete.put(entityname, objectsToDelete);
			}
		}
		else {
			for (Map.Entry<ImportObjectKey, ImportObject> object : entity.entrySet()) {
				checkInterrupted();
				try {
					EntityObjectVO vo = getExistingVO(entityname, getKeyDefinitions(), object.getValue());
					if (vo == null) {
						throw new NuclosFileImportException(StringUtils.getParameterizedExceptionMessage("import.exception.referencednotfound", object.getValue().toString(), entityname));
					}
					object.getValue().setValueObject(vo);
					this.notifier.increment();
				}
				catch (NuclosFileImportException ex) {
					getLogger().error(object.getValue().getLineNumber(), ex.getMessage(), ex);
					if (isAtomic()) {
						throw new NuclosFileImportException("import.notification.error");
					}
				}
			}
		}
	}



	/**
	 * Merge imported attribute values into existing or new objects
	 *
	 * @param name of entity to merge
	 */
	private void setAttributeValues(String entityname) throws NuclosFileImportException {
		for (ImportObject io : getData().get(entityname).values()) {
			checkInterrupted();
			if (io.getValueObject() == null) {
				continue;
			}
			try {
				for (ImportStructure definition : getStructures()) {
					if (definition.getEntityName().equals(entityname)) {
						for (Item item : definition.getItems().values()) {
							if (ATTRIBUTENAME_STATE.equals(item.getFieldName()) || ATTRIBUTENAME_PROCESS.equals(item.getFieldName())) {
								continue;
							}

							if (item.isPreserve() && io.getValueObject().getFields().get(item.getFieldName()) != null) {
								continue;
							}

							if (item.isReferencing()) {
								ImportObject referenced = io.getReferences().get(item.getFieldName());
								Long valueId = null;
								Object value = null;
								if (referenced != null) {
									if (referenced.getValueObject() == null || referenced.getValueObject().getId() == null) {
										getLogger().error(io.getLineNumber(), StringUtils.getParameterizedExceptionMessage("import.exception.referencedobjectnotimported", referenced.getKey(), referenced.getEntityname()));
										if (isAtomic()) {
											throw new NuclosFileImportException("import.notification.error");
										}
									}
									valueId = referenced.getValueObject().getId();

									EntityFieldMetaDataVO fieldmeta = MetaDataServerProvider.getInstance().getEntityField(definition.getEntityName(), item.getFieldName());
									value = ImportUtils.getReferenceValue(fieldmeta.getForeignEntityField(), referenced.getValueObject());
								}
								io.getValueObject().getFields().put(item.getFieldName(), value);
								io.getValueObject().getFieldIds().put(item.getFieldName(), valueId);
							}
							else {
								io.getValueObject().getFields().put(item.getFieldName(), io.getAttributes().get(item.getFieldName()));
							}
						}

						Integer process = null;

						// Set process if necessary
						if (io.getAttributes().containsKey(ATTRIBUTENAME_PROCESS) && io.getAttributes().get(ATTRIBUTENAME_PROCESS) instanceof String) {
							String prozess = (String) io.getAttributes().get(ATTRIBUTENAME_PROCESS);

							if (getProcessCache().get(entityname).containsKey(prozess)) {
								process = getProcessCache().get(entityname).get(prozess);
								io.getValueObject().getFields().put(ATTRIBUTENAME_PROCESS, prozess);
								io.getValueObject().getFieldIds().put(ATTRIBUTENAME_PROCESS, LangUtils.convertId(process));
							}
							else {
								io.getValueObject().getFields().put(ATTRIBUTENAME_PROCESS, null);
								io.getValueObject().getFieldIds().put(ATTRIBUTENAME_PROCESS, null);
							}
						}
					}
				}
			}
			catch (Exception ex) {
				getLogger().error(io.getLineNumber(), ex.getMessage(), ex);
				if (isAtomic()) {
					throw new NuclosFileImportException("import.notification.error");
				}
			}
		}
	}

	public static EntityObjectVO getExistingVO(final String entity, final Map<String, Set<String>> definitions, final ImportObject io) throws NuclosFileImportException {
		try {
			CollectableSearchCondition condition = ImportUtils.getSearchCondition(entity, definitions, io);

			List<EntityObjectVO> foundobjects = NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getBySearchExpression(new CollectableSearchExpression(condition));

			if (foundobjects == null || foundobjects.size() == 0) {
				return null;
			}
			else if (foundobjects.size() > 1) {
				throw new NuclosFileImportException(StringUtils.getParameterizedExceptionMessage("import.exception.duplicatesfound", io, entity));
			}
			else {
				return foundobjects.get(0);
			}
		}
		catch (NuclosBusinessException ex) {
			throw new NuclosFileImportException(ex);
		}
	}

	/**
	 * Update database.
	 *
	 * @param entityname entity to update
	 */
	private FileImportResult save(String entityname) throws NuclosFileImportException {
		int inserted = 0;
		int updated = 0;
		int deleted = 0;

		for (Long id : dataToDelete.get(entityname)) {
			checkInterrupted();
			try {
				deleteObject(entityname, id);
				deleted++;
			}
			catch (Exception ex) {
				String message = StringUtils.getParameterizedExceptionMessage("import.exception.delete", id, entityname);
				getLogger().error(message, ex);
				if (isAtomic()) {
					throw new NuclosFileImportException("import.notification.error");
				}
			}
			this.notifier.increment();
		}

		for (ImportObject io : getData().get(entityname).values()) {
			checkInterrupted();
			if (io.getValueObject() == null) {
				continue;
			}
			if (io.getValueObject().getId() == null && (getImportSettings().get(entityname) & ImportStructure.INSERT) == ImportStructure.INSERT) {
				try {
					io.setValueObject(insertObject(entityname, io.getValueObject()));
					inserted++;
				}
				catch (Exception ex) {
					String message = StringUtils.getParameterizedExceptionMessage("import.exception.insert", io.toString(), entityname);
					getLogger().error(io.getLineNumber(), message, ex);
					if (isAtomic()) {
						throw new NuclosFileImportException("import.notification.error");
					}
				}
			}
			else if (io.getValueObject().getId() != null && (getImportSettings().get(entityname) & ImportStructure.UPDATE) == ImportStructure.UPDATE) {
				try {
					io.setValueObject(updateObject(entityname, io.getValueObject()));
					updated++;
				}
				catch (Exception ex) {
					String message = StringUtils.getParameterizedExceptionMessage("import.exception.update", io.toString(), entityname);
					getLogger().error(io.getLineNumber(), message, ex);
					if (isAtomic()) {
						throw new NuclosFileImportException("import.notification.error");
					}
				}
			}
			this.notifier.increment();
		}
		return new FileImportResult(entityname, inserted, updated, deleted);
	}

	public EntityObjectVO insertObject(String entityname, EntityObjectVO vo) throws Exception {
		if (MetaDataServerProvider.getInstance().getEntity(entityname).isStateModel()) {
			GenericObjectVO go = DalSupportForGO.getGenericObjectVO(vo);
			go = getGenericObjectFacade().create(new GenericObjectWithDependantsVO(go, new DependantMasterDataMap()));
			return DalSupportForGO.wrapGenericObjectVO(go);
		}
		else {
			MasterDataVO md = DalSupportForMD.wrapEntityObjectVO(vo);
			md = getMasterDataFacade().create(entityname, md, null);
			EntityObjectVO result = DalSupportForMD.getEntityObjectVO(md);
			result.setEntity(entityname);
			return result;
		}
	}

	public EntityObjectVO updateObject(String entityname, EntityObjectVO vo) throws Exception {
		if (MetaDataServerProvider.getInstance().getEntity(entityname).isStateModel()) {
			GenericObjectVO go = DalSupportForGO.getGenericObjectVO(vo);
			go = getGenericObjectFacade().modify(Modules.getInstance().getModuleIdByEntityName(entityname), new GenericObjectWithDependantsVO(go, null));
			return DalSupportForGO.wrapGenericObjectVO(go);
		}
		else {
			MasterDataVO md = DalSupportForMD.wrapEntityObjectVO(vo);
			md.setId(getMasterDataFacade().modify(entityname, md, null));
			EntityObjectVO result = DalSupportForMD.getEntityObjectVO(md);
			result.setEntity(entityname);
			return result;
		}
	}

	public void deleteObject(String entityname, Long id) throws Exception {
		if (Modules.getInstance().existModule(entityname)) {
			GenericObjectWithDependantsVO govo = getGenericObjectFacade().getWithDependants(LangUtils.convertId(id), null);
			govo.remove();
			getGenericObjectFacade().remove(govo, true);
		}
		else {
			MasterDataVO mdvo = getMasterDataFacade().get(entityname, LangUtils.convertId(id));
			mdvo.remove();
			getMasterDataFacade().remove(entityname, mdvo, false);
		}
	}
}
