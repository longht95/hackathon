package sql.generator.hackathon.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
			
			values.append(getCorrectValue(columnInfo));
		}
		
		// Get structure of tableName
		// Add default data when can't not null
		List<ColumnInfo> structure = tableInfo.get(tableName);
		for (ColumnInfo columnInfo : structure) {
			if ((!columnInfo.isNull && !columnInfo.isKey()) && colNames.indexOf(columnInfo.name) == -1) {
				if (colNames.length() != 0) {
					colNames.append(",");
				}
				
				if (values.length() != 0) {
					values.append(",");
				}
				
				colNames.append(columnInfo.name);
				String type = getDataTypeOfColumn(columnInfo);
				ColumnInfo innerInfo = new ColumnInfo(columnInfo.name, "");
				switch(type) {
				case "char":
					innerInfo.val = dataExamples.get("name");
					break;
				case "number":
					innerInfo.val = dataExamples.get("number");
					break;
				case "date":
					// TODO
					// Need check!
					innerInfo.val = "2021-10-10";
					break;
				}
				innerInfo.typeName = type;
				values.append(getCorrectValue(innerInfo));
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
		int len = 1;
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
			if (columnInfo.val.indexOf("'") >= 0) {
				res = columnInfo.val;
			} else {
				res = "'" + columnInfo.val + "'";
			}
			break;
		case "char":
			// Has ''
			if (columnInfo.val.indexOf("'") >= 0) {
				res = columnInfo.val;
			} else {
				res = "'" + columnInfo.val + "'";
			}
			break;
		case "number":
			res = columnInfo.val;
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
}
