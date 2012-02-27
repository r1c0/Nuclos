package org.nuclos.server.genericobject;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.genericobject.ejb3.GenerationResult;

public class GeneratorFailedException extends CommonValidationException {
	
	private final GenerationResult result;
	
	public GeneratorFailedException(String message, GenerationResult result, CommonBusinessException exception) {
		super(message, exception);
		this.result = result;
	}
	
	public GenerationResult getGenerationResult() {
		return result;
	}

}
