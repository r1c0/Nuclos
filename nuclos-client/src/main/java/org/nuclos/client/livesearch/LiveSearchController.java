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

package org.nuclos.client.livesearch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.entityobject.EntityFacadeDelegate;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.MainController;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MasterDataLayoutHelper;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Factories;
import org.nuclos.common.collection.LazyInitMapWrapper;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.livesearch.ejb3.LiveSearchFacadeRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LiveSearchController implements LiveSearchSearchPaneListener, LiveSearchResultPaneListener {
	
	private static final Logger LOG = Logger.getLogger(LiveSearchController.class);

	private static final int MAX_RESULTS = 500;
	
	private static LiveSearchController INSTANCE;
	
	//

	private JFrame                              parentFrame;
    private SearchComponent                     searchComponent;
    private LiveSearchPane                      resultPane;

    private volatile String                     currentSearchText;
    private ArrayList<LiveSearchResultRow>      currentResult;

    // Search queue and the consumer thread
    private LinkedBlockingQueue<SearchDef>	    searchQueue;
    private SearchThread	                    searchThread;

    // Preparation queue and the consumer thread
    private LinkedBlockingQueue<Pair<SearchDef, List<Pair<EntityObjectVO, Set<String>>>>>
                                                preparationQueue;
    private PreparationThread                   preparationThread;

    private Bubble                              overflowMessage;
    private String                              bubbleMessage;
    
    private ResourceCache resourceCache;

    LiveSearchController() {
    	INSTANCE = this;
	}
    
    @Autowired
    void setResourceCache(ResourceCache resourceCache) {
    	this.resourceCache = resourceCache;
    }
    
    @Autowired
    void setParentFrame(MainFrame parentFrame) {
    	this.parentFrame = parentFrame;
    }
    
    public static LiveSearchController getInstance() {
    	return INSTANCE;
    }
    
    public final void init() {
        // this.parentFrame = parentFrame;
        searchComponent = new SearchComponent();
        searchComponent.setMaximumSize(searchComponent.getPreferredSize());

        searchComponent.addLiveSearchSearchPaneListener(this);
        searchComponent.addKeyListener(searchFieldListener);

        currentResult = new ArrayList<LiveSearchResultRow>();

        searchQueue = new LinkedBlockingQueue<SearchDef>();
        searchThread = new SearchThread();

        preparationQueue = new LinkedBlockingQueue<Pair<SearchDef, List<Pair<EntityObjectVO, Set<String>>>>>();
        preparationThread = new PreparationThread();

        resultPane = new LiveSearchPane();
        resultPane.addLiveSearchPaneListener(this);

        bubbleMessage = SpringLocaleDelegate.getInstance().getResource("livesearch.controller.overflow", null);

        searchComponent.addFocusListener(new FocusAdapter() {
			@Override
            public void focusLost(FocusEvent e) {
	            if(searchComponent.getButtonSelection())
	            	searchComponent.setButtonSelection(false);
	            if(overflowMessage != null) {
	            	overflowMessage.dispose();
	            	overflowMessage = null;
	            }
            }});
    }

    public JComponent getSearchComponent() {
        return searchComponent;
    }

    private KeyListener searchFieldListener = new KeyListener() {
        private void conditionalForwad(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER
                || e.getKeyCode() == KeyEvent.VK_UP
                || e.getKeyCode() == KeyEvent.VK_DOWN) {
            	if(!searchComponent.getButtonSelection() && !currentResult.isEmpty())
            		searchComponent.setButtonSelection(true);
            	resultPane.dispatchEvent(e);
            	e.consume();
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        	if(e.getKeyCode() != KeyEvent.VK_ENTER)
        		conditionalForwad(e);
        	else {
        		if (searchComponent.getButtonSelection())
        			conditionalForwad(e);
        		if(!searchComponent.getButtonSelection() && !currentResult.isEmpty())
            		searchComponent.setButtonSelection(true);
        		e.consume();
        	}
        }

        @Override
        public void keyPressed(KeyEvent e) {
        	if(e.getKeyCode() != KeyEvent.VK_ENTER)
        		conditionalForwad(e);
            	
        	if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
        		if(searchComponent.getButtonSelection() || resultPane.getParent() != null) {
        			searchComponent.setButtonSelection(false);
        			e.consume();
        		}
        }
    };

    /**
     * Callback from the panel when the text has changed. Cancels running
     * searches, then initiates a new search with the new search string.
     */
    @Override
    public synchronized void searchTextUpdated(String newSearchText) {
        if(currentSearchText != null && newSearchText.equals(currentSearchText))
            return;

        if(!preparationThread.isAlive())
            preparationThread.start();
        if(!searchThread.isAlive())
            searchThread.start();

        currentSearchText = newSearchText;

        searchQueue.clear();
        currentResult.clear();

        ArrayList<EntityMetaDataVO> toSearch = null;

        // Filter system entities and ensure the user's read right
        if(newSearchText.length() > 0) {
        	toSearch = getSearchEntities();
        }
        else {
            searchComponent.setBackground(Color.WHITE);
            searchComponent.setMaxBackgroundProgress(0);
            searchComponent.setMaxProgress(0);
        }
        if(toSearch != null && !toSearch.isEmpty()) {
            // Enqueue the searches with a small start-delay, so that the fast-typing
            // types can further refine their search-terms before the actual job starts
            int idx = 0;
            searchQueue.offer(new SearchDef(100L, idx, toSearch.size()));
            for(EntityMetaDataVO md : toSearch) {
                Map<String, EntityFieldMetaDataVO> fields
                    = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(md.getEntity());
                searchQueue.offer(new SearchDef(newSearchText, md, fields, idx, toSearch.size()));
                idx++;
            }
            searchComponent.setCurrentProgress(0);
            searchComponent.setMaxProgress(toSearch.size());
        }
    }


    private ArrayList<EntityMetaDataVO> getSearchEntities() {
    	// Create a list of all readable entities first:
        Set<String> systemEntities
        = CollectionUtils.transformIntoSet(EnumSet.<NuclosEntity>allOf(NuclosEntity.class),
            new Transformer<NuclosEntity, String>() {
                @Override
                public String transform(NuclosEntity i) {
                    return i.getEntityName();
                }});

        List<EntityMetaDataVO> allEntities = new ArrayList<EntityMetaDataVO>();
        for(EntityMetaDataVO md : MetaDataClientProvider.getInstance().getAllEntities())
            if(!systemEntities.contains(md.getEntity())
                    && SecurityCache.getInstance().isReadAllowedForEntity(md.getEntity()))
            	allEntities.add(md);

        Collections.sort(allEntities, new Comparator<EntityMetaDataVO>() {
            @Override
            public int compare(EntityMetaDataVO o1, EntityMetaDataVO o2) {
                String l1 = SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(o1);
                String l2 = SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(o2);
                return l1.compareTo(l2);
            }});

        Map<String, EntityMetaDataVO> entityLookup
        = CollectionUtils.generateLookupMap(allEntities, new Transformer<EntityMetaDataVO, String>() {
			@Override
            public String transform(EntityMetaDataVO i) {
				return i.getEntity();
            }});

        // Read the entity names, that have been explicitly selected or
        // deselected. Note, that savedSelected is ordered according to
        // the user's prefs
		ArrayList<String> savedSelected = new ArrayList<String>();
		ArrayList<String> savedDeselected = new ArrayList<String>();
		try {
	        String[] savedSelectedArray = PreferencesUtils.getStringArray(
	        	ClientPreferences.getUserPreferences().node("livesearch"), "selected");
	        CollectionUtils.addAll(savedSelected, savedSelectedArray);
        	String[] savedDeselectedArray = PreferencesUtils.getStringArray(
        		ClientPreferences.getUserPreferences().node("livesearch"), "deselected");
        	CollectionUtils.addAll(savedDeselected, savedDeselectedArray);
        }
        catch(PreferencesException e) {
        	LOG.warn("getSearchEntities failed: " + e);
        }

        // Put it all together
        ArrayList<EntityMetaDataVO> selected = new ArrayList<EntityMetaDataVO>();
        for(String s : savedSelected)
        	if(entityLookup.containsKey(s)) {  // still existant?
        		selected.add(entityLookup.get(s));
        		entityLookup.remove(s);
        	}
        for(EntityMetaDataVO e : allEntities)  // Remaining: either deselected or new -> select
        	if(entityLookup.containsKey(e.getEntity()))
	        	if(!savedDeselected.contains(e.getEntity()))
	        		selected.add(e);

        return selected;
    }


    /**
     * Callback, when the user has activated a function on a search result
     */
    @Override
    public void functionAction(final Function function, final List<LiveSearchResultRow> rows) {
        switch(function) {
        case OPEN:
        case KB_OPEN:
        case OPEN_DETAILS:
            UIUtils.runCommand(Main.getInstance().getMainFrame(), new CommonRunnable() {
                @Override
                public void run() throws CommonBusinessException {
                	functionOpen(rows, function == Function.OPEN_DETAILS);
                	searchComponent.setButtonSelection(false);
                	SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
			                	searchComponent.setButtonSelection(false);
								searchComponent.requestFocus();
							}
							catch (Exception e) {
								LOG.error("functionAction failed: " + e, e);
							}
						}
					});
                }
            });
            break;
        }
    }


    private void functionOpen(List<LiveSearchResultRow> rows, boolean individual) throws CommonBusinessException {
    	final Main main = Main.getInstance();
    	final MainController mc = main.getMainController();
    	final MainFrame mf = main.getMainFrame();
    	
    	Map<String, ArrayList<LiveSearchResultRow>> byEntity
    	= new LazyInitMapWrapper<String, ArrayList<LiveSearchResultRow>>(
    		new LinkedHashMap<String, ArrayList<LiveSearchResultRow>>(),
    		Factories.cloneFactory(new ArrayList<LiveSearchResultRow>()));

    	for(LiveSearchResultRow row : rows)
    		byEntity.get(row.entityName).add(row);

    	for(String entity : byEntity.keySet()) {
    		if(MasterDataLayoutHelper.isLayoutMLAvailable(entity, false)) {
    			ArrayList<LiveSearchResultRow> openRows = byEntity.get(entity);
    			if(openRows.size() == 1 || individual) {
    				for(LiveSearchResultRow resRow : openRows)
    					mc.showDetails(resRow.entityName, resRow.theObject.getId());
    			}
    			else {
    				ArrayList<Long> ids = new ArrayList<Long>();
    				for(LiveSearchResultRow resRow : openRows)
    					ids.add(resRow.theObject.getId());
    				CollectableSearchCondition cond
    				= SearchConditionUtils.getCollectableSearchConditionForIds(ids);

    				NuclosCollectController<?> collectController
    				= NuclosCollectControllerFactory.getInstance()
    					.newCollectController(mf.getHomePane(), entity, null);
    				collectController.runViewResults(cond);
    			}
    		}
    		else {
    			if(byEntity.get(entity).size() == 1 || individual) {
	    			for(LiveSearchResultRow row : byEntity.get(entity)) {
	    	    		// Check alternatives:
	    	    		// - selection for more than one
	    	    		// - error, if none is found
	    	    		// - if only one has been found: open directly
	    	    		List<EntityFieldMetaDataVO> fieldMetas = getParentFieldsWithLayout(row);
	    	    		if(fieldMetas.size() > 1) {
	    	    			new ShowAsDialog(mf, row.theObject, fieldMetas).setVisible(true);
	    	    		}
	    	    		else if(fieldMetas.isEmpty()) {
	    	    			JOptionPane.showMessageDialog(mf, 
	    	    					SpringLocaleDelegate.getInstance().getResource("livesearch.controller.nolayout", "No layout available"));
	    	    		}
	    	    		else {
	    	    			EntityFieldMetaDataVO fm = fieldMetas.get(0);
	    	    			mc.showDetails(
	    	    				fm.getForeignEntity(),
	    	    				row.theObject.getFieldIds().get(fm.getField()));
	    	    		}
	    			}
    			}
    			else {
    				// For multi-selection, we only accept one definite parent
    				// layout.
    				HashSet<EntityFieldMetaDataVO> intersection = null;
    				for(LiveSearchResultRow row : byEntity.get(entity))
    					if(intersection == null)
    						intersection = new HashSet<EntityFieldMetaDataVO>(getParentFieldsWithLayout(row));
    					else
    						intersection.retainAll(getParentFieldsWithLayout(row));

    				if(intersection.size() == 1) {
    					EntityFieldMetaDataVO fm = intersection.iterator().next();

        				LinkedHashSet<Long> ids = new LinkedHashSet<Long>();
        				for(LiveSearchResultRow res : byEntity.get(entity))
        					ids.add(res.theObject.getFieldIds().get(fm.getField()));
        				CollectableSearchCondition cond
        				= SearchConditionUtils.getCollectableSearchConditionForIds(ids);

        				NuclosCollectController<?> collectController
        				= NuclosCollectControllerFactory.getInstance()
        					.newCollectController(mf.getHomePane(), fm.getForeignEntity(), null);
        				collectController.runViewResults(cond);
    				}
    				else {
    	    			JOptionPane.showMessageDialog(mf, 
    	    					SpringLocaleDelegate.getInstance().getResource("livesearch.controller.nolayout", "No layout available"));
    				}
    			}
    		}
    	}
    }


    /**
     * For an entity, which does not have its own layout, obtain a list of
     * parent entities, which contain the concrete object and have the layout
     * set.
     *
     * Maybe this should be remodeled to return the entity and id instead,
     * so that the function interface remains the same for a recursive parent
     * search?
     *
     * @param row    the result row
     * @return       a list of field-metas of the result row, which contain FK-
     *               references, which in turn lead to an object which contains
     *               the given row as part of a details view.
     * @throws CommonBusinessException
     */
    private List<EntityFieldMetaDataVO> getParentFieldsWithLayout(LiveSearchResultRow row) throws CommonBusinessException {
    	List<EntityFieldMetaDataVO> res = new ArrayList<EntityFieldMetaDataVO>();
    	Map<String, EntityFieldMetaDataVO> fields
    		= MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(row.entityMeta.getEntity());
		for(EntityFieldMetaDataVO fieldMeta : fields.values()) {
    		// Field is foreign key, the target entity has a layout, and the
    		// reference is not null?
    		if(fieldMeta.getForeignEntity() != null
    			&& MasterDataLayoutHelper.isLayoutMLAvailable(fieldMeta.getForeignEntity(), false)
    			&& row.theObject.getFieldIds().get(fieldMeta.getField()) != null
    			) {
    			try {
    				// Check, whether the layout contains the row's entity
	                Map<EntityAndFieldName, String> subStuff
	                = EntityFacadeDelegate.getInstance().getSubFormEntityAndParentSubFormEntityNames(
	                		fieldMeta.getForeignEntity(),
	                		MasterDataDelegate.getInstance().getLayoutId(fieldMeta.getForeignEntity(), false));
	                for(EntityAndFieldName ef : subStuff.keySet())
	                	if(ef.getEntityName().equals(row.entityName)) {
	                		res.add(fieldMeta);
	                		break;
	                	}
                }
                catch(RemoteException e) {
                	throw new CommonBusinessException(e);
                }
    		} else if(fieldMeta.getForeignEntity() != null
        			&& !MasterDataLayoutHelper.isLayoutMLAvailable(fieldMeta.getForeignEntity(), false)
        			&& row.theObject.getFieldIds().get(fieldMeta.getField()) != null
        			&& row.entityMeta.isDynamic()) {
    			try {
    				Integer iGenericObjectId = IdUtils.unsafeToId(row.theObject.getFieldIds().get(fieldMeta.getField()));
        			Integer iModuleId = GenericObjectDelegate.getInstance().get(iGenericObjectId).getModuleId();
        			String sForeignEntity = Modules.getInstance().getEntityNameByModuleId(iModuleId);
        			// Check, whether the layout contains the row's entity
	                Map<EntityAndFieldName, String> subStuff
	                = EntityFacadeDelegate.getInstance().getSubFormEntityAndParentSubFormEntityNames(
	                		sForeignEntity,
	                		MasterDataDelegate.getInstance().getLayoutId(sForeignEntity, false));
	                for(EntityAndFieldName ef : subStuff.keySet())
	                	if(ef.getEntityName().equals(row.entityName)) {
	                		EntityFieldMetaDataVO fldMeta = (EntityFieldMetaDataVO)fieldMeta.clone();
	                		fldMeta.setForeignEntity(sForeignEntity); //ugly
	                		res.add(fldMeta);
	                		break;
	                	}
                }
                catch(RemoteException e) {
                	throw new CommonBusinessException(e);
                }
    		}
    	}
    	return res;
    }


    /**
     * Called from the preparation thread: transfers the results to the view
     * @param searchDef the underlying search definition
     * @param newRows the prepared rows
     */
    private synchronized void addResults(final SearchDef searchDef, final ArrayList<LiveSearchResultRow> newRows) {
        // if the search term which produced the results is no more the current
        // search term, the results are discarded.
    	if(searchDef.search.equals(currentSearchText))
            SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                    	try {
	                    	if(searchDef.search.equals(currentSearchText)) {
		                        currentResult.addAll(newRows);
		                        resultPane.setResultData(currentResult);
		                        if(searchComponent.hasFocus()) {
			                        searchComponent.setButtonSelection(true);
			                        showResultPane();
		                        }
	                    	}
						}
						catch (Exception e) {
							LOG.error("addResults failed: " + e, e);
						}
                    }
                });
    }

    private synchronized void finishedSearch(final boolean overflowed) {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                	try {
	                    searchComponent.setMaxProgress(0);
	                    searchComponent.setMaxBackgroundProgress(0);
	                    if(currentResult.isEmpty())
	                        searchComponent.setBackground(new Color(0xff6565));
	
	                   if(overflowed) {
	                		overflowMessage = new Bubble(
	                			searchComponent,
	                			bubbleMessage,
	                			10,
	                			Bubble.Position.SE);
	                		overflowMessage.setVisible(true);
	                	}
					}
					catch (Exception e) {
						LOG.error("finishedSearch failed: " + e, e);
					}
                }
            });
    }

    private synchronized void startedSearch() {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public void run() {
                	try {
	                	if(overflowMessage != null) {
	                		overflowMessage.dispose();
	                		overflowMessage = null;
	                	}
	                    searchComponent.setBackground(Color.WHITE);
					}
					catch (Exception e) {
						LOG.error("startSearch failed: " + e, e);
					}
                }
            });
    }

    /**
     * The search definition objects come in two types: either a real search
     * definition, or a delay
     */
    private enum SearchDefType {
        SEARCH, DELAY, OVERFLOW;
    };

    /**
     * Struct containing a search definition in terms of entity and search term.
     */
    private class SearchDef {
        SearchDefType                      type;
        // SEARCH:
        String		                       search;
        EntityMetaDataVO		           entity;
        Map<String, EntityFieldMetaDataVO> fields;
        // DELAY
        long                               delay;

        // both
        int                                index;        // index of current search
        int                                groupSize;    // total size of current group

        private SearchDef(String search, EntityMetaDataVO entity, Map<String, EntityFieldMetaDataVO> fields, int index, int groupSize) {
            this.type = SearchDefType.SEARCH;
            this.search = search;
            this.entity = entity;
            this.fields = fields;
            this.index = index;
            this.groupSize = groupSize;
        }

        private SearchDef(long delay, int index, int groupSize) {
            this.type = SearchDefType.DELAY;
            this.delay = delay;
            this.index = index;
            this.groupSize = groupSize;
        }
    }

    /**
     * take() on a blocking queue, which simple gets repeated, if an underlying
     * interrupted exception is thrown.
     */
    private static <T> T pollInfinite(BlockingQueue<T> q) {
        T t = null;
        while(t == null) {
            try {
                t = q.take();
            }
            catch(InterruptedException e) {
    			LOG.info("pollInfinite: " + e);
            }
        }
        return t;
    }


    private void setProgress(final int curr, final int max, final boolean isSearch) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	try {
	                if(isSearch) {
	                    if(searchComponent.getMaxBackgroundProgress() != max)
	                        searchComponent.setMaxBackgroundProgress(max);
	                    if(searchComponent.getCurrentBackgroundProgress() != curr)
	                        searchComponent.setCurrentBackgroundProgress(curr);
	                }
	                else {
	                    if(searchComponent.getMaxProgress() != max)
	                        searchComponent.setMaxProgress(max);
	                    if(searchComponent.getCurrentProgress() != curr)
	                        searchComponent.setCurrentProgress(curr);
	                }
				}
				catch (Exception e) {
					LOG.error("setProgress failed: " + e, e);
				}
            }
        });
    }


    /**
     * The search-thread takes search definitions from the searchQueue, and
     * does the server-communication. The results get added to the preparationQueue.
     */
    private class SearchThread extends Thread {
        public SearchThread() {
            super("LiveSearchAction.SearchThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            while(true) {
                SearchDef nowSearching = pollInfinite(searchQueue);

				if (nowSearching.type == SearchDefType.DELAY) {
					setProgress(nowSearching.index, nowSearching.groupSize, true);
					try {
						Thread.sleep(nowSearching.delay);
					} catch (InterruptedException e) {
						LOG.info("SearchThread.run: " + e);
					}
					preparationQueue.offer(new Pair<SearchDef, List<Pair<EntityObjectVO, Set<String>>>>(
							nowSearching, null));
					continue;
				}

                LiveSearchFacadeRemote searchService
                    = ServiceLocator.getInstance().getFacade(LiveSearchFacadeRemote.class);

                try {
                	if(nowSearching.search.equals(currentSearchText)) {
                		// Overflow breaker: if the result display is already
                		// bigger than MAX, we fetch the further searches from
                		// the queue and send an OVERFLOW message to the prep-
                		// thread
                		if(currentResult.size() > MAX_RESULTS) {
                			SearchDef consume = searchQueue.peek();
                			while(consume != null && consume.search != null && consume.search.equals(nowSearching.search)) {
                				searchQueue.poll();
                				consume = searchQueue.peek();
                			}
                			setProgress(0, 0, true);
                			nowSearching.type = SearchDefType.OVERFLOW;
                			preparationQueue.clear();
    	                    preparationQueue.offer(new Pair<SearchDef, List<Pair<EntityObjectVO, Set<String>>>>(nowSearching, Collections.<Pair<EntityObjectVO, Set<String>>>emptyList()));
    	                    continue;
                		}

	                    List<Pair<EntityObjectVO, Set<String>>> res
	                        = searchService.search(nowSearching.entity.getEntity(), nowSearching.search);

	                    if(!nowSearching.search.equals(currentSearchText))
	                        continue;

	                    setProgress(nowSearching.index, nowSearching.groupSize, true);
	                    preparationQueue.offer(new Pair<SearchDef, List<Pair<EntityObjectVO, Set<String>>>>(nowSearching, res));
                	}
                }
                catch(Exception e) {
                	Errors.getInstance().showExceptionDialog(Main.getInstance().getMainFrame(), e);
                }
            }
        }
    }


    /**
     * The preparation thread takes search-results from the preparationQueue,
     * transforms these into search result structures, and passes these on to
     * the function addResults above (which in turn passes them to the view)
     */
    private class PreparationThread extends Thread {
        public PreparationThread() {
            super("LiveSearchAction.PreparationThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            OUTER:
            while(true) {
                Pair<SearchDef, List<Pair<EntityObjectVO, Set<String>>>> searchResult
                    = pollInfinite(preparationQueue);

                switch(searchResult.x.type) {
                case DELAY:
                    startedSearch();
                    break;
                case SEARCH: {
                    if(!searchResult.x.search.equals(currentSearchText))
                        continue OUTER;
                    ArrayList<LiveSearchResultRow> preparedResults
                    	= prepareResults(searchResult.x, searchResult.y);
                    if(!searchResult.x.search.equals(currentSearchText))
                        continue OUTER;
                    addResults(searchResult.x, preparedResults);
                    break;
                }
                case OVERFLOW: {
                	setProgress(0, 0, false);
                	finishedSearch(true);
                	continue OUTER;
                }
                }

                setProgress(searchResult.x.index, searchResult.x.groupSize, false);
                if(searchResult.x.index == searchResult.x.groupSize - 1)
                    finishedSearch(false);
            }
        }

        /**
         * Converts a list of entityObjectVO to a list of LiveSearchResultRows
         * @param searchDef the underlying search definition
         * @param res       the list of EntityObjectVO as returned by the server
         * @return a list of LiveSearchResultRow suitable for the view
         */
        private ArrayList<LiveSearchResultRow> prepareResults(SearchDef searchDef, List<Pair<EntityObjectVO, Set<String>>> res) {
            ArrayList<LiveSearchResultRow> newRows = new ArrayList<LiveSearchResultRow>();

            String lowerCaseSearchString = searchDef.search.toLowerCase();

            for(Pair<EntityObjectVO, Set<String>> p : res) {
            	EntityObjectVO entityObject = p.x;
            	Set<String> hideFields = CollectionUtils.emptySetIfNull(p.y);

                // Icon: either the standard folder icon, or whatever is configured.
                // In any case: scaled to 32 pix
                ImageIcon rowIcon = Icons.getInstance().getIconDesktopFolder();
                Integer iconId = searchDef.entity.getResourceId();
                String nuclosResource = searchDef.entity.getNuclosResource();
                if(iconId != null)
                    rowIcon = resourceCache.getIconResource(iconId);
                else if (nuclosResource != null)
    					rowIcon = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);

                String title = "";
                if (!searchDef.entity.isDynamic()) {
                	String treeRep = SpringLocaleDelegate.getInstance().getTreeViewFromMetaDataVO(searchDef.entity);
                    title = StringUtils.replaceParameters(treeRep, new ParameterTransformer(entityObject));
                } else {
                	EntityMetaDataVO eEntityMetaData = null;
                	Map<String, EntityFieldMetaDataVO> fields
            		= MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(searchDef.entity.getEntity());
                	for(EntityFieldMetaDataVO fieldMeta : fields.values()) {
                		if(fieldMeta.getForeignEntity() != null
                    			&& !MasterDataLayoutHelper.isLayoutMLAvailable(fieldMeta.getForeignEntity(), false)
                    			&& entityObject.getFieldIds().get(fieldMeta.getField()) != null) {
                			try {
                				Integer iGenericObjectId = IdUtils.unsafeToId(entityObject.getFieldIds().get(fieldMeta.getField()));
                				GenericObjectVO genericObjectVO = GenericObjectDelegate.getInstance().get(iGenericObjectId);
                				
                    			String sForeignEntity = Modules.getInstance().getEntityNameByModuleId(genericObjectVO.getModuleId());
                    			// Check, whether the layout contains the row's entity
            	                Map<EntityAndFieldName, String> subStuff
            	                = EntityFacadeDelegate.getInstance().getSubFormEntityAndParentSubFormEntityNames(
            	                		sForeignEntity,
            	                		MasterDataDelegate.getInstance().getLayoutId(sForeignEntity, false));
            	                for(EntityAndFieldName ef : subStuff.keySet())
            	                	if(ef.getEntityName().equals(searchDef.entity.getEntity())) {
            	                		eEntityMetaData = MetaDataClientProvider.getInstance().getEntity(sForeignEntity);
            	            			String treeRep = SpringLocaleDelegate.getInstance().getTreeViewFromMetaDataVO(eEntityMetaData);
            	                        title = StringUtils.replaceParameters(treeRep, new ParameterTransformer(wrapGenericObjectVO(genericObjectVO)));
            	                		break;
            	                	}
            	                if (eEntityMetaData != null)
            	                	break;
                            }
                            catch(Exception e) {
                            	// ignore.
                            }
                		}
                	}

                    if (eEntityMetaData == null)
                    	continue;
                }
                
                Map<String, String> matchMap = new HashMap<String, String>();
                // Line 2: list of attribute matches
                for(String fieldName : searchDef.fields.keySet()) {
                    EntityFieldMetaDataVO fdef = searchDef.fields.get(fieldName);
                    NuclosEOField eoField = NuclosEOField.getByField(fieldName);
                    if(eoField == null || eoField.isForceValueSearch()) {
                        if((fdef.getDataType().equals(String.class.getName()) || fdef.getDataType().equals(Date.class.getName())) && !hideFields.contains(fdef.getField())) {
                            String fieldValue;
                            Object fld = entityObject.getField(fieldName);
    						if (fld instanceof Date) {
    							DateFormat df = SpringLocaleDelegate.getInstance().getDateFormat();
    							fieldValue = fld == null ? "" : df.format((Date)fld);
    						} else if (fld instanceof String) {
    							fieldValue = StringUtils.emptyIfNull((String)fld);
    						} else {
    							fieldValue = fld == null ? "" : fld.toString();
    						}
                            int matchIndex = fieldValue.toLowerCase().indexOf(lowerCaseSearchString);
                            if(matchIndex >= 0) {
                                int endIndex = matchIndex + searchDef.search.length();
                                String hilighedValue
                                    = fieldValue.substring(0, matchIndex)
                                    + "<b>"
                                    + fieldValue.substring(matchIndex, endIndex)
                                    + "</b>"
                                    + fieldValue.substring(endIndex);

                                String fieldLabel
                                    = SpringLocaleDelegate.getInstance().getLabelFromMetaFieldDataVO(fdef);

                                matchMap.put(fieldLabel, hilighedValue);
                            }
                        }
                    }
                }
                LiveSearchResultRow row = new LiveSearchResultRow(entityObject, rowIcon, searchDef.entity, title, matchMap);
                newRows.add(row);
            }
            Collections.sort(newRows, new Comparator<LiveSearchResultRow>() {
                @Override
                public int compare(LiveSearchResultRow o1, LiveSearchResultRow o2) {
                    return StringUtils.compareIgnoreCase(o1.titleString, o2.titleString);
                }});
            return newRows;
        }

        private class ParameterTransformer implements Transformer<String, String> {
            private final EntityObjectVO	evo;

            public ParameterTransformer(EntityObjectVO evo) {
                this.evo = evo;
            }

            @Override
            public String transform(String rid) {
                // the first element is the key; all other are flags separated by
                // ':'
                String[] elems = rid.split(":");

                String resIfNull = "";
                for(int i = 1; i < elems.length; i++) {
                    if(elems[i].startsWith("ifnull="))
                        resIfNull = elems[i].substring(7);
                }

                Object value = evo.getFields().get(elems[0]);
                return value != null ? value.toString() : resIfNull;
            }
        }
    }

	/**
	 */
	private static EntityObjectVO wrapGenericObjectVO(GenericObjectVO go) {
		EntityObjectVO eo = new EntityObjectVO();

		eo.setEntity(Modules.getInstance().getEntityNameByModuleId(go.getModuleId()));

		eo.setId(IdUtils.toLongId(go.getId()));
		eo.setCreatedBy(go.getCreatedBy());
		eo.setCreatedAt(InternalTimestamp.toInternalTimestamp(go.getCreatedAt()));
		eo.setChangedBy(go.getChangedBy());
		eo.setChangedAt(InternalTimestamp.toInternalTimestamp(go.getChangedAt()));
		eo.setVersion(go.getVersion());

		eo.initFields(go.getAttributes().size(), go.getAttributes().size());
		for (DynamicAttributeVO attr : go.getAttributes()) {
			final String field = org.nuclos.client.attribute.AttributeCache.getInstance().getAttribute(attr.getAttributeId()).getName();
			if (attr.isRemoved()) {
				eo.getFields().remove(field);
				eo.getFieldIds().remove(field);
			} else {
				eo.getFields().put(field, attr.getValue());
				if (attr.getValueId() != null) {
					eo.getFieldIds().put(field, IdUtils.toLongId(attr.getValueId()));
				}
			}
		}
		eo.getFields().put(NuclosEOField.LOGGICALDELETED.getMetaData().getField(), go.isDeleted());

		return eo;
	}

    @Override
    public void buttonSelectionChanged(boolean shallShowResult) {
        if(shallShowResult && resultPane.getParent() == null && !currentResult.isEmpty())
            showResultPane();
        else
            hideResultPane();
    }


    // in AWT-Thread
    private void showResultPane() {
        JLayeredPane layeredPane = parentFrame.getLayeredPane();

        if(resultPane.getParent() == null)
            layeredPane.add(resultPane, JLayeredPane.PALETTE_LAYER);

        Dimension layeredPaneSize = layeredPane.getSize();
        Dimension d = new Dimension(500, layeredPaneSize.height / 2);

        int maxHeight = currentResult.size() * 48 + 2;
        if(d.height > maxHeight)
            d.height = maxHeight;

        Rectangle searchBounds = searchComponent.getBounds();

        Rectangle targetBounds = new Rectangle(
            searchBounds.x + searchBounds.width - d.width,
            searchBounds.y + searchBounds.height + 1,
            d.width,
            d.height
            );
        if(targetBounds.x < 0)
            targetBounds.x = 0;

        resultPane.setBounds(targetBounds);
        resultPane.validate();
        resultPane.repaint();
    }


    private void hideResultPane() {
        if(resultPane.getParent() != null)
            resultPane.getParent().remove(resultPane);
        parentFrame.repaint();
        if(searchComponent.getButtonSelection())
        	searchComponent.setButtonSelection(false);
    }
}
