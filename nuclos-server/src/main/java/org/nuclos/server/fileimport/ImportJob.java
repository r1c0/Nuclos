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

import org.apache.log4j.Logger;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.fileimport.ejb3.ImportFacadeLocal;
import org.nuclos.server.job.NuclosInterruptableJob;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

/**
 * Implementation of a interruptable quartz job that can be started by nuclos.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ImportJob extends NuclosInterruptableJob {

	public ImportJob() {
	    super(new ImportJobImpl());
    }

	private static class ImportJobImpl implements InterruptableJob {

		private static final Logger log = Logger.getLogger(MasterDataFacadeHelper.class);

		private ImportContext context;

		@Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
			String sId = context.getJobDetail().getName();
			Integer importFileId = Integer.parseInt(sId);
			String correlationId = context.getJobDetail().getJobDataMap().getString("ProcessId");
			Integer localeId = context.getJobDetail().getJobDataMap().getInt("LocaleId");
			String username = context.getJobDetail().getJobDataMap().getString("ProcessId");

			try {
				this.context = new ImportContext(importFileId, correlationId, localeId, username);
			}
			catch(Exception ex) {
				log.error("Object import job could not be started.", ex);
			}
			try {
				getImportExecutionFacade().doImport(this.context);
			}
			catch (NuclosFileImportException ex) {
				log.error("Import job " + sId + " terminated.", ex);
			}
        }

		@Override
        public void interrupt() throws UnableToInterruptJobException {
	        if (this.context != null) {
	        	this.context.interrupt();
	        }
	        else {
	        	throw new UnableToInterruptJobException("import.not.running");
	        }
        }

		private ImportFacadeLocal getImportExecutionFacade() {
			return ServiceLocator.getInstance().getFacade(ImportFacadeLocal.class);
		}
	}
}
