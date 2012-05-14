package org.nuclos.client.ui.collect.component.verifier;

import javax.swing.InputVerifier;
import javax.swing.JComponent;

public class TrueInputVerifier extends InputVerifier {
	
	public TrueInputVerifier() {
	}

	@Override
	public boolean verify(JComponent input) {
		return true;
	}

}
