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

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.labeled.ILabeledComponentSupport;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;

public class HyperlinkTextFieldWithButton extends TextFieldWithButton {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(HyperlinkTextFieldWithButton.class);
	
	protected static final int ENTER = KeyEvent.VK_ENTER;
	
	protected static final int CTRL = InputEvent.CTRL_MASK;
	
	private static final Cursor curHand = new Cursor(Cursor.HAND_CURSOR);
	
//	private Font defaultFont;
//	
//	private Font underlineFont;
	
	private final boolean bSearchable;
	
	private boolean buttonEnabled = true;
	
	private boolean openLink = true;

	public HyperlinkTextFieldWithButton(ILabeledComponentSupport support, boolean bSearchable) {
		super(Icons.getInstance().getIconTextFieldButtonHyperlink(), support);
		this.bSearchable = bSearchable;
		if (!bSearchable) {
			addMouseListener(new HyperlinkMouseListener());
			addFocusListener(new HyperlinkFocusListener());
			addKeyListener(new HyperlinkKeyListener());
		}
//		setFont(getFont());
	}
	
//	@Override
//	public void setFont(Font f) {
//		defaultFont = f;
//		underlineFont = getUnderline(f);
//		super.setFont(openLink?underlineFont:defaultFont);
//	}

//	private Font getUnderline(Font change) {
//		Map<TextAttribute, Object> fontAttributes = new HashMap<TextAttribute, Object>(change.getAttributes());
//		fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
//		return new Font(change.getName(), change.getStyle(), change.getSize()).deriveFont(fontAttributes);
//	}
	
	public void setButtonEnabled(boolean enabled) {
		this.buttonEnabled = enabled;
	}

	@Override
	public boolean isButtonEnabled() {
		return buttonEnabled;
	}
	
	public void setOpenLink(boolean openLink) {
		this.openLink = openLink;
		setButtonEnabled(openLink);
//		super.setFont(openLink?underlineFont:defaultFont);
	}
	
	@Override
	public void setText(String text) {
		super.setText(text);
		setOpenLink(true);
	}

	@Override
	public void buttonClicked(MouseEvent me) {
		if (SwingUtilities.isLeftMouseButton(me)) {
			setOpenLink(!openLink);
		}
	}
	
	@Override
	public void textClicked(MouseEvent me) {
		if (!bSearchable && SwingUtilities.isLeftMouseButton(me) && !me.isControlDown() && 
				openLink && !StringUtils.looksEmpty(getText())) {
			openLink();
		}
	}
	
	/**
	 * Click or Ctrl+Enter
	 */
	public void openLink() {
		try {
			if (!StringUtils.looksEmpty(getText())) {
				String url = getText();
				if (url.indexOf("://") == -1) {
					url = "http://"+url;
				}
				Desktop.getDesktop().browse(URI.create(url));
			}
		} catch (IllegalArgumentException e) {
			// ignore.
		} catch (IOException e) {
			log.info(e.getMessage());
		}
	}
	
	@Override
	protected Cursor getDefaultCursor(MouseEvent me) {
		if (!bSearchable && !me.isControlDown() &&
				openLink && !StringUtils.looksEmpty(getText())) {
			return curHand;
		} else {
			return super.getDefaultCursor(me);
		}
	}
	
	protected List<JComponent> getContextMenuItems() {
		List<JComponent> result = new ArrayList<JComponent>();
		result.add(getEditMenuItem());
		result.add(getOpenMenuItem());
		return result;
	}
	
	protected JMenuItem getOpenMenuItem() {
		JMenuItem miOpen = new JMenuItem(new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage("Hyperlink.open", "Öffnen")) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				openLink();
			}
		});
		miOpen.setAccelerator(KeyStroke.getKeyStroke(ENTER, CTRL));
		miOpen.setEnabled(!StringUtils.looksEmpty(getText()));
		return miOpen;
	}
	
	protected JMenuItem getEditMenuItem() {
		return new JMenuItem(new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage("Hyperlink.edit", "Bearbeiten"),
				Icons.getInstance().getIconEdit16()) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setOpenLink(false);
			}
		});
	}

	private class HyperlinkMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent me) {
			if (SwingUtilities.isRightMouseButton(me)) {
				requestFocusInWindow();
				JPopupMenu popup = new JPopupMenu();
				for (JComponent c : getContextMenuItems()) {
					popup.add(c);
				}
				popup.show(HyperlinkTextFieldWithButton.this, me.getX(), me.getY());
			}
		}
		
	}
	
	private class HyperlinkFocusListener extends FocusAdapter {
		
		@Override
		public void focusGained(FocusEvent e) {
			if (openLink) {
				setOpenLink(false);
			} 
		}

		@Override
		public void focusLost(FocusEvent e) {
			if (!openLink) {
				setOpenLink(true);
			}
		}
				
	}
	
	private class HyperlinkKeyListener extends KeyAdapter {

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.isControlDown()) {
				if (e.getKeyCode() == ENTER) {
					openLink();
				}
			}
		}
		
	}

}
