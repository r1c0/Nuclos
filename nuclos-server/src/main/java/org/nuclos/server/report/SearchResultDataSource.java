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
package org.nuclos.server.report;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.nuclos.common.PDFHelper;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.genericobject.GenericObjectUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;

/**
 * Data source for leased object search result with dynamic attributes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class SearchResultDataSource implements JRDataSource {

	private final Map<String, Class<?>> mpTypes;
	private final Map<String, Object> mpRows = new HashMap<String, Object>();
	private final Iterator<GenericObjectWithDependantsVO> iterator;
	private final String sMainEntityName;
	private final String sParentEntityName;
	private final List<? extends CollectableEntityField> lstclctefweSelected;

	/**
	 * @param clctexpr search expression
	 * @param lstclctefweSelected columns to include in the data source
	 * @param iModuleId id of module to get result for
	 * @param bIncludeSubModules
	 */
	public SearchResultDataSource(CollectableSearchExpression clctexpr,
			List<? extends CollectableEntityField> lstclctefweSelected, Integer iModuleId, boolean bIncludeSubModules) {

		final GenericObjectFacadeLocal goFacade = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);

		this.lstclctefweSelected = lstclctefweSelected;
		this.sMainEntityName = Modules.getInstance().getEntityNameByModuleId(iModuleId);
		// module id is null in generalsearch...
		this.sParentEntityName = (iModuleId == null) ? "" : Modules.getParentModuleName(Modules.getInstance().getModuleById(iModuleId));

		final List<Integer> lstAttributeIds = GenericObjectUtils.getAttributeIds(lstclctefweSelected, sMainEntityName, AttributeCache.getInstance());
		final Set<String> stRequiredSubEntityNames = GenericObjectUtils.getSubEntityNames(lstclctefweSelected, sMainEntityName, Modules.getInstance());
		final boolean bIncludeParentObjects = iModuleId != null && GenericObjectUtils.containsParentField(lstclctefweSelected, sParentEntityName);

		this.mpTypes = CollectionUtils.transformIntoMap(lstclctefweSelected, new Transformer<CollectableEntityField, String>() {
				@Override
				public String transform(CollectableEntityField i) {
					return PDFHelper.getFieldName(i);
				}
			},
			new Transformer<CollectableEntityField, Class<?>>() {
				@Override
				public Class<?> transform(CollectableEntityField i) {
					return i.getJavaClass();
				}
			});
		this.iterator = goFacade.getPrintableGenericObjectsWithDependants(iModuleId, clctexpr, new HashSet<Integer>(lstAttributeIds),
				stRequiredSubEntityNames, bIncludeParentObjects, bIncludeSubModules).iterator();
	}

	/**
	 * fetches next search result row
	 *
	 * @return fetch successful?
	 */
	@Override
	public boolean next() throws JRException {
		mpRows.clear();

		final boolean result = iterator.hasNext();
		if (result) {
			final GenericObjectWithDependantsVO lowdcvo = iterator.next();
			for (CollectableEntityField clctefwe : lstclctefweSelected) {
				final Object oData = getData(lowdcvo, clctefwe);
				final String sPdfFieldName = PDFHelper.getFieldName(clctefwe);
				mpRows.put(sPdfFieldName, oData);
			}
		}
		return result;
	}

	private Object getData(GenericObjectWithDependantsVO lowdcvo, CollectableEntityField clctefwe) {
		final String sFieldName = clctefwe.getName();
		final String sFieldEntityName = clctefwe.getEntityName();

		final Object result;
		if (sFieldEntityName.equals(this.sMainEntityName)) {
			// own attribute:
			final DynamicAttributeVO davo = lowdcvo.getAttribute(sFieldName, AttributeCache.getInstance());
			result = davo != null ? davo.getValue() : null;
		}
		else if (sFieldEntityName.equals(this.sParentEntityName)) {
			// parent attribute:
			final DynamicAttributeVO davo = lowdcvo.getParent().getAttribute(sFieldName, AttributeCache.getInstance());
			result = davo != null ? davo.getValue() : null;
		}
		else {
			// subform field:
			result = GenericObjectUtils.getConcatenatedValue(lowdcvo.getDependants().getData(sFieldEntityName), sFieldName);
		}
		return result;
	}

	/**
	 * get a specific attribute value for search result report
	 *
	 * @param jrfield jasper reports field
	 * @return specific attribute value
	 */
	@Override
	public Object getFieldValue(JRField jrfield) throws JRException {
		return CollectableFieldFormat.getInstance(mpTypes.get(jrfield.getName())).format(null, mpRows.get(jrfield.getName()));
	}

}	// class SearchResultDataSource
