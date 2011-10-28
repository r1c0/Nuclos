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
package org.nuclos.client.genericobject.logbook;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.ui.model.AbstractListTableModel;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.genericobject.valueobject.LogbookVO;

/**
 * class LogbookTableModel
 */
class LogbookTableModel extends AbstractListTableModel<LogbookVO> {

	static final int COLUMN_CHANGEDAT = 0;
	static final int COLUMN_CHANGEDBY = 1;
	static final int COLUMN_LABEL = 2;
	static final int COLUMN_OLDVALUE = 3;
	static final int COLUMN_NEWVALUE = 4;
	static final int COLUMN_ID = 5;

	// NOTE: "Feld" is used (hard-coded) in LogbookPanel to access the column
	private static final String[] asColumnNames =
			{CommonLocaleDelegate.getMessage("LogbookController.5", "Ge\u00e4ndert am"), 
			CommonLocaleDelegate.getMessage("LogbookController.7", "Ge\u00e4ndert von"), 
			CommonLocaleDelegate.getMessage("LogbookController.8", "Feld"), 
			CommonLocaleDelegate.getMessage("LogbookController.9", "Alter Wert"), 
			CommonLocaleDelegate.getMessage("LogbookController.10", "Neuer Wert"), 
			CommonLocaleDelegate.getMessage("LogbookController.11", "ID")};

	private final DateFormat dateformat = CommonLocaleDelegate.getDateTimeFormat(); // new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	LogbookTableModel(Collection<LogbookVO> colllogbookvo, AttributeCVO attrcvoHeader) {
		super(new ArrayList<LogbookVO>(colllogbookvo));
		if (attrcvoHeader != null) {
			// Pre-sort logbook to ensure the interesting attribute comes first.
			// Since (further) sorting is stable, this relative attribute order will remain.
			Collections.sort(this.getRows(), new AttributeHeaderComparator(attrcvoHeader.getId()));
		}
	}
	
	@Override
	public String getColumnName(int iColumn) {
		return asColumnNames[iColumn];
	}

	@Override
	public int getColumnCount() {
		return asColumnNames.length;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case COLUMN_CHANGEDAT:
			return Date.class;
		case COLUMN_CHANGEDBY:
		case COLUMN_LABEL:
		case COLUMN_OLDVALUE:
		case COLUMN_NEWVALUE:
			return String.class;
		case COLUMN_ID:
			return Integer.class;
		default:
			return Object.class;
		}
	}

	@Override
	public Object getValueAt(int iRow, int iColumn) {
		final Object result;
		final LogbookVO logbookvo = getRow(iRow);
		final String sAction = logbookvo.getMasterDataAction();
		switch (iColumn) {
			case COLUMN_CHANGEDAT:
				result = new Date(logbookvo.getCreatedAt().getTime()) {

					@Override public String toString() {
						return dateformat.format(logbookvo.getCreatedAt());
					}
				};
				break;
			case COLUMN_CHANGEDBY:
				result = logbookvo.getCreatedBy();
				break;
			case COLUMN_LABEL:
				if(logbookvo.isMigrated())
					result = CommonLocaleDelegate.getMessage("LogbookTableModel.1","(migriert)");
				else if(logbookvo.getAttribute() != null)
					result = CommonLocaleDelegate.getLabelFromAttributeCVO(AttributeCache.getInstance().getAttribute(logbookvo.getAttribute()));
				else
					result = logbookvo.getLabel(); // masterdata only
				break;
			case COLUMN_ID:
				result = logbookvo.getMasterDataRecordId();
				break;
			case COLUMN_OLDVALUE:
				if ("C".equals(sAction)) {
					result = "(Neu erzeugt)";
				}
				else {
					result = logbookvo.getOldValue();
				}
				break;
			case COLUMN_NEWVALUE:
				if ("D".equals(sAction)) {
					result = "(Gel\u00f6scht)";
				}
				else {
					result = logbookvo.getNewValue();
				}
				break;
			default:
				throw new IllegalArgumentException("iColumn");
		}
		return result;
	}

	public static int getPreferredColumnWidth(int iColumn) {
		final int result;
		switch (iColumn) {
			case COLUMN_CHANGEDAT:
				result = 150;
				break;
			case COLUMN_CHANGEDBY:
				result = 75;
				break;
			case COLUMN_LABEL:
				result = 200;
				break;
			case COLUMN_ID:
				result = 75;
				break;
			case COLUMN_OLDVALUE:
			case COLUMN_NEWVALUE:
				result = 300;
				break;
			default:
				result = 100;
		}
		return result;
	}
	
	private static class AttributeHeaderComparator implements Comparator<LogbookVO> {
		
		private final Integer attrHeaderId;
			
		public AttributeHeaderComparator(Integer attrHeaderId) {
			this.attrHeaderId = attrHeaderId;
		}
	
		@Override
		public int compare(LogbookVO logbook1, LogbookVO logbook2) {
			// header attribute (version number) goes first...
			final Integer attr1 = safeAttributeId(logbook1);
			final Integer attr2 = safeAttributeId(logbook2);
			if (attr1.equals(attrHeaderId)) {
				return -1;
			} else if (attr2.equals(attrHeaderId)) {
				return 1;
			} else {
				return attr1.compareTo(attr2);
			}
		}
		
		private static Integer safeAttributeId(LogbookVO logbook) {
			Integer id = logbook.getAttribute();
			return (id != null) ? id : Integer.valueOf(Integer.MIN_VALUE);
		}
	}


}	//  class LogbookTableModel
