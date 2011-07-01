package org.nuclos.client.task;

import javax.swing.JRadioButtonMenuItem;

public interface ScheduledRefreshable {

	public void setRefreshInterval(int sec);
	
	public int getRefreshInterval();
	
	public int[] getRefreshIntervals();
	
	public String[] getRefreshIntervalLabels();
	
	public  JRadioButtonMenuItem[] getRefreshIntervalRadioButtons();
}
