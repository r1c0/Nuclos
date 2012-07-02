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
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Static utility class in the "common" hierarchy, which holds the selected
 * locale data for the user vm.
 *
 * 2011-06-08, tsc: As this class is located in the common part, it must not hold the locale or the
 * resources as static variables. These fields need to be located in the LookupService implementation that
 * can hold the user's locale (client) or perform a locale-specific lookup (server).
 *
 * @author marc.jackisch
 */
// @Component
public class SpringLocaleDelegate {

	private static final Logger LOG = Logger.getLogger(SpringLocaleDelegate.class);

	public static interface LookupService {

		public Locale getLocale();

		public LocaleInfo getLocaleInfo();

		public boolean isResource(String key);

		/**
		 * getResource always returns a value.
		 * If there is not resource for the given key, the resource is marked with <[key]>
		 * @param key the resource id
		 * @return resource
		 */
		public String getResource(String key);

		/**
		 * get resource from database.
		 * @param li the target locale info
		 * @param key the resource id
		 * @return
		 */
		public String getResourceById(LocaleInfo li, String key);

		public NumberFormat getNumberFormat();

		public DateFormat getDateFormat();

		public DateFormat getTimeFormat();

		public DateFormat getDateTimeFormat();

		/**
		 * Gets the complete (ordered) chain of candidate locales for the given locale.
		 */
		public List<LocaleInfo> getParentChain();

		public Date getLastChange();

	};

	private static SpringLocaleDelegate INSTANCE;

	//

	private LookupService keyLookup;

	SpringLocaleDelegate() {
		INSTANCE = this;
	}

	// @Autowired
	public void setLookupService(LookupService lookupService) {
		this.keyLookup = lookupService;
	}

	public static SpringLocaleDelegate getInstance() {
		return INSTANCE;
	}

	private List<LocaleInfo> getLocaleCandidates() {
		return getKeyLookup().getParentChain();
	}

	public Locale getLocale() {
		return getKeyLookup().getLocale();
	}

	private LookupService getKeyLookup() {
		return keyLookup;
	}

	public LocaleInfo getUserLocaleInfo()  {
		return getKeyLookup().getLocaleInfo();
	}

	public NumberFormat getIntegerFormat() {
		NumberFormat nf = NumberFormat.getIntegerInstance(getLocale());
		nf.setGroupingUsed(false);
		return nf;
	}

	public NumberFormat getNumberFormat() {
		return getKeyLookup().getNumberFormat();
	}

	public DateFormat getDateFormat() {
		return getKeyLookup().getDateFormat();
	}

	public DateFormat getTimeFormat() {
		return getKeyLookup().getTimeFormat();
	}

	public DateFormat getDateTimeFormat() {
		return getKeyLookup().getDateTimeFormat();
	}

	//==========================================================================


	public String getResourceById(LocaleInfo li, String id) {
		return getKeyLookup().getResourceById(li, id);
	}

	/**
	 * GetMessage returns a fully formatted message with parameters extended and
	 * eventual recursive resolution of ressource strings.
	 * This method should only be used for message texts, i.e. texts which are
	 * written with {@link java.text.MessageFormat)} in mind.
	 *
	 * To get custom texts for fields (like labels, descriptions, menu paths),
	 * which are not properly escaped for {@link java.text.MessageFormat)},
	 * use {@link #getText(String, String)} instead.
	 *
	 * @param rid     the resource id
	 * @param otext   original resource text, only used for informational purposes
	 *                in the call and by the build-process examining program
	 * @param params  parameters for the message format
	 * @return the formatted string
	 * @exception java.lang.RuntimeException if a reference recursion occurs, or
	 *            a reference does not exist
	 * @see java.text.MessageFormat
	 */
	public String getMessage(String rid, String otext, Object ... params) {
		if (rid != null) {
			return getMessageInternal(rid, new ResourceBundleResolverUtils.RecursiveResolver(keyLookup, rid), otext, params);
		}
		return otext != null ? "[" + otext + "]" : "[Missing translation for locale " + getKeyLookup().getLocaleInfo().getTag() + "]";
	}

	/**
	 * Gets the plain-vanilla text with the given resource id.
	 * @param rid     the resource id
	 * @param otext   original resource text (optional, just for debugging purposes)
	 */
	public String getText(String rid) {
		return getKeyLookup().getResource(rid);
	}
	/**
	 * Gets the plain-vanilla text with the given resource id.
	 * Deprecated. Use <code>getText(String rid)</code>.
	 * @param rid     the resource id
	 * @param otext   original resource text (optional, just for debugging purposes)
	 */
	@Deprecated
	public String getText(String rid, String otext) {
		return getText(rid);
	}

	public String getTextFallback(String rid, String fallback) {
		try {
			if (rid != null && getKeyLookup().isResource(rid)) {
				return getText(rid);
			}
		} catch (MissingResourceException e) {
		}
		return fallback;
	}

	public String getText(Localizable localizable) {
		String resId = localizable.getResourceId();
		return getText(resId);
	}

	private String getMessageInternal(String rid, Transformer<String, String> resolver, String otext, Object ... params) {
		try {
			return ResourceBundleResolverUtils.getMessageInternal(keyLookup, resolver, rid, params);
		}
		catch(RuntimeException e) {
			LOG.info("getMessageInternal failed: rid=" + rid + " params=" + (params == null ? "null" : Arrays.asList(params)) + " otext=" + otext + ": " + e);
			return getTextFallback(rid, otext);
		}
	}

	/**
	 * Shortcut to
	 * @param date
	 * @return
	 */
	public String formatDate(Date date) {
		return getKeyLookup().getDateFormat().format(date);
	}

	public String formatDate(Object date) {
		return getKeyLookup().getDateFormat().format(date);
	}

	public Date parseDate(String text) throws ParseException {
		DateFormat df = getKeyLookup().getDateFormat();
		if(df instanceof SimpleDateFormat) {
			df = new SimpleDateFormat(((SimpleDateFormat) df).toPattern().replace("yyyy", "yy"));
			((SimpleDateFormat)df).set2DigitYearStart(new SimpleDateFormat("dd.MM.yyyy").parse("01.01.1980"));
		}
		if (RelativeDate.today().toString().equals(text)) {
			return DateUtils.today();
		}
		return df.parse(text);
	}

	public String formatTime(Date time) {
		return getKeyLookup().getTimeFormat().format(time);
	}

	public String formatTime(Object time) {
		return getKeyLookup().getTimeFormat().format(time);
	}

	public String formatDateTime(Date dt) {
		return getKeyLookup().getDateTimeFormat().format(dt);
	}

	public String formatDateTime(Object dt) {
		return getKeyLookup().getDateTimeFormat().format(dt);
	}

	public String getLabelFromAttributeCVO(AttributeCVO a) {
		if(a.getResourceSIdForLabel() != null && a.getResourceSIdForLabel().length() > 0) {
			return getTextFallback(a.getResourceSIdForLabel(), a.getLabel());
		}
		return a.getLabel();
	}

	public String getDescriptionFromAttributeCVO(AttributeCVO a) {
		if(a.getResourceSIdForDescription() != null && a.getResourceSIdForDescription().length() > 0)
			return getText(a.getResourceSIdForDescription(), a.getDescription());
		return a.getDescription();
	}

	public String getTextForStaticLabel(String resourceId) {
		try {
			if (resourceId != null) {
				return getText(resourceId, null);
			}
		}
		catch (MissingResourceException ex) {
		}
		return "[Missing resource id=" + resourceId + ", locale " + getKeyLookup().getLocaleInfo().getTag() + "]";
	}

	public String getLabelFromMetaDataVO(MasterDataMetaVO metavo) {
		String result = null;
		if(metavo.getResourceSIdForLabel() != null && keyLookup != null)
			result = getText(metavo.getResourceSIdForLabel(), metavo.getLabel());

		if (result == null)
			result = metavo.getEntityName();
		return result;
	}

	public String getLabelFromMetaDataVO(EntityMetaDataVO entitymetavo) {
		String result = null;
		if(entitymetavo.getLocaleResourceIdForLabel() != null && keyLookup != null)
			result = getText(entitymetavo.getLocaleResourceIdForLabel(), null);

		if (result == null)
			result = entitymetavo.getEntity();
		return result;
	}

	public String getLabelFromMetaFieldDataVO(EntityFieldMetaDataVO fieldmetavo) {
		String result = null;
		if(fieldmetavo.getLocaleResourceIdForLabel() != null && keyLookup != null)
			result = getText(fieldmetavo.getLocaleResourceIdForLabel(), null);

		if (result == null)
			result = fieldmetavo.getField();
		return result;
	}

	public String getTreeViewFromMetaDataVO(EntityMetaDataVO entitymetavo) {
		String result = null;
		if(entitymetavo.getLocaleResourceIdForTreeView() != null && keyLookup != null)
			result = getResourceById(getUserLocaleInfo(), entitymetavo.getLocaleResourceIdForTreeView());

		return result;
	}

	public String getTreeViewLabel(MasterDataVO mdvo, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		return getTreeViewLabel(mdvo.getFields(), entityname, metaDataProvider);
	}

	public String getTreeViewLabel(GenericObjectVO govo, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		Map<String, Object> values = new HashMap<String, Object>();
		for (DynamicAttributeVO att : govo.getAttributes()) {
			values.put(metaDataProvider.getEntityField(entityname, att.getAttributeId().longValue()).getField(), att.getValue());
		}
		return getTreeViewLabel(values, entityname, metaDataProvider);
	}

	public String getTreeViewLabel(EntityObjectVO eovo, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		return getTreeViewLabel(eovo.getFields(), entityname, metaDataProvider);
	}

	public String getTreeViewLabel(Collectable clct, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		Map<String, Object> values = new HashMap<String, Object>();
		for (EntityFieldMetaDataVO field : metaDataProvider.getAllEntityFieldsByEntity(entityname).values()) {
			CollectableField value = clct.getField(field.getField());
			if (value != null && value.getValue() != null) {
				values.put(field.getField(), value.getValue());
			}
			else {
				values.put(field.getField(), null);
			}
		}
		return getTreeViewLabel(values, entityname, metaDataProvider);
	}

	public String getTreeViewLabel(Map<String, Object> values, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		String result = getTreeViewFromMetaDataVO(metaDataProvider.getEntity(entityname));
		if (result != null) {
			return replace(result, values, entityname, metaDataProvider);
		}
		else {
			if (values.containsKey(NuclosEOField.SYSTEMIDENTIFIER.getName())) {
				return (String)values.get(NuclosEOField.SYSTEMIDENTIFIER.getName());
			}
			else if (values.containsKey("name") && values.get("name") != null) {
				return values.get("name").toString();
			}
			else {
				return "<unknown>";
			}
		}
	}

	public String getTreeViewDescriptionFromMetaDataVO(EntityMetaDataVO metavo) {
		String result = null;
		if(metavo.getLocaleResourceIdForTreeViewDescription() != null && keyLookup != null)
			result = getResourceById(getUserLocaleInfo(), metavo.getLocaleResourceIdForTreeViewDescription());

		return result;
	}

	public String getTreeViewDescription(MasterDataVO mdvo, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		return getTreeViewDescription(mdvo.getFields(), entityname, metaDataProvider);
	}

	public String getTreeViewDescription(GenericObjectVO govo, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		Map<String, Object> values = new HashMap<String, Object>();
		for (DynamicAttributeVO att : govo.getAttributes()) {
			values.put(metaDataProvider.getEntityField(entityname, att.getAttributeId().longValue()).getField(), att.getValue());
		}
		return getTreeViewDescription(values, entityname, metaDataProvider);
	}

	public String getTreeViewDescription(EntityObjectVO eovo, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		return getTreeViewDescription(eovo.getFields(), entityname, metaDataProvider);
	}

	public String getTreeViewDescription(Collectable clct, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		Map<String, Object> values = new HashMap<String, Object>();
		for (EntityFieldMetaDataVO field : metaDataProvider.getAllEntityFieldsByEntity(entityname).values()) {
			CollectableField value = clct.getField(field.getField());
			if (value != null && value.getValue() != null) {
				values.put(field.getField(), value.getValue());
			}
			else {
				values.put(field.getField(), null);
			}
		}
		return getTreeViewLabel(values, entityname, metaDataProvider);
	}

	public String getTreeViewDescription(Map<String, Object> values, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		String result = getTreeViewDescriptionFromMetaDataVO(metaDataProvider.getEntity(entityname));
		if (result != null) {
			return replace(result, values, entityname, metaDataProvider);
		}
		else {
			if (values.containsKey(NuclosEOField.CHANGEDAT.getName()) && values.get(NuclosEOField.CHANGEDAT.getName()) != null) {
				Date date = (Date)values.get(NuclosEOField.CHANGEDAT.getName());
				return MessageFormat.format(getResourceById(getUserLocaleInfo(), "gotreenode.tooltip"), getDateTimeFormat().format(date));
			}
			else if (values.containsKey(NuclosEOField.SYSTEMIDENTIFIER.getName())) {
				return (String)values.get(NuclosEOField.SYSTEMIDENTIFIER.getName());
			}
			else if (values.containsKey("name") && values.get("name") != null) {
				return values.get("name").toString();
			}
			else {
				return "<unknown>";
			}
		}
	}

	private String replace(String input, Map<String, Object> values, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		int sidx = 0;
		while ((sidx = input.indexOf("${", sidx)) >= 0) {
			int eidx = input.indexOf("}", sidx);
			String key = input.substring(sidx + 2, eidx);
			String flags = null;
			int ci = key.indexOf(':');
			if(ci >= 0) {
				flags = key.substring(ci + 1);
				key = key.substring(0, ci);
			}
			String rep = findReplacement(key, flags, values, entityname, metaDataProvider);
			input = input.substring(0, sidx) + rep + input.substring(eidx + 1);
			sidx = sidx + rep.length();
		}
		return input;
	}

	private String findReplacement(String sKey, String sFlag, Map<String, Object> values, String entityname, MetaDataProvider<? extends EntityMetaDataVO, ? extends EntityFieldMetaDataVO> metaDataProvider) {
		String sResIfNull = "";
		if(sFlag != null) {
			for(StringTokenizer st = new StringTokenizer(sFlag, ":"); st.hasMoreElements(); ) {
				String flag = st.nextToken();
				if(flag.startsWith("ifnull=")) {
					sResIfNull = flag.substring(7);
				}
			}
		}

		if (values.containsKey(sKey) && values.get(sKey) != null) {
			Object value = values.get(sKey);
			EntityFieldMetaDataVO fieldmeta = metaDataProvider.getEntityField(entityname, sKey);
			try {
				CollectableFieldFormat formatter = CollectableFieldFormat.getInstance(Class.forName(fieldmeta.getDataType()));
				return formatter.format(fieldmeta.getFormatOutput(), value);
			}
			catch (Exception ex) {
				return sResIfNull;
			}
		}
		else {
			return sResIfNull;
		}
	}

	public String getResource(String resId, String sText) {
		if (resId != null && keyLookup != null) {
			return getMessage(resId, sText);
		}
		return sText;
	}

	/**
	 * Given a map from locale tags to translations, this method returns
	 * the best match; or null.
	 */
	public String selectBestTranslation(Map<String, String> map) {
		for (LocaleInfo li : getLocaleCandidates()) {
			String tag = li.getTag();
			String text = map.get(tag);
			if (text != null) {
				return text;
			}
		}
		return null;
	}

	public boolean isResourceId(String id) {
		return keyLookup.isResource(id);
	}

	public String getMessageFromResource(String text) {
		String resText = null;

		try {
			if (text != null && text.indexOf("{") > -1) {
				resText = getText(text.substring(0, text.indexOf("{")).trim());
			}
			else {
				resText = getText(text);
			}
		}
		catch (RuntimeException e) {
			// text seems to be no resourceId
		}

		if (resText != null && text != null) {
			Pattern REF_PATTERN = Pattern.compile("\\{([^\\}]*?)\\}");
			List<String> paramList = new ArrayList<String>();
			StringBuffer rb = new StringBuffer();

			Matcher m = REF_PATTERN.matcher(text);
			while(m.find()) {
				String param = m.group(1);

				try {
					if (param.length() > 0 && isResourceId(param.trim()))
						param = getText(param.trim(),null);
				}
				catch (RuntimeException e) {
					// param seems to be no resourceId
				}
				paramList.add(param);
			}

			m = REF_PATTERN.matcher(resText);
			try {
			while(m.find())
				m.appendReplacement(rb, paramList.get(Integer.valueOf(m.group(1))).replace("$", "\\$"));
			} catch (IndexOutOfBoundsException ex) {
				// wrong number of params...
			}

			m.appendTail(rb);

			resText = rb.toString();
		}
		else {
			return text;
		}

		return resText;
	}
}
