package org.nuclos.client.ui.collect.result;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public interface ResultKeyListener {
	
	public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed);
}
