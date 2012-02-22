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
/*
 * Created on 26.05.2009
 */
package org.nuclos.common2;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * @deprecated Use {@link SpringLocaleDelegate}.
 */
public class CommonLocaleDelegate {

	private static final Logger LOG = Logger.getLogger(CommonLocaleDelegate.class);

	private CommonLocaleDelegate() {
		// Never invoked
	}
	
	public static Locale getLocale() {
		return SpringLocaleDelegate.getInstance().getLocale();
	}

	public static LocaleInfo getUserLocaleInfo()  {
		return SpringLocaleDelegate.getInstance().getUserLocaleInfo();
	}

	public static NumberFormat getIntegerFormat() {
		return SpringLocaleDelegate.getInstance().getIntegerFormat();
	}

	public static NumberFormat getNumberFormat() {
		return SpringLocaleDelegate.getInstance().getNumberFormat();
	}

	public static DateFormat getDateFormat() {
		return SpringLocaleDelegate.getInstance().getDateFormat();
	}

	public static DateFormat getTimeFormat() {
		return SpringLocaleDelegate.getInstance().getTimeFormat();
	}

	public static DateFormat getDateTimeFormat() {
		return SpringLocaleDelegate.getInstance().getDateTimeFormat();
	}

	public static String getResourceById(LocaleInfo li, String id) {
		return SpringLocaleDelegate.getInstance().getResourceById(li, id);
	}

	public static String getMessage(String rid, String otext, Object ... params) {
		return SpringLocaleDelegate.getInstance().getMessage(rid, otext, params);
	}

	public static String getText(String rid) {
		return SpringLocaleDelegate.getInstance().getText(rid);
	}

	@Deprecated
	public static String getText(String rid, String otext) {
		return SpringLocaleDelegate.getInstance().getText(rid, otext);
	}

	public static String getTextFallback(String rid, String fallback) {
		return SpringLocaleDelegate.getInstance().getTextFallback(rid, fallback);
	}

	public static String getText(Localizable localizable) {
		return SpringLocaleDelegate.getInstance().getText(localizable);
	}

	public static String formatDate(Date date) {
		return SpringLocaleDelegate.getInstance().formatDate(date);
	}

	public static String formatDate(Object date) {
		return SpringLocaleDelegate.getInstance().formatDate(date);
	}

	public static Date parseDate(String text) throws ParseException {
		return SpringLocaleDelegate.getInstance().parseDate(text);
	}

	public static String formatTime(Date time) {
		return SpringLocaleDelegate.getInstance().formatTime(time);
	}

	public static String formatTime(Object time) {
		return SpringLocaleDelegate.getInstance().formatTime(time);
	}

	public static String formatDateTime(Date dt) {
		return SpringLocaleDelegate.getInstance().formatDateTime(dt);
	}

	public static String formatDateTime(Object dt) {
		return SpringLocaleDelegate.getInstance().formatDateTime(dt);
	}

	public static String getLabelFromAttributeCVO(AttributeCVO a) {
		return SpringLocaleDelegate.getInstance().getLabelFromAttributeCVO(a);
	}

	public static String getDescriptionFromAttributeCVO(AttributeCVO a) {
		return SpringLocaleDelegate.getInstance().getDescriptionFromAttributeCVO(a);
	}

	public static String getTextForStaticLabel(String resourceId) {
		return SpringLocaleDelegate.getInstance().getTextForStaticLabel(resourceId);
	}

	public static String getLabelFromMetaDataVO(MasterDataMetaVO metavo) {
		return SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(metavo);
	}

	public static String getLabelFromMetaDataVO(EntityMetaDataVO entitymetavo) {
		return SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(entitymetavo);
	}

	public static String getLabelFromMetaFieldDataVO(EntityFieldMetaDataVO fieldmetavo) {
		return SpringLocaleDelegate.getInstance().getLabelFromMetaFieldDataVO(fieldmetavo);
	}

	public static String getTreeViewFromMetaDataVO(EntityMetaDataVO entitymetavo) {
		return SpringLocaleDelegate.getInstance().getTreeViewFromMetaDataVO(entitymetavo);
	}

	public static String getTreeViewLabel(MasterDataVO mdvo, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewLabel(mdvo, entityname, metaDataProvider);
	}

	public static String getTreeViewLabel(GenericObjectVO govo, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewLabel(govo, entityname, metaDataProvider);
	}

	public static String getTreeViewLabel(EntityObjectVO eovo, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewLabel(eovo, entityname, metaDataProvider);
	}

	public static String getTreeViewLabel(Collectable clct, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewLabel(clct, entityname, metaDataProvider);
	}

	public static String getTreeViewLabel(Map<String, Object> values, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewLabel(values, entityname, metaDataProvider);
	}

	public static String getTreeViewDescriptionFromMetaDataVO(EntityMetaDataVO metavo) {
		return SpringLocaleDelegate.getInstance().getTreeViewDescriptionFromMetaDataVO(metavo);
	}

	public static String getTreeViewDescription(MasterDataVO mdvo, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewDescription(mdvo, entityname, metaDataProvider);
	}

	public static String getTreeViewDescription(GenericObjectVO govo, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewDescription(govo, entityname, metaDataProvider);
	}

	public static String getTreeViewDescription(EntityObjectVO eovo, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewDescription(eovo, entityname, metaDataProvider);
	}

	public static String getTreeViewDescription(Collectable clct, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewDescription(clct, entityname, metaDataProvider);
	}

	public static String getTreeViewDescription(Map<String, Object> values, String entityname, MetaDataProvider metaDataProvider) {
		return SpringLocaleDelegate.getInstance().getTreeViewDescription(values, entityname, metaDataProvider);
	}

	public static String getResource(String resId, String sText) {
		return SpringLocaleDelegate.getInstance().getResource(resId, sText);
	}

	public static String selectBestTranslation(Map<String, String> map) {
		return SpringLocaleDelegate.getInstance().selectBestTranslation(map);
	}

	public static boolean isResourceId(String id) {
		return SpringLocaleDelegate.getInstance().isResourceId(id);
	}

	public static String getMessageFromResource(String text) {
		return SpringLocaleDelegate.getInstance().getMessageFromResource(text);
	}
	
}
