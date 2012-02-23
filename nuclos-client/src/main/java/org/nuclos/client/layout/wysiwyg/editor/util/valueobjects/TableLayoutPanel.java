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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor.WYSIWYGLayoutEditorChangeDescriptor;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COMPONENT_POPUP;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL;
import org.nuclos.client.layout.wysiwyg.component.ComponentProcessors;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSplitPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertiesPanel;
import org.nuclos.client.layout.wysiwyg.datatransfer.DragElement;
import org.nuclos.client.layout.wysiwyg.datatransfer.TransferableComponent;
import org.nuclos.client.layout.wysiwyg.datatransfer.TransferableElement;
import org.nuclos.client.layout.wysiwyg.datatransfer.TransferablePlaceholder;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGMetaInformationPicker;
import org.nuclos.client.layout.wysiwyg.editor.util.DnDUtil.GhostGlassPane;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.UndoRedoFunction;
import org.nuclos.client.layout.wysiwyg.editor.util.mouselistener.PropertiesMouseListener;
import org.nuclos.client.layout.wysiwyg.editor.util.mouselistener.ResizeLayoutWithMouse;
import org.nuclos.client.layout.wysiwyg.editor.util.popupmenu.ChangeSizeMeasurementPopupColumn;
import org.nuclos.client.layout.wysiwyg.editor.util.popupmenu.ChangeSizeMeasurementPopupMultipleItems;
import org.nuclos.client.layout.wysiwyg.editor.util.popupmenu.ChangeSizeMeasurementPopupRows;
import org.nuclos.client.layout.wysiwyg.palette.PaletteController;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This is the LayoutPanel where all actions are made.<br>
 * Here are all {@link WYSIWYGComponent} places, dragged from the {@link PaletteController}.
 * <br><br>
 * Every {@link TableLayoutPanel} is placed in a {@link WYSIWYGLayoutEditorPanel}.
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class TableLayoutPanel extends JPanel implements DropTargetListener, MouseMotionListener, MouseListener, ComponentListener {
	
	private static final Logger LOG = Logger.getLogger(TableLayoutPanel.class);

	private TableLayoutUtil tableLayoutUtil;

	/** for handling change events */
	private WYSIWYGLayoutEditorChangeDescriptor chgDescriptor;

	private TableLayout tableLayout;
	private double[][] tablelayoutDescription = {{}, {}};
	private LayoutCell current;

	private ChangeSizeMeasurementPopupColumn changeSizeMeasurementPopupColumn;
	private ChangeSizeMeasurementPopupRows changeSizeMeasurementPopupRows;
	private ChangeSizeMeasurementPopupMultipleItems changeSizeMeasurementPopupMultipleItems;
	private ResizeLayoutWithMouse resizeLayoutWithMouse;
	private PropertiesMouseListener propertiesMouseListener;

	/** how should a new cell be created? preferred Size or absolute size? */
	private boolean usePreferredSizeInsteadofAbsoluteSizes = false;

	/** stack used for marking layoutcells needed for ChangeSizeMeasurementPopupMultipleItems */
	private Stack<LayoutCell> cellsToEdit = new Stack<LayoutCell>();
	
	private static final Logger log = Logger.getLogger(TableLayoutPanel.class);
	//NUCLEUSINT-999
	private KeyStroke deleteCellsBackspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
	private KeyStroke deleteCellsDelete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

	private boolean isCurrentCellNotLocked = true;

	public TableLayoutPanel() {
		tableLayout = new TableLayout(tablelayoutDescription);
		this.setLayout(tableLayout);
		tableLayoutUtil = new TableLayoutUtil(tableLayout, this);

		//DropTarget dropTarget = new DropTarget(this, this);
		//dropTarget.setActive(true);
		//dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);

        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
        //new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
        
		changeSizeMeasurementPopupColumn = new ChangeSizeMeasurementPopupColumn(tableLayoutUtil);
		changeSizeMeasurementPopupRows = new ChangeSizeMeasurementPopupRows(tableLayoutUtil);
		changeSizeMeasurementPopupMultipleItems = new ChangeSizeMeasurementPopupMultipleItems(tableLayoutUtil);
		propertiesMouseListener = new PropertiesMouseListener(this);
		resizeLayoutWithMouse = new ResizeLayoutWithMouse(tableLayoutUtil);

		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addComponentListener(this);
		this.addMouseListener(resizeLayoutWithMouse);
		this.addMouseMotionListener(resizeLayoutWithMouse);
		this.setToolTipText("");

		//NUCLEUSINT-999 Listener for DELETE on multiple selected cols and rows
		InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(deleteCellsBackspace, "deleteMarkedCells");
		inputMap.put(deleteCellsDelete, "deleteMarkedCells");
		ActionMap actionMap = this.getActionMap();
		Action deleteMarkedCells = new DeleteMarkedCells();
		actionMap.put("deleteMarkedCells", deleteMarkedCells);

		setOpaque(false);
	}

	private class DeleteMarkedCells extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (cellsToEdit.size() > 0)
				changeSizeMeasurementPopupMultipleItems.deleteCellsAndRowsWithShortcut(cellsToEdit);
				tableLayoutUtil.setCurrentLayoutCell(new LayoutCell());
				cellsToEdit.removeAllElements();
				revalidate();
		}
	}

	/**
	 * 
	 * @return PropertiesMouseListener
	 */
	public PropertiesMouseListener getPropertiesMouseListener() {
		return this.propertiesMouseListener;
	}

	/**
	 * 
	 * @return TableLayoutUtil
	 */
	public TableLayoutUtil getTableLayoutUtil() {
		return this.tableLayoutUtil;
	}

	/**
	 * This Method does all the drawing... Highlight of the current {@link LayoutCell}, display of the {@link TableLayout} Columns and Rows.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		Point mousepos = getCurrentTableLayoutUtil().getContainer().getMousePosition();
		if (mousepos != null) {
			if ((mousepos.x < InterfaceGuidelines.MARGIN_LEFT || mousepos.y < InterfaceGuidelines.MARGIN_TOP) && isCurrentCellNotLocked) {
				if (getCurrentTableLayoutUtil().getContainer().equals(this)) {
					if (mousepos.y < InterfaceGuidelines.MARGIN_TOP && mousepos.x > InterfaceGuidelines.MARGIN_LEFT)
						changeSizeMeasurementPopupColumn.drawModifyingBox(g);
					if (mousepos.x < InterfaceGuidelines.MARGIN_LEFT && mousepos.y > InterfaceGuidelines.MARGIN_TOP)
						changeSizeMeasurementPopupRows.drawModifyingBox(g);
				}
			} else {
				if (getCurrentTableLayoutUtil().getContainer().equals(this))
					/** highlight of the current layoutcell */
					tableLayoutUtil.drawCurrentCell(g);
			}
		}
		if (getCurrentTableLayoutUtil().getContainer().equals(this)) {
			if (!isCurrentCellNotLocked) {
				if (getCurrentLayoutCell().getCellY() == 0)
					changeSizeMeasurementPopupColumn.drawModifyingBox(g);
				if (getCurrentLayoutCell().getCellX() == 0)
					changeSizeMeasurementPopupRows.drawModifyingBox(g);
			}
		}

		tableLayoutUtil.drawTableLayoutCells(g);

		/** highlighting selected columns and rows for multiedit */
		if (!cellsToEdit.isEmpty()) {
			g2d.setStroke(new BasicStroke(0.5f));
			g2d.setColor(Color.RED);
			int completeHeight = tableLayoutUtil.getCalculatedLayoutHeight();
			int completeWidth = tableLayoutUtil.getCalculatedLayoutWidth();

			Iterator<LayoutCell> cellsSelected = cellsToEdit.iterator();

			while (cellsSelected.hasNext()) {
				LayoutCell cell = cellsSelected.next();
				if (cell.getCellY() == 0) {
					schraffiertesRechteckZeichnen(g, cell.getCellDimensions().x, cell.getCellDimensions().y, cell.getCellDimensions().width, completeHeight);
				}
				if (cell.getCellX() == 0) {
					schraffiertesRechteckZeichnen(g, cell.getCellDimensions().x, cell.getCellDimensions().y, completeWidth, cell.getCellDimensions().height);
				}
			}
		}

		for (Component c : getComponents()) {
			if (c instanceof WYSIWYGComponent) {
				if (!c.isVisible()) {

					g2d.setColor(Color.BLACK);
					g2d.fillRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
				}
			}
		}
	}
	
	/*
	 * Returns the fitting Tooltip for Columns and Rows in the Layout - Position and Size.
	 * 
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
	 */
	@Override
	public String getToolTipText(MouseEvent event) {
		
		Point mousepos = getCurrentTableLayoutUtil().getContainer().getMousePosition();
		LayoutCell actual = getCurrentTableLayoutUtil().getCurrentLayoutCell();
		int cellX = actual.getCellX();
		int cellY = actual.getCellY();
		
		double sizeX = actual.getCellWidth();
		double sizeY = actual.getCellHeight();
		
		String tooltiptext = "";
		
		if (mousepos != null) {
			if ((mousepos.x < InterfaceGuidelines.MARGIN_LEFT || mousepos.y < InterfaceGuidelines.MARGIN_TOP) && isCurrentCellNotLocked) {
				if (getCurrentTableLayoutUtil().getContainer().equals(this)) {
					if (mousepos.y < InterfaceGuidelines.MARGIN_TOP && mousepos.x > InterfaceGuidelines.MARGIN_LEFT) {
						String size = getSizeForTooltip(sizeX);
						tooltiptext = WYSIWYGStringsAndLabels.partedString(WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.TOOLTIPHEADER, WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.COLUMN,  cellX + "", size);
					} else if (mousepos.x < InterfaceGuidelines.MARGIN_LEFT && mousepos.y > InterfaceGuidelines.MARGIN_TOP) {
						String size = getSizeForTooltip(sizeY);
						tooltiptext = WYSIWYGStringsAndLabels.partedString(WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.TOOLTIPHEADER, WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.ROW,  cellY + "", size);
					}
				}
			}
		}
		
		return tooltiptext;		
	}
	
	/**
	 * This Method creates the Tooltiptext for columns and rows
	 * @param value the size of the cell
	 * @return the text representation for the size
	 */
	private String getSizeForTooltip(double value) {
		String size = "";
		
		if (value == TableLayoutConstants.PREFERRED)
			size = WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.PREFERRED;
		else if (value == TableLayoutConstants.MINIMUM)
			size =  WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.MINIMUM;
		else if (value == TableLayoutConstants.FILL)
			size = WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.FILL;
		else if (value == InterfaceGuidelines.MARGIN_BETWEEN)
			size = WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.MARGIN_BETWEEN;
		else if (value == InterfaceGuidelines.MARGIN_RIGHT)
			size = WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.MARGIN;
		else 
			size = WYSIWYGStringsAndLabels.partedString(WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL_TOOLTIP.ABSOLUTE, value + "");
		
		return size;
	}

	public void schraffiertesRechteckZeichnen(Graphics g, int x, int y, int width, int height) {
		final int DST = 8;

		g.drawRect(x, y, width, height);
		for (int i = DST; i < width + height; i += DST) {
			int p1x = (i <= height) ? x : x + i - height;
			int p1y = (i <= height) ? y + i : y + height;
			int p2x = (i <= width) ? x + i : x + width;
			int p2y = (i <= width) ? y : y + i - width;
			g.drawLine(p1x, p1y, p2x, p2y);
		}
	}

	public boolean isResizeDragPerformed() {
		return ResizeLayoutWithMouse.isPerfomingAction();
	}
	
	/**
	 * ########## Listener ##########
	 */


	/**
	 * DropTargetListener
	 */
	@Override 
	public void dragEnter(DropTargetDragEvent dtde) {
		// reject drop by default
		dtde.rejectDrag();
		
		if (getComponentToMove() != null && !glassPane.isVisible())
			initGlassPane((Component)getComponentToMove(), dtde.getLocation());
	}

	private static GhostGlassPane glassPane = new GhostGlassPane();
    public void initGlassPane(Component c, Point p) {
        getRootPane().setGlassPane(glassPane);
        glassPane.addMouseListener(this);
        glassPane.setBorder(BorderFactory.createLineBorder(Color.RED));

        //paint ghost
        BufferedImage image = new BufferedImage(c.getWidth( ),
									c.getHeight( ), 
									BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics( );
		c.paint(g);

		glassPane.setPoint(new Point(-1000,-1000));
		if (!Main.isMacOSX())
			glassPane.setVisible(true);

		glassPane.setImage(image);
		glassPane.revalidate();
		glassPane.repaint();
    }
    
    public void hideGlassPane() {
		resetComponentToMove();
		
		glassPane.setCursor(null); // reset cursor. cause cursor flickering on dnd inside the component.
		tableLayoutUtil.getContainer().setCursor(null); // reset cursor. cause cursor flickering on dnd inside the component.
		
		glassPane.setVisible(false);
		glassPane.setImage(null);
    }

	@Override
	public void dragExit(DropTargetEvent dte) {
				
	}

	/** 
	 * This Method highlights the Cell the Mouse/ Component is currently over
	 */
	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		Point p = (Point)dtde.getLocation().clone();
		
		tableLayoutUtil.setCurrentLayoutCell(p);

		if (isDropAllowed(dtde)) {
			glassPane.setCursor(DragSource.DefaultCopyDrop); // prevents cursor flickering on dnd inside the component.
			tableLayoutUtil.getContainer().setCursor(DragSource.DefaultCopyDrop); // prevents cursor flickering on dnd inside the component.
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
			//tableLayoutUtil.drawCurrentCell(this.getCurrentTableLayoutUtil().getContainer().getGraphics());
		} else {
			glassPane.setCursor(DragSource.DefaultCopyNoDrop); // prevents cursor flickering on dnd inside the component.
			tableLayoutUtil.getContainer().setCursor(DragSource.DefaultCopyNoDrop); // prevents cursor flickering on dnd inside the component.
			dtde.rejectDrag();
		}

		repaint();
		
		try {
			Point px = (Point)dtde.getLocation().clone();
			
			SwingUtilities.convertPointToScreen(px, this);
			SwingUtilities.convertPointFromScreen(px, getParentEditorPanel().getController().getEditorPanel());
			
			Point l = getParentEditorPanel().getController().getEditorPanel().getLocationOnScreen();
			px = new Point(px.x+l.x, px.y+l.y);
			
			glassPane.setPoint(px);
			//glassPane.revalidate();
			glassPane.repaint();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * Checking if drop is allowed
	 * @param event
	 * @return
	 */
	private boolean isDropAllowed(DropTargetEvent event) {
		boolean dataFlavorSupported = false;
		if (event instanceof DropTargetDragEvent) {
			if (((DropTargetDragEvent) event).isDataFlavorSupported(TransferableElement.flavor)) {
				dataFlavorSupported = true;
			} else if (((DropTargetDragEvent) event).isDataFlavorSupported(TransferableComponent.flavor)) {
				dataFlavorSupported = true;
			}
		} else if (event instanceof DropTargetDropEvent) {
			if (((DropTargetDropEvent) event).isDataFlavorSupported(TransferableElement.flavor)) {
				dataFlavorSupported = true;
			} else if (((DropTargetDropEvent) event).isDataFlavorSupported(TransferableComponent.flavor)) {
				dataFlavorSupported = true;
			}
		}
		
		/** in which mode is the editor running?*/
		int wysiwygEditorMode = -1;
		try {
			wysiwygEditorMode = getParentEditorPanel().getController().getMode();
		} catch (NuclosBusinessException e) {
			log.error(e);
		}

		if (dataFlavorSupported) {
			LayoutCell cell = tableLayoutUtil.getCurrentLayoutCell();
		
			if (wysiwygEditorMode == WYSIWYGEditorModes.EXPERT_MODE){
				//NUCLEUSINT-365
				/** dropping of a component to a not empty cell is allowed if expertmode is enabled */
				return true;
			}
			// NUCLEUSINT-496
			Point mouse = tableLayoutUtil.getContainer().getMousePosition();
			if (mouse == null) {
				// nullpointer exception prevention
				if (event instanceof DropTargetDragEvent) {
					mouse = ((DropTargetDragEvent) event).getLocation();
				} else if (event instanceof DropTargetDropEvent) {
					mouse = ((DropTargetDropEvent) event).getLocation();
				}

				if (mouse == null) {
					return false;
				}
			}

			if (tableLayoutUtil.isCellEmpty(cell)
					|| (mouse.x > tableLayoutUtil.getCalculatedLayoutWidth() || mouse.y > tableLayoutUtil.getCalculatedLayoutHeight())) {
				boolean emptycell = true;
					// NUCLEUSINT-496 check if the next cell is empty too, else do not allow drop
					try {
						Object transferData = null;
						if (event instanceof DropTargetDragEvent) {
							transferData = ((DropTargetDragEvent) event).getTransferable().getTransferData(new DataFlavor(DragElement.class, "Element"));
							if (transferData == null) {
								transferData =  ((DropTargetDragEvent) event).getTransferable().getTransferData(new DataFlavor(WYSIWYGComponent.class, "Element"));
							}
						} else if (event instanceof DropTargetDropEvent) {
							transferData = ((DropTargetDropEvent) event).getTransferable().getTransferData(new DataFlavor(DragElement.class, "Element"));
							if (transferData == null) {
								transferData =  ((DropTargetDropEvent) event).getTransferable().getTransferData(new DataFlavor(WYSIWYGComponent.class, "Element"));
							}
						}
						
						if (transferData instanceof DragElement) {
							DragElement element = (DragElement) transferData;
							if (element.isLabeledComponent()) {
								LayoutCell secondCell = tableLayoutUtil.getLayoutCellByPosition(cell.getCellX() + 1, cell.getCellY());
	
								// if the next cell is a small cell with the width of the margin between, use the next one
								if (secondCell.getCellDimensions().width == InterfaceGuidelines.MARGIN_BETWEEN)
									secondCell = tableLayoutUtil.getLayoutCellByPosition(secondCell.getCellX() +1, secondCell.getCellY());
								
								log.debug(cell.toString());
								
								if (!tableLayoutUtil.isCellEmpty(secondCell)) {
									emptycell = false;
									if (mouse.x > tableLayoutUtil.getCalculatedLayoutWidth() || mouse.y > tableLayoutUtil.getCalculatedLayoutHeight()) {
										emptycell = true;
									}
								}
							}
						} else if (transferData.equals(WYSIWYGComponent.class)) {
							//do nothing. we do not have to check if labeled component and if next cell is available.
						}
					}
					catch (ClassCastException e) {
						// nothing to do
						LOG.info("isDropAllowed: " + e);
					}
					catch(UnsupportedFlavorException e) {
						// nothing to do
						LOG.info("isDropAllowed: " + e);
					}
					catch(IOException e) {
						// nothing to do
						LOG.info("isDropAllowed: " + e);
					}
					
				/** if editor is running in standardmode dropping is only allowed to a empty cell */
				return emptycell;
			} 
		}
		return false;
	}

	/**
	 * Drag and Drop<br>
	 * The {@link WYSIWYGComponent} dragged from the {@link PaletteController} are added. Some elements are {@link TransferablePlaceholder} (This are Layoutcontaining Elements like {@link WYSIWYGSplitPane}).<br>
	 * If the Drop Action is allowed the {@link WYSIWYGComponent} is added.<br>
	 * This Method also checks if there is a {@link LayoutCell} avaible for this {@link WYSIWYGComponent}.<br> 
	 * If not it creates a new {@link LayoutCell} using {@link TableLayoutUtil#sliceCellToPieces(Point, java.awt.Rectangle, LayoutCell)}
	 */
	@Override
	public void drop(DropTargetDropEvent dtde) {
		glassPane.setCursor(null); // reset cursor. cause cursor flickering on dnd inside the component.
		tableLayoutUtil.getContainer().setCursor(null); // reset cursor. cause cursor flickering on dnd inside the component.
		if (chgDescriptor != null) {
			chgDescriptor.setContentChanged();
		}
		Point mouseLocation = dtde.getLocation();

		/** be sure the mouse is inside layout and not in the border area */
		if (mouseLocation.x < InterfaceGuidelines.MARGIN_LEFT)
			mouseLocation.x = InterfaceGuidelines.MARGIN_LEFT;
		if (mouseLocation.y < InterfaceGuidelines.MARGIN_TOP)
			mouseLocation.y = InterfaceGuidelines.MARGIN_TOP;
		
		Point p = (Point)mouseLocation.clone();
		
		tableLayoutUtil.setCurrentLayoutCell(p);
		current = tableLayoutUtil.getCurrentLayoutCell();
		
		// NUCLEUSINT-496 controlling the do while loop
		boolean processSecondComponent = false;

		if (!isDropAllowed(dtde)) {
			dtde.rejectDrop();
			hideGlassPane();
			return;
		}

		Component component = null;
		//NUCLEUSINT-496 the component for the labeled component
		Component componentAfterLabel = null;
		try {
			Object transferData =  ((DropTargetDropEvent) dtde).getTransferable().getTransferData(new DataFlavor(DragElement.class, "Element"));
			if (transferData == null) {
				transferData =  ((DropTargetDropEvent) dtde).getTransferable().getTransferData(new DataFlavor(WYSIWYGComponent.class, "Element"));
			}
			
			if (transferData instanceof DragElement) {
				DragElement element = (DragElement) transferData;
	
				String elem = element.getElement();
				String control = element.getControltype();
				String field = null;
				if (LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT.equals(elem)) {
					List<String> values = getParentEditorPanel().getMetaInformation().getFieldNamesByControlType(control);
					if (values.size() > 0) {
						//NUCLEUSINT-465
						field = WYSIWYGMetaInformationPicker.showPickDialog(getParentEditorPanel(), values, this);
						if (field == null) {
							return;
						}
					} else {
						throw new NuclosBusinessException(TABLELAYOUT_PANEL.NO_ATTRIBUTE_FOR_THIS_COMPONENT_AVAILABLE);
					}
					// NUCLEUSINT-496
				} else if ((LayoutMLConstants.ELEMENT_LABEL + LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT).equals(elem)) {
					control = elem.substring(LayoutMLConstants.ELEMENT_LABEL.length(), control.length());
					List<String> values = getParentEditorPanel().getMetaInformation().getFieldNamesByControlType(control);
					if (values.size() > 0) {
					
						field = WYSIWYGMetaInformationPicker.showPickDialog(getParentEditorPanel(), values, this);
						if (field == null) {
							return;
						}
					} else {
						throw new NuclosBusinessException(TABLELAYOUT_PANEL.NO_ATTRIBUTE_FOR_THIS_COMPONENT_AVAILABLE);
					}
				}
				Component droppedComponent = ComponentProcessors.getInstance().createComponent(elem, control, getParentEditorPanel().getController().getMetaInformation(), field);
	
				if (droppedComponent instanceof TransferablePlaceholder) {
					try {
						component = ((TransferablePlaceholder) droppedComponent).createComponent();
					} catch (CommonBusinessException e) {
						Errors.getInstance().showExceptionDialog(this, e);
					}
				} else {
					// NUCLEUSINT-496
					if (element.isLabeledComponent()) {
						componentAfterLabel = droppedComponent;
						component = ComponentProcessors.getInstance().createComponent(elem, LayoutMLConstants.ELEMENT_LABEL, getParentEditorPanel().getController().getMetaInformation(), field);
						componentAfterLabel.setBounds(mouseLocation.x + component.getPreferredSize().width + InterfaceGuidelines.MARGIN_BETWEEN, mouseLocation.y, componentAfterLabel.getPreferredSize().width, componentAfterLabel.getPreferredSize().height);
						processSecondComponent = true;
					} else {
						component = droppedComponent;
					}
				}
				component.setBounds(mouseLocation.x, mouseLocation.y, component.getPreferredSize().width, component.getPreferredSize().height);
			} else if (transferData.equals(WYSIWYGComponent.class)) {
				if (!wasComponentMoved() && mouseLocation.x > InterfaceGuidelines.MARGIN_LEFT && mouseLocation.y > InterfaceGuidelines.MARGIN_TOP) {
					if (getComponentToMove() != null) {
						try {
							//NUCLEUSINT-427 restore the previous set value for enabled
							WYSIWYGComponent wysiwygComponent = getComponentToMove();
							Boolean enabledValue = (Boolean) wysiwygComponent.getProperties().getProperty(WYSIWYGComponent.PROPERTY_ENABLED).getValue();

							if ((getParentEditorPanel().getController().getMode() & WYSIWYGEditorModes.STANDARD_MODE) == WYSIWYGEditorModes.STANDARD_MODE) {
								if (tableLayoutUtil.isCellEmpty(tableLayoutUtil.getCurrentLayoutCell())) {
									tableLayoutUtil.moveComponentTo(getComponentToMove(), getCurrentLayoutCell());
									((Component) getComponentToMove()).setEnabled(enabledValue);
									setComponentToMove(null);
								}
							} else if ((getParentEditorPanel().getController().getMode() & WYSIWYGEditorModes.EXPERT_MODE) == WYSIWYGEditorModes.EXPERT_MODE) {
								tableLayoutUtil.moveComponentTo(getComponentToMove(), getCurrentLayoutCell());
								
								((Component) getComponentToMove()).setEnabled(enabledValue);
								setComponentToMove(null);
							}

			   	 			final WYSIWYGLayoutEditorPanel parent = wysiwygComponent.getParentEditor();
							if (parent != null) {
								parent.getController().safePendingPropertyChanges();
							}
						} catch (HeadlessException e1) {
						} catch (NuclosBusinessException e1) {
						}
					}
					//NUCLEUSINT-273 //NUCLEUSINT-999
				} else if ((mouseLocation.x > InterfaceGuidelines.MARGIN_LEFT  && mouseLocation.y <= InterfaceGuidelines.MARGIN_TOP) || (mouseLocation.x <= InterfaceGuidelines.MARGIN_LEFT && mouseLocation.y > InterfaceGuidelines.MARGIN_TOP)) {
					LayoutCell selectedCell = tableLayoutUtil.getLayoutCell(mouseLocation);

					boolean existing = false;

					try {
						for (LayoutCell cell : cellsToEdit) {
							if (cell.equals(selectedCell)) {
								cellsToEdit.remove(selectedCell);
								existing = true;
							}
						}

						if (!existing) {
							cellsToEdit.push(selectedCell);
						}
					} catch (ConcurrentModificationException ex) {
						/** nothing to do, bad timing */
					}

					repaint();
				}

				hideGlassPane();
				
				return;
			}
		} catch (UnsupportedFlavorException e) {
			log.error(e);
			Errors.getInstance().showExceptionDialog(this, e);
		} catch (IOException e) {
			log.error(e);
			Errors.getInstance().showExceptionDialog(this, e);
		} catch (CommonBusinessException e) {
			log.error(e);
			Errors.getInstance().showExceptionDialog(this, e);
			return;
		}
		
		/** starting transaction for undo redo, otherwise every slicing is one undo step */
		tableLayoutUtil.getUndoRedoFunction().beginTransaction();
		// NUCLEUSINT-496 in a do while loop for processing labeled components
		do {
			/** this part toggles the slicing */
			if (getParentEditorPanel().isJustAddEnabled()) {
				log.debug("no slice, just use whats there...");
			} else if (current.getRightSide() >= mouseLocation.x && current.getBottomSide() >= mouseLocation.y) {
				log.debug("// mouse is inbetween layout");
				if (current.getCellDimensions().height > component.getBounds().height + InterfaceGuidelines.TOLERANCE_HEIGHT_EXISTING_CELL || current.getCellDimensions().width > component.getBounds().width + InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL) {
					log.debug("// cell to huge");
					if (current.getCellDimensions().height > component.getBounds().height + InterfaceGuidelines.TOLERANCE_HEIGHT_EXISTING_CELL && current.getCellDimensions().width > component.getBounds().width + InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL) {
						log.debug("// cell is completly oversized");
						current = tableLayoutUtil.sliceCellToPieces(mouseLocation, component.getBounds(), current);
					} else if (current.getCellDimensions().height > component.getBounds().height + InterfaceGuidelines.TOLERANCE_HEIGHT_EXISTING_CELL) {
						log.debug("// cell is just too high");
						current = tableLayoutUtil.sliceAFittingRowInIt(mouseLocation, component.getBounds(), current);
					} else if (current.getCellDimensions().width > component.getBounds().width + InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL) {
						log.debug("// cell is just too wide");
						current = tableLayoutUtil.sliceAFittingColumnInIt(mouseLocation, component.getBounds(), current);
					}
				} else {
					log.debug("// cell not to huge");
					if (current.getCellDimensions().height + InterfaceGuidelines.TOLERANCE_HEIGHT_EXISTING_CELL >= component.getBounds().height && current.getCellDimensions().width + InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL >= component.getBounds().width) {
						if (current.getBottomSide() - (component.getBounds().getHeight() + InterfaceGuidelines.MARGIN_TOP) < 0) {
							log.debug("// there is no margin on top if just added");
							mouseLocation.y = InterfaceGuidelines.MARGIN_TOP;
							current = tableLayoutUtil.createRowBeneath(mouseLocation, component.getBounds(), current);
						} else if (current.getRightSide() - (component.getBounds().getWidth() + InterfaceGuidelines.MARGIN_LEFT) < 0) {
							log.debug("// there is no margin on left if just added");
							mouseLocation.x = InterfaceGuidelines.MARGIN_LEFT;
							current = tableLayoutUtil.createColumnBeside(mouseLocation, component.getBounds(), current);
						} else {
							log.debug(" // current cell does fit perfectly, just add");
						}
					} else if (current.getCellDimensions().width + InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL >= component.getBounds().width) {
						log.debug("// current cells height is too small");
						current = tableLayoutUtil.createRowBeneath(mouseLocation, component.getBounds(), current);
					} else if (current.getCellDimensions().height + InterfaceGuidelines.TOLERANCE_HEIGHT_EXISTING_CELL >= component.getBounds().height) {
						log.debug("// current cells width is too small");
						current = tableLayoutUtil.createColumnBeside(mouseLocation, component.getBounds(), current);
					}
				}
			} else {
				log.debug("// mouse is outside existing layout");
				if (current.getRightSide() < mouseLocation.x && current.getBottomSide() < mouseLocation.y) {
					log.debug("// SE of layout");
					current = tableLayoutUtil.createCellBelowWhereNoLayoutIs(mouseLocation, component.getBounds(), current);
				} else if (current.getRightSide() < mouseLocation.x) {
					log.debug("// E of layout");
					log.debug(current.toString());
					log.debug(mouseLocation.toString());
					current = tableLayoutUtil.createCellOnTheRightSideWhereNoLayoutIs(mouseLocation, component.getBounds(), current);
				} else {
					log.debug("// S of layout");
					current = tableLayoutUtil.createCellBelowWhereNoLayoutIs(mouseLocation, component.getBounds(), current);
				}
			}
	
			/** the mouselocation is exactly at the upper left corner of the component, moving a bit to be inside the {@link LayoutCell} */
			mouseLocation.y = mouseLocation.y + 2;
			mouseLocation.x = mouseLocation.x + 2;
	
			/** now its the right cell */
			current = tableLayoutUtil.getLayoutCell(mouseLocation);
	
			/** adding the component to the layout */
			tableLayoutUtil.insertComponentTo((WYSIWYGComponent) component, current);
			
			if (usePreferredSizeInsteadofAbsoluteSizes) {
				tableLayoutUtil.modifyTableLayoutSizes(TableLayout.PREFERRED, false, current, true);
				tableLayoutUtil.modifyTableLayoutSizes(TableLayout.PREFERRED, true, current, true);
			}

			// NUCLEUSINT-496
			if(processSecondComponent) {
				if(componentAfterLabel != null) {
					mouseLocation.x = current.getRightSide() + InterfaceGuidelines.MARGIN_BETWEEN;
					mouseLocation.y = current.getTopSide();
					tableLayoutUtil.setCurrentLayoutCell(mouseLocation);
					current = tableLayoutUtil.getCurrentLayoutCell();
					component = componentAfterLabel;
					componentAfterLabel = null;
				} else {
					processSecondComponent = false;
				}
			}
			
		} while (processSecondComponent);
		
		/** slicing and adding complete, transaction is finished */
		tableLayoutUtil.getUndoRedoFunction().commitTransaction();
		
		cellsToEdit.clear();
		
		if (component instanceof WYSIWYGSubForm) {
			//NUCLEUSINT-909
			PropertiesPanel.showPropertiesForComponent((WYSIWYGSubForm)component, tableLayoutUtil);
			((WYSIWYGSubForm)component).finalizeInitialLoading();
		}
		
		updateUI();
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	/**
	 * MouseMotionListener
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		cellsToEdit.clear();
		updateUI();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.getPoint() != null) {
			tableLayoutUtil.setCurrentLayoutCell(e.getPoint());
		}
		if (e.isControlDown() && (e.getPoint().x <= InterfaceGuidelines.MARGIN_LEFT || e.getPoint().y <= InterfaceGuidelines.MARGIN_TOP)) {
			e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		updateUI();
	}

	/**
	 * MouseListener
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		////NUCLEUSINT-999
		this.requestFocus();
		
		if (e.getButton() == MouseEvent.BUTTON1) {
				performActionsOnLeftClick(e);
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			/** middle button has no action right now */
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			performActionsOnRightClick(e);
		}
	}
	
	/**
	 * This Method handles {@link MouseEvent#BUTTON1} Events.
	 * called by {@link #mouseClicked(MouseEvent)}
	 * @param e
	 */
	private void performActionsOnLeftClick(MouseEvent e) {
		if (e.getPoint().x < InterfaceGuidelines.MARGIN_LEFT || e.getPoint().y < InterfaceGuidelines.MARGIN_TOP) {
		}
		if (!wasComponentMoved() && e.getPoint().x > InterfaceGuidelines.MARGIN_LEFT && e.getPoint().y > InterfaceGuidelines.MARGIN_TOP) {
			if (getComponentToMove() != null) {
				try {
					//NUCLEUSINT-427 restore the previous set value for enabled
					WYSIWYGComponent component = getComponentToMove();
					Boolean enabledValue = (Boolean) component.getProperties().getProperty(WYSIWYGComponent.PROPERTY_ENABLED).getValue();

					if ((getParentEditorPanel().getController().getMode() & WYSIWYGEditorModes.STANDARD_MODE) == WYSIWYGEditorModes.STANDARD_MODE) {
						if (tableLayoutUtil.isCellEmpty(tableLayoutUtil.getCurrentLayoutCell())) {
							tableLayoutUtil.moveComponentTo(getComponentToMove(), getCurrentLayoutCell());
							((Component) getComponentToMove()).setEnabled(enabledValue);
							setComponentToMove(null);
						}
					} else if ((getParentEditorPanel().getController().getMode() & WYSIWYGEditorModes.EXPERT_MODE) == WYSIWYGEditorModes.EXPERT_MODE) {
						tableLayoutUtil.moveComponentTo(getComponentToMove(), getCurrentLayoutCell());
						
						((Component) getComponentToMove()).setEnabled(enabledValue);
						setComponentToMove(null);
					}
				} catch (HeadlessException e1) {
				} catch (NuclosBusinessException e1) {
				}
			}
			//NUCLEUSINT-273 //NUCLEUSINT-999
		} else if ((e.getPoint().x > InterfaceGuidelines.MARGIN_LEFT  && e.getPoint().y <= InterfaceGuidelines.MARGIN_TOP) || (e.getPoint().x <= InterfaceGuidelines.MARGIN_LEFT && e.getPoint().y > InterfaceGuidelines.MARGIN_TOP)) {
			performActionsOnLeftClickAndControlPressedForMultiEdit(e);
		}
	}
	
	/**
	 * This Method handles the "multiEdit".
	 * called by {@link #performActionsOnLeftClick(MouseEvent)}
	 * @see ChangeSizeMeasurementPopupMultipleItems
	 * @param e
	 */
	private void performActionsOnLeftClickAndControlPressedForMultiEdit(MouseEvent e){
		e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		LayoutCell selectedCell = tableLayoutUtil.getLayoutCell(e.getPoint());

		boolean existing = false;

		try {
			for (LayoutCell cell : cellsToEdit) {
				if (cell.equals(selectedCell)) {
					cellsToEdit.remove(selectedCell);
					existing = true;
				}
			}

			if (!existing) {
				cellsToEdit.push(selectedCell);
			}
		} catch (ConcurrentModificationException ex) {
			/** nothing to do, bad timing */
		}

		repaint();
	}
	
	/**
	 * Everything that should be done if {@link MouseEvent#BUTTON3} is clicked.<br>
	 * called by {@link #mouseClicked(MouseEvent)}
	 * @param e
	 */
	private void performActionsOnRightClick(MouseEvent e) {
		if (!cellsToEdit.isEmpty()) {
			changeSizeMeasurementPopupMultipleItems.showChangeSizeMeasurementPopupForColumns(cellsToEdit);
		} else {
			LayoutCell cellForEditing = tableLayoutUtil.getLayoutCell(e.getPoint());
			if (e.getY() <= InterfaceGuidelines.MARGIN_TOP && e.getX() > InterfaceGuidelines.MARGIN_LEFT)
				changeSizeMeasurementPopupColumn.showChangeSizeMeasurementPopupForColumns(cellForEditing);
			else if (e.getX() <= InterfaceGuidelines.MARGIN_LEFT && e.getY() > InterfaceGuidelines.MARGIN_TOP)
				changeSizeMeasurementPopupRows.showChangeSizeMeasurementPopupForRows(cellForEditing);
			else if (e.getX() > InterfaceGuidelines.MARGIN_LEFT && e.getY() > InterfaceGuidelines.MARGIN_TOP) {
				JPopupMenu layoutPanelPopup = new JPopupMenu();
				
				/**
				 * add a menuitem to the popup for toggeling the standardborder
				 * NUCLEUSINT-439
				 */
				LayoutCell layoutCell = tableLayoutUtil.getLayoutCellByPosition(0, 0);

				boolean borderIsShown = true;
				if (layoutCell.getCellHeight() == 0 && layoutCell.getCellWidth() == 0) {
					borderIsShown = false;
				}

				JMenuItem hideStandardBorder;
				if (borderIsShown)
					hideStandardBorder = new JMenuItem(COMPONENT_POPUP.LABEL_HIDE_STANDARD_BORDER);
				else
					hideStandardBorder = new JMenuItem(COMPONENT_POPUP.LABEL_SHOW_STANDARD_BORDER);

				hideStandardBorder.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						toggleStandardBorderVisible();
					}
				});

				layoutPanelPopup.add(hideStandardBorder);
				layoutPanelPopup.show(this, e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.updateUI();
		
		if (glassPane.isVisible())
			hideGlassPane();
	}

	/**
	 * ComponentListener
	 */
	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		updateUI();
	}
	
	/**
	 * This method toogles the StandardBorder.<b>
	 * It does set the outer Border to a size of 0.<b>
	 * This is the best Method, deleting would mess up quite a lot.<b>
	 * NUCLEUSINT-439
	 */
	private void toggleStandardBorderVisible(){
		LayoutCell upperLeftCorner = tableLayoutUtil.getLayoutCellByPosition(0, 0);
		
		boolean borderIsShown = true;
		if (upperLeftCorner.getCellHeight() == 0 && upperLeftCorner.getCellWidth() == 0){
			borderIsShown = false;
		}
		
		if (borderIsShown){
			tableLayoutUtil.modifyTableLayoutSizes(TableLayoutUtil.ACTION_TOGGLE_STANDARDBORDER, true, upperLeftCorner, false);
			tableLayoutUtil.modifyTableLayoutSizes(TableLayoutUtil.ACTION_TOGGLE_STANDARDBORDER, false, upperLeftCorner, false);
		} else {
			tableLayoutUtil.modifyTableLayoutSizes(InterfaceGuidelines.MARGIN_TOP, false, upperLeftCorner, false);
			tableLayoutUtil.modifyTableLayoutSizes(InterfaceGuidelines.MARGIN_LEFT, true, upperLeftCorner, false);
		}
	}

	/**
	 * Registering WYSIWYGLayoutEditorChangeDescriptor
	 * @param chgDescriptor
	 */
	public void setEditorChangeDescriptor(WYSIWYGLayoutEditorChangeDescriptor chgDescriptor) {
		this.chgDescriptor = chgDescriptor;
	}

	public WYSIWYGLayoutEditorChangeDescriptor getEditorChangeDescriptor() {
		return this.chgDescriptor;
	}

	/**
	 * Setting the current Layoutcell
	 * @param currentLayoutCell
	 * @param currentTableLayoutUtil
	 */
	public void setCurrentLayoutCell(LayoutCell currentLayoutCell, TableLayoutUtil currentTableLayoutUtil) {
		WYSIWYGLayoutEditorPanel parentContainer = getParentEditorPanel();
		parentContainer.setCurrentLayoutCell(currentLayoutCell, currentTableLayoutUtil);
	}

	/**
	 * @return LayoutCell currently under the mouse
	 */
	public LayoutCell getCurrentLayoutCell() {
		WYSIWYGLayoutEditorPanel parentContainer = getParentEditorPanel();
		return parentContainer.getCurrentLayoutCell();
	}

	/** 
	 * @return TableLayoutUtil that is controlling this {@link TableLayoutPanel}
	 */
	public TableLayoutUtil getCurrentTableLayoutUtil() {
		WYSIWYGLayoutEditorPanel parentContainer = getParentEditorPanel();
		return parentContainer.getCurrentTableLayoutUtil();
	}

	/**
	 * @return WYSIWYGComponent marked for move
	 */
	public WYSIWYGComponent getComponentToMove() {
		WYSIWYGLayoutEditorPanel parentContainer = getParentEditorPanel();
		return parentContainer.getComponentToMove();
	}

	/**
	 * This Method marks the {@link WYSIWYGComponent} to move.
	 * @param componentToMove
	 */
	public void setComponentToMove(WYSIWYGComponent componentToMove) {
		WYSIWYGLayoutEditorPanel parentContainer = getParentEditorPanel();
		parentContainer.setComponentToMove(componentToMove);
	}
	
	public void resetComponentToMove() {
		if (getComponentToMove() != null) {
			try {
				//NUCLEUSINT-427 restore the previous set value for enabled
				WYSIWYGComponent component = getComponentToMove();
				Boolean enabledValue = (Boolean) component.getProperties().getProperty(WYSIWYGComponent.PROPERTY_ENABLED).getValue();

				((Component) getComponentToMove()).setEnabled(enabledValue);
				setComponentToMove(null);
			} catch (HeadlessException e1) {
			}
		}
	}

	/**
	 * @return true if {@link WYSIWYGComponent} was moved
	 */
	public boolean wasComponentMoved() {
		WYSIWYGLayoutEditorPanel parentContainer = getParentEditorPanel();
		return parentContainer.wasComponentMoved();
	}

	/**
	 * @return UndoRedoFunction
	 */
	public UndoRedoFunction getUndoRedoFunction() {
		WYSIWYGLayoutEditorPanel parentContainer = getParentEditorPanel();
		return parentContainer.getUndoRedoFunction();
	}

	/**
	 * Method for finding the ParentEditor Panel.<br>
	 * 
	 * @return WYSIWYGLayoutEditorPanel
	 */
	public WYSIWYGLayoutEditorPanel getParentEditorPanel() {
		WYSIWYGLayoutEditorPanel parentContainer = null;

		if (this.getParent() != null) {
			if (this.getParent() instanceof WYSIWYGLayoutEditorPanel)
				parentContainer = (WYSIWYGLayoutEditorPanel) this.getParent();
			if (parentContainer == null) {
				if (this.getParent().getParent() instanceof WYSIWYGLayoutEditorPanel)
					parentContainer = (WYSIWYGLayoutEditorPanel) this.getParent().getParent();
			}
			if (parentContainer == null) {
				if (this.getParent().getParent().getParent() instanceof WYSIWYGLayoutEditorPanel)
					parentContainer = (WYSIWYGLayoutEditorPanel) this.getParent().getParent().getParent();
			}
		}
		return parentContainer;
	}
}
