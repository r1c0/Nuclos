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

package org.nuclos.client.ui.resplan.header;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.View;

import org.apache.commons.httpclient.util.LangUtils;
import org.apache.commons.lang.ObjectUtils;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.DefaultVisuals;
import org.jdesktop.swingx.renderer.LabelProvider;
import org.jdesktop.swingx.renderer.PainterAware;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.util.GeomUtils;
import org.nuclos.client.ui.util.MultiActionMouseHandler;
import org.nuclos.client.ui.util.MultiActionMouseHandler.MouseActionHandler;
import org.nuclos.client.ui.util.Orientation;
import org.nuclos.client.ui.util.PainterUtils;
import org.nuclos.client.ui.util.Range;

public class JHeaderGrid<E> extends JComponent implements ListDataListener {

	private static final Dimension DEFAULT_CELL_EXTENT = new Dimension(75, 20);

	private static final Dimension MINIMUM_CELL_EXTENT = new Dimension(10, 10);
	
	public static final String MODEL_PROPERTY = "model";
	public static final String CELL_EXTENT_PROPERTY = "cellExtent";
	public static final String CELL_RESIZING_RANGE_PROPERTY = "cellResizingRange";

	private static final int GRID_SIZE = 1;

	/** Constant for horizontal orientation, i.e. works as column header. */
	public static final int COLUMN = SwingConstants.HORIZONTAL;
	/** Constant for vertical orientation, i.e. works as row header. */
	public static final int ROW = SwingConstants.VERTICAL;

	private Orientation orientation;

	private TypesafeListModel<E> model;
	private E selectedValue;
	
	private int cellExtent = 75;
	private boolean cellExtentSet;
	
	private GroupMapper<E> categoryModel;
	private CellRendererPane cellRendererPane;
	private ComponentProvider<?> cellRendererProvider;

	private CategoryView[] groupings;

	private Paint gridColor;
	private Range cellResizingRange;

	private MultiActionMouseHandler mouseActionHandler;
	
	private Component cornerComponent;

	public JHeaderGrid(Orientation orientation, TypesafeListModel<E> primaryModel) {
		this(orientation, primaryModel, null);
	}

	public JHeaderGrid(Orientation orientation, TypesafeListModel<E> model, GroupMapper<E> categoryModel) {
		this.orientation = orientation;
		this.cellExtent = orientation.extentFrom(DEFAULT_CELL_EXTENT);

		this.gridColor = new Color(163, 172, 187);

		this.cellRendererPane = new CellRendererPane();
		this.cellRendererProvider = createDefaultCellRendererProvider();
		this.add(cellRendererPane);

		this.setModel(model);
		this.setCategoryModel(categoryModel);

		// Enable location-sensitive tool-tips (don't fiddle with setting a dummy tooltip,
		// registering the component with the ToolTipManager is the standard Swing way to do it)
		ToolTipManager.sharedInstance().registerComponent(this);

		initializeModel();
		
		mouseActionHandler = new MultiActionMouseHandler(new DefaultMouseHandler());
		mouseActionHandler.install(this);
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	public ChangeListener[] getChangeListeners() {
		return listenerList.getListeners(ChangeListener.class);
	}

	public int getCellExtent() {
		return cellExtent;
	}

	public void setCellExtent(int newExtent) {
		if (newExtent == -1) {
			setCellExtentImpl(getPreferredCellExtent(), false);
		} else if (newExtent >= 0) {
			setCellExtentImpl(Math.max(orientation.extentFrom(MINIMUM_CELL_EXTENT), newExtent), true);
		} else {
			throw new IllegalArgumentException("extent < 0");
		}
	}
	
	public boolean isCellExtentSet() {
		return cellExtentSet;
	}

	private void setCellExtentImpl(int newExtent, boolean extentSet) {
		int oldExtent = this.cellExtent;
		if (oldExtent != newExtent) {
			this.cellExtent = newExtent;
			this.cellExtentSet = extentSet;
			invalidate();
			if (this.categoryModel != null) {
				this.categoryModel.setCellExtent(this.orientation, newExtent);
			}
			firePropertyChange(CELL_EXTENT_PROPERTY, oldExtent, newExtent);
			fireStateChanged();
		}
	}
	
	public Component getCornerComponent() {
		return cornerComponent;
	}
	
	public void setCornerComponent(Component cornerComponent) {
		this.cornerComponent = cornerComponent;
		invalidate();
	}
	
	public Range getCellResizingRange() {
		return cellResizingRange;
	}
	
	public void setCellResizingRange(Range newRange) {
		Range oldRange = this.cellResizingRange;
		if (!ObjectUtils.equals(oldRange, newRange)) {
			repaint(oldRange);
			this.cellResizingRange = newRange;
			repaint(newRange);
			firePropertyChange(CELL_RESIZING_RANGE_PROPERTY, oldRange, newRange);
			fireStateChanged();
		}
	}

	protected void fireStateChanged() {
		ChangeEvent changeEvent = null;
		for (ChangeListener listener : listenerList.getListeners(ChangeListener.class)) {
			if (changeEvent == null) {
				changeEvent = new ChangeEvent(this);
			}
			listener.stateChanged(changeEvent);
		}
	}

	protected ComponentProvider<?> createDefaultCellRendererProvider() {
		return new LabelProvider(JLabel.CENTER) {
			@Override
			protected DefaultVisuals<JLabel> createDefaultVisuals() {
				return new  DefaultVisuals<JLabel>() {
					private final Painter<?> painter = new PainterUtils.HeaderPainter();
					@Override
					protected void configurePainter(JLabel renderingComponent, CellContext context) {
						if (renderingComponent instanceof PainterAware) {
							((PainterAware) renderingComponent).setPainter(painter);
						}
					}
				};
			}
		};
	}

	public TypesafeListModel<E> getModel() {
		return model;
	}

	public void setModel(TypesafeListModel<E> newModel) {
		TypesafeListModel<E> oldModel = this.model;
		if (oldModel != null) {
			oldModel.removeListDataListener(this);
		}
		this.model = newModel;
		if (newModel != null) {
			newModel.addListDataListener(this);
		}
		initializeModel();
		firePropertyChange(MODEL_PROPERTY, oldModel, newModel);
		fireStateChanged();
	}

	public E getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(E newValue) {
		E oldValue = this.selectedValue;
		this.selectedValue = newValue;
		if (!LangUtils.equals(oldValue, newValue)) {
			repaint();
		}
	}

	public GroupMapper<E> getCategoryModel() {
		return categoryModel;
	}

	public void setCategoryModel(GroupMapper<E> categoryModel) {
		this.categoryModel = categoryModel;
		if (categoryModel != null && categoryModel.getCellExtent(this.orientation) > 0) {
			setCellExtent(categoryModel.getCellExtent(this.orientation));
		}

		initializeModel();
		fireStateChanged();
	}

	protected void initializeModel() {
		int levelCount = getLevelCount();
		groupings = new CategoryView[levelCount];
		for (int level = 0; level < levelCount; level++) {
			CategoryView categoryView = groupings[level] = new CategoryView(level);
			int count = model.getSize();
			Object levelValue = null;
			int startIndex = -1;
			// TODO_RESPLAN: grouping must be senseful
			for (int index = 0; index < count; index++) {
				if (levelValue != null && levelValue.equals(getLevelComponent(level, index))) {
					continue;
				} else {
					if (startIndex != -1) {
						categoryView.add(levelValue, startIndex, index-1);
					}
					startIndex = index;
					levelValue = getLevelComponent(level, index);
				}
			}
			if (levelValue != null) {
				categoryView.add(levelValue, startIndex, count-1);
			}
		}
		
		if (!isCellExtentSet()) {
			setCellExtentImpl(getPreferredCellExtent(), false);
		}
		// TODO: this triggers calculation of group sizes
		calculateGroupingExtents();

		super.invalidate();
		repaint();
	}
	
	private void calculateGroupingExtents() {
		final Orientation groupOrient = orientation.opposite();
		int preferredGroupingsExtent = 0;
		for (CategoryView categoryView : groupings) {
			int preferredCategorySize = 0;
			for (HeaderCell cell : categoryView.cells) {
				JComponent renderer = setupCellRenderer(cell.levelValue, false);
				Dimension preferredSize = renderer.getPreferredSize();
				int preferredExtent = groupOrient.extentFrom(preferredSize);
				if (renderer instanceof JLabel) {
					// This is a some kind of hack for determing the preferred size of an HTML JLabel
					// using a given width or height. See also:
					// http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
					// NOTE: This seems to work for horizontal orientations (i.e. calculating the height for a
					// given width) while for vertical orientations often the dummy value 0 is returned.
					View view = (View) renderer.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
					if (view != null) {
						float w = (orientation == Orientation.HORIZONTAL) ? cellExtent : 0;
						float h = (orientation == Orientation.VERTICAL) ? cellExtent : 0;
						view.setSize(w, h);
						float span = (int) Math.ceil(view.getPreferredSpan(groupOrient.swingConstant()));
						preferredExtent = Math.max(preferredExtent, (int) Math.ceil(span));
					}
				}
				preferredCategorySize = Math.max(preferredCategorySize, preferredExtent + 2);
			}
			categoryView.preferredSize = preferredCategorySize;
			categoryView.size = preferredCategorySize;
			preferredGroupingsExtent += preferredCategorySize + GRID_SIZE;
		}

		// Real size and preferred size may vary
		int realGroupingsExtent = groupOrient.extentFrom(getSize());
		int delta = realGroupingsExtent - preferredGroupingsExtent;
		if (delta > 0) {
			int gap = delta / groupings.length;
			for (CategoryView v : groupings) {
				v.size += gap;
			}
			int rem = delta % groupings.length;
			if (rem > 0 && groupings.length > 0) {
				groupings[groupings.length-1].size += rem;
			}
		}
	}
	
	public int getPreferredCellExtent() {
		int preferredCellExtent = -1;
		for (CategoryView categoryView : groupings) {
			for (HeaderCell cell : categoryView.cells) {
				JComponent renderer = setupCellRenderer(cell.levelValue, false);
				Dimension preferredSize = renderer.getPreferredSize();
				preferredCellExtent = Math.max(preferredCellExtent, orientation.extentFrom(preferredSize) + 2);
			}
		}
		if (preferredCellExtent == -1) {
			preferredCellExtent = orientation.extentFrom(DEFAULT_CELL_EXTENT);
		}
		return preferredCellExtent;
	}

	@Override
	public void invalidate() {
		initializeModel();
		super.invalidate();
	}

	private void repaint(Range range) {
		if (range == null)
			return;
		Rectangle rect = new Rectangle(getWidth(), getHeight());
		orientation.updateRange(rect, range);
		rect.grow(GRID_SIZE + 1, GRID_SIZE + 1);
		repaint(rect);
	}

	public E getValue(int index) {
		return model.getElementAt(index);
	}

	public E getValueAt(Point pt) {
		int index = stripAtPoint(pt);
		return (index != -1) ? getValue(index) : null;
	}

	public String getStringAt(int index) {
		JComponent renderer = setupCellRenderer(model.getElementAt(index), false);
		return UIUtils.getRendererText(renderer);
	}

	public int getCount() {
		return model.getSize();
	}

	public ComponentProvider<?> getCellRendererProvider() {
		return cellRendererProvider;
	}

	public void setCellRendererProvider(ComponentProvider<?> cellRendererProvider) {
		this.cellRendererProvider = cellRendererProvider;
		initializeModel();
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
		initializeModel();
	}

	public int stripAtPoint(Point pt) {
		int coord = orientation.coordFrom(pt);
		return stripAtCoord(coord);
	}
	
	public int stripAtCoord(int coord) {
		int index = coord / (this.cellExtent + GRID_SIZE);
		if (index >= 0 && index < model.getSize()) {
			return index;
		}
		return -1;
	}

	public int[] stripRangeForRect(Rectangle rect) {
		if (rect == null) {
			return new int[] { 0, model.getSize()};
		}
		Range range = orientation.rangeFrom(rect);
		int index1 = stripAtCoord(range.coord);
		int index2 =  stripAtCoord(range.coord + range.extent);
		return new int[]{ index1 != -1 ? index1 : 0, index2 != -1 ? index2 + 1 : model.getSize() };
	}

	public Rectangle getStrip(int index) {
		return getStrip(index, index + 1);
	}

	/**
	 * Returns the "strip" rectangle for the given index range.
	 */
	public Rectangle getStrip(int startIndex, int endIndex) {
		return getStrip(startIndex, endIndex, null);
	}

	public Rectangle getStrip(int startIndex, int endIndex, Rectangle rect) {
		if (rect == null)
			rect = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		int startLine = getGridLine(startIndex);
		int endLine = getGridLine(endIndex);
		orientation.updateCoordExtent(rect, startLine + GRID_SIZE, endLine - startLine - GRID_SIZE);
		return rect;
	}

	public Rectangle getRect(int index) {
		return getStrip(index, index + 1, new Rectangle(getWidth(), getHeight()));
	}

	// TODO_RESPLAN: rounding issues
	public Rectangle getRect(int level, int index) {
		Rectangle rect = getRect(index);
		switch (orientation) {
		case HORIZONTAL:
			for (int i = 0; i < level; i++) {
				rect.y += groupings[i].size + GRID_SIZE;
			}
			rect.height = groupings[level].size;
			return rect;
		case VERTICAL:
			for (int i = 0; i < level; i++) {
				rect.x += groupings[i].size + GRID_SIZE;
			}
			rect.width = groupings[level].size;
			return rect;
		default:
			throw new IllegalStateException();
		}
	}

	// TODO_RESPLAN: rounding issues
	public Rectangle getRect(int level, int start, int end) {
		return getRect(level, start).union(getRect(level, end));
	}

	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet())
			return super.getPreferredSize();
		int preferredGroupingsExtent = 0;
		for (CategoryView v : groupings) {
			preferredGroupingsExtent += v.preferredSize + GRID_SIZE;
		}
		if (cornerComponent != null) {
			int cornerExtent = orientation.opposite().extentFrom(cornerComponent.getPreferredSize());
			preferredGroupingsExtent = Math.max(preferredGroupingsExtent, cornerExtent);
		}
		switch (orientation) {
		case HORIZONTAL:
			return new Dimension(getPreferredExtent() + GRID_SIZE, preferredGroupingsExtent);
		case VERTICAL:
			return new Dimension(preferredGroupingsExtent, getPreferredExtent() + GRID_SIZE);
		default:
			throw new IllegalStateException();
		}
	}

	public int getPreferredExtent() {
		return getGridLine(model.getSize()) + GRID_SIZE;
	}

	public Dimension getPreferredExtent(Dimension dimension) {
		if (dimension == null)
			dimension = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		orientation.updateExtent(dimension, getPreferredExtent());
		return dimension;
	}

	public int getGridSize() {
		return GRID_SIZE;
	}

	public int getGridLine(int index) {
		return index * (this.cellExtent + GRID_SIZE) - GRID_SIZE;
	}
	
	public int getGridLineLevel(int index) {
		if (groupings.length <= 1)
			return 0;
		int n = groupings.length - 1;
		for (int i = 0; i < n; i++) {
			if (groupings[i].isBoundary(index)) {
				return i;
			}
		}
		return n;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		int index = stripAtPoint(event.getPoint());
		if (index != -1) {
			JComponent renderer = setupCellRenderer(model.getElementAt(index), false);
			return renderer.getToolTipText();
		} else {
			return getToolTipText();
		}
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		initializeModel();
	}


	@Override
	public void intervalAdded(ListDataEvent e) {
		initializeModel();
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		initializeModel();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		super.paintComponent(g2d);
		
		g2d.setPaint(gridColor);

		Rectangle clipBounds = g.getClipBounds();
		
		int[] indices = stripRangeForRect(clipBounds);
		int startIndex = indices[0];
		int endIndex = indices[1];
		
		Point p1, p2;
		p1 = new Point(); p2 = new Point(getWidth(), getHeight());
		for (int i = startIndex; i <= endIndex; i++) {
			int gridLine = getGridLine(i);
			orientation.updateCoord(p1, gridLine);
			orientation.updateCoord(p2, gridLine);
			g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
		
		p1.setLocation(0, 0);
		p2.setLocation(getWidth(), getHeight());
		int groupGridLine = 0;
		for (CategoryView v : groupings) {
			groupGridLine += v.size;
			orientation.opposite().updateCoord(p1, groupGridLine);
			orientation.opposite().updateCoord(p2, groupGridLine);
			g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
			groupGridLine += GRID_SIZE;
		}
		
		for (int level = 0, levelCount = groupings.length; level < levelCount; level++) {
			for (HeaderCell cell : groupings[level].cells) {
				if (cell.endIndex < startIndex || cell.startIndex >= endIndex)
					continue;
				Rectangle rect = getRect(level, cell.startIndex, cell.endIndex);
				if (clipBounds != null && !clipBounds.intersects(rect))
					continue;
				paintHeaderCell(g2d, rect, cell.levelValue);
			}
		}
		
		if (cellResizingRange != null) {
			Rectangle rect = new Rectangle(getWidth(), getHeight());
			orientation.updateRange(rect, cellResizingRange);
			rect.grow(orientation.select(GRID_SIZE, 0), orientation.select(0, GRID_SIZE));
			g2d.setColor(new Color(0x9999ff, false));
			g2d.draw(rect);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
			g2d.fill(rect);
		}
	}

	private void paintHeaderCell(Graphics2D g2d, Rectangle rect, Object value) {
		Rectangle clipBounds = g2d.getClipBounds();
		if (clipBounds != null && !clipBounds.intersects(rect))
			return;
		boolean selected = (selectedValue != null && selectedValue.equals(value));
		JComponent renderer = setupCellRenderer(value, selected);
		cellRendererPane.paintComponent(g2d, renderer, this, rect);
		cellRendererPane.remove(renderer);
	}

	private JComponent setupCellRenderer(Object value, boolean selected) {
		GridHeaderContext context = new GridHeaderContext();
		context.installContext(this, value, 0, 0, selected, false, false, false);
		return cellRendererProvider.getRendererComponent(context);
	}

	public int getLevelCount() {
		return (categoryModel != null ? categoryModel.getCategoryCount() : 1);
	}

	// TODO_RESPLAN:
	protected Object getLevelComponent(int level, int index) {
		E value = model.getElementAt(index);
		if (categoryModel != null) {
			return categoryModel.getCategoryValue(level, value);
		} else {
			return value;
		}
	}

	private static class CategoryView {

		private List<HeaderCell> cells;
		private int preferredSize = 50;
		private int size = 50;

		public CategoryView(int level) {
			cells = new ArrayList<HeaderCell>();
		}

		public void add(Object levelValue, int startIndex, int endIndex) {
			cells.add(new HeaderCell(levelValue, startIndex, endIndex));
		}
		
		public boolean isBoundary(int index) {
			for (HeaderCell cell : cells) {
				if (index == cell.startIndex || index == cell.endIndex+1)
					return true;
			}
			return false;
		}
	}

	private static class HeaderCell {
		Object levelValue;
		int startIndex;
		int endIndex;

		public HeaderCell(Object levelValue, int startIndex, int endIndex) {
			this.levelValue = levelValue;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
	}

	public static class GridHeaderContext extends CellContext {

		@Override
		public JHeaderGrid<?> getComponent() {
			return (JHeaderGrid<?>) super.getComponent();
		}

		public void installContext(JHeaderGrid<?> component, Object value, int row, int column, boolean selected, boolean focused, boolean expanded, boolean leaf) {
			this.component = component;
			installState(value, row, column, selected, focused, expanded, leaf);
		}
	}

	private class DefaultMouseHandler extends MouseInputAdapter {
		
		private final Insets INSETS = new Insets(5, 5, 5, 5);
		
		private int resizingIndex;
		
		@Override
		public void mouseMoved(MouseEvent evt) {
			resizingIndex = -1;
			int stripAt = stripAtPoint(evt.getPoint());
			Cursor cursor = null;
			if (stripAt != -1) {
				Rectangle rect = getRect(stripAt);
				if (rect.contains(evt.getPoint())) {
					int compass = GeomUtils.findInsetDirection(rect, evt.getPoint(), INSETS);
					compass = orientation.normalizeCompassDirection(compass);
					switch (orientation.testCompassDirection(compass)) {
					case -1:
						resizingIndex = stripAt - 1;
						break;
					case 1:
						resizingIndex = stripAt;
						break;
					}
					if (resizingIndex != -1) {
						cursor = Cursor.getPredefinedCursor(GeomUtils.getResizeCursor(compass));
					}
				}
			}
			JHeaderGrid.this.setCursor(cursor);
		}
		
		@Override
		public void mouseExited(MouseEvent evt) {
			JHeaderGrid.this.setCursor(null);
		}

		@Override
		public void mousePressed(MouseEvent evt) {
			if (evt.getButton() == MouseEvent.BUTTON1) {
				if (resizingIndex != -1) {
					Rectangle rect = getRect(resizingIndex);
					mouseActionHandler.startHandler(new CellResizingHandler(orientation.rangeFrom(rect)), evt);
				}
			}
		}
	}
	
	private class CellResizingHandler extends MouseActionHandler {

		private final int anchor;
		private int extent;
		
		public CellResizingHandler(Range range) {
			this.anchor = range.coord;
			this.extent = range.extent;
		}

		@Override
		protected void start(MouseEvent evt) {
			super.start(evt);
			setCellResizingRange(new Range(anchor, extent));
		}
		
		@Override
		public void mouseDragged(MouseEvent evt) {
			int lead = orientation.coordFrom(evt.getPoint());
			extent = Math.max(orientation.extentFrom(MINIMUM_CELL_EXTENT), lead - anchor);
			setCellResizingRange(new Range(anchor, extent));
		}
		
		@Override
		public void mouseReleased(MouseEvent evt) {
			final Rectangle visibleRect = getVisibleRect();
			int visibleIndex = stripAtPoint(visibleRect.getLocation());
			int visibleOffset = orientation.coordFrom(getVisibleRect()) - getGridLine(visibleIndex);

			setCellResizingRange(null);
			setCellExtent(extent);

			orientation.updateCoord(visibleRect, getGridLine(visibleIndex) + visibleOffset);
			scrollRectToVisible(visibleRect);

			evt.consume();
			stop();
		}
	}
}
