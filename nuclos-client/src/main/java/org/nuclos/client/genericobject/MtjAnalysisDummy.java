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
/*
 * Created on 07.08.2009
 */
package org.nuclos.client.genericobject;


/* Dummy zur Analyse des interface. nur eingechecht, weil ich mal den Branch
 * wechseln mu\u00df...
 */


public abstract class MtjAnalysisDummy /* extends GenericObjectCollectController */ {
//
//	public MtjAnalysisDummy(JComponent parent, Integer iModuleId, boolean bAutoInit) {
//		super(parent, iModuleId, bAutoInit);
//		throw new RuntimeException("Just an analysis class!");
//	}
//
//	/**
//	 * clears all search fields.
//	 */
//	@Override
//	protected void _clearSearchFields() {
//		super._clearSearchFields();
//	}
//
//	// ND
//	@Override
//	protected void _setSearchFieldsAccordingToSubCondition(
//		CollectableSubCondition cond) throws CommonBusinessException {
//		super._setSearchFieldsAccordingToSubCondition(cond);
//	}
//
//	// ND
//	@Override
//	protected void addAdditionalChangeListenersForDetails() {
//		super.addAdditionalChangeListenersForDetails();
//	}
//
//	/**
//	 * mtj: deletes an object physically after checking the according permission
//	 * throws a CommonBusinessException if the permission has not been granted
//	 */
//	@Override
//	protected void checkedDeleteCollectablePhysically(CollectableGenericObjectWithDependants clctlo) throws CommonBusinessException {
//		super.checkedDeleteCollectablePhysically(clctlo);
//	}
//
//	/**
//	 * mtj: restores a deleted object after checking the according permission
//	 * throws a CommonBusinessException if the permission has not been granted
//	 */
//
//	@Override
//	protected void checkedRestoreCollectable(CollectableGenericObjectWithDependants clct) throws CommonBusinessException {
//		super.checkedRestoreCollectable(clct);
//	}
//
//	/**
//	 * This method is called by <code>cmdClearSearchFields</code>, that is when the user clicks
//	 * the "Clear Search Fields" button. This implementation selects the default search filter.
//	 */
//	@Override
//	protected void clearSearchCondition() {
//		super.clearSearchCondition();
//	}
//
//	/**
//	 * switches to "New" mode and fills the Details panel with the contents of the selected Collectable.
//	 * @throws CommonBusinessException
//	 */
//	// mtj: WTF?
//	@Override
//	protected void cloneSelectedCollectable() throws CommonBusinessException {
//		super.cloneSelectedCollectable();
//	}
//
//	/**
//	 * Called when the internal frame is closed. Releases all resources held by the controller.
//	 * This is the right place to remove all listeners.
//	 */
//	@Override
//	protected void close() {
//		super.close();
//	}
//
//	/**
//	 * closes all subform controllers in the Details panel.
//	 */
//	@Override
//	protected void closeSubFormControllersInDetails() {
//		super.closeSubFormControllersInDetails();
//	}
//
//	/**
//	 * command: switch to View mode
//	 * 
//	 * mtj - note: does a permission check, whether the object is marked for
//	 * deletion and - if so - the user has the right to view deleted objects.
//	 * As opposed to the other permission checking methods, this one throws
//	 * a NuclosFatalException instead of a business exception
//	 */
//	@Override
//	protected void cmdEnterViewMode() {
//		super.cmdEnterViewMode();
//	}
//
//	/**
//	 * customizes the given layout, respecting the user rights.
//	 * @param layoutroot
//	 */
//	@Override
//	protected void customizeLayout(LayoutRoot layoutroot, UsageCriteria usagecriteria, CollectState collectstate) {
//		super.customizeLayout(layoutroot, usagecriteria, collectstate);
//	}
//
//	// ND
//	@Override
//	protected void deleteCollectable(CollectableGenericObjectWithDependants clct) throws CommonBusinessException {
//		super.deleteCollectable(clct);
//	}
//
//	// ND
//	@Override
//	public void executeBusinessRules(List<RuleVO> lstRuleVO, boolean bSaveAfterRuleExecution) throws CommonBusinessException {
//		super.executeBusinessRules(lstRuleVO, bSaveAfterRuleExecution);
//	}
//
//	/**
//	 * fills the subform controllers with collectablemasterdata found in the
//	 * given DependantCollectableMasterDataMap
//	 * @param mpDependants
//	 * @throws NuclosBusinessException
//	 */
//	@Override
//	public void fillSubForm(DependantCollectableMasterDataMap mpDependants) throws NuclosBusinessException {
//		super.fillSubForm(mpDependants);
//	}
//
//	// ND
//	@Override
//	public CollectableGenericObjectWithDependants findCollectableById(String sEntityName, Object oId) throws CommonBusinessException {
//		return super.findCollectableById(sEntityName, oId);
//	}
//
//	// ND
//	/**
//	 * @param clct
//	 * @return DependantCollectableMap containing the dependants of the given
//	 *         Collectable relevant for multiple updates
//	 *         additional data (if any) needed for multiple updates.
//	 * @throws CommonValidationException if some subform data is invalid.
//	 */
//	@Override
//	protected DependantCollectableMasterDataMap getAdditionalDataForMultiUpdate(CollectableGenericObjectWithDependants clct) throws CommonValidationException {
//		return super.getAdditionalDataForMultiUpdate(clct);
//	}
//
//	/**
//	 * gathers the data from all enabled subforms. All rows are gathered, even
//	 * the removed ones.
//	 * 
//	 * @param oParentId set as the parent id for each subform row.
//	 * @return the data from all subforms
//	 */
//	@Override
//	protected DependantCollectableMasterDataMap getAllSubFormData(Object oParentId) throws CommonValidationException {
//		return super.getAllSubFormData(oParentId);
//	}
//
//	/**
//	 * @return a Comparator that compares the entity labels first, then the field
//	 *         labels.
//	 *         Fields of the main entity are sorted lower than all other fields.
//	 */
//	@Override
//	protected Comparator<? extends CollectableEntityField> getCollectableEntityFieldComparator() {
//		return super.getCollectableEntityFieldComparator();
//	}
//
//	// ND
//	/**
//	 * We need to return a <code>CollectableEntityFieldWithEntity</code> here so
//	 * we can filter by entity.
//	 * @param clcte
//	 * @param sFieldName
//	 * @return a <code>CollectableEntityField</code> of the given entity with the
//	 * given field name, to be used in the Result metadata.
//	 */
//	@Override
//	protected CollectableEntityField getCollectableEntityFieldForResult(CollectableEntity sClcte, String sFieldName) {
//		return super.getCollectableEntityFieldForResult(sClcte, sFieldName);
//	}
//
//	// ND
//	@Override
//	protected CollectableFieldsProviderFactory getCollectableFieldsProviderFactoryForSearchEditor() {
//		return super.getCollectableFieldsProviderFactoryForSearchEditor();
//	}
//
//	// ND
//	@Override
//	protected ProxyList<CollectableGenericObjectWithDependants> getCollectableProxyList() {
//		return super.getCollectableProxyList();
//	}
//
//	// ND
//	/**
//	 * @param bMakeConsistent
//	 * @return the search condition contained in the search panel's fields
//	 * (including the subforms' search fields).
//	 * @precondition this.isSearchPanelAvailable()
//	 * @postcondition result == null || result.isSyntacticallyCorrect()
//	 */
//	@Override
//	protected CollectableSearchCondition getCollectableSearchConditionFromSearchFields(boolean bMakeConsistent) throws CollectableFieldFormatException {
//		return super.getCollectableSearchConditionFromSearchFields(bMakeConsistent);
//	}
//
//	// ND
//	/**
//	 * @return the search condition to display. Includes the currently selected
//	 * global search filter's search condition (if any).
//	 * @throws CollectableFieldFormatException
//	 */
//	@Override
//	protected CollectableSearchCondition getCollectableSearchConditionToDisplay() throws CollectableFieldFormatException {
//		return super.getCollectableSearchConditionToDisplay();
//	}
//
//	// ND   (jau, das ist der Originalkommentar hier...)
//	// @todo eliminate this workaround
//	@Override
//	protected List<CollectableSorting> getCollectableSortingSequence() {
//		return super.getCollectableSortingSequence();
//	}
//
//	// ND
//	@Override
//	protected SearchFilter getCurrentSearchFilterFromSearchPanel() throws CommonBusinessException {
//		return super.getCurrentSearchFilterFromSearchPanel();
//	}
//
//	/*
//	 * creates a SearchResultTemplate accordng to selected columns in search result
//	 * @throws CommonBusinessException
//	 */
//	@Override
//	protected SearchResultTemplate getCurrentSearchResultFormatFromResultPanel() throws CommonBusinessException {
//		return super.getCurrentSearchResultFormatFromResultPanel();
//	}
//
//	/**
//	 * @return where condition in database syntax
//	 */
//	@Override
//	public String getCustomizedWhereCondition() throws CollectableFieldFormatException {
//		return super.getCustomizedWhereCondition();
//	}
//
//	// ND
//	@Override
//	public GenericObjectDetailsPanel getDetailsPanel() {
//		return super.getDetailsPanel();
//	}
//
//	// ND
//	@Override
//	protected String getEntityLabel() {
//		return super.getEntityLabel();
//	}
//
//	// ND
//	/**
//	 * @param clcte
//	 * @return the fields of the given entity, plus the fields of all subentities for that entity.
//	 */
//	@Override
//	protected List<CollectableEntityField> getFieldsAvailableForResult(CollectableEntity clcte) {
//		return super.getFieldsAvailableForResult(clcte);
//	}
//
//	// ND
//	@Override
//	protected CollectableDateChooser getHistoricalCollectableDateChooser() {
//		return super.getHistoricalCollectableDateChooser();
//	}
//
//	// ND
//	@Override
//	protected LayoutRoot getInitialLayoutMLDefinitionForDetailsPanel() {
//		return super.getInitialLayoutMLDefinitionForDetailsPanel();
//	}
//
//	/**
//	 * parses the LayoutML definition and gets the layout information
//	 * @return the LayoutRoot containing the layout information
//	 */
//	@Override
//	protected LayoutRoot getInitialLayoutMLDefinitionForSearchPanel() {
//		return super.getInitialLayoutMLDefinitionForSearchPanel();
//	}
//
//	// ND
//	/**
//	 * @return search expression containing the internal version of the collectable search condition,
//	 *         that is used for performing the actual search, and the sorting sequence.
//	 * @throws CollectableFieldFormatException
//	 */
//	@Override
//	protected CollectableGenericObjectSearchExpression getInternalSearchExpression() throws CollectableFieldFormatException {
//		return super.getInternalSearchExpression();
//	}
//
//	/**
//	 * @return Map<String sEntity, DetailsSubFormController>. May be <code>null</code>.
//	 */
//	@Override
//	protected Map<String, DetailsSubFormController<CollectableMasterData>> getMapOfSubFormControllersInDetails() {
//		return super.getMapOfSubFormControllersInDetails();
//	}
//
//	// ND
//	@Override
//	protected Map<String, SearchConditionSubFormController> getMapOfSubFormControllersInSearch() {
//		return super.getMapOfSubFormControllersInSearch();
//	}
//
//	/**
//	 * @return <code>MultiActionProgressPanel</code> for the MultiObjectsActionController.
//	 */
//	@Override
//	protected MultiActionProgressPanel getMultiActionProgressPanel(int iCount) {
//		return super.getMultiActionProgressPanel(iCount);
//	}
//
//	// ND
//	/**
//	 * Get also changes in subforms
//	 * @todo move to DetailsPanel
//	 */
//	@Override
//	protected String getMultiEditChangeString() {
//		return super.getMultiEditChangeString();
//	}
//
//	/**
//	 * @return the parent entity, if any, of this <code>CollectController</code>'s entity.
//	 */
//	@Override
//	protected CollectableEntity getParentEntity() {
//		return super.getParentEntity();
//	}
//
//	// ND
//	@Override
//	public GenericObjectResultPanel getResultPanel() {
//		return super.getResultPanel();
//	}
//
//	/**
//	 * @return the search filters that can be edited in this collect controller.
//	 *         By default, it's the search filters for this collect controller's module.
//	 */
//	@Override
//	protected SearchFilters getSearchFilters() {
//		return super.getSearchFilters();
//	}
//
//	// ND
//	@Override
//	public GenericObjectSearchPanel getSearchPanel() {
//		return super.getSearchPanel();
//	}
//
//	// ND
//	@Override
//	protected org.nuclos.client.ui.collect.CollectController.SearchWorker<CollectableGenericObjectWithDependants> getSearchWorker() {
//		return super.getSearchWorker();
//	}
//
//	// ND
//	@Override
//	protected org.nuclos.client.ui.collect.CollectController.SearchWorker<CollectableGenericObjectWithDependants> getSearchWorker(
//		List<Observer> lstObservers) {
//		return super.getSearchWorker(lstObservers);
//	}
//
//	/**
//	 * @return the module id of the selected Collectable, if any. The module id of this controller, otherwise.
//	 */
//	@Override
//	protected Integer getSelectedCollectableModuleId() {
//		return super.getSelectedCollectableModuleId();
//	}
//
//	/**
//	 * @return Set<String>
//	 * @postcondition result != null
//	 */
//	@Override
//	protected Set<String> getSelectedSubEntityNames() {
//		return super.getSelectedSubEntityNames();
//	}
//
//	/**
//	 * @return the source object's id if current collectable is generated
//	 */
//	@Override
//	public Integer getSourceObjectId() {
//		return super.getSourceObjectId();
//	}
//
//	// ND
//	@Override
//	protected Collection<? extends SubFormController> getSubFormControllers(boolean bSearchTab) {
//		return super.getSubFormControllers(bSearchTab);
//	}
//
//	// ND
//	/**
//	 * @postcondition result != null
//	 */
//	@Override
//	protected Collection<DetailsSubFormController<CollectableMasterData>> getSubFormControllersInDetails() {
//		return super.getSubFormControllersInDetails();
//	}
//
//	// ND
//	@Override
//	protected String getTitle(int iTab, int iMode) {
//		return super.getTitle(iTab, iMode);
//	}
//
//	// ND
//	@Override
//	public Integer getVersionOfCollectableById(String sEntityName, Object oId) throws CommonBusinessException {
//		return super.getVersionOfCollectableById(sEntityName, oId);
//	}
//
//	// ND
//	/**
//	 * refactoring to avoid dirty use of SwingUtilities.invokeLater()
//	 * allows local variable initialization of extenders [FS]
//	 *
//	 */
//	@Override
//	protected void init() {
//		super.init();
//	}
//
//	// ND
//	@Override
//	protected CollectableGenericObjectWithDependants insertCollectable(CollectableGenericObjectWithDependants clctNew) throws CommonBusinessException {
//		return super.insertCollectable(clctNew);
//	}
//
//	// ND
//	/**
//	 * @return whether the current collectable is generated
//	 */
//	@Override
//	public boolean isCollectableGenerated() {
//		return super.isCollectableGenerated();
//	}
//
//	// ND
//	@Override
//	protected boolean isCurrentRecordWritable() {
//		return super.isCurrentRecordWritable();
//	}
//
//	/**
//	 * generic objects may deleted if we are not in historical view <code>AND</code>
//	 * ((Delete is allowed for this module and the current user and the user has the right to write the object)
//	 * <code>OR</code> this object is in its initial state and the current user created this object.)
//	 * @param clct
//	 * @return Is the "Delete" action for the given Collectable allowed?
//	 */
//	@Override
//	protected boolean isDeleteAllowed(CollectableGenericObjectWithDependants clct) {
//		return super.isDeleteAllowed(clct);
//	}
//
//	// ND
//	/**
//	 * @return true
//	 */
//	@Override
//	protected boolean isMultiEditAllowed() {
//		return super.isMultiEditAllowed();
//	}
//
//	// ND
//	@Override
//	protected boolean isPhysicallyDeleteAllowed(CollectableGenericObjectWithDependants clct) {
//		return super.isPhysicallyDeleteAllowed(clct);
//	}
//
//	/**
//	 * @return Is the "Read" action for the given Collectable allowed? May be overridden by subclasses.
//	 * @precondition clct != null
//	 */
//	@Override
//	protected boolean isReadAllowed(CollectableGenericObjectWithDependants clct) {
//		return super.isReadAllowed(clct);
//	}
//
//	/**
//	 * @return Is the "Read" action for the given set of Collectables allowed? May be overridden by subclasses.
//	 * @precondition clct != null
//	 */
//	@Override
//	protected boolean isReadAllowed(List<CollectableGenericObjectWithDependants> lsClct) {
//		return super.isReadAllowed(lsClct);
//	}
//
//	@Override
//	protected boolean isSaveAllowed() {
//		// TODO Auto-generated method stub
//		return super.isSaveAllowed();
//	}
//
//	@Override
//	protected void loadSpecializedLayoutForDetails() {
//		// TODO Auto-generated method stub
//		super.loadSpecializedLayoutForDetails();
//	}
//
//	@Override
//	public void makeConsistent(boolean bSearchTab)
//		throws CollectableFieldFormatException {
//		// TODO Auto-generated method stub
//		super.makeConsistent(bSearchTab);
//	}
//
//	@Override
//	public CollectableGenericObjectWithDependants newCollectable() {
//		// TODO Auto-generated method stub
//		return super.newCollectable();
//	}
//
//	@Override
//	protected void newCollectableWithDependantSearchValues()
//		throws NuclosBusinessException {
//		// TODO Auto-generated method stub
//		super.newCollectableWithDependantSearchValues();
//	}
//
//	@Override
//	protected CollectableGenericObjectWithDependants newCollectableWithSearchValues(
//		CollectableGenericObjectWithDependants currclct)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		return super.newCollectableWithSearchValues(currclct);
//	}
//
//	@Override
//	protected CollectPanel newCollectPanel() {
//		// TODO Auto-generated method stub
//		return super.newCollectPanel();
//	}
//
//	@Override
//	protected Map<String, DetailsSubFormController<CollectableMasterData>> newDetailsSubFormControllers(
//		Map<String, SubForm> mpSubForms) {
//		// TODO Auto-generated method stub
//		return super.newDetailsSubFormControllers(mpSubForms);
//	}
//
//	@Override
//	protected SortableCollectableTableModel<Collectable> newResultTableModel() {
//		// TODO Auto-generated method stub
//		return super.newResultTableModel();
//	}
//
//	@Override
//	protected Map<String, SearchConditionSubFormController> newSearchConditionSubFormControllers(
//		Map<String, SubForm> mpSubForms) {
//		// TODO Auto-generated method stub
//		return super.newSearchConditionSubFormControllers(mpSubForms);
//	}
//
//	@Override
//	public CollectableGenericObjectWithDependants readCollectable(
//		CollectableGenericObjectWithDependants clct)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		return super.readCollectable(clct);
//	}
//
//	@Override
//	protected CollectableGenericObjectWithDependants readSelectedCollectable()
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		return super.readSelectedCollectable();
//	}
//
//	@Override
//	protected List<CollectableEntityFieldWithEntity> readSelectedFieldsFromPreferences(
//		CollectableEntity clcte) {
//		// TODO Auto-generated method stub
//		return super.readSelectedFieldsFromPreferences(clcte);
//	}
//
//	@Override
//	protected void removeAdditionalChangeListenersForDetails() {
//		// TODO Auto-generated method stub
//		super.removeAdditionalChangeListenersForDetails();
//	}
//
//	@Override
//	protected void removeAdditionalChangeListenersForSearch() {
//		// TODO Auto-generated method stub
//		super.removeAdditionalChangeListenersForSearch();
//	}
//
//	@Override
//	protected void removePreviousChangeListenersForResultTableVerticalScrollBar() {
//		// TODO Auto-generated method stub
//		super.removePreviousChangeListenersForResultTableVerticalScrollBar();
//	}
//
//	@Override
//	protected void respectRights(Collection<CollectableComponent> collclctcomp,
//		Collection<SubForm> collsubform, UsageCriteria usagecriteria,
//		CollectState collectstate) {
//		// TODO Auto-generated method stub
//		super.respectRights(collclctcomp, collsubform, usagecriteria, collectstate);
//	}
//
//	@Override
//	protected void restoreSearchCriteriaFromPreferences(Preferences prefs)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.restoreSearchCriteriaFromPreferences(prefs);
//	}
//
//	@Override
//	protected int restoreStateFromPreferences(Preferences prefs)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		return super.restoreStateFromPreferences(prefs);
//	}
//
//	@Override
//	public void runLookupCollectable(CollectableListOfValues clctlovSource)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.runLookupCollectable(clctlovSource);
//	}
//
//	@Override
//	public void save() throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.save();
//	}
//
//	@Override
//	protected void search() throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.search();
//	}
//
//	@Override
//	protected void search(boolean bRefreshOnly) throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.search(bRefreshOnly);
//	}
//
//	@Override
//	protected void setCollectableProxyList(
//		ProxyList<CollectableGenericObjectWithDependants> proxylstclct) {
//		// TODO Auto-generated method stub
//		super.setCollectableProxyList(proxylstclct);
//	}
//
//	@Override
//	public void setColumnWidths(JTable tbl) {
//		// TODO Auto-generated method stub
//		super.setColumnWidths(tbl);
//	}
//
//	@Override
//	protected void setHistoricalDate(Date dateHistorical) {
//		// TODO Auto-generated method stub
//		super.setHistoricalDate(dateHistorical);
//	}
//
//	@Override
//	public void setMapOfSubFormControllersInDetails(
//		Map<String, DetailsSubFormController<CollectableMasterData>> mpSubFormControllersInDetails) {
//		// TODO Auto-generated method stub
//		super.setMapOfSubFormControllersInDetails(mpSubFormControllersInDetails);
//	}
//
//	@Override
//	public void setSearchDeleted(Integer iSearchDeleted) {
//		// TODO Auto-generated method stub
//		super.setSearchDeleted(iSearchDeleted);
//	}
//
//	@Override
//	protected void setSearchFieldsAccordingToSearchCondition(
//		CollectableSearchCondition cond, boolean bClearSearchFields)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.setSearchFieldsAccordingToSearchCondition(cond, bClearSearchFields);
//	}
//
//	@Override
//	protected void setSearchResultFormatAccordingToTemplate(
//		SearchResultTemplate templateSelected) {
//		// TODO Auto-generated method stub
//		super.setSearchResultFormatAccordingToTemplate(templateSelected);
//	}
//
//	@Override
//	public void setupAdditionalActions() {
//		// TODO Auto-generated method stub
//		super.setupAdditionalActions();
//	}
//
//	@Override
//	protected void setupChangeListenerForResultTableVerticalScrollBar() {
//		// TODO Auto-generated method stub
//		super.setupChangeListenerForResultTableVerticalScrollBar();
//	}
//
//	@Override
//	protected void setupSearchToolBar() {
//		// TODO Auto-generated method stub
//		super.setupSearchToolBar();
//	}
//
//	@Override
//	protected void setupSubFormController(Map<String, SubForm> mpSubForm,
//		Map<String, ? extends SubFormController> mpSubFormController) {
//		// TODO Auto-generated method stub
//		super.setupSubFormController(mpSubForm, mpSubFormController);
//	}
//
//	@Override
//	protected void showFrame() {
//		// TODO Auto-generated method stub
//		super.showFrame();
//	}
//
//	@Override
//	protected boolean stopEditing(boolean bSearchTab) {
//		// TODO Auto-generated method stub
//		return super.stopEditing(bSearchTab);
//	}
//
//	@Override
//	protected boolean stopEditingInDetails() {
//		// TODO Auto-generated method stub
//		return super.stopEditingInDetails();
//	}
//
//	@Override
//	protected boolean stopEditingInSearch() {
//		// TODO Auto-generated method stub
//		return super.stopEditingInSearch();
//	}
//
//	@Override
//	protected void transferCustomDataInDetailsPanel(LayoutRoot layoutroot,
//		JComponent compEditOld, JComponent compEditNew) {
//		// TODO Auto-generated method stub
//		super.transferCustomDataInDetailsPanel(layoutroot, compEditOld, compEditNew);
//	}
//
//	@Override
//	protected void trimAllStringValues(GenericObjectWithDependantsVO lowdcvo) {
//		// TODO Auto-generated method stub
//		super.trimAllStringValues(lowdcvo);
//	}
//
//	@Override
//	protected void unsafeFillDetailsPanel(
//		CollectableGenericObjectWithDependants clct)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.unsafeFillDetailsPanel(clct);
//	}
//
//	@Override
//	protected void unsafeFillMultiEditDetailsPanel(
//		Collection<CollectableGenericObjectWithDependants> collclct)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.unsafeFillMultiEditDetailsPanel(collclct);
//	}
//
//	@Override
//	protected CollectableGenericObjectWithDependants updateCollectable(
//		CollectableGenericObjectWithDependants clct, Object oAdditionalData)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		return super.updateCollectable(clct, oAdditionalData);
//	}
//
//	@Override
//	protected CollectableGenericObjectWithDependants updateCurrentCollectable(
//		CollectableGenericObjectWithDependants clctCurrent)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		return super.updateCurrentCollectable(clctCurrent);
//	}
//
//	@Override
//	protected void writeSearchCriteriaToPreferences(Preferences prefs)
//		throws PreferencesException {
//		// TODO Auto-generated method stub
//		super.writeSearchCriteriaToPreferences(prefs);
//	}
//
//	@Override
//	protected void writeSelectedFieldsToPreferences(
//		List<? extends CollectableEntityField> lstclctefweSelected)
//		throws PreferencesException {
//		// TODO Auto-generated method stub
//		super.writeSelectedFieldsToPreferences(lstclctefweSelected);
//	}
//
//	@Override
//	protected void writeStateToPreferences(Preferences prefs)
//		throws PreferencesException {
//		// TODO Auto-generated method stub
//		super.writeStateToPreferences(prefs);
//	}
//
//	@Override
//	protected void addAdditionalChangeListeners(boolean bSearchable) {
//		// TODO Auto-generated method stub
//		super.addAdditionalChangeListeners(bSearchable);
//	}
//
//	@Override
//	protected void addAdditionalChangeListenersForSearch() {
//		// TODO Auto-generated method stub
//		super.addAdditionalChangeListenersForSearch();
//	}
//
//	@Override
//	protected void enterNewChangedMode() {
//		// TODO Auto-generated method stub
//		super.enterNewChangedMode();
//	}
//
//	@Override
//	public Collection<String> getAdditionalLoaderNames() {
//		// TODO Auto-generated method stub
//		return super.getAdditionalLoaderNames();
//	}
//
//	@Override
//	protected ChangeListener getChangeListener(boolean bSearchable) {
//		// TODO Auto-generated method stub
//		return super.getChangeListener(bSearchable);
//	}
//
//	@Override
//	public org.nuclos.client.common.EntityCollectController.SubFormsLoader getSubFormsLoader() {
//		// TODO Auto-generated method stub
//		return super.getSubFormsLoader();
//	}
//
//	@Override
//	protected void initialize(CollectPanel pnlCollect) {
//		// TODO Auto-generated method stub
//		super.initialize(pnlCollect);
//	}
//
//	@Override
//	protected void initSubFormsLoader() {
//		// TODO Auto-generated method stub
//		super.initSubFormsLoader();
//	}
//
//	@Override
//	protected boolean isCloneAllowed() {
//		// TODO Auto-generated method stub
//		return super.isCloneAllowed();
//	}
//
//	@Override
//	protected boolean isDeleteSelectedCollectableAllowed() {
//		// TODO Auto-generated method stub
//		return super.isDeleteSelectedCollectableAllowed();
//	}
//
//	@Override
//	protected boolean isNotLoadingSubForms() {
//		// TODO Auto-generated method stub
//		return super.isNotLoadingSubForms();
//	}
//
//	@Override
//	public MasterDataSubFormController newDetailsSubFormController(
//		SubForm subform, String sParentEntityName,
//		CollectableComponentModelProvider clctcompmodelprovider,
//		JInternalFrame ifrmParent, JComponent parent, JComponent compDetails,
//		Preferences prefs) {
//		// TODO Auto-generated method stub
//		return super.newDetailsSubFormController(subform, sParentEntityName,
//			clctcompmodelprovider, ifrmParent, parent, compDetails, prefs);
//	}
//
//	@Override
//	protected void showLoading(boolean loading) {
//		// TODO Auto-generated method stub
//		super.showLoading(loading);
//	}
//
//	@Override
//	public void cmdExecuteRuleByUser(CommonJInternalFrame iFrame,
//		String sEntityName, CollectableGenericObjectWithDependants clct) {
//		// TODO Auto-generated method stub
//		super.cmdExecuteRuleByUser(iFrame, sEntityName, clct);
//	}
//
//	@Override
//	protected void cmdSetCollectableSearchConditionAccordingToFilter() {
//		// TODO Auto-generated method stub
//		super.cmdSetCollectableSearchConditionAccordingToFilter();
//	}
//
//	@Override
//	protected Collectable getCurrentCollectableFromPreferences(Preferences prefs)
//		throws PreferencesException, CommonBusinessException {
//		// TODO Auto-generated method stub
//		return super.getCurrentCollectableFromPreferences(prefs);
//	}
//
//	@Override
//	public String getEntity() {
//		// TODO Auto-generated method stub
//		return super.getEntity();
//	}
//
//	@Override
//	protected synchronized ActionListener getLayoutMLButtonsActionListener() {
//		// TODO Auto-generated method stub
//		return super.getLayoutMLButtonsActionListener();
//	}
//
//	@Override
//	public JComponent getParent() {
//		// TODO Auto-generated method stub
//		return super.getParent();
//	}
//
//	@Override
//	public JComboBox getSearchFilterComboBox() {
//		// TODO Auto-generated method stub
//		return super.getSearchFilterComboBox();
//	}
//
//	@Override
//	protected Preferences getUserPreferencesRoot() {
//		// TODO Auto-generated method stub
//		return super.getUserPreferencesRoot();
//	}
//
//	@Override
//	protected void handleSaveException(CommonBusinessException ex,
//		String sMessage1) throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.handleSaveException(ex, sMessage1);
//	}
//
//	@Override
//	protected boolean isMultiThreadingEnabled() {
//		// TODO Auto-generated method stub
//		return super.isMultiThreadingEnabled();
//	}
//
//	@Override
//	public boolean isRestorableFromPreferences() {
//		// TODO Auto-generated method stub
//		return super.isRestorableFromPreferences();
//	}
//
//	@Override
//	protected boolean isTransferable() {
//		// TODO Auto-generated method stub
//		return super.isTransferable();
//	}
//
//	@Override
//	public void lockFrame(boolean bLock) {
//		// TODO Auto-generated method stub
//		super.lockFrame(bLock);
//	}
//
//	@Override
//	protected JToolBar newCustomSearchToolBar() {
//		// TODO Auto-generated method stub
//		return super.newCustomSearchToolBar();
//	}
//
//	@Override
//	protected CommonJInternalFrame newInternalFrame() {
//		// TODO Auto-generated method stub
//		return super.newInternalFrame();
//	}
//
//	@Override
//	protected void prepareCollectableForSaving(
//		CollectableGenericObjectWithDependants clctCurrent,
//		CollectableEntity clcteCurrent) {
//		// TODO Auto-generated method stub
//		super.prepareCollectableForSaving(clctCurrent, clcteCurrent);
//	}
//
//	@Override
//	protected void refreshFilterView() {
//		// TODO Auto-generated method stub
//		super.refreshFilterView();
//	}
//
//	@Override
//	protected void refreshResult() throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.refreshResult();
//	}
//
//	@Override
//	protected void refreshResult(List<Observer> lstObserver)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.refreshResult(lstObserver);
//	}
//
//	@Override
//	public void run() throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.run();
//	}
//
//	@Override
//	protected void run(boolean bStartWithSearchPanel)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.run(bStartWithSearchPanel);
//	}
//
//	@Override
//	public void selectDefaultFilter() {
//		// TODO Auto-generated method stub
//		super.selectDefaultFilter();
//	}
//
//	@Override
//	protected void setDefaultWindowState(JInternalFrame ifrm) {
//		// TODO Auto-generated method stub
//		super.setDefaultWindowState(ifrm);
//	}
//
//	@Override
//	protected void setupShortcutsForTabs(JInternalFrame frame) {
//		// TODO Auto-generated method stub
//		super.setupShortcutsForTabs(frame);
//	}
//
//	@Override
//	protected void viewAll() throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.viewAll();
//	}
//
//	@Override
//	protected void writeCurrentCollectableToPreferences(Preferences prefs)
//		throws PreferencesException {
//		// TODO Auto-generated method stub
//		super.writeCurrentCollectableToPreferences(prefs);
//	}
//
//	@Override
//	protected void writeSerializableCurrentCollectableIdToPreferences(
//		Preferences prefs) throws PreferencesException {
//		// TODO Auto-generated method stub
//		super.writeSerializableCurrentCollectableIdToPreferences(prefs);
//	}
//
//	@Override
//	public void writeToPreferences(Preferences prefs)
//		throws PreferencesException {
//		// TODO Auto-generated method stub
//		super.writeToPreferences(prefs);
//	}
//
//	@Override
//	protected void _setSearchFieldsAccordingToReferencingSearchCondition(
//		ReferencingCollectableSearchCondition refcond)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super._setSearchFieldsAccordingToReferencingSearchCondition(refcond);
//	}
//
//	@Override
//	public boolean askAndSaveIfNecessary() {
//		// TODO Auto-generated method stub
//		return super.askAndSaveIfNecessary();
//	}
//
//	@Override
//	protected void clearSearchFields() {
//		// TODO Auto-generated method stub
//		super.clearSearchFields();
//	}
//
//	@Override
//	protected void cmdCloneSelectedCollectable() {
//		// TODO Auto-generated method stub
//		super.cmdCloneSelectedCollectable();
//	}
//
//	@Override
//	protected void cmdEnterMultiViewMode() {
//		// TODO Auto-generated method stub
//		super.cmdEnterMultiViewMode();
//	}
//
//	@Override
//	protected void cmdEnterNewMode() {
//		// TODO Auto-generated method stub
//		super.cmdEnterNewMode();
//	}
//
//	@Override
//	protected void cmdEnterNewModeWithSearchValues() {
//		// TODO Auto-generated method stub
//		super.cmdEnterNewModeWithSearchValues();
//	}
//
//	@Override
//	protected void cmdFrameClosing() {
//		// TODO Auto-generated method stub
//		super.cmdFrameClosing();
//	}
//
//	@Override
//	protected void cmdRefreshCurrentCollectable() {
//		// TODO Auto-generated method stub
//		super.cmdRefreshCurrentCollectable();
//	}
//
//	@Override
//	protected void cmdViewSelectedCollectables() {
//		// TODO Auto-generated method stub
//		super.cmdViewSelectedCollectables();
//	}
//
//	@Override
//	public void disableToolbarButtons() {
//		// TODO Auto-generated method stub
//		super.disableToolbarButtons();
//	}
//
//	@Override
//	public void enableToolbarButtonsForDetailsMode(int iDetailsMode) {
//		// TODO Auto-generated method stub
//		super.enableToolbarButtonsForDetailsMode(iDetailsMode);
//	}
//
//	@Override
//	public void forceUnlockFrame() {
//		// TODO Auto-generated method stub
//		super.forceUnlockFrame();
//	}
//
//	@Override
//	protected CollectableEntity getCollectableEntity() {
//		// TODO Auto-generated method stub
//		return super.getCollectableEntity();
//	}
//
//	@Override
//	protected CollectableSearchCondition getCollectableSearchCondition()
//		throws CollectableFieldFormatException {
//		// TODO Auto-generated method stub
//		return super.getCollectableSearchCondition();
//	}
//
//	@Override
//	protected CollectNavigationModel getCollectNavigationModel() {
//		// TODO Auto-generated method stub
//		return super.getCollectNavigationModel();
//	}
//
//	@Override
//	public CollectPanel getCollectPanel() {
//		// TODO Auto-generated method stub
//		return super.getCollectPanel();
//	}
//
//	@Override
//	public CollectState getCollectState() {
//		// TODO Auto-generated method stub
//		return super.getCollectState();
//	}
//
//	@Override
//	public List<CollectableComponent> getDetailCollectableComponentsFor(
//		String sFieldName) {
//		// TODO Auto-generated method stub
//		return super.getDetailCollectableComponentsFor(sFieldName);
//	}
//
//	@Override
//	public Fields getFields() {
//		// TODO Auto-generated method stub
//		return super.getFields();
//	}
//
//	@Override
//	protected List<CollectableEntityField> getFieldsFromFieldNames(
//		CollectableEntity clcte, List<String> fieldNames) {
//		// TODO Auto-generated method stub
//		return super.getFieldsFromFieldNames(clcte, fieldNames);
//	}
//
//	@Override
//	public CommonJInternalFrame getFrame() {
//		// TODO Auto-generated method stub
//		return super.getFrame();
//	}
//
//	@Override
//	protected CollectableSearchCondition getImportedSearchCondition() {
//		// TODO Auto-generated method stub
//		return super.getImportedSearchCondition();
//	}
//
//	@Override
//	public Preferences getPreferences() {
//		// TODO Auto-generated method stub
//		return super.getPreferences();
//	}
//
//	@Override
//	public Collection<SearchComponentModel> getSearchCollectableComponentModels() {
//		// TODO Auto-generated method stub
//		return super.getSearchCollectableComponentModels();
//	}
//
//	@Override
//	protected boolean isFieldToBeDisplayedInTable(String sFieldName) {
//		// TODO Auto-generated method stub
//		return super.isFieldToBeDisplayedInTable(sFieldName);
//	}
//
//	@Override
//	public boolean isLocked() {
//		// TODO Auto-generated method stub
//		return super.isLocked();
//	}
//
//	@Override
//	protected boolean isNavigationAllowed() {
//		// TODO Auto-generated method stub
//		return super.isNavigationAllowed();
//	}
//
//	@Override
//	protected boolean isNewAllowed() {
//		// TODO Auto-generated method stub
//		return super.isNewAllowed();
//	}
//
//	@Override
//	protected boolean isRefreshSelectedCollectableAllowed() {
//		// TODO Auto-generated method stub
//		return super.isRefreshSelectedCollectableAllowed();
//	}
//
//	@Override
//	protected boolean isSetAllowedForClctComponent(CollectableComponent clctcomp) {
//		// TODO Auto-generated method stub
//		return super.isSetAllowedForClctComponent(clctcomp);
//	}
//
//	@Override
//	protected void makeSureSelectedFieldsAreNonEmpty(CollectableEntity clcte,
//		List<CollectableEntityField> lstclctefSelected) {
//		// TODO Auto-generated method stub
//		super.makeSureSelectedFieldsAreNonEmpty(clcte, lstclctefSelected);
//	}
//
//	@Override
//	protected CollectableGenericObjectWithDependants newCollectableWithDefaultValues() {
//		// TODO Auto-generated method stub
//		return super.newCollectableWithDefaultValues();
//	}
//
//	@Override
//	protected boolean readIsAscendingFromPreferences() {
//		// TODO Auto-generated method stub
//		return super.readIsAscendingFromPreferences();
//	}
//
//	@Override
//	protected List<Integer> readOrderedFieldsFromPreferences() {
//		// TODO Auto-generated method stub
//		return super.readOrderedFieldsFromPreferences();
//	}
//
//	@Override
//	protected void readValuesFromEditPanel(
//		CollectableGenericObjectWithDependants clct, boolean bSearchTab)
//		throws CollectableValidationException {
//		// TODO Auto-generated method stub
//		super.readValuesFromEditPanel(clct, bSearchTab);
//	}
//
//	@Override
//	public void refreshCurrentCollectable() throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.refreshCurrentCollectable();
//	}
//
//	@Override
//	protected void removeImportedSearchConditionWithStatus() {
//		// TODO Auto-generated method stub
//		super.removeImportedSearchConditionWithStatus();
//	}
//
//	@Override
//	protected void setCollectableComponentModelsInDetailsPanelMultiEditable(
//		boolean bMultiEditable) {
//		// TODO Auto-generated method stub
//		super.setCollectableComponentModelsInDetailsPanelMultiEditable(bMultiEditable);
//	}
//
//	@Override
//	protected void setCollectState(int iTab, int iMode)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.setCollectState(iTab, iMode);
//	}
//
//	@Override
//	public void setDisplayMixedSearchConditionForSearchEditor(
//		boolean isMixedSearchCondition) {
//		// TODO Auto-generated method stub
//		super.setDisplayMixedSearchConditionForSearchEditor(isMixedSearchCondition);
//	}
//
//	@Override
//	protected void setImportedSearchCondition(
//		CollectableSearchCondition pImportedSearchCondition) {
//		// TODO Auto-generated method stub
//		super.setImportedSearchCondition(pImportedSearchCondition);
//	}
//
//	@Override
//	protected void setImportedSearchConditionWithStatus(
//		CollectableSearchCondition searchCondition) {
//		// TODO Auto-generated method stub
//		super.setImportedSearchConditionWithStatus(searchCondition);
//	}
//
//	@Override
//	protected void setTitle() {
//		// TODO Auto-generated method stub
//		super.setTitle();
//	}
//
//	@Override
//	protected void setTitle(String sTitle) {
//		// TODO Auto-generated method stub
//		super.setTitle(sTitle);
//	}
//
//	@Override
//	protected void validate(CollectableGenericObjectWithDependants clct)
//		throws CommonBusinessException {
//		// TODO Auto-generated method stub
//		super.validate(clct);
//	}
//
//	@Override
//	protected void writeColumnOrderToPreferences() {
//		// TODO Auto-generated method stub
//		super.writeColumnOrderToPreferences();
//	}

}
