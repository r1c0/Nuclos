package org.nuclos.client.ui.collect.component.verifier;

import java.util.Date;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.nuclos.client.ui.Errors;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.StringUtils;

public class DateInputVerifier extends InputVerifier {

	private final CollectableEntityField clctef;
	
	private CollectableFieldFormatException exception;
	private boolean bCheckState;
	
	public DateInputVerifier(CollectableEntityField clctef) {
		if (clctef == null) {
			throw new NullPointerException();
		}
		this.clctef = clctef;
	}

	@Override
	public boolean verify(JComponent comp) {
		if (!(comp instanceof JTextField)) {
			return true;
		}
		final JTextField tf = (JTextField) comp;
		try {
			CollectableFieldFormat.getInstance(Date.class).parse(null, tf.getText());
			return true;
		}
		catch (CollectableFieldFormatException ex) {
			// we can't handle the exception here, because we can't show the ExceptionDialog
			exception = ex;
			return false;
		}
	}

	@Override
	public boolean shouldYieldFocus(JComponent comp) {
		final boolean valid = verify(comp);
		if (!valid && !bCheckState) {
			bCheckState = true;
			final String sMessage = StringUtils.getParameterizedExceptionMessage("field.invalid.value", clctef.getLabel());
			//"Das Feld \"" + clctef.getLabel() + "\" hat keinen g\u00fcltigen Wert.";
			Errors.getInstance().showExceptionDialog(null, sMessage, exception);
			return false;
		}
		bCheckState = false;
		return valid;
	}
}
