package org.nuclos.client.ui.collect;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;

public class PivotController extends SelectFixedColumnsController {
	
	private class ShowPivotListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			final JCheckBox src = (JCheckBox) e.getSource();
			if (src.isSelected()) {
				
			}
			else {
				resultController.initializeFields(resultController.getEntity(), 
						(CollectController) resultController.getCollectController(), resultController.getCollectController().getPreferences());
			}		
		}
		
	}
	
	private final ResultController<? extends Collectable> resultController;
	
	public PivotController(Component parent, PivotPanel panel, List<? extends CollectableEntityField> base, List<? extends CollectableEntityField> subforms) {
		super(parent, panel);
		resultController = null;
	}
	
	public PivotController(Component parent, PivotPanel panel, ResultController<? extends Collectable> resultController) {
		super(parent, panel);
		this.resultController = resultController;
		panel.addActionListener(new ShowPivotListener());
	}
	
	private PivotPanel getPivotPanel() {
		return (PivotPanel) getPanel();
	}

}
