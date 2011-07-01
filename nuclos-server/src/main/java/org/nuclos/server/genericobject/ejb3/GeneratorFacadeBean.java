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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.NuclosBusinessException;
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
import org.nuclos.common2.EntityAndFieldName;
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
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.GeneratorRuleVO;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.DbTuple.DbTupleElement;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GeneratorUsageVO;
import org.nuclos.server.genericobject.valueobject.GeneratorVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.RuleConstants;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeLocal;
import org.nuclos.server.ruleengine.valueobject.RuleEngineGenerationVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO.Event;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.springframework.transaction.annotation.Transactional;


/**
 * Facade bean for all generic object generator functions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Local(GeneratorFacadeLocal.class)
@Remote(GeneratorFacadeRemote.class)
@Transactional
public class GeneratorFacadeBean extends NuclosFacadeBean implements GeneratorFacadeLocal, GeneratorFacadeRemote {

   private static final String sCollectiveOrderProcessName = "Sammelauftrag";
   private static final Integer iCollectiveOrderProcessId = 100030;

   private static final Integer iAttributeIdOrigin = NuclosEOField.ORIGIN.getMetaData().getId().intValue();
   private static final Integer iAttributeIdIdentifier = NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue();

   private final Collection<Integer> collExcludedAttributeIds = new HashSet<Integer>();

   //private final Integer iExcludedAttributeIds[];

   @PostConstruct
	@RolesAllowed("Login")
	@Override
	public void postConstruct() {
      super.postConstruct();

      collExcludedAttributeIds.add(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue());
      collExcludedAttributeIds.add(NuclosEOField.ORIGIN.getMetaData().getId().intValue());
      collExcludedAttributeIds.add(NuclosEOField.CHANGEDAT.getMetaData().getId().intValue());
      collExcludedAttributeIds.add(NuclosEOField.CHANGEDBY.getMetaData().getId().intValue());
      collExcludedAttributeIds.add(NuclosEOField.CREATEDAT.getMetaData().getId().intValue());
      collExcludedAttributeIds.add(NuclosEOField.CREATEDBY.getMetaData().getId().intValue());
      collExcludedAttributeIds.add(NuclosEOField.PROCESS.getMetaData().getId().intValue());
      collExcludedAttributeIds.add(NuclosEOField.STATE.getMetaData().getId().intValue());
      collExcludedAttributeIds.add(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());

      /*iExcludedAttributeIds = getAttributeIdsFromNames(asExcludedAttributes);
      for(int i = 0; i < iExcludedAttributeIds.length; i++){
         collExcludedAttributeIds.add(iExcludedAttributeIds[i]);
      }*/
   }

   /**
    * @return all generator actions
    */
   @Override
@RolesAllowed("Login")
   public GeneratorVO getGeneratorActions() throws CommonPermissionException {
   	List<GeneratorActionVO> actions = new LinkedList<GeneratorActionVO>();
      CollectableComparison cond = SearchConditionUtils.newMDComparison(
         MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.GENERATION),"ruleonly", ComparisonOperator.EQUAL, false);
      Collection<MasterDataVO> mdGenerationsVO = getMasterDataFacade().getMasterData(NuclosEntity.GENERATION.getEntityName(), cond, true);
      for (MasterDataVO mdVO : mdGenerationsVO) {
         actions.add(MasterDataWrapper.getGeneratorActionVO(mdVO, getGeneratorUsages(mdVO.getIntId())));
      }
      return new GeneratorVO(actions);
   }

   /**
    * generate a new generic object from an existing generic object (copying attributes)
    *
    * @param iSourceGenericObjectId source generic object id to generate from
    * @param sGenerator						name of object generation to determine what to do
    * @return id of generated generic object (if exactly one object was generated)
    */
   @Override
   public Integer generateGenericObject(Integer iSourceGenericObjectId, String sGenerator)
         throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException,
         CommonStaleVersionException, CommonValidationException {

      return generateGenericObject(iSourceGenericObjectId, null, getGeneratorActionByName(sGenerator)).getId();
   }

   /**
    * generate one or more generic objects from an existing generic object (copying selected attributes and subforms)
    *
    * @param iSourceGenericObjectId source generic object id to generate from
    * @param generatoractionvo		 generator action value object to determine what to do
    * @return id of generated generic object (if exactly one object was generated)
    * @nucleus.permission mayWrite(generatoractionvo.getTargetModuleId())
    */
   @Override
@RolesAllowed("Login")
   public GenericObjectVO generateGenericObject(Integer iSourceGenericObjectId, Integer parameterObjectId, GeneratorActionVO generatoractionvo)
         throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException,
         CommonStaleVersionException, CommonValidationException {

      this.checkWriteAllowed(Modules.getInstance().getEntityNameByModuleId(generatoractionvo.getTargetModuleId()));

      // Get the source object and create a target template
      final RuleObjectContainerCVO loccvoSource = getGenericObjectFacade().getRuleObjectContainerCVO(Event.GENERATION_BEFORE, iSourceGenericObjectId);
      
      return generateGenericObject(loccvoSource, parameterObjectId, generatoractionvo, true);
   }
   
   private GenericObjectVO generateGenericObject(Integer iSourceGenericObjectId, Integer parameterObjectId, GeneratorActionVO generatoractionvo, boolean copyDepandants)
   throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException,
   CommonStaleVersionException, CommonValidationException {

	   this.checkWriteAllowed(Modules.getInstance().getEntityNameByModuleId(generatoractionvo.getTargetModuleId()));

	   //Get the source object and create a target template
	   final RuleObjectContainerCVO loccvoSource = getGenericObjectFacade().getRuleObjectContainerCVO(Event.GENERATION_BEFORE, iSourceGenericObjectId);

	   return generateGenericObject(loccvoSource, parameterObjectId, generatoractionvo, copyDepandants);
}


   /**
    * generate one or more generic objects from an existing generic object (copying selected attributes and subforms)
    *
    * @param loccvoSource source generic object to generate from
    * @param sGenerator	 name of generator action to determine what to do
    * @return id of generated generic object (if exactly one object was generated)
    */
   @Override
public Integer generateGenericObject(RuleObjectContainerCVO loccvoSource, String sGenerator)
         throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException,
         CommonStaleVersionException, CommonValidationException {

      return generateGenericObject(loccvoSource, null, getGeneratorActionByName(sGenerator), true).getId();
   }

   @SuppressWarnings("deprecation")
	private GenericObjectVO generateGenericObject(RuleObjectContainerCVO loccvoSource, Integer parameterObjectId, GeneratorActionVO generatoractionvo, boolean copyDepandants)
         throws CommonFinderException, CommonValidationException, CommonPermissionException {

	   final String sourceEntityName = Modules.getInstance().getEntityNameByModuleId(loccvoSource.getGenericObject().getModuleId());

      // Create a template for the target object
      final GenericObjectVO govoTargetTemplate = new GenericObjectVO(generatoractionvo.getTargetModuleId(), null, loccvoSource.getGenericObject().getInstanceId(), GenericObjectMetaDataCache.getInstance());

      // Create target process
      if (generatoractionvo.getTargetProcessId() != null) {
         final Integer iProcessAttributeId = NuclosEOField.PROCESS.getMetaData().getId().intValue();
         final String sTargetProcess = getMasterDataFacade().get(NuclosEntity.PROCESS.getEntityName(), generatoractionvo.getTargetProcessId()).getField("name").toString();
         govoTargetTemplate.setAttribute(DynamicAttributeVO.createGenericObjectAttributeVOCanonical(iProcessAttributeId, generatoractionvo.getTargetProcessId(), sTargetProcess, AttributeCache.getInstance()));
      }

      // Create target origin
      final DynamicAttributeVO davoSourceId = loccvoSource.getGenericObject().getAttribute(iAttributeIdIdentifier);
      govoTargetTemplate.setAttribute(DynamicAttributeVO.createGenericObjectAttributeVOCanonical(iAttributeIdOrigin, null, (String) davoSourceId.getValue(), AttributeCache.getInstance()));

      // Load parameter object
      String parameterEntityName = null;
      RuleObjectContainerCVO parameterCVO = null;
      if (generatoractionvo.getParameterEntityId() != null) {
    	  if (parameterObjectId == null) {
    		  // TODO: exception that parameter object is missing
    		  throw new NuclosFatalException("Missing parameter object");
    	  }
    	  parameterEntityName = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(generatoractionvo.getParameterEntityId())).getEntity();
    	  try {
    		  parameterCVO = getRuleObjectContainerCVO(Event.GENERATION_BEFORE, parameterEntityName, parameterObjectId);
    	  }
    	  catch(CommonBusinessException e) {
    		  throw new CommonFinderException(e);
    	  }
      }

      // Copy attributes
      copyAttributes(loccvoSource.getGenericObject(), govoTargetTemplate, generatoractionvo);
      // Copy parameter attributes
      if (parameterCVO != null) {
    	  copyAttributes(parameterCVO, parameterEntityName, "parameter", govoTargetTemplate, generatoractionvo);
      }
      
      // link target with source object if possible
      if(generatoractionvo.isCreateRelationBetweenObjects()) {
    	  Integer iTargetAttributeId = null;
    	  iTargetAttributeId = getTargetFieldIdIfAny(generatoractionvo,	iTargetAttributeId);
	      if(iTargetAttributeId != null){
	    	  Integer iSourceId = loccvoSource.getGenericObject().getId();
		      DynamicAttributeVO voIntid = new DynamicAttributeVO(null, iTargetAttributeId, iSourceId, "");
		      govoTargetTemplate.setAttribute(voIntid);
	      }
      }
      
      // Fill the target collection with creation instructions for each object to be created
      final Collection<GeneratorGenericObjectVO> collgovoTarget = new ArrayList<GeneratorGenericObjectVO>();
      final int iObjectCount = generatoractionvo.getObjectCount();
      final int iContainerCount = generatoractionvo.getContainerCount();
      final int iTotalObjectCount = (iContainerCount == 0) ? iObjectCount : iContainerCount * iObjectCount;
      int iObjectsToRelate = iTotalObjectCount;

      // Create the generator objects
      collgovoTarget.add(new GeneratorGenericObjectVO());

      // Now go and create all the objects from the instructions, and relate them to the source object
      Integer iTargetGenericObjectId = null;
      GenericObjectVO goContainer = null;
      for (GeneratorGenericObjectVO genlovo : collgovoTarget) {
         try {
            final GenericObjectVO govotarget = govoTargetTemplate.clone();
            for (GeneratorGenericObjectAttributeVO genloavo : genlovo.getAttributes()) {
               govotarget.setAttribute(DynamicAttributeVO.createGenericObjectAttributeVOCanonical(genloavo.getAttributeId(), genloavo.getValueId(), genloavo.getValue(), AttributeCache.getInstance()));
            }

            // Create a map with the copies of specified masterdata entities
            final DependantMasterDataMap dmdmp = new DependantMasterDataMap();

            copyDependants(loccvoSource, sourceEntityName, null, govotarget, generatoractionvo, dmdmp, copyDepandants);
            if (parameterCVO != null) {
                copyDependants(parameterCVO, parameterEntityName, "parameter", govotarget, generatoractionvo, dmdmp, copyDepandants);
            }

            
            final List<String> lstActionsFromRules = new ArrayList<String>();
            // fire all rules for the current object generation
            final RuleObjectContainerCVO loccvoTargetAfterRules = executeGenerationRules(generatoractionvo, loccvoSource, new RuleObjectContainerCVO(Event.GENERATION_BEFORE, govotarget, dmdmp), parameterCVO, lstActionsFromRules, false);

            for (AttributeCVO attrcvo : GenericObjectMetaDataCache.getInstance().getAttributeCVOsByModuleId(loccvoTargetAfterRules.getGenericObject().getModuleId(), false)) {
               if (!attrcvo.isSystemAttribute() && !attrcvo.isNullable() && loccvoTargetAfterRules.getGenericObject().getAttribute(attrcvo.getId()) == null) {
                  return new GenericObjectWithDependantsVO(loccvoTargetAfterRules.getGenericObject(), loccvoTargetAfterRules.getDependants());
               }
            }
            
            // check for mandatory state fields and columns
            StateFacadeLocal stateFacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
            DynamicAttributeVO attrProcess = loccvoTargetAfterRules.getGenericObject().getAttribute(NuclosEOField.PROCESS.getName(), AttributeCache.getInstance());
            StateVO initialStateVO = stateFacade.getInitialState(
            	new UsageCriteria(loccvoTargetAfterRules.getGenericObject().getModuleId(), 
            		attrProcess==null?null:attrProcess.getValueId()));
            try {
            	stateFacade.checkMandatory(DalSupportForGO.wrapGenericObjectVO(loccvoTargetAfterRules.getGenericObject()), initialStateVO);
            } catch (NuclosBusinessException pex) {
            	return new GenericObjectWithDependantsVO(loccvoTargetAfterRules.getGenericObject(), loccvoTargetAfterRules.getDependants());
            }

            final GenericObjectFacadeLocal lofacade = getGenericObjectFacade();
            // Actually create the object
            try {
            	GenericObjectVO createdGoVO = lofacade.create(new GenericObjectWithDependantsVO(loccvoTargetAfterRules));
            	
                iTargetGenericObjectId = createdGoVO.getId();

                performDeferredActionsFromRules(lstActionsFromRules, iTargetGenericObjectId, lofacade);

                relateCreatedGenericObjectToParent(loccvoSource.getGenericObject(), lofacade, generatoractionvo, iTargetGenericObjectId);

                goContainer = insertCreatedGenericObjectIntoContainer(iObjectsToRelate, iContainerCount, iObjectCount, lofacade, goContainer, iTargetGenericObjectId);
                iObjectsToRelate--;
                
                executeGenerationRules(generatoractionvo, new RuleObjectContainerCVO(Event.GENERATION_AFTER, loccvoSource.getGenericObject(), loccvoSource.getDependants()), new RuleObjectContainerCVO(Event.GENERATION_AFTER, createdGoVO, dmdmp), parameterCVO, new ArrayList<String>(), true);
            }
            catch (NuclosBusinessException ex) {
            	return new GenericObjectWithDependantsVO(loccvoTargetAfterRules.getGenericObject(), loccvoTargetAfterRules.getDependants());
             }
         }
         catch (CommonBusinessException ex) {
            throw new NuclosFatalException(ex);
         }
         catch (CreateException ex) {
            throw new NuclosFatalException(ex);
         }
      }

      // return the id of a single created object or else the id of a single container object
      return (collgovoTarget.size() == 1) ? getGenericObjectFacade().get(iTargetGenericObjectId) : (iContainerCount == 1 ? goContainer : null);
   }

	private Integer getTargetFieldIdIfAny(GeneratorActionVO generatoractionvo, Integer iTargetAttributeId) {
		Integer iSourceModuleId = generatoractionvo.getSourceModuleId();
		  String sSourceEntity = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iSourceModuleId)).getEntity();
		  Integer iTargetModuleId = generatoractionvo.getTargetModuleId();
		  String sTargetEntity = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iTargetModuleId)).getEntity();
		  Map<String, EntityFieldMetaDataVO> mp = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(sTargetEntity);
		  for(String sFieldName : mp.keySet()) {
			  EntityFieldMetaDataVO voField = mp.get(sFieldName);
			  if(sSourceEntity.equals(voField.getForeignEntity())) {
				  iTargetAttributeId = voField.getId().intValue();
				  break;
			  }
		  }
		return iTargetAttributeId;
	}

   /**
    * 	Relate the new target object to the source object.
    * If source object is an invoice section relate the new object to the invoice:
    * @param govoSource
    * @param lofacade
    * @param generatoractionvo
    * @param iTargetGenericObjectId
    * @throws CommonFinderException
    */
   private void relateCreatedGenericObjectToParent(GenericObjectVO govoSource, GenericObjectFacadeLocal lofacade,
         GeneratorActionVO generatoractionvo, Integer iTargetGenericObjectId)
         throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException {

      // todo: \u00dcber Objekttyp differenzieren? (Rechnungsabschnitte in Reklamationen?)
      final Integer iSourceId = govoSource.getId();
      if (iSourceId != null) {
         try {
            lofacade.relate(generatoractionvo.getTargetModuleId(), iTargetGenericObjectId,
                  iSourceId, GenericObjectTreeNode.SystemRelationType.PREDECESSOR_OF.getValue());
         }
         catch (CommonCreateException ex) {
            throw new NuclosFatalException(ex);
         }
      }
   }

   /**
    * Creates a container object if necessary, and inserts the newly created generic object into it (by creating a part-of relation).
    * @param iObjectsToRelate the total number of generic objects to create
    * @param iContainerCount the number of container objects to create
    * @param iObjectCount the number of generic object in one container (or equals nNumTotal, if no container is desired)
    * @param gofacade
    * @param iContainerId the id of the last container created by this routine
    * @param iTargetGenericObjectId the id of the generic object to possibly insert into a container
    * @return the id of the last container created by this routine
    * @throws CommonPermissionException
    * @throws NuclosBusinessRuleException
    * @throws CommonCreateException
    * @throws CommonFinderException
    * @todo group exceptions
    */
   @SuppressWarnings("deprecation")
	private GenericObjectVO insertCreatedGenericObjectIntoContainer(int iObjectsToRelate, int iContainerCount, int iObjectCount,
         GenericObjectFacadeLocal gofacade, GenericObjectVO goContainer, Integer iTargetGenericObjectId)
         throws CommonPermissionException, NuclosBusinessRuleException, CommonCreateException, CommonFinderException {

      GenericObjectVO result = goContainer;

      // if there are collective orders to relate to, choose the right one and relate to it (as part-of)
      if (iContainerCount > 0) {
         if (iObjectsToRelate % iObjectCount == 0) {
            final GenericObjectVO govoContainer = new GenericObjectVO(RuleConstants.MODULE_ORDER, null, null, GenericObjectMetaDataCache.getInstance());

            // Set the process of the container to "Sammelauftrag"
            try {
               final Integer iProcessAttributeId = NuclosEOField.PROCESS.getMetaData().getId().intValue();
               govoContainer.setAttribute(DynamicAttributeVO.createGenericObjectAttributeVOCanonical(iProcessAttributeId, iCollectiveOrderProcessId, sCollectiveOrderProcessName, AttributeCache.getInstance()));
            }
            catch (CommonValidationException ex) {
               throw new NuclosFatalException(ex);
            }

            // create a new collective order (empty order object)
            result = gofacade.create(new GenericObjectWithDependantsVO(govoContainer, new DependantMasterDataMap()));
         }
         // relate the newly created order to the newly created collective order
         try {
            gofacade.relate(RuleConstants.MODULE_ORDER, result.getId(), iTargetGenericObjectId, GenericObjectTreeNode.SystemRelationType.PART_OF.getValue());
         }
         catch (CommonCreateException ex) {
            throw new NuclosFatalException(ex);
         }
      }
      return result;
   }

   /**
    * Executes a list of commands prepared by the rule interface for actions, which cannot be performed before object creation, e.g. object relation with the freshly created object.
    * @param lstActionsFromRules List<String> of commands from deferred methods
    * @param iTargetGenericObjectId id of the  freshly created object (if any)
    * @param lofacade (for performance)
    * @throws NuclosBusinessRuleException
    * @throws CommonCreateException
    * @throws CommonFinderException
    */
   private void performDeferredActionsFromRules(List<String> lstActionsFromRules, Integer iTargetGenericObjectId, GenericObjectFacadeLocal lofacade)
         throws NuclosBusinessRuleException, CommonCreateException, CommonFinderException, CommonPermissionException {

      for (String sAction : lstActionsFromRules) {
         final String[] asAction = sAction.split(":", 4);
         if ("relate".equals(asAction[0])) {
            if (asAction[1].equals(asAction[2])) {
               throw new NuclosBusinessRuleException("generator.facade.exception.1");//"Ein Objekt darf nicht mit sich selbst verkn\u00fcpft werden.");
            }
            final Integer iTargetId = "this".equals(asAction[1]) ? iTargetGenericObjectId : Integer.parseInt(asAction[1]);
            final Integer iTargetModuleId = lofacade.getModuleContainingGenericObject(iTargetId);
            String relationType = asAction[3];
				lofacade.relate(iTargetModuleId, iTargetId,
                  "this".equals(asAction[2]) ? iTargetGenericObjectId : Integer.parseInt(asAction[2]),
                  relationType
            );
         }
      }
   }
   
   private Collection<RuleObjectContainerCVO> getObjectSourceVOs(Collection<Integer> collSourceGenericObjectId, Integer parameterObjectId, GeneratorActionVO generatoractionvo) throws CommonPermissionException, CommonFinderException {
	   Collection<RuleObjectContainerCVO> col = new ArrayList<RuleObjectContainerCVO>();
	   for(Integer iSourceId : collSourceGenericObjectId){
		   RuleObjectContainerCVO loccvoSource = getGenericObjectFacade().getRuleObjectContainerCVO(Event.GENERATION_BEFORE, iSourceId);
		   col.add(loccvoSource);
	   }	   
	   return col;
   }
   
   private Map<String, Collection<RuleObjectContainerCVO>> groupGenerationSources(Collection<RuleObjectContainerCVO> col, GeneratorActionVO generatoractionvo) {
	   
	   List<Pair<Integer, Integer>> lstAttributes = getIncludedAttributeIds(generatoractionvo, null);
	   
	   Map<String, Collection<RuleObjectContainerCVO>> mp = new HashMap<String, Collection<RuleObjectContainerCVO>>();
	   
	   for(RuleObjectContainerCVO containerVO : col) {
		   GenericObjectVO gvo = containerVO.getGenericObject();
		   StringBuffer sb = new StringBuffer();
		   for(Pair<Integer, Integer> pair : lstAttributes){
			   Object obj = gvo.getAttribute(pair.getX()).getValue();
			   sb.append(obj);
			   sb.append(".");
		   }
		   final String key = sb.toString();
		   if(!mp.containsKey(key)) {
			   mp.put(key, new ArrayList<RuleObjectContainerCVO>());
			   mp.get(key).add(containerVO);
		   }
		   else {
			   for(String sKey : mp.keySet()) {
				   if(sKey.equals(key)) {
					   mp.get(sKey).add(containerVO);
				   }
			   }
		   }
	   }
	   
	   return mp;
	   
   }
   
   /**
    * Generates one target object and fires the generation rules for a collection of source objects
    * @param collSourceGenericObjectId
    * @param generatoractionvo
    * @return
 * @throws CommonFinderException 
 * @throws CommonPermissionException 
    * @throws CommonFinderException
    * @throws CommonPermissionException
    * @throws NuclosBusinessRuleException
    * @throws CommonStaleVersionException
    * @throws CommonValidationException
    * @nucleus.permission mayWrite(generatoractionvo.getTargetModuleId())
    */
   @Override
   @RolesAllowed("Login")
   public Collection<Integer> generateGenericObjectFromMultipleSourcesWithAttributeGrouping(Collection<Integer> collSourceGenericObjectId,
       Integer parameterObjectId, GeneratorActionVO generatoractionvo) throws CommonPermissionException, CommonFinderException {
	   
	    Collection<Integer> col = new ArrayList<Integer>();
	   
	    Collection<RuleObjectContainerCVO> colContainer = getObjectSourceVOs(collSourceGenericObjectId, parameterObjectId, generatoractionvo);
	    Map<String, Collection<RuleObjectContainerCVO>> mp = groupGenerationSources(colContainer, generatoractionvo);
	    
	    for(String sKey : mp.keySet()) {
	    	Collection<RuleObjectContainerCVO> colSources = mp.get(sKey);
	    	
	    	Collection<Integer> coSourceId = CollectionUtils.transform(colSources, new Transformer<RuleObjectContainerCVO, Integer>() {
				@Override
				public Integer transform(RuleObjectContainerCVO i) {
					return i.getGenericObject().getId();
				}	    		
	    	});
	    	
	    	
	    	try {
				col.add(generateGenericObjectFromMultipleSources(coSourceId, parameterObjectId, generatoractionvo));
			}
			catch(NuclosBusinessRuleException e) {
				throw new NuclosFatalException(e);
			}
			catch(NuclosBusinessException e) {
				throw new NuclosFatalException(e);
			}
			catch(CommonStaleVersionException e) {
				throw new NuclosFatalException(e);
			}
			catch(CommonValidationException e) {
				throw new NuclosFatalException(e);
			}
	    	
	    }
	    
	    return col;	   
   }

   /**
    * Generates one target object and fires the generation rules for a collection of source objects
    * @param collSourceGenericObjectId
    * @param generatoractionvo
    * @return
    * @throws CommonFinderException
    * @throws CommonPermissionException
    * @throws NuclosBusinessRuleException
    * @throws CommonStaleVersionException
    * @throws CommonValidationException
    * @nucleus.permission mayWrite(generatoractionvo.getTargetModuleId())
    */
   @Override
@RolesAllowed("Login")
   public Integer generateGenericObjectFromMultipleSources(Collection<Integer> collSourceGenericObjectId,
         Integer parameterObjectId, GeneratorActionVO generatoractionvo)
         throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, NuclosBusinessException,
         CommonStaleVersionException, CommonValidationException {

	   String parameterEntityName = null;
	   if (generatoractionvo.getParameterEntityId() != null) {
		   parameterEntityName = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(generatoractionvo.getParameterEntityId())).getEntity();
	   }

	   Integer iTargetGenericObjectId = null;
	   final DependantMasterDataMap dmdmp = new DependantMasterDataMap();
      final Iterator<Integer> iterSource = collSourceGenericObjectId.iterator();
      if (iterSource.hasNext()) {
         Integer iSourceGenericObjectId = iterSource.next();
         iTargetGenericObjectId = generateGenericObject(iSourceGenericObjectId, parameterObjectId, generatoractionvo, false).getId();
         
         if (iTargetGenericObjectId == null)
         	throw new NuclosBusinessException("generator.facade.exception.2");//"Die Objektgenerierung kann nicht durchgef\u00fchrt werden da Pflichtfelder nicht gef\u00fcllt sind.");

         
         RuleObjectContainerCVO loccvoTarget = getGenericObjectFacade().getRuleObjectContainerCVO(Event.GENERATION_BEFORE, iTargetGenericObjectId);
         final List<String> lstActionsFromRules = new ArrayList<String>();
         try {
            // Against the first impression this loop construct is correct; the generation rules for the first source are executed in the generateGenericObject above!
            while (iterSource.hasNext()) {
               iSourceGenericObjectId = iterSource.next();
               final RuleObjectContainerCVO loccvoSource = getGenericObjectFacade().getRuleObjectContainerCVO(Event.GENERATION_BEFORE, iSourceGenericObjectId);
               // (Re-)load parameter object (maybe it's changed by previous rule invocation)
               final String sourceEntityName = Modules.getInstance().getEntityNameByModuleId(loccvoSource.getGenericObject().getModuleId());
               
               copyDependants(loccvoSource, sourceEntityName, null, loccvoTarget.getGenericObject(), generatoractionvo, dmdmp, true);
               
               RuleObjectContainerCVO loccvoParameter = null;
               if (parameterEntityName != null) {
            	   loccvoParameter = getRuleObjectContainerCVO(Event.GENERATION_BEFORE, parameterEntityName, parameterObjectId);
               }
               addDepandants(loccvoTarget, dmdmp);
               loccvoTarget = executeGenerationRules(generatoractionvo, loccvoSource, loccvoTarget, loccvoParameter, lstActionsFromRules, false);

               // Object relations must not be generated when a claim is created from many invoice sections
               relateCreatedGenericObjectToParent(loccvoSource.getGenericObject(), getGenericObjectFacade(), generatoractionvo, iTargetGenericObjectId);
            }
            
            DependantMasterDataMap mp = groupSubformAttributes(loccvoTarget, collSourceGenericObjectId, generatoractionvo);
            if(mp != null && !mp.isEmpty()) 
            	replaceDepandants(loccvoTarget, mp);
            
            // Actually write the changes to the object
            iTargetGenericObjectId = getGenericObjectFacade().modify(loccvoTarget.getGenericObject(), loccvoTarget.getDependants(), true).getId();

            performDeferredActionsFromRules(lstActionsFromRules, iTargetGenericObjectId, getGenericObjectFacade());
            
            // (Re-)load parameter object (maybe it's changed by previous rule invocation)
            RuleObjectContainerCVO loccvoParameter = null;
            if (parameterEntityName != null) {
         	   loccvoParameter = getRuleObjectContainerCVO(Event.GENERATION_AFTER, parameterEntityName, parameterObjectId);
            }
            executeGenerationRules(generatoractionvo, null, new RuleObjectContainerCVO(Event.GENERATION_AFTER, loccvoTarget.getGenericObject(), loccvoTarget.getDependants()), loccvoParameter, new ArrayList<String>(), true);

         }
         catch (CommonBusinessException ex) {
            throw new NuclosFatalException(ex);
         }
         catch (CreateException ex) {
            throw new NuclosFatalException(ex);
         }

      }
      
      return iTargetGenericObjectId;
   }
   
   private DependantMasterDataMap groupSubformAttributes(RuleObjectContainerCVO loccvoTarget, Collection<Integer> collSourceGenericObjectId, 
	   GeneratorActionVO generatoractionvo) {
	   final Collection<String> collEntityNamesSource = new ArrayList<String>();
	   final Collection<String> collEntityNamesTarget = new ArrayList<String>();
	   DependantMasterDataMap mp = new DependantMasterDataMap();
	   final String sourceEntityName = Modules.getInstance().getEntityNameByModuleId(generatoractionvo.getSourceModuleId());
	   
	   final Collection<MasterDataVO> collEntitiesForThisGeneration = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONSUBENTITY.getEntityName(), "generation", generatoractionvo.getId());
	   if (collEntitiesForThisGeneration.isEmpty())
		   return null; 
	   
	   List<PairHelper> lstSubEntitiesToTransfer = new ArrayList<PairHelper>(collEntitiesForThisGeneration.size());
	   for (MasterDataVO mdvoGen : collEntitiesForThisGeneration) {
		   if(mdvoGen.getField("groupAttributes") != null && (Boolean)mdvoGen.getField("groupAttributes") == false)
			   continue;
			   
		   
		   // check source type
		   String sSource = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId((Integer)mdvoGen.getField("entitySourceId"))).getEntity();
		   String sTarget = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId((Integer)mdvoGen.getField("entityTargetId"))).getEntity();
		   // check specified against available subentities
//		   if (!collEntityNamesSource.contains(sSource) || !collEntityNamesTarget.contains(sTarget)) {
//			   info("In der Objektgenerierung " + generatoractionvo.getName() + " wird versucht, die Quellentit\u00e4t " + sSource + " in die Zielentit\u00e4t " + sTarget + " zu kopieren. " +
//			   "Dies ist aufgrund der verwendeten Layouts nicht m\u00f6glich und wird deshalb \u00fcbersprungen.");
//			   continue;
//		   }
		   lstSubEntitiesToTransfer.add(new PairHelper(Pair.makePair(sSource, sTarget), mdvoGen.getIntId()));
	   }
	   
	   for (PairHelper p : lstSubEntitiesToTransfer) {
		   final String sEntitySourceName = p.getPair().x;
		   final String sEntityTargetName = p.getPair().y;

		   final MasterDataMetaVO sourceDependantMetaVO = MasterDataMetaCache.getInstance().getMetaData(sEntitySourceName);
		   final MasterDataMetaVO targetDependantMetaVO = MasterDataMetaCache.getInstance().getMetaData(sEntityTargetName);
		   
		   final Collection<MasterDataVO> collmdvoOriginal = getGroupedAttributes(generatoractionvo, sEntitySourceName, p.getId(), collSourceGenericObjectId);
		   final Collection<MasterDataVO> collmdvoCopy = new ArrayList<MasterDataVO>();

		   if (collmdvoOriginal.isEmpty()) {
			   continue;
		   }
		   
		   Integer id = p.getId();
		   final Collection<MasterDataVO> collAttributesForThisGeneration = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONSUBENTITYATTRIBUTE.getEntityName(), "entity", id);
		   if(collAttributesForThisGeneration.size() == 0) {
			   continue;
		   }
		   else {
			   for (MasterDataVO mdvoOriginal : collmdvoOriginal) {
				   MasterDataVO mdvo = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(sEntityTargetName), true);
				   for(MasterDataVO voField : collAttributesForThisGeneration) {
					   final String sField = (String)voField.getField("subentityAttributeSource");					   
					   final Object value = mdvoOriginal.getField(sField);
					   mdvo.setField((String)voField.getField("subentityAttributeTarget"), value);
					   
					   final EntityFieldMetaDataVO metaField = MetaDataServerProvider.getInstance().getEntityField(sourceDependantMetaVO.getEntityName(), sField);
					   if(metaField.getForeignEntity() != null && metaField.getForeignEntityField() != null) {
						   final Object valueId = mdvoOriginal.getField(sField+ "Id");
						   mdvo.setField((String)voField.getField("subentityAttributeTarget")+"Id", valueId);
					   }
				   }
				   mdvo.setField(DependantMasterDataMap.getForeignKeyField(sourceDependantMetaVO, sourceEntityName), null);
				   collmdvoCopy.add(mdvo);
			   }
		   }

		   mp.addAllValues(sEntityTargetName, collmdvoCopy);		   
	   }	 
	   return mp;
   }
   
   private Collection<MasterDataVO> getGroupedAttributes(GeneratorActionVO generatoractionvo, final String subEntity, 
	   Integer iId, Collection<Integer> collSourceGenericObjectId) {
	   Collection<MasterDataVO> col = new ArrayList<MasterDataVO>();
	   final Collection<MasterDataVO> collSubFormAttributes = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONSUBENTITYATTRIBUTE.getEntityName(), "entity", iId);
	   if(collSubFormAttributes.size() < 1)
		   return new ArrayList<MasterDataVO>();
	   
	   Collection<MasterDataVO> colCopy = new ArrayList<MasterDataVO>(collSubFormAttributes);
	   for(MasterDataVO vo : colCopy) {
		   final String sField = (String)vo.getField("subentityAttributeSource");
		   EntityFieldMetaDataVO metaField = MetaDataServerProvider.getInstance().getEntityField(subEntity, sField);
		   if(metaField.getForeignEntity() != null && metaField.getForeignEntityField() != null){
			   MasterDataVO voCopy = vo.copy();
			   voCopy.setField("subentityAttributeSource", voCopy.getField("subentityAttributeSource")+"Id");
			   voCopy.setField("#isIdField#", true);
			   collSubFormAttributes.add(voCopy);
		   }
	   }
	   
	   
	   
	   List<MasterDataVO> lstGroup = new ArrayList<MasterDataVO>();
	   List<MasterDataVO> lstSum = new ArrayList<MasterDataVO>();
	   List<MasterDataVO> lstMin = new ArrayList<MasterDataVO>();
	   List<MasterDataVO> lstMax = new ArrayList<MasterDataVO>();
	   List<MasterDataVO> lstNoFunction = new ArrayList<MasterDataVO>();
	   for(MasterDataVO vo : collSubFormAttributes) {
		   final String sGroupField = (String)vo.getField("subentityAttributeGrouping");
		   if("group by".equals(sGroupField)) {
			   lstGroup.add(vo);
		   }
		   else if("summate".equals(sGroupField)) {
			   lstSum.add(vo);
		   }
		   else if("minimum value".equals(sGroupField)) {
			   lstMin.add(vo);
		   }
		   else if("maximum value".equals(sGroupField)) {
			   lstMax.add(vo);
		   }
		   else {
			   lstNoFunction.add(vo);
		   }
	   }
	   
	   try {
		    String sTable = MetaDataServerProvider.getInstance().getEntity(subEntity).getDbEntity();
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();			
			DbQuery<DbTuple> query = builder.createTupleQuery();
			DbFrom t = query.from(sTable).alias("t");
			List<DbExpression<?>> lstSelection = new ArrayList<DbExpression<?>>();
			List<DbExpression<?>> lst = new ArrayList<DbExpression<?>>();
			for(MasterDataVO vo : lstGroup){
				String sColumn = (String)vo.getField("subentityAttributeSource");
				if(vo.getField("#isIdField#") != null){
					sColumn = sColumn.substring(0, sColumn.length()-2);
					MasterDataMetaFieldVO mdmfVO = MasterDataMetaCache.getInstance().getMetaData(subEntity).getField(sColumn);
					sColumn = mdmfVO.getDBFieldName();
					sColumn = sColumn.replaceFirst("STRVALUE", "INTID");
					DbColumnExpression<?> c = t.column(sColumn, DalUtils.getDbType(mdmfVO.getJavaClass()));
					c.alias(mdmfVO.getFieldName()+"Id");
					lst.add(c);
					lstSelection.add(c);
				}
				else {
					MasterDataMetaFieldVO mdmfVO = MasterDataMetaCache.getInstance().getMetaData(subEntity).getField(sColumn);
					sColumn = mdmfVO.getDBFieldName();
					DbColumnExpression<?> c = t.column(sColumn, DalUtils.getDbType(mdmfVO.getJavaClass()));
					c.alias(mdmfVO.getFieldName());
					lst.add(c);
					lstSelection.add(c);
				}
			}
			if(lst.size() > 0)
				query.groupBy(lst);
			
			for(MasterDataVO vo : lstSum){
				String sColumn = (String)vo.getField("subentityAttributeSource");
				MasterDataMetaFieldVO mdmfVO = MasterDataMetaCache.getInstance().getMetaData(subEntity).getField(sColumn);
				sColumn = mdmfVO.getDBFieldName();
				DbColumnExpression<?> c = t.column(sColumn, DalUtils.getDbType(mdmfVO.getJavaClass()));
				c.alias(mdmfVO.getFieldName());
				DbExpression<?> dbe = builder.sum(c);
				dbe.alias(mdmfVO.getFieldName());
				lstSelection.add(dbe);
			}
			
			for(MasterDataVO vo : lstMin){
				String sColumn = (String)vo.getField("subentityAttributeSource");
				MasterDataMetaFieldVO mdmfVO = MasterDataMetaCache.getInstance().getMetaData(subEntity).getField(sColumn);
				sColumn = mdmfVO.getDBFieldName();
				DbColumnExpression<?> c = t.column(sColumn, DalUtils.getDbType(mdmfVO.getJavaClass()));
				c.alias(mdmfVO.getFieldName());
				DbExpression<?> dbe = builder.min(c);
				dbe.alias(mdmfVO.getFieldName());
				lstSelection.add(dbe);
			}
			
			for(MasterDataVO vo : lstMax){
				String sColumn = (String)vo.getField("subentityAttributeSource");
				MasterDataMetaFieldVO mdmfVO = MasterDataMetaCache.getInstance().getMetaData(subEntity).getField(sColumn);
				sColumn = mdmfVO.getDBFieldName();
				DbColumnExpression<?> c = t.column(sColumn, DalUtils.getDbType(mdmfVO.getJavaClass()));
				c.alias(mdmfVO.getFieldName());
				DbExpression<?> dbe = builder.min(c);
				dbe.alias(mdmfVO.getFieldName());				
				lstSelection.add(dbe);
			}
			
			for(MasterDataVO vo : lstNoFunction){
				String sColumn = (String)vo.getField("subentityAttributeSource");
				MasterDataMetaFieldVO mdmfVO = MasterDataMetaCache.getInstance().getMetaData(subEntity).getField(sColumn);
				sColumn = mdmfVO.getDBFieldName();
				DbColumnExpression<?> c = t.column(sColumn, DalUtils.getDbType(mdmfVO.getJavaClass()));
				c.alias(mdmfVO.getFieldName());								
				lstSelection.add(c);
			}
			
			query.multiselect(lstSelection);

			DbCondition cond[] = new DbCondition[collSourceGenericObjectId.size()];
			int counter = 0;
			String sourceEntityName = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(generatoractionvo.getSourceModuleId())).getEntity();
			String foreignField = DependantMasterDataMap.getForeignKeyField(MasterDataMetaCache.getInstance().getMetaData(subEntity), sourceEntityName, false);
			String dbColumn = MetaDataServerProvider.getInstance().getEntityField(subEntity, foreignField).getDbColumn();
			dbColumn = dbColumn.replaceFirst("STRVALUE", "INTID");
			for(Integer iSourceId : collSourceGenericObjectId) {
				cond[counter++] = builder.equal(t.column(dbColumn, Integer.class), iSourceId);
			}
			query.where(builder.or(cond));
			
			List<MasterDataVO> lstTransformed = DataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, MasterDataVO>() {

				@Override
				public MasterDataVO transform(DbTuple i) {
					MasterDataVO mvo = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(subEntity), true);
					for(DbTupleElement<?> el : i.getElements()) {
						String sAlias = el.getAlias();
						Object obj = i.get(sAlias);
						obj = getObjectForAlias(subEntity, sAlias, obj);
						mvo.setField(sAlias, obj);
					}
					
					return mvo;
				}
				
			});
			col.addAll(lstTransformed);
		}
		catch(Exception ex) {
			throw new NuclosFatalException(ex);
		} 
	   
	   return col;
   }
   
   private static Object getObjectForAlias(String entity, String alias, Object obj){
	   
	   if(alias.endsWith("Id")){
		   EntityFieldMetaDataVO metaField = MetaDataServerProvider.getInstance().getEntityField(entity, alias.substring(0, alias.length()-2));
		   if(metaField.getForeignEntity() != null && metaField.getForeignEntityField() != null){
			   if(obj instanceof String){
				   if(NumberUtils.isNumber((String)obj)) {
					   return new Long((String)obj);
				   }
			   }
		   }
	   }
	   
	   return obj;
   }
   
   
   
   private void addDepandants(RuleObjectContainerCVO container, DependantMasterDataMap mp) {
	   for(String sEntity : mp.getEntityNames()) {
		   for(MasterDataVO vo : mp.getValues(sEntity)) {			   
			   container.addDependant(sEntity, vo);
		   }
	   }
   }
   
   private void replaceDepandants(RuleObjectContainerCVO container, DependantMasterDataMap mp) {
	   for(String sEntity : mp.getEntityNames()) {
		   container.getDependants(false).getValues(sEntity).clear();
		   for(MasterDataVO vo : mp.getValues(sEntity)) {			   
			   container.addDependant(sEntity, vo);
		   }
	   }
   }

   private RuleObjectContainerCVO executeGenerationRules(GeneratorActionVO generatoractionvo,
         RuleObjectContainerCVO loccvoSource, RuleObjectContainerCVO loccvoTargetBeforeRules, RuleObjectContainerCVO loccvoParameter, List<String> lstActions, Boolean after) throws NuclosBusinessRuleException, CreateException
   {
   	RuleEngineFacadeLocal facade = ServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class);
   	return facade.fireGenerationRules(generatoractionvo.getId(), loccvoSource, loccvoTargetBeforeRules, loccvoParameter, lstActions, generatoractionvo.getProperties(), after);
   }

   /**
    * copies all dependant records from source object to target object.
    * Copies also a basekey and ordernumber attribute value as dependant into target, if appropriate and desired
    *
    * @param loccvoSource source generic object with dependant masterdata
 * @param gavo	 details about generation action
 * @param dependants DependantMasterDataMap for the the result
    * @throws CommonPermissionException
    */
   private void copyDependants(RuleObjectContainerCVO sourceCVO, String sourceEntityName, String sourceType, GenericObjectVO govoTarget, GeneratorActionVO gavo, DependantMasterDataMap dependants, boolean copyDepandants) throws CommonPermissionException {
	   final GenericObjectMetaDataCache lometacache = GenericObjectMetaDataCache.getInstance();

	   final Collection<String> collEntityNamesSource;
	   final Collection<String> collEntityNamesTarget;

	   try {
		   final AttributeProvider attrprovider = AttributeCache.getInstance();

		   // Get the available subentities by the layout
		   if (sourceCVO.getGenericObject() != null) {
			   final int iLayoutIdSource = lometacache.getBestMatchingLayoutId(sourceCVO.getGenericObject().getUsageCriteria(attrprovider), false);
			   collEntityNamesSource = lometacache.getSubFormEntityNamesByLayoutId(iLayoutIdSource);

		   } else {
			   LayoutFacadeLocal layoutLocal = ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
			   Map<EntityAndFieldName, String> subFormEntityAndParentSubFormEntityNames = layoutLocal.getSubFormEntityAndParentSubFormEntityNames(sourceEntityName, sourceCVO.getMasterData().getIntId(), false);
			   collEntityNamesSource = new HashSet<String>();
			   for (Map.Entry<EntityAndFieldName, String> e : subFormEntityAndParentSubFormEntityNames.entrySet()) {
				   if (e.getValue() == null)
					   collEntityNamesSource.add(e.getKey().getEntityName());
			   }
		   }
		   final int iLayoutIdTarget = lometacache.getBestMatchingLayoutId(govoTarget.getUsageCriteria(attrprovider), false);
		   collEntityNamesTarget = lometacache.getSubFormEntityNamesByLayoutId(iLayoutIdTarget);
	   } catch (CommonFinderException ex) {
		   // Nothing found, nothing to copy (?)
		   return;
	   }

	   // Get the names of all subentity entries to transfer, specified for this generation
	   final Collection<MasterDataVO> collEntitiesForThisGeneration = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONSUBENTITY.getEntityName(), "generation", gavo.getId());
	   if (collEntitiesForThisGeneration.isEmpty())
		   return;

	   // Check the subentites defined in object generation entry
	   List<PairHelper> lstSubEntitiesToTransfer = new ArrayList<PairHelper>(collEntitiesForThisGeneration.size());
	   for (MasterDataVO mdvoGen : collEntitiesForThisGeneration) {
		   // check source type
		   if (!LangUtils.equals(sourceType, mdvoGen.getField("sourceType")))
			   continue;
		   String sSource = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId((Integer)mdvoGen.getField("entitySourceId"))).getEntity();
		   String sTarget = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId((Integer)mdvoGen.getField("entityTargetId"))).getEntity();
		   // check specified against available subentities
		   if (!collEntityNamesSource.contains(sSource) || !collEntityNamesTarget.contains(sTarget)) {
			   info("In der Objektgenerierung " + gavo.getName() + " wird versucht, die Quellentit\u00e4t " + sSource + " in die Zielentit\u00e4t " + sTarget + " zu kopieren. " +
			   "Dies ist aufgrund der verwendeten Layouts nicht m\u00f6glich und wird deshalb \u00fcbersprungen.");
			   continue;
		   }
		   lstSubEntitiesToTransfer.add(new PairHelper(Pair.makePair(sSource, sTarget), mdvoGen.getIntId()));
	   }
	
	   // Now the preliminaries are done, copy the actual data:
	   for (PairHelper p : lstSubEntitiesToTransfer) {
		   final String sEntitySourceName = p.getPair().x;
		   final String sEntityTargetName = p.getPair().y;

		   final MasterDataMetaVO sourceDependantMetaVO = MasterDataMetaCache.getInstance().getMetaData(sEntitySourceName);
		   final MasterDataMetaVO targetDependantMetaVO = MasterDataMetaCache.getInstance().getMetaData(sEntityTargetName);

		   final Collection<MasterDataVO> collmdvoOriginal = sourceCVO.getDependants(sEntitySourceName);
		   final Collection<EntityObjectVO> collmdvoCopy = new ArrayList<EntityObjectVO>();

		   if (collmdvoOriginal.isEmpty()) {
			   continue;
		   }
		   // If target dependant entity is not editable, the corresponding modification will silently 
		   // fail. So we make this error case explicit here by throwing an an exception.
		   if (!targetDependantMetaVO.isEditable()) {
			   throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("generator.dependant.noneditable", sEntityTargetName));
		   }

		   boolean groupingfunction = false;
		   Integer id = p.getId();
		   final Collection<MasterDataVO> collAttributesForThisGeneration = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONSUBENTITYATTRIBUTE.getEntityName(), "entity", id);
		   if(collAttributesForThisGeneration.size() == 0) {
			   for (MasterDataVO mdvoOriginal : collmdvoOriginal) {
				   final MasterDataVO mdvo = mdvoOriginal.copy(false);
				   // Cut the dependance to the source generic object
				   // NUCLEUSINT-937: Previously, "genericObjectId" was hard-coded. Since NUCLEUSINT-888,
				   // the name of the foreign key field is determined by the referenced entity.
				   mdvo.setField(DependantMasterDataMap.getForeignKeyField(sourceDependantMetaVO, sourceEntityName), null);
				   collmdvoCopy.add(DalSupportForMD.getEntityObjectVO(mdvo));
			   }
		   }
		   else {
			   for (MasterDataVO mdvoOriginal : collmdvoOriginal) {
				   MasterDataVO mdvo = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(sEntityTargetName), true);
				   for(MasterDataVO voField : collAttributesForThisGeneration) {
					   final String sField = (String)voField.getField("subentityAttributeSource");
					   Object value = mdvoOriginal.getField(sField);
					   mdvo.setField((String)voField.getField("subentityAttributeTarget"), value);
					   
					   final EntityFieldMetaDataVO metaField = MetaDataServerProvider.getInstance().getEntityField(sourceDependantMetaVO.getEntityName(), sField);
					   if(metaField.getForeignEntity() != null) {
						   final Object valueId = mdvoOriginal.getField(sField+ "Id");
						   mdvo.setField((String)voField.getField("subentityAttributeTarget")+"Id", valueId);
					   }
					   
					   String function = (String)voField.getField("subentityAttributeGrouping");
					   if(function != null && function.length() > 0)
						   groupingfunction = true;
				   }
				   collmdvoCopy.add(DalSupportForMD.getEntityObjectVO(mdvo));
			   }
		   }
		   if(copyDepandants || !groupingfunction)
			   dependants.addAllData(sEntityTargetName, collmdvoCopy);
		   
	   }
   }
   
   class PairHelper {
	   
	   Pair<String, String> pair;
	   Integer id;
	   public PairHelper(Pair<String, String> pair, Integer id) {
		   this.pair = pair;
		   this.id = id;
	   }
	   
		public Pair<String, String> getPair() {
			return pair;
		}
		public Integer getId() {
			return id;
		}	   
   }

   /**
    * transfers (copies) a specified set of attributes from one generic object to another.
    * Called from within rules.
    * Attention: because this is called within a rule, the source genericobject and its attributes were not saved until now ->
    * the consequence is, that the old attribute values of the source genericobject were transfered to the target genericobject
    * this is very ugly -> todo
    *
    * @param iSourceGenericObjectId source generic object id to transfer data from
    * @param iTargetGenericObjectId target generic object id to transfer data to
    * @param asAttributes Array of attribute names to specify transferred data
    * @precondition asAttributes != null
    */
   @Override
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

         for (int i=0; i<aiIncludedAttributeIds.length; i++) {
            if (stSourceAttributeIds.contains(aiIncludedAttributeIds[i][0]) && !stExcludedAttributeIds.contains(aiIncludedAttributeIds[i][0])) {
               if (stTargetAttributeIds.contains(aiIncludedAttributeIds[i][1]) && !stExcludedAttributeIds.contains(aiIncludedAttributeIds[i][1])) {
                  copyAttribute(aiIncludedAttributeIds[i][0], aiIncludedAttributeIds[i][1], govoSource, govoTarget);
               }
            }
         }

         // todo: avoid using modify here as it triggers another rule!
         getGenericObjectFacade().modify(govoTarget.getModuleId(), new GenericObjectWithDependantsVO(govoTarget, new DependantMasterDataMap()));
      }
      catch (CommonBusinessException ex) {
         throw new NuclosFatalException(ex);
      }
      debug("Leaving transferGenericObjectData()");
   }

   /**
    * Copy the attribute with the given ID from the source generic object to the target generic object.
    * @param iAttributeSourceId
    * @param iAttributeTargetId
    * @param govoSource
    * @param govoTarget
    */
   private void copyAttribute(Integer iAttributeSourceId, Integer iAttributeTargetId, GenericObjectVO govoSource, GenericObjectVO govoTarget) {
      debug("Copying attribute " + AttributeCache.getInstance().getAttribute(iAttributeSourceId).getName() + "from " + govoSource.toString() +
            "to attribute " + AttributeCache.getInstance().getAttribute(iAttributeTargetId).getName() + "from " + govoTarget.toString());
      final DynamicAttributeVO attrvoSource = govoSource.getAttribute(iAttributeSourceId);
      final DynamicAttributeVO attrvoTarget = govoTarget.getAttribute(iAttributeTargetId);
      if (attrvoSource == null) {
         if (attrvoTarget != null) {
            // remove existent attribute:
            attrvoTarget.remove();
         }
      }
      else {
         if (attrvoTarget != null) {
            // update existent attribute:
            attrvoTarget.setValueId(attrvoSource.getValueId());
            attrvoTarget.setValue(attrvoSource.getValue());
         }
         else {
            // add nonexistent attribute:
            govoTarget.setAttribute(new DynamicAttributeVO(iAttributeTargetId,
                  attrvoSource.getValueId(),
                  attrvoSource.getValue()));
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

      for (int i = 0; i < asAttributeNames.length; i++ ){
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
    * @param govoSource				 source generic object
    * @param govoTargetTemplate target generic object template
    * @param generatoractionvo	 generator vo to determine attributes
    * @throws CommonPermissionException
    */
   private void copyAttributes(GenericObjectVO govoSource, GenericObjectVO govoTargetTemplate,
         GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonValidationException, CommonPermissionException {
      final List<Pair<Integer, Integer>> includedAttributes = getIncludedAttributeIds(generatoractionvo, null);

      final Collection<Integer> stSourceAttributeIds = getAttributeIdsByModuleId(govoSource.getModuleId());
      final Collection<Integer> stTargetAttributesIds = getAttributeIdsByModuleId(govoTargetTemplate.getModuleId());

      for (Pair<Integer, Integer> p : includedAttributes) {
		 if (stSourceAttributeIds.contains(p.x)) {
            if (stTargetAttributesIds.contains(p.y)) {
               DynamicAttributeVO davo_source = govoSource.getAttribute(p.x);
               if (davo_source != null) {
                  govoTargetTemplate.setAttribute(new DynamicAttributeVO(p.y, davo_source.getValueId(), davo_source.getValue()));
               }
            }
         }
      }
   }

   private void copyAttributes(RuleObjectContainerCVO sourceVO, String sourceEntityName, String sourceType, GenericObjectVO govoTargetTemplate,
       GeneratorActionVO generatoractionvo) throws CommonFinderException, CommonValidationException, CommonPermissionException {
    final List<Pair<Integer, Integer>> includedAttributes = getIncludedAttributeIds(generatoractionvo, sourceType);

    final Collection<Integer> stTargetAttributesIds = getAttributeIdsByModuleId(govoTargetTemplate.getModuleId());

    for (Pair<Integer, Integer> p : includedAttributes) {
      if (stTargetAttributesIds.contains(p.y)) {
    	  Integer valueId;
    	  Object value;
    	  if (sourceVO.getGenericObject() != null) {
    		  DynamicAttributeVO attr = sourceVO.getGenericObject().getAttribute(p.y);
    		  valueId = attr.getId();
    		  value = attr.getValue();
    	  } else if (sourceVO.getMasterData() != null) {
    		  EntityFieldMetaDataVO sourceField = MetaDataServerProvider.getInstance().getEntityField(sourceEntityName, p.x.longValue());
        	  String sourceFieldName = sourceField.getField();
    		  valueId = (Integer) sourceVO.getMasterData().getField(sourceFieldName + "Id");
    		  value = sourceVO.getMasterData().getField(sourceFieldName);
    	  } else {
    		  throw new NuclosFatalException("Invalid RuleObjectContainerCVO");
    	  }
    	  govoTargetTemplate.setAttribute(new DynamicAttributeVO(p.y, valueId, value));
       }
    }
 }

   private Collection<Integer> getAttributeIdsByModuleId(Integer iModuleId) {
      final Collection<Integer> stAttributesIds = new HashSet<Integer>();
      for (AttributeCVO attrcvo : GenericObjectMetaDataCache.getInstance().getAttributeCVOsByModuleId(iModuleId, false)) {
         stAttributesIds.add(attrcvo.getId());
      }
      return stAttributesIds;
   }

   /**
    * Extract the IDs of the attributes to copy from the given GeneratorActionVO as a collection.
    * @param generatoractionvo
    * @param sourceType
    * @return list of pairs (source id, target id)
    */
   private List<Pair<Integer, Integer>> getIncludedAttributeIds(GeneratorActionVO generatoractionvo, String sourceType) {
	   Collection<MasterDataVO> colmdvo = getMasterDataFacade().getDependantMasterData(NuclosEntity.GENERATIONATTRIBUTE.getEntityName(), "generation", generatoractionvo.getId());
	   List<Pair<Integer, Integer>> result = new ArrayList<Pair<Integer, Integer>>(colmdvo.size());
	   for (MasterDataVO mdvo : colmdvo) {
		   if (!ObjectUtils.equals(sourceType, mdvo.getField("sourceType")))
			   continue;
		   Integer sourceId = mdvo.getField("attributeSourceId", Integer.class);
		   Integer targetId = mdvo.getField("attributeTargetId", Integer.class);
		   if (getExcludedAttributeIds().contains(sourceId) || getExcludedAttributeIds().contains(targetId))
			   continue;
		   result.add(Pair.makePair(sourceId, targetId));
	   }
	   return result;
   }

   private Collection<Integer> getExcludedAttributeIds() {
      return collExcludedAttributeIds;
   }

   /**
    * update usages of rules
    * @param generatorId
    * @param usages
    * @throws EJBException
    */
   @Override
public void updateRuleUsages(Integer generatorId, Collection<GeneratorRuleVO> usages)
   	throws NuclosBusinessRuleException, CommonCreateException, CommonPermissionException, CommonStaleVersionException, CommonRemoveException, CommonFinderException {

      // as there is no chance to find existing database records and update them we have to remove all and create new ones:

   	CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
         MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULEGENERATION),"generation", generatorId);
      Collection<MasterDataVO> mdGenerationsVO = getMasterDataFacade().getMasterData(NuclosEntity.RULEGENERATION.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdGenerationsVO)
      	getMasterDataFacade().remove(NuclosEntity.RULEGENERATION.getEntityName(), mdVO, false);

      for (GeneratorRuleVO gr : usages) {
      	MasterDataVO mdVO = MasterDataWrapper.wrapRuleEngineGenerationVO(
      		new RuleEngineGenerationVO(new NuclosValueObject(), generatorId, gr.getId(), gr.getOrder(), gr.isRunAfterwards()));
      	getMasterDataFacade().create(NuclosEntity.RULEGENERATION.getEntityName(), mdVO, null);
      }
   }

   /**
    * get generator usages for specified GeneratorId
    * @param iGeneratorId
    * @return Collection<GeneratorUsageVO>
    * @throws CommonFatalException
    */
   @Override
public Collection<GeneratorUsageVO> getGeneratorUsages(Integer id) throws CommonFatalException {
      List<GeneratorUsageVO> usages = new ArrayList<GeneratorUsageVO>(); 
      CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
         MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.GENERATIONUSAGE),"generation", id);
      Collection<MasterDataVO> mdUsagesVO = getMasterDataFacade().getMasterData(NuclosEntity.GENERATIONUSAGE.getEntityName(), cond, true);

      for (MasterDataVO md : mdUsagesVO)
         usages.add(MasterDataWrapper.getGeneratorUsageVO(md));

      return usages;
   }

   /**
    * get rule usages for specified GeneratorId
    * @param iGeneratorId
    * @return
    * @throws CommonPermissionException
    */
   @Override
public Collection<GeneratorRuleVO> getRuleUsages(Integer generatorId) throws CommonPermissionException {
   	Collection<GeneratorRuleVO> result = new ArrayList<GeneratorRuleVO>();

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_RULE_GENERATION").alias("t");
		query.multiselect(t.column("INTID_T_MD_RULE", Integer.class), t.column("INTORDER", Integer.class), t.column("BLNRUNAFTERWARDS", Boolean.class));
		query.where(builder.equal(t.column("INTID_T_MD_GENERATION", Integer.class), generatorId));
		query.orderBy(builder.asc(t.column("INTORDER", Integer.class)));
  	
   	List<DbTuple> rulesWithOrders = DataBaseHelper.getDbAccess().executeQuery(query.distinct(true));
   		
   	for (DbTuple ruleWithOrder : rulesWithOrders) {
   		RuleVO ruleVO = RuleCache.getInstance().getRule(ruleWithOrder.get(0, Integer.class));
   		Boolean bRunAfterwards = ruleWithOrder.get(2, Boolean.class);
   		result.add(new GeneratorRuleVO(ruleVO.getId(), ruleVO.getName(), ruleVO.getDescription(), ruleWithOrder.get(1, Integer.class), bRunAfterwards==null?Boolean.FALSE:bRunAfterwards));
   	}

   	return result;
   }

   // Some private helpers...

   private GeneratorActionVO getGeneratorActionByName(String name) throws CommonFinderException {
   	CollectableComparison cond = SearchConditionUtils.newMDComparison(
         MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.GENERATION),"name", ComparisonOperator.EQUAL, name);
      Collection<MasterDataVO> mdGenerationsVO = getMasterDataFacade().getMasterData(NuclosEntity.GENERATION.getEntityName(), cond, true);

      if (mdGenerationsVO == null || mdGenerationsVO.isEmpty())
      	throw new CommonFinderException(StringUtils.getParameterizedExceptionMessage("generator.facade.exception.3", name));
      		//"Es ist ein Fehler bei der Objektgenerierung aufgetreten. Objektgenerator mit dem Namen " + name + " kann nicht gefunden werden.");

      MasterDataVO mdVO = mdGenerationsVO.iterator().next();

      return MasterDataWrapper.getGeneratorActionVO(mdVO,getGeneratorUsages(mdVO.getIntId()));
   }
   
	/**
	 * generate one or more generic objects from an existing generic object (copying selected attributes and subforms)
	 *
	 * @param iSourceGenericObjectId source generic object id to generate from
	 * @param generatoractionvo		 generator action value object to determine what to do
	 * @return id of generated generic object (if exactly one object was generated)
	 * @ejb.interface-method view-type="both"
	 * @ejb.permission role-name="Login"
	 */
	@Override
	public GenericObjectVO generateGenericObjectWithoutCheckingPermission(Integer iSourceGenericObjectId, GeneratorActionVO generatoractionvo)
			throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException,
			CommonStaleVersionException, CommonValidationException {

		return generateGenericObject(iSourceGenericObjectId, null, generatoractionvo);
	}
	
	private RuleObjectContainerCVO getRuleObjectContainerCVO(Event event, String entity, Integer intId) throws CommonBusinessException {
		EntityMetaDataVO entityVO = MetaDataServerProvider.getInstance().getEntity(entity);
		if (entityVO.isStateModel()) {
			GenericObjectVO govo = getGenericObjectFacade().get(intId);
			DependantMasterDataMap dependants = getGenericObjectFacade().reloadDependants(govo, null, true);
			return new RuleObjectContainerCVO(event, govo, dependants);
		} else {
			MasterDataWithDependantsVO mdvo = getMasterDataFacade().getWithDependants(entity, intId);
			return new RuleObjectContainerCVO(event, mdvo, mdvo.getDependants());
		}
	}
}
