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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.nuclos.client.ui.Icons;

public class SearchComponent extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ProgressTextField   progressTextField;
	private JToggleButton       arrowButton;
//	private Icon                up;
//	private Icon                down;
	
	private List<LiveSearchSearchPaneListener>   listeners;
	
	public SearchComponent() {
		super(new BorderLayout(2, 2));

		listeners = new ArrayList<LiveSearchSearchPaneListener>();
		
		progressTextField = new ProgressTextField("", 15, 0, 100);
//		up = Icons.getInstance().getSimpleArrowUp();
//		down = Icons.getInstance().getSimpleArrowDown();
		
		Dimension buttonDim = new Dimension(16, progressTextField.getPreferredSize().height);
		
		arrowButton = new JToggleButton() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				
				final Rectangle bounds = getBounds();
				final BufferedImage bi = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g2bi = bi.createGraphics();		
				
				ImageIcon bg = Icons.getInstance().getArrowButtonX();
				ImageIcon ar = Icons.getInstance().getArrowButtonDown();
				
				int xBg = getWidth()-bg.getIconWidth();
				int yBg = 0;
				int xAr = xBg+(bg.getIconWidth()-ar.getIconWidth())/2;
				int yAr = yBg+(bg.getIconHeight()-ar.getIconHeight())/2;
				
				g2bi.drawImage(bg.getImage(), xBg, yBg, null);
				g2bi.drawImage(ar.getImage(), xAr, yAr, null);
				g2bi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
				g2bi.setColor(Color.WHITE);
				g2bi.fillRect(0, 0,bounds.width, bounds.height);
				g2bi.dispose();
				
				AffineTransform trans = isSelected()? AffineTransform.getRotateInstance(Math.toRadians(180),
	                bounds.width / 2,
	                bounds.height / 2) : null;
				
				g2.drawRenderedImage(bi, trans);
			}
		};
		arrowButton.setRolloverEnabled(true);
		arrowButton.addActionListener(buttonListener);
		arrowButton.setPreferredSize(buttonDim);
		arrowButton.setMinimumSize(buttonDim);
		arrowButton.setMaximumSize(buttonDim);
		arrowButton.setFocusable(false);
		
		add(progressTextField, BorderLayout.CENTER);
		add(arrowButton, BorderLayout.EAST);
		
		progressTextField.getDocument().addDocumentListener(docChangeListener);
		
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
		
		validate();
	}
	
	private ActionListener buttonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			setButtonSelection(arrowButton.isSelected());
		}
	};
	
	public synchronized void setButtonSelection(boolean newSelection) {
		arrowButton.setSelected(newSelection);
//		arrowButton.setIcon(arrowButton.isSelected() ? up : down);
		broadcastButtonSelectionChanged();
	}
	
	public boolean getButtonSelection() {
		return arrowButton.isSelected();
	}
	
	public int getCurrentProgress() {
	    return progressTextField.getCurrentProgress();
    }

	public void setCurrentProgress(int currentProgress) {
	    progressTextField.setCurrentProgress(currentProgress);
    }

	public int getMaxProgress() {
	    return progressTextField.getMaxProgress();
    }

	public void setMaxProgress(int maxProgress) {
	    progressTextField.setMaxProgress(maxProgress);
    }


	public int getCurrentBackgroundProgress() {
	    return progressTextField.getCurrentBackgroundProgress();
    }

	public void setCurrentBackgroundProgress(int currentBackgroundProgress) {
	    progressTextField.setCurrentBackgroundProgress(currentBackgroundProgress);
    }

	public int getMaxBackgroundProgress() {
	    return progressTextField.getMaxBackgroundProgress();
    }

	public void setMaxBackgroundProgress(int maxBackgroundProgress) {
	    progressTextField.setMaxBackgroundProgress(maxBackgroundProgress);
    }

	public void addLiveSearchSearchPaneListener(LiveSearchSearchPaneListener l) {
		listeners.add(l);
	}
	
	public void removeLiveSearchSearchPaneListener(LiveSearchSearchPaneListener l) {
		listeners.remove(l);
	}
	
	private void broadcastSearchTextChanged() {
		String newSearchText = progressTextField.getText();
		for(LiveSearchSearchPaneListener l : new ArrayList<LiveSearchSearchPaneListener>(listeners))
	        l.searchTextUpdated(newSearchText);
	}

	private void broadcastButtonSelectionChanged() {
		boolean newSelected = arrowButton.isSelected();
		for(LiveSearchSearchPaneListener l : new ArrayList<LiveSearchSearchPaneListener>(listeners))
	        l.buttonSelectionChanged(newSelected);
	}

	private DocumentListener docChangeListener = new DocumentListener() {
		@Override
		public void removeUpdate(DocumentEvent e) {
			broadcastSearchTextChanged();
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			broadcastSearchTextChanged();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			broadcastSearchTextChanged();
		}
	};

	@Override
    public void setBackground(Color bg) {
		super.setBackground(bg);
		if(progressTextField != null)
			progressTextField.setBackground(bg);
    }

	@Override
    public void addKeyListener(KeyListener l) {
	    progressTextField.addKeyListener(l);
    }

	@Override
    public void removeKeyListener(KeyListener l) {
	    progressTextField.removeKeyListener(l);
    }
	
	@Override
	public void requestFocus() {
		progressTextField.requestFocus();
	}
	
	@Override
	public boolean requestFocusInWindow() {
		return progressTextField.requestFocusInWindow();
	}

	@Override
    public void addFocusListener(FocusListener l) {
		progressTextField.addFocusListener(l);
	}

	@Override
    public void removeFocusListener(FocusListener l) {
		progressTextField.removeFocusListener(l);
	}
	
	@Override
	public boolean hasFocus() {
		return progressTextField.hasFocus();
	}
}
