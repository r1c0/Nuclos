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
package org.nuclos.client.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonValidationException;

/**
 * MasterDataSubFormController for jobrun.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:stefan.geiling@nuclos.de">stefan.geiling</a>
 * @version 01.00.00
 */
public class JobRunSubFormController extends MasterDataSubFormController {

   private final String formatPattern;
   
   public JobRunSubFormController(MainFrameTab tab,
      CollectableComponentModelProvider clctcompmodelproviderParent,
      String sParentEntityName, SubForm subform,
      Preferences prefsUserParent, EntityPreferences entityPrefs, CollectableFieldsProviderCache valueListProviderCache) {
      super(tab, clctcompmodelproviderParent,
         sParentEntityName, subform, prefsUserParent, entityPrefs, valueListProviderCache);
      DateFormat format = SpringLocaleDelegate.getInstance().getDateTimeFormat();
      if (format instanceof SimpleDateFormat)
    	  formatPattern = ((SimpleDateFormat)format).toPattern();
      else {
    	  if (SpringLocaleDelegate.getInstance().getLocale().equals(Locale.GERMAN))
			  formatPattern = DATE_FORMAT_STRING_GER;
    	  else
			  formatPattern = DATE_FORMAT_STRING_EN;
      }
   }
   
   @Override
   protected List<CollectableEntityObject> newCollectableList(
			List<CollectableEntityObject> lstclct) {
	   for (CollectableEntityObject clct : lstclct) {
		   clct.setField("startdate", new CollectableValueField(
					getInternalTimestamp(clct.getField("startdate"))));
		   clct.setField("enddate", new CollectableValueField(
					getInternalTimestamp(clct.getField("enddate"))));
		}
		return super.newCollectableList(lstclct);
   }
   @Override
   protected List<CollectableEntityField> getTableColumns() {
	   List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
	   for (CollectableEntityField clctef : super.getTableColumns()) {
		   if (clctef.getName().equals("startdate") || clctef.getName().equals("enddate") ) {
			   EntityFieldMetaDataVO efMeta = ((CollectableEOEntityField)clctef).getMeta();
			   efMeta.setDataType(Date.class.getName());
			   efMeta.setFormatOutput(formatPattern);
			   result.add(new CollectableEOEntityField(efMeta, clctef.getEntityName()));
		   }
		   else
			   result.add(clctef);
	   }
	   return result;
   }
   
   @Override
   public List<CollectableEntityObject> getAllCollectables(
			Object oParentId,
			Collection<DetailsSubFormController<CollectableEntityObject>> collSubForms,
			boolean bSetParent, CollectableEntityObject clct)
			throws CommonValidationException {
	   	List<CollectableEntityObject> lstclct = super.getAllCollectables(oParentId, collSubForms, bSetParent, clct);
		for (CollectableEntityObject clcteo : lstclct) {
			clcteo.setField("startdate", new CollectableValueField(
						getInternalTimestampAsString(clcteo.getField("startdate"))));
			clcteo.setField("enddate", new CollectableValueField(
					   getInternalTimestampAsString(clcteo.getField("enddate"))));
		}
		return lstclct;
   }
   
   public static final String DATE_FORMAT_STRING_EN = "MM/dd/yyyy HH:mm:ss";
   public static final String DATE_FORMAT_STRING_GER = "dd.MM.yyyy HH:mm:ss";

   private Date getInternalTimestamp(CollectableField field) {
	   CollectableFieldFormat format = CollectableFieldFormat.getInstance(Date.class);
	   try {
		   return (Date)format.parse(DATE_FORMAT_STRING_EN, (String)field.getValue());
	   } catch (Exception e) {
		   try {
			   return (Date)format.parse(DATE_FORMAT_STRING_GER, (String)field.getValue());
		   } catch (Exception e1) {
			   return null; //@todo other formats are not supported at the moment.
		   }
	   }
   }
   private String getInternalTimestampAsString(CollectableField field) {
	   CollectableFieldFormat format = CollectableFieldFormat.getInstance(Date.class);
	   try {
		   return format.format(DATE_FORMAT_STRING_EN, (Date)field.getValue());
	   } catch (Exception e) {
		   try {
			   return format.format(DATE_FORMAT_STRING_GER, (Date)field.getValue());
		   } catch (Exception e1) {
			   return null; //@todo other formats are not supported at the moment.
		   }
	   }
   }
}
