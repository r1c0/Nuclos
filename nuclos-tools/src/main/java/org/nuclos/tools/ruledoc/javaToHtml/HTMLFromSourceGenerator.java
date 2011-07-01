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
package org.nuclos.tools.ruledoc.javaToHtml;

import org.nuclos.tools.ruledoc.doclet.ConfigurationImpl;
import com.sun.javadoc.*;
import com.sun.tools.doclets.internal.toolkit.util.DirectoryManager;

/**
 * Schreibt eine HTML-Datei mit dem Source-Code einer Klasse und f\u00fchrenden Zeilennummern. Oben und unten werden
 * Header und Footer erzeugt.
 *
 */
public class HTMLFromSourceGenerator {
	protected ClassDoc classdoc = null;

	/**
	 * Convert the Classes in the given RootDoc to an HTML.
	 * @param configuration the configuration
	 * @param rd the RootDoc to convert
	 * @param outputdir the name of the directory to output to, with trailing path delimiter (i.e. "/", "\").
	 */
	public static void convertRoot(ConfigurationImpl configuration, RootDoc rd, String outputdir) {
		if (rd == null || outputdir == null) {
			return;
		}
		PackageDoc[] pds = rd.specifiedPackages();
		for (int i = 0; i < pds.length; i++) {
			convertPackage(configuration, pds[i], outputdir);
		}
		ClassDoc[] cds = rd.specifiedClasses();
		for (int i = 0; i < cds.length; i++) {
			ClassSourceWriter.generate(configuration, cds[i], outputdir + DirectoryManager.getDirectoryPath(cds[i].containingPackage()), cds[i].name() + ".html");
		}
	}

	/**
	 * Convert the Classes in the given Package to an HTML
	 * @param configuration the configuration
	 * @param pd the Package to convert
	 * @param outputdir the name of the directory to output to, with trailing File.separator.
	 */
	public static void convertPackage(ConfigurationImpl configuration, PackageDoc pd, String outputdir) {
		if (pd == null || outputdir == null) {
			return;
		}
		outputdir += DirectoryManager.getDirectoryPath(pd);
		ClassDoc[] cds = pd.allClasses();
		for (int i = 0; i < cds.length; i++) {
			ClassSourceWriter.generate(configuration, cds[i], outputdir, cds[i].name() + ".html");
		}
	}

	/**
	 * Given a <code>Doc</code>, return an anchor which refers to that Doc in the Source-Code-HTML Output.
	 * @param d the <code>Doc</code> which the returned anchor should refer.
	 * @return an anchor of the form &lt;a name="line.xxxx">&lt;/a> which refers to the given Doc d
	 */
	protected static String getAnchor(Doc d) {
		return "<a name=\"" +
				getAnchorName(d) +
				"\"></a>";
	}

	/**
	 * Given a <code>Doc</code>, return the anchor name which has been assigned to that Doc in the Source-Code-HTML Output.
	 * @param d the <code>Doc</code> to check.
	 * @return the name of the anchor
	 */
	public static String getAnchorName(Doc d) {
		return ClassSourceWriter.getAnchorName(d);
	}
}
