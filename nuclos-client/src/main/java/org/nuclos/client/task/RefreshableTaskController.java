package org.nuclos.client.task;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.ui.Controller;
import org.nuclos.common.NuclosFatalException;

public abstract class RefreshableTaskController extends Controller<MainFrameTabbedPane> {

    private static final Logger log = Logger.getLogger(RefreshableTaskController.class);

    protected ScheduledExecutorService scheduler = null;
    protected Map<ScheduledRefreshable, ScheduledFuture<?>> refreshandles = null;

    public RefreshableTaskController() {
        super(null);
        refreshandles = new HashMap<ScheduledRefreshable, ScheduledFuture<?>>();
    }

    @Override
	public MainFrameTabbedPane getParent() {
		return MainFrame.getHomePane(); // TODO PersonalTask Home in future here !!!
	}

    public MainFrameTabbedPane getTabbedPane() {
		return getParent();
	}

	public abstract ScheduledRefreshable getSingleScheduledRefreshableView();

    public abstract void refreshScheduled(ScheduledRefreshable sRefreshable);

    public int getScheduledThreadPoolSize(){
    	return 1;
    }
    
    public void addRefreshIntervalActionsToSingleScheduledRefreshable() {
    		addRefreshIntervalActions(getSingleScheduledRefreshableView());
    }
    
    public void addRefreshIntervalActions(final ScheduledRefreshable sr) {
    		final int refreshInterval = sr.getRefreshInterval();
    		for (int i = 0; i < sr.getRefreshIntervals().length; i++) {
    			final int min = sr.getRefreshIntervals()[i];
    			sr.getRefreshIntervalRadioButtons()[i].setAction(new AbstractAction(sr.getRefreshIntervalLabels()[i]) {

					@Override
					public void actionPerformed(ActionEvent e) {
						setRefreshIntervalForMultiViewRefreshable(sr, min);
					}
				});
    			if ((i == 0 && refreshInterval <= 0) || 
    				(i >= 1 && refreshInterval == sr.getRefreshIntervals()[i])) {
    				sr.getRefreshIntervalRadioButtons()[i].setSelected(true);
    			}
    		}
    }
    
    public void setRefreshIntervalForSingleViewRefreshable(int min){
    	setRefreshIntervalForMultiViewRefreshable(getSingleScheduledRefreshableView(), min);
    }

    public void setRefreshIntervalForMultiViewRefreshable(ScheduledRefreshable sRefreshable, int min){
        if(sRefreshable == null) { throw new NuclosFatalException("ScheduledRefreshable is null"); }
        if(min >= 0){
	        sRefreshable.setRefreshInterval(min);
	        if(this.scheduler == null){
	            this.scheduler = Executors.newScheduledThreadPool(getScheduledThreadPoolSize());
	        }
        }
        scheduleRefreshRunner(sRefreshable);
    }
    
    private void scheduleRefreshRunner(ScheduledRefreshable sRefreshable){
        if(sRefreshable == null) { throw new NuclosFatalException("ScheduledRefreshable is null"); }
    	ScheduledFuture<?> old_Refreshandle = this.refreshandles.get(sRefreshable);
		if(old_Refreshandle != null){
    		old_Refreshandle.cancel(false);
    	}        
        int refreshInterval = sRefreshable.getRefreshInterval();
        if(refreshInterval > 0){
			this.refreshandles.put(sRefreshable, scheduler.scheduleWithFixedDelay(new RefreshViewRunnable(sRefreshable),
	            refreshInterval, refreshInterval, TimeUnit.MINUTES));
        } else {
        	this.refreshandles.remove(sRefreshable);
        	if(refreshandles.isEmpty() && this.scheduler != null){
        		this.scheduler.shutdown();
        		this.scheduler = null;
        	}
        }
    }

    private class RefreshViewRunnable implements Runnable {
    	
    	ScheduledRefreshable sRefreshable;
    	
    	public RefreshViewRunnable(ScheduledRefreshable isRefreshable){
    		sRefreshable = isRefreshable;
    	}
    	
        @Override
		public void run() {
            refreshScheduled(sRefreshable);
        }
    }	// inner class RefreshRunnable
    
}
