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
package org.nuclos.client.wizard.util;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.Icons;
import org.nuclos.common2.CommonLocaleDelegate;

public class MoreOptionPanel extends JPanel {
	
	private static final Logger LOG = Logger.getLogger(MoreOptionPanel.class);

	private boolean blnFadeIn;
	
	private JPanel content;
	
	private JButton btnShow;
	
	private JSplitPane split;
	
	private static final int velocity = 30;
	private static final double velocity_step = 0.05;
	
	public MoreOptionPanel(JPanel pnlContent) {
		super();
		this.content = pnlContent;
		init();
	}
	
	protected void init() {
		double sizeMoreOptions [][] = {{TableLayout.PREFERRED,TableLayout.FILL}, {20,TableLayout.PREFERRED, TableLayout.FILL}};
		TableLayout layout = new TableLayout(sizeMoreOptions);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		
		btnShow = new JButton(CommonLocaleDelegate.getMessage("nuclos.resplan.wizard.step5.title.in","Erweiterte Einstellungen einblenden"), Icons.getInstance().getIconDown16());
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JPanel emptypanel = new JPanel();
		split.setTopComponent(emptypanel);
		split.setBottomComponent(content);
		
		this.add(btnShow, "0,0");
		this.add(split, "0,1,1,1");
		
		split.setDividerLocation(1000);
		split.setDividerSize(0);
	

		blnFadeIn = false;
		
		btnShow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(blnFadeIn) {
					blnFadeIn = false;
					btnShow.setIcon(Icons.getInstance().getIconDown16());
					btnShow.setText(CommonLocaleDelegate.getMessage("nuclos.resplan.wizard.step5.title.in","Erweiterte Einstellungen einblenden"));
					Runnable r = new Runnable() {
						
						@Override
						public void run() {
							for(double d = 0.0; d < 1.0; d += velocity_step) {
								split.setDividerLocation(d);
								try {
			                        Thread.sleep(velocity);
		                        }
		                        catch(InterruptedException e1) {
		                        	// stop loop
		                        	LOG.warn("run: " + e1, e1);
		                        	split.setDividerLocation(1.0);
		                        	break;
		                        }
							}							
						}
					};
					Thread t = new Thread(r);
					t.start();
				}
				else {
					blnFadeIn = true;
					btnShow.setIcon(Icons.getInstance().getIconUp16());
					btnShow.setText(CommonLocaleDelegate.getMessage("nuclos.resplan.wizard.step5.title.out","Erweiterte Einstellungen ausblenden"));
					Runnable r = new Runnable() {
						
						@Override
						public void run() {
							for(double d = 1.0; d > 0.0; d -= velocity_step) {
								split.setDividerLocation(d);
								try {
			                        Thread.sleep(velocity);
		                        }
		                        catch(InterruptedException e1) {
		                        	// stop loop
		                        	LOG.warn("run: " + e1, e1);
		                        	split.setDividerLocation(0.0);
		                        	break;
		                        }
							}							
						}
					};
					Thread t = new Thread(r);
					t.start();
				}
				
			}
		});
		
	}
	
}
