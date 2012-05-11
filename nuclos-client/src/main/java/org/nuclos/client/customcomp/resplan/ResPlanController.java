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
import java.awt.Color;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXLabel;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.customcomp.CustomComponentController;
import org.nuclos.client.customcomp.resplan.ResPlanPanel.NewCustomSearchFilter;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.scripting.GroovySupport;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SearchFilters;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.resplan.Interval;
import org.nuclos.client.ui.resplan.TimeModel;
import org.nuclos.client.ui.resplan.header.GroupMapper;
import org.nuclos.client.ui.util.Orientation;
import org.nuclos.client.ui.util.PainterUtils;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.time.LocalTime;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ResPlanController extends CustomComponentController {

	private static final Logger LOG = Logger.getLogger(ResPlanController.class);
	
	private static final int DEFAULT_RESOURCE_LIMIT = 100;

	private CustomComponentVO componentVO;
	private ResPlanConfigVO configVO;
	private ResPlanResourceVO resourceVO;
	private ResPlanPanel component;
	private CollectableResPlanModel resPlanModel;

	private CollectableHelper<?> resEntity;
	private CollectableHelper<?> entryEntity;

	private Rectangle viewRectFromPreferences;

	public ResPlanController(CustomComponentVO componentVO, MainFrameTab tabIfAny) {
		super(componentVO.getInternalName(), tabIfAny);
		init(componentVO);
	}
	
	@Override
	public boolean askAndSaveIfNecessary() {
		return true;
	}

	void init(CustomComponentVO vo) {
		componentVO = vo;
		configVO = ResPlanConfigVO.fromBytes(vo.getData());
		resourceVO = configVO.getResources(SpringLocaleDelegate.getInstance().getUserLocaleInfo());

		resEntity = CollectableHelper.getForEntity(configVO.getResourceEntity());
		entryEntity = CollectableHelper.getForEntity(configVO.getEntryEntity());

		CollectableLabelProvider resourceLabelProvider = new CollectableLabelProvider();
		resourceLabelProvider.setLabelTemplate(resourceVO.getResourceLabel());
		resourceLabelProvider.setToolTipTemplate(resourceVO.getResourceTooltip());

		CollectableLabelProvider entryLabelProvider = new CollectableLabelProvider();
		entryLabelProvider.setLabelTemplate(resourceVO.getBookingLabel());
		entryLabelProvider.setToolTipTemplate(resourceVO.getBookingTooltip());

		JXLabel captionLabel = new JXLabel();
		captionLabel.setVerticalAlignment(JLabel.TOP);
		captionLabel.setText(resourceVO.getLegendLabel());
		captionLabel.setToolTipText(StringUtils.nullIfEmpty(resourceVO.getLegendTooltip()));
		captionLabel.setBackgroundPainter(new PainterUtils.HeaderPainter());
		JPanel captionPanel = new JPanel(new BorderLayout());
		captionPanel.add(captionLabel);
		captionPanel.setBackground(new Color(163, 172, 187));
		captionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 1));

		BackgroundPainter backgroundPainter = new BackgroundPainter();

		if (configVO.isScriptingActivated()) {
			String code = configVO.getScriptingCode();
			String entryLabelCode = configVO.getScriptingEntryCellMethod();
			try {
				if (code != null && !code.trim().isEmpty()) {
					GroovySupport support = new GroovySupport();
					support.compile(code);
					support.prepare();

					resourceLabelProvider.setGroovyMethod(support.getInvocable(
						configVO.getScriptingResourceCellMethod(), CollectableLabelProvider.SCRIPTING_SIGNATURE));
					entryLabelProvider.setGroovyMethod(support.getInvocable(
						configVO.getScriptingEntryCellMethod(), CollectableLabelProvider.SCRIPTING_SIGNATURE));
					backgroundPainter.setGroovyMethod(support.getInvocable(
						configVO.getScriptingBackgroundPaintMethod(), BackgroundPainter.SCRIPTING_SIGNATURE));
				} else {
					if (entryLabelCode != null) {
						JOptionPane.showMessageDialog(Main.getInstance().getMainFrame(), "Kein Code hinterlegt, angegebene Regeln werden ignoriert");
					}
				}
			} catch (Exception ex) {
				Errors.getInstance().showExceptionDialog(Main.getInstance().getMainFrame(), "Fehler beim Initialisieren des Skripting", ex);
			}
		}

		resPlanModel = new CollectableResPlanModel(this);

		DateTimeModel dateTimeModel = new DateTimeModel(configVO.getParsedTimePeriods());

		component = new ResPlanPanel(this, resPlanModel, dateTimeModel);
		component.setResourceRenderer(resourceLabelProvider);
		component.setEntryRenderer(entryLabelProvider);
		component.setBackgroundPainter(backgroundPainter);
		component.setCaptionComponent(captionPanel);
	}

	/**
	 * {@inheritDoc}.
	 * Additionally to this method, the {@link ResPlanController} supports the alternative
	 * entry points {@link #runWith(Collection)} and {@link #runWith(CollectableSearchCondition)}.
	 * @see #runWith(Collection)
	 * @see #runWith(CollectableSearchCondition)
	 */
	@Override
	public void run() {
		component.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				panelPropertyChanged(evt);
			}
		});
		refresh();

		super.run();
	}

	/**
	 * Alternative entry point which starts this controller with the specified ressources.
	 * @param ids resource ids
	 * @see #run()
	 */
	public void runWith(Collection<Object> ids) {
		component.setCustomSearchFilter(new CollectableIdListCondition(new ArrayList<Object>(ids)));
		run();
	}

	/**
	 * Alternative entry point which starts this controller with the ressources specified
	 * by the given search condition.
	 * @param searchCondition resource search condition
	 */
	public void runWith(CollectableSearchCondition searchCondition) {
		component.setCustomSearchFilter(searchCondition);
		run();
	}

	private void panelPropertyChanged(PropertyChangeEvent evt) {
		if (ResPlanPanel.TIME_HORIZON_PROPERTY.equals(evt.getPropertyName())) {
			refresh();
		} else if (ResPlanPanel.SEARCH_CONDITION_PROPERTY.equals(evt.getPropertyName())) {
			refresh();
		}
	}

	CollectableHelper<?> getResEntity() {
		return resEntity;
	}

	CollectableHelper<?> getEntryEntity() {
		return entryEntity;
	}

	ResPlanConfigVO getConfigVO() {
		return configVO;
	}
	
	ResPlanResourceVO getResourceVO() {
		return resourceVO;
	}

	List<TimeGranularity> getTimeGranularityOptions() {
		List<TimeGranularity> options = new ArrayList<TimeGranularity>();
		List<Pair<LocalTime, LocalTime>> timePeriods = configVO.getParsedTimePeriods();
		if (timePeriods != null && !timePeriods.isEmpty()) {
			options.add(new TimeGranularity(GranularityType.TIME, new DateTimeModel(timePeriods)));
		}
		options.add(new TimeGranularity(GranularityType.DAY, new DateTimeModel()));
		options.add(new TimeGranularity(GranularityType.WEEK, new WeekModel()));
		options.add(new TimeGranularity(GranularityType.MONTH, new MonthModel()));
		return options;
	}

	private CollectableSearchExpression getInternalResourceSearchExpression() {
		List<CollectableSorting> sorting = new ArrayList<CollectableSorting>();
		if (configVO.getResourceSortField() != null) {
			sorting.add(new CollectableSorting(SystemFields.BASE_ALIAS, configVO.getResourceEntity(), true, configVO.getResourceSortField(), true));
		}
		return new CollectableSearchExpression(component.getSearchCondition(), sorting);
	}

	private CollectableSearchCondition getInteralEntrySearchCondition(Interval<Date> interval) {
		CollectableEntityField dateFromField = entryEntity.getCollectableEntity().getEntityField(configVO.getDateFromField());
		CollectableEntityField dateUntilField = entryEntity.getCollectableEntity().getEntityField(configVO.getDateUntilField());
		return new CompositeCollectableSearchCondition(LogicalOperator.AND, Arrays.asList(
			new CollectableComparison(dateFromField, ComparisonOperator.LESS_OR_EQUAL, new CollectableValueField(interval.getEnd())),
			new CollectableComparison(dateUntilField, ComparisonOperator.GREATER_OR_EQUAL, new CollectableValueField(interval.getStart()))));
	}

	public void refresh() {
		execute(new RefreshTask(true));
	}
	
	@Override
	public void close() {
	}

	@Override
	public JComponent getComponent() {
		return component;
	}

	@Override
	protected void storeSharedState() throws PreferencesException {
		super.storeSharedState();
		Preferences defaultPrefs = getPreferences().node("default");

		defaultPrefs.put("granularity", component.getTimeGranularity().getValue());
		component.storeViewPreferences(defaultPrefs, null);
	}

	@Override
	protected void restoreSharedState() throws PreferencesException {
		super.restoreSharedState();
		Preferences defaultPrefs = getPreferences().node("default");

//		Date startDate = DateUtils.getPureDate(new Date());
//		Date endDate = DateUtils.addMonths(startDate, 1);
		GranularityType granularity = KeyEnum.Utils.findEnum(GranularityType.class, defaultPrefs.get("granularity", null));
		if (granularity != null) {
			component.setTimeGranularity(granularity);
		}
//		component.setTimeHorizon(new Interval<Date>(startDate, endDate, true));
		component.restoreViewPreferences(defaultPrefs, null);
	}

	@Override
	public boolean isRestoreTab() {
		return true;
	}

	@Override
	protected String storeInstanceStateToXML() {
		RestorePreferences rp = new RestorePreferences();
		Interval<Date> timeHorizon = component.getTimeHorizon();
		String searchFilter = component.getSearchFilter();
		CollectableSearchCondition searchCondition = component.getSearchCondition();

		rp.granularity = component.getTimeGranularity().getValue();
		rp.startDate =  String.format("%tF", timeHorizon.getStart());
		rp.endDate = String.format("%tF", timeHorizon.getEnd());
		rp.searchFilter = searchFilter;
		rp.searchCondition = searchCondition;
		rp.viewRect = component.getViewRect();

		try {
			component.storeViewPreferences(null, rp);
		} catch(PreferencesException e) {
			LOG.warn("storeInstanceStateToXML failed: " + e);
		}

		return toXML(rp);
	}

	@Override
	protected void restoreInstanceStateFromXML(String xml) {
		RestorePreferences rp = fromXML(xml);

		GranularityType granularity = KeyEnum.Utils.findEnum(GranularityType.class, rp.granularity);
		if (granularity != null) {
			component.setTimeGranularity(granularity);
		}

		if (component != null && rp.startDate != null && rp.endDate != null) {
			Date startDate = java.sql.Date.valueOf(rp.startDate);
			Date endDate = java.sql.Date.valueOf(rp.endDate);
			component.setTimeHorizon(new Interval<Date>(startDate, endDate, true));
		}

		if (rp.searchFilter == null && rp.searchCondition != null) {
			component.setCustomSearchFilter(rp.searchCondition);
		} else {
			component.setSearchFilter(rp.searchFilter);
		}
		try {
			component.restoreViewPreferences(null, rp);
		} catch(PreferencesException e) {
			LOG.warn("restoreInstanceStateFromXML failed: " + e);
		}

		viewRectFromPreferences = rp.viewRect;
	}

	/**
	 *
	 *
	 */
	public static class RestorePreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		String granularity;
		String startDate;
		String endDate;
		String searchFilter;
		Rectangle viewRect;
		CollectableSearchCondition searchCondition;

		int orientation;
		final Map<String, Integer> resourceCellExtent = new HashMap<String, Integer>();
		final Map<String, Integer> timelineCellExtent = new HashMap<String, Integer>();
	}

	private static String toXML(RestorePreferences rp) {
		XStream xstream = new XStream(new DomDriver());
		return xstream.toXML(rp);
	}

	private static RestorePreferences fromXML(String xml) {
		XStream xstream = new XStream(new DomDriver());
		return (RestorePreferences) xstream.fromXML(xml);
	}

	@Override
	protected boolean handleSpecialException(Exception ex) {
		NuclosBusinessRuleException.extractNuclosBusinessRuleExceptionIfAny(ex);
		if (ex != null) {
			String exceptionMessage;

			String originMessage = NuclosBusinessRuleException.extractOriginFromNuclosBusinessRuleExceptionIfAny(ex);
			if (originMessage != null) {
				exceptionMessage = originMessage;
			} else {
				exceptionMessage = Errors.getReasonableMessage(ex);
			}
			if (exceptionMessage != null) {
				log.error(exceptionMessage, ex);
				List<String> messages = Collections.singletonList(Errors.formatErrorMessage(exceptionMessage));
				component.setInfoMessages(messages, true);
				return true;
			}
		}
		return super.handleSpecialException(ex);
	}

	class RefreshTask extends BackgroundTask {

		final int resourceLimit;
		final boolean showMessage;
		final CollectableSearchExpression resourceSearchExpr;
		final CollectableSearchCondition entryCond;

		volatile List<? extends Collectable> resources;
		volatile List<? extends Collectable> entries;
		volatile boolean truncated;

		List<EntitySearchFilter> searchFilters;

		public RefreshTask(boolean show) {
			resourceLimit = getResourceLimit();
			showMessage = show;
			resourceSearchExpr = getInternalResourceSearchExpression();
			entryCond = getInteralEntrySearchCondition(component.getTimeHorizon());
		}

		@Override
		public void doInBackground() throws CommonBusinessException {
			List<?> resourceIds = resEntity.getIds(resourceSearchExpr);
			if (resourceIds.size() > resourceLimit) {
				resourceIds.subList(resourceLimit, resourceIds.size()).clear();
				truncated = true;
			}
			resources = resEntity.get(resourceIds);
			entries = entryEntity.get(entryEntity.getIds(entryCond));
			searchFilters = getSearchFilters();
		}

		@Override
		public void done() throws CommonBusinessException {
			component.refreshSearchFilter(searchFilters);
			List<String> messages = new ArrayList<String>();
			for (Iterator<? extends Collectable> iter = entries.iterator(); iter.hasNext(); ) {
				Collectable entry = iter.next();
				if (!resPlanModel.isCollectableEntryValid(entry)) {
					// if entry is invalid, remove it from the collection and add an error message instead
					iter.remove();
					messages.add(getSpringLocaleDelegate().getMessage("nuclos.resplan.invalidEntry", null, entry.getIdentifierLabel()));
				}
			}
			resPlanModel.setData(resources, entries);
			if (truncated) {
				messages.add(getSpringLocaleDelegate().getMessage("nuclos.resplan.limitSearchResult", null, resourceLimit));
			}
			if (messages.size() > 0) {
				component.setInfoMessages(messages, showMessage);
			} else {
				component.setInfoMessages(null, false);
			}
		}
	}

	public int getResourceLimit() {
		return Math.max(1, ClientParameterProvider.getInstance().getIntValue(
			ParameterProvider.KEY_RESPLAN_RESOURCE_LIMIT, DEFAULT_RESOURCE_LIMIT));
	}

	public List<EntitySearchFilter> getSearchFilters() {
		List<EntitySearchFilter> entitySearchFilters = new ArrayList<EntitySearchFilter>();
		entitySearchFilters.add(EntitySearchFilter.newDefaultFilter());
		try {
			entitySearchFilters.addAll(SearchFilters.forEntity(resPlanModel.getResourceEntity().getCollectableEntity().getName()).getAll());
		} catch (PreferencesException e) {
			LOG.warn("getSearchFilters failed: " + e, e);
		}
		entitySearchFilters.add(new NewCustomSearchFilter());

		return entitySearchFilters;
	}

	public static enum GranularityType implements KeyEnum<String> {
		
		MONTH("month", 0, Calendar.MONTH),
		WEEK("week", 1, Calendar.WEEK_OF_YEAR),
		@Deprecated
		TIME("time", 3, -1),
		DAY("day", 2, Calendar.DAY_OF_WEEK);

		private final String name;
		private final int level;
		private final int calendarQuantizer;

		private GranularityType(String name, int level, int calendarQuantizer) {
			this.name = name;
			this.level = level;
			this.calendarQuantizer = calendarQuantizer;
		}

		@Override
		public String getValue() {
			return name;
		}
		
		public int getLevel() {
			return level;
		}
		
		public int getCalendarQuantizer() {
			return calendarQuantizer;
		}
		
		public static GranularityType getGranularityForLevel(int level) {
			for (GranularityType result: GranularityType.class.getEnumConstants()) {
				if (result.getLevel() == level) {
					return result;
				}
			}
			return null;
		}
	}

	public static class TimeGranularity implements GroupMapper<Interval<Date>> {

		private final GregorianCalendar calendar = new GregorianCalendar();

		private final TimeModel<Date> timeModel;
		private final GranularityType type;
		private final Map<Orientation, Integer> mapCellExtent = new HashMap<Orientation, Integer>();
		private final String cwLabel;

		TimeGranularity(GranularityType type, TimeModel<Date> timeModel) {
			this.type = type;
			this.timeModel = timeModel;
			this.cwLabel = SpringLocaleDelegate.getInstance().getText("nuclos.resplan.cw.label");
		}

		public GranularityType getType() {
			return type;
		}

		public TimeModel<Date> getTimeModel() {
			return timeModel;
		}

		public GroupMapper<Interval<Date>> getHeaderCategories() {
			// TODO_RESPLAN: refactor
			return this;
		}

		@Override
		public int getCategoryCount() {
			return type.level + 1;
		}

		@Override
		public String getCategoryName(int category) {
			final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
			switch (category) {
			case 0: return localeDelegate.getText("nuclos.resplan.granularity.month", null);
			case 1: return localeDelegate.getText("nuclos.resplan.granularity.calendarWeek", null);
			case 2: return localeDelegate.getText("nuclos.resplan.granularity.day", null);
			case 3: return localeDelegate.getText("nuclos.resplan.granularity.time", null);
			}
			return null;
		}

		@Override
		public Object getCategoryValue(int category, Interval<Date> interval) {
			switch (category) {
			case 0:
				calendar.setTime(interval.getStart());
				return String.format("%1$Tb %1$Ty", calendar);
			case 1:
				calendar.setTime(interval.getStart());
				return cwLabel + String.format("%02d", calendar.get(GregorianCalendar.WEEK_OF_YEAR));
			case 2:
				return String.format("%1$Td.%1$Tm", interval.getStart());
			case 3:
				return String.format("%Tk-%Tk", interval.getStart(), interval.getEnd());
			default:
				throw new IllegalArgumentException();
			}
		}

		public String getCategoryValue(int category, Date start) {
			switch (category) {
			case 0:
				calendar.setTime(start);
				return String.format("%1$Tb %1$Ty", calendar);
			case 1:
				calendar.setTime(start);
				return cwLabel + String.format("%02d", calendar.get(GregorianCalendar.WEEK_OF_YEAR));
			case 2:
				return String.format("%1$Td.%1$Tm", start);
			case 3:
				return String.format("%Tk", start);
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public void setCellExtent(Orientation o, int extent) {
			this.mapCellExtent.put(o, extent);
		}

		@Override
		public int getCellExtent(Orientation o) {
			return LangUtils.defaultIfNull(mapCellExtent.get(o), 0);
		}

		@Override
		public String toString() {
			return getCategoryName(type.level);
		}
	}
}
