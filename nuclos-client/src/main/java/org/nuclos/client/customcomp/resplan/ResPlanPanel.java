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

package org.nuclos.client.customcomp.resplan;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXFindPanel;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.search.SearchFactory;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.customcomp.resplan.ResPlanController.GranularityType;
import org.nuclos.client.customcomp.resplan.ResPlanController.RestorePreferences;
import org.nuclos.client.customcomp.resplan.ResPlanController.TimeGranularity;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SearchFilterListCellRenderer;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController.CollectableEventListener;
import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.client.ui.labeled.LabeledComponentSupport;
import org.nuclos.client.ui.popupmenu.AbstractJPopupMenuListener;
import org.nuclos.client.ui.resplan.Interval;
import org.nuclos.client.ui.resplan.JResPlanComponent;
import org.nuclos.client.ui.resplan.JResPlanComponent.Area;
import org.nuclos.client.ui.resplan.ResPlanModel;
import org.nuclos.client.ui.resplan.header.JHeaderGrid;
import org.nuclos.client.ui.util.Orientation;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.exception.PreferencesException;

public class ResPlanPanel extends JPanel {
	
	private static final Logger LOG = Logger.getLogger(ResPlanPanel.class);

	public static final String TIME_HORIZON_PROPERTY = "timeHorizon";

	public static final String SEARCH_CONDITION_PROPERTY = "searchCondition";

	private ResPlanController controller;

	private DateChooser startDateChooser;
	private DateChooser endDateChooser;

	private JResPlanComponent<Collectable, Date, Collectable> resPlan;
	private CollectableResPlanModel resPlanModel;
	private DateTimeModel timeModel;

	private CollectableSearchCondition searchCondition;
	private Interval<Date> timeHorizon;

	private List<EntitySearchFilter> entitySearchFilters;
	private JComboBox searchFilterComboBox;
	private ListComboBoxModel<ResPlanController.TimeGranularity> timeGranularityModel;
	private JComboBox timeGranularityComboBox;
	private JScrollPane scrollPane;
	private final JButton infoButton;

	private List<String> infoMessages;

	// Last known user-specified extents
	private Dimension userResourceCellExtent = new Dimension(-1, -1);
//	private Dimension userTimelineCellExtent = new Dimension(-1, -1);

	public ResPlanPanel(ResPlanController cntrl, CollectableResPlanModel model, DateTimeModel timeModel) {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		
		this.controller = cntrl;
		this.timeModel = timeModel;
		this.resPlanModel = model;
		setLayout(new BorderLayout());

		JToolBar tb = UIUtils.createNonFloatableToolBar();
		tb.setFloatable(false);
		tb.add(new AbstractAction(localeDelegate.getText("nuclos.resplan.action.refresh"), 
				Icons.getInstance().getIconRefresh16()) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.refresh();
			}
		});
		tb.add(exportAction);
		tb.addSeparator();
		
		this.timeHorizon = new Interval<Date>(model.getDefaultViewFrom(), model.getDefaultViewUntil());

		final LabeledComponentSupport support = new LabeledComponentSupport();
		startDateChooser = new DateChooser(support, timeHorizon.getStart());
		startDateChooser.setMinimumSize(startDateChooser.getPreferredSize());
		startDateChooser.setMaximumSize(startDateChooser.getPreferredSize());
		endDateChooser = new DateChooser(support, timeHorizon.getEnd());
		endDateChooser.setMinimumSize(endDateChooser.getPreferredSize());
		endDateChooser.setMaximumSize(endDateChooser.getPreferredSize());

		tb.add(new JLabel(localeDelegate.getText("nuclos.resplan.toolbar.from")));
		tb.add(startDateChooser);
		tb.add(Box.createHorizontalStrut(5));
		tb.add(new JLabel(localeDelegate.getText("nuclos.resplan.toolbar.until")));
		tb.add(endDateChooser);

		timeGranularityModel = new ListComboBoxModel<ResPlanController.TimeGranularity>(controller.getTimeGranularityOptions());
		tb.addSeparator();
		tb.add(new JLabel(localeDelegate.getText("nuclos.resplan.toolbar.granularity")));
		timeGranularityComboBox = new JComboBox(timeGranularityModel);
		tb.add(timeGranularityComboBox);
		timeGranularityComboBox.setMaximumSize(Orientation.VERTICAL.updateExtent(timeGranularityComboBox.getPreferredSize(), 20));

		tb.addSeparator();
		tb.add(new JLabel(localeDelegate.getText("nuclos.resplan.toolbar.resourceFilter")));
		searchFilterComboBox = new JComboBox();
		searchFilterComboBox.setRenderer(new SearchFilterListCellRenderer());
		refreshSearchFilter();
		tb.add(searchFilterComboBox);
		searchFilterComboBox.setMaximumSize(Orientation.VERTICAL.updateExtent(searchFilterComboBox.getPreferredSize(), 20));

		tb.add(Box.createGlue());

		infoButton = new JButton(infoAction);
		infoButton.setVisible(false);
		tb.add(infoButton);

		tb.add(Box.createHorizontalStrut(3));

		initJResPlan();

		ActionListener dateChooserListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeHorzionChanged();
			}
		};
		startDateChooser.addActionListener(dateChooserListener);
		endDateChooser.addActionListener(dateChooserListener);

		timeGranularityComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Runnable runnable = createScrollToCurrentAreaRunnable();
					ResPlanController.TimeGranularity granularity = timeGranularityModel.getSelectedItem();
					resPlan.setTimeModel(granularity.getTimeModel());
					resPlan.getTimelineHeader().setCategoryModel(granularity.getHeaderCategories());
					SwingUtilities.invokeLater(runnable);
				}
			}
		});
		searchFilterComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					EntitySearchFilter filter = (EntitySearchFilter) searchFilterComboBox.getSelectedItem();
					if (filter instanceof NewCustomSearchFilter) {
						runCustomSearch();
						return;
					}
					setSearchCondition(filter.getSearchCondition());
				}
			}
		});

		scrollPane = new JScrollPane(resPlan);

		JButton corner = new JButton(switchOrientationAction);
		corner.setBorderPainted(false);
		scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, corner);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		add(tb, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		resPlan.setTimeHorizon(this.timeHorizon);
		resPlan.invalidate();

		setFocusable(true);
		setFocusCycleRoot(true);
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "delete");
		//getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control F"), "find");
		getActionMap().put("delete", removeAction);
		//getActionMap().put("find", findAction);
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.ACTIVATE_SEARCH_PANEL_2, findAction, this);
	}
	
	JResPlanComponent<Collectable, Date, Collectable> getResPlan() {
		return resPlan;
	}
	
	ResPlanController getController() {
		return controller;
	}

	public ListComboBoxModel<EntitySearchFilter> getSearchFilterModel() {
		return (ListComboBoxModel<EntitySearchFilter>) searchFilterComboBox.getModel();
	}

	public void refreshSearchFilter() {
		this.refreshSearchFilter(controller.getSearchFilters());
	}

	public void refreshSearchFilter(List<EntitySearchFilter> searchFilters) {
		Object selectedItem = searchFilterComboBox.getSelectedItem();
		entitySearchFilters = searchFilters;
		searchFilterComboBox.setModel(new ListComboBoxModel<EntitySearchFilter>(searchFilters));
		searchFilterComboBox.setMaximumSize(Orientation.VERTICAL.updateExtent(searchFilterComboBox.getPreferredSize(), 20));
		if (selectedItem != null)
			searchFilterComboBox.setSelectedItem(selectedItem);
	}

	public CollectableSearchCondition getSearchCondition() {
		return searchCondition;
	}

	private void setSearchCondition(CollectableSearchCondition newSearchCondition) {
		CollectableSearchCondition oldSearchCondition = this.searchCondition;
		if (!ObjectUtils.equals(oldSearchCondition, newSearchCondition)) {
			this.searchCondition = newSearchCondition;
			firePropertyChange(SEARCH_CONDITION_PROPERTY, oldSearchCondition, newSearchCondition);
		}
	}

	public String getSearchFilter() {
		EntitySearchFilter filter = (EntitySearchFilter) searchFilterComboBox.getSelectedItem();
		if (filter == null || filter.getSearchCondition() == null || filter instanceof CustomSearchFilter) {
			return null;
		}
		return filter.getName();
	}

	public void setSearchFilter(String name) {
		for (EntitySearchFilter filter : entitySearchFilters) {
			if ((name == null && filter.isDefaultFilter()) || name.equals(filter.getName())) {
				searchFilterComboBox.setSelectedItem(filter);
				return;
			}
		}
	}

	public void setCustomSearchFilter(CollectableSearchCondition searchCondition) {
		CustomSearchFilter customFilter = new CustomSearchFilter(searchCondition);
		List<EntitySearchFilter> filters = new ArrayList<EntitySearchFilter>(entitySearchFilters);
		filters.add(filters.size() - 1, customFilter);
		ListComboBoxModel<EntitySearchFilter> model = new ListComboBoxModel<EntitySearchFilter>(filters);
		searchFilterComboBox.setModel(model);
		searchFilterComboBox.setSelectedItem(customFilter);
		// -> fires event -> setSearchCondition
	}

	public GranularityType getTimeGranularity() {
		ResPlanController.TimeGranularity granularity = timeGranularityModel.getSelectedItem();
		return (granularity != null) ? granularity.getType() : null;
	}

	public void setTimeGranularity(GranularityType granularity) {
		for (int i = 0; i < timeGranularityModel.getSize(); i++) {
			ResPlanController.TimeGranularity option = timeGranularityModel.getElementAt(i);
			if (option.getType() == granularity) {
				timeGranularityModel.setSelectedItem(option);
				break;
			}
		}
	}

	public Interval<Date> getTimeHorizon() {
		return timeHorizon;
	}

	public void setTimeHorizon(Interval<Date> newTimeHorizon) {
		Interval<Date> oldTimeHorizon = this.timeHorizon;
		if (!ObjectUtils.equals(oldTimeHorizon, newTimeHorizon)) {
			this.timeHorizon = newTimeHorizon;
			startDateChooser.setDate(newTimeHorizon.getStart());
			endDateChooser.setDate(newTimeHorizon.getEnd());
			resPlan.setTimeHorizon(timeHorizon);
			firePropertyChange(TIME_HORIZON_PROPERTY, oldTimeHorizon, newTimeHorizon);
		}
	}

	private void timeHorzionChanged() {
		try {
			Date startDate = startDateChooser.getDate();
			Date endDate = endDateChooser.getDate();
			setTimeHorizon(new Interval<Date>(startDate, endDate, true));
		} catch (CommonValidationException ex) {
			Errors.getInstance().showExceptionDialog(ResPlanPanel.this, ex);
			return;
		}
	}

	public void setInfoMessages(List<String> messages, boolean show) {
		this.infoMessages = messages == null || messages.isEmpty() ? null : messages;
		infoButton.setVisible(infoMessages != null);
		if (show)
			showInfoMessages();
	}

	private void showInfoMessages() {
		if (infoMessages != null && infoButton.isVisible()) {
			StringBuilder sb = new StringBuilder("<html>");
			for (String message : infoMessages) {
				String escapedMessage = StringUtils.xmlEncode(message).replaceAll("\n", "<br/>");
				sb.append("<p>").append(escapedMessage).append("</p>");
			}
			final String message = sb.toString();
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						Bubble bubble = new Bubble(infoButton, message, 8, Bubble.Position.SE);
						bubble.setVisible(true);
					}
					catch (Exception e) {
						LOG.error("showInfoMessages failed: " + e, e);
					}
				}
			});
		}
	}

	private void initJResPlan() {
		resPlan = new JResPlanComponent<Collectable, Date, Collectable>(resPlanModel, timeModel);
		resPlan.getTimelineHeader().setCategoryModel(timeGranularityModel.getSelectedItem());
		resPlan.addMouseListener(new AbstractJPopupMenuListener() {
			@Override
			protected JPopupMenu getJPopupMenu(MouseEvent evt) {
				JPopupMenu popupMenu = new JPopupMenu();
				Point pt = evt.getPoint();

				Area<Collectable, Date> blankSelection = resPlan.getSelectedBlankArea();
				if (blankSelection != null) {
					popupMenu.add(new AddAction(blankSelection.getResource(), blankSelection.getInterval()));
				} else {
					popupMenu.add(new AddAction(resPlan.getResourceAt(pt), resPlan.getTimeIntervalAt(pt)));
				}

				List<Collectable> selection = selectForEvent(pt);
				if (resPlan.isEditable() && !selection.isEmpty()) {
					JMenuItem menuItem = popupMenu.add(removeAction);
					boolean enabled = false;
					for (Collectable clct : selection) {
						if (resPlanModel.isRemoveAllowed(clct)) {
							enabled = true;
							break;
						}
					}
					// Note: just change the state of the menu item (and leave the action as is)
					menuItem.setEnabled(enabled);
				}
				if (!selection.isEmpty()) {
					popupMenu.add(detailsAction);
				}

				return popupMenu;
			}

			private List<Collectable> selectForEvent(Point pt) {
				List<Collectable> selection = resPlan.getSelectedEntries();
				Collectable entryAt = resPlan.getEntryAt(pt);
				if (entryAt != null && (selection.isEmpty() || !selection.contains(entryAt))) {
					selection = Collections.singletonList(entryAt);
					resPlan.setSelectedEntries(selection);
				}
				return selection;
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					Collectable clct = resPlan.getEntryAt(evt.getPoint());
					runDetailsCollectable(resPlanModel.getEntryEntity().getEntityName(), clct);
					evt.consume();
				}
			}
		});
		resPlan.getResourceHeader().addMouseListener(new AbstractJPopupMenuListener() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					Collectable clct = resPlan.getResourceHeader().getValueAt(evt.getPoint());
					runDetailsCollectable(resPlanModel.getResourceEntity().getEntityName(), clct);
					evt.consume();
				}
			}
			@Override
			protected JPopupMenu getJPopupMenu(MouseEvent evt) {
				final Collectable clct = resPlan.getResourceHeader().getValueAt(evt.getPoint());
				if (clct != null) {
					JPopupMenu popupMenu = new JPopupMenu();
					popupMenu.add(new AbstractAction(SpringLocaleDelegate.getInstance().getText(
							"nuclos.resplan.action.showDetails")) {
						@Override
						public void actionPerformed(ActionEvent e) {
							runDetailsCollectable(resPlanModel.getResourceEntity().getEntityName(), clct);
						}
					});
					return popupMenu;
				}
				return null;
			}
		});
		Date start = DateUtils.addDays(DateUtils.getPureDate(new Date()), -5);
		Date end = DateUtils.addDays(start, 30);
		resPlan.setTimeHorizon(new Interval<Date>(start, end));
	}

	public void setResourceRenderer(ComponentProvider<?> renderer) {
		resPlan.getResourceHeader().setCellRendererProvider(renderer);
	}

	public void setEntryRenderer(ComponentProvider<?> renderer) {
		resPlan.setCellRendererProvider(renderer);
	}

	public void setBackgroundPainter(Painter<? super Area<Collectable, Date>> painter) {
		resPlan.setTimeslotBackgroundPainter(painter);
	}

	public void setCaptionComponent(Component c) {
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, c);
		resPlan.getResourceHeader().setCornerComponent(c);
		resPlan.getTimelineHeader().setCornerComponent(c);
	}

	private void runCustomSearch() {
		// Set-up new custom filter with the current search condition
		// (since the underlying search conditon does not change, no events are triggered)
		setCustomSearchFilter(searchCondition);

		try {
			final NuclosCollectController ctl = NuclosCollectControllerFactory.getInstance().newCollectController(
					resPlanModel.getResourceEntity().getEntityName(), null);
			ctl.getSearchPanel().btnSearch.setAction(new CommonAbstractAction(Icons.getInstance().getIconFind16(), 
					SpringLocaleDelegate.getInstance().getText("CollectController.30")) {
				@Override
				public void actionPerformed(ActionEvent ev) {
					UIUtils.runCommand(getParent(), new CommonRunnable() {
						@Override
						public void run() throws CommonBusinessException {
							try {
								ctl.makeConsistent(true);
								CollectableSearchCondition result = ctl.getSearchStrategy().getCollectableSearchCondition();
								setCustomSearchFilter(result);
							} catch (CollectableFieldFormatException e) {
								throw new NuclosFatalException(e);
							}
							ctl.getTab().dispose();
						}
					});
				}
			});
			ctl.runSearch();
		} catch (CommonBusinessException e) {
			LOG.warn("runCustomSearch failed: " + e, e);
		}
	}

	public Rectangle getViewRect() {
		return scrollPane.getViewport().getViewRect();
	}

	public void setViewRect(Rectangle rect) {
		scrollPane.getViewport().setViewPosition(rect.getLocation());
	}

	public void storeViewPreferences(Preferences prefs, RestorePreferences rp) throws PreferencesException {
		preserveCellExtent(userResourceCellExtent, resPlan.getResourceHeader());
//		preserveCellExtent(userTimelineCellExtent, resPlan.getTimelineHeader());

		int swingConstant = resPlan.getOrientation().swingConstant();
		if (prefs != null) {
			prefs.putInt("orientation", swingConstant);
		}
		if (rp != null) {
			rp.orientation = swingConstant;
		}
		for (Orientation orientation : Orientation.values()) {
			String vh = orientation.select("H", "V");
			if (prefs != null) {
				prefs.putInt("resourceCellExtent" + vh, orientation.extentFrom(userResourceCellExtent));
//				prefs.putInt("timelineCellExtent" + vh, orientation.extentFrom(userTimelineCellExtent));
			}
			if (rp != null) {
				rp.resourceCellExtent.put("resourceCellExtent" + vh, orientation.extentFrom(userResourceCellExtent));

				//rp.timelineCellExtent.put("timelineCellExtent" + vh, orientation.extentFrom(userTimelineCellExtent));
				for (TimeGranularity tg : this.getTimeGranularities()) {
					rp.timelineCellExtent.put(tg.getType().getValue()+"_timelineCellExtent" + "H", tg.getCellExtent(Orientation.HORIZONTAL));
					rp.timelineCellExtent.put(tg.getType().getValue()+"_timelineCellExtent" + "V", tg.getCellExtent(Orientation.VERTICAL));
				}
			}
		}
	}

	public void restoreViewPreferences(Preferences prefs, RestorePreferences rp) throws PreferencesException {
		if (prefs != null) {
			int orientation = prefs.getInt("orientation", -1);
			userResourceCellExtent = new Dimension(prefs.getInt("resourceCellExtentH", -1), prefs.getInt("resourceCellExtentV", -1));
//			userTimelineCellExtent = new Dimension(prefs.getInt("timelineCellExtentH", -1), prefs.getInt("timelineCellExtentV", -1));
			if (orientation != -1)
				resPlan.setOrientation(Orientation.fromSwingConstant(orientation));
			setCellExtent(resPlan.getResourceHeader(), userResourceCellExtent);
//			setCellExtent(resPlan.getTimelineHeader(), userTimelineCellExtent);
		}

		if (rp != null) {
			userResourceCellExtent = new Dimension(LangUtils.defaultIfNull(rp.resourceCellExtent.get("resourceCellExtentH"), -1),
				LangUtils.defaultIfNull(rp.resourceCellExtent.get("resourceCellExtentV"), -1));
//			userTimelineCellExtent = new Dimension(LangUtils.defaultIfNull(rp.timelineCellExtent.get("timelineCellExtentH"), -1),
//				LangUtils.defaultIfNull(rp.timelineCellExtent.get("timelineCellExtentV"), -1));
			if (rp.orientation != -1)
				resPlan.setOrientation(Orientation.fromSwingConstant(rp.orientation));
			setCellExtent(resPlan.getResourceHeader(), userResourceCellExtent);

			//setCellExtent(resPlan.getTimelineHeader(), userTimelineCellExtent);
			for (TimeGranularity tg : this.getTimeGranularities()) {
				tg.setCellExtent(Orientation.HORIZONTAL, rp.timelineCellExtent.get(tg.getType().getValue()+"_timelineCellExtent" + "H"));
				tg.setCellExtent(Orientation.VERTICAL, rp.timelineCellExtent.get(tg.getType().getValue()+"_timelineCellExtent" + "V"));
				if (getTimeGranularity() == tg.getType()) {
					setCellExtent(resPlan.getTimelineHeader(), tg.getCellExtent(resPlan.getOrientation()));
				}
			}
		}
	}

	private static void preserveCellExtent(Dimension dim, JHeaderGrid<?> header) {
		header.getOrientation().updateExtent(dim, header.getCellExtent());
	}

	private static void setCellExtent(JHeaderGrid<?> header, Dimension dim) {
		header.setCellExtent(header.getOrientation().extentFrom(dim));
	}

	private static void setCellExtent(JHeaderGrid<?> header, int extent) {
		header.setCellExtent(extent);
	}

	/**
	 * Determines the current top-left area (resource/time) and creates a Runnable
	 * which can be used to scroll the view to that area.
	 */
	private Runnable createScrollToCurrentAreaRunnable() {
		Point pt = resPlan.getVisibleRect().getLocation();
		pt.translate(5, 5);
		final Collectable resource = resPlan.getResourceAt(pt);
		final Interval<Date> timeInterval = resPlan.getTimeIntervalAt(pt);
		return new Runnable() {
			@Override
			public void run() {
				try {
					resPlan.scrollRectToArea(resource, timeInterval, true);
				}
				catch (Exception e) {
					LOG.error("createScrollToCurrentAreaRunnable failed: " + e, e);
				}
			}
		};
	}

	private Action switchOrientationAction = new AbstractAction(
			SpringLocaleDelegate.getInstance().getText("nuclos.resplan.action.switchOrientation"), 
			Icons.getInstance().getIconRelate()) {
		@Override
		public void actionPerformed(ActionEvent e) {
			Runnable runnable = createScrollToCurrentAreaRunnable();
			preserveCellExtent(userResourceCellExtent, resPlan.getResourceHeader());
//			preserveCellExtent(userTimelineCellExtent, resPlan.getTimelineHeader());
			Orientation orientation = resPlan.getOrientation();
			resPlan.setOrientation(orientation.opposite());
			setCellExtent(resPlan.getResourceHeader(), userResourceCellExtent);
//			setCellExtent(resPlan.getTimelineHeader(), userTimelineCellExtent);
			if (timeGranularityModel.getSelectedItem() != null &&
				timeGranularityModel.getSelectedItem().getCellExtent(resPlan.getOrientation()) > 0) {
				setCellExtent(resPlan.getTimelineHeader(), timeGranularityModel.getSelectedItem().getCellExtent(resPlan.getOrientation()));
			}
			resPlan.invalidate();
			SwingUtilities.invokeLater(runnable);
		}
	};

	private Action removeAction = new AbstractAction(SpringLocaleDelegate.getInstance().getText("nuclos.resplan.action.remove")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
			ResPlanModel<Collectable, Date, Collectable> model = resPlan.getModel();
			final List<Collectable> selectedEntries = resPlan.getSelectedEntries();
			String title, message;
			if (selectedEntries.isEmpty()) {
				return;
			} else if (selectedEntries.size() == 1) {
				title = localeDelegate.getMessage("ResultController.8", null);
				message = localeDelegate.getMessage("ResultController.12", null, selectedEntries.get(0).getIdentifierLabel());
			} else { // selectedEntries.size() > 1
				title = localeDelegate.getMessage("ResultController.7", null);
				message = localeDelegate.getMessage("ResultController.13", null, selectedEntries.size());
			}
			int opt = JOptionPane.showConfirmDialog(ResPlanPanel.this, message, title,JOptionPane.YES_NO_OPTION);
			if (opt == JOptionPane.YES_OPTION) {
				for (Collectable clct : selectedEntries) {
					if (model.isRemoveAllowed(clct)) {
						model.removeEntry(clct);
					}
				}
			}
		}
	};

	private Action detailsAction = new AbstractAction(SpringLocaleDelegate.getInstance().getText("nuclos.resplan.action.showDetails")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			final List<Collectable> selectedEntries = resPlan.getSelectedEntries();
			for (Collectable clct : selectedEntries) {
				runDetailsCollectable(resPlanModel.getEntryEntity().getEntityName(), clct);
			}
		}
	};

	private Action infoAction = new AbstractAction(null, Icons.getInstance().getIconAbout16()) {
		@Override
		public void actionPerformed(ActionEvent e) {
			showInfoMessages();
		}
	};

	private Action findAction = new FindAction();

	class FindAction extends AbstractAction implements AncestorListener {

		private final JXFindPanel findPanel;

		public FindAction() {
			super("find");
			findPanel = SearchFactory.getInstance().getSharedFindPanel();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!Arrays.asList(findPanel.getAncestorListeners()).contains(this)) {
				findPanel.addAncestorListener(this);
			}
			SearchFactory.getInstance().showFindDialog(ResPlanPanel.this, resPlan.new ResPlanSearchable());
		}

		@Override
		public void ancestorAdded(AncestorEvent event) {
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			// Remove this listener
			findPanel.removeAncestorListener(this);
			// Clear resource selection
			resPlan.getResourceHeader().setSelectedValue(null);
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
		}
	}

	private Action exportAction = new ExportAction();

	class ExportAction extends AbstractAction {

		ExportAction() {
			super(SpringLocaleDelegate.getInstance().getText("nuclos.resplan.action.export"),
					Icons.getInstance().getIconExport16());
			// setEnabled(resPlanModel.isCreateAllowed());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// controller.getFrame().add(new ResPlanExportPanel());
			final ResPlanExportDialog d = new ResPlanExportDialog(ResPlanPanel.this, controller.getTab());
		}
	}

	class RemoveAction extends AbstractAction {

		public RemoveAction() {
			super(SpringLocaleDelegate.getInstance().getText("nuclos.resplan.action.remove"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
			ResPlanModel<Collectable, Date, Collectable> model = resPlan.getModel();
			final List<Collectable> selectedEntries = resPlan.getSelectedEntries();
			String title, message;
			if (selectedEntries.isEmpty()) {
				return;
			} else if (selectedEntries.size() == 1) {
				title = localeDelegate.getMessage("ResultController.8", null);
				message = localeDelegate.getMessage("ResultController.12", null, selectedEntries.get(0).getIdentifierLabel());
			} else { // selectedEntries.size() > 1
				title = localeDelegate.getMessage("ResultController.7", null);
				message = localeDelegate.getMessage("ResultController.13", null, selectedEntries.size());
			}
			int opt = JOptionPane.showConfirmDialog(ResPlanPanel.this, message, title,JOptionPane.YES_NO_OPTION);
			if (opt == JOptionPane.YES_OPTION) {
				for (Collectable clct : selectedEntries) {
					if (model.isRemoveAllowed(clct)) {
						model.removeEntry(clct);
					}
				}
			}
		}
	}

	class AddAction extends AbstractAction {

		private Collectable resource;
		private Interval<Date> interval;

		public AddAction(Collectable resource, Interval<Date> interval) {
			super(SpringLocaleDelegate.getInstance().getText("nuclos.resplan.action.add"));
			setEnabled(resPlanModel.isCreateAllowed());
			this.resource = resource;
			this.interval = interval;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			UIUtils.runShortCommand(resPlan, new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					final MainFrameTab tabIfAny = new MainFrameTab();
					final NuclosCollectController cntrl = NuclosCollectControllerFactory.getInstance().newCollectController(
							resPlanModel.getEntryEntity().getCollectableEntity().getName(), tabIfAny);
					Main.getInstance().getMainController().initMainFrameTab(controller, tabIfAny);
					controller.getTab().add(tabIfAny);
					
					cntrl.addCollectableEventListener(new CollectControllerEventHandler(cntrl));
					Collectable template = null;
					if (interval != null && resource != null) {
						template = resPlanModel.createCollectableEntry(resource, interval);
					}
					cntrl.runNewWith(template);
				}
			});
		}
	}

	private void runDetailsCollectable(final String entityName, final Collectable clct) {
		if (clct == null)
			return;

		UIUtils.runCommand(Main.getInstance().getMainFrame(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final MainFrameTab tabIfAny = new MainFrameTab();
				final NuclosCollectController cntrl = NuclosCollectControllerFactory.getInstance().newCollectController(
						entityName, null);
				
				Main.getInstance().getMainController().initMainFrameTab(cntrl, tabIfAny);
				controller.getTab().add(tabIfAny);
				
				cntrl.addCollectableEventListener(new CollectControllerEventHandler(cntrl));
				cntrl.runViewSingleCollectableWithId(clct.getId());
			}
		});
	}

	private final class CollectControllerEventHandler implements CollectableEventListener {

		private final NuclosCollectController<?> collectController;

		public CollectControllerEventHandler(NuclosCollectController<?> clctCntrl) {
			this.collectController = clctCntrl;
		}

		@Override
		public void handleCollectableEvent(Collectable collectable, MessageType messageType) {
			switch (messageType) {
			case EDIT_DONE:
			case NEW_DONE :
			case DELETE_DONE:
				try {
					collectController.getTab().dispose();
				} finally {
					controller.refresh();
				}
			}
		}
	}

	public static class NewCustomSearchFilter extends EntitySearchFilter {

		public NewCustomSearchFilter() {
			setName(SpringLocaleDelegate.getInstance().getText("nuclos.resplan.action.newSearch"));
		}

		@Override
		public Integer getId() {
			return -1;
		}
	}

	public static class CustomSearchFilter extends EntitySearchFilter {

		static int ID_COUNTER = -3;

		private final int id;
		private boolean initialized;

		public CustomSearchFilter() {
			setName(SpringLocaleDelegate.getInstance().getText("nuclos.resplan.action.customSearch"));
			id = ID_COUNTER--;
		}

		public CustomSearchFilter(CollectableSearchCondition cond) {
			this();
			setSearchCondition(cond);
		}

		@Override
		public void setSearchCondition(CollectableSearchCondition searchcond) {
			super.setSearchCondition(searchcond);
			this.initialized = searchcond != null;
		}

		public boolean isInitialized() {
			return initialized;
		}

		@Override
		public Integer getId() {
			return id;
		}
	}

	public List<TimeGranularity> getTimeGranularities() {
		List<TimeGranularity> result = new ArrayList<TimeGranularity>();
		for (int i = 0; i < timeGranularityModel.getSize(); i++) {
			result.add(timeGranularityModel.getElementAt(i));
		}
		return result;
	}
}

