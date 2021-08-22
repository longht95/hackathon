package sql.generator.hackathon.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import sql.generator.hackathon.create.CreateData;
import sql.generator.hackathon.create.main.TestInsertDB;
import sql.generator.hackathon.create.main.TestReadParse;
import sql.generator.hackathon.service.CreateService;
import sql.generator.hackathon.service.ExecuteDBSQLServer;

@Controller
public class TestCreateController {

	@Autowired
	private ExecuteDBSQLServer executeDBServer;
	
	@Autowired
	private CreateService createService;
	
	@RequestMapping(value = "/testCreate")
	public String testCreate() {
		// Test insert H2 with native query
//		TestInsertDB testInsertDb = new TestInsertDB();
//		testInsertDb.create();
		
		return "index";
	}
	
	@RequestMapping(value = "/showTable")
	public String showTable() {
		// Test insert H2 with native query
		TestInsertDB testInsertDb = new TestInsertDB();
		testInsertDb.show();	
		return "index";
	}
	
	@RequestMapping(value = "/testConnectT")
	public String testConnect() throws Exception {
		executeDBServer.connectDB("", "", "");
		
		
//		Map<String, List<ColumnInfo>> inforTable = executeDBServer.getInforTable("admindb", lstTableName);
//		for (Map.Entry<String, List<ColumnInfo>> entry : inforTable.entrySet()) {
//			System.out.println("Key: " + entry.getKey());
//	        List<ColumnInfo> lstColInfo = entry.getValue();
//	        for (ColumnInfo columnInfo: lstColInfo) {
//		        System.out.println("		name: " + columnInfo.getName());
//		        System.out.println("		typeName: " + columnInfo.getTypeName());
//		        System.out.println("		typeValue: " + columnInfo.getTypeValue());
//		        System.out.println("		isNull: " + columnInfo.getIsNull());
//		        System.out.println("		isPrimarykey: " + columnInfo.getIsPrimarykey());
//		        System.out.println("		isForeignKey: " + columnInfo.getIsForeignKey());
//		        System.out.println("		unique: " + columnInfo.getUnique());
//		        System.out.println("----------------------------------");
//	        }
//	    }
		
//		InfoDisplayScreen infoDisplayScreen = executeDBServer.getDataDisplay("users");
//		System.out.println("infoDisplayScreen: " + infoDisplayScreen);

		// Test read
		int n = 1;
		for (int i = 1; i <= n; ++i) {
			TestReadParse.readObjectTest(String.valueOf(i));
		}
		
		// BEGIN: CALL AFTER PARSE
		
		// Need list table from parse 
		List<String> lstTableName = Arrays.asList("persons");
		
		// Connect dB
		createService.connect(executeDBServer.connect);
		createService.setTableInfo(executeDBServer.getInforTable("admindb", lstTableName));
		
		// Test create
		CreateData createData = new CreateData(createService, TestReadParse.tables, TestReadParse.keys);
		createData.create();
		// END: CALL AFTER PARSE
		
		
		// BEGIN: CALL WHEN USER CLICK BUTTON GEN DATA!
		// Need Map<String, List<ColumnInfo>>
		// Key = tableName, List Cac columnInfo voi colName va value!
		// TODO
		// Hiện tại chưa thể change value cho KEY
		// createData.update(); 
		
		return "index";
	}
}
