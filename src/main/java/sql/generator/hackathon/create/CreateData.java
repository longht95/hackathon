package sql.generator.hackathon.create;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
			System.out.println("---- Show sorted list ----");
			System.out.println(t.toString());
		}
		
		// Check all condition in mapValOfColumn With key is tableName.columName
		// Calculator valid value for column
		// Key = tableName.colName, Value = 
//		Map<String, String[]> mapValOfColumn = calValidValueOfColumn(mapValOfColumn);
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
	private Map<String, List<Cond>> caValidValueOfColumn(Map<String, List<String[]>> mapValOfColumn) {
		Map<String, List<Cond>> m = new HashMap<>();
		for (Map.Entry<String, List<String[]>> entry : mapValOfColumn.entrySet()) {
			String key = entry.getKey();
			
			// Get data type of column?
			// a Trung
			// char
			// number [p,s] OR [p]
			// date
			String type = "char";
			
			// operator, value
			List<String[]> list = entry.getValue();
			int sz = list.size();
			boolean flgNext = false;
			List<Cond> tmpVal = new ArrayList<>();
			
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
					try {
						addValueToColWithComparationsOperator(type, cur, tmpVal);
					} catch (ParseException e) {
						e.printStackTrace();
						System.out.println("Error parse from String to (Date OR Number) !");
					}
					break;
				case ">=":
					try {
						addValueToColWithComparationsOperator(type, cur, tmpVal);
					} catch (ParseException e) {
						e.printStackTrace();
						System.out.println("Error parse from String to (Date OR Number) !");
					}
					break;
				case "<":
					try {
						addValueToColWithComparationsOperator(type, cur, tmpVal);
					} catch (ParseException e) {
						e.printStackTrace();
						System.out.println("Error parse from String to (Date OR Number) !");
					}
					break;
				case ">":
					try {
						addValueToColWithComparationsOperator(type, cur, tmpVal);
					} catch (ParseException e) {
						e.printStackTrace();
						System.out.println("Error parse from String to (Date OR Number) !");
					}
					break;
				case "NOT IN":
					addValueToColWithNotInOperator(cur, tmpVal);
					break;
				case "<>":
					addValueToColWithDifferentOperator(cur, tmpVal);
				case "!=":
					break;
				default:
					System.out.println("Not valid case!");
					assert(false);
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
	public void addValueToColWithInOperator(String[] cur, List<Cond> values) {
		// Value of IN ("123", "456")
		int lenValue = cur[1].length();
		
		// Just get "123","123","123"
		String[] val = cur[1].substring(1, lenValue - 1).split(",");
		for (int i = 0; i < val.length; ++i) {
			// Remove \" \" just get 123
			values.add(new Cond("=", removeSpecifyCharacter("\"'", val[i])));
		}
	}
	
	/**
	 * Execute add value for column with operator NOT IN
	 * @param String[] cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] exactly value can save 
	 */
	public void addValueToColWithNotInOperator(String[] cur, List<Cond> values) {
		// Value of IN ("123", "456")
		int lenValue = cur[1].length();
		
		// Just get "123","123","123"
		String[] val = cur[1].substring(1, lenValue - 1).split(",");
		for (int i = 0; i < val.length; ++i) {
			// Remove \" \" just get 123
			String v = removeSpecifyCharacter("\"'", val[i]);
			Cond obj = new Cond("!=", v);
			if (values.contains(obj)) {
				values.remove(obj);
			}
		}
	}
	
	/**
	 * Execute add value for column with operator <>, !=
	 * @param String[] cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] exactly value can save 
	 */
	public void addValueToColWithDifferentOperator(String[] cur, List<Cond> values) {
		String v = removeSpecifyCharacter("\"'", cur[1]);
		Cond obj = new Cond("!=", v);
		if (values.contains(obj)) {
			values.remove(obj);
		}
	}
	
	
	
	/**
	 * Execute add value for column with operator <=, >=, <, >
	 * Just apply for data type is DATE OR NUMBER
	 * @param data type of value. (date, number, char)
	 * @param String[] cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] valid value can save 
	 * @throws ParseException 
	 */
	public void addValueToColWithComparationsOperator(String type, String[] cur, 
			List<Cond> values) throws ParseException {
		int sz = values.size();
		String operator = cur[0];
		
		// Convert to date when type = date
		if (type.equals("date")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date curD = sdf.parse(cur[1]);
			for (int i = 0; i < sz; ++i) {
				if (type.equals("date")) {
					Date ld = sdf.parse(values.get(i).value);
					Cond cond = new Cond("", sdf.format(ld));
					
					// Just remove all element > curD
					if (operator.equals("<=")) {
						if (ld.compareTo(curD) > 0) {
							values.remove(cond);
						} else {
							break;
						}
					// Just remove all element > curD
					} else if (operator.equals(">=")) {
						if (ld.compareTo(curD) < 0) {
							values.remove(cond);
						} else {
							break;
						}
					// Just remove all element > curD
					} else if (operator.equals("<")) {
						if (ld.compareTo(curD) >= 0) {
							values.remove(cond);
						} else {
							break;
						}
					} else if (operator.equals(">")) {
						if (ld.compareTo(curD) <= 0) {
							values.remove(cond);
						} else {
							break;
						}
					} else {
						// TODO
					}
					
				}
			}
		} else if (type.equals("number")) {
			// Convert to long
			long curV = Long.parseLong(cur[1]);
			
			for (int i = 0; i < sz; ++i) {
				long innerV = Long.parseLong(values.get(i).value);
				Cond cond = new Cond("", values.get(i).value);
				
				// Search in list to remove add value > this value;
				if (operator.equals("<=")) {
					if (innerV > curV) {
						values.remove(cond);
					} else {
						break;
					}
				// Search in list to remove add value < this value;
				} else if (operator.equals(">=")) {
					if (innerV < curV) {
						values.remove(cond);
					} else {
						break;
					}
				} else if (operator.equals("<")) {
					if (innerV >= curV) {
						values.remove(cond);
					} else {
						break;
					}
				} else if (operator.equals(">")) {
					if (innerV <= curV) {
						values.remove(cond);
					} else {
						break;
					}
				} else {
					// TODO
					// Other operator
				}
			}
		} else {
			// TODO
			// Other data type?
		}
		
		// Add new element
		// With character "\'
		values.add(new Cond(operator, cur[1]));
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
	
	class Cond {
		public String operator;
		public String value;
		
		public Cond() {
			
		}
		
		public Cond(String operator, String value) {
			this.operator = operator;
			this.value = value;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Cond)) {
				return false;
			}
			Cond obj = (Cond) o;
			return this.value.equals(obj.value);
		}
		
		@Override
		public int hashCode() {
			return operator.hashCode() + value.hashCode() * 31;
		}
	}
}
