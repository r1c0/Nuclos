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
package org.nuclos.client.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractBufferedLayerUI;

import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.ValueValidationHelper;
import org.nuclos.common2.exception.CommonFatalException;


/**
 * A Factory for creation of layered text components. 
 * Took partly from Alexander Potochkin's example TextValidationDemo (https://swinghelper.dev.java.net/)
 * 
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version	01.00.00
 */
public class ValidationLayerFactory {
    
    // ********** InputValidator **********

    public static interface InputValidator<V extends JTextComponent> {
    	public boolean isValid();
    	public String getValidationMessage();
    }

    public static class TypeInputValidator<V extends JTextComponent> implements InputValidator<V> {
    	
    	protected JTextComponent component;
    	protected Class<?> javaClass;
    	
    	public TypeInputValidator(JTextComponent component, Class<?> javaClass) throws CommonFatalException {
    		this.component = component;
    		this.javaClass = javaClass;
    	}
    	
    	@Override
		public boolean isValid(){
    		if(this.component.getText() == null || this.component.getText().length() == 0){
    			return true;
    		}
    		return getValue() != null;
    	}

    	@Override
		public String getValidationMessage(){
    		return CommonLocaleDelegate.getInstance().getMessage(
    				"ValidationLayerFactory.1", "Das Feld muss einen Wert enthalten, der dem verlangten Format entspricht.");
    	}

    	protected Object getValue(){
    		final Object oValue;
    		try {
				oValue = CollectableFieldFormat.getInstance(javaClass).parse(null, StringUtils.nullIfEmpty(this.component.getText()));
			} catch (CollectableFieldFormatException e1) {
				return null;
			} catch (NumberFormatException ne) {
				return null;
			}
    		return oValue;
     	}
    }
    
    public static class RangeInputValidator<V extends JTextComponent> extends TypeInputValidator<V> {
    	
    	private String sInputFormat;
    	
    	public RangeInputValidator(JTextComponent component, Class<?> javaClass, String sInputFormat){
    		super(component, javaClass);
    		this.sInputFormat = sInputFormat;
    	}
    	
    	@Override
		public boolean isValid(){
    		if(this.component.getText() == null || this.component.getText().length() == 0){
    			return true;
    		}
    		final Object oValue = getValue(); 
    		return oValue != null && ValueValidationHelper.validateBoundaries(oValue, sInputFormat);
    	}

    	@Override
		public String getValidationMessage(){
    		return CommonLocaleDelegate.getInstance().getMessage("ValidationLayerFactory.2", 
    			"Das Feld muss einen Wert enthalten, der dem verlangten Format entspricht und innerhalb des erlaubten Wertebereichs liegt.");
    	}
    }

    public static class RegExpInputValidator<V extends JTextComponent> implements InputValidator<V> {
    	
    	private JTextComponent component;
    	private String sInputFormat;
    	
    	public RegExpInputValidator(JTextComponent component, String sInputFormat){
    		this.component = component;
    		this.sInputFormat = sInputFormat;
    	}
    	
    	@Override
		public boolean isValid(){
    		if(this.component.getText() == null || this.component.getText().length() == 0){
    			return true;
    		}
    		return ValueValidationHelper.validateInputFormat(this.component.getText(), sInputFormat);
    	}

    	@Override
		public String getValidationMessage(){
    		return CommonLocaleDelegate.getInstance().getMessage(
    				"ValidationLayerFactory.1", "Das Feld muss einen Wert enthalten, der dem verlangten Format entspricht.");
    	}
    }
 
    public static class NullableInputValidator<V extends JTextComponent> implements InputValidator<V> {
    	
    	private JTextComponent component;
    	
    	public NullableInputValidator(JTextComponent component){
    		this.component = component;
    	}
    	
    	@Override
		public boolean isValid(){
    		return this.component.getText() != null && component.getText().trim().length() > 0;
    	}

    	@Override
		public String getValidationMessage(){
    		return CommonLocaleDelegate.getInstance().getMessage(
    				"ValidationLayerFactory.3", "Das Feld darf nicht leer sein.");
    	}
    }
    
    // ********** PainterCode **********
	/**
	 * @todo use T_AD_PARAMETER to set the painter after merging of common and nucleus
	 */

    public static final String TRANSLUCENT_PAINTER_CODE = "translucent";
    public static final String ICON_PAINTER_CODE = "icon";
    
    private static String currentPainter = null;
    
    /* 
     * !!!! - please use it carefully, set currentPainter only ones - !!!!
     */ 
    public static synchronized void setCurrentPainter(String painterCode){
    	ValidationLayerFactory.currentPainter = painterCode;
    }
    
    // ********** Translucent **********
	/**
	 * @todo use T_AD_PARAMETER to set the painter after merging of common and nucleus
	 */
    public static JXLayer<JComponent> createValidationLayer(JComponent c, List<InputValidator<?>> validators) {
    	//final String sColor = ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_CLIENT_VALIDATION_LAYER_PAINTER_NAME);

    	if(currentPainter != null && currentPainter.equalsIgnoreCase(ValidationLayerFactory.ICON_PAINTER_CODE)){
    		return createIconLayer(c, validators);
    	} else {
    		if(currentPainter != null && currentPainter.equalsIgnoreCase(ValidationLayerFactory.TRANSLUCENT_PAINTER_CODE)){
    			return createTranslucentLayer(c, validators);
    		} else {
    			return null;
    		}
    	}
    }
    
    public static JXLayer<JComponent> createTranslucentLayer(JComponent c, List<InputValidator<?>> validators) {
        return new JXLayer<JComponent>(c, new TranslucentLayerUI<JComponent>(validators));
    }
        
    static class TranslucentLayerUI<V extends JComponent> extends AbstractBufferedLayerUI<V> {
    	
    	private List<InputValidator<?>> validators;
    	
    	public TranslucentLayerUI(List<InputValidator<?>> validators){
    		super();
    		this.validators = validators;
    	}
    	
    	private boolean isValid(){
    		for(InputValidator<?> validator : validators){
    			if(!validator.isValid()){
    				return false;
    			}
    		}
    		return true;
    	}
    	
        @Override
		public void paintLayer(Graphics2D g2, JXLayer<V> l) {
            // paints the layer as is
            super.paintLayer(g2, l);

            // to be in sync with the view if the layer has a border
            Insets layerInsets = l.getInsets();
            g2.translate(layerInsets.left, layerInsets.top);
            
            JComponent view = l.getView();
            // To prevent painting on view's border
            Insets insets = view.getInsets();
            g2.clip(new Rectangle(insets.left, insets.top,
                    view.getWidth() - insets.left - insets.right,
                    view.getHeight() - insets.top - insets.bottom));

            g2.setColor(this.isValid() ? Color.GREEN : Color.RED);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
            g2.fillRect(0, 0, l.getWidth(), l.getHeight());
        }
    }
    

    // ********** Icon **********

    public static JXLayer<JComponent> createIconLayer(JComponent c, List<InputValidator<?>> validators) {

        final JXLayer<JComponent> layer = new JXLayer<JComponent>(c, new IconLayerUI<JComponent>(validators));

        // set necessary insets for the layer
		//layer.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 3));
		layer.setBorder(BorderFactory.createEmptyBorder(2, 3, 0, 0));

        if(c instanceof JTextComponent){
	        // layer's border area should be repainted when textComponent is updated
	        ((JTextComponent)layer.getView()).getDocument().addDocumentListener(new DocumentListener() {
	            @Override
				public void insertUpdate(DocumentEvent e) {
	                layer.repaint();
	            }
	
	            @Override
				public void removeUpdate(DocumentEvent e) {
	                layer.repaint();
	            }
	
	            @Override
				public void changedUpdate(DocumentEvent e) {
	                layer.repaint();
	            }
	        });
        } else {
	        layer.getView().addFocusListener(new FocusListener() {
	            @Override
				public void focusLost(FocusEvent e) {
	                layer.repaint();
	            }

	            @Override
				public void focusGained(FocusEvent e) {
	               
	            }
	        });
        }
        return layer;
    }

    static class IconLayerUI<V extends JComponent> extends AbstractBufferedLayerUI<V> {

    	private List<InputValidator<?>> validators;

    	public IconLayerUI(List<InputValidator<?>> validators){
    		this.validators = validators;
    	}
    	
    	private boolean isValid(){
    		for(InputValidator<?> validator : validators){
    			if(!validator.isValid()){
    				return false;
    			}
    		}
    		return true;
    	}
    	    	
    	// The red icon to be shown at the layer's corner
        private final static BufferedImage INVALID_ICON;
        static {
            int width = 7;
            int height = 8;
            INVALID_ICON = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D) INVALID_ICON.getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(Color.RED);
            g2.fillRect(0, 0, width, height);
            g2.setColor(Color.WHITE);
            g2.drawLine(0, 0, width, height);
            g2.drawLine(0, height, width, 0);
            g2.dispose();

        }

        @Override
		public void paintLayer(Graphics2D g2, JXLayer<V> l) {
            super.paintLayer(g2, l);

            // There is no need to take insets into account for this painter
            if (!this.isValid()) {
               //g2.drawImage(INVALID_ICON, l.getWidth() - INVALID_ICON.getWidth() - 1, 0, null);
               //g2.drawImage(INVALID_ICON, l.getWidth() - INVALID_ICON.getWidth() - 2, 3, null);
               g2.drawImage(INVALID_ICON, 3, 2, null);
            }
       }
    }
}
