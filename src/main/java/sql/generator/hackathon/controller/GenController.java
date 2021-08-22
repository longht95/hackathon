package sql.generator.hackathon.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import sql.generator.hackathon.model.TableSQL;
import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.InfoDisplayScreen;
import sql.generator.hackathon.service.ExecuteDBSQLServer;
import sql.generator.hackathon.service.ServiceDatabase;

@Controller
public class GenController {
	//Save the upload file to this folder
	
	@Autowired
	ServiceDatabase serviceDatabase;
	
	@Autowired
	ExecuteDBSQLServer executeDBServer;
	
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
	
	@PostMapping("/upload")
	public ModelAndView singleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("inputFile") String input) throws IOException {
		ModelAndView model = new ModelAndView("input");
		String result = new BufferedReader(new InputStreamReader(file.getInputStream())).lines().collect(Collectors.joining("\n"));
		System.out.println(result);
		if (result.isEmpty()) {
			model.addObject("query", input);
		} else {
			model.addObject("query", result);
		}
		
		
		
		return model;
	}
	
	@GetMapping(value = "/selectTable")
	public @ResponseBody String selectTable(@RequestParam String tableName) throws Exception {
		System.out.println("Select Table");
		System.out.println(tableName);
		ObjectMapper mapper = new ObjectMapper();
		TableSQL tableSQL = new TableSQL();
		tableSQL.tableName = "okk";
		tableSQL.alias = "xzcxz";
      		ModelAndView model = new ModelAndView("input");
		List<List<String>> tien = new ArrayList<>();
		List<String> longa = new ArrayList<String>();
		longa.add("1234");
		longa.add("12345");
		longa.add("123456");
		longa.add("12341");
		List<String> longab = new ArrayList<String>();
		longab.add("a");
		longab.add("b");
		longab.add("c");
		longab.add("d");
		tien.add(longab);
		tien.add(longa);
		
		
        List<List<String>> listData = executeDBServer.getListData(tableName);
        List<String> listColumn = executeDBServer.getListColumn("admindb", tableName);
        listData.add(0,listColumn);
        
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(listData);
        String json1 = mapper.writeValueAsString(listColumn);
        System.out.println(json);
        System.out.println(json1);
		
		return mapper.writeValueAsString(json);
	}
	
	

}
