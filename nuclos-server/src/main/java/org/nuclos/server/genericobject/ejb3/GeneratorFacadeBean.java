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
package org.nuclos.server.genericobject.ejb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.ejb3.LayoutFacadeLocal;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.GeneratorRuleVO;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GeneratorUsageVO;
import org.nuclos.server.genericobject.valueobject.GeneratorVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeLocal;
import org.nuclos.server.ruleengine.valueobject.RuleEngineGenerationVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO.Event;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for all generic object generator functions. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional
public class GeneratorFacadeBean extends NuclosFacadeBean implements GeneratorFacadeRemote {

	private static final Integer iAttributeIdOrigin = NuclosEOField.ORIGIN.getMetaData().getId().intValue();
	private static final Integer iAttributeIdIdentifier = NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue();

	private List<String> systemAttributes;
	private final Collection<Integer> collExcludedAttributeIds = new HashSet<Integer>();
	
	private GenericObjectFacadeLocal genericObjectFacade;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	public GeneratorFacadeBean() {
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

	@PostConstruct
	@RolesAllowed("Login")
	public void postConstruct() {

		systemAttributes = CollectionUtils.transform(NuclosEOField.values(), new Transformer<NuclosEOField, String>() {
			@Override
			public String transform(NuclosEOField i) {
				return i.getName();
			}
		});

		collExcludedAttributeIds.add(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue());
		collExcludedAttributeIds.add(NuclosEOField.ORIGIN.getMetaData().getId().intValue());
		collExcludedAttributeIds.add(NuclosEOField.CHANGEDAT.getMetaData().getId().intValue());
		collExcludedAttributeIds.add(NuclosEOField.CHANGEDBY.getMetaData().getId().intValue());
		collExcludedAttributeIds.add(NuclosEOField.CREATEDAT.getMetaData().getId().intValue());
		collExcludedAttributeIds.add(NuclosEOField.CREATEDBY.getMetaData().getId().intValue());
		collExcludedAttributeIds.add(NuclosEOField.PROCESS.getMetaData().getId().intValue());
		collExcludedAttributeIds.add(NuclosEOField.STATE.getMetaData().getId().intValue());
		collExcludedAttributeIds.add(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());
	}

	/**
	 * @return all generator actions
	 */
	@RolesAllowed("Login")
	public GeneratorVO getGeneratorActions() throws CommonPermissionException {
		List<GeneratorActionVO> actions = new LinkedList<GeneratorActionVO>();
		CollectableComparison cond = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.GENERATION), "ruleonly", ComparisonOperator.EQUAL, false);
		Collection<MasterDataVO> mdGenerationsVO = getMasterDataFacade().getMasterData(NuclosEntity.GENERATION.getEntityName(), cond, true);
		for (MasterDataVO mdVO : mdGenerationsVO) {
			actions.add(MasterDataWrapper.getGeneratorActionVO(mdVO, getGeneratorUsages(mdVO.getIntId())));
		}
		return new GeneratorVO(actions);
	}

	/**
	 * generate a new generic object from an existing generic object (copying
	 * attributes)
	 *
	 * @param iSourceGenericObjectId
	 *            source generic object id to generate from
	 * @param sGenerator
	 *            name of object generation to determine what to do
	 * @return id of generated generic object (if exactly one object was
	 *         generated)
	 */
	public Long generateGenericObject(Long iSourceObjectId, String sGenerator) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonStaleVersionException, CommonValidationException {
		return generateGenericObject(iSourceObjectId, null, getGeneratorActionByName(sGenerator)).getGeneratedObject().getId();
	}

	/**
	 * generate one or more generic objects from an existing generic object
	 * (copying selected attributes and subforms)
	 *
	 * @param iSourceGenericObjectId
	 *            source generic object id to generate from
	 * @param generatoractionvo
	 *            generator action value object to determine what to do
	 * @return id of generated generic object (if exactly one object was
	 *         generated)
	 * @nucleus.permission mayWrite(generatoractionvo.getTargetModuleId())
	 */
	@RolesAllowed("Login")
	public GenerationResult generateGenericObject(Long iSourceObjectId, Long parameterObjectId, GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonStaleVersionException, CommonValidationException {
		EntityMetaDataVO sourceMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getSourceModuleId()));
		EntityMetaDataVO targetMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getTargetModuleId()));

		this.checkWriteAllowed(targetMeta.getEntity());

		JdbcEntityObjectProcessor proc = NucletDalProvider.getInstance().getEntityObjectProcessor(sourceMeta.getEntity());
		EntityObjectVO eo = proc.getByPrimaryKey(iSourceObjectId);
		return generateGenericObject(Collections.singletonList(eo), parameterObjectId, generatoractionvo);
	}

	@RolesAllowed("Login")
	public Long generateGenericObject(RuleObjectContainerCVO loccvoSource, String sGenerator) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonStaleVersionException, CommonValidationException {
		GeneratorActionVO generator = getGeneratorActionByName(sGenerator);
		EntityMetaDataVO meta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generator.getSourceModuleId()));

		final EntityObjectVO source;
		if (meta.isStateModel()) {
			source = DalSupportForGO.wrapGenericObjectVO(loccvoSource.getGenericObject());
		}
		else {
			source = DalSupportForMD.getEntityObjectVO(meta.getEntity(), loccvoSource.getMasterData());
			// source.setEntity(meta.getEntity());
		}
		return generateGenericObject(Collections.singletonList(source), null, generator).getGeneratedObject().getId();
	}

	@RolesAllowed("Login")
	public Map<String, Collection<EntityObjectVO>> groupObjects(Collection<Long> sourceIds, GeneratorActionVO generatoractionvo) {
		final EntityMetaDataVO meta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getSourceModuleId()));

		Collection<EntityObjectVO> objects = CollectionUtils.transform(sourceIds, new Transformer<Long, EntityObjectVO>() {
			@Override
			public EntityObjectVO transform(Long i) {
				return NucletDalProvider.getInstance().getEntityObjectProcessor(meta.getEntity()).getByPrimaryKey(i);
			}
		});

		return groupGenerationSources(objects, generatoractionvo);
	}

	/**
	 * Generate a single new object. If the generator action is configured with attribute grouping, the grouping has to be done in advance.
	 *
	 * @param sourceObjects
	 * @param parameterObjectId
	 * @param generatoractionvo
	 * @return
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public GenerationResult generateGenericObject(Collection<EntityObjectVO> sourceObjects, Long parameterObjectId, GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonStaleVersionException, CommonValidationException {
		final EntityMetaDataVO sourceMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getSourceModuleId()));
		final EntityMetaDataVO targetMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getTargetModuleId()));

		final String sourceEntityName = sourceMeta.getEntity();

		// Create a template for the target object (from single or multiple objects)
		final EntityObjectVO target;
		if (sourceObjects.size() > 1) {
			target = getNewObject(sourceObjects, generatoractionvo);
		}
		else if (sourceObjects.size() == 1) {
			EntityObjectVO source = sourceObjects.iterator().next();
			target = getNewObject(source, generatoractionvo);

			// Create target origin
			if (targetMeta.isStateModel() && sourceMeta.isStateModel()) {
				target.getFields().put(NuclosEOField.ORIGIN.getName(), source.getField(NuclosEOField.SYSTEMIDENTIFIER.getName()));
			}

			// link target with source object if possible
			if (generatoractionvo.isCreateRelationBetweenObjects()) {
				String targetAttribute = getTargetFieldIdIfAny(generatoractionvo, generatoractionvo.getSourceModuleId());
				if (targetAttribute != null) {
					target.getFieldIds().put(targetAttribute, source.getId());
				}
			}
		}
		else {
			throw new NuclosFatalException("No source objects for generation");
		}

		// Create target process
		if (targetMeta.isStateModel() && generatoractionvo.getTargetProcessId() != null) {
			final String sTargetProcess = getMasterDataFacade().get(NuclosEntity.PROCESS.getEntityName(), generatoractionvo.getTargetProcessId()).getField("name").toString();
			target.getFields().put(NuclosEOField.PROCESS.getName(), sTargetProcess);
			target.getFieldIds().put(NuclosEOField.PROCESS.getName(), generatoractionvo.getTargetProcessId().longValue());
		}

		// Load parameter object (if available)
		String parameterEntityName = null;
		EntityObjectVO parameterObject = null;
		RuleObjectContainerCVO parameterCVO = null;
		if (generatoractionvo.getParameterEntityId() != null) {
			if (parameterObjectId == null) {
				// TODO: exception that parameter object is missing
				throw new NuclosFatalException("Missing parameter object");
			}

			if (generatoractionvo.isCreateRelationToParameterObject()) {
				String targetAttribute = getTargetFieldIdIfAny(generatoractionvo, generatoractionvo.getParameterEntityId());
				if (targetAttribute != null) {
					target.getFieldIds().put(targetAttribute, parameterObjectId);
				}
			}

			parameterEntityName = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getParameterEntityId())).getEntity();
			parameterObject = NucletDalProvider.getInstance().getEntityObjectProcessor(parameterEntityName).getByPrimaryKey(IdUtils.toLongId(parameterObjectId));
			try {
				parameterCVO = getRuleObjectContainerCVO(Event.GENERATION_BEFORE, parameterEntityName, parameterObjectId);
			} catch (CommonBusinessException e) {
				throw new CommonFinderException(e);
			}
		}
		target.setEntity(targetMeta.getEntity());

		// Copy parameter attributes
		if (parameterObject != null) {
			copyParameterAttributes(parameterObject, parameterEntityName, target, generatoractionvo);
		}

		// add dependants
		final DependantMasterDataMap dependants = new DependantMasterDataMap();
		final Collection<MasterDataVO> subentities = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONSUBENTITY.getEntityName(), "generation", generatoractionvo.getId());

		// copy dependants (grouping == false)
		for (MasterDataVO subentity : subentities) {
			if (LangUtils.defaultIfNull(subentity.getField("groupAttributes", Boolean.class), Boolean.FALSE)) {
				continue;
			}
			copyDependants(sourceObjects, parameterObject, generatoractionvo, subentity, target, dependants);
		}

		// aggregate dependants
		for (MasterDataVO subentity : subentities) {
			if (!LangUtils.defaultIfNull(subentity.getField("groupAttributes", Boolean.class), Boolean.FALSE)) {
				continue;
			}
			Collection<Long> sourceObjectIds = CollectionUtils.transform(sourceObjects, new Transformer<EntityObjectVO, Long>() {
				@Override
				public Long transform(EntityObjectVO i) {
					return i.getId();
				}
			});
			aggregateDependants(sourceObjectIds, generatoractionvo, subentity, target, dependants);
		}

		Collection<RuleObjectContainerCVO> sourceContainers = CollectionUtils.transform(sourceObjects, new Transformer<EntityObjectVO, RuleObjectContainerCVO>() {
			@Override
			public RuleObjectContainerCVO transform(EntityObjectVO i) {
				try {
					return getRuleObjectContainerCVO(Event.GENERATION_BEFORE, sourceEntityName, i.getId());
				} catch (CommonBusinessException e) {
					throw new NuclosFatalException(e);
				}
			}
		});

		if (targetMeta.isStateModel()) {
			target.getFields().put(NuclosEOField.LOGGICALDELETED.getName(), Boolean.FALSE);
			final GenericObjectVO go = DalSupportForGO.getGenericObjectVO(target);

			final GenericObjectVO result = new GenericObjectVO(generatoractionvo.getTargetModuleId(), null, null, GenericObjectMetaDataCache.getInstance());
			final Set<String> targetAttributes = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(targetMeta.getEntity()).keySet();

			for (String targetAttribute : targetAttributes) {
				Integer attributeId = AttributeCache.getInstance().getAttribute(targetMeta.getEntity(), targetAttribute).getId();
				DynamicAttributeVO davo = go.getAttribute(targetAttribute, AttributeCache.getInstance());
				if (davo != null) {
					result.setAttribute(new DynamicAttributeVO(attributeId, davo.getValueId(), davo.getValue()));
				}
			}

			RuleObjectContainerCVO container = new RuleObjectContainerCVO(Event.GENERATION_BEFORE, result, dependants);

			// Create the new object
			try {
				// execute rules (before)
				final List<String> lstActions = new ArrayList<String>();
				container = executeGenerationRules(generatoractionvo, container, sourceContainers, parameterCVO, lstActions, false);

				GenericObjectVO created = getGenericObjectFacade().create(new GenericObjectWithDependantsVO(container));

				performDeferredActionsFromRules(lstActions, created.getId(), getGenericObjectFacade());

				for (EntityObjectVO source : sourceObjects) {
					relateCreatedGenericObjectToParent(IdUtils.unsafeToId(source.getId()), getGenericObjectFacade(), generatoractionvo, created.getId());
				}

				// execute rules (after)
				container = new RuleObjectContainerCVO(Event.GENERATION_AFTER, created, dependants);
				container = executeGenerationRules(generatoractionvo, container, sourceContainers, parameterCVO, new ArrayList<String>(), true);

				return new GenerationResult(CollectionUtils.transform(sourceObjects, new ExtractIdTransformer()), DalSupportForGO.wrapGenericObjectVO(getGenericObjectFacade().get(created.getId())), null) ;
			}
			catch (CommonBusinessException ex) {
				EntityObjectVO temp = DalSupportForGO.wrapGenericObjectVO(container.getGenericObject());
				temp.setEntity(targetMeta.getEntity());
				temp.setDependants(container.getDependants());
				return new GenerationResult(CollectionUtils.transform(sourceObjects, new ExtractIdTransformer()), temp, ex.getMessage()) ;
			}
		}
		else {
			final MasterDataVO md = DalSupportForMD.wrapEntityObjectVO(target);

			RuleObjectContainerCVO container = new RuleObjectContainerCVO(Event.GENERATION_BEFORE, md, dependants);

			// Create the new object
			final String entity = targetMeta.getEntity();
			try {
				// execute rules (before)
				final List<String> lstActions = new ArrayList<String>();
				container = executeGenerationRules(generatoractionvo, container, sourceContainers, parameterCVO, lstActions, false);

				MasterDataVO created = getMasterDataFacade().create(entity, container.getMasterData(), container.getDependants());

				// execute rules (after)
				container = new RuleObjectContainerCVO(Event.GENERATION_AFTER, created, container.getDependants());
				container = executeGenerationRules(generatoractionvo, container, sourceContainers, parameterCVO, new ArrayList<String>(), true);

				return new GenerationResult(CollectionUtils.transform(sourceObjects, 
						new ExtractIdTransformer()), 
						DalSupportForMD.getEntityObjectVO(entity, 
								getMasterDataFacade().get(entity, created.getId())), null) ;
			}
			catch (CommonBusinessException ex) {
				// this is required, because the id is already generated
				container.getMasterData().setId(null);
				EntityObjectVO temp = DalSupportForMD.getEntityObjectVO(entity, container.getMasterData());
				temp.setEntity(targetMeta.getEntity());
				temp.setDependants(container.getDependants());
				return new GenerationResult(CollectionUtils.transform(sourceObjects, new ExtractIdTransformer()), temp, ex.getMessage()) ;
			}
		}
	}

	private String getTargetFieldIdIfAny(GeneratorActionVO generatoractionvo, Integer sourceEntityId) {
		String result = null;
		String sSourceEntity = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(sourceEntityId)).getEntity();
		Integer iTargetModuleId = generatoractionvo.getTargetModuleId();
		String sTargetEntity = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(iTargetModuleId)).getEntity();
		Map<String, EntityFieldMetaDataVO> mp = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(sTargetEntity);
		for (String sFieldName : mp.keySet()) {
			EntityFieldMetaDataVO voField = mp.get(sFieldName);
			if (sSourceEntity.equals(voField.getForeignEntity())) {
				result = voField.getField();
				break;
			}
		}
		return result;
	}

	/**
	 * Relate the new target object to the source object. If source object is an
	 * invoice section relate the new object to the invoice:
	 *
	 * @param govoSource
	 * @param lofacade
	 * @param generatoractionvo
	 * @param iTargetGenericObjectId
	 * @throws CommonFinderException
	 */
	private void relateCreatedGenericObjectToParent(Integer iSourceId, GenericObjectFacadeLocal lofacade, GeneratorActionVO generatoractionvo, Integer iTargetGenericObjectId) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException {
		if (iSourceId != null) {
			try {
				lofacade.relate(generatoractionvo.getTargetModuleId(), iTargetGenericObjectId, iSourceId, GenericObjectTreeNode.SystemRelationType.PREDECESSOR_OF.getValue());
			} catch (CommonCreateException ex) {
				throw new NuclosFatalException(ex);
			}
		}
	}

	/**
	 * Executes a list of commands prepared by the rule interface for actions,
	 * which cannot be performed before object creation, e.g. object relation
	 * with the freshly created object.
	 *
	 * @param lstActionsFromRules
	 *            List<String> of commands from deferred methods
	 * @param iTargetGenericObjectId
	 *            id of the freshly created object (if any)
	 * @param lofacade
	 *            (for performance)
	 * @throws NuclosBusinessRuleException
	 * @throws CommonCreateException
	 * @throws CommonFinderException
	 */
	private void performDeferredActionsFromRules(List<String> lstActionsFromRules, Integer iTargetGenericObjectId, GenericObjectFacadeLocal lofacade) throws NuclosBusinessRuleException, CommonCreateException, CommonFinderException, CommonPermissionException {

		for (String sAction : lstActionsFromRules) {
			final String[] asAction = sAction.split(":", 4);
			if ("relate".equals(asAction[0])) {
				if (asAction[1].equals(asAction[2])) {
					throw new NuclosBusinessRuleException("generator.facade.exception.1");// "Ein Objekt darf nicht mit sich selbst verkn\u00fcpft werden.");
				}
				final Integer iTargetId = "this".equals(asAction[1]) ? iTargetGenericObjectId : Integer.parseInt(asAction[1]);
				final Integer iTargetModuleId = lofacade.getModuleContainingGenericObject(iTargetId);
				String relationType = asAction[3];
				lofacade.relate(iTargetModuleId, iTargetId, "this".equals(asAction[2]) ? iTargetGenericObjectId : Integer.parseInt(asAction[2]), relationType);
			}
		}
	}

	private Collection<RuleObjectContainerCVO> getObjectSourceVOs(Collection<Integer> collSourceGenericObjectId, Integer parameterObjectId, GeneratorActionVO generatoractionvo) throws CommonPermissionException, CommonFinderException {
		Collection<RuleObjectContainerCVO> col = new ArrayList<RuleObjectContainerCVO>();
		for (Integer iSourceId : collSourceGenericObjectId) {
			RuleObjectContainerCVO loccvoSource = getGenericObjectFacade().getRuleObjectContainerCVO(Event.GENERATION_BEFORE, iSourceId);
			col.add(loccvoSource);
		}
		return col;
	}

	private Map<String, Collection<EntityObjectVO>> groupGenerationSources(Collection<EntityObjectVO> col, GeneratorActionVO generatoractionvo) {
		Set<String> groupingAttributes = new HashSet<String>();

		Collection<MasterDataVO> attributes = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONATTRIBUTE.getEntityName(), "generation", generatoractionvo.getId());
		for (MasterDataVO attribute : attributes) {
			if (!StringUtils.isNullOrEmpty(attribute.getField("sourceType", String.class))) {
				continue;
			}
			String groupFunction = attribute.getField("groupfunction", String.class);
			if (!StringUtils.isNullOrEmpty(groupFunction) && !"group by".equals(groupFunction)) {
				continue;
			}
			String source = attribute.getField("attributeSource", String.class);
			if (systemAttributes.contains(source)) {
				continue;
			}
			groupingAttributes.add(source);
		}

		Map<String, Collection<EntityObjectVO>> mp = new Hashtable<String, Collection<EntityObjectVO>>();

		for (EntityObjectVO eo : col) {
			StringBuffer sb = new StringBuffer();
			for (String attribute : groupingAttributes) {
				Object obj = eo.getRealField(attribute);
				sb.append(obj);
				sb.append(".");
			}
			final String key = sb.toString();
			if (!mp.containsKey(key)) {
				mp.put(key, new ArrayList<EntityObjectVO>());
				mp.get(key).add(eo);
			}
			else {
				mp.get(key).add(eo);
			}
		}

		return mp;
	}

	private EntityObjectVO getGroupedGeneratedObject(GeneratorActionVO generator, Collection<Long> sourceIds) {
		final String sourceEntity = Modules.getInstance().getEntityNameByModuleId(generator.getSourceModuleId());
		final String targetEntity = Modules.getInstance().getEntityNameByModuleId(generator.getTargetModuleId());
		final Collection<MasterDataVO> attributes = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONATTRIBUTE.getEntityName(), "generation", generator.getId());

		String sTable = MetaDataServerProvider.getInstance().getEntity(sourceEntity).getDbEntity();
		// NUCLOSINT-1358
		sTable = "V_" + sTable.substring(2);
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from(sTable).alias(SystemFields.BASE_ALIAS);

		List<DbSelection<?>> selection = new ArrayList<DbSelection<?>>();
		List<DbExpression<?>> groupby = new ArrayList<DbExpression<?>>();

		for (MasterDataVO attribute : attributes) {
			final String type = (String) attribute.getField("sourceType");
			// only process source entity attributes (skip parameter entity attributes)
			if (StringUtils.isNullOrEmpty(type)) {
				final String function = (String) attribute.getField("groupfunction");
				final String source = (String) attribute.getField("attributeSource");
				EntityFieldMetaDataVO meta = MetaDataServerProvider.getInstance().getEntityField(sourceEntity, source);

				String column = meta.getDbColumn();
				DbColumnExpression<?> c;
				try {
					c = t.baseColumn(column, DalUtils.getDbType(Class.forName(meta.getDataType())));
				} catch (ClassNotFoundException e) {
					throw new NuclosFatalException(e);
				}

				if (function == null || "group by".equals(function)) {
					c.alias(meta.getField());
					selection.add(c);
					groupby.add(c);
					if (meta.getForeignEntity() != null && meta.getForeignEntityField() != null) {
						column = column.replaceFirst("^STRVALUE", "INTID");
						c = t.baseColumn(column, Long.class);
						c.alias(meta.getField() + "Id");
						selection.add(c);
						groupby.add(c);
					}
				}
				else if ("summate".equals(function)) {
					selection.add(builder.sum(c).alias(meta.getField()));
				}
				else if ("minimum value".equals(function)) {
					selection.add(builder.min(c).alias(meta.getField()));
				}
				else if ("maximum value".equals(function)) {
					selection.add(builder.max(c).alias(meta.getField()));
				}
			}
		}

		query.multiselect(selection);
		if (groupby.size() > 0) {
			query.groupBy(groupby);
		}

		ArrayList<DbCondition> conditions = new ArrayList<DbCondition>();
		for (Long sourceId : sourceIds) {
			conditions.add(builder.equal(t.baseColumn("INTID", Long.class), sourceId));
		}
		query.where(builder.or(conditions.toArray(new DbCondition[conditions.size()])));

		DbTuple tuple = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);

		final EntityObjectVO result = new EntityObjectVO();
		result.initFields(1, 1);
		result.setEntity(targetEntity);
		result.getFields().put(NuclosEOField.LOGGICALDELETED.getMetaData().getField(), Boolean.FALSE);

		for (MasterDataVO attribute : attributes) {
			final String type = (String) attribute.getField("sourceType");
			if (!StringUtils.isNullOrEmpty(type)) {
				continue;
			}
			final String source = (String) attribute.getField("attributeSource");
			final String target = (String) attribute.getField("attributeTarget");

			result.getFields().put(target, tuple.get(source));
			EntityFieldMetaDataVO meta = MetaDataServerProvider.getInstance().getEntityField(sourceEntity, source);
			if (meta.getForeignEntity() != null && meta.getForeignEntityField() != null) {
				result.getFieldIds().put(target, tuple.get(source + "Id", Long.class));
			}
		}

		return result;
	}

	private void aggregateDependants(Collection<Long> sourceIds, GeneratorActionVO gavo, MasterDataVO subentity, EntityObjectVO target, DependantMasterDataMap dependants) {
		final Collection<MasterDataVO> attributes = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONSUBENTITYATTRIBUTE.getEntityName(), "entity", subentity.getId());
		if (attributes.size() == 0) {
			return;
		}

		final EntityMetaDataVO sourceEntity = MetaDataServerProvider.getInstance().getEntity(subentity.getField("entitySourceId", Integer.class).longValue());
		final EntityMetaDataVO targetEntity = MetaDataServerProvider.getInstance().getEntity(subentity.getField("entityTargetId", Integer.class).longValue());

		String sTable = sourceEntity.getDbEntity();
		// NUCLOSINT-1358
		sTable = "V_" + sTable.substring(2);
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from(sTable).alias(SystemFields.BASE_ALIAS);

		List<DbSelection<?>> selection = new ArrayList<DbSelection<?>>();
		List<DbExpression<?>> groupby = new ArrayList<DbExpression<?>>();

		for (MasterDataVO attribute : attributes) {
			final String function = (String) attribute.getField("subentityAttributeGrouping");
			final String source = (String) attribute.getField("subentityAttributeSource");
			EntityFieldMetaDataVO meta = MetaDataServerProvider.getInstance().getEntityField(sourceEntity.getEntity(), source);

			String column = meta.getDbColumn();
			DbColumnExpression<?> c;
			try {
				c = t.baseColumn(column, DalUtils.getDbType(Class.forName(meta.getDataType())));
			} catch (ClassNotFoundException e) {
				throw new NuclosFatalException(e);
			}

			if ("group by".equals(function)) {
				c.alias(meta.getField());
				selection.add(c);
				groupby.add(c);
				if (meta.getForeignEntity() != null && meta.getForeignEntityField() != null) {
					column = column.replaceFirst("^STRVALUE", "INTID");
					c = t.baseColumn(column, Long.class);
					c.alias(meta.getField() + "Id");
					selection.add(c);
					groupby.add(c);
				}
			}
			else if ("summate".equals(function)) {
				selection.add(builder.sum(c).alias(source));
			}
			else if ("minimum value".equals(function)) {
				selection.add(builder.min(c).alias(source));
			}
			else if ("maximum value".equals(function)) {
				selection.add(builder.max(c).alias(source));
			}
		}

		query.multiselect(selection);
		if (groupby.size() > 0) {
			query.groupBy(groupby);
		}

		String mainSourceEntity = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(gavo.getSourceModuleId())).getEntity();
		String foreignField = DependantMasterDataMap.getForeignKeyField(MasterDataMetaCache.getInstance().getMetaData(sourceEntity.getEntity()), mainSourceEntity, false);
		String dbColumn = MetaDataServerProvider.getInstance().getEntityField(sourceEntity.getEntity(), foreignField).getDbColumn();
		dbColumn = dbColumn.replaceFirst("^STRVALUE", "INTID");

		ArrayList<DbCondition> conditions = new ArrayList<DbCondition>();
		for (Long sourceId : sourceIds) {
			conditions.add(builder.equal(t.baseColumn(dbColumn, Long.class), sourceId));
		}
		query.where(builder.or(conditions.toArray(new DbCondition[conditions.size()])));

		List<EntityObjectVO> aggregated = dataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, EntityObjectVO>() {
			@Override
			public EntityObjectVO transform(DbTuple tuple) {
				final EntityObjectVO result = new EntityObjectVO();
				result.initFields(1, 1);
				result.setEntity(targetEntity.getEntity());

				for (MasterDataVO attribute : attributes) {
					final String source = (String) attribute.getField("subentityAttributeSource");
					final String target = (String) attribute.getField("subentityAttributeTarget");

					result.getFields().put(target, tuple.get(source));
					EntityFieldMetaDataVO meta = MetaDataServerProvider.getInstance().getEntityField(sourceEntity.getEntity(), source);
					if (meta.getForeignEntity() != null && meta.getForeignEntityField() != null) {
						result.getFieldIds().put(target, tuple.get(source + "Id", Long.class));
					}
				}
				return result;
			}
		});

		dependants.addAllData(targetEntity.getEntity(), aggregated);
	}

	private RuleObjectContainerCVO executeGenerationRules(GeneratorActionVO generatoractionvo, RuleObjectContainerCVO loccvoTargetBeforeRules, Collection<RuleObjectContainerCVO> loccvoSourceObjects, RuleObjectContainerCVO loccvoParameter, List<String> lstActions, Boolean after) throws NuclosBusinessRuleException {
		RuleEngineFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class);
		return facade.fireGenerationRules(generatoractionvo.getId(), loccvoTargetBeforeRules, loccvoSourceObjects, loccvoParameter, lstActions, generatoractionvo.getProperties(), after);
	}

	/**
	 * copies all dependant records from source object to target object. Copies
	 * also a basekey and ordernumber attribute value as dependant into target,
	 * if appropriate and desired
	 *
	 * @param loccvoSource
	 *            source generic object with dependant masterdata
	 * @param gavo
	 *            details about generation action
	 * @param dependants
	 *            DependantMasterDataMap for the the result
	 * @throws CommonPermissionException
	 */
	private void copyDependants(Collection<EntityObjectVO> sources, EntityObjectVO parameterObject, GeneratorActionVO gavo, MasterDataVO subentity, EntityObjectVO target, DependantMasterDataMap dependants) throws CommonPermissionException, CommonFinderException {
		final GenericObjectMetaDataCache lometacache = GenericObjectMetaDataCache.getInstance();
		final AttributeProvider attrprovider = AttributeCache.getInstance();

		final UsageCriteria criteria = new UsageCriteria(gavo.getTargetModuleId(), IdUtils.unsafeToId(target.getFieldId(NuclosEOField.PROCESS.getName())));
		final int iLayoutIdTarget = lometacache.getBestMatchingLayoutId(criteria, false);
		final Set<String> setEntityNamesTarget = lometacache.getSubFormEntityNamesByLayoutId(iLayoutIdTarget);

		final EntityMetaDataVO sourceMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(subentity.getField("entitySourceId")));
		final EntityMetaDataVO targetMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(subentity.getField("entityTargetId")));

		String sSource = sourceMeta.getEntity();
		String sTarget = targetMeta.getEntity();

		if (!setEntityNamesTarget.contains(sTarget)) {
			return;
		}
		if (!targetMeta.isEditable()) {
			throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("generator.dependant.noneditable", sTarget));
		}

		final Collection<MasterDataVO> attributes = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONSUBENTITYATTRIBUTE.getEntityName(), "entity", subentity.getId());

		if (StringUtils.isNullOrEmpty(subentity.getField("sourceType", String.class))) {
			for (EntityObjectVO source : sources) {
				final UsageCriteria sourcecriteria = new UsageCriteria(gavo.getSourceModuleId(), IdUtils.unsafeToId(source.getFieldId(NuclosEOField.PROCESS.getName())));
				final int iLayoutIdSource = lometacache.getBestMatchingLayoutId(sourcecriteria, false);
				final Set<String> setEntityNamesSource = lometacache.getSubFormEntityNamesByLayoutId(iLayoutIdSource);

				if (!setEntityNamesSource.contains(sSource)) {
					continue;
				}

				final Collection<EntityAndFieldName> eafns = lometacache.getSubFormEntityAndForeignKeyFieldNamesByLayoutId(iLayoutIdSource);

				String foreignfield = null;
				for (EntityAndFieldName eafn : eafns) {
					if (eafn.getEntityName().equals(sSource)) {
						foreignfield = eafn.getFieldName();
					}
				}
				if (foreignfield == null) {
					throw new NuclosFatalException();
				}

				copyAttributes(gavo, attributes, sSource, foreignfield, source.getId(), sTarget, dependants);
			}
		}
		else {
			EntityMetaDataVO parameterEntity = MetaDataServerProvider.getInstance().getEntity(gavo.getParameterEntityId().longValue());
			LayoutFacadeLocal layoutLocal = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
			Map<EntityAndFieldName, String> eafns = layoutLocal.getSubFormEntityAndParentSubFormEntityNames(parameterEntity.getEntity(), parameterObject.getId().intValue(), false);
			String foreignfield = null;
			for (Map.Entry<EntityAndFieldName, String> e : eafns.entrySet()) {
				if (e.getValue() == null) {
					if (e.getKey().getEntityName().equals(sSource)) {
						foreignfield = e.getKey().getFieldName();
					}
				}
			}

			if (foreignfield == null) {
				throw new NuclosFatalException();
			}

			copyAttributes(gavo, attributes, sSource, foreignfield, parameterObject.getId(), sTarget, dependants);
		}
	}

	private void copyAttributes(GeneratorActionVO gavo, Collection<MasterDataVO> attributes, String source, String fef, 
			Object sourceId, String target, DependantMasterDataMap dependants) {
		final Collection<MasterDataVO> data = getMasterDataFacade().getDependantMasterData(source, fef, sourceId);

		if (attributes.size() == 0) {
			for (MasterDataVO mdvoOriginal : data) {
				final MasterDataVO mdvo = mdvoOriginal.copy(false);
				String sMainSourceEntity = MetaDataServerProvider.getInstance().getEntity(gavo.getSourceModuleId().longValue()).getEntity();
				for (EntityFieldMetaDataVO fieldmeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(source).values()) {
					if (sMainSourceEntity.equals(fieldmeta.getForeignEntity())) {
						mdvo.setField(fieldmeta.getField() + "Id", null);
					}
				}
				dependants.addData(target, DalSupportForMD.getEntityObjectVO(target, mdvo));
			}
		}
		else {
			for (MasterDataVO mdvoOriginal : data) {
				MasterDataVO mdvo = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(target), true);
				for (MasterDataVO attribute : attributes) {
					final String sField = (String) attribute.getField("subentityAttributeSource");
					Object value = mdvoOriginal.getField(sField);
					mdvo.setField((String) attribute.getField("subentityAttributeTarget"), value);

					final EntityFieldMetaDataVO metaField = MetaDataServerProvider.getInstance().getEntityField(source, sField);
					if (metaField.getForeignEntity() != null) {
						final Object valueId = mdvoOriginal.getField(sField + "Id");
						mdvo.setField((String) attribute.getField("subentityAttributeTarget") + "Id", valueId);
					}
				}
				dependants.addData(target, DalSupportForMD.getEntityObjectVO(target, mdvo));
			}
		}
	}

	/**
	 * transfers (copies) a specified set of attributes from one generic object
	 * to another. Called from within rules. Attention: because this is called
	 * within a rule, the source genericobject and its attributes were not saved
	 * until now -> the consequence is, that the old attribute values of the
	 * source genericobject were transfered to the target genericobject this is
	 * very ugly -> todo
	 *
	 * @param iSourceGenericObjectId
	 *            source generic object id to transfer data from
	 * @param iTargetGenericObjectId
	 *            target generic object id to transfer data to
	 * @param asAttributes
	 *            Array of attribute names to specify transferred data
	 * @precondition asAttributes != null
	 */
	public void transferGenericObjectData(GenericObjectVO govoSource, Integer iTargetGenericObjectId, String[][] asAttributes) {
		if (asAttributes == null) {
			throw new NullArgumentException("asAttributes");
		}
		debug("Entering transferGenericObjectData()");
		try {
			final Collection<Integer> stSourceAttributeIds = getAttributeIdsByModuleId(govoSource.getModuleId());
			final GenericObjectVO govoTarget = getGenericObjectFacade().get(iTargetGenericObjectId);
			final Collection<Integer> stTargetAttributeIds = getAttributeIdsByModuleId(govoTarget.getModuleId());

			final Collection<Integer> stExcludedAttributeIds = this.getExcludedAttributeIds();
			final Integer[][] aiIncludedAttributeIds = getAttributeIdsFromNames(asAttributes, govoSource.getModuleId(), govoTarget.getModuleId());

			for (int i = 0; i < aiIncludedAttributeIds.length; i++) {
				if (stSourceAttributeIds.contains(aiIncludedAttributeIds[i][0]) && !stExcludedAttributeIds.contains(aiIncludedAttributeIds[i][0])) {
					if (stTargetAttributeIds.contains(aiIncludedAttributeIds[i][1]) && !stExcludedAttributeIds.contains(aiIncludedAttributeIds[i][1])) {
						copyAttribute(aiIncludedAttributeIds[i][0], aiIncludedAttributeIds[i][1], govoSource, govoTarget);
					}
				}
			}

			// todo: avoid using modify here as it triggers another rule!
			getGenericObjectFacade().modify(govoTarget.getModuleId(), new GenericObjectWithDependantsVO(govoTarget, new DependantMasterDataMap()));
		} catch (CommonBusinessException ex) {
			throw new NuclosFatalException(ex);
		}
		debug("Leaving transferGenericObjectData()");
	}

	/**
	 * Copy the attribute with the given ID from the source generic object to
	 * the target generic object.
	 *
	 * @param iAttributeSourceId
	 * @param iAttributeTargetId
	 * @param govoSource
	 * @param govoTarget
	 */
	private void copyAttribute(Integer iAttributeSourceId, Integer iAttributeTargetId, GenericObjectVO govoSource, GenericObjectVO govoTarget) {
		debug("Copying attribute " + AttributeCache.getInstance().getAttribute(iAttributeSourceId).getName() + "from " + govoSource.toString() + "to attribute " + AttributeCache.getInstance().getAttribute(iAttributeTargetId).getName() + "from " + govoTarget.toString());
		final DynamicAttributeVO attrvoSource = govoSource.getAttribute(iAttributeSourceId);
		final DynamicAttributeVO attrvoTarget = govoTarget.getAttribute(iAttributeTargetId);
		if (attrvoSource == null) {
			if (attrvoTarget != null) {
				// remove existent attribute:
				attrvoTarget.remove();
			}
		} else {
			if (attrvoTarget != null) {
				// update existent attribute:
				attrvoTarget.setValueId(attrvoSource.getValueId());
				attrvoTarget.setValue(attrvoSource.getValue());
			} else {
				// add nonexistent attribute:
				govoTarget.setAttribute(new DynamicAttributeVO(iAttributeTargetId, attrvoSource.getValueId(), attrvoSource.getValue()));
			}
		}
	}

	/**
	 * @param asAttributeNames
	 * @return
	 * @precondition asAttributeNames != null
	 * @postcondition result != null
	 */
	private static Integer[][] getAttributeIdsFromNames(String[][] asAttributeNames, Integer iSourceModuleId, Integer iTargetModuleId) {
		if (asAttributeNames == null) {
			throw new NullArgumentException("asAttributeNames");
		}
		final AttributeCache attrcache = AttributeCache.getInstance();
		final Integer[][] aiAttributeIds = new Integer[asAttributeNames.length][2];

		for (int i = 0; i < asAttributeNames.length; i++) {
			String sSourceAttributeName = asAttributeNames[i][0];
			String sTargetAttributeName = asAttributeNames[i][1];
			aiAttributeIds[i][0] = attrcache.getAttribute(iSourceModuleId, sSourceAttributeName).getId();
			aiAttributeIds[i][1] = attrcache.getAttribute(iTargetModuleId, sTargetAttributeName).getId();
		}

		assert aiAttributeIds != null;
		return aiAttributeIds;
	}

	/**
	 * copies all attributes from source object to target template
	 *
	 * @param govoSource
	 *            source generic object
	 * @param govoTargetTemplate
	 *            target generic object template
	 * @param generatoractionvo
	 *            generator vo to determine attributes
	 * @throws CommonPermissionException
	 */
	private EntityObjectVO getNewObject(EntityObjectVO eoSource, GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonValidationException, CommonPermissionException {
		final EntityMetaDataVO sourceMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getSourceModuleId()));
		final EntityMetaDataVO targetMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getTargetModuleId()));

		final EntityObjectVO result = new EntityObjectVO();
		result.setEntity(targetMeta.getEntity());
		result.initFields(10, 10);

		final List<Pair<String, String>> includedAttributes = getIncludedAttributes(generatoractionvo, null);

		final Set<String> stSourceAttributes = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(sourceMeta.getEntity()).keySet();
		final Set<String> stTargetAttribute = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(targetMeta.getEntity()).keySet();

		for (Pair<String, String> p : includedAttributes) {
			if (stSourceAttributes.contains(p.x)) {
				if (stTargetAttribute.contains(p.y)) {
					if (eoSource.getField(p.x) != null) {
						result.getFields().put(p.y, eoSource.getField(p.x));
					}
					if (eoSource.getFieldId(p.x) != null) {
						result.getFieldIds().put(p.y, eoSource.getFieldId(p.x));
					}
				}
			}
		}
		return result;
	}

	private EntityObjectVO getNewObject(Collection<EntityObjectVO> sourceObjects, GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonValidationException, CommonPermissionException {
		final EntityObjectVO object = getGroupedGeneratedObject(generatoractionvo, CollectionUtils.transform(sourceObjects, new Transformer<EntityObjectVO, Long>() {
			@Override
			public Long transform(EntityObjectVO i) {
				return i.getId();
			}
		}));
		return object;
	}

	private void copyParameterAttributes(EntityObjectVO sourceVO, String sourceEntityName, EntityObjectVO target, GeneratorActionVO generatoractionvo) {
		final List<Pair<String, String>> includedAttributes = getIncludedAttributes(generatoractionvo, "parameter");

		final EntityMetaDataVO sourceMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getSourceModuleId()));
		final EntityMetaDataVO targetMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(generatoractionvo.getTargetModuleId()));

		final Set<String> stSourceAttributes = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(sourceMeta.getEntity()).keySet();
		final Set<String> stTargetAttribute = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(targetMeta.getEntity()).keySet();

		for (Pair<String, String> p : includedAttributes) {
			if (stSourceAttributes.contains(p.x)) {
				if (stTargetAttribute.contains(p.y)) {
					if (sourceVO.getField(p.x) != null) {
						target.getFields().put(p.y, sourceVO.getField(p.x));
					}
					if (sourceVO.getFieldId(p.x) != null) {
						target.getFieldIds().put(p.y, sourceVO.getFieldId(p.x));
					}
				}
			}
		}
	}

	/**
	 * Extract the IDs of the attributes to copy from the given
	 * GeneratorActionVO as a collection.
	 *
	 * @param generatoractionvo
	 * @param sourceType
	 * @return list of pairs (source id, target id)
	 */
	private List<Pair<String, String>> getIncludedAttributes(GeneratorActionVO generatoractionvo, String sourceType) {
		Collection<MasterDataVO> colmdvo = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONATTRIBUTE.getEntityName(), "generation", generatoractionvo.getId());
		List<Pair<String, String>> result = new ArrayList<Pair<String, String>>(colmdvo.size());
		for (MasterDataVO mdvo : colmdvo) {
			if (!ObjectUtils.equals(sourceType, mdvo.getField("sourceType")))
				continue;
			String source = mdvo.getField("attributeSource", String.class);
			String target = mdvo.getField("attributeTarget", String.class);
			if (systemAttributes.contains(source) || systemAttributes.contains(target))
				continue;
			result.add(Pair.makePair(source, target));
		}
		return result;
	}

	private Collection<Integer> getExcludedAttributeIds() {
		return collExcludedAttributeIds;
	}

	/**
	 * update usages of rules
	 *
	 * @param generatorId
	 * @param usages
	 * @throws EJBException
	 */
	public void updateRuleUsages(Integer generatorId, Collection<GeneratorRuleVO> usages) throws NuclosBusinessRuleException, CommonCreateException, CommonPermissionException, CommonStaleVersionException, CommonRemoveException, CommonFinderException {

		// as there is no chance to find existing database records and update
		// them we have to remove all and create new ones:

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULEGENERATION), "generation", generatorId);
		Collection<MasterDataVO> mdGenerationsVO = getMasterDataFacade().getMasterData(NuclosEntity.RULEGENERATION.getEntityName(), cond, true);

		for (MasterDataVO mdVO : mdGenerationsVO)
			getMasterDataFacade().remove(NuclosEntity.RULEGENERATION.getEntityName(), mdVO, false);

		for (GeneratorRuleVO gr : usages) {
			MasterDataVO mdVO = MasterDataWrapper.wrapRuleEngineGenerationVO(new RuleEngineGenerationVO(new NuclosValueObject(), generatorId, gr.getId(), gr.getOrder(), gr.isRunAfterwards()));
			getMasterDataFacade().create(NuclosEntity.RULEGENERATION.getEntityName(), mdVO, null);
		}
	}

	/**
	 * get generator usages for specified GeneratorId
	 *
	 * @param iGeneratorId
	 * @return Collection<GeneratorUsageVO>
	 * @throws CommonFatalException
	 */
	public Collection<GeneratorUsageVO> getGeneratorUsages(Integer id) throws CommonFatalException {
		List<GeneratorUsageVO> usages = new ArrayList<GeneratorUsageVO>();
		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.GENERATIONUSAGE), "generation", id);
		Collection<MasterDataVO> mdUsagesVO = getMasterDataFacade().getMasterData(NuclosEntity.GENERATIONUSAGE.getEntityName(), cond, true);

		for (MasterDataVO md : mdUsagesVO)
			usages.add(MasterDataWrapper.getGeneratorUsageVO(md));

		return usages;
	}

	/**
	 * get rule usages for specified GeneratorId
	 *
	 * @param iGeneratorId
	 * @return
	 * @throws CommonPermissionException
	 */
	public Collection<GeneratorRuleVO> getRuleUsages(Integer generatorId) throws CommonPermissionException {
		Collection<GeneratorRuleVO> result = new ArrayList<GeneratorRuleVO>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_RULE_GENERATION").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID_T_MD_RULE", Integer.class), t.baseColumn("INTORDER", Integer.class), t.baseColumn("BLNRUNAFTERWARDS", Boolean.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_GENERATION", Integer.class), generatorId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));

		List<DbTuple> rulesWithOrders = dataBaseHelper.getDbAccess().executeQuery(query.distinct(true));

		for (DbTuple ruleWithOrder : rulesWithOrders) {
			RuleVO ruleVO = RuleCache.getInstance().getRule(ruleWithOrder.get(0, Integer.class));
			Boolean bRunAfterwards = ruleWithOrder.get(2, Boolean.class);
			result.add(new GeneratorRuleVO(ruleVO.getId(), ruleVO.getRule(), ruleVO.getDescription(), ruleWithOrder.get(1, Integer.class), 
					bRunAfterwards == null ? Boolean.FALSE : bRunAfterwards));
		}

		return result;
	}

	// Some private helpers...

	private GeneratorActionVO getGeneratorActionByName(String name) throws CommonFinderException {
		CollectableComparison cond = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.GENERATION), "name", ComparisonOperator.EQUAL, name);
		Collection<MasterDataVO> mdGenerationsVO = getMasterDataFacade().getMasterData(NuclosEntity.GENERATION.getEntityName(), cond, true);

		if (mdGenerationsVO == null || mdGenerationsVO.isEmpty())
			throw new CommonFinderException(StringUtils.getParameterizedExceptionMessage("generator.facade.exception.3", name));
		// "Es ist ein Fehler bei der Objektgenerierung aufgetreten. Objektgenerator mit dem Namen "
		// + name + " kann nicht gefunden werden.");

		MasterDataVO mdVO = mdGenerationsVO.iterator().next();

		return MasterDataWrapper.getGeneratorActionVO(mdVO, getGeneratorUsages(mdVO.getIntId()));
	}

	/**
	 * generate one or more generic objects from an existing generic object
	 * (copying selected attributes and subforms)
	 *
	 * @param iSourceGenericObjectId
	 *            source generic object id to generate from
	 * @param generatoractionvo
	 *            generator action value object to determine what to do
	 * @return id of generated generic object (if exactly one object was
	 *         generated)
	 * @ejb.interface-method view-type="both"
	 * @ejb.permission role-name="Login"
	 */
	public EntityObjectVO generateGenericObjectWithoutCheckingPermission(Long iSourceObjectId, GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonStaleVersionException, CommonValidationException {
		return generateGenericObject(iSourceObjectId, null, generatoractionvo).getGeneratedObject();
	}

	private RuleObjectContainerCVO getRuleObjectContainerCVO(Event event, String entity, Long intId) throws CommonBusinessException {
		EntityMetaDataVO entityVO = MetaDataServerProvider.getInstance().getEntity(entity);
		if (entityVO.isStateModel()) {
			GenericObjectVO govo = getGenericObjectFacade().get(intId.intValue());
			DependantMasterDataMap dependants = getGenericObjectFacade().reloadDependants(govo, null, true);
			return new RuleObjectContainerCVO(event, govo, dependants);
		} else {
			MasterDataWithDependantsVO mdvo = getMasterDataFacade().getWithDependants(entity, intId.intValue());
			return new RuleObjectContainerCVO(event, mdvo, mdvo.getDependants());
		}
	}

	private Collection<Integer> getAttributeIdsByModuleId(Integer iModuleId) {
		final Collection<Integer> stAttributesIds = new HashSet<Integer>();
		for (AttributeCVO attrcvo : GenericObjectMetaDataCache.getInstance().getAttributeCVOsByModuleId(iModuleId, false)) {
			stAttributesIds.add(attrcvo.getId());
		}
		return stAttributesIds;
	}

	private class ExtractIdTransformer implements Transformer<EntityObjectVO, Long> {
		@Override
		public Long transform(EntityObjectVO i) {
			return i.getId();
		}
	}
}
