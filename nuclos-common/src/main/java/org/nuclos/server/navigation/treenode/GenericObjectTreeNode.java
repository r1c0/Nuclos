// Copyright (C) 2010 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.server.navigation.treenode;

import java.io.ObjectStreamException;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.ejb3.SecurityFacadeRemote;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeRemote;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;

/**
 * Abstract tree node implementation representing a generic object. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class GenericObjectTreeNode extends AbstractTreeNode<Integer> implements
	Comparable<GenericObjectTreeNode> {

	private static final Logger LOG = Logger.getLogger(GenericObjectTreeNode.class);

	private SecurityFacadeRemote		facade;
	private GenericObjectFacadeRemote	gofacade;

	public static enum SystemRelationType implements KeyEnum<String> {

		/** relation type: "is predecessor of" */
		PREDECESSOR_OF("PredecessorOf"),
		/** relation type: "is part of" */
		PART_OF("PartOf");

		private String	name;

		private SystemRelationType(String name) {
			this.name = name;
		}

		@Override
		public String getValue() {
			return name;
		}

		public static SystemRelationType findSystemRelationType(
			String relationType) {
			return KeyEnum.Utils.findEnum(SystemRelationType.class,
				relationType);
		}
	} // enum SystemRelationType

	public enum RelationDirection {

		/**
		 * relation direction: forward, eg. (lo1, lo2, part-of, forward) means:
		 * "lo1 is-part-of lo2"
		 */
		FORWARD,
		/**
		 * relation direction: reverse, eg. (lo1, lo2, part-of, reverse) means:
		 * "lo2 is-part-of lo1"
		 */
		REVERSE;

		public boolean isForward() {
			return this == FORWARD;
		}

		public boolean isReverse() {
			return this == REVERSE;
		}

	} // enum RelationDirection

	private final UsageCriteria			usagecriteria;
	private final String				sIdentifier;
	private final Integer				iParentId;
	private final Integer				iRelationId;
	private final SystemRelationType	relationtype;
	private final RelationDirection		direction;
	private String						sUserName;
	private Integer						iStatusId;
	private String						sEntity;

	protected GenericObjectTreeNode(GenericObjectWithDependantsVO gowdvo,
		AttributeProvider attrprovider, Integer iRelationId,
		SystemRelationType relationtype, RelationDirection direction,
		String sUserName, String label, String description, Integer parentId) {
		this(gowdvo.getId(), gowdvo.getUsageCriteria(attrprovider),
			gowdvo.getSystemIdentifier(), parentId, iRelationId,
			relationtype, direction, sUserName, gowdvo.getStatusId());
		this.setLabel(label);
		this.setChangedAt(gowdvo.getChangedAt());
		this.setDescription(description);
	}

	protected GenericObjectTreeNode(Integer iId, UsageCriteria usagecriteria,
		String sIdentifier, Integer iParentId, Integer iRelationId,
		SystemRelationType relationtype, RelationDirection direction,
		String sUserName, Integer iStatusId) {

		super(iId);

		this.usagecriteria = usagecriteria;
		this.sIdentifier = sIdentifier;
		this.iParentId = iParentId;
		this.iRelationId = iRelationId;
		this.relationtype = relationtype;
		this.direction = direction;
		this.sUserName = sUserName;
		this.iStatusId = iStatusId;

		if (usagecriteria != null) {
			MetaDataProvider provider = SpringApplicationContextHolder.getBean(MetaDataProvider.class);
			if (provider != null) {
				sEntity = provider.getEntity(usagecriteria.getModuleId().longValue()).getEntity();
			}
		}
	}

	/**
	 * @return module id of leased object tree node
	 */
	public Integer getModuleId() {
		return this.usagecriteria.getModuleId();
	}

	/**
	 * @return process id of leased object tree node
	 */
	public Integer getProcessId() {
		return this.usagecriteria.getProcessId();
	}

	/**
	 * @return identifier of generic object tree node
	 */
	@Override
	public String getIdentifier() {
		return this.sIdentifier;
	}

	/**
	 * @return the parent id, if any, of the generic object.
	 */
	public Integer getParentId() {
		return this.iParentId;
	}

	/**
	 * @return Is this node related to another?
	 */
	public boolean isRelated() {
		return this.getRelationId() != null;
	}

	/**
	 * @return the id of the relation, if any.
	 */
	public Integer getRelationId() {
		return this.iRelationId;
	}

	/**
	 * @return the type of the relation, if any.
	 */
	public SystemRelationType getRelationType() {
		return this.relationtype;
	}

	/**
	 * @return the direction of the relation, if any.
	 */
	public RelationDirection getRelationDirection() {
		return this.direction;
	}

	/**
	 * @postcondition result != null
	 */
	public UsageCriteria getUsageCriteria() {
		return this.usagecriteria;
	}

	/**
	 * set the status id
	 */
	public void setStatusId(Integer iStatusId) {
		this.iStatusId = iStatusId;
	}

	/**
	 * @return the status id.
	 */
	public Integer getStatusId() {
		return this.iStatusId;
	}

	@Override
	public boolean implementsNewRefreshMethod() {
		return true;
	}

	/**
	 * @postcondition result != null
	 * @throws CommonFinderException
	 */
	@Override
	public GenericObjectTreeNode refreshed() throws CommonFinderException {
		return getTreeNodeFacade().getGenericObjectTreeNode(this.getId(),
			usagecriteria.getModuleId(), this.getParentId());
	}

	@Override
	protected List<? extends TreeNode> getSubNodesImpl() throws RemoteException {
		return getTreeNodeFacade().getSubNodesIgnoreUser(this);
	}

	@Override
	public String getEntityName() {
		return this.sEntity;
	}

	/**
	 * orders <code>GenericObjectTreeNode</code>s by relation type, direction,
	 * module id and label.
	 */
	@Override
	public int compareTo(GenericObjectTreeNode that) {
		// 1. order by relation type
		// Note that getRelationtype() may be null:
		int result = LangUtils.compare(this.getRelationType(),
			that.getRelationType());

		// 2. FORWARD < REVERSE so forward relationships are shown before
		// reverse relationships.
		// Note that getDirection() may be null:
		if(result == 0) {
			result = LangUtils.compare(this.getRelationDirection(),
				that.getRelationDirection());
		}

		// 3. order by module id
		if(result == 0) {
			result = this.getModuleId().compareTo(that.getModuleId());
		}

		// 4. order by label:
		if(result == 0) {
			result = this.getLabel().compareTo(that.getLabel());
		}

		return result;
	}

	private static TreeNodeFacadeRemote getTreeNodeFacade() {
		try {
			return ServiceLocator.getInstance().getFacade(
				TreeNodeFacadeRemote.class);
		}
		catch(RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	private String getUserName() {
		return this.sUserName;
	}

	/**
	 * Don't use @Override as there is nothing to override seen by the
	 * (maven) java compiler.
	 * 
	 * Must be public and must not throw ObjectStreamException
	 * {@link org.springframework.beans.factory.aspectj.AbstractInterfaceDrivenDependencyInjectionAspect}.
	 */
	public Object readResolve() {
		if(this.getUserName() == null) {
			try {
				this.sUserName = getSecurityFacade().getSessionContextAsString();
			}
			catch(NuclosFatalException e) {
				throw new IllegalStateException("Username could not be set.");
			}
			catch(RuntimeException ex) {
				throw new IllegalStateException("Username could not be set.");
			}
		}

		if(this.getStatusId() == null) {
			try {
				this.iStatusId = getGenericObjectFacade().getStateIdByGenericObject(
					this.getId());
			}
			catch(NuclosFatalException e) {
				throw new IllegalStateException("StatusId could not be set.");
			}
			catch(RuntimeException ex) {
				throw new IllegalStateException("Username could not be set.");
			}
			catch(CommonFinderException e) {
				throw new IllegalStateException("StatusId could not be set.");
			}
		}
		return this;
	}

	/**
	 * gets the facade once for this object and stores it in a member variable.
	 */
	private SecurityFacadeRemote getSecurityFacade()
		throws NuclosFatalException {
		if(this.facade == null) {
			try {
				this.facade = ServiceLocator.getInstance().getFacade(
					SecurityFacadeRemote.class);
			}
			catch(RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return this.facade;
	}

	/**
	 * gets the facade once for this object and stores it in a member variable.
	 */
	private GenericObjectFacadeRemote getGenericObjectFacade()
		throws NuclosFatalException {
		if(this.gofacade == null) {
			try {
				this.gofacade = ServiceLocator.getInstance().getFacade(
					GenericObjectFacadeRemote.class);
			}
			catch(RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return this.gofacade;
	}
} // class GenericObjectTreeNode
