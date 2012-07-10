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
package org.nuclos.client.ui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.util.TableLayoutBuilder;
import org.nuclos.common2.SpringLocaleDelegate;

public class OverlayOptionPane extends JScrollPane implements IOverlayCenterComponent {
	
	/** 
     * Type meaning Look and Feel should not supply any options -- only
     * use the options from the <code>OverlayOptionPane</code>.
     */
    public static final int         DEFAULT_OPTION = JOptionPane.DEFAULT_OPTION;
    /** Type used for <code>showConfirmDialog</code>. */
    public static final int         YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
    /** Type used for <code>showConfirmDialog</code>. */
    public static final int         YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
    /** Type used for <code>showConfirmDialog</code>. */
	public static final int         OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;
	
	/** Return value from class method if YES is chosen. */
    public static final int         YES_OPTION = JOptionPane.YES_OPTION;
    /** Return value from class method if NO is chosen. */
    public static final int         NO_OPTION = JOptionPane.NO_OPTION;
    /** Return value from class method if CANCEL is chosen. */
    public static final int         CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
    /** Return value form class method if OK is chosen. */
    public static final int         OK_OPTION = JOptionPane.OK_OPTION;
    /** Return value from class method if user closes window without selecting
     * anything, more than likely this should be treated as either a
     * <code>CANCEL_OPTION</code> or <code>NO_OPTION</code>. */
    public static final int         CLOSED_OPTION = JOptionPane.CLOSED_OPTION;
 
    private static final int ESC = KeyEvent.VK_ESCAPE;
    
    private static final int ENTER = KeyEvent.VK_ENTER;
    
    private final JPanel main;
    
    private final List<IOverlayFrameChangeListener> overlayFrameChangeListeners = new ArrayList<IOverlayFrameChangeListener>(1);
    
    private final OvOpListener listener;
    
    private int result = CLOSED_OPTION;
    
    public static void showConfirmDialog(MainFrameTab tab, Object message, String title, int optionType, OvOpListener listener) {
    	new OverlayOptionPane(tab, message, title, optionType, null, listener);
    }
    
    public static void showConfirmDialog(MainFrameTab tab, Object message, String title, int optionType, Icon icon, OvOpListener listener) {
    	new OverlayOptionPane(tab, message, title, optionType, icon, listener);
    }
	
	public OverlayOptionPane(final MainFrameTab tab, Object message, String title, int optionType, Icon icon, final OvOpListener listener) {
		super();
		this.listener = listener;
		final MainFrameTab parentTab = tab.getLastOverlayTab();
		
		main = new JPanel();
		setViewportView(main);
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		setBorder(BorderFactory.createEmptyBorder());
				
		TableLayoutBuilder tbllay = new TableLayoutBuilder(main).columns(TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL);
		tbllay.newRow();
		JLabel jlbTitle = new JLabel(title, JLabel.CENTER);
		Map<TextAttribute, Object> fontAttributes = new HashMap<TextAttribute, Object>(jlbTitle.getFont().getAttributes());
		fontAttributes.put(TextAttribute.SIZE, new Float(((Float)fontAttributes.get(TextAttribute.SIZE)).intValue()   +1  ));
		fontAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		jlbTitle.setFont(new Font(fontAttributes));
		jlbTitle.setBorder(BorderFactory.createEmptyBorder(20, 40, 5, 40));
		jlbTitle.setOpaque(true);
		jlbTitle.setForeground(Color.WHITE);
		jlbTitle.setBackground(NuclosThemeSettings.BACKGROUND_ROOTPANE);
		tbllay.addFullSpan(jlbTitle);
		tbllay.newRow();
		
		JPanel jpnOptions = createOptions(optionType);
		
		if (icon != null) {
			final JPanel labHolder = new JPanel(new TableLayout(new double[] {TableLayout.PREFERRED}, new double[] {TableLayout.FILL}));
			labHolder.add(new JLabel(icon), new TableLayoutConstraints(0, 0, 0, 0, TableLayout.LEFT, TableLayout.CENTER));
			labHolder.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 0));
//			labHolder.setBackground(Color.BLUE);
			tbllay.add(labHolder);
		}
		
		if (message instanceof Component) {
			tbllay.addFullSpan((Component)message);
		} else {
			final JLabel label = new LineBreakLabel(message==null?"":message.toString(), jpnOptions.getPreferredSize().width + 60);
			final JPanel labHolder = new JPanel();
			labHolder.add(label);
			labHolder.setBorder(BorderFactory.createEmptyBorder(20, icon==null?20:10, 10, 20));
//			labHolder.setBackground(Color.GREEN);
			tbllay.addFullSpan(labHolder);
		}
		
		tbllay.newRow().addFullSpan(jpnOptions);
		
		parentTab.add(this);
	}
	
	private JPanel createOptions(int optionType) {
		
		switch (optionType) {
		case YES_NO_OPTION: {
			JButton jbtnYES = createButton("OverlayOptionPane.Yes", YES_OPTION);
			JButton jbtnNO = createButton("OverlayOptionPane.No", NO_OPTION);
			
			return createPanel(0, jbtnYES, jbtnNO); }
		case YES_NO_CANCEL_OPTION: {
			JButton jbtnYES = createButton("OverlayOptionPane.Yes", YES_OPTION);
			JButton jbtnNO = createButton("OverlayOptionPane.No", NO_OPTION);
			JButton jbtnCANCEL = createButton("OverlayOptionPane.Cancel", CANCEL_OPTION);
			
			return createPanel(0, jbtnYES, jbtnNO, jbtnCANCEL); }
		default: {
			JButton jbtnOK = createButton("OverlayOptionPane.OK", OK_OPTION);
			JButton jbtnCANCEL = createButton("OverlayOptionPane.Cancel", CANCEL_OPTION);
			
			return createPanel(0, jbtnOK, jbtnCANCEL);}
		}
	}
	
	private OverlayOptionPaneButton createButton(final String resource, final int result) {
		OverlayOptionPaneButton oopb = new OverlayOptionPaneButton(new AbstractAction(SpringLocaleDelegate.getInstance().getMessage(resource,resource)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				OverlayOptionPane.this.result = result;
				close();
			}
		});
		return oopb;
	}
	
	private JPanel createPanel(final int focusOwner, final Component...components) {
		JPanel result = new JPanel(new FlowLayout());
		int maxWidth = 0;
		for (Component c : components) {
			result.add(c);
			maxWidth = Math.max(c.getPreferredSize().width, maxWidth);
		}
		
		for (Component c : components) {
			Dimension prefSize = c.getPreferredSize();
			prefSize.width = maxWidth;
			c.setPreferredSize(prefSize);
		}
		
		result.setFocusTraversalPolicy(new OverlayOptionPaneFocusTraversalPolicy(components));
		result.setFocusCycleRoot(true);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				components[focusOwner].requestFocusInWindow();
			}
		});
		
		return result;
	}
	
	private void close() {
		for (IOverlayFrameChangeListener listener : new ArrayList<IOverlayFrameChangeListener>(overlayFrameChangeListeners)) {
			listener.closeOverlay();
		}
	}
	
	@Override
	public Dimension getCenterSize() {
		return getPreferredSize();
	}
	
	@Override
	public boolean isClosable() {
		return true;
	}

	@Override
	public void notifyClosing(ResultListener<Boolean> rl) {
		rl.done(isClosable());
		if (isClosable() && listener != null) {
			listener.done(result);
		}
	}

	@Override
	public void transferSize(Dimension size) {
	}

	@Override
	public void addOverlayFrameChangeListener(
			IOverlayFrameChangeListener listener) {
		overlayFrameChangeListeners.add(listener);
	}


	@Override
	public void removeOverlayFrameChangeListener(
			IOverlayFrameChangeListener listener) {
		overlayFrameChangeListeners.remove(listener);
	}
	
	private class OverlayOptionPaneButton extends JButton {
		
		public OverlayOptionPaneButton(Action act) {
			super(act);
		}

		@Override
		public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
			if (e.getKeyCode() == ENTER) {
				if (!pressed) {
					doClick();
					return true;
				}
			} else if (e.getKeyCode() == ESC) {
				if (!pressed) {
					close();
					if (listener != null) {
						listener.done(CLOSED_OPTION);
					}
					return true;
				}
			}
			return false;
		}	
		
	}
	
	private static class OverlayOptionPaneFocusTraversalPolicy extends FocusTraversalPolicy {
		Vector<Component> order;

		public OverlayOptionPaneFocusTraversalPolicy(Component[] order) {
			this.order = new Vector<Component>(order.length);
			for (Component c : order) {
				this.order.add(c);
			}
		}

		public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
			int idx = (order.indexOf(aComponent) + 1) % order.size();
			return order.get(idx);
		}

		public Component getComponentBefore(Container focusCycleRoot,
				Component aComponent) {
			int idx = order.indexOf(aComponent) - 1;
			if (idx < 0) {
				idx = order.size() - 1;
			}
			return order.get(idx);
		}

		public Component getDefaultComponent(Container focusCycleRoot) {
			return order.get(0);
		}

		public Component getLastComponent(Container focusCycleRoot) {
			return order.lastElement();
		}

		public Component getFirstComponent(Container focusCycleRoot) {
			return order.get(0);
		}
	}
	
}
