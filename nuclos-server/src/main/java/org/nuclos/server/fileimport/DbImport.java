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

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.fileimport.ImportResult;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.fileimport.FileImportResult;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.common.BusinessIDFactory;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.dal.processor.jdbc.impl.ImportObjectProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.fileimport.ImportStructure.ForeignEntityIdentifier;
import org.nuclos.server.fileimport.ImportStructure.Item;
import org.nuclos.server.fileimport.ejb3.ImportFacadeLocal;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * <code>DbImport</code> imports object from a CSV file into the database.
 * Does an import "without executing rules".
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class DbImport extends AbstractImport {

	private static final Logger nucloslog = Logger.getLogger(NuclosImport.class);

	private ImportProgressNotifier notifier;

	private Map<String, ImportObjectProcessor> processors = new HashMap<String, ImportObjectProcessor>();

	public DbImport(GenericObjectDocumentFile file, ImportContext context, List<ImportStructure> structures, ImportLogger logger, boolean atomic) {
		super(file, context, structures, logger, atomic);
	}

	@Override
	public List<FileImportResult> doImport() throws NuclosFileImportException {
		final List<FileImportResult> result = new ArrayList<FileImportResult>();

		this.notifier = new ImportProgressNotifier(getContext().getCorrelationId());
		this.notifier.start();

		int lineCount = 0;
        try {
        	lineCount = ImportUtils.countLines(getFile(), getStructures().get(0).getDelimiter());
        }
        catch(IOException e1) {
        	nucloslog.error("Error counting lines for csv import.", e1);
        	this.notifier.setMessage("import.notification.progresscalculationerror");
        }

        try {
    		final ProcessorFactorySingleton processorFac = ProcessorFactorySingleton.getInstance();
	        Double step = (100D / Double.valueOf(getStructures().size()));

	        int i = 0;
			for (ImportStructure importstructure : getStructures()) {
				checkInterrupted();

				String entityname = importstructure.getEntityName();

				EntityMetaDataVO meta = MetaDataServerProvider.getInstance().getEntity(entityname);

				this.notifier.setMessage(StringUtils.getParameterizedExceptionMessage("import.notification.loadinglookups", entityname));
				this.notifier.setNextStep(getLookupCount(importstructure), ((Double)(i * step.intValue() + step / 2)).intValue() );
				loadLookupsToContext(importstructure);

				this.notifier.setMessage(StringUtils.getParameterizedExceptionMessage("import.notification.updating", entityname));
				this.notifier.setNextStep(lineCount, ((Double)(i * step.intValue() + step)).intValue() );

				if (!processors.containsKey(entityname)) {
					processors.put(entityname, processorFac.newImportObjectProcessor(MetaDataServerProvider.getInstance().getEntity(importstructure.getEntityName()),
						MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(importstructure.getEntityName()).values(), importstructure));
				}
				ImportObjectProcessor processor = processors.get(entityname);

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
						String message = StringUtils.getParameterizedExceptionMessage("import.exception.4", getFile().getFilename());
						getLogger().error(lines.getCurrentLine(), message, ex);
						if (isAtomic()) {
							throw new NuclosFileImportException(message, ex);
						}
					}

					try {
						ImportObject io = getObject(importstructure, asLineValues, lines.getCurrentLine());
						io.setValueObject(ImportUtils.getNewObject(entityname));

						for (Item item : importstructure.getItems().values()) {
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
								if (referenced != null && referenced.getValueObject() != null) {
									valueId = referenced.getValueObject().getId();

									EntityFieldMetaDataVO fieldmeta = MetaDataServerProvider.getInstance().getEntityField(importstructure.getEntityName(), item.getFieldName());
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

						// Set initial state if necessary
						if (meta.isStateModel() && !io.getAttributes().containsKey(ATTRIBUTENAME_STATE)) {
							StateVO statevo = getStateFacade().getInitialState(new UsageCriteria(LangUtils.convertId(meta.getId()), process));
							io.getAttributes().put(ATTRIBUTENAME_STATE, statevo.getStatename());
						}

						// Set state if necessary
						if (meta.isStateModel() && io.getAttributes().containsKey(ATTRIBUTENAME_STATE) && io.getAttributes().get(ATTRIBUTENAME_STATE) instanceof String) {
							String status = (String) io.getAttributes().get(ATTRIBUTENAME_STATE);

							if (getStateCache().containsKey(entityname) && getStateCache().get(entityname).containsKey(process) &&  getStateCache().get(entityname).get(process).containsKey(status)) {
								io.getValueObject().getFields().put(ATTRIBUTENAME_STATE, status);
								io.getValueObject().getFieldIds().put(ATTRIBUTENAME_STATE, LangUtils.convertId(getStateCache().get(entityname).get(process).get(status)));
							}
							else {
								io.getValueObject().getFields().put(ATTRIBUTENAME_STATE, null);
								io.getValueObject().getFieldIds().put(ATTRIBUTENAME_STATE, null);
							}
						}

						// generate system identifier:
						if (meta.isStateModel()) {
							Integer moduleId = Modules.getInstance().getModuleIdByEntityName(importstructure.getEntityName());
							final String sCanonicalValueSystemIdentifier = BusinessIDFactory.generateSystemIdentifier(moduleId);
							io.getValueObject().getFields().put(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField(), sCanonicalValueSystemIdentifier);
						}

						EntityObjectVO eo = io.getValueObject();
						eo.getFields().put(NuclosEOField.LOGGICALDELETED.getMetaData().getField(), false);
						DalUtils.updateVersionInformation(eo, "import");

						processor.insertOrUpdate(eo);
					}
					catch (Exception ex) {
						getLogger().error(lines.getCurrentLine(), "import.exception.dbimport", ex);
						if (isAtomic()) {
							throw new NuclosFileImportException("import.notification.error");
						}
					}
					finally {
						this.notifier.increment();
					}
				}
				i++;
			}

			for (Map.Entry<String, ImportObjectProcessor> processor : processors.entrySet()) {
				result.add(new FileImportResult(processor.getKey(), processor.getValue().getInserted(), processor.getValue().getUpdated(), 0));
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
        }
        catch (NuclosFileImportException ex) {
        	ServiceLocator.getInstance().getFacade(ImportFacadeLocal.class).setImportResult(getContext().getImportfileId(), ImportResult.ERROR, getLogger().localize(ex.getMessage()));
        	this.notifier.stop(ex.getMessage());
        	throw ex;
        }

		return result;
	}

	private StateFacadeLocal stateFacade;

	private StateFacadeLocal getStateFacade() {
		if (stateFacade == null) {
			stateFacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
		}
		return stateFacade;
	}

	private int getLookupCount(ImportStructure structure) {
		try {
			int result = 0;
			for (ImportStructure.Item item : structure.getItems().values()) {
				if (item.isReferencing()) {
					JdbcEntityObjectProcessor eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(item.getForeignEntityName());

					result += eoProcessor.count(new CollectableSearchExpression(TrueCondition.TRUE));
				}
			}
			return result;
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

	private void loadLookupsToContext(ImportStructure structure) {
		try {
			for (ImportStructure.Item item : structure.getItems().values()) {
				if (item.isReferencing()) {
					JdbcEntityObjectProcessor eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(item.getForeignEntityName());

					List<EntityObjectVO> objects = eoProcessor.getAll();

					for (EntityObjectVO object : objects) {
						Map<String, Object> foreignkey = new HashMap<String, Object>(1);

						for (ForeignEntityIdentifier feid : item.getForeignEntityIdentifiers()) {
							foreignkey.put(feid.getFieldName(), object.getFields().get(feid.getFieldName()));
						}

						ImportObject io = new ImportObject(item.getForeignEntityName(), new ImportObjectKey(foreignkey), foreignkey, -1, new HashMap<String, ImportObject>());
						io.setValueObject(object);
						putObject(io);
						this.notifier.increment();
					}
				}
			}
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}
}
