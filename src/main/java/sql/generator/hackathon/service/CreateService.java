package sql.generator.hackathon.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import sql.generator.hackathon.model.ColumnInfo;

@Service
public class CreateService {
	
	// Load resource data example
	private static Resource resource = new ClassPathResource("/example_data.properties");
	private static HashMap<String, String> dataExamples = new HashMap<>();
	
	private static int DEFAULT_LENGTH = 5;
	
	private Map<String, List<ColumnInfo>> tableInfo = new HashMap<>();
	
	private Connection conn;
	
	public CreateService() {
	}
	
	public void connect(Connection conn) {
		this.conn = conn;
	}
	
	public void setTableInfo(Map<String, List<ColumnInfo>> tableInfo) {
		this.tableInfo = tableInfo;
	}
	
	public Map<String, List<ColumnInfo>> getTableInfo() {
		return tableInfo;
	}
	
	/**
	 * Insert data table in DB
	 * @param tableName
	 * @param columnInfos
	 */
	public void insert(String tableName, List<ColumnInfo> columnInfos) {
		// Get all column
		// Get all value sorted follumn column
		Map<String, String> m = getMapColumnsAndValues(tableName, columnInfos);
		
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
		String colUpdate = getStrColumnUpdate(columnInfos);
		String conditionUpdate = getStrColUpdateCondition(condition);

		String sqlInsert = "Update " + tableName + " SET " + colUpdate + " WHERE " + conditionUpdate;
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
	public Map<String, String> getMapColumnsAndValues(String tableName, List<ColumnInfo> columnInfos) {
		
		// Init
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
			
			// Get default value
			if (!columnInfo.isNull && columnInfo.val.isEmpty()) {
				values.append(getDefaultValue(columnInfo.getTypeName()));
			} else {
				values.append(getCorrectValue(columnInfo));
			}
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
			res.append(columnInfo.name + " = " + getCorrectValue(columnInfo));
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
			res.append(columnInfo.name + " = " + getCorrectValue(columnInfo));
		}
		return res.toString();
	}
	
	
	/**
	 * Get ColumnInfo
	 * @return ColumnInfo 
	 */
	public ColumnInfo getColumInfo(String tableName, String colName) {
		List<ColumnInfo> listColInfo = tableInfo.get(tableName);
		if (listColInfo == null) {
			return null;
		}
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
		case "int":
		case "bigint":
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
		int len = DEFAULT_LENGTH;
		switch (columnInfo.typeName) {
		case "char":
		case "nchar":
		case "varchar":
		case "nvarchar":
			if (columnInfo.typeValue != null) {
				len = Integer.parseInt(columnInfo.typeValue);
			}
			break;
		case "number":
		case "int":
		case "bigint":
			if (columnInfo.typeValue != null) {
				len = Integer.parseInt(columnInfo.typeValue); // case [p,s]?
			}
			break;
		case "date":
			break;
		default:
			System.out.println("Other data type");
			break;
		}
		return len;
	}
	
	/**
	 * Get correct value column
	 */
	private String getCorrectValue(ColumnInfo columnInfo) {
		String type = getDataTypeOfColumn(columnInfo);
		String res = "";
		switch (type) {
		case "date":
			String val = columnInfo.val;
			if (columnInfo.val.indexOf("'") >= 0) {
				res = columnInfo.val;
			} else {
				if (val.isEmpty()) {
					// Get current date
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
					LocalDateTime now = LocalDateTime.now();  
					val = dtf.format(now);
				}
				res = "'" + val + "'";
			}
			break;
		case "varchar":
		case "nvarchar":
		case "char":
		case "nchar":
			// Has ''
			if (columnInfo.val.indexOf("'") >= 0) {
				res = columnInfo.val;
			} else {
				res = "'" + columnInfo.val + "'";
			}
			break;
		case "number":
		case "int":
		case "bigint":
			if (columnInfo.val.isEmpty()) {
				res = "0";
			} else {
				res = columnInfo.val;
			}
			break;
		default:
			System.out.println("Other?");
			break;
		}
		return res;
	}
	
	/**
	 * Get data example
	 */
	public void getDataExample() {
		try {
			// Load data example
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			Set<String> keys = props.stringPropertyNames();
			for (String key : keys) {
				dataExamples.put(key, props.getProperty(key));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Check table composite key
	 * @return true when isCompositeKey
	 */
	public boolean isCompositeKey(String tableName) {
		List<ColumnInfo> table = tableInfo.get(tableName);
		int cnt = 0;
		for (ColumnInfo colInfo : table) {
			if (colInfo.isKey()) {
				cnt++;
			}
		}
		return cnt > 1;
	}
	
	private String getDefaultValue(String type) {
		String res = "";
		switch(type) {
		case "varchar":
		case "nvarchar":
		case "char":
		case "nchar":
			res = dataExamples.get("name");
			break;
		case "number":
		case "int":
		case "bigint":
			res = dataExamples.get("number");
			break;
		case "date":
			// Get current date
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
			LocalDateTime now = LocalDateTime.now();  
			res = dtf.format(now);
			break;
		}
		return res;
	}
}
