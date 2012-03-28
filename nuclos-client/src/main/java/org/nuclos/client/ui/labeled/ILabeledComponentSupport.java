package org.nuclos.client.ui.labeled;

import java.util.List;

import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.ValidationLayerFactory.InputValidator;

public interface ILabeledComponentSupport {

	List<InputValidator<JTextComponent>> getInputValidators();
	
	ToolTipTextProvider getToolTipTextProvider();
	
	void setToolTipTextProvider(ToolTipTextProvider ttProvider);
	
	ColorProvider getColorProvider();
	
	void setColorProvider(ColorProvider cProvider);
	
}
