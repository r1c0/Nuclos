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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;

public class FrameUtils {
	public static Map<JInternalFrame, Window> int2ext = new HashMap<JInternalFrame, Window>();
	public static Map<Window, JInternalFrame> ext2int = new HashMap<Window, JInternalFrame>();
	
	public static boolean isToolWindow(JInternalFrame internalframe) {
		if(internalframe == null)
			throw new IllegalArgumentException();
		final Object obj = internalframe.getClientProperty("JInternalFrame.isPalette");
		return obj != null && obj == Boolean.TRUE;
	}

	public static void makeToolWindow(JInternalFrame frame, boolean isToolWindow) {
		if(frame == null)
			throw new IllegalArgumentException();
		frame.putClientProperty("JInternalFrame.isPalette", isToolWindow ? Boolean.TRUE : Boolean.FALSE);
	}

	public static boolean isExternal(JInternalFrame internalframe) {
		return int2ext.containsKey(internalframe);
	}

	public static void externalizeWindow(final JInternalFrame internalframe) {
		CommonJFrame externalFrame = new CommonJFrame();
		boolean success = externalizeIntoWindow(internalframe, externalFrame);
		if (!success)
			return;
		externalFrame.setTitle(internalframe.getTitle());
		externalFrame.setVisible(true);
	}
	
	public static <W extends Window & RootPaneContainer> boolean externalizeIntoWindow(final JInternalFrame internalframe, final W window) {
		if(internalframe == null)
			throw new IllegalArgumentException();
		
		if(isExternal(internalframe))
			return false;

		window.setContentPane(internalframe.getContentPane());
		window.setBounds(checkBounds(internalframe.getBounds(), internalframe));
		
		int2ext.put(internalframe, window);
		ext2int.put(window, internalframe);
		
		Icon icon = internalframe.getFrameIcon();
		if(icon instanceof ImageIcon)
			window.setIconImage(((ImageIcon)internalframe.getFrameIcon()).getImage());
		else
			window.setIconImage(UIUtils.iconToImage(internalframe.getFrameIcon()));

		internalframe.addInternalFrameListener(internalFrameAdapter);

		window.addWindowListener(new WindowAdapter() {
			final MainFrame mainframe = Main.getInstance().getMainFrame();
			@Override
			public void windowClosing(WindowEvent e) {
				ext2int.remove(int2ext.remove(internalframe));
				internalframe.dispose();
			}
			@Override
			public void windowActivated(WindowEvent e) {
				//mainframe.setInternalFrameSelectedInWindowMenu(internalframe, true);
				//mainframe.getTaskBar().setInternalFrameSelectedInWindowMenu(internalframe, true);
			}
		});

		internalframe.setVisible(false);

		if(internalframe.getParent() != null)
			internalframe.getParent().validate();
		
		return true;
	}
	
	public static void internalizeWindow(JInternalFrame internalframe, JDesktopPane desktop) {
		if(internalframe == null)
			throw new IllegalArgumentException();
		
		if(!isExternal(internalframe))
			return;
		
		ext2int.remove(int2ext.remove(internalframe));
		
		Window container = (Window) SwingUtilities.getAncestorOfClass(Window.class, internalframe.getContentPane());
		if(container != null)
			container.setVisible(false);

		// internalframe.setTitle(container.getTitle());
		internalframe.setContentPane(((RootPaneContainer) container).getContentPane());
		internalframe.setBounds(checkBounds(container.getBounds(), null));
		internalframe.removeInternalFrameListener(internalFrameAdapter);
		
		if(container != null)
			container.dispose();
		internalframe.setVisible(true);
		desktop.validate();
	}
	
	private static Rectangle checkBounds(Rectangle bounds, JInternalFrame internalFrame) {
		Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle returnValue = bounds;
		
		if(returnValue.x < 0)
			returnValue.x = 0;
		if(returnValue.y < 0)
			returnValue.y = 0;
		
		if(internalFrame == null) {
			if(returnValue.x > screensize.width) {
				returnValue.x = screensize.width - returnValue.width;
			}

			if(returnValue.y > screensize.height) {
				returnValue.y = screensize.height - returnValue.height;
			}
		} else if (internalFrame.getDesktopPane() != null) {
			Rectangle desktopsize = internalFrame.getDesktopPane().getBounds();
			
			if(returnValue.x > desktopsize.width) {
				returnValue.x = desktopsize.width - returnValue.width;
			}

			if(returnValue.y > desktopsize.height) {
				returnValue.y = desktopsize.height - returnValue.height;
			}
			
		}

		return returnValue;
	}

	private static InternalFrameAdapter internalFrameAdapter = new InternalFrameAdapter() {
		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
			Window window = int2ext.get(e.getInternalFrame());
			if (window != null)
				window.dispose();
		}
	};

	public static void externalizeIntoWindow(MainFrameTab ifrm,
		JDialog d) {
		// TODO Auto-generated method stub
		
	}
}
