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

package org.nuclos.tools.l10n;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.nuclos.client.ui.model.AbstractListTableModel;
import org.nuclos.common2.StringUtils;

public class L10nTool extends JXFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private File file;
	private L10nTableModel tableModel;
	private JXTable	table;
	
	public L10nTool(File f) throws IOException {
		super("L10nTool", true);
		
		this.file = f;
		this.tableModel = new L10nTableModel(parse(new InputStreamReader(new FileInputStream(f), "utf-8")));

		table = new JXTable(tableModel);
		table.setColumnSelectionAllowed(true);

		JToolBar toolBar = new JToolBar();
		toolBar.add(saveAction);
		toolBar.addSeparator();
		toolBar.add(addRowAction);
		toolBar.add(deleteRowAction);
		getContentPane().add(new JScrollPane(table));
		setToolBar(toolBar);
		pack();
	}

	Action saveAction = new AbstractAction("Save") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			List<Object> array = new ArrayList<Object>();
			for (L10n l10n : tableModel) {
				if (StringUtils.looksEmpty(l10n.resourceId))
					continue;
				for (Map.Entry<Language, String> e : l10n.translations.entrySet()) {
					if (StringUtils.looksEmpty(e.getValue()))
						continue;
					Map<String, String> map = new LinkedHashMap<String, String>();
					map.put(RESOURCEID_KEY, l10n.resourceId);
					map.put(LOCALE_KEY, e.getKey().tag);
					map.put(TEXT_KEY, e.getValue());
					array.add(map);
				}
			}
			try {
				Writer writer = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
				new PrettyPrinter(writer).prettyPrint(array);
				writer.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	};
	
	Action addRowAction = new AbstractAction("Add") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			List<L10n> list = new ArrayList<L10n>();
			for (int i : table.getSelectedRows()) {
				list.add(tableModel.getRow(table.convertRowIndexToModel(i)).clone());
			}
			if (!list.isEmpty()) {
				tableModel.addAll(table.convertRowIndexToModel(table.getSelectedRow()), list);
			}
		}
	};
	
	Action deleteRowAction = new AbstractAction("Remove") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			List<Integer> rows = new ArrayList<Integer>();
			for (int row : table.getSelectedRows()) {
				rows.add(table.convertRowIndexToModel(row));
			}
			Collections.sort(rows, Collections.reverseOrder());
			for (int row : rows) {
				tableModel.remove(row);
			}
		}
	};	
	private static final String	LOCALE_KEY = "locale";
	private static final String	TEXT_KEY = "text";
	private static final String	RESOURCEID_KEY = "resourceID";

	private static enum Language {
		I_DEFAULT("i-default"),
		EN("en"),
		DE("de");
		
		public final String tag;

		private Language(String tag) {
			this.tag = tag;
		}
		
		@Override
		public String toString() {
			return tag;
		}
		
		public static Language find(String tag) {
			for (Language l : Language.values()) {
				if (tag.equals(l.tag))
					return l;
			}
			throw new IllegalArgumentException("Unknown tag " + tag);
		}
	}
	
	private static class L10n implements Cloneable {
		String resourceId;
		EnumMap<Language, String> translations = new EnumMap<Language, String>(Language.class);
		
		@Override
		public L10n clone() {
			L10n clone = new L10n();
			clone.resourceId = resourceId;
			clone.translations.putAll(translations);
			return clone;
		}
	}
	
	private static List<L10n> parse(Reader reader) {
		Map<String, L10n> map = new LinkedHashMap<String, L10n>();
		for (Object item : (JSONArray) JSONValue.parse(reader)) {
			JSONObject obj = (JSONObject) item;
			String resourceId = (String) obj.get(RESOURCEID_KEY);
			Language lang = Language.find((String) obj.get(LOCALE_KEY ));
			String text = (String) obj.get(TEXT_KEY);
			
			L10n l10n = map.get(resourceId);
			if (l10n == null) {
				l10n = new L10n();
				l10n.resourceId = resourceId;
				map.put(resourceId, l10n);
			}
			Object old = l10n.translations.get(lang);
			if (old != null) {
				System.err.println("Duplicate entry for " + resourceId + "/" + lang + " : [" + old + "] vs. [" + text + "]");
			} else {
				l10n.translations.put(lang, text);
			}
		}
		return new ArrayList<L10n>(map.values());
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.err.println("Run with path to localeresource.json as argument");
			System.exit(1);
		}
		File file = new File(args[0]).getAbsoluteFile();
		if (!file.isFile()) {
			System.err.println("File " + file + " does not exist");
			System.exit(1);
		}
		new L10nTool(file).setVisible(true);
	}
	
	static class L10nTableModel extends AbstractListTableModel<L10n> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public L10nTableModel() {
		}

		public L10nTableModel(List<L10n> list) {
			super(list);
		}

		@Override
		public int getRowCount() {
			return super.getRowCount() + 1;
		}

		@Override
		public int getColumnCount() {
			return 1 + Language.values().length;
		}
		
		@Override
		public String getColumnName(int column) {
			Language lang = columnLang(column);
			if (lang == null) {
				return "Resource Id";
			} else {
				return lang.toString();
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex == getRows().size())
				return "";
			L10n l10n = getRows().get(rowIndex);
			Language lang = columnLang(columnIndex);
			if (lang == null) {
				return l10n.resourceId;
			} else {
				return l10n.translations.get(lang);
			}
		}
		
		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			L10n l10n;
			if (rowIndex == getRows().size()) {
				addAll(Arrays.asList(l10n = new L10n()));
			} else {
				l10n = getRows().get(rowIndex);
			}
			Language lang = columnLang(columnIndex);
			if (lang == null) {
				l10n.resourceId = (String) value;
			} else {
				l10n.translations.put(lang, (String) value);
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
		
		static Language columnLang(int columnIndex) {
			if (columnIndex == 0) {
				return null;
			} else {
				return Language.values()[columnIndex - 1];
			}
		}
	}

	private static class PrettyPrinter {

		private final String GAP ="  ";
		private final Appendable appendable;
		
		public PrettyPrinter(Appendable appendable) {
			this.appendable = appendable;
		}
		
		public void prettyPrint(Object obj) throws IOException {
			prettyPrint(obj, "");
		}
		
		private void prettyPrint(Object obj, String indent) throws IOException {
			String indentGap = indent + GAP;
			boolean multiline = isMultiline(obj);
			if (obj instanceof Map<?, ?>) {
				appendable.append("{");
				boolean b = false;
				for (Map.Entry<?, ?> e : ((Map<?, ?>) obj).entrySet()) {
					String property = e.getKey().toString();
					if (b)
						appendable.append(',');
					if (multiline) {
						appendable.append('\n');
						appendable.append(indentGap);
					} else {
						appendable.append(' ');
					}
					appendable.append('"').append(JSONValue.escape(property)).append('"');
					appendable.append(": ");
					prettyPrint(e.getValue(), indentGap);
					b = true;
				}
				if (b && multiline) {
					appendable.append("\n").append(indent);
				} else {
					appendable.append(' ');
				}
				appendable.append("}");
			} else if (obj instanceof List<?>) {
				appendable.append("[");
				boolean b = false;
				int index = 0;
				for (Object o : (List<?>) obj) {
					if (b)
						appendable.append(',');
					if (multiline) {
						appendable.append('\n');
						appendable.append(indentGap);
					} else {
						appendable.append(' ');
					}
					prettyPrint(o, indentGap);
					b = true;
					index++;
				}
				if (b && multiline) {
					appendable.append("\n").append(indent);
				} else {
					appendable.append(' ');
				}
				appendable.append("]");
			} else {
				appendable.append(JSONValue.toJSONString(obj));
			}
		}
		
		private boolean isMultiline(Object obj) {
			if (obj instanceof Map<?, ?>) {
				Map<?, ?> map = (Map<?, ?>) obj;
				switch (map.size()) {
					case 0: return false;
					case 1: return isMultiline(map.values().iterator().next());
					case 2: 
						Iterator<?> iter = map.values().iterator();
						return isMultiline(iter.next()) && isMultiline(iter.next());
					default: return true;
				}
			} else if (obj instanceof List<?>) {
				List<?> list = (List<?>) obj;
				switch (list.size()) {
					case 0: return false;
					case 1: return isMultiline(list.get(0));
					default: return true;
				}
			}
			return false;
		}
	}
}
