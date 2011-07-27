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

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.CommonRunnableAdapter;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;

/**
 * UI utility methods.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class UIUtils {
	private static final Logger log = Logger.getLogger(UIUtils.class);

	private static CommandHandler commandhandler = new DefaultCommandHandler();

	protected UIUtils() {
	}

	/**
	 * creates a rectangle out of two points.
	 * @param x1 x coordinate of first point
	 * @param y1 y coordinate of first point
	 * @param x2 x coordinate of second point
	 * @param y2 y coordinate of second point
	 * @return the smallest Rectangle containing both points
	 */
	public static Rectangle newRectangle(int x1, int y1, int x2, int y2) {
		final int x = Math.min(x1, x2);
		final int y = Math.min(y1, y2);
		final int w = Math.abs(x1 - x2 + 1);
		final int h = Math.abs(y1 - y2 + 1);

		return new Rectangle(x, y, w, h);
	}

	/**
	 * creates a rectangle out of two points.
	 * @param p1	first point
	 * @param p2	second point
	 * @return the smallest Rectangle containing both points
	 */
	public static Rectangle newRectangle(Point p1, Point p2) {
		return newRectangle(p1.x, p1.y, p2.x, p2.y);
	}

	/**
	 * repaints a specified area inside a component.
	 * @param comp the component to repaint
	 * @param rect the area (relative to the component) to repaint
	 */
	public static void repaint(Component comp, Rectangle rect) {
		comp.repaint(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * repaints a specified area inside a component.
	 * @param comp the component to repaint
	 * @param compChild The area occupied by the child gets repainted.
	 */
	public static void repaint(Component comp, Component compChild) {
		repaint(comp, compChild.getBounds());
	}

	/**
	 * translates a point from Component <code>compFrom</code>'s coordinate space
	 * to Component <code>compTo</code>'s coordinate space.
	 * @param p a point relative to <code>compFrom</code>'s left upper corner
	 * @param compFrom component providing the source coordinate space
	 * @param compTo component providing the target coordinate space
	 * @return the translated point
	 */
	public static Point translatedPoint(Point p, Component compFrom, Component compTo) {
		final Point pFromLocation = compFrom.getLocationOnScreen();
		final Point pToLocation = compTo.getLocationOnScreen();

		return new Point(p.x + pFromLocation.x - pToLocation.x, p.y + pFromLocation.y - pToLocation.y);
	}

	/**
	 * translates the origin of Component <code>compFrom</code>'s coordinate space
	 * to Component <code>compTo</code>'s coordinate space.
	 * @return the translated origin
	 */
	public static Point translatedOrigin(Component compFrom, Component compTo) {
		final Point pFromLocation = compFrom.getLocationOnScreen();
		final Point pToLocation = compTo.getLocationOnScreen();

		return new Point(pFromLocation.x - pToLocation.x, pFromLocation.y - pToLocation.y);
	}

	/**
	 * translates the bounds of Component <code>compFrom</code>'s coordinate space
	 * to Component compTo's coordinate space.
	 * @return the translated bounds
	 */
	public static Rectangle translatedBounds(Component compFrom, Component compTo) {
		final Rectangle result = compFrom.getBounds();

		result.setLocation(translatedOrigin(compFrom, compTo));

		return result;
	}

	/**
	 * workaround for bug: mouse button1 isn't recognized in
	 * MOUSE_PRESSED events. Ask button2/button3 states instead.
	 * (button1 pressed <--> !(button2 pressed or button3 pressed))
	 * @return was neither button2 nor button3 pressed?
	 * @todo review this method regarding MouseEvent.BUTTON1_DOWN_MASK and MouseEvent.getButton()
	 */
	public static boolean wa_button1Pressed(MouseEvent ev) {
		return ((ev.getModifiers() & (MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK)) == 0);
	}

	/**
	 * @return Was button2 pressed?
	 * @todo review this method regarding MouseEvent.BUTTON2_DOWN_MASK and MouseEvent.getButton()
	 */
	public static boolean button2Pressed(MouseEvent ev) {
		return ((ev.getModifiers() & MouseEvent.BUTTON2_MASK) != 0);
	}

	/**
	 * @return Was button3 pressed?
	 * @todo review this method regarding MouseEvent.BUTTON3_DOWN_MASK and MouseEvent.getButton()
	 */
	public static boolean button3Pressed(MouseEvent ev) {
		return ((ev.getModifiers() & MouseEvent.BUTTON3_MASK) != 0);
	}

	/**
	 * Centers <code>comp</code> in <code>parent</code>. Useful for displaying subframes/dialogs.
	 * For <code>Window</code>s, an alternative is to use Window.setLocationRelativeTo.
	 * @param bAdjust if true, ensure that location >= (0,0)
	 * todo: may be erroneous, when the "parent" is not really te parent component of comp, i.e. comp has not been added on parent.
	 */
	public static void center(Component comp, Component parent, boolean bAdjust) {
		if(parent == null)
			return;

		final Dimension dimParentSize = parent.getSize();
		final Point pParentLocation = parent.getLocation();

		placeComponent(comp, pParentLocation, dimParentSize, bAdjust);
	}

	/**
	 * Centers a child window or dialog on a JDesktopPane.
	 * If the given parent component is not a JDesktop pane, the generic center method is used.
	 * @param bAdjust if true, ensure that location >= (0,0)
	 */
	public static void centerOnDesktop(Component comp, Component parent, boolean bAdjust) {
		if(!(parent instanceof JDesktopPane)) {
			center(comp, parent, bAdjust);
		}
		else {
			JViewport jvp = (JViewport) parent.getParent();
			final Dimension dimParentSize = jvp.getSize();
			final Point pParentLocation = jvp.getViewPosition();

			placeComponent(comp, pParentLocation, dimParentSize, bAdjust);
		}
	}

	/**
	 * Helper for center methods.
	 * @param comp
	 * @param pParentLocation
	 * @param dimParentSize
	 * @param bAdjust
	 */
	private static void placeComponent(Component comp, Point pParentLocation, Dimension dimParentSize, boolean bAdjust) {
		final Dimension dimCompSize = comp.getSize();
		int x = pParentLocation.x + (dimParentSize.width - dimCompSize.width) / 2;
		int y = pParentLocation.y + (dimParentSize.height - dimCompSize.height) / 2;

		if (bAdjust) {
			x = Math.max(0, x);
			y = Math.max(0, y);
		}

		comp.setLocation(x, y);
	}

	/**
	 * taken from JOptionPane
	 * @return the <code>Window</code> that contains comp
	 * @todo UIUtils.getWindowForComponent(JInternalFrame) doesn't work if the JInternalFrame is minimized.
	 */
	public static Window getWindowForComponent(Component comp) {
		if (comp == null) {
			return JOptionPane.getRootFrame();
		}
		if (comp instanceof Window) {
			return (Window) comp;
		}
		return getWindowForComponent(comp.getParent());
	}

	/**
	 * @return the <code>Frame</code>, if any, that contains <code>comp</code>. null otherwise.
	 */
	public static Frame getFrameForComponent(Component comp) {
		// @todo This violates the specification. Adjust spec or implementation!
		if (comp == null) {
			return JOptionPane.getRootFrame();
		}
		if (comp instanceof Frame) {
			return (Frame) comp;
		}
		return getFrameForComponent(comp.getParent());
	}

	/**
	 * @return the <code>JInternalFrame</code>, if any, containing the given component. <code>null</code> otherwise.
	 */
	public static MainFrameTab getInternalFrameForComponent(Component comp) {
		if (comp == null) {
			return null;
		}
		if (comp instanceof MainFrameTab) {
			return (MainFrameTab) comp;
		}
		return getInternalFrameForComponent(comp.getParent());
	}

	/**
	 * @param comp
	 * @return the innermost internal frame or window (whatever comes first), if any, containing the given component.
	 * <code>null</code> otherwise.
	 */
	public static Component getInternalFrameOrWindowForComponent(JComponent comp) {
		final MainFrameTab ifrm = getInternalFrameForComponent(comp);
		return (ifrm != null) ? ifrm : getWindowForComponent(comp);
	}

	/**
	 * makes sure that <code>comp</code> is as least as big as its minimum size.
	 * Adjusts <code>comp</code>'s size if necessary.
	 * @return Has <code>comp</code>'s size been changed?
	 * @postcondition comp.getSize() >= comp.getMinimumSize()
	 */
	public static boolean ensureMinimumSize(Component comp) {
		return ensureSize(comp, comp.getMinimumSize());
	}

	/**
	 * makes sure that <code>comp</code> is as least as big as the given size.
	 * Adjusts <code>comp</code>'s size if necessary.
	 * @return Has <code>comp</code>'s size been changed?
	 * @postcondition comp.getSize() >= dimMinSize
	 */
	public static boolean ensureSize(Component comp, Dimension dimMinSize) {
		boolean result = false;

		final Dimension dimSize = comp.getSize();

		if (dimSize.width < dimMinSize.width) {
			dimSize.width = dimMinSize.width;
			result = true;
		}
		if (dimSize.height < dimMinSize.height) {
			dimSize.height = dimMinSize.height;
			result = true;
		}

		if (result) {
			comp.setSize(dimSize);
		}
		return result;
	}

	/**
	 * Expands a JTree completely
	 * @param tree
	 * @param expand true means expand, false collapse
	 */
    public static void expandOrCollapseJTree(JTree tree, boolean expand) {
        Object root = tree.getModel().getRoot();

        // Traverse tree from root
        expandOrCollapseJTree(tree, new TreePath(root), expand);
    }

    /**
     * Expands a JTree from a given path.
     * @param tree
     * @param parent
     * @param expand
     */
    public static void expandOrCollapseJTree(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        Object node = parent.getLastPathComponent();
        for (int i = 0; i < tree.getModel().getChildCount(node); i++) {
            Object n = tree.getModel().getChild(node, i);
            TreePath path = parent.pathByAddingChild(n);
            expandOrCollapseJTree(tree, path, expand);
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

	/**
	 * Scrolls a component to the view position of a scrollpane if necessary
	 * @param comp
	 */
	public static void scrollToVisible(Component comp, JScrollPane parent) {
		// todo: optimize this, so that scrolling only happens when necessary (omit flicker)
		/** @todo comp.getBounds().getLocation() =?= comp.getLocation() */
		Point p0 = parent.getViewport().getViewPosition();
		Point p1 = comp.getLocation();
		Rectangle r0 = parent.getViewport().getBounds();
		Rectangle r1 = comp.getBounds();

		if(p0.x > p1.x || p0.y > p1.y
		|| (p0.x < p1.x && (p0.x + r0.width < p1.x + r1.width))
		|| (p0.y < p1.y && (p0.y + r0.height < p1.y + r1.height)))
			parent.getViewport().setViewPosition(comp.getBounds().getLocation());
	}

	public static JToolBar createNonFloatableToolBar(int orientation) {
		final JToolBar result = new NuclosToolBar(orientation);
		result.setFloatable(false);
		return result;
	}

	/**
	 * creates a non-floatable toolbar
	 * @return a new non-floatable toolbar
	 */
	public static JToolBar createNonFloatableToolBar() {
		final JToolBar result = new NuclosToolBar();
		result.setFloatable(false);
		return result;
	}

	/**
	 * convenience method. Sets the wait cursor on the given component.
	 * @param comp
	 */
	public static void setWaitCursor(Component comp) {
		// this is a lot of "cursor" for one wait cursor ;-)
		comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	/**
	 * paints the given component immediately. May only be called in the event dispatching thread.
	 * @param comp
	 */
	public static void paintImmediately(JComponent comp) {
		comp.paintImmediately(0, 0, comp.getWidth(), comp.getHeight());
	}

	/**
	 * finds a <code>JComponent</code> with the given name as direct or indirect successor of the given parent component.
	 * Performs a depth first search.
	 * @param parent
	 * @param sName
	 * @return the component if found; else null
	 * @precondition parent != null
	 */
	public static JComponent findJComponent(JComponent parent, String sName) {
		JComponent result = null;

		for (Iterator<Component> iter = Arrays.asList(parent.getComponents()).iterator(); iter.hasNext() && result == null;)
		{
			final Component compChild = iter.next();
			if (JComponent.class.isAssignableFrom(compChild.getClass())) {
				final JComponent jcompChild = (JComponent) compChild;
				if (jcompChild.getName() != null && jcompChild.getName().equals(sName)) {
					result = jcompChild;
					break;
				}
				else {
					result = findJComponent(jcompChild, sName);
				}
			}
		}
		return result;
	}
	
	/**
	 * finds a <code>JComponent</code> with the given name as direct or indirect successor of the given parent component.
	 * Performs a depth first search.
	 * @param parent
	 * @param sName
	 * @return the component if found; else null
	 * @precondition parent != null
	 */
	public static JComponent findJComponentStartsWithName(JComponent parent, String sName) {
		JComponent result = null;

		for (Iterator<Component> iter = Arrays.asList(parent.getComponents()).iterator(); iter.hasNext() && result == null;)
		{
			final Component compChild = iter.next();
			
			if (JComponent.class.isAssignableFrom(compChild.getClass())) {
				final JComponent jcompChild = (JComponent) compChild;
				if(jcompChild instanceof LabeledComponent) {
					LabeledComponent lc = (LabeledComponent)jcompChild;
					if(!lc.getControlComponent().isVisible())
						continue;
				}
				
				if (jcompChild.getName() != null && jcompChild.getName().startsWith(sName)) {
					result = jcompChild;
					break;
				}
				else {
					result = findJComponentStartsWithName(jcompChild, sName);
				}
				
			}
		}
		return result;
	}

	/**
	 * finds the first <code>JComponent</code> with the given class as direct or indirect successor of the given parent component.
	 * Performs a depth first search.
	 * @param parent
	 * @param clazz
	 * @return the component if found; else null
	 * @precondition parent != null
	 */
	public static JComponent findFirstJComponent(JComponent parent, Class<? extends JComponent> clazz) {
		JComponent result = null;

		for (Iterator<Component> iter = Arrays.asList(parent.getComponents()).iterator(); iter.hasNext() && result == null;)
		{
			final Component compChild = iter.next();
			if (JComponent.class.isAssignableFrom(compChild.getClass())) {
				final JComponent jcompChild = (JComponent) compChild;
//				if (jcompChild.getClass().isAssignableFrom(clazz)) {
				if (clazz.isAssignableFrom(jcompChild.getClass())) {
					result = jcompChild;
					break;
				}
				else {
					result = findFirstJComponent(jcompChild, clazz);
				}
			}
		}
		return result;
	}

	/**
	 * recursively search a component tree for instances of a specified class and return them as a collection.
	 * @param <T>
	 * @param comp
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Component> Collection<T> findAllInstancesOf(Component comp, Class<T> cls) {
		final Collection<T> result;
		if(comp == null) {
			result = Collections.emptySet();
		}
		else {
			result = new LinkedList<T>();
			if (LangUtils.isInstanceOf(comp, cls)) {
				result.add((T) comp);
			}
			if(comp instanceof Container) {
				final Container cont = (Container) comp;
				for (Component compChild : cont.getComponents()) {
					result.addAll(findAllInstancesOf(compChild, cls));
				}
			}
		}
		return result;
	}

	/**
	 * Clears the keymaps of all JComponents of the given class in the hierarchy beneath the parent object.
	 * Performs a depth first search.
	 * @param parent
	 * @param cls
	 */
	public static void clearJComponentKeymap(JComponent parent, Class<? extends JComponent> cls) {
		for (Component compChild : parent.getComponents()) {
			if (JComponent.class.isAssignableFrom(compChild.getClass())) {
				final JComponent jcompChild = (JComponent) compChild;
				if (jcompChild.getClass() == cls) {
					clearKeymaps(jcompChild);
				}
				else {
					clearJComponentKeymap(jcompChild, cls);
				}
			}
		}
	}

	/**
	 * Clears the keymaps of the given JComponent.
	 * @param compChild
	 */
	public static void clearKeymaps(JComponent compChild) {
		SwingUtilities.replaceUIInputMap(compChild, JComponent.WHEN_FOCUSED, null);
		SwingUtilities.replaceUIInputMap(compChild, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
		SwingUtilities.replaceUIInputMap(compChild, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
	}

	/**
	 * defines what happens before and after the execution of a command.
	 * @see UIUtils#runCommand(Component, CommonRunnable)
	 */
	public static interface CommandHandler {
		/**
		 * is executed when a command is started.
		 * @param parent
		 */
		void commandStarted(Component parent);

		/**
		 * is executed when a command is finished.
		 * @param parent
		 */
		void commandFinished(Component parent);
	}

	/**
	 * "null command handler": does nothing. ("Null Object" pattern)
	 */
	public static class NullCommandHandler implements CommandHandler {
		@Override
        public void commandStarted(Component parent) {
		}

		@Override
        public void commandFinished(Component parent) {
		}
	}	// inner class NullCommandHandler

	/**
	 * default command handler: shows a wait cursor during the execution of a command.
	 * May be subclassed to add behavior (such as showing a progress bar etc.).
	 */
	protected static class DefaultCommandHandler implements CommandHandler {
		@Override
        public void commandStarted(Component parent) {
			showWaitCursorForFrame(parent, true);
		}

		@Override
        public void commandFinished(Component parent) {
			showWaitCursorForFrame(parent, false);
		}

	}	// inner class DefaultCommandHandler

	public static void showWaitCursorForFrame(Component parent, boolean bShow) {
		if (parent == null) {
			log.debug("Cannot set wait cursor, as component is null");
			return;
		}

		if (bShow) {
			// Every frame gets its own glasspane, especially every mdi child window
			if (parent instanceof MainFrameTab) {
				((MainFrameTab) parent).lockLayer();
			}
			else if (parent instanceof JFrame) {
				final JFrame frm = (JFrame) parent;
				frm.getGlassPane().setVisible(true);
				UIUtils.setWaitCursor(frm.getGlassPane());
			}
			else {
				UIUtils.setWaitCursor(parent);
			}
		}
		else {
			if (parent instanceof MainFrameTab) {
				((MainFrameTab) parent).unlockLayer();
			}
			else if (parent instanceof JFrame) {
				final JFrame frm = (JFrame) parent;
				frm.getGlassPane().setVisible(false);
				frm.getGlassPane().setCursor(null);
			}
			else {
				parent.setCursor(null);
			}
		}
	}
	
	public static void setupCopyAction(final JTable table) {
		//override copy Action for both tables
		final ActionMap am = new ActionMap();
		am.put("copy", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				final StringBuffer sb = new StringBuffer();

				for (int iSelectedRow : table.getSelectedRows()) {
					sb.append(getColumnData(table, iSelectedRow));
				}
				final StringSelection stsel = new StringSelection(sb.toString());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stsel, stsel);
			}

			@SuppressWarnings("unchecked")
			private StringBuffer getColumnData(final JTable table, final int iSelectedRow) {
				final StringBuffer sb = new StringBuffer();
				
				
				int col[] = table.getSelectedColumns();
				for(int i = 0; i < col.length; i++) {
					sb.append(table.getValueAt(iSelectedRow, col[i]));
					sb.append("\t");
				}
				return sb;
			}
		});
		am.setParent(table.getActionMap());

		table.setActionMap(am);
    }

	/**
	 * @return the CommandHandler used in <code>runCommand[Later]</code>.
	 * @postcondition result != null
	 */
	public static synchronized CommandHandler getCommandHandler() {
		final CommandHandler result = commandhandler;
		assert result != null;
		return result;
	}

	/**
	 * sets the CommandHandler used in <code>runCommand[Later]</code>.
	 * @param ch If <code>null</code>, the default command handler is installed.
	 */
	public static synchronized void setCommandHandler(CommandHandler ch) {
		commandhandler = (ch == null) ? new DefaultCommandHandler() : ch;
	}

	public static <T> T runCommand(Component parent, Callable<T> callable) {
		return runCommand(parent, UIUtils.getCommandHandler(), callable, null);
	}

	public static <T> T runCommand(Component parent, Callable<T> callable, T fallback) {
		return runCommand(parent, UIUtils.getCommandHandler(), callable, fallback);
	}

	public static <T> T runCommand(Component parent, CommandHandler ch, Callable<T> callable, T fallback) {
		try {
			try {
				ch.commandStarted(parent);
				return callable.call();
			}
			finally {
				ch.commandFinished(parent);
			}
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(parent, ex);
		}
		catch (Error error) {
			Errors.getInstance().getCriticalErrorHandler().handleCriticalError(parent, error);
		}
		return fallback;
	}

	/**
	 * Sets the wait cursor for <code>parent</code>, runs the given <code>runnable</code> and
	 * restores the cursor afterwards. Catches any exception and shows it to the user.
	 * @param parent may be <code>null</code>.
	 * @param runnable
	 */
	public static void runCommand(Component parent, CommonRunnable runnable) {
		runCommand(parent, UIUtils.getCommandHandler(), runnable);
	}

	/**
	 * Runs the given <code>runnable</code>. Catches any exception and shows it to the user.
	 * Shows no wait cursor, so it should be used for "short" commands only.
	 * @param parent may be <code>null</code>.
	 * @param runnable
	 */
	public static void runShortCommand(Component parent, CommonRunnable runnable) {
		runCommand(parent, new NullCommandHandler(), runnable);
	}

	/**
	 * Runs the given <code>runnable</code>, performing pre- and post-actions defined by the given <code>CommandHandler</code>.
	 * Catches any exception and shows it to the user.
	 * @param parent may be <code>null</code>.
	 * @param ch
	 * @param runnable
	 */
	public static void runCommand(Component parent, CommandHandler ch, CommonRunnable runnable) {
		try {
			try {
				ch.commandStarted(parent);

				runnable.run();
			}
			finally {
				ch.commandFinished(parent);
			}
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(parent, ex);
		}
		catch (Error error) {
			Errors.getInstance().getCriticalErrorHandler().handleCriticalError(parent, error);
		}
	}

	/**
	 * Sets the wait cursor for <code>parent</code>, runs the given <code>runnable</code> and
	 * restores the cursor afterwards. Catches any exception and shows it to the user.
	 * @param runnable
	 * @param parent may be null.
	 * @see #runCommand(Component, CommonRunnable)
	 */
	public static void runCommand(Component parent, final Runnable runnable) {
		runCommand(parent, new CommonRunnableAdapter(runnable));
	}

	/**
	 * Calls <code>runCommand</code>, but later (using <code>EventQueue.invokeLater</code>).
	 * @param runnable
	 * @param parent may be null.
	 */
	public static void runCommandLater(final Component parent, final CommonRunnable runnable) {
		EventQueue.invokeLater(new Runnable() {
			@Override
            public void run() {
				runCommand(parent, runnable);
			}
		});
	}

	/**
	 * Calls <code>runShortCommand</code>, but later (using <code>EventQueue.invokeLater</code>).
	 * Shows no wait cursor, so it should be used for "short" commands only.
	 * @param runnable
	 * @param parent may be null.
	 */
	public static void runShortCommandLater(final Component parent, final CommonRunnable runnable) {
		EventQueue.invokeLater(new Runnable() {
			@Override
            public void run() {
				runShortCommand(parent, runnable);
			}
		});
	}

	/**
	 * Calls <code>runCommand</code>, but later (using <code>EventQueue.invokeLater</code>).
	 * Shows no wait cursor, so it should be used for "short" commands only.
	 * @param runnable
	 * @param parent may be null.
	 * @see #runCommandLater(Component, CommonRunnable)
	 */
	public static void runCommandLater(Component parent, final Runnable runnable) {
		runCommandLater(parent, new CommonRunnableAdapter(runnable));
	}

	/**
	 * Calls <code>runShortCommand</code>, but later (using <code>EventQueue.invokeLater</code>).
	 * @param runnable
	 * @param parent may be null.
	 * @see #runCommandLater(Component, CommonRunnable)
	 */
	public static void runShortCommandLater(Component parent, final Runnable runnable) {
		runShortCommandLater(parent, new CommonRunnableAdapter(runnable));
	}

	/**
	 * sets the font style of the given component according to <code>iFontStyle</code>.
	 * @param comp
	 * @param iFontStyle
	 */
	public static void setFontStyle(Component comp, int iFontStyle) {
		comp.setFont(comp.getFont().deriveFont(iFontStyle));
	}

	/**
	 * sets the font style of the given component to BOLD or PLAIN, depending on <code>bBold</code>.
	 * Useful to distinguish the default action from other actions in popup menus.
	 * @param comp
	 * @param bBold
	 */
	public static void setFontStyleBold(Component comp, boolean bBold) {
		setFontStyle(comp, bBold ? Font.BOLD : Font.PLAIN);
	}

	/**
	 * @param menu
	 * @param sActionCommand
	 * @return the first <code>JMenuItem</code> in the given menu with the given actionCommand, if any.
	 */
	public static JMenuItem findMenuItemByActionCommand(JMenu menu, String sActionCommand) {
		for (Component comp : menu.getMenuComponents()) {
			if (comp instanceof JMenuItem) {
				final JMenuItem mi = (JMenuItem) comp;
				if (sActionCommand.equals(mi.getActionCommand())) {
					return mi;
				}
			}
		}
		return null;
	}

	/**
	 * @param comp
	 * @return the maximum visible y-value in the component.
	 */
	public static int getMaxVisibleY(JComponent comp) {
		final Rectangle rect = comp.getVisibleRect();
		return rect.y + rect.height - 1;
	}

	/**
	 * @param comp
	 * @return comp.getMinimumSize().width
	 */
	public static int getMinimumWidth(JComponent comp) {
		return comp.getMinimumSize().width;
	}

	/**
	 * @param comp
	 * @return comp.getMinimumSize().height
	 */
	public static int getMinimumHeight(JComponent comp) {
		return comp.getMinimumSize().height;
	}

	/**
	 * @param comp
	 * @return comp.getPreferredSize().width
	 */
	public static int getPreferredWidth(JComponent comp) {
		return comp.getPreferredSize().width;
	}

	/**
	 * @param comp
	 * @return comp.getPreferredSize().height
	 */
	public static int getPreferredHeight(JComponent comp) {
		return comp.getPreferredSize().height;
	}

	/**
	 * @param comp
	 * @return comp.getMaximumSize().width
	 */
	public static int getMaximumWidth(JComponent comp) {
		return comp.getMaximumSize().width;
	}

	/**
	 * @param comp
	 * @return comp.getMaximumSize().height
	 */
	public static int getMaximumHeight(JComponent comp) {
		return comp.getMaximumSize().height;
	}

	/**
	 * sets the minimum width of the given component (without changing its minimum height).
	 * @param comp
	 * @param iWidth
	 */
	public static void setMinimumWidth(JComponent comp, int iWidth) {
		comp.setMinimumSize(new Dimension(iWidth, getMinimumHeight(comp)));
	}

	/**
	 * sets the minimum height of the given component (without changing its minimum width).
	 * @param comp
	 * @param iHeight
	 */
	public static void setMinimumHeight(JComponent comp, int iHeight) {
		comp.setMinimumSize(new Dimension(getMinimumWidth(comp), iHeight));
	}

	/**
	 * sets the preferred width of the given component (without changing its preferred height).
	 * @param comp
	 * @param iWidth
	 */
	public static void setPreferredWidth(JComponent comp, int iWidth) {
		comp.setPreferredSize(new Dimension(iWidth, getPreferredHeight(comp)));
	}

	/**
	 * sets the preferred height of the given component (without changing its preferred width).
	 * @param comp
	 * @param iHeight
	 */
	public static void setPreferredHeight(JComponent comp, int iHeight) {
		comp.setPreferredSize(new Dimension(getPreferredWidth(comp), iHeight));
	}

	/**
	 * sets the maximum width of the given component (without changing its maximum height).
	 * @param comp
	 * @param iWidth
	 */
	public static void setMaximumWidth(JComponent comp, int iWidth) {
		comp.setMaximumSize(new Dimension(iWidth, getMaximumHeight(comp)));
	}

	/**
	 * sets the maximum height of the given component (without changing its maximum width).
	 * @param comp
	 * @param iHeight
	 */
	public static void setMaximumHeight(JComponent comp, int iHeight) {
		comp.setMaximumSize(new Dimension(getMaximumWidth(comp), iHeight));
	}

	/**
	 * @param comp
	 * @postcondition LangUtils.equals(comp.getMinimumSize(), comp.getPreferredSize())
	 */
	public static void setMinimumSizeToPreferredSize(JComponent comp) {
		comp.setMinimumSize(comp.getPreferredSize());

		assert LangUtils.equals(comp.getMinimumSize(), comp.getPreferredSize());
	}

	/**
	 * @param comp
	 * @postcondition LangUtils.equals(comp.getMaximumSize(), comp.getPreferredSize())
	 */
	public static void setMaximumSizeToPreferredSize(JComponent comp) {
		comp.setMaximumSize(comp.getPreferredSize());

		assert LangUtils.equals(comp.getMaximumSize(), comp.getPreferredSize());
	}

	/**
	 * @return Is the current Look&Feel Windows Look&Feel?
	 */
	public static boolean isWindowsLookAndFeel() {
		try {
			final Class<?> clsLaf = Class.forName("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			// UIManager.getLookAndFeel() instanceof WindowsLookAndFeel ?
			return clsLaf.isAssignableFrom(UIManager.getLookAndFeel().getClass());
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}
	
	/**
	 * return false if Component is not editable, otherwise true
	 */
	public static boolean isEditable(Component comp) {
		if(comp instanceof JTextComponent) {
			return ((JTextComponent)comp).isEditable();
		}
		else if(comp instanceof JComboBox) {
			return ((JComboBox)comp).isEditable();
		}
		
		else 
			return true;
	}

	/**
	 * @return Is the current Look&Feel the Classic Windows (Windows95) Look&Feel?
	 */
	public static boolean isClassicWindowsLookAndFeel() {
		return UIUtils.isWindowsLookAndFeel() && !UIUtils.isXP();
	}

	/**
	 * @return Is the current Look&Feel the Windows XP Look&Feel?
	 */
	public static boolean isWindowsXPLookAndFeel() {
		return UIUtils.isWindowsLookAndFeel() && UIUtils.isXP();
	}

	private static boolean isXP() {
		return Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive"));
	}

	/**
	 * removes all action listeners from the given button
	 * @param btn
	 */
	public static void removeAllActionListeners(AbstractButton btn) {
		for (ActionListener al : btn.getActionListeners()) {
			btn.removeActionListener(al);
		}
	}

	/**
	 * removes all mouse listeners from the given component.
	 * @param comp
	 */
	public static void removeAllMouseListeners(Component comp) {
		for (MouseListener ml : comp.getMouseListeners()) {
			comp.removeMouseListener(ml);
		}
	}

	/**
	 * @param sParentName may be <code>null</code>.
	 * @param sChildName
	 * @return a combined name for a component, consisting of the given parent and child names.
	 * Useful for GUI testing of composite components.
	 * @precondition sChildName != null
	 * @postcondition (sParentName == null) --> (result == null)
	 * @postcondition (sParentName != null) --> (result.equals(sParentName + "." + sChildName)
	 */
	public static String getCombinedName(String sParentName, String sChildName) {
		return (sParentName == null) ? null : sParentName + "." + sChildName;
	}

	/**
	 * sets a combined name for the given component. Useful for GUI testing of composite components.
	 * @param comp
	 * @param sParentName
	 * @param sChildName
	 * @see #getCombinedName(String, String)
	 */
	public static void setCombinedName(Component comp, String sParentName, String sChildName) {
		comp.setName(getCombinedName(sParentName, sChildName));
	}

	/**
	 * invokes the given runnable (thread) on the dispatch thread
	 * @param runnable
	 */
	public static void invokeOnDispatchThread(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			try {
				EventQueue.invokeAndWait(runnable);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static <T> T invokeOnDispatchThread(Callable<T> callable) {
      if (EventQueue.isDispatchThread()) {
         try {
            return callable.call();
         } catch (RuntimeException e) {
            throw e;
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      } else {
         try {
            CallableRunner<T> runner = new CallableRunner<T>(callable);
            EventQueue.invokeAndWait(runner);
            return runner.get();
         } catch (InterruptedException e) {
            throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
         }
      }
   }

   private static class CallableRunner<T> implements Runnable {

      private final Callable<T> callable;
      private T result;
      private Throwable exception;

      public CallableRunner(Callable<T> callable) {
         this.callable = callable;
      }

      public T get() throws InvocationTargetException {
         if (exception != null) {
            throw new InvocationTargetException(exception);
         }
         return result;
      }

      @Override
    public void run() {
         try {
            result = callable.call();
         } catch (Exception e) {
            exception = e;
         }
      }
   }

	/**
	 * cleans up the toolbars containing in the given component
	 */
	public static void cleanUpToolBar(JComponent comp) {
		for (Component compToolBar : comp.getComponents()) {
			if (compToolBar instanceof JToolBar) {
				cleanUpToolBar((JToolBar)compToolBar);
			}
		}
	}

	/**
	 * cleans up the toolbar, that means unnecessary separators
	 * will be set invisible
	 */
	public static void cleanUpToolBar(JToolBar toolbar) {
		Component firstVisibleComponent = null;
		Component lastVisibleComponent = null;

		int iCompCount = toolbar.getComponentCount();

		for (int idx_all=0 ; idx_all<iCompCount ; idx_all++) {
			// remember first and last visible component
			if (toolbar.getComponent(idx_all).isVisible()) {
				if (firstVisibleComponent == null) {
					firstVisibleComponent = toolbar.getComponent(idx_all);
				}
				else {
					lastVisibleComponent = toolbar.getComponent(idx_all);
				}
			}

			// check whether the next visible component after a separator is a separator again
			if (toolbar.getComponent(idx_all).isVisible() && toolbar.getComponent(idx_all) instanceof JSeparator) {
				int idx_visible;
				for (idx_visible=idx_all+1 ; idx_visible<iCompCount ; idx_visible++) {
					if (toolbar.getComponent(idx_visible).isVisible()) {
						if (toolbar.getComponent(idx_visible) instanceof JSeparator) {
							toolbar.getComponent(idx_visible).setVisible(false);
						}
						else {
							break;
						}
					}
				}
				idx_all = idx_visible;
			}
		}

		// check whether the first visible component is a separator
		if (firstVisibleComponent != null && firstVisibleComponent instanceof JSeparator) {
			firstVisibleComponent.setVisible(false);
		}
		// check whether the last visible component is a separator
		else if (lastVisibleComponent != null && lastVisibleComponent instanceof JSeparator) {
			lastVisibleComponent.setVisible(false);
		}
	}

	public static BufferedImage toBufferedImage(Image image) {
		if(image instanceof BufferedImage)
			return (BufferedImage) image;

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();
		// Determine if the image has transparent pixels
		boolean hasAlpha = hasAlpha(image);
		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if(hasAlpha)
				transparency = Transparency.BITMASK;

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		}
		catch(HeadlessException e) {} //No screen

		if(bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha == true) {type = BufferedImage.TYPE_INT_ARGB;}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if(image instanceof BufferedImage)
			return ((BufferedImage) image).getColorModel().hasAlpha();

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		}
		catch(InterruptedException e) {}

		// Get the image's color model
		return pg.getColorModel() != null && pg.getColorModel().hasAlpha();
	}

	public static Image iconToImage(Icon icon) {
		if (icon instanceof ImageIcon) {
			return ((ImageIcon)icon).getImage();
		} else {
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			BufferedImage image = gc.createCompatibleImage(w, h);
			Graphics2D g = image.createGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
			return image;
		}
	}

	public static void enableComponentsInContainer(Container c) {
		Component[] compArray = c.getComponents();
		for (int i = 0; i < compArray.length; i++) {
			setComponentEnabled(compArray[i], true);
			if (compArray[i] instanceof Container) {
				enableComponentsInContainer((Container) compArray[i]);
			}
		}
	}

	public static void disableComponentsInContainer(Container c) {
		Component[] compArray = c.getComponents();
		for (int i = 0; i < compArray.length; i++) {
			setComponentEnabled(compArray[i], false);
			if (compArray[i] instanceof Container) {
				disableComponentsInContainer((Container) compArray[i]);
			}

		}
	}

	public static void setComponentEnabled(Component c, boolean b) {
		if (c instanceof AbstractButton && ((AbstractButton) c).getAction() != null)
			((AbstractButton) c).getAction().setEnabled(b);
		else
			c.setEnabled(b);
	}

	public static Box createHorizontalBox(Object...children) {
		return createBox(BoxLayout.X_AXIS, children);
	}

	public static Box createVerticalBox(Object...childdren) {
		return createBox(BoxLayout.Y_AXIS, childdren);
	}

	private static Box createBox(int axis, Object...children) {
		if (axis != BoxLayout.X_AXIS && axis != BoxLayout.Y_AXIS)
			throw new IllegalArgumentException();
		boolean horizontal = (axis == BoxLayout.X_AXIS);
		Box box = new Box(axis);
		for (Object child : children) {
			if (child instanceof Component) {
				box.add((Component) child);
			} else if (child instanceof Dimension) {
				box.add(Box.createRigidArea((Dimension) child));
			} else if (child == null) {
				box.add(horizontal ? Box.createHorizontalGlue() : Box.createVerticalGlue());
			} else if (child instanceof Integer) {
				int strut = (Integer) child;
				box.add(horizontal ? Box.createHorizontalStrut(strut) : Box.createVerticalStrut(strut));
			} else {
				throw new ClassCastException("Child must be component or invisible box specification (strut=integer, rigid area=dimension, glue=null)");
			}
		}
		return box;
	}
	
	public static void copyCells(JTable table) {		
		StringBuffer sb = new StringBuffer();
		int row = table.getSelectedRow();
		for(int col : table.getSelectedColumns()) {
			sb.append(table.getValueAt(row, col));
			sb.append("\t");
		}
		final StringSelection stsel = new StringSelection(sb.toString());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stsel, stsel);
	}
	
	public static void copyRows(JTable table) {
		
		StringBuffer sb = new StringBuffer();
		for (int iSelectedRow : table.getSelectedRows()) {
			sb.append(getColumnData(table, iSelectedRow));
			sb.append("\n");
		}
		
		final StringSelection stsel = new StringSelection(sb.toString());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stsel, stsel);
	}
	
	private static StringBuffer getColumnData(final JTable table, final int iSelectedRow) {
		final int iColumnCount = table.getColumnCount();
		final StringBuffer sb = new StringBuffer();
		for (int iColumn = 0; iColumn < iColumnCount; iColumn++) {
			sb.append(table.getValueAt(iSelectedRow, iColumn));
			sb.append("\t");
		}
		return sb;
	}


	public static StatusBarPanel newStatusBar(Component...comps) {
		final StatusBarPanel result = new StatusBarPanel();
		for (int i = 0; i < comps.length; i++) {
			result.add(comps[i], null);
		}
		return result;
	}

	public static Image resizeImage(Image img, int width, int height) {
		return img.getScaledInstance(width, height,  java.awt.Image.SCALE_SMOOTH);
	}

	public static ImageIcon resizeImageIcon(ImageIcon imgico, int width, int height) {
		Image img = imgico.getImage();
		Image newimg = img.getScaledInstance(width, height,  java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(newimg);
	}

	public static String getRendererText(Component renderer) {
		if (renderer instanceof JLabel) {
			JLabel label = (JLabel) renderer;
			View view = (View) label.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
			// If HTML is enabled, try to extract the plain text (without markup) from the underlying view
			if (view != null) {
				Document document = view.getDocument();
				if (document != null) {
					try {
						return document.getText(0, document.getLength());
					} catch (BadLocationException ex) {
						// ignore
					}
				}
			}
			return label.getText();
		}
		return null;
	}

	/**
	 *
	 * @param window
	 * @param opacity 0.0f - 1.0f
	 */
	public static void setWindowOpacity(Window window, float opacity) {
		try {
			Class<?> sunAwtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
			Method setWindowOpaqueMethod = sunAwtUtilitiesClass.getMethod("setWindowOpaque", Window.class, boolean.class);
			Method setWindowOpacityMethod = sunAwtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);

			setWindowOpaqueMethod.invoke(null, window, false);
			setWindowOpacityMethod.invoke(null, window, opacity);
		} catch (Exception ex) {
			log.debug("com.sun.awt.AWTUtilities not avaiable or error during method calls: ", ex);
		}
	}

	public static void writeSplitPaneStateToPrefs(Preferences prefsParent, Component rootComp) {
		Collection<JSplitPane> splitpanes = findAllInstancesOf(rootComp, JSplitPane.class);
		for (JSplitPane pane : splitpanes) {
			Preferences prefs = prefsParent.node(rootComp.getName());
			String pname = pane.getName();
			if(pname == null) {
				// Plan B -> Name wird generiert als Folge von Indices ab rootComp
				StringBuilder sb = new StringBuilder();
				Component c = pane;
				while(c != null && c != rootComp) {
					Container parent = c.getParent();
					if(c instanceof JSplitPane) {
						int idx = -1;
						for(int i = 0, n = parent.getComponentCount(); i < n; i++)
							if(parent.getComponent(i) == c)
								idx = i;
						sb.insert(0, "#" + idx);
					}
					c = parent;
				}
				pname = sb.toString();
			}
			if(pname != null) {
				PreferencesUtils.writeSplitPaneStateToPrefs(prefs, pname, pane);
			}
		}
	}

	public static void readSplitPaneStateFromPrefs(Preferences prefsParent, Component rootComp) {
		Collection<JSplitPane> splitpanes = findAllInstancesOf(rootComp, JSplitPane.class);
		for (JSplitPane pane : splitpanes) {
			Preferences prefs = prefsParent.node(rootComp.getName());
			String pname = pane.getName();
			if(pname == null) {
				// Plan B -> Name wird generiert als Folge von Indices ab rootComp
				StringBuilder sb = new StringBuilder();
				Component c = pane;
				while(c != null && c != rootComp) {
					Container parent = c.getParent();
					if(c instanceof JSplitPane) {
						int idx = -1;
						for(int i = 0, n = parent.getComponentCount(); i < n; i++)
							if(parent.getComponent(i) == c)
								idx = i;
						sb.insert(0, "#" + idx);
					}
					c = parent;
				}
				pname = sb.toString();
			}
			if(pname != null) {
				//Preferences subPrefs = prefs.node(pname);
				PreferencesUtils.readSplitPaneStateFromPrefs(prefs, pname, pane);
				//pane.setOrientation(subPrefs.getInt(PREFS_KEY_ORIENTATION,pane.getOrientation()));
			}
		}
	}
}	// class UIUtils
