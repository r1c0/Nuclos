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
package org.nuclos.client.task;

import org.nuclos.common.collect.collectable.AbstractCollectableBean;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.SpringLocaleDelegate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.nuclos.server.common.valueobject.TaskVO;
import org.nuclos.server.common.valueobject.TaskVO.TaskVisibility;

/**
 * <code>CollectableAdapter</code> for <code>TaskVO</code>.
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableTask extends AbstractCollectableBean<TaskVO> {

	public static final String TASK_ENTITY = "Task";
	
	public static final String FIELDNAME_NAME = "name";
	public static final String FIELDNAME_VISIBILITY = "visibility";
	public static final String FIELDNAME_PRIORITY = "priority";
	public static final String FIELDNAME_SCHEDULED = "scheduled";
	public static final String FIELDNAME_COMPLETED = "completed";
	public static final String FIELDNAME_DELEGATOR = "taskdelegator";
	public static final String FIELDNAME_TASKSTATUS = "taskstatus";
	public static final String FIELDNAME_DESCRIPTION = "description";
	public static final String FIELDNAME_COMMENT = "comment";

	public static final String FIELDNAME_CREATEDAT = "createdAt";
	public static final String FIELDNAME_CREATEDBY = "createdBy";
	public static final String FIELDNAME_CHANGEDAT = "changedAt";
	public static final String FIELDNAME_CHANGEDBY = "changedBy";

	public static final String FIELDNAME_OWNERNAME = "ownerDisplayName";
	public static final String FIELDNAME_DELEGATORNAME = "delegatorDisplayName";
	public static final String FIELDNAME_RELATEDOBJECTS = "identifier";

	public static final Integer DEFAULT_VISIBILITY = TaskVisibility.PRIVATE.getValue();

	public static class Entity extends AbstractCollectableEntity {

		private Entity() {
			super("__task", SpringLocaleDelegate.getInstance().getMessage("CollectableTask.2","Titel"));

			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_NAME, String.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.name.label","Titel"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.name.description","Titel"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_VISIBILITY, Integer.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.visibility.label","Sichtbarkeit"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.visibility.description","Sichtbarkeit"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_PRIORITY, Integer.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.priority.label","Priorit\u00e4t"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.priority.description","Priorit\u00e4t"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_SCHEDULED, Date.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.scheduled.label","Termin"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.scheduled.description","Termin"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_TASKSTATUS, String.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.taskstatus.label","Bearbeitungsstatus"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.taskstatus.description","Bearbeitungsstatus"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_DESCRIPTION, String.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.description.label","Beschreibung"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.description.description","Beschreibung"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_COMMENT, String.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.comment.label","Bemerkung"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.comment.description","Bemerkung"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_DELEGATOR, String.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.taskdelegator.label","Ersteller"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.taskdelegator.description","Ersteller"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_COMPLETED, Date.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.completed.label","Erledigt am"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.completed.description","Erledigt am"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_OWNERNAME, String.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.taskowner.user.label","Bearbeiter"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.taskowner.user.label","Bearbeiter"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_DELEGATORNAME, String.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.taskdelegator.label","Ersteller)"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.tasklist.taskdelegator.description","Ersteller"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_RELATEDOBJECTS, String.class,
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.taskobject.genericObject.label","Zugeordnet(e) Objekt(e)"),
					getSpringLocaleDelegate().getMessage("nuclos.entityfield.taskobject.genericObject.label","Zugeordnet(e) Objekt(e)"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_CREATEDAT, Date.class,  
					getSpringLocaleDelegate().getMessage("CollectableTask.16","Erstellt am"),
					getSpringLocaleDelegate().getMessage("CollectableTask.18","Erstellt am"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_CREATEDBY, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTask.17","Erstellt von"),
					getSpringLocaleDelegate().getMessage("CollectableTask.6","Erstellt von"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_CHANGEDAT, Date.class, 
					getSpringLocaleDelegate().getMessage("CollectableTask.23","Ge\u00e4ndert am"),
					getSpringLocaleDelegate().getMessage("CollectableTask.8","Ge\u00e4ndert am"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_CHANGEDBY, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTask.24","Ge\u00e4ndert von"),
					getSpringLocaleDelegate().getMessage("CollectableTask.7","Ge\u00e4ndert von"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, TASK_ENTITY));
		}

		public List<String> getNamesOfFieldsToDisplay() {
			return Arrays.asList(FIELDNAME_NAME, FIELDNAME_VISIBILITY, FIELDNAME_PRIORITY, FIELDNAME_SCHEDULED, FIELDNAME_COMPLETED,
				FIELDNAME_DELEGATORNAME, FIELDNAME_TASKSTATUS, FIELDNAME_DESCRIPTION, FIELDNAME_COMMENT, FIELDNAME_OWNERNAME, FIELDNAME_RELATEDOBJECTS);
		}

		@Override
		public String getIdentifierFieldName() {
			return FIELDNAME_NAME;
		}
	}

	public static final CollectableTask.Entity clcte = new Entity();


	public CollectableTask(TaskVO taskvo) {
		super(taskvo);
	}

	public TaskVO getTaskVO() {
		return this.getBean();
	}

	@Override
	protected CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public Object getId() {
		return this.getTaskVO().getId();
	}

	@Override
	public String getIdentifierLabel() {
		return this.getTaskVO().getName();
	}

	@Override
	public int getVersion() {
		return this.getTaskVO().getVersion();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getBean());
		result.append(",taskVo=").append(getTaskVO());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}
	
	public static class MakeCollectable implements Transformer<TaskVO, CollectableTask> {
		@Override
		public CollectableTask transform(TaskVO taskvo) {
			return new CollectableTask(taskvo);
		}
	}
}