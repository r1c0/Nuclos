package org.nuclos.client.ui.labeled;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.ValidationLayerFactory.InputValidator;
import org.nuclos.client.ui.ValidationLayerFactory.NullableInputValidator;
import org.nuclos.client.ui.ValidationLayerFactory.RangeInputValidator;
import org.nuclos.client.ui.ValidationLayerFactory.RegExpInputValidator;
import org.nuclos.client.ui.ValidationLayerFactory.TypeInputValidator;

public class LabeledComponentSupport implements ILabeledComponentSupport {

	private List<InputValidator<JTextComponent>> inputValidators;

	private ToolTipTextProvider ttProvider;

	private ColorProvider cProvider;

	public LabeledComponentSupport() {
	}

	@Override
	public List<InputValidator<JTextComponent>> getInputValidators() {
		return inputValidators;
	}

	@Override
	public ToolTipTextProvider getToolTipTextProvider() {
		return ttProvider;
	}

	@Override
	public ColorProvider getColorProvider() {
		return cProvider;
	}
	
	@Override
	public void setColorProvider(ColorProvider cProvider) {
		this.cProvider = cProvider;
	}
	
	@Override
	public void setToolTipTextProvider(ToolTipTextProvider ttProvider) {
		this.ttProvider = ttProvider;
	}

	public void initValidators(JTextComponent layeredTextComponent, boolean isNullable,
			Class<?> javaClass, String inputFormat) {
		this.inputValidators = new ArrayList<InputValidator<JTextComponent>>();
		if (!isNullable) {
			inputValidators.add(new NullableInputValidator<JTextComponent>(layeredTextComponent));
		}
		if (inputFormat != null && inputFormat.trim().length() > 0) {
			if (javaClass.equals(String.class)) {
				inputValidators.add(new RegExpInputValidator<JTextComponent>(layeredTextComponent, inputFormat));
			}
			else {
				inputValidators.add(new RangeInputValidator<JTextComponent>(layeredTextComponent, javaClass,
						inputFormat));
			}
		}
		else {
			if (!javaClass.equals(String.class)) {
				inputValidators.add(new TypeInputValidator<JTextComponent>(layeredTextComponent, javaClass));
			}
		}
	}

	public String getValidationToolTip() {
		StringBuffer validationToolTip = new StringBuffer("");
		validationToolTip.append("<html><body>");
		int i = 0;
		if (inputValidators != null) {
			for (InputValidator<JTextComponent> validator : inputValidators) {
				if (i > 0)
					validationToolTip.append("<br/>");
				validationToolTip.append(validator.getValidationMessage());
			}
			i++;
		}
		validationToolTip.append("</body></html>");
		return validationToolTip.toString();
	}

}
