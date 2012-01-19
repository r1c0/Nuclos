package org.nuclos.client.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.nuclos.common2.StringUtils;

public class PopupButton extends JToggleButton implements PopupMenuListener, ActionListener {

	public final JPopupMenu popupMenu;

	private long hideTime = 0l;

	private final MouseListener itemMouseListener = new MouseAdapter() {
		@Override
		public void mouseReleased(MouseEvent e) {
			popupMenu.setVisible(false);
		}
	};

	public PopupButton() {
		this("");
	}
	
	public PopupButton(String text) {
		super(text);
		setSelected(false);
		setHorizontalAlignment(SwingConstants.LEFT);

		popupMenu = new JPopupMenu() {

			@Override
			protected JMenuItem createActionComponent(Action a) {
				JMenuItem result = super.createActionComponent(a);
				result.addMouseListener(itemMouseListener);
				return result;
			}
		};

		popupMenu.addPopupMenuListener(this);
		addActionListener(this);
	}

	public void addSeparator() {
		popupMenu.addSeparator();
	}

	public JMenuItem add(JMenuItem menuItem) {
		menuItem.addMouseListener(itemMouseListener);
		return popupMenu.add(menuItem);
	}

	@Override
	public Component add(Component comp) {
		comp.addMouseListener(itemMouseListener);
		return popupMenu.add(comp);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension defaultPrefSize = super.getPreferredSize();
		return new Dimension(
				defaultPrefSize.width
				+ Icons.getInstance().getArrowButtonX().getIconWidth()
				+ (StringUtils.looksEmpty(getText())?-4:1), 
				defaultPrefSize.height
				+ (StringUtils.looksEmpty(getText())?Icons.getInstance().getArrowButtonX().getIconHeight()-2:0));
	}

	@Override
	public Dimension getMaximumSize() {
		Dimension defaultMaxSize = super.getMaximumSize();
		return new Dimension(
				defaultMaxSize.width
				+ Icons.getInstance().getArrowButtonX().getIconWidth()
				+ (StringUtils.looksEmpty(getText())?-4:1), 
				defaultMaxSize.height
				+ (StringUtils.looksEmpty(getText())?Icons.getInstance().getArrowButtonX().getIconHeight()-2:0));
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension defaultMinSize = super.getMinimumSize();
		return new Dimension(
				defaultMinSize.width
				+ Icons.getInstance().getArrowButtonX().getIconWidth()
				+ (StringUtils.looksEmpty(getText())?-4:1), 
				defaultMinSize.height
				+ (StringUtils.looksEmpty(getText())?Icons.getInstance().getArrowButtonX().getIconHeight()-2:0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!popupMenu.isShowing()) {
			if (hideTime + 200l < System.currentTimeMillis()) {
				popupMenu.show(PopupButton.this, 0, PopupButton.this.getHeight());
			} else {
				PopupButton.this.setSelected(false);
			}
		}
		else {
			popupMenu.setVisible(false);
		}
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		setSelected(false);
		hideTime = System.currentTimeMillis();
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		for (int i = 0; i < popupMenu.getComponentCount(); i++) {
			Component c = popupMenu.getComponent(i);
			if (c instanceof AbstractButton) {
				AbstractButton btn = (AbstractButton) c;
				if (btn.getAction() != null) {
					btn.setEnabled(btn.getAction().isEnabled());
				}
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;

		ImageIcon bg = Icons.getInstance().getArrowButtonX();
		ImageIcon ar = Icons.getInstance().getArrowButtonDown();

		int xBg = getWidth() - 2 - bg.getIconWidth();
		int yBg = 3;
		int xAr = xBg + (bg.getIconWidth() - ar.getIconWidth()) / 2;
		int yAr = yBg + (bg.getIconHeight() - ar.getIconHeight()) / 2;

		g2.drawImage(bg.getImage(), xBg, yBg, null);
		g2.drawImage(ar.getImage(), xAr, yAr, null);
	}


	public int getItemCount() {
		return popupMenu.getComponentCount();
	}
}
