package org.nuclos.client.tasklist;

import java.util.Collection;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.tasklist.TasklistDefinition;
import org.nuclos.common.tasklist.TasklistFacadeRemote;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;

public class TasklistDelegate {

	private static TasklistDelegate INSTANCE = new TasklistDelegate();

	private TasklistFacadeRemote facade;

	public static TasklistDelegate getInstance() {
		return INSTANCE;
	}
	
	private TasklistFacadeRemote getTasklistFacade() throws NuclosFatalException {
		if (facade == null) {
			try {
				facade = ServiceLocator.getInstance().getFacade(TasklistFacadeRemote.class);
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return facade;
	}

	private TasklistDelegate() {
		super();
	}

	public Collection<TasklistDefinition> getUsersTasklists() {
		return getTasklistFacade().getUsersTasklists();
	}
}
