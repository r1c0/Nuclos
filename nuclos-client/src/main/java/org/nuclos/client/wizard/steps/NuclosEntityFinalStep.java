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
package org.nuclos.client.wizard.steps;

import info.clearthought.layout.TableLayout;

import javax.annotation.PostConstruct;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.nuclos.common2.CommonLocaleDelegate;
import org.springframework.beans.factory.annotation.Configurable;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/
@Configurable
public class NuclosEntityFinalStep extends NuclosEntityAbstractStep {

	private JLabel lbName;

	private JScrollPane pane;
	private JTextArea ta;	
	

	public NuclosEntityFinalStep() {	
		// initComponents();		
	}

	public NuclosEntityFinalStep(String name, String summary) {
		super(name, summary);
		// initComponents();
	}

	public NuclosEntityFinalStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		// initComponents();
	}
	
	@PostConstruct
	@Override
	protected void initComponents() {
		
		double size [][] = {{TableLayout.PREFERRED, 150, TableLayout.FILL}, {20, TableLayout.FILL}};
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		lbName = new JLabel();
		
		ta = new JTextArea();
		pane = new JScrollPane(ta);
		
		this.add(lbName, "0,0");
		this.add(pane, "0,1, 2,1");
	}

	@Override
	public void prepare() {
		super.prepare();
		if(this.model.isEditMode()) {
			this.lbName.setText(CommonLocaleDelegate.getInstance().getMessage(
					"wizard.step.final.1", "Entit\u00e4t {0} ge\u00e4ndert", this.model.getEntityName()) + " ...");
		}
		else {
			this.lbName.setText(CommonLocaleDelegate.getInstance().getMessage(
					"wizard.step.final.2", "Entit\u00e4t {0} erstellt", this.model.getEntityName()) + " ...");
		}
		ta.setText(getResultText());		
		ta.setCaretPosition(0);	
	}
	
	private String getResultText() {
		StringBuffer sb = new StringBuffer();
		if(this.model.getResultText() != null && this.model.getResultText().length() != 0){
			sb.append(this.model.getResultText());
		}
		
		return sb.toString();
	}

}
