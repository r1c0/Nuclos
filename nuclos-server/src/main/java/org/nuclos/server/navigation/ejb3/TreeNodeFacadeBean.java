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
package org.nuclos.server.navigation.ejb3;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.AttributeProvider;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.DatasourceCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.DefaultMasterDataTreeNode;
import org.nuclos.server.navigation.treenode.DynamicTreeNode;
import org.nuclos.server.navigation.treenode.EntitySearchResultTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.SystemRelationType;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNodeFactory;
import org.nuclos.server.navigation.treenode.GroupSearchResultTreeNode;
import org.nuclos.server.navigation.treenode.GroupTreeNode;
import org.nuclos.server.navigation.treenode.MasterDataSearchResultTreeNode;
import org.nuclos.server.navigation.treenode.MasterDataTreeNode;
import org.nuclos.server.navigation.treenode.RelationTreeNode;
import org.nuclos.server.navigation.treenode.SubFormEntryTreeNode;
import org.nuclos.server.navigation.treenode.SubFormTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NucletTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NuclosInstanceTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.AbstractNucletContentEntryTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.DefaultNucletContentEntryTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentCustomComponentTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentEntityTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentProcessTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentRuleTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.ReportNucletContentTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for managing explorer tree structures and contents.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @todo restrict
*/
@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class TreeNodeFacadeBean extends NuclosFacadeBean implements TreeNodeFacadeRemote {

	private static final int DEFAULT_ROWCOUNT_FOR_SEARCHRESULT = 500;
	
	//
	
	private ServerParameterProvider serverParameterProvider;
	
	private GenericObjectFacadeLocal genericObjectFacade;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	private LocaleFacadeLocal localeFacade;
	
	public TreeNodeFacadeBean() {
	}
	
	@Autowired
	void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
		this.serverParameterProvider = serverParameterProvider;
	}

	@Autowired
	final void setGenericObjectFacade(GenericObjectFacadeLocal genericObjectFacade) {
		this.genericObjectFacade = genericObjectFacade;
	}
	
	private final GenericObjectFacadeLocal getGenericObjectFacade() {
		return genericObjectFacade;
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}
	
	private final LocaleFacadeLocal getLocaleFacade() {
		return localeFacade;
	}

	@Autowired
	final void setLocaleFacade(LocaleFacadeLocal localeFacade) {
		this.localeFacade = localeFacade;
	}

	/**
	 * gets a generic object tree node for a specific generic object
	 * @param iGenericObjectId id of generic object to get tree node for
	 * @return generic object tree node for given id, if existing and allowed. null otherwise.
	 * @throws CommonFinderException if the object doesn't exist (anymore).
	 * @postcondition result != null
	 */
	public GenericObjectTreeNode getGenericObjectTreeNode(Integer iGenericObjectId, Integer moduleId, Integer parentId) throws CommonFinderException {
		final GenericObjectTreeNode result = newGenericObjectTreeNode(iGenericObjectId, moduleId, null, null, null, parentId);
		assert result != null;
		return result;
	}

	/**
	 * Note that user rights are ignored here.
	 * @param iGenericObjectId
	 * @param moduleId the module id
	 * @param iRelationId
	 * @param relationtype
	 * @param direction
	 * @return a new tree node for the generic object with the given id.
	 * @throws CommonFinderException if the object doesn't exist (anymore).
	 * @postcondition result != null
	 */
	public GenericObjectTreeNode newGenericObjectTreeNode(Integer iGenericObjectId,
			Integer moduleId, Integer iRelationId, SystemRelationType relationtype, RelationDirection direction, Integer parentId) throws CommonFinderException {

		// @todo 1. write/use LOFB method that doesn't require the module id
		// @todo 2. Fix BUG: getWithDependants() throws a CommonPermissionException, even if bIgnoreUser == true

		final GenericObjectFacadeLocal gofacade = this.getGenericObjectFacade();

		//final int iModuleId = gofacade.getModuleContainingGenericObject(iGenericObjectId);

		final GenericObjectWithDependantsVO gowdvo = getWithDependants(gofacade, moduleId, iGenericObjectId);

		final GenericObjectTreeNode result = GenericObjectTreeNodeFactory.getInstance().newTreeNode(gowdvo, AttributeCache.getInstance(), 
				serverParameterProvider, iRelationId, relationtype, direction, getCurrentUserName(), parentId);

		assert result != null;
		return result;
	}

	/**
	 * @param lofacade
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @return the generic object with the given id, along with necessary attributes and dependants.
	 */
	private static GenericObjectWithDependantsVO getWithDependants(GenericObjectFacadeLocal lofacade, int iModuleId, Integer iGenericObjectId)
			throws CommonFinderException {
		// WORKAROUND: Load only necessary attributes (and to avoid slow calculated attributes):
		// @todo move this workaround to GenericObjectFacadeBean

		final CollectableSearchExpression clctexpr = new CollectableSearchExpression(
				SearchConditionUtils.getCollectableSearchConditionForIds(Collections.singletonList(iGenericObjectId)));

		final List<GenericObjectWithDependantsVO> lstlowdcvo = lofacade.getGenericObjects(iModuleId, clctexpr,
				getAttributeIdsRequiredForGenericObjectTreeNode(iModuleId), getSubEntityNamesRequiredForGenericObjectTreeNode(iModuleId), true);

		switch (lstlowdcvo.size()) {
			case 0:
				throw new CommonFinderException();
			case 1:
				return lstlowdcvo.get(0);
			default:
				throw new CommonFatalException(lstlowdcvo.size() + " objects found while one was expected.");
		}
	}

	private static Set<Integer> getAttributeIdsRequiredForGenericObjectTreeNode(Integer moduleId) {
		final AttributeProvider attrprovider = AttributeCache.getInstance();

		Set<Integer> result = new HashSet<Integer>();
		result.add(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue());
		result.add(NuclosEOField.PROCESS.getMetaData().getId().intValue());
		result.add(NuclosEOField.STATE.getMetaData().getId().intValue());

		final List<MasterDataVO> lstModules = new ArrayList<MasterDataVO>();
		if (moduleId == null) {
			//choose all modules for finding required attributes for tree labels (tree node for generic search)
			lstModules.addAll(Modules.getInstance().getModules());
		} else {
			//choose requested module for finding required attributes for tree labels
			lstModules.add(Modules.getInstance().getModuleById(moduleId.intValue()));
		}

		for (final MasterDataVO mdvo : lstModules) {
			String treeView = mdvo.getField("treeview", String.class);
			if(treeView != null)
				result.addAll(
					CollectionUtils.transform(
						StringUtils.getFieldsFromTreeViewPattern(treeView),
						new Transformer<String, Integer>() {
							@Override
							public Integer transform(String s) {
								return attrprovider.getAttribute(mdvo.getField("entity", String.class), s).getId();
							}}
						));
		}
		return result;
	}

	private static Set<String> getSubEntityNamesRequiredForGenericObjectTreeNode(Integer iModuleId) {
		return Collections.emptySet();
	}

	/**
	 * gets the list of sub nodes for a specific generic object tree node.
	 * Note that there is a specific method right on this method.
	 * @param node tree node of type generic object tree node
	 * @return list of sub nodes for given tree node
	 * @throws CommonPermissionException
	 * @postcondition result != null
	 */
	public List<TreeNode> getSubNodesIgnoreUser(GenericObjectTreeNode node) {
		final List<TreeNode> result = new ArrayList<TreeNode>();
		final MasterDataVO mdcvoModule = Modules.getInstance().getModuleById(node.getModuleId());
		//add relations
		if((Boolean)mdcvoModule.getField("showrelations")) {
		  result.addAll(getRelatedSubNodes(node, RelationDirection.FORWARD));
		  result.addAll(getRelatedSubNodes(node, RelationDirection.REVERSE));
		  Collections.sort(result, new GenericObjectTreeNodeChildrenComparator());
		}

		// add groups
		if((Boolean)mdcvoModule.getField("showgroups")) {
		  result.addAll(getGroupSubNodes(node));
		}

		final String base = (String)mdcvoModule.getField("entity");
		final Collection<MasterDataVO> mds = Modules.getInstance().getSubnodesMD(base);
		final Collection<EntityTreeViewVO> etvs = Modules.getInstance().getSubnodesETV(base);
		subformSubnodes(result, node, mds, etvs);
		return result;
	}

	private void subformSubnodes(final List<TreeNode> result, TreeNode node,
		Collection<MasterDataVO> mds, Collection<EntityTreeViewVO> etvs)
	{
		final Iterator<MasterDataVO> it1 = mds.iterator();
		final Iterator<EntityTreeViewVO> it2 = etvs.iterator();
		final NavigableMap<Integer,List<? extends TreeNode>> subforms = new TreeMap<Integer,List<? extends TreeNode>>();
		// add subnodes defined in the module meta data
		while (it1.hasNext()) {
			final MasterDataVO mdvoSub = it1.next();
			final EntityTreeViewVO etv = it2.next();
			assert etv.getEntity().equals(mdvoSub.getField(EntityTreeViewVO.SUBFORM_ENTITY_FIELD))
				&& etv.getField().equals(mdvoSub.getField(EntityTreeViewVO.SUBFORM2ENTITY_REF_FIELD))
				&& IdUtils.equals(etv.getOriginentityid(), mdvoSub.getId());
				// && etv.getOriginentityid().equals(mdvoSub.getField(EntityTreeViewVO.ENTITY_FIELD));

			final String entity = etv.getEntity();
			final String field = etv.getField();
			final String foldername = etv.getFoldername();
			final boolean active = etv.isActive();
			final Integer sortOrder = etv.getSortOrder() == null ? Integer.valueOf(0) : etv.getSortOrder();

			if (node instanceof GenericObjectTreeNode) {
				final GenericObjectTreeNode gotn = (GenericObjectTreeNode) node;
				// check whether the user has the right to see the subform data
				Permission permission = SecurityCache.getInstance().getSubForm(
					getCurrentUserName(), entity).get(gotn.getStatusId());
				if (permission == null || !permission.includesReading()) {
					continue;
				}
			}

			if (active) {
				if(!org.apache.commons.lang.StringUtils.isBlank(foldername)) {
					SubFormTreeNode treenode = new SubFormTreeNode<Integer>(null, node, mdvoSub);
					treenode.getSubNodes();
					subforms.put(sortOrder, Collections.singletonList(treenode));
				}
				else {
					if(Modules.getInstance().isModuleEntity(entity)) {
						subforms.put(sortOrder, getModuleSubNodes(node, entity, field));
					}
					else {
						subforms.put(sortOrder, getMasterDataSubNodes(node, entity, field));
					}
				}
			}
		}
		for (List<? extends TreeNode> l: subforms.values()) {
			result.addAll(l);
		}
	}

	/**
	 * get subnodes of type sEntity where the valueId of sField corresponds to node.getId()
	 * @param node
	 * @param sEntity
	 * @param sField
	 * @return
	 * @throws NoSuchElementException
	 * @throws CommonFatalException
	 */
	private List<TreeNode> getModuleSubNodes(TreeNode node, final String sEntity, final String sField) throws NoSuchElementException {
		final Integer moduleId = Modules.getInstance().getModuleIdByEntityName(sEntity);
		final Integer nodeId = (Integer)node.getId();
		if(nodeId == null)
			return Collections.emptyList();
		CollectableComparison cond = SearchConditionUtils.newEOidComparison(sEntity, sField, ComparisonOperator.EQUAL, 
				IdUtils.toLongId(nodeId), MetaDataServerProvider.getInstance());

		List<Integer> lstIds = getGenericObjectFacade().getGenericObjectIds(moduleId, cond);

		List<TreeNode> result = CollectionUtils.transform(lstIds, new Transformer<Integer, TreeNode>() {

			@Override
			public TreeNode transform(Integer genericObjectId) {
				try {
					GenericObjectTreeNode node = newGenericObjectTreeNode(genericObjectId, moduleId, null, null, null, nodeId);
					return node;
				}
				catch(CommonFinderException e) {
					throw new NuclosFatalException(e);
				}
			}
		});
		return result;
	}

	/**
	 * Note that user rights are always ignored here.
	 * @param node
	 * @param direction
	 * @return the given node's subnodes related in the given direction (excluding GenericObjectTreeNode.RELATIONTYPE_INVOICE_OF).
	 * @throws CommonPermissionException
	 */
	private List<TreeNode> getRelatedSubNodes(GenericObjectTreeNode node, final RelationDirection direction) {
		final List<TreeNode> result = new ArrayList<TreeNode>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom r = query.from("T_UD_GO_RELATION").alias("r");
		DbFrom l = query.from("T_UD_GENERICOBJECT").alias("l");
		DbColumnExpression<Integer> genericObject1 = r.baseColumn(direction.isForward() ? "INTID_T_UD_GO_1" : "INTID_T_UD_GO_2", Integer.class);
		DbColumnExpression<Integer> genericObject2 = r.baseColumn(direction.isForward() ? "INTID_T_UD_GO_2" : "INTID_T_UD_GO_1", Integer.class);
		query.multiselect(
			r.baseColumn("INTID", Integer.class),
			//genericObject2,
			l.baseColumn("INTID", Integer.class),
			r.baseColumn("STRRELATIONTYPE", String.class),
			l.baseColumn("INTID_T_MD_MODULE", Integer.class));
		query.where(builder.and(
			builder.equal(genericObject2, l.baseColumn("INTID", Integer.class)),
			node.getParentId() != null ? builder.equal(genericObject2, node.getParentId()).not() : builder.alwaysTrue(),
			builder.equal(genericObject1, node.getId())//,
		//	builder.equal(l.column("BLNDELETED", Boolean.class), false)));
			));
		// order by type ???

		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query.distinct(true))) {
			final Integer iRelationId = tuple.get(0, Integer.class);
			final int iGenericObjectId = tuple.get(1, Integer.class);

			final String relationType = tuple.get(2, String.class);
			int moduleId = tuple.get(3, Integer.class);

			SystemRelationType systemRelationType = SystemRelationType.findSystemRelationType(relationType);
			if (systemRelationType != null) {
				// predecessor or part-of relation:
				try {
					result.add(newGenericObjectTreeNode(iGenericObjectId, moduleId, iRelationId, systemRelationType, direction, node.getId()));
				}
				catch (CommonFinderException ex) {
					// the object doesn't exist anymore - ignore.
				}
			}
			else {
				try {
					final GenericObjectTreeNode nodeRelatedObject = newGenericObjectTreeNode(iGenericObjectId, moduleId, null, null, null, node.getId());
					String label = getRelationTypeLabel(relationType);
					result.add(new RelationTreeNode(iRelationId, label, relationType, direction, nodeRelatedObject));
				}
				catch (CommonFinderException ex) {
					// the object doesn't exist anymore - ignore.
				}
			}
		}

		return result;
	}

	private String getRelationTypeLabel(String relationType){
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_MD_RELATIONTYPE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("STRLOCALERESOURCEID", String.class));
		query.where(builder.equal(t.baseColumn("STRRELATIONTYPE", String.class), relationType));
		String resourceId = CollectionUtils.getFirst(dataBaseHelper.getDbAccess().executeQuery(query));
		if (resourceId != null) {
			SpringLocaleDelegate.getInstance().getMessage(resourceId, relationType);
		}
		return null;
	}

	private List<TreeNode> getGroupSubNodes(GenericObjectTreeNode node) {
		final List<TreeNode> result = new ArrayList<TreeNode>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom l = query.from("T_UD_GO_GROUP").alias("l");
		DbFrom g = l.join("T_UD_GROUP", JoinType.INNER).alias("g").on("INTID_T_UD_GROUP", "INTID", Integer.class);
		DbFrom t = g.join("T_MD_GROUPTYPE", JoinType.INNER).alias(SystemFields.BASE_ALIAS).on("INTID_T_MD_GROUPTYPE", "INTID", Integer.class);
		query.multiselect(
			l.baseColumn("INTID_T_UD_GROUP", Integer.class),
			g.baseColumn("STRGROUP", String.class),
			g.baseColumn("STRDESCRIPTION", String.class),
			t.baseColumn("STRNAME", String.class));
		query.where(builder.equal(l.baseColumn("INTID_T_UD_GROUP", Integer.class), node.getId()));
		query.orderBy(builder.asc(g.baseColumn("STRGROUP", String.class)), builder.desc(g.baseColumn("DATVALIDFROM", Date.class)));

		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query)) {
			final GroupTreeNode nodeGroupDefined = new GroupTreeNode(
				tuple.get(0, Integer.class), tuple.get(1, String.class),
				tuple.get(2, String.class), tuple.get(3, String.class));
			nodeGroupDefined.getSubNodes();
			result.add(nodeGroupDefined);
		}

		return result;
	}

	// group:

	/**
	 * method to get a group tree node for a specific group
	 * @param iId id of group to get tree node for
	 * @return group tree node for given id
	 * @postcondition result != null
	 */
	public GroupTreeNode getGroupTreeNode(final Integer iId, final boolean bLoadSubNodes) throws CommonFinderException {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_UD_GROUP").alias(SystemFields.BASE_ALIAS);
		DbFrom fk1 = t.join("T_MD_GROUPTYPE", JoinType.INNER).alias("fk1").on("INTID_T_MD_GROUPTYPE", "INTID", Integer.class);
		query.multiselect(
			t.baseColumn("STRGROUP", String.class),
			fk1.baseColumn("STRNAME", String.class),
			t.baseColumn("STRDESCRIPTION", String.class));
		query.where(builder.equal(t.baseColumn("INTID", Integer.class), iId));

		DbTuple tuple;
		try {
			tuple = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		} catch (DbInvalidResultSizeException e) {
			throw new NuclosFatalException("treenode.error.missing.group");//"Die Gruppe existiert nicht mehr.");
		}

		final GroupTreeNode result = new GroupTreeNode(iId, tuple.get(0, String.class),
			tuple.get(1, String.class), tuple.get(2, String.class));

		if (bLoadSubNodes) {
			result.getSubNodes();
		}
		return result;
	}

	public NucletTreeNode getNucletTreeNode(Integer iId) throws CommonFinderException {
		try {
			EntityObjectVO eovo = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLET).getByPrimaryKey(iId.longValue());
			return new NucletTreeNode(eovo, false);
		} catch (Exception ex) {
			throw new CommonFinderException();
		}
	}

	/**
	 * method to get a masterdata tree node for a specific masterdata record
	 * @param iId id of masterdata record to get tree node for
	 * @return masterdata tree node for given id
	 * @throws CommonPermissionException
	 * @postcondition result != null
	 */
	public MasterDataTreeNode<Integer> getMasterDataTreeNode(Integer iId, String sEntity, boolean bLoadSubNodes) throws CommonFinderException, CommonPermissionException {
		final MasterDataVO mdvo = this.getMasterDataFacade().get(sEntity, iId);
		final MasterDataTreeNode<Integer> result = new DefaultMasterDataTreeNode(sEntity, mdvo);

		if (bLoadSubNodes) {
			result.getSubNodes();
		}
		assert result != null;
		return result;
	}

	public SubFormEntryTreeNode getSubFormEntryTreeNode(Integer iId, String sEntity, boolean bLoadSubNodes) throws CommonFinderException, CommonPermissionException {
		final MasterDataVO mdvo = this.getMasterDataFacade().get(sEntity, iId);
		final SubFormEntryTreeNode result = new SubFormEntryTreeNode(sEntity, mdvo);

		if (bLoadSubNodes) {
			result.getSubNodes();
		}
		assert result != null;
		return result;
	}

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	public List<GenericObjectTreeNode> getSubNodes(GroupTreeNode node) {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom grp = query.from("T_UD_GO_GROUP").alias("grp");
		DbFrom gob = grp.join("T_UD_GENERICOBJECT", JoinType.INNER).alias("gob").on("INTID_T_UD_GENERICOBJECT", "INTID", Integer.class);
		query.multiselect(
			grp.baseColumn("INTID_T_UD_GENERICOBJECT", Integer.class),
			gob.baseColumn("INTID_T_MD_MODULE", Integer.class));
		query.where(builder.equal(grp.baseColumn("INTID_T_UD_GROUP", Integer.class), node.getId()));
		query.orderBy(builder.asc(grp.baseColumn("DATCREATED", Date.class)));

		final List<GenericObjectTreeNode> result = new ArrayList<GenericObjectTreeNode>();
		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query)) {
			try {
				result.add(getGenericObjectTreeNode(tuple.get(0, Integer.class), tuple.get(1, Integer.class), null));
			}
			catch (CommonFinderException ex) {
				// the object doesn't exist anymore - ignore.
			}
		}
		Collections.sort(result);
		return result;
	}

	public List<TreeNode> getSubNodes(NucletTreeNode node) {
		List<TreeNode> result = new ArrayList<TreeNode>();
		if (node.isShowDependeces()) {

			CollectableSearchCondition cond = org.nuclos.common.SearchConditionUtils.newEOidComparison(
				NuclosEntity.NUCLETDEPENDENCE.getEntityName(),
				"nuclet",
				ComparisonOperator.EQUAL,
				IdUtils.toLongId(node.getId()),
				MetaDataServerProvider.getInstance());

			List<NucletTreeNode> nucletNodes = new ArrayList<NucletTreeNode>();
			for (EntityObjectVO eoDependence : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETDEPENDENCE).getBySearchExpression(new CollectableSearchExpression(cond))) {
				EntityObjectVO eoNuclet = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLET).getByPrimaryKey(eoDependence.getFieldId("nucletDependence"));
				nucletNodes.add(new NucletTreeNode(eoNuclet, true));
			}
			result.addAll(CollectionUtils.sorted(nucletNodes, new Comparator<NucletTreeNode>() {
				@Override
				public int compare(NucletTreeNode o1, NucletTreeNode o2) {
					return LangUtils.compare(o1.getLabel(), o2.getLabel());
				}}));
		} else {
			result.addAll(getNucletContentTypes(IdUtils.toLongId(node.getId())));
		}
		return result;
	}

	private List<NucletContentTreeNode> getNucletContentTypes(Long nucletId) {
		List<NucletContentTreeNode> result = new ArrayList<NucletContentTreeNode>();
		for (NuclosEntity ne : AbstractNucletContentEntryTreeNode.getNucletContentEntities()) {
			switch (ne) {
			case REPORT:
				result.add(new ReportNucletContentTreeNode(nucletId));
				break;
			default:
				result.add(new NucletContentTreeNode(nucletId, ne));	
			}
		}
		return result;
	}

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	public List<TreeNode> getSubnodes(DefaultMasterDataTreeNode node) {
		final List<TreeNode> result = new ArrayList<TreeNode>();
		final MasterDataMetaVO mdMeta = MasterDataMetaCache.getInstance().getMetaData(
			node.getEntityName());
		final Collection<MasterDataVO> colSubNodes = MasterDataMetaCache.getInstance().getSubnodesMD(
			node.getEntityName(), mdMeta.getId());
		final Collection<EntityTreeViewVO> subnodes = MasterDataMetaCache.getInstance().getSubnodesETV(
			node.getEntityName(), mdMeta.getId());

		subformSubnodes(result, node, colSubNodes, subnodes);
		return result;
	}

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	public List<AbstractNucletContentEntryTreeNode> getSubNodes(NucletContentTreeNode node) {
		final List<AbstractNucletContentEntryTreeNode> result = new ArrayList<AbstractNucletContentEntryTreeNode>();

		EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(node.getEntity());
		EntityFieldMetaDataVO efMetaNuclet = MetaDataServerProvider.getInstance().getEntityField(eMeta.getEntity(), AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET);

		CollectableSearchCondition cond = org.nuclos.common.SearchConditionUtils.newEOidComparison(
			eMeta.getEntity(),
			efMetaNuclet.getField(),
			ComparisonOperator.EQUAL,
			node.getNucletId(),
			MetaDataServerProvider.getInstance());

		for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(node.getEntity()).getBySearchExpression(new CollectableSearchExpression(cond))) {
			result.add(getNucletContentEntryNode(eo));
		}

		return sortAbstractNucletContentEntryTreeNodes(result);
	}

	public List<AbstractNucletContentEntryTreeNode> getNucletContent(NucletTreeNode node) {
		final List<AbstractNucletContentEntryTreeNode> result = new ArrayList<AbstractNucletContentEntryTreeNode>();

		for (NucletContentTreeNode contentTypeNode : getNucletContentTypes(IdUtils.toLongId(node.getId()))) {
			result.addAll(getSubNodes(contentTypeNode));
		}
		return result;
	}

	public List<NucletTreeNode> getSubNodes(NuclosInstanceTreeNode node) {
		final Collection<NucletTreeNode> result = new ArrayList<NucletTreeNode>();

		EntityMetaDataVO eMetaDependence = MetaDataServerProvider.getInstance().getEntity(NuclosEntity.NUCLETDEPENDENCE);
		EntityFieldMetaDataVO efMetaDependence = MetaDataServerProvider.getInstance().getEntityField(eMetaDependence.getEntity(), "nucletDependence");

		for (EntityObjectVO eoNuclet : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLET).getAll()) {

			CollectableSearchCondition cond = org.nuclos.common.SearchConditionUtils.newEOidComparison(
				eMetaDependence.getEntity(),
				efMetaDependence.getField(),
				ComparisonOperator.EQUAL,
				eoNuclet.getId(),
				MetaDataServerProvider.getInstance());

			if (NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETDEPENDENCE)
				.count(new CollectableSearchExpression(cond)) == 0) {
				// is root Nuclet
				result.add(new NucletTreeNode(eoNuclet, true));
			}
		}

		return CollectionUtils.sorted(result, new Comparator<NucletTreeNode>() {
			@Override
			public int compare(NucletTreeNode o1, NucletTreeNode o2) {
				return LangUtils.compare(o1.getLabel(), o2.getLabel());
			}});
	}

	public List<AbstractNucletContentEntryTreeNode> getAvailableNucletContents() {
		List<AbstractNucletContentEntryTreeNode> result = new ArrayList<AbstractNucletContentEntryTreeNode>();
		for (NuclosEntity ne : AbstractNucletContentEntryTreeNode.getNucletContentEntities()) {
			EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(ne);
			EntityFieldMetaDataVO efMetaNuclet = MetaDataServerProvider.getInstance().getEntityField(eMeta.getEntity(), AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET);

			CollectableSearchCondition cond = org.nuclos.common.SearchConditionUtils.newEOIsNullComparison(
				eMeta.getEntity(),
				efMetaNuclet.getField(),
				ComparisonOperator.IS_NULL,
				MetaDataServerProvider.getInstance());
			
			switch(ne) {
			case WORKSPACE:
				cond = SearchConditionUtils.and(cond, SearchConditionUtils.newEOIsNullComparison(
						ne.getEntityName(), 
						"user", 
						ComparisonOperator.IS_NULL, 
						MetaDataServerProvider.getInstance()));
				break;
			}

			List<AbstractNucletContentEntryTreeNode> nodes = new ArrayList<AbstractNucletContentEntryTreeNode>();
			for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(ne).getBySearchExpression(new CollectableSearchExpression(cond))) {
				nodes.add(getNucletContentEntryNode(eo));
			}
			result.addAll(sortAbstractNucletContentEntryTreeNodes(nodes));
		}
		return result;
	}

	public void addNucletContents(Long nucletId, Set<AbstractNucletContentEntryTreeNode> contents) throws NuclosBusinessException {
		CacheInvalidator ci = new CacheInvalidator();

		for (AbstractNucletContentEntryTreeNode node : contents) {
			final JdbcEntityObjectProcessor processor = NucletDalProvider.getInstance().getEntityObjectProcessor(node.getEntity());
			final EntityObjectVO eo = processor.getByPrimaryKey(node.getId()); //reload the content, no version check here!

			if (eo.getFieldIds().get(AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET) != null) {
				if (LangUtils.equals(nucletId, eo.getFieldIds().get(AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET))) {
					continue;
				} else {
					throw new NuclosBusinessException("treenode.facade.businessexception.1");
				}
			}

			eo.getFieldIds().put(AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET, nucletId);
			eo.flagUpdate();
			DalUtils.updateVersionInformation(eo, getCurrentUserName());
			processor.insertOrUpdate(eo);

			ci.handleNode(node);
		}

		ci.run();
	}

	public boolean removeNucletContents(Set<AbstractNucletContentEntryTreeNode> contents) {
		boolean result = false;
		CacheInvalidator ci = new CacheInvalidator();

		for (AbstractNucletContentEntryTreeNode node : contents) {
			final JdbcEntityObjectProcessor processor = NucletDalProvider.getInstance().getEntityObjectProcessor(node.getEntity());
			final EntityObjectVO eo = processor.getByPrimaryKey(node.getId()); //reload the content, no version check here!

			if (eo.getFieldIds().containsKey(AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET)) {
				result = true;
				eo.getFieldIds().put(AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET, null);
				eo.flagUpdate();
				DalUtils.updateVersionInformation(eo, getCurrentUserName());
				processor.insertOrUpdate(eo);
			}

			if (result)
				ci.handleNode(node);
		}

		ci.run();
		return result;
	}

	private class CacheInvalidator {
		boolean invalidateRuleCache = false;
		boolean invalidateDatasourceCache = false;
		public void handleNode(AbstractNucletContentEntryTreeNode node) {
			if (node.getEntity() == NuclosEntity.RULE ||
				node.getEntity() == NuclosEntity.TIMELIMITRULE ||
				node.getEntity() == NuclosEntity.CODE) {
				invalidateRuleCache = true;
			}
			if (node.getEntity() == NuclosEntity.DATASOURCE ||
				node.getEntity() == NuclosEntity.DYNAMICENTITY ||
				node.getEntity() == NuclosEntity.VALUELISTPROVIDER ||
				node.getEntity() == NuclosEntity.RECORDGRANT) {
				invalidateDatasourceCache = true;
			}
		}
		public void run() {
			if (invalidateRuleCache) RuleCache.getInstance().invalidate();
			if (invalidateDatasourceCache) DatasourceCache.getInstance().invalidate();
		}
	}

	private List<AbstractNucletContentEntryTreeNode> sortAbstractNucletContentEntryTreeNodes(List<AbstractNucletContentEntryTreeNode> nodes) {
		return CollectionUtils.sorted(nodes, new AbstractNucletContentEntryTreeNode.Comparator());
	}

	public AbstractNucletContentEntryTreeNode getNucletContentEntryNode(NuclosEntity entity, Long eoId) {
		return getNucletContentEntryNode(NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getByPrimaryKey(eoId));
	}

	private AbstractNucletContentEntryTreeNode getNucletContentEntryNode(EntityObjectVO eo) {
		if (eo == null) {
			throw new IllegalArgumentException("eo must not be null");
		}
		NuclosEntity entity = NuclosEntity.getByName(eo.getEntity());
		if (entity == null) {
			throw new IllegalArgumentException("entity object must be nuclos entity");
		}
		switch (entity) {
			case ENTITY:
				return new NucletContentEntityTreeNode(eo);
			case CUSTOMCOMPONENT:
				return new NucletContentCustomComponentTreeNode(eo);
			case RULE:
				return new NucletContentRuleTreeNode(eo);
			case PROCESS:
				return new NucletContentProcessTreeNode(eo);
			default:
				return new DefaultNucletContentEntryTreeNode(eo);
		}
	}

	public DynamicTreeNode<Integer> getDynamicTreeNode(TreeNode node, MasterDataVO mdVO) {
		return new DynamicTreeNode<Integer>(null, node, mdVO);
	}

	public SubFormTreeNode getSubFormTreeNode(GenericObjectTreeNode node, MasterDataVO mdVO) {
		return new SubFormTreeNode(null, node, mdVO);
	}

	public List<TreeNode> getSubNodesForDynamicTreeNode(TreeNode node, MasterDataVO mdVO) {
		final String sEntity = (String)mdVO.getField("entity");
		final String sField = (String)mdVO.getField("field");

		List<TreeNode> result = new ArrayList<TreeNode>();
		if(Modules.getInstance().isModuleEntity(sEntity)) {
			result.addAll(getModuleSubNodes(node, sEntity, sField));
		}
		else {
			result.addAll(getMasterDataSubNodes(node, sEntity, sField));
		}

		return result;
	}

    public List<SubFormEntryTreeNode> getSubNodesForSubFormTreeNode(TreeNode node, MasterDataVO mdVO) {
		final String sEntity = (String)mdVO.getField("entity");
		final String sField = (String)mdVO.getField("field");

		final Collection<MasterDataVO> colmdvo = this.getMasterDataFacade().getDependantMasterData(sEntity, sField, node.getId());
		final Collection<SubFormEntryTreeNode> colResult = CollectionUtils.transform(colmdvo, new Transformer<MasterDataVO, SubFormEntryTreeNode>() {
			@Override
			public SubFormEntryTreeNode transform(MasterDataVO i) {
				return new SubFormEntryTreeNode(sEntity, i);
			}
		});

		final List<SubFormEntryTreeNode> result = new ArrayList<SubFormEntryTreeNode>(colResult);
		Collections.sort(result);
		return result;
	}

	/**
	 * get the masterdata subnodes for the given node
	 * @param node
	 * @param sEntity
	 * @param sField
	 * @return
	 */
	private List<DefaultMasterDataTreeNode> getMasterDataSubNodes(TreeNode node, final String sEntity, String sField) {
		final Collection<MasterDataVO> colmdvo = this.getMasterDataFacade().getDependantMasterData(sEntity, sField, node.getId());
		final Collection<DefaultMasterDataTreeNode> colResult = CollectionUtils.transform(colmdvo, new Transformer<MasterDataVO, DefaultMasterDataTreeNode>() {
			@Override
			public DefaultMasterDataTreeNode transform(MasterDataVO i) {
				return new DefaultMasterDataTreeNode(sEntity, i);
			}
		});

		final List<DefaultMasterDataTreeNode> result = new ArrayList<DefaultMasterDataTreeNode>(colResult);
		Collections.sort(result);
		return result;

	}

	public java.util.List<org.nuclos.server.navigation.treenode.GroupTreeNode> getSubNodes(GroupSearchResultTreeNode node) {
		final List<GroupTreeNode> result = new ArrayList<GroupTreeNode>();

		for (MasterDataVO mdvo : this.getMasterDataFacade().getMasterData(NuclosEntity.GROUP.getEntityName(), appendRecordGrants(node.getSearchCondition(), NuclosEntity.GROUP.getEntityName()), false)) {
			result.add(new GroupTreeNode(mdvo.getIntId(), mdvo.getField("name", String.class),
					mdvo.getField("grouptype", String.class), mdvo.getField("description", String.class)));
		}

		Collections.sort(result);
		assert result != null;
		return result;
	}

	/**
	 * get the subnodes for a masterdata search result
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	public java.util.List<org.nuclos.server.navigation.treenode.DefaultMasterDataTreeNode> getSubNodes(MasterDataSearchResultTreeNode node) {
		final List<DefaultMasterDataTreeNode> result = new ArrayList<DefaultMasterDataTreeNode>();

		for (MasterDataVO mdvo : this.getMasterDataFacade().getMasterData(node.getEntity(), appendRecordGrants(node.getSearchCondition(), node.getEntity()), false)) {
			result.add(new DefaultMasterDataTreeNode(node.getEntity(), mdvo));
		}

		Collections.sort(result);
		assert result != null;
		return result;
	}


	/**
	 * method to get the list of sub nodes for a specific generic object search result tree node
	 * @param node tree node of type search result tree node
	 * @return list of sub nodes for given tree node
	 * @postcondition result != null
	 */
	public List<TreeNode> getSubNodes(EntitySearchResultTreeNode node) {
		final int iMaxRowCount = serverParameterProvider.getIntValue(ParameterProvider.KEY_MAX_ROWCOUNT_FOR_SEARCHRESULT_IN_TREE, DEFAULT_ROWCOUNT_FOR_SEARCHRESULT);

		if (Modules.getInstance().isModuleEntity(node.getEntity())) {
			final AttributeProvider attrprovider = AttributeCache.getInstance();

			final List<TreeNode> result = new ArrayList<TreeNode>(Math.min(iMaxRowCount, DEFAULT_ROWCOUNT_FOR_SEARCHRESULT));
			Integer iModuleId = Modules.getInstance().getModuleIdByEntityName(node.getEntity());
			final TruncatableCollection<GenericObjectWithDependantsVO> collgowdvo =
					this.getGenericObjectFacade().getRestrictedNumberOfGenericObjects(iModuleId, appendRecordGrants(node.getSearchExpression(), node.getEntity()),
							getAttributeIdsRequiredForGenericObjectTreeNode(iModuleId), getSubEntityNamesRequiredForGenericObjectTreeNode(iModuleId), iMaxRowCount);

			for (GenericObjectWithDependantsVO gowdvo : collgowdvo) {
				result.add(GenericObjectTreeNodeFactory.getInstance().newTreeNode(gowdvo, attrprovider, serverParameterProvider, null, null, null, getCurrentUserName(), null));
			}

			String sLabel = MessageFormat.format(getLocaleFacade().getResourceById(getLocaleFacade().getUserLocale(), "treenode.subnode.label"), node.getLabel(), collgowdvo.size(), collgowdvo.totalSize());
			if (collgowdvo.isTruncated()) {
				node.setLabel(sLabel);
					//node.getLabel() + " (begrenzt auf " + collgowdvo.size() + " von " + collgowdvo.totalSize() + " Ergebnissen)");
			}
			/** @todo OPTIMIZE: sort in the database! */
			//Collections.sort(result);

			assert result != null;
			return result;
		} else {
			final List<TreeNode> result = new ArrayList<TreeNode>(Math.min(iMaxRowCount, DEFAULT_ROWCOUNT_FOR_SEARCHRESULT));

			final TruncatableCollection<MasterDataVO> collmdvo = this.getMasterDataFacade().getMasterData(node.getEntity(), appendRecordGrants(node.getSearchExpression(), node.getEntity()).getSearchCondition(), false);
			for (MasterDataVO mdvo : collmdvo) {
				result.add(new DefaultMasterDataTreeNode(node.getEntity(), mdvo));
			}
			String sLabel = MessageFormat.format(getLocaleFacade().getResourceById(getLocaleFacade().getUserLocale(), "treenode.subnode.label"), node.getLabel(), collmdvo.size(), collmdvo.totalSize());
			if (collmdvo.isTruncated()) {
				node.setLabel(sLabel);
					//node.getLabel() + " begrenzt auf " + collmdvo.size() + " von " + collmdvo.totalSize() + " Ergebnissen)");
			}

			//Collections.sort(result);
			assert result != null;
			return result;
		}
	}

	private static class GenericObjectTreeNodeChildrenComparator implements Comparator<TreeNode> {
		@Override
		public int compare(TreeNode tn1, TreeNode tn2) {
			int result = getOrder(tn1) - getOrder(tn2);
			if (result == 0) {
				if (tn1 instanceof GenericObjectTreeNode) {
					result = ((Comparable<GenericObjectTreeNode>) tn1).compareTo((GenericObjectTreeNode) tn2);
				}
				else if (tn1 instanceof RelationTreeNode) {
					result = ((Comparable<TreeNode>) tn1).compareTo(tn2);
				}
				else {
					throw new CommonFatalException("Cannot compare the given TreeNodes.");
				}
			}
			return result;
		}

		private int getOrder(TreeNode treenode) {
			return LangUtils.isInstanceOf(treenode, GenericObjectTreeNode.class) ? 1 : 2;
		}

	}	// inner class GenericObjectTreeNodeChildrenComparator
}
