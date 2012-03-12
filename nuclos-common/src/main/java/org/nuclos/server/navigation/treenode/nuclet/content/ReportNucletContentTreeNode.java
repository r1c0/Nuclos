package org.nuclos.server.navigation.treenode.nuclet.content;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.SpringLocaleDelegate;

public class ReportNucletContentTreeNode extends NucletContentTreeNode {

	public ReportNucletContentTreeNode(Long nucletId) {
		super(nucletId, NuclosEntity.REPORT);
	}

	@Override
	public String getLabel() {
		return SpringLocaleDelegate.getInstance().getMessage("report.and.form", "Report & Formulare");
	}

	
}
