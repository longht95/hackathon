package sql.generator.hackathon.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sql.generator.hackathon.create.CreateData;
import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.InfoDisplayScreen;

@Service
public class ExecuteDBSQLServer {
	DataSource dataSource;
	Connection connect;
	Statement statement;
	PreparedStatement p;
	
	@Autowired
	BeanFactory beanFactory;
	
//	@Autowired
//	CreateData createData;
	
	//connect database
	public void connectDB(String schemaName, String account, String pass) throws Exception {
		dataSource = (DataSource)beanFactory.getBean("dataSource", "jdbc:mysql://localhost:3306/" + schemaName, account, pass);
    	connect = dataSource.getConnection();
	}
	
	// get infor table (columnName, isNull, dataType, PK, FK, unique, maxLength)
	public Map<String, List<ColumnInfo>> getInforTable(String schemaName, List<String> lstTableName) throws Exception {
		Map<String, List<ColumnInfo>> inforTable = new HashMap<String, List<ColumnInfo>>();
		List<ColumnInfo> list_col;
		ColumnInfo columnInfo;
		PreparedStatement p;
		for (String tableName : lstTableName) {
			list_col = new ArrayList<ColumnInfo>();
			p = connect.prepareStatement("SELECT COLUMN_NAME, COLUMN_KEY, IS_NULLABLE, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH\r\n" + 
					"FROM    \r\n" + 
					"    information_schema.columns c\r\n" + 
					"WHERE\r\n" + 
					"	TABLE_NAME = ? AND TABLE_SCHEMA = ?\r\n"
					+ "\n" +
					"order by ORDINAL_POSITION");
			p.setString(1, tableName);
			p.setString(2, schemaName);
	        ResultSet resultSet = p.executeQuery();
	        
	        while (resultSet.next()) {
	        	columnInfo = new ColumnInfo();
				columnInfo.setName(resultSet.getString("COLUMN_NAME"));
				columnInfo.setTypeName(resultSet.getString("DATA_TYPE"));
				columnInfo.setTypeValue(resultSet.getString("CHARACTER_MAXIMUM_LENGTH"));
				
				if(resultSet.getString("IS_NULLABLE").equals("YES")) {
					columnInfo.setIsNull(true);
				}
				
				if(resultSet.getString("COLUMN_KEY").equals("PRI")){
					columnInfo.setIsPrimarykey(true);
				} else if (resultSet.getString("COLUMN_KEY").equals("UNI")) {
					columnInfo.setUnique(true);
				}
				columnInfo.setName(resultSet.getString("COLUMN_NAME"));
				list_col.add(columnInfo);
			}
	        inforTable.put(tableName, list_col);
	        resultSet.close();
		}
        return inforTable;
	}
	
	// get object (column, data) to show screen
	public InfoDisplayScreen getDataDisplay(String schemaName, String tableName) throws Exception {
		InfoDisplayScreen infoDisplayScreen = new InfoDisplayScreen();
		infoDisplayScreen.setListColumnName(this.getListColumn(schemaName, tableName));
		infoDisplayScreen.setListData(this.getListData(tableName));
		return infoDisplayScreen;
	}

	//get list column to show screen
	private List<String> getListColumn(String schemaName, String tableName) throws Exception {
		List<String> list_col = new ArrayList<String>();
		PreparedStatement p = connect.prepareStatement("SELECT COLUMN_NAME FROM information_schema.columns WHERE TABLE_NAME = ? AND TABLE_SCHEMA = ? order by ORDINAL_POSITION");
		p.setString(1, tableName);
		p.setString(2, schemaName);
		ResultSet resultSet = p.executeQuery();
		
		while (resultSet.next()) {
			for (int i = 0; i< resultSet.getMetaData().getColumnCount(); i++) {
				list_col.add(resultSet.getString(i + 1));
			}
		}
		resultSet.close();
		return list_col;
	}
	
	//get list data to show screen, limit 20 record
	private List<List<String>> getListData(String tableName) throws Exception {
		List<List<String>> listData = new ArrayList<List<String>>();
		List<String> rowData;
		Statement stmt = connect.createStatement();
		StringBuilder SQL = new StringBuilder();
		SQL.append("SELECT * FROM ");
		SQL.append(tableName);
		SQL.append(" LIMIT 20");
		ResultSet resultSet = stmt.executeQuery(SQL.toString());
		
		while (resultSet.next()) {
			rowData = new ArrayList<String>();
			for (int i = 0; i< resultSet.getMetaData().getColumnCount(); i++) {
				rowData.add(resultSet.getString(i + 1));
			}
			listData.add(rowData);
		}
		stmt.close();
		resultSet.close();
		return listData;
	}
	
	// check value is unique or not
	public boolean isUniqueValue(String tableName, ColumnInfo columnInfo, String value) throws SQLException {
		Statement stmt = connect.createStatement();
		StringBuilder SQL = new StringBuilder();
		SQL.append("SELECT Count(*) FROM " + tableName);
		SQL.append(" WHERE " + columnInfo.getName() + " = '" + value +"'");
		ResultSet resultSet = stmt.executeQuery(SQL.toString());
		while (resultSet.next()) {
			if(("0").equals(resultSet.getString(1))) {
				return true;
			}
		}
		return false;
	}
	
	// gene list value unique
	public List<String> genListUniqueVal(String tableName, ColumnInfo columnInfo, String start, String end) throws SQLException {
		List<String> lstUniqueVal = new ArrayList<String>();
		switch(columnInfo.getTypeName()) {
			case "varchar":
				lstUniqueVal = genListStringUnique(tableName, columnInfo);
				break;
			case "int":
				lstUniqueVal = genListNumberUnique(tableName, columnInfo, start, end);
				break;
			default:
				break;
		}
		
		return lstUniqueVal;
	}
	
	private List<String> genListStringUnique(String tableName, ColumnInfo columnInfo) throws SQLException {
		CreateData createData = new CreateData();
		List<String> lstStringUnique = new ArrayList<String>();
		Statement stmt = connect.createStatement();
		StringBuilder SQL = new StringBuilder();
		SQL.append("SELECT MAX(" + columnInfo.getName() + ") FROM " + tableName);
		ResultSet resultSet = stmt.executeQuery(SQL.toString());
		while (resultSet.next()) {
			String increaseValue = "";
			for (int i = 0; i < 10000; i++) {
				increaseValue = createData.genKeyWithTypeChar(true, resultSet.getString(1));
				lstStringUnique.add(increaseValue);
			}
		}
		resultSet.close();
		return lstStringUnique;
	}
	
	private List<String> genListNumberUnique(String tableName, ColumnInfo columnInfo, String start, String end) throws SQLException {
		List<String> lstNumberUnique = new ArrayList<String>();
		Statement stmt = connect.createStatement();
		StringBuilder SQL = new StringBuilder();
		int indexStart = 0;
		int indexEnd = 0;
		SQL.append("SELECT " + columnInfo.getName() + " FROM " + tableName);
		SQL.append(" WHERE ");
		if (!(start == null || start.isEmpty()) && !(end == null || end.isEmpty())) {
			indexStart = Integer.parseInt(start);
			indexEnd = Integer.parseInt(end);
		} else if(start == null || start.isEmpty()) {
			indexStart = Integer.parseInt(end) - 10000;
			indexEnd = Integer.parseInt(end);
		} else {
			indexStart = Integer.parseInt(start);
			indexEnd = Integer.parseInt(start)  + 10000;
		}
		SQL.append(columnInfo.getName() + " BETWEEN " + indexStart + " AND " + end);
		ResultSet resultSet = stmt.executeQuery(SQL.toString());
		
		for (int i = indexStart; i <= indexEnd; i++) {
			lstNumberUnique.add(Integer.toString(i));
		}
		while (resultSet.next()) {
			lstNumberUnique.remove(resultSet.getString(1));
		}
		resultSet.close();
		return lstNumberUnique;
	}
	
	// gene value for key
	public Map<String, String> genUniqueCol(String schema, String tableName, ColumnInfo columnInfo, String value) throws SQLException {
		Map<String, String> mapUnique = new HashMap<String, String>();
		List<ColumnInfo> listColPri = getColPrimaryKey(schema, tableName, columnInfo);
		if(listColPri.size() == 0) {
			return mapUnique;
		}
		Map<String, ColumnInfo> mapValueKey = getValuePrimaryKey(tableName, listColPri, columnInfo);
		String randomValue = "";
//		ColumnInfo columnInfoTmp;
		for (Map.Entry<String, ColumnInfo> entry : mapValueKey.entrySet()) {
//			columnInfoTmp = new ColumnInfo();
//			columnInfoTmp.setName(entry.getKey());
			do {
				randomValue = createValueRandom(entry.getValue());
			} while (!isUniqueValue(tableName, entry.getValue(), randomValue));
			mapUnique.put(entry.getKey(), randomValue);
		}
		return mapUnique;
	}
	
	//get column primary key in table
	private List<ColumnInfo> getColPrimaryKey(String schema, String tableName, ColumnInfo columnInfo) throws SQLException {
		List<ColumnInfo> listColPri = new ArrayList<ColumnInfo>();
		ColumnInfo columnKeyReturn;
		Statement stmt = connect.createStatement();
		StringBuilder SQL = new StringBuilder();
		SQL.append("SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE table_schema = '" + schema + "'");
		SQL.append(" AND (column_key = 'PRI' OR column_key = 'UNI') AND table_name = '" + tableName + "' AND COLUMN_NAME <> '" + columnInfo.getName() + "'");
		ResultSet resultSet = stmt.executeQuery(SQL.toString());
		while (resultSet.next()) {
			columnKeyReturn = new ColumnInfo();
			columnKeyReturn.setName(resultSet.getString(1));
			columnKeyReturn.setTypeName(resultSet.getString(2));
			listColPri.add(columnKeyReturn);
		}
		resultSet.close();
		return listColPri;
	}
	
	// get value of all column primary key
	private Map<String, ColumnInfo> getValuePrimaryKey(String tableName, List<ColumnInfo> listColKey, ColumnInfo columnInfo) throws SQLException {
		Map<String, ColumnInfo> mapValuePrimaryKey = new HashMap<String, ColumnInfo>();
		Statement stmt = connect.createStatement();
		StringBuilder SQL = new StringBuilder();
		SQL.append("SELECT ");
		for (int i = 0; i < listColKey.size(); i++) {
			if(i > 0) {
				SQL.append(", ");
			}
			SQL.append(listColKey.get(i).getName());
		}
		SQL.append(" FROM " + tableName + " LIMIT 1");
		ResultSet resultSet = stmt.executeQuery(SQL.toString());
		while (resultSet.next()) {
			for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
				listColKey.get(i).setValue(resultSet.getString(i + 1));
				mapValuePrimaryKey.put(listColKey.get(i).getName(), listColKey.get(i));
			}
		}
		resultSet.close();
		return mapValuePrimaryKey;
	}
	
	//create random String
	private String createValueRandom(ColumnInfo columnInfo) throws SQLException {
		int length = 0;
		if(!columnInfo.getValue().isEmpty()) {
			length = columnInfo.getValue().length();
		}else {
			length = Integer.parseInt(columnInfo.getTypeValue());
		}
		char[] ch = new char[length];
		int random = 0;
		// random char from 65 -> 122
		for (int i = 0; i < length; i++) {
			if(columnInfo.getTypeName().contentEquals("varchar")) {
				random = new Random().nextInt(57) + 65;
			} else {
				random = new Random().nextInt(9) + 48;
			}
			ch[i] = (char) (random);
			System.out.println(random);
		}
		return String.valueOf(ch);
	}
}
