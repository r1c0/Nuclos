package org.nuclos.client.tasklist;

import java.util.Collection;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.tasklist.TasklistDefinition;
import org.nuclos.common.tasklist.TasklistFacadeRemote;
import org.nuclos.common2.exception.CommonFatalException;

public class TasklistDelegate {

	private static TasklistDelegate INSTANCE;
	
	// Spring injection

	private TasklistFacadeRemote tasklistFacadeRemote;
	
	// end of Spring injection

	TasklistDelegate() {
		INSTANCE = this;
	}

	public static TasklistDelegate getInstance() {
		return INSTANCE;
	}
	
	public final void setTasklistFacadeRemote(TasklistFacadeRemote tasklistFacadeRemote) {
		this.tasklistFacadeRemote = tasklistFacadeRemote;
	}
	
	public Collection<TasklistDefinition> getUsersTasklists() {
		return tasklistFacadeRemote.getUsersTasklists();
	}
}
