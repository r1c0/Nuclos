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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.log4j.Logger;
import org.nuclos.common.PDFHelper;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.entityobject.CollectableEOEntityField;
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
	
	private static final Logger LOG = Logger.getLogger(SearchResultDataSource.class); 

	private final Map<String, Class<?>> mpTypes;
	private final Map<String, String> mpOutputFormats;
	private final Map<String, Object> mpRows = new HashMap<String, Object>();
	private final Iterator<GenericObjectWithDependantsVO> iterator;
	private final String sMainEntityName;
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

		final List<Integer> lstAttributeIds = GenericObjectUtils.getAttributeIds(lstclctefweSelected, sMainEntityName, AttributeCache.getInstance());
		final Set<String> stRequiredSubEntityNames = Collections.emptySet();

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
		this.mpOutputFormats = CollectionUtils.transformIntoMap(lstclctefweSelected, new Transformer<CollectableEntityField, String>() {
			@Override
			public String transform(CollectableEntityField i) {
				return PDFHelper.getFieldName(i);
			}
		},
		new Transformer<CollectableEntityField, String>() {
			@Override
			public String transform(CollectableEntityField i) {
				return i.getFormatOutput();
			}
		});
		this.iterator = goFacade.getPrintableGenericObjectsWithDependants(iModuleId, clctexpr, new HashSet<Integer>(lstAttributeIds),
				stRequiredSubEntityNames, false, bIncludeSubModules).iterator();
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

	/**
	 * TODO:
	 * This looks a lot like {@link org.nuclos.client.ui.collect.model.GenericObjectsResultTableModel#getValueAt(int, int)}.
	 * Shouldn't be there only *one* implementation? (tp)
	 */
	private Object getData(GenericObjectWithDependantsVO lowdcvo, CollectableEntityField clctefwe) {
		final String sFieldName = clctefwe.getName();
		final String sFieldEntityName = clctefwe.getEntityName();

		final Object result;
		if (sFieldEntityName.equals(this.sMainEntityName)) {
			// own attribute:
			final DynamicAttributeVO davo = lowdcvo.getAttribute(sFieldName, AttributeCache.getInstance());
			result = davo != null ? davo.getValue() : null;
		}
		else {
			final PivotInfo pinfo;
			if (clctefwe instanceof CollectableEOEntityField) {
				final CollectableEOEntityField f = (CollectableEOEntityField) clctefwe;
				pinfo = f.getMeta().getPivotInfo();
			}
			else {
				pinfo = null;
			}
			// pivot field:
			if (pinfo != null) {
				final List<Object> values = new ArrayList<Object>(1);
				final Collection<EntityObjectVO> items = lowdcvo.getDependants().getData(sFieldEntityName);

				for (EntityObjectVO k: items) {
					if (sFieldName.equals(k.getRealField(pinfo.getKeyField(), String.class))) {
						values.add(k.getRealField(pinfo.getValueField(), pinfo.getValueType()));
					}
				}
				if (values.isEmpty()) {
					result = null;
				}
				else {
					assert values.size() == 1 : "Expected 1 value, got " + values;
					result = values.get(0);
				}
			}
			// subform field:
			else {
				// result = GenericObjectUtils.getConcatenatedValue(lowdcvo.getDependants().getData(sFieldEntityName), sFieldName);
				
				final Collection<EntityObjectVO> collmdvo = lowdcvo.getDependants().getData(sFieldEntityName);
				final List<Object> values = CollectionUtils.transform(collmdvo, new Transformer<EntityObjectVO, Object>() {
					@Override
					public Object transform(EntityObjectVO i) {
						return i.getRealField(sFieldName);
					}
				});
				return values;
			}
		}
		return result;
	}

	/**
	 * get a specific attribute value for search result report
	 *
	 * @param jrfield jasper reports field
	 * @return formated string representation of attribute
	 */
	@Override
	public Object getFieldValue(JRField jrfield) throws JRException {
		final String name = jrfield.getName();
		final Class<?> type = mpTypes.get(name);
		final String outputFormat = mpOutputFormats.get(name);
		final Object row = mpRows.get(name);
		if (LOG.isDebugEnabled()) {
			LOG.debug("getFieldValue: format field " + name + " value '" + row + "' type" + type + " with '" + outputFormat + "'");
		}
		final StringBuilder result = new StringBuilder();
		if (row instanceof List) {
			for (Iterator<?> it = ((List<?>) row).iterator(); it.hasNext();) {
				final Object v = it.next();
				result.append(CollectableFieldFormat.getInstance(type).format(outputFormat, v));
				if (it.hasNext()) {
					result.append(", ");
				}
			}
		}
		else {
			return CollectableFieldFormat.getInstance(type).format(outputFormat, row);
		}
		return result.toString();
	}

}	// class SearchResultDataSource
