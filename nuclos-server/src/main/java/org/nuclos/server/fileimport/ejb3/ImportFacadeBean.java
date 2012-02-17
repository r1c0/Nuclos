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
package org.nuclos.server.fileimport.ejb3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.HashResourceBundle;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.fileimport.ImportMode;
import org.nuclos.common.fileimport.ImportResult;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.fileimport.FileImportResult;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.fileimport.AbstractImport;
import org.nuclos.server.fileimport.ImportContext;
import org.nuclos.server.fileimport.ImportJob;
import org.nuclos.server.fileimport.ImportLogger;
import org.nuclos.server.fileimport.ImportStructure;
import org.nuclos.server.fileimport.ImportUtils;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.report.NuclosQuartzJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * Facade for managing and executing imports.
 * Takes care of executing an import with respect to its transaction settings.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@RolesAllowed("Login")
@Transactional
public class ImportFacadeBean extends NuclosFacadeBean implements ImportFacadeRemote {
	
	private Scheduler nuclosScheduler;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	public ImportFacadeBean() {
	}
	
	@Autowired
	final void setNuclosScheduler(Scheduler nuclosScheduler) {
		this.nuclosScheduler = nuclosScheduler;
	}	

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}

	public MasterDataVO createImportStructure(MasterDataWithDependantsVO importStructure) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.IMPORT);
		ImportUtils.validateImportStructure(importStructure);

		MasterDataFacadeLocal mdfacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		return mdfacade.create(NuclosEntity.IMPORT.getEntityName(), importStructure, importStructure.getDependants());
	}

	public Object modifyImportStructure(MasterDataWithDependantsVO importStructure) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.IMPORT);
		ImportUtils.validateImportStructure(importStructure);

		MasterDataFacadeLocal mdfacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		return mdfacade.modify(NuclosEntity.IMPORT.getEntityName(), importStructure, importStructure.getDependants());
	}

	public void removeImportStructure(MasterDataVO importStructure) throws CommonBusinessException {
		checkDeleteAllowed(NuclosEntity.IMPORT);

		MasterDataFacadeLocal mdfacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		mdfacade.remove(NuclosEntity.IMPORT.getEntityName(), importStructure, true);
	}

	public MasterDataVO createFileImport(MasterDataWithDependantsVO fileImport) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.IMPORTFILE);
		ImportUtils.validateFileImport(fileImport);

		MasterDataFacadeLocal mdfacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		return mdfacade.create(NuclosEntity.IMPORTFILE.getEntityName(), fileImport, fileImport.getDependants());
	}

	public Object modifyFileImport(MasterDataWithDependantsVO fileImport) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.IMPORTFILE);
		ImportUtils.validateFileImport(fileImport);

		if (getImportCorrelationId(fileImport.getIntId()) == null) {
			MasterDataFacadeLocal mdfacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			return mdfacade.modify(NuclosEntity.IMPORTFILE.getEntityName(), fileImport, fileImport.getDependants());
		}
		else {
			throw new CommonValidationException("import.exception.modify.running");
		}
	}

	public void removeFileImport(MasterDataVO fileImport) throws CommonBusinessException {
		checkDeleteAllowed(NuclosEntity.IMPORTFILE);

		if (getImportCorrelationId(fileImport.getIntId()) == null) {
			MasterDataFacadeLocal mdfacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			mdfacade.remove(NuclosEntity.IMPORTFILE.getEntityName(), fileImport, true);
		}
		else {
			throw new CommonRemoveException("import.exception.remove.running");
		}
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	public List<FileImportResult> doImport(ImportContext context) throws NuclosFileImportException {
		ImportFacadeLocal localInterface = ServerServiceLocator.getInstance().getFacade(ImportFacadeLocal.class);

		MasterDataWithDependantsVO importfilevo;
		try {
			importfilevo = getMasterDataFacade().getWithDependants(NuclosEntity.IMPORTFILE.getEntityName(), context.getImportfileId());
		}
		catch(Exception e) {
			throw new NuclosFatalException(e);
		}

		GenericObjectDocumentFile file = (GenericObjectDocumentFile)importfilevo.getField("name");

		// initialize import logger
		ImportLogger logger;
		try {
			GenericObjectDocumentFile logFile = new GenericObjectDocumentFile(file.getFilename().substring(0, file.getFilename().lastIndexOf(".")) + ".log", new byte[]{});
			importfilevo.setField("log", logFile);
			getMasterDataFacade().modify(NuclosEntity.IMPORTFILE.getEntityName(), importfilevo, importfilevo.getDependants());

	        // get path for log file
	        java.io.File f = new java.io.File(NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH), importfilevo.getIntId() + "." + logFile.getFilename());

	        // get translations
	        LocaleInfo info = null;
	        for (LocaleInfo i : ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).getAllLocales(true)) {
	        	if (i.localeId == context.getLocaleId()) {
	        		info = i;
	        	}
	        }
	        if (info == null) {
	        	info = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).getDefaultLocale();
	        }
	        HashResourceBundle bundle = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).getResourceBundle(info);

	        logger = new ImportLogger(f.getAbsolutePath(), bundle);
        }
        catch(Exception ex) {
	        throw new NuclosFatalException();
        }

        // prepare import structures
        List<EntityObjectVO> order = CollectionUtils.sorted(importfilevo.getDependants().getData(NuclosEntity.IMPORTUSAGE.getEntityName()), new Comparator<EntityObjectVO> () {
			@Override
			public int compare(EntityObjectVO o1, EntityObjectVO o2) {
				Integer index1 = o1.getField("order", Integer.class);
				Integer index2 = o2.getField("order", Integer.class);
				return index1.compareTo(index2);
			}
		});

		List<ImportStructure> structures = new ArrayList<ImportStructure>();
		for (EntityObjectVO importusage : order) {
			structures.add(new ImportStructure(importusage.getField("importId", Integer.class)));
		}

		ImportMode mode = org.nuclos.common2.KeyEnum.Utils.findEnum(ImportMode.class, (String) importfilevo.getField("mode"));

		boolean atomic = importfilevo.getField("atomic") == null ? false : importfilevo.getField("atomic", Boolean.class);

        // create import instance
		AbstractImport instance = AbstractImport.newInstance(mode, file, context, structures, logger, atomic);

		List<FileImportResult> result;
		if (atomic) {
			result = localInterface.doAtomicImport(instance);
		}
		else {
			result = localInterface.doNonAtomicImport(instance);
		}

		return result;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
    public List<FileImportResult> doAtomicImport(AbstractImport instance) throws NuclosFileImportException {
		try {
			return doImportInternal(instance);
		}
		catch (NuclosFileImportException ex) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw ex;
		}
    }

	@Transactional(propagation=Propagation.NOT_SUPPORTED)
    public List<FileImportResult> doNonAtomicImport(AbstractImport instance) throws NuclosFileImportException {
	    return doImportInternal(instance);
    }

	private List<FileImportResult> doImportInternal(AbstractImport instance) throws NuclosFileImportException {
		return instance.doImport();
	}

    public void stopImport(Integer importfileId) throws NuclosFileImportException {
		try {
	        nuclosScheduler.interrupt(importfileId.toString(), NuclosQuartzJob.JOBGROUP_IMPORT);
        }
        catch(UnableToInterruptJobException e) {
        	throw new NuclosFileImportException("import.exception.stop");
        }
    }

    public String doImport(Integer importfileId) throws NuclosFileImportException {
	    String correlationId = getImportCorrelationId(importfileId);

	    try {
	    	MasterDataWithDependantsVO importfile = getMasterDataFacade().getWithDependants(NuclosEntity.IMPORTFILE.getEntityName(), importfileId);
	    	ImportUtils.validateFileImport(importfile);

	    	// reset state
	    	GenericObjectDocumentFile file = (GenericObjectDocumentFile)importfile.getField("name");
	    	GenericObjectDocumentFile logFile = new GenericObjectDocumentFile(file.getFilename().substring(0, file.getFilename().lastIndexOf(".")) + ".log", new byte[]{});
			importfile.setField("log", logFile);
			importfile.setField("laststate", null);
			importfile.setField("result", null);
			getMasterDataFacade().modify(NuclosEntity.IMPORTFILE.getEntityName(), importfile, importfile.getDependants());
	    }
	    catch (Exception ex) {
	    	throw new NuclosFileImportException(ex.getMessage(), ex);
	    }

	    if (StringUtils.isNullOrEmpty(correlationId)) {
	    	correlationId = UUID.randomUUID().toString();

	    	try {
			    if (nuclosScheduler.getJobDetail(importfileId.toString(), NuclosQuartzJob.JOBGROUP_IMPORT) != null) {
			    	nuclosScheduler.deleteJob(importfileId.toString(), NuclosQuartzJob.JOBGROUP_IMPORT);
			    }
			    JobDetail jobDetail = new JobDetail(importfileId.toString(), NuclosQuartzJob.JOBGROUP_IMPORT, ImportJob.class);
		    	jobDetail.getJobDataMap().put("ProcessId", correlationId);
		    	jobDetail.getJobDataMap().put("LocaleId", ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).getUserLocale().localeId);
		    	jobDetail.getJobDataMap().put("User", getCurrentUserName());
		    	jobDetail.setDurability(true);
		    	nuclosScheduler.addJob(jobDetail, false);

		    	nuclosScheduler.triggerJob(importfileId.toString(), NuclosQuartzJob.JOBGROUP_IMPORT);
		    }
		    catch (SchedulerException ex) {
		    	throw new NuclosFileImportException("import.exception.start");
		    }
	    }
	    return correlationId;
    }

    public String getImportCorrelationId(Integer importfileId) {
		try {
	        List<?> executingJobs = nuclosScheduler.getCurrentlyExecutingJobs();
	        for (Object o : executingJobs) {
	        	if (o instanceof JobExecutionContext) {
	        		JobExecutionContext job = (JobExecutionContext) o;
	        		if (NuclosQuartzJob.JOBGROUP_IMPORT.equals(job.getJobDetail().getGroup()) && job.getJobDetail().getName().equals(importfileId.toString())) {
	        			return job.getJobDetail().getJobDataMap().getString("ProcessId");
	        		}
	        	}
	        }
        }
        catch(SchedulerException e) {
	        throw new NuclosFatalException("import.exception.getcorrelationid");
        }
	    return null;
    }

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void setImportResult(Integer importfileId, ImportResult result, String summary) {
		try {
	    	MasterDataWithDependantsVO importfile = getMasterDataFacade().getWithDependants(NuclosEntity.IMPORTFILE.getEntityName(), importfileId);
			importfile.setField("laststate", result.getValue());
			importfile.setField("result", summary);
			getMasterDataFacade().modify(NuclosEntity.IMPORTFILE.getEntityName(), importfile, importfile.getDependants());
	    }
	    catch (Exception ex) {
	    	// just log error as the user cannot react on it
	    	error(ex);
	    }
	}
}
