package sql.generator.hackathon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import sql.generator.hackathon.create.main.TestInsertDB;

@Controller
public class TestCreateController {

	@RequestMapping(value = "/testCreate")
	public String testCreate() {
		// Test insert H2 with native query
		TestInsertDB testInsertDb = new TestInsertDB();
		testInsertDb.create();	
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
