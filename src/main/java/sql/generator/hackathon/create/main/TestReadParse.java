package sql.generator.hackathon.create.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sql.generator.hackathon.create.CreateData;
import sql.generator.hackathon.model.ConditionTest;
import sql.generator.hackathon.model.TableSQL;

public class TestReadParse {

	public static List<TableSQL> tables = new ArrayList<>();
	public static HashMap<String, List<String>> keys = new HashMap<>();
	
	public static void readObjectTest(String name) {
		// Read file test xml
		try {
			Resource resource = new ClassPathResource("Test_parse" + name + ".xml");
			
			// creating a constructor of file class and parsing an XML file
			File file = resource.getFile();
			
			// an instance of factory that gives a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			
			// an instance of builder to parse the specified xml file
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			
			NodeList nodeList = doc.getElementsByTagName("table");
			
			
			// nodeList is not iterable, so we are using for loop
			for (int itr = 0; itr < nodeList.getLength(); itr++) {
				Node node = nodeList.item(itr);
				TableSQL tableSQL = new TableSQL();
				
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node;
					String tableName = eElement.getElementsByTagName("name").item(0).getTextContent();
					String alias = eElement.getElementsByTagName("alias").item(0).getTextContent();
					
					tableSQL.tableName = tableName;
					tableSQL.alias = alias;
					
					NodeList conditionList = eElement.getElementsByTagName("condition");
					
					List<ConditionTest> listCond = new ArrayList<>();
					for (int i = 0; i < conditionList.getLength(); ++i) {
						Node condNode = conditionList.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element innerElement = (Element) condNode;
							
							String left = innerElement.getElementsByTagName("left").item(0).getTextContent();
							String operator = innerElement.getElementsByTagName("operator").item(0).getTextContent();
							operator = operator.substring(1, operator.length() - 1);
							String right = innerElement.getElementsByTagName("right").item(0).getTextContent();
							 
							ConditionTest condTest = new ConditionTest(left, operator, right);
							if (innerElement.getElementsByTagName("listRight").item(0).getTextContent() != null) {
								String[] sp = innerElement.getElementsByTagName("listRight").item(0).getTextContent().split(",");
								List<String> listRight = new ArrayList<>();
								for (int k = 0; k < sp.length; ++k) {
									listRight.add(sp[k]);
								}
								condTest.listRight = listRight;
							}
							listCond.add(condTest);
						}
					}
					tableSQL.condition = listCond;
				}
				
				tables.add(tableSQL);
			}
			
			// Read key
			Resource resource2 = new ClassPathResource("key" + name + ".xml");
			
			if (!resource2.exists()) {
				return;
			}
			// creating a constructor of file class and parsing an XML file
			File file2 = resource2.getFile();
			
			// an instance of factory that gives a document builder
			DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
			
			// an instance of builder to parse the specified xml file
			DocumentBuilder db2 = dbf2.newDocumentBuilder();
			Document doc2 = db2.parse(file2);
			doc2.getDocumentElement().normalize();
			
			NodeList key = doc2.getElementsByTagName("key");
			
			
			// nodeList is not iterable, so we are using for loop
			for (int itr = 0; itr < key.getLength(); itr++) {
				Node node = key.item(itr);
				
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node;
					String nameKey = eElement.getElementsByTagName("name").item(0).getTextContent();
					String mapping = eElement.getElementsByTagName("mapping").item(0).getTextContent();
					
					List<String> valMap = new ArrayList<>();
					keys.put(nameKey, valMap);
					
					String[] sp = mapping.split(",");
					for (int j = 0; j < sp.length; ++j) {
						valMap.add(sp[j]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
