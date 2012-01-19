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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.tools.translation.translationdata.TranslationData;
import org.nuclos.tools.translation.translationdata.TranslationData.TranslationAction;



public class TranslationDAO {

	private static String tableName = "T_MD_LOCALERESOURCE_TEST";
	private static Integer locale = 3;

   private static String URL = "jdbc:oracle:thin:@127.0.0.1:1521:oracle";
   private static String LOGIN = "nucleus";
   private static String PASS = "nucleus";

   public static String UNINTERESTING_STRINGS_PREFIX = "NOT_";

   public static Map<String,RessourceData> getRessourceData() throws SQLException {

      Map<String,RessourceData> res = new HashMap<String,RessourceData>();

      try {
         Connection con = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;

         try {

        	Class.forName("oracle.jdbc.driver.OracleDriver");
  			con = DriverManager.getConnection(URL, LOGIN, PASS);

            pstmt = con.prepareStatement("select * from "+tableName); //+" r where r.STRRESOURCEID not like '"+UNINTERESTING_STRINGS_PREFIX+"'");
            rs = pstmt.executeQuery();

            while (rs.next()) {
               RessourceData rd = new RessourceData();
               rd.setRessId(rs.getString("STRRESOURCEID"));
               rd.setText("\""+rs.getString("STRTEXT")+"\"");

               res.put(rd.getRessId(),rd);
            }

         } finally {
           if(rs != null) rs.close();
           if(pstmt != null) pstmt.close();
           if(con != null) con.close();
         }
       } catch (Exception ex) {
         ex.printStackTrace();
       }

       return res;
   }

//   public static Set<String> getUninterestingStrings() throws SQLException {
//
//      Set<String> res = new HashSet<String>();
//
//      try {
//         Connection con = null;
//         PreparedStatement pstmt = null;
//         ResultSet rs = null;
//
//         try {
//
//            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
//            con = DriverManager.getConnection(URL, LOGIN, PASS);
//
//            pstmt = con.prepareStatement("select * from T_MD_LOCALERESOURCE_TEST r where r.STRRESOURCEID like '"+UNINTERESTING_STRINGS_PREFIX+"'");
//            rs = pstmt.executeQuery();
//
//            while (rs.next())
//               res.add(rs.getString("text"));
//
//         } finally {
//           if(rs != null) rs.close();
//           if(pstmt != null) pstmt.close();
//           if(con != null) con.close();
//         }
//       } catch (Exception ex) {
//         ex.printStackTrace();
//       }
//
//       return res;
//    }

   public static void saveTransactionData(List<TranslationData> dataList) throws SQLException {
      Map<String,RessourceData> resList = new HashMap<String,RessourceData>();

      String lastWrittenRessId = "";

      for (TranslationData td : dataList) {
         if (!td.getPerform())
            continue;

         if (  td.getAction().equals(TranslationAction.NEW_ID) ||
               td.getAction().equals(TranslationAction.SAVE_ID) ||
               td.getAction().equals(TranslationAction.UPDATE_DB))
               // || td.getAction().equals(TranslationAction.MARK_UNINTERESTING))
         {
            // only once per RessId
            if (!lastWrittenRessId.equals(td.getResourceId())) {
               String text = "";
               if (td.getText().startsWith(TranslationHelper.TARGET_METHOD))
                  text = TranslationUtil.removeQuotes(TranslationUtil.removeMethodFrame(td.getText()));
               else
                  text = TranslationUtil.removeQuotes(td.getText());

               resList.put(td.getResourceId(), new RessourceData(td.getResourceId(),text,td.getAction().equals(TranslationAction.UPDATE_DB)));
               lastWrittenRessId = td.getResourceId();
            }
         }
      }

      saveRessourceData(resList);
   }

   public static void saveRessourceData(Map<String,RessourceData> data) throws SQLException {

      try {
      	int n = 0;
         Connection con = null;
         Statement stmt = null;
         //ResultSet rs = null;

         try {
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            con = DriverManager.getConnection(URL, LOGIN, PASS);
            stmt = con.createStatement();

            for (RessourceData rd : data.values()) {
               String sql = String.format(
               	"insert into %s " +
               	"(INTID,STRRESOURCEID,INTID_T_MD_LOCALE,STRTEXT,DATCREATED,STRCREATED,DATCHANGED,STRCHANGED,INTVERSION) " +
               	"values(%d,'%s',%d, '%s',sysdate,'nucleus',sysdate,'nucleus',1)",
               	tableName, getNextIdAsInteger(), rd.getRessId(), locale, dbEscape(rd.getText()));

               System.out.println("saving RessourceData with RessId='"+rd.getRessId()+"' and text="+rd.getText());
               stmt.executeUpdate(sql);
               //rs.close();
               n++;
            }

         } finally {
           //if(rs != null) rs.close();
           if(stmt != null) stmt.close();
           if(con != null) con.close();
           
           System.out.println("Saved " + n + " of " + data.values().size() + " entries");
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   private static String dbEscape(String t) {
   	return t.replaceAll("'", "''");
   }
   
   
   private static Integer getNextIdAsInteger() {

   	Integer id = null;

   	try {
         Connection con = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;

         try {

            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            con = DriverManager.getConnection(URL, LOGIN, PASS);

            pstmt = con.prepareStatement("select IDFACTORY.NEXTVAL id from dual");
            rs = pstmt.executeQuery();

            if (rs.next())
            	id = rs.getInt("id");

            rs.close();
            pstmt.close();

         } finally {
           if(rs != null) rs.close();
           if(pstmt != null) pstmt.close();
           if(con != null) con.close();
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      return id;
   }
}
