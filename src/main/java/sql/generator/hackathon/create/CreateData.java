package sql.generator.hackathon.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sql.generator.hackathon.model.ConditionTest;
import sql.generator.hackathon.model.TableSQL;

public class CreateData {

	// Priority of operator
	static Map<String, Integer> priorityOfOperator = new HashMap<>();
	{
		priorityOfOperator.put("=", 1);
		priorityOfOperator.put("IN", 2);
		priorityOfOperator.put("<=", 3);
		priorityOfOperator.put(">=", 4);
		priorityOfOperator.put("<", 5);
		priorityOfOperator.put(">", 6);
		priorityOfOperator.put("NOT IN", 7);
		priorityOfOperator.put("!=", 8);
		priorityOfOperator.put("<>", 9);
	}
	
	// Save Key exists
	private List<TableSQL> tables;
	private Map<String, List<String>> keys;
	
	public CreateData(List<TableSQL> tables, Map<String, List<String>> keys) {
		this.tables = tables;
		this.keys = keys;
	}
	
	public void create() {
		int sz = tables.size();
		for (int i = 0; i < sz; ++i) {
			exeEachTable(tables.get(i));
		}
	}
	
	private void exeEachTable(TableSQL table) {
		
		String tableName = table.tableName;
		
		// Save cur value of column table
		// Key = table.column, Value = [operator, value] in Where
		Map<String, List<String[]>> mapValOfColumn = new HashMap<>();
		
		// Read list condition
		List<ConditionTest> conditions = table.condition;
		
		// Read all condition.
		int szCond = conditions.size();
		for (int i = 0; i < szCond; ++i) {
			if (!conditions.get(i).right.startsWith("KEY")) {
				// Normal case
				readValueForColumn(tableName, conditions.get(i), mapValOfColumn);
			}
		}
		
		// Calculator Priority of condition.
		for (Map.Entry<String, List<String[]>> entry : mapValOfColumn.entrySet()) {
			List<String[]> t = entry.getValue();
			
			// Sort list priority execute operator
			Collections.sort(t, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					return Integer.parseInt(o1[2]) - Integer.parseInt(o2[2]);
				}
			});
		}
		
		// Check all condition in mapValOfColumn With key is tableName.columName
		// Calculator valid value for column
		// Key = tableName.colName, Value = 
//		Map<String, String[]> mapValOfColumn = calExactlyValueOfColumn(mapValOfColumn);
	}
	
	/**
	 * Read value to mapValOfColumn to execute bridging case (Bắc cầu)
	 * @param condition
	 * @param mapValueOfColumn
	 */
	private void readValueForColumn(String tableName, ConditionTest condition, 
			Map<String, List<String[]>> mapValOfColumn) {
		String col = condition.left;
		String operator = condition.operator;
		String val = condition.right;
		
		// Check has alias => save, if normal case no save.
		// Save to execute with bridging case (Bắc cầu)
		if (!hasAliasName(val)) {
			return;
		}
		
		// TableName.columnName
		String fullColName = tableName + "." + getTableAndColName(col)[1];

		// Add all condition.
		List<String[]> t;
		if (mapValOfColumn.containsKey(fullColName)) {
			t = mapValOfColumn.get(fullColName);
		} else {
			// New condition
			t = new ArrayList<>();
			mapValOfColumn.put(fullColName, t);
		}
		t.add(new String[] {operator, val, String.valueOf(priorityOfOperator.get(operator))});
	}
	
	/**
	 * Check column has alias Name?
	 * @param column (table.colName || colName)
	 * @return true has aliasName, otherwise return false
	 */
	private boolean hasAliasName(String column) {
		if (column.indexOf('.') == -1) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Get table name and column name
	 * @param column table.colName
	 * @return String[2], String[0] = tableName, String[1] = columnName   
	 */
	private String[] getTableAndColName(String input) {
		String[] res = input.split("\\.");
		return res;
	}
	
	/**
	 * Calculator exactly value for column
	 * @return Map<String, String> Key = tableName.colName, Value = Valid value.
	 */
	private Map<String, String[]> calExactlyValueOfColumn(Map<String, List<String[]>> mapValOfColumn) {
		Map<String, String[]> m = new HashMap<>();
		for (Map.Entry<String, List<String[]>> entry : mapValOfColumn.entrySet()) {
			String key = entry.getKey();
			
			// operator, value
			List<String[]> list = entry.getValue();
			int sz = list.size();
			boolean flgNext = false;
			List<String[]> tmpVal = new ArrayList<>();
			
			for (int i = 0; i < sz; ++i) {
				// String[operator, value, priority] 
				String[] cur = list.get(i);
				String operator = cur[0];
				
				// When priority = 1 stop here
				if (operator.equals("=")) {
					flgNext = true;
					break;
				}
				
//				priorityOfOperator.put("IN", 2);
//				priorityOfOperator.put("<=", 3);
//				priorityOfOperator.put(">=", 4);
//				priorityOfOperator.put("<", 5);
//				priorityOfOperator.put(">", 6);
//				priorityOfOperator.put("NOT IN", 7);
//				priorityOfOperator.put("!=", 8);
//				priorityOfOperator.put("<>", 9);
				
				switch (operator) {
				case "IN":
					addValueToColWithInOperator(cur, tmpVal);
					break;
				case "<=":
					
					break;
				case ">=":
					break;
				case "<":
					break;
				case ">":
					break;
				case "NOT IN":
					break;
				case "<>":
				case "!=":
					break;
				default:
					assert(false);
					System.out.println("Not valid case!");
					break;
				}
			}
			
			if(flgNext) {
				continue;
			}
		}
		return m;
	}
	
	/**
	 * Execute add value for column with operator IN
	 * @param String[] cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] exactly value can save 
	 */
	public void addValueToColWithInOperator(String[] cur, List<String[]> values) {
		// Value of IN ("123", "456")
		int lenValue = cur[1].length();
		
		// Just get "123","123","123"
		String[] val = cur[1].substring(1, lenValue - 1).split(",");
		for (int i = 0; i < val.length; ++i) {
			// Remove \" \" just get 123
			values.add(new String[] {"=", removeSpecifyCharacter("\"'", val[i])});
		}
	}
	
	/**
	 * Execute add value for column with operator NOT IN
	 * @param String[] cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] exactly value can save 
	 */
	public void addValueToColWithNotInOperator(String[] cur, List<String[]> values) {
		// Value of IN ("123", "456")
		int lenValue = cur[1].length();
		
		// Just get "123","123","123"
		String[] val = cur[1].substring(1, lenValue - 1).split(",");
		for (int i = 0; i < val.length; ++i) {
			// Remove \" \" just get 123
			String v = removeSpecifyCharacter("\"'", val[i]);
			if (values.contains(v)) {
				values.remove(v);
			}
		}
	}
	
	/**
	 * Execute add value for column with operator <>, !=
	 * @param String[] cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] exactly value can save 
	 */
	public void addValueToColWithDifferentOperator(String[] cur, List<String> values) {
		String v = removeSpecifyCharacter("\"'", cur[1]);
		if (values.contains(v)) {
			values.remove(v);
		}
	}
	
	/**
	 * 
	 * @param type
	 * @param len (When type == date no save)
	 * @return
	 */
	private List<String> genValueForCol(String type, int len) {
		List<String> res = new ArrayList<>();
		
		// 3 case
		// Data for date
		// Data for number
		// Data for char
		if (type.equals("date")) {
			
		} else if (type.equals("number")) {
			
		} else if (type.equals("char")) {
			
		} else {
			// TODO
			// Maybe other data type?
			assert(false);
		}
		return res;
	}
	
	
	
	/**
	 * Remove all specify character in string origin
	 * @param specifyStr String of specify character to remove.
	 * @param origin
	 * @return String without character in specifyStr.
	 */
	private String removeSpecifyCharacter(String specifyStr, String origin) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < origin.length(); ++i) {
			if (!specifyStr.contains("" + origin.charAt(i))) {
				sb.append(origin.charAt(i));
			}
		}
		return sb.toString();
	}
}
