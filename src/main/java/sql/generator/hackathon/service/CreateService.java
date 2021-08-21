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

	private HashMap<String, List<ColumnInfo>> tableInfo = new HashMap<>();
	
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
	
	
	/**
	 * Get ColumnInfo
	 * @return ColumnInfo 
	 */
	public ColumnInfo getColumInfo(String tableName, String colName) {
		List<ColumnInfo> listColInfo = tableInfo.get(tableName);
		for (ColumnInfo columnInfo : listColInfo) {
			if (columnInfo.name.equals(colName)) {
				return columnInfo;
			}
		}
		return null;
	}
	
	/**
	 * Get data type of column
	 * @return dataType of coumn(date, char, number), other null -> error!
	 */
	public String getDataTypeOfColumn(ColumnInfo columnInfo) {
		String dataType = "";
		switch (columnInfo.typeName) {
		case "char":
		case "nchar":
		case "varchar":
		case "nvarchar":
			dataType = "char";
			break;
		case "date":
			dataType = "date";
			break;
		case "number":
			dataType = "number";
			break;
		default:
			System.out.println("Other data type");
			break;
		}
		return dataType;
	}
	
	/**
	 * Get length of columnInfo
	 * @return length of column
	 */
	public int getLengthOfColumn(ColumnInfo columnInfo) {
		int len = 1;
		switch (columnInfo.typeName) {
		case "char":
		case "nchar":
		case "varchar":
		case "nvarchar":
			len = columnInfo.typeValue;
			break;
		case "number":
			len = columnInfo.typeValue; // case [p,s]?
			break;
		case "date":
			break;
		default:
			System.out.println("Other data type");
			break;
		}
		return len;
	}
}
