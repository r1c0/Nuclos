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
package org.nuclos.tools.ruledoc.doclet;

import com.sun.javadoc.*;
import com.sun.tools.doclets.internal.toolkit.util.DocletAbortException;
import com.sun.tools.doclets.internal.toolkit.util.IndexBuilder;

import java.io.IOException;
import java.util.*;

/**
 * Generate the file with list of all the classes in this run. This page will be
 * used in the left-hand bottom frame, when "All Classes" link is clicked in
 * the left-hand top frame. The name of the generated file is
 * "allclasses-frame.html".
 *
 *
 */
public class AllClassesFrameWriter extends CommonDocletWriter {

	/**
	 * The name of the output file with frames
	 */
	public static final String OUTPUT_FILE_NAME_FRAMES = "allclasses-frame.html";

	/**
	 * The name of the output file without frames
	 */
	public static final String OUTPUT_FILE_NAME_NOFRAMES = "allclasses-noframe.html";

	/**
	 * Index of all the classes.
	 */
	protected IndexBuilder indexbuilder;

	/**
	 * Construct AllClassesFrameWriter object. Also initilises the indexbuilder
	 * variable in this class.
	 * @throws IOException
	 * @throws DocletAbortException
	 */
	public AllClassesFrameWriter(ConfigurationImpl configuration,
			String filename, IndexBuilder indexbuilder)
			throws IOException {
		super(configuration, filename);
		this.indexbuilder = indexbuilder;
	}

	/**
	 * Create AllClassesFrameWriter object. Then use it to generate the
	 * "allclasses-frame.html" file. Generate the file in the current or the
	 * destination directory.
	 *
	 * @param indexbuilder IndexBuilder object for all classes index.
	 * @throws DocletAbortException
	 */
	public static void generate(ConfigurationImpl configuration,
			IndexBuilder indexbuilder) {
		AllClassesFrameWriter allclassgen;
		String filename = OUTPUT_FILE_NAME_FRAMES;
		try {
			allclassgen = new AllClassesFrameWriter(configuration,
					filename, indexbuilder);
			allclassgen.generateAllClassesFile(true);
			allclassgen.close();
			filename = OUTPUT_FILE_NAME_NOFRAMES;
			allclassgen = new AllClassesFrameWriter(configuration,
					filename, indexbuilder);
			allclassgen.generateAllClassesFile(false);
			allclassgen.close();
		}
		catch (IOException exc) {
			configuration.standardCommonMessage.
					error("doclet.exception_encountered",
							exc.toString(), filename);
			throw new DocletAbortException();
		}
	}

	/**
	 * Print all the classes in table format in the file.
	 * @param wantFrames True if we want frames.
	 */
	protected void generateAllClassesFile(boolean wantFrames) throws IOException {
		String label = "Alle Regeln";

		printHtmlHeader(label, null, false);

		printAllClassesTableHeader();
		printAllClasses(wantFrames);
		printAllClassesTableFooter();

		printBodyHtmlEnd();
	}

	/**
	 * Use the sorted index of all the classes and print all the classes.
	 *
	 * @param wantFrames True if we want frames.
	 */
	@SuppressWarnings("unchecked")
	protected void printAllClasses(boolean wantFrames) {
		for (int i = 0; i < indexbuilder.elements().length; i++) {
			Character unicode = (Character) ((indexbuilder.elements())[i]);
			
			generateContents(indexbuilder.getMemberList(unicode), wantFrames);
		}
	}

	/**
	 * Given a list of classes, generate links for each class or interface.
	 * If the class kind is interface, print it in the italics font. Also all
	 * links should target the right-hand frame. If clicked on any class name
	 * in this page, appropriate class page should get opened in the right-hand
	 * frame.
	 *
	 * @param classlist Sorted list of classes.
	 * @param wantFrames True if we want frames.
	 */
	protected void generateContents(List<ClassDoc> classlist, boolean wantFrames) {
		sortClasslist(classlist);
		for (int i = 0; i < classlist.size(); i++) {
			ClassDoc cd = classlist.get(i);

			String label = "";

			if (wantFrames) {
				print(getClassLink(cd, "", label, false, "", "classFrame"));
			}
			else {
				print(getClassLink(cd, "", label, false, "", ""));
			}

			br();
		}
	}

	protected static <T extends ClassDoc> void sortClasslist(List<T> classlist) {
		Collections.sort(classlist, new Comparator<T>() {
			@Override
			public int compare(T cd1, T cd2) {
				String sRuleName1 = getRuleName(cd1);
				String sRuleName2 = getRuleName(cd2);
				return sRuleName1 != null ? sRuleName1
						.compareTo((sRuleName2 != null ? sRuleName2 : "")) : 0;
			}
		});
	}

	public static String getRuleName(ClassDoc cd) {
		String sRuleName = null;
		if (cd != null) {
			MethodDoc[] methods = cd.methods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].name().compareTo(METHOD_NAME) == 0) {
					MethodDoc rul = methods[i];
					Tag[] x = rul.tags();
					for (int j = 0; j < x.length; j++) {
						if (x[j].name().compareTo("@name") == 0) {
							sRuleName = x[j].text();
							break;
						}
					}
				}
			}
		}
		return sRuleName;
	}

	@Override
	public String getClassLink(ClassDoc cd, String where, String label,
			boolean bold, String color, String target) {
		boolean nameUnspecified = label.length() == 0;
		if (nameUnspecified) {

			// _________________________myDoclet_______________________________________

			MethodDoc[] methods = cd.methods();

			for (int i = 0; i < methods.length; i++) {

				if (methods[i].name().compareTo(METHOD_NAME) == 0) {

					MethodDoc rul = methods[i];

					Tag[] x = rul.tags();

					for (int j = 0; j < x.length; j++) {

						if (x[j].name().compareTo("@name") == 0) {

							label = x[j].text();

							break;

						}

					}// end of inner for

				}// end of if
			}

			// __________________________________________________________________________
			displayLength += label.length();

			// Create a tool tip if we are linking to a class or interface.
			// Don't
			// create one if we are linking to a member.

			String title = "Name der Regel";
			if (cd.isIncluded()) {
				if (isGeneratedDoc(cd)) {
					String filename = pathToClass(cd);
					return getHyperLink(filename, where, label, bold, color,
							title, target);
				}
			}
			else {
				String crosslink = getCrossClassLink(cd.qualifiedName(), where,
						label, bold, color, true);
				if (crosslink != null) {
					return crosslink;
				}
			}
			if (nameUnspecified) {
				displayLength -= label.length();
				label = configuration.getClassName(cd);
				displayLength += label.length();
			}
		}
		return label;
	}

	@Override
	public boolean isGeneratedDoc(Doc doc) {
		return configuration.isGeneratedDoc(doc);
	}

	@Override
	public String getClassLink(ClassDoc cd, String where, String label,
			boolean bold) {

		return getClassLink(cd, where, label, bold, "", "");
	}

	@Override
	public String getClassLink(ClassDoc cd) {

		return getClassLink(cd, false);
	}

	@Override
	public String getClassLink(ClassDoc cd, boolean bold) {

		return getClassLink(cd, "", "", bold);
	}

	@Override
	public String getClassLink(ClassDoc cd, String label) {
		return getClassLink(cd, "", label, false);
	}

	@Override
	public String getClassLink(ClassDoc cd, String where, String label) {
		return getClassLink(cd, where, label, false);
	}

	@Override
	public String getText(String key) {
		return msg(false).getText(key);
	}

	@Override
	public String getText(String key, String a1) {
		return msg(false).getText(key, a1);
	}

	@Override
	public String getText(String key, String a1, String a2) {
		return msg(false).getText(key, a1, a2);
	}

	@Override
	public String getText(String key, String a1, String a2, String a3) {
		return msg(false).getText(key, a1, a2, a3);
	}

	@Override
	public com.sun.tools.doclets.internal.toolkit.util.MessageRetriever msg(
			boolean checkVersion) {

		;
		return null;
	}

	/**
	 * Print the heading "All Classes" and also print Html table tag.
	 */
	protected void printAllClassesTableHeader() {
		fontSizeStyle("+1", "FrameHeadingFont");
		bold("Alle Regeln");
		fontEnd();
		br();
		table();
		tr();
		tdNowrap();
		fontStyle("FrameItemFont");
	}

	/**
	 * Print Html closing table tag.
	 */
	protected void printAllClassesTableFooter() {
		fontEnd();
		tdEnd();
		trEnd();
		tableEnd();
	}
}
