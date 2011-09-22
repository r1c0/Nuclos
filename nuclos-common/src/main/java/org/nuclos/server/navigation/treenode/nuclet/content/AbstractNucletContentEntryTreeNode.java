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
package org.nuclos.server.navigation.treenode.nuclet.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 00.01.000
 */
public abstract class AbstractNucletContentEntryTreeNode implements TreeNode {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	protected final NuclosEntity entity;
	protected final EntityObjectVO eo;

	/**
	 *
	 */
	public static final String FOREIGN_FIELD_TO_NUCLET = "nuclet";

	/**
	 *
	 */
	public static final String NAME_FIELD = "name";

	/**
	 *
	 * @return
	 */
	public static List<NuclosEntity> getNucletContentEntities() {
		List<NuclosEntity> result = new ArrayList<NuclosEntity>();
		result.add(NuclosEntity.ENTITY);
		result.add(NuclosEntity.PROCESS);
		result.add(NuclosEntity.LAYOUT);
		result.add(NuclosEntity.STATEMODEL);
		result.add(NuclosEntity.GENERATION);
		result.add(NuclosEntity.ENTITYRELATION);
		result.add(NuclosEntity.ENTITYFIELDGROUP);
		result.add(NuclosEntity.CUSTOMCOMPONENT);

		result.add(NuclosEntity.REPORT);
		result.add(NuclosEntity.DATASOURCE);
		result.add(NuclosEntity.DYNAMICENTITY);
		result.add(NuclosEntity.VALUELISTPROVIDER);
		result.add(NuclosEntity.RECORDGRANT);

		result.add(NuclosEntity.RULE);
		result.add(NuclosEntity.TIMELIMITRULE);
		result.add(NuclosEntity.CODE);

		result.add(NuclosEntity.DBOBJECT);

		result.add(NuclosEntity.GROUP);
		result.add(NuclosEntity.GROUPTYPE);
		result.add(NuclosEntity.TASKSTATUS);
		result.add(NuclosEntity.RELATIONTYPE);
		result.add(NuclosEntity.RESOURCE);

		result.add(NuclosEntity.ROLE);
		result.add(NuclosEntity.PARAMETER);
		result.add(NuclosEntity.JOBCONTROLLER);
		result.add(NuclosEntity.SEARCHFILTER);
		result.add(NuclosEntity.WEBSERVICE);

		result.add(NuclosEntity.IMPORT);

		return result;
	}

	public AbstractNucletContentEntryTreeNode(EntityObjectVO eo) {
		this.entity = NuclosEntity.getByName(eo.getEntity());
		this.eo = eo;
		if (entity == null) {
			throw new IllegalArgumentException("entity must not be null");
		}
		if (!getNucletContentEntities().contains(entity)) {
			throw new IllegalArgumentException("entity " + entity.getEntityName() + " must be nuclet content");
		}
	}

	/**
	 * client only
	 */
	@Override
	public final String getLabel() {
		return getName();
	}

	/**
	 * client only
	 * @return
	 */
	public final String getLabelWithEntity() {
		return CommonLocaleDelegate.getLabelFromMetaDataVO(getMetaData(entity)) + ": " + getName();
	}

	/**
	 * client only
	 */
	public String getName() {
		return StringUtils.defaultIfEmpty(eo.getField(NAME_FIELD, String.class), "<[NO NAME]> ID=" + eo.getId());
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public AbstractNucletContentEntryTreeNode refreshed() throws CommonFinderException {
		try {
			return null;//Utils.getTreeNodeFacade().getNucletTreeNode(this.getId(), false);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public final EntityObjectVO getEntityObjectVO() {
		return eo;
	}

	public final NuclosEntity getEntity() {
		return entity;
	}

	@Override
	public final Long getId() {
		return eo.getId();
	}

	@Override
	public final String getIdentifier() {
		return getLabelWithEntity();
	}

	@Override
	public List<? extends TreeNode> getSubNodes() {
		return Collections.emptyList();
	}

	@Override
	public Boolean hasSubNodes() {
		return false;
	}

	@Override
	public void removeSubNodes() {
	}

	@Override
	public void refresh() {
	}

	@Override
	public boolean implementsNewRefreshMethod() {
		return false;
	}

	@Override
	public boolean needsParent() {
		return true;
	}

	@Override
	public String toString() {
		return getLabelWithEntity();
	}

	protected static EntityMetaDataVO getMetaData(NuclosEntity entity) {
		return getMetaData(entity.getEntityName());
	}

	protected static EntityMetaDataVO getMetaData(String entity) {
		return SpringApplicationContextHolder.getBean(MetaDataProvider.class).getEntity(entity);
	}

	@Override
	public String getEntityName() {
		return null;
	}

	public static class Comparator implements java.util.Comparator<AbstractNucletContentEntryTreeNode> {
		@Override
		public int compare(AbstractNucletContentEntryTreeNode o1, AbstractNucletContentEntryTreeNode o2) {
			return o1.entity == o2.entity ?
				LangUtils.compare(o1.getLabel(), o2.getLabel()) :
				LangUtils.compare(getNucletContentEntities().indexOf(o1.entity), getNucletContentEntities().indexOf(o2.entity));
		};
	}
}	// class NucletTreeNode
