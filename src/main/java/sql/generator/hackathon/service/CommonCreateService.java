package sql.generator.hackathon.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.CommonCreateObj;
import sql.generator.hackathon.model.Condition;
import sql.generator.hackathon.model.TableSQL;

@Service
public class CommonCreateService {

	@Autowired
	private CreateService createService;
	
	private Map<String, List<ColumnInfo>> tableInfo;
	
	private CommonCreateObj commonCreateObj;
	
	private Map<String, ColumnInfo> mappingColInfo;
	
	private Map<String, Integer> cntCompositeKeyInTable;
	
	private int DEFAULT_LENGTH = 5;
	
	// Load resource data example
	private Resource resource = new ClassPathResource("/example_data.properties");
	private HashMap<String, String> dataExamples = new HashMap<>();
		
	
	public void init(String type, List<String> listTable, String schema, 
			ExecuteDBSQLServer executeDBServer) {
		tableInfo = new HashMap<>();
		mappingColInfo = new HashMap<>();
		cntCompositeKeyInTable = new HashMap<>();
		this.commonCreateObj = new CommonCreateObj(); 
		if (type.equalsIgnoreCase("no database")) {
			commonCreateObj.init(0);
		} else {
			createService.init(executeDBServer);
			commonCreateObj.init(1, schema, listTable);
		}
		loadDataExample();
	}
	
	/**
	 * Common get table info
	 * @param tables
	 * @return
	 * @throws Exception 
	 */
	public void exeGetTableInfo(List<TableSQL> tables) throws Exception {
		if (commonCreateObj.getType() == 0) {
			tableInfo = getTableInfoWithoutConnect(tables);
		} else {
			tableInfo = createService.getTableInfo(commonCreateObj.getSchema(), commonCreateObj.getListTable()); 
		}
		exeMappingColInfo();
		exeCntCompositeKeyInTable();
	}
	
	/**
	 * Get Column info
	 * @param tableName
	 * @param colName
	 * @return
	 */
	public ColumnInfo getColumnInfo(String tableName, String colName) {
		String tableColName = tableName + "." + colName;
		return mappingColInfo.get(tableColName);
	}
	
	/**
	 * Get data type of column
	 * @return dataType of coumn(date, char, number), other null -> error!
	 */
	public String getDataTypeOfColumn(String typeName) {
		String dataType = "";
		switch (typeName) {
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
	 * Get data example
	 */
	public void loadDataExample() {
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
	 * Check table composite key
	 * @return true when isCompositeKey
	 */
	public boolean isCompositeKey(String tableName) {
		return cntCompositeKeyInTable.get(tableName) > 1;
	}
	
	/**
	 * Get table name and column name
	 * 
	 * @param column table.colName || colName
	 * @return String[2], String[0] = tableName, String[1] = columnName
	 */
	public String[] getTableAndColName(String input) {
		String[] res = new String[2];
		if (input.indexOf(".") != -1) {
			res = input.split("\\.");
		} else {
			res[1] = input;
		}
		return res;
	}
	
	public String getDefaultValue(String type) {
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
	
	public Map<String, List<ColumnInfo>> getTableInfo() {
		return tableInfo;
	}
	
	public CommonCreateObj getCommonCreateObj() {
		return commonCreateObj;
	}
	
	public void setCommonCreateObj(CommonCreateObj commonCreateObj) {
		this.commonCreateObj = commonCreateObj;
	}
	
	/**
	 * Get table info for NO connection
	 * @param tables
	 * @return
	 */
	private Map<String, List<ColumnInfo>> getTableInfoWithoutConnect(List<TableSQL> tables) {
		Map<String, List<ColumnInfo>> res = new HashMap<>();
		for (TableSQL table : tables) {
			List<ColumnInfo> listColInfo = new ArrayList<>();
			for (Condition condition : table.getCondition()) {
				String[] tableColName = getTableAndColName(condition.getLeft());
				ColumnInfo colInfo = new ColumnInfo(tableColName[1], "", "varchar", "255");
				listColInfo.add(colInfo);
			}
			res.put(table.getTableName(), listColInfo);
		}
		return res;
	}
	
	private void exeMappingColInfo() {
		tableInfo.entrySet().forEach(e -> {
			String tableName = e.getKey();
			for (ColumnInfo colInfo : e.getValue()) {
				mappingColInfo.put(tableName + "." + colInfo.getName(), colInfo);
			}
		});
	}

	/**
	 * Execute count key in table
	 */
	private void exeCntCompositeKeyInTable() {
		tableInfo.entrySet().forEach(e -> {
			int cnt = 0;
			String tableName = e.getKey();
			for (ColumnInfo colInfo : e.getValue()) {
				if (colInfo.isKey()) {
					cnt++;
				}
			}
			cntCompositeKeyInTable.put(tableName, cnt);
		});
	}
}
