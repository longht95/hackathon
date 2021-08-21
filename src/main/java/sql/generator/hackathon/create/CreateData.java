package sql.generator.hackathon.create;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.Cond;
import sql.generator.hackathon.model.ConditionTest;
import sql.generator.hackathon.model.NodeColumn;
import sql.generator.hackathon.model.TableSQL;
import sql.generator.hackathon.service.CreateService;

public class CreateData {

	// Load resource data example
	private static Resource resource = new ClassPathResource("/example_data.properties");
	private static HashMap<String, String> dataExamples = new HashMap<>();

	// Priority of operator
	private static Map<String, Integer> priorityOfOperator = new HashMap<>();
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

	private CreateService createService = new CreateService();;
	
	// Save mapping table.column mapping with other table.column
	private Map<String, Set<Cond>> columnMap = new HashMap<>();

	// Save Key exists
	private List<TableSQL> tables = new ArrayList<>();
	private Map<String, List<String>> keys = new HashMap<>();

	// alias.Name => <tableName.columnName, operator>
	private Map<String, String[]> infoCol = new HashMap<>();

	// Data current values
	// TableName => List ColumnInfo
	private Map<String, List<ColumnInfo>> tableData = new HashMap<>();

	// Calculator valid values in Where.
	// Key = tableName.colName => Value = List<Cond> => Cond
	private Map<String, List<Cond>> validValuesForColumn = new HashMap<>();
	
	// Key = tableName.colName => List data use operator (NOT IN, !=, <>)
	private Map<String, List<String>> valueInValidOfColumn = new HashMap<>();
	
	// Key tableName.colName
	private Map<String, String> lastEndValidValue = new HashMap<>();
	
	// Tmp will remove after!
	private String dataType = "date";
	
	public CreateData(List<TableSQL> tables, Map<String, List<String>> keys) {
		this.tables = tables;
		this.keys = keys;
	}

	public void create() {
		try {
			// Load data example
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			Set<String> keys = props.stringPropertyNames();
			for (String key : keys) {
				dataExamples.put(key, props.getProperty(key));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		int sz = tables.size();
		for (int i = 0; i < sz; ++i) {
			exeEachTable(tables.get(i));
		}

		// Get column Mapping (Key1 -> Key2, Key2 -> Key) in keys
		Map<String, Set<String>> colMapping = getMappingColumn();
		// Get all mapping of 1 column (Key1 -> key2,key3,key4)
		getAllMappingColum(colMapping);
		
		// execute calculator for mapping key.
		exeCalcForMappingKey();
		
		// Create last data for column!
		createLastData();
		
		// Insert each table
		for (Map.Entry<String, List<ColumnInfo>> e : tableData.entrySet()) {
			// Call JDBC execute insert data to table!
			createService.insert(e.getKey(), e.getValue());
		}
	}
	
	/**
	 * Execute update data from client!
	 * @param dataClient String = tableName => List<ColumnInfo>
	 */
	public void update(Map<String, List<ColumnInfo>> dataClient) {
		// Get data from client?
		// Check current data valid?
		
		// use columnMap
		// Save mapping table.column mapping with other table.column
		// Calculator valid values in Where.
		// Key = tableName.colName => Value = List<Cond> => Cond
//		private Map<String, List<Cond>> validValuesForColumn = new HashMap<>();
		
		// Key = tableName.colName => List data use operator (NOT IN, !=, <>)
//		private Map<String, List<String>> valueInValidOfColumn = new HashMap<>();
		
		// Check this condition valid?
		for (Map.Entry<String, List<ColumnInfo>> e : dataClient.entrySet()) {
			String tableName = e.getKey();
			
			// Array flag check when all column in flg = true => insert, otherwise will update
			
			// Execute update
			for (ColumnInfo curCol : e.getValue()) {
			
				// Check valid value in ValidValues

				// Check valid value for all mapping
				
				// Check value in value different with value of (NOT IN, !=, <>)
				
				// Add to flg Check
			}
			
			// When insert just call method insert and input List<ColumnInfo>
			
			// When update -> List<ColumnInfo> can Update,
			// List<ColumnInfo> condition of primary key in this table!
		}
	}

	private void exeEachTable(TableSQL table) {

		String tableName = table.tableName;
		
		// Init data for table
		tableData.put(tableName, new ArrayList<>());

		// Save cur value of column table
		// Key = table.column, Value = [operator, value, priority] in Where
		Map<String, List<String[]>> mapValOfColumn = new HashMap<>();

		// Read list condition
		List<ConditionTest> conditions = table.condition;

		// Read all condition.
		int szCond = conditions.size();
		for (int i = 0; i < szCond; ++i) {
			if (!conditions.get(i).right.startsWith("KEY")) {
				// Normal case
				readValueForColumn(tableName, conditions.get(i), mapValOfColumn);
				continue;
			}

			String left = conditions.get(i).left;
			String operator = conditions.get(i).operator;
			String right = conditions.get(i).right;

			// Execute find valid value column = KEY
			// Find mapping = KEY of all column
			// Save to columnMap
			if (!keys.containsKey(right)) {
				System.out.println("Error not found KEY Mapping!");
				return;
			}

			// TODO when columnname is alias?
			// Put aliasTable.aliasName => [tableName.columnName]
			// Save for calculator all mapping
			String[] sp = getTableAndColName(left);
			infoCol.put(left, new String[] { tableName + "." + sp[1], operator });
			lastEndValidValue.put(tableName + "." + sp[1], "");
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
		// Key = tableName.colName, Value = List<Cond>
		validValuesForColumn = calValidValueOfColumn(mapValOfColumn);
	}

	/**
	 * Read value to mapValOfColumn to execute bridging case (Bắc cầu)
	 * 
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
//		if (!hasAliasName(col)) { //hasAliasName(val)
//			return;
//		}

		// TableName.columnName
		String fullColName = tableName + "." + getTableAndColName(col)[1];

		lastEndValidValue.put(fullColName, "");
		
		// Add all condition.
		List<String[]> t;
		if (mapValOfColumn.containsKey(fullColName)) {
			t = mapValOfColumn.get(fullColName);
		} else {
			// New condition
			t = new ArrayList<>();
			mapValOfColumn.put(fullColName, t);
		}
		if (!(operator.equals("NOT IN") || operator.equals("IN"))) {
			t.add(new String[] { operator, val, String.valueOf(priorityOfOperator.get(operator)) });
		} else {
			List<String> listRight = condition.listRight;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < listRight.size(); ++i) {
				sb.append(listRight.get(i));
				if (i != listRight.size() - 1) {
					sb.append(",");
				}
			}
			t.add(new String[] { operator, sb.toString(), String.valueOf(priorityOfOperator.get(operator)) });
		}
	}

	/**
	 * Check column has alias Name?
	 * 
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
	 * 
	 * @param column table.colName || colName
	 * @return String[2], String[0] = tableName, String[1] = columnName
	 */
	private String[] getTableAndColName(String input) {
		String[] res = new String[2];
		if (input.indexOf(".") != -1) {
			res = input.split("\\.");
			;
		} else {
			res[1] = input;
		}
		return res;
	}

	/**
	 * Calculator valid value for column
	 * Use for condition in where
	 * @return Map<String, List<Cond> Key = tableName.colName, Value = Valid value.
	 */
	private Map<String, List<Cond>> calValidValueOfColumn(Map<String, List<String[]>> mapValOfColumn) {
		Map<String, List<Cond>> m = new HashMap<>();
		for (Map.Entry<String, List<String[]>> entry : mapValOfColumn.entrySet()) {
			String key = entry.getKey();

			// Get data type of column?
			// a Trung
			// char
			// number [p,s] OR [p]
			// date
//			String dataType = "number";
			
			// Get dataType of column
			String tableName = getTableAndColName(key)[0];
			String colName = getTableAndColName(key)[1];
			String dataType = createService.getDataTypeOfColumn(createService.getColumInfo(tableName, colName));

			// {operator, value, priority}
			// operator => (=, <>, !=, ..)
			// value => current value in condition where
			// Priority of current operator
			List<String[]> list = entry.getValue();
			int sz = list.size();
			List<Cond> tmpVal = new ArrayList<>();
			List<Cond> listCondIN = new ArrayList<>();
			boolean flgCheckCondIN = false;
			
			for (int i = 0; i < sz; ++i) {
				// String[operator, value, priority]
				String[] cur = list.get(i);
				String operator = cur[0];

				// When priority = 1 stop here
				if (operator.equals("=")) {
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
					flgCheckCondIN = true;
					addValueToColWithInOperator(cur, tmpVal);
					break;
				case "<=":
					try {
						addValueToColWithComparationsOperator(dataType, cur, tmpVal);
					} catch (ParseException e) {
						e.printStackTrace();
						System.out.println("Error parse from String to (Date OR Number) !");
					}
					break;
				case ">=":
					try {
						addValueToColWithComparationsOperator(dataType, cur, tmpVal);
					} catch (ParseException e) {
						e.printStackTrace();
						System.out.println("Error parse from String to (Date OR Number) !");
					}
					break;
				case "<":
					try {
						addValueToColWithComparationsOperator(dataType, cur, tmpVal);
					} catch (ParseException e) {
						e.printStackTrace();
						System.out.println("Error parse from String to (Date OR Number) !");
					}
					break;
				case ">":
					try {
						addValueToColWithComparationsOperator(dataType, cur, tmpVal);
					} catch (ParseException e) {
						e.printStackTrace();
						System.out.println("Error parse from String to (Date OR Number) !");
					}
					break;
				case "NOT IN":
					List<String> t;
					if (valueInValidOfColumn.containsKey(key)) {
						t = valueInValidOfColumn.get(key);
					} else {
						t = new ArrayList<>();
						valueInValidOfColumn.put(key, t);
					}
					addValueToColWithNotInOperator(dataType, cur, tmpVal, t);
					break;
				case "<>":
				case "!=":
					List<String> t2;
					if (valueInValidOfColumn.containsKey(key)) {
						t2 = valueInValidOfColumn.get(key);
					} else {
						t2 = new ArrayList<>();
						valueInValidOfColumn.put(key, t2);
					}
					addValueToColWithDifferentOperator(dataType, cur, tmpVal, t2);
					break;
				default:
					System.out.println("Not valid case!");
					assert (false);
					break;
				}
			}

//			// When filter not contains conditions => query invalid
//			if (tmpVal.isEmpty()) {
//				System.out.println("SQL condition invalid!");
//			}
			
			// When has condition IN => get value of IN
			if (flgCheckCondIN) {
				int cnt = 0;
				for (int i = 0; i < tmpVal.size(); ++i) {
					// Just check 1 condition IN pass => SQL valid
					if (cnt > 0) {
						break;
					}
					if (listCondIN.contains(tmpVal.get(i))) {
						cnt++;
					}
				}
				
				if (cnt == 0) {
					System.out.println("SQL condition IN invalid!");
				}
			}
			
			m.put(key, tmpVal);
		}
		return m;
	}

	/**
	 * Execute add value for column with operator IN
	 * 
	 * @param String[]     cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] exactly value can save
	 */
	public List<Cond> addValueToColWithInOperator(String[] cur, List<Cond> values) {
		// Just get "123","123","123"
		String[] val = cur[1].split(",");
		for (int i = 0; i < val.length; ++i) {
			// Remove \" \" just get 123
			values.add(new Cond("=", removeSpecifyCharacter("\"'", val[i])));
		}
		
		List<Cond> res = new ArrayList<>();
		
		// Copy all values to new listCondIN
		for (int i = 0; i < values.size(); ++i) {
			res.add(values.get(i));
		}
		return res;
	}

	/**
	 * Execute add value for column with operator NOT IN
	 * 
	 * @param String[]     cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] exactly value can save
	 */
	public void addValueToColWithNotInOperator(String dataType, String[] cur, List<Cond> values, List<String> valNotIN) {
		// Just get "123","123","123"
		String[] val = cur[1].split(",");
		for (int i = 0; i < val.length; ++i) {
			// Remove \" \" just get 123
			String v = removeSpecifyCharacter("\"'", val[i]);
			
			// add value
			valNotIN.add(v);
			
			// Check in previous condition, and remove it if exists!
			Cond obj = new Cond("!=", v);
			Cond c = null;
			for (int j = 0; j < values.size(); ++j) {
				if (values.get(j).equals(obj)) {
					if (values.get(j).operator.equals("<=") || values.get(j).operator.equals(">=")) {
						c = values.get(j);
						break;
					}
				}
			}
			
			// Remove if contains
			if (values.contains(obj)) {
				// Check when not in mapping with less than or greater than!
				if (c != null) {
					
					Cond tmp = new Cond();
					tmp.operator = c.operator;

					// TODO
					// Get datatype of this column
					
					if (c.operator.equals("<=")) {
						// Decrease - 1
//						values.add(new Cond(c.operator, c.))
						
						if (dataType.equals("number")) {
							tmp.value = genKeyWithTypeNumber(false, c.value);
						} else if (dataType.equals("date")) {
							tmp.value = genKeyWithTypeDate(false, c.value);
						}
					} else if (c.operator.equals(">=")) {
						// Increase + 1
						if (dataType.equals("number")) {
							tmp.value = genKeyWithTypeNumber(true, c.value);
						} else if (dataType.equals("date")) {
							tmp.value = genKeyWithTypeDate(true, c.value);
						}
					}
					values.add(tmp);
				}
				values.remove(obj);
			}
		}
	}

	/**
	 * Execute add value for column with operator <>, !=
	 * 
	 * @param String[]     cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] exactly value can save
	 */
	public void addValueToColWithDifferentOperator(String dataType, String[] cur, List<Cond> values, List<String> valNotIn) {
		String v = removeSpecifyCharacter("\"'", cur[1]);
		
		// Add value to not in table.column!
		valNotIn.add(v);
		// Check in previous condition, and remove it if exists!
		Cond obj = new Cond("!=", v);
		Cond c = null;
		for (int j = 0; j < values.size(); ++j) {
			if (values.get(j).equals(obj)) {
				if (values.get(j).operator.equals("<=") || values.get(j).operator.equals(">=")) {
					c = values.get(j);
					break;
				}
			}
		}
		
		// Check if exists in previous condition then remove it!
		if (values.contains(obj)) {
			
			if (c != null) {

				Cond tmp = new Cond();
				tmp.operator = c.operator;
				
				// TODO
				// Get datatype of this column
				
				if (c.operator.equals("<=")) {
					// Decrease - 1
//					values.add(new Cond(c.operator, c.))
					
					if (dataType.equals("number")) {
						tmp.value = genKeyWithTypeNumber(false, c.value);
					} else if (dataType.equals("date")) {
						tmp.value = genKeyWithTypeDate(false, c.value);
					}
				} else if (c.operator.equals(">=")) {
					// Increase + 1
					if (dataType.equals("number")) {
						tmp.value = genKeyWithTypeNumber(true, c.value);
					} else if (dataType.equals("date")) {
						tmp.value = genKeyWithTypeDate(true, c.value);
					}
				}
				values.add(tmp);
			}
			
			values.remove(obj);
		}
	}

	/**
	 * Execute add value for column with operator <=, >=, <, > Just apply for data
	 * type is DATE OR NUMBER
	 * 
	 * @param data type of value. (date, number, char)
	 * @param String[] cur (Each condition) [operator, value, priority]
	 * @param List<String> String[value] valid value can save
	 * @throws ParseException
	 */
	public void addValueToColWithComparationsOperator(String type, String[] cur, List<Cond> values)
			throws ParseException {
		String operator = cur[0];
		String strVal = cur[1];
		// Convert to date when type = date
		if (type.equals("date")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date curD = sdf.parse(cur[1]);
			
			Queue<Cond> toExploder = new LinkedList<>();
			
			for (int i = 0; i < values.size(); ++i) {
				toExploder.add(values.get(i));
			}
			
			while (!toExploder.isEmpty()) {
				Cond cond = toExploder.poll();
				Date ld = sdf.parse(cond.value);
				
				// Just remove all element > curD
				if (operator.equals("<=")) {
					if (ld.compareTo(curD) > 0) {
						values.remove(cond);
					}
					// Just remove all element > curD
				} else if (operator.equals(">=")) {
					if (ld.compareTo(curD) < 0) {
						values.remove(cond);
					}
					// Just remove all element > curD
				} else if (operator.equals("<")) {
					if (ld.compareTo(curD) >= 0) {
						values.remove(cond);
					}
				} else if (operator.equals(">")) {
					if (ld.compareTo(curD) <= 0) {
						values.remove(cond);
					}
				} else {
					// TODO
				}
			}
			
			// Not operator <= and >=
			if (!(operator.equals("<=") || operator.equals(">="))) {
				Calendar c = Calendar.getInstance();
				c.setTime(curD);
				// current Date + 1
				if (operator.equals(">")) {
					c.add(Calendar.DATE, 1);
					// current Date - 1
				} else {
					c.add(Calendar.DATE, -1);
				}
				curD = c.getTime();
			}
			
			// Convert date time to string
			strVal = sdf.format(curD);
		} else if (type.equals("number")) {
			// Convert to long
			long curV = Long.parseLong(cur[1]);

			Queue<Cond> toExploder = new LinkedList<>();
			
			for (int i = 0; i < values.size(); ++i) {
				toExploder.add(values.get(i));
			}

			while (!toExploder.isEmpty()) {
				Cond cond = toExploder.poll();
				long innerV = Long.parseLong(cond.value);
				
				// Search in list to remove add value > this value;
				if (operator.equals("<=")) {
					if (innerV > curV) {
						values.remove(cond);
					}
					// Search in list to remove add value < this value;
				} else if (operator.equals(">=")) {
					if (innerV < curV) {
						values.remove(cond);
					}
				} else if (operator.equals("<")) {
					if (innerV >= curV) {
						values.remove(cond);
					}
				} else if (operator.equals(">")) {
					if (innerV <= curV) {
						values.remove(cond);
					}
				} else {
					// TODO
					// Other operator
				}
			}
			
			// Not operator <= and >=
			if (!(operator.equals("<=") || operator.equals(">="))) {
				// currentValue - 1
				if (operator.equals("<")) {
					strVal = String.valueOf(curV - 1);
					// currentValue + 1
				} else {
					strVal = String.valueOf(curV + 1);
				}
			}
		} else {
			// TODO
			// Other data type?
		}
		// Add new element
		// With character "\' ?
		// [Operator, current value]
		values.add(new Cond(operator, strVal));
	}

	/**
	 * Read Mapping in keys With each key add 2 mapping
	 * 
	 * @return Map<String, String> Key1 - Key2, Key2 - Key1
	 */
	private Map<String, Set<String>> getMappingColumn() {
		Map<String, Set<String>> m = new HashMap<>();
		for (Map.Entry<String, List<String>> e : keys.entrySet()) {
			List<String> v = e.getValue();
			for (int i = 0; i < v.size(); ++i) {
				Set<String> t;
				if (m.containsKey(v.get(i))) {
					t = m.get(v.get(i));
				} else {
					t = new HashSet<>();
					m.put(v.get(i), t);
				}
				// Add other.
				t.add(v.get(i == 0 ? 1 : 0));
			}
		}
		return m;
	}
	
	/**
	 * Create last data for table with remain condition in there!
	 */
	private void createLastData() {
		for (Map.Entry<String, String> e : lastEndValidValue.entrySet()) {
			String lastVal = e.getValue();
			String fullTableColName = e.getKey();
			
			// Get tableName and colName
			String tableName = getTableAndColName(fullTableColName)[0];
			String colName = getTableAndColName(fullTableColName)[1];
			
			// When last have calculator in mapping
			List<ColumnInfo> l = tableData.get(tableName);
			if (!lastVal.isEmpty()) {
				// New columnInfo
				ColumnInfo columnInfo = new ColumnInfo(colName, lastVal);
				l.add(columnInfo);
			} else {
				// When calculator not in mapping 
				// This case will read last condition remain -> gendata
//				private Map<String, List<Cond>> validValuesForColumn = new HashMap<>();
				
				// Key = tableName.colName => List data use operator (NOT IN, !=, <>)
//				private Map<String, List<String>> valueInValidOfColumn
				
				List<Cond> validVal = validValuesForColumn.get(fullTableColName);
				List<String> invalidVal = valueInValidOfColumn.get(fullTableColName);
				
				// TODO
				// Get len of column;
				// Call tu a Trung
				ColumnInfo colInfo = createService.getColumInfo(tableName, colName); 
				int len = createService.getLengthOfColumn(colInfo);
				
				// TODO
				// Get dataType
				// Call tu a trung
				// String dataType
				String dataType = createService.getDataTypeOfColumn(colInfo);
				
				// Get
				List<String> curValidVal = new ArrayList<>();
				
				if (validVal == null) {
					curValidVal = genAutoKey("", "", dataType, len);
				} else {
					boolean flgEqual = false;
					boolean flgGreater = false;
					boolean flgLess = false;
					
					String valLess = "";
					String valGreater = "";
					
					for (Cond cond : validVal) {
						String operator = cond.operator;
						String val = cond.value;

						switch (operator) {
						case "=":
							flgEqual = true;
							curValidVal.add(val);
							break;
						case "<=":
							flgLess = true;
							valLess = val;
							break;
						case ">=":
							flgGreater = true;
							valGreater = val;
							break;
						}
					}
					
					// Gen auto key
					// May be call method a Trung get primarykey
					// input(tableName.colName) => List<Primary key>
					if (!flgEqual && (flgLess || flgGreater)) {
						curValidVal = genAutoKey(valGreater, valLess, dataType, len);
					}
				}
				
				// Remove
				for (int i = 0; i < curValidVal.size(); ++i) {
					if (invalidVal == null || (!invalidVal.contains(curValidVal.get(i)))) {
						ColumnInfo columnInfo = new ColumnInfo(colName, curValidVal.get(i));
						l.add(columnInfo);
						break;
					}
				}
			}
		}
	}

	/**
	 * Get all mapping for each column With each column will find all related
	 * column. Put data to columnMap variable
	 */
	private void getAllMappingColum(Map<String, Set<String>> columnMapping) {
		for (Map.Entry<String, Set<String>> e : columnMapping.entrySet()) {
			Set<String> mappings = new HashSet<>();
			String curKey = e.getKey();
			for (String val : e.getValue()) {
				Queue<String> toExploder = new LinkedList<>();
				Set<String> visited = new HashSet<>();
				toExploder.add(val);
				visited.add(curKey);
				
				while (!toExploder.isEmpty()) {
					String cur = toExploder.remove();
					if (visited.contains(cur)) {
						continue;
					}
					visited.add(val);
					mappings.add(cur);
					if (columnMapping.containsKey(cur)) {
						for (String t : columnMapping.get(cur)) {
							toExploder.add(t);
						}
					}
				}
			}

			// Save ben luc read list object.
			// Need Set<Cond>
			// KEY ==> aliasTable.aliasColumn => alias
			// VALUE COND {operator, value{KEY}}
			Set<Cond> s = new HashSet<Cond>();

//			columnMap.put(infoCol.get(e.getKey()), set);
			for (String c : mappings) {
				Cond cond = new Cond(infoCol.get(c)[1], infoCol.get(c)[0]);
				s.add(cond);
			}
			columnMap.put(infoCol.get(curKey)[0], s);
		}
	}

	/**
	 * columnMap variable (mapping of column)
	 * validValuesForColumn valid values current of column
	 * @return push value to dataTable
	 */
	private void exeCalcForMappingKey() {
		// table.colName => all column mapping of current table.colName
		// Map<String, Set<Cond>> columnMap;
		
		Set<String> visitedMapping = new HashSet<>();
		
		// table.colName => condition valid
		// Map<String, List<Cond>> validValuesForColumn;
		for (Map.Entry<String, Set<Cond>> e : columnMap.entrySet()) {
			String col = e.getKey();
			String tableName = getTableAndColName(col)[0];
			String colName = getTableAndColName(col)[1];
			
			// calculated this column!
			if (visitedMapping.contains(col)) {
				continue;
			}
			
			// Check current col is not primary key or foriegn key
			// Then create with hand!.
			// If this col isPrimaryKey then get all key this key!
			// if (!isPrimaryKey(col) && !isForignKey(col)
			
			// Get valid value of column
			List<Cond> validV = validValuesForColumn.get(col);
			
			// Call method cua a Trung
			// Number
			// Date
			// char
			ColumnInfo colInfo = createService.getColumInfo(tableName, colName);
			String dataType = createService.getDataTypeOfColumn(colInfo);
			int len = createService.getLengthOfColumn(colInfo);
			
			List<String> validOfCol = new ArrayList<>();
			
			// When size = 1 => use equals(=)
			if (validV == null) {
				// When not validValue for this column => free style this case.
				// Maybe data type, min-len => push default key for this.
				// Get len from a Trung
				validOfCol = genAutoKey("", "", dataType, len);
			} else if (validV.size() == 1) {
				validOfCol.add(validV.get(0).value);
			} else if (validV.size() > 1) {
				boolean flgUseEquals = false;
				boolean flgLess = false;
				boolean flgGreater = false;
				String valLess = "";
				String valGreater = "";
				
				// Get all valid value of current value
				for (int i = 0; i < validV.size(); ++i) {
					if (validV.get(i).operator.equals("=")) {
						flgUseEquals = true;
						validOfCol.add(validV.get(i).value);
					} else if (validV.get(i).operator.equals("<=")) {
						if(!flgUseEquals) {
							valLess = validV.get(i).value;
						}
						flgLess = true;
					} else if (validV.get(i).operator.equals(">=")){
						if(!flgUseEquals) {
							valGreater = validV.get(i).value;
						}
						flgGreater = true;
					}
				}
				
				if (!flgUseEquals && (flgLess || flgGreater)) {
					// gen with limit!
					validOfCol = genAutoKey(valLess, valGreater, dataType, len);
				}
			}
			
			// Save in there!
			// Use DFS confirm this case!
			// BFS not performance!
			Stack<NodeColumn> toExploder = new Stack<>();
			Map<NodeColumn, NodeColumn> parentMap = new HashMap<>();
			
			// Visited
			Set<NodeColumn> visited = new HashSet<>();
			
			// Init 
			// index -> Cond in e.getValue()
			// 0 -> Cond in e.getValue()
			// 0 -> Cond = value => columnName.tableName, operator => <=, >=, >, <, !=
			HashMap<Integer, Cond> l = new HashMap<>(e.getValue().size());
			int i = 0;
			for (Cond conD : e.getValue()) {
				l.put(i, conD);
				++i;
			}
			
			// Init
			i = 0;
			for (; i < validOfCol.size(); ++i) {
				NodeColumn nodeCol = new NodeColumn(col, validOfCol.get(i), 0);
				toExploder.add(nodeCol);
			}
			
			NodeColumn nodeGoal = null;
			
			// Init flagMeet
			boolean[] checkMeet = new boolean[e.getValue().size()];
			i = 0;
			for (; i < e.getValue().size(); ++i) {
				checkMeet[i] = false;
			}
			
			while (!toExploder.isEmpty()) {
				NodeColumn curNode = toExploder.pop();
				if (visited.contains(curNode)) {
					continue;
				}
				
				String tableColName = curNode.tableColumnName;
				String val = curNode.val;
				int index = curNode.index;
				
				// Find goal then stop
				if (index == e.getValue().size()) {
					nodeGoal = curNode;
					break;
				}
				
				// Get next mapping.
				Cond nextCond = l.get(index);
				
				// Remove value generator
				// Just calculator first meet index
				// Valid value will increase
				if (!checkMeet[index]) {
					List<Cond> conditionInWhere = new ArrayList<>();
					// When has condition will remove current 
					if (validValuesForColumn.get(nextCond.value) != null) {
						conditionInWhere = validValuesForColumn.get(nextCond.value);
						validOfCol = calculatorValidValWithColumnCondition(validOfCol, dataType,
								conditionInWhere, null);
					} 
					if (valueInValidOfColumn.containsKey(nextCond.value)) {
						List<String> inValidValue = valueInValidOfColumn.get(nextCond.value);
						validOfCol = calculatorValidValWithColumnCondition(validOfCol, dataType,
								conditionInWhere, inValidValue);
					}
				}
				
				checkMeet[index] = true;
				
				i = 0;
				for (; i < validOfCol.size(); ++i) {
					boolean flgAdd = false;
					switch (nextCond.operator) {
					case "=":
						if (val.equals(validOfCol.get(i))) {
							flgAdd = true;
						}
						break;
					case "<=":
						if (dataType.equals("date")) {
							// date
							Date tmp1 = parseStringToDate(val);
							Date tmp2 = parseStringToDate(validOfCol.get(i));
							if (tmp2.compareTo(tmp1) <= 0) {
								flgAdd = true;
							}
						} else if (dataType.equals("number")) {
							Integer int1 = parseStringToInt(val);
							Integer int2 = parseStringToInt(validOfCol.get(i));
							if (int2 <= int1) {
								flgAdd = true;
							}
						}
						// number
						break;
					case ">=":
						if (dataType.equals("date")) {
							// date
							Date tmp1 = parseStringToDate(val);
							Date tmp2 = parseStringToDate(validOfCol.get(i));
							if (tmp2.compareTo(tmp1) >= 0) {
								flgAdd = true;
							}
						} else if (dataType.equals("number")) {
							Integer int1 = parseStringToInt(val);
							Integer int2 = parseStringToInt(validOfCol.get(i));
							if (int2 >= int1) {
								flgAdd = true;
							}
						}
						break;
					case "<":
						if (dataType.equals("date")) {
							// date
							Date tmp1 = parseStringToDate(val);
							Date tmp2 = parseStringToDate(validOfCol.get(i));
							if (tmp2.compareTo(tmp1) < 0) {
								flgAdd = true;
							}
						} else if (dataType.equals("number")) {
							Integer int1 = parseStringToInt(val);
							Integer int2 = parseStringToInt(validOfCol.get(i));
							if (int2 < int1) {
								flgAdd = true;
							}
						}
						break;
					case ">":
						if (dataType.equals("date")) {
							// date
							Date tmp1 = parseStringToDate(val);
							Date tmp2 = parseStringToDate(validOfCol.get(i));
							if (tmp2.compareTo(tmp1) < 0) {
								flgAdd = true;
							}
						} else if (dataType.equals("number")) {
							Integer int1 = parseStringToInt(val);
							Integer int2 = parseStringToInt(validOfCol.get(i));
							if (int2 < int1) {
								flgAdd = true;
							}
						}
						break;
					case "!=":
						if (!val.equals(validOfCol.get(i))) {
							flgAdd = true;
						}
						break;
					default:
						System.out.println("Not have other condition!");
						assert(false);
					}
					
					if (flgAdd) {
						// Table columnName, value, index
						NodeColumn innerNode = new NodeColumn(nextCond.value, validOfCol.get(i), index + 1);
						parentMap.put(innerNode, curNode);
						toExploder.add(innerNode);
					}
				}
			} // end while
			
			// Find valid value path
			// Next to insert to data table.
			// Flag true all Set<condtion> include this column! 
			// Will not execute again this link mapping.
			List<NodeColumn> pathValidValue = findPathValidForMapping(parentMap, nodeGoal);
			i = 0;
			for (; i < pathValidValue.size(); ++i) {
				lastEndValidValue.put(pathValidValue.get(i).tableColumnName, pathValidValue.get(i).val);
				visitedMapping.add(pathValidValue.get(i).tableColumnName);
			}
		}
	}
	
	/**
	 * @param List<Boolean> flgCheck
	 * @return true when all boolean = true, otherwiser return false
	 */
	private boolean checkFlgMapping(List<Boolean> flgCheck) {
		if (flgCheck.isEmpty()) {
			return false;
		}
		for (int i = 0; i < flgCheck.size(); ++i) {
			if (!flgCheck.get(i)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param 1 value start with operator >=
	 * @param 2 value end operator <=
	 * @param 3 datatype of column
	 * @param len of value
	 * @return List key
	 */
	public List<String> genAutoKey(String valGreater, String valLess, String dataType, int len) {
		List<String> res = new ArrayList<>();
		
		boolean hasLess = !valLess.isEmpty();
		
		// Calculator increase or decrease?
		boolean isIncrease = false;
		if (valGreater.isEmpty() && valLess.isEmpty()) {
			isIncrease = true;
		} else if (!valGreater.isEmpty()) {
			isIncrease = true;
		}
		String curVal = isIncrease ? valGreater.isEmpty() ? genKeyWithLen(dataType, len) : valGreater : valLess;
		
		int limit = 1000000;
		for (int i = 1; i <= limit; ++i) {
			res.add(curVal);
			String newVal = "";
			if (dataType.equals("date")) {
				newVal = genKeyWithTypeDate(isIncrease, curVal);
				if (hasLess && isIncrease) {
					Date t1 = parseStringToDate(valLess);
					Date t2 = parseStringToDate(newVal);
					
					// New value large than limit value
					if (t2.compareTo(t1) > 0) {
						break;
					}
				}
			} else if (dataType.equals("number")) {
				newVal = genKeyWithTypeNumber(isIncrease, curVal);
				// When greater len stop!
				if (newVal.length() > len) {
					break;
				}
				
				if (hasLess && isIncrease) {
					Integer t1 = parseStringToInt(valLess);
					Integer t2 = parseStringToInt(newVal);
					
					// New value large than limit value
					if (t2 > t1) {
						break;
					}
				}
			} else if (dataType.equals("char")) {
				newVal = genKeyWithTypeChar(isIncrease, curVal);
				// When greater len stop!
				if (newVal.length() > len) {
					break;
				}
			}
			curVal = newVal;
		}
		return res;
	}
	
	/**
	 * Just confirm in normal character [26 character]
	 * @param type (++, --)
	 * @param curVal current value
	 * @return String new value after (++, --)
	 */
	public String genKeyWithTypeChar(boolean isIncrease, String curVal) {
		// Init character
		char[] chr = new char[26];
		for (int i = 0; i < 26; ++i) {
			chr[i] = (char) ('a' + i); 
		}
		
		char[] curChar = curVal.toCharArray();
		
		// Increase
		// 'abcde' -> 'abcdf'
		if (isIncrease) {
			int remain = 0;
			for (int i = curChar.length - 1; i >= 0; --i) {
				char c = curChar[i];
				if (c == 'z') {
					remain = 1;
				} else {
					remain = 0;	
					curChar[i] = (char) (c + 1);
				}
				
				if (remain == 0) {
					return curChar.toString();
				}
			}
		}
		
		// Decrease
		// 'abcdf' -> 'abcde'
		int remain = 0;
		for (int i = curChar.length - 1; i >= 0; --i) {
			char c = curChar[i];
			if (c == 'a') {
				remain = 1;
			} else {
				remain = 0;	
				curChar[i] = (char) (c - 1);
			}
			
			if (remain == 0) {
				break;
			}
		}
		
		return curChar.toString();
	}
	
	/**
	 * @param type (++, --)
	 * @param curVal current value
	 * @return String new value after (++, --)
	 */
	public String genKeyWithTypeDate(boolean isIncrease, String curVal) {
		Date curD;
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			curD = sdf.parse(curVal);
			c.setTime(curD);
		} catch(ParseException e) {
			e.printStackTrace();
		}
		
		if (isIncrease) {
			c.add(Calendar.DATE, 1);
		} else {
			// Decrease
			c.add(Calendar.DATE, -1);
		}
		curD = c.getTime();
		
		return sdf.format(curD);
	}
	
	/**
	 * @param type (++, --)
	 * @param curVal current value
	 * @return String new value after (++, --)
	 */
	public String genKeyWithTypeNumber(boolean isIncrease, String curVal) {
		String val = "";
		try {
			Integer i = Integer.parseInt(curVal);
			if (isIncrease) {
				i++;
			} else {
				i--;
			}
			val = String.valueOf(i);
		} catch (NumberFormatException e) {
			System.out.println("Format number error!");
		}
		
		return val;
	}
	
	/**
	 * 
	 * @param curValValid
	 * @param dataType
	 * @param conditionInWhere
	 * @param valInValid value in valid of this column
	 */
	public List<String> calculatorValidValWithColumnCondition(List<String> curValValid, String dataType,
			List<Cond> conditionInWhere, List<String> valInValid) {
		List<String> res = new ArrayList<>();
		
		// Check condition mapping
		for (int i = 0; i < conditionInWhere.size(); ++i) {
			String operator = conditionInWhere.get(i).operator;
			String value = conditionInWhere.get(i).value;
			
			for (int j = 0; j < curValValid.size(); ++j) {
				String curValue = curValValid.get(i);
				boolean flg = false;
				switch (operator) {
				case "=":
					if (value.equals(curValValid.get(j))) {
						flg = true;
					}
					break;
				case "<=":
					if (dataType.equals("number")) {
						Integer curI = Integer.parseInt(value);
						Integer innerI = parseStringToInt(curValue);
						if (innerI <= curI) {
							flg = true;
						}
					} else if (dataType.equals("date")) {
						Date curD = parseStringToDate(value);
						Date innerD = parseStringToDate(curValue);
						if (innerD.compareTo(curD) <= 0) {
							flg = true;
						}
					}
					break;
				case ">=":
					if (dataType.equals("number")) {
						Integer curI = Integer.parseInt(value);
						Integer innerI = parseStringToInt(curValue);
						if (innerI >= curI) {
							flg = true;
						}
					} else if (dataType.equals("date")) {
						Date curD = parseStringToDate(value);
						Date innerD = parseStringToDate(curValue);
						if (innerD.compareTo(curD) >= 0) {
							flg = true;
						}
					}
					break;
				default:
					System.out.println("Not other condition in there!");
					assert(false);
					break;
				}
				
				// Current value not in InValid value
				if (valInValid != null && valInValid.contains(curValue)) {
					flg = false;
				}
					
				if (flg) {
					res.add(curValue);
				}
			}
			
			curValValid = res;
			res.clear();
		}
		
		// Remove all invalid value
		if (valInValid != null) {
			Queue<String> toExploder = new LinkedList<>();
			for (int i = 0; i < curValValid.size(); ++i) {
				toExploder.add(curValValid.get(i));
			}
			res.clear();
			while (!toExploder.isEmpty()) {
				String curVal = toExploder.poll();
				if (!valInValid.contains(curVal)) {
					res.add(curVal);
				}
			}
			curValValid = res;
		}
		return curValValid;
	}
	
	
	/**
	 * Parse string to date with format yyyy-MM-dd
	 * @return date formated
	 */
	public Date parseStringToDate(String origin) {
		Date curDate = new Date();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			curDate = sdf.parse(origin);
		} catch (ParseException e) {
			System.out.println("Parse string to date error!");
		}
		return curDate;
	}
	
	public Integer parseStringToInt(String origin) {
		Integer res = 0;
		try {
			res = Integer.parseInt(origin);
		} catch (NumberFormatException e) {
			System.out.println("Parse string to number error!");
		}
		return res;
	}
	
	/**
	 * Find path valid in parentMap from node goal.
	 * @param parentMap
	 * @param nodeGoal
	 * @return
	 */
	public List<NodeColumn> findPathValidForMapping(Map<NodeColumn, NodeColumn> parentMap, 
			NodeColumn nodeGoal) {
		List<NodeColumn> res = new ArrayList<>();
		NodeColumn curNode = nodeGoal;
		while (curNode != null) {
			res.add(curNode);
			curNode = parentMap.get(curNode);
		}
		return res;
	}
	
	/**
	 * Genarate key with len
	 * @param len
	 * @return one value with this len
	 */
	private String genKeyWithLen(String dataType, int len) {
		if (dataType.equals("date")) {
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
			LocalDateTime now = LocalDateTime.now();
			// TODO
			// more?
			return "2000-10-10";  
		}
		
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < len; ++i) {
			if (dataType.equals("number")) {
				res.append("1");
			} else if (dataType.equals("char")) {
				res.append("A");
			}
		}
		return res.toString();
	}
	
	/**
	 * Remove all specify character in string origin
	 * 
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
