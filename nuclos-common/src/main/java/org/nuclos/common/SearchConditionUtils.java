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
package org.nuclos.common;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

/**
 * Utility methods for CollectableSearchConditions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SearchConditionUtils extends org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils {

	private SearchConditionUtils() {
	}

	public static CollectableComparison newComparison(String sEntityName, String sFieldName, ComparisonOperator compop, Object oValue) {
		final CollectableEntityField clctef = newEntityField(sEntityName, sFieldName);
		return new CollectableComparison(clctef, compop, CollectableUtils.newCollectableFieldForValue(clctef, oValue));
	}

	public static CollectableLikeCondition newLikeCondition(String sEntityName, String sFieldName, String sLikeValue) {
		return new CollectableLikeCondition(newEntityField(sEntityName, sFieldName), sLikeValue);
	}

	public static CollectableIsNullCondition newIsNullCondition(String sEntityName, String sFieldName) {
		return new CollectableIsNullCondition(newEntityField(sEntityName, sFieldName));
	}

	/** Caution: Do NOT use this method to compare an id value, use {@link #newMDReferenceComparison(MasterDataMetaVO, String, Integer) instead. */
	public static CollectableComparison newMDComparison(MasterDataMetaVO mdmetavo, String sFieldName, ComparisonOperator compop, Object oValue) {
		final CollectableEntityField clctef = newMasterDataEntityField(mdmetavo, sFieldName);
		return new CollectableComparison(clctef, compop, CollectableUtils.newCollectableFieldForValue(clctef, oValue));
	}

	public static CollectableComparison newMDReferenceComparison(MasterDataMetaVO mdmetavo, String sFieldName, Integer referenceId) {
		final CollectableEntityField clctef = newMasterDataEntityField(mdmetavo, sFieldName);
		return new CollectableComparison(clctef, ComparisonOperator.EQUAL, CollectableUtils.newCollectableValueIdFieldForValueId(clctef, referenceId));
	}

	public static CollectableComparison newMDReferenceComparison(MasterDataMetaVO mdmetavo, String sFieldName, Integer referenceId, Object referenceValue) {
		final CollectableEntityField clctef = newMasterDataEntityField(mdmetavo, sFieldName);
		return new CollectableComparison(clctef, ComparisonOperator.EQUAL, CollectableUtils.newCollectableValueIdField(clctef, referenceId, referenceValue));
	}

	public static CollectableLikeCondition newMDLikeCondition(MasterDataMetaVO mdmetavo, String sFieldName, String sLikeValue) {
		return new CollectableLikeCondition(newMasterDataEntityField(mdmetavo, sFieldName), sLikeValue);
	}

	public static CollectableIsNullCondition newMDIsNullCondition(MasterDataMetaVO mdmetavo, String sFieldName) {
		return new CollectableIsNullCondition(newMasterDataEntityField(mdmetavo, sFieldName));
	}

	public static CollectableComparison newLOComparison(AttributeProvider attrprovider, String sEntity, String sAttributeName, ComparisonOperator compop, Object oValue) {
		final CollectableEntityField clctef = newGenericObjectEntityField(attrprovider, sEntity, sAttributeName);
		return new CollectableComparison(clctef, compop, CollectableUtils.newCollectableFieldForValue(clctef, oValue));
	}

	public static CollectableLikeCondition newLOLikeCondition(AttributeProvider attrprovider, String sEntity, String sAttributeName, String sLikeValue) {
		return new CollectableLikeCondition(newGenericObjectEntityField(attrprovider, sEntity, sAttributeName), sLikeValue);
	}

	public static CollectableIsNullCondition newLOIsNullCondition(AttributeProvider attrprovider, String sEntity, String sAttributeName) {
		return new CollectableIsNullCondition(newGenericObjectEntityField(attrprovider, sEntity, sAttributeName));
	}

	private static CollectableEntityField newGenericObjectEntityField(AttributeProvider attrprovider, String sEntity, String sAttributeName) {
		return new CollectableGenericObjectEntityField(
			attrprovider.getAttribute(sEntity, sAttributeName),
			attrprovider.getEntityField(sEntity, sAttributeName),
			sEntity);
	}

	public static CollectableEntityField newMasterDataEntityField(MasterDataMetaVO mdmetavo, String sFieldName) {
		return new CollectableMasterDataEntity(mdmetavo).getEntityField(sFieldName);
	}

	public static CollectableEntityField newEntityField(String sEntityName, String sFieldName) {
		return DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName).getEntityField(sFieldName);
	}

	public static CollectableComparison newEOComparison(String entity, String field, ComparisonOperator compop, Object oValue, MetaDataProvider metaProvider) {
		final CollectableEntityField clctef = newEOEntityField(entity, field, metaProvider);
		return new CollectableComparison(clctef, compop, CollectableUtils.newCollectableFieldForValue(clctef, oValue));
	}

	public static CollectableComparison newEOidComparison(String entity, String field, ComparisonOperator compop, Long id, MetaDataProvider metaProvider) {
		final CollectableEntityField clctef = newEOEntityField(entity, field, metaProvider);
		return new CollectableComparison(clctef, compop, CollectableUtils.newCollectableValueIdFieldForValueId(clctef, LangUtils.convertId(id)));
	}

	public static CollectableLikeCondition newEOLikeComparison(String entity, String field, String comparand, MetaDataProvider metaProvider) {
		final CollectableEntityField clctef = newEOEntityField(entity, field, metaProvider);
		return new CollectableLikeCondition(clctef, ComparisonOperator.LIKE, comparand);
	}

	public static CollectableIsNullCondition newEOIsNullComparison(String entity, String field, ComparisonOperator compop, MetaDataProvider metaProvider) {
		final CollectableEntityField clctef = newEOEntityField(entity, field, metaProvider);
		return new CollectableIsNullCondition(clctef, compop);
	}

	public static CollectableEntityField newEOEntityField(String entity, String field, MetaDataProvider metaProvider) {
		return new CollectableEOEntity(metaProvider.getEntity(entity), metaProvider.getAllEntityFieldsByEntity(entity)).getEntityField(field);
	}

}	// class SearchConditionUtils
