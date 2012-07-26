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

package org.nuclos.client.ui.resplan;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.CellRendererPane;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.search.AbstractSearchable;
import org.nuclos.client.customcomp.resplan.ResPlanConstants;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.resplan.ResPlanTransferHandler.EntryWrapper;
import org.nuclos.client.ui.resplan.header.JHeaderGrid;
import org.nuclos.client.ui.resplan.header.TypesafeListModel;
import org.nuclos.client.ui.resplan.header.TypesafeListModel.StaticListModel;
import org.nuclos.client.ui.util.GeomUtils;
import org.nuclos.client.ui.util.Orientation;
import org.nuclos.client.ui.util.PainterUtils;
import org.nuclos.common2.LangUtils;

public class JResPlanComponent<R, T extends Comparable<? super T>, E, L> extends JComponent implements ResPlanModelListener, ChangeListener, Scrollable {

	private static final Logger LOG = Logger.getLogger(JResPlanComponent.class);

	public interface Area<R, T extends Comparable<? super T>> {
		
		R getResource();
		
		Interval<T> getInterval();
	}
	
	private class AreaImpl implements Area<R, T> {
		
		private R resource;
		private Interval<T> interval;
		
		private AreaImpl() {
		}
		
		private AreaImpl(R resource, Interval<T> interval) {
			this.resource = resource;
			this.interval = interval;
		}
		
		@Override
		public R getResource() {
			return resource;
		}
		
		@Override
		public Interval<T> getInterval() {
			return interval;
		}
		
		@Override
		public int hashCode() {
			return LangUtils.hash(resource, interval);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Area) {
				Area<?, ?> other = (Area<?, ?>) obj;
				return LangUtils.equals(resource, other.getInterval())
					&& LangUtils.equals(interval, other.getInterval());
			}
			return false;
		}
	}
	
	private final static BasicStroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, new float[]{2,5}, 0.0f);
	
	private boolean editable = true;
	private boolean dragEnabled = true;
	private DropMode dropMode = DropMode.USE_SELECTION;

	private final DefaultMouseHandler mouseHandler = new DefaultMouseHandler();
	private final DropTargetHandler dropTargetHandler = new DropTargetHandler();

	private static enum DecoratedView {
		MOUSE_OVER,
		RESIZING,
		DROP_LOCATION,
		BLANK;
	}
	
	/** All existing cell views. */
	private List<CellView> cellViewList = new ArrayList<CellView>();
	private Map<Object, CellView> cellViewMap = new HashMap<Object, CellView>();
	
	/** All existing relation views. */
	private List<RelationView> relViewList = new ArrayList<RelationView>();
	private Map<Object, RelationView> relViewMap = new HashMap<Object, RelationView>();

	/** Selected cell views (also members of the overall cellViews list). */
	private List<CellView> selectedCellViews = new ArrayList<CellView>();
	
	private List<RelationView> selectedRelationViews = new ArrayList<RelationView>();
	private E relateBegin;
	
	/** Decorated views. */
	private EnumMap<DecoratedView, CellView> decoratedViewMap = new EnumMap<DecoratedView, CellView>(DecoratedView.class);
	private EnumMap<DecoratedView, RelationView> decoratedRelationViewMap = new EnumMap<JResPlanComponent.DecoratedView, JResPlanComponent<R,T,E,L>.RelationView>(DecoratedView.class);
	
	private CellRendererPane cellRendererPane;
	private ComponentProvider<?> cellRendererProvider = new DefaultResPlanRendererProvider();
	private JRelation relationRenderer = new JRelation();
	private JMilestone milestoneRenderer = new JMilestone();
	private ResPlanModel<R, T, E, L> model;
	
	private TimeModel<T> timeModel;
	private Interval<T> timeHorizon;
	private StaticListModel<Interval<T>> timeIntervalModel;
	private Orientation orientation;
	
	// Some visuals
	private Paint gridColor;
	private Painter<? super Area<R, T>> timeslotBackgroundPainter;

	private final JHeaderGrid<R> resourceHeader;
	private final JHeaderGrid<Interval<T>> timelineHeader;
	
	private int relationPresentation;
	private int relationFromPresentation;
	private int relationToPresentation;

	public JResPlanComponent(ResPlanModel<R, T, E, L> model, TimeModel<T> timeModel) {
		this(Orientation.VERTICAL, model, timeModel);
	}
	
	public JResPlanComponent(Orientation orientation, ResPlanModel<R, T, E, L> model, TimeModel<T> timeModel) {
		this.setTransferHandler(new ResPlanTransferHandler());
		this.setAutoscrolls(true);

		addMouseMotionListener(mouseHandler);
		addMouseListener(mouseHandler);
		try {
			getDropTarget().addDropTargetListener(dropTargetHandler);
		} catch (TooManyListenersException ex) {
		}

		// Enable location-sensitive tool-tips (don't fiddle with setting a dummy tooltip,
		// registering the component with the ToolTipManager is the standard Swing way to do it)
		ToolTipManager.sharedInstance().registerComponent(this);
		// Install cellRenderPane
		cellRendererPane = new CellRendererPane();
		add(cellRendererPane);
		// Setup some visual defaults
		
		this.setOpaque(true);
		this.gridColor = new Color(163, 172, 187);
		
		this.orientation = orientation;
		this.timeIntervalModel = new StaticListModel<Interval<T>>();
		this.timelineHeader = new JHeaderGrid<Interval<T>>(orientation, timeIntervalModel);
		this.resourceHeader = new JHeaderGrid<R>(orientation.opposite(), new TypesafeListModel.StaticListModel<R>(model.getResources()));
		timelineHeader.addChangeListener(this);
		resourceHeader.addChangeListener(this);
		
		setTimeModel(timeModel);
		setModel(model);
	}
	
	//
	// Model and header accessors
	//
	
	public TimeModel<T> getTimeModel() {
		return timeModel;
	}
	
	public void setTimeModel(TimeModel<T> newTimeModel) {
		TimeModel<T> oldTimeModel = this.timeModel;
		if (oldTimeModel != newTimeModel) {
			this.timeModel = newTimeModel;
			if (timeHorizon != null) {
				timeIntervalModel.setData(timeModel.getTimeIntervals(timeHorizon.getStart(), timeHorizon.getEnd()));
			} else {
				timeIntervalModel.setData(Collections.<Interval<T>>emptyList());
			}
			initCellViews();
			firePropertyChange("timeModel", oldTimeModel, newTimeModel);
			revalidate();
		}
	}
	
	public ResPlanModel<R, T, E, L> getModel() {
		return model;
	}

	public void setModel(ResPlanModel<R, T, E, L> newModel) {
		ResPlanModel<R, T, E, L> oldModel = this.model;
		if (newModel != oldModel) {
			if (oldModel != null)
				oldModel.removeResPlanModelListener(this);
			this.model = newModel;
			if (newModel != null) {
				newModel.addResPlanModelListener(this);
			}
			initCellViews();
			firePropertyChange("model", oldModel, newModel);
		}
	}
	
	public JHeaderGrid<Interval<T>> getTimelineHeader() {
		return timelineHeader;
	}
	
	public JHeaderGrid<R> getResourceHeader() {
		return resourceHeader;
	}
	
	//
	// Configuring the rendering of this component and its sub-elements.
	//

	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public Orientation getOrientation() {
		return this.orientation;
	}
	
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
		this.timelineHeader.setOrientation(orientation);
		this.resourceHeader.setOrientation(orientation.opposite());
		configureEnclosingScrollPane();
		initCellViews();
	}
	
	public Paint getGridColor() {
		return gridColor;
	}
	
	public void setGridColor(Paint gridColor) {
		if (gridColor == null) {
			throw new IllegalArgumentException("Grid color is null");
		}
		this.gridColor = gridColor;
	}

	public ComponentProvider<?> getCellRendererProvider() {
		return cellRendererProvider;
	}
	
	public void setCellRendererProvider(ComponentProvider<?> cellRendererProvider) {
		if (cellRendererProvider == null) {
			throw new IllegalArgumentException("Renderer is null");
		}
		this.cellRendererProvider = cellRendererProvider;
	}
	
	public Painter<? super Area<R, T>> getTimeslotBackgroundPainter() {
		return timeslotBackgroundPainter;
	}
	
	/**
	 * Sets the painter used for painting the timeslot backgrounds. The painter
	 * is parameterized with the timeslot, so that the painter can paint some
	 * timeslots differently than other.
	 */
	public void setTimeslotBackgroundPainter(Painter<? super Area<R, T>> painter) {
		this.timeslotBackgroundPainter = painter;
		repaint();
	}
	
	// TODO_RESPLAN
	public List<? extends Interval<T>> getTimeIntervals() {
		return timeIntervalModel.getData();
	}
	
	// TODO_RESPLAN: dependency on other method
	public void setTimeHorizon(Interval<T> newTimeHorizon) {
		this.timeHorizon = newTimeHorizon;
		timeIntervalModel.setData(timeModel.getTimeIntervals(timeHorizon.getStart(), timeHorizon.getEnd()));
		initCellViews();
		revalidate();
	}
	
	//
	// Internal methods
	//
	
	private void initCellViews() {
		relateBegin = null;
		cellViewList.clear();
		cellViewMap.clear();
		relViewList.clear();
		relViewMap.clear();
		if (timeModel == null || model == null)
			return;
		
		List<List<EntryCellView>> overlapCellGroups = new ArrayList<List<EntryCellView>>();
		
		for (R resource : model.getResources()) {
			List<EntryCellView> resourceCellViews = new ArrayList<EntryCellView>();
			for (E entry : model.getEntries(resource)) {
				Interval<T> span = model.getInterval(entry);
				// Test if the visible time horizon intersects the interval; if not, skip it
				if (!timeHorizon.intersects(span))
					continue;
				
				EntryCellView cellview = new EntryCellView(resource, entry, span);
				cellViewMap.put(model.getEntryId(entry), cellview);
				resourceCellViews.add(cellview);
			}
			
			// Sort views by start index/time
			Collections.sort(resourceCellViews, new Comparator<EntryCellView>() {
				@Override
				public int compare(EntryCellView v1, EntryCellView v2) {
					int cmp;
					int index1 = v1.startIndex;
					int index2 = v2.startIndex;
					if (index1 != index2) {
						cmp = (index1 < index2) ? -1 : 1;
					} else {
						cmp = v1.interval.getStart().compareTo(v2.interval.getStart());
						if (cmp == 0) {
							// This implicity sorts by duration
							cmp = v1.interval.getEnd().compareTo(v2.interval.getEnd());
						}
					}
					return cmp;
				};
			});
			
			int i = 0;
			int n = resourceCellViews.size();
			while (i < n) {
				EntryCellView v1 = resourceCellViews.get(i);
				i++;

				int endIndex = v1.endIndex;
				List<EntryCellView> group = null;
				
				EntryCellView v2;
				while (i < n && endIndex > (v2 = resourceCellViews.get(i)).startIndex) {
					if (group == null) {
						group = new ArrayList<EntryCellView>();
						overlapCellGroups.add(group);
						group.add(v1);
					}
					group.add(v2);
					endIndex = LangUtils.max(endIndex, v2.endIndex);
					
					i++;
				}
			}
			
			cellViewList.addAll(resourceCellViews);
		}
		
		if (model.getAllRelations() != null) {
			for (L relation : model.getAllRelations()) {
				EntryCellView from = (EntryCellView) cellViewMap.get(model.getRelationFromId(relation));
				EntryCellView to = (EntryCellView) cellViewMap.get(model.getRelationToId(relation));
				if (from != null && to != null) {
					RelationView relView = new RelationView(relation,from,to,false);
					relViewList.add(relView);
					relViewMap.put(model.getRelationId(relation), relView);
				}
			}
		}

		// Layout overlapping cells
		List<EntryCellView> overlapSectionsByIndex = new ArrayList<EntryCellView>();
		for (List<EntryCellView> group : overlapCellGroups) {
			// overlapSectionsByIndex represents the sections (which will be rendered side by side)
			// by their most recent entry (greatest end date).
			// When a new entry is processed (for-loop below), it will be either a) placed in an
			// existing section (start time of the entry >= end of the section's most recent entry),
			// or b) create a new section.
			overlapSectionsByIndex.clear();
			int overlapSectionsCount = 0;
			int index = 0;
			for (EntryCellView v : group) {
				boolean newSection = true;
				// Try to find an appropiate existing section: start with the current index
				// and cycle once through all existing sections 
				for (int di = 0; di < overlapSectionsCount; di++) {
					index = (index + 1) % overlapSectionsCount;
					EntryCellView vi = overlapSectionsByIndex.get(index);
					if (vi.getInterval().getEnd().compareTo(v.getInterval().getStart()) <= 0) {
						newSection = false;
						break;
					}
				}
				if (newSection) {
					overlapSectionsByIndex.add(v);
					overlapSectionsCount++;
					index = overlapSectionsCount - 1;
				} else {
					overlapSectionsByIndex.set(index, v);
				}
				v.setOverlapIndex(index);
			}
			for (EntryCellView v : group) {
				v.setOverlapSize(overlapSectionsCount);
			}
		}

		decoratedViewMap.clear();
		selectedCellViews.clear();
		
		repaint();
	}

	private CellView getDecoratedView(DecoratedView deco) {
		return decoratedViewMap.get(deco);
	}
	
	private void setDecoratedView(DecoratedView deco, CellView newView) {
		CellView oldView = decoratedViewMap.put(deco, newView);
		repaint(oldView);
		repaint(newView);
	}
	
	private RelationView getDecoratedRelationView(DecoratedView deco) {
		return decoratedRelationViewMap.get(deco);
	}
	
	private void setDecoratedRelationView(DecoratedView deco, RelationView newView) {
		RelationView oldView = decoratedRelationViewMap.put(deco, newView);
		repaint(oldView);
		repaint(newView);
	}
	
	//
	// Overridden standard methods
	//
	
	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet())
			return super.getPreferredSize();
		Dimension dimension = new Dimension();
		timelineHeader.getPreferredExtent(dimension);
		resourceHeader.getPreferredExtent(dimension);
		return dimension;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		configureEnclosingScrollPane();
	}
	
	//
	// Drag & Drop support
	//
	
	// By Swing conventions, we provide a property dragEnabled.
	public boolean getDragEnabled() {
		return dragEnabled;
	}

	public void setDragEnabled(boolean dragEnabled) {
		this.dragEnabled = dragEnabled;
	}

	public DropMode getDropMode() {
		return dropMode;
	}

	public void setDropMode(DropMode dropMode) {
		if (dropMode != null) {
			switch (dropMode) {
			case USE_SELECTION:
				this.dropMode = dropMode;
				return;
			}
		}
		throw new IllegalArgumentException("Illegal drop mode " + dropMode);
	}
	
	public Interval<T> getDropInterval() {
		CellView dropCellView = getDecoratedView(DecoratedView.DROP_LOCATION);
		return (dropCellView != null) ? dropCellView.interval : null;
	}

	protected void setDropLocation(Transferable transferable, Point p) {
		final CellView dropCellView = getDecoratedView(DecoratedView.DROP_LOCATION);

		if (transferable == null || p == null) {
			setDecoratedView(DecoratedView.DROP_LOCATION, null);
			return;
		}
		
		CellView transferredCellView = null;
		try {
			// Test if transferable contains a resplan entry and if we find the corresponding view
			// (Note: Even if it contains an entry, it still may be an entry from a different component)
			EntryWrapper transferredWrapper = ResPlanTransferHandler.RESPLAN_ENTRY_FLAVOR.extractTransferData(transferable);
			E entry = transferredWrapper.unwrap(model.getEntryType());
			transferredCellView = findCellView(entry);
		} catch (Exception e) {
			// ignore
			LOG.warn("setDropLocation failed: " + e, e);
		}
		R resource = getResourceAt(p);
		Interval<T> interval = getTimeIntervalAt(p);
		if (resource != null && interval != null) {
			if (dropCellView == null || !resource.equals(dropCellView.resource) || !interval.getStart().equals(dropCellView.interval.getStart())) {
				E entry = (transferredCellView != null ? transferredCellView.getEntry() : null);
				if (entry != null) {
					interval = timeModel.shiftInterval(transferredCellView.interval, interval.getStart());
				}
				setDecoratedView(DecoratedView.DROP_LOCATION, new EntryCellView(resource, entry, interval));
			}
		}
	}

	//
	// Scrollable support
	//
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(600, 400);
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		int increment = 0;
		if (this.orientation.swingConstant() == orientation) {
			increment = timelineHeader.getCellExtent() + timelineHeader.getGridSize();
		} else {
			increment = resourceHeader.getCellExtent() + resourceHeader.getGridSize();
		}
		return Math.max(increment, 10);
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return (orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	private void configureEnclosingScrollPane() {
		Container p = getParent();
		if (p instanceof JViewport) {
			Container c = p.getParent();
			if (c instanceof JScrollPane) {
				JScrollPane sp = (JScrollPane)c;
				JViewport viewport = sp.getViewport();
				if (viewport != null && viewport.getView() == this) {
					JHeaderGrid<?> columnHeader;
					JHeaderGrid<?> rowHeader;
					switch (orientation) {
					case VERTICAL:
						columnHeader = resourceHeader;
						rowHeader = timelineHeader;
						break;
					case HORIZONTAL:
						columnHeader = timelineHeader;
						rowHeader = resourceHeader;
						break;
					default:
						throw new IllegalStateException();
					}
					// (Re-)configure only if the corresponding view is empty or the opposite view
					if (sp.getColumnHeader() == null || sp.getColumnHeader().getView() == null || sp.getColumnHeader().getView() == rowHeader) {
						sp.setColumnHeaderView(columnHeader);
					}
					if (sp.getRowHeader() == null || sp.getRowHeader().getView() == null || sp.getRowHeader().getView() == columnHeader) {
						sp.setRowHeaderView(rowHeader);
					}
				}
			}
		}
	}
	
	public void scrollRectToArea(R resource, Interval<T> interval) {
		scrollRectToArea(resource, interval, false);
	}

	public void scrollRectToArea(R resource, Interval<T> interval, boolean topleft) {
		Rectangle rect = getAreaRect(resource, interval);
		if (rect != null) {
			Rectangle visibleRect = getVisibleRect();
			if (topleft) {
				rect.setSize(visibleRect.width, visibleRect.height);
			}
			scrollRectToVisible(rect);
		}
	}
	
	//
	//
	//
	
	public Area<R, T> getSelectedBlankArea() {
		return getDecoratedView(DecoratedView.BLANK);
	}
	
	public L getRelationAt(Point p) {
		RelationView relView = getRelationViewAt(p);
		if (relView != null) {
			return relView.getRelation();
		}
		return null;
	}
	
	public List<L> getSelectedRelations() {
		List<L> relations = new ArrayList<L>();
		for (RelationView relView : selectedRelationViews) {
			relations.add(relView.getRelation());
		}
		return relations;
	}
	
	public void setSelectedRelations(List<L> relations) {
		for (RelationView relView : selectedRelationViews) {
			relView.invalidate();
		}
		selectedRelationViews.clear();
		for (L rel : relations) {
			RelationView relView = findRelationView(rel);
			if (relView != null)
				select(relView, true);
		}
	}
	
	public E getEntryAt(Point p) {
		CellView cellView = getCellViewAt(p);
		if (cellView != null) {
			return cellView.getEntry();
		}
		return null;
	}
	
	public List<E> getSelectedEntries() {
		List<E> entries = new ArrayList<E>();
		for (CellView cellView : selectedCellViews) {
			entries.add(cellView.getEntry());
		}
		return entries;
	}
	
	public void setSelectedEntries(List<E> entries) {
		for (CellView cellView : selectedCellViews) {
			cellView.invalidate();
		}
		selectedCellViews.clear();
		for (E entry : entries) {
			CellView cellView = findCellView(entry);
			if (cellView != null)
				select(cellView, true);
		}
	}
	
	private CellView findCellView(E entry) {
		if (entry != null) {
			return cellViewMap.get(model.getEntryId(entry));
		}
		return null;
	}

	private CellView getCellViewAt(Point p) {
		if (p == null)
			return null;
		for (CellView cellView : cellViewList) {
			if (cellView.contains(p))
				return cellView;
		}
		// TODO_RESPLAN: include dummy ?!?
		return null;
	}
	
	private RelationView findRelationView(L relation) {
		if (relation != null) {
			return relViewMap.get(model.getRelationId(relation));
		}
		return null;
	}
	
	private RelationView getRelationViewAt(Point p) {
		if (p == null) {
			return null;
		}
		for (RelationView relView : relViewList) {
			if (relView.contains(p)) {
				return relView;
			}
		}
		return null;
	}
	
	/**
	 * Fills the rect with the area of the given timeslot span.
	 * Note that this method fills only one axis of the rectangle, i.e.
	 * either x/width or y/height.
	 * @see #getResourceRectImpl
	 */
	private void getIntervalStripImpl(Interval<T> interval, Rectangle rect, boolean exact) {
		int startIndex = Intervals.findStartIndex(getTimeIntervals(), interval.getStart());
		int endIndex = Intervals.findEndIndex(getTimeIntervals(), interval.getEnd(), startIndex);
		getIntervalStripImpl(startIndex, endIndex, rect);
		if (exact && endIndex > startIndex) {
			int dcoord = 0;
			int dextent = 0;

			Interval<T> realStartInterval = timelineHeader.getValue(startIndex);
			if (realStartInterval.getStart().compareTo(interval.getStart()) < 0) {
				double d = ((double) timeModel.getDuration(realStartInterval.getStart(), interval.getStart()))
					/ ((double) timeModel.getDuration(realStartInterval.getStart(), realStartInterval.getEnd()));
				if (d > 0 && d < 1.0) {
					dcoord = (int) Math.round(d * timelineHeader.getCellExtent());
					dextent = -dcoord;
				}
			}
			
			Interval<T> realEndInterval = timelineHeader.getValue(endIndex - 1);
			if (realEndInterval.getEnd().compareTo(interval.getEnd()) > 0) {
				double d = ((double) timeModel.getDuration(interval.getEnd(), realEndInterval.getEnd()))
					/ ((double) timeModel.getDuration(realEndInterval.getStart(), realEndInterval.getEnd()));
				if (d > 0 && d < 1.0) {
					dextent -= (int) Math.round(d * timelineHeader.getCellExtent());
				}
			}
			
			if (dcoord != 0)
				orientation.updateCoord(rect, orientation.coordFrom(rect) + dcoord);
			if (dextent != 0)
				orientation.updateExtent(rect, orientation.extentFrom(rect) + dextent);
		}
	}
	
	private void getIntervalStripImpl(int startIndex, int endIndex, Rectangle rect) {
		timelineHeader.getStrip(startIndex, endIndex, rect);
	}

	/**
	 * Fills the rect with the area of the given resource.
	 * Note that this method fills only one axis of the rectangle, i.e.
	 * either x/width or y/height.
	 * @see #getTimeslotRectImpl
	 */
	private void getResourceRectImpl(R resource, Rectangle rect) {
		int resourceIndex = model.getResources().indexOf(resource);
		resourceHeader.getStrip(resourceIndex, resourceIndex + 1, rect);
	}
	
	/**
	 * Gets the rectangle for the given resource and start/end span.
	 */
	public Rectangle getCellRect(R resource, Interval<T> interval) {
		return getCellRect(resource, interval, false);
	}

	private Rectangle getCellRect(R resource, Interval<T> interval, boolean exact) {
		Rectangle rect = new Rectangle();
		getResourceRectImpl(resource, rect);
		getIntervalStripImpl(interval, rect, exact);
		return rect;
	}
	
	/**
	 * Gets the rectangle for the given area.
	 */
	public Rectangle getAreaRect(Area<R, T> area) {
		if (area == null)
			return null;
		return getAreaRect(area.getResource(), area.getInterval());
	}
	
	public Rectangle getAreaRect(R resource, Interval<T> interval) {
		Rectangle rect = new Rectangle();
		if (resource != null)
			getResourceRectImpl(resource, rect);
		if (interval != null)
			getIntervalStripImpl(interval, rect, false);
		return rect;
	}

	// TODO_RESPLAN: remove
	public T getTimeAt(Point p, int direction) {
		Interval<T> interval = getTimeIntervalAt(p);
		return (interval != null ? interval.get(direction) : null);
	}
	
	public Interval<T> getTimeIntervalAt(Point p) {
		int index = timelineHeader.stripAtPoint(p);
		return (index != -1) ? timelineHeader.getValue(index) : null;
	}
	
	public R getResourceAt(Point p) {
		int index = resourceHeader.stripAtPoint(p);
		return (index != -1) ? resourceHeader.getValue(index) : null;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g.create();

		paintBackground(g2d);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

		Rectangle clipBounds = g.getClipBounds();
		if (clipBounds != null) {
			clipBounds.grow(2, 2);
		}
		
		Collection<CellView> decoratedViews = decoratedViewMap.values();
		for (CellView cellView : cellViewList) {
			// Decorated views are painted separataly on top
			if (decoratedViews.contains(cellView))
				continue;
			paintCell(g2d, clipBounds, cellView);
		}
		
		for (RelationView relView : relViewList) {
			paintRelation(g2d, clipBounds, relView);
		}

		paintCell(g2d, clipBounds, decoratedViewMap.get(DecoratedView.MOUSE_OVER));
		paintRelation(g2d, clipBounds, decoratedRelationViewMap.get(DecoratedView.MOUSE_OVER));

		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.75f));

		paintCell(g2d, clipBounds, decoratedViewMap.get(DecoratedView.BLANK));
		paintCell(g2d, clipBounds, decoratedViewMap.get(DecoratedView.RESIZING));
		paintCell(g2d, clipBounds, decoratedViewMap.get(DecoratedView.DROP_LOCATION));
		
		paintRelation(g2d, clipBounds, decoratedRelationViewMap.get(DecoratedView.DROP_LOCATION));
		
		g2d.dispose();
	}
	
	private static void paintCell(Graphics2D g2d, Rectangle clipBounds, JResPlanComponent<?, ?, ?, ?>.CellView view) {
		if (view != null) {
			Rectangle rect = view.getRect();
			if (clipBounds.intersects(rect))
				view.paintCell(g2d, rect);
		}
	}
	
	private static void paintRelation(Graphics2D g2d, Rectangle clipBounds, JResPlanComponent.RelationView view) {
		if (view != null) {
			Rectangle rect = view.getRect();
			if (clipBounds.intersects(rect))
				view.paintRelation(g2d, rect);
		}
	}

	private void paintBackground(Graphics2D g2d) {
		int width = getWidth();
		int height = getHeight();
		
		Rectangle clipBounds = g2d.getClipBounds();
		
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		if (timeslotBackgroundPainter != null) {
			int[] visibleResIndices = resourceHeader.stripRangeForRect(clipBounds);
			int[] visibleTimeIndices = timelineHeader.stripRangeForRect(clipBounds);
			
			// For performance reason, we create only one Area/Rectangle instance and
			// reuse it during rendering
			AreaImpl area = new AreaImpl();
			Rectangle rect = new Rectangle();
			for (int ti = visibleTimeIndices[0], tn = visibleTimeIndices[1]; ti < tn; ti++) {
				rect.setBounds(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
				timelineHeader.getStrip(ti, ti + 1, rect);
				area.interval = timelineHeader.getValue(ti);
				orientation.updateExtent(rect, orientation.extentFrom(rect) + 1);
				for (int ri = visibleResIndices[0], rn = visibleResIndices[1]; ri < rn; ri++) {
					resourceHeader.getStrip(ri, ri + 1, rect);
					if (clipBounds != null && !clipBounds.intersects(rect))
						continue;
					area.resource = resourceHeader.getValue(ri);
					PainterUtils.paint(timeslotBackgroundPainter, g2d, area, rect);
				}
			}
		}

		Stroke oldStroke = g2d.getStroke();
		g2d.setPaint(gridColor);
		for (Orientation o : Orientation.values()) {
			JHeaderGrid<?> header = (o == orientation) ? timelineHeader : resourceHeader;
			
			int[] indices = header.stripRangeForRect(clipBounds);
			int startIndex = indices[0];
			int endIndex = indices[1];
			
			Point p1 = new Point(), p2 = new Point(width, height);
			int headerLevels = header.getLevelCount();
			for (int index = startIndex; index <= endIndex; index++) {
				int gridLine = header.getGridLine(index);
				o.updateCoord(p1, gridLine);
				o.updateCoord(p2, gridLine);
				boolean dashed = false;
				if (headerLevels > 1) {
					int gridLineLevel = header.getGridLineLevel(index);
					dashed = (gridLineLevel == headerLevels-1);
				}
				if (dashed) {
					g2d.setStroke(DASHED_STROKE);
					g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
					g2d.setStroke(oldStroke);
				} else {
					g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
			}
		}
	}

	private void repaint(RectView rv) {
		repaint(rv, null);
	}
	
	private void repaint(RectView rv, Rectangle add) {
		if (rv == null)
			return;
		Rectangle r = rv.getRect();
		if (add != null) {
			r = r.union(add);
		}
		super.repaint(r.x-3, r.y-3, r.width+6, r.height+6);
	}

	void selectForEvent(CellView cellView, RelationView relView, MouseEvent evt) {
		int button = evt.getButton();
		if (evt.getButton() == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3) {
			CellView blankView = getDecoratedView(DecoratedView.BLANK);
			boolean multiSelection = evt.isControlDown();
			if (!multiSelection) {
				for (CellView v : selectedCellViews)
					repaint(v);
				selectedCellViews.clear();
				for (RelationView v : selectedRelationViews)
					repaint(v);
				selectedRelationViews.clear();
			}
			if (cellView != null) {
				boolean removed = selectedCellViews.remove(cellView);
				if (!multiSelection || !removed)
					selectedCellViews.add(0, cellView);
				repaint(cellView);
				blankView = null;
			} else if (relView != null) {
				boolean removed = selectedRelationViews.remove(relView);
				if (!multiSelection || !removed)
					selectedRelationViews.add(0, relView);
				repaint(relView);
			} else {
				R resource = getResourceAt(evt.getPoint());
				Interval<T> interval = getTimeIntervalAt(evt.getPoint());
				if (blankView == null || blankView.resource != resource || !blankView.interval.contains(interval)) {
					blankView = new BlankView(resource, interval);
				}
			}
			setDecoratedView(DecoratedView.BLANK, blankView);
			evt.consume();
		}
	}
	
	void select(CellView cellView, boolean selected) {
		boolean modified = false;
		if (selected) {
			if (!selectedCellViews.contains(cellView)) {
				selectedCellViews.add(cellView);
				modified = true;
			}
		} else {
			modified  = selectedCellViews.remove(cellView);
		}
		if (modified ) {
			repaint(cellView);
		}
	}
	
	void select(RelationView relView, boolean selected) {
		boolean modified = false;
		if (selected) {
			if (!selectedRelationViews.contains(relView)) {
				selectedRelationViews.add(relView);
				modified = true;
			}
		} else {
			modified  = selectedRelationViews.remove(relView);
		}
		if (modified ) {
			repaint(relView);
		}
	}

	void setMouseOverCellView(CellView newMouseOverCellView) {
		setDecoratedView(DecoratedView.MOUSE_OVER, newMouseOverCellView);
	}
	
	void setMouseOverRelationView(RelationView newMouseOverRelationView) {
		setDecoratedRelationView(DecoratedView.MOUSE_OVER, newMouseOverRelationView);
	}
	
	@Override
	public String getToolTipText(MouseEvent event) {
		CellView cellView = getCellViewAt(event.getPoint());
		if (cellView != null) {
			return cellView.getToolTip();
		} else {
			return getToolTipText();
		}
	}

	private static final Insets RESIZE_INSETS = new Insets(5, 5, 5, 5);
	
	private interface RectView {
		public Rectangle getRect();
	}

	private abstract class CellView implements Area<R, T>, RectView {

		final R resource;
		final Interval<T> interval;
		final int startIndex;
		final int endIndex;
		/** (Cached) view rectangle, can be null. */
		private Rectangle rect;

		CellView(R resource, Interval<T> interval) {
			this.resource = resource;
			this.interval = interval;
			startIndex = Intervals.findStartIndex(getTimeIntervals(), interval.getStart());
			endIndex = Intervals.findEndIndex(getTimeIntervals(), interval.getEnd(), startIndex);
		}

		public String getAsText() {
			return null;
		}
		
		public String getToolTip() {
			return null;
		}

		@Override
		public R getResource() {
			return resource;
		}
		
		@Override
		public Interval<T> getInterval() {
			return interval;
		}
		
		public E getEntry() {
			return null;
		}
		
		public Rectangle getRect() {
			if (rect == null)
				rect = calculateRect();
			return rect;
		}
		
		public void setRect(Rectangle rect) {
			this.rect = rect;
		}
		
		protected Rectangle calculateRect() {
			return getCellRect(resource, interval);
		}

		public void invalidate() {
			rect = null;
		}
		
		public abstract void paintCell(Graphics2D g2d, Rectangle rect);
	
		public boolean contains(Point p) {
			return getRect().contains(p);
		}
	}
	
	private static class JMilestone extends JComponent {
		
		private final Color mouseOverColor = new Color(0xcc9999);
		
		private final Color selectionBackgroundColor = new Color(0xccccff);
		private final Color selectionBorderColor = new Color(0x8080ff);
		
		private Point start;
		private Point end;
		private int thickness;
		private boolean mouseOver;
		private boolean selected;
		
		public JMilestone() {
			super();
			setOpaque(true);
		}
		
		public void setValues(Point start, Point end, int thickness, boolean mouseOver, boolean selected) {
			this.start = start;
			this.end = end;
			this.thickness = thickness;
			this.mouseOver = mouseOver;
			this.selected = selected;
		}
		
		@Override
		public void paint(Graphics g) {	
			Graphics2D g2 = (Graphics2D) g;
			
			Polygon p = new Polygon();
			p.addPoint(start.x, start.y);
			if (start.x != end.x) {
				p.addPoint(start.x + thickness/2, start.y - thickness/2);
				p.addPoint(end.x - thickness/2, start.y - thickness/2);
				p.addPoint(end.x, end.y);
				p.addPoint(end.x - thickness/2, end.y + thickness/2);
				p.addPoint(start.x + thickness/2, end.y + thickness/2);
			} else {
				p.addPoint(start.x + thickness/2, start.y + thickness/2);
				p.addPoint(start.x + thickness/2, end.y - thickness/2);
				p.addPoint(end.x, end.y);
				p.addPoint(end.x - thickness/2, end.y - thickness/2);
				p.addPoint(end.x - thickness/2, start.y + thickness/2);
			}
			g2.setColor(selected?selectionBackgroundColor:Color.GRAY);
			g2.fillPolygon(p);
			if (mouseOver || selected) {
				g2.setColor(selected?selectionBorderColor:mouseOverColor);
				g2.drawPolygon(p);
				p.translate(-1, -1);
				g2.drawPolygon(p);
				p.translate(0, 2);
				g2.drawPolygon(p);
				p.translate(2, 0);
				g2.drawPolygon(p);
				p.translate(0, -2);
				g2.drawPolygon(p);
			} else {
				g2.setColor(Color.BLACK);
				g2.drawPolygon(p);
			}
		}
	}
	
	private static class JRelation extends JComponent {
		
		private final Color mouseOverColor = new Color(0xcc9999);
		
		private final Color selectionColor = new Color(0x8080ff);

		private List<Point> points = new ArrayList<Point>();
		private List<Point> dots = new ArrayList<Point>();
		private Point leftArrow = null;
		private Point rightArrow = null;
		private Point topArrow = null;
		private Point bottomArrow = null;
		
		private boolean dnd = false;
		private boolean selected = false;
		private boolean mouseOver = false;
		
		public JRelation() {
			super();
			setOpaque(true);
		}
		
		public void clear() {
			points.clear();
			dots.clear();
			leftArrow = null;
			rightArrow = null;
			topArrow = null;
			bottomArrow = null;
			
			dnd = false;
			selected = false;
			mouseOver = false;
		}
		
		public void setDragAndDrop(boolean dnd) {
			this.dnd = dnd;
		}
		
		public void setSelected(boolean selected) {
			this.selected = selected;
		}
		
		public void setMouseOver(boolean mouseOver) {
			this.mouseOver = mouseOver;
		}
		
		public void addPoint(Point p) {
			this.points.add(p);
		}
		
		public void addDot(Point p) {
			this.dots.add(p);
		}
		
		public void setLeftArrow(Point p) {
			this.leftArrow = p;
		}
		
		public void setRightArrow(Point p) {
			this.rightArrow = p;
		}
		
		public void setTopArrow(Point p) {
			this.topArrow = p;
		}
		
		public void setBottomArrow(Point p) {
			this.bottomArrow = p;
		}

		@Override
		public void paint(Graphics g) {
			if (points.size() < 2) {
				return;
			}
			
			g.setColor(dnd?Color.MAGENTA:mouseOver?mouseOverColor:selected?selectionColor:Color.BLUE);
			Point p1 = points.get(0);
			for (int i = 1; i < points.size(); i++) {
				Point p2 = points.get(i);
				drawThickLine(g, p1.x, p1.y, p2.x, p2.y, selected?3:2);
				p1 = p2;
			}
			
			for (Point dot : dots) {
				g.fillOval(dot.x-3, dot.y-3, 6, 6);
			}
			if (leftArrow != null) {
				Polygon p = new Polygon();
				p.addPoint(leftArrow.x, leftArrow.y);
				p.addPoint(leftArrow.x+6, leftArrow.y-4);
				p.addPoint(leftArrow.x+6, leftArrow.y+4);
				g.fillPolygon(p);
			}
			if (rightArrow != null) {
				Polygon p = new Polygon();
				p.addPoint(rightArrow.x, rightArrow.y);
				p.addPoint(rightArrow.x-6, rightArrow.y-4);
				p.addPoint(rightArrow.x-6, rightArrow.y+4);
				g.fillPolygon(p);
			}
			if (topArrow != null) {
				Polygon p = new Polygon();
				p.addPoint(topArrow.x, topArrow.y);
				p.addPoint(topArrow.x-4, topArrow.y+6);
				p.addPoint(topArrow.x+4, topArrow.y+6);
				g.fillPolygon(p);
			}
			if (bottomArrow != null) {
				Polygon p = new Polygon();
				p.addPoint(bottomArrow.x, bottomArrow.y);
				p.addPoint(bottomArrow.x-4, bottomArrow.y-6);
				p.addPoint(bottomArrow.x+4, bottomArrow.y-6);
				g.fillPolygon(p);
			}
		}

		public void drawThickLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
			g.fillPolygon(getThickLine(x1, y1, x2, y2, thickness));
		}	
		
		static Polygon getThickLine(int x1, int y1, int x2, int y2, int thickness) {
			int dX = x2 - x1;
			int dY = y2 - y1;
			double lineLength = Math.sqrt(dX * dX + dY * dY);
			double scale = (double) (thickness) / (2 * lineLength);
			double ddx = -scale * (double) dY;
			double ddy = scale * (double) dX;
			ddx += (ddx > 0) ? 0.5 : -0.5;
			ddy += (ddy > 0) ? 0.5 : -0.5;
			int dx = (int) ddx;
			int dy = (int) ddy;

			int xPoints[] = new int[4];
			int yPoints[] = new int[4];

			xPoints[0] = x1 + dx;
			yPoints[0] = y1 + dy;
			xPoints[1] = x1 - dx;
			yPoints[1] = y1 - dy;
			xPoints[2] = x2 - dx;
			yPoints[2] = y2 - dy;
			xPoints[3] = x2 + dx;
			yPoints[3] = y2 + dy;
			
			return new Polygon(xPoints, yPoints, 4);
		}
	}
	
	private void growRelationRectangle(Rectangle origin) {
		if (orientation == Orientation.HORIZONTAL)
			origin.grow(20, 10);
		else
			origin.grow(10, 20);
	}
	
	private class RelationView implements RectView {
		
		private final L relation;
		
		private final RectView from;
		private final RectView to;
		private final boolean dnd;
		
		private final List<Polygon> selectionArea = new ArrayList<Polygon>();
		private Point lastPoint = null;
		private Rectangle rect;
		
		public RelationView (L relation, RectView from, RectView to, boolean dnd) {
			this.relation = relation;
			this.from = from;
			this.to = to;
			this.dnd = dnd;
		}
		
		public L getRelation() {
			return relation;
		}
		
		public Rectangle getRect() {
			Rectangle start = from.getRect();
			Rectangle end = to.getRect();
			
			Rectangle result = start.union(end);
			growRelationRectangle(result);
			
			return result;
		}

		private JComponent setupRenderer(Rectangle rect) {
			this.rect = rect;
			boolean selected = selectedRelationViews.contains(this);
			boolean mouseOver = getDecoratedRelationView(DecoratedView.MOUSE_OVER) == this;
			
			lastPoint = null;
			selectionArea.clear();
			relationRenderer.clear();
			relationRenderer.setDragAndDrop(dnd);
			relationRenderer.setSelected(selected);
			relationRenderer.setMouseOver(mouseOver);
			Rectangle start = from.getRect();
			Rectangle end = to.getRect();
			
			int xStart;
			int xEnd;
			if (start.x >= end.x) {
				xStart = start.x-end.x;
				xEnd = 0;
			} else {
				xEnd = end.x-start.x;
				xStart = 0;
			}
			int yStart;
			int yEnd;
			if (start.y >= end.y) {
				yStart = start.y-end.y;
				yEnd = 0;
			} else {
				yEnd = end.y-start.y;
				yStart = 0;
			}
			
			if (orientation == Orientation.HORIZONTAL) {
				xStart += start.width;
				yStart += start.height/2;
				xEnd += 0;
				yEnd += end.height/2;
			} else {
				xStart += start.width/2;
				yStart += start.height;
				xEnd += end.width/2;
				yEnd += 0;
			}
			
			
			switch (relationFromPresentation) {
			case ResPlanConstants.RELATION_ENDPOINT_PRESENTATION_DOT:
				addDot(xStart, yStart); break;
			case ResPlanConstants.RELATION_ENDPOINT_PRESENTATION_ARROW:
				if (orientation == Orientation.HORIZONTAL)
					setLeftArrow(xStart, yStart); 
				else 
					setTopArrow(xStart, yStart); 
				break;
			}
			
			addPoint(xStart, yStart);
			if (orientation == Orientation.HORIZONTAL)
				addPoint(xStart +10, yStart);
			else
				addPoint(xStart, yStart +10);
			
			if (relationPresentation == ResPlanConstants.RELATION_PRESENTATION_ORTHOGONAL) {
				if (orientation == Orientation.HORIZONTAL) {
					if (yStart <= yEnd) {
						addPoint(xStart +10, yStart + start.height/2 +3);
						addPoint(xEnd -10, yStart + start.height/2 +3);
					} else {
						addPoint(xStart +10, yStart - start.height/2 -3);
						addPoint(xEnd -10, yStart - start.height/2 -3);
					}
				} else {
					if (xStart <= xEnd) {
						addPoint(xStart + start.width/2 +3, yStart +10);
						addPoint(xStart + start.width/2 +3, yEnd -10);
					} else {
						addPoint(xStart - start.width/2 -3, yStart +10);
						addPoint(xStart - start.width/2 -3, yEnd -10);
					}
				}
			}
			
			if (orientation == Orientation.HORIZONTAL)
				addPoint(xEnd -10, yEnd);
			else
				addPoint(xEnd, yEnd -10);
			addPoint(xEnd, yEnd);
			
			switch (relationToPresentation) {
			case ResPlanConstants.RELATION_ENDPOINT_PRESENTATION_DOT:
				addDot(xEnd, yEnd); break;
			case ResPlanConstants.RELATION_ENDPOINT_PRESENTATION_ARROW:
				if (orientation == Orientation.HORIZONTAL)
					setRightArrow(xEnd, yEnd); 
				else
					setBottomArrow(xEnd, yEnd);
				break;
			}
			
			return relationRenderer;
		}
		
		private void addPoint(int x, int y) {
			Point p = adjustPoint(x, y);
			relationRenderer.addPoint(new Point(p));
			p.translate(rect.x, rect.y);
			if (lastPoint != null) {
				selectionArea.add(JRelation.getThickLine(lastPoint.x, lastPoint.y, p.x, p.y, 6));
			}
			lastPoint = p;
		}
		
		private void addDot(int x, int y) {
			relationRenderer.addDot(adjustPoint(x, y));
		}
		
		private void setLeftArrow(int x, int y) {
			relationRenderer.setLeftArrow(adjustPoint(x, y));
		}
		
		private void setRightArrow(int x, int y) {
			relationRenderer.setRightArrow(adjustPoint(x, y));
		}
		
		private void setTopArrow(int x, int y) {
			relationRenderer.setTopArrow(adjustPoint(x, y));
		}
		
		private void setBottomArrow(int x, int y) {
			relationRenderer.setBottomArrow(adjustPoint(x, y));
		}
		
		private Point adjustPoint(int x, int y) {
			if (orientation == Orientation.HORIZONTAL)
				return new Point(x+20, y+10);
			else 
				return new Point(x+10, y+20);
		}

		public void paintRelation(Graphics2D g2d, Rectangle rect) {
			JComponent renderer = setupRenderer(rect);
			JResPlanComponent.this.cellRendererPane.paintComponent(g2d, renderer, JResPlanComponent.this, rect);
			cellRendererPane.remove(renderer);
		}
		
		public boolean contains(Point p) {
			for (Polygon pol : selectionArea) {
				if (pol.contains(p)) {
					return true;
				}
			}
			return false;
		}
		
		public void invalidate() {
			// revalidates always
		}
		
	}
	
	private class EntryCellView extends CellView {

		private final E entry;
		
		private int overlapGroupIndex = 0;
		private int overlapGroupSize = -1;

		public EntryCellView(R resource, E entry, Interval<T> interval) {
			super(resource, interval);
			this.entry = entry;
		}
		
		public EntryCellView(R resource, Interval<T> interval) {
			super(resource, interval);
			this.entry = null;
		}
		
		@Override
		public E getEntry() {
			return entry;
		}
		
		public void setOverlapIndex(int index) {
			this.overlapGroupIndex = index;
		}
		
		public void setOverlapSize(int size) {
			this.overlapGroupSize = size;
		}
		
		@Override
		public Rectangle calculateRect() {
			Rectangle rect = getCellRect(resource, interval, true);
			
			Orientation overlapOrientation = orientation.opposite();
			rect.grow(-1, -1);
			if (overlapGroupIndex < overlapGroupSize) {
				double itemExtent = ((double) overlapOrientation.extentFrom(rect)) / overlapGroupSize;
				int itemPos = overlapOrientation.coordFrom(rect) + (int) (overlapGroupIndex * itemExtent);
				overlapOrientation.updateCoordExtent(rect, itemPos, (int) itemExtent - 1);
			}
			
			if (!model.getAllRelations().isEmpty()) {
				Object entryId = model.getEntryId(entry);
				boolean from = false;
				boolean to = false;
				for (L rel : model.getRelations(entry)) {
					if (LangUtils.equals(entryId, model.getRelationFromId(rel))) {
						from = true;
					}
					if (LangUtils.equals(entryId, model.getRelationToId(rel))) {
						to = true;
					}
				}
			
				if (orientation == Orientation.HORIZONTAL) {
					rect.x += 0;//(to?3:0);
					rect.y += 3;
					rect.width -= 0;//(from?3:0)+(to?3:0);
					rect.height -= 6;
				} else {
					rect.x += 3;
					rect.y += 0;//(to?3:0);
					rect.width -= 6;
					rect.height -= 0;//(from?3:0)+(to?3:0);
				}
			}
			
			return rect;
		}
		
		/**
		 * Setups the cell renderer for the given cell.
		 */
		private JComponent setupCellRenderer() {
			boolean selected = selectedCellViews.contains(this);
			boolean focused = false; // isFocusOwner();
			boolean mouseOver = getDecoratedView(DecoratedView.MOUSE_OVER) == this;
			boolean dropOn = getDecoratedView(DecoratedView.DROP_LOCATION) == this;
			int row = 0;
			int column = 0;
			ResPlanCellContext context = new ResPlanCellContext();
			context.installContext(JResPlanComponent.this, entry, row, column, selected, focused, false, false);
			context.setMouseOver(mouseOver);
			context.setDropOn(dropOn);
			
			if (model.isMilestone(entry)) {
				Rectangle r = getRect();
				if (orientation == Orientation.HORIZONTAL)
					milestoneRenderer.setValues(new Point(0, 0 + r.height/2), new Point(0 + r.width, 0 + r.height/2), Math.min(30, Math.min(r.height, r.width))-2, mouseOver, selected);
				else
					milestoneRenderer.setValues(new Point(0 + r.width/2, 0), new Point(0 + r.width/2, 0 + r.height), Math.min(30, Math.min(r.height, r.width))-2, mouseOver, selected);
				return milestoneRenderer;
			} else {
				JComponent renderer = cellRendererProvider.getRendererComponent(context);
				return renderer;
			}
		}
		
		/**
		 * Paints the cell
		 */
		@Override
		public void paintCell(Graphics2D g2d, Rectangle rect) {
			JComponent renderer = setupCellRenderer();
			JResPlanComponent.this.cellRendererPane.paintComponent(g2d, renderer, JResPlanComponent.this, rect);
			cellRendererPane.remove(renderer);
			/*
			if (getDecoratedView(DecoratedView.MOUSE_OVER) == this) {
				Graphics2D scratch = (Graphics2D) g2d.create();
				scratch.translate(rect.x, rect.y);
				Rectangle glowRect = new Rectangle(0, 0, rect.width, rect.height);
				GlowPathEffect glowEffect = new GlowPathEffect();
				glowEffect.setBrushColor(Color.YELLOW);
				glowEffect.apply(scratch, glowRect, rect.width, rect.height);
				scratch.dispose();
			}
			*/
		}

		@Override
		public String getAsText() {
			return UIUtils.getRendererText(setupCellRenderer());
		}
		
		@Override
		public String getToolTip() {
			JComponent renderer = setupCellRenderer();
			return renderer.getToolTipText();
		}
	}
	
	public String getEntryAsText(E entry) {
		CellView cellView = findCellView(entry);
		if (cellView != null) {
			return cellView.getAsText();
		}
		return null;
	}
	
	class BlankView extends CellView {
		
		private final Color COLOR = new Color(0x809999ff, true);

		BlankView(R resource, Interval<T> interval) {
			super(resource, interval);
		}

		@Override
		public void paintCell(Graphics2D g2d, Rectangle rect) {
			g2d.setPaint(COLOR);
			g2d.fill(rect);
		}
	}
	


	/** 
	 * This handler is a workaround for Sun's limited implementation of custom drag-and-drop,
	 * esp. that JComponent.setDropLocation() is package-protected.
	 * See also bug #6448332 and the forum's thread at http://forums.sun.com/thread.jspa?threadID=5422279.
	 */
	private class DropTargetHandler extends DropTargetAdapter {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			setDropLocation(dtde.getTransferable(), dtde.getLocation());
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			setDropLocation(dtde.getTransferable(), dtde.getLocation());
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			setDropLocation(null, null);
		}

		@Override
		public void drop(DropTargetDropEvent dtde) {
			setDropLocation(null, null);
		}
	}

	private class DefaultMouseHandler implements MouseInputListener {

		private MouseActionHandler currentHandler = null;
		private Point pressedPosition = null;
		private CellView pressedCellView = null;
		private RelationView pressedRelationView = null;
		private boolean dragReady;
		
		@Override
		public void mouseClicked(MouseEvent evt) {
			if (checkHandler()) {
				currentHandler.mouseClicked(evt);
				return;
			}
		}

		@Override
		public void mousePressed(MouseEvent evt) {
			if (checkHandler()) {
				currentHandler.mousePressed(evt);
				return;
			}
			dragReady = isEditable() && getDragEnabled();
			pressedPosition = evt.getPoint();
			pressedCellView = getCellViewAt(pressedPosition);
			pressedRelationView = getRelationViewAt(pressedPosition);
			selectForEvent(pressedCellView, pressedRelationView, evt);
		}
		
		@Override
		public void mouseDragged(MouseEvent evt) {
			if (checkHandler()) {
				currentHandler.mouseDragged(evt);
				return;
			}
			boolean leftShift = evt.getModifiersEx() == (MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK);
			boolean leftButton = (evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0;
			boolean rightButton = (evt.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0;
			if (dragReady && (leftButton || rightButton)) {
				double distance = evt.getPoint().distance(pressedPosition);
				if (distance > 0) {
					if (leftShift && pressedCellView != null && pressedCellView.getEntry() != null) {
						if (model.isCreateRelationAllowed()) {
							startHandler(new RelateMouseActionHandler(pressedCellView), evt);
						}
					} else if (leftButton && pressedCellView != null && pressedCellView.getEntry() != null) {
						int compass = GeomUtils.findInsetDirection(pressedCellView.getRect(), pressedPosition, RESIZE_INSETS);
						int direction = orientation.testCompassDirection(compass);
						if (direction == 0) {
							if (distance > DragSource.getDragThreshold()) {
								dragReady = false;
								setSelectedEntries(Collections.singletonList(pressedCellView.getEntry()));
								JComponent source = (JComponent) evt.getSource();
								getTransferHandler().exportAsDrag(source, evt, TransferHandler.MOVE);
							}
						} else if (model.isUpdateEntryAllowed(pressedCellView.getEntry())) {
							startHandler(new ResizeMouseActionHandler(pressedCellView, pressedPosition, direction), evt);
						}
					} else if (rightButton || pressedCellView == null) {
						R resource = getResourceAt(pressedPosition);
						Interval<T> interval = getTimeIntervalAt(pressedPosition);
						if (resource != null && interval != null) {
							BlankView view = new BlankView(resource, interval);
							startHandler(new BlankSelectionMouseActionHandler(view, pressedPosition, 1), evt);
						}
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent evt) {
			if (checkHandler()) {
				currentHandler.mouseReleased(evt);
				return;
			}
			dragReady = false;
			pressedPosition = null;
			pressedCellView = null;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			setMouseOverPosition(e.getPoint());
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			setMouseOverPosition(e.getPoint());
		}

		@Override
		public void mouseExited(MouseEvent e) {
			setMouseOverPosition(null);
		}
		
		private void startHandler(MouseActionHandler listener, MouseEvent evt) {
			if (currentHandler != null && currentHandler.isActive()) {
				currentHandler.stop();
			}
			currentHandler = listener;
			currentHandler.start(evt);
		}
		
		private boolean checkHandler() {
			if (currentHandler != null) {
				if (currentHandler.isActive()) {
					return true;
				} else {
					currentHandler = null;
				}
			}
			return false;
		}
		
		void setMouseOverPosition(Point p) {
			CellView mouseOverCellView = getCellViewAt(p);
			setMouseOverCellView(mouseOverCellView);
			
			if (mouseOverCellView == null) {
				RelationView mouserOverRelationView = getRelationViewAt(p);
				setMouseOverRelationView(mouserOverRelationView);
			} else {
				setMouseOverRelationView(null);
			}

			int compass = -1;
			if (mouseOverCellView != null) {
				compass = GeomUtils.findInsetDirection(mouseOverCellView.getRect(), p, RESIZE_INSETS);
				compass = orientation.normalizeCompassDirection(compass);
			}
			if (compass > 0) {
				setCursor(Cursor.getPredefinedCursor(GeomUtils.getResizeCursor(compass)));
			} else {
				setCursor(null);
			}
		}
	}
	
	private class RectangleViewImpl implements RectView {
		private Rectangle rect;
		@Override
		public Rectangle getRect() {
			return rect;
		}
		public void setRect(Rectangle rect) {
			this.rect = rect;
		}
	}
	
	private class RelateMouseActionHandler extends MouseActionHandler {
		
		protected final CellView relatingCellView;
		protected CellView dragOverCellView;
		protected final RectangleViewImpl dragView;
		protected Rectangle rectLastDrag;
		
		public RelateMouseActionHandler(CellView relatingCellView) {
			super();
			this.relatingCellView = relatingCellView;
			this.dragView = new RectangleViewImpl();
			decoratedRelationViewMap.put(DecoratedView.DROP_LOCATION, new RelationView(null, relatingCellView, dragView, true));
		}
		
		@Override
		public void mouseDragged(MouseEvent evt) {
			evt.consume();
			dragOverCellView = getCellViewAt(evt.getPoint());
			if (relatingCellView == dragOverCellView) {
				dragOverCellView = null;
			}
			if (dragOverCellView != null) {
				dragView.setRect(new Rectangle(dragOverCellView.getRect()));
			} else {
				dragView.setRect(new Rectangle(evt.getPoint()));
			}
			repaint(decoratedRelationViewMap.get(DecoratedView.DROP_LOCATION), rectLastDrag);
			rectLastDrag = new Rectangle(dragView.getRect());
			growRelationRectangle(rectLastDrag);
		}

		@Override
		public void mouseReleased(MouseEvent evt) {
			evt.consume();
			commit();
			stop();
		}
		
		protected void commit() {
			if (model.isCreateEntryAllowed() && dragOverCellView != null) {
				model.createRelation(relatingCellView.getEntry(), dragOverCellView.getEntry());
			}
		}
		
		protected void release() {
			setDecoratedRelationView(DecoratedView.DROP_LOCATION, null);
		}
		
		@Override
		public void stop() {
			release();
			super.stop();
		}
	}
	
	private abstract class SpanMouseActionHandler extends MouseActionHandler {
		
		protected final CellView resizingCellView;
		protected final Collection<RelationView> relationViews;
		protected final int direction;
		protected T start;
		protected T end;
		
		SpanMouseActionHandler(CellView cellView, Point anchor, int direction) {
			Interval<T> span = cellView.interval;
			this.resizingCellView = cellView;
			this.direction = direction;
			this.start = span.getStart();
			this.end = span.getEnd();
			
			this.relationViews = new ArrayList<JResPlanComponent<R,T,E,L>.RelationView>();
			for (L relation : model.getRelations(cellView.getEntry())) {
				RelationView relView = relViewMap.get(model.getRelationId(relation));
				if (relView != null) {
					relationViews.add(relView);
				}
			}
		}
		
		private Rectangle getRelationRectangle() {
			Rectangle rect = new Rectangle();
			for (RelationView relView : relationViews) {
				rect = rect.union(relView.getRect());
			}
			return rect;
		}
		
		@Override
		public void mouseDragged(MouseEvent evt) {
			evt.consume();
			repaint(resizingCellView);
			Rectangle relRectBefore = getRelationRectangle();

			Interval<T> interval = null;
			if (direction == -1) {
				T time = getTimeAt(evt.getPoint(), Interval.START);
				if (time != null) {
					interval = new Interval<T>(time, resizingCellView.interval.getEnd(), true);
				}
			} else if (direction == 1) {
				T time = getTimeAt(evt.getPoint(), Interval.END);
				if (time != null) {
					interval = new Interval<T>(resizingCellView.interval.getStart(), time, true);
				}
			}
			if (interval != null && !interval.isEmpty()) {
				start = interval.getStart(); 
				end = interval.getEnd();
				Rectangle cellRect = getCellRect(resizingCellView.resource, interval, true);
				resizingCellView.setRect(cellRect);
				repaint(resizingCellView);
			}
			
			Rectangle relRectAfter = getRelationRectangle();
			repaint(relRectBefore.union(relRectAfter));
		}
		
		@Override
		public void mouseReleased(MouseEvent evt) {
			evt.consume();
			commit();
			stop();
		}
		
		@Override
		public void stop() {
			repaint(resizingCellView);
			resizingCellView.invalidate();
			repaint(resizingCellView);
			release();
			super.stop();
		}
		
		protected abstract void commit();
		
		protected abstract void release();
	}
	
	private class ResizeMouseActionHandler extends SpanMouseActionHandler {
		
		ResizeMouseActionHandler(CellView cellView, Point anchor, int direction) {
			super(cellView, anchor, direction);
			setDecoratedView(DecoratedView.RESIZING, cellView);
		}
		
		@Override
		protected void commit() {
			model.updateEntry(resizingCellView.getEntry(), resizingCellView.resource, new Interval<T>(start, end));
		}
		
		@Override
		protected void release() {
			setDecoratedView(DecoratedView.RESIZING, null);
		}
	}
	
	private class BlankSelectionMouseActionHandler extends SpanMouseActionHandler {
		
		BlankSelectionMouseActionHandler(CellView cellView, Point anchor, int direction) {
			super(cellView, anchor, direction);
			setDecoratedView(DecoratedView.BLANK, cellView);
		}
		
		@Override
		protected void commit() {
			CellView cellView = new BlankView(resizingCellView.resource, new Interval<T>(start, end));
			setDecoratedView(DecoratedView.BLANK, cellView);
		}
		
		@Override
		protected void release() {
		}
	}	
	
	private static abstract class MouseActionHandler extends MouseInputAdapter {

		private boolean active;
		
		public boolean isActive() {
			return active;
		}
		
		public void start(MouseEvent evt) {
			active = true;
			evt.consume();
		}
		
		public void stop() {
			active = false;
		}
	}
	
	//
	// Event listeners
	//
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// at the moment, we simply reset all cell views
		initCellViews();
		invalidate();
	};

	@Override
	public void entryChanged(ResPlanModelEvent evt) {
		// at the moment, we simply reset all cell views
		initCellViews();
	}
	
	@Override
	public void resourceEntriesChanged(ResPlanModelEvent evt) {
		// at the moment, we simply reset all cell views
		initCellViews();
	}
	
	@Override
	public void resourcesChanged(ResPlanModelEvent evt) {
		// here we need to reset the resource header, too
		resourceHeader.setModel(new TypesafeListModel.StaticListModel<R>(model.getResources()));
		initCellViews();
		invalidate();
	}
	
	//
	// Searchable support
	//
	
	public class ResPlanSearchable extends AbstractSearchable {
		
		

		public ResPlanSearchable() {
		}

		@Override
		protected void findMatchAndUpdateState(Pattern pattern, int startRow, boolean backwards) {
			SearchResult searchResult = null;
			if (backwards) {
				for (int index = startRow; index >= 0 && searchResult == null; index--) {
					searchResult = findExtendedMatch(pattern, index);
				}
			} else {
				for (int index = startRow; index < getSize() && searchResult == null; index++) {
					searchResult = findExtendedMatch(pattern, index);
				}
			}
			updateState(searchResult);
		}

		@Override
		protected SearchResult findExtendedMatch(Pattern pattern, int index) {
			if (index >= 0) {
				int resCount = resourceHeader.getCount();
				String text = null;
				if (index < resCount) {
					text = resourceHeader.getStringAt(index);
				} else if (index - resCount < cellViewList.size()) {
					CellView cv = cellViewList.get(index - resCount);
					text = cv.getAsText();
				}
				if (text != null && text.length() > 0) {
					Matcher matcher = pattern.matcher(text);
					if (matcher.find()) {
						return createSearchResult(matcher, index, 0);
					}
				}
			}
			return null;
		}

		@Override
		protected int getSize() {
			return resourceHeader.getCount() + cellViewList.size();
		}

		@Override
		protected void moveMatchMarker() {
			resourceHeader.setSelectedValue(null);
			if (!hasMatch()) {
				return;
			}
			int index = lastSearchResult.getFoundRow();
			if (index >= 0) {
				int resCount = resourceHeader.getCount();
				if (index < resCount) {
					R resource = resourceHeader.getValue(index);
					if (resource != null) {
						resourceHeader.setSelectedValue(resource);
						JResPlanComponent.this.scrollRectToArea(resource, null, true);
					}
				} else if (index - resCount < cellViewList.size()) {
					CellView cv = cellViewList.get(index - resCount);
					E entry = cv.getEntry();
					if (entry != null) {
						JResPlanComponent.this.setSelectedEntries(Collections.singletonList(entry));
						JResPlanComponent.this.scrollRectToArea(cv.getResource(), cv.getInterval());
					}
				}
			}
		}

		@Override
		public JComponent getTarget() {
			return JResPlanComponent.this;
		}

		@Override
		protected void addHighlighter(Highlighter highlighter) {
		}

		@Override
		protected void removeHighlighter(Highlighter searchHighlighter) {
		}

		@Override
		protected Highlighter[] getHighlighters() {
			return new Highlighter[0];
		}
	}
	
	public void setRelationPresentation(int relationPresentation) {
		this.relationPresentation = relationPresentation;
	}

	public void setRelationFromPresentation(int relationFromPresentation) {
		this.relationFromPresentation = relationFromPresentation;
	}

	public void setRelationToPresentation(int relationToPresentation) {
		this.relationToPresentation = relationToPresentation;
	}

	public E getRelateBegin() {
		return relateBegin;
	}

	public void setRelateBegin(E relateBegin) {
		this.relateBegin = relateBegin;
	}
}
