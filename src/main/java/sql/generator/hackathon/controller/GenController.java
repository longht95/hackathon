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
	ServiceParse serviceParse;
	
	@RequestMapping(value = "/")
	public String index() throws Exception {
//		serviceDatabase.showTables();
		executeDBServer.connectDB("admindb", "root", "");
		
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
		
		InfoDisplayScreen infoDisplayScreen = executeDBServer.getDataDisplay("admindb", "users");
		System.out.println("infoDisplayScreen: " + infoDisplayScreen);
		
		return "index";
		
	}
	
	@RequestMapping(value = "/input")
	public String input() throws Exception {
		serviceDatabase.showTables();
		return "input";
	}
	
	@PostMapping("/uploadFile")
	public @ResponseBody String singleFileUpload(@RequestParam("file") MultipartFile file) throws IOException, JSQLParserException {
		String query = new BufferedReader(new InputStreamReader(file.getInputStream())).lines().collect(Collectors.joining("\n"));
		List<String> listTable = serviceParse.getListTableByStatement(query);
		System.out.println(listTable.toString());
		// ko nghe gi a
		//k nge, cho nay call tra ve list table ok roi, gio e tao 1 object de dua' 2 thong tin
		// list table, cau query de view len textarea. hieu ko
		ObjectMapper mapper = new ObjectMapper();
		// cho nay e tra ve json
		String json = mapper.writeValueAsString(listTable);
		return mapper.writeValueAsString(json);
	}
	
	@GetMapping(value = "/updateQuery")      
	public @ResponseBody String updateQuery(@RequestParam String query) throws JSQLParserException, JsonProcessingException {
		List<String> listTable = serviceParse.getListTableByStatement(query);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(listTable);
		return mapper.writeValueAsString(json);
	}
	
	@GetMapping(value = "/selectTable")      
	public @ResponseBody String selectTable(@RequestParam String tableName) throws Exception {
		System.out.println("Select Table");
		System.out.println(tableName);
		ObjectMapper mapper = new ObjectMapper();
		TableSQL tableSQL = new TableSQL();
		
        
        InfoDisplayScreen infoDisplayScreen = executeDBServer.getDataDisplay("admindb", tableName);
       
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(infoDisplayScreen);

		
		return mapper.writeValueAsString(json);
		
	}
	
	

}
