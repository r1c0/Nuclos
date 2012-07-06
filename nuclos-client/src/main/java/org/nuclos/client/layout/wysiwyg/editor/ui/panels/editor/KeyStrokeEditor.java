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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.editor;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.PlainDocument;

import org.nuclos.client.NuclosIcons;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.KEYSTROKE_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableOptionGroup;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOptions;
import org.nuclos.common2.StringUtils;

/**
 * This Editor is used to edit a {@link WYSIWYGOptions} Object.<br>
 * Used for {@link WYSIWYGCollectableOptionGroup}<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class KeyStrokeEditor extends JDialog implements SaveAndCancelButtonPanelControllable {

	private int height = 160;
	private int width = 250;

    private JCheckBox cbxAlt;
    private JCheckBox cbxShift;
    private JCheckBox cbxCtrl;
    private JLabel lblModifiers;
    private JLabel lblKey;
    private JTextField tftKey;

	private String value;

	private String backupString;

	public static String returnString;

	/**
	 * @param options the {@link WYSIWYGOptions} to be edited by this Editor
	 */
	private KeyStrokeEditor(String value) {
		this.setIconImage(NuclosIcons.getInstance().getScaledDialogIcon(48).getImage());
	
		this.setTitle(KEYSTROKE_EDITOR.LABEL_TITEL);
		
		//TODO align relative to parent Component
		this.value = value;

        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel pnlMain = new javax.swing.JPanel();
        javax.swing.JPanel pnlCenter = new javax.swing.JPanel();
        lblModifiers = new javax.swing.JLabel();
        cbxCtrl = new javax.swing.JCheckBox();
        cbxShift = new javax.swing.JCheckBox();
        cbxAlt = new javax.swing.JCheckBox();
        lblKey = new javax.swing.JLabel();
        tftKey = new javax.swing.JTextField();
        tftKey.setDocument(new SpecialCharacterDocument());
        
        setLayout(new java.awt.BorderLayout());

        pnlMain.setLayout(new java.awt.GridBagLayout());

        pnlCenter.setBorder(javax.swing.BorderFactory.createTitledBorder(KEYSTROKE_EDITOR.LABEL_TITELEDBORDER));
        pnlCenter.setLayout(new java.awt.GridBagLayout());

        lblModifiers.setText(KEYSTROKE_EDITOR.LABEL_MODIFIERS);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlCenter.add(lblModifiers, gridBagConstraints);

        cbxCtrl.setText(KEYSTROKE_EDITOR.LABEL_MODIFIER_CTRL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        pnlCenter.add(cbxCtrl, gridBagConstraints);

        cbxShift.setText(KEYSTROKE_EDITOR.LABEL_MODIFIER_SHIFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlCenter.add(cbxShift, gridBagConstraints);

        cbxAlt.setText(KEYSTROKE_EDITOR.LABEL_MODIFIER_ALT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlCenter.add(cbxAlt, gridBagConstraints);

        lblKey.setText(KEYSTROKE_EDITOR.LABEL_KEY);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlCenter.add(lblKey, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 14, 0, 0);
        pnlCenter.add(tftKey, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        pnlMain.add(pnlCenter, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlMain.add(new SaveAndCancelButtonPanel(getBackground(), this, null), gridBagConstraints);

        add(pnlMain, java.awt.BorderLayout.CENTER);

		backupString = new String(value);
		
		setValuesToString();

		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - width) / 2;
		int y = (screenSize.height - height) / 2;
		this.setBounds(x, y, width, height);
		this.setModal(true);
		this.setResizable(false);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tftKey.requestFocus(true);
				tftKey.requestFocusInWindow();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						tftKey.selectAll();
					}
				});
			}
		});
		this.setVisible(true);
	}
	
	class SpecialCharacterDocument extends PlainDocument {

		@Override
		public void insertString(int offset, String str, javax.swing.text.AttributeSet a)  throws javax.swing.text.BadLocationException {
			PlainDocument doc = new PlainDocument();
			doc.insertString(0, tftKey.getText(), a);
			doc.insertString(offset, str, a);
			String current = doc.getText(0, doc.getLength());
			if (current.equals("F"))
				super.insertString(offset, str, a);
			else {
				for (int i = 1; i <= 12; i++) {
					if (current.equals("F" + i)) {
						super.insertString(offset, str, a);
						break;
					}
				}
			}
	    }
	}

	/**
	 * This Method is to be called to open a new Editor.<br>
	 * Works like {@link JOptionPane#showInputDialog(Object)}
	 * @param String the {@link String} to be edited
	 * @return the edited {@link String} Object
	 */
	public static String showEditor(String value) {
		new KeyStrokeEditor(value);

		return returnString;
	}

	private void setValuesToString() {
		KeyStroke key = KeyStroke.getKeyStroke(value);
		if (key != null) {
			tftKey.setText(getVKText(key.getKeyCode()));
			int modifiers = key.getModifiers();
	        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0 )
				cbxAlt.setSelected(true);
	        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0 )
				cbxShift.setSelected(true);
	        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0 )
				cbxCtrl.setSelected(true);
		}
	}
	
	private void setValuesFromString() {
		KeyStroke key = KeyStroke.getKeyStroke(tftKey.getText().toUpperCase());
		
		int modifiers = 0;
		if (cbxAlt.isSelected())
			modifiers |= KeyEvent.ALT_DOWN_MASK;
		if (cbxShift.isSelected())
			modifiers |= KeyEvent.SHIFT_DOWN_MASK;
		if (cbxCtrl.isSelected())
			modifiers |= KeyEvent.CTRL_DOWN_MASK;
		
		if (key == null)
			value = null;
		else
			value = KeyStroke.getKeyStroke(key.getKeyCode(), modifiers).toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performCancelAction()
	 */
	@Override
	public void performCancelAction() {
		value = null;
		value = backupString;
		returnString = backupString;
		this.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performSaveAction()
	 */
	@Override
	public void performSaveAction() {
		setValuesFromString();
		
		if (StringUtils.isNullOrEmpty(value) ||
				KeyStroke.getKeyStroke(value) == null) {
			JOptionPane.showMessageDialog(this, KEYSTROKE_EDITOR.ERROR_INCOMPLETE_KEY);
			return;
		}
		
		returnString = value;
		this.dispose();
	}
	
	static class VKCollection {
	    Map code2name;
	    Map name2code;

	    public VKCollection() {
	        code2name = new HashMap();
	        name2code = new HashMap();
	    }

	    public synchronized void put(String name, Integer code) {
	        assert((name != null) && (code != null));
	        assert(findName(code) == null);
	        assert(findCode(name) == null);
	        code2name.put(code, name);
	        name2code.put(name, code);
	    }

	    public synchronized Integer findCode(String name) {
	        assert(name != null);
	        return (Integer)name2code.get(name);
	    }

	    public synchronized String findName(Integer code) {
	        assert(code != null);
	        return (String)code2name.get(code);
	    }
	}
    static String getVKText(int keyCode) { 
        VKCollection vkCollect = new VKCollection();
        Integer key = Integer.valueOf(keyCode);
        String name = vkCollect.findName(key);
        if (name != null) {
            return name.substring(3);
        }
        int expected_modifiers = 
            (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                if (fields[i].getModifiers() == expected_modifiers
                    && fields[i].getType() == Integer.TYPE
                    && fields[i].getName().startsWith("VK_")
                    && fields[i].getInt(KeyEvent.class) == keyCode) 
                {
                    name = fields[i].getName();
                    vkCollect.put(name, key);
                    return name.substring(3);
                }
            } catch (IllegalAccessException e) {
                assert(false);
            }
        }
        return "UNKNOWN";
    }
}
