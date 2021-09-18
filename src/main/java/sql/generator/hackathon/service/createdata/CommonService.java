package sql.generator.hackathon.service.createdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.Condition;
import sql.generator.hackathon.model.ObjectCommonCreate;
import sql.generator.hackathon.model.ObjectGenate;
import sql.generator.hackathon.model.ParseObject;
import sql.generator.hackathon.model.TableSQL;
import sql.generator.hackathon.model.createdata.constant.Constant;

public class CommonService {

	private static ObjectCommonCreate objCommon;

	public static void init(ObjectGenate objectGenate, ParseObject parseObject) throws Exception {
		objCommon = new ObjectCommonCreate();
		String typeConnection = objectGenate.getInfoDatabase().getType();
		objCommon.setObjectGenate(objectGenate);
		objCommon.setListTableName(getListTableName(parseObject.getListTableSQL()));
		
		Map<String, List<ColumnInfo>> tableInfo;
		if (typeConnection.equals(Constant.NO_CONNECTION)) {
			tableInfo = getInfoTableWithoutConnect(parseObject.getListTableSQL());
		} else {
			tableInfo = ServiceCreateData.dbService
					.getInforTable(objCommon.getObjectGenate().getInfoDatabase().getSchema(), 
							objCommon.getListTableName());
		}
		objCommon.setTableInfo(tableInfo);
	}
	
	public static ColumnInfo getColumnInfo(String tableName, String columnName) {
		List<ColumnInfo> columnsInfo = objCommon.getTableInfo().get(tableName);
		List<ColumnInfo> res = columnsInfo.stream().filter(x -> x.getName().equals(columnName))
								.collect(Collectors.toList());
		
		if (res.size() != 1) {
			throw new IllegalStateException();
		}
		return res.get(0);
	}
	
	
	/**
	 * Get table info for NoConnection
	 * @param tables
	 * @return
	 */
	private static Map<String, List<ColumnInfo>> getInfoTableWithoutConnect(List<TableSQL> tables) {
		Map<String, List<ColumnInfo>> res = new HashMap<>();
		for (TableSQL table : tables) {
			List<ColumnInfo> listColInfo = new ArrayList<>();
			Set<String> listColumn = new HashSet<>();
			for (Condition condition : table.getCondition()) {
				String[] tableColName = getArrInCondition(condition.getLeft());
				if (listColumn.contains(tableColName[1])) {
					continue;
				}
				listColumn.add(tableColName[1]);
				ColumnInfo colInfo = new ColumnInfo(tableColName[1], "", Constant.STR_TYPE_CHAR, String.valueOf(Constant.DEFAULT_LENGTH_TYPE_CHAR));
				listColInfo.add(colInfo);
			}
			if (res.containsKey(table.getTableName())) {
				List<ColumnInfo> columnsCanAdd = new ArrayList<>();
				List<ColumnInfo> currentColumns = res.get(table.getTableName());
				for (ColumnInfo c : listColInfo) {
					boolean flg = true;
					for (ColumnInfo current : currentColumns) {
						if (current.getName().equals(c.getName())) {
							flg = false;
							break;
						}
					}
					if (flg) {
						columnsCanAdd.add(c);
					}
				}
				for (ColumnInfo c : columnsCanAdd) {
					currentColumns.add(c);
				}
			} else {
				res.put(table.getTableName(), listColInfo);
			}
		}
		return res;
	}
	
	/**
	 * Get table name and column name
	 * 
	 * @param column table.colName || colName
	 * @return String[2], String[0] = tableName, String[1] = columnName
	 */
	public static String[] getArrInCondition(String input) {
		String[] res = new String[2];
		if (input.indexOf(Constant.STR_DOT) != -1) {
			res = input.split("\\" + Constant.STR_DOT);
		} else {
			res[1] = input;
		}
		return res;
	}
	
	
	/**
	 * Process Gen value
	 */
	public static List<String> processGenValue(String dataType, int len, String valGreater, String valLess) {
		List<String> res = new ArrayList<>();
		
		boolean hasLess = !valLess.isEmpty();
		
		// Calculator increase or decrease?
		boolean isIncrement = false;
		if (valGreater.isEmpty() && valLess.isEmpty()) {
			isIncrement = true;
		} else if (!valGreater.isEmpty()) {
			isIncrement = true;
		}
		String curVal = isIncrement ? valGreater.isEmpty() 
									? CommonService.processGenValueWithLength(dataType, len) 
									: valGreater : valLess;
		
		for (int i = 1; i <= Constant.LIMIT_GEN_VALUE; ++i) {
			res.add(curVal);
			String newVal = "";
			if (dataType.equals(Constant.STR_TYPE_DATE)) {
				newVal = CommonService.processGenValueTypeDate(isIncrement, curVal);
				if (hasLess && isIncrement) {
					Date t1 = CommonService.convertStringToDate(valLess);
					Date t2 = CommonService.convertStringToDate(newVal);
					
					// New value large than limit value
					if (t2.compareTo(t1) > 0) {
						break;
					}
				}
			} else if (dataType.equals(Constant.STR_TYPE_NUMBER)) {
				newVal = CommonService.processGenValueTypeNumber(isIncrement, curVal);
				
				Integer t2 = CommonService.convertStringToInt(newVal);
				
				// When greater len stop!
				if (newVal.length() > len || t2 < 0) {
					break;
				}
				
				if (hasLess && isIncrement) {
					Integer t1 = CommonService.convertStringToInt(valLess);
					
					// New value large than limit value
					if (t2 > t1) {
						break;
					}
				}
			} else if (dataType.equals(Constant.STR_TYPE_CHAR)) {
				newVal = CommonService.processGenValueTypeChar(isIncrement, curVal);
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
	 * All data type will to 3 dataType below
	 * number - char - date
	 * @param dataType
	 * @return
	 */
	public static String getCommonDataType(String dataType) {
		String res = "";
		switch(dataType) {
		case "number":
		case "int":
		case "bigint":
			res = "number";
			break;
		case "char":
		case "nchar":
		case "varchar":
		case "nvarchar":
			res = "char";
			break;
		case "date":
			res = "date";
			break;
		default:
			res = "unknow";
		}
		return res;
	}
	
	/**
	 * Convert String to int
	 * When Numberformatexception will get default length
	 * @param val
	 * @return
	 */
	public static int convertLength(String val) {
		int len;
		try {
			len = Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return Constant.DEFAULT_LENGTH;
		}
		return len;
	}
	
	/**
	 * Convert string to date
	 * Format will get from input value
	 */
	@SuppressWarnings("finally")
	public static Date convertStringToDate(String input) {
		Date res = new Date();
		try {
			String format = readFormatDate(input);
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			res = sdf.parse(input);
		} catch (java.text.ParseException e) {
			return res;
		} finally {
			// Get current date
			SimpleDateFormat sdf = new SimpleDateFormat(Constant.DEFAULT_FORMAT_DATE);
			Date date = new Date();
			try {
				return sdf.parse(sdf.format(date));
			} catch (java.text.ParseException e) {
				return res;
			}
		}
	}

	/**
	 * Convert Date to string 
	 * Format will get from input value
	 */
	public static String convertDateToString(String format, Date input) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(input);
	}
	
	/**
	 * Convert string to int
	 */
	public static int convertStringToInt(String input) {
		int res;
		try {
			res = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return Constant.DEFAULT_LENGTH;
		}
		return res;
	}
	
	
	/**
	 * Read format date from input string
	 * @param input
	 * @return
	 */
	public static String readFormatDate(String input) {
		// TODO
		return Constant.DEFAULT_FORMAT_DATE;
	}

	public static String[] StringToArrWithRegex(String regex, String input) {
		return input.split(regex);
	}
	
	/**
	 * @param dataType
	 * @param len
	 * @return
	 */
	public static String processGenValueWithLength(String dataType, int len) {
		if (dataType.equals(Constant.STR_TYPE_DATE)) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constant.DEFAULT_FORMAT_DATE);  
			LocalDateTime now = LocalDateTime.now();  
			return dtf.format(now);  
		}
		
		StringBuilder res = new StringBuilder();
		if (dataType.equals(Constant.STR_TYPE_NUMBER)) {
			res.append(Constant.DEFAULT_NUMBER);
		} else if (dataType.equals(Constant.STR_TYPE_CHAR)) {
			res.append(Constant.DEFAULT_CHAR);
		}
		return res.toString();
	}
	
	/**
	 * @param type (++, --)
	 * @param curVal current value
	 * @return String new value after (++, --)
	 */
	public static String processGenValueTypeDate(boolean isIncrease, String curVal) {
		Date curD;
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(Constant.DEFAULT_FORMAT_DATE);
		try {
			curD = sdf.parse(curVal);
			c.setTime(curD);
		} catch(ParseException e) {
		} finally {
			
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
	public static String processGenValueTypeNumber(boolean isIncrease, String curVal) {
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
			return String.valueOf(Constant.DEFAULT_NUMBER);
		}
		return val;
	}
	
	/**
	 * Just confirm in normal character [26 character]
	 * @param type (++, --)
	 * @param curVal current value
	 * @return String new value after (++, --)
	 */
	public static String processGenValueTypeChar(boolean isIncrement, String curVal) {
		char[] chr = new char[26];
		for (int i = 0; i < 26; ++i) {
			chr[i] = (char) (Constant.DEFAULT_CHAR + i); 
		}
		
		if (curVal == null || curVal.isEmpty()) {
			curVal = String.valueOf(Constant.DEFAULT_CHAR);
		}
		
		char[] curChar = curVal.toCharArray();
		
		String res;
		if (isIncrement) {
			res = processGenValueTypeCharIncrement(curChar);
		} else {
			res = processGenValueTypeCharDecrement(curChar);
		}
		return res;
	}
	
	/**
	 * Increase
	 * 'z' -> 'aa'
	 * @param curChar
	 */
	private static String processGenValueTypeCharIncrement(char[] curChar) {
		int remain = 0;
		int length = curChar.length;
		for (int i = length - 1; i >= 0; --i) {
			char c = curChar[i];
			if (c == Constant.CHAR_Z) {
				remain = 1;
				if (i == 0) {
					return repeat(Constant.DEFAULT_CHAR, length + 1);
				}
			} else {
				remain = 0;	
				curChar[i] = (char) (c + 1);
			}
			
			if (remain == 0) {
				break;
			}
		}
		return String.valueOf(curChar);
	}
	
	/**
	 * Decrease
	 * 'abcdf' -> 'abcde'
	 * @param curChar
	 * @return
	 */
	private static String processGenValueTypeCharDecrement(char[] curChar) {
		int remain = 0;
		int length = curChar.length;
		for (int i = length - 1; i >= 0; --i) {
			char c = curChar[i];
			if (c == Constant.DEFAULT_CHAR) {
				remain = 1;
				if (i == 0) {
					return repeat(Constant.CHAR_Z, length - 1);
				}
			} else {
				remain = 0;	
				curChar[i] = (char) (c - 1);
			}
			if (remain == 0) {
				break;
			}
		}
		return String.valueOf(curChar);
	}
	
	/**
	 * Repeat character
	 * @param character need repeat
	 * @param length need repeat
	 * @return String repeated
	 */
	private static String repeat(char c, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; ++i) {
			sb.append("" + c);
		}
		return sb.toString();
	}
	
	/**
	 * Get list tableName
	 * @param tables
	 * @return
	 */
	private static List<String> getListTableName(List<TableSQL> tables) {
		Set<String> res = tables.stream().map(x -> x.tableName).collect(Collectors.toSet());
		return res.stream().collect(Collectors.toList());
	}
}
