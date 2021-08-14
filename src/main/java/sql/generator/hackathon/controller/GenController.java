package sql.generator.hackathon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import sql.generator.hackathon.service.ServiceDatabase;

@Controller
public class GenController {
	@Autowired
	ServiceDatabase serviceDatabase;
	
	@RequestMapping(value = "/")
	public String index() throws Exception {
		serviceDatabase.showTables();
		return "index";
	}
}
