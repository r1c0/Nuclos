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
package org.nuclos.tools.translation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.tools.translation.translationdata.TranslationData;
import org.nuclos.tools.translation.translationdata.TranslationData.TranslationAction;



class TranslationHelper {
	public static long lexerTime = 0;

	static JavaLexer lexer;

	private Map<String,RessourceData> ressIdMap = new HashMap<String,RessourceData>();

	private Map<String,FileData> fileDataMap = new HashMap<String,FileData>();

	private Set<String> uninterestingStrings = new HashSet<String>();

	private static int TOKENTYPE_IDENTIFIER = 4;
	private static int TOKENTYPE_STRINGLITERAL = 8;
	private static int TOKENTYPE_WHITESPACE = 22;
	private static int TOKENTYPE_COMMA = 41;
	private static int TOKENTYPE_LEFT_BRACKET = 66;
	private static int TOKENTYPE_SEMICOLON = 26;

	public static String TARGET_METHOD = "getTranslation";
	public static String REPLACING_METHOD = "getMessage";
	public static String GETMESSAGE_IMPORT = "import org.nuclos.common2.CommonLocaleDelegate;";
	public static String GETTRANSLATION_IMPORT = "import static org.nuclos.common2.CommonLocaleDelegate.getTranslation;";

	private CommonTokenStream tokens;
	private List<TranslationData> transData = new ArrayList<TranslationData>();
	private Map<TranslationFilter.FilterType,Set<String>> filters;

	private Map<String,Set<Long>> fileResourceIdMap = new HashMap<String,Set<Long>>();
	//private Integer uninterestingRessId = 1000000;

	public List<TranslationData> getTranslationData() {
	   return transData;
	}

	public Map<String,FileData> getFileData() {
	   return fileDataMap;
	}

	public List<TranslationData> analysePaths(String filePath, Map<TranslationFilter.FilterType,Set<String>> filters) throws Exception {
	   this.filters = filters;

	   fileResourceIdMap.clear();

	   ressIdMap = TranslationDAO.getRessourceData();
	   //uninterestingStrings = TranslationDAO.getUninterestingStrings();

	   transData.clear();
	   doFile(new File(filePath));

	   return transData;
	}

	// This method decides what action to take based on the type of
	//   file we are looking at
	public void doFile(File f) throws Exception {
		// If this is a directory, walk each file/dir in that directory
		if (f.isDirectory()) {
			String files[] = f.list();
			for(int i=0; i < files.length; i++)
				doFile(new File(f, files[i]));
		}

		// otherwise, if this is a java file, parse it!
		else if ( ((f.getName().length()>5) &&
				f.getName().substring(f.getName().length()-5).equals(".java"))
			|| f.getName().equals("input") )
		{
		   if (filters.get(TranslationFilter.FilterType.CLASS).contains(f.getName())) {
		      System.out.println("skipping "+f.getAbsolutePath()+" because of filter");
		   } else {
		      System.out.println("parsing "+f.getAbsolutePath());
		      parseFile(f.getAbsolutePath());
		   }
		}
	}

	public void parseFile(String f) throws Exception {

		try {
		   boolean hasDelegateImport = false;
		   int translationCalls = 0;
		   Integer firstImportStartIndex = -1;
		   Integer translationImportStartIndex = -1;
		   Integer translationImportEndIndex = -1;

		   List<TranslationData> subList = new ArrayList<TranslationData>();

			// Create a scanner that reads from the input stream passed to us
			if ( lexer==null ) {
				lexer = new JavaLexer();
			}
			lexer.setCharStream(new ANTLRFileStream(f));
			tokens = new CommonTokenStream();
			tokens.setTokenSource(lexer);
			tokens.LT(1); // force load

			// Create a parser that reads from the scanner
			JavaParser parser = null;
			parser = new JavaParser(tokens);

			// start parsing at the compilationUnit rule
			parser.compilationUnit();

			for (Object o : tokens.getTokens()) {

				CommonToken token = (CommonToken)o;
				String id = TranslationUtil.removeQuotes(token.getText());

				// package ermitteln um eventuell nachfolgend imports einzuf\u00fcgen
				if (token.getText().equals("package")) {
					CommonToken nextToken = null;
					int index = token.getTokenIndex();

					// nachfolgende Zeile der package-Deklaration ermitteln
					while (nextToken == null || !nextToken.getText().equals("\n"))
						nextToken = (CommonToken)tokens.get(index++);

					// erste nicht leere Zeile ermitteln um dort sp\u00e4ter den import f\u00fcr getMessage einzuf\u00fcgen
					while (nextToken.getText().equals("\n"))
						nextToken = (CommonToken)tokens.get(index++);

					if (firstImportStartIndex == -1)
						firstImportStartIndex = nextToken.getStartIndex();
					continue;
				}

				// imports ermitteln
				if (token.getText().equals("import")) {
					CommonToken nextToken = null;
					int index = token.getTokenIndex();

					while (nextToken == null || nextToken.getType() != TOKENTYPE_SEMICOLON)
						nextToken = (CommonToken)tokens.get(index++);

					if (firstImportStartIndex == -1)
						firstImportStartIndex = token.getStartIndex();

					String importString = tokens.toString(token.getTokenIndex(), nextToken.getTokenIndex());
					if (importString.equals(GETMESSAGE_IMPORT) && token.getCharPositionInLine() == 0)
						hasDelegateImport = true;
					if (importString.equals(GETTRANSLATION_IMPORT) && token.getCharPositionInLine() == 0 && translationImportStartIndex == -1) {
						translationImportStartIndex = token.getStartIndex();
						translationImportEndIndex = nextToken.getStopIndex();
					}
					continue;
				}

				if ( token.getChannel() != CommonToken.HIDDEN_CHANNEL && token.getType() == TOKENTYPE_STRINGLITERAL) {

					CommonToken prevToken = getPreviousTokenWithType(token, TOKENTYPE_LEFT_BRACKET);

					// Methodenaufruf mit String-Parameter gefunden
					if (prevToken != null && getPreviousTokenWithType(prevToken, TOKENTYPE_IDENTIFIER) != null) {
						CommonToken methodToken = getPreviousTokenWithType(prevToken, TOKENTYPE_IDENTIFIER);

						if (methodToken == null)
							continue;

						if ( methodToken.getText().equals(TARGET_METHOD)) {
							TranslationData td = new TranslationData();
							td.setFilePath(f);
							td.setLineNumber(token.getLine());
							td.setFilePos(methodToken.getStartIndex());
							td.setText(methodToken.getText()+"("+token.getText()+((CommonToken)tokens.getTokens().get(tokens.getTokens().indexOf(token)+1)).getText());
							td.setAction(TranslationAction.NEW_ID);
							//td.setResourceId(getNextResourceIdForFile(td.getFileName()));

							translationCalls++;

							subList.add(td);
						}
						else if ( methodToken.getText().equals(REPLACING_METHOD)) {
							// 'getMessage("..."' wurde gefunden -> nachfolgenden Text auslesen
							CommonToken nextToken = getNextTokenWithType(token, TOKENTYPE_COMMA);

							if (nextToken == null)
								continue;

							nextToken = getNextTokenWithType(nextToken, TOKENTYPE_STRINGLITERAL);

							if (nextToken == null || nextToken.getType() != TOKENTYPE_STRINGLITERAL)
								continue;

							String text = nextToken.getText();

							TranslationData td = new TranslationData();
							td.setFilePath(f);
							td.setLineNumber(token.getLine());
							td.setFilePos(token.getStartIndex());
							td.setText(text);
							td.setResourceId(id);

							if (!ressIdMap.keySet().contains(id)) {
								td.setAction(TranslationAction.SAVE_ID);
								td.setResourceId(id);
								ensureUniquenessInSublist(subList, td);
								ressIdMap.put(td.getResourceId(), new RessourceData(td.getResourceId(), text));
							}
							else {
								RessourceData rd = ressIdMap.get(id);

								if (!rd.getText().equals(text)) {
									td.setAction(TranslationAction.UPDATE_DB);
									td.setVisible(true);
									rd.setText(text);
								}
								else {
									td.setVisible(false);
								}
							}

							subList.add(td);
							addToFileResourceIdMap(td);
						}
						// unbekannte Methode, Text mit 'getMessage("Ress-ID","Text")' ersetzen
						else if (!filters.get(TranslationFilter.FilterType.METHOD).contains(methodToken.getText())){
							TranslationData td = buildTranslationData(f, token);
							if (td != null)
								subList.add(td);
						}
					}
					else {
						CommonToken t = (CommonToken)tokens.getTokens().get(token.getTokenIndex()-4);
						if (t.getType() != TOKENTYPE_IDENTIFIER || !t.getText().equals(REPLACING_METHOD) && !t.getText().equals(TARGET_METHOD)){
							TranslationData td = buildTranslationData(f, token);
							if (td != null)
								subList.add(td);
						}
					}
				}
			}

			FileData fd = new FileData();
			fd.setTranslationCalls(translationCalls);
			fd.setHasDelegateImport(hasDelegateImport);
			fd.setFirstImportStartIndex(firstImportStartIndex);
			fd.setTranslationImportStartIndex(translationImportStartIndex);
			fd.setTranslationImportEndIndex(translationImportEndIndex);

			fileDataMap.put(f, fd);

			transData.addAll(subList);
		}
		catch (Exception e) {
			System.err.println("parser exception: "+e);
			e.printStackTrace();
		}
	}

	/*
	 *  Etwas unsch\u00f6ne L\u00f6sung:
	 *  Werden im Quellcode bereits vergebene ResourceIds erkannt, wird \u00fcberpr\u00fcft ob diese ID
	 *  im momentan laufenden Parsing vergeben wurden und dort ggf. korrigiert
	 */
	private void ensureUniquenessInSublist(List<TranslationData> tdList, TranslationData newData) {
		for (TranslationData td : tdList) {
			if (td.getResourceId() != null && td.getResourceId().equals(newData.getResourceId()))
				td.setResourceId(getNextResourceIdForFile(td.getFileName()));
		}
	}

	private TranslationData buildTranslationData(String filePath, CommonToken token) {
	   // Stringliteral muss mindestens 2 aufeinanderfolgende Buchstaben haben
      if (uninterestingStrings.contains(TranslationUtil.removeQuotes(token.getText())) ||  !Pattern.matches(".+[A-Za-z][A-Za-z].+", token.getText()))
         return null;

      TranslationData td = new TranslationData();
      td.setText(token.getText());
      td.setFilePath(filePath);
      td.setLineNumber(token.getLine());
      td.setFilePos(token.getStartIndex());

//      if (token.getText().contains("'")) {
//         td.setPerform(false);
//         td.setAction(TranslationAction.MARK_UNINTERESTING);
//         td.setResourceId(TranslationDAO.UNINTERESTING_STRINGS_PREFIX + uninterestingRessId++);
//      }
//      else {
         td.setPerform(false);
         //td.setResourceId(getNextResourceIdForFile(td.getFileName()));
         td.setAction(TranslationAction.NEW_ID);
//      }

      return td;
	}

	private void addToFileResourceIdMap(TranslationData td) {
		if (td.getResourceId() == null)
			System.out.println(td.getFileName());
		Long id = Long.valueOf(td.getResourceId().substring(td.getResourceId().lastIndexOf(".")+1));
		String key = td.getFileName().substring(0, td.getFileName().indexOf(".java"));

		if (fileResourceIdMap.containsKey(key))
			fileResourceIdMap.get(key).add(id);
		else
			fileResourceIdMap.put(key,CollectionUtils.asSet(id));
	}

	private String getNextResourceIdForFile(String fileName) {

		String name = fileName.endsWith(".java") ? fileName.substring(0, fileName.indexOf(".java")) : fileName;

		Set<Long> ids = fileResourceIdMap.get(name);
		Long id = 1L;
		if (ids != null) {
			for( id = 1L;id<Long.MAX_VALUE;id++)
				if (!ids.contains(id))
					break;
		} else {
			ids = new HashSet<Long>();
		}

		ids.add(id);
		fileResourceIdMap.put(name, ids);

		return name+"."+id;
	}

	public void setResourceId(TranslationData td) {
		td.setResourceId(getNextResourceIdForFile(td.getFileName()));
		if (!ressIdMap.containsKey(td.getResourceId()))
			ressIdMap.put(td.getResourceId(), new RessourceData(td.getResourceId(), td.getText()));
		addToFileResourceIdMap(td);
	}

	public void deleteResourceId(TranslationData td) {
		if(td.getResourceId() != null && td.getResourceId().length() > 0) {
			String fileName = td.getFileName().endsWith(".java") ? td.getFileName().substring(0, td.getFileName().indexOf(".java")) : td.getFileName();
			Set<Long> ids = fileResourceIdMap.get(fileName);
			ids.remove(Long.valueOf(td.getResourceId().substring(td.getResourceId().lastIndexOf('.')+1,td.getResourceId().length())));
			fileResourceIdMap.put(fileName, ids);
			td.setResourceId("");
		}
	}

	private CommonToken getPreviousTokenWithType(CommonToken token, int type) {
	   return getAdjacendTokenWithType(token,type,true);
	}

	private CommonToken getNextTokenWithType(CommonToken token, int type) {
	   return getAdjacendTokenWithType(token,type,false);
   }

	private CommonToken getAdjacendTokenWithType(CommonToken token, int type, boolean leftSide) {
	   int step = leftSide ? -1 : 1;

      if (token == null)
         return null;

      int index = token.getTokenIndex()+step;
      CommonToken adjacendToken = (CommonToken)tokens.getTokens().get(index);
      while (adjacendToken.getType() == TOKENTYPE_WHITESPACE) {
         index = index+step;
         adjacendToken = (CommonToken)tokens.getTokens().get(index);
      }

      return adjacendToken.getChannel() != CommonToken.HIDDEN_CHANNEL && adjacendToken.getType() == type ? adjacendToken : null;
   }
}

