package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.nuclos.common2.SpringLocaleDelegate;

public class EventSupportNewInstanceDialog extends JDialog {

	private final EventSupportNewInstanceDialogModel model = new EventSupportNewInstanceDialogModel();
	
	final JTextField txtClassname = new JTextField();
	final JTextField txtDescription = new JTextField();
	
	public EventSupportNewInstanceDialog(Frame owner) {
		super(owner, SpringLocaleDelegate.getInstance().getMessage(
				"Neue Regel erstellen","Hintergrundprozesse"), true);
		
		getContentPane().add(createPanel());
		
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(owner);
	}
	
	
	public EventSupportNewInstanceDialogModel getModel() {
		return model;
	}


	@Override
	public Dimension getMinimumSize() {
		return new Dimension(600, 600);
	}

	private JPanel createPanel() {
		
		JPanel retVal = new JPanel();
		retVal.setLayout(new BorderLayout());
		retVal.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		
		final JPanel pnlHeadline = new JPanel();
		pnlHeadline.setLayout(new BorderLayout());
		
		final JPanel pnlInputFields = new JPanel();
		pnlInputFields.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(10,5,10,5);
		pnlHeadline.add(new JLabel("Bitte geben Sie den Klassennamen und die Beschreibung für die neue Regel an."), BorderLayout.NORTH);
		pnlHeadline.add(new JLabel("Die Werte werden dann direkt im Quellcode der Klasse hinterlegt."), BorderLayout.CENTER);
		pnlHeadline.add(new JLabel("(Die Angaben können im Editor noch verändert werden)"), BorderLayout.SOUTH);
		pnlInputFields.add(pnlHeadline, c);
		c.insets = new Insets(10,5,0,5);
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.PAGE_START;
	
	
		pnlInputFields.add(new JLabel("Bezeichnung:"), c);
		c.gridy = 2;
		pnlInputFields.add(new JLabel("Beschreibung:"), c);
		c.weightx = 1.0;
		c.gridx = 1;
		pnlInputFields.add(txtDescription, c);
		c.gridy = 1;
		pnlInputFields.add(txtClassname, c);
		
		c.gridy = 4;
		c.fill = GridBagConstraints.EAST;
		c.anchor = GridBagConstraints.LAST_LINE_END;
		final JPanel pnlButtons = new JPanel();
		
		JButton btnAbbrechen = new JButton("Abbrechen");
		btnAbbrechen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EventSupportNewInstanceDialog.this.dispose();
			}
		});
		JButton btnSave = new JButton("Speichern");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				EventSupportNewInstanceDialog.this.model.setDescription(txtDescription.getText());
				EventSupportNewInstanceDialog.this.model.setClassname(txtClassname.getText());
				
				EventSupportNewInstanceDialog.this.dispose();
			}
		});
		
		pnlButtons.add(btnAbbrechen, c);
		pnlButtons.add(btnSave, c);
		pnlInputFields.add(pnlButtons, c);
		
		retVal.add(pnlInputFields, BorderLayout.NORTH);
		
		return retVal;
	}

}
