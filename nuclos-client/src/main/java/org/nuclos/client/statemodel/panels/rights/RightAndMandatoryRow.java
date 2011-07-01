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
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nuclos.client.ui.Icons;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * 
 * represents one row for a group (subform) or attribute (column)
 */
public class RightAndMandatoryRow implements RightAndMandatoryConstants{

	final Integer id;
	final String name;
	
	Map<Integer, Boolean> roleRights = new HashMap<Integer, Boolean>();
	List<Integer> sortOrder = new ArrayList<Integer>();
	boolean mandatory = false;
	boolean metaMandatory = false;
	boolean rightsEnabled = false;
	boolean collapsed = false;
	boolean nullRightAllowed = true;
	boolean subform = false;
	RightAndMandatoryRow group = null;
	
	ExpandCollapseListener expandCollapseListener;
	SelectionListener selectionListener;
	
	ChangeListener detailsChangedListener;
	
	/**
	 * 
	 * @param id
	 * @param name
	 * @param rightsEnabled
	 * @param roleRights
	 * @param nullRightAllowed
	 * @param mandatory
	 * @param sortOrder
	 * @param group
	 * @param subform
	 * @param detailsChangedListener
	 */
	public RightAndMandatoryRow(Integer id, String name, boolean rightsEnabled, Map<Integer, Boolean> roleRights, boolean nullRightAllowed, boolean mandatory, boolean metaMandatory, List<Integer> sortOrder, RightAndMandatoryRow group, boolean subform, ChangeListener detailsChangedListener) {
	    super();
	    this.id = id;
	    this.name = name;
	    this.rightsEnabled = rightsEnabled;
	    this.roleRights = roleRights;
	    this.nullRightAllowed = nullRightAllowed;
	    this.sortOrder = sortOrder;
	    this.mandatory = mandatory;
	    this.metaMandatory = metaMandatory;
	    this.group = group;
	    this.subform = subform;
	    this.detailsChangedListener = detailsChangedListener;
	    respectNullRightAllowed();
	    if (metaMandatory)
	    	this.mandatory = false;
    }
	
	View view = null;
	
	/**
	 * 
	 * @param namewidth initial value
	 * @return JPanel
	 */
	public View initView(double namewidth) {
		if (view == null) {
			view = new View(namewidth);
		}
		return view;
	}
	
	/**
	 * 
	 * @param namewidth
	 */
	public void updateView(double namewidth) {
		if (view != null) {
			((TableLayout)view.getLayout()).setColumn(0, namewidth);
		}
	}
	
	/**
	 * 
	 * @return preferred row header width
	 */
	public int getRowHeaderWidth() {
		return view!=null ? view.getRowHeaderWidth() : 0;
	}
	
	/**
	 * 
	 * @param role
	 * @param selected
	 */
	public void setSelected(Integer role, boolean selected) {
		if (view!=null)
			view.setSelected(role, selected);
	}
	
	/**
	 * remove selection from all right columns
	 */
	public void removeAllSelections() {
		if (view!=null)
			view.removeAllSelections();
	}
	
	/**
	 * 
	 * @return map of role with right: Map<Integer, Boolean> 
	 */
	public Map<Integer, Boolean> getRoleRights() {
		return roleRights;
	}

	/**
	 * 
	 * @param role
	 * @param right
	 */
	public void setRoleRight(Integer role, Boolean right) {
		if (!nullRightAllowed && right == null)
			right = new Boolean(false);
		
		roleRights.put(role, right);
		if (view != null) {
			if (view.rightButtons.get(role) != null) {
				view.rightButtons.get(role).right = right;
				view.rightButtons.get(role).updateIcon();
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public ExpandCollapseListener getExpandCollapseListener() {
		return expandCollapseListener;
	}
	
	/**
	 * 
	 * @param ecl
	 */
	public void setExpandCollapseListener(ExpandCollapseListener ecl) {
		expandCollapseListener = ecl;
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
	 * @return id of group (subform) / attribute (column)
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * 
	 * @return name of group (subform) / attribute (column)
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return true if row is a group or subform
	 */
	public boolean isGroup() {
		return group==null;
	}
	
	/**
	 * 
	 * @return group id 
	 */
	public Integer getGroupId() {
		if (isGroup())
			return getId();
		else
			return group.getId();
	}
	
	/**
	 * 
	 * @return group name
	 */
	public String getGroupName() {
		if (isGroup())
			return name;
		else
			return group.getGroupName();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSubForm() {
		return subform;
	}
	
	/**
	 * 
	 * @return true if no right is allowed
	 */
	public boolean isNullRightAllowed() {
		return nullRightAllowed;
	}
	
	/**
	 * 
	 * @return true if this row has right values
	 */
	public boolean isRightsEnabled() {
		return rightsEnabled;
	}
	
	/**
	 * 
	 * @param rightsEnabled 
	 */
	public void setRightsEnabled(boolean rightsEnabled) {
		this.rightsEnabled = rightsEnabled;
	}
	
	/**
	 * 
	 * @return true if attribute / column is mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}
	
	/**
	 * 
	 * @param mandatory
	 */
	public void setMandatory(boolean mandatory) {
		setMandatory(mandatory, true);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isCollapsed() {
		return collapsed;
	}

	/**
	 * 
	 * @param collapsed
	 */
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
		if (view!=null) {
			view.rowHeader.updateExpandCollapseIcon();
		}
	}

	private void setMandatory(boolean mandatory, boolean updateUI) {
		this.mandatory = mandatory;
		if (updateUI)
			if (view != null)
				view.rowHeader.checkbMandatory.setSelected(mandatory);
	}
	
	private void respectNullRightAllowed() {
		if (nullRightAllowed)
			return;
		
		for (Integer role : roleRights.keySet()) {
			if (roleRights.get(role) == null) {
				setRoleRight(role, false);
			}
		}
	}
	
	/**
	 * 
	 * @param role
	 */
	public void hideRoleRight(Integer role) {
		if (view != null) {
			((TableLayout)view.getLayout()).setColumn(view.findRoleInLayout(role), 0);
			view.revalidate();
			view.repaint();
		}
	}
	
	/**
	 * 
	 * @param role
	 */
	public void showRoleRight(Integer role) {
		if (view != null) {
			((TableLayout)view.getLayout()).setColumn(view.findRoleInLayout(role), TableLayoutConstants.PREFERRED);
			view.revalidate();
			view.repaint();
		}
	}
	
	/**
	 * copy rights from group.
	 * row has to be attribute or column
	 */
	public void copyRightsFromGroup() {
		if (view != null && !isGroup()) {
			view.copyRightsFromGroup();
		}
	}
	
	/**
	 * 
	 * @param right
	 */
	public void setAllRoleRights(Boolean right) {
		if (!isRightsEnabled()) {
			setRightsEnabled(true);
		}
		for (Integer role : sortOrder) {
			setRoleRight(role, right);
		}
	}

	/**
	 * 
	 * the main JPanel for this row
	 */
	public class View extends JPanel {

		RowHeader rowHeader = null;
		Map<Integer, RightButton> rightButtons = new HashMap<Integer, RightButton>();
		RevertButton revert = new RevertButton();
		
		/**
		 * 
		 * @param namewidth
		 */
		public View(double namewidth) {
			double[] cols = new double[1 + 1 + roleRights.size() + 1 + 1];
			cols[0] = TableLayoutConstants.PREFERRED;
			cols[1] = GAP_ROWHEADER;
			for (int i = 2; i < cols.length; i++) {
				cols[i] = TableLayoutConstants.PREFERRED;
			}
			cols[cols.length-2] = 1; // last grid on the right
			final double size [][] = {cols, new double[] {TableLayoutConstants.PREFERRED}};
			final TableLayout layout = new TableLayout(size);
			
			setLayout(layout);
			setBackground(COLOR_BACKGROUND);
			
			rowHeader = new RowHeader(COLOR_BACKGROUND, namewidth);
			add(rowHeader, "0,0");
			
			if (group == null && id == 0)
				return; // is "system" group
			
			MouseOver mOver = new MouseOver();
			
			int col = 2;
			for (int i = 0; i < sortOrder.size(); i++) {
				Integer role = sortOrder.get(i);
				
				rightButtons.put(role, new RightButton(role, roleRights.get(role)));
				rightButtons.get(role).setPreferredSize(new Dimension(
					CELL_WIDTH-1, 
					CELL_HEIGHT));
				rightButtons.get(role).setBackground(COLOR_BACKGROUND);
				if (DEV_MODUS) rightButtons.get(role).addMouseListener(mOver);
				
				add(rightButtons.get(role), col+",0");
				col++;
			}
			JPanel lastGridOnTheRight = new JPanel() {

				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2d = (Graphics2D) g;				
					g2d.setStroke(new BasicStroke(1.f));
					g2d.setColor(group != null ? COLOR_GRID : COLOR_BACKGROUND);
					g2d.drawLine(0, 0, 0, CELL_HEIGHT-1);
				}
				
			};
			add(lastGridOnTheRight, col+",0");
			col++;
			
			if (DEV_MODUS) revert.addMouseListener(mOver);
			if (!isGroup())
				add(revert, col+",0");
		}

		private void setSelected(Integer role, boolean selected) {
			if (view.rightButtons.get(role) != null) {
				rightButtons.get(role).setSelected(selected);
				rightButtons.get(role).revalidate();
				rightButtons.get(role).repaint();
			}
		}
		
		private void removeAllSelections() {
			for (RightButton rb : rightButtons.values()) {
				if (rb.isSelected()) {
					rb.setSelected(false);
				}
			}
		}
		
		private int getRowHeaderWidth() {
			return rowHeader!=null ? rowHeader.getPreferredSize().width : 0;
		}
		
		private void copyRightsFromGroup() {
			if (DEV_MODUS) {
			rightsEnabled = true;
			for (Integer role : group.roleRights.keySet()) {
				setRoleRight(role, group.roleRights.get(role));
			}
			}
		}
		
		private void revertRights() {
			if (DEV_MODUS) {
			rightsEnabled = false;
			for (RightButton rb : rightButtons.values()) {
				rb.updateIcon();
			}
			}
		}
		
		private ImageIcon getTreeIcon(boolean collapsed) {
			Object uiTreeIcon = UIManager.get(collapsed?"Tree.collapsedIcon":"Tree.expandedIcon");
			
			if (uiTreeIcon != null && uiTreeIcon instanceof ImageIcon) {
				return (ImageIcon) uiTreeIcon;
			} else {
				throw new IllegalArgumentException("Look&Feel without Tree.expandedIcon/Tree.collapsedIcon");
			}
		}
		
		private int findRoleInLayout(Integer role) {
			int posInLayout = 2;
			for (int i = 0; i < sortOrder.size(); i++) {
				if (sortOrder.get(i).equals(role)) {
					posInLayout = posInLayout + i;
					break;
				}
			}
			return posInLayout;
		}
		
		/**
		 * 
		 * for highlighting rights from group
		 */
		class MouseOver implements MouseListener {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {
				if (group != null) {
					if (!rightsEnabled) {
						for (Integer role : rightButtons.keySet()) {
							RightButton rb = rightButtons.get(role);
							if (!nullRightAllowed && group.roleRights.get(role) == null)
								rb.updateIcon(false, true);
							else
								rb.updateIcon(group.roleRights.get(role), true);
						}
					} else {
						revert.updateIcon(true);
					}
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if (group != null) {
					if (!rightsEnabled) {
						for (RightButton rb : rightButtons.values()) {
							rb.updateIcon();
						}
					} else {
						revert.updateIcon(false);
					}
				}
			}
		}
		
		/**
		 * 
		 * revert attribute / column rights
		 */
		class RevertButton extends JLabel {
			
			public RevertButton() {
				if (DEV_MODUS) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));	
				if (DEV_MODUS) 
				addMouseListener(new MouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {}
					@Override
					public void mousePressed(MouseEvent e) {
						revertRights();
						updateIcon(false);
						
						if (selectionListener != null) {
							selectionListener.actionPerformed(new ActionEvent(RightAndMandatoryRow.this, 0, 
								SelectionListener.COMMAND_DESELECT));
						}
						
						detailsChangedListener.stateChanged(new ChangeEvent(this));
					}
					@Override
					public void mouseReleased(MouseEvent e) {}
					@Override
					public void mouseEntered(MouseEvent e) {}
					@Override
					public void mouseExited(MouseEvent e) {}
				});
				updateIcon(false);
			}
			
			private void updateIcon(boolean show) {
				if (show) {
					setIcon(Icons.getInstance().getIconPriorityCancel16());
				} else {
					setIcon(Icons.getInstance().getIconEmpty16());
				}
			}
		}
		
		/**
		 * 
		 * one right/role button
		 */
		class RightButton extends JLabel {
			
			Integer role;
			Boolean right;
			boolean selected;
			
			/**
			 * 
			 * @return current right (could be null)
			 */
            public Boolean getRight() {
            	return right;
            }

            /**
             * 
             * @param role
             * @param right
             */
			public RightButton(final Integer role, Boolean right) {
				this.role = role;
				this.right = right;
				
				if (DEV_MODUS || isGroup()) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));	
				if (DEV_MODUS || isGroup()) 
				addMouseListener(new MouseListener() {
					@Override
                    public void mouseClicked(MouseEvent e) {}
					@Override
                    public void mousePressed(MouseEvent e) {
						if (!rightsEnabled) {
							copyRightsFromGroup();
						} else {
							if (RightButton.this.right == null) {
								RightButton.this.right = false;
							} else if (RightButton.this.right == false) {
								RightButton.this.right = true;
							} else if (RightButton.this.right == true) {
								if (nullRightAllowed)
									RightButton.this.right = null;
								else
									RightButton.this.right = false;
							} 
							roleRights.put(RightButton.this.role, RightButton.this.right);
							updateIcon();
						}
						
						if (selectionListener != null) {
							selectionListener.actionPerformed(new ActionEvent(RightAndMandatoryRow.this, RightButton.this.role, 
								SelectionListener.COMMAND_DESELECT));
						}
						
						detailsChangedListener.stateChanged(new ChangeEvent(this));
					}
					@Override
                    public void mouseReleased(MouseEvent e) {}
					@Override
                    public void mouseEntered(MouseEvent e) {}
					@Override
                    public void mouseExited(MouseEvent e) {}});
				
				updateIcon();
			}
			
			private void setSelected(boolean selected) {
				this.selected = selected;
				revalidate();
				repaint();
			}
			
			private boolean isSelected() {
				return this.selected;
			}
			
			private void updateIcon() {
				updateIcon(right, rightsEnabled);
			}
			
			private void updateIcon(Boolean rightToShow, boolean rightsEnabled) {
				if (rightsEnabled) {
					if (rightToShow == null) {
						setIcon(ICON_NO_RIGHT);
					} else {
						if (rightToShow) {
							setIcon(ICON_WRITE);
						} else {
							setIcon(ICON_READ);
						}
					}
				} else {
					setIcon(Icons.getInstance().getIconEmpty16());
				}
			}
			
			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				
				if (selected) {
					g2d.setColor(COLOR_SELECTION_BACKGROUND);
					g2d.fillRect(1, 0, CELL_WIDTH-2, CELL_HEIGHT);
				}
				
				super.paintComponent(g);
				
				if (group != null) {
					g2d.setStroke(new BasicStroke(1.f));
					g2d.setColor(COLOR_GRID);
					g2d.drawLine(0, 0, CELL_WIDTH-1, 0);
					g2d.drawLine(CELL_WIDTH-1, 0, CELL_WIDTH-1, CELL_HEIGHT-1);
					g2d.drawLine(CELL_WIDTH-1, CELL_HEIGHT-1, 0, CELL_HEIGHT-1);
					g2d.drawLine(0, CELL_HEIGHT-1, 0, 0);
				}
			}
		}
		
		/**
		 * 
		 * header for this row
		 */
		class RowHeader extends JPanel {
			
			private JLabel labExpandCollapse;
			private MouseListener mlExpandCollapse;
			final private JCheckBox checkbMandatory = new JCheckBox();
			
			public RowHeader(Color colorBackground, double namewidth) {
				double[] cols = new double[3];
				cols[0] = getTreeIcon(true).getIconWidth()+5;
				cols[1] = TableLayoutConstants.FILL;
				cols[2] = TableLayoutConstants.PREFERRED;
				
				final double size [][] = {cols, new double[] {TableLayoutConstants.PREFERRED}};
				final TableLayout layout = new TableLayout(size);
				
				setLayout(layout);
				setBackground(colorBackground);
				
				if (group == null) {
					if (DEV_MODUS || !isSubForm()) {
					JLabel ecLabel = getExpandCollapseLabel();
					ecLabel.setBackground(colorBackground);
					add(ecLabel, "0,0");}
				}
				
				JLabel labName;
				if (isGroup()) {
					labName = new JLabel("<html><b>"+name+"</b></html>");
				} else {
					labName = new JLabel(name);
				}
				labName.setBackground(colorBackground);
				if (DEV_MODUS || isGroup()) 
				labName.addMouseListener(new MouseListener() {
					@Override
					public void mouseReleased(MouseEvent e) {}
					@Override
					public void mousePressed(MouseEvent e) {
						if(SwingUtilities.isRightMouseButton(e)) {
							if (getId().intValue() == 0)
								return;
							
							final JPopupMenu menu = new JPopupMenu();
							if (nullRightAllowed) {
								JMenuItem itemNull = new JMenuItem(CommonLocaleDelegate.getMessage("RightAndMandatory.1", "Alle nicht sichtbar"), ICON_NO_RIGHT);
								itemNull.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {setAllRoleRights(null);detailsChangedListener.stateChanged(new ChangeEvent(this));}
								});
								menu.add(itemNull);
							}
							JMenuItem itemRead = new JMenuItem(CommonLocaleDelegate.getMessage("RightAndMandatory.2", "Alle lesen"), ICON_READ);
							itemRead.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {setAllRoleRights(false);detailsChangedListener.stateChanged(new ChangeEvent(this));}
							});
							menu.add(itemRead);
							JMenuItem itemWrite = new JMenuItem(CommonLocaleDelegate.getMessage("RightAndMandatory.3", "Alle schreiben"), ICON_WRITE);
							itemWrite.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {setAllRoleRights(true);detailsChangedListener.stateChanged(new ChangeEvent(this));}
							});
							menu.add(itemWrite);
							menu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
					@Override
					public void mouseExited(MouseEvent e) {}
					@Override
					public void mouseEntered(MouseEvent e) {}
					@Override
					public void mouseClicked(MouseEvent e) {}
				});
				add(labName, "1,0,l,b");
				
				if (group != null) {
					checkbMandatory.setPreferredSize(new Dimension(checkbMandatory.getPreferredSize().width,labName.getPreferredSize().height));
					checkbMandatory.setBackground(colorBackground);
					if (metaMandatory) {
						checkbMandatory.setSelected(true);
						checkbMandatory.setEnabled(false);
					} else {
						checkbMandatory.setSelected(mandatory);
						checkbMandatory.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								setMandatory(checkbMandatory.isSelected(), false);
								detailsChangedListener.stateChanged(new ChangeEvent(this));
							}
						});
					}
					add(checkbMandatory, "2,0");
					
					setBorder(BorderFactory.createLineBorder(COLOR_GRID, 1));
				}
			}
			
			private JLabel getExpandCollapseLabel() {
				if (labExpandCollapse == null) {
					labExpandCollapse = new JLabel(getTreeIcon(collapsed));
					labExpandCollapse.addMouseListener(getExpandCollapseMouseListener());
				}
				return labExpandCollapse;
			}
			
			private void updateExpandCollapseIcon() {
				if (labExpandCollapse != null)
					labExpandCollapse.setIcon(getTreeIcon(collapsed));
			}
			
			private MouseListener getExpandCollapseMouseListener() {
				if (mlExpandCollapse == null)
					mlExpandCollapse = new MouseListener() {
						@Override
						public void mouseReleased(MouseEvent e) {}
						@Override
						public void mousePressed(MouseEvent e) {
							collapsed = !collapsed;
							getExpandCollapseLabel().setIcon(getTreeIcon(collapsed));
							if (expandCollapseListener != null)
								expandCollapseListener.actionPerformed(new ActionEvent(RightAndMandatoryRow.this, 0, 
									collapsed? ExpandCollapseListener.COMMAND_COLLAPSE : ExpandCollapseListener.COMMAND_EXPAND));
						}
						@Override
						public void mouseExited(MouseEvent e) {}
						@Override
						public void mouseEntered(MouseEvent e) {}
						@Override
						public void mouseClicked(MouseEvent e) {}
					};
				return mlExpandCollapse;
			}
			
		}
	}
}
