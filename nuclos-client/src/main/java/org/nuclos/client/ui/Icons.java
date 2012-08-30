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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

/**
 * Singleton that contains common icons.
 * This class uses some icons of the "Java Look & Feel Graphics Repository",
 * so be sure to include jlfgr-1_0.jar in the class path.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class Icons {

	private static final Logger LOG = Logger.getLogger(Icons.class);

	/**
	 * the one and only instance of this class
	 */
	private static Icons singleton;

	/**
	 * Use <code>getInstance()</code> to create the one and only <code>Icons</code> object
	 */
	protected Icons() {
		// do nothing
	}

	/**
	 * @return the one and only instance of <code>Icons</code>
	 */
	public static synchronized Icons getInstance() {
		if (singleton == null) {
			singleton = new Icons();
		}
		return singleton;
	}

	/** @deprecated Use UIManager.getIcon("Table.descendingSortIcon") instead. */
	@Deprecated public Icon getSortingDown() {
		return new ImageIcon(this.getClass().getResource("/org/nuclos/client/lookandfeel/images/arrow8x8Down_pressed.png"));
	}

	/** @deprecated Use UIManager.getIcon("Table.ascendingSortIcon") instead. */
	@Deprecated public Icon getSortingUp() {
		return new ImageIcon(this.getClass().getResource("/org/nuclos/client/lookandfeel/images/arrow8x8Up_pressed.png"));
	}

	public Icon getSimpleArrowDown() {
		return new ImageIcon(this.getClass().getResource("/org/nuclos/client/lookandfeel/images/arrowDown.png"));
	}

	public Icon getSimpleArrowUp() {
		return new ImageIcon(this.getClass().getResource("/org/nuclos/client/lookandfeel/images/arrowUp.png"));
	}

	/**
	 * "Novabit Informationssysteme GmbH" icon
	 */
	public Icon getCommonInfSysLogo() {
		return this.getCachedImageIcon("org/nuclos/client/ui/images/logo/novabit-150x45.gif");
	}

	/**
	 * "Novabit" icon
	 */
	public Icon getCommonLogo() {
		return this.getCachedImageIcon("org/nuclos/client/ui/images/logo/novabit-150x28.gif");
	}

	/**
	 * "Novabit live search" icon
	 */
	public Icon getLiveSearchLogo() {
		return this.getCachedImageIcon("org/nuclos/client/ui/images/logo/LiveSearchLogo_small.png");
	}

	/**
	 * @return a large magnifier icon
	 */
	public ImageIcon getDefaultSearchWatermark() {
		return this.getCachedImageIcon("org/nuclos/client/ui/images/logo/magnifier.png");
	}

	public ImageIcon getIconEmpty16() {
		return this.getIconByName("empty");
	}

	public Icon getIconCut16() {
		return this.getIconByName("cut");
	}

	public Icon getIconCopy16() {
		return this.getIconByName("copy");
	}

	public Icon getIconPaste16() {
		return this.getIconByName("paste");
	}
	
	public Icon getIconRedo16() {
		return this.getIconByName("redo");
	}
	
	public Icon getIconUndo16() {
		return this.getIconByName("undo");
	}

	public Icon getIconNew16() {
		return this.getIconByName("new");
	}

	public Icon getIconNewWithSearchValues16() {
		return this.getIconByName("new-with-search-values");
	}

	public Icon getIconEdit16() {
		return this.getIconByName("edit");
	}

	public Icon getIconSave16() {
		return this.getIconByName("save");
	}
	
	public Icon getIconSaveS16() {
		return this.getIconByName("saveS");
	}

	public Icon getIconClone16() {
		return this.getIconByName("clone");
	}

	public Icon getIconDelete16() {
		return this.getIconByName("delete");
	}

	public Icon getIconRealDelete16() {
		return this.getIconByName("delete-physical");
	}

	public Icon getIconMultiEdit16() {
		return this.getIconByName("multi-edit");
	}

	public Icon getIconRefresh16() {
		return this.getIconByName("refresh");
	}

	public Icon getIconPrint16() {
		return this.getIconByName("print");
	}

	public Icon getIconPrintReport16() {
		return this.getIconByName("print-report");
	}

	public Icon getIconImport16() {
		return this.getIconByName("import");
	}

	public Icon getIconExport16() {
		return this.getIconByName("export");
	}

	public Icon getIconFind16() {
		return this.getIconByName("find");
	}

	public Icon getIconClearSearch16() {
		return this.getIconByName("clear");
	}

	public Icon getIconCancel16() {
		return this.getIconByName("cancel");
	}

	public Icon getIconFilter16() {
		return this.getIconByName("filter");
	}

	public Icon getIconFilterActive16() {
		return this.getIconByName("filter-active");
	}

	public Icon getIconRuleUsage16() {
		return this.getIconByName("rule-usage");
	}

	public Icon getIconProperties16() {
		return this.getIconByName("properties");
	}

	public Icon getIconHelp() {
		return this.getIconByName("help-contents");
	}

	public Icon getIconContextualHelp16() {
		return this.getIconByName("help-contextual");
	}

	public Icon getIconAbout16() {
		return this.getIconByName("help-about");
	}

	public Icon getIconOpenLinkedObject() {
		return this.getIconByName("open-linked-object");
	}

	public Icon getIconHome16() {
		return this.getIconByName("home");
	}

	public Icon getIconLeft16() {
		return this.getIconByName("left");
	}

	public Icon getIconRight16() {
		return this.getIconByName("right");
	}

	public Icon getIconUp16() {
		return this.getIconByName("up");
	}

	public Icon getIconDown16() {
		return this.getIconByName("down");
	}

	public Icon getIconFirst16() {
		return this.getIconByName("first");
	}

	public Icon getIconPrevious16() {
		return this.getIconByName("previous");
	}

	public Icon getIconNext16() {
		return this.getIconByName("next");
	}

	public Icon getIconPlus16() {
		return this.getIconByName("plus");
	}

	public Icon getIconMinus16() {
		return this.getIconByName("minus");
	}

	public Icon getIconLast16() {
		return this.getIconByName("last");
	}

	public ImageIcon getIconFirstWhite16() {
		return this.getIconByName("first-white");
	}

	public ImageIcon getIconPreviousWhite16() {
		return this.getIconByName("previous-white");
	}

	public ImageIcon getIconNextWhite16() {
		return this.getIconByName("next-white");
	}

	public ImageIcon getIconLastWhite16() {
		return this.getIconByName("last-white");
	}

	public ImageIcon getIconFirstWhiteHover16() {
		return this.getIconByName("first-white-hover");
	}

	public ImageIcon getIconPreviousWhiteHover16() {
		return this.getIconByName("previous-white-hover");
	}

	public ImageIcon getIconNextWhiteHover16() {
		return this.getIconByName("next-white-hover");
	}

	public ImageIcon getIconLastWhiteHover16() {
		return this.getIconByName("last-white-hover");
	}
	
	public ImageIcon getIconSelectAll12() {
		return this.getIconByName("selectAll");
	}

	public ImageIcon getIconDeSelectAll12() {
		return this.getIconByName("deSelectAll");
	}
	
	public ImageIcon getIconSelectAllHover12() {
		return this.getIconByName("selectAll-hover");
	}

	public ImageIcon getIconDeSelectAllHover12() {
		return this.getIconByName("deSelectAll-hover");
	}

	public Icon getIconGoTop16() {
		return this.getIconByName("gotop");
	}

	public Icon getIconGoBottom16() {
		return this.getIconByName("gobottom");
	}
	
	public Icon getIconGoFirst16() {
		return this.getIconByName("gofirst");
	}

	public Icon getIconGoLast16() {
		return this.getIconByName("golast");
	}

	public Icon getIconPlay16() {
		return this.getIconByName("start");
	}

	public Icon getIconPause16() {
		return this.getIconByName("pause");
	}

	public Icon getIconStop16() {
		return this.getIconByName("stop");
	}

	public Icon getIconStateHistory16() {
		return this.getIconByName("history-state");
	}

	public Icon getIconLogBook16() {
		return this.getIconByName("history");
	}

	public Icon getIconTree16() {
		return this.getIconByName("result-in-explorer");
	}

	public Icon getIconMakeTreeRoot16() {
		return this.getIconByName("show-in-explorer");
	}

	public Icon getIconExecuteRule16() {
		return this.getIconByName("rule-execute");
	}

	public Icon getIconRemoveColumn16() {
		return this.getIconByName("column-delete");
	}

	public Icon getIconInsertColumn16() {
		return this.getIconByName("column-insert");
	}
	
	public Icon getIconInsertRow16() {
		return this.getIconByName("row-insert");
	}
	
	public Icon getIconInsertTable16() {
		return this.getIconByName("table-insert");
	}
	
	public Icon getIconInsertCell16() {
		return this.getIconByName("cell-insert");
	}

	public Icon getIconSelectVisibleColumns16() {
		return this.getIconByName("select-visible-columns");
	}

	public Icon getIconPriorityCancel16() {
		return this.getIconByName("priority-cancel");
	}

	public Icon getIconPriorityHigh16() {
		return this.getIconByName("priority-high");
	}

	public Icon getIconPriorityNormal16() {
		return this.getIconByName("priority-normal");
	}

	public Icon getIconPriorityLow16() {
		return this.getIconByName("priority-low");
	}

	public Icon getIconJobError() {
		return this.getIconByName("job-error");
	}

	public Icon getIconJobWarning() {
		return this.getIconByName("job-warning");
	}

	public Icon getIconJobSuccessful() {
		return this.getIconByName("job-successful");
	}

	public Icon getIconJobSuccessfulAlt() {
		return this.getIconByName("job-successful-alt");
	}

	public Icon getIconJobUnknown() {
		return this.getIconByName("job-unknown");
	}

	public Icon getIconJobRunning() {
		return this.getIconByName("job-running");
	}

	public Icon getIconGenericObject16() {
		return this.getIconByName("generic-object");
	}

	public Icon getIconModule() {
		return this.getIconByName("module");
	}

	public Icon getIconRowSelection16() {
		return this.getIconByName("marker-row");
	}

	public Icon getIconValidate16() {
		return this.getIconByName("validate");
	}

	public Icon getIconReverseRelationship16() {
		return this.getIconByName("reverse-relationship");
	}

	public Icon getIconGenerateDbScript16() {
		return this.getIconByName("generate-db-script");
	}

	public Icon getIconCopyToServer16() {
		return this.getIconByName("copy-to-server");
	}

	public Icon getIconTreeParentToChild() {
		return this.getIconByName("tree-parent-to-child");
	}

	public Icon getIconTreeChildToParent() {
		return this.getIconByName("tree-child-to-parent");
	}

	public Icon getIconPartOf() {
		return this.getIconByName("part-of");
	}

	public Icon getIconCompositeOf() {
		return this.getIconByName("composite-of");
	}

	public Icon getIconSortAscending() {
		return this.getIconByName("marker-asc");
	}

	public Icon getIconSortDescending() {
		return this.getIconByName("marker-desc");
	}

	public Icon getIconRelationChildToParent() {
		return this.getIconByName("node-tree-child-to-parent");
	}

	public Icon getIconRelationParentToChild() {
		return this.getIconByName("node-tree-parent-to-child");
	}

	public Icon getIconRuleNode() {
		return this.getIconByName("node-rule");
	}

	public Icon getIconRuleNodeDisabled() {
		return this.getIconByName("node-rule-disabled");
	}

	public Icon getIconStateModel() {
		return this.getIconByName("node-state-model");
	}

	public Icon getIconStateTransitionExplorer() {
		return this.getIconByName("node-state-transition");
	}

	public Icon getIconAdGeneration() {
		return this.getIconByName("node-ad-generation");
	}

	public Icon getIconDatasource() {
		return this.getIconByName("datasource");
	}

	public Icon getIconDatasourceUsed() {
		return this.getIconByName("datasource-used");
	}

	public Icon getIconDatasourceUsing() {
		return this.getIconByName("datasource-using");
	}

	public Icon getIconReport() {
		return this.getIconByName("report");
	}

	public Icon getIconLDAP() {
		return this.getIconByName("ldap");
	}

	public ImageIcon getIconTextFieldButton() {
		return this.getIconByName("textFieldButton");
	}

	public ImageIcon getIconTextFieldButtonHover() {
		return this.getIconByName("textFieldButton_hover");
	}

	public ImageIcon getIconTextFieldButtonPressed() {
		return this.getIconByName("textFieldButton_pressed");
	}

	public ImageIcon getIconTextFieldButtonFile() {
		return this.getIconByName("textFieldButton-file");
	}

	public ImageIcon getIconTextFieldButtonCalendar() {
		return this.getIconByName("textFieldButton-calendar");
	}

	public ImageIcon getIconTextFieldButtonLOV() {
		return this.getIconByName("textFieldButton-lov");
	}

	public ImageIcon getIconTextFieldButtonScript() {
		return this.getIconByName("textFieldButton-script");
	}
	
	public ImageIcon getIconTextFieldButtonCombobox() {
		return this.getIconByName("textFieldButton-combobox");
	}
	
	public ImageIcon getIconTextFieldButtonHyperlink() {
		return this.getIconByName("textFieldButton-hyperlink");
	}

	public ImageIcon getIconTabNotRestored() {
		return this.getIconByName("tabNotRestored");
	}

	public ImageIcon getIconTabGeneric() {
		return this.getIconByName("tabGeneric");
	}

	public ImageIcon getIconTabCloseButton() {
		return this.getIconByName("tabCloseButton");
	}

	public ImageIcon getIconTabCloseButton_hover() {
		return this.getIconByName("tabCloseButton_hover");
	}

	public ImageIcon getIconTabHiddenHint() {
		return this.getIconByName("tabHiddenHint");
	}

	public ImageIcon getIconTabHiddenHint_hover() {
		return this.getIconByName("tabHiddenHint_hover");
	}

	public ImageIcon getIconTabbedPaneClose() {
		return this.getIconByName("tabbedPaneClose");
	}

	public ImageIcon getIconTabbedPaneClose_hover() {
		return this.getIconByName("tabbedPaneClose_hover");
	}

	public ImageIcon getIconTabbedPaneMax() {
		return this.getIconByName("tabbedPaneMax");
	}

	public ImageIcon getIconTabbedPaneMax_hover() {
		return this.getIconByName("tabbedPaneMax_hover");
	}

	public ImageIcon getIconTabbedPaneSplit() {
		return this.getIconByName("tabbedPaneSplit");
	}

	public ImageIcon getIconTabbedPaneSplit_hover() {
		return this.getIconByName("tabbedPaneSplit_hover");
	}

	public ImageIcon getIconTabbedPaneMin() {
		return this.getIconByName("tabbedPaneMin");
	}

	public ImageIcon getIconTabbedPaneMin_hover() {
		return this.getIconByName("tabbedPaneMin_hover");
	}

	public ImageIcon getIconTabbedPaneMaximized() {
		return this.getIconByName("tabbedPaneMaximized");
	}
	
	public ImageIcon getIconStartTab() {
		return this.getIconByName("startTab");
	}

	public ImageIcon getIconTabbedPaneMaximized_Home() {
		return this.getIconByName("tabbedPaneMaximizedHome");
	}

	public ImageIcon getIconTabbedPaneMaximized_HomeTree() {
		return this.getIconByName("tabbedPaneMaximizedHomeTree");
	}

	public ImageIcon getIconTabTask() {
		return this.getIconByName("tabTask");
	}

	public ImageIcon getIconTabTimtlimit() {
		return this.getIconByName("tabTimelimit");
	}

	public ImageIcon getIconOpenInNewTab16() {
		return this.getIconByName("openInNewTab");
	}

	public ImageIcon getIconCustomComponent16() {
		return this.getIconByName("customComponent");
	}

	public ImageIcon getIconMagnifier() {
		return this.getIconByName("magnifier");
	}

	public ImageIcon getIconTest() {
		return this.getIconByName("test");
	}

	public Icon getIconZoomIn() {
		return this.getIconByName("zoom-in");
	}

	public Icon getIconZoomOut() {
		return this.getIconByName("zoom-out");
	}

	public Icon getIconSelectObject() {
		return this.getIconByName("cursor");
	}

	public Icon getIconBookmark16() {
		return this.getIconByName("bookmark");
	}

	public Icon getIconStateNewNote() {
		return this.getIconByName("states.state-note"); // also used as bookmark!
	}

	public Icon getIconStateTransitionRules() {
		return this.getIconByName("states.transition-rules");
	}

	public Icon getIconStateStart() {
		return this.getIconByName("states.transition-start");
	}

	public Icon getIconStateEnd() {
		return this.getIconByName("states.state-end");
	}

	public Icon getIconStateIntermediate() {
		return this.getIconByName("states.state-intermediate");
	}

	public Icon getIconState() {
		return this.getIconByName("states.state");
	}

	public Icon getIconStateTransition() {
		return this.getIconByName("states.transition");
	}

	public Icon getIconStateTransitionAuto() {
		return this.getIconByName("states.transition-auto");
	}

	public Icon getIconStateTransitionDefault() {
		return this.getIconByName("states.transition-default");
	}

	public Icon getIconApplicationGeneric() {
		return this.getIconByName("mimetypes.application-x-generic");
	}

	public Icon getIconApplicationWord() {
		return this.getIconByName("mimetypes.application-x-word");
	}

	public Icon getIconApplicationExcel() {
		return this.getIconByName("mimetypes.application-x-excel");
	}

	public Icon getIconApplicationPowerpoint() {
		return this.getIconByName("mimetypes.application-x-powerpoint");
	}

	public Icon getIconApplicationPdf() {
		return this.getIconByName("mimetypes.application-x-pdf");
	}

	public Icon getIconApplicationText() {
		return this.getIconByName("mimetypes.text-x-generic");
	}

	public Icon getIconRelate() {
		return this.getIconByName("relate");
	}

	public Icon getIconPrefsCopy() {
		return this.getIconByName("preferences-copy");
	}

	public ImageIcon getIconDesktopFolder() {
		return getIconByName("desktop-folder");
	}

	public ImageIcon getArrowButtonX() {
		return this.getIconByName("arrow-button-x");
	}

	public ImageIcon getArrowButtonDown() {
		return this.getIconByName("arrow-button-down");
	}

	public ImageIcon getIconShowList() {
		return this.getIconByName("showList");
	}
	
	public ImageIcon getWorkspaceChooser_bg() {
		return this.getIconByName("workspaceChooser_bg");
	}

	public ImageIcon getWorkspaceChooser_buttonBG() {
		return this.getIconByName("workspaceChooser_buttonBG");
	}
	
	public ImageIcon getWorkspaceChooser_buttonLeft() {
		return this.getIconByName("workspaceChooser_buttonLeft");
	}
	
	public ImageIcon getWorkspaceChooser_buttonRight() {
		return this.getIconByName("workspaceChooser_buttonRight");
	}
	
	public ImageIcon getWorkspaceChooser_left() {
		return this.getIconByName("workspaceChooser_left");
	}
	
	public ImageIcon getWorkspaceChooser_right() {
		return this.getIconByName("workspaceChooser_right");
	}
	
	public ImageIcon getWorkspaceChooser_assigned() {
		return this.getIconByName("workspaceChooser_assigned");
	}
	
	public ImageIcon getTableMoveIndicator() {
		return this.getIconByName("table-move-indicator");
	}
	
	public ImageIcon getIconTargetBorder64() {
		return this.getIconByName("iconTargetBorder64");
	}
	
	public ImageIcon getIconSwitchWorkspace() {
		return this.getIconByName("iconSwitchWorkspace");
	}
	
	public ImageIcon getIconTextBold() {
		return this.getIconByName("text-bold");
	}
	
	public ImageIcon getIconTextItalic() {
		return this.getIconByName("text-italic");
	}
	
	public ImageIcon getIconTextUnderline() {
		return this.getIconByName("text-underline");
	}
	
	public ImageIcon getIconListUnordered() {
		return this.getIconByName("list-unordered");
	}
	
	public ImageIcon getIconListOrdered() {
		return this.getIconByName("list-ordered");
	}
	
	public ImageIcon getIconStateChangeArrow() {
		return this.getIconByName("state-change-arrow");
	}

	public ImageIcon getBarChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/barchart.png");
	}
	
	public ImageIcon getLineChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/linechart.png");
	}
	
	public ImageIcon getPieChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/piechart.png");
	}
	
	public ImageIcon getTimeSeriesChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/timeseries.png");
	}
	
	public ImageIcon getXYSeriesChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/xyseries.png");
	}
	
	public ImageIcon getXYBarChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/xybarchart.png");
	}
	
	public ImageIcon getStepChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/stepchart.png");
	}
	
	public ImageIcon getScatterChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/scatterchart.png");
	}
	
	public ImageIcon getDialChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/dial1.png");
	}
	
	public ImageIcon getDial2ChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/dial2.png");
	}
	
	public ImageIcon getCombinedCategoryChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/combined-category.png");
	}
	
	public ImageIcon getCombinedXYChartIcon() {
		return getCachedImageIcon("org/nuclos/client/layout/wysiwyg/editor/ui/charts/combined-xy.png");
	}
	
	public Icon getStateViewStateNormal(String type) {
		if (type.equals("first"))
			return this.getIconByName("state-view-state-normal-first");
		if (type.equals("second"))
			return this.getIconByName("state-view-state-normal-second");
		if (type.equals("third"))
			return this.getIconByName("state-view-state-normal-third");
		throw new IllegalArgumentException("only first, second, third is allowed as argument here.");
	}
	public Icon getStateViewStateDisabled(String type) {
		if (type.equals("first"))
			return this.getIconByName("state-view-state-disabled-first");
		if (type.equals("second"))
			return this.getIconByName("state-view-state-disabled-second");
		if (type.equals("third"))
			return this.getIconByName("state-view-state-disabled-third");
		throw new IllegalArgumentException("only first, second, third is allowed as argument here.");
	}
	public Icon getStateViewStateSelected(String type) {
		if (type.equals("first"))
			return this.getIconByName("state-view-state-selected-first");
		if (type.equals("second"))
			return this.getIconByName("state-view-state-selected-second");
		if (type.equals("third"))
			return this.getIconByName("state-view-state-selected-third");
		throw new IllegalArgumentException("only first, second, third is allowed as argument here.");
	}
	
	public Icon getStateViewDefaultStateIcon() {
		return this.getIconByName("state-view-state-default");
	}

	public Icon[] getStateIcons() {
		List<Icon> stateIcons = new LinkedList<Icon>();
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_(.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_).png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_@.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_#.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_=.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_A.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_Ae.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_B.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_C.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_D.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_E.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_F.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_G.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_H.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_I.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_J.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_K.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_L.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_M.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_N.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_O.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_Oe.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_P.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_Q.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_r.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_S.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_T.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_U.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_Ue.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_V.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_W.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_X.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_Y.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Alpha_Z.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farb_grau3.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farbe_gelb.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farbe_grau1.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farbe_grau2.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farbe_gruen.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farbe_hellblau.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farbe_nachtblau.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farbe_neon.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farbe_orange.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Farbe_rot.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Sybol_Stern2.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol__.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_!.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_08.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_09.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Fadenkreuz.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Flieger.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Halt.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_karo.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Muell.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Muell3.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Schloss_auf.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Schloss_zu.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_smily.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Sniley.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Stern3.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Symbol_Sternsw.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_0.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_1.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_2.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_3.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_4.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_5.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_6.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_7.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_8.png"));
		stateIcons.add(getCachedImageIcon("org/nuclos/client/ui/images/status/Zahl_9.png"));
		return stateIcons.toArray(new Icon[0]);
	}
	

	/**
	 * Properties with the icon theme (mapping from names to resources)
	 */
	private java.util.Properties iconTheme;

	/**
	 * Icon cache
	 */
	private final Map<String, ImageIcon> icons = new HashMap<String, ImageIcon>();

	public synchronized ImageIcon getIconByName(String name) {
		ImageIcon icon = this.icons.get(name);
		if (icon != null)
			return icon;

		String res = getResource(name);
		if (res == null)
			throw new NullPointerException(name);

		icon = getImageIcon(res);
		icons.put(name, icon);

		return getCachedImageIcon(res);
	}

	private synchronized String getResource(String name) {
		if (iconTheme == null) {
			iconTheme = new java.util.Properties();
			try {
				iconTheme.load(this.getClass().getClassLoader().getResourceAsStream("icons.properties"));
			}
			catch(java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}

		return iconTheme.getProperty(name);
	}

	private synchronized ImageIcon getCachedImageIcon(String sFileName) {
		ImageIcon result = this.icons.get(sFileName);
		if (result == null) {
			result = this.getImageIcon(sFileName);
			this.icons.put(sFileName, result);
		}
		return result;
	}

	private ImageIcon getImageIcon(String sFileName) {
		final URL url = getClass().getClassLoader().getResource(sFileName);
		if (url == null) {
			throw new IllegalArgumentException("Can't find icon resource " + sFileName);
		}
		return new ImageIcon(url);
	}

	public static class ResizedIcon implements Icon {

		private Icon icon;
		private final double f;

		public ResizedIcon(Icon icon, double f) {
			this.icon = icon;
			this.f = f;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g;
			AffineTransform tx = g2d.getTransform();
			g2d.translate(x, y);
			g2d.scale(f, f);
			icon.paintIcon(c, g2d, 0, 0);
			g2d.setTransform(tx);
		}

		@Override
		public int getIconWidth() {
			return (int) (f * icon.getIconWidth());
		}

		@Override
		public int getIconHeight() {
			return (int) (f * icon.getIconHeight());
		}
	}

	public static class CompositeIcon implements Icon
	{
		private Icon icon1;
		private Icon icon2;

		public CompositeIcon(Icon icon1, Icon icon2)
		{
			this.icon1 = icon1;
			this.icon2 = icon2;
		}

		@Override
		public int getIconHeight()
		{
			return Math.max(icon1.getIconHeight(), icon2.getIconHeight());
		}

		@Override
		public int getIconWidth()
		{
			return icon1.getIconWidth() + icon2.getIconWidth();
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			icon1.paintIcon(c, g, x, y);
			icon2.paintIcon(c, g, x + icon1.getIconWidth(), y);
		}
	}

}	// class Icons
