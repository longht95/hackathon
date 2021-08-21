package sql.generator.hackathon.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import sql.generator.hackathon.create.main.TestInsertDB;
import sql.generator.hackathon.model.ColumnInfo;

@Service
public class CreateService {

	private static Connection conn;
	
	public CreateService() {
		conn = TestInsertDB.connect();
	}
	
	/**
	 * Insert data table in DB
	 * @param tableName
	 * @param columnInfos
	 */
	public void insert(String tableName, List<ColumnInfo> columnInfos) {
		// Get all column
		// Get all value sorted follumn column
		Map<String, String> m = getMapColumnsAndValues(columnInfos);
		
		String sqlInsert = "INSERT INTO " + tableName + "(" + m.get("columns") + ") VALUES (" + m.get("values") + ")";
        try {
            // crate statement to insert student
            PreparedStatement stmt = conn.prepareStatement(sqlInsert);
            int c = stmt.executeUpdate();
            if (c == 0) {
            	System.out.println("Insert Error!");
            } else {
            	System.out.println("Insert success!");
            }
            
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
	}
	
	/**
	 * Update data table
	 * @param tableName
	 * @param columnInfos
	 */
	public void update(String tableName, List<ColumnInfo> columnInfos, List<ColumnInfo> condition) {
		// Get all column
		// Get all value sorted follumn column
		Map<String, String> m = getMapColumnsAndValues(columnInfos);
		
		String sqlInsert = "Update " + tableName + " SET " + m.get("columns") + ") VALUES (" + m.get("values") + ")";
        try {
            // crate statement to insert student
            PreparedStatement stmt = conn.prepareStatement(sqlInsert);
            int c = stmt.executeUpdate();
            if (c == 0) {
            	System.out.println("Update Error!");
            } else {
            	System.out.println("Update success!");
            }
            
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
	}
	
	/**
	 * Get all colName
	 * @param ColumnInfos List columnInfo
	 * @return Map<String, String>all columnName
	 */
	public Map<String, String> getMapColumnsAndValues(List<ColumnInfo> columnInfos) {
		Map<String, String> res = new HashMap<>();
		StringBuilder colNames = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (ColumnInfo columnInfo : columnInfos) {
			if (colNames.length() != 0) {
				colNames.append(",");
			}
			
			if (values.length() != 0) {
				values.append(",");
			}
			
			colNames.append(columnInfo.name);
			values.append(columnInfo.val);
		}
		res.put("columns", colNames.toString());
		res.put("values", values.toString());
		return res;
	}
	
	/**
	 * Get str column update
	 * @param columnInfos
	 * @return String column update (col1 = '5', col2 = '6')
	 */
	public String getStrColumnUpdate(List<ColumnInfo> columnInfos) {
		StringBuilder res = new StringBuilder();
		for (ColumnInfo columnInfo : columnInfos) {
			if (res.length() != 0) {
				res.append(", ");
			}
			res.append(columnInfo.name + " = " + columnInfo.val);
		}
		return res.toString();
	}
	
	/**
	 * Get str column in condition where
	 * @param condition
	 * @return String column condition (col1 = '5' AND col2 = '6')
	 */
	public String getStrColUpdateCondition(List<ColumnInfo> condition) {
		StringBuilder res = new StringBuilder();
		for (ColumnInfo columnInfo : condition) {
			if (res.length() != 0) {
				res.append("AND ");
			}
			res.append(columnInfo.name + " = " + columnInfo.val);
		}
		return res.toString();
	}
	
}
