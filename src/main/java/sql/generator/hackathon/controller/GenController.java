package sql.generator.hackathon.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.service.ExecuteDBSQLServer;
import sql.generator.hackathon.service.ServiceDatabase;

@Controller
public class GenController {
	@Autowired
	ServiceDatabase serviceDatabase;
	
	@Autowired
	ExecuteDBSQLServer executeDBServer;
	
	@RequestMapping(value = "/")
	public String index() throws Exception {
//		serviceDatabase.showTables();
		executeDBServer.connectDB("admindb", "root", "Root123!");
		
//		List<String> lstTableName = Arrays.asList("users", "class");
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
//		
//		InfoDisplayScreen infoDisplayScreen = executeDBServer.getDataDisplay("admindb", "users");
//		System.out.println("infoDisplayScreen: " + infoDisplayScreen);
		
		
//		System.out.println(executeDBServer.isUniqueValue("users", columnInfo, "test3"));
//		ColumnInfo columnInfo = new ColumnInfo();
//		columnInfo.setName("user_name");
//		columnInfo.setTypeName("varchar");
//		Map<String, String> mapUnique = executeDBServer.genUniqueCol("admindb", "class", columnInfo, "test3");
//		ColumnInfo columnInfo1 = new ColumnInfo();
//		columnInfo1.setName("email");
//		columnInfo1.setTypeName("varchar");
//		Map<String, String> mapUnique1 = executeDBServer.genUniqueCol("admindb", "users", columnInfo1, "test3");
//		ColumnInfo columnInfo2 = new ColumnInfo();
//		columnInfo2.setName("email");
//		columnInfo2.setTypeName("varchar");
		ColumnInfo columnInfo3 = new ColumnInfo();
		columnInfo3.setName("age");
		columnInfo3.setTypeName("int");
		List<String> aaa = executeDBServer.genListUniqueVal("users", columnInfo3, null, "30");
		return "index";
	}
}
