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
		TestInsertDB.connect();
	}
	
	public void insert(String tableName, List<ColumnInfo> columnInfos) {
		// Get all column
		// Get all value sorted follumn column
		Map<String, String> m = getMapColumnsAndValues(columnInfos);
		
		String sqlInsert = "INSERT INTO" + tableName + " (?) VALUES (?)";
        try {
            // crate statement to insert student
            PreparedStatement stmt = conn.prepareStatement(sqlInsert);
            stmt.setString(1, m.get("colName"));
            stmt.setString(2, m.get("val"));
            int c = stmt.executeUpdate();
            if (c == 0) {
            	System.out.println("Error");
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
			
			colNames.append(columnInfo.name);
			values.append(columnInfo.val);
			
			if (colNames.length() != 0) {
				colNames.append(",");
			}
			
			if (values.length() != 0) {
				values.append(",");
			}
		}
		return res;
	}
}
