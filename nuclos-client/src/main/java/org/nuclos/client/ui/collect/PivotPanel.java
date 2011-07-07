package org.nuclos.client.ui.collect;

import javax.swing.JCheckBox;
import javax.swing.JPanel;


public class PivotPanel extends SelectFixedColumnsPanel {
	
	private static class Header extends JPanel {
		
		private JCheckBox checkbox;
		
		private Header() {
			checkbox = new JCheckBox("Test");
			add(checkbox);
			setVisible(true);
			checkbox.setVisible(true);
			checkbox.setEnabled(true);
		}
	}
	
	public PivotPanel() {
		super(new Header());		
	}
		
}
