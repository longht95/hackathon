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

import javax.sql.DataSource;

import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.InfoColumnConditionValue;
import sql.generator.hackathon.model.InfoDisplayScreen;

@Service
public class ExecuteDBSQLServer {
	DataSource dataSource;
	public Connection connect;
	Statement statement;
	PreparedStatement p;
	
	@Autowired
	BeanFactory beanFactory;
	
	//connect database
	public void connectDB(String url, String account, String pass) throws Exception {
		dataSource = (DataSource)beanFactory.getBean("dataSource", "jdbc:mysql://localhost:3306/admindb", "root", "");
//		dataSource = (DataSource)beanFactory.getBean("dataSource", url, account, pass);
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
//			p.setString(1, "Users");
//	        p.setString(2, "admindb");
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
	public InfoDisplayScreen getDataDisplay(String tableName) throws Exception {
		InfoDisplayScreen infoDisplayScreen = new InfoDisplayScreen();
		infoDisplayScreen.setListColumnName(this.getListColumn(tableName));
		infoDisplayScreen.setListData(this.getListData(tableName));
		return infoDisplayScreen;
	}

	//get list column to show screen
	private List<String> getListColumn(String tableName) throws Exception {
		List<String> list_col = new ArrayList<String>();
		PreparedStatement p = connect.prepareStatement("SELECT COLUMN_NAME FROM information_schema.columns WHERE TABLE_NAME = ? AND TABLE_SCHEMA = ? order by ORDINAL_POSITION");
		p.setString(1, "Users");
		p.setString(2, "admindb");
		ResultSet resultSet = p.executeQuery();
		
		while (resultSet.next()) {
			for (int i = 0; i< resultSet.getMetaData().getColumnCount(); i++) {
				list_col.add(resultSet.getString(i + 1));
			}
		}
//		stmt.close();
		resultSet.close();
		return list_col;
	}
	
	//get list data to show screen, limit 20 record
	private List<List<String>> getListData(String tableName) throws Exception {
		List<List<String>> listData = new ArrayList<List<String>>();
		List<String> rowData;
//		PreparedStatement p = connect.prepareStatement("SELECT * FROM ? LIMIT 20");
//		p.setString(1, "Users");
//		ResultSet resultSet = p.executeQuery();
		
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
	
	//get data match condition and is unique
	public void getDataMatchConditionAndUnique(List<InfoColumnConditionValue> infoColumnConditionValueLst) throws SQLException {
		Statement stmt = connect.createStatement();
		StringBuilder SQL = new StringBuilder();
		SQL.append("");
		ResultSet resultSet = stmt.executeQuery(SQL.toString());
	}
	
	// get data match all column and is unique 
	public void getDataMatchColumnAndUnique(List<InfoColumnConditionValue> infoColumnConditionValueLst) throws SQLException {
		
	}
}
