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
package org.nuclos.client.ui.labeled;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;

import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosImage;

/**
 * <code>CollectableComponent</code> that presents a value in a <code>JTextField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class LabeledImage extends LabeledMediaComponent {

	private JLabel lbImage;
	private NuclosImage ni;
	
	
	public LabeledImage(){
		this(true, String.class, null, false);
	}
	
	public LabeledImage(boolean isNullable, Class<?> javaClass, String inputFormat, boolean bSearchable) {
		super(isNullable, javaClass, inputFormat, bSearchable);
		
		lbImage = new JLabel() {

			@Override
			public String getToolTipText() {
				if(getIcon() == null) {
					return super.getToolTipText();
				}
				StringBuffer sb = new StringBuffer();
			
				return sb.toString();				
			}

			@Override
			public JToolTip createToolTip() {
				MyToolTip tooltip = new MyToolTip();
				tooltip.setComponent(this);
				if(ni != null && ni.getContent() != null) {
					ImageIcon ii = new ImageIcon(ni.getContent());
					int height = ii.getIconHeight();
					int width = ii.getIconWidth();
					Dimension dimScreen = Toolkit.getDefaultToolkit().getScreenSize();
					if(dimScreen.width <= width || dimScreen.height <= height) {
						Image imageScaled = ii.getImage().getScaledInstance(dimScreen.width, dimScreen.height, Image.SCALE_DEFAULT);
						ii = new ImageIcon(imageScaled);
					}
					
					if(dimScreen.width < width)
						width = dimScreen.width;
					if(dimScreen.height < height)
						height = dimScreen.height;					
					
					tooltip.setSize(width, height);
					tooltip.setPreferredSize(new Dimension(width, height));
					JLabel image = new JLabel(ii);
					tooltip.setLayout(new BorderLayout());
					tooltip.add(image, BorderLayout.CENTER);
					
					
				}
				return tooltip;
			}
			
			
			
		};
		
		
		if(this.validationLayer != null){
			this.addControl(this.validationLayer);
		} else {
			this.addControl(this.lbImage);
		}
		this.getJLabel().setLabelFor(this.lbImage);
		lbImage.setToolTipText("");
		lbImage.setHorizontalAlignment(SwingConstants.CENTER);
		
	}
	
	/**
	 * @deprecated Why is this needed at all?
	 */
	class MyToolTip extends JToolTip {

		@Override
		public void setTipText(String tipText) {
			super.setTipText(tipText);
		}
		
	}
	

	@Override
	protected JComponent getLayeredComponent(){
		return this.lbImage;
	}

	public void setNuclosImage(NuclosImage ni) {
		this.ni = ni;	
	}
	
	public NuclosImage getNuclosImage() {
		return ni;
	}
	
	public JLabel getImage() {
		return this.lbImage;
	}
	
	public JLabel getLabel(){
		return this.getJLabel();
	}

	/**
	 * sets the number of columns of the textfield
	 * @param iColumns
	 */
	@Override
	public void setColumns(int iColumns) {
		
	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.lbImage, sName, "lbImage");
	}

	@Override
	public JLabel getJMediaComponent() {
		return lbImage;
	}

	@Override
	protected JLabel getLayeredLabel() {
		return lbImage;
	}

	@Override
	protected GridBagConstraints getGridBagConstraintsForControl(boolean bFill) {
		final GridBagConstraints result = super.getGridBagConstraintsForControl(bFill);
		result.fill = GridBagConstraints.BOTH;
		result.weighty = 1D;
		result.insets = new Insets(0, 0, 0, 0);
		return result;
	}

}  
