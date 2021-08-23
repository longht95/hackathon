package sql.generator.hackathon.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import sql.generator.hackathon.create.CreateData;
import sql.generator.hackathon.create.main.TestInsertDB;
import sql.generator.hackathon.create.main.TestReadParse;
import sql.generator.hackathon.model.ColumnInfo;
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
		executeDBServer.connectDB("admindb", "root", "");

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
		
		Map<String, List<ColumnInfo>> dataClient = new HashMap<>();
//		List<ColumnInfo> listCol = new ArrayList<ColumnInfo>();
//		listCol.add(new ColumnInfo("name", "T-Company"));
//		listCol.add(new ColumnInfo("description", "acc"));
//		listCol.add(new ColumnInfo("address", "----"));
//		dataClient.put("company", listCol);
		
		// Need List<TableSQL> FROM PARSE
		// Map<String, List<String> FROM PARSE
		CreateData createData = new CreateData(executeDBServer, createService, TestReadParse.tables, TestReadParse.keys);
		createData.create(dataClient);

		return "index";
	}
}
