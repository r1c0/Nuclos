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
package org.nuclos.server.report;

import org.nuclos.common.ParameterProvider;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.security.NuclosLocalServerSession;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Base class for Quartz jobs in Nucleus. Performs login/logout for the Quartz user.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">Christoph Radig</a>
 * @version 01.00.00
 */
@Configurable
public class NuclosQuartzJob implements StatefulJob {

	public static final String JOBGROUP_IMPORT = "import";

	//

	private final Job job;
	private static String sUserName;

	private NuclosLocalServerSession nuclosLocalServerSession;

	public NuclosQuartzJob(Job job) {
		this.job = job;
	}

	@Autowired
	void setNuclosLocalServerSession(NuclosLocalServerSession nuclosLocalServerSession) {
		this.nuclosLocalServerSession = nuclosLocalServerSession;
	}

	/**
	 * executes the job given in the constructor by logging in as the Quartz user, executing the job itself and
	 * <code>finally</code> logging out.
	 * @param context
	 * @throws JobExecutionException
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			setUserNameAndPassword();
			String realuser = sUserName;
			JobDetail jd = context.getJobDetail();
			if (jd != null && jd.getJobDataMap() != null && jd.getJobDataMap().get("User") != null) {
				realuser = jd.getJobDataMap().getString("User");
			}
			nuclosLocalServerSession.loginAsUser(realuser);
			try {
				this.job.execute(context);
			}
			finally {
				nuclosLocalServerSession.logout();
			}
		}
		catch (Exception ex) {
			throw new JobExecutionException(ex);
		}
	}

	protected void setUserNameAndPassword() {
		sUserName = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_TIMELIMIT_RULE_USER);
	}

	protected static String getUserName() {
		return sUserName;
	}

}	// class NuclosQuartzJob
