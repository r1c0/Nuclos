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

package org.nuclos.client.layout.wysiwyg.editor.ui.panels;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.LabelProvider;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LocaleInfo;

public class TranslationPanel extends JPanel {

	private final List<LocaleInfo> localeList;
	private final Map<String, String> translations;
	private final String defaultText;

	private final JTable table;
	
	public TranslationPanel(Map<String, String> translations, String defaultText) {
		this.translations = new HashMap<String, String>(translations);
		this.defaultText = (defaultText != null) ? defaultText : translations.get(LocaleInfo.I_DEFAULT_TAG);
		
		Map<String, LocaleInfo> allLocales = CollectionUtils.transformIntoMap(
			LocaleDelegate.getInstance().getAllLocales(false),
			new Transformer<LocaleInfo, String>() {
				@Override public String transform(LocaleInfo li) { return li.getTag(); }
			});
		
		// Extend locale list with unknown locale from the provided translations
		localeList = new ArrayList<LocaleInfo>(allLocales.values());
		for (String tag : translations.keySet()) {
			if (!tag.equals(LocaleInfo.I_DEFAULT_TAG) && !allLocales.containsKey(tag)) {
				localeList.add(LocaleInfo.parseTag(tag));
			}
		}
		
		table = new JTable();
		table.setModel(new TranslationTableModel());
		table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableRenderer(new TranslationLabelProvider()));
		add(new JScrollPane(table));
	}
	
	
	/**
	 * Returns the current translations.
	 * Note that the map can contain null values even if the initial map doesn't.
	 */
	public Map<String, String> getTranslations() {
		return translations;
	}
	
	private class TranslationTableModel extends AbstractTableModel {
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return CommonLocaleDelegate.getMessage("wizard.step.entitytranslationstable.3", null);
			case 1:
				return CommonLocaleDelegate.getMessage("wizard.step.entitytranslationstable.4", null);
			}
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return localeList.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			LocaleInfo localeInfo = localeList.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return localeInfo.toString();
			case 1:
				return translations.get(localeInfo.getTag());
			}
			return null;
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}
		
		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			LocaleInfo localeInfo = localeList.get(rowIndex);
			if (columnIndex == 1) {
				String text = (String) value;
				if (text != null && text.trim().isEmpty()) {
					text = null;
				}
				translations.put(localeInfo.getTag(), text);
				fireTableDataChanged();
			}
		}
	}
	
	private class TranslationLabelProvider extends LabelProvider {
		
		@Override
		protected void configureState(CellContext context) {
			if (context.getValue() == null) {
				rendererComponent.setEnabled(false);
			}
			super.configureState(context);
		}
		
		@Override
		protected String getValueAsString(CellContext context) {
			if (context.getValue() == null) {
				String bestTranslation = CommonLocaleDelegate.selectBestTranslation(translations);
				return (bestTranslation != null) ? bestTranslation : defaultText;
			}
			return super.getValueAsString(context);
		}
	}
	
	public static Map<String, String> showDialog(Component parent, Map<String, String> translations, String defaultText) {
		TranslationPanel translationPanel = new TranslationPanel(translations, defaultText);
		int option =JOptionPane.showConfirmDialog(
			parent,
			translationPanel,
			CommonLocaleDelegate.getMessage("action.EditTranslationsForLocale", null),
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			return translationPanel.getTranslations();
		} else {
			return null;
		}
	}
}