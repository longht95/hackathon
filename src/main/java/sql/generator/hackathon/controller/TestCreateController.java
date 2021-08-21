package sql.generator.hackathon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import sql.generator.hackathon.create.main.TestInsertDB;
import sql.generator.hackathon.create.main.TestReadParse;

@Controller
public class TestCreateController {

	@RequestMapping(value = "/testCreate")
	public String testCreate() {
		// Test insert H2 with native query
//		TestInsertDB testInsertDb = new TestInsertDB();
//		testInsertDb.create();
		TestReadParse.main(null);
		
		return "index";
	}
	
	@RequestMapping(value = "/showTable")
	public String showTable() {
		// Test insert H2 with native query
		TestInsertDB testInsertDb = new TestInsertDB();
		testInsertDb.show();	
		return "index";
	}
}
