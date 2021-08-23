package sql.generator.hackathon.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.sf.jsqlparser.JSQLParserException;
import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.InfoDisplayScreen;
import sql.generator.hackathon.model.TableSQL;
import sql.generator.hackathon.model.ViewQuery;
import sql.generator.hackathon.service.ExecuteDBSQLServer;
import sql.generator.hackathon.service.ServiceDatabase;
import sql.generator.hackathon.service.ServiceParse;

@Controller
public class GenController {
	//Save the upload file to this folder
	
	@Autowired
	ServiceDatabase serviceDatabase;
	
	@Autowired
	ExecuteDBSQLServer executeDBServer;
	
	@Autowired
	private ServiceParse serviceParse;
	
	@RequestMapping(value = "/zxczxc")
	public String index123() throws Exception {
//		serviceDatabase.showTables();
		executeDBServer.connectDB("com.mysql.cj.jdbc.Driver", "admindb", "root", "Root123!");
		
		List<String> lstTableName = Arrays.asList("users", "class");
		Map<String, List<ColumnInfo>> inforTable = executeDBServer.getInforTable("admindb", lstTableName);
		for (Map.Entry<String, List<ColumnInfo>> entry : inforTable.entrySet()) {
			System.out.println("Key: " + entry.getKey());
	        List<ColumnInfo> lstColInfo = entry.getValue();
	        for (ColumnInfo columnInfo: lstColInfo) {
		        System.out.println("		name: " + columnInfo.getName());
		        System.out.println("		typeName: " + columnInfo.getTypeName());
		        System.out.println("		typeValue: " + columnInfo.getTypeValue());
		        System.out.println("		isNull: " + columnInfo.getIsNull());
		        System.out.println("		isPrimarykey: " + columnInfo.getIsPrimarykey());
		        System.out.println("		isForeignKey: " + columnInfo.getIsForeignKey());
		        System.out.println("		unique: " + columnInfo.getUnique());
		        System.out.println("----------------------------------");
	        }
	    }
		
//		InfoDisplayScreen infoDisplayScreen = executeDBServer.getDataDisplay("admindb", "users");
//		System.out.println("infoDisplayScreen: " + infoDisplayScreen);
		
		ColumnInfo columnInfo4 = new ColumnInfo();
		columnInfo4.setName("user_name");
		columnInfo4.setTypeName("varchar");
		System.out.println(executeDBServer.isUniqueValue("class", columnInfo4, "test3"));
		ColumnInfo columnInfo = new ColumnInfo();
		columnInfo.setName("user_name");
		columnInfo.setTypeName("varchar");
		Map<String, String> mapUnique = executeDBServer.genUniqueCol("admindb", "class", columnInfo, "test3");
		ColumnInfo columnInfo1 = new ColumnInfo();
		columnInfo1.setName("id");
		columnInfo1.setTypeName("bigint");
		Map<String, String> mapUnique1 = executeDBServer.genUniqueCol("admindb", "users", columnInfo1, "test3");
		ColumnInfo columnInfo2 = new ColumnInfo();
		columnInfo2.setName("email");
		columnInfo2.setTypeName("varchar");
		ColumnInfo columnInfo3 = new ColumnInfo();
		columnInfo3.setName("age");
		columnInfo3.setTypeName("int");
		List<String> aaa = executeDBServer.genListUniqueVal("users", columnInfo3, null, "30");
		
		return "index";
		
	}
	
	@RequestMapping(value = "/input")
	public String input() throws Exception {
		serviceDatabase.showTables();
		return "input";
	}
	
	@GetMapping(value = "/")
	public String index() {
		return "index";
	}
	
	@PostMapping("/uploadFile")
	public @ResponseBody String singleFileUpload(@RequestParam("file") MultipartFile file) throws IOException, JSQLParserException {
		String query = new BufferedReader(new InputStreamReader(file.getInputStream())).lines().collect(Collectors.joining("\n"));
		ViewQuery viewQuery = ViewQuery.builder().listTable(serviceParse.getListTableByStatement(query)).query(query).build();
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(viewQuery);
	}
	
	@GetMapping(value = "/updateQuery")      
	public @ResponseBody String updateQuery(@RequestParam String query) throws JSQLParserException, JsonProcessingException {
		ViewQuery viewQuery = ViewQuery.builder().listTable(serviceParse.getListTableByStatement(query)).query(query).build();
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(viewQuery);
	}
	
	@GetMapping(value = "/selectTable")      
	public @ResponseBody String selectTable(@RequestParam String tableName) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
        InfoDisplayScreen infoDisplayScreen = executeDBServer.getDataDisplay("admindb", tableName);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper.writeValueAsString(infoDisplayScreen);
		
	}
	
	

}
