package sql.generator.hackathon.service.createdata.execute;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.ObjectMappingTable;
import sql.generator.hackathon.model.createdata.ColumnCondition;
import sql.generator.hackathon.model.createdata.constant.Constant;
import sql.generator.hackathon.service.ExecuteDBSQLServer;
import sql.generator.hackathon.service.createdata.CommonService;

public class ExecExpressionService {

	@Autowired
	private ExecInAndNotInService execInAndNotInService;
	
	@Autowired
	private ExecLikeService execLikeService;
	
	@Autowired
	private ExecuteDBSQLServer dbService;
	
	/**
	 * Get last values from list conditions for each column
	 * @param conditions (Key -> tablesName-aliasName-colName)
	 * @return
	 */
	public Map<String, List<String>> calcLastValue(Map<String, List<ObjectMappingTable>> mappingTables) throws SQLException{
		HashMap<String, List<String>> res = new HashMap<>();
		mappingTables.entrySet().forEach(x -> {
			String tableAliasName = x.getKey();
			String[] tableAliasNameArr = CommonService.StringToArrWithRegex(Constant.STR_LINK, tableAliasName);
			x.getValue().stream().forEach(y -> {
				List<ColumnCondition> conditions = y.getColumnsCondition();
				String columnName = y.getColumnName();
				
				String tableAliasColumnName = tableAliasName + Constant.STR_LINK + columnName;
				ColumnInfo columnInfo = CommonService.getColumnInfo(tableAliasNameArr[0], columnName);
				String dataType = CommonService.getCommonDataType(columnInfo.getTypeName());
				int length = CommonService.convertLength(columnInfo.getTypeValue());
				try {
					res.put(tableAliasColumnName, processCalcValue(conditions, tableAliasNameArr[0], columnInfo, dataType, length));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		});
		return res;
	}
	
	/**
	 * Define compare priority for operators
	 * @param conditions
	 * @return
	 */
	public Comparator<ColumnCondition> processComparatorPriority() {
		Comparator<ColumnCondition> comparator = new Comparator<ColumnCondition>() {
			@Override
			public int compare(ColumnCondition o1, ColumnCondition o2) {
				return Constant.priorityOperators.get(o1.getExpression()) - Constant.priorityOperators.get(o2.getExpression());
			}
		};
		return comparator;
	}
	
	
	/**
	 * Calculator last value from list condition for 1 column
	 * @param conditions
	 * @param dataType
	 * @param length
	 * @return
	 * @throws SQLException 
	 */
	private List<String> processCalcValue(List<ColumnCondition> conditions, String tableName,
			ColumnInfo columnInfo, String dataType, int length) throws SQLException {
		List<String> lastValue = new ArrayList<>();
		
		List<ColumnCondition> conditionCompare = new ArrayList<>();
		List<String> valuesInValid = new ArrayList<>();
		
		boolean flgEquals = false;
		boolean flgIn = false;
		for(ColumnCondition x : conditions) {
			String expression = x.getExpression();
			List<String> values = x.getValues();
			switch (expression) {
			case Constant.EXPRESSION_EQUALS:
				flgEquals = true;
				break;
			case Constant.EXPRESSION_IN:
				flgIn = true;
				lastValue.addAll(execInAndNotInService.processExpressionIn(lastValue, values));
				break;
			case Constant.EXPRESSION_LIKE:
				if (!flgIn) {
					lastValue.addAll(execLikeService.processLike(values.get(0)));	
				}
				break;
			case Constant.EXPRESSION_GREATER_EQUALS: // Just use dataType number, date
				if (checkOtherChar(dataType)) {
					conditionCompare.add(x);
				}
				break;
			case Constant.EXPRESSION_LESS_EQUALS: // Just use dataType number, date
				if (checkOtherChar(dataType)) {
					conditionCompare.add(x);
				}
				break;
			case Constant.EXPRESSION_GREATER: // Just use dataType number, date
				if (checkOtherChar(dataType)) {
					ColumnCondition t = processExpressionCompare(dataType, length, expression, values.get(0));
					conditionCompare.add(t);
				}
				break;
			case Constant.EXPRESSION_LESS: // Just use dataType number, date
				if (checkOtherChar(dataType)) {
					ColumnCondition t = processExpressionCompare(dataType, length, expression, values.get(0));
					conditionCompare.add(t);
				}
				break;
			case Constant.EXPRESSION_NOT_IN:
				valuesInValid.addAll(values);
				break;
			case Constant.EXPRESSION_DIFF_1:
			case Constant.EXPRESSION_DIFF_2:
				valuesInValid.add(values.get(0));
				break;
			default:
				// Other expression?
				break;
			}
			
			// Stop when use expression equals
			if (flgEquals) {
				lastValue.add(x.getValues().get(0));
				break;
			}
		}
		
		
		// Excute calculator compare expression
		if (!conditionCompare.isEmpty()) {
			List<String> valuesCompare = calcCompareExpression(conditionCompare, tableName, columnInfo, dataType, length);
			lastValue = processCalcLastValueWithValuesCompare(lastValue, valuesInValid, valuesCompare);
		}
		
		// Execute remove value invalid
		if (!valuesInValid.isEmpty()) {
			lastValue = execInAndNotInService.processNotIn(lastValue, valuesInValid);
		}
		
		return lastValue;
	}
	
	/**
	 * Confirm execute with dataType is number or date
	 * @param dataType
	 * @return
	 */
	private boolean checkOtherChar(String dataType) {
		return dataType.equals("number") || dataType.equals("date");
	}
	
	/**
	 * Execute calculator for compare expression
	 * @throws SQLException 
	 */
	private List<String> calcCompareExpression(List<ColumnCondition> conditionsCompare,
			String tableName, ColumnInfo columnInfo, String dataType, int length) throws SQLException {
		List<String> res = new ArrayList<>();
		boolean isKey = columnInfo.isKey();
		String valLess = "";
		String valGreater = "";
		int len = conditionsCompare.size();
		int cnt = 0;
		for (int i = 0; i < len; ++i) {
			ColumnCondition x = conditionsCompare.get(i);
			String expression = x.getExpression();
			String value = x.getValues().get(0);
			switch (expression) {
			case Constant.EXPRESSION_LESS_EQUALS:
				if (valLess.isEmpty()) {
					valLess = value;
				}
				break;
			case Constant.EXPRESSION_GREATER_EQUALS:
				if (valGreater.isEmpty()) {
					valGreater = value;
				}
				break;
			}
			
			cnt++;			
			if (cnt == 2) {
				ColumnCondition prevC = conditionsCompare.get(i - 1);
				if (expression.equals(Constant.EXPRESSION_LESS_EQUALS)) {
					if (prevC.getExpression().equals(Constant.EXPRESSION_LESS_EQUALS)) {
						valLess = prevC.getValues().get(0);
						cnt = 1;
						valGreater = "";
					} else {
						// Valid
						if (isKey) {
							res.addAll(dbService.genListUniqueVal(tableName, columnInfo, valGreater, valLess));
						} else {
							res.addAll(CommonService.processGenValue(dataType, length, valGreater, valLess));
						}
						valLess = "";
						valGreater = "";
						cnt = 0;
					}
				} else {
					if (prevC.getExpression().equals(Constant.EXPRESSION_LESS_EQUALS)) {
						if (isKey) {
							res.addAll(dbService.genListUniqueVal(tableName, columnInfo, valGreater, ""));
							res.addAll(dbService.genListUniqueVal(tableName, columnInfo, "", valLess));
						} else {
							res.addAll(CommonService.processGenValue(dataType, length, valGreater, ""));
							res.addAll(CommonService.processGenValue(dataType, length, "", valLess));
						}
						valLess = "";
						valGreater = "";
						cnt = 0;
					} else {
						valLess = "";
						valGreater = prevC.getValues().get(0);
						cnt = 1;
					}
				}
			}
		}
		
		// Remain
		if (len % 2 == 1) {
			if (isKey) {
				res.addAll(dbService.genListUniqueVal(tableName, columnInfo, valGreater, valLess));
			} else {
				res.addAll(CommonService.processGenValue(dataType, length, valGreater, valLess));
			}
		}
		return res;
	}
	
	/**
	 * Comparator use for compare expression (<, >, <=, >=)
	 * @param sizeConditions
	 * @param dataType
	 * @return
	 */
	private Comparator<ColumnCondition> comparatorForCompareExpress(int sizeConditions, String dataType) {
		return new Comparator<ColumnCondition>(){

			@Override
			public int compare(ColumnCondition o1, ColumnCondition o2) {
				String val1 = o1.getValues().get(0);
				String val2 = o2.getValues().get(0);
				if (dataType.equals("number")) {
					int x = Integer.parseInt(val1);
					int y = Integer.parseInt(val2);
					if (Integer.compare(x, y) == 0) {
						int priority1 = Constant.priorityOperators.get(o1.getExpression());
						int priority2 = Constant.priorityOperators.get(o2.getExpression());
						return sizeConditions % 2 == 0 ? priority2 - priority1 : priority1 - priority2;
					}
					return Integer.compare(x, y);
				} else if (dataType.equals("date")) {
					Date x = CommonService.convertStringToDate(val1);
					Date y = CommonService.convertStringToDate(val2);
					if (x.compareTo(y) < 0) {
						return -1;
					} else if (x.compareTo(y) > 0) {
						return 1;
					} else {
						int priority1 = Constant.priorityOperators.get(o1.getExpression());
						int priority2 = Constant.priorityOperators.get(o2.getExpression());
						return sizeConditions % 2 == 0 ? priority2 - priority1 : priority1 - priority2;
					}
				}
				return 0;
			}
		};
	}
	
	/**
	 * Process for expression compare <, >
	 * Rewrite to <=, >=
	 * @return ColumnCondition
	 */
	private ColumnCondition processExpressionCompare(String dataType, int length, String expression, String value) {
		ColumnCondition res = new ColumnCondition();
		String lastValue;
		boolean flgLess = false;
		boolean flgGreater = false;
		if (dataType.equals("number")) {
			int v = CommonService.convertStringToInt(value);
			if (expression.equals(Constant.EXPRESSION_GREATER)) {
				flgGreater = true;
				v += 1;
			} else {
				// expression.equals("<")
				flgLess = true;
				v -= 1;
			}
			lastValue = String.valueOf(v);
		} else {
			// Date
			Date v = CommonService.convertStringToDate(value);
			String format = CommonService.readFormatDate(value);
			Calendar c = Calendar.getInstance();
			c.setTime(v);
			if (expression.equals(Constant.EXPRESSION_GREATER)) {
				flgGreater = true;
				c.add(Calendar.DATE, 1);
			} else {
				// expression.equals("<")
				flgLess = true;
				c.add(Calendar.DATE, -1);
			}
			lastValue = CommonService.convertDateToString(format, c.getTime());
		}
		if (flgLess) {
			res.setExpression(Constant.EXPRESSION_LESS_EQUALS);
		} else if (flgGreater) {
			res.setExpression(Constant.EXPRESSION_GREATER_EQUALS);
		}
		res.setValues(new ArrayList<>(Arrays.asList(lastValue)));
		return res;
	}
	
	/**
	 * Calculator last values with compare expression
	 */
	private List<String> processCalcLastValueWithValuesCompare(List<String> lastValues, 
			List<String> valuesInValid, List<String> valuesCompare) {
		if (lastValues.isEmpty()) {
			return valuesCompare;
		}
		return lastValues.stream().flatMap(x -> valuesCompare.stream().filter(y -> x.equals(y))
				.filter(y -> !valuesInValid.contains(y))).collect(Collectors.toList());
	}
}
