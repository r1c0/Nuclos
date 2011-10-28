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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

/**
 * class for <b>CommonJTabbedPane.java</b>...
 * <br>
 * @author <a href="mailto:stefan.geiling@novabit.de">Stefan Geiling</a>
 * @version	$Revision$
 */
public class CommonJTabbedPane extends DnDTabbedPane {

	private static final Logger LOG = Logger.getLogger(CommonJTabbedPane.class);

	private static final DataFlavor TAB_COMPONENT_DATAFLAVOR = new DataFlavor(TabComponent.class, "Tab");

   /** Constructor of this object...
    */
   public CommonJTabbedPane() {
   	setPaintGhost(false);
      addPropertyChangeListener(new PropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent evt) {
            if ("indexForTitle".equals(evt.getPropertyName())) {
               int index = (Integer) evt.getNewValue();
               Component c = getTabComponentAt(index );
               if (c instanceof TabComponent) {
                  TabComponent tc = (TabComponent) c;
                  tc.setTitle(getTitleAt(index), getIconAt(index), getToolTipTextAt(index));
               }
            }
         }
      });
      setTransferHandler(new TransferHandlerExtension());
      addMouseMotionListener(new MouseMotionAdapter() {
         @Override
         public void mouseDragged(MouseEvent evt) {
            int index = indexAtLocation(evt.getX(), evt.getY());
            if (index != -1)
               getTransferHandler().exportAsDrag(CommonJTabbedPane.this, evt, TransferHandler.MOVE);
         }
      });
   }

   @Override
   public void insertTab(String title, Icon icon, Component component, String tip, int index) {
      super.insertTab(title, icon, component, tip, index);

      index = indexOfComponent(component);
      TabComponent tc = new TabComponent(title, icon, tip);
//      if (component instanceof GenericObjectTaskView || getParent() instanceof ExplorerPanel) {
//         tc.setClosable(true);
//      }
      setTabComponentAt(index, tc);
   }

   @Override
   public void setTitleAt(int index, String title) {
      super.setTitleAt(index, title);
   }

   private final class TransferHandlerExtension extends TransferHandler {
   	
	@Override
   	protected Transferable createTransferable(JComponent c) {
   		return super.createTransferable(c);
   	}
   	
      @Override
      public void exportAsDrag(JComponent comp, InputEvent e, int action) {
         super.exportAsDrag(comp, e, action);
      }

      @Override
      public int getSourceActions(JComponent c) {
         return MOVE;
      }

      @Override
      protected void exportDone(JComponent source, Transferable data, int action) {
         super.exportDone(source, data, action);
      }

      @Override
      public boolean canImport(TransferSupport support) {
         Transferable transferable = support.getTransferable();
         return transferable.isDataFlavorSupported(TAB_COMPONENT_DATAFLAVOR);
      }

		/**
		 * @deprecated Always returns true.
		 */
		@Override
		public boolean importData(TransferSupport support) {
			return true;
		}
   }

   private class TabComponent extends JComponent implements Transferable {

	   private JLabel label;
	   private JButton button;

      public TabComponent(String title, Icon icon, String tip) {
         setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
         label = new JLabel();
         button = new JButton();
         
         button.setMargin(new Insets(0, 0, 0, 0));
         button.setFocusable(false);
         button.setContentAreaFilled(false);
         button.putClientProperty("rollover", true);
         button.setBorderPainted(false);
         button.setOpaque(false);

         add(label);
         add(Box.createHorizontalStrut(3));
         add(button);
         button.setVisible(false);

         setTitle(title, icon, tip);
      }

      public void setTitle(String text, Icon icon, String toolTip) {
         label.setText(text);
         label.setIcon(icon);
         label.setToolTipText(toolTip);
      }

      @Override
      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
      	if (flavor == TAB_COMPONENT_DATAFLAVOR)
      		return this;
      	throw new UnsupportedFlavorException(flavor);
      }

      @Override
      public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[] { TAB_COMPONENT_DATAFLAVOR };
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor flavor) {
      	return flavor == TAB_COMPONENT_DATAFLAVOR;
      }

   }
}


//
// Code from http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html
//
class DnDTabbedPane extends JTabbedPane {
	
	private static final Logger LOG = Logger.getLogger(DnDTabbedPane.class);

	private static final int LINEWIDTH = 3;
   private static final String NAME = "test";
   private final GhostGlassPane glassPane = new GhostGlassPane();
   private final Rectangle lineRect  = new Rectangle();
   private final Color     lineColor = new Color(0, 100, 255);
   private int dragTabIndex = -1;

   private void clickArrowButton(String actionKey) {
       ActionMap map = getActionMap();
       if(map != null) {
           Action action = map.get(actionKey);
           if (action != null && action.isEnabled()) {
               action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, 0, 0));
           }
       }
   }
   private static Rectangle rBackward = new Rectangle();
   private static Rectangle rForward  = new Rectangle();
   private static int rwh = 20;
   private static int buttonsize = 30; //xxx magic number of scroll button size
   private void autoScrollTest(Point glassPt) {
       Rectangle r = getTabAreaBounds();
       int tabPlacement = getTabPlacement();
       if(tabPlacement==TOP || tabPlacement==BOTTOM) {
           rBackward.setBounds(r.x, r.y, rwh, r.height);
           rForward.setBounds(r.x+r.width-rwh-buttonsize, r.y, rwh+buttonsize, r.height);
       }else if(tabPlacement==LEFT || tabPlacement==RIGHT) {
           rBackward.setBounds(r.x, r.y, r.width, rwh);
           rForward.setBounds(r.x, r.y+r.height-rwh-buttonsize, r.width, rwh+buttonsize);
       }
       if(rBackward.contains(glassPt)) {
           clickArrowButton("scrollTabsBackwardAction");
       }else if(rForward.contains(glassPt)) {
           clickArrowButton("scrollTabsForwardAction");
       }
   }
   public DnDTabbedPane() {
       super();
       final DragSourceListener dsl = new DragSourceListener() {
           @Override
		public void dragEnter(DragSourceDragEvent e) {
               e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
           }
           @Override
		public void dragExit(DragSourceEvent e) {
               e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
               lineRect.setRect(0,0,0,0);
               glassPane.setPoint(new Point(-1000,-1000));
               glassPane.repaint();
           }
           @Override
		public void dragOver(DragSourceDragEvent e) {
               Point glassPt = e.getLocation();
               SwingUtilities.convertPointFromScreen(glassPt, glassPane);
               int targetIdx = getTargetTabIndex(glassPt);
               //if(getTabAreaBounds().contains(tabPt) && targetIdx>=0 &&
               //getTabAreaBounds().contains(glassPt) && 
               if(targetIdx>=0 && targetIdx!=dragTabIndex && targetIdx!=dragTabIndex+1) {
                   e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
                   glassPane.setCursor(DragSource.DefaultMoveDrop);
               }else{
                   e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                   glassPane.setCursor(DragSource.DefaultMoveNoDrop);
               }
           }
           @Override
		public void dragDropEnd(DragSourceDropEvent e) {
               lineRect.setRect(0,0,0,0);
               dragTabIndex = -1;
               glassPane.setVisible(false);
               if(hasGhost()) {
                   glassPane.setVisible(false);
                   glassPane.setImage(null);
               }
           }
           @Override
		public void dropActionChanged(DragSourceDragEvent e) {}
       };
       final Transferable t = new Transferable() {
           private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
           @Override
		public Object getTransferData(DataFlavor flavor) {
               return DnDTabbedPane.this;
           }
           @Override
		public DataFlavor[] getTransferDataFlavors() {
               DataFlavor[] f = new DataFlavor[1];
               f[0] = this.FLAVOR;
               return f;
           }
           @Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
               return flavor.getHumanPresentableName().equals(NAME);
           }
       };
       final DragGestureListener dgl = new DragGestureListener() {
           @Override
		public void dragGestureRecognized(DragGestureEvent e) {
               if(getTabCount()<=1) return;
               Point tabPt = e.getDragOrigin();
               dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
               //"disabled tab problem".
               if(dragTabIndex<0 || !isEnabledAt(dragTabIndex)) return;
               initGlassPane(e.getComponent(), e.getDragOrigin());
               try{
                   e.startDrag(DragSource.DefaultMoveDrop, t, dsl);
               }catch(InvalidDnDOperationException e1) {
            	   LOG.warn("dragGestureRecognized failed: " + e1, e1);
               }
           }
       };
       new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
       new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
   }

   class CDropTargetListener implements DropTargetListener{
       @Override
	public void dragEnter(DropTargetDragEvent e) {
           if(isDragAcceptable(e)) e.acceptDrag(e.getDropAction());
           else e.rejectDrag();
       }
       @Override
	public void dragExit(DropTargetEvent e) {}
       @Override
	public void dropActionChanged(DropTargetDragEvent e) {}

       private Point pt_ = new Point();
       @Override
	public void dragOver(final DropTargetDragEvent e) {
           Point pt = e.getLocation();
           if(getTabPlacement()==JTabbedPane.TOP || getTabPlacement()==JTabbedPane.BOTTOM) {
               initTargetLeftRightLine(getTargetTabIndex(pt));
           }else{
               initTargetTopBottomLine(getTargetTabIndex(pt));
           }
           if(hasGhost()) {
               glassPane.setPoint(pt);
           }
           if(!pt_.equals(pt)) glassPane.repaint();
           pt_ = pt;
           autoScrollTest(pt);
       }

       @Override
	public void drop(DropTargetDropEvent e) {
           if(isDropAcceptable(e)) {
               convertTab(dragTabIndex, getTargetTabIndex(e.getLocation()));
               e.dropComplete(true);
           }else{
               e.dropComplete(false);
           }
           repaint();
       }
       public boolean isDragAcceptable(DropTargetDragEvent e) {
           Transferable t = e.getTransferable();
           if(t==null) return false;
           DataFlavor[] f = e.getCurrentDataFlavors();
           if(t.isDataFlavorSupported(f[0]) && dragTabIndex>=0) {
               return true;
           }
           return false;
       }
       public boolean isDropAcceptable(DropTargetDropEvent e) {
           Transferable t = e.getTransferable();
           if(t==null) return false;
           DataFlavor[] f = t.getTransferDataFlavors();
           if(t.isDataFlavorSupported(f[0]) && dragTabIndex>=0) {
               return true;
           }
           return false;
       }
   }

   private boolean hasGhost = true;
   public void setPaintGhost(boolean flag) {
       hasGhost = flag;
   }
   public boolean hasGhost() {
       return hasGhost;
   }
   private boolean isPaintScrollArea = true;
   public void setPaintScrollArea(boolean flag) {
       isPaintScrollArea = flag;
   }
   public boolean isPaintScrollArea() {
       return isPaintScrollArea;
   }

   private int getTargetTabIndex(Point glassPt) {
       Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, DnDTabbedPane.this);
       boolean isTB = getTabPlacement()==JTabbedPane.TOP || getTabPlacement()==JTabbedPane.BOTTOM;
       for(int i=0;i<getTabCount();i++) {
           Rectangle r = getBoundsAt(i);
           if(isTB) r.setRect(r.x-r.width/2, r.y,  r.width, r.height);
           else     r.setRect(r.x, r.y-r.height/2, r.width, r.height);
           if(r.contains(tabPt)) return i;
       }
       Rectangle r = getBoundsAt(getTabCount()-1);
       if(isTB) r.setRect(r.x+r.width/2, r.y,  r.width, r.height);
       else     r.setRect(r.x, r.y+r.height/2, r.width, r.height);
       return   r.contains(tabPt)?getTabCount():-1;
   }
   private void convertTab(int prev, int next) {
       if(next<0 || prev==next) {
           return;
       }
       Component cmp = getComponentAt(prev);
       Component tab = getTabComponentAt(prev);
       String str    = getTitleAt(prev);
       Icon icon     = getIconAt(prev);
       String tip    = getToolTipTextAt(prev);
       boolean flg   = isEnabledAt(prev);
       int tgtindex  = prev>next ? next : next-1;
       remove(prev);
       insertTab(str, icon, cmp, tip, tgtindex);
       setEnabledAt(tgtindex, flg);
       //When you drag'n'drop a disabled tab, it finishes enabled and selected.
       //pointed out by dlorde
       if(flg) setSelectedIndex(tgtindex);

       //I have a component in all tabs (jlabel with an X to close the tab) and when i move a tab the component disappear.
       //pointed out by Daniel Dario Morales Salas
       setTabComponentAt(tgtindex, tab);
   }

   private void initTargetLeftRightLine(int next) {
       if(next<0 || dragTabIndex==next || next-dragTabIndex==1) {
           lineRect.setRect(0,0,0,0);
       }else if(next==0) {
           Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
           lineRect.setRect(r.x-LINEWIDTH/2,r.y,LINEWIDTH,r.height);
       }else{
           Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next-1), glassPane);
           lineRect.setRect(r.x+r.width-LINEWIDTH/2,r.y,LINEWIDTH,r.height);
       }
   }
   private void initTargetTopBottomLine(int next) {
       if(next<0 || dragTabIndex==next || next-dragTabIndex==1) {
           lineRect.setRect(0,0,0,0);
       }else if(next==0) {
           Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
           lineRect.setRect(r.x,r.y-LINEWIDTH/2,r.width,LINEWIDTH);
       }else{
           Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next-1), glassPane);
           lineRect.setRect(r.x,r.y+r.height-LINEWIDTH/2,r.width,LINEWIDTH);
       }
   }

   private void initGlassPane(Component c, Point tabPt) {
       getRootPane().setGlassPane(glassPane);
       if(hasGhost()) {
           Rectangle rect = getBoundsAt(dragTabIndex);
           BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
           Graphics g = image.getGraphics();
           c.paint(g);
           rect.x = rect.x<0?0:rect.x;
           rect.y = rect.y<0?0:rect.y;
           image = image.getSubimage(rect.x,rect.y,rect.width,rect.height);
           glassPane.setImage(image);
       }
       Point glassPt = SwingUtilities.convertPoint(c, tabPt, glassPane);
       glassPane.setPoint(glassPt);
       glassPane.setVisible(true);
   }

   private Rectangle getTabAreaBounds() {
       Rectangle tabbedRect = getBounds();
       //pointed out by daryl. NullPointerException: i.e. addTab("Tab",null)
       //Rectangle compRect   = getSelectedComponent().getBounds();
       Component comp = getSelectedComponent();
       int idx = 0;
       while(comp==null && idx<getTabCount()) comp = getComponentAt(idx++);
       Rectangle compRect = (comp==null)?new Rectangle():comp.getBounds();
       int tabPlacement = getTabPlacement();
       if(tabPlacement==TOP) {
           tabbedRect.height = tabbedRect.height - compRect.height;
       }else if(tabPlacement==BOTTOM) {
           tabbedRect.y = tabbedRect.y + compRect.y + compRect.height;
           tabbedRect.height = tabbedRect.height - compRect.height;
       }else if(tabPlacement==LEFT) {
           tabbedRect.width = tabbedRect.width - compRect.width;
       }else if(tabPlacement==RIGHT) {
           tabbedRect.x = tabbedRect.x + compRect.x + compRect.width;
           tabbedRect.width = tabbedRect.width - compRect.width;
       }
       tabbedRect.grow(2, 2);
       return tabbedRect;
   }
   
   class GhostGlassPane extends JPanel {

	   private final AlphaComposite composite;
       private Point location = new Point(0, 0);
       private BufferedImage draggingGhost = null;
       public GhostGlassPane() {
           setOpaque(false);
           composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
           //http://bugs.sun.com/view_bug.do?bug_id=6700748
           //setCursor(null);
       }
       public void setImage(BufferedImage draggingGhost) {
           this.draggingGhost = draggingGhost;
       }
       public void setPoint(Point location) {
           this.location = location;
       }
       @Override
       public void paintComponent(Graphics g) {
           Graphics2D g2 = (Graphics2D) g;
           g2.setComposite(composite);
           if(isPaintScrollArea() && getTabLayoutPolicy()==SCROLL_TAB_LAYOUT) {
               g2.setPaint(Color.RED);
               g2.fill(rBackward);
               g2.fill(rForward);
           }
           if(draggingGhost != null) {
               double xx = location.getX() - (draggingGhost.getWidth(this) /2d);
               double yy = location.getY() - (draggingGhost.getHeight(this)/2d);
               g2.drawImage(draggingGhost, (int)xx, (int)yy , null);
           }
           if(dragTabIndex>=0) {
               g2.setPaint(lineColor);
               g2.fill(lineRect);
           }
       }
   }
}
