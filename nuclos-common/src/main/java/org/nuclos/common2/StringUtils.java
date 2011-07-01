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
package org.nuclos.common2;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.xml.security.utils.Base64;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Utility methods for Strings.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class StringUtils {

	/**
	 * the Euro symbol in Unicode.
	 */
	public static final char EURO_SYMBOL = '\u20AC';

	private static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");

	private StringUtils() {
	}

	/**
	 * @param s
	 * @return Is s <code>null</code> or empty?
	 * @postcondition result <--> (s == null) || (s.length() == 0)
	 */
	public static boolean isNullOrEmpty(String s) {
		return (s == null || s.length() == 0);
	}

	/**
	 * @param s
	 * @return Does the given String look empty? Is it <code>null</code> or does it contain only whitespace?
	 * @postcondition (s == null) || (s.length() == 0) --> result
	 */
	public static boolean looksEmpty(String s) {
		final boolean result = (s == null) || (org.apache.commons.lang.StringUtils.deleteWhitespace(s).length() == 0);

		assert !((s == null) || (s.length() == 0)) || result;

		return result;
	}

	/**
	 * @param s
	 * @return <code>null</code>, if <code>s</code> is empty. <code>s</code> otherwise
	 * @postcondition !"".equals(result)
	 * @postcondition ("".equals(s)) --> (result == null)
	 * @postcondition (!"".equals(s)) --> (result == s)
	 */
	public static String nullIfEmpty(String s) {
		final String result = (s != null && s.length() > 0) ? s : null;

		assert !"".equals(result);
		assert !("".equals(s)) || (result == null);
		assert !(!"".equals(s)) || (result == s);

		return result;
	}

	/**
	 * @return <code>""</code>, if <code>s == null</code>. <code>s</code> otherwise
	 * @postcondition result != null
	 * @postcondition (s == null) --> result.equals("")
	 * @postcondition (s != null) --> result.equals(s)
	 */
	public static String emptyIfNull(String s) {
		final String result = (s == null) ? "" : s;

		assert result != null;
		assert !(s == null) || result.equals("");
		assert !(s != null) || result.equals(s);

		return result;
	}

	/**
	 * @return a substring of s (or null, if the input was null),
	 *  ending at the first appearance of one of the given delimiters (or at the first whitespace if no delimiters were given)
	 * @param s the input string
	 */
	public static String getFirstSubString(String s, Set<String> delimiters) {
		if (s == null)
			return null;

		String sub = s.trim();
		int endIndex = -1;

		if (delimiters != null) {
			Iterator<String> iter = delimiters.iterator();

			while (iter.hasNext()) {
				String delimiter = iter.next();
				if (delimiter != null && (endIndex < 0 || sub.indexOf(delimiter) < endIndex) )
					endIndex = sub.indexOf(delimiter);
			}
		}

		if (endIndex < 0)
			endIndex = sub.indexOf(" ");

		return endIndex < 0 ? s : sub.substring(0,endIndex);
	}

	/**
	 * @return the parameterized exception message
	 * @param resId the resource id (intid) of the exception message
	 */
	public static String getParameterizedExceptionMessage(String resId, Object ... params) {
		if (resId == null)
			return null;

		StringBuilder sb = new StringBuilder(resId);

		if (params != null) {
			for (Object param : params)
				sb.append("{").append(param == null ? "" : param.toString()).append("}");
		}

		return sb.toString();
	}

	public static String getParameterizedExceptionMessageWithIds(String resId, Integer... params) {
		return getParameterizedExceptionMessage(resId, (Object[]) params);
	}

	/**
	 * @param s
	 * @return a trimmed version of the given String - leading and trailing whitespace are omitted.
	 * @postcondition (s == null) --> result == null
	 * @postcondition (s != null) --> result.equals(s.trim())
	 * @see String#trim()
	 */
	public static String trim(String s) {
		return (s == null) ? null : s.trim();
	}

	public static String getModuleFieldName(String s) {
		if(s.indexOf(".") < 0) {
			return s;
		}

		StringTokenizer st = new StringTokenizer(s, ".");
		st.nextToken();
		return st.nextToken();

	}

	/**
	 * @param s
	 * @return a copy of the given string without any white space character.
	 * @postcondition (s == null) --> result == null
	 */
	public static String deleteWhitespace(String s) {
		final String result;
		if (s == null) {
			result = null;
		}
		else {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length(); i++) {
				final char c = s.charAt(i);
				if (!Character.isWhitespace(c)) {
					sb.append(c);
				}
			}
			result = sb.toString();
		}
		assert (s != null) || result == null;
		return result;
	}

	/**
	 * splits the given text into separate lines by inserting newline characters between words.
	 * Doesn't separate words. Each line has approximately iColumns.
	 * @param sText
	 * @param iColumns
	 * @return a String containing the given text, split into several lines.
	 */
	public static String splitIntoSeparateLines(String sText, int iColumns) {
		//NUCLEUSINT-977
		final StringBuilder sb = new StringBuilder();
		if(sText != null) {
			final String sOptionalWhitespaceFollowedByNonWhiteSpace = "\\s*\\S*";
			final Pattern pattern = Pattern.compile(sOptionalWhitespaceFollowedByNonWhiteSpace);
			final Matcher matcher = pattern.matcher(sText);
			StringBuilder sbLine = new StringBuilder();
			while(matcher.find()) {
				final String sWord = sText.substring(matcher.start(), matcher.end());
				if((sbLine.length() == 0)
					|| (sbLine.length() + sWord.length() <= iColumns)) {
					sbLine.append(sWord);
				}
				else {
					if(sb.length() > 0) {
						sb.append("\n");
					}
					sb.append(sbLine);
					sbLine = new StringBuilder(sWord);
				}
			} // while
			if(sbLine.length() > 0) {
				if(sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(sbLine);
			}
		}
		return sb.toString();
	}

	/**
	 * clears the given array by writing null bytes to it. Useful for clearing sensitive data like passwords from memory.
	 * @param acPassword
	 */
	public static void clear(char[] acPassword) {
		if (acPassword != null) {
			for (int i = 0; i < acPassword.length; i++) {
				acPassword[i] = 0;
			}
		}
	}

	/**
	 * @param s
	 * @return s with the first character of s (if any) in upper case.
	 * @postcondition isNullOrEmpty(s) --> LangUtils.equals(result, s)
	 */
	public static String capitalized(String s) {
		return isNullOrEmpty(s) ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String encrypt(String x)  {
	  java.security.MessageDigest digest = null;
		try {
			digest = java.security.MessageDigest.getInstance("SHA-1");
			digest.reset();
			digest.update(x.getBytes());
			return new String(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
  }

	public static String encryptBase64(String x)  {
		try {
			// previously used jboss class uses ISO-8859-1 encoding
			return new String(Base64.encode(encrypt(x).getBytes("ISO-8859-1")));
		}
		catch(UnsupportedEncodingException e) {
			throw new NuclosFatalException();
		}
	}

	public static String defaultIfNull(String x, String sDefault) {
		if (isNullOrEmpty(x)) {
			return sDefault;
		}
		return x;
	}

	public static int countChar(String s, char c) {
		int result = 0;

		char[] chars = s.toCharArray();

		for(int i = 0; i < chars.length; i++){
			if (chars[i] == c){
				result++;
			}
		}

		return result;
	}

	public static double[] getDoubleArrayFromString(String sComplete, char cSeparator)
	throws NumberFormatException
	{
		String sValue;
		int index = 0;
		int i = 0;

		double[] result = new double[StringUtils.countChar(sComplete, cSeparator)+1];
		index = sComplete.indexOf(cSeparator);
		while(index != -1){
			sValue = sComplete.substring(0, index);
			sComplete = sComplete.substring(index+1);
			result[i] = Double.parseDouble(sValue);
			i++;
			index = sComplete.indexOf(cSeparator);
		}
		result[i] = Double.parseDouble(sComplete);

		return result;
	}


	public static String xmlEncode(String s) {
		if(s == null)
			return null;
		StringBuilder sb = new StringBuilder(s.length() + 16);
		for(int i = 0, n = s.length(); i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			case '\"':
				sb.append("&quot;");
				break;
			default:
				if(c >= 128)
					sb.append(String.format("&#x%04x;", (int) c));
				else
					sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * get all required attribute names from the user defined pattern
	 * @param sPattern
	 * @return
	 */
	  public static Set<String> getFieldsFromTreeViewPattern(String sPattern) {
	     final Set<String> stAttributeNames = new HashSet<String>();
	     int sidx = 0;
	     while ((sidx = sPattern.indexOf("${", sidx)) >= 0) {
	         int eidx = sPattern.indexOf("}", sidx);
	         String key = sPattern.substring(sidx + 2, eidx);
	         int ci = key.indexOf(':');
	         if(ci >= 0) key = key.substring(0, ci);
	         stAttributeNames.add(key);
	         sidx = eidx;
	     }
	     return stAttributeNames;
	 }


	  /**
	   * Replaces all embedded parameters of the form <code>${...}</code>.
	   * The replacement string is determined by calling a given transformer.
	   * @param s the input string
	   * @param t the transformer which maps a parameter to its replacement
	   * @return the given string with all parameters replaced
	   */
	  public static String replaceParameters(String s, Transformer<String, String> t) {
		  StringBuffer sb = new StringBuffer();
		  Matcher m = PARAM_PATTERN.matcher(s);
		  while(m.find()) {
			  String resId = m.group(1);
			  String repString = t.transform(resId);
			  m.appendReplacement(sb, Matcher.quoteReplacement(repString));
		  }
		  m.appendTail(sb);
		  return sb.toString();
	  }

	  public static String replaceParametersRecursively(String text, final Properties props) {
		  return replaceParameters(text, new Transformer<String, String>() {
			  LinkedList<String> nesting = new LinkedList<String>();
			  @Override
			  public String transform(String p) {
				  if (nesting.contains(p))
					  throw new IllegalArgumentException("Recursive property definition for " + nesting.getFirst());
				  String rtext = props.getProperty(p);
				  if (rtext == null)
					  throw new IllegalArgumentException("Missing property " + p);
				  nesting.push(p);
				  String r = replaceParameters(rtext, this);
				  nesting.pop();
				  return r;
			  }
		  });
	  }

	  /**
	   * Decode a String with "classic java" unicode escape sequences
	   * @param s the input string
	   * @return the decoded string (or null, if the input was null)
	   */
	  public static String unicodeDecode(String s) {
		  if(s == null)
			  return null;
		  StringBuffer res = new StringBuffer();
		  Pattern p = Pattern.compile("\\\\u\\p{XDigit}{4}");
		  Matcher m = p.matcher(s);
		  while(m.find()) {
			  int v = Integer.parseInt(m.group().substring(2), 16);
			  m.appendReplacement(res, Character.toString((char) v));
		  }
		  return m.appendTail(res).toString();
	  }


	  /**
	   * As unicode-decode, but also parsing "\n" as newlines, "\t" as tabs, and
	   * "\"" as a simple quote
	   * @param s the input string
	   * @return the decoded string (or null, if the input was null)
	   */
	  public static String unicodeDecodeWithNewlines(String s) {
		  if(s == null)
			  return null;
		  s = unicodeDecode(s);
		  return s.replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t").replaceAll("\\\\\"", "\"");
	  }

	  public static String wildcardToRegex(CharSequence s) {
		  StringBuilder sb = new StringBuilder();
		  int len = s.length();
		  for (int i = 0; i < len; i++) {
			  char ch = s.charAt(i);
			  switch (ch) {
				  case '*':
					  sb.append(".*"); break;
				  case '?':
					  sb.append("."); break;
				  case '[': case ']': case '(': case ')': case '{': case '}':
				  case '|': case '+': case '-': case '^': case '$': case '\\':
				  case '.':
					  sb.append("\\").append(ch); break;
				  default:
					  sb.append(ch); break;
			  }
		  }
		  return sb.toString();
	  }

	  public static <T> String join(String sep, T...s) {
		  return org.apache.commons.lang.StringUtils.join(s, sep);
	  }

	  public static String join(String sep, Iterable<?> iter) {
		  return org.apache.commons.lang.StringUtils.join(iter.iterator(), sep);
	  }

	  @Deprecated
	  /** Use {@link StringUtils#join instead. That's the standard name for
	   * such this operation. */
	  public static String arrayToString(String[] a, String separator) {
		    if (a == null || separator == null) {
		        return null;
		    }
		    final StringBuilder result = new StringBuilder();
		    if (a.length > 0) {
		        result.append(a[0]);
		        for (int i=1; i < a.length; i++) {
		            result.append(separator);
		            result.append(a[i]);
		        }
		    }
		    return result.toString();
	  }

	  /**
	   * Locale-independent variant of {@link String#toLowerCase()}.
	   * This method works on {@code char} level and hence uses a locale-independent
	   * character mapping as defined by the Unicode standard.
	   */
	  public static String toLowerCase(String s) {
		  StringBuilder sb = new StringBuilder(s);
		  for (int i = 0, len = sb.length(); i < len; i++) {
			  char ch = sb.charAt(i);
			  char ch2 = Character.toLowerCase(ch);
			  if (ch != ch2)
				  sb.setCharAt(i, ch2);
		  }
		  return sb.toString();
	  }

	  /**
	   * Locale-independent variant of {@link String#toUpperCase()}.
	   * This method works on {@code char} level and hence uses a locale-independent
	   * character mapping as defined by the Unicode standard.
	   */
	  public static String toUpperCase(String s) {
		  StringBuilder sb = new StringBuilder(s);
		  for (int i = 0, len = sb.length(); i < len; i++) {
			  char ch = sb.charAt(i);
			  char ch2 = Character.toUpperCase(ch);
			  if (ch != ch2)
				  sb.setCharAt(i, ch2);
		  }
		  return sb.toString();
	  }

	  public static Integer parseInt(String s, Integer def) {
		  try {
			  return Integer.parseInt(s);
		  } catch (NumberFormatException e) {
			  return def;
		  }
	  }

	  public static String[] splitWithMatches(Pattern pattern, CharSequence input) {
		  return splitWithMatches(pattern, 0, input);
	  }

	  /**
	   * Splits the given input sequence around matches of the given pattern but
	   * also include the matches in the result.
	   * All strings at odd indices are matches of the given pattern, all strings
	   * at even indices are the splitted text fragments.
	   */
	  public static String[] splitWithMatches(Pattern pattern, int group, CharSequence input) {
		  Matcher m = pattern.matcher(input);
		  List<String> list = new ArrayList<String>();
		  while (m.find()) {
			  StringBuffer sb = new StringBuffer();
			  m.appendReplacement(sb, "");
			  list.add(sb.toString());
			  list.add(m.group(group));
		  }
		  StringBuffer sb = new StringBuffer();
		  m.appendTail(sb);
		  list.add(sb.toString());
		  return list.toArray(new String[list.size()]);
	}

	/**
	 * Returns a living view of the given map containing all strings that starts with the given
	 * prefix (including the prefix string itself). The view is backed by the original map.
	 * <p>
	 * Note: This method uses {@link SortedMap#subMap(Object, Object)} internally and works
	 * <em>only</em> for sorted string maps with natural (lexiographic) ordering!
	 */
	public static <V> SortedMap<String, V> submapWithPrefix(SortedMap<String, V> map, String prefix) {
		if (map.comparator() != null)
			throw new IllegalArgumentException("only natural (lexiographic) ordering supported");
		int length = prefix.length();
		if (length == 0)
			return map;
		// create a string lhs which is the _least higher string_ for the prefix, i.e.
		// there is no other string value between any prefixed string and lhs w.r.t ordering.
		StringBuilder lhs = new StringBuilder(prefix);
		char ch = lhs.charAt(length-1);
		lhs.setCharAt(length-1, (char) (ch+1));
		return map.subMap(prefix, lhs.toString());
	}

	/**
	 * Concatenate strings to one html string that starts with <code>&lt;html&gt;&lt;body&gt;</code>
	 * and ends with <code>&lt;/html&gt;&lt;/body&gt;</code>. The parts will be separated by <code>&lt;br/&gt;</code>.
	 * If a part is itself a html string (recognized if it starts with <code>&lt;html&gt;</code>), the start and end tags will be removed prior to concatenation.
	 *
	 * @param parts
	 * @return
	 */
	public static String concatHtml(String... parts) {
		StringBuilder result = new StringBuilder();
		result.append("<html><body>");
		int i = 0;
		for (String part : parts) {
			if (StringUtils.isNullOrEmpty(part)) {
				continue;
			}
			if (i > 0) {
				result.append("<br/>");
			}
			if (part.regionMatches(true, 0, "<html>", 0, "<html>".length())) {
				result.append(part.replaceAll("\\<\\/?(?i:html)\\>", "").replaceAll("\\<\\/?(?i:body)\\>", ""));
			}
			else {
				result.append(StringEscapeUtils.escapeHtml(part));
			}
			i++;
		}
		return result.append("</body></html>").toString();
	}
	
	public static String trimInvalidCharactersInFilename(String sFilename){
		// this is very ugly. regexp should be used here !
	    String invalidChars = "\\/:*?\"<>|(){}[]";
	    char ch[] = invalidChars.toCharArray();
	    
	    for(int i = 0 ; i < ch.length; i++){
	    	sFilename = sFilename.replace(ch[i], '_');	    	
	    }
	    sFilename = sFilename.replace(' ', '_');
	    

	    return sFilename.trim();
	}
	
	/**
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean equalsIgnoreCase(String s1, String s2) {
		return (s1 == null) ? (s2 == null) : s1.equalsIgnoreCase(s2);
	}
	
}	// class StringUtils
