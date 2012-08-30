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
package org.nuclos.client.common;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.nuclos.client.ui.util.TableLayoutBuilder;
import org.nuclos.common.LafParameter;
import org.nuclos.common.LafParameterStorage;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;

public class LafParameterHelper {
	
	public static void installPopup(JComponent c, LafParameter<?> parameter) {
		installPopup(c, parameter, null);
	}

	public static void installPopup(JComponent c, LafParameter<?> parameter, Long entityId) {
		boolean changeAllowed = false;
		for (LafParameterStorage storage : LafParameterStorage.values()) {
			if (LafParameterProvider.getInstance().isStorageAllowed(parameter, storage)) {
				changeAllowed = true;
			}
		}
		
		if (changeAllowed) {
			JPopupMenu pm = c.getComponentPopupMenu();
			if (pm == null) {
				pm = new JPopupMenu();
				c.setComponentPopupMenu(pm);
			}
			pm.add(new LafParameterAction(parameter, entityId, c));
		}
	}
	
	@SuppressWarnings("serial")
	private static class LafParameterAction extends AbstractAction {
		
		private final JComponent parent;
		
		private final LafParameter<?> parameter;
		
		private final Long entityId;
		
		public LafParameterAction(LafParameter<?> parameter, Long entityId, JComponent parent) {
			super(String.format(SpringLocaleDelegate.getInstance().getResource("LafParameterAction.1", "Parameter \"%s\" ändern"), SpringLocaleDelegate.getInstance().getResource(parameter.getName(), parameter.getName())));
			this.parameter = parameter;
			this.entityId = entityId;
			this.parent = parent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LafParameterEditor editor = new LafParameterEditor(parameter, entityId, parent);
			editor.run();
			if (editor.okay()) {
				String value = editor.getValueFromEditor();
				LafParameterStorage storage = editor.getSelectedStorage();
				LafParameterProvider.getInstance().setValue(parameter, entityId, storage, value);
			}
		}
		
	}
	
	private static class LafParameterEditor implements ActionListener {
		
		private final JComponent parent;
		
		private final LafParameter<?> parameter;
		
		private final Long entityId;
		
		private JPanel jpnMain;
		private JPanel jpnStorage;
		
		private JLabel jlb;
		
		private JTextField jtfEditor;
		private JComboBox jcbEditor;
		private ButtonGroup bgStorages;
		
		private LafParameterStorage storage;
		private boolean okay = false;
		
		public LafParameterEditor(LafParameter<?> parameter, Long entityId, JComponent parent) {
			this.parameter = parameter;
			this.entityId = entityId;
			this.parent = parent;
			init();
		}
		
		private void init() {
			jpnMain = new JPanel();
			jpnMain.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			jpnStorage = new JPanel();
			jpnStorage.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
			jlb = new JLabel(SpringLocaleDelegate.getInstance().getResource(parameter.getName(), parameter.getName()) + ":");
			
			TableLayout tbllayMain = new TableLayout(
					new double[] {TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL}, 
					new double[] {TableLayout.PREFERRED, TableLayout.FILL, 20, TableLayout.PREFERRED});
			jpnMain.setLayout(tbllayMain);
			TableLayoutBuilder tbllayStorage = new TableLayoutBuilder(jpnStorage).columns(TableLayout.PREFERRED);
			tbllayStorage.newRow().addLabel(SpringLocaleDelegate.getInstance().getResource("LafParameterEditor.2", "Wo möchten Sie den Wert ändern?"));
			
			jpnMain.add(jpnStorage, "0,0,0,1,l,t");
			jpnMain.add(new JSeparator(JSeparator.VERTICAL), "1,0,1,1");
			jpnMain.add(jlb, "2,0,2,0,l,t");
			
			if (parameter.getParameterClass() == boolean.class || parameter.getParameterClass() == Boolean.class) {
				jcbEditor = new JComboBox();
				jcbEditor.addItem("");
				jcbEditor.addItem(SpringLocaleDelegate.getInstance().getResource("LafParameterEditor.7", "Ja"));
				jcbEditor.addItem(SpringLocaleDelegate.getInstance().getResource("LafParameterEditor.8", "Nein"));
				jpnMain.add(jcbEditor, "2,1,2,1,l,t");
			} else {
				if (parameter.getFixedValueList() != null) {
					jcbEditor = new JComboBox();
					jcbEditor.addItem("");
					for (Object o : parameter.getFixedValueList()) {
						jcbEditor.addItem(SpringLocaleDelegate.getInstance().getResource(o.toString(), o.toString()));
					}
					jpnMain.add(jcbEditor, "2,1,2,1,l,t");
				} else {
					jtfEditor = new JTextField(40);
					jpnMain.add(jtfEditor, "2,1,2,1,l,t");
				}
			}
			
			bgStorages = new ButtonGroup();
			
			initStorage(LafParameterStorage.SYSTEMPARAMETER, SpringLocaleDelegate.getInstance().getResource("LafParameterEditor.4", "Systemparameter"), tbllayStorage);
			initStorage(LafParameterStorage.WORKSPACE, SpringLocaleDelegate.getInstance().getResource("LafParameterEditor.5", "Arbeitsumgebung"), tbllayStorage);
			initStorage(LafParameterStorage.ENTITY, SpringLocaleDelegate.getInstance().getResource("LafParameterEditor.6", "Entity"), tbllayStorage);
			
			jpnMain.add(new JLabel(SpringLocaleDelegate.getInstance().getResource("LafParameterEditor.3", "Möglicherweise müssen Sie die Maske erneut öffnen oder den Client neu starten, damit die Einstellung wirksam wird!")), "0,3,3,3,l,t");
		}
		
		public void run() {
			okay = JOptionPane.showConfirmDialog(parent, jpnMain, SpringLocaleDelegate.getInstance().getResource("LafParameterEditor.1", "L&F Parameter ändern"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
		}
		
		private void initStorage(LafParameterStorage storage, String label, TableLayoutBuilder tbllayStorage) {
			if (LafParameterProvider.getInstance().isStorageAllowed(parameter, storage)) {
				JRadioButton jrb = new JRadioButton(label);
				jrb.setActionCommand(storage.name());
				jrb.addActionListener(this);
				bgStorages.add(jrb);
				tbllayStorage.newRow();
				tbllayStorage.add(jrb);
				if (this.storage == null) {
					jrb.setSelected(true);
					setValueToEditor(storage);
				}
			}
		}
		
		private boolean okay() {
			return okay;
		}
		
		private LafParameterStorage getSelectedStorage() {
			return storage;
		}
		
		private String getValueFromEditor() {
			if (jcbEditor != null) {
				if (parameter.getParameterClass() == boolean.class || parameter.getParameterClass() == Boolean.class) {
					switch (jcbEditor.getSelectedIndex()) {
					case 1:
						return Boolean.TRUE.toString();
					case 2:
						return Boolean.FALSE.toString();
					default:
						return null;
					}
				} else {
					int index = jcbEditor.getSelectedIndex();
					if (index == 0) {
						return null;
					} else {
						return parameter.getFixedValueList()[index-1].toString();
					}
				}
			} else {
				String result = jtfEditor.getText();
				return "".equals(result)? null: result;
			}
		}
		
		private void setValueToEditor(LafParameterStorage storage) {
			this.storage = storage;
			if (parameter.getParameterClass() == boolean.class || parameter.getParameterClass() == Boolean.class) {
				Boolean value = (Boolean) LafParameterProvider.getInstance().getValue(parameter, entityId, storage);
				if (value == null) {
					jcbEditor.setSelectedIndex(0);
				} else if (value) {
					jcbEditor.setSelectedIndex(1);
				} else {
					jcbEditor.setSelectedIndex(2);
				}
			} else {
				Object o = LafParameterProvider.getInstance().getValue(parameter, entityId, storage);
				if (parameter.getFixedValueList() != null) {
					jcbEditor.setSelectedIndex(0);
					for (int i = 0; i < parameter.getFixedValueList().length; i++) {
						if (LangUtils.equals(o, parameter.getFixedValueList()[i])) {
							jcbEditor.setSelectedIndex(i+1);
						}
					}
				} else {
					jtfEditor.setText(o==null?"":o.toString());
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for (LafParameterStorage storage : LafParameterStorage.values()) {
				if (storage.name().equals(e.getActionCommand())) {
					setValueToEditor(storage);
				}
			}
		}
	}
}
