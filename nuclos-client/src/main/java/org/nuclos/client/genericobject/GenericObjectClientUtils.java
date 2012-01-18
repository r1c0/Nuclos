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
package org.nuclos.client.genericobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.Utils;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.access.CgoWithDependantsSecurityAgentImpl;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.CollectableEntityFieldWithEntityForExternal;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.access.CefAllowAllSecurityAgentImpl;
import org.nuclos.common.collect.collectable.access.CefSecurityAgent;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.PivotJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.ModuleConstants;

/**
 * Utility methods for leased objects, client specific.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GenericObjectClientUtils {

	private static final Logger LOG = Logger.getLogger(GenericObjectCollectController.class);

	private GenericObjectClientUtils() {
	}

	/**
	 * @param clcte the entity of the field
	 * @param sFieldName the name of the field
	 * @param clcteMain the main entity
	 * @return a <code>CollectableEntityField</code> for the Result tab with the given entity and field name.
	 */
	public static CollectableEntityFieldWithEntityForExternal getCollectableEntityFieldForResult(final CollectableEntity clcte, String sFieldName, CollectableEntity clcteMain) {
		final String sMainEntityName = clcteMain.getName();
		final boolean bFieldBelongsToMainEntity = clcte.getName().equals(sMainEntityName);
		final boolean bFieldBelongsToSubEntity = !(bFieldBelongsToMainEntity);
		final CollectableEntityFieldWithEntityForExternal clctefwefe = new CollectableEntityFieldWithEntityForExternal(clcte, sFieldName, bFieldBelongsToSubEntity, bFieldBelongsToMainEntity);

		// set security agent, to check whether the user has the right to see the data in the result panel
		// setSecurityAgent(clcte, clctefwefe, bFieldBelongsToSubEntity);
		
		return clctefwefe;
	}

	/**
	 * TODO: For me, it is complete miracle why this is needed... (tp)
	 *
	 * @param entity Could be another entity than the entity the fields belongs to, because it is a subform field display in the result panel.
	 * @param field to set the SecurityAgent on
	 * @param bFieldBelongsToSubEntity true if fields belongs to a subform
	 */
	public static void setSecurityAgent(final Collectable instance, final CollectableEntityField field, final boolean bFieldBelongsToSubEntity) {
		final CefSecurityAgent sa;
		if (instance instanceof CollectableGenericObject) {
			// If instance is not CollectableGenericObjectWithDependants
			// but only CollectableGenericObject we could not find the state of the GenericObject.
			// Hence it is impossible to get the rights. (tp)
			// 
			try {
				sa = new CgoWithDependantsSecurityAgentImpl((CollectableGenericObjectWithDependants) instance, field, bFieldBelongsToSubEntity);
			}
			catch (ClassCastException e) {
				throw new NuclosFatalException("Permission can only be deduced from CollectableGenericObjectWithDependants, not from "
						+ instance.getClass().getName() + ": " + instance);
			}
		}
		else {
			sa = new CefAllowAllSecurityAgentImpl();
		}
		field.setSecurityAgent(sa);
	}

	/**
	 * @param iModuleId
	 * @param clctcond the "official" search condition that is visible to the user.
	 * @return the internal version of the collectable search condition, that is used for performing the actual search.
	 * The search condition for "general search" is treated specially here.
	 */
	public static CollectableSearchCondition getInternalSearchCondition(Integer iModuleId, CollectableSearchCondition clctcond) {
		return (iModuleId == null) ? getInternalSearchConditionForGeneralSearch(clctcond) : clctcond;
	}

	/**
	 * @param clctcond
	 * @return the internal search condition for the "general search" module. Attributes which have corresponding
	 * columns in subforms are treated specially.
	 */
	private static CollectableSearchCondition getInternalSearchConditionForGeneralSearch(CollectableSearchCondition clctcond) {
		return SearchConditionUtils.trueIfNull(clctcond).accept(new GetInternalSearchConditionVisitor());
	}

	/**
	 * inner class GetInternalSearchConditionVisitor
	 */
	private static class GetInternalSearchConditionVisitor implements Visitor<CollectableSearchCondition, RuntimeException> {

		@Override
		public CollectableSearchCondition visitTrueCondition(TrueCondition truecond) throws RuntimeException {
			return null;
		}

		@Override
		public CollectableSearchCondition visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) throws RuntimeException {
			final CollectableSearchCondition result;
			final List<Pair<String, String>> lstEntityAndFieldnames = AttributeFieldMapper.getFieldsForAttribute(atomiccond.getEntityField().getName());
			if (lstEntityAndFieldnames == null) {
				result = atomiccond;
			}
			else {
				final Collection<CollectableSearchCondition> collOperands = new ArrayList<CollectableSearchCondition>(lstEntityAndFieldnames.size() + 1);
				collOperands.add(atomiccond);
				for (Pair<String, String> pairEntityAndFieldname : lstEntityAndFieldnames) {
					final CollectableEntity clcteSub = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(pairEntityAndFieldname.getX());
					final String sForeignKeyFieldName = ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME;
					final CollectableEntityField clctefSub = clcteSub.getEntityField(pairEntityAndFieldname.getY());

					collOperands.add(new CollectableSubCondition(clcteSub.getName(), sForeignKeyFieldName,
						SearchConditionUtils.getConditionForPeer(clctefSub, atomiccond)));
				}
				result = new CompositeCollectableSearchCondition(LogicalOperator.OR, collOperands);
			}
			return result;
		}

		@Override
		public CollectableSearchCondition visitCompositeCondition(CompositeCollectableSearchCondition compositecond) throws RuntimeException {
			final List<CollectableSearchCondition> lstOperands = CollectionUtils.transform(compositecond.getOperands(), new Transformer<CollectableSearchCondition, CollectableSearchCondition>() {
				@Override
				public CollectableSearchCondition transform(CollectableSearchCondition cond) {
					return getInternalSearchConditionForGeneralSearch(cond);
				}
			});
			return new CompositeCollectableSearchCondition(compositecond.getLogicalOperator(), lstOperands);
		}

		@Override
		public CollectableSearchCondition visitIdCondition(CollectableIdCondition idcond) throws RuntimeException {
			return idcond;
		}

		@Override
		public CollectableSearchCondition visitSubCondition(CollectableSubCondition subcond) throws RuntimeException {
			return subcond;
		}

		@Override
		public CollectableSearchCondition visitPivotJoinCondition(PivotJoinCondition joincond) throws RuntimeException {
			return joincond;
		}

		@Override
		public CollectableSearchCondition visitRefJoinCondition(RefJoinCondition joincond) throws RuntimeException {
			return joincond;
		}

		@Override
		public CollectableSearchCondition visitReferencingCondition(ReferencingCollectableSearchCondition refcond) throws RuntimeException {
			throw new NotImplementedException("refcond");
		}

		@Override
        public CollectableSearchCondition visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
	        return collectableIdListCondition;
        }

	}	// inner class GetInternalSearchConditionVisitor

	/**
	 * Open a generic object of a certain module in the details view of a new GenericObjectCollectController
	 * @param parent
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @throws CommonBusinessException
	 */
	public static void showDetails(JComponent parent, int iModuleId, int iGenericObjectId) throws CommonBusinessException {
		final String sEntityName = Modules.getInstance().getEntityNameByModuleId(iModuleId);

		if (!SecurityCache.getInstance().isReadAllowedForModule(sEntityName, iGenericObjectId)) {
			throw new CommonPermissionException(CommonLocaleDelegate.getInstance().getMessage(
					"GenericObjectclientUtils.1", "Sie haben nicht das Recht, dieses Objekt anzuzeigen."));
		}

		Main.getInstance().getMainController().showDetails(sEntityName, iGenericObjectId);
	}

	/**
	 * Open a generic object of a certain module in the details view of a new GenericObjectCollectController
	 * @param parent
	 * @param iModuleId
	 * @throws CommonBusinessException
	 */
	public static CollectController<? extends Collectable> showDetails(JComponent parent, int iModuleId) throws CommonBusinessException {
		final String sEntityName = Modules.getInstance().getEntityNameByModuleId(iModuleId);

		if (!SecurityCache.getInstance().isWriteAllowedForModule(sEntityName, null)) {
			throw new CommonPermissionException(CommonLocaleDelegate.getInstance().getMessage(
					"GenericObjectclientUtils.2", "Sie haben nicht das Recht, dieses Objekt anzulegen."));
		}

		return Main.getInstance().getMainController().showDetails(sEntityName);
	}

	/**
	 * Open a collection of generic objects of unknown and possibly different modules in the appropriate view of the appropriate CollectController.
	 * @param parent
	 * @param collLeasedObjectIds
	 * @throws CommonBusinessException
	 */
	public static void showDetails(JComponent parent, Collection<Integer> collLeasedObjectIds) throws CommonBusinessException {
		Collection<Integer> collAssetModuleIds = new HashSet<Integer>(collLeasedObjectIds.size());
		for(Integer iAssetId : collLeasedObjectIds) {
			collAssetModuleIds.add(GenericObjectDelegate.getInstance().getModuleContainingGenericObject(iAssetId));
		}
		Integer iCommonModuleId = Utils.getCommonObject(collAssetModuleIds);

		if(collLeasedObjectIds.size() == 1) {
			// One related object must have a defined module, so open it in its detail view of the GenericObjectCollectController
			final GenericObjectCollectController ctlLeasedObject = NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(parent, iCommonModuleId, null);
			ctlLeasedObject.runViewSingleCollectableWithId(collLeasedObjectIds.iterator().next());
		}
		else if(iCommonModuleId != null) {
			// If more than one related objects share one module, open them in the result view of the GenericObjectCollectController
			final GenericObjectCollectController ctlLeasedObject = NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(parent, iCommonModuleId, null);
			ctlLeasedObject.runViewResults(getSearchConditionForRelatedObjects(collLeasedObjectIds));
		}
	}

	/**
	 * Build a search condition if there are more than one related objects
	 * @param collLeasedObjectIds
	 * @return OR condition over all related object ids
	 */
	private static CollectableSearchCondition getSearchConditionForRelatedObjects(Collection<Integer> collLeasedObjectIds) {
		CompositeCollectableSearchCondition cond = new CompositeCollectableSearchCondition(LogicalOperator.OR);

		for(Integer iId : collLeasedObjectIds) {
			cond.addOperand(new CollectableIdCondition(iId));
		}

		return cond;
	}

}	// class GenericObjectClientUtils
