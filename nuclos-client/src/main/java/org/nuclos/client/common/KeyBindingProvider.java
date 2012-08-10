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
 * Created on 23.05.2005
 *
 */
package org.nuclos.client.common;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * This class is responsible for setting the functionkeys for the accelerators
 * The class is a singleton.
 * @author leo.zeef
 */
public class KeyBindingProvider {
//	public static KeyBindingProvider singleton = null;

	public static final KeyBinding SHOW_LOGBOOK = new KeyBinding("START_LOGBOOK");
	public static final KeyBinding SHOW_STATE_HISTORIE = new KeyBinding("SHOW_STATE_HISTORIE");
	public static final KeyBinding SHOW_HIDE_TASKLIST = new KeyBinding("SHOW_HIDE_TASKLIST");
	public static final KeyBinding SHOW_HIDE_EXPLORER = new KeyBinding("SHOW_HIDE_EXPLORER");
	public static final KeyBinding NEXT_TAB = new KeyBinding("NEXT_WINDOW");
	public static final KeyBinding PREVIOUS_TAB = new KeyBinding("PREVIOUS_WINDOW");
	public static final KeyBinding ACTIVATE_SEARCH_PANEL_1 = new KeyBinding("ACTIVATE_SEARCH_PANEL_1");
	public static final KeyBinding ACTIVATE_SEARCH_PANEL_2 = new KeyBinding("ACTIVATE_SEARCH_PANEL_2");
	public static final KeyBinding START_SEARCH = new KeyBinding("START_SEARCH");
	public static final KeyBinding REFRESH = new KeyBinding("REFRESH");
	public static final KeyBinding SAVE_1 = new KeyBinding("SAVE_1");
	public static final KeyBinding SAVE_2 = new KeyBinding("SAVE_2");
	public static final KeyBinding NEW = new KeyBinding("NEW");
	public static final KeyBinding NEW_SEARCHVALUE = new KeyBinding("NEW_SEARCHVALUE");
	public static final KeyBinding PRINT_LEASED_OBJECT = new KeyBinding("PRINT_LEASED_OBJECT");
	public static final KeyBinding FOCUS_ON_LIVE_SEARCH = new KeyBinding("FOCUS_ON_LIVE_SEARCH");

	public static final KeyBinding FIRST = new KeyBinding("FIRST");
	public static final KeyBinding LAST = new KeyBinding("LAST");
	public static final KeyBinding PREVIOUS_1 = new KeyBinding("PREVIOUS_1");
	public static final KeyBinding PREVIOUS_2 = new KeyBinding("PREVIOUS_2");
	public static final KeyBinding NEXT_1 = new KeyBinding("NEXT_1");
	public static final KeyBinding NEXT_2 = new KeyBinding("NEXT_2");

	public static final KeyBinding CLOSE_CHILD = new KeyBinding("CLOSE_CHILD");
	public static final KeyBinding EDIT_1 = new KeyBinding("EDIT_1");
	public static final KeyBinding EDIT_2 = new KeyBinding("EDIT_2");

	/**
	 * The initialisation is put in the static block to ensure that the keybindings
	 * are initialized at classload time
	 */
	static {
		SHOW_LOGBOOK.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		SHOW_STATE_HISTORIE.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		SHOW_HIDE_EXPLORER.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		SHOW_HIDE_TASKLIST.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		REFRESH.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		// ELISA-6851 ELISA-6971
		PREVIOUS_TAB.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		NEXT_TAB.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		ACTIVATE_SEARCH_PANEL_1.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		ACTIVATE_SEARCH_PANEL_2.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		START_SEARCH.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
		SAVE_1.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
		SAVE_2.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		NEW.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		NEW_SEARCHVALUE.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		PRINT_LEASED_OBJECT.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		FOCUS_ON_LIVE_SEARCH.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));

		FIRST.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.ALT_MASK));
		LAST.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.ALT_MASK));
		PREVIOUS_1.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_MASK));
		PREVIOUS_2.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK));
		NEXT_1.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_MASK));
		NEXT_2.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK));

		CLOSE_CHILD.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		EDIT_1.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		EDIT_2.setKeystroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	}

	private KeyBindingProvider() {
	}

//	public synchronized static KeyBindingProvider getInstance() {
//		if (singleton == null) {
//			singleton = new KeyBindingProvider();
//		}
//		return singleton;
//	}

	/**
	 *  Binds an action with a component.
	 * @param keybinding KeyBinding The key that has to be bound
	 * @param action Action The action that has be performed after pressing the key
	 * @param component JComponent The component on wich the action is performed
	 */
	public static void bindActionToComponent(KeyBinding keybinding, Action action, JComponent component) {
		removeActionFromComponent(keybinding, component);// remove first
		component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keybinding.getKeystroke(), keybinding.getKey());
		component.getInputMap(JComponent.WHEN_FOCUSED).put(keybinding.getKeystroke(), keybinding.getKey());
		component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keybinding.getKeystroke(), keybinding.getKey());
		component.getActionMap().put(keybinding.getKey(), action);
	}

	/**
	 *  Removes a certain action from a component.
	 * @param keybinding KeyBinding The key that has to be removed
	 * @param component JComponent The component on wich the key has to be removed
	 */
	public static void removeActionFromComponent(KeyBinding keybinding, JComponent component) {
		component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(keybinding.getKeystroke());
		component.getInputMap(JComponent.WHEN_FOCUSED).remove(keybinding.getKeystroke());
		component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(keybinding.getKeystroke());
		component.getActionMap().remove(keybinding.getKey());
	}
	
	public static void removeActionFromComponents(final KeyBinding keybinding, JComponent component) {
		removeActionFromComponent(keybinding, component);

		Component[] components = component.getComponents();
		for (int i = 0; i < components.length; i++) {
			final Component c = components[i];
			if (c instanceof JComponent)
				removeActionFromComponents(keybinding, ((JComponent)c));
			if (c instanceof JComboBox) {
				removeActionFromComponents(keybinding, ((JComponent)((JComboBox)c).getEditor().getEditorComponent()));
				((JComboBox)c).addAncestorListener(new AncestorListener() {
					@Override
					public void ancestorAdded(AncestorEvent event) {
						if (keybinding.getKeystroke().equals(KeyStroke.getKeyStroke("ctrl pressed F1"))) {
							clearInputMap(((JComponent)((JComboBox)c).getEditor().getEditorComponent()));
							// better remove only the keybinding. but behavior is okay if we clear all here.
							//removeActionFromComponent(keybinding, ((JComponent)((JComboBox)c).getEditor().getEditorComponent()));
						}

						((JComboBox)c).removeAncestorListener(this);
					}
					@Override
					public void ancestorMoved(AncestorEvent event) {
					}
					@Override
					public void ancestorRemoved(AncestorEvent event) {
					}
				});
			}
		}
	}

	/**
	 * Convenience method to clear the inputmap of a component.
	 * Use for Swing Components that have a default keybinding that conflicts with
	 * the application keysettings.
	 * @param component JComponent the compoment whose inputmap needs to be cleared
	 */
	public static void clearInputMap(JComponent component) {
		SwingUtilities.replaceUIInputMap(component, JComponent.WHEN_FOCUSED, null);
		SwingUtilities.replaceUIInputMap(component, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
		SwingUtilities.replaceUIInputMap(component, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
	}
}
