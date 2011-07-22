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
package org.nuclos.client.statemodel.panels.rights;

import info.clearthought.layout.TableLayout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nuclos.client.statemodel.panels.rights.RightTransfer.RoleRight;
import org.nuclos.client.ui.Icons;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * 
 * Represents one role column header
 */
public class RightAndMandatoryColumnHeader implements RightAndMandatoryConstants {

	Integer role;
	String name;
	
	boolean selected;
	SelectionListener selectionListener;
	MultiEditListener multiEditListener;
	RightTransfer rightTransfer;
	
	ChangeListener detailsChangedListener;
	
	MouseListener selectionMouseListener = new MouseListener() {
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				if (multiEditListener != null) {
					final JPopupMenu menu = new JPopupMenu();
					JMenuItem itemNull = new JMenuItem(CommonLocaleDelegate.getMessage("RightAndMandatory.1", "Alle nicht sichtbar"), ICON_NO_RIGHT);
					itemNull.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							multiEditListener.actionPerformed(new ActionEvent(new MultiEditListener.Holder(null), role, MultiEditListener.COMMAND_SET_ROLE_RIGHT));
						}
					});
					menu.add(itemNull);
					JMenuItem itemRead = new JMenuItem(CommonLocaleDelegate.getMessage("RightAndMandatory.2", "Alle lesen"), ICON_READ);
					itemRead.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							multiEditListener.actionPerformed(new ActionEvent(new MultiEditListener.Holder(new Boolean(false)), role, MultiEditListener.COMMAND_SET_ROLE_RIGHT));
						}
					});
					menu.add(itemRead);
					JMenuItem itemWrite = new JMenuItem(CommonLocaleDelegate.getMessage("RightAndMandatory.3", "Alle schreiben"), ICON_WRITE);
					itemWrite.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							multiEditListener.actionPerformed(new ActionEvent(new MultiEditListener.Holder(new Boolean(true)), role, MultiEditListener.COMMAND_SET_ROLE_RIGHT));
						}
					});
					menu.add(itemWrite);
					menu.addSeparator();
					JMenuItem itemCopy = new JMenuItem(CommonLocaleDelegate.getMessage("RightAndMandatory.4", "Kopieren"), Icons.getInstance().getIconCopy16());
					itemCopy.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							copyToClipboard();
						}
					});
					menu.add(itemCopy);
					JMenuItem itemPaste = new JMenuItem(CommonLocaleDelegate.getMessage("RightAndMandatory.5", "Einfügen"), Icons.getInstance().getIconPaste16());
					itemPaste.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							pasteFromClipboard();
						}
					});
					menu.add(itemPaste);
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			} 
			
			view.nameLabel.grabFocus();
			if (selectionListener != null) {
				if (SwingUtilities.isRightMouseButton(e)) {
					if (!selected) {
						selectionListener.actionPerformed(new ActionEvent(RightAndMandatoryColumnHeader.this, role, SelectionListener.COMMAND_SELECT));
					}
				} else {
					selectionListener.actionPerformed(new ActionEvent(RightAndMandatoryColumnHeader.this, role, 
						selected ? SelectionListener.COMMAND_DESELECT : SelectionListener.COMMAND_SELECT));
				}
			}
		}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseClicked(MouseEvent e) {}		
		
	};
	
	/**
	 * 
	 * @return name of role
	 */
	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * 
	 * @param role
	 * @param name
	 * @param detailsChangedListener
	 */
	public RightAndMandatoryColumnHeader(Integer role, String name, ChangeListener detailsChangedListener) {
		super();
		this.role = role;
		this.name = name;
		this.detailsChangedListener = detailsChangedListener;
	}
	
	/**
	 * 
	 * @return id of role
	 */
	public Integer getId() {
		return this.role;
	}
	
	/**
	 * 
	 * @return name of role
	 */
	public String getName() {
		return this.name;
	}
	
	View view = null;
	
	/**
	 * 
	 * @param namewidth
	 * @return
	 */
	public View initView(double namewidth){
		if (view == null) {
			view = new View(namewidth);
		}
		return view;
	}
	
	/**
	 * 
	 * @param sl
	 */
	public void setSelectionListener(SelectionListener sl) {
		selectionListener = sl;
	}
	
	/**
	 * 
	 * @param mel
	 */
	public void setMultieditListener(MultiEditListener mel) {
		multiEditListener = mel;
	}
	
	/**
	 * 
	 * @param rt
	 */
	public void setRightTransfer(RightTransfer rt) {
		rightTransfer = rt;
	}

	/**
	 * 
	 * main JPanel for this column
	 */
	public class View extends JPanel{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		NameLabel nameLabel;
		
		/**
		 * 
		 * @param namewidth
		 */
		public View(double namewidth) {
			final double size [][] = {new double[] {CELL_WIDTH}, 
									  new double[] {
													CELL_HEIGHT,
													namewidth}};
			final TableLayout layout = new TableLayout(size);
			
			setLayout(layout);
			setBackground(COLOR_BACKGROUND);
			
			Marker marker = new Marker();
			marker.setBackground(COLOR_BACKGROUND);
			add(marker, "0,0");
			
			nameLabel = new NameLabel(name, namewidth);
			nameLabel.setBackground(COLOR_BACKGROUND);
			add(nameLabel, "0,1");
		}
		
		protected NameLabel getNameLabel() {
			return nameLabel;
		}
	}
	
	/**
	 * 
	 * @param namewidth
	 */
	public void updateView(double namewidth) {
		if (view != null) {
			((TableLayout)view.getLayout()).setRow(1, namewidth);
			view.nameLabel.setNamewidth(namewidth);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int getColumnNameHeight() {
		if (view != null)
			return view.getNameLabel().getPreferredSize().width;
		else 
			return 0;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * 
	 * @param selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
		if (view != null) {
			view.revalidate();
			view.repaint();
		}
	}

	private void copyToClipboard() {
		if (view != null) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(view.nameLabel, null);
		}
	}
	
	private void pasteFromClipboard() {
		Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard(); 
		Transferable transfer = sysClip.getContents(null); 
		if (selected && rightTransfer != null) {
			try {
				Object transferO = transfer.getTransferData(RightTransfer.OneRoleRightsDataFlavor.flavor);
				if (transferO instanceof RoleRight)
					rightTransfer.setRoleRight((RoleRight) transferO);
				detailsChangedListener.stateChanged(new ChangeEvent(this));
			}
			catch(UnsupportedFlavorException e) {}
			catch(IOException e) {}
		}
	}
	
	/**
	 * 
	 * horizontal name label
	 */
	public class NameLabel extends JLabel implements Transferable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private boolean needsRotate;
		
		private int namewidth;
		
		private Color defaultColor;
		
		public NameLabel(String text, double namewidth) {
			super(text);
			this.namewidth = Double.valueOf(namewidth).intValue();
			this.addMouseListener(selectionMouseListener);
			this.defaultColor = this.getForeground();
			this.setFocusable(true);
			
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent ev) {
					if (ev.getKeyCode() == KeyEvent.VK_C && ev.isControlDown()) {
						copyToClipboard();
					} else if (ev.getKeyCode() == KeyEvent.VK_V && ev.isControlDown()) {
						pasteFromClipboard();
					}
				}
			});
			
			// disable default handler
			setTransferHandler(new TransferHandler() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				@Override
				public boolean importData(JComponent comp, Transferable t) {
					return true;
				}
				@Override
				public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
					return false;
				}
			});
		}
		
		private void setNamewidth(double namewidth) {
			this.namewidth = Double.valueOf(namewidth).intValue();
			revalidate();
			repaint();
		}
		
		@Override
		public Dimension getPreferredSize() {
			if (!needsRotate) {
			    return super.getPreferredSize();
			}
			
			Dimension size = super.getPreferredSize();
			return new Dimension(size.height, size.width);
		}

		@Override
		public Dimension getSize() {
			if (!needsRotate) {
				return super.getSize();
			}
			
			Dimension size = super.getSize();
			return new Dimension(size.height, size.width);
		}

		@Override
		public int getHeight() {
			return getSize().height;
		}

		@Override
		public int getWidth() {
			return getSize().width;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D gr = (Graphics2D) g.create();

			gr.transform(AffineTransform.getQuadrantRotateInstance(1));
			gr.translate(0, -getSize().getWidth());

			needsRotate = true;
			
			Graphics2D g2d = (Graphics2D) g;
			
			if (selected) {
				setForeground(Color.WHITE);
				g2d.setColor(COLOR_SELECTION_BACKGROUND);
				g2d.fillRect(0, 0, CELL_WIDTH, namewidth);
			} else {
				setForeground(defaultColor);
			}
			
			super.paintComponent(gr);
			
			g2d.setStroke(new BasicStroke(1.f));
			g2d.setColor(COLOR_GRID);
			g2d.drawLine(0, 0, 0, namewidth-1);
			g2d.drawLine(0, namewidth-1, CELL_WIDTH-1, namewidth-1);
			g2d.drawLine(CELL_WIDTH-1, namewidth-1, CELL_WIDTH-1, 0);
			needsRotate = false;
		}
		
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{RightTransfer.OneRoleRightsDataFlavor.flavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor instanceof RightTransfer.OneRoleRightsDataFlavor;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (rightTransfer != null)
				return rightTransfer.getRoleRight(role);
			return null;
		}
	}
	
	/**
	 * 
	 * rectangle in top of name
	 */
	public class Marker extends JLabel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Marker() {
			addMouseListener(selectionMouseListener);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			
			g2d.setColor(COLOR_MARKER_BACKGROUND);
			g2d.fillRect(0, 0, CELL_WIDTH, CELL_HEIGHT);
			
			g2d.setStroke(new BasicStroke(1.f));
			g2d.setColor(COLOR_MARKER_GRID);
			g2d.drawLine(0, 0, CELL_WIDTH-1, 0);
			g2d.drawLine(CELL_WIDTH-1, 0, CELL_WIDTH-1, CELL_HEIGHT-1);
			g2d.drawLine(CELL_WIDTH-1, CELL_HEIGHT-1, 0, CELL_HEIGHT-1);
			g2d.drawLine(0, CELL_HEIGHT-1, 0, 0);
		}

	}
	
	public static class Comparator implements java.util.Comparator<RightAndMandatoryColumnHeader> {
		@Override
		public int compare(RightAndMandatoryColumnHeader o1, RightAndMandatoryColumnHeader o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
}
