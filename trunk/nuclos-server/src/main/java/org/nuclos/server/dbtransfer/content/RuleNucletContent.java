package org.nuclos.server.dbtransfer.content;

import java.util.List;

import org.nuclos.common.NuclosEntity;

public class RuleNucletContent extends DefaultNucletContent {

	public RuleNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.RULE, null, contentTypes);
	}

	@Override
	public String getIdentifierField() {
		return "rule";
	}
	
}
