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
import java.util.Locale;

import org.nuclos.client.ui.collect.component.CollectableDateChooser;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.access.CefSecurityAgent;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.SpringLocaleDelegate;

/**
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:stefan.geiling@nuclos.de">stefan.geiling</a>
 * @version 01.00.00
 */
public class CollectableJobRunDate extends CollectableDateChooser {
	public static final String DATE_FORMAT_STRING_EN = "MM/dd/yyyy HH:mm:ss";
	public static final String DATE_FORMAT_STRING_GER = "dd.MM.yyyy HH:mm:ss";
	
	public CollectableJobRunDate(final CollectableEntityField clctef, Boolean bSearchable) {
		super(new CollectableEntityField() {
			@Override
			public void setSecurityAgent(CefSecurityAgent sa) {
				clctef.getSecurityAgent();
			}			
			@Override
			public void setCollectableEntity(CollectableEntity clent) {
				clctef.setCollectableEntity(clent);
			}
			@Override
			public boolean isWritable() {
				return clctef.isWritable();
			}			
			@Override
			public boolean isRestrictedToValueList() {
				return clctef.isRestrictedToValueList();
			}			
			@Override
			public boolean isRemovable() {
				return clctef.isRemovable();
			}
			@Override
			public boolean isReferencing() {
				return clctef.isReferencing();
			}
			@Override
			public boolean isReferencedEntityDisplayable() {
				return clctef.isReferencedEntityDisplayable();
			}
			@Override
			public boolean isReadable() {
				return clctef.isReadable();
			}
			@Override
			public boolean isNullable() {
				return clctef.isNullable();
			}
			@Override
			public boolean isIdField() {
				return clctef.isIdField();
			}
			@Override
			public CefSecurityAgent getSecurityAgent() {
				return clctef.getSecurityAgent();
			}
			@Override
			public String getReferencedEntityName() {
				return clctef.getReferencedEntityName();
			}
			@Override
			public String getReferencedEntityFieldName() {
				return clctef.getReferencedEntityFieldName();
			}
			@Override
			public Integer getPrecision() {
				return clctef.getPrecision();
			}
			@Override
			public CollectableField getNullField() {
				return clctef.getNullField();
			}
			@Override
			public String getName() {
				return clctef.getName();
			}
			@Override
			public Integer getMaxLength() {
				return clctef.getMaxLength();
			}
			@Override
			public String getLabel() {
				return clctef.getLabel();
			}
			@Override
			public Class<?> getJavaClass() {
				return InternalTimestamp.class;
			}
			@Override
			public String getFormatOutput() {
				String formatPattern;
				DateFormat format = SpringLocaleDelegate.getInstance().getDateTimeFormat();
			      if (format instanceof SimpleDateFormat)
			    	  formatPattern = ((SimpleDateFormat)format).toPattern();
			      else {
			    	  if (SpringLocaleDelegate.getInstance().getLocale().equals(Locale.GERMAN))
						  formatPattern = DATE_FORMAT_STRING_GER;
			    	  else
						  formatPattern = DATE_FORMAT_STRING_EN;
			      }
				return formatPattern;
			}
			@Override
			public String getFormatInput() {
				return clctef.getFormatInput();
			}
			@Override
			public int getFieldType() {
				return clctef.getFieldType();
			}
			@Override
			public String getEntityName() {
				return clctef.getEntityName();
			}
			@Override
			public String getDescription() {
				return clctef.getDescription();
			}			
			@Override
			public String getDefaultComponentType() {
				return clctef.getDefaultComponentType();
			}
			@Override
			public int getDefaultCollectableComponentType() {
				return clctef.getDefaultCollectableComponentType();
			}
			@Override
			public CollectableField getDefault() {
				return clctef.getDefault();
			}
			@Override
			public CollectableEntity getCollectableEntity() {
				return clctef.getCollectableEntity();
			}
		}, bSearchable.booleanValue());
	}
}
